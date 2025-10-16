package com.example.shieldx.api

import com.example.shieldx.data.AnalysisResponse
import com.example.shieldx.data.NotificationPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ShieldXApiService {
    @POST("/api/v1/mobile/analyze-notification")
    suspend fun analyzeNotification(
        @Header("Authorization") token: String,
        @Body payload: NotificationPayload
    ): Response<AnalysisResponse>
    
    @POST("/api/v1/mobile/analyze-notification")
    suspend fun analyzeNotificationWithoutAuth(
        @Body payload: NotificationPayload
    ): Response<AnalysisResponse>
}
