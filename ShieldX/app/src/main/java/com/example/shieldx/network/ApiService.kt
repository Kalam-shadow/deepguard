package com.example.shieldx.network

import com.example.shieldx.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * DeepGuard v3.0 - API Service Interface
 * Complete REST API interface for all backend endpoints
 */
interface ApiService {
    
    // ================================
    // Authentication Endpoints
    // ================================
    
    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<ApiResponse<LoginResponse>>
    
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<ApiResponse<LoginResponse>>
    
    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<ApiResponse<User>>
    
    @PUT("api/v1/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body user: User
    ): Response<ApiResponse<User>>
    
    // ================================
    // Scanning Endpoints
    // ================================
    
    @POST("api/v1/scan_text")
    suspend fun scanText(
        @Header("Authorization") token: String,
        @Body request: ScanRequest
    ): Response<ApiResponse<ScanResult>>
    
    @Multipart
    @POST("api/v1/scan_media")
    suspend fun scanMedia(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("scan_type") scanType: RequestBody
    ): Response<ApiResponse<ScanResult>>
    
    @Multipart
    @POST("api/v1/scan_deepfake")
    suspend fun scanDeepfake(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<ScanResult>>
    
    @POST("api/v1/deepscan")
    suspend fun startDeepScan(
        @Header("Authorization") token: String,
        @Body scanRequest: ScanRequest
    ): Response<ApiResponse<ScanResult>>
    
    // ================================
    // User Management Endpoints
    // ================================
    
    @GET("api/v1/user/scans")
    suspend fun getUserScans(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<ScanResult>>>
    
    @GET("api/v1/user/stats")
    suspend fun getUserStats(
        @Header("Authorization") token: String,
        @Query("period") period: String = "week" // "day", "week", "month"
    ): Response<ApiResponse<StatsResponse>>
    
    @DELETE("api/v1/user/scan/{scanId}")
    suspend fun deleteScan(
        @Header("Authorization") token: String,
        @Path("scanId") scanId: String
    ): Response<ApiResponse<String>>
    
    // ================================
    // File Management Endpoints
    // ================================
    
    @Multipart
    @POST("api/v1/upload")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("file_type") fileType: RequestBody
    ): Response<ApiResponse<FileUploadResponse>>
    
    @GET("api/v1/download/{fileId}")
    suspend fun downloadFile(
        @Header("Authorization") token: String,
        @Path("fileId") fileId: String
    ): Response<okhttp3.ResponseBody>
    
    @DELETE("api/v1/file/{fileId}")
    suspend fun deleteFile(
        @Header("Authorization") token: String,
        @Path("fileId") fileId: String
    ): Response<ApiResponse<String>>
    
    // ================================
    // Analytics Endpoints
    // ================================
    
    @GET("api/v1/analytics/dashboard")
    suspend fun getDashboardAnalytics(
        @Header("Authorization") token: String,
        @Query("days") days: Int = 7
    ): Response<ApiResponse<StatsResponse>>
    
    @GET("api/v1/analytics/trends")
    suspend fun getTrends(
        @Header("Authorization") token: String,
        @Query("period") period: String = "month"
    ): Response<ApiResponse<List<WeeklyTrend>>>
    
    @GET("api/v1/analytics/summary")
    suspend fun getScanSummary(
        @Header("Authorization") token: String
    ): Response<ApiResponse<ScanSummary>>
    
    // ================================
    // Health Check
    // ================================
    
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<String>>
    
    // ================================
    // Admin Endpoints (if user has admin privileges)
    // ================================
    
    @GET("api/v1/admin/users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<User>>>
    
    @GET("api/v1/admin/scans")
    suspend fun getAllScans(
        @Header("Authorization") token: String,
        @Query("date") date: String? = null
    ): Response<ApiResponse<List<ScanResult>>>
    
    @GET("api/v1/admin/stats")
    suspend fun getSystemStats(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Any>>
    
    // ================================
    // Notification Management
    // ================================
    
    @POST("api/v1/notifications/register")
    suspend fun registerForNotifications(
        @Header("Authorization") token: String,
        @Body fcmToken: Map<String, String>
    ): Response<ApiResponse<String>>
    
    @PUT("api/v1/notifications/settings")
    suspend fun updateNotificationSettings(
        @Header("Authorization") token: String,
        @Body settings: NotificationSettings
    ): Response<ApiResponse<NotificationSettings>>
    
    @GET("api/v1/notifications/settings")
    suspend fun getNotificationSettings(
        @Header("Authorization") token: String
    ): Response<ApiResponse<NotificationSettings>>
}

/**
 * API Client Factory
 */
object ApiClient {
    // Production URL (Render deployment) - PRIMARY
    private const val PROD_BASE_URL = "https://deepguard-api.onrender.com/"
    // For Android Emulator - 10.0.2.2 maps to host machine's localhost
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:8002/"
    // For real device testing - use your computer's IP address
    private const val DEVICE_BASE_URL = "http://192.168.0.22:8002/"

    // ✅ Using PRODUCTION URL - works for both emulators and real devices
    private const val BASE_URL = PROD_BASE_URL
    // private const val BASE_URL = EMULATOR_BASE_URL  // Use this for local development with emulator
    // private const val BASE_URL = DEVICE_BASE_URL  // Use this for local development with real device
    
    private var retrofit: retrofit2.Retrofit? = null
    
    fun getRetrofit(): retrofit2.Retrofit {
        if (retrofit == null) {
            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)  // Increased for Render cold starts
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)     // Render free tier needs time to wake up
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            
            retrofit = retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
    
    fun getApiService(): ApiService {
        return getRetrofit().create(ApiService::class.java)
    }
}

/**
 * Network Utils
 */
object NetworkUtils {
    
    fun createAuthHeader(token: String): String {
        return "Bearer $token"
    }
    
    fun createMultipartFile(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String
    ): MultipartBody.Part {
        val requestFile = RequestBody.create(
            mimeType.toMediaTypeOrNull(), 
            fileBytes
        )
        return MultipartBody.Part.createFormData("file", fileName, requestFile)
    }
    
    fun createRequestBody(value: String): RequestBody {
        return RequestBody.create(
            "text/plain".toMediaTypeOrNull(), 
            value
        )
    }
}
