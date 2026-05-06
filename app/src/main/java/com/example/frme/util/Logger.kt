package com.fibcam.util

import android.util.Log

/**
 * Centralized logging with structured error tracking.
 * Enables filtering and consistent log formatting across the app.
 */
object Logger {

    private const val PREFIX = "FibCam"

    enum class Level {
        DEBUG, INFO, WARNING, ERROR
    }

    fun d(tag: String, message: String) {
        Log.d("$PREFIX:$tag", message)
    }

    fun d(tag: String, message: String, throwable: Throwable) {
        Log.d("$PREFIX:$tag", message, throwable)
    }

    fun i(tag: String, message: String) {
        Log.i("$PREFIX:$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("$PREFIX:$tag", message)
    }

    fun w(tag: String, message: String, throwable: Throwable) {
        Log.w("$PREFIX:$tag", message, throwable)
    }

    fun e(tag: String, message: String) {
        Log.e("$PREFIX:$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e("$PREFIX:$tag", message, throwable)
    }

    fun logCapture(fileName: String, format: String, sizeBytes: Long) {
        i("CaptureManager", "Photo captured: $fileName ($format, ${sizeBytes / 1024}KB)")
    }

    fun logError(feature: String, exception: Exception) {
        e("ErrorHandler", "$feature failed: ${exception.message}", exception)
    }
}