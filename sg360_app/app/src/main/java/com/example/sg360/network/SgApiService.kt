package com.example.sg360.network

import com.example.sg360.models.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SgApiService {
        /**
         * Sends login request and returns login response
         */
        @POST("api/user/login/")
        suspend fun login( @Body loginRequest: loginbod): loginres

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
         * Sends project data and returns prediction response
         */
        @POST("api/user/project-data/")
        suspend fun sendData(
                @Body dataRequest: StatsData,
                @Header("Authorization") headerBody: String
        ) : PredictResponse

        /**
         * Sends request to check profile and returns nothing
         */
        @POST("api/user/profile/")
        suspend fun check(
                @Body checkBody: String,
                @Header("Authorization") headBody: String
        )
}
