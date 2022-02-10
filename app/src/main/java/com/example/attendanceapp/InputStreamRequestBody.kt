package com.example.attendanceapp

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.Nullable
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.IOException
import okio.source


class InputStreamRequestBody(contentType: MediaType?, contentResolver: ContentResolver, uri: Uri?) :
    RequestBody() {
    private val contentType: MediaType?
    private val contentResolver: ContentResolver
    private val uri: Uri?

    @Nullable
    override fun contentType(): MediaType? {
        return contentType
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        uri?.let { contentResolver.openInputStream(it)?.source() }?.let { sink.writeAll(it) }
    }

    init {
        if (uri == null) throw NullPointerException("uri == null")
        this.contentType = contentType
        this.contentResolver = contentResolver
        this.uri = uri
    }
}