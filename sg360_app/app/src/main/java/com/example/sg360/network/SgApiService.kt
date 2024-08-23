package com.example.sg360.network

import com.example.sg360.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.sql.Date


interface SgApiService {
        /**
         * Sends login request and returns login response
         */
        @POST("api/user/login/")
        suspend fun login( @Body loginRequest: LoginBod): LoginRes

        /**
         * Sends registration request and returns registration response
         */
        @POST("api/user/register/")
        suspend fun register( @Body registerRequest: RegisterBod): RegisterRes

        /**
         * Sends OTP verification request and returns registration response
         */
        @POST("api/user/otpverify/")
        suspend fun verify( @Body verifyRequest: VerifyBod) : RegisterRes

        /**
         * Sends request to check profile and returns nothing
         */
        @POST("api/user/profile/")
        suspend fun check(
                @Body checkBody: String,
                @Header("Authorization") headBody: String
        )

        @POST("api/user/app-data/")
        suspend fun sendAppData(
                @Body appData: AllAppsData
        ): ScanRes

        @POST("/federated/clientData/")
        suspend fun sendClientData(@Body clusterRequest: ClusterRequest): ClusterResponse

        @Headers("Content-Type: application/json")
        @POST("/federated/downloadModel/")
        fun downloadModel(@Body requestBody: Map<String, String>): Call<ResponseBody>
}
