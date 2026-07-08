package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Verifies user recovery details via the Gemini API using the gemini-3.5-flash model.
     * Returns a recovery result containing whether it is verified, the password, and a message.
     */
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
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is missing or is placeholder. Falling back to local offline AI validation.")
            return@withContext localOfflineVerify(name, email, phone, bloodGroup, district, upazila, registeredDonors, userPasswords)
        }

        // Build the list of existing users to match
        val usersJson = JSONArray()
        for (donor in registeredDonors) {
            val userObj = JSONObject()
            userObj.put("name", donor.name)
            userObj.put("email", donor.email)
            userObj.put("phone", donor.phone)
            userObj.put("bloodGroup", donor.bloodGroup)
            userObj.put("district", donor.district)
            userObj.put("upazila", donor.upazila)
            // Match password from our map
            val pwd = userPasswords[donor.email.lowercase()] ?: userPasswords[donor.phone] ?: "019Alif11#"
            userObj.put("password", pwd)
            usersJson.put(userObj)
        }

        val systemInstruction = """
            You are the secure AI Password Recovery Controller for Alif Blood Bank.
            Your task is to securely verify user details against our database of registered users.
            If the details match, you recover their password.
            
            Database of Registered Users:
            ${usersJson.toString(2)}
            
            Validation Rules:
            1. The User-Provided Name, Email, and Phone MUST match a record in our database.
            2. Match phone numbers exactly.
            3. Match emails and names case-insensitively. Allow minor trailing/leading whitespaces.
            4. Blood Group, District, and Upazila should match.
            
            Response Format:
            You MUST return a JSON object ONLY. No explanation, no markdown blocks, just raw JSON with these exact fields:
            {
              "verified": true or false,
              "password": "the_password_if_verified_else_null",
              "message": "A polite message in Bengali (preferred) or English confirming the status."
            }
        """.trimIndent()

        val prompt = """
            Please verify these User-Provided Details:
            - Name: $name
            - Email: $email
            - Phone: $phone
            - Blood Group: $bloodGroup
            - District: $district
            - Upazila: $upazila
        """.trimIndent()

        try {
            // Construct generateContent request body
            val requestJson = JSONObject()
            
            // contents list
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // systemInstruction
            val systemInstructionObj = JSONObject()
            val systemPartsArray = JSONArray()
            val systemPartObj = JSONObject()
            systemPartObj.put("text", systemInstruction)
            systemPartsArray.put(systemPartObj)
            systemInstructionObj.put("parts", systemPartsArray)
            requestJson.put("systemInstruction", systemInstructionObj)

            // generationConfig to force JSON output
            val generationConfig = JSONObject()
            val responseFormat = JSONObject()
            responseFormat.put("mimeType", "application/json")
            generationConfig.put("responseFormat", responseFormat)
            generationConfig.put("temperature", 0.1) // Low temperature for factual precision
            requestJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API call failed with code: ${response.code}")
                return@withContext localOfflineVerify(name, email, phone, bloodGroup, district, upazila, registeredDonors, userPasswords)
            }

            val responseBodyString = response.body?.string() ?: ""
            Log.d(TAG, "Gemini Raw Response: $responseBodyString")

            val responseJson = JSONObject(responseBodyString)
            val candidates = responseJson.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val replyText = parts.getJSONObject(0).getString("text").trim()

            Log.d(TAG, "Gemini Reply Text: $replyText")

            val replyJson = JSONObject(replyText)
            val verified = replyJson.optBoolean("verified", false)
            val password = replyJson.optString("password", "")
            val message = replyJson.optString("message", "Verification completed.")

            RecoveryResult(verified, if (verified) password else null, message)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            localOfflineVerify(name, email, phone, bloodGroup, district, upazila, registeredDonors, userPasswords)
        }
    }

    /**
     * Local fallback check for offline use or when Gemini API key is missing.
     */
    private fun localOfflineVerify(
        name: String,
        email: String,
        phone: String,
        bloodGroup: String,
        district: String,
        upazila: String,
        registeredDonors: List<BloodDonor>,
        userPasswords: Map<String, String>
    ): RecoveryResult {
        val match = registeredDonors.find { donor ->
            val nameMatch = donor.name.equals(name, ignoreCase = true) || donor.name.contains(name, ignoreCase = true) || name.contains(donor.name, ignoreCase = true)
            val emailMatch = donor.email.equals(email, ignoreCase = true)
            val phoneMatch = donor.phone.replace("+88", "").trim() == phone.replace("+88", "").trim()
            val bloodMatch = donor.bloodGroup == bloodGroup
            
            // Allow minor flexibility but require high matching
            phoneMatch && (emailMatch || nameMatch)
        }

        return if (match != null) {
            val pwd = userPasswords[match.email.lowercase()] ?: userPasswords[match.phone] ?: "019Alif11#"
            RecoveryResult(
                verified = true,
                password = pwd,
                message = "সফলভাবে তথ্য যাচাই করা হয়েছে! আপনার একাউন্ট পাসওয়ার্ডটি হলো: $pwd"
            )
        } else {
            RecoveryResult(
                verified = false,
                password = null,
                message = "দুঃখিত, আপনার দেওয়া তথ্যের সাথে কোনো মিল পাওয়া যায়নি। দয়া করে সঠিক নাম, ইমেইল ও মোবাইল নম্বর দিন।"
            )
        }
    }
}

data class RecoveryResult(
    val verified: Boolean,
    val password: String?,
    val message: String
)
