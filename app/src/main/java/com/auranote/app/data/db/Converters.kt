package com.auranote.app.data.db

import androidx.room.TypeConverter
import com.auranote.app.data.model.RecordingType
import com.auranote.app.data.model.TranscriptionStatus

class Converters {

    @TypeConverter
    fun fromTranscriptionStatus(value: TranscriptionStatus): String = value.name

    @TypeConverter
    fun toTranscriptionStatus(value: String): TranscriptionStatus =
        TranscriptionStatus.valueOf(value)

    @TypeConverter
    fun fromRecordingType(value: RecordingType): String = value.name

    @TypeConverter
    fun toRecordingType(value: String): RecordingType = RecordingType.valueOf(value)
}
