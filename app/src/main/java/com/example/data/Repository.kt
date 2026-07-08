package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BloodConnectRepository private constructor() {

    private val _currentUser = MutableStateFlow<BloodDonor?>(null)
    val currentUser: StateFlow<BloodDonor?> = _currentUser.asStateFlow()

    private val _donors = MutableStateFlow<List<BloodDonor>>(MockData.initialDonors)
    val donors: StateFlow<List<BloodDonor>> = _donors.asStateFlow()

    private val _requests = MutableStateFlow<List<BloodRequest>>(MockData.initialRequests)
    val requests: StateFlow<List<BloodRequest>> = _requests.asStateFlow()

    private val _notifications = MutableStateFlow<List<DonationNotification>>(MockData.initialNotifications)
    val notifications: StateFlow<List<DonationNotification>> = _notifications.asStateFlow()

    private val _scamReports = MutableStateFlow<List<ScamReport>>(
        listOf(
            ScamReport(
                id = "rep_mock1",
                reporterName = "Sabbir Ahmed",
                reporterPhone = "01722883344",
                scammerDonorId = "d_4",
                scammerDonorName = "Nusrat Jahan",
                scammerDonorPhone = "01911223344",
                reason = "Asked for Tk. 2000 in advance for 'travel cost', but after receiving mobile transaction, blocked my number and did not verify blood donation.",
                amountDemanded = "Tk. 2000",
                timestamp = "2026-06-13",
                status = "Pending"
            )
        )
    )
    val scamReports: StateFlow<List<ScamReport>> = _scamReports.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(MockData.initialMessages)
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _ambulances = MutableStateFlow<List<Ambulance>>(MockData.initialAmbulances)
    val ambulances: StateFlow<List<Ambulance>> = _ambulances.asStateFlow()

    private val _appName = MutableStateFlow("Alif Blood Bank")
    val appName: StateFlow<String> = _appName.asStateFlow()

    private val _donationClaims = MutableStateFlow<List<DonationClaim>>(emptyList())
    val donationClaims: StateFlow<List<DonationClaim>> = _donationClaims.asStateFlow()

    fun updateAppName(newName: String) {
        _appName.value = newName
    }

    fun registerAmbulance(
        ownerName: String,
        serviceName: String,
        phone: String,
        district: String,
        upazila: String,
        ambulanceType: String,
        description: String,
        country: String = "Bangladesh"
    ) {
        val newAmbulance = Ambulance(
            id = "amb_${System.currentTimeMillis()}",
            ownerName = ownerName,
            serviceName = serviceName,
            phone = phone,
            district = district,
            upazila = upazila,
            ambulanceType = ambulanceType,
            description = description,
            country = country
        )
        _ambulances.value = listOf(newAmbulance) + _ambulances.value
        
        // Notification
        addNotification(
            titleEn = "New Ambulance added!",
            titleBn = "নতুন অ্যাম্বুলেন্স যুক্ত হয়েছে!",
            messageEn = "$serviceName ($ambulanceType) is now available in $upazila, $district.",
            messageBn = "$serviceName ($ambulanceType) এখন $upazila, $district এ সেবার জন্য প্রস্তুত।",
            type = "SUCCESS",
            country = country
        )
    }

    private val _homeNotice = MutableStateFlow("স্বাগতম আলিফ ব্লাড ব্যাংকে! জরুরি প্রয়োজনে চ্যাট বা কল করুন।")
    val homeNotice: StateFlow<String> = _homeNotice.asStateFlow()

    fun updateHomeNotice(newNotice: String) {
        _homeNotice.value = newNotice
    }

    private val _popupNotice = MutableStateFlow("আমাদের অ্যাপটি নিয়মিত আপডেট করুন এবং রক্ত দানে উৎসাহিত হোন।")
    val popupNotice: StateFlow<String> = _popupNotice.asStateFlow()

    fun updatePopupNotice(newNotice: String) {
        _popupNotice.value = newNotice
    }

    private val _emailNotifyEnabled = MutableStateFlow(true)
    val emailNotifyEnabled: StateFlow<Boolean> = _emailNotifyEnabled.asStateFlow()

    private val _smtpHost = MutableStateFlow("smtp.gmail.com")
    val smtpHost: StateFlow<String> = _smtpHost.asStateFlow()

    private val _smtpPort = MutableStateFlow("587")
    val smtpPort: StateFlow<String> = _smtpPort.asStateFlow()

    private val _smtpUsername = MutableStateFlow("help.alifshen.ltd@gmail.com")
    val smtpUsername: StateFlow<String> = _smtpUsername.asStateFlow()

    private val _smtpPassword = MutableStateFlow("")
    val smtpPassword: StateFlow<String> = _smtpPassword.asStateFlow()

    private val _emailSubjectTemplate = MutableStateFlow("New Blood Inquiry: \$senderName")
    val emailSubjectTemplate: StateFlow<String> = _emailSubjectTemplate.asStateFlow()

    private val _emailBodyTemplate = MutableStateFlow("Hello \$receiverName,\n\nYou have received a new blood donation inquiry from \$senderName (\$senderPhone).\n\nMessage:\n\$messageText\n\nPlease login to Alif Blood Bank app to respond.")
    val emailBodyTemplate: StateFlow<String> = _emailBodyTemplate.asStateFlow()

    // --- GOOGLE ADMOB CONFIG ---
    private val _adMobEnabled = MutableStateFlow(true)
    val adMobEnabled: StateFlow<Boolean> = _adMobEnabled.asStateFlow()

    private val _adMobAppId = MutableStateFlow("ca-app-pub-3940256099942544~3347511713")
    val adMobAppId: StateFlow<String> = _adMobAppId.asStateFlow()

    private val _adMobBannerId = MutableStateFlow("ca-app-pub-3940256099942544/6300978111")
    val adMobBannerId: StateFlow<String> = _adMobBannerId.asStateFlow()

    private val _adMobInterstitialId = MutableStateFlow("ca-app-pub-3940256099942544/1033173712")
    val adMobInterstitialId: StateFlow<String> = _adMobInterstitialId.asStateFlow()

    private val _adMobNativeId = MutableStateFlow("ca-app-pub-3940256099942544/2247696110")
    val adMobNativeId: StateFlow<String> = _adMobNativeId.asStateFlow()

    // --- CUSTOM CPA/AFFILIATE AD NETWORK CONFIG (e.g. Affmine, CPA networks) ---
    private val _customAdsEnabled = MutableStateFlow(true)
    val customAdsEnabled: StateFlow<Boolean> = _customAdsEnabled.asStateFlow()

    private val _customAdNetworkName = MutableStateFlow("Affmine")
    val customAdNetworkName: StateFlow<String> = _customAdNetworkName.asStateFlow()

    private val _customAdTitle = MutableStateFlow("Earn with Affmine CPA Network!")
    val customAdTitle: StateFlow<String> = _customAdTitle.asStateFlow()

    private val _customAdBannerUrl = MutableStateFlow("https://images.unsplash.com/photo-1542744094-3a31f103e35f?auto=format&fit=crop&w=600&q=80")
    val customAdBannerUrl: StateFlow<String> = _customAdBannerUrl.asStateFlow()

    private val _customAdTargetUrl = MutableStateFlow("https://www.affmine.com")
    val customAdTargetUrl: StateFlow<String> = _customAdTargetUrl.asStateFlow()

    private val _customAdTargetCountries = MutableStateFlow("All")
    val customAdTargetCountries: StateFlow<String> = _customAdTargetCountries.asStateFlow()

    private val _customAdConfigs = MutableStateFlow<List<CustomAdConfig>>(emptyList())
    val customAdConfigs: StateFlow<List<CustomAdConfig>> = _customAdConfigs.asStateFlow()

    private fun serializeAds(ads: List<CustomAdConfig>): String {
        return ads.joinToString("||AD_SEP||") { ad ->
            listOf(
                ad.id,
                ad.networkName,
                ad.title,
                ad.bannerUrl,
                ad.isVideo.toString(),
                ad.videoUrl,
                ad.targetUrl,
                ad.targetCountries,
                ad.weight.toString()
            ).joinToString("||FIELD_SEP||")
        }
    }

    fun deserializeAds(serialized: String): List<CustomAdConfig> {
        if (serialized.isEmpty()) return emptyList()
        val list = mutableListOf<CustomAdConfig>()
        val items = serialized.split("||AD_SEP||")
        for (item in items) {
            val parts = item.split("||FIELD_SEP||")
            if (parts.size >= 8) {
                list.add(
                    CustomAdConfig(
                        id = parts[0],
                        networkName = parts[1],
                        title = parts[2],
                        bannerUrl = parts[3],
                        isVideo = parts[4].toBoolean(),
                        videoUrl = parts[5],
                        targetUrl = parts[6],
                        targetCountries = parts[7],
                        weight = parts.getOrNull(8)?.toIntOrNull() ?: 1
                    )
                )
            }
        }
        return list
    }

    fun updateCustomAdConfigsList(context: Context, list: List<CustomAdConfig>) {
        _customAdConfigs.value = list
        val serialized = serializeAds(list)
        val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("custom_ad_configs_list", serialized).apply()
        
        // Also update single ad variables to fallback to the first active ad, for compatibility
        if (list.isNotEmpty()) {
            val first = list.first()
            _customAdNetworkName.value = first.networkName
            _customAdTitle.value = first.title
            _customAdBannerUrl.value = if (first.isVideo) first.videoUrl else first.bannerUrl
            _customAdTargetUrl.value = first.targetUrl
            _customAdTargetCountries.value = first.targetCountries
        }
    }

    fun updateCustomAdsConfig(
        context: Context,
        enabled: Boolean,
        networkName: String,
        adTitle: String,
        bannerUrl: String,
        targetUrl: String,
        targetCountries: String
    ) {
        _customAdsEnabled.value = enabled
        _customAdNetworkName.value = networkName
        _customAdTitle.value = adTitle
        _customAdBannerUrl.value = bannerUrl
        _customAdTargetUrl.value = targetUrl
        _customAdTargetCountries.value = targetCountries

        val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("custom_ads_enabled", enabled)
            putString("custom_ad_network_name", networkName)
            putString("custom_ad_title", adTitle)
            putString("custom_ad_banner_url", bannerUrl)
            putString("custom_ad_target_url", targetUrl)
            putString("custom_ad_target_countries", targetCountries)
            apply()
        }
    }

    fun updateAdMobConfig(
        context: Context,
        enabled: Boolean,
        appId: String,
        bannerId: String,
        interstitialId: String,
        nativeId: String
    ) {
        _adMobEnabled.value = enabled
        _adMobAppId.value = appId
        _adMobBannerId.value = bannerId
        _adMobInterstitialId.value = interstitialId
        _adMobNativeId.value = nativeId

        val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("admob_enabled", enabled)
            putString("admob_app_id", appId)
            putString("admob_banner_id", bannerId)
            putString("admob_interstitial_id", interstitialId)
            putString("admob_native_id", nativeId)
            apply()
        }
    }

    fun updateEmailConfig(
        context: Context,
        enabled: Boolean,
        host: String,
        port: String,
        user: String,
        pass: String,
        subject: String,
        body: String
    ) {
        _emailNotifyEnabled.value = enabled
        _smtpHost.value = host
        _smtpPort.value = port
        _smtpUsername.value = user
        _smtpPassword.value = pass
        _emailSubjectTemplate.value = subject
        _emailBodyTemplate.value = body

        val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("email_notify_enabled", enabled)
            putString("smtp_host", host)
            putString("smtp_port", port)
            putString("smtp_username", user)
            putString("smtp_password", pass)
            putString("email_subject_template", subject)
            putString("email_body_template", body)
            apply()
        }
    }

    private val _language = MutableStateFlow(AppLanguage.ENG)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _customCountries = MutableStateFlow<List<Pair<String, String>>>(listOf(
        Pair("Bangladesh", "BD"),
        Pair("United States", "US"),
        Pair("India", "IN"),
        Pair("Saudi Arabia", "SA"),
        Pair("United Arab Emirates", "AE"),
        Pair("United Kingdom", "GB"),
        Pair("Canada", "CA"),
        Pair("Malaysia", "MY"),
        Pair("Singapore", "SG"),
        Pair("Kuwait", "KW"),
        Pair("Oman", "OM"),
        Pair("Qatar", "QA"),
        Pair("Bahrain", "BH"),
        Pair("Italy", "IT")
    ))
    val customCountries: StateFlow<List<Pair<String, String>>> = _customCountries.asStateFlow()

    fun addCountry(context: Context, name: String, code: String) {
        val updated = _customCountries.value.toMutableList()
        if (updated.none { it.first.equals(name, ignoreCase = true) || it.second.equals(code, ignoreCase = true) }) {
            updated.add(Pair(name, code))
            _customCountries.value = updated
            val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            val serialized = updated.joinToString(";") { "${it.first}:${it.second}" }
            prefs.edit().putString("custom_countries_list", serialized).apply()
        }
    }

    fun deleteCountry(context: Context, name: String) {
        val updated = _customCountries.value.filterNot { it.first.equals(name, ignoreCase = true) }
        _customCountries.value = updated
        val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
        val serialized = updated.joinToString(";") { "${it.first}:${it.second}" }
        prefs.edit().putString("custom_countries_list", serialized).apply()
    }

    private val _systemCountry = MutableStateFlow("Bangladesh")
    val systemCountry: StateFlow<String> = _systemCountry.asStateFlow()

    private val _isBangladesh = MutableStateFlow(true)
    val isBangladesh: StateFlow<Boolean> = _isBangladesh.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val appScope = CoroutineScope(Dispatchers.IO)
    private var prefsInitialized = false

    // Trigger background email notification via WorkManager
    fun triggerEmailNotification(context: Context, subject: String, body: String) {
        val workRequest = OneTimeWorkRequestBuilder<EmailNotificationWorker>()
            .setInputData(workDataOf(
                "subject" to subject,
                "body" to body
            ))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // Direct Intent for foreground email/SMS sending
    fun sendEmailViaIntent(context: Context, subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("help.alifshen.ltd@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var appContext: Context? = null

    private val _remoteApiKey = MutableStateFlow("")
    val remoteApiKey: StateFlow<String> = _remoteApiKey.asStateFlow()

    private fun setCurrentUser(user: BloodDonor?) {
        _currentUser.value = user
        appContext?.let { saveUserSession(it, user) }
    }

    fun saveUserSession(context: Context, donor: BloodDonor?) {
        val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
        if (donor == null) {
            prefs.edit().apply {
                remove("user_session_id")
                remove("user_session_name")
                remove("user_session_bloodGroup")
                remove("user_session_phone")
                remove("user_session_email")
                remove("user_session_district")
                remove("user_session_upazila")
                remove("user_session_lastDonationDate")
                remove("user_session_isAvailable")
                remove("user_session_isApproved")
                remove("user_session_donationCount")
                remove("user_session_isGoogleUser")
                remove("user_session_country")
                remove("user_session_userId")
                remove("user_session_isWarning")
                remove("user_session_warningReason")
                remove("user_session_role")
                apply()
            }
        } else {
            prefs.edit().apply {
                putString("user_session_id", donor.id)
                putString("user_session_name", donor.name)
                putString("user_session_bloodGroup", donor.bloodGroup)
                putString("user_session_phone", donor.phone)
                putString("user_session_email", donor.email)
                putString("user_session_district", donor.district)
                putString("user_session_upazila", donor.upazila)
                putString("user_session_lastDonationDate", donor.lastDonationDate)
                putBoolean("user_session_isAvailable", donor.isAvailable)
                putBoolean("user_session_isApproved", donor.isApproved)
                putInt("user_session_donationCount", donor.donationCount)
                putBoolean("user_session_isGoogleUser", donor.isGoogleUser)
                putString("user_session_country", donor.country)
                putString("user_session_userId", donor.userId)
                putBoolean("user_session_isWarning", donor.isWarning)
                putString("user_session_warningReason", donor.warningReason)
                putString("user_session_role", donor.role)
                apply()
            }
        }
    }

    fun initRemoteConfig(context: Context) {
        appContext = context.applicationContext
        if (prefsInitialized) return
        val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
        
        // Load User Session
        val savedUserId = prefs.getString("user_session_id", "") ?: ""
        if (savedUserId.isNotBlank()) {
            val loadedDonor = BloodDonor(
                id = savedUserId,
                name = prefs.getString("user_session_name", "") ?: "",
                bloodGroup = prefs.getString("user_session_bloodGroup", "") ?: "",
                phone = prefs.getString("user_session_phone", "") ?: "",
                email = prefs.getString("user_session_email", "") ?: "",
                district = prefs.getString("user_session_district", "") ?: "",
                upazila = prefs.getString("user_session_upazila", "") ?: "",
                lastDonationDate = prefs.getString("user_session_lastDonationDate", "Available") ?: "Available",
                isAvailable = prefs.getBoolean("user_session_isAvailable", true),
                isApproved = prefs.getBoolean("user_session_isApproved", true),
                donationCount = prefs.getInt("user_session_donationCount", 0),
                isGoogleUser = prefs.getBoolean("user_session_isGoogleUser", false),
                country = prefs.getString("user_session_country", "Bangladesh") ?: "Bangladesh",
                userId = prefs.getString("user_session_userId", "") ?: "",
                isWarning = prefs.getBoolean("user_session_isWarning", false),
                warningReason = prefs.getString("user_session_warningReason", "") ?: "",
                role = prefs.getString("user_session_role", "Donor") ?: "Donor"
            )
            _currentUser.value = loadedDonor
            
            // Register in list
            val exists = _donors.value.any { it.phone == loadedDonor.phone }
            if (!exists) {
                _donors.value = _donors.value + loadedDonor
            }
        }

        var savedUrl = prefs.getString("remote_api_url", "") ?: ""
        var savedKey = prefs.getString("remote_api_key", "") ?: ""

        if (savedUrl.isBlank()) {
            savedUrl = com.example.BuildConfig.SUPABASE_URL
            if (savedUrl.isBlank()) {
                savedUrl = "https://hpturyhypcplydvtslpq.supabase.co"
            }
            prefs.edit().putString("remote_api_url", savedUrl).apply()
        }
        if (savedKey.isBlank()) {
            savedKey = com.example.BuildConfig.SUPABASE_PUBLISHABLE_KEY
            prefs.edit().putString("remote_api_key", savedKey).apply()
        }
        _remoteApiKey.value = savedKey

        if (savedUrl.isNotBlank()) {
            BloodConnectApiClient.updateBaseUrl(savedUrl, savedKey)
            triggerRemoteSync()
        }

        val serialized = prefs.getString("custom_countries_list", "") ?: ""
        if (serialized.isNotBlank()) {
            val loaded = serialized.split(";").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) Pair(parts[0], parts[1]) else null
            }
            if (loaded.isNotEmpty()) {
                _customCountries.value = loaded
            }
        }
        
        _emailNotifyEnabled.value = prefs.getBoolean("email_notify_enabled", true)
        _smtpHost.value = prefs.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com"
        _smtpPort.value = prefs.getString("smtp_port", "587") ?: "587"
        _smtpUsername.value = prefs.getString("smtp_username", "help.alifshen.ltd@gmail.com") ?: "help.alifshen.ltd@gmail.com"
        _smtpPassword.value = prefs.getString("smtp_password", "") ?: ""
        _emailSubjectTemplate.value = prefs.getString("email_subject_template", "New Blood Inquiry: \$senderName") ?: "New Blood Inquiry: \$senderName"
        _emailBodyTemplate.value = prefs.getString("email_body_template", "Hello \$receiverName,\n\nYou have received a new blood donation inquiry from \$senderName (\$senderPhone).\n\nMessage:\n\$messageText\n\nPlease login to Alif Blood Bank app to respond.") ?: "Hello \$receiverName,\n\nYou have received a new blood donation inquiry from \$senderName (\$senderPhone).\n\nMessage:\n\$messageText\n\nPlease login to Alif Blood Bank app to respond."

        _adMobEnabled.value = prefs.getBoolean("admob_enabled", true)
        _adMobAppId.value = prefs.getString("admob_app_id", "ca-app-pub-3940256099942544~3347511713") ?: "ca-app-pub-3940256099942544~3347511713"
        _adMobBannerId.value = prefs.getString("admob_banner_id", "ca-app-pub-3940256099942544/6300978111") ?: "ca-app-pub-3940256099942544/6300978111"
        _adMobInterstitialId.value = prefs.getString("admob_interstitial_id", "ca-app-pub-3940256099942544/1033173712") ?: "ca-app-pub-3940256099942544/1033173712"
        _adMobNativeId.value = prefs.getString("admob_native_id", "ca-app-pub-3940256099942544/2247696110") ?: "ca-app-pub-3940256099942544/2247696110"

        _customAdsEnabled.value = prefs.getBoolean("custom_ads_enabled", true)
        _customAdNetworkName.value = prefs.getString("custom_ad_network_name", "Affmine") ?: "Affmine"
        _customAdTitle.value = prefs.getString("custom_ad_title", "Earn with Affmine CPA Network!") ?: "Earn with Affmine CPA Network!"
        _customAdBannerUrl.value = prefs.getString("custom_ad_banner_url", "https://images.unsplash.com/photo-1542744094-3a31f103e35f?auto=format&fit=crop&w=600&q=80") ?: "https://images.unsplash.com/photo-1542744094-3a31f103e35f?auto=format&fit=crop&w=600&q=80"
        _customAdTargetUrl.value = prefs.getString("custom_ad_target_url", "https://www.affmine.com") ?: "https://www.affmine.com"
        _customAdTargetCountries.value = prefs.getString("custom_ad_target_countries", "All") ?: "All"

        val adsListStr = prefs.getString("custom_ad_configs_list", "") ?: ""
        var loadedAdsList = deserializeAds(adsListStr)
        if (loadedAdsList.isEmpty()) {
            loadedAdsList = listOf(
                CustomAdConfig(
                    id = "default_affmine",
                    networkName = _customAdNetworkName.value,
                    title = _customAdTitle.value,
                    bannerUrl = _customAdBannerUrl.value,
                    isVideo = false,
                    videoUrl = "",
                    targetUrl = _customAdTargetUrl.value,
                    targetCountries = _customAdTargetCountries.value,
                    weight = 1
                )
            )
            prefs.edit().putString("custom_ad_configs_list", serializeAds(loadedAdsList)).apply()
        }
        _customAdConfigs.value = loadedAdsList

        prefsInitialized = true
    }

    fun updateRemoteApiUrl(context: Context, url: String, apiKey: String): Boolean {
        _remoteApiKey.value = apiKey
        val success = BloodConnectApiClient.updateBaseUrl(url, apiKey)
        if (success) {
            val prefs = context.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("remote_api_url", url)
                putString("remote_api_key", apiKey)
                apply()
            }
            _syncError.value = null
            triggerRemoteSync()
        } else {
            _syncError.value = if (url.isBlank()) null else "Invalid API URL format"
        }
        return success
    }

    fun triggerRemoteSync() {
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isBlank()) return@launch
            _isSyncing.value = true
            _syncError.value = null
            
            // 1. Fetch Donors
            val donorsResult = BloodConnectApiClient.fetchDonors()
            if (donorsResult.isSuccess) {
                val remoteDonors = donorsResult.getOrNull()
                if (remoteDonors != null && remoteDonors.isNotEmpty()) {
                    val local = _donors.value.toMutableList()
                    remoteDonors.forEach { remote ->
                        val existingIdx = local.indexOfFirst { it.phone == remote.phone }
                        if (existingIdx != -1) {
                            local[existingIdx] = remote
                        } else {
                            local.add(remote)
                        }
                    }
                    _donors.value = local
                }
            } else {
                _syncError.value = donorsResult.exceptionOrNull()?.message ?: "Donors Sync failed"
            }

            // 2. Fetch Requests
            val requestsResult = BloodConnectApiClient.fetchRequests()
            if (requestsResult.isSuccess) {
                val remoteRequests = requestsResult.getOrNull()
                if (remoteRequests != null && remoteRequests.isNotEmpty()) {
                    val local = _requests.value.toMutableList()
                    remoteRequests.forEach { remote ->
                        val existingIdx = local.indexOfFirst { it.id == remote.id }
                        if (existingIdx != -1) {
                            local[existingIdx] = remote
                        } else {
                            local.add(remote)
                        }
                    }
                    _requests.value = local
                }
            } else {
                val err = requestsResult.exceptionOrNull()?.message ?: "Requests Sync failed"
                _syncError.value = if (_syncError.value == null) err else "${_syncError.value}\n$err"
            }

            _isSyncing.value = false
        }
    }

    init {
        val locale = java.util.Locale.getDefault()
        val timezoneId = java.util.TimeZone.getDefault().id
        val isBD = locale.country.equals("BD", ignoreCase = true) || 
                   locale.language.equals("bn", ignoreCase = true) || 
                   timezoneId.contains("Dhaka", ignoreCase = true)
        
        _language.value = if (isBD) AppLanguage.BAN else AppLanguage.ENG
        _isBangladesh.value = isBD
        
        val detectedCountry = if (isBD) {
            "Bangladesh"
        } else {
            val display = locale.displayCountry
            if (display.isNullOrBlank()) "United States" else display
        }
        _systemCountry.value = detectedCountry
    }

    fun toggleLanguage() {
        val nextLang = if (_language.value == AppLanguage.ENG) AppLanguage.BAN else AppLanguage.ENG
        _language.value = nextLang
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    // AUTH ACTIONS
    fun loginWithPhoneOrEmail(username: String, email: String, password: String = "", isGoogle: Boolean = false): Boolean {
        // Specific credential check for the user
        if (!isGoogle && email.equals("Alifsheenshopping@gmail.com", ignoreCase = true) && password == "019Alif11#") {
            val adminUser = _donors.value.find { it.email.equals(email, ignoreCase = true) }
            if (adminUser != null) {
                setCurrentUser(adminUser)
            } else {
                val newAdmin = BloodDonor(
                    id = "u_alif_admin",
                    name = "Alif",
                    bloodGroup = "B+",
                    phone = "01900000000",
                    email = email,
                    district = "Dhaka",
                    upazila = "Dhaka",
                    lastDonationDate = "Available",
                    isAvailable = true,
                    isApproved = true,
                    donationCount = 10,
                    isGoogleUser = false
                )
                setCurrentUser(newAdmin)
                _donors.value = _donors.value + newAdmin
            }
            return true
        }

        // Find existing donor if any, otherwise return false for standard login
        val existing = _donors.value.find { 
            (username.isNotBlank() && it.phone == username) || (email.isNotBlank() && it.email.equals(email, ignoreCase = true))
        }
        if (existing != null) {
            setCurrentUser(existing)
            return true
        } else {
            if (isGoogle) {
                // Log in as a newly simulated user for Google Sign-in if simulated
                val newSimulatedUser = BloodDonor(
                    id = "u_sim",
                    name = "Alif Shen",
                    bloodGroup = "B+",
                    phone = "01781223344",
                    email = "help.alifshen.ltd@gmail.com",
                    district = "Dhaka",
                    upazila = "Dhanmondi",
                    lastDonationDate = "Available",
                    isAvailable = true,
                    isApproved = true,
                    donationCount = 1,
                    isGoogleUser = true
                )
                setCurrentUser(newSimulatedUser)
                _donors.value = _donors.value + newSimulatedUser
                return true
            }
            return false
        }
    }

    fun registerDonor(
        name: String,
        phone: String,
        email: String,
        bloodGroup: String,
        district: String,
        upazila: String,
        lastDonationDate: String,
        country: String = "Bangladesh",
        role: String = "Donor"
    ) {
        val randId = "ABB-${(10000..99999).random()}"
        val newUser = BloodDonor(
            id = "u_${System.currentTimeMillis()}",
            name = name,
            bloodGroup = bloodGroup,
            phone = phone,
            email = email,
            district = district,
            upazila = upazila,
            lastDonationDate = if (lastDonationDate.isBlank()) "Available" else lastDonationDate,
            isAvailable = true,
            isApproved = true, // Auto-approved for standard users, admin screen can still moderate!
            donationCount = 0,
            country = country,
            userId = randId,
            role = role
        )
        setCurrentUser(newUser)
        _donors.value = _donors.value + newUser

        // Post to remote API if configured
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                val result = BloodConnectApiClient.registerDonor(newUser)
                if (result.isSuccess) {
                    Log.d("BloodConnectRepo", "Successfully registered donor in cloud!")
                } else {
                    Log.e("BloodConnectRepo", "Failed to register donor in cloud: ${result.exceptionOrNull()?.message}")
                }
            }
        }

        // Trigger notification
        addNotification(
            titleEn = "New Donor registered!",
            titleBn = "নতুন রক্তদাতা যুক্ত হয়েছেন!",
            messageEn = "$name (${bloodGroup}) is now available in $upazila, $district, $country.",
            messageBn = "$name (${bloodGroup}) এখন $upazila, $district, $country এ রক্ত দিতে প্রস্তুত।",
            type = "SUCCESS",
            country = country
        )
    }

    fun logout() {
        setCurrentUser(null)
    }

    // PROFILE ACTIONS
    fun updateProfile(
        name: String,
        phone: String,
        email: String,
        bloodGroup: String,
        district: String,
        upazila: String,
        lastDonation: String,
        available: Boolean,
        country: String = "Bangladesh",
        role: String? = null
    ) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            name = name,
            phone = phone,
            email = email,
            bloodGroup = bloodGroup,
            district = district,
            upazila = upazila,
            lastDonationDate = lastDonation,
            isAvailable = available,
            country = country,
            role = role ?: current.role
        )
        setCurrentUser(updated)
        _donors.value = _donors.value.map { if (it.id == current.id) updated else it }

        // Post profile update to remote API if configured
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                val result = BloodConnectApiClient.registerDonor(updated)
                if (result.isSuccess) {
                    Log.d("BloodConnectRepo", "Successfully updated profile in cloud!")
                } else {
                    Log.e("BloodConnectRepo", "Failed to update profile in cloud: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    // DONATION ACTION & HISTORY
    fun addDonationToHistory() {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            donationCount = current.donationCount + 1,
            lastDonationDate = "2026-06-12"
        )
        setCurrentUser(updated)
        _donors.value = _donors.value.map { if (it.id == current.id) updated else it }
    }

    fun submitDonationClaim(requestId: String, donorPhone: String, donorName: String, contactNumber: String) {
        val newClaim = DonationClaim(
            id = "claim_${System.currentTimeMillis()}",
            requestId = requestId,
            donorPhone = donorPhone,
            donorName = donorName,
            contactNumber = contactNumber,
            status = "Pending"
        )
        _donationClaims.value = _donationClaims.value + newClaim
    }

    fun acceptDonationClaim(claimId: String) {
        val claims = _donationClaims.value
        val claim = claims.find { it.id == claimId } ?: return
        
        // Mark claim as Accepted
        _donationClaims.value = claims.map {
            if (it.id == claimId) it.copy(status = "Accepted") else it
        }
        
        // Find donor by phone and increment donation count
        val donorPhone = claim.donorPhone
        val currentDonors = _donors.value
        val donor = currentDonors.find { it.phone == donorPhone }
        if (donor != null) {
            val updatedDonor = donor.copy(donationCount = donor.donationCount + 1)
            _donors.value = currentDonors.map {
                if (it.phone == donorPhone) updatedDonor else it
            }
            // Also update current logged in user if they are the donor!
            val currentLoggedIn = _currentUser.value
            if (currentLoggedIn != null && currentLoggedIn.phone == donorPhone) {
                setCurrentUser(updatedDonor)
            }
        }
        
        // Mark blood request as completed
        val currentRequests = _requests.value
        _requests.value = currentRequests.map {
            if (it.id == claim.requestId) it.copy(status = "Completed") else it
        }
    }

    fun rejectDonationClaim(claimId: String) {
        val claims = _donationClaims.value
        _donationClaims.value = claims.map {
            if (it.id == claimId) it.copy(status = "Rejected") else it
        }
    }

    // REQUEST ACTIONS
    fun createBloodRequest(
        context: Context?,
        patientName: String,
        bloodGroup: String,
        bloodAmount: String,
        hospitalName: String,
        district: String,
        upazila: String,
        contactNumber: String,
        details: String,
        isEmergency: Boolean,
        country: String = "Bangladesh",
        patientGender: String = "Male",
        medicalCondition: String = ""
    ) {
        val newReq = BloodRequest(
            id = "r_${System.currentTimeMillis()}",
            patientName = patientName,
            bloodGroup = bloodGroup,
            bloodAmount = bloodAmount,
            hospitalName = hospitalName,
            district = district,
            upazila = upazila,
            contactNumber = contactNumber,
            details = details,
            isEmergency = isEmergency,
            isApproved = true,
            dateRequested = "2026-06-12",
            status = "Active",
            country = country,
            patientGender = patientGender,
            medicalCondition = medicalCondition
        )
        _requests.value = listOf(newReq) + _requests.value

        // Post request to remote API in background if configured
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                val result = BloodConnectApiClient.createRequest(newReq)
                if (result.isSuccess) {
                    Log.d("BloodConnectRepo", "Successfully created request in cloud!")
                } else {
                    Log.e("BloodConnectRepo", "Failed to create request in cloud: ${result.exceptionOrNull()?.message}")
                }
            }
        }

        // Trigger Notification alert!
        addNotification(
            titleEn = "URGENT request for $bloodGroup",
            titleBn = "জরুরি রক্তের অনুরোধ: $bloodGroup",
            messageEn = "$patientName needs $bloodGroup blood at $hospitalName ($upazila, $district, $country).",
            messageBn = "$patientName এর $hospitalName-এ ($upazila, $district, $country) $bloodGroup রক্তের প্রয়োজন।",
            type = if (isEmergency) "ALERT" else "REQUEST",
            country = country
        )

        // Trigger Email Notification to Admin
        context?.let {
            triggerEmailNotification(
                it,
                "New Blood Request: $bloodGroup",
                "Patient: $patientName\nHospital: $hospitalName\nLocation: $upazila, $district, $country\nContact: $contactNumber\nEmergency: $isEmergency"
            )
        }
    }

    // NOTIFICATION CHANNELS
    fun addNotification(titleEn: String, titleBn: String, messageEn: String, messageBn: String, type: String, country: String = "Bangladesh") {
        val newNotification = DonationNotification(
            id = "n_${System.currentTimeMillis()}",
            titleEn = titleEn,
            titleBn = titleBn,
            messageEn = messageEn,
            messageBn = messageBn,
            timestamp = "Just now",
            type = type,
            country = country
        )
        _notifications.value = listOf(newNotification) + _notifications.value
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    // SCAM / FRAUD REPORT ACTIONS
    fun submitScamReport(
        reporterName: String,
        reporterPhone: String,
        scammerDonorId: String,
        scammerDonorName: String,
        scammerDonorPhone: String,
        reason: String,
        amountDemanded: String,
        country: String = "Bangladesh",
        scammerPhotoUri: String? = null
    ) {
        val newReport = ScamReport(
            id = "rep_${System.currentTimeMillis()}",
            reporterName = reporterName,
            reporterPhone = reporterPhone,
            scammerDonorId = scammerDonorId,
            scammerDonorName = scammerDonorName,
            scammerDonorPhone = scammerDonorPhone,
            reason = reason,
            amountDemanded = amountDemanded,
            timestamp = "2026-06-14",
            country = country,
            scammerPhotoUri = scammerPhotoUri
        )
        _scamReports.value = listOf(newReport) + _scamReports.value

        // Trigger notification Alert
        addNotification(
            titleEn = "FRAUD REPORTED!",
            titleBn = "প্রতারণার রিপোর্ট জমা পড়েছে!",
            messageEn = "Donor $scammerDonorName was reported by $reporterName ($amountDemanded).",
            messageBn = "রক্তদাতা $scammerDonorName এর বিরুদ্ধে প্রতারণার অভিযোগ করা হয়েছে ($amountDemanded)।",
            type = "ALERT",
            country = country
        )
    }

    fun actionOnReport(id: String, action: String) {
        _scamReports.value = _scamReports.value.map {
            if (it.id == id) it.copy(status = action) else it
        }
        val report = _scamReports.value.find { it.id == id }
        if (report != null && action == "Banned") {
            val cleanReportPhone = report.scammerDonorPhone.trim().replace("+88", "").replace(" ", "")
            // Unapprove and make the scammer unavailable in directories
            _donors.value = _donors.value.map {
                val cleanDonorPhone = it.phone.trim().replace("+88", "").replace(" ", "")
                if (it.id == report.scammerDonorId || cleanDonorPhone == cleanReportPhone || cleanDonorPhone.endsWith(cleanReportPhone) || cleanReportPhone.endsWith(cleanDonorPhone)) {
                    it.copy(isApproved = false, isAvailable = false)
                } else it
            }
        }
    }

    fun updateScamReport(
        id: String,
        scammerName: String,
        scammerPhone: String,
        amount: String,
        reason: String,
        status: String
    ) {
        _scamReports.value = _scamReports.value.map {
            if (it.id == id) {
                it.copy(
                    scammerDonorName = scammerName,
                    scammerDonorPhone = scammerPhone,
                    amountDemanded = amount,
                    reason = reason,
                    status = status
                )
            } else it
        }
        if (status == "Banned") {
            val cleanReportPhone = scammerPhone.trim().replace("+88", "").replace(" ", "")
            _donors.value = _donors.value.map {
                val cleanDonorPhone = it.phone.trim().replace("+88", "").replace(" ", "")
                val scammerDonorId = _scamReports.value.find { rep -> rep.id == id }?.scammerDonorId ?: ""
                if (it.id == scammerDonorId || cleanDonorPhone == cleanReportPhone || cleanDonorPhone.endsWith(cleanReportPhone) || cleanReportPhone.endsWith(cleanDonorPhone)) {
                    it.copy(isApproved = false, isAvailable = false)
                } else it
            }
        }
    }

    // CHAT SYSTEM ACTIONS
    fun sendChatMessage(
        context: Context?,
        senderPhone: String,
        senderName: String,
        receiverPhone: String,
        receiverName: String,
        messageText: String
    ) {
        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val curTime = formatter.format(java.util.Date())
        
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderPhone = senderPhone,
            senderName = senderName,
            receiverPhone = receiverPhone,
            receiverName = receiverName,
            message = messageText,
            timestamp = curTime,
            isRead = false
        )
        _messages.value = _messages.value + newMsg

        // Trigger Email notification to Admin for new messages
        context?.let {
            triggerEmailNotification(
                it,
                "New In-App Message from $senderName",
                "From: $senderName ($senderPhone)\nTo: $receiverName ($receiverPhone)\nMessage: $messageText"
            )
        }

        // Trigger Email notification to Receiver if they have a Gmail/Email address
        if (_emailNotifyEnabled.value) {
            val receiverUser = _donors.value.find { it.phone == receiverPhone }
            if (receiverUser != null && !receiverUser.email.isNullOrBlank()) {
                val receiverEmail = receiverUser.email
                // Build email from templates
                val subject = _emailSubjectTemplate.value
                    .replace("\$senderName", senderName)
                    .replace("\$receiverName", receiverName)
                    .replace("\$senderPhone", senderPhone)
                    .replace("\$messageText", messageText)
                
                val body = _emailBodyTemplate.value
                    .replace("\$senderName", senderName)
                    .replace("\$receiverName", receiverName)
                    .replace("\$senderPhone", senderPhone)
                    .replace("\$messageText", messageText)

                context?.let { ctx ->
                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<EmailNotificationWorker>()
                        .setInputData(androidx.work.workDataOf(
                            "subject" to subject,
                            "body" to body,
                            "recipient" to receiverEmail,
                            "isSmtp" to true
                        ))
                        .build()
                    androidx.work.WorkManager.getInstance(ctx).enqueue(workRequest)
                }
            }
        }
    }

    fun markChatAsRead(userPhone: String, peerPhone: String) {
        _messages.value = _messages.value.map {
            if (it.senderPhone == peerPhone && it.receiverPhone == userPhone && !it.isRead) {
                it.copy(isRead = true)
            } else {
                it
            }
        }
    }

    // ADMIN ACTIONS
    fun warnDonor(id: String, isWarning: Boolean, reason: String) {
        _donors.value = _donors.value.map {
            if (it.id == id) it.copy(isWarning = isWarning, warningReason = reason) else it
        }
        val current = _currentUser.value
        if (current != null && current.id == id) {
            setCurrentUser(current.copy(isWarning = isWarning, warningReason = reason))
        }
    }

    fun approveDonor(id: String) {
        _donors.value = _donors.value.map {
            if (it.id == id) it.copy(isApproved = true) else it
        }
    }

    fun deleteDonor(id: String) {
        _donors.value = _donors.value.filterNot { it.id == id }
    }

    fun deleteRequest(id: String) {
        _requests.value = _requests.value.filterNot { it.id == id }
    }

    fun toggleRequestStatus(id: String) {
        _requests.value = _requests.value.map {
            if (it.id == id) {
                val nextStatus = if (it.status == "Active") "Completed" else "Active"
                it.copy(status = nextStatus)
            } else it
        }
    }

    // POLICY STATE MANAGEMENT
    private val _privacyPolicyEn = MutableStateFlow("We value your privacy. Your contact details are only shared securely with registered members when requesting or donating blood. We do not sell or lease your personal information to any third party.")
    val privacyPolicyEn: StateFlow<String> = _privacyPolicyEn.asStateFlow()

    private val _privacyPolicyBn = MutableStateFlow("আমরা আপনার গোপনীয়তাকে শতভাগ মূল্যায়ন করি। আপনার যোগাযোগের তথ্য কেবল রক্তদান বা রক্তদাতার সন্ধানের জন্য নিবন্ধিত সদস্যদের সঙ্গে শেয়ার করা হয়। আমরা আপনার কোনো ব্যক্তিগত তথ্য কোনো তৃতীয় পক্ষকে প্রদান করি না।")
    val privacyPolicyBn: StateFlow<String> = _privacyPolicyBn.asStateFlow()

    private val _termsConditionsEn = MutableStateFlow("By using Blood Connect BD, you agree to participate voluntarily and donate blood without demanding any financial compensation. All matched services and platform usages are conducted at the user's own discretion and responsibility.")
    val termsConditionsEn: StateFlow<String> = _termsConditionsEn.asStateFlow()

    private val _termsConditionsBn = MutableStateFlow("ব্লাড কানেক্ট বিডি ব্যবহার করে, আপনি সম্পূর্ণ রক্তদানে সম্মতি প্রকাশ করছেন কোনো প্রকার আর্থিক সুবিধা ছাড়াই। যেকোনো রক্তদাতার তথ্যের ব্যবহার এবং যোগাযোগ কেবল ব্যবহারকারীর নিজস্ব দায়িত্বে এবং স্বেচ্ছাধীন সিদ্ধান্তে পরিচালিত হবে।")
    val termsConditionsBn: StateFlow<String> = _termsConditionsBn.asStateFlow()

    private val _refundPolicyEn = MutableStateFlow("Refund & Free Campaign: This application is a 100% free, non-profit platform created purely for humanitarian purposes. There are no fees associated with donor search, registration, requests, or cancel premiums.")
    val refundPolicyEn: StateFlow<String> = _refundPolicyEn.asStateFlow()

    private val _refundPolicyBn = MutableStateFlow("রিফান্ড এবং অন্যান্য শর্ত: এই মোবাইল অ্যাপ্লিকেশনটি সম্পূর্ণ বিনামূল্যে ব্যবহারযোগ্য একটি অলাভজনক সামাজিক ও মানবিক প্ল্যাটফর্ম। রক্তদাতা অনুসন্ধান, রক্তদানের অনুরোধ বা রেজিস্ট্রেশনে কোনো ফি কিংবা চার্জ নেই।")
    val refundPolicyBn: StateFlow<String> = _refundPolicyBn.asStateFlow()

    fun updatePolicies(
        privacyEn: String, privacyBn: String,
        termsEn: String, termsBn: String,
        refundEn: String, refundBn: String
    ) {
        _privacyPolicyEn.value = privacyEn
        _privacyPolicyBn.value = privacyBn
        _termsConditionsEn.value = termsEn
        _termsConditionsBn.value = termsBn
        _refundPolicyEn.value = refundEn
        _refundPolicyBn.value = refundBn
    }

    companion object {
        @Volatile
        private var instance: BloodConnectRepository? = null

        fun getInstance(): BloodConnectRepository {
            return instance ?: synchronized(this) {
                instance ?: BloodConnectRepository().also { instance = it }
            }
        }
    }
}
