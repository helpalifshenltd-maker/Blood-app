package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Retrofit API Endpoints Mapping Interface
interface BloodConnectApiService {
    @GET("donors")
    suspend fun getDonors(): List<BloodDonor>

    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("donors")
    suspend fun registerDonor(@Body donor: BloodDonor): List<BloodDonor>

    @PATCH("donors")
    suspend fun updateDonor(@Query("id") idFilter: String, @Body donor: BloodDonor): List<BloodDonor>

    @DELETE("donors")
    suspend fun deleteDonor(@Query("id") idFilter: String): List<BloodDonor>

    @GET("requests")
    suspend fun getRequests(): List<BloodRequest>

    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("requests")
    suspend fun createRequest(@Body request: BloodRequest): List<BloodRequest>

    @PATCH("requests")
    suspend fun updateRequest(@Query("id") idFilter: String, @Body request: BloodRequest): List<BloodRequest>

    @DELETE("requests")
    suspend fun deleteRequest(@Query("id") idFilter: String): List<BloodRequest>

    @GET("ambulance_bookings")
    suspend fun getAmbulanceBookings(): List<AmbulanceBooking>

    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("ambulance_bookings")
    suspend fun createAmbulanceBooking(@Body booking: AmbulanceBooking): List<AmbulanceBooking>

    @PATCH("ambulance_bookings")
    suspend fun updateAmbulanceBooking(@Query("id") idFilter: String, @Body booking: AmbulanceBooking): List<AmbulanceBooking>

    @DELETE("ambulance_bookings")
    suspend fun deleteAmbulanceBooking(@Query("id") idFilter: String): List<AmbulanceBooking>

    @GET("chat_messages")
    suspend fun getChatMessages(): List<ChatMessage>

    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("chat_messages")
    suspend fun sendChatMessage(@Body message: ChatMessage): List<ChatMessage>

    @GET("notifications")
    suspend fun getNotifications(): List<DonationNotification>

    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("notifications")
    suspend fun sendNotification(@Body notification: DonationNotification): List<DonationNotification>

    @GET("app_config")
    suspend fun getAppConfig(): List<AppConfig>

    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("app_config")
    suspend fun updateAppConfig(@Body config: AppConfig): List<AppConfig>
}

// Client Manager for dynamically configuring Base URL and performing Retrofit network calls
object BloodConnectApiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val _apiUrl = MutableStateFlow("")
    val apiUrl: StateFlow<String> = _apiUrl.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isRemoteConnected = MutableStateFlow(false)
    val isRemoteConnected: StateFlow<Boolean> = _isRemoteConnected.asStateFlow()

    private var apiService: BloodConnectApiService? = null

    // Set or Update the Remote API Base URL and dynamic API Key
    fun updateBaseUrl(url: String, key: String = ""): Boolean {
        _apiKey.value = key
        if (url.isBlank()) {
            _apiUrl.value = ""
            apiService = null
            _isRemoteConnected.value = false
            return true
        }

        // Standardize URL formatting
        var formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }.replace(Regex("/+$"), "")

        // Auto-append rest/v1/ for Supabase URLs if not already present
        if (formattedUrl.contains("supabase.co") && !formattedUrl.contains("/rest/v1")) {
            formattedUrl += "/rest/v1"
        }
        formattedUrl += "/" // Ensure trailing slash

        return try {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val requestBuilder = chain.request().newBuilder()
                    val currentKey = _apiKey.value
                    if (currentKey.isNotBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer $currentKey")
                        if (chain.request().url.host.contains("supabase.co")) {
                            requestBuilder.addHeader("apikey", currentKey)
                        }
                    }
                    chain.proceed(requestBuilder.build())
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            apiService = retrofit.create(BloodConnectApiService::class.java)
            _apiUrl.value = formattedUrl
            _isRemoteConnected.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _isRemoteConnected.value = false
            false
        }
    }

    // Network request wrappers for safe Remote Integration
    suspend fun fetchDonors(): Result<List<BloodDonor>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set or configured."))
        return try {
            val list = service.getDonors()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerDonor(donor: BloodDonor): Result<List<BloodDonor>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set. Action performed locally."))
        return try {
            val response = service.registerDonor(donor)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRequests(): Result<List<BloodRequest>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set or configured."))
        return try {
            val list = service.getRequests()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRequest(request: BloodRequest): Result<List<BloodRequest>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set. Action performed locally."))
        return try {
            val response = service.createRequest(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAmbulanceBookings(): Result<List<AmbulanceBooking>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val list = service.getAmbulanceBookings()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAmbulanceBooking(booking: AmbulanceBooking): Result<List<AmbulanceBooking>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.createAmbulanceBooking(booking)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAmbulanceBooking(id: String, booking: AmbulanceBooking): Result<List<AmbulanceBooking>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.updateAmbulanceBooking("eq.$id", booking)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAmbulanceBooking(id: String): Result<List<AmbulanceBooking>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.deleteAmbulanceBooking("eq.$id")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchChatMessages(): Result<List<ChatMessage>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val list = service.getChatMessages()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(message: ChatMessage): Result<List<ChatMessage>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.sendChatMessage(message)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDonor(id: String, donor: BloodDonor): Result<List<BloodDonor>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.updateDonor("eq.$id", donor)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDonor(id: String): Result<List<BloodDonor>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.deleteDonor("eq.$id")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRequest(id: String, request: BloodRequest): Result<List<BloodRequest>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.updateRequest("eq.$id", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRequest(id: String): Result<List<BloodRequest>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.deleteRequest("eq.$id")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAppConfig(): Result<AppConfig?> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.getAppConfig()
            Result.success(response.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppConfig(config: AppConfig): Result<List<AppConfig>> {
        val service = apiService ?: return Result.failure(Exception("API URL is not set."))
        return try {
            val response = service.updateAppConfig(config)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
