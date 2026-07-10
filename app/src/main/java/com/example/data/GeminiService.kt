package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RecoveryResult(
    val verified: Boolean,
    val password: String?,
    val message: String
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun recoverPassword(
        name: String,
        email: String,
        phone: String,
        bloodGroup: String,
        district: String,
        upazila: String,
        registeredDonors: List<BloodDonor>,
        userPasswords: Map<String, String>
    ): RecoveryResult = withContext(Dispatchers.IO) {
        // 1. Perform actual validation against registered donors
        val emailNormalized = email.trim().lowercase()
        val phoneNormalized = phone.trim().replace(Regex("[^a-zA-Z0-9+]"), "")

        val donor = registeredDonors.find { d ->
            val dEmail = d.email.trim().lowercase()
            val dPhone = d.phone.trim().replace(Regex("[^a-zA-Z0-9+]"), "")
            
            (dEmail == emailNormalized || dPhone == phoneNormalized) &&
                    d.name.trim().equals(name.trim(), ignoreCase = true) &&
                    d.bloodGroup.trim().equals(bloodGroup.trim(), ignoreCase = true) &&
                    d.district.trim().equals(district.trim(), ignoreCase = true) &&
                    d.upazila.trim().equals(upazila.trim(), ignoreCase = true)
        }

        if (donor == null) {
            return@withContext RecoveryResult(
                verified = false,
                password = null,
                message = "প্রদত্ত তথ্যের সাথে কোনো নিবন্ধিত রক্তদাতার মিল পাওয়া যায়নি। দয়া করে আপনার নাম, ইমেইল, ফোন নম্বর এবং রক্তের গ্রুপ পুনরায় চেক করুন।"
            )
        }

        // Retrieve the password from map
        val matchedEmail = donor.email.lowercase()
        val matchedPhone = donor.phone
        val password = userPasswords[matchedEmail] ?: userPasswords[matchedPhone] ?: "alif1234"

        // 2. Call Gemini to generate a friendly greeting and recovery instruction in Bengali
        val apiKey = try {
            val field = com.example.BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "your-gemini-api-key") {
            Log.d(TAG, "Gemini API key is missing or default. Using local fallback.")
            return@withContext RecoveryResult(
                verified = true,
                password = password,
                message = "অভিনন্দন ${donor.name}! আপনার পরিচয় সফলভাবে যাচাই করা হয়েছে। নিচে আপনার পাসওয়ার্ড দেওয়া হলো। নিরাপত্তা বজায় রাখতে এটি পরিবর্তন করার পরামর্শ দেওয়া হচ্ছে।"
            )
        }

        try {
            val prompt = """
                You are Alif Blood Bank password recovery assistant. 
                A user has successfully verified their identity. 
                Name: ${donor.name}
                Email: ${donor.email}
                Phone: ${donor.phone}
                Blood Group: ${donor.bloodGroup}
                District: ${donor.district}
                Upazila: ${donor.upazila}
                
                Please generate a warm, compassionate, and extremely polite message in Bengali language welcoming them back, congratulating them on successful verification, and letting them know that their password has been securely recovered below. Keep it under 100 words. Keep it professional.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val respJson = JSONObject(respStr)
                    val candidates = respJson.optJSONArray("candidates")
                    val text = candidates?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")

                    if (!text.isNullOrBlank()) {
                        return@withContext RecoveryResult(
                            verified = true,
                            password = password,
                            message = text.trim()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
        }

        // Generic fallback if Gemini API call fails
        return@withContext RecoveryResult(
            verified = true,
            password = password,
            message = "অভিনন্দন ${donor.name}! আপনার পরিচয় সফলভাবে যাচাই করা হয়েছে। নিচে আপনার পাসওয়ার্ড দেওয়া হলো। নিরাপত্তা বজায় রাখতে এটি পরিবর্তন করার পরামর্শ দেওয়া হচ্ছে।"
        )
    }
}
