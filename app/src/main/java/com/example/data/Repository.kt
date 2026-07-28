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

    private val _isFirebaseConnected = MutableStateFlow<Boolean>(true)
    val isFirebaseConnected: StateFlow<Boolean> = _isFirebaseConnected.asStateFlow()

    private val _ambulances = MutableStateFlow<List<Ambulance>>(MockData.initialAmbulances)
    val ambulances: StateFlow<List<Ambulance>> = _ambulances.asStateFlow()

    private val _donorTeams = MutableStateFlow<List<DonorTeam>>(MockData.initialDonorTeams)
    val donorTeams: StateFlow<List<DonorTeam>> = _donorTeams.asStateFlow()

    private val _ambulanceBookings = MutableStateFlow<List<AmbulanceBooking>>(emptyList())
    val ambulanceBookings: StateFlow<List<AmbulanceBooking>> = _ambulanceBookings.asStateFlow()

    private val _appName = MutableStateFlow("Alif Blood Bank")
    val appName: StateFlow<String> = _appName.asStateFlow()

    private val _donationClaims = MutableStateFlow<List<DonationClaim>>(emptyList())
    val donationClaims: StateFlow<List<DonationClaim>> = _donationClaims.asStateFlow()

    private val _subscriptionPlans = MutableStateFlow<List<V9SubscriptionPlan>>(emptyList())
    val subscriptionPlans: StateFlow<List<V9SubscriptionPlan>> = _subscriptionPlans.asStateFlow()

    private val _userSubscriptions = MutableStateFlow<List<UserSubscription>>(emptyList())
    val userSubscriptions: StateFlow<List<UserSubscription>> = _userSubscriptions.asStateFlow()

    private val _useMockStats = MutableStateFlow(false)
    val useMockStats: StateFlow<Boolean> = _useMockStats.asStateFlow()

    private val _mockTotalUsers = MutableStateFlow(80424)
    val mockTotalUsers: StateFlow<Int> = _mockTotalUsers.asStateFlow()

    private val _mockTotalDonors = MutableStateFlow(12300)
    val mockTotalDonors: StateFlow<Int> = _mockTotalDonors.asStateFlow()

    fun updateMockStats(use: Boolean, users: Int, donors: Int) {
        _useMockStats.value = use
        _mockTotalUsers.value = users
        _mockTotalDonors.value = donors
        saveAppConfigLocal()
        pushAppConfigToRemote()
    }

    fun saveAppConfigLocal() {
        val now = System.currentTimeMillis()
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putLong("config_last_updated", now)
                if (_appName.value.isNotBlank()) putString("app_name_pref", _appName.value)
                if (_homeNotice.value.isNotBlank()) putString("home_notice", _homeNotice.value)
                if (_popupNotice.value.isNotBlank()) putString("popup_notice", _popupNotice.value)
                if (_bkashNumber.value.isNotBlank()) putString("payment_bkash", _bkashNumber.value)
                if (_nagadNumber.value.isNotBlank()) putString("payment_nagad", _nagadNumber.value)
                if (_rocketNumber.value.isNotBlank()) putString("payment_rocket", _rocketNumber.value)
                if (_googlePlayMerchant.value.isNotBlank()) putString("payment_googleplay", _googlePlayMerchant.value)
                putFloat("booking_acceptance_fee", _bookingAcceptanceFee.value.toFloat())
                putFloat("standard_commission_rate", _standardCommissionRate.value.toFloat())
                putFloat("mplus_commission_rate", _mPlusCommissionRate.value.toFloat())
                if (_privacyPolicyEn.value.isNotBlank()) putString("policy_privacy_en", _privacyPolicyEn.value)
                if (_privacyPolicyBn.value.isNotBlank()) putString("policy_privacy_bn", _privacyPolicyBn.value)
                if (_privacyPolicyUrl.value.isNotBlank()) putString("policy_privacy_url", _privacyPolicyUrl.value)
                if (_termsConditionsEn.value.isNotBlank()) putString("policy_terms_en", _termsConditionsEn.value)
                if (_termsConditionsBn.value.isNotBlank()) putString("policy_terms_bn", _termsConditionsBn.value)
                if (_refundPolicyEn.value.isNotBlank()) putString("policy_refund_en", _refundPolicyEn.value)
                if (_refundPolicyBn.value.isNotBlank()) putString("policy_refund_bn", _refundPolicyBn.value)
                putBoolean("custom_ads_enabled", _customAdsEnabled.value)
                putString("custom_ad_network_name", _customAdNetworkName.value)
                putString("custom_ad_title", _customAdTitle.value)
                putString("custom_ad_banner_url", _customAdBannerUrl.value)
                putString("custom_ad_target_url", _customAdTargetUrl.value)
                putString("custom_ad_target_countries", _customAdTargetCountries.value)
                putString("custom_ad_configs_list", serializeAds(_customAdConfigs.value))
                putBoolean("admob_enabled", _adMobEnabled.value)
                putString("admob_app_id", _adMobAppId.value)
                putString("admob_banner_id", _adMobBannerId.value)
                putString("admob_interstitial_id", _adMobInterstitialId.value)
                putString("admob_native_id", _adMobNativeId.value)
                putBoolean("email_notify_enabled", _emailNotifyEnabled.value)
                putString("smtp_host", _smtpHost.value)
                putString("smtp_port", _smtpPort.value)
                putString("smtp_username", _smtpUsername.value)
                putString("smtp_password", _smtpPassword.value)
                putString("email_subject_template", _emailSubjectTemplate.value)
                putString("email_body_template", _emailBodyTemplate.value)
                putBoolean("use_mock_stats", _useMockStats.value)
                putInt("mock_total_users", _mockTotalUsers.value)
                putInt("mock_total_donors", _mockTotalDonors.value)
                apply()
            }
        }
    }

    fun updateAppName(newName: String) {
        _appName.value = newName
        saveAppConfigLocal()
        pushAppConfigToRemote()
    }

    fun registerAmbulance(
        ownerName: String,
        serviceName: String,
        phone: String,
        district: String,
        upazila: String,
        ambulanceType: String,
        description: String,
        country: String = "Bangladesh",
        email: String = "",
        password: String = ""
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
        saveAmbulancesLocal()
        pushAmbulanceToFirebase(newAmbulance)

        // Ensure user/donor account exists for this ambulance driver so they can log in
        val existingDonor = _donors.value.find { phonesMatch(it.phone, phone) || (email.isNotBlank() && it.email.equals(email, ignoreCase = true)) }
        if (existingDonor == null) {
            val randId = "ABB-${(10000..99999).random()}"
            val ambUser = BloodDonor(
                id = "u_${System.currentTimeMillis()}",
                name = if (ownerName.isNotBlank()) ownerName else serviceName,
                bloodGroup = "N/A",
                phone = phone,
                email = email,
                district = district,
                upazila = upazila,
                lastDonationDate = "Available",
                isAvailable = true,
                isApproved = true,
                donationCount = 0,
                country = country,
                userId = randId,
                role = "Ambulance",
                password = password
            )
            _donors.value = _donors.value + ambUser
            saveDonorsLocal()
            pushDonorToFirebase(ambUser)
            setCurrentUser(ambUser)
        } else {
            val updatedDonor = existingDonor.copy(
                role = "Ambulance",
                password = if (password.isNotBlank()) password else existingDonor.password,
                email = if (email.isNotBlank()) email else existingDonor.email
            )
            _donors.value = _donors.value.map { if (it.id == existingDonor.id) updatedDonor else it }
            saveDonorsLocal()
            pushDonorToFirebase(updatedDonor)
            setCurrentUser(updatedDonor)
        }

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

    fun toggleAmbulanceAvailability(ambulanceId: String) {
        var updated: Ambulance? = null
        _ambulances.value = _ambulances.value.map {
            if (it.id == ambulanceId) {
                val upd = it.copy(isAvailable = !it.isAvailable)
                updated = upd
                upd
            } else {
                it
            }
        }
        saveAmbulancesLocal()
        updated?.let { pushAmbulanceToFirebase(it) }
    }

    private val _homeNotice = MutableStateFlow("স্বাগতম আলিফ ব্লাড ব্যাংকে! জরুরি প্রয়োজনে চ্যাট বা কল করুন।")
    val homeNotice: StateFlow<String> = _homeNotice.asStateFlow()

    private val _standardCommissionRate = MutableStateFlow(30.0)
    val standardCommissionRate: StateFlow<Double> = _standardCommissionRate.asStateFlow()

    private val _mPlusCommissionRate = MutableStateFlow(50.0)
    val mPlusCommissionRate: StateFlow<Double> = _mPlusCommissionRate.asStateFlow()

    private val _bookingAcceptanceFee = MutableStateFlow(50.0)
    val bookingAcceptanceFee: StateFlow<Double> = _bookingAcceptanceFee.asStateFlow()

    fun updateBookingAcceptanceFee(fee: Double) {
        _bookingAcceptanceFee.value = fee
        saveAppConfigLocal()
        pushAppConfigToRemote()
    }

    fun updateCommissionRates(standardRate: Double, mPlusRate: Double) {
        _standardCommissionRate.value = standardRate
        _mPlusCommissionRate.value = mPlusRate
        saveAppConfigLocal()
        pushAppConfigToRemote()
    }

    fun updateHomeNotice(newNotice: String) {
        _homeNotice.value = newNotice
        saveAppConfigLocal()
        pushAppConfigToRemote()
    }

    private val _popupNotice = MutableStateFlow("আমাদের অ্যাপটি নিয়মিত আপডেট করুন এবং রক্ত দানে উৎসাহিত হোন।")
    val popupNotice: StateFlow<String> = _popupNotice.asStateFlow()

    fun updatePopupNotice(newNotice: String) {
        _popupNotice.value = newNotice
        saveAppConfigLocal()
        pushAppConfigToRemote()
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

    private val _bkashNumber = MutableStateFlow("+8801700000000")
    val bkashNumber: StateFlow<String> = _bkashNumber.asStateFlow()

    private val _nagadNumber = MutableStateFlow("+8801700000000")
    val nagadNumber: StateFlow<String> = _nagadNumber.asStateFlow()

    private val _rocketNumber = MutableStateFlow("+8801700000000")
    val rocketNumber: StateFlow<String> = _rocketNumber.asStateFlow()

    private val _googlePlayMerchant = MutableStateFlow("play_v9_premium_active")
    val googlePlayMerchant: StateFlow<String> = _googlePlayMerchant.asStateFlow()

    fun updatePaymentConfig(bkash: String, nagad: String, rocket: String, googlePlay: String) {
        _bkashNumber.value = bkash
        _nagadNumber.value = nagad
        _rocketNumber.value = rocket
        _googlePlayMerchant.value = googlePlay
        saveAppConfigLocal()
        pushAppConfigToRemote()
    }

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
        saveAppConfigLocal()
        pushAppConfigToRemote()
    }

    fun serializeBookings(list: List<AmbulanceBooking>): String {
        return list.joinToString("||BOOK_SEP||") { booking ->
            listOf(
                booking.id,
                booking.patientName,
                booking.contactPhone,
                booking.pickupAddress,
                booking.destinationAddress,
                booking.ambulanceType,
                booking.urgencyLevel,
                booking.dateTime,
                booking.status,
                booking.assignedAmbulanceId ?: "",
                booking.assignedAmbulanceName ?: "",
                booking.assignedAmbulancePhone ?: "",
                booking.notes,
                booking.timestamp,
                booking.fare.toString(),
                booking.commission.toString(),
                booking.isCommissionPaid.toString(),
                booking.paymentMethod ?: "",
                booking.paymentTxnId ?: "",
                booking.paymentPhone ?: ""
            ).joinToString("||FIELD_SEP||")
        }
    }

    fun deserializeBookings(serialized: String): List<AmbulanceBooking> {
        if (serialized.isEmpty()) return emptyList()
        val list = mutableListOf<AmbulanceBooking>()
        val items = serialized.split("||BOOK_SEP||")
        for (item in items) {
            val parts = item.split("||FIELD_SEP||")
            if (parts.size >= 8) {
                list.add(
                    AmbulanceBooking(
                        id = parts[0],
                        patientName = parts[1],
                        contactPhone = parts[2],
                        pickupAddress = parts[3],
                        destinationAddress = parts[4],
                        ambulanceType = parts[5],
                        urgencyLevel = parts[6],
                        dateTime = parts[7],
                        status = parts.getOrNull(8) ?: "Pending",
                        assignedAmbulanceId = parts.getOrNull(9)?.ifBlank { null },
                        assignedAmbulanceName = parts.getOrNull(10)?.ifBlank { null },
                        assignedAmbulancePhone = parts.getOrNull(11)?.ifBlank { null },
                        notes = parts.getOrNull(12) ?: "",
                        timestamp = parts.getOrNull(13) ?: "",
                        fare = parts.getOrNull(14)?.toDoubleOrNull() ?: 0.0,
                        commission = parts.getOrNull(15)?.toDoubleOrNull() ?: 0.0,
                        isCommissionPaid = parts.getOrNull(16)?.toBoolean() ?: false,
                        paymentMethod = parts.getOrNull(17)?.ifBlank { null },
                        paymentTxnId = parts.getOrNull(18)?.ifBlank { null },
                        paymentPhone = parts.getOrNull(19)?.ifBlank { null }
                    )
                )
            }
        }
        return list
    }

    fun submitAmbulanceBooking(
        patientName: String,
        contactPhone: String,
        pickupAddress: String,
        destinationAddress: String,
        ambulanceType: String,
        urgencyLevel: String,
        dateTime: String,
        notes: String
    ) {
        val newBooking = AmbulanceBooking(
            id = "book_${System.currentTimeMillis()}",
            patientName = patientName,
            contactPhone = contactPhone,
            pickupAddress = pickupAddress,
            destinationAddress = destinationAddress,
            ambulanceType = ambulanceType,
            urgencyLevel = urgencyLevel,
            dateTime = dateTime,
            status = "Pending",
            notes = notes,
            timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        )
        _ambulanceBookings.value = listOf(newBooking) + _ambulanceBookings.value
        saveBookingsLocal()
        
        // Push ambulance booking to Firebase Realtime Database
        pushAmbulanceBookingToFirebase(newBooking)

        // Push to remote API if configured
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                BloodConnectApiClient.createAmbulanceBooking(newBooking)
            }
        }
        
        // Push notification
        addNotification(
            titleEn = "New Ambulance Booking Request",
            titleBn = "নতুন অ্যাম্বুলেন্স বুকিং অনুরোধ",
            messageEn = "Ambulance booking request received for $patientName. Contact: $contactPhone.",
            messageBn = "$patientName এর জন্য একটি অ্যাম্বুলেন্স বুকিং অনুরোধ পাওয়া গেছে। যোগাযোগ: $contactPhone।",
            type = "URGENT_REQUEST",
            country = "Bangladesh"
        )
    }

    fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        assignedName: String? = null,
        assignedPhone: String? = null,
        adminNotes: String? = null,
        fare: Double? = null
    ) {
        var updatedItem: AmbulanceBooking? = null
        _ambulanceBookings.value = _ambulanceBookings.value.map {
            if (it.id == bookingId) {
                val finalFare = fare ?: it.fare
                val resolvedName = assignedName ?: it.assignedAmbulanceName
                val isMPlus = resolvedName?.contains("M Plus", ignoreCase = true) == true ||
                              resolvedName?.contains("এম প্লাস") == true
                val rate = if (isMPlus) _mPlusCommissionRate.value else _standardCommissionRate.value
                val finalCommission = finalFare * (rate / 100.0)
                val item = it.copy(
                    status = newStatus,
                    assignedAmbulanceName = resolvedName,
                    assignedAmbulancePhone = assignedPhone ?: it.assignedAmbulancePhone,
                    notes = adminNotes ?: it.notes,
                    fare = finalFare,
                    commission = finalCommission
                )
                updatedItem = item
                item
            } else {
                it
            }
        }
        saveBookingsLocal()

        updatedItem?.let { booking ->
            pushAmbulanceBookingToFirebase(booking)
            appScope.launch {
                if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                    BloodConnectApiClient.updateAmbulanceBooking(booking.id, booking)
                }
            }
        }
    }

    fun payBookingCommission(
        bookingId: String,
        method: String,
        txnId: String,
        phone: String
    ) {
        var updatedBook: AmbulanceBooking? = null
        _ambulanceBookings.value = _ambulanceBookings.value.map {
            if (it.id == bookingId) {
                val upd = it.copy(
                    isCommissionPaid = true,
                    paymentMethod = method,
                    paymentTxnId = txnId,
                    paymentPhone = phone
                )
                updatedBook = upd
                upd
            } else {
                it
            }
        }
        saveBookingsLocal()
        updatedBook?.let { pushAmbulanceBookingToFirebase(it) }
    }

    fun saveBookingsLocal() {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("ambulance_bookings_list", serializeBookings(_ambulanceBookings.value)).apply()
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
        saveAppConfigLocal()
        pushAppConfigToRemote()
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
        saveAppConfigLocal()
        pushAppConfigToRemote()
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
        saveAppConfigLocal()
        pushAppConfigToRemote()
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
                remove("user_session_walletBalance")
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
                putFloat("user_session_walletBalance", donor.walletBalance.toFloat())
                apply()
            }
        }
    }

    // Seen request IDs for notification deduplication
    private val seenRequestIds = mutableSetOf<String>()

    // Local Persistence and Helper Methods
    fun serializeDonors(donors: List<BloodDonor>): String {
        return donors.joinToString("||DONOR_SEP||") { donor ->
            listOf(
                donor.id,
                donor.name,
                donor.bloodGroup,
                donor.phone,
                donor.email,
                donor.district,
                donor.upazila,
                donor.lastDonationDate,
                donor.isAvailable.toString(),
                donor.isApproved.toString(),
                donor.donationCount.toString(),
                donor.isGoogleUser.toString(),
                donor.country,
                donor.userId,
                donor.isWarning.toString(),
                donor.warningReason,
                donor.role,
                donor.walletBalance.toString()
            ).joinToString("||FIELD_SEP||")
        }
    }

    fun deserializeDonors(serialized: String): List<BloodDonor> {
        if (serialized.isEmpty()) return emptyList()
        val list = mutableListOf<BloodDonor>()
        val items = serialized.split("||DONOR_SEP||")
        for (item in items) {
            val parts = item.split("||FIELD_SEP||")
            if (parts.size >= 8) {
                list.add(
                    BloodDonor(
                        id = parts[0],
                        name = parts[1],
                        bloodGroup = parts[2],
                        phone = parts[3],
                        email = parts[4],
                        district = parts[5],
                        upazila = parts[6],
                        lastDonationDate = parts[7],
                        isAvailable = parts.getOrNull(8)?.toBoolean() ?: true,
                        isApproved = parts.getOrNull(9)?.toBoolean() ?: true,
                        donationCount = parts.getOrNull(10)?.toIntOrNull() ?: 0,
                        isGoogleUser = parts.getOrNull(11)?.toBoolean() ?: false,
                        country = parts.getOrNull(12) ?: "Bangladesh",
                        userId = parts.getOrNull(13) ?: "",
                        isWarning = parts.getOrNull(14)?.toBoolean() ?: false,
                        warningReason = parts.getOrNull(15) ?: "",
                        role = parts.getOrNull(16) ?: "Donor",
                        walletBalance = parts.getOrNull(17)?.toDoubleOrNull() ?: 0.0
                    )
                )
            }
        }
        return list
    }

    fun saveDonorsLocal() {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("local_donors_list", serializeDonors(_donors.value)).apply()
        }
    }

    fun serializeRequests(requests: List<BloodRequest>): String {
        return requests.joinToString("||REQ_SEP||") { req ->
            listOf(
                req.id,
                req.patientName,
                req.bloodGroup,
                req.bloodAmount,
                req.hospitalName,
                req.district,
                req.upazila,
                req.contactNumber,
                req.details,
                req.isEmergency.toString(),
                req.isApproved.toString(),
                req.dateRequested,
                req.status,
                req.country,
                req.patientGender,
                req.medicalCondition
            ).joinToString("||FIELD_SEP||")
        }
    }

    fun deserializeRequests(serialized: String): List<BloodRequest> {
        if (serialized.isEmpty()) return emptyList()
        val list = mutableListOf<BloodRequest>()
        val items = serialized.split("||REQ_SEP||")
        for (item in items) {
            val parts = item.split("||FIELD_SEP||")
            if (parts.size >= 8) {
                list.add(
                    BloodRequest(
                        id = parts[0],
                        patientName = parts[1],
                        bloodGroup = parts[2],
                        bloodAmount = parts[3],
                        hospitalName = parts[4],
                        district = parts[5],
                        upazila = parts[6],
                        contactNumber = parts[7],
                        details = parts.getOrNull(8) ?: "Urgent blood needed, please contact immediately.",
                        isEmergency = parts.getOrNull(9)?.toBoolean() ?: true,
                        isApproved = parts.getOrNull(10)?.toBoolean() ?: true,
                        dateRequested = parts.getOrNull(11) ?: "2026-06-12",
                        status = parts.getOrNull(12) ?: "Active",
                        country = parts.getOrNull(13) ?: "Bangladesh",
                        patientGender = parts.getOrNull(14) ?: "Male",
                        medicalCondition = parts.getOrNull(15) ?: ""
                    )
                )
            }
        }
        return list
    }

    fun saveRequestsLocal() {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("local_requests_list", serializeRequests(_requests.value)).apply()
        }
    }

    fun saveMessagesLocal() {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("local_messages_list", serializeMessages(_messages.value)).apply()
        }
    }

    fun serializeMessages(msgs: List<ChatMessage>): String {
        return msgs.joinToString("||MSG_SEP||") { m ->
            listOf(
                m.id,
                m.senderPhone,
                m.senderName,
                m.receiverPhone,
                m.receiverName,
                m.message,
                m.timestamp,
                m.isRead.toString()
            ).joinToString("||FIELD_SEP||")
        }
    }

    fun deserializeMessages(serialized: String): List<ChatMessage> {
        if (serialized.isEmpty()) return emptyList()
        val list = mutableListOf<ChatMessage>()
        val items = serialized.split("||MSG_SEP||")
        for (item in items) {
            val parts = item.split("||FIELD_SEP||")
            if (parts.size >= 8) {
                list.add(
                    ChatMessage(
                        id = parts[0],
                        senderPhone = parts[1],
                        senderName = parts[2],
                        receiverPhone = parts[3],
                        receiverName = parts[4],
                        message = parts[5],
                        timestamp = parts[6],
                        isRead = parts[7].toBoolean()
                    )
                )
            }
        }
        return list
    }

    fun serializeAmbulances(ambulances: List<Ambulance>): String {
        return ambulances.joinToString("||AMB_SEP||") { amb ->
            listOf(
                amb.id,
                amb.ownerName,
                amb.serviceName,
                amb.phone,
                amb.district,
                amb.upazila,
                amb.ambulanceType,
                amb.isAvailable.toString(),
                amb.description,
                amb.country
            ).joinToString("||FIELD_SEP||")
        }
    }

    fun deserializeAmbulances(serialized: String): List<Ambulance> {
        if (serialized.isEmpty()) return emptyList()
        val list = mutableListOf<Ambulance>()
        val items = serialized.split("||AMB_SEP||")
        for (item in items) {
            val parts = item.split("||FIELD_SEP||")
            if (parts.size >= 7) {
                list.add(
                    Ambulance(
                        id = parts[0],
                        ownerName = parts[1],
                        serviceName = parts[2],
                        phone = parts[3],
                        district = parts[4],
                        upazila = parts[5],
                        ambulanceType = parts[6],
                        isAvailable = parts.getOrNull(7)?.toBoolean() ?: true,
                        description = parts.getOrNull(8) ?: "",
                        country = parts.getOrNull(9) ?: "Bangladesh"
                    )
                )
            }
        }
        return list
    }

    fun saveAmbulancesLocal() {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("ambulances_list", serializeAmbulances(_ambulances.value)).apply()
        }
    }

    fun serializeDonorTeams(teams: List<DonorTeam>): String {
        val array = org.json.JSONArray()
        teams.forEach { team ->
            val obj = org.json.JSONObject()
            obj.put("id", team.id)
            obj.put("teamName", team.teamName)
            obj.put("leaderName", team.leaderName)
            obj.put("leaderPhone", team.leaderPhone)
            obj.put("district", team.district)
            obj.put("upazila", team.upazila)
            obj.put("description", team.description)
            obj.put("isApproved", team.isApproved)
            obj.put("status", team.status)
            obj.put("createdAt", team.createdAt)
            obj.put("country", team.country)

            val membersArray = org.json.JSONArray()
            team.members.forEach { m ->
                val mObj = org.json.JSONObject()
                mObj.put("memberPhone", m.memberPhone)
                mObj.put("memberName", m.memberName)
                mObj.put("bloodGroup", m.bloodGroup)
                mObj.put("role", m.role)
                mObj.put("joinedAt", m.joinedAt)
                membersArray.put(mObj)
            }
            obj.put("members", membersArray)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeDonorTeams(jsonStr: String): List<DonorTeam> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<DonorTeam>()
        try {
            val array = org.json.JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val membersList = mutableListOf<TeamMember>()
                val membersArray = obj.optJSONArray("members")
                if (membersArray != null) {
                    for (j in 0 until membersArray.length()) {
                        val mObj = membersArray.getJSONObject(j)
                        membersList.add(
                            TeamMember(
                                memberPhone = mObj.optString("memberPhone", ""),
                                memberName = mObj.optString("memberName", ""),
                                bloodGroup = mObj.optString("bloodGroup", ""),
                                role = mObj.optString("role", "Member"),
                                joinedAt = mObj.optString("joinedAt", "")
                            )
                        )
                    }
                }
                list.add(
                    DonorTeam(
                        id = obj.optString("id", ""),
                        teamName = obj.optString("teamName", ""),
                        leaderName = obj.optString("leaderName", ""),
                        leaderPhone = obj.optString("leaderPhone", ""),
                        district = obj.optString("district", ""),
                        upazila = obj.optString("upazila", ""),
                        description = obj.optString("description", ""),
                        isApproved = obj.optBoolean("isApproved", false),
                        status = obj.optString("status", "Pending"),
                        members = membersList,
                        createdAt = obj.optString("createdAt", ""),
                        country = obj.optString("country", "Bangladesh")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveDonorTeamsLocal() {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("donor_teams_list", serializeDonorTeams(_donorTeams.value)).apply()
        }
    }

    fun pushDonorTeamToFirebase(team: DonorTeam) {
        appScope.launch {
            try {
                getDb()?.getReference("donor_teams")?.child(team.id)?.setValue(team)
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase push donor_team error: ${e.message}")
            }
        }
    }

    fun deleteDonorTeamFromFirebase(teamId: String) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val key = cleanKey(teamId)
                db.getReference("donor_teams").child(key).removeValue()
                if (key != teamId) {
                    db.getReference("donor_teams").child(teamId).removeValue()
                }
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase delete donor_team error: ${e.message}")
            }
        }
    }

    fun createDonorTeam(
        teamName: String,
        district: String,
        upazila: String,
        description: String,
        leaderName: String,
        leaderPhone: String,
        leaderBloodGroup: String = ""
    ): DonorTeam {
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val newTeam = DonorTeam(
            id = "team_${System.currentTimeMillis()}",
            teamName = teamName,
            leaderName = leaderName,
            leaderPhone = leaderPhone,
            district = district,
            upazila = upazila,
            description = description,
            isApproved = false, // Admin approval required!
            status = "Pending",
            createdAt = dateStr,
            country = "Bangladesh",
            members = listOf(
                TeamMember(
                    memberPhone = leaderPhone,
                    memberName = leaderName,
                    bloodGroup = leaderBloodGroup,
                    role = "Leader",
                    joinedAt = dateStr
                )
            )
        )
        _donorTeams.value = listOf(newTeam) + _donorTeams.value
        saveDonorTeamsLocal()
        pushDonorTeamToFirebase(newTeam)
        
        // Notify admin about pending team request
        addNotification(
            titleEn = "New Donor Team Application",
            titleBn = "নতুন রক্তদাতা টিমের আবেদন",
            messageEn = "Team '$teamName' applied by $leaderName. Pending Admin approval.",
            messageBn = "$leaderName এর মাধ্যমে '$teamName' টিমের আবেদন জমা হয়েছে। এডমিনের অনুমোদন প্রয়োজন।",
            type = "ALERT",
            country = "Bangladesh"
        )
        return newTeam
    }

    fun approveDonorTeam(teamId: String) {
        _donorTeams.value = _donorTeams.value.map { team ->
            if (team.id == teamId) {
                val updated = team.copy(isApproved = true, status = "Approved")
                pushDonorTeamToFirebase(updated)
                addNotification(
                    titleEn = "Donor Team Approved!",
                    titleBn = "টিম অনুমোদন লাভ করেছে!",
                    messageEn = "Congratulations! Your team '${team.teamName}' has been approved by Admin.",
                    messageBn = "অভিনন্দন! আপনার ব্লাড ডোনার টিম '${team.teamName}' এডমিন কর্তৃক অনুমোদিত হয়েছে।",
                    type = "SUCCESS",
                    country = team.country
                )
                updated
            } else team
        }
        saveDonorTeamsLocal()
    }

    fun rejectDonorTeam(teamId: String) {
        _donorTeams.value = _donorTeams.value.map { team ->
            if (team.id == teamId) {
                val updated = team.copy(isApproved = false, status = "Rejected")
                pushDonorTeamToFirebase(updated)
                updated
            } else team
        }
        saveDonorTeamsLocal()
    }

    fun deleteDonorTeam(teamId: String) {
        _donorTeams.value = _donorTeams.value.filter { it.id != teamId }
        saveDonorTeamsLocal()
        deleteDonorTeamFromFirebase(teamId)
    }

    fun joinDonorTeam(teamId: String, memberPhone: String, memberName: String, bloodGroup: String) {
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        _donorTeams.value = _donorTeams.value.map { team ->
            if (team.id == teamId) {
                if (team.members.any { phonesMatch(it.memberPhone, memberPhone) }) {
                    team
                } else {
                    val newMember = TeamMember(memberPhone, memberName, bloodGroup, "Member", dateStr)
                    val updated = team.copy(members = team.members + newMember)
                    pushDonorTeamToFirebase(updated)
                    updated
                }
            } else team
        }
        saveDonorTeamsLocal()
    }

    fun leaveDonorTeam(teamId: String, memberPhone: String) {
        _donorTeams.value = _donorTeams.value.map { team ->
            if (team.id == teamId) {
                val updated = team.copy(members = team.members.filterNot { phonesMatch(it.memberPhone, memberPhone) })
                pushDonorTeamToFirebase(updated)
                updated
            } else team
        }
        saveDonorTeamsLocal()
    }

    fun addMemberToTeam(teamId: String, memberPhone: String, memberName: String, bloodGroup: String, role: String = "Member") {
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        _donorTeams.value = _donorTeams.value.map { team ->
            if (team.id == teamId) {
                val existing = team.members.filterNot { phonesMatch(it.memberPhone, memberPhone) }
                val newMember = TeamMember(memberPhone, memberName, bloodGroup, role, dateStr)
                val updated = team.copy(members = existing + newMember)
                pushDonorTeamToFirebase(updated)
                updated
            } else team
        }
        saveDonorTeamsLocal()
    }

    fun removeMemberFromTeam(teamId: String, memberPhone: String) {
        leaveDonorTeam(teamId, memberPhone)
    }

    fun deleteAmbulance(ambulanceId: String) {
        _ambulances.value = _ambulances.value.filter { it.id != ambulanceId }
        saveAmbulancesLocal()
        deleteAmbulanceFromFirebase(ambulanceId)
    }

    fun deleteAmbulanceBooking(bookingId: String) {
        _ambulanceBookings.value = _ambulanceBookings.value.filter { it.id != bookingId }
        saveBookingsLocal()
        deleteBookingFromFirebase(bookingId)
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                BloodConnectApiClient.deleteAmbulanceBooking(bookingId)
            }
        }
    }

    fun showSystemStatusBarNotification(title: String, message: String) {
        val ctx = appContext ?: return
        try {
            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Create channel for API 26+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channelId = "blood_connect_alerts"
                val channelName = "Blood Connect Alerts"
                val channel = android.app.NotificationChannel(channelId, channelName, android.app.NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alerts for urgent blood requests and notifications"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Build notification
            val builder = androidx.core.app.NotificationCompat.Builder(ctx, "blood_connect_alerts")
                .setSmallIcon(android.R.drawable.stat_notify_chat) // default system icon, always available
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)

            // Show it
            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
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
                role = prefs.getString("user_session_role", "Donor") ?: "Donor",
                walletBalance = prefs.getFloat("user_session_walletBalance", 0.0f).toDouble()
            )
            _currentUser.value = loadedDonor
            
            // Register in list
            val exists = _donors.value.any { it.phone == loadedDonor.phone }
            if (!exists) {
                _donors.value = _donors.value + loadedDonor
            }
        }

        // 1. Load Local Offline Donors, Requests, and Ambulances first
        val donorsStr = prefs.getString("local_donors_list", "") ?: ""
        if (donorsStr.isNotBlank()) {
            _donors.value = deserializeDonors(donorsStr)
        } else {
            _donors.value = MockData.initialDonors
        }

        val requestsStr = prefs.getString("local_requests_list", "") ?: ""
        if (requestsStr.isNotBlank()) {
            _requests.value = deserializeRequests(requestsStr)
        } else {
            _requests.value = MockData.initialRequests
        }

        // Initialize seenRequestIds so we don't duplicate notifications for already loaded requests
        _requests.value.forEach { seenRequestIds.add(it.id) }

        val ambulancesStr = prefs.getString("ambulances_list", "") ?: ""
        if (ambulancesStr.isNotBlank()) {
            _ambulances.value = deserializeAmbulances(ambulancesStr)
        }

        val donorTeamsStr = prefs.getString("donor_teams_list", "") ?: ""
        if (donorTeamsStr.isNotBlank()) {
            val loadedTeams = deserializeDonorTeams(donorTeamsStr)
            if (loadedTeams.isNotEmpty()) {
                _donorTeams.value = loadedTeams
            }
        }

        val messagesStr = prefs.getString("local_messages_list", "") ?: ""
        if (messagesStr.isNotBlank()) {
            _messages.value = deserializeMessages(messagesStr)
        } else {
            _ambulances.value = MockData.initialAmbulances
        }

        val customCountriesSerialized = prefs.getString("custom_countries_list", "") ?: ""
        if (customCountriesSerialized.isNotBlank()) {
            val loaded = customCountriesSerialized.split(";").mapNotNull {
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

        _useMockStats.value = prefs.getBoolean("use_mock_stats", false)
        _mockTotalUsers.value = prefs.getInt("mock_total_users", 80424)
        _mockTotalDonors.value = prefs.getInt("mock_total_donors", 12300)

        _homeNotice.value = prefs.getString("home_notice", _homeNotice.value) ?: _homeNotice.value
        _popupNotice.value = prefs.getString("popup_notice", _popupNotice.value) ?: _popupNotice.value
        _appName.value = prefs.getString("app_name_pref", _appName.value) ?: _appName.value

        _standardCommissionRate.value = prefs.getFloat("standard_commission_rate", 30.0f).toDouble()
        _mPlusCommissionRate.value = prefs.getFloat("mplus_commission_rate", 50.0f).toDouble()
        _bookingAcceptanceFee.value = prefs.getFloat("booking_acceptance_fee", 50.0f).toDouble()

        _bkashNumber.value = prefs.getString("payment_bkash", _bkashNumber.value) ?: _bkashNumber.value
        _nagadNumber.value = prefs.getString("payment_nagad", _nagadNumber.value) ?: _nagadNumber.value
        _rocketNumber.value = prefs.getString("payment_rocket", _rocketNumber.value) ?: _rocketNumber.value
        _googlePlayMerchant.value = prefs.getString("payment_googleplay", _googlePlayMerchant.value) ?: _googlePlayMerchant.value

        _privacyPolicyEn.value = prefs.getString("policy_privacy_en", _privacyPolicyEn.value) ?: _privacyPolicyEn.value
        _privacyPolicyBn.value = prefs.getString("policy_privacy_bn", _privacyPolicyBn.value) ?: _privacyPolicyBn.value
        _privacyPolicyUrl.value = prefs.getString("policy_privacy_url", _privacyPolicyUrl.value) ?: _privacyPolicyUrl.value
        _termsConditionsEn.value = prefs.getString("policy_terms_en", _termsConditionsEn.value) ?: _termsConditionsEn.value
        _termsConditionsBn.value = prefs.getString("policy_terms_bn", _termsConditionsBn.value) ?: _termsConditionsBn.value
        _refundPolicyEn.value = prefs.getString("policy_refund_en", _refundPolicyEn.value) ?: _refundPolicyEn.value
        _refundPolicyBn.value = prefs.getString("policy_refund_bn", _refundPolicyBn.value) ?: _refundPolicyBn.value

        val bookingsStr = prefs.getString("ambulance_bookings_list", "") ?: ""
        _ambulanceBookings.value = deserializeBookings(bookingsStr)

        // Load Subscriptions
        val subscriptionPlansStr = prefs.getString("v9_subscription_plans_list", "") ?: ""
        var loadedPlans = deserializeSubscriptionPlans(subscriptionPlansStr)
        if (loadedPlans.isEmpty()) {
            loadedPlans = listOf(
                V9SubscriptionPlan("plan_basic", "V9 Starter Pack", "ভি৯ স্টার্টার প্যাক", 150.0, 30, "Get basic access & priority support", "বেসিক অ্যাক্সেস এবং অগ্রাধিকার সাপোর্ট"),
                V9SubscriptionPlan("plan_standard", "V9 Pro Pack", "ভি৯ প্রো প্যাক", 350.0, 90, "Get full system premium access", "সম্পূর্ণ সিস্টেম প্রিমিয়াম অ্যাক্সেস"),
                V9SubscriptionPlan("plan_premium", "V9 Ultimate VIP Pack", "ভি৯ আল্টিমেট ভিআইপি প্যাক", 999.0, 365, "Lifetime special badge and VIP status", "আজীবন বিশেষ ব্যাজ এবং ভিআইপি স্ট্যাটাস")
            )
            prefs.edit().putString("v9_subscription_plans_list", serializeSubscriptionPlans(loadedPlans)).apply()
        }
        _subscriptionPlans.value = loadedPlans

        val userSubsStr = prefs.getString("v9_user_subscriptions_list", "") ?: ""
        _userSubscriptions.value = deserializeUserSubscriptions(userSubsStr)

        prefsInitialized = true

        // 2. NOW setup remote base URL and trigger async sync securely
        var savedUrl = prefs.getString("remote_api_url", "") ?: ""
        var savedKey = prefs.getString("remote_api_key", "") ?: ""

        val buildUrl = com.example.BuildConfig.SUPABASE_URL
        val buildKey = com.example.BuildConfig.SUPABASE_PUBLISHABLE_KEY

        val isBuildUrlValid = buildUrl.isNotBlank() && buildUrl != "https://your-supabase-url.supabase.co"
        val isBuildKeyValid = buildKey.isNotBlank() && buildKey != "your-supabase-anon-key"

        if (savedUrl.isBlank() || savedUrl == "https://your-supabase-url.supabase.co" || (isBuildUrlValid && savedUrl != buildUrl)) {
            savedUrl = if (isBuildUrlValid) buildUrl else "https://hpturyhypcplydvtslpq.supabase.co"
            prefs.edit().putString("remote_api_url", savedUrl).apply()
        }
        if (savedKey.isBlank() || savedKey == "your-supabase-anon-key" || (isBuildKeyValid && savedKey != buildKey)) {
            savedKey = if (isBuildKeyValid) buildKey else "sb_publishable_vNUUU1OamY9SExJb78MoCQ_CwmyEu5e"
            prefs.edit().putString("remote_api_key", savedKey).apply()
        }
        _remoteApiKey.value = savedKey

        if (savedUrl.isNotBlank()) {
            BloodConnectApiClient.updateBaseUrl(savedUrl, savedKey)
            triggerRemoteSync()
        }

        // Initialize Firebase Realtime Database sync
        initFirebaseDatabase()
    }

    private var firebaseDb: com.google.firebase.database.FirebaseDatabase? = null
    private var isFirebaseListening = false

    private fun cleanKey(raw: String): String {
        val sanitized = raw.replace(Regex("[.#$\\[\\]/+]"), "_").trim()
        return if (sanitized.isBlank()) "item_${System.currentTimeMillis()}" else sanitized
    }

    private fun parseDonorFromSnapshot(child: com.google.firebase.database.DataSnapshot): BloodDonor? {
        try {
            val direct = child.getValue(BloodDonor::class.java)
            if (direct != null) {
                val safeId = if (direct.id.isBlank()) (child.key ?: "") else direct.id
                return direct.copy(id = safeId)
            }
        } catch (e: Exception) {
            // Fallback to manual map parsing if direct deserialization fails
        }

        val map = child.value as? Map<*, *> ?: return null

        fun getString(key: String, default: String = ""): String =
            (map[key] as? String) ?: (map[key]?.toString()) ?: default

        fun getBool(vararg keys: String, default: Boolean = true): Boolean {
            for (k in keys) {
                val v = map[k] ?: continue
                when (v) {
                    is Boolean -> return v
                    is String -> return v.equals("true", ignoreCase = true) || v == "1"
                    is Number -> return v.toInt() != 0
                }
            }
            return default
        }

        fun getInt(key: String, default: Int = 0): Int {
            val v = map[key] ?: return default
            return when (v) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: default
                else -> default
            }
        }

        fun getDouble(key: String, default: Double = 0.0): Double {
            val v = map[key] ?: return default
            return when (v) {
                is Number -> v.toDouble()
                is String -> v.toDoubleOrNull() ?: default
                else -> default
            }
        }

        val id = getString("id", child.key ?: "")
        val name = getString("name", "Donor")
        val phone = getString("phone")
        val bloodGroup = getString("bloodGroup", "O+")
        val email = getString("email")
        val district = getString("district", "Bangladesh")
        val upazila = getString("upazila")
        val village = getString("village")
        val lastDonationDate = getString("lastDonationDate", "Available")
        val isAvailable = getBool("isAvailable", "available", default = true)
        val isApproved = getBool("isApproved", "approved", default = true)
        val donationCount = getInt("donationCount", 0)
        val isGoogleUser = getBool("isGoogleUser", "googleUser", default = false)
        val country = getString("country", "Bangladesh")
        val userId = getString("userId")
        val isWarning = getBool("isWarning", "warning", default = false)
        val warningReason = getString("warningReason")
        val role = getString("role", "Donor")
        val walletBalance = getDouble("walletBalance", 0.0)
        val password = getString("password")

        return BloodDonor(
            id = id,
            name = name,
            bloodGroup = bloodGroup,
            phone = phone,
            email = email,
            district = district,
            upazila = upazila,
            village = village,
            lastDonationDate = lastDonationDate,
            isAvailable = isAvailable,
            isApproved = isApproved,
            donationCount = donationCount,
            isGoogleUser = isGoogleUser,
            country = country,
            userId = userId,
            isWarning = isWarning,
            warningReason = warningReason,
            role = role,
            walletBalance = walletBalance,
            password = password
        )
    }

    private fun parseChatMessageFromSnapshot(child: com.google.firebase.database.DataSnapshot): ChatMessage? {
        try {
            val direct = child.getValue(ChatMessage::class.java)
            if (direct != null) {
                val safeId = if (direct.id.isBlank()) (child.key ?: "") else direct.id
                return direct.copy(id = safeId)
            }
        } catch (e: Exception) {
            // Fallback to map parsing
        }

        val map = child.value as? Map<*, *> ?: return null

        fun getString(key: String, default: String = ""): String =
            (map[key] as? String) ?: (map[key]?.toString()) ?: default

        fun getBool(key: String, default: Boolean = false): Boolean {
            val v = map[key] ?: return default
            return when (v) {
                is Boolean -> v
                is String -> v.equals("true", ignoreCase = true) || v == "1"
                is Number -> v.toInt() != 0
                else -> default
            }
        }

        val id = getString("id", child.key ?: "")
        val senderPhone = getString("senderPhone")
        val senderName = getString("senderName")
        val receiverPhone = getString("receiverPhone")
        val receiverName = getString("receiverName")
        val message = getString("message")
        val timestamp = getString("timestamp")
        val isRead = getBool("isRead", false)

        if (id.isBlank()) return null

        return ChatMessage(
            id = id,
            senderPhone = senderPhone,
            senderName = senderName,
            receiverPhone = receiverPhone,
            receiverName = receiverName,
            message = message,
            timestamp = timestamp,
            isRead = isRead
        )
    }

    fun getDb(): com.google.firebase.database.FirebaseDatabase? {
        if (firebaseDb != null) return firebaseDb
        try {
            appContext?.let { ctx ->
                if (com.google.firebase.FirebaseApp.getApps(ctx).isEmpty()) {
                    com.google.firebase.FirebaseApp.initializeApp(ctx)
                }
            }
            firebaseDb = com.google.firebase.database.FirebaseDatabase.getInstance("https://alif-blood-bank-2bc68-default-rtdb.firebaseio.com")
        } catch (e: Exception) {
            try {
                firebaseDb = com.google.firebase.database.FirebaseDatabase.getInstance("https://alif-blood-bank-2bc68-default-rtdb.asia-southeast1.firebasedatabase.app")
            } catch (e2: Exception) {
                try {
                    firebaseDb = com.google.firebase.database.FirebaseDatabase.getInstance()
                } catch (e3: Exception) {
                    Log.e("BloodConnectRepo", "Firebase DB init error: ${e3.message}")
                }
            }
        }
        return firebaseDb
    }

    private fun parseDonorTeamFromSnapshot(snapshot: com.google.firebase.database.DataSnapshot): DonorTeam? {
        return try {
            val id = snapshot.child("id").getValue(String::class.java) ?: snapshot.key ?: ""
            val teamName = snapshot.child("teamName").getValue(String::class.java) ?: ""
            val leaderName = snapshot.child("leaderName").getValue(String::class.java) ?: ""
            val leaderPhone = snapshot.child("leaderPhone").getValue(String::class.java) ?: ""
            val district = snapshot.child("district").getValue(String::class.java) ?: ""
            val upazila = snapshot.child("upazila").getValue(String::class.java) ?: ""
            val description = snapshot.child("description").getValue(String::class.java) ?: ""
            val isApproved = snapshot.child("isApproved").getValue(Boolean::class.java) ?: false
            val status = snapshot.child("status").getValue(String::class.java) ?: "Pending"
            val createdAt = snapshot.child("createdAt").getValue(String::class.java) ?: ""
            val country = snapshot.child("country").getValue(String::class.java) ?: "Bangladesh"

            val membersList = mutableListOf<TeamMember>()
            val membersSnap = snapshot.child("members")
            if (membersSnap.exists()) {
                for (mChild in membersSnap.children) {
                    val memberPhone = mChild.child("memberPhone").getValue(String::class.java) ?: ""
                    val memberName = mChild.child("memberName").getValue(String::class.java) ?: ""
                    val bloodGroup = mChild.child("bloodGroup").getValue(String::class.java) ?: ""
                    val role = mChild.child("role").getValue(String::class.java) ?: "Member"
                    val joinedAt = mChild.child("joinedAt").getValue(String::class.java) ?: ""
                    if (memberPhone.isNotBlank()) {
                        membersList.add(TeamMember(memberPhone, memberName, bloodGroup, role, joinedAt))
                    }
                }
            }

            if (id.isBlank()) null else DonorTeam(
                id = id,
                teamName = teamName,
                leaderName = leaderName,
                leaderPhone = leaderPhone,
                district = district,
                upazila = upazila,
                description = description,
                isApproved = isApproved,
                status = status,
                members = membersList,
                createdAt = createdAt,
                country = country
            )
        } catch (e: Exception) {
            null
        }
    }

    fun initFirebaseDatabase() {
        val db = getDb() ?: return
        if (!isFirebaseListening) {
            isFirebaseListening = true
            listenToFirebaseRealtimeDatabase()
        }
    }

    fun pushDonorToFirebase(donor: BloodDonor) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val safeId = donor.id.ifBlank { "u_${donor.phone}" }
                val finalDonor = donor.copy(id = safeId)
                val key = cleanKey(safeId)
                db.getReference("donors").child(key).setValue(finalDonor)
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase push donor error: ${e.message}")
            }
        }
    }

    fun cleanExpiredRequests() {
        val sevenDaysAgoMs = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000L)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        val validRequests = _requests.value.filter { req ->
            var isExpired = false
            if (req.id.startsWith("r_")) {
                val ts = req.id.removePrefix("r_").toLongOrNull()
                if (ts != null && ts > 0) {
                    if (ts < sevenDaysAgoMs) {
                        isExpired = true
                    }
                }
            }
            if (!isExpired && req.dateRequested.isNotBlank()) {
                try {
                    val parsedDate = sdf.parse(req.dateRequested)
                    if (parsedDate != null) {
                        if (parsedDate.time < (sevenDaysAgoMs - 86400000L)) {
                            isExpired = true
                        }
                    }
                } catch (e: Exception) {
                    // keep if unparseable
                }
            }
            !isExpired
        }

        if (validRequests.size != _requests.value.size) {
            val expired = _requests.value.filter { !validRequests.contains(it) }
            _requests.value = validRequests
            saveRequestsLocal()
            appScope.launch {
                try {
                    val db = getDb() ?: return@launch
                    expired.forEach { req ->
                        val safeId = req.id.ifBlank { "r_${System.currentTimeMillis()}" }
                        val key = cleanKey(safeId)
                        db.getReference("requests").child(key).removeValue()
                    }
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error removing expired requests from Firebase: ${e.message}")
                }
            }
        }
    }

    fun pushRequestToFirebase(request: BloodRequest) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val safeId = request.id.ifBlank { "r_${System.currentTimeMillis()}" }
                val finalRequest = request.copy(id = safeId)
                val key = cleanKey(safeId)
                db.getReference("requests").child(key).setValue(finalRequest)
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase push request error: ${e.message}")
            }
        }
    }

    fun pushAmbulanceBookingToFirebase(booking: AmbulanceBooking) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val safeId = booking.id.ifBlank { "b_${System.currentTimeMillis()}" }
                val finalBooking = booking.copy(id = safeId)
                val key = cleanKey(safeId)
                db.getReference("ambulance_bookings").child(key).setValue(finalBooking)
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase push booking error: ${e.message}")
            }
        }
    }

    fun pushAmbulanceToFirebase(ambulance: Ambulance) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val safeId = ambulance.id.ifBlank { "amb_${System.currentTimeMillis()}" }
                val finalAmb = ambulance.copy(id = safeId)
                val key = cleanKey(safeId)
                db.getReference("ambulances").child(key).setValue(finalAmb)
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase push ambulance error: ${e.message}")
            }
        }
    }

    fun deleteDonorFromFirebase(donorId: String) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val key = cleanKey(donorId)
                db.getReference("donors").child(key).removeValue()
                if (key != donorId) {
                    db.getReference("donors").child(donorId).removeValue()
                }
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase delete donor error: ${e.message}")
            }
        }
    }

    fun deleteRequestFromFirebase(requestId: String) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val key = cleanKey(requestId)
                db.getReference("requests").child(key).removeValue()
                if (key != requestId) {
                    db.getReference("requests").child(requestId).removeValue()
                }
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase delete request error: ${e.message}")
            }
        }
    }

    fun deleteAmbulanceFromFirebase(ambulanceId: String) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val key = cleanKey(ambulanceId)
                db.getReference("ambulances").child(key).removeValue()
                if (key != ambulanceId) {
                    db.getReference("ambulances").child(ambulanceId).removeValue()
                }
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase delete ambulance error: ${e.message}")
            }
        }
    }

    fun deleteBookingFromFirebase(bookingId: String) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val key = cleanKey(bookingId)
                db.getReference("ambulance_bookings").child(key).removeValue()
                if (key != bookingId) {
                    db.getReference("ambulance_bookings").child(bookingId).removeValue()
                }
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase delete booking error: ${e.message}")
            }
        }
    }

    fun pushChatMessageToFirebase(msg: ChatMessage) {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val safeId = msg.id.ifBlank { "m_${System.currentTimeMillis()}" }
                val finalMsg = msg.copy(id = safeId)
                val key = cleanKey(safeId)
                db.getReference("chat_messages").child(key).setValue(finalMsg)
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase push chat error: ${e.message}")
            }
        }
    }

    fun pushAppConfigToFirebase() {
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val prefs = appContext?.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
                val lastUpdated = prefs?.getLong("config_last_updated", System.currentTimeMillis()) ?: System.currentTimeMillis()
                val configMap = mapOf(
                    "app_name" to _appName.value,
                    "home_notice" to _homeNotice.value,
                    "popup_notice" to _popupNotice.value,
                    "bkash_number" to _bkashNumber.value,
                    "nagad_number" to _nagadNumber.value,
                    "rocket_number" to _rocketNumber.value,
                    "google_play_merchant" to _googlePlayMerchant.value,
                    "booking_acceptance_fee" to _bookingAcceptanceFee.value,
                    "standard_commission_rate" to _standardCommissionRate.value,
                    "mplus_commission_rate" to _mPlusCommissionRate.value,
                    "privacy_policy_en" to _privacyPolicyEn.value,
                    "privacy_policy_bn" to _privacyPolicyBn.value,
                    "privacy_policy_url" to _privacyPolicyUrl.value,
                    "terms_en" to _termsConditionsEn.value,
                    "terms_bn" to _termsConditionsBn.value,
                    "refund_en" to _refundPolicyEn.value,
                    "refund_bn" to _refundPolicyBn.value,
                    "custom_ads_enabled" to _customAdsEnabled.value,
                    "custom_ad_network_name" to _customAdNetworkName.value,
                    "custom_ad_title" to _customAdTitle.value,
                    "custom_ad_banner_url" to _customAdBannerUrl.value,
                    "custom_ad_target_url" to _customAdTargetUrl.value,
                    "custom_ad_target_countries" to _customAdTargetCountries.value,
                    "custom_ad_configs_list" to serializeAds(_customAdConfigs.value),
                    "admob_enabled" to _adMobEnabled.value,
                    "admob_app_id" to _adMobAppId.value,
                    "admob_banner_id" to _adMobBannerId.value,
                    "admob_interstitial_id" to _adMobInterstitialId.value,
                    "admob_native_id" to _adMobNativeId.value,
                    "email_notify_enabled" to _emailNotifyEnabled.value,
                    "smtp_host" to _smtpHost.value,
                    "smtp_port" to _smtpPort.value,
                    "smtp_username" to _smtpUsername.value,
                    "smtp_password" to _smtpPassword.value,
                    "email_subject_template" to _emailSubjectTemplate.value,
                    "email_body_template" to _emailBodyTemplate.value,
                    "use_mock_stats" to _useMockStats.value,
                    "mock_total_users" to _mockTotalUsers.value,
                    "mock_total_donors" to _mockTotalDonors.value,
                    "updated_at" to lastUpdated
                )
                db.getReference("app_config").setValue(configMap)
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase push config error: ${e.message}")
            }
        }
    }

    fun pushInitialDataToFirebaseIfEmpty() {
        val db = getDb() ?: return
        appScope.launch {
            try {
                _donors.value.forEach { donor ->
                    pushDonorToFirebase(donor)
                }
                _requests.value.forEach { req ->
                    pushRequestToFirebase(req)
                }
                _messages.value.forEach { msg ->
                    pushChatMessageToFirebase(msg)
                }
                _ambulances.value.forEach { amb ->
                    pushAmbulanceToFirebase(amb)
                }
                _ambulanceBookings.value.forEach { booking ->
                    pushAmbulanceBookingToFirebase(booking)
                }
                pushAppConfigToFirebase()
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Firebase initial sync error: ${e.message}")
            }
        }
    }

    private fun listenToFirebaseRealtimeDatabase() {
        val db = getDb() ?: return

        // Connection Status Presence Listener
        db.getReference(".info/connected").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                _isFirebaseConnected.value = connected
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Listen to App Config
        db.getReference("app_config").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    val remoteUpdatedAt = snapshot.child("updated_at").getValue(Long::class.java) ?: 0L
                    val prefs = appContext?.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
                    val localUpdatedAt = prefs?.getLong("config_last_updated", 0L) ?: 0L

                    if (localUpdatedAt > 0L && remoteUpdatedAt < localUpdatedAt) {
                        pushAppConfigToFirebase()
                    } else if (snapshot.exists() && snapshot.childrenCount > 0) {
                        val name = snapshot.child("app_name").getValue(String::class.java)
                        if (name != null) _appName.value = name

                        val homeNot = snapshot.child("home_notice").getValue(String::class.java)
                        if (homeNot != null) _homeNotice.value = homeNot

                        val popNot = snapshot.child("popup_notice").getValue(String::class.java)
                        if (popNot != null) _popupNotice.value = popNot

                        val bkash = snapshot.child("bkash_number").getValue(String::class.java)
                        if (bkash != null) _bkashNumber.value = bkash

                        val nagad = snapshot.child("nagad_number").getValue(String::class.java)
                        if (nagad != null) _nagadNumber.value = nagad

                        val rocket = snapshot.child("rocket_number").getValue(String::class.java)
                        if (rocket != null) _rocketNumber.value = rocket

                        val googlePlay = snapshot.child("google_play_merchant").getValue(String::class.java)
                        if (googlePlay != null) _googlePlayMerchant.value = googlePlay

                        val feeObj = snapshot.child("booking_acceptance_fee").value
                        if (feeObj != null) {
                            val fee = when (feeObj) {
                                is Number -> feeObj.toDouble()
                                is String -> feeObj.toDoubleOrNull()
                                else -> null
                            }
                            if (fee != null) _bookingAcceptanceFee.value = fee
                        }

                        val stdCommObj = snapshot.child("standard_commission_rate").value
                        if (stdCommObj != null) {
                            val rate = when (stdCommObj) {
                                is Number -> stdCommObj.toDouble()
                                is String -> stdCommObj.toDoubleOrNull()
                                else -> null
                            }
                            if (rate != null) _standardCommissionRate.value = rate
                        }

                        val mplusCommObj = snapshot.child("mplus_commission_rate").value
                        if (mplusCommObj != null) {
                            val rate = when (mplusCommObj) {
                                is Number -> mplusCommObj.toDouble()
                                is String -> mplusCommObj.toDoubleOrNull()
                                else -> null
                            }
                            if (rate != null) _mPlusCommissionRate.value = rate
                        }

                        val privEn = snapshot.child("privacy_policy_en").getValue(String::class.java)
                        if (privEn != null) _privacyPolicyEn.value = privEn

                        val privBn = snapshot.child("privacy_policy_bn").getValue(String::class.java)
                        if (privBn != null) _privacyPolicyBn.value = privBn

                        val privUrl = snapshot.child("privacy_policy_url").getValue(String::class.java)
                        if (privUrl != null) _privacyPolicyUrl.value = privUrl

                        val termsEn = snapshot.child("terms_en").getValue(String::class.java)
                        if (termsEn != null) _termsConditionsEn.value = termsEn

                        val termsBn = snapshot.child("terms_bn").getValue(String::class.java)
                        if (termsBn != null) _termsConditionsBn.value = termsBn

                        val refundEn = snapshot.child("refund_en").getValue(String::class.java)
                        if (refundEn != null) _refundPolicyEn.value = refundEn

                        val refundBn = snapshot.child("refund_bn").getValue(String::class.java)
                        if (refundBn != null) _refundPolicyBn.value = refundBn

                        val customEnabled = snapshot.child("custom_ads_enabled").getValue(Boolean::class.java)
                        if (customEnabled != null) _customAdsEnabled.value = customEnabled

                        val netName = snapshot.child("custom_ad_network_name").getValue(String::class.java)
                        if (netName != null) _customAdNetworkName.value = netName

                        val adTitle = snapshot.child("custom_ad_title").getValue(String::class.java)
                        if (adTitle != null) _customAdTitle.value = adTitle

                        val bannerUrl = snapshot.child("custom_ad_banner_url").getValue(String::class.java)
                        if (bannerUrl != null) _customAdBannerUrl.value = bannerUrl

                        val targetUrl = snapshot.child("custom_ad_target_url").getValue(String::class.java)
                        if (targetUrl != null) _customAdTargetUrl.value = targetUrl

                        val targetCountries = snapshot.child("custom_ad_target_countries").getValue(String::class.java)
                        if (targetCountries != null) _customAdTargetCountries.value = targetCountries

                        val serializedAdList = snapshot.child("custom_ad_configs_list").getValue(String::class.java)
                        if (!serializedAdList.isNullOrEmpty()) {
                            _customAdConfigs.value = deserializeAds(serializedAdList)
                        }

                        val admobEnabled = snapshot.child("admob_enabled").getValue(Boolean::class.java)
                        if (admobEnabled != null) _adMobEnabled.value = admobEnabled

                        val admobApp = snapshot.child("admob_app_id").getValue(String::class.java)
                        if (admobApp != null) _adMobAppId.value = admobApp

                        val admobBanner = snapshot.child("admob_banner_id").getValue(String::class.java)
                        if (admobBanner != null) _adMobBannerId.value = admobBanner

                        val admobInter = snapshot.child("admob_interstitial_id").getValue(String::class.java)
                        if (admobInter != null) _adMobInterstitialId.value = admobInter

                        val admobNative = snapshot.child("admob_native_id").getValue(String::class.java)
                        if (admobNative != null) _adMobNativeId.value = admobNative

                        val emailEnabled = snapshot.child("email_notify_enabled").getValue(Boolean::class.java)
                        if (emailEnabled != null) _emailNotifyEnabled.value = emailEnabled

                        val sHost = snapshot.child("smtp_host").getValue(String::class.java)
                        if (sHost != null) _smtpHost.value = sHost

                        val sPort = snapshot.child("smtp_port").getValue(String::class.java)
                        if (sPort != null) _smtpPort.value = sPort

                        val sUser = snapshot.child("smtp_username").getValue(String::class.java)
                        if (sUser != null) _smtpUsername.value = sUser

                        val sPass = snapshot.child("smtp_password").getValue(String::class.java)
                        if (sPass != null) _smtpPassword.value = sPass

                        val eSubject = snapshot.child("email_subject_template").getValue(String::class.java)
                        if (eSubject != null) _emailSubjectTemplate.value = eSubject

                        val eBody = snapshot.child("email_body_template").getValue(String::class.java)
                        if (eBody != null) _emailBodyTemplate.value = eBody

                        val useMock = snapshot.child("use_mock_stats").getValue(Boolean::class.java)
                        if (useMock != null) _useMockStats.value = useMock

                        val mockUsers = snapshot.child("mock_total_users").getValue(Int::class.java)
                        if (mockUsers != null) _mockTotalUsers.value = mockUsers

                        val mockDonors = snapshot.child("mock_total_donors").getValue(Int::class.java)
                        if (mockDonors != null) _mockTotalDonors.value = mockDonors

                        saveAppConfigLocal()
                    } else {
                        // Seed Firebase config only if config node is missing
                        pushAppConfigToFirebase()
                    }
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error listening to app_config: ${e.message}")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Listen to Broadcast Notifications
        db.getReference("broadcast_notifications").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        val list = mutableListOf<DonationNotification>()
                        for (child in snapshot.children) {
                            val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                            val titleEn = child.child("titleEn").getValue(String::class.java) ?: ""
                            val titleBn = child.child("titleBn").getValue(String::class.java) ?: ""
                            val messageEn = child.child("messageEn").getValue(String::class.java) ?: ""
                            val messageBn = child.child("messageBn").getValue(String::class.java) ?: ""
                            val timestamp = child.child("timestamp").getValue(String::class.java) ?: "Just now"
                            val type = child.child("type").getValue(String::class.java) ?: "URGENT"
                            val country = child.child("country").getValue(String::class.java) ?: "Bangladesh"
                            val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                            if (id.isNotBlank()) {
                                list.add(
                                    DonationNotification(
                                        id = id,
                                        titleEn = titleEn,
                                        titleBn = titleBn,
                                        messageEn = messageEn,
                                        messageBn = messageBn,
                                        timestamp = timestamp,
                                        isRead = isRead,
                                        type = type,
                                        country = country
                                    )
                                )
                            }
                        }
                        if (list.isNotEmpty()) {
                            val combined = (list + _notifications.value).distinctBy { it.id }
                            _notifications.value = combined
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error reading notifications: ${e.message}")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Listen to Donors
        db.getReference("donors").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    val list = mutableListOf<BloodDonor>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val parsed = parseDonorFromSnapshot(child)
                            if (parsed != null && parsed.id.isNotBlank()) {
                                list.add(parsed)
                            }
                        }
                    }
                    _donors.value = list
                    saveDonorsLocal()

                    _currentUser.value?.let { current ->
                        val remoteCurrent = list.find { 
                            phonesMatch(it.phone, current.phone) || 
                            (current.email.isNotBlank() && it.email.equals(current.email, ignoreCase = true))
                        }
                        if (remoteCurrent != null && remoteCurrent != current) {
                            setCurrentUser(remoteCurrent)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error listening to Firebase donors: ${e.message}")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Listen to Requests
        db.getReference("requests").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    val list = mutableListOf<BloodRequest>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val req = child.getValue(BloodRequest::class.java)
                            if (req != null) {
                                val safeId = if (req.id.isBlank()) (child.key ?: "") else req.id
                                val finalReq = req.copy(id = safeId)
                                if (finalReq.id.isNotBlank()) {
                                    list.add(finalReq)
                                }
                            }
                        }
                    }
                    _requests.value = list
                    cleanExpiredRequests()
                    saveRequestsLocal()
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error listening to Firebase requests: ${e.message}")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Listen to Chat Messages
        db.getReference("chat_messages").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    val list = mutableListOf<ChatMessage>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val parsed = parseChatMessageFromSnapshot(child)
                            if (parsed != null && parsed.id.isNotBlank()) {
                                list.add(parsed)
                            }
                        }
                    }

                    // Check for new incoming unread messages for current user
                    val currentUsersPhone = _currentUser.value?.phone ?: ""
                    fun cleanPhone(p: String): String = p.replace(Regex("[^0-9]"), "").takeLast(10)
                    val myClean = cleanPhone(currentUsersPhone)

                    if (myClean.isNotEmpty()) {
                        val previousIds = _messages.value.map { it.id }.toSet()
                        val newIncomingUnread = list.filter { msg ->
                            !msg.isRead && 
                            !previousIds.contains(msg.id) &&
                            cleanPhone(msg.receiverPhone) == myClean &&
                            cleanPhone(msg.senderPhone) != myClean
                        }
                        for (msg in newIncomingUnread) {
                            val title = if (msg.senderName.isNotBlank()) msg.senderName else "নতুন মেসেজ 💬"
                            showSystemStatusBarNotification(title, msg.message)
                        }
                    }

                    _messages.value = list
                    saveMessagesLocal()
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error listening to Firebase chat: ${e.message}")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Listen to Ambulances
        db.getReference("ambulances").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    val list = mutableListOf<Ambulance>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val amb = child.getValue(Ambulance::class.java)
                            if (amb != null) {
                                val safeId = if (amb.id.isBlank()) (child.key ?: "") else amb.id
                                val finalAmb = amb.copy(id = safeId)
                                if (finalAmb.id.isNotBlank()) {
                                    list.add(finalAmb)
                                }
                            }
                        }
                    }
                    _ambulances.value = list
                    saveAmbulancesLocal()
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error listening to Firebase ambulances: ${e.message}")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Listen to Donor Teams
        db.getReference("donor_teams").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    val list = mutableListOf<DonorTeam>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val team = parseDonorTeamFromSnapshot(child)
                            if (team != null && team.id.isNotBlank()) {
                                list.add(team)
                            }
                        }
                    }
                    _donorTeams.value = list
                    saveDonorTeamsLocal()
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error listening to Firebase donor_teams: ${e.message}")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        // Listen to Ambulance Bookings
        db.getReference("ambulance_bookings").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    val list = mutableListOf<AmbulanceBooking>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val book = child.getValue(AmbulanceBooking::class.java)
                            if (book != null) {
                                val safeId = if (book.id.isBlank()) (child.key ?: "") else book.id
                                val finalBook = book.copy(id = safeId)
                                if (finalBook.id.isNotBlank()) {
                                    list.add(finalBook)
                                }
                            }
                        }
                    }
                    _ambulanceBookings.value = list
                    saveBookingsLocal()
                } catch (e: Exception) {
                    Log.e("BloodConnectRepo", "Error listening to Firebase bookings: ${e.message}")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
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
                if (remoteDonors != null) {
                    val currentLocal = _donors.value
                    val mergedDonors = remoteDonors + currentLocal.filter { local ->
                        remoteDonors.none { remote -> remote.id == local.id || remote.phone == local.phone }
                    }
                    _donors.value = mergedDonors
                    saveDonorsLocal()
                    
                    // Keep local current user state synchronized
                    _currentUser.value?.let { current ->
                        val remoteCurrent = mergedDonors.find { it.phone == current.phone }
                        if (remoteCurrent != null) {
                            _currentUser.value = remoteCurrent
                        }
                    }
                }
            } else {
                _syncError.value = donorsResult.exceptionOrNull()?.message ?: "Donors Sync failed"
            }

            // 2. Fetch Requests
            val requestsResult = BloodConnectApiClient.fetchRequests()
            if (requestsResult.isSuccess) {
                val remoteRequests = requestsResult.getOrNull()
                if (remoteRequests != null) {
                    // Trigger real system notifications for any new requests fetched from the cloud
                    val currentLang = language.value
                    remoteRequests.forEach { req ->
                        if (!seenRequestIds.contains(req.id)) {
                            seenRequestIds.add(req.id)
                            val title = if (currentLang == AppLanguage.BAN) "জরুরি রক্তের অনুরোধ: ${req.bloodGroup}" else "URGENT request for ${req.bloodGroup}"
                            val msg = if (currentLang == AppLanguage.BAN) "${req.patientName} এর ${req.hospitalName}-এ ${req.bloodGroup} রক্তের প্রয়োজন।" else "${req.patientName} needs ${req.bloodGroup} blood at ${req.hospitalName}."
                            showSystemStatusBarNotification(title, msg)
                        }
                    }

                    val currentLocal = _requests.value
                    val mergedRequests = remoteRequests + currentLocal.filter { local ->
                        remoteRequests.none { remote -> remote.id == local.id }
                    }
                    _requests.value = mergedRequests
                    saveRequestsLocal()
                }
            } else {
                val err = requestsResult.exceptionOrNull()?.message ?: "Requests Sync failed"
                _syncError.value = if (_syncError.value == null) err else "${_syncError.value}\n$err"
            }

            // 3. Fetch Ambulance Bookings
            val bookingsResult = BloodConnectApiClient.fetchAmbulanceBookings()
            if (bookingsResult.isSuccess) {
                val remoteBookings = bookingsResult.getOrNull()
                if (remoteBookings != null) {
                    val currentLocal = _ambulanceBookings.value
                    val mergedBookings = remoteBookings + currentLocal.filter { local ->
                        remoteBookings.none { remote -> remote.id == local.id }
                    }
                    _ambulanceBookings.value = mergedBookings
                    saveBookingsLocal()
                }
            }

            // 4. Fetch Chat Messages
            val messagesResult = BloodConnectApiClient.fetchChatMessages()
            if (messagesResult.isSuccess) {
                val remoteMessages = messagesResult.getOrNull()
                if (remoteMessages != null) {
                    val currentLocal = _messages.value
                    val mergedMessages = remoteMessages + currentLocal.filter { local ->
                        remoteMessages.none { remote -> remote.id == local.id }
                    }
                    _messages.value = mergedMessages
                    saveMessagesLocal()
                }
            }

            // 5. Fetch AppConfig
            val configResult = BloodConnectApiClient.fetchAppConfig()
            if (configResult.isSuccess) {
                val remoteConfig = configResult.getOrNull()
                if (remoteConfig != null) {
                    val prefs = appContext?.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
                    val localUpdatedAt = prefs?.getLong("config_last_updated", 0L) ?: 0L
                    if (localUpdatedAt == 0L) {
                        if (remoteConfig.app_name.isNotBlank()) _appName.value = remoteConfig.app_name
                        if (remoteConfig.home_notice.isNotBlank()) _homeNotice.value = remoteConfig.home_notice
                        if (remoteConfig.popup_notice.isNotBlank()) _popupNotice.value = remoteConfig.popup_notice
                        if (remoteConfig.privacy_policy_en.isNotBlank()) _privacyPolicyEn.value = remoteConfig.privacy_policy_en
                        if (remoteConfig.privacy_policy_bn.isNotBlank()) _privacyPolicyBn.value = remoteConfig.privacy_policy_bn
                        if (remoteConfig.privacy_policy_url.isNotBlank()) _privacyPolicyUrl.value = remoteConfig.privacy_policy_url
                        if (remoteConfig.terms_conditions_en.isNotBlank()) _termsConditionsEn.value = remoteConfig.terms_conditions_en
                        if (remoteConfig.terms_conditions_bn.isNotBlank()) _termsConditionsBn.value = remoteConfig.terms_conditions_bn
                        if (remoteConfig.refund_policy_en.isNotBlank()) _refundPolicyEn.value = remoteConfig.refund_policy_en
                        if (remoteConfig.refund_policy_bn.isNotBlank()) _refundPolicyBn.value = remoteConfig.refund_policy_bn
                        saveAppConfigLocal()
                    } else {
                        pushAppConfigToRemote()
                    }
                }
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
                    isGoogleUser = false,
                    password = password,
                    role = "Admin"
                )
                setCurrentUser(newAdmin)
                _donors.value = _donors.value + newAdmin
                pushDonorToFirebase(newAdmin)
            }
            return true
        }

        val cleanInput = if (username.isNotBlank()) username.trim() else email.trim()

        // Find existing donor if any
        var existing = _donors.value.find { 
            (cleanInput.isNotBlank() && (it.email.equals(cleanInput, ignoreCase = true) || it.phone.equals(cleanInput, ignoreCase = true) || phonesMatch(it.phone, cleanInput))) ||
            (username.isNotBlank() && (it.phone == username || phonesMatch(it.phone, username) || it.email.equals(username, ignoreCase = true))) || 
            (email.isNotBlank() && (it.email.equals(email, ignoreCase = true) || phonesMatch(it.phone, email)))
        }

        // Fallback: check ambulances list if user registered an ambulance
        if (existing == null && (username.isNotBlank() || email.isNotBlank() || cleanInput.isNotBlank())) {
            val amb = _ambulances.value.find { 
                (cleanInput.isNotBlank() && (it.phone == cleanInput || phonesMatch(it.phone, cleanInput))) ||
                (username.isNotBlank() && (it.phone == username || phonesMatch(it.phone, username))) ||
                (email.isNotBlank() && phonesMatch(it.phone, email))
            }
            if (amb != null) {
                val randId = "ABB-${(10000..99999).random()}"
                val ambDonor = BloodDonor(
                    id = "u_${System.currentTimeMillis()}",
                    name = if (amb.ownerName.isNotBlank()) amb.ownerName else amb.serviceName,
                    bloodGroup = "N/A",
                    phone = amb.phone,
                    email = email,
                    district = amb.district,
                    upazila = amb.upazila,
                    lastDonationDate = "Available",
                    isAvailable = true,
                    isApproved = true,
                    donationCount = 0,
                    country = amb.country,
                    userId = randId,
                    role = "Ambulance",
                    password = password
                )
                _donors.value = _donors.value + ambDonor
                saveDonorsLocal()
                pushDonorToFirebase(ambDonor)
                existing = ambDonor
            }
        }

        if (existing != null) {
            // Update password or email if missing
            val updatedPassword = if (password.isNotBlank() && existing.password.isBlank()) password else existing.password
            val updatedEmail = if (email.isNotBlank() && existing.email.isBlank()) email else existing.email
            if (updatedPassword != existing.password || updatedEmail != existing.email) {
                val updatedDonor = existing.copy(password = updatedPassword, email = updatedEmail)
                _donors.value = _donors.value.map { if (it.id == existing.id) updatedDonor else it }
                saveDonorsLocal()
                pushDonorToFirebase(updatedDonor)
                setCurrentUser(updatedDonor)
            } else {
                setCurrentUser(existing)
            }
            return true
        } else {
            if (isGoogle) {
                // Log in as a newly simulated user for Google Sign-in if simulated
                val newSimulatedUser = BloodDonor(
                    id = "u_${System.currentTimeMillis()}",
                    name = "Alif Shen",
                    bloodGroup = "B+",
                    phone = if (username.isNotBlank()) username else "01781223344",
                    email = if (email.isNotBlank()) email else "help.alifshen.ltd@gmail.com",
                    district = "Dhaka",
                    upazila = "Dhanmondi",
                    lastDonationDate = "Available",
                    isAvailable = true,
                    isApproved = true,
                    donationCount = 1,
                    isGoogleUser = true,
                    password = password
                )
                setCurrentUser(newSimulatedUser)
                _donors.value = _donors.value + newSimulatedUser
                saveDonorsLocal()
                pushDonorToFirebase(newSimulatedUser)
                return true
            }
            return false
        }
    }

    fun syncAllWithFirebase() {
        initFirebaseDatabase()
        pushInitialDataToFirebaseIfEmpty()
        _donors.value.forEach { pushDonorToFirebase(it) }
        _requests.value.forEach { pushRequestToFirebase(it) }
        _ambulances.value.forEach { pushAmbulanceToFirebase(it) }
        _ambulanceBookings.value.forEach { pushAmbulanceBookingToFirebase(it) }
        _donorTeams.value.forEach { pushDonorTeamToFirebase(it) }
        _messages.value.forEach { pushChatMessageToFirebase(it) }
        pushAppConfigToFirebase()
    }

    fun testFirebaseConnection(onResult: (Boolean, String) -> Unit) {
        val db = getDb()
        if (db == null) {
            _isFirebaseConnected.value = false
            onResult(false, "ফায়ারবেস ডাটাবেস ইনিশিয়ালাইজ করা যায়নি। (Firebase Instance Null)")
            return
        }

        try {
            val pingRef = db.getReference("_connection_test").child("ping")
            pingRef.setValue(System.currentTimeMillis())
                .addOnSuccessListener {
                    _isFirebaseConnected.value = true
                    onResult(true, "✅ ফায়ারবেস ডাটাবেসের সাথে সফলভাবে কানেক্ট হয়েছে! (Data Write Successful)")
                }
                .addOnFailureListener { error ->
                    _isFirebaseConnected.value = false
                    val msg = error.message ?: "Unknown Error"
                    if (msg.contains("Permission denied", ignoreCase = true)) {
                        onResult(false, "❌ সিকিউরিটি রুলস ব্লক করা আছে! Firebase Console -> Realtime Database -> Rules এ গিয়ে 'Start in test mode' (.read: true, .write: true) সিলেক্ট করে Enable ক্লিক করুন।")
                    } else {
                        onResult(false, "❌ কানেকশন টেস্ট ব্যর্থ হয়েছে: $msg")
                    }
                }
        } catch (e: Exception) {
            _isFirebaseConnected.value = false
            onResult(false, "❌ ত্রুটি: ${e.message}")
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
        role: String = "Donor",
        password: String = "",
        village: String = ""
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
            village = village,
            lastDonationDate = if (lastDonationDate.isBlank()) "Available" else lastDonationDate,
            isAvailable = true,
            isApproved = true, // Auto-approved for standard users, admin screen can still moderate!
            donationCount = 0,
            country = country,
            userId = randId,
            role = role,
            password = password
        )
        setCurrentUser(newUser)
        _donors.value = _donors.value + newUser
        saveDonorsLocal()

        // Push donor to Firebase Realtime Database
        pushDonorToFirebase(newUser)

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

    fun addWalletBalance(amount: Double) {
        val current = _currentUser.value ?: return
        val updated = current.copy(walletBalance = current.walletBalance + amount)
        setCurrentUser(updated)
        _donors.value = _donors.value.map { if (it.id == current.id) updated else it }
        saveDonorsLocal()
    }

    fun deductWalletBalance(amount: Double): Boolean {
        val current = _currentUser.value ?: return false
        if (current.walletBalance < amount) return false
        val updated = current.copy(walletBalance = current.walletBalance - amount)
        setCurrentUser(updated)
        _donors.value = _donors.value.map { if (it.id == current.id) updated else it }
        saveDonorsLocal()
        return true
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
        role: String? = null,
        village: String = ""
    ) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            name = name,
            phone = phone,
            email = email,
            bloodGroup = bloodGroup,
            district = district,
            upazila = upazila,
            village = village,
            lastDonationDate = lastDonation,
            isAvailable = available,
            country = country,
            role = role ?: current.role
        )
        setCurrentUser(updated)
        _donors.value = _donors.value.map { if (it.id == current.id) updated else it }
        saveDonorsLocal()

        // Push profile update to Firebase Realtime Database
        pushDonorToFirebase(updated)

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

    fun submitDonationClaim(
        requestId: String,
        donorPhone: String,
        donorName: String,
        contactNumber: String,
        donationDate: String = "",
        donationTime: String = "",
        donorBloodGroup: String = "",
        lastDonationDate: String = "",
        healthStatus: String = "",
        additionalRemarks: String = ""
    ) {
        val newClaim = DonationClaim(
            id = "claim_${System.currentTimeMillis()}",
            requestId = requestId,
            donorPhone = donorPhone,
            donorName = donorName,
            contactNumber = contactNumber,
            status = "Pending",
            donationDate = donationDate,
            donationTime = donationTime,
            donorBloodGroup = donorBloodGroup,
            lastDonationDate = lastDonationDate,
            healthStatus = healthStatus,
            additionalRemarks = additionalRemarks
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
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
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
            dateRequested = todayStr,
            status = "Active",
            country = country,
            patientGender = patientGender,
            medicalCondition = medicalCondition
        )
        _requests.value = listOf(newReq) + _requests.value
        seenRequestIds.add(newReq.id)
        cleanExpiredRequests()
        saveRequestsLocal()

        // Push blood request to Firebase Realtime Database
        pushRequestToFirebase(newReq)

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
        
        appScope.launch {
            try {
                val db = getDb() ?: return@launch
                val key = cleanKey(newNotification.id)
                db.getReference("broadcast_notifications").child(key).setValue(newNotification)
            } catch (e: Exception) {
                Log.e("BloodConnectRepo", "Error pushing notification: ${e.message}")
            }
        }
        
        // Trigger OS Status Bar Notification
        val currentLang = language.value
        val displayTitle = if (currentLang == AppLanguage.BAN) titleBn else titleEn
        val displayMessage = if (currentLang == AppLanguage.BAN) messageBn else messageEn
        showSystemStatusBarNotification(displayTitle, displayMessage)
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
            var updatedDonor: BloodDonor? = null
            _donors.value = _donors.value.map {
                val cleanDonorPhone = it.phone.trim().replace("+88", "").replace(" ", "")
                if (it.id == report.scammerDonorId || cleanDonorPhone == cleanReportPhone || cleanDonorPhone.endsWith(cleanReportPhone) || cleanReportPhone.endsWith(cleanDonorPhone)) {
                    val upd = it.copy(isApproved = false, isAvailable = false)
                    updatedDonor = upd
                    upd
                } else it
            }

            // Post update to remote API
            updatedDonor?.let { donor ->
                pushDonorToFirebase(donor)
                appScope.launch {
                    if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                        BloodConnectApiClient.updateDonor(donor.id, donor)
                    }
                }
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
            var updatedDonor: BloodDonor? = null
            _donors.value = _donors.value.map {
                val cleanDonorPhone = it.phone.trim().replace("+88", "").replace(" ", "")
                val scammerDonorId = _scamReports.value.find { rep -> rep.id == id }?.scammerDonorId ?: ""
                if (it.id == scammerDonorId || cleanDonorPhone == cleanReportPhone || cleanDonorPhone.endsWith(cleanReportPhone) || cleanReportPhone.endsWith(cleanDonorPhone)) {
                    val upd = it.copy(isApproved = false, isAvailable = false)
                    updatedDonor = upd
                    upd
                } else it
            }

            // Post update to remote API
            updatedDonor?.let { donor ->
                pushDonorToFirebase(donor)
                appScope.launch {
                    if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                        BloodConnectApiClient.updateDonor(donor.id, donor)
                    }
                }
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
        saveMessagesLocal()

        // Push chat message to Firebase Realtime Database
        pushChatMessageToFirebase(newMsg)

        // Push to remote API if configured
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                BloodConnectApiClient.sendChatMessage(newMsg)
            }
        }

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
        fun cleanPhone(p: String): String = p.replace(Regex("[^0-9]"), "").takeLast(10)
        val cleanUser = cleanPhone(userPhone)
        val cleanPeer = cleanPhone(peerPhone)

        val updatedList = _messages.value.map {
            val msgSenderClean = cleanPhone(it.senderPhone)
            val msgReceiverClean = cleanPhone(it.receiverPhone)

            val matchesPeerSender = (cleanPeer.isNotEmpty() && msgSenderClean == cleanPeer) ||
                                    it.senderPhone.equals(peerPhone, ignoreCase = true)
            val matchesUserReceiver = (cleanUser.isNotEmpty() && msgReceiverClean == cleanUser) ||
                                      it.receiverPhone.equals(userPhone, ignoreCase = true) ||
                                      it.receiverPhone.equals("LIVE_SUPPORT", ignoreCase = true)

            if (matchesPeerSender && matchesUserReceiver && !it.isRead) {
                val updated = it.copy(isRead = true)
                pushChatMessageToFirebase(updated)
                updated
            } else {
                it
            }
        }
        _messages.value = updatedList
        saveMessagesLocal()
    }

    // ADMIN ACTIONS
    fun warnDonor(id: String, isWarning: Boolean, reason: String) {
        var updatedDonor: BloodDonor? = null
        _donors.value = _donors.value.map {
            if (it.id == id) {
                val upd = it.copy(isWarning = isWarning, warningReason = reason)
                updatedDonor = upd
                upd
            } else it
        }
        saveDonorsLocal()
        val current = _currentUser.value
        if (current != null && current.id == id) {
            setCurrentUser(current.copy(isWarning = isWarning, warningReason = reason))
        }

        // Post to Firebase & remote API
        updatedDonor?.let { donor ->
            pushDonorToFirebase(donor)
            appScope.launch {
                if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                    BloodConnectApiClient.updateDonor(id, donor)
                }
            }
        }
    }

    fun approveDonor(id: String) {
        var updatedDonor: BloodDonor? = null
        _donors.value = _donors.value.map {
            if (it.id == id) {
                val upd = it.copy(isApproved = true)
                updatedDonor = upd
                upd
            } else it
        }
        saveDonorsLocal()

        // Post to Firebase & remote API
        updatedDonor?.let { donor ->
            pushDonorToFirebase(donor)
            appScope.launch {
                if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                    BloodConnectApiClient.updateDonor(id, donor)
                }
            }
        }
    }

    fun deleteDonor(id: String) {
        _donors.value = _donors.value.filterNot { it.id == id }
        saveDonorsLocal()
        deleteDonorFromFirebase(id)

        // Delete from remote API
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                BloodConnectApiClient.deleteDonor(id)
            }
        }
    }

    fun deleteRequest(id: String) {
        _requests.value = _requests.value.filterNot { it.id == id }
        saveRequestsLocal()
        deleteRequestFromFirebase(id)

        // Delete from remote API
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                BloodConnectApiClient.deleteRequest(id)
            }
        }
    }

    fun toggleRequestStatus(id: String) {
        var updatedReq: BloodRequest? = null
        _requests.value = _requests.value.map {
            if (it.id == id) {
                val nextStatus = if (it.status == "Active") "Completed" else "Active"
                val upd = it.copy(status = nextStatus)
                updatedReq = upd
                upd
            } else it
        }
        saveRequestsLocal()

        // Post to Firebase & remote API
        updatedReq?.let { req ->
            pushRequestToFirebase(req)
            appScope.launch {
                if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                    BloodConnectApiClient.updateRequest(id, req)
                }
            }
        }
    }

    // POLICY STATE MANAGEMENT
    private val _privacyPolicyEn = MutableStateFlow("We value your privacy. Your contact details are only shared securely with registered members when requesting or donating blood. We do not sell or lease your personal information to any third party.")
    val privacyPolicyEn: StateFlow<String> = _privacyPolicyEn.asStateFlow()

    private val _privacyPolicyBn = MutableStateFlow("আমরা আপনার গোপনীয়তাকে শতভাগ মূল্যায়ন করি। আপনার যোগাযোগের তথ্য কেবল রক্তদান বা রক্তদাতার সন্ধানের জন্য নিবন্ধিত সদস্যদের সঙ্গে শেয়ার করা হয়। আমরা আপনার কোনো ব্যক্তিগত তথ্য কোনো তৃতীয় পক্ষকে প্রদান করি না।")
    val privacyPolicyBn: StateFlow<String> = _privacyPolicyBn.asStateFlow()

    private val _privacyPolicyUrl = MutableStateFlow("https://alifshengroup.com/privacy-policy")
    val privacyPolicyUrl: StateFlow<String> = _privacyPolicyUrl.asStateFlow()

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
        refundEn: String, refundBn: String,
        privacyUrl: String = _privacyPolicyUrl.value
    ) {
        _privacyPolicyEn.value = privacyEn
        _privacyPolicyBn.value = privacyBn
        _privacyPolicyUrl.value = privacyUrl
        _termsConditionsEn.value = termsEn
        _termsConditionsBn.value = termsBn
        _refundPolicyEn.value = refundEn
        _refundPolicyBn.value = refundBn
        saveAppConfigLocal()
        pushAppConfigToRemote()
    }

    fun pushAppConfigToRemote() {
        pushAppConfigToFirebase()
        appScope.launch {
            if (BloodConnectApiClient.apiUrl.value.isNotBlank()) {
                val config = AppConfig(
                    id = "default",
                    app_name = _appName.value,
                    home_notice = _homeNotice.value,
                    popup_notice = _popupNotice.value,
                    privacy_policy_en = _privacyPolicyEn.value,
                    privacy_policy_bn = _privacyPolicyBn.value,
                    privacy_policy_url = _privacyPolicyUrl.value,
                    terms_conditions_en = _termsConditionsEn.value,
                    terms_conditions_bn = _termsConditionsBn.value,
                    refund_policy_en = _refundPolicyEn.value,
                    refund_policy_bn = _refundPolicyBn.value
                )
                val result = BloodConnectApiClient.updateAppConfig(config)
                if (result.isSuccess) {
                    Log.d("BloodConnectRepo", "Successfully pushed app config to cloud!")
                } else {
                    Log.e("BloodConnectRepo", "Failed to push app config to cloud: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    // --- V9 SUBSCRIPTION HELPERS ---
    fun serializeSubscriptionPlans(plans: List<V9SubscriptionPlan>): String {
        return plans.joinToString("||PLAN_SEP||") { plan ->
            listOf(
                plan.id,
                plan.nameEn,
                plan.nameBn,
                plan.price.toString(),
                plan.durationDays.toString(),
                plan.descriptionEn,
                plan.descriptionBn
            ).joinToString("||FIELD_SEP||")
        }
    }

    fun deserializeSubscriptionPlans(serialized: String): List<V9SubscriptionPlan> {
        if (serialized.isEmpty()) return emptyList()
        val list = mutableListOf<V9SubscriptionPlan>()
        val items = serialized.split("||PLAN_SEP||")
        for (item in items) {
            val parts = item.split("||FIELD_SEP||")
            if (parts.size >= 7) {
                list.add(
                    V9SubscriptionPlan(
                        id = parts[0],
                        nameEn = parts[1],
                        nameBn = parts[2],
                        price = parts[3].toDoubleOrNull() ?: 0.0,
                        durationDays = parts[4].toIntOrNull() ?: 30,
                        descriptionEn = parts[5],
                        descriptionBn = parts[6]
                    )
                )
            }
        }
        return list
    }

    fun serializeUserSubscriptions(subs: List<UserSubscription>): String {
        return subs.joinToString("||SUB_SEP||") { sub ->
            listOf(
                sub.userPhone,
                sub.planId,
                sub.planNameEn,
                sub.planNameBn,
                sub.pricePaid.toString(),
                sub.startDate,
                sub.endDate,
                sub.isExpired.toString(),
                sub.transactionId,
                sub.paymentMethod
            ).joinToString("||FIELD_SEP||")
        }
    }

    fun deserializeUserSubscriptions(serialized: String): List<UserSubscription> {
        if (serialized.isEmpty()) return emptyList()
        val list = mutableListOf<UserSubscription>()
        val items = serialized.split("||SUB_SEP||")
        for (item in items) {
            val parts = item.split("||FIELD_SEP||")
            if (parts.size >= 10) {
                list.add(
                    UserSubscription(
                        userPhone = parts[0],
                        planId = parts[1],
                        planNameEn = parts[2],
                        planNameBn = parts[3],
                        pricePaid = parts[4].toDoubleOrNull() ?: 0.0,
                        startDate = parts[5],
                        endDate = parts[6],
                        isExpired = parts[7].toBoolean(),
                        transactionId = parts[8],
                        paymentMethod = parts[9]
                    )
                )
            }
        }
        return list
    }

    fun addOrUpdateSubscriptionPlan(plan: V9SubscriptionPlan) {
        val current = _subscriptionPlans.value.toMutableList()
        val index = current.indexOfFirst { it.id == plan.id }
        if (index >= 0) {
            current[index] = plan
        } else {
            current.add(plan)
        }
        _subscriptionPlans.value = current
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("v9_subscription_plans_list", serializeSubscriptionPlans(current)).apply()
        }
    }

    fun deleteSubscriptionPlan(planId: String) {
        val current = _subscriptionPlans.value.filter { it.id != planId }
        _subscriptionPlans.value = current
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("v9_subscription_plans_list", serializeSubscriptionPlans(current)).apply()
        }
    }

    fun addUserSubscription(sub: UserSubscription) {
        val current = _userSubscriptions.value.toMutableList()
        current.add(sub)
        _userSubscriptions.value = current
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("v9_user_subscriptions_list", serializeUserSubscriptions(current)).apply()
        }
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
