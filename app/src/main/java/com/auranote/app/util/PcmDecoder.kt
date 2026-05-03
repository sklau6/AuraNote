package com.auranote.app.util

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

/**
 * Decodes any compressed audio file (m4a/aac/mp3/ogg/wav) into a raw
 * 16-bit signed PCM file that Android's on-device [SpeechRecognizer]
 * can ingest via `RecognizerIntent.EXTRA_AUDIO_SOURCE`.
 *
 * The output is forced to mono 16 kHz — what every Google on-device speech
 * model expects.
 */
object PcmDecoder {
    private const val TAG = "PcmDecoder"
    const val TARGET_SAMPLE_RATE = 16_000
    const val TARGET_CHANNELS = 1            // mono
    const val TARGET_BITS_PER_SAMPLE = 16

    data class Result(val pcmFile: File, val sampleRate: Int, val channels: Int)

    /**
     * Decode `source` into a temp PCM file in `outputDir`.
     * @throws IllegalStateException with a human-readable message on any failure.
     */
    fun decodeToPcm(source: File, outputDir: File): Result {
        require(source.exists()) { "Audio file does not exist: ${source.absolutePath}" }
        require(source.length() > 0) { "Audio file is empty" }
        outputDir.mkdirs()

        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        val pcmFile = File(outputDir, "decoded_${System.currentTimeMillis()}.pcm")

        try {
            extractor.setDataSource(source.absolutePath)
            val (audioTrack, srcFormat) = pickAudioTrack(extractor)
                ?: throw IllegalStateException("No audio track found in file")
            extractor.selectTrack(audioTrack)

            val mime = srcFormat.getString(MediaFormat.KEY_MIME)
                ?: throw IllegalStateException("Audio track has no MIME type")
            val srcSampleRate = srcFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val srcChannels = srcFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            codec = MediaCodec.createDecoderByType(mime).apply {
                configure(srcFormat, null, null, 0)
                start()
            }

            FileOutputStream(pcmFile).use { fos ->
                BufferedOutputStream(fos).use { bos ->
                    drainDecoder(extractor, codec, bos, srcSampleRate, srcChannels)
                }
            }

            if (pcmFile.length() == 0L) {
                throw IllegalStateException("Decoder produced 0 bytes (file too short or unsupported)")
            }
            return Result(pcmFile, TARGET_SAMPLE_RATE, TARGET_CHANNELS)
        } catch (t: Throwable) {
            // Best effort cleanup on failure.
            try { pcmFile.delete() } catch (_: Exception) {}
            throw IllegalStateException(
                "Audio decoding failed: ${t.message ?: t.javaClass.simpleName}", t
            )
        } finally {
            try { codec?.stop() } catch (_: Exception) {}
            try { codec?.release() } catch (_: Exception) {}
            try { extractor.release() } catch (_: Exception) {}
        }
    }

    private fun pickAudioTrack(extractor: MediaExtractor): Pair<Int, MediaFormat>? {
        for (i in 0 until extractor.trackCount) {
            val fmt = extractor.getTrackFormat(i)
            val mime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) return i to fmt
        }
        return null
    }

    private fun drainDecoder(
        extractor: MediaExtractor,
        codec: MediaCodec,
        out: BufferedOutputStream,
        srcSampleRate: Int,
        srcChannels: Int
    ) {
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false
        val timeoutUs = 10_000L

        while (!outputDone) {
            // Feed input.
            if (!inputDone) {
                val inputIndex = codec.dequeueInputBuffer(timeoutUs)
                if (inputIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputIndex)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(
                            inputIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(
                            inputIndex, 0, sampleSize, extractor.sampleTime, 0
                        )
                        extractor.advance()
                    }
                }
            }

            // Drain output.
            val outputIndex = codec.dequeueOutputBuffer(info, timeoutUs)
            if (outputIndex >= 0) {
                if (info.size > 0) {
                    val outBuf = codec.getOutputBuffer(outputIndex)!!
                    outBuf.position(info.offset)
                    outBuf.limit(info.offset + info.size)

                    val pcm = ByteArray(info.size)
                    outBuf.get(pcm)

                    val resampled = resamplePcm16(
                        pcm,
                        srcSampleRate, srcChannels,
                        TARGET_SAMPLE_RATE, TARGET_CHANNELS
                    )
                    out.write(resampled)
                }
                codec.releaseOutputBuffer(outputIndex, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    outputDone = true
                }
            }
        }
        out.flush()
    }

    /**
     * Cheap linear resampler + channel down-mix for 16-bit signed little-endian PCM.
     * Acceptable quality for speech recognition where the model already expects 16 kHz mono.
     */
    private fun resamplePcm16(
        srcBytes: ByteArray,
        srcRate: Int, srcChannels: Int,
        dstRate: Int, dstChannels: Int
    ): ByteArray {
        if (srcBytes.isEmpty()) return srcBytes

        // 1. Read source as short[] little-endian.
        val srcBuf = ByteBuffer.wrap(srcBytes).order(ByteOrder.LITTLE_ENDIAN)
        val srcShortCount = srcBytes.size / 2
        val srcShorts = ShortArray(srcShortCount)
        for (i in 0 until srcShortCount) srcShorts[i] = srcBuf.short

        // 2. Down-mix to mono if needed.
        val monoShorts: ShortArray = if (srcChannels == 1) srcShorts else {
            val frames = srcShortCount / srcChannels
            ShortArray(frames) { i ->
                var sum = 0
                for (c in 0 until srcChannels) sum += srcShorts[i * srcChannels + c]
                (sum / srcChannels).toShort()
            }
        }
        val monoFrames = monoShorts.size

        // 3. Resample if rates differ (linear interpolation).
        val outShorts: ShortArray = if (srcRate == dstRate) monoShorts else {
            val ratio = dstRate.toDouble() / srcRate
            val outLen = (monoFrames * ratio).toInt()
            ShortArray(outLen) { i ->
                val srcIdx = i / ratio
                val i0 = srcIdx.toInt().coerceIn(0, monoFrames - 1)
                val i1 = min(i0 + 1, monoFrames - 1)
                val frac = srcIdx - i0
                val s = monoShorts[i0] * (1 - frac) + monoShorts[i1] * frac
                s.toInt().toShort()
            }
        }

        // 4. Up-mix to N channels if requested (rare — we always pass dstChannels=1).
        val finalShorts: ShortArray = if (dstChannels == 1) outShorts else {
            ShortArray(outShorts.size * dstChannels) { i ->
                outShorts[i / dstChannels]
            }
        }

        // 5. Write back as little-endian bytes.
        val outBytes = ByteArray(finalShorts.size * 2)
        val outBuf = ByteBuffer.wrap(outBytes).order(ByteOrder.LITTLE_ENDIAN)
        for (s in finalShorts) outBuf.putShort(s)
        return outBytes
    }

    fun safeLog(msg: String) { Log.d(TAG, msg) }
}
