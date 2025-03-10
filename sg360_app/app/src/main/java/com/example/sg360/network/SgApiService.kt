package com.example.sg360.network

import com.example.sg360.models.AnalysisResult
import com.example.sg360.models.CheckRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit API service interface for SG360 app.
 *
 * This interface defines the endpoints for interacting with the backend server.
 * It includes methods for checking analysis status and uploading APK chunks.
 */
interface SgApiService {

        /**
         * Checks the analysis status of an app by sending a request body to the server.
         *
         * @param checkRequestBody The request body containing details about the app to check.
         * @return [AnalysisResult] representing the analysis status of the app.
         */
        @POST("/api/checkApp/")
        suspend fun checkAnalysisStatus(@Body checkRequestBody: CheckRequestBody): AnalysisResult

        /**
         * Uploads a chunk of an APK file to the server as part of a multipart upload process.
         *
         * This method is used to upload large APK files in smaller chunks, ensuring reliability
         * and resumability. Each chunk includes metadata such as the app name, package name,
         * chunk index, total chunks, file name, and whether it is the last chunk.
         *
         * @param chunk The chunk of the APK file being uploaded, wrapped in a [MultipartBody.Part].
         * @param appName The name of the app, sent as a form-data part.
         * @param packageName The package name of the app, sent as a form-data part.
         * @param chunkIndex The index of the current chunk, sent as a form-data part.
         * @param totalChunks The total number of chunks for the APK file, sent as a form-data part.
         * @param fileName The name of the APK file, sent as a form-data part.
         * @param isLastChunk Indicates whether this is the last chunk of the APK file.
         * @return [Response<AnalysisResult>] containing the server's response to the upload.
         */
        @Multipart
        @POST("/api/receiveApp/")
        suspend fun uploadApkChunk(
                @Part chunk: MultipartBody.Part, // The APK chunk to upload
                @Part("app_name") appName: RequestBody, // Name of the app
                @Part("package_name") packageName: RequestBody, // Package name of the app
                @Part("chunk_index") chunkIndex: RequestBody, // Index of the current chunk
                @Part("total_chunks") totalChunks: RequestBody, // Total number of chunks
                @Part("file_name") fileName: RequestBody, // Name of the APK file
                @Part("isLastChunk") isLastChunk: Boolean // Whether this is the last chunk
        ): Response<AnalysisResult>
}