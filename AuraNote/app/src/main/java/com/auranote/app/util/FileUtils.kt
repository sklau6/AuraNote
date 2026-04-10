package com.auranote.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object FileUtils {

    fun getFileSize(path: String): Long = try {
        File(path).length()
    } catch (e: Exception) {
        0L
    }

    fun deleteFile(path: String): Boolean = try {
        File(path).delete()
    } catch (e: Exception) {
        false
    }

    fun fileExists(path: String): Boolean = File(path).exists()

    fun getUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

    fun shareText(context: Context, text: String, title: String = "Share Transcript") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareAudioFile(context: Context, filePath: String, title: String = "Share Recording") {
        val file = File(filePath)
        if (!file.exists()) return
        val uri = getUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun importedAudioDir(context: Context): File =
        File(context.filesDir, "imported").apply { mkdirs() }

    fun recordingsDir(context: Context): File =
        File(context.filesDir, "recordings").apply { mkdirs() }

    fun copyUriToFile(context: Context, uri: Uri, destFile: File): Boolean = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}
