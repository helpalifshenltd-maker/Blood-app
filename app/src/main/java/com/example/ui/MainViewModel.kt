package com.example.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: BloodConnectRepository = BloodConnectRepository.getInstance()
) : ViewModel() {

    init {
        // Periodically sync with remote Supabase database every 15 seconds to fetch latest updates, requests, and donor profiles
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            while (true) {
                try {
                    repository.triggerRemoteSync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(15000) // 15 seconds
            }
        }

        // Periodically check for expired pending ambulance bookings (older than 20 minutes)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            while (true) {
                try {
                    val now = System.currentTimeMillis()
                    val bookingsList = repository.ambulanceBookings.value
                    bookingsList.forEach { booking ->
                        if (booking.status == "Pending" && booking.id.startsWith("book_")) {
                            val creationTime = booking.id.substringAfter("book_").toLongOrNull() ?: 0L
                            if (creationTime > 0L && (now - creationTime) > 20 * 60 * 1000) {
                                // Cancel the booking automatically
                                repository.updateBookingStatus(
                                    bookingId = booking.id,
                                    newStatus = "Cancelled",
                                    adminNotes = "Auto-cancelled: No reply within 20 minutes."
                                )
                                // Add a system notification about this cancellation
                                repository.addNotification(
                                    titleEn = "Booking Auto-Cancelled",
                                    titleBn = "বুকিং স্বয়ংক্রিয়ভাবে বাতিল",
                                    messageEn = "Ambulance booking for ${booking.patientName} was automatically cancelled due to no response in 20 minutes.",
                                    messageBn = "২০ মিনিটের মধ্যে কোনো সাড়া না দেওয়ায় ${booking.patientName} এর অ্যাম্বুলেন্স বুকিংটি স্বয়ংক্রিয়ভাবে বাতিল করা হয়েছে।",
                                    type = "GENERAL_INFO",
                                    country = "Bangladesh"
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(10000) // Check every 10 seconds
            }
        }
    }

    // --- NAVIGATION BACKSTACK ---
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val backStack = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            backStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (backStack.isNotEmpty()) {
            _currentScreen.value = backStack.removeAt(backStack.size - 1)
        } else {
            // If empty, return to Home or Splash
            if (_currentScreen.value != AppScreen.HOME) {
                _currentScreen.value = AppScreen.HOME
            }
        }
    }

    fun clearBackStackAndNavigateTo(screen: AppScreen) {
        backStack.clear()
        _currentScreen.value = screen
    }

    // --- LOCATION/COUNTRY DETECTOR AND STATES ---
    private val _detectedCountry = MutableStateFlow("Bangladesh")
    val detectedCountry: StateFlow<String> = _detectedCountry.asStateFlow()

    private val _detectedCountryCode = MutableStateFlow("BD")
    val detectedCountryCode: StateFlow<String> = _detectedCountryCode.asStateFlow()

    // --- MOCK STATISTICS FOR LIVE DISPLAY ---
    private val _useMockStats = MutableStateFlow(true)
    val useMockStats: StateFlow<Boolean> = _useMockStats.asStateFlow()

    private val _mockTotalUsers = MutableStateFlow(80424)
    val mockTotalUsers: StateFlow<Int> = _mockTotalUsers.asStateFlow()

    private val _mockTotalDonors = MutableStateFlow(12300)
    val mockTotalDonors: StateFlow<Int> = _mockTotalDonors.asStateFlow()

    fun setUseMockStats(use: Boolean) {
        _useMockStats.value = use
    }

    fun updateMockStats(users: Int, donors: Int) {
        _mockTotalUsers.value = users
        _mockTotalDonors.value = donors
    }

    private val _isUserInBangladesh = MutableStateFlow(true)
    val isUserInBangladesh: StateFlow<Boolean> = _isUserInBangladesh.asStateFlow()

    private val _isDeviceInBangladesh = MutableStateFlow(true)
    val isDeviceInBangladesh: StateFlow<Boolean> = _isDeviceInBangladesh.asStateFlow()

    // --- LOGIN/REGISTER STATE ---
    private val _showRegistrationTab = MutableStateFlow(false)
    val showRegistrationTab: StateFlow<Boolean> = _showRegistrationTab.asStateFlow()

    fun setShowRegistrationTab(show: Boolean) {
        _showRegistrationTab.value = show
    }

    // --- REPOSITORY BINDINGS (FILTERED SPECIFICALLY BY ACTIVE COUNTRY SERVER/SANDBOX) ---
    val currentUser: StateFlow<BloodDonor?> = repository.currentUser
    val donors: StateFlow<List<BloodDonor>> = combine(repository.donors, detectedCountry) { list, countryName ->
        list.filter { it.country.equals(countryName, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val requests: StateFlow<List<BloodRequest>> = combine(repository.requests, detectedCountry) { list, countryName ->
        list.filter { it.country.equals(countryName, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<DonationNotification>> = combine(repository.notifications, detectedCountry) { list, countryName ->
        list.filter { it.country.equals(countryName, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val language: StateFlow<AppLanguage> = repository.language
    val appName: StateFlow<String> = repository.appName

    val bkashNumber: StateFlow<String> = repository.bkashNumber
    val nagadNumber: StateFlow<String> = repository.nagadNumber
    val rocketNumber: StateFlow<String> = repository.rocketNumber
    val googlePlayMerchant: StateFlow<String> = repository.googlePlayMerchant

    val donationClaims: StateFlow<List<com.example.data.DonationClaim>> = repository.donationClaims
    val homeNotice: StateFlow<String> = repository.homeNotice
    val popupNotice: StateFlow<String> = repository.popupNotice

    val emailNotifyEnabled: StateFlow<Boolean> = repository.emailNotifyEnabled
    val smtpHost: StateFlow<String> = repository.smtpHost
    val smtpPort: StateFlow<String> = repository.smtpPort
    val smtpUsername: StateFlow<String> = repository.smtpUsername
    val smtpPassword: StateFlow<String> = repository.smtpPassword
    val emailSubjectTemplate: StateFlow<String> = repository.emailSubjectTemplate
    val emailBodyTemplate: StateFlow<String> = repository.emailBodyTemplate

    // --- GOOGLE ADMOB CONFIG ---
    val adMobEnabled: StateFlow<Boolean> = repository.adMobEnabled
    val adMobAppId: StateFlow<String> = repository.adMobAppId
    val adMobBannerId: StateFlow<String> = repository.adMobBannerId
    val adMobInterstitialId: StateFlow<String> = repository.adMobInterstitialId
    val adMobNativeId: StateFlow<String> = repository.adMobNativeId

    // --- CUSTOM CPA/AFFILIATE AD NETWORK CONFIG ---
    val customAdsEnabled: StateFlow<Boolean> = repository.customAdsEnabled
    val customAdNetworkName: StateFlow<String> = repository.customAdNetworkName
    val customAdTitle: StateFlow<String> = repository.customAdTitle
    val customAdBannerUrl: StateFlow<String> = repository.customAdBannerUrl
    val customAdTargetUrl: StateFlow<String> = repository.customAdTargetUrl
    val customAdTargetCountries: StateFlow<String> = repository.customAdTargetCountries
    val customAdConfigs: StateFlow<List<CustomAdConfig>> = repository.customAdConfigs

    fun updateCustomAdConfigsList(context: android.content.Context, list: List<CustomAdConfig>) {
        repository.updateCustomAdConfigsList(context, list)
    }

    fun updateCustomAdsConfig(
        context: android.content.Context,
        enabled: Boolean,
        networkName: String,
        adTitle: String,
        bannerUrl: String,
        targetUrl: String,
        targetCountries: String
    ) {
        repository.updateCustomAdsConfig(context, enabled, networkName, adTitle, bannerUrl, targetUrl, targetCountries)
    }

    fun updateAdMobConfig(
        context: android.content.Context,
        enabled: Boolean,
        appId: String,
        bannerId: String,
        interstitialId: String,
        nativeId: String
    ) {
        repository.updateAdMobConfig(context, enabled, appId, bannerId, interstitialId, nativeId)
    }

    fun updateAppName(newName: String) {
        repository.updateAppName(newName)
    }

    fun updateHomeNotice(newNotice: String) {
        repository.updateHomeNotice(newNotice)
    }

    fun updatePopupNotice(newNotice: String) {
        repository.updatePopupNotice(newNotice)
    }

    fun updatePaymentConfig(bkash: String, nagad: String, rocket: String, googlePlay: String) {
        repository.updatePaymentConfig(bkash, nagad, rocket, googlePlay)
    }

    val customCountries: StateFlow<List<Pair<String, String>>> = repository.customCountries

    fun addCountry(context: android.content.Context, name: String, code: String) {
        repository.addCountry(context, name, code)
    }

    fun deleteCountry(context: android.content.Context, name: String) {
        repository.deleteCountry(context, name)
    }

    fun updateEmailConfig(
        context: android.content.Context,
        enabled: Boolean,
        host: String,
        port: String,
        user: String,
        pass: String,
        subject: String,
        body: String
    ) {
        repository.updateEmailConfig(context, enabled, host, port, user, pass, subject, body)
    }

    // ADMIN MODE STATE
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    fun setAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
    }
    val scamReports: StateFlow<List<ScamReport>> = combine(repository.scamReports, detectedCountry) { list, countryName ->
        list.filter { it.country.equals(countryName, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ambulances: StateFlow<List<Ambulance>> = repository.ambulances

    val ambulanceBookings: StateFlow<List<AmbulanceBooking>> = repository.ambulanceBookings

    val messages: StateFlow<List<ChatMessage>> = repository.messages

    val subscriptionPlans: StateFlow<List<V9SubscriptionPlan>> = repository.subscriptionPlans
    val userSubscriptions: StateFlow<List<UserSubscription>> = repository.userSubscriptions

    // --- REMOTE WEB API PROPERTIES ---
    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val syncError: StateFlow<String?> = repository.syncError
    val apiUrl: StateFlow<String> = BloodConnectApiClient.apiUrl
    val isRemoteConnected: StateFlow<Boolean> = BloodConnectApiClient.isRemoteConnected
    val remoteApiKey: StateFlow<String> = repository.remoteApiKey

    fun updateRemoteApiUrl(context: android.content.Context, url: String, apiKey: String = ""): Boolean {
        return repository.updateRemoteApiUrl(context, url, apiKey)
    }

    fun triggerRemoteSync() {
        repository.triggerRemoteSync()
    }

    // --- SCREEN TRANSLATION ACCESSOR ---
    val strings: StateFlow<Map<String, String>> = repository.language.map { lang ->
        Loc.strings(lang)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loc.strings(AppLanguage.ENG))

    fun toggleLanguage() {
        repository.toggleLanguage()
    }

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun checkNetworkStatus(context: android.content.Context) {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (connectivityManager == null) {
            _isOnline.value = true
            return
        }
        
        _isOnline.value = isInternetCurrentlyAvailable(connectivityManager)

        try {
            val request = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(request, object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    _isOnline.value = true
                }
                
                override fun onLost(network: android.net.Network) {
                    _isOnline.value = isInternetCurrentlyAvailable(connectivityManager)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            _isOnline.value = true
        }
    }

    private fun isInternetCurrentlyAvailable(connectivityManager: android.net.ConnectivityManager): Boolean {
        try {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                     activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                     activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET))
        } catch (e: Exception) {
            return true
        }
    }

    fun detectUserLocation(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.initRemoteConfig(context)
            } catch (e: Exception) {}

            var detectedCountryFromIp: String? = null
            var detectedCodeFromIp: String? = null

            // Try to detect by IP first (to respect VPN)
            try {
                val url = java.net.URL("https://ipapi.co/json/")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(text)
                    if (json.has("country_name") && json.has("country_code")) {
                        detectedCountryFromIp = json.getString("country_name")
                        detectedCodeFromIp = json.getString("country_code")
                    }
                }
            } catch (e: Exception) {
                // Ignore and fall back to locale/system info
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                try {
                    val locale = java.util.Locale.getDefault()
                    val systemCountryCode = locale.country ?: ""
                    val systemLanguage = locale.language ?: ""

                    val tm = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
                    val simCountry = tm?.simCountryIso ?: ""
                    val networkCountry = tm?.networkCountryIso ?: ""

                    val isBDByLocale = systemCountryCode.equals("BD", ignoreCase = true) || systemLanguage.equals("bn", ignoreCase = true)
                    val isBDBySim = simCountry.equals("bd", ignoreCase = true) || networkCountry.equals("bd", ignoreCase = true)
                    val isBDByTimeZone = java.util.TimeZone.getDefault().id.contains("Dhaka", ignoreCase = true) || java.util.TimeZone.getDefault().rawOffset == 6 * 3600000

                    val isFromBangladesh = if (detectedCountryFromIp != null) {
                        detectedCountryFromIp.equals("Bangladesh", ignoreCase = true) || detectedCodeFromIp.equals("BD", ignoreCase = true)
                    } else {
                        isBDByLocale || isBDBySim || isBDByTimeZone
                    }

                    _isUserInBangladesh.value = isFromBangladesh
                    _isDeviceInBangladesh.value = isFromBangladesh

                    if (isFromBangladesh) {
                        _detectedCountry.value = "Bangladesh"
                        _detectedCountryCode.value = "BD"
                        regCountry = "Bangladesh"
                        reqCountry = "Bangladesh"
                        profileEditCountry = "Bangladesh"
                        regDistrict = "Dhaka"
                        reqDistrict = "Dhaka"
                        _searchDistrict.value = "Dhaka"
                        _searchUpazila.value = "All"
                        _searchHospital.value = "All"
                        _searchAmbulanceType.value = "All"
                        repository.setLanguage(AppLanguage.BAN)
                    } else {
                        val countryName = detectedCountryFromIp ?: if (systemCountryCode.isNotBlank()) locale.displayCountry else "International"
                        val countryCode = detectedCodeFromIp ?: if (systemCountryCode.isNotBlank()) systemCountryCode.uppercase() else "GL"
                        _detectedCountry.value = countryName
                        _detectedCountryCode.value = countryCode
                        regCountry = countryName
                        reqCountry = countryName
                        profileEditCountry = countryName
                        _searchDistrict.value = "All"
                        _searchUpazila.value = "All"
                        _searchHospital.value = "All"
                        _searchAmbulanceType.value = "All"
                        repository.setLanguage(AppLanguage.ENG)
                    }
                } catch (e: Exception) {
                    _isUserInBangladesh.value = true
                    _isDeviceInBangladesh.value = true
                    _detectedCountry.value = "Bangladesh"
                    _detectedCountryCode.value = "BD"
                    regCountry = "Bangladesh"
                    reqCountry = "Bangladesh"
                    profileEditCountry = "Bangladesh"
                    regDistrict = "Dhaka"
                    reqDistrict = "Dhaka"
                    _searchDistrict.value = "Dhaka"
                    _searchUpazila.value = "All"
                    _searchHospital.value = "All"
                    _searchAmbulanceType.value = "All"
                    repository.setLanguage(AppLanguage.BAN)
                }
            }
        }
    }

    fun setDetectedCountry(countryName: String, countryCode: String) {
        _detectedCountry.value = countryName
        _detectedCountryCode.value = countryCode
        val isBD = countryName.equals("Bangladesh", ignoreCase = true)
        _isUserInBangladesh.value = isBD
        
        regCountry = countryName
        reqCountry = countryName
        profileEditCountry = countryName
        
        if (isBD) {
            _searchDistrict.value = "Dhaka"
            _searchUpazila.value = "All"
            _searchHospital.value = "All"
            _searchAmbulanceType.value = "All"
            repository.setLanguage(AppLanguage.BAN)
        } else {
            _searchDistrict.value = "All"
            _searchUpazila.value = "All"
            _searchHospital.value = "All"
            _searchAmbulanceType.value = "All"
            repository.setLanguage(AppLanguage.ENG)
        }
    }

    // --- SELECTED DONOR FOR DETAILED PROFILE ---
    private val _selectedDonorId = MutableStateFlow<String?>(null)
    val selectedDonor: StateFlow<BloodDonor?> = combine(_selectedDonorId, donors) { id, list ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectDonorAndNavigate(donorId: String) {
        _selectedDonorId.value = donorId
        navigateTo(AppScreen.DONOR_PROFILE)
    }

    // --- SELECTED REQUEST FOR DETAILED VIEW ---
    private val _selectedRequestId = MutableStateFlow<String?>(null)
    val selectedRequest: StateFlow<BloodRequest?> = combine(_selectedRequestId, requests) { id, list ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectRequestAndNavigate(requestId: String) {
        _selectedRequestId.value = requestId
        navigateTo(AppScreen.REQUEST_DETAIL)
    }

    private val _activeChatPeerPhone = MutableStateFlow<String?>(null)
    val activeChatPeerPhone: StateFlow<String?> = _activeChatPeerPhone.asStateFlow()

    private val _activeChatPeerName = MutableStateFlow<String?>(null)
    val activeChatPeerName: StateFlow<String?> = _activeChatPeerName.asStateFlow()

    var isSupportChatMode by mutableStateOf(false)

    fun openChatRoom(peerPhone: String, peerName: String, isSupport: Boolean = false) {
        _activeChatPeerPhone.value = peerPhone
        _activeChatPeerName.value = peerName
        isSupportChatMode = isSupport
        val user = currentUser.value
        if (user != null) {
            markInAppChatRead(user.phone, peerPhone)
        }
        navigateTo(AppScreen.CHAT_ROOM)
    }

    fun startSupportChat() {
        openChatRoom("LIVE_SUPPORT", "Live Support Admin", isSupport = false)
    }

    // --- SEARCH / FILTERING STATES ---
    private val _searchBloodGroup = MutableStateFlow("O+")
    val searchBloodGroup: StateFlow<String> = _searchBloodGroup.asStateFlow()

    private val _searchDistrict = MutableStateFlow("Dhaka")
    val searchDistrict: StateFlow<String> = _searchDistrict.asStateFlow()

    private val _searchUpazila = MutableStateFlow("All")
    val searchUpazila: StateFlow<String> = _searchUpazila.asStateFlow()

    private val _searchHospital = MutableStateFlow("All")
    val searchHospital: StateFlow<String> = _searchHospital.asStateFlow()

    private val _searchAmbulanceType = MutableStateFlow("All")
    val searchAmbulanceType: StateFlow<String> = _searchAmbulanceType.asStateFlow()

    fun updateFilters(bloodGroup: String, district: String, upazila: String, hospital: String = "All") {
        _searchBloodGroup.value = bloodGroup
        _searchDistrict.value = district
        _searchUpazila.value = upazila
        _searchHospital.value = hospital
    }

    fun updateAmbulanceFilters(district: String, upazila: String, type: String) {
        _searchDistrict.value = district
        _searchUpazila.value = upazila
        _searchAmbulanceType.value = type
    }

    private val hospitalLocations = mapOf(
        "Dhaka Medical College Hospital (DMCH)" to Pair("Dhaka", "Tejgaon"),
        "Sir Salimullah Medical College Hospital" to Pair("Dhaka", "Dhanmondi"),
        "Chattogram General Hospital (CGH)" to Pair("Chattogram", "Double Mooring"),
        "Sylhet MAG Osmani Medical College" to Pair("Sylhet", "Sylhet Sadar"),
        "Rajshahi Medical College Hospital" to Pair("Rajshahi", "Rajpara"),
        "Mymensingh Medical College Hospital" to Pair("Mymensingh", "Sadar"),
        "Khulna Medical College Hospital" to Pair("Khulna", "Sadar"),
        "Sher-e-Bangla Medical College Hospital" to Pair("Barishal", "Sadar"),
        "Mount Sinai Hospital" to Pair("New York", "Manhattan"),
        "Stanford Health Care" to Pair("California", "San Francisco"),
        "Houston Methodist Hospital" to Pair("Texas", "Houston"),
        "AIIMS New Delhi" to Pair("Delhi", "Connaught Place"),
        "Fortis Hospital Mumbai" to Pair("Maharashtra", "Mumbai Worli"),
        "King Faisal Specialist Hospital" to Pair("Riyadh", "Al-Olaya"),
        "Cleveland Clinic Abu Dhabi" to Pair("Abu Dhabi", "Al-Reem Island"),
        "St Thomas' Hospital London" to Pair("London", "Westminster")
    )

    val filteredDonors: StateFlow<List<BloodDonor>> = combine(
        donors,
        _searchBloodGroup,
        _searchDistrict,
        _searchUpazila,
        _searchHospital
    ) { list, group, dist, upz, hospital ->
        val hospitalLocation = if (hospital != "All") hospitalLocations[hospital] else null
        list.filter { donor ->
            val matchGroup = (group == "All" || donor.bloodGroup == group)
            val matchDist = if (hospitalLocation != null) {
                donor.district.equals(hospitalLocation.first, ignoreCase = true)
            } else {
                (dist == "All" || donor.district.equals(dist, ignoreCase = true))
            }
            val matchUpz = if (hospitalLocation != null) {
                donor.upazila.equals(hospitalLocation.second, ignoreCase = true)
            } else {
                (upz == "All" || donor.upazila.equals(upz, ignoreCase = true))
            }
            matchGroup && matchDist && matchUpz && donor.isApproved && donor.isAvailable && donor.role == "Donor"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAmbulances: StateFlow<List<Ambulance>> = combine(
        ambulances,
        _searchDistrict,
        _searchUpazila,
        _searchAmbulanceType,
        detectedCountry
    ) { list, dist, upz, type, countryName ->
        list.filter { amb ->
            val matchCountry = amb.country.equals(countryName, ignoreCase = true)
            val matchDist = (dist == "All" || amb.district.equals(dist, ignoreCase = true))
            val matchUpz = (upz == "All" || amb.upazila.equals(upz, ignoreCase = true))
            val matchType = (type == "All" || amb.ambulanceType.equals(type, ignoreCase = true))
            matchCountry && matchDist && matchUpz && matchType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- STATISTICS CARD VALUES ---
    val statistics = combine(donors, requests, useMockStats, mockTotalUsers, mockTotalDonors) { donorList, requestList, useMock, mockUsers, mockDonors ->
        if (useMock) {
            mapOf(
                "total_donors" to mockDonors,
                "total_users" to mockUsers,
                "active_requests" to 42, // Also some mock values for others if needed
                "lives_saved" to 2340,
                "hospitals" to 14
            )
        } else {
            val totalDonors = donorList.filter { it.isApproved }.size
            val activeRequests = requestList.filter { it.status == "Active" }.size
            val livesSaved = donorList.sumOf { it.donationCount }
            val hospitalCount = if (detectedCountry.value.equals("Bangladesh", ignoreCase = true)) 14 else 5
            mapOf(
                "total_donors" to totalDonors,
                "total_users" to totalDonors + 120, // Real users = donors + some guests
                "active_requests" to activeRequests,
                "lives_saved" to livesSaved,
                "hospitals" to hospitalCount
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mapOf(
        "total_donors" to 12300,
        "total_users" to 80424,
        "active_requests" to 4,
        "lives_saved" to 23,
        "hospitals" to 14
    ))

    // --- USER SUBMIT FORM STATES ---
    // Log in forms
    var loginPhone = ""
    var loginEmail = ""
    var loginPassword = ""

    // User passwords map (simulated secure database mapping)
    private val _userPasswords = MutableStateFlow<Map<String, String>>(
        mapOf(
            "anis@gmail.com" to "anis123",
            "tasnim@gmail.com" to "tasnim456",
            "rakib@gmail.com" to "rakib789",
            "nusrat@gmail.com" to "nusrat112",
            "mehedi@gmail.com" to "mehedi334",
            "israt@gmail.com" to "israt556",
            "fahim@gmail.com" to "fahim778",
            "russell@gmail.com" to "russell990",
            "sania@gmail.com" to "sania223",
            "john@gmail.com" to "john556",
            "alifsheenshopping@gmail.com" to "019Alif11#",
            "help.alifshen.ltd@gmail.com" to "019Alif11#"
        )
    )
    val userPasswords: StateFlow<Map<String, String>> = _userPasswords.asStateFlow()

    // Password Recovery form states
    var recoveryName by mutableStateOf("")
    var recoveryEmail by mutableStateOf("")
    var recoveryPhone by mutableStateOf("")
    var recoveryBloodGroup by mutableStateOf("A+")
    var recoveryDistrict by mutableStateOf("Dhaka")
    var recoveryUpazila by mutableStateOf("Mirpur")
    
    private val _isRecovering = MutableStateFlow(false)
    val isRecovering: StateFlow<Boolean> = _isRecovering.asStateFlow()
    
    private val _recoveryResult = MutableStateFlow<RecoveryResult?>(null)
    val recoveryResult: StateFlow<RecoveryResult?> = _recoveryResult.asStateFlow()

    fun resetRecoveryState() {
        recoveryName = ""
        recoveryEmail = ""
        recoveryPhone = ""
        recoveryBloodGroup = "A+"
        recoveryDistrict = "Dhaka"
        recoveryUpazila = "Mirpur"
        _recoveryResult.value = null
    }

    fun triggerPasswordRecovery() {
        viewModelScope.launch {
            _isRecovering.value = true
            _recoveryResult.value = null
            try {
                val res = GeminiService.recoverPassword(
                    name = recoveryName,
                    email = recoveryEmail,
                    phone = recoveryPhone,
                    bloodGroup = recoveryBloodGroup,
                    district = recoveryDistrict,
                    upazila = recoveryUpazila,
                    registeredDonors = repository.donors.value,
                    userPasswords = _userPasswords.value
                )
                _recoveryResult.value = res
            } catch (e: Exception) {
                _recoveryResult.value = RecoveryResult(
                    verified = false,
                    password = null,
                    message = "ত্রুটি: ${e.localizedMessage ?: "যাচাইকরণ ব্যর্থ হয়েছে।"}"
                )
            } finally {
                _isRecovering.value = false
            }
        }
    }

    // User Profile edit states
    var profileEditName = ""
    var profileEditPhone = ""
    var profileEditEmail = ""
    var profileEditBlood = ""
    var profileEditDistrict = ""
    var profileEditUpazila = ""
    var profileEditLastDonation = ""
    var profileEditAvailable = true
    var profileEditCountry = "Bangladesh"
    var profileEditRole = "Donor"

    // Request form states
    var reqPatientName = ""
    var reqPatientGender = "Male"
    var reqMedicalCondition = ""
    var reqBloodGroup = "O+"
    var reqBloodAmount = ""
    var reqHospitalName = ""
    var reqDistrict = "Dhaka"
    var reqUpazila = "Mirpur"
    var reqContactNumber = ""
    var reqDetails = ""
    var reqIsEmergency = true
    var reqCountry = "Bangladesh"

    // Registration states
    var regName = ""
    var regPhone = ""
    var regEmail = ""
    var regPassword = ""
    var regBloodGroup = "A+"
    var regDistrict = "Dhaka"
    var regUpazila = "Mirpur"
    var regLastDonation = "Never"
    var regCountry = "Bangladesh"
    var regRole = "Donor"

    // Ambulance form states
    var ambOwnerName = ""
    var ambServiceName = ""
    var ambPhone = ""
    var ambDistrict = "Dhaka"
    var ambUpazila = "Mirpur"
    var ambType = "AC"
    var ambDescription = ""
    var ambCountry = "Bangladesh"

    // --- ACTION DISPATCHERS ---

    fun triggerLogin(isGoogle: Boolean = false): Int {
        val emailToUse = if (isGoogle) "help.alifshen.ltd@gmail.com" else loginEmail
        val phoneToUse = if (isGoogle) "01781223344" else loginPhone
        
        // Find if user exists in the system (either by email or phone)
        val userExists = repository.donors.value.any {
            (emailToUse.isNotBlank() && it.email.equals(emailToUse, ignoreCase = true)) ||
            (phoneToUse.isNotBlank() && it.phone == phoneToUse)
        } || emailToUse.equals("Alifsheenshopping@gmail.com", ignoreCase = true) || emailToUse.equals("help.alifshen.ltd@gmail.com", ignoreCase = true)

        if (!userExists && !isGoogle) {
            return -2 // Account not found
        }

        // Enforce password verification from local passwords map for non-Google login
        if (!isGoogle) {
            val expectedPassword = _userPasswords.value[emailToUse.lowercase()] ?: _userPasswords.value[phoneToUse]
            if (expectedPassword != null && loginPassword != expectedPassword) {
                return -1 // Incorrect password
            }
        }

        val success = repository.loginWithPhoneOrEmail(phoneToUse, emailToUse, loginPassword, isGoogle)
        if (success) {
            // Seed profile edit forms
            val user = repository.currentUser.value
            if (user != null) {
                seedProfileForm(user)
                if (user.email.equals("alifsheenshopping@gmail.com", ignoreCase = true) || 
                    user.email.equals("help.alifshen.ltd@gmail.com", ignoreCase = true) ||
                    user.email.contains("admin", ignoreCase = true) ||
                    user.name.contains("Alif", ignoreCase = true)) {
                    setAdminMode(true)
                }
            }
            return 1 // Success
        }
        return -2 // Default error
    }

    fun triggerSignup(): Boolean {
        if (regName.isBlank() || regPhone.isBlank()) return false
        
        // Save the registered password in our local passwords map
        if (regPassword.isNotBlank()) {
            val newMap = _userPasswords.value.toMutableMap()
            if (regEmail.isNotBlank()) {
                newMap[regEmail.lowercase()] = regPassword
            }
            newMap[regPhone] = regPassword
            _userPasswords.value = newMap
        }

        repository.registerDonor(
            name = regName,
            phone = regPhone,
            email = regEmail,
            bloodGroup = regBloodGroup,
            district = regDistrict,
            upazila = regUpazila,
            lastDonationDate = regLastDonation,
            country = regCountry,
            role = regRole
        )
        // Login session auto sets
        val user = repository.currentUser.value
        if (user != null) {
            seedProfileForm(user)
        }
        return true
    }

    private fun seedProfileForm(user: BloodDonor) {
        profileEditName = user.name
        profileEditPhone = user.phone
        profileEditEmail = user.email
        profileEditBlood = user.bloodGroup
        profileEditDistrict = user.district
        profileEditUpazila = user.upazila
        profileEditLastDonation = user.lastDonationDate
        profileEditAvailable = user.isAvailable
        profileEditCountry = user.country
        profileEditRole = user.role
    }

    fun triggerUpdateProfile() {
        repository.updateProfile(
            name = profileEditName,
            phone = profileEditPhone,
            email = profileEditEmail,
            bloodGroup = profileEditBlood,
            district = profileEditDistrict,
            upazila = profileEditUpazila,
            lastDonation = profileEditLastDonation,
            available = profileEditAvailable,
            country = profileEditCountry,
            role = profileEditRole
        )
    }

    fun triggerSubmitRequest(context: android.content.Context? = null): Boolean {
        if (reqPatientName.isBlank() || reqContactNumber.isBlank() || reqHospitalName.isBlank()) return false
        repository.createBloodRequest(
            context = context,
            patientName = reqPatientName,
            bloodGroup = reqBloodGroup,
            bloodAmount = reqBloodAmount,
            hospitalName = reqHospitalName,
            district = reqDistrict,
            upazila = reqUpazila,
            contactNumber = reqContactNumber,
            details = if (reqDetails.isBlank()) "Urgent blood transfusion needed." else reqDetails,
            isEmergency = reqIsEmergency,
            country = reqCountry,
            patientGender = reqPatientGender,
            medicalCondition = reqMedicalCondition
        )
        // Reset request form fields
        reqPatientName = ""
        reqPatientGender = "Male"
        reqMedicalCondition = ""
        reqHospitalName = ""
        reqBloodAmount = ""
        reqContactNumber = ""
        reqDetails = ""
        return true
    }

    fun recordNewDonation() {
        repository.addDonationToHistory()
        val user = repository.currentUser.value
        if (user != null) {
            seedProfileForm(user)
        }
    }

    fun submitDonationClaim(requestId: String, donorPhone: String, donorName: String, contactNumber: String) {
        repository.submitDonationClaim(requestId, donorPhone, donorName, contactNumber)
    }

    fun acceptDonationClaim(claimId: String) {
        repository.acceptDonationClaim(claimId)
    }

    fun rejectDonationClaim(claimId: String) {
        repository.rejectDonationClaim(claimId)
    }

    fun triggerLogout() {
        repository.logout()
        setAdminMode(false)
        clearBackStackAndNavigateTo(AppScreen.LOGIN_REGISTER)
    }

    // Admin commands
    fun adminApproveDonor(id: String) {
        repository.approveDonor(id)
    }

    fun adminDeleteDonor(id: String) {
        repository.deleteDonor(id)
    }

    fun adminDeleteRequest(id: String) {
        repository.deleteRequest(id)
    }

    fun adminToggleRequest(id: String) {
        repository.toggleRequestStatus(id)
    }

    fun adminDeleteAmbulance(id: String) {
        repository.deleteAmbulance(id)
    }

    fun adminDeleteAmbulanceBooking(id: String) {
        repository.deleteAmbulanceBooking(id)
    }

    fun triggerSubmitScamReport(
        reporterName: String,
        reporterPhone: String,
        scammerDonorId: String,
        scammerDonorName: String,
        scammerDonorPhone: String,
        reason: String,
        amountDemanded: String,
        scammerPhotoUri: String? = null
    ) {
        repository.submitScamReport(
            reporterName = reporterName,
            reporterPhone = reporterPhone,
            scammerDonorId = scammerDonorId,
            scammerDonorName = scammerDonorName,
            scammerDonorPhone = scammerDonorPhone,
            reason = reason,
            amountDemanded = amountDemanded,
            country = detectedCountry.value,
            scammerPhotoUri = scammerPhotoUri
        )
    }

    fun adminActionOnScamReport(id: String, action: String) {
        repository.actionOnReport(id, action)
    }

    fun adminWarnDonor(id: String, isWarning: Boolean, reason: String) {
        repository.warnDonor(id, isWarning, reason)
    }

    fun updateScamReport(
        id: String,
        scammerName: String,
        scammerPhone: String,
        amount: String,
        reason: String,
        status: String
    ) {
        repository.updateScamReport(id, scammerName, scammerPhone, amount, reason, status)
    }

    fun sendInAppChatMessage(
        context: android.content.Context? = null,
        senderPhone: String,
        senderName: String,
        receiverPhone: String,
        receiverName: String,
        messageText: String
    ) {
        repository.sendChatMessage(context, senderPhone, senderName, receiverPhone, receiverName, messageText)
        
        // Auto-reply for Support Chat (Placeholder for real admin response)
        if (receiverPhone == "LIVE_SUPPORT") {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1500) // Delay to simulate typing
                repository.sendChatMessage(
                    null,
                    "LIVE_SUPPORT",
                    "Live Support Admin",
                    senderPhone,
                    senderName,
                    if (language.value == AppLanguage.BAN) 
                        "ধন্যবাদ আপনার মেসেজের জন্য। আমি আলিফ ব্লাড ব্যাংকের অ্যাডমিন বলছি। আমরা শীঘ্রই আপনার সমস্যার সমাধান দিচ্ছি।" 
                    else 
                        "Thank you for your message. This is the Admin of Alif Blood Bank. We will assist you shortly."
                )
            }
        }
    }

    fun markInAppChatRead(userPhone: String, peerPhone: String) {
        repository.markChatAsRead(userPhone, peerPhone)
    }

    fun markNotificationsRead() {
        repository.markAllNotificationsAsRead()
    }

    fun sendSystemNotification(titleEn: String, titleBn: String, messageEn: String, messageBn: String, type: String = "ALERT") {
        repository.addNotification(titleEn, titleBn, messageEn, messageBn, type, detectedCountry.value)
    }

    fun triggerRegisterAmbulance(): Boolean {
        if (ambServiceName.isBlank() || ambPhone.isBlank()) return false
        repository.registerAmbulance(
            ownerName = ambOwnerName,
            serviceName = ambServiceName,
            phone = ambPhone,
            district = ambDistrict,
            upazila = ambUpazila,
            ambulanceType = ambType,
            description = ambDescription,
            country = ambCountry
        )
        // Reset form
        ambOwnerName = ""
        ambServiceName = ""
        ambPhone = ""
        ambDescription = ""
        navigateTo(AppScreen.AMBULANCE_LIST)
        return true
    }

    fun triggerToggleAmbulanceAvailability(ambulanceId: String) {
        repository.toggleAmbulanceAvailability(ambulanceId)
    }

    fun triggerSubmitAmbulanceBooking(
        patientName: String,
        contactPhone: String,
        pickupAddress: String,
        destinationAddress: String,
        ambulanceType: String,
        urgencyLevel: String,
        dateTime: String,
        notes: String
    ) {
        repository.submitAmbulanceBooking(
            patientName = patientName,
            contactPhone = contactPhone,
            pickupAddress = pickupAddress,
            destinationAddress = destinationAddress,
            ambulanceType = ambulanceType,
            urgencyLevel = urgencyLevel,
            dateTime = dateTime,
            notes = notes
        )
    }

    fun triggerUpdateBookingStatus(
        bookingId: String,
        newStatus: String,
        assignedName: String? = null,
        assignedPhone: String? = null,
        adminNotes: String? = null,
        fare: Double? = null
    ) {
        repository.updateBookingStatus(bookingId, newStatus, assignedName, assignedPhone, adminNotes, fare)
    }

    fun triggerPayBookingCommission(
        bookingId: String,
        method: String,
        txnId: String,
        phone: String
    ) {
        repository.payBookingCommission(bookingId, method, txnId, phone)
    }

    // POLICY STATE STREAMS
    val privacyPolicyEn: StateFlow<String> = repository.privacyPolicyEn
    val privacyPolicyBn: StateFlow<String> = repository.privacyPolicyBn

    val termsConditionsEn: StateFlow<String> = repository.termsConditionsEn
    val termsConditionsBn: StateFlow<String> = repository.termsConditionsBn

    val refundPolicyEn: StateFlow<String> = repository.refundPolicyEn
    val refundPolicyBn: StateFlow<String> = repository.refundPolicyBn

    fun updatePolicies(
        privacyEn: String, privacyBn: String,
        termsEn: String, termsBn: String,
        refundEn: String, refundBn: String
    ) {
        repository.updatePolicies(privacyEn, privacyBn, termsEn, termsBn, refundEn, refundBn)
    }

    // --- V9 SUBSCRIPTION OPERATIONS ---
    fun triggerAddOrUpdateSubscriptionPlan(plan: V9SubscriptionPlan) {
        repository.addOrUpdateSubscriptionPlan(plan)
    }

    fun triggerDeleteSubscriptionPlan(planId: String) {
        repository.deleteSubscriptionPlan(planId)
    }

    fun triggerAddUserSubscription(sub: UserSubscription) {
        repository.addUserSubscription(sub)
    }
}

enum class AppScreen {
    SPLASH,
    LOGIN_REGISTER,
    HOME,
    SEARCH_DONOR,
    DONOR_PROFILE,
    REQUEST_BLOOD,
    EMERGENCY_REQUESTS,
    NOTIFICATIONS,
    USER_PROFILE,
    ADMIN_DASHBOARD,
    PRIVACY_POLICY,
    TERMS_CONDITIONS,
    REFUND_POLICY,
    CHAT_INBOX,
    CHAT_ROOM,
    REQUEST_DETAIL,
    AMBULANCE_LIST,
    ADD_AMBULANCE,
    BOOK_AMBULANCE,
    AMBULANCE_BOOKINGS,
    AMBULANCE_DASHBOARD,
    SUPPORT_CHAT
}
