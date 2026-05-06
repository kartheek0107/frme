package com.fibcam.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilities for file storage, naming, and MediaStore integration.
 */
object FileUtil {

    private const val FILENAME_FORMAT = "yyyy-MM-dd_HH-mm-ss-SSS"
    private val timeStamp: String
        get() = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())

    /**
     * Create ContentValues for saving a photo to MediaStore.
     * Handles both legacy (<Q) and modern (>=Q) storage patterns.
     */
    fun createImageContentValues(displayName: String = "fibcam_$timeStamp"): ContentValues {
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/FibCam")
            }
        }
    }

    /**
     * Create ContentValues for saving a RAW image.
     */
    fun createRawContentValues(displayName: String = "fibcam_raw_$timeStamp"): ContentValues {
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/x-raw")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/FibCam/RAW")
            }
        }
    }

    /**
     * Create ContentValues for saving video.
     */
    fun createVideoContentValues(displayName: String = "fibcam_video_$timeStamp"): ContentValues {
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/FibCam/Videos")
            }
        }
    }
}