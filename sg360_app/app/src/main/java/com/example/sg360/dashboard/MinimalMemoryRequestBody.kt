package com.example.sg360.dashboard

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import kotlin.math.min

/**
 * A custom [RequestBody] implementation for uploading file chunks with minimal memory usage.
 *
 * This class reads and streams a specific range of bytes from a file to minimize memory overhead.
 * It is designed for scenarios where large files need to be uploaded in chunks, such as multipart
 * file uploads. The file is read in small buffers to avoid loading the entire file into memory.
 *
 * @param file The file to upload.
 * @param contentType The MIME type of the file (e.g., "application/octet-stream").
 * @param startByte The starting byte position of the chunk to upload (default is 0).
 * @param endByte The ending byte position of the chunk to upload (default is the last byte of the file).
 * @param bufferSize The size of the buffer used for reading the file (default is 4KB).
 */
class MinimalMemoryRequestBody(
    private val file: File,
    private val contentType: String,
    private val startByte: Long = 0,
    private val endByte: Long = file.length() - 1,
    private val bufferSize: Int = 4096 // 4KB buffer
) : RequestBody() {

    /**
     * Returns the content type of the file being uploaded.
     *
     * This method converts the provided `contentType` string into a [MediaType] object.
     * If the content type is invalid, it returns `null`.
     *
     * @return The [MediaType] of the file or `null` if the content type is invalid.
     */
    override fun contentType() = contentType.toMediaTypeOrNull()

    /**
     * Returns the length of the file chunk being uploaded.
     *
     * This method calculates the size of the chunk based on the `startByte` and `endByte` parameters.
     *
     * @return The length of the file chunk in bytes.
     */
    override fun contentLength(): Long = endByte - startByte + 1

    /**
     * Writes the specified file chunk to the provided [BufferedSink].
     *
     * This method reads the file in small buffers to minimize memory usage. It ensures that only
     * the specified range of bytes (`startByte` to `endByte`) is uploaded. The file is read using
     * a [FileInputStream] and streamed directly to the sink.
     *
     * @param sink The [BufferedSink] to which the file chunk is written.
     */
    override fun writeTo(sink: BufferedSink) {
        FileInputStream(file).channel.use { channel ->
            // Set the file channel's position to the start of the chunk
            channel.position(startByte)

            // Create a buffer to read chunks of the file
            val buffer = ByteArray(bufferSize)
            var bytesRemaining = contentLength()

            // Read and write the file in small chunks until the entire range is processed
            while (bytesRemaining > 0) {
                // Determine the number of bytes to read in this iteration
                val bytesToRead = min(buffer.size.toLong(), bytesRemaining).toInt()
                val bytesRead = channel.read(ByteBuffer.wrap(buffer, 0, bytesToRead))

                // Break the loop if no more bytes are available
                if (bytesRead == -1) break

                // Write the read bytes to the sink and flush the data
                sink.write(buffer, 0, bytesRead)
                sink.flush()

                // Update the remaining bytes counter
                bytesRemaining -= bytesRead
            }
        }
    }
}