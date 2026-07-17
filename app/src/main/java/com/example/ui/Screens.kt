package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.data.*
import com.example.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage

data class HospitalInfo(
    val name: String,
    val banglaName: String,
    val district: String,
    val upazila: String,
    val country: String
)

@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val appName by viewModel.appName.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val context = LocalContext.current
    val currentUserSession = userSession

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.checkNetworkStatus(context)
        viewModel.detectUserLocation(context)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showAdminPasswordDialog by remember { mutableStateOf(false) }
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminPasswordError by remember { mutableStateOf(false) }

    // State for draggable FAB position
    var fabOffset by remember { mutableStateOf(IntOffset(0, 0)) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        val isWideScreen = maxWidth > 600.dp
        val containerModifier = if (isWideScreen) {
            Modifier
                .width(420.dp)
                .fillMaxHeight()
                .padding(vertical = 16.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(8.dp, Color(0xFF1E1E1E), RoundedCornerShape(28.dp))
        } else {
            Modifier.fillMaxSize()
        }

        Surface(
            modifier = containerModifier,
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Main content based on active screen state
                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    if (screen == AppScreen.SPLASH) {
                        SplashScreen(viewModel)
                    } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                modifier = Modifier.width(310.dp),
                                drawerContainerColor = MaterialTheme.colorScheme.surface,
                                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                            ) {
                                // Drawer Header
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(BloodRed, DarkBloodRed)
                                            )
                                        )
                                        .padding(vertical = 24.dp, horizontal = 16.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Bloodtype,
                                                contentDescription = "App Logo",
                                                tint = Color.White,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = appName,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = strings["splash_tagline"] ?: "Every blood donor is a hero",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(20.dp))
                                        
                                        // User Info
                                        if (currentUserSession != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = currentUserSession.bloodGroup,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = currentUserSession.name,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                    Text(
                                                        text = currentUserSession.phone,
                                                        color = Color.White.copy(alpha = 0.8f),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        scope.launch { drawerState.close() }
                                                        viewModel.setShowRegistrationTab(false)
                                                        viewModel.navigateTo(AppScreen.LOGIN_REGISTER)
                                                    }
                                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AccountCircle,
                                                    contentDescription = "Guest",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = if (language == AppLanguage.ENG) "Guest User" else "অতিথি ব্যবহারকারী",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                    Text(
                                                        text = if (language == AppLanguage.ENG) "Tap to Login / Register" else "লগইন / রেজিস্টার করতে ট্যাপ করুন",
                                                        color = Color.White.copy(alpha = 0.8f),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Navigational options
                                ScrollableDrawerItems(
                                    strings = strings,
                                    currentLanguage = language,
                                    currentScreen = screen,
                                    userSession = currentUserSession,
                                    onItemClick = { targetScreen ->
                                        if (targetScreen == AppScreen.SUPPORT_CHAT) {
                                            scope.launch { drawerState.close() }
                                            AdManager.showRewarded(context) {
                                                viewModel.startSupportChat()
                                            }
                                        } else if (targetScreen == AppScreen.ADMIN_DASHBOARD) {
                                            showAdminPasswordDialog = true
                                        } else {
                                            if (targetScreen == AppScreen.LOGIN_REGISTER) {
                                                viewModel.setShowRegistrationTab(false)
                                            }
                                            scope.launch { drawerState.close() }
                                            AdManager.showInterstitial(context) {
                                                viewModel.navigateTo(targetScreen)
                                            }
                                        }
                                    },
                                    onLanguageToggle = {
                                        scope.launch { drawerState.close() }
                                        viewModel.toggleLanguage()
                                    },
                                    onLogout = {
                                        scope.launch { drawerState.close() }
                                        viewModel.triggerLogout()
                                    },
                                    isAdmin = currentUserSession?.email?.equals("Alifsheenshopping@gmail.com", ignoreCase = true) == true || currentUserSession?.email?.equals("help.alifshen.ltd@gmail.com", ignoreCase = true) == true || currentUserSession?.email?.contains("admin") == true || currentUserSession?.name?.contains("Alif") == true
                                )
                            }
                        }
                    ) {
                        // All general application screens have a common scaffold with navigation
                        Scaffold(
                            topBar = {
                                CommonTopAppBar(
                                    title = appName,
                                    currentLang = language,
                                    onLangToggle = { viewModel.toggleLanguage() },
                                    onBack = { viewModel.navigateBack() },
                                    showBack = screen != AppScreen.HOME && screen != AppScreen.LOGIN_REGISTER,
                                    userSession = userSession,
                                    onProfileClick = {
                                        if (userSession == null) {
                                            viewModel.setShowRegistrationTab(false)
                                            viewModel.navigateTo(AppScreen.LOGIN_REGISTER)
                                        } else {
                                            viewModel.navigateTo(AppScreen.USER_PROFILE)
                                        }
                                    },
                                    onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH_DONOR) },
                                    onMenuClick = {
                                        scope.launch {
                                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                        }
                                    },
                                    viewModel = viewModel
                                )
                            },
                            bottomBar = {
                                CommonBottomNavigationBar(
                                    currentScreen = screen,
                                    onNavigate = { targetScreen ->
                                        if (userSession == null) {
                                            // Handle auto guest-login when clicking navigation items
                                            viewModel.clearBackStackAndNavigateTo(targetScreen)
                                        } else {
                                            AdManager.showInterstitial(context) {
                                                viewModel.navigateTo(targetScreen)
                                            }
                                        }
                                    },
                                    isAdmin = viewModel.isAdminMode.collectAsState().value,
                                    strings = strings
                                )
                            },
                            contentWindowInsets = WindowInsets.safeDrawing
                        ) { paddingValues ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                AdBanner(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(vertical = 4.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    when (screen) {
                                        AppScreen.LOGIN_REGISTER -> LoginRegisterScreen(viewModel)
                                        AppScreen.HOME -> HomeScreen(viewModel)
                                        AppScreen.SEARCH_DONOR -> SearchDonorScreen(viewModel)
                                        AppScreen.DONOR_PROFILE -> DonorProfileScreen(viewModel)
                                        AppScreen.REQUEST_BLOOD -> RequestBloodScreen(viewModel)
                                        AppScreen.EMERGENCY_REQUESTS -> EmergencyRequestsScreen(viewModel)
                                        AppScreen.NOTIFICATIONS -> NotificationsScreen(viewModel)
                                        AppScreen.USER_PROFILE -> UserProfileScreen(viewModel)
                                        AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel)
                                        AppScreen.PRIVACY_POLICY -> PrivacyPolicyScreen(viewModel)
                                        AppScreen.TERMS_CONDITIONS -> TermsConditionsScreen(viewModel)
                                        AppScreen.REFUND_POLICY -> RefundPolicyScreen(viewModel)
                                        AppScreen.CHAT_INBOX -> ChatInboxScreen(viewModel)
                                        AppScreen.CHAT_ROOM -> ChatRoomScreen(viewModel)
                                        AppScreen.REQUEST_DETAIL -> RequestDetailScreen(viewModel)
                                        AppScreen.AMBULANCE_LIST -> AmbulanceListScreen(viewModel)
                                        AppScreen.ADD_AMBULANCE -> AddAmbulanceScreen(viewModel)
                                        AppScreen.BOOK_AMBULANCE -> BookAmbulanceScreen(viewModel)
                                        AppScreen.AMBULANCE_BOOKINGS -> AmbulanceBookingsScreen(viewModel)
                                        AppScreen.AMBULANCE_DASHBOARD -> AmbulanceDashboardScreen(viewModel)
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Draggable Live Support FAB Overlay
            if (currentScreen != AppScreen.CHAT_ROOM && currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.LOGIN_REGISTER) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp, end = 16.dp), // Initial padding near bottom bar
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = {
                            AdManager.showRewarded(context) {
                                viewModel.startSupportChat()
                            }
                        },
                        containerColor = BloodRed,
                        contentColor = Color.White,
                        modifier = Modifier
                            .offset { fabOffset }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    fabOffset = IntOffset(
                                        x = fabOffset.x + dragAmount.x.roundToInt(),
                                        y = fabOffset.y + dragAmount.y.roundToInt()
                                    )
                                }
                            }
                    ) {
                        Icon(Icons.Filled.HeadsetMic, contentDescription = "Support Chat")
                    }
                }
            }

            // Admin Password Dialog
            if (showAdminPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showAdminPasswordDialog = false
                        adminPasswordInput = ""
                        adminPasswordError = false
                    },
                    title = { Text(text = strings["admin_auth_title"] ?: "Admin Authentication") },
                    text = {
                        Column {
                            Text(text = strings["admin_auth_msg"] ?: "Please enter Admin Password to continue:")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = adminPasswordInput,
                                onValueChange = { 
                                    adminPasswordInput = it
                                    adminPasswordError = false
                                },
                                label = { Text(strings["password_label"] ?: "Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                isError = adminPasswordError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (adminPasswordError) {
                                Text(
                                    text = strings["invalid_password"] ?: "Invalid password",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (adminPasswordInput == "admin123") {
                                    showAdminPasswordDialog = false
                                    adminPasswordInput = ""
                                    scope.launch { drawerState.close() }
                                    viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                                } else {
                                    adminPasswordError = true
                                }
                            }
                        ) {
                            Text(strings["btn_confirm"] ?: "Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showAdminPasswordDialog = false
                            adminPasswordInput = ""
                            adminPasswordError = false
                        }) {
                            Text(strings["btn_cancel"] ?: "Cancel")
                        }
                    }
                )
            }

            if (!isOnline) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF9FAFB))
                        .clickable(enabled = false) { }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFFFFEBEE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "No Internet Icon",
                                tint = BloodRed,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Text(
                            text = if (language == AppLanguage.ENG) "No Internet Connection" else "কোনো ইন্টারনেট সংযোগ নেই",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DarkText,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = if (language == AppLanguage.ENG) 
                                "Alif Blood Bank requires an active Mobile Data or Wi-Fi connection to function. Please turn on internet access and try again." 
                            else 
                                "আলিফ ব্লাড ব্যাংক অ্যাপটি ব্যবহার করতে সচল মোবাইল ডাটা অথবা ওয়াই-ফাই সংযোগ প্রয়োজন। অনুগ্রহ করে আপনার ইন্টারনেট সংযোগ সচল করে পুনরায় চেষ্টা করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(36.dp))
                        
                        Button(
                            onClick = {
                                viewModel.checkNetworkStatus(context)
                                if (viewModel.isOnline.value) {
                                    android.widget.Toast.makeText(
                                        context, 
                                        if (language == AppLanguage.ENG) "Internet connected successfully!" else "ইন্টারনেট সফলভাবে সংযুক্ত হয়েছে!", 
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    android.widget.Toast.makeText(
                                        context, 
                                        if (language == AppLanguage.ENG) "Still offline. Please check your network connection." else "এখনো কোনো সংযোগ পাওয়া যায়নি। অনুগ্রহ করে পুনরায় চেক করুন।", 
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("offline_retry_connection_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry Connection Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.ENG) "Try Again" else "পুনরায় চেষ্টা করুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// --- COMMON UI COMPONENTS ---

@Composable
fun V9SubscriptionDialog(
    viewModel: MainViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val plans by viewModel.subscriptionPlans.collectAsState()
    val subscriptions by viewModel.userSubscriptions.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val currentBkash by viewModel.bkashNumber.collectAsState()
    val currentNagad by viewModel.nagadNumber.collectAsState()
    val currentRocket by viewModel.rocketNumber.collectAsState()
    val currentGooglePlay by viewModel.googlePlayMerchant.collectAsState()

    val detectedCountryCode by viewModel.detectedCountryCode.collectAsState()
    val detectedCountry by viewModel.detectedCountry.collectAsState()

    val activeSub = remember(subscriptions, currentUser) {
        val user = currentUser
        if (user == null) null
        else subscriptions.find { it.userPhone == user.phone && !it.isExpired }
    }

    var selectedPlan by remember { mutableStateOf<V9SubscriptionPlan?>(null) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    var billingRegion by remember { mutableStateOf(if (detectedCountryCode == "BD" || detectedCountry == "Bangladesh") "BD" else "INT") }
    var paymentMethod by remember { mutableStateOf("bKash") }
    var transactionId by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }

    // GPay billing simulator state
    var isProcessingGPay by remember { mutableStateOf(false) }
    var isGPaySuccess by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFA500),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "V9 Premium Subscriptions" else "ভি৯ প্রিমিয়াম সাবস্ক্রিপশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BloodRed
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (activeSub != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (language == AppLanguage.ENG) "★ Active Subscription" else "★ সক্রিয় সাবস্ক্রিপশন",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57F17),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (language == AppLanguage.ENG) "Plan" else "প্যাক"}: ${if (language == AppLanguage.ENG) activeSub.planNameEn else activeSub.planNameBn}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${if (language == AppLanguage.ENG) "Price" else "মূল্য"}: ${activeSub.pricePaid} BDT",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${if (language == AppLanguage.ENG) "Expires on" else "মেয়াদ শেষ হবে"}: ${activeSub.endDate}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!showPaymentSheet) {
                    Text(
                        text = if (language == AppLanguage.ENG) 
                            "Upgrade to V9 premium plan to unlock VIP badge, ad-free experience, and dedicated blood request prioritizing system." 
                        else 
                            "ভিআইপি ব্যাজ, বিজ্ঞাপন-মুক্ত অভিজ্ঞতা এবং অগ্রাধিকার রক্তের অনুরোধের সুবিধা পেতে ভি৯ প্রিমিয়াম প্যাকে আপগ্রেড করুন।",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = if (language == AppLanguage.ENG) "Select a Plan:" else "প্যাক নির্বাচন করুন:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    if (plans.isEmpty()) {
                        Text(
                            text = if (language == AppLanguage.ENG) "No subscription plans available." else "কোন প্যাক পাওয়া যায়নি।",
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }

                    // Gorgeous Horizontal Box System for subscription plans
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        plans.forEach { plan ->
                            val isSelected = selectedPlan?.id == plan.id
                            val isPopular = plan.id.contains("standard") || plan.id.contains("premium")
                            
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { selectedPlan = plan },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFFFEBEE) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BloodRed else Color(0xFFE5E7EB)
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        if (isPopular) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFFF4E5), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    .align(Alignment.End)
                                            ) {
                                                Text(
                                                    text = if (language == AppLanguage.ENG) "POPULAR" else "সেরা অফার",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD84315)
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(14.dp))
                                        }

                                        Text(
                                            text = if (language == AppLanguage.ENG) plan.nameEn else plan.nameBn,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) BloodRed else DarkText,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "${plan.price.toInt()}",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 24.sp,
                                                color = BloodRed
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (language == AppLanguage.ENG) "BDT" else "টাকা",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 3.dp)
                                            )
                                        }

                                        Text(
                                            text = "${if (language == AppLanguage.ENG) "Validity" else "মেয়াদ"}: ${plan.durationDays} ${if (language == AppLanguage.ENG) "Days" else "দিন"}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32),
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )

                                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

                                        // Feature highlights
                                        val highlights = if (language == AppLanguage.ENG) {
                                            listOf("✓ VIP Badge on Profile", "✓ Priority Blood Request", "✓ Fully Ad-Free App")
                                        } else {
                                            listOf("✓ প্রোফাইলে ভিআইপি ব্যাজ", "✓ রক্তের জরুরি নোটিফিকেশন", "✓ বিজ্ঞাপন-মুক্ত অ্যাপ")
                                        }
                                        highlights.forEach { h ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = h, fontSize = 9.sp, color = Color.DarkGray)
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Box(modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = BloodRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val plan = selectedPlan!!
                    
                    // Toggle for Billing Region
                    Text(
                        text = if (language == AppLanguage.ENG) "Select Billing Region:" else "বিলিং অঞ্চল নির্বাচন করুন:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (billingRegion == "BD") Color.White else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { 
                                    billingRegion = "BD"
                                    paymentMethod = "bKash"
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.ENG) "Bangladesh (মোবাইল ব্যাংকিং)" else "বাংলাদেশ (বিকাশ/নগদ/রকেট)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (billingRegion == "BD") BloodRed else Color.Gray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (billingRegion == "INT") Color.White else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { 
                                    billingRegion = "INT"
                                    paymentMethod = "Google Play"
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.ENG) "International (Google Play)" else "বাহির/আন্তর্জাতিক (গুগল প্লে)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (billingRegion == "INT") BloodRed else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (billingRegion == "BD") {
                        Text(
                            text = if (language == AppLanguage.ENG) "Complete Your Payment" else "পেমেন্ট সম্পন্ন করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BloodRed,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "${if (language == AppLanguage.ENG) "Amount to Pay" else "পরিশোধের পরিমাণ"}: ${plan.price} BDT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = if (language == AppLanguage.ENG) "Payment Method:" else "পেমেন্ট মাধ্যম:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("bKash", "Nagad", "Rocket").forEach { method ->
                                val isMethodSelected = paymentMethod == method
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isMethodSelected) BloodRed else Color(0xFFF3F4F6),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { paymentMethod = method }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = method,
                                        color = if (isMethodSelected) Color.White else DarkText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        val targetNumber = when (paymentMethod) {
                            "Nagad" -> currentNagad
                            "Rocket" -> currentRocket
                            else -> currentBkash
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (language == AppLanguage.ENG) 
                                "Please send money to our official $paymentMethod number $targetNumber and enter details below:" 
                            else 
                                "দয়া করে আমাদের অফিশিয়াল $paymentMethod নম্বর $targetNumber এ সেন্ড মানি করুন এবং নিচের তথ্যগুলো দিন:",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = senderPhone,
                            onValueChange = { senderPhone = it },
                            label = { Text(if (language == AppLanguage.ENG) "Your Payment Number" else "আপনার পেমেন্ট নম্বর") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = transactionId,
                            onValueChange = { transactionId = it },
                            label = { Text(if (language == AppLanguage.ENG) "Transaction ID" else "ট্রানজেকশন আইডি (TxnID)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    } else {
                        // GOOGLE PLAY SECURE BILLING SIMULATOR BOX
                        val usdPrice = remember(plan) { (plan.price / 115.0).let { "%.2f".format(it) } }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFF4285F4),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Google Play Billing",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF202124)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE8F0FE), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SECURE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1967D2)
                                        )
                                    }
                                }
                                
                                Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ENG) plan.nameEn else plan.nameBn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DarkText
                                        )
                                        Text(
                                            text = "App Store Merchant: $currentGooglePlay",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = "$$usdPrice USD",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color(0xFF202124)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (isProcessingGPay) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color(0xFF4285F4)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Connecting to Google Play securely...",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                } else if (isGPaySuccess) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Google Play verified successfully!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            isProcessingGPay = true
                                            // Process simulator delay
                                            val sPhone = currentUser?.phone ?: "Google User"
                                            val txnId = "GPA.3392-4921-5021-${(10000..99999).random()}"
                                            senderPhone = sPhone
                                            transactionId = txnId
                                            paymentMethod = "Google Play"
                                            
                                            // We simulate a secure Google Play billing approval
                                            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
                                            scope.launch {
                                                kotlinx.coroutines.delay(1800)
                                                isProcessingGPay = false
                                                isGPaySuccess = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF202124))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Pay with Google Play",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "By clicking Pay, you authorize Google Play Store to securely verify and charge your connected credit card or Play Balance.",
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val user = currentUser
            if (!showPaymentSheet) {
                Button(
                    onClick = {
                        if (user == null) {
                            android.widget.Toast.makeText(context, if (language == AppLanguage.ENG) "Please login first to subscribe." else "সাবস্ক্রাইব করতে প্রথমে লগইন করুন।", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (selectedPlan == null) {
                            android.widget.Toast.makeText(context, if (language == AppLanguage.ENG) "Please select a plan." else "একটি প্যাক নির্বাচন করুন।", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            // Automatically switch billing region based on detected country
                            billingRegion = if (detectedCountryCode == "BD" || detectedCountry == "Bangladesh") "BD" else "INT"
                            paymentMethod = if (billingRegion == "BD") "bKash" else "Google Play"
                            showPaymentSheet = true
                            isGPaySuccess = false
                            isProcessingGPay = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                ) {
                    Text(if (language == AppLanguage.ENG) "Continue to Payment" else "পেমেন্টে এগিয়ে যান")
                }
            } else {
                Button(
                    onClick = {
                        if (billingRegion == "BD") {
                            if (senderPhone.isBlank() || transactionId.isBlank()) {
                                android.widget.Toast.makeText(context, if (language == AppLanguage.ENG) "Please fill in all payment details" else "সব পেমেন্ট তথ্য পূরণ করুন", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        } else {
                            // International Billing
                            if (!isGPaySuccess) {
                                android.widget.Toast.makeText(context, if (language == AppLanguage.ENG) "Please complete Pay with Google Play verification" else "দয়া করে গুগল প্লে ভেরিফিকেশনটি সম্পন্ন করুন", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }

                        if (user != null) {
                            val sDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            val cal = java.util.Calendar.getInstance()
                            cal.add(java.util.Calendar.DAY_OF_YEAR, selectedPlan!!.durationDays)
                            val eDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)

                            val newSub = UserSubscription(
                                userPhone = user.phone,
                                planId = selectedPlan!!.id,
                                planNameEn = selectedPlan!!.nameEn,
                                planNameBn = selectedPlan!!.nameBn,
                                pricePaid = selectedPlan!!.price,
                                startDate = sDate,
                                endDate = eDate,
                                isExpired = false,
                                transactionId = transactionId,
                                paymentMethod = paymentMethod
                            )
                            viewModel.triggerAddUserSubscription(newSub)
                            android.widget.Toast.makeText(context, if (language == AppLanguage.ENG) "V9 Subscription Purchased successfully!" else "ভি৯ সাবস্ক্রিপশন সফলভাবে কেনা হয়েছে!", android.widget.Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(
                        text = if (billingRegion == "BD") {
                            if (language == AppLanguage.ENG) "Submit Payment" else "পেমেন্ট জমা দিন"
                        } else {
                            if (language == AppLanguage.ENG) "Confirm Subscription" else "সাবস্ক্রিপশন নিশ্চিত করুন"
                        }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (showPaymentSheet) {
                        showPaymentSheet = false
                        isGPaySuccess = false
                        isProcessingGPay = false
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (language == AppLanguage.ENG) "Cancel" else "বাতিল")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String,
    currentLang: AppLanguage,
    onLangToggle: () -> Unit,
    onBack: () -> Unit,
    showBack: Boolean,
    userSession: BloodDonor?,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    viewModel: MainViewModel? = null
) {
    var showSubscriptionDialog by remember { mutableStateOf(false) }

    if (showSubscriptionDialog && viewModel != null) {
        V9SubscriptionDialog(
            viewModel = viewModel,
            language = currentLang,
            onDismiss = { showSubscriptionDialog = false }
        )
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bloodtype,
                    contentDescription = "Blood Drop",
                    tint = BloodRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BloodRed,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("app_bar_back")) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkText
                    )
                }
            } else if (onMenuClick != null) {
                IconButton(onClick = onMenuClick, modifier = Modifier.testTag("app_bar_menu")) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = DarkText
                    )
                }
            }
        },
        actions = {
            // Search button added to top bar
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = BloodRed
                )
            }

            // Language Selector button (Toggle instantly between ENG & BAN)
            Button(
                onClick = onLangToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightPinkRed,
                    contentColor = DarkBloodRed
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("lang_toggle_btn")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Translate,
                    contentDescription = "Translate",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (currentLang == AppLanguage.ENG) "বাংলা" else "ENG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // V9 Premium Badge Button
            if (viewModel != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showSubscriptionDialog = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Premium V9",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "V9",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (userSession != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BloodRed)
                        .clickable { onProfileClick() }
                        .testTag("app_bar_profile_active"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userSession.bloodGroup,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("app_bar_profile_guest")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Sign In",
                        tint = BloodRed,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            scrolledContainerColor = Color.White
        ),
        modifier = Modifier.shadow(4.dp)
    )
}

@Composable
fun ScrollableDrawerItems(
    strings: Map<String, String>,
    currentLanguage: AppLanguage,
    currentScreen: AppScreen,
    userSession: BloodDonor?,
    onItemClick: (AppScreen) -> Unit,
    onLanguageToggle: () -> Unit,
    onLogout: () -> Unit,
    isAdmin: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val activeColor = LightPinkRed
        val activeContentColor = DarkBloodRed
        
        // Helper lambda for drawer item
        val drawerItem = @Composable { label: String, icon: ImageVector, screen: AppScreen, tag: String ->
            val isSelected = currentScreen == screen
            NavigationDrawerItem(
                label = { Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                selected = isSelected,
                onClick = { onItemClick(screen) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = activeColor,
                    selectedIconColor = activeContentColor,
                    selectedTextColor = activeContentColor,
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = SecondaryText,
                    unselectedTextColor = DarkText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .testTag(tag)
            )
        }

        // 1. Home
        drawerItem(
            strings["btn_nav_home"] ?: "Home",
            Icons.Filled.Home,
            AppScreen.HOME,
            "drawer_home"
        )

        // 2. Search Donors
        drawerItem(
            strings["card_search_donor"] ?: "Search Donor",
            Icons.Filled.Search,
            AppScreen.SEARCH_DONOR,
            "drawer_search"
        )

        // 3. Post Blood Request
        drawerItem(
            strings["card_request_blood"] ?: "Request Blood",
            Icons.Filled.AddCircle,
            AppScreen.REQUEST_BLOOD,
            "drawer_request"
        )

        // 4. Emergency Requests
        drawerItem(
            strings["card_emergency_req"] ?: "Emergency Requests",
            Icons.Filled.LocalHospital,
            AppScreen.EMERGENCY_REQUESTS,
            "drawer_emergency"
        )

        // 5. Notifications
        drawerItem(
            strings["notification_title"] ?: "Notifications",
            Icons.Filled.Notifications,
            AppScreen.NOTIFICATIONS,
            "drawer_notifications"
        )

        // 6. Direct Chat & Messaging
        drawerItem(
            strings["chat_title"] ?: "Chat & Messaging",
            Icons.Filled.Forum,
            AppScreen.CHAT_INBOX,
            "drawer_chat_inbox"
        )

        // 7. Ambulance Service
        drawerItem(
            strings["card_ambulance"] ?: "Ambulance Service",
            Icons.Filled.AirportShuttle,
            AppScreen.AMBULANCE_LIST,
            "drawer_ambulance"
        )

        if (userSession?.role == "Ambulance") {
            drawerItem(
                if (currentLanguage == AppLanguage.ENG) "Ambulance Dashboard" else "অ্যাম্বুলেন্স ড্যাশবোর্ড",
                Icons.Filled.Dashboard,
                AppScreen.AMBULANCE_DASHBOARD,
                "drawer_ambulance_dashboard"
            )
        }

        // 8. Live Support Chat
        drawerItem(
            strings["support_chat"] ?: "Live Support Chat",
            Icons.Filled.HeadsetMic,
            AppScreen.SUPPORT_CHAT,
            "drawer_support_chat"
        )

        // Only show if Admin is authenticated
        if (isAdmin) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LightBorder)
            Text(
                text = if (currentLanguage == AppLanguage.ENG) "Admin Panel" else "এডমিন প্যানেল",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SecondaryText),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            drawerItem(
                strings["dashboard_title"] ?: "Admin Dashboard",
                Icons.Filled.Security,
                AppScreen.ADMIN_DASHBOARD,
                "drawer_admin"
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LightBorder)
        Text(
            text = if (currentLanguage == AppLanguage.ENG) "Settings" else "সেটিংস",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SecondaryText),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // Language Select
        NavigationDrawerItem(
            label = { 
                Text(
                    text = if (currentLanguage == AppLanguage.ENG) "Switch Language (বাংলা)" else "ভাষা পরিবর্তন (English)",
                    fontWeight = FontWeight.Normal
                ) 
            },
            icon = { Icon(imageVector = Icons.Filled.Translate, contentDescription = "Language") },
            selected = false,
            onClick = onLanguageToggle,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = DarkText
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(vertical = 4.dp)
                .testTag("drawer_language")
        )

        // Profile / Login / Logout
        if (userSession != null) {
            drawerItem(
                strings["user_profile_title"] ?: "My Profile",
                Icons.Filled.Person,
                AppScreen.USER_PROFILE,
                "drawer_profile"
            )

            NavigationDrawerItem(
                label = { Text(text = strings["logout"] ?: "Log Out", fontWeight = FontWeight.Normal) },
                icon = { Icon(imageVector = Icons.Filled.ExitToApp, contentDescription = "Log Out") },
                selected = false,
                onClick = onLogout,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = BloodRed,
                    unselectedTextColor = BloodRed
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .testTag("drawer_logout")
            )
        } else {
            NavigationDrawerItem(
                label = { Text(text = strings["login_title"] ?: "Sign In", fontWeight = FontWeight.Normal) },
                icon = { Icon(imageVector = Icons.Filled.Login, contentDescription = "Login") },
                selected = false,
                onClick = { onItemClick(AppScreen.LOGIN_REGISTER) },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = SecondaryText,
                    unselectedTextColor = DarkText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .testTag("drawer_login")
            )
        }
    }
}

@Composable
fun CommonBottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    isAdmin: Boolean,
    strings: Map<String, String>
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.shadow(12.dp)
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text(strings["btn_nav_home"] ?: "Home", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BloodRed,
                selectedTextColor = BloodRed,
                indicatorColor = LightPinkRed
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.CHAT_INBOX || currentScreen == AppScreen.CHAT_ROOM,
            onClick = { onNavigate(AppScreen.CHAT_INBOX) },
            icon = { Icon(Icons.Filled.Chat, contentDescription = "Chat") },
            label = { Text(strings["btn_nav_chat"] ?: "Chat", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BloodRed,
                selectedTextColor = BloodRed,
                indicatorColor = LightPinkRed
            ),
            modifier = Modifier.testTag("nav_chat")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.EMERGENCY_REQUESTS || currentScreen == AppScreen.REQUEST_BLOOD,
            onClick = { onNavigate(AppScreen.EMERGENCY_REQUESTS) },
            icon = { Icon(Icons.Filled.LocalHospital, contentDescription = "Urgent") },
            label = { Text(strings["btn_nav_emergency"] ?: "Urgent", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BloodRed,
                selectedTextColor = BloodRed,
                indicatorColor = LightPinkRed
            ),
            modifier = Modifier.testTag("nav_emergency")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.NOTIFICATIONS,
            onClick = { onNavigate(AppScreen.NOTIFICATIONS) },
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Alerts") },
            label = { Text(strings["btn_nav_notify"] ?: "Alerts", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BloodRed,
                selectedTextColor = BloodRed,
                indicatorColor = LightPinkRed
            ),
            modifier = Modifier.testTag("nav_alerts")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.AMBULANCE_LIST,
            onClick = { onNavigate(AppScreen.AMBULANCE_LIST) },
            icon = { Icon(Icons.Filled.AirportShuttle, contentDescription = "Ambulance") },
            label = { Text(strings["btn_nav_ambulance"] ?: "Ambulance", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BloodRed,
                selectedTextColor = BloodRed,
                indicatorColor = LightPinkRed
            ),
            modifier = Modifier.testTag("nav_ambulance")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.USER_PROFILE,
            onClick = { onNavigate(AppScreen.USER_PROFILE) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text(strings["btn_nav_profile"] ?: "Profile", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BloodRed,
                selectedTextColor = BloodRed,
                indicatorColor = LightPinkRed
            ),
            modifier = Modifier.testTag("nav_profile")
        )

        if (isAdmin) {
            NavigationBarItem(
                selected = currentScreen == AppScreen.ADMIN_DASHBOARD,
                onClick = { onNavigate(AppScreen.ADMIN_DASHBOARD) },
                icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Admin") },
                label = { Text(strings["btn_nav_admin"] ?: "Admin", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BloodRed,
                    selectedTextColor = BloodRed,
                    indicatorColor = LightPinkRed
                ),
                modifier = Modifier.testTag("nav_admin")
            )
        }
    }
}


// --- 1. SPLASH SCREEN ---

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val language by viewModel.language.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.detectUserLocation(context)
        kotlinx.coroutines.delay(3500) // 3.5 seconds aesthetic delay for loading/welcome
        AdManager.showInterstitial(context, forceShow = true) {
            viewModel.clearBackStackAndNavigateTo(AppScreen.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFF5F5)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 1. Logo Image
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_alif_blood_bank_logo_1783648271595),
                contentDescription = "Alif Blood Bank Logo",
                modifier = Modifier
                    .size(220.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 2. App Name
            Text(
                text = if (language == AppLanguage.BAN) "আলিফ ব্লাড ব্যাংক" else "Alif Blood Bank",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD32F2F),
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Welcome Message
            Text(
                text = if (language == AppLanguage.BAN) "আলিফ ব্লাড ব্যাংকে আপনাকে স্বাগতম!" else "Welcome to Alif Blood Bank!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Tagline
            Text(
                text = "নিরাপদ রক্তদান, বাঁচাবে বহু প্রাণ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(44.dp))

            // 5. Sleek Circular Progress Indicator
            CircularProgressIndicator(
                color = Color(0xFFD32F2F),
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Loading Text
            Text(
                text = if (language == AppLanguage.BAN) "লোডিং হচ্ছে, অনুগ্রহ করে অপেক্ষা করুন..." else "Loading, please wait...",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF888888)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Data class for Conversational AI Password Recovery
data class RecoveryMessage(
    val id: String,
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)


// --- 2. LOGIN / REGISTER SCREEN ---

@Composable
fun LoginRegisterScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val appName by viewModel.appName.collectAsState()
    val context = LocalContext.current
    val showRegTab by viewModel.showRegistrationTab.collectAsState()
    var regRoleInput by remember { mutableStateOf("Donor") } // "Donor" or "Requester"
    var selectedTab by remember(showRegTab) {
        mutableStateOf(
            if (showRegTab) {
                if (regRoleInput == "Requester") 1 else 2
            } else {
                0
            }
        )
    }

    var loginMethodIsEmail by remember { mutableStateOf(true) }
    var showLoginSuccessPopup by remember { mutableStateOf(false) }
    var showSignupSuccessPopup by remember { mutableStateOf(false) }
    val isUserInBangladesh by viewModel.isUserInBangladesh.collectAsState()

    // Forgot Password and Recovery States
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val isRecovering by viewModel.isRecovering.collectAsState()
    val recoveryResult by viewModel.recoveryResult.collectAsState()

    // AI Chat Recovery States
    val chatMessages = remember { androidx.compose.runtime.mutableStateListOf<RecoveryMessage>() }
    var chatStep by remember { mutableStateOf(1) } // 1: Name, 2: Phone, 3: Email, 4: Blood Group, 5: District, 6: Upazila, 7: Ready, 8: Result
    var currentChatInput by remember { mutableStateOf("") }

    // Forms states
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    // Register details
    var regNameInput by remember { mutableStateOf("") }
    var regPhoneInput by remember { mutableStateOf("") }
    var regEmailInput by remember { mutableStateOf("") }
    var regPasswordInput by remember { mutableStateOf("") }
    var regBloodInput by remember { mutableStateOf("A+") }
    var regDistrictInput by remember { mutableStateOf("") }
    var regUpazilaInput by remember { mutableStateOf("") }
    var regLastDonationInput by remember { mutableStateOf("Never") }
    val initialDetectedCountry = viewModel.detectedCountry.value
    var regCountryInput by remember { mutableStateOf(initialDetectedCountry) }

    val detectedCountryFlow by viewModel.detectedCountry.collectAsState()
    androidx.compose.runtime.LaunchedEffect(detectedCountryFlow) {
        if (regCountryInput == "Bangladesh" || regCountryInput == "" || regCountryInput == "International" || regCountryInput == "United States") {
            regCountryInput = detectedCountryFlow
            regDistrictInput = ""
            regUpazilaInput = ""
        }
    }

    var expandedBlood by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedUpazila by remember { mutableStateOf(false) }
    var expandedCountry by remember { mutableStateOf(false) }

    // Sync regRoleInput when selectedTab changes
    androidx.compose.runtime.LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            regRoleInput = "Requester"
        } else if (selectedTab == 2) {
            regRoleInput = "Donor"
        } else if (selectedTab == 3) {
            regRoleInput = "Ambulance"
        }
    }

    if (showForgotPasswordDialog) {
        // Handle initial welcome message
        androidx.compose.runtime.LaunchedEffect(showForgotPasswordDialog) {
            if (showForgotPasswordDialog) {
                chatMessages.clear()
                chatStep = 1
                currentChatInput = ""
                viewModel.resetRecoveryState()
                
                val welcomeText = if (language == AppLanguage.BAN) {
                    "আসসালামু আলাইকুম! আমি আপনার এআই পাসওয়ার্ড পুনরুদ্ধার সহকারী। 🤖\n\nআপনার নিবন্ধিত অ্যাকাউন্টটি পুনরুদ্ধার করতে আমি আপনাকে কয়েকটি সাধারণ প্রশ্ন করব। অনুগ্রহ করে একটি একটি করে উত্তর দিন।\n\nশুরু করতে, দয়া করে আপনার **নিবন্ধিত পূর্ণ নাম** টাইপ করুন:"
                } else {
                    "Hello! I am your AI Password Recovery Assistant. 🤖\n\nI will ask you a few quick questions step-by-step to verify and recover your registered account. Please answer one by one.\n\nTo begin, please type your **registered full name**:"
                }
                chatMessages.add(RecoveryMessage("welcome", "AI Assistant", welcomeText, false))
            }
        }

        // Handle typing simulations and successive questions
        androidx.compose.runtime.LaunchedEffect(chatStep) {
            if (chatStep in 2..7) {
                kotlinx.coroutines.delay(1000)
                chatMessages.removeAll { it.text == "..." }
                val idStr = "ai_${System.currentTimeMillis()}"
                when (chatStep) {
                    2 -> {
                        val reply = if (language == AppLanguage.BAN) {
                            "চমৎকার! ধন্যবাদ, **${viewModel.recoveryName}**। এবার দয়া করে আপনার একাউন্টের **মোবাইল নম্বরটি** দিন:"
                        } else {
                            "Excellent! Thank you, **${viewModel.recoveryName}**.\nNow, please provide your registered **mobile phone number**:"
                        }
                        chatMessages.add(RecoveryMessage(idStr, "AI Assistant", reply, false))
                    }
                    3 -> {
                        val reply = if (language == AppLanguage.BAN) {
                            "ধন্যবাদ। এবার আপনার নিবন্ধিত **ইমেইল এড্রেসটি** দিন:"
                        } else {
                            "Got it. Now, please enter your registered **email address**:"
                        }
                        chatMessages.add(RecoveryMessage(idStr, "AI Assistant", reply, false))
                    }
                    4 -> {
                        val reply = if (language == AppLanguage.BAN) {
                            "ধন্যবাদ। আপনার **রক্তের গ্রুপ** কোনটি? নিচের অপশন থেকে নির্বাচন করুন বা টাইপ করুন:"
                        } else {
                            "Thanks. What is your **blood group**?\nPlease select from the quick options below or type it:"
                        }
                        chatMessages.add(RecoveryMessage(idStr, "AI Assistant", reply, false))
                    }
                    5 -> {
                        val reply = if (language == AppLanguage.BAN) {
                            "ধন্যবাদ। আপনার নিবন্ধিত **জেলা** কোনটি? নিচে প্রধান জেলাগুলো দেওয়া হলো অথবা আপনার জেলাটি টাইপ করুন:"
                        } else {
                            "Great. What is your registered **district**?\nSelect one or type your district name:"
                        }
                        chatMessages.add(RecoveryMessage(idStr, "AI Assistant", reply, false))
                    }
                    6 -> {
                        val reply = if (language == AppLanguage.BAN) {
                            "বুঝেছি। শেষ প্রশ্ন, আপনার নিবন্ধিত **উপজেলা** কোনটি?"
                        } else {
                            "I see. Last question, what is your registered **upazila**?"
                        }
                        chatMessages.add(RecoveryMessage(idStr, "AI Assistant", reply, false))
                    }
                    7 -> {
                        val reply = if (language == AppLanguage.BAN) {
                            "সব তথ্য নেওয়া হয়েছে! আমি এখন ডাটাবেস ও এআই ইঞ্জিনের সাথে মিলিয়ে আপনার বিবরণ যাচাই করে দেখছি। দয়া করে কিছুক্ষণ অপেক্ষা করুন... ⏳"
                        } else {
                            "All details collected! I am now securely verifying your details with our database and AI engine. Please wait a moment... ⏳"
                        }
                        chatMessages.add(RecoveryMessage(idStr, "AI Assistant", reply, false))
                        
                        viewModel.triggerPasswordRecovery()
                        chatStep = 8
                    }
                }
            }
        }

        // Handle final password verification outcome
        androidx.compose.runtime.LaunchedEffect(recoveryResult) {
            val result = recoveryResult
            if (result != null && chatStep == 8) {
                chatMessages.removeAll { it.text == "..." }
                val idStr = "ai_res_${System.currentTimeMillis()}"
                if (result.verified) {
                    val successText = if (language == AppLanguage.BAN) {
                        "🎉 অভিনন্দন! আপনার তথ্য সফলভাবে যাচাই করা হয়েছে। আপনার একাউন্টের পাসওয়ার্ড নিচে দেওয়া হলো:"
                    } else {
                        "🎉 Congratulations! Your details have been successfully verified. Your password is shown below:"
                    }
                    chatMessages.add(RecoveryMessage(idStr, "AI Assistant", successText, false))
                } else {
                    val errorText = if (language == AppLanguage.BAN) {
                        "❌ যাচাইকরণ ব্যর্থ হয়েছে!\n\n${result.message}"
                    } else {
                        "❌ Verification failed!\n\n${result.message}"
                    }
                    chatMessages.add(RecoveryMessage(idStr, "AI Assistant", errorText, false))
                }
            }
        }

        AlertDialog(
            onDismissRequest = {
                if (!isRecovering) {
                    showForgotPasswordDialog = false
                    viewModel.resetRecoveryState()
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "AI Chat Recovery",
                        tint = BloodRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (language == AppLanguage.BAN) "এআই সিকিউরিটি চ্যাট" else "AI Security Chat",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BloodRed)
                    )
                }
            },
            text = {
                val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
                
                // Auto-scroll to the last message when messages change size
                androidx.compose.runtime.LaunchedEffect(chatMessages.size) {
                    if (chatMessages.isNotEmpty()) {
                        lazyListState.animateScrollToItem(chatMessages.size - 1)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Chat Messages LazyColumn
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { message ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
                            ) {
                                // Sender name bubble tag
                                Text(
                                    text = if (message.isUser) (if (language == AppLanguage.BAN) "আপনি" else "You") else "Alif AI Security",
                                    fontSize = 9.sp,
                                    color = SecondaryText,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                                
                                // Message bubble
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (message.isUser) BloodRed else Color(0xFFF1F3F4)
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (message.isUser) 12.dp else 0.dp,
                                        bottomEnd = if (message.isUser) 0.dp else 12.dp
                                    ),
                                    modifier = Modifier.widthIn(max = 220.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        if (message.text == "...") {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(12.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = if (language == AppLanguage.BAN) "লিখছে..." else "Typing...",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = message.text,
                                                fontSize = 12.sp,
                                                color = if (message.isUser) Color.White else DarkText,
                                                fontWeight = FontWeight.Normal,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // If successfully verified, show the password explicitly with a copy button in the scroll view
                        if (chatStep == 8 && recoveryResult?.verified == true && recoveryResult?.password != null) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (language == AppLanguage.BAN) "পাসওয়ার্ডটি কপি করুন:" else "Copy Password:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = recoveryResult?.password ?: "",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clip = android.content.ClipData.newPlainText("password", recoveryResult?.password)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, if (language == AppLanguage.BAN) "পাসওয়ার্ড কপি করা হয়েছে!" else "Password copied!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ContentCopy,
                                                    contentDescription = "Copy Password",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // If verification failed, show restart button
                        if (chatStep == 8 && recoveryResult?.verified == false) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        onClick = {
                                            chatMessages.clear()
                                            chatStep = 1
                                            currentChatInput = ""
                                            viewModel.resetRecoveryState()
                                            val welcomeText = if (language == AppLanguage.BAN) {
                                                "চলুন আবার চেষ্টা করি! 🤖\n\nদয়া করে আপনার **নিবন্ধিত পূর্ণ নাম** টাইপ করুন:"
                                            } else {
                                                "Let's try again! 🤖\n\nPlease type your **registered full name**:"
                                            }
                                            chatMessages.add(RecoveryMessage("welcome_retry", "AI Assistant", welcomeText, false))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.Sync, contentDescription = "restart", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (language == AppLanguage.BAN) "আবার শুরু করুন" else "Restart Recovery", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Quick Reply Chips / Buttons depending on the step
                    if (chatStep == 4) { // Blood groups
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val groups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                            items(groups) { bg ->
                                androidx.compose.material3.SuggestionChip(
                                    onClick = {
                                        val input = bg
                                        chatMessages.add(RecoveryMessage("user_bg_${System.currentTimeMillis()}", "You", input, true))
                                        viewModel.recoveryBloodGroup = input
                                        chatStep = 5
                                        currentChatInput = ""
                                        chatMessages.add(RecoveryMessage("ai_thinking_${System.currentTimeMillis()}", "AI Assistant", "...", false))
                                    },
                                    label = { Text(bg, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BloodRed) }
                                )
                            }
                        }
                    } else if (chatStep == 5) { // District suggestions
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val popularDistricts = listOf("Dhaka", "Chattogram", "Rajshahi", "Sylhet", "Khulna", "Barishal", "Rangpur", "Mymensingh")
                            items(popularDistricts) { dist ->
                                androidx.compose.material3.SuggestionChip(
                                    onClick = {
                                        val input = dist
                                        chatMessages.add(RecoveryMessage("user_dist_${System.currentTimeMillis()}", "You", input, true))
                                        viewModel.recoveryDistrict = input
                                        chatStep = 6
                                        currentChatInput = ""
                                        chatMessages.add(RecoveryMessage("ai_thinking_${System.currentTimeMillis()}", "AI Assistant", "...", false))
                                    },
                                    label = { Text(dist, fontSize = 10.sp, color = Color.DarkGray) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text Input Bar
                    if (chatStep < 7) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val keyboardType = when (chatStep) {
                                2 -> KeyboardType.Phone
                                3 -> KeyboardType.Email
                                else -> KeyboardType.Text
                            }
                            
                            val placeholderText = when (chatStep) {
                                1 -> if (language == AppLanguage.BAN) "আপনার নাম লিখুন..." else "Enter full name..."
                                2 -> if (language == AppLanguage.BAN) "মোবাইল নম্বর লিখুন..." else "Enter phone number..."
                                3 -> if (language == AppLanguage.BAN) "ইমেইল এড্রেস..." else "Enter email..."
                                4 -> if (language == AppLanguage.BAN) "রক্তের গ্রুপ..." else "Enter blood group..."
                                5 -> if (language == AppLanguage.BAN) "জেলার নাম..." else "Enter district..."
                                6 -> if (language == AppLanguage.BAN) "উপজেলার নাম..." else "Enter upazila..."
                                else -> ""
                            }

                            OutlinedTextField(
                                value = currentChatInput,
                                onValueChange = { currentChatInput = it },
                                placeholder = { Text(placeholderText, fontSize = 12.sp, color = Color.Gray) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("recovery_chat_input"),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = keyboardType,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Send
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onSend = {
                                        if (currentChatInput.isNotBlank()) {
                                            val input = currentChatInput.trim()
                                            currentChatInput = ""
                                            chatMessages.add(RecoveryMessage("user_${System.currentTimeMillis()}", "You", input, true))
                                            when (chatStep) {
                                                1 -> {
                                                    viewModel.recoveryName = input
                                                    chatStep = 2
                                                }
                                                2 -> {
                                                    viewModel.recoveryPhone = input
                                                    chatStep = 3
                                                }
                                                3 -> {
                                                    viewModel.recoveryEmail = input
                                                    chatStep = 4
                                                }
                                                4 -> {
                                                    viewModel.recoveryBloodGroup = input
                                                    chatStep = 5
                                                }
                                                5 -> {
                                                    viewModel.recoveryDistrict = input
                                                    chatStep = 6
                                                }
                                                6 -> {
                                                    viewModel.recoveryUpazila = input
                                                    chatStep = 7
                                                }
                                            }
                                            chatMessages.add(RecoveryMessage("ai_thinking_${System.currentTimeMillis()}", "AI Assistant", "...", false))
                                        }
                                    }
                                )
                            )

                            IconButton(
                                onClick = {
                                    if (currentChatInput.isNotBlank()) {
                                        val input = currentChatInput.trim()
                                        currentChatInput = ""
                                        chatMessages.add(RecoveryMessage("user_${System.currentTimeMillis()}", "You", input, true))
                                        when (chatStep) {
                                            1 -> {
                                                viewModel.recoveryName = input
                                                chatStep = 2
                                            }
                                            2 -> {
                                                viewModel.recoveryPhone = input
                                                chatStep = 3
                                            }
                                            3 -> {
                                                viewModel.recoveryEmail = input
                                                chatStep = 4
                                            }
                                            4 -> {
                                                viewModel.recoveryBloodGroup = input
                                                chatStep = 5
                                            }
                                            5 -> {
                                                viewModel.recoveryDistrict = input
                                                chatStep = 6
                                            }
                                            6 -> {
                                                viewModel.recoveryUpazila = input
                                                chatStep = 7
                                            }
                                        }
                                        chatMessages.add(RecoveryMessage("ai_thinking_${System.currentTimeMillis()}", "AI Assistant", "...", false))
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(BloodRed, CircleShape)
                                    .testTag("recovery_chat_send_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = false
                        viewModel.resetRecoveryState()
                    }
                ) {
                    Text(
                        text = if (language == AppLanguage.BAN) "বন্ধ করুন" else "Close",
                        color = DarkText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }



    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val districts = remember(regCountryInput) {
        when (regCountryInput) {
            "United States" -> listOf("New York", "California", "Texas")
            "India" -> listOf("Delhi", "Maharashtra", "Karnataka")
            "Saudi Arabia" -> listOf("Riyadh", "Makkah")
            "United Arab Emirates" -> listOf("Dubai", "Abu Dhabi")
            "United Kingdom" -> listOf("London", "Greater Manchester")
            else -> MockData.districts
        }
    }
    val availableUpazilas = remember(regCountryInput, regDistrictInput) {
        when (regCountryInput) {
            "United States" -> when (regDistrictInput) {
                "New York" -> listOf("Manhattan", "Queens", "Brooklyn")
                "California" -> listOf("San Francisco", "Los Angeles", "San Jose")
                "Texas" -> listOf("Houston", "Dallas", "Austin")
                else -> listOf("Manhattan")
            }
            "India" -> when (regDistrictInput) {
                "Delhi" -> listOf("Connaught Place")
                "Maharashtra" -> listOf("Mumbai Worli")
                "Karnataka" -> listOf("Bangalore Indiranagar")
                else -> listOf("Connaught Place")
            }
            "Saudi Arabia" -> when (regDistrictInput) {
                "Riyadh" -> listOf("Al-Olaya")
                "Makkah" -> listOf("Jeddah Al-Hamra")
                else -> listOf("Al-Olaya")
            }
            "United Arab Emirates" -> when (regDistrictInput) {
                "Dubai" -> listOf("Dubai Marina")
                "Abu Dhabi" -> listOf("Al-Reem Island")
                else -> listOf("Dubai Marina")
            }
            "United Kingdom" -> when (regDistrictInput) {
                "London" -> listOf("Westminster")
                "Greater Manchester" -> listOf("Deansgate")
                else -> listOf("Westminster")
            }
            else -> MockData.getUpazilasForDistrict(regDistrictInput)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MedicalBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier.testTag("login_back_home")
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back to Home",
                    tint = DarkText
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            imageVector = Icons.Filled.Bloodtype,
            contentDescription = "Blood Red",
            tint = BloodRed,
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = appName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = BloodRed
            )
        )

        Text(
            text = strings["login_subtitle"] ?: "Connect with active donors instantly",
            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Tab Row Switcher (4 options)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(25.dp))
                .padding(4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { selectedTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) BloodRed else Color.Transparent,
                    contentColor = if (selectedTab == 0) Color.White else DarkText
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .testTag("tab_login"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BAN) "লগ ইন" else "Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Button(
                onClick = { selectedTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) BloodRed else Color.Transparent,
                    contentColor = if (selectedTab == 1) Color.White else DarkText
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .testTag("tab_register_seeker"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BAN) "রক্ত গ্রহীতা" else "Blood Seeker",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Button(
                onClick = { selectedTab = 2 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 2) BloodRed else Color.Transparent,
                    contentColor = if (selectedTab == 2) Color.White else DarkText
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .testTag("tab_register_donor"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BAN) "রক্তদাতা" else "Join Donor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Button(
                onClick = { selectedTab = 3 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 3) BloodRed else Color.Transparent,
                    contentColor = if (selectedTab == 3) Color.White else DarkText
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .testTag("tab_register_ambulance"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BAN) "অ্যাম্বুলেন্স" else "Ambulance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (selectedTab == 0) {
            // LOGIN FORM
            if (loginMethodIsEmail) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(strings["email_label"] ?: "Email Address") },
                    placeholder = { Text(strings["email_placeholder"] ?: "e.g., donor@email.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") }
                )
            } else {
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text(strings["phone_label"] ?: "Phone Number") },
                    placeholder = { Text(strings["phone_placeholder"] ?: "e.g., 01712345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text(strings["password_label"] ?: "Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showForgotPasswordDialog = true }
                ) {
                    Text(
                        text = if (language == AppLanguage.BAN) "পাসওয়ার্ড ভুলে গেছেন?" else "Forgot Password?",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }

                TextButton(
                    onClick = { loginMethodIsEmail = !loginMethodIsEmail }
                ) {
                    Text(
                        text = if (loginMethodIsEmail)
                            (if (language == AppLanguage.BAN) "ফোন নম্বর দিয়ে প্রবেশ" else "Log in with Phone")
                        else
                            (if (language == AppLanguage.BAN) "ইমেইল দিয়ে প্রবেশ" else "Log in with Email"),
                        color = BloodRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (loginMethodIsEmail && emailInput.isBlank()) {
                        Toast.makeText(context, if (language == AppLanguage.BAN) "অনুগ্রহ করে ইমেইল দিন" else "Please enter Email", Toast.LENGTH_SHORT).show()
                    } else if (!loginMethodIsEmail && phoneInput.isBlank()) {
                        Toast.makeText(context, if (language == AppLanguage.BAN) "অনুগ্রহ করে ফোন নাম্বার দিন" else "Please enter Phone", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.loginPhone = if (loginMethodIsEmail) "" else phoneInput
                        viewModel.loginEmail = if (loginMethodIsEmail) emailInput else ""
                        viewModel.loginPassword = passwordInput
                        val loginResult = viewModel.triggerLogin(isGoogle = false)
                        if (loginResult == 1) {
                            showLoginSuccessPopup = true
                        } else if (loginResult == -1) {
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.BAN) 
                                    "ভুল পাসওয়ার্ড! পাসওয়ার্ড মনে না থাকলে দয়া করে 'পাসওয়ার্ড ভুলে গেছেন?' অপশনে ক্লিক করে পাসওয়ার্ড রিসেট করুন।" 
                                else 
                                    "Incorrect password! If you don't remember your password, please use the 'Forgot Password?' option to reset it.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.BAN) 
                                    "লগইন ব্যর্থ হয়েছে! এই ইমেইল বা ফোন নম্বর দিয়ে কোনো অ্যাকাউন্ট নেই।" 
                                else 
                                    "Login failed! There is no account registered with this email or phone.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("login_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = strings["btn_login"] ?: "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.BAN) "কোনো একাউন্ট নেই? " else "Don't have an account? ",
                    color = SecondaryText,
                    fontSize = 14.sp
                )
                Text(
                    text = if (language == AppLanguage.BAN) "নিবন্ধন করুন" else "Register Now",
                    color = BloodRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { selectedTab = 2 }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    viewModel.clearBackStackAndNavigateTo(AppScreen.HOME)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("continue_guest_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, BloodRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BloodRed)
            ) {
                Icon(Icons.Filled.PersonOutline, contentDescription = "Guest")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.BAN) "অতিথি হিসেবে প্রবেশ করুন" else "Continue as Guest",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // REGISTER FORM
            Text(
                text = if (selectedTab == 1) {
                    if (language == AppLanguage.BAN) "রক্ত গ্রহীতা (Seeker) হিসেবে নিবন্ধন" else "Register as Blood Seeker"
                } else {
                    if (language == AppLanguage.BAN) "রক্তদাতা (Donor) হিসেবে যোগ দিন" else "Join as Blood Donor"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BloodRed
                ),
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
            )

            OutlinedTextField(
                value = regNameInput,
                onValueChange = { regNameInput = it },
                label = { Text(strings["name_label"] ?: "Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_name_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "User icon") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = regPhoneInput,
                onValueChange = { regPhoneInput = it },
                label = { Text(strings["phone_label"] ?: "Phone Number") },
                placeholder = { Text("e.g. 01712345678") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_phone_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = regEmailInput,
                onValueChange = { regEmailInput = it },
                label = { Text(strings["email_label"] ?: "Email Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_email_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = regPasswordInput,
                onValueChange = { regPasswordInput = it },
                label = { Text(strings["password_label"] ?: "Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_password_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock") }
            )

            if (regRoleInput != "Ambulance") {
                Spacer(modifier = Modifier.height(12.dp))

                // Dropdown for Blood Group selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = regBloodInput,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings["bg_label"] ?: "Blood Group") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "down") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Filled.Bloodtype, contentDescription = "Blood Group") }
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expandedBlood = true })
                    
                    DropdownMenu(
                        expanded = expandedBlood,
                        onDismissRequest = { expandedBlood = false }
                    ) {
                        bloodGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group, fontWeight = FontWeight.Bold, color = BloodRed) },
                                onClick = {
                                    regBloodInput = group
                                    expandedBlood = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            val isBD = regCountryInput.equals("Bangladesh", ignoreCase = true)

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = regDistrictInput,
                        onValueChange = { regDistrictInput = it },
                        label = { Text(if (isBD) (strings["district_label"] ?: "District") else (strings["city_state_label"] ?: "City / State")) },
                        placeholder = { Text(if (isBD) "e.g., Dhaka" else "e.g., New York") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = regUpazilaInput,
                        onValueChange = { regUpazilaInput = it },
                        label = { Text(if (isBD) (strings["upazila_label"] ?: "Upazila") else (if (language == AppLanguage.BAN) "অঞ্চল" else "Region")) },
                        placeholder = { Text(if (isBD) "e.g., Mirpur" else "e.g., Queens") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = regCountryInput,
                    onValueChange = {},
                    readOnly = true,
                    enabled = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedBorderColor = BloodRed,
                        unfocusedBorderColor = LightBorder,
                        focusedLeadingIconColor = BloodRed,
                        unfocusedLeadingIconColor = BloodRed,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    label = { Text(if (language == AppLanguage.BAN) "দেশ (Country)" else "Country (দেশ)") },
                    placeholder = { Text("e.g. Bangladesh") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "down") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_country_input"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = "Country") }
                )
                Box(modifier = Modifier.matchParentSize().clickable { expandedCountry = true })

                DropdownMenu(
                    expanded = expandedCountry,
                    onDismissRequest = { expandedCountry = false }
                ) {
                    val countryList by viewModel.customCountries.collectAsState()
                    countryList.forEach { (ctyName, ctyCode) ->
                        val flag = try {
                            val firstChar = Character.codePointAt(ctyCode.uppercase(), 0) - 0x41 + 0x1F1E6
                            val secondChar = Character.codePointAt(ctyCode.uppercase(), 1) - 0x41 + 0x1F1E6
                            String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
                        } catch (e: Exception) {
                            "🌐"
                        }
                        DropdownMenuItem(
                            text = { Text("$flag $ctyName", fontSize = 14.sp) },
                            onClick = {
                                regCountryInput = ctyName
                                if (ctyName != "Bangladesh") {
                                    regDistrictInput = ""
                                    regUpazilaInput = ""
                                } else {
                                    regDistrictInput = "Dhaka"
                                    regUpazilaInput = "Mirpur"
                                }
                                expandedCountry = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (regRoleInput == "Donor") {
                OutlinedTextField(
                    value = regLastDonationInput,
                    onValueChange = { regLastDonationInput = it },
                    label = { Text(strings["last_donation_label"] ?: "Last Donation Date") },
                    placeholder = { Text(strings["last_donation_placeholder"] ?: "e.g. 2026-03-10 or Never") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_last_donation_input"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Calendar") }
                )

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (regNameInput.isBlank() || regPhoneInput.isBlank()) {
                        Toast.makeText(context, "Name and Phone are mandatory!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.regName = regNameInput
                        viewModel.regPhone = regPhoneInput
                        viewModel.regEmail = regEmailInput
                        viewModel.regPassword = regPasswordInput
                        viewModel.regBloodGroup = regBloodInput
                        viewModel.regDistrict = regDistrictInput
                        viewModel.regUpazila = regUpazilaInput
                        viewModel.regLastDonation = if (regRoleInput == "Donor") regLastDonationInput else "N/A"
                        viewModel.regCountry = regCountryInput
                        viewModel.regRole = regRoleInput

                        val ok = viewModel.triggerSignup()
                        if (ok) {
                            showSignupSuccessPopup = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("register_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = strings["btn_register"] ?: "Register",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    viewModel.clearBackStackAndNavigateTo(AppScreen.HOME)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("reg_continue_guest_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, BloodRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BloodRed)
            ) {
                Icon(Icons.Filled.PersonOutline, contentDescription = "Guest")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.BAN) "অতিথি হিসেবে প্রবেশ করুন" else "Continue as Guest",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showLoginSuccessPopup) {
        val isBn = language == AppLanguage.BAN
        val isAmbulance = viewModel.currentUser.collectAsState().value?.role == "Ambulance"
        val targetScreen = if (isAmbulance) AppScreen.AMBULANCE_DASHBOARD else AppScreen.HOME
        AlertDialog(
            onDismissRequest = {
                showLoginSuccessPopup = false
                viewModel.clearBackStackAndNavigateTo(targetScreen)
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(54.dp)
                )
            },
            title = {
                Text(
                    text = if (isBn) "লগইন সফল হয়েছে!" else "Login Successful!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = if (isBn) {
                        if (isAmbulance) "স্বাগতম! আপনি সফলভাবে অ্যাম্বুলেন্স চালক/মালিক ড্যাশবোর্ডে প্রবেশ করেছেন।" else "স্বাগতম! আপনি সফলভাবে আপনার অ্যাকাউন্টে লগইন করেছেন। রক্তদান করুন ও জীবন বাঁচান।"
                    } else {
                        if (isAmbulance) "Welcome! You have successfully logged into your Ambulance Dashboard." else "Welcome back! You have successfully logged into your account. Keep donating blood to save lives."
                    },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginSuccessPopup = false
                        viewModel.clearBackStackAndNavigateTo(targetScreen)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBn) {
                            if (isAmbulance) "ড্যাশবোর্ডে যান" else "হোমে যান"
                        } else {
                            if (isAmbulance) "Go to Dashboard" else "Go to Home"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    if (showSignupSuccessPopup) {
        val isBn = language == AppLanguage.BAN
        val isAmbulance = viewModel.currentUser.collectAsState().value?.role == "Ambulance"
        val targetScreen = if (isAmbulance) AppScreen.AMBULANCE_DASHBOARD else AppScreen.HOME
        AlertDialog(
            onDismissRequest = {
                showSignupSuccessPopup = false
                viewModel.clearBackStackAndNavigateTo(targetScreen)
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(54.dp)
                )
            },
            title = {
                Text(
                    text = if (isBn) "নিবন্ধন সফল হয়েছে!" else "Registration Successful!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = if (isBn) {
                        if (isAmbulance) "অভিনন্দন! আপনার অ্যাম্বুলেন্স চালক/মালিক অ্যাকাউন্ট সফলভাবে তৈরি করা হয়েছে।" else "অভিনন্দন! আপনার অ্যাকাউন্টটি সফলভাবে তৈরি করা হয়েছে। আপনি এখন এই সেভিংস ক্লাবের একজন গর্বিত সদস্য।"
                    } else {
                        if (isAmbulance) "Congratulations! Your Ambulance driver/owner account has been created successfully." else "Congratulations! Your account has been created successfully. You are now a proud member of this life-saving club."
                    },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignupSuccessPopup = false
                        viewModel.clearBackStackAndNavigateTo(targetScreen)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBn) {
                            if (isAmbulance) "ড্যাশবোর্ডে যান" else "হোমে যান"
                        } else {
                            if (isAmbulance) "Go to Dashboard" else "Go to Home"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}


// --- 3. HOME SCREEN ---

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val appName by viewModel.appName.collectAsState()
    val homeNotice by viewModel.homeNotice.collectAsState()
    val popupNotice by viewModel.popupNotice.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val language by viewModel.language.collectAsState()
    val detectedCountry by viewModel.detectedCountry.collectAsState()
    val detectedCountryCode by viewModel.detectedCountryCode.collectAsState()
    val isDeviceInBangladesh by viewModel.isDeviceInBangladesh.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val displayHomeNotice = remember(homeNotice, language) {
        if (language == AppLanguage.ENG) {
            if (homeNotice == "স্বাগতম আলিফ ব্লাড ব্যাংকে! জরুরি প্রয়োজনে চ্যাট বা কল করুন।") {
                "Welcome to Alif Blood Bank! Chat or call in case of emergency."
            } else {
                homeNotice
            }
        } else {
            homeNotice
        }
    }

    val displayPopupNotice = remember(popupNotice, language) {
        if (language == AppLanguage.ENG) {
            if (popupNotice == "আমাদের অ্যাপটি নিয়মিত আপডেট করুন এবং রক্ত দানে উৎসাহিত হোন।") {
                "Please update our app regularly and be encouraged to donate blood."
            } else {
                popupNotice
            }
        } else {
            popupNotice
        }
    }

    var showGiftPopup by rememberSaveable(popupNotice) { mutableStateOf(popupNotice.isNotEmpty()) }

    val networkCountryBn = when (detectedCountry) {
        "Bangladesh" -> "বাংলাদেশ"
        "United States" -> "যুক্তরাষ্ট্র"
        "India" -> "ভারত"
        "Saudi Arabia" -> "সৌদি আরব"
        "United Arab Emirates" -> "সংযুক্ত আরব আমিরাত"
        "United Kingdom" -> "যুক্তরাজ্য"
        else -> detectedCountry
    }

    var showCountryChangeDialog by remember { mutableStateOf(false) }
    var showSwitchWarningDialog by remember { mutableStateOf(false) }
    var isNoticeDismissed by rememberSaveable(homeNotice) { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    if (showGiftPopup && popupNotice.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showGiftPopup = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(BloodRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CardGiftcard,
                        contentDescription = "Gift",
                        tint = BloodRed,
                        modifier = Modifier.size(40.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (language == AppLanguage.ENG) "Special Announcement!" else "বিশেষ ঘোষণা!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = BloodRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = displayPopupNotice,
                    fontSize = 16.sp,
                    color = DarkText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showGiftPopup = false },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Okay, Got it!" else "ঠিক আছে, বুঝেছি!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        )
    }

    val quickCountries by viewModel.customCountries.collectAsState()

    if (showCountryChangeDialog) {
        val isBn = language == AppLanguage.BAN
        AlertDialog(
            onDismissRequest = { showCountryChangeDialog = false },
            title = {
                Text(
                    text = if (isBn) "সার্ভার দেশ নির্বাচন করুন" else "Select Server Country",
                    fontWeight = FontWeight.Bold,
                    color = BloodRed,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // VPN sync / detection button at the top
                    Button(
                        onClick = {
                            scope.launch {
                                android.widget.Toast.makeText(context, if (isBn) "ভিপিএন / আইপি সংযোগ চেক করা হচ্ছে..." else "Checking VPN / IP connection...", android.widget.Toast.LENGTH_SHORT).show()
                                viewModel.detectUserLocation(context)
                                kotlinx.coroutines.delay(1500)
                                val current = viewModel.detectedCountry.value
                                android.widget.Toast.makeText(context, if (isBn) "আইপি রিফ্রেশ করা হয়েছে। বর্তমান সার্ভার: $current" else "IP refreshed. Active server: $current", android.widget.Toast.LENGTH_LONG).show()
                                showCountryChangeDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.Sync, contentDescription = "VPN Sync")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isBn) "ভিপিএন / আইপি রিফ্রেশ করুন" else "Refresh IP / VPN", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isBn) {
                            "আইপি অনুযায়ী আপনার বর্তমান সার্ভার: $detectedCountry"
                        } else {
                            "Your current server based on IP: $detectedCountry"
                        },
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    val staticCountries = listOf(
                        Pair("Bangladesh", "বাংলাদেশ"),
                        Pair("India", "ভারত"),
                        Pair("Saudi Arabia", "সৌদি আরব"),
                        Pair("United Arab Emirates", "সংযুক্ত আরব আমিরাত"),
                        Pair("United States", "যুক্তরাষ্ট্র"),
                        Pair("United Kingdom", "যুক্তরাজ্য")
                    )
                    
                    val allCountries = (staticCountries + quickCountries.map { Pair(it.first, it.second) }).distinctBy { it.first }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        allCountries.forEach { (name, nativeName) ->
                            val isCurrent = name.equals(detectedCountry, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) Color(0xFFFFEBEE) else Color.Transparent)
                                    .clickable {
                                        if (!isCurrent) {
                                            val countryCode = when (name) {
                                                "Bangladesh" -> "BD"
                                                "India" -> "IN"
                                                "Saudi Arabia" -> "SA"
                                                "United Arab Emirates" -> "AE"
                                                "United States" -> "US"
                                                "United Kingdom" -> "GB"
                                                else -> "GL"
                                            }
                                            viewModel.setDetectedCountry(name, countryCode)
                                            android.widget.Toast.makeText(context, if (isBn) "$name সার্ভারে পরিবর্তন করা হয়েছে।" else "Switched to $name server.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        showCountryChangeDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isCurrent,
                                    onClick = {
                                        if (!isCurrent) {
                                            val countryCode = when (name) {
                                                "Bangladesh" -> "BD"
                                                "India" -> "IN"
                                                "Saudi Arabia" -> "SA"
                                                "United Arab Emirates" -> "AE"
                                                "United States" -> "US"
                                                "United Kingdom" -> "GB"
                                                else -> "GL"
                                            }
                                            viewModel.setDetectedCountry(name, countryCode)
                                            android.widget.Toast.makeText(context, if (isBn) "$name সার্ভারে পরিবর্তন করা হয়েছে।" else "Switched to $name server.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        showCountryChangeDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = BloodRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) BloodRed else Color.DarkGray
                                    )
                                    Text(
                                        text = nativeName,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = if (isBn) "সংযুক্ত" else "Connected",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showCountryChangeDialog = false }
                ) {
                    Text(if (isBn) "বন্ধ করুন" else "Close", color = Color.Gray)
                }
            }
        )
    }

    if (showSwitchWarningDialog) {
        val isBn = language == AppLanguage.BAN
        AlertDialog(
            onDismissRequest = { showSwitchWarningDialog = false },
            title = {
                Text(
                    text = if (isBn) "সার্ভার সংযোগ নোটিশ" else "Server Connection Notice",
                    fontWeight = FontWeight.Bold,
                    color = BloodRed,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isBn) {
                        "আপনার ডিভাইসটি আইপি অনুযায়ী স্বয়ংক্রিয়ভাবে $detectedCountry সার্ভারে সংযুক্ত আছে। নিরাপত্তাজনিত কারণে অন্য সার্ভার নির্বাচন করা সম্ভব নয়।"
                    } else {
                        "Your device is automatically connected to the $detectedCountry server based on your IP. For security reasons, selecting other servers is not permitted."
                    },
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSwitchWarningDialog = false 
                        showCountryChangeDialog = false
                    },
                    modifier = Modifier.testTag("ok_server_notice_btn")
                ) {
                    Text(if (isBn) "ওকে" else "OK", color = BloodRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    var showDonorsListDialog by remember { mutableStateOf(false) }
    var visibleDonorsLimit by remember { mutableStateOf(9) }
    var visibleUrgentRequestsLimit by remember { mutableStateOf(9) }
    var selectedHomeTab by remember { mutableStateOf(1) } // 0 = Donors, 1 = Urgent Blood
    val donors by viewModel.donors.collectAsState()

    if (showDonorsListDialog) {
        val isBn = language == AppLanguage.BAN
        AlertDialog(
            onDismissRequest = { showDonorsListDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = "Donors",
                        tint = BloodRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isBn) "রক্তদাতাদের তালিকা ও রক্তদান সংখ্যা" else "Registered Donors & Donations",
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isBn) {
                            "$networkCountryBn-এ নিবন্ধিত সকল সক্রিয় রক্তাদাতা এবং তাদের মোট রক্তদানের বিবরণ নিচের তালিকায় দেওয়া হলো:"
                        } else {
                            "Active registered donors in $detectedCountry and their donation records:"
                        },
                        fontSize = 12.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (donors.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBn) "কোনো রক্তদাতা পাওয়া যায়নি।" else "No active donors registered.",
                                color = SecondaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 350.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(donors.size) { index ->
                                val donor = donors[index]
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showDonorsListDialog = false
                                            viewModel.selectDonorAndNavigate(donor.id)
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
                                    border = BorderStroke(1.dp, LightBorder),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Blood Badge
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(BloodRed, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = donor.bloodGroup,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = donor.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = DarkText
                                            )
                                            Text(
                                                text = "${donor.upazila}, ${donor.district}",
                                                fontSize = 11.sp,
                                                color = SecondaryText
                                            )
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            // Donation count indicator
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .background(LightPinkRed.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Favorite,
                                                    contentDescription = "Donated",
                                                    tint = BloodRed,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isBn) "রক্তদান: ${donor.donationCount} বার" else "Donated: ${donor.donationCount} times",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DarkBloodRed
                                                )
                                            }
                                        }

                                        // Call icon button
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier
                                                .background(LightPinkRed.copy(alpha = 0.5f), CircleShape)
                                                .size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Call,
                                                contentDescription = "Call",
                                                tint = BloodRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDonorsListDialog = false }) {
                    Text(if (isBn) "ঠিক আছে" else "OK", color = BloodRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    var showHospitalNetworksDialog by remember { mutableStateOf(false) }
    var selectedHospitalForDetails by remember { mutableStateOf<HospitalInfo?>(null) }

    val hospitals = remember(detectedCountry) {
        when (detectedCountry) {
            "Bangladesh" -> listOf(
                HospitalInfo("Dhaka Medical College Hospital (DMCH)", "ঢাকা মেডিকেল কলেজ হাসপাতাল (DMCH)", "Dhaka", "Tejgaon", "Bangladesh"),
                HospitalInfo("Sir Salimullah Medical College Hospital", "স্যার সলিমুল্লাহ মেডিকেল কলেজ হাসপাতাল", "Dhaka", "Dhanmondi", "Bangladesh"),
                HospitalInfo("Chattogram General Hospital (CGH)", "চট্টগ্রাম জেনারেল হাসপাতাল (CGH)", "Chattogram", "Double Mooring", "Bangladesh"),
                HospitalInfo("Sylhet MAG Osmani Medical College", "সিলেট এমএজি ওসমানী মেডিকেল কলেজ", "Sylhet", "Sylhet Sadar", "Bangladesh"),
                HospitalInfo("Rajshahi Medical College Hospital", "রাজশাহী মেডিকেল কলেজ হাসপাতাল", "Rajshahi", "Rajpara", "Bangladesh"),
                HospitalInfo("Mymensingh Medical College Hospital", "ময়মনসিংহ মেডিকেল কলেজ হাসপাতাল", "Mymensingh", "Sadar", "Bangladesh"),
                HospitalInfo("Khulna Medical College Hospital", "খুলনা মেডিকেল কলেজ হাসপাতাল", "Khulna", "Sadar", "Bangladesh"),
                HospitalInfo("Sher-e-Bangla Medical College Hospital", "শের-ই-বাংলা মেডিকেল কলেজ হাসপাতাল", "Barishal", "Sadar", "Bangladesh")
            )
            "United States" -> listOf(
                HospitalInfo("Mount Sinai Hospital", "মাউন্ট সিনাই হাসপাতাল", "New York", "Manhattan", "United States"),
                HospitalInfo("Stanford Health Care", "স্ট্যানফোর্ড হেলথ কেয়ার", "California", "San Francisco", "United States"),
                HospitalInfo("Houston Methodist Hospital", "হিউস্টন মেথোডিস্ট হাসপাতাল", "Texas", "Houston", "United States")
            )
            "India" -> listOf(
                HospitalInfo("AIIMS New Delhi", "এইমস নিউ দিল্লি", "Delhi", "Connaught Place", "India"),
                HospitalInfo("Fortis Hospital Mumbai", "ফোর্টিস হাসপাতাল মুম্বাই", "Maharashtra", "Mumbai Worli", "India")
            )
            "Saudi Arabia" -> listOf(
                HospitalInfo("King Faisal Specialist Hospital", "কিং ফয়সাল স্পেশালিস্ট হাসপাতাল", "Riyadh", "Al-Olaya", "Saudi Arabia")
            )
            "United Arab Emirates" -> listOf(
                HospitalInfo("Cleveland Clinic Abu Dhabi", "ক্লিভল্যান্ড ক্লিনিক আবুধাবি", "Abu Dhabi", "Al-Reem Island", "United Arab Emirates")
            )
            "United Kingdom" -> listOf(
                HospitalInfo("St Thomas' Hospital London", "সেন্ট থমাস হাসপাতাল লন্ডন", "London", "Westminster", "United Kingdom")
            )
            else -> listOf(
                HospitalInfo("$detectedCountry Central Health Center", "$detectedCountry সেন্ট্রাল হেলথ কেয়ার", "Sadar", "Sadar", detectedCountry)
            )
        }
    }

    if (showHospitalNetworksDialog) {
        val isBn = language == AppLanguage.BAN
        AlertDialog(
            onDismissRequest = { showHospitalNetworksDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CorporateFare,
                        contentDescription = "Hospitals",
                        tint = BloodRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isBn) "হসপিটাল ওয়াইজ রক্তদাতা তথ্য" else "Hospital-wise Donors",
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isBn) {
                            "নিচের হসপিটালগুলোর জেলাভিত্তিক নিবন্ধিত সক্রিয় রক্তদাতার সংখ্যা দেখুন। বিস্তারিত দেখতে যেকোনো হসপিটালে চাপুন:"
                        } else {
                            "See the number of active registered donors near each hospital. Tap any hospital or medical center to view its name and detailed info:"
                        },
                        fontSize = 12.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hospitals.size) { index ->
                            val hosp = hospitals[index]
                            val matchCount = donors.count { it.district.equals(hosp.district, ignoreCase = true) }
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedHospitalForDetails = hosp
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFD)),
                                border = BorderStroke(1.dp, LightBorder),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalHospital,
                                        contentDescription = "Hosp",
                                        tint = BloodRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBn) hosp.banglaName else hosp.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DarkText
                                        )
                                        Text(
                                            text = "${hosp.upazila}, ${hosp.district}",
                                            fontSize = 11.sp,
                                            color = SecondaryText
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Badge(
                                        containerColor = LightPinkRed,
                                        contentColor = BloodRed
                                    ) {
                                        Text(
                                            text = if (isBn) "$matchCount দাতা" else "$matchCount Donors",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHospitalNetworksDialog = false }) {
                    Text(if (isBn) "ঠিক আছে" else "Close", color = BloodRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (selectedHospitalForDetails != null) {
        val hosp = selectedHospitalForDetails!!
        val isBn = language == AppLanguage.BAN
        val matchDonors = donors.filter { it.district.equals(hosp.district, ignoreCase = true) }
        
        AlertDialog(
            onDismissRequest = { selectedHospitalForDetails = null },
            title = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalHospital,
                            contentDescription = "Hospital details",
                            tint = BloodRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isBn) "নির্বাচিত হাসপাতাল বিবরণ" else "Selected Hospital Info",
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = if (isBn) hosp.banglaName else hosp.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BloodRed,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = if (isBn) "ঠিকানা: ${hosp.upazila}, ${hosp.district}" else "Address: ${hosp.upazila}, ${hosp.district}",
                        fontSize = 12.sp,
                        color = SecondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isBn) {
                            "এই হাসপাতালের জেলা সংশ্লিষ্ট সকল নিবন্ধিত রক্তদাতার তালিকা নিচে দেয়া হলো:"
                        } else {
                            "List of all registered donors in this hospital's district:"
                        },
                        fontSize = 12.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (matchDonors.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBn) "দুঃখিত, এই এলাকায় কোনো রক্তদাতা পাওয়া যায়নি।" else "Sorry, no donors registered in this district.",
                                color = SecondaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(matchDonors.size) { index ->
                                val donor = matchDonors[index]
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedHospitalForDetails = null
                                            showHospitalNetworksDialog = false
                                            viewModel.selectDonorAndNavigate(donor.id)
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCFC)),
                                    border = BorderStroke(1.dp, LightBorder),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(BloodRed, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = donor.bloodGroup,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = donor.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = DarkText
                                            )
                                            Text(
                                                text = "${donor.upazila}, ${donor.district}",
                                                fontSize = 11.sp,
                                                color = SecondaryText
                                            )
                                            Text(
                                                text = if (isBn) "রক্তদান: ${donor.donationCount} বার" else "Donations: ${donor.donationCount} times",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BloodRed
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier
                                                .background(LightPinkRed.copy(alpha = 0.5f), CircleShape)
                                                .size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Call,
                                                contentDescription = "Call",
                                                tint = BloodRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedHospitalForDetails = null }) {
                    Text(if (isBn) "ফিরে যান" else "Back", color = BloodRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val currentUser by viewModel.currentUser.collectAsState()
    var showHomeScamReportDialog by remember { mutableStateOf(false) }
    var homeReporterName by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var homeReporterPhone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
    var homeScamAmount by remember { mutableStateOf("") }
    var homeScamReason by remember { mutableStateOf("") }
    var homeScammerDonorId by remember { mutableStateOf("") }
    var homeScammerDonorName by remember { mutableStateOf("") }
    var homeScammerDonorPhone by remember { mutableStateOf("") }
    var homeScammerPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val homePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        homeScammerPhotoUri = uri
    }

    val emergencyRequests = remember(requests) {
        requests.filter { it.isEmergency && it.status == "Active" }.take(2)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Dynamic Home Notice
        if (homeNotice.isNotEmpty() && !isNoticeDismissed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BloodRed.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, BloodRed.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = "Notice",
                        tint = BloodRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = displayHomeNotice,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BloodRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isNoticeDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = BloodRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Support Our App Card (Rewarded Ad)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BloodRed.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(BloodRed.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Support Us",
                            tint = BloodRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.BAN) "অ্যাপ সাপোর্ট করুন" else "Support Our App",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkText
                        )
                        Text(
                            text = if (language == AppLanguage.BAN) "একটি ছোট বিজ্ঞাপন দেখে রক্তদান সেবা সচল রাখতে আমাদের সাহায্য করুন।" else "Watch a short video ad to help us keep blood donation free.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        AdManager.showRewarded(context, onRewardEarned = {
                            showSupportDialog = true
                        })
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "Watch Video",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) "বিজ্ঞাপন দেখুন" else "Watch Ad Video",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showSupportDialog) {
            AlertDialog(
                onDismissRequest = { showSupportDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.BAN) "ধন্যবাদ!" else "Thank You!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E7D32)
                        )
                    }
                },
                text = {
                    Text(
                        text = if (language == AppLanguage.BAN) "আমাদের রক্তদান কার্যক্রমকে সাপোর্ট করার জন্য আপনাকে আন্তরিক ধন্যবাদ। আপনার কারণে আমরা বিনামূল্যে সেবাটি সচল রাখতে পারছি!" else "Thank you so much for supporting Alif Blood Bank! Your support helps us keep our blood donation services free for everyone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showSupportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = if (language == AppLanguage.BAN) "ওকে" else "OK", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Hero Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BloodRed)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BAN) "Assalamu Alaikum / Namaskar (New Version)" else "আসসালামু আলাইকুম / নমস্কার (নতুন সংস্করণ)",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                    
                    // Cute dynamic country/flag capsule
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable { showCountryChangeDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Country Flag",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$detectedCountryCode ▾",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = if (userSession != null) {
                        if (language == AppLanguage.BAN) "${userSession?.name} ❤️\n(স্বাগত জানাই আপনাকে!)" else "${userSession?.name} ❤️\n(Welcome, glad to have you!)"
                    } else {
                        if (language == AppLanguage.BAN) "Guest Hero ❤️\n(লাইফ সেভার্স ক্লাব)" else "Guest Hero ❤️\n(Life Savers Club)"
                    },
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = strings["splash_tagline"] ?: "Every blood donor is a hero",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showCountryChangeDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) "রিয়েলটাইম দেশ: $detectedCountry (ম্যানুয়ালি পরিবর্তন করতে চাপুন ▾)" else "Detected Country: $detectedCountry (Tap to change manually ▾)",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Status: Active Saver",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.SEARCH_DONOR) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BloodRed),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Search Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grid Statistics Cards
        Text(
            text = if (language == AppLanguage.BAN) "$networkCountryBn ব্লাড নেটওয়ার্ক আজ" else "$detectedCountry Blood Network Today",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = DarkText,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Live Dynamic Stats Row (Matching User Image Design)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(4.dp, Color.White), // White border for contrast
            colors = CardDefaults.cardColors(containerColor = BloodRed) // Red background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Users Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Transparent to show parent red
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White, // White icon
                            modifier = Modifier.size(32.dp)
                        )
                        val totalUsersTarget = (statistics["total_users"] ?: 80424).toFloat()
                        var startCount by remember { mutableStateOf(false) }
                        val animatedUsers by animateFloatAsState(
                            targetValue = if (startCount) totalUsersTarget else 1f,
                            animationSpec = tween(durationMillis = 2500, easing = LinearOutSlowInEasing),
                            label = "users_count"
                        )
                        LaunchedEffect(Unit) {
                            startCount = true
                        }
                        val usersText = remember(animatedUsers) {
                            val countVal = animatedUsers.toInt()
                            if (countVal >= 1000) "${countVal / 1000}k" else countVal.toString()
                        }
                        Text(
                            text = usersText,
                            color = Color.White, // White text
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = strings["stat_total_users"] ?: "Total Customer",
                            color = Color.White.copy(alpha = 0.9f), // White label
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Divider (Vertical Line)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                )

                // Total Donors Card (Total Donor)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.White, // White icon
                            modifier = Modifier.size(32.dp)
                        )
                        val totalDonorsTarget = (statistics["total_donors"] ?: 12300).toFloat()
                        var startDonorCount by remember { mutableStateOf(false) }
                        val animatedDonors by animateFloatAsState(
                            targetValue = if (startDonorCount) totalDonorsTarget else 1f,
                            animationSpec = tween(durationMillis = 2500, easing = LinearOutSlowInEasing),
                            label = "donors_count"
                        )
                        LaunchedEffect(Unit) {
                            startDonorCount = true
                        }
                        val donorsText = remember(animatedDonors) {
                            val countVal = animatedDonors.toInt()
                            if (countVal >= 1000) "${countVal / 1000}k" else countVal.toString()
                        }
                        Text(
                            text = donorsText,
                            color = Color.White, // White text
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = strings["stat_total_donors_large"] ?: "Total Donor",
                            color = Color.White.copy(alpha = 0.9f), // White label
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Custom CPA/Affiliate banner ad (Affmine, etc.)
        val customAdsEnabled by viewModel.customAdsEnabled.collectAsState()
        val customAdConfigs by viewModel.customAdConfigs.collectAsState()

        val activeAdsForCountry = remember(customAdConfigs, detectedCountry) {
            customAdConfigs.filter { ad ->
                val countries = ad.targetCountries.split(",").map { it.trim() }
                countries.any { it.equals("All", ignoreCase = true) || it.equals(detectedCountry, ignoreCase = true) }
            }
        }

        if (customAdsEnabled && activeAdsForCountry.isNotEmpty()) {
            // Select one ad based on weights (using a random selection weighted by ad.weight)
            val selectedAd = remember(activeAdsForCountry) {
                val totalWeight = activeAdsForCountry.sumOf { it.weight.coerceAtLeast(1) }
                if (totalWeight <= 0) {
                    activeAdsForCountry.first()
                } else {
                    var randomVal = (1..totalWeight).random()
                    var selected = activeAdsForCountry.first()
                    for (ad in activeAdsForCountry) {
                        randomVal -= ad.weight.coerceAtLeast(1)
                        if (randomVal <= 0) {
                            selected = ad
                            break
                        }
                    }
                    selected
                }
            }

            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        try {
                            uriHandler.openUri(selectedAd.targetUrl)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, BloodRed.copy(alpha = 0.4f))
            ) {
                Column {
                    // Header label indicating Sponsored Ad
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BloodRed.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = BloodRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedAd.networkName.uppercase() + " SPONSORED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BloodRed
                            )
                        }
                        Text(
                            text = if (language == AppLanguage.BAN) "বিজ্ঞাপন" else "AD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            modifier = Modifier
                                .background(LightBorder, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Image or Video Banner
                    if (selectedAd.isVideo && (selectedAd.videoUrl.isNotEmpty() || selectedAd.bannerUrl.isNotEmpty())) {
                        val videoPath = if (selectedAd.videoUrl.isNotEmpty()) selectedAd.videoUrl else selectedAd.bannerUrl
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    android.widget.VideoView(ctx).apply {
                                        try {
                                            val uri = android.net.Uri.parse(videoPath)
                                            setVideoURI(uri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                mp.setVolume(0f, 0f) // Mute by default
                                                mp.start()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        setOnErrorListener { _, _, _ ->
                                            true // Prevent error dialog popup
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        // Banner Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(Color.LightGray)
                        ) {
                            coil.compose.AsyncImage(
                                model = selectedAd.bannerUrl,
                                contentDescription = "CPA Ad Offer",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }

                    // Title and Description / Call-To-Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = selectedAd.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedAd.targetUrl,
                                fontSize = 10.sp,
                                color = SecondaryText,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                try {
                                    uriHandler.openUri(selectedAd.targetUrl)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BAN) "ভিজিট করুন" else "Visit Now",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = strings["stat_total_donors"] ?: "Total Donors",
                value = (statistics["total_donors"] ?: 8).toString(),
                icon = Icons.Filled.People,
                modifier = Modifier.weight(1f),
                onClick = { showDonorsListDialog = true }
            )
            Spacer(modifier = Modifier.width(10.dp))
            StatCard(
                title = strings["stat_active_requests"] ?: "Urgent Needs",
                value = (statistics["active_requests"] ?: 4).toString(),
                icon = Icons.Filled.LocalHospital,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.EMERGENCY_REQUESTS) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = strings["stat_lives_saved"] ?: "Lives Saved",
                value = (statistics["lives_saved"] ?: 23).toString(),
                icon = Icons.Filled.Favorite,
                modifier = Modifier.weight(1f),
                onClick = { showDonorsListDialog = true }
            )
            Spacer(modifier = Modifier.width(10.dp))
            StatCard(
                title = strings["stat_blood_banks"] ?: "Network Hubs",
                value = (statistics["hospitals"] ?: 14).toString(),
                icon = Icons.Filled.CorporateFare,
                modifier = Modifier.weight(1f),
                onClick = { showHospitalNetworksDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (userSession == null) {
            // Promotional Registration Card for Guests
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clickable { 
                        viewModel.setShowRegistrationTab(true)
                        viewModel.navigateTo(AppScreen.LOGIN_REGISTER) 
                    }
                    .testTag("promo_register_card"),
                colors = CardDefaults.cardColors(containerColor = BloodRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AppRegistration,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings["btn_register"] ?: "Become a Blood Donor",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (language == AppLanguage.BAN) "জীবন বাঁচাতে আজই নিবন্ধন করুন" else "Register today to start saving lives.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Ambulance Service Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clickable { 
                    viewModel.navigateTo(AppScreen.AMBULANCE_LIST) 
                }
                .testTag("home_ambulance_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF2196F3).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AirportShuttle,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings["card_ambulance"] ?: "Ambulance Service",
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (language == AppLanguage.BAN) "জরুরি প্রয়োজনে অ্যাম্বুলেন্স খুঁজুন" else "Find ambulance service for emergency.",
                        color = Color(0xFF1565C0).copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- DONOR CHECK BOX (ডোনার চেক) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .testTag("donor_check_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            var searchPhone by remember { mutableStateOf("") }
            var searchedDonor by remember { mutableStateOf<BloodDonor?>(null) }
            var searchedScammerReport by remember { mutableStateOf<ScamReport?>(null) }
            var searchedPendingReport by remember { mutableStateOf<ScamReport?>(null) }
            var searchPressed by remember { mutableStateOf(false) }
            val donorsList by viewModel.donors.collectAsState()
            val scamReportsList by viewModel.scamReports.collectAsState()
            val context = LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonSearch,
                        contentDescription = "Donor Check",
                        tint = BloodRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings["donor_check_title"] ?: "Donor Check",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )
                }

                Text(
                    text = strings["donor_check_desc"] ?: "Verify donation counts, eligibility, history, and active status by donor phone number.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchPhone,
                        onValueChange = { searchPhone = it },
                        placeholder = {
                            Text(
                                text = strings["donor_check_placeholder"] ?: "e.g., 01711223344",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BloodRed,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("donor_check_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            searchPressed = true
                            val cleanQuery = searchPhone.trim().replace("+88", "").replace(" ", "")
                            searchedDonor = if (cleanQuery.isNotEmpty()) {
                                donorsList.find { donor ->
                                    val cleanPhone = donor.phone.replace("+88", "").replace(" ", "").trim()
                                    cleanPhone == cleanQuery || cleanPhone.endsWith(cleanQuery) || cleanQuery.endsWith(cleanPhone)
                                }
                            } else {
                                null
                            }
                            searchedScammerReport = if (cleanQuery.isNotEmpty()) {
                                scamReportsList.find { report ->
                                    val cleanPhone = report.scammerDonorPhone.replace("+88", "").replace(" ", "").trim()
                                    (cleanPhone == cleanQuery || cleanPhone.endsWith(cleanQuery) || cleanQuery.endsWith(cleanPhone)) && report.status == "Banned"
                                }
                            } else {
                                null
                            }
                            searchedPendingReport = if (cleanQuery.isNotEmpty()) {
                                scamReportsList.find { report ->
                                    val cleanPhone = report.scammerDonorPhone.replace("+88", "").replace(" ", "").trim()
                                    (cleanPhone == cleanQuery || cleanPhone.endsWith(cleanQuery) || cleanQuery.endsWith(cleanPhone)) && report.status == "Pending"
                                }
                            } else {
                                null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(54.dp)
                            .testTag("donor_check_btn")
                    ) {
                        Text(
                            text = strings["donor_check_btn"] ?: "Check Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Results view
                if (searchPressed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (searchedScammerReport != null) {
                        val scammerReport = searchedScammerReport!!
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .border(3.dp, Color(0xFFD32F2F), RoundedCornerShape(16.dp))
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .testTag("confirmed_scammer_card"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Beautiful "STAY ALERT! SCAM" custom canvas-drawn header
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF375D6E), RoundedCornerShape(8.dp))
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // "STAY ALERT!" banner
                                    Box(
                                        modifier = Modifier
                                            .border(1.5.dp, Color.White, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "STAY ALERT!",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.2.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Custom Exclamation warning sign
                                    Box(
                                        modifier = Modifier.size(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val path = androidx.compose.ui.graphics.Path().apply {
                                                moveTo(size.width / 2, 6f)
                                                lineTo(6f, size.height - 6f)
                                                lineTo(size.width - 6f, size.height - 6f)
                                                close()
                                            }
                                            drawPath(
                                                path = path,
                                                color = Color(0xFFD32F2F),
                                                style = androidx.compose.ui.graphics.drawscope.Fill
                                            )
                                            drawPath(
                                                path = path,
                                                color = Color.White,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                            )
                                        }
                                        
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.offset(y = 8.dp)
                                        ) {
                                            Text(
                                                text = "!",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "SCAM",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Text(
                                        text = "এই নম্বরটি প্রতারক/স্ক্যামার হিসেবে চিহ্নিত!",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFFCDD2),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "CONFIRMED SCAMMER NOTICE",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Scammer Photo (if uploaded) or Warning Icon
                                    if (!scammerReport.scammerPhotoUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = scammerReport.scammerPhotoUri,
                                            contentDescription = "Scammer Photo",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(2.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .background(Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PersonOff,
                                                contentDescription = "Scammer Silhouette",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(44.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = scammerReport.scammerDonorName,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = Color(0xFFB71C1C)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Phone: ${scammerReport.scammerDonorPhone}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (language == AppLanguage.BAN) "অবস্থা: নিষিদ্ধ (Banned)" else "Status: Suspended / Banned",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFB71C1C)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color(0xFFFFCDD2), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Scammed amount and details
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Filled.Payments,
                                        contentDescription = "Money Involved",
                                        tint = Color(0xFFB71C1C),
                                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.BAN) "দাবিকৃত/প্রতারণার টাকা বা বিবরণ:" else "Amount Involved / Details:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = scammerReport.amountDemanded,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFB71C1C)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Filled.Description,
                                        contentDescription = "Scam details",
                                        tint = Color.DarkGray,
                                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.BAN) "প্রতারণার বিবরণ:" else "Incident Description:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = scammerReport.reason,
                                            fontSize = 13.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFFFCDD2), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Reporter info
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF9F9), RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BAN) "অভিযোগকারী তথ্য:" else "Reporter Information:",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Name: ${scammerReport.reporterName} (${scammerReport.reporterPhone})",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Warning instructions
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BAN)
                                            "সতর্কতা: এই মোবাইল নম্বরের ব্যক্তি রক্তদানের নামে টাকা চেয়ে প্রতারণা করেছে। একে কোনো প্রকার অগ্রিম টাকা পাঠাবেন না!"
                                        else "WARNING: This person scammed people by demanding money. DO NOT send any money or interact with them!",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        val currentDonor = searchedDonor
                        if (currentDonor != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .border(1.dp, LightPinkRed, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    if (searchedPendingReport != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                                            border = BorderStroke(1.dp, Color(0xFFFFEBAA))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Warning,
                                                    contentDescription = "Allegation",
                                                    tint = Color(0xFF856404),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (language == AppLanguage.BAN)
                                                        "⚠️ সতর্কতা: এই রক্তদাতার বিরুদ্ধে একটি তদন্তাধীন প্রতারণার অভিযোগ রয়েছে।"
                                                    else "⚠️ Warning: A pending scam/fraud report is filed against this donor.",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF856404)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(LightPinkRed, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = currentDonor.bloodGroup,
                                                color = BloodRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = currentDonor.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = DarkText
                                            )
                                            Text(
                                                text = "${currentDonor.upazila}, ${currentDonor.district}, ${currentDonor.country}",
                                                fontSize = 12.sp,
                                                color = SecondaryText
                                            )
                                        }

                                        // Availability Badging
                                        val isAvailable = currentDonor.isAvailable
                                        val statusText = if (isAvailable) (strings["eligible"] ?: "Eligible & Fit") else (strings["resting"] ?: "Currently Resting/Not Available")
                                        val statusColor = if (isAvailable) Color(0xFF2E7D32) else Color(0xFFE65100)
                                        
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    statusColor.copy(alpha = 0.12f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = statusText,
                                                color = statusColor,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            // Total donation stats & fitness review info
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Favorite,
                                                    contentDescription = "Donation Count",
                                                    tint = BloodRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = (if (language == AppLanguage.ENG) "Total Donations: " else "মোট রক্তদান: ") + "${currentDonor.donationCount}" + (if (language == AppLanguage.ENG) " times" else " বার"),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = DarkText
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Today,
                                                    contentDescription = "Last Donation",
                                                    tint = SecondaryText,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = (strings["profile_last_donation"] ?: "Last Donation") + ": ${currentDonor.lastDonationDate}",
                                                    fontSize = 12.sp,
                                                    color = SecondaryText
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentDonor.phone}"))
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier
                                                    .background(LightPinkRed.copy(alpha = 0.2f), CircleShape)
                                                    .size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Call,
                                                    contentDescription = "Call",
                                                    tint = BloodRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Card(
                                        modifier = Modifier.fillMaxWidth().testTag("home_scam_report_card"),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2)),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Warning,
                                                    contentDescription = "Warning",
                                                    tint = BloodRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = strings["report_scam_title"] ?: "Report Fraud/Scam",
                                                    fontWeight = FontWeight.Bold,
                                                    color = BloodRed,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = if (language == AppLanguage.BAN)
                                                    "এই রক্তদাতা যদি রক্তদানের পূর্বে/নামে টাকা দাবি করে বা প্রতারণা করে থাকে, তবে রিপোর্ট করুন।"
                                                else "If this donor demanded money or scammed you, please report them.",
                                                color = Color.DarkGray,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    homeScammerDonorId = currentDonor.id
                                                    homeScammerDonorName = currentDonor.name
                                                    homeScammerDonorPhone = currentDonor.phone
                                                    showHomeScamReportDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("btn_home_show_scam")
                                            ) {
                                                Text(
                                                    text = strings["report_scam_btn"] ?: "Report Scam",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (searchedPendingReport != null) {
                                val scammerReport = searchedPendingReport!!
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize()
                                        .border(2.dp, Color(0xFFF57C00), RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F2)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = "Warning",
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (language == AppLanguage.BAN) "⚠️ প্রতারণার অভিযোগ (তদন্তাধীন)" else "⚠️ Pending Fraud Allegation",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (language == AppLanguage.BAN)
                                                "এই নম্বরের রক্তদাতার বিরুদ্ধে একটি প্রতারণার অভিযোগ জমা পড়েছে। রক্তদানের আগে সতর্কতা অবলম্বন করুন।"
                                            else "A fraud report has been submitted against this number. Proceed with caution.",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Accused: ${scammerReport.scammerDonorName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DarkText
                                        )
                                        Text(
                                            text = "Details: ${scammerReport.reason}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            } else {
                                // Not found state
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BloodRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    colors = CardDefaults.cardColors(containerColor = LightPinkRed.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = "Not Found",
                                            tint = BloodRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (language == AppLanguage.ENG) "No donor registered under this phone number." else "এই মোবাইল নম্বরে কোনো রক্তদাতা নিবন্ধিত পাওয়া যায়নি।",
                                            fontSize = 13.sp,
                                            color = BloodRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.setShowRegistrationTab(true)
                                            viewModel.navigateTo(AppScreen.LOGIN_REGISTER)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.BAN) "রক্তদাতা হিসেবে নিবন্ধন করুন" else "Register as Donor",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DYNAMIC TABS: DONORS & URGENT BLOOD GRID (রক্তদাতা ও জরুরী রক্তের আবেদন গ্রিড) ---
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tab 1: Donors
            val tabDonorsSelected = selectedHomeTab == 0
            Button(
                onClick = { selectedHomeTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("tab_home_donors"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tabDonorsSelected) BloodRed else Color.White,
                    contentColor = if (tabDonorsSelected) Color.White else DarkText
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (tabDonorsSelected) BloodRed else LightBorder),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = "Donors",
                        modifier = Modifier.size(18.dp),
                        tint = if (tabDonorsSelected) Color.White else BloodRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) "রক্তদাতা (Donors)" else "Donors (রক্তদাতা)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Tab 2: Urgent Blood
            val tabUrgentSelected = selectedHomeTab == 1
            Button(
                onClick = { selectedHomeTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("tab_home_urgent"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tabUrgentSelected) BloodRed else Color.White,
                    contentColor = if (tabUrgentSelected) Color.White else DarkText
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (tabUrgentSelected) BloodRed else LightBorder),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Whatshot,
                        contentDescription = "Urgent Blood",
                        modifier = Modifier.size(18.dp),
                        tint = if (tabUrgentSelected) Color.White else BloodRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) "জরুরী রক্ত (Urgent)" else "Urgent Blood (জরুরী)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (selectedHomeTab == 0) {
            Text(
                text = if (language == AppLanguage.BAN) 
                    "সক্রিয় রক্তদাতাদের তালিকা নিচে দেওয়া হলো। আরো দেখতে 'আরো দেখুন' বাটনে চাপুন।" 
                else "List of active donors. To see more donors, tap the 'More' button below.",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val baseDonorsList = remember(donors, detectedCountry) {
                donors.filter { it.isAvailable && it.country.equals(detectedCountry, ignoreCase = true) }
            }
            val topDonorsList = remember(baseDonorsList, visibleDonorsLimit) {
                baseDonorsList.take(visibleDonorsLimit)
            }

            if (topDonorsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, LightBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == AppLanguage.BAN) "বর্তমানে কোনো ডোনার পাওয়া যায়নি।" else "No donors available right now.",
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Render lines, each containing 3 boxes
                val totalRows = (topDonorsList.size + 2) / 3
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            if (index < topDonorsList.size) {
                                val donor = topDonorsList[index]
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clickable {
                                            viewModel.selectDonorAndNavigate(donor.id)
                                        }
                                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, LightPinkRed.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(BloodRed, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = donor.bloodGroup,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Text(
                                            text = donor.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = DarkText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Text(
                                            text = donor.district,
                                            fontSize = 9.sp,
                                            color = SecondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .background(LightPinkRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Favorite,
                                                contentDescription = null,
                                                tint = BloodRed,
                                                modifier = Modifier.size(8.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${donor.donationCount}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BloodRed
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                // "More" button (আরো দেখুন) - clicking appends 3 more rows continuously
                if (baseDonorsList.size > visibleDonorsLimit) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { visibleDonorsLimit += 9 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BloodRed.copy(alpha = 0.2f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.BAN) "আরো দেখুন (More Donors)" else "More Donors (আরো দেখুন)",
                                color = BloodRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = BloodRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = if (language == AppLanguage.BAN) 
                    "যাদের রক্ত প্রয়োজন তাদের তালিকা নিচে দেওয়া হলো। আরো দেখতে 'আরো দেখুন' বাটনে চাপুন।" 
                else "List of active blood requests. To see more requests, tap the 'More' button below.",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val baseRequestsList = remember(requests, detectedCountry) {
                requests.filter { it.status == "Active" && it.country.equals(detectedCountry, ignoreCase = true) }
            }
            val topRequestsList = remember(baseRequestsList, visibleUrgentRequestsLimit) {
                baseRequestsList.take(visibleUrgentRequestsLimit)
            }

            if (topRequestsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, LightBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == AppLanguage.BAN) "বর্তমানে কোনো রক্ত আবেদন পাওয়া যায়নি।" else "No blood requests available right now.",
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Render lines, each containing 3 boxes
                val totalRows = (topRequestsList.size + 2) / 3
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            if (index < topRequestsList.size) {
                                val req = topRequestsList[index]
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clickable {
                                            viewModel.selectRequestAndNavigate(req.id)
                                        }
                                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, LightPinkRed.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(BloodRed, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = req.bloodGroup,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Text(
                                            text = req.patientName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = DarkText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Text(
                                            text = req.district,
                                            fontSize = 9.sp,
                                            color = SecondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        if (req.isEmergency) {
                                            Box(
                                                modifier = Modifier
                                                    .background(BloodRed, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (language == AppLanguage.BAN) "জরুরী" else "URGENT",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (language == AppLanguage.BAN) "সক্রিয়" else "ACTIVE",
                                                    color = DarkText,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                // "More" button (আরো দেখুন) - clicking appends 3 more rows continuously
                if (baseRequestsList.size > visibleUrgentRequestsLimit) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { visibleUrgentRequestsLimit += 9 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BloodRed.copy(alpha = 0.2f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.BAN) "আরো দেখুন (More Requests)" else "More Requests (আরো দেখুন)",
                                color = BloodRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = BloodRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- URGENT BLOOD REQUESTS SECTION (জরুরী রক্তের আবেদনসমূহ) ---
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.BAN) "জরুরী রক্তের আবেদনসমূহ" else "Urgent Blood Requests",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DarkText
            )
            Text(
                text = if (language == AppLanguage.BAN) "সব দেখুন" else "View All",
                color = BloodRed,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { /* Future: Navigate to full list */ }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        val urgentRequests = remember(requests, detectedCountry) {
            val base = requests.filter { it.status == "Active" && it.country.equals(detectedCountry, ignoreCase = true) }
            val fillers = listOf(
                BloodRequest("ur_1", "ফাতেমা বেগম", "O+", "1 Bag", "DMCH", "Dhaka", "Tejgaon", "01712345678", "জরুরী O+ রক্ত প্রয়োজন।", true, true, "2026-06-25", "Active", "Bangladesh"),
                BloodRequest("ur_2", "রহিম উদ্দিন", "A+", "2 Bags", "CGH", "Chattogram", "Double Mooring", "01812345679", "আইসিইউ-তে A+ রক্ত লাগবে।", true, true, "2026-06-25", "Active", "Bangladesh")
            ).filter { it.country.equals(detectedCountry, ignoreCase = true) }
            (base + fillers).distinctBy { it.contactNumber }.take(5)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            urgentRequests.forEach { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectRequestAndNavigate(req.id) }
                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, LightPinkRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(BloodRed.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = req.bloodGroup,
                                color = BloodRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = req.patientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DarkText
                            )
                            Text(
                                text = "${req.hospitalName}, ${req.district}",
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Standalone Scam/Fraud reporting box directly below the Donor Check section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp))
                .testTag("home_standalone_scam_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFFCDD2))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Report,
                        contentDescription = "Warning",
                        tint = BloodRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) "রক্তদাতার বিরুদ্ধে প্রতারণার অভিযোগ" else "Report Donor Fraud/Scam",
                        fontWeight = FontWeight.Bold,
                        color = BloodRed,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == AppLanguage.BAN)
                        "কোনো রক্তদাতা যদি রক্ত দেওয়ার নামে অগ্রিম টাকা চেয়ে থাকে বা টাকা নিয়ে প্রতারণা করে থাকে, তবে দ্রুত এখানে তার নম্বর দিয়ে অভিযোগ করুন।"
                    else "If any donor asked for advance money (transport/blood) or committed a scam, immediately file a report here with their phone number.",
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        homeScammerDonorId = ""
                        homeScammerDonorName = ""
                        homeScammerDonorPhone = ""
                        showHomeScamReportDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_homepage_scam_report")
                ) {
                    Text(
                        text = if (language == AppLanguage.BAN) "অভিযোগ দাখিল করুন (File Report)" else "File Fraud Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CORE SYSTEM NAVIGATION DIRECT LINKS
        Text(
            text = "Donor Services",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = DarkText,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceIconLink(
                title = strings["card_search_donor"] ?: "Find Donors",
                icon = Icons.Filled.PersonSearch,
                tag = "home_search_service",
                onClick = { viewModel.navigateTo(AppScreen.SEARCH_DONOR) },
                color = CoralRed,
                modifier = Modifier.weight(1f)
            )

            ServiceIconLink(
                title = strings["card_request_blood"] ?: "Request Blood",
                icon = Icons.Filled.Queue,
                tag = "home_request_service",
                onClick = { viewModel.navigateTo(AppScreen.REQUEST_BLOOD) },
                color = BloodRed,
                modifier = Modifier.weight(1f)
            )

            ServiceIconLink(
                title = strings["card_emergency_req"] ?: "Urgent Needs",
                icon = Icons.Filled.CrisisAlert,
                tag = "home_emergency_service",
                onClick = { viewModel.navigateTo(AppScreen.EMERGENCY_REQUESTS) },
                color = DarkBloodRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ACTIVE EMERGENCY PREVIEWS ---
        if (emergencyRequests.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚨 Live Urgent Requests",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BloodRed
                )
                TextButton(onClick = { viewModel.navigateTo(AppScreen.EMERGENCY_REQUESTS) }) {
                    Text("View All", color = BloodRed)
                }
            }

            emergencyRequests.forEach { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { viewModel.selectRequestAndNavigate(req.id) }
                        .border(1.dp, LightPinkRed, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(LightPinkRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = req.bloodGroup,
                                color = BloodRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = req.patientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DarkText
                            )
                            Text(
                                text = "${req.hospitalName}, ${req.upazila}",
                                fontSize = 12.sp,
                                color = SecondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.navigateTo(AppScreen.EMERGENCY_REQUESTS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Respond", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // BLOOD DONATION TIPS
        Text(
            text = strings["card_blood_tips"] ?: "Blood Donation Tips",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = DarkText,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                TipCard(
                    title = strings["tip1_title"] ?: "Stay Hydrated!",
                    desc = strings["tip1_desc"] ?: "Drink plenty of water before and after donation.",
                    icon = Icons.Filled.WaterDrop
                )
            }
            item {
                TipCard(
                    title = strings["tip2_title"] ?: "Rest is Crucial",
                    desc = strings["tip2_desc"] ?: "Avoid heavy lifting or high intensity training.",
                    icon = Icons.Filled.Bedtime
                )
            }
            item {
                TipCard(
                    title = strings["tip3_title"] ?: "Iron-Rich Foods",
                    desc = strings["tip3_desc"] ?: "Ensure foods like red meat & leafy spinach.",
                    icon = Icons.Filled.Restaurant
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- FOOTER SECTION FOR POLICIES ---
        HorizontalDivider(color = LightBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (language == AppLanguage.ENG) "$appName ❤️" else "${appName} ❤️",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BloodRed
            )
            Text(
                text = if (language == AppLanguage.ENG) "Saving Lives Voluntarily" else "স্বেচ্ছায় জীবন বাঁচান",
                fontSize = 11.sp,
                color = SecondaryText,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Privacy Policy Link
                TextButton(
                    onClick = { viewModel.navigateTo(AppScreen.PRIVACY_POLICY) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Privacy Policy" else "প্রাইভেসি পলিসি",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0284C7)
                    )
                }

                Text("|", color = Color.LightGray, fontSize = 12.sp)

                // Terms & Conditions Link
                TextButton(
                    onClick = { viewModel.navigateTo(AppScreen.TERMS_CONDITIONS) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Terms & Conditions" else "টার্মস এন্ড কন্ডিশন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0284C7)
                    )
                }

                Text("|", color = Color.LightGray, fontSize = 12.sp)

                // Refund Policy Link
                TextButton(
                    onClick = { viewModel.navigateTo(AppScreen.REFUND_POLICY) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Refund Policy" else "রিফান্ড পলিসি",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0284C7)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "© 2026 Alif Shen Ltd. All Rights Reserved.",
                fontSize = 10.sp,
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (showHomeScamReportDialog) {
            AlertDialog(
                onDismissRequest = { showHomeScamReportDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Report, contentDescription = "Report", tint = BloodRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings["report_scam_title"] ?: "Report Fraud/Scam",
                            fontWeight = FontWeight.Bold,
                            color = BloodRed
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BAN)
                                "স্বেচ্ছায় রক্তদানের নামে কোনো আর্থিক স্ক্যাম বা অসদুপায় প্রতিহত করতে আমরা প্রতিশ্রুতিবদ্ধ।"
                            else "We strive to prevent financial fraud in the name of volunteer blood donation.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        // Accused/Scammer Phone Number
                        Column {
                            Text(
                                text = if (language == AppLanguage.BAN) "অভিযুক্ত রক্তদাতার মোবাইল নম্বর *" else "Accused Donor's Phone *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = homeScammerDonorPhone,
                                onValueChange = { homeScammerDonorPhone = it },
                                placeholder = { Text("e.g. 018xxxxxxxx") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("accused_phone_input")
                            )
                        }

                        // Accused/Scammer Name
                        Column {
                            Text(
                                text = if (language == AppLanguage.BAN) "অভিযুক্ত রক্তদাতার নাম *" else "Accused Donor's Name *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = homeScammerDonorName,
                                onValueChange = { homeScammerDonorName = it },
                                placeholder = { Text("e.g. Korim Uddin") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("accused_name_input")
                            )
                        }

                        // Reporter name
                        Column {
                            Text(
                                text = strings["report_reporter_name_label"] ?: "Your Name",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = homeReporterName,
                                onValueChange = { homeReporterName = it },
                                placeholder = { Text("e.g. Sabbir Ahmed") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("home_reporter_name_input")
                            )
                        }

                        // Reporter Phone
                        Column {
                            Text(
                                text = strings["report_reporter_phone_label"] ?: "Your Contact Number",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = homeReporterPhone,
                                onValueChange = { homeReporterPhone = it },
                                placeholder = { Text("e.g. 017xxxxxxxx") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("home_reporter_phone_input")
                            )
                        }

                        // Amount involved
                        Column {
                            Text(
                                text = strings["report_amount_label"] ?: "Amount Involved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = homeScamAmount,
                                onValueChange = { homeScamAmount = it },
                                placeholder = { Text("e.g. Tk. 2000 or advance transport") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("home_scam_amount_input")
                            )
                        }

                        // Detailed reason
                        Column {
                            Text(
                                text = strings["report_reason_label"] ?: "Description",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = homeScamReason,
                                onValueChange = { homeScamReason = it },
                                placeholder = { Text("Describe what happened...") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth().testTag("home_scam_reason_input")
                            )
                        }

                        // Photo Upload Section
                        Column {
                            Text(
                                text = strings["report_photo_label"] ?: "Upload Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                    .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
                                    .clickable { homePhotoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (homeScammerPhotoUri != null) {
                                    AsyncImage(
                                        model = homeScammerPhotoUri,
                                        contentDescription = "Scammer Photo",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    // Remove overlay
                                    Box(
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).clickable { homeScammerPhotoUri = null },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", tint = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = strings["report_photo_hint"] ?: "Click to select photo",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (homeReporterName.isBlank() || homeReporterPhone.isBlank() || homeScammerDonorPhone.isBlank() || homeScammerDonorName.isBlank() || homeScamReason.isBlank()) {
                                android.widget.Toast.makeText(context, "Please fill in all required fields", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.triggerSubmitScamReport(
                                    reporterName = homeReporterName,
                                    reporterPhone = homeReporterPhone,
                                    scammerDonorId = homeScammerDonorId.ifBlank { "custom_${System.currentTimeMillis()}" },
                                    scammerDonorName = homeScammerDonorName,
                                    scammerDonorPhone = homeScammerDonorPhone,
                                    reason = homeScamReason,
                                    amountDemanded = homeScamAmount.ifBlank { "Tk. 0 (Demand)" },
                                    scammerPhotoUri = homeScammerPhotoUri?.toString()
                                )
                                showHomeScamReportDialog = false
                                homeScammerPhotoUri = null // Reset photo after submit
                                android.widget.Toast.makeText(
                                    context,
                                    strings["report_success"] ?: "Report submitted to Admin.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                    ) {
                        Text(text = strings["report_submit"] ?: "Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showHomeScamReportDialog = false }) {
                        Text(text = if (language == AppLanguage.BAN) "বাতিল" else "Cancel", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = BloodRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = title, fontSize = 11.sp, color = SecondaryText, maxLines = 1)
        }
    }
}

@Composable
fun ServiceIconLink(
    title: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag(tag)
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun TipCard(title: String, desc: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = LightPinkRed),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BloodRed,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkBloodRed)
                Text(text = desc, fontSize = 11.sp, color = DarkText, lineHeight = 13.sp)
            }
        }
    }
}


// --- 4. SEARCH DONOR SCREEN ---

@Composable
fun SearchDonorScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val donorsList by viewModel.filteredDonors.collectAsState()

    // Auto-sync remote data when Search Donor screen is opened
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.triggerRemoteSync()
    }

    val bloodGroupFilter by viewModel.searchBloodGroup.collectAsState()
    val districtFilter by viewModel.searchDistrict.collectAsState()
    val upazilaFilter by viewModel.searchUpazila.collectAsState()
    val hospitalFilter by viewModel.searchHospital.collectAsState()

    var exBloodGroup by remember { mutableStateOf(false) }
    var exDistrict by remember { mutableStateOf(false) }
    var exUpazila by remember { mutableStateOf(false) }
    var exHospital by remember { mutableStateOf(false) }

    val bloodGroups = listOf("All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val detectedCountry by viewModel.detectedCountry.collectAsState()
    val districts = remember(detectedCountry) {
        listOf("All") + when (detectedCountry) {
            "United States" -> listOf("New York", "California", "Texas")
            "India" -> listOf("Delhi", "Maharashtra", "Karnataka")
            "Saudi Arabia" -> listOf("Riyadh", "Makkah")
            "United Arab Emirates" -> listOf("Dubai", "Abu Dhabi")
            "United Kingdom" -> listOf("London", "Greater Manchester")
            else -> MockData.districts
        }
    }
    val matchingUpazilas = remember(detectedCountry, districtFilter) {
        if (districtFilter == "All") {
            listOf("All")
        } else {
            listOf("All") + when (detectedCountry) {
                "United States" -> when (districtFilter) {
                    "New York" -> listOf("Manhattan", "Queens", "Brooklyn")
                    "California" -> listOf("San Francisco", "Los Angeles", "San Jose")
                    "Texas" -> listOf("Houston", "Dallas", "Austin")
                    else -> listOf("Manhattan")
                }
                "India" -> when (districtFilter) {
                    "Delhi" -> listOf("Connaught Place")
                    "Maharashtra" -> listOf("Mumbai Worli")
                    "Karnataka" -> listOf("Bangalore Indiranagar")
                    else -> listOf("Connaught Place")
                }
                "Saudi Arabia" -> when (districtFilter) {
                    "Riyadh" -> listOf("Al-Olaya")
                    "Makkah" -> listOf("Jeddah Al-Hamra")
                    else -> listOf("Al-Olaya")
                }
                "United Arab Emirates" -> when (districtFilter) {
                    "Dubai" -> listOf("Dubai Marina")
                    "Abu Dhabi" -> listOf("Al-Reem Island")
                    else -> listOf("Dubai Marina")
                }
                "United Kingdom" -> when (districtFilter) {
                    "London" -> listOf("Westminster")
                    "Greater Manchester" -> listOf("Deansgate")
                    else -> listOf("Westminster")
                }
                else -> MockData.getUpazilasForDistrict(districtFilter)
            }
        }
    }
    val hospitalsList = remember(detectedCountry) {
        listOf("All") + when (detectedCountry) {
            "Bangladesh" -> listOf(
                "Dhaka Medical College Hospital (DMCH)",
                "Sir Salimullah Medical College Hospital",
                "Chattogram General Hospital (CGH)",
                "Sylhet MAG Osmani Medical College",
                "Rajshahi Medical College Hospital",
                "Mymensingh Medical College Hospital",
                "Khulna Medical College Hospital",
                "Sher-e-Bangla Medical College Hospital"
            )
            "United States" -> listOf(
                "Mount Sinai Hospital",
                "Stanford Health Care",
                "Houston Methodist Hospital"
            )
            "India" -> listOf(
                "AIIMS New Delhi",
                "Fortis Hospital Mumbai"
            )
            "Saudi Arabia" -> listOf(
                "King Faisal Specialist Hospital"
            )
            "United Arab Emirates" -> listOf(
                "Cleveland Clinic Abu Dhabi"
            )
            "United Kingdom" -> listOf(
                "St Thomas' Hospital London"
            )
            else -> listOf("$detectedCountry Central Health Center")
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = strings["search_title"] ?: "Find Blood Donor",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BloodRed)
        )
        Text(
            text = "Filtered Realtime Global Database",
            fontSize = 12.sp,
            color = SecondaryText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // FILTER FIELDS BAR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = strings["search_filter"] ?: "Filters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BloodRed,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Hospital filter dropdown
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Button(
                        onClick = { exHospital = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LightPinkRed, contentColor = DarkBloodRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.LocalHospital, contentDescription = "Hospital", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val dispHospital = if (hospitalFilter == "All") {
                            if (language == AppLanguage.BAN) "সকল হাসপাতাল" else "All Hospitals"
                        } else {
                            hospitalFilter
                        }
                        Text(if (language == AppLanguage.BAN) "হাসপাতাল ফিল্টার: $dispHospital" else "Filter Hospital: $dispHospital", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(expanded = exHospital, onDismissRequest = { exHospital = false }) {
                        hospitalsList.forEach { hosp ->
                            val textLabel = if (hosp == "All") {
                                if (language == AppLanguage.BAN) "সকল হাসপাতাল" else "All Hospitals"
                            } else {
                                hosp
                            }
                            DropdownMenuItem(
                                text = { Text(textLabel) },
                                onClick = {
                                    viewModel.updateFilters(bloodGroupFilter, districtFilter, upazilaFilter, hosp)
                                    exHospital = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Blood filter
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { exBloodGroup = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LightPinkRed, contentColor = DarkBloodRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_bg_trigger"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Group: $bloodGroupFilter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(expanded = exBloodGroup, onDismissRequest = { exBloodGroup = false }) {
                            bloodGroups.forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(bg) },
                                    onClick = {
                                        viewModel.updateFilters(bg, districtFilter, upazilaFilter, hospitalFilter)
                                        exBloodGroup = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // District filter
                    Box(modifier = Modifier.weight(1.2f)) {
                        Button(
                            onClick = { exDistrict = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LightPinkRed, contentColor = DarkBloodRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_district_trigger"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Dist: $districtFilter", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        DropdownMenu(expanded = exDistrict, onDismissRequest = { exDistrict = false }) {
                            districts.forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text(dist) },
                                    onClick = {
                                        viewModel.updateFilters(bloodGroupFilter, dist, "All", hospitalFilter)
                                        exDistrict = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Upazila filter
                    Box(modifier = Modifier.weight(1.2f)) {
                        Button(
                            onClick = { exUpazila = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LightPinkRed, contentColor = DarkBloodRed),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Upazila: $upazilaFilter", fontSize = 11.sp)
                        }

                        DropdownMenu(expanded = exUpazila, onDismissRequest = { exUpazila = false }) {
                            matchingUpazilas.forEach { upz ->
                                DropdownMenuItem(
                                    text = { Text(upz) },
                                    onClick = {
                                        viewModel.updateFilters(bloodGroupFilter, districtFilter, upz, hospitalFilter)
                                        exUpazila = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${donorsList.size} ${strings["donor_found"] ?: "Donors Ready"}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DarkText
            )

            // Quick clear filters button
            if (bloodGroupFilter != "All" || districtFilter != "All" || upazilaFilter != "All" || hospitalFilter != "All") {
                TextButton(
                    onClick = { viewModel.updateFilters("All", "All", "All") },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Clear Filters", color = BloodRed, fontSize = 12.sp)
                }
            }
        }

        // DONOR RESULTS LIST
        if (donorsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = "No match",
                        tint = SecondaryText,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No compatible donors found.",
                        color = SecondaryText,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Try clearing filters to find other groups nearby.",
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(donorsList) { donor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectDonorAndNavigate(donor.id) }
                            .shadow(1.dp, RoundedCornerShape(12.dp))
                            .testTag("donor_card_${donor.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Blood display badge circle
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(BloodRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = donor.bloodGroup,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = donor.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DarkText
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Status pill indicator
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF4CAF50), CircleShape)
                                    )
                                }

                                Text(
                                    text = "${donor.upazila}, ${donor.district}",
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )

                                Text(
                                    text = "Last donated: ${donor.lastDonationDate}",
                                    fontSize = 11.sp,
                                    color = SecondaryText,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )

                                Text(
                                    text = if (language == AppLanguage.BAN) "সর্বমোট রক্তদান: ${donor.donationCount} বার" else "Total Donations: ${donor.donationCount} Times",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BloodRed,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .background(LightPinkRed, CircleShape)
                                    .size(40.dp)
                                    .testTag("call_donor_${donor.phone}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Call,
                                    contentDescription = "Call Donor",
                                    tint = BloodRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- 5. DONOR PROFILE ---

@Composable
fun DonorProfileScreen(viewModel: MainViewModel) {
    val donor by viewModel.selectedDonor.collectAsState()
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val appName by viewModel.appName.collectAsState()
    val context = LocalContext.current

    val currentUser by viewModel.currentUser.collectAsState()
    var showScamReportDialog by remember { mutableStateOf(false) }
    var reporterName by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var reporterPhone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
    var scamAmount by remember { mutableStateOf("") }
    var scamReason by remember { mutableStateOf("") }
    var scammerPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        scammerPhotoUri = uri
    }

    if (donor == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No donor selected", color = SecondaryText)
        }
        return
    }

    val finalDonor = donor!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Warning banner if account is warned by admin
        if (finalDonor.isWarning) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(2.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.BAN) "⚠️ সতর্কবার্তা / WARNING" else "⚠️ ACCOUNT WARNING",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = finalDonor.warningReason,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == AppLanguage.BAN) 
                                "এই রক্তদাতা টাকা দাবি করলে বা প্রতারণা করলে অবিলম্বে প্রশাসনকে জানান।" 
                                else "If this donor demands money or exhibits suspicious behavior, report immediately.",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Large Badge Shield
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(LightPinkRed, CircleShape)
                .padding(10.dp)
                .background(BloodRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = finalDonor.bloodGroup,
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = finalDonor.name,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = DarkText
        )

        Text(
            text = "User ID: ${finalDonor.displayUserId}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BloodRed,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (language == AppLanguage.ENG) "Available to Donate" else "রক্তদানে প্রস্তুত",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Donor Info Matrix Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(
                    icon = Icons.Filled.LocationOn,
                    label = strings["profile_loc"] ?: "Location",
                    value = "${finalDonor.upazila}, ${finalDonor.district}, ${finalDonor.country}"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightBorder)

                ProfileInfoRow(
                    icon = Icons.Filled.CalendarMonth,
                    label = strings["profile_last_donation"] ?: "Last Donation",
                    value = finalDonor.lastDonationDate
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightBorder)

                ProfileInfoRow(
                    icon = Icons.Filled.ContactPhone,
                    label = strings["phone_label"] ?: "Contact Number",
                    value = finalDonor.phone
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightBorder)

                ProfileInfoRow(
                    icon = Icons.Filled.LocalHospital,
                    label = "Total Donations Count",
                    value = "${finalDonor.donationCount} Times"
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Contact action grid buttons
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${finalDonor.phone}"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("profile_call_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Call, contentDescription = "call")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings["profile_btn_call"] ?: "Call Donor",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.openChatRoom(finalDonor.phone, finalDonor.name)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("profile_inapp_chat_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1), contentColor = Color(0xFF00796B)),
            border = BorderStroke(1.dp, Color(0xFFB2DFDB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Forum, contentDescription = "chat")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings["profile_btn_chat"] ?: "In-App Chat",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Scam Warning and Reporting Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("scam_report_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFFCDD2))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warning",
                        tint = BloodRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings["report_scam_title"] ?: "Report Fraud/Scam",
                        fontWeight = FontWeight.Bold,
                        color = BloodRed,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings["report_scam_desc"] ?: "If this donor asked for money or scammed you, file a report.",
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { showScamReportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_show_scam_report")
                ) {
                    Text(
                        text = strings["report_scam_btn"] ?: "Report Scam",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (showScamReportDialog) {
            AlertDialog(
                onDismissRequest = { showScamReportDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Report, contentDescription = "Report", tint = BloodRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings["report_scam_title"] ?: "Report Fraud/Scam",
                            fontWeight = FontWeight.Bold,
                            color = BloodRed
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BAN)
                                "স্বেচ্ছায় রক্তদানের নামে কোনো আর্থিক স্ক্যাম প্রতিহত করতে আমরা প্রতিশ্রুতিবদ্ধ। অভিযুক্ত: ${finalDonor.name}"
                            else "We strive to prevent financial scam in the name of volunteer blood donation. Accused: ${finalDonor.name}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        // Reporter name
                        Column {
                            Text(
                                text = strings["report_reporter_name_label"] ?: "Your Name",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = reporterName,
                                onValueChange = { reporterName = it },
                                placeholder = { Text("e.g. Sabbir Ahmed") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reporter_name_input")
                            )
                        }

                        // Reporter Phone
                        Column {
                            Text(
                                text = strings["report_reporter_phone_label"] ?: "Your Contact Number",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = reporterPhone,
                                onValueChange = { reporterPhone = it },
                                placeholder = { Text("e.g. 017xxxxxxxx") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reporter_phone_input")
                            )
                        }

                        // Amount involved
                        Column {
                            Text(
                                text = strings["report_amount_label"] ?: "Amount Involved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = scamAmount,
                                onValueChange = { scamAmount = it },
                                placeholder = { Text("e.g. Tk. 2000 or advance transport") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("scam_amount_input")
                            )
                        }

                        // Detailed reason
                        Column {
                            Text(
                                text = strings["report_reason_label"] ?: "Description",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = scamReason,
                                onValueChange = { scamReason = it },
                                placeholder = { Text("Describe what happened...") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth().testTag("scam_reason_input")
                            )
                        }

                        // Photo Upload Section
                        Column {
                            Text(
                                text = strings["report_photo_label"] ?: "Upload Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                    .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (scammerPhotoUri != null) {
                                    AsyncImage(
                                        model = scammerPhotoUri,
                                        contentDescription = "Scammer Photo",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    // Remove overlay
                                    Box(
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).clickable { scammerPhotoUri = null },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", tint = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = strings["report_photo_hint"] ?: "Click to select photo",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (reporterName.isBlank() || reporterPhone.isBlank() || scamReason.isBlank()) {
                                android.widget.Toast.makeText(context, "Please fill in all required fields", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.triggerSubmitScamReport(
                                    reporterName = reporterName,
                                    reporterPhone = reporterPhone,
                                    scammerDonorId = finalDonor.id,
                                    scammerDonorName = finalDonor.name,
                                    scammerDonorPhone = finalDonor.phone,
                                    reason = scamReason,
                                    amountDemanded = scamAmount.ifBlank { "Tk. 0 (Demand)" },
                                    scammerPhotoUri = scammerPhotoUri?.toString()
                                )
                                showScamReportDialog = false
                                scammerPhotoUri = null // Reset photo after submit
                                android.widget.Toast.makeText(
                                    context,
                                    strings["report_success"] ?: "Report submitted to Admin.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                    ) {
                        Text(text = strings["report_submit"] ?: "Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showScamReportDialog = false }) {
                        Text(text = if (language == AppLanguage.BAN) "বাতিল" else "Cancel", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LightPinkRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = BloodRed, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = label, color = SecondaryText, fontSize = 11.sp)
            Text(text = value, color = DarkText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}


// --- 6. REQUEST BLOOD SCREEN ---

@Composable
fun RequestBloodScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val isUserInBangladesh by viewModel.isUserInBangladesh.collectAsState()
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current

    var patientName by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("Male") }
    var medicalCondition by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var bloodAmount by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var upazila by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var isEmergency by remember { mutableStateOf(true) }

    val detectedCountryFlow by viewModel.detectedCountry.collectAsState()
    var reqCountryInput by remember { mutableStateOf(detectedCountryFlow) }

    androidx.compose.runtime.LaunchedEffect(detectedCountryFlow) {
        if (reqCountryInput == "Bangladesh" || reqCountryInput == "" || reqCountryInput == "International" || reqCountryInput == "United States") {
            reqCountryInput = detectedCountryFlow
            district = ""
            upazila = ""
        }
    }

    var expandedBlood by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedUpazila by remember { mutableStateOf(false) }
    var expandedCountry by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val districts = remember(reqCountryInput) {
        when (reqCountryInput) {
            "United States" -> listOf("New York", "California", "Texas")
            "India" -> listOf("Delhi", "Maharashtra", "Karnataka")
            "Saudi Arabia" -> listOf("Riyadh", "Makkah")
            "United Arab Emirates" -> listOf("Dubai", "Abu Dhabi")
            "United Kingdom" -> listOf("London", "Greater Manchester")
            else -> MockData.districts
        }
    }
    val availableUpazilas = remember(reqCountryInput, district) {
        when (reqCountryInput) {
            "United States" -> when (district) {
                "New York" -> listOf("Manhattan", "Queens", "Brooklyn")
                "California" -> listOf("San Francisco", "Los Angeles", "San Jose")
                "Texas" -> listOf("Houston", "Dallas", "Austin")
                else -> listOf("Manhattan")
            }
            "India" -> when (district) {
                "Delhi" -> listOf("Connaught Place")
                "Maharashtra" -> listOf("Mumbai Worli")
                "Karnataka" -> listOf("Bangalore Indiranagar")
                else -> listOf("Connaught Place")
            }
            "Saudi Arabia" -> when (district) {
                "Riyadh" -> listOf("Al-Olaya")
                "Makkah" -> listOf("Jeddah Al-Hamra")
                else -> listOf("Al-Olaya")
            }
            "United Arab Emirates" -> when (district) {
                "Dubai" -> listOf("Dubai Marina")
                "Abu Dhabi" -> listOf("Al-Reem Island")
                else -> listOf("Dubai Marina")
            }
            "United Kingdom" -> when (district) {
                "London" -> listOf("Westminster")
                "Greater Manchester" -> listOf("Deansgate")
                else -> listOf("Westminster")
            }
            else -> MockData.getUpazilasForDistrict(district)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = strings["request_title"] ?: "Post Blood Request",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BloodRed)
        )
        Text(
            text = "Initiate alert notifications to nearby donors",
            fontSize = 12.sp,
            color = SecondaryText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = patientName,
            onValueChange = { patientName = it },
            label = { Text(strings["req_patient_name"] ?: "Patient Name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("req_patient_name_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gender Selector
        Text(
            text = strings["req_patient_gender"] ?: "Patient Gender",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Male", "Female").forEach { gender ->
                val isSelected = patientGender == gender
                val label = if (gender == "Male") (strings["gender_male"] ?: "Male") else (strings["gender_female"] ?: "Female")
                
                Button(
                    onClick = { patientGender = gender },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) BloodRed else Color.White,
                        contentColor = if (isSelected) Color.White else BloodRed
                    ),
                    border = BorderStroke(1.dp, BloodRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = medicalCondition,
            onValueChange = { medicalCondition = it },
            label = { Text(strings["req_medical_condition"] ?: "Reason / Medical Condition") },
            placeholder = { Text("e.g. Surgery, Accident, Anemia") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("req_medical_condition_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Blood selector
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = bloodGroup,
                onValueChange = {},
                readOnly = true,
                label = { Text(strings["bg_label"] ?: "Blood Group Needed") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "down") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            // Transparent overlay to catch clicks for the entire field area
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expandedBlood = true }
            )
            DropdownMenu(
                expanded = expandedBlood,
                onDismissRequest = { expandedBlood = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                bloodGroups.forEach { bg ->
                    DropdownMenuItem(
                        text = { Text(bg, fontWeight = FontWeight.Medium) },
                        onClick = {
                            bloodGroup = bg
                            expandedBlood = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = bloodAmount,
            onValueChange = { bloodAmount = it },
            label = { Text(strings["req_blood_amount"] ?: "Amount of Blood Needed (e.g. 2 Bags)") },
            placeholder = { Text("e.g. 1 Bag, 2 Units") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("req_blood_amount_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = hospitalName,
            onValueChange = { hospitalName = it },
            label = { Text(strings["req_hospital"] ?: "Hospital Name & Details") },
            placeholder = { Text("e.g. Dhaka Medical College, Cabin 104") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("req_hospital_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Country Input (Dropdown selection, non-editable)
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = reqCountryInput,
                onValueChange = {},
                readOnly = true,
                label = { Text(if (language == AppLanguage.BAN) "দেশ (Country)" else "Country (দেশ)") },
                placeholder = { Text("e.g. Bangladesh") },
                trailingIcon = {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Country")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("req_country_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = "Country") }
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expandedCountry = true }
            )
            DropdownMenu(
                expanded = expandedCountry,
                onDismissRequest = { expandedCountry = false }
            ) {
                val countryList by viewModel.customCountries.collectAsState()
                countryList.forEach { (ctyName, ctyCode) ->
                    val flag = try {
                        val firstChar = Character.codePointAt(ctyCode.uppercase(), 0) - 0x41 + 0x1F1E6
                        val secondChar = Character.codePointAt(ctyCode.uppercase(), 1) - 0x41 + 0x1F1E6
                        String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
                    } catch (e: Exception) {
                        "🌐"
                    }
                    DropdownMenuItem(
                        text = { Text("$flag $ctyName", fontSize = 14.sp) },
                        onClick = {
                            reqCountryInput = ctyName
                            district = ""
                            upazila = ""
                            expandedCountry = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // District and Upazila Inputs (Both fully editable with autocomplete dropdowns)
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text(if (language == AppLanguage.BAN) "জেলা (District)" else "District (জেলা)") },
                    placeholder = { Text("e.g. Dhaka") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = expandedDistrict,
                    onDismissRequest = { expandedDistrict = false }
                ) {
                    districts.forEach { dist ->
                        DropdownMenuItem(
                            text = { Text(dist) },
                            onClick = {
                                district = dist
                                val subUpz = MockData.getUpazilasForDistrict(dist)
                                upazila = subUpz.firstOrNull() ?: ""
                                expandedDistrict = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = upazila,
                    onValueChange = { upazila = it },
                    label = { Text(if (language == AppLanguage.BAN) "উপজেলা (Upazila)" else "Upazila (উপজেলা)") },
                    placeholder = { Text("e.g. Mirpur") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = expandedUpazila,
                    onDismissRequest = { expandedUpazila = false }
                ) {
                    val currentUpazilas = MockData.getUpazilasForDistrict(district)
                    if (currentUpazilas.isNotEmpty()) {
                        currentUpazilas.forEach { upz ->
                            DropdownMenuItem(
                                text = { Text(upz) },
                                onClick = {
                                    upazila = upz
                                    expandedUpazila = false
                                }
                            )
                        }
                    } else {
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.BAN) "কোনো সাজেশন নেই" else "No suggestions") },
                            onClick = { expandedUpazila = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contactNumber,
            onValueChange = { contactNumber = it },
            label = { Text(strings["req_phone"] ?: "Contact Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("req_phone_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text("Additional Instructions") },
            placeholder = { Text("How many bags? When do you need it?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .testTag("req_details_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Switch for Emergency
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(LightPinkRed, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings["req_is_emergency"] ?: "Urgent Emergency Request?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkBloodRed
                )
                Text(
                    text = "Broadcast immediately as live 🚨 critical warning badge",
                    fontSize = 11.sp,
                    color = DarkText
                )
            }
            Switch(
                checked = isEmergency,
                onCheckedChange = { isEmergency = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BloodRed
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (patientName.isBlank() || contactNumber.isBlank() || hospitalName.isBlank() || medicalCondition.isBlank()) {
                    Toast.makeText(context, if (language == AppLanguage.BAN) "অনুগ্রহ করে সব তথ্য পূরণ করুন!" else "Please fill all mandatory fields!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.reqPatientName = patientName
                    viewModel.reqPatientGender = patientGender
                    viewModel.reqMedicalCondition = medicalCondition
                    viewModel.reqBloodGroup = bloodGroup
                    viewModel.reqBloodAmount = bloodAmount
                    viewModel.reqHospitalName = hospitalName
                    viewModel.reqDistrict = district
                    viewModel.reqUpazila = upazila
                    viewModel.reqContactNumber = contactNumber
                    viewModel.reqDetails = details
                    viewModel.reqIsEmergency = isEmergency
                    viewModel.reqCountry = reqCountryInput

                    AdManager.showInterstitial(context, forceShow = true) {
                        val success = viewModel.triggerSubmitRequest(context)
                        if (success) {
                            Toast.makeText(context, strings["msg_request_posted"], Toast.LENGTH_LONG).show()
                            viewModel.navigateTo(AppScreen.EMERGENCY_REQUESTS)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_request_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = strings["req_submit"] ?: "Submit Blood Request",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// --- 7. EMERGENCY REQUESTS SCREEN ---

@Composable
fun EmergencyRequestsScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val appName by viewModel.appName.collectAsState()
    val requestsList by viewModel.requests.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val context = LocalContext.current

    // Auto-sync remote data when Emergency Requests screen is opened
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.triggerRemoteSync()
    }

    val activeUrgentRequests = remember(requestsList) {
        requestsList.filter { it.status == "Active" }
    }

    var visibleRequestsLimit by remember { mutableStateOf(9) }

    val topRequestsList = remember(activeUrgentRequests, visibleRequestsLimit) {
        activeUrgentRequests.take(visibleRequestsLimit)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings["emergency_requests_title"] ?: "Emergency Blood Requests",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BloodRed)
                    )
                    Text(
                        text = "${activeUrgentRequests.size} ${strings["active_req_count"] ?: "live requests need support"}",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }
                IconButton(
                    onClick = { viewModel.triggerRemoteSync() },
                    modifier = Modifier.size(40.dp).testTag("emergency_requests_refresh_btn")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = BloodRed,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Requests",
                            tint = BloodRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (activeUrgentRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, "success", tint = Color(0xFF4CAF50), modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("All requests fulfilled! Alhamdulillah.", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Render 3 items per Row (Grid-like)
                    val totalRows = (topRequestsList.size + 2) / 3
                    for (row in 0 until totalRows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                if (index < topRequestsList.size) {
                                    val req = topRequestsList[index]
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(145.dp)
                                            .clickable {
                                                viewModel.selectRequestAndNavigate(req.id)
                                            }
                                            .shadow(2.dp, RoundedCornerShape(12.dp))
                                            .testTag("emergency_request_card_${req.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, LightPinkRed.copy(alpha = 0.6f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(BloodRed, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = req.bloodGroup,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            Text(
                                                text = req.patientName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = DarkText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                            
                                            Text(
                                                text = req.hospitalName,
                                                fontSize = 9.sp,
                                                color = SecondaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = req.district,
                                                fontSize = 9.sp,
                                                color = SecondaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )

                                            if (req.bloodAmount.isNotBlank()) {
                                                Text(
                                                    text = req.bloodAmount,
                                                    fontSize = 9.sp,
                                                    color = BloodRed,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            if (req.isEmergency) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(BloodRed, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (language == AppLanguage.BAN) "জরুরী" else "URGENT",
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // See More button (সিমুর / See More) - clicking appends 3 more rows continuously
                    if (activeUrgentRequests.size > visibleRequestsLimit) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { visibleRequestsLimit += 9 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BloodRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BloodRed.copy(alpha = 0.2f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.BAN) "আরো দেখুন (See More)" else "See More (আরো দেখুন)",
                                    color = BloodRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = BloodRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to post a new request
        FloatingActionButton(
            onClick = {
                AdManager.showRewarded(context) {
                    viewModel.navigateTo(AppScreen.REQUEST_BLOOD)
                }
            },
            containerColor = BloodRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_post_request"),
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Request")
        }
    }
}


// --- 8. NOTIFICATIONS SCREEN ---

@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val notificationsList by viewModel.notifications.collectAsState()
    val language by viewModel.language.collectAsState()

    // Flag notification count as read
    LaunchedEffect(Unit) {
        viewModel.markNotificationsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = strings["notification_title"] ?: "Notifications & Alerts",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BloodRed)
                )
                Text(
                    text = if (language == AppLanguage.ENG) "Nearby Blood Donor Alerts & Request status" else "নিকটবর্তী রক্তদাতার এলার্ট এবং অনুরোধের অবস্থা",
                    fontSize = 12.sp,
                    color = SecondaryText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        if (notificationsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (language == AppLanguage.ENG) "No notifications yet" else "এখনো কোনো নোটিফিকেশন নেই", color = SecondaryText)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notificationsList) { notify ->
                    val finalTitle = if (language == AppLanguage.ENG) notify.titleEn else notify.titleBn
                    val finalMessage = if (language == AppLanguage.ENG) notify.messageEn else notify.messageBn

                    val containerColor = when (notify.type) {
                        "ALERT" -> LightPinkRed
                        "REQUEST" -> Color(0xFFFFF3E0)
                        else -> Color(0xFFE8F5E9)
                    }

                    val iconTint = when (notify.type) {
                        "ALERT" -> BloodRed
                        "REQUEST" -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }

                    val icon = when (notify.type) {
                        "ALERT" -> Icons.Filled.CrisisAlert
                        "REQUEST" -> Icons.Filled.LiveHelp
                        else -> Icons.Filled.CheckCircle
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = notify.type,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = finalTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = finalMessage,
                                    fontSize = 12.sp,
                                    color = DarkText.copy(alpha = 0.85f),
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notify.timestamp,
                                    fontSize = 10.sp,
                                    color = SecondaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- 9. USER PROFILE SCREEN ---

@Composable
fun UserProfileScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()
    val isUserInBangladesh by viewModel.isUserInBangladesh.collectAsState()
    val context = LocalContext.current

    if (userSession == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Please sign in to view and save your donor profile")
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { 
                    viewModel.setShowRegistrationTab(false)
                    viewModel.navigateTo(AppScreen.LOGIN_REGISTER) 
                }) {
                    Text("Go to Sign In")
                }
            }
        }
        return
    }

    val finalUser = userSession!!

    // Local form states synced with ViewModel
    var editName by remember { mutableStateOf(viewModel.profileEditName) }
    var editPhone by remember { mutableStateOf(viewModel.profileEditPhone) }
    var editEmail by remember { mutableStateOf(viewModel.profileEditEmail) }
    var editBlood by remember { mutableStateOf(viewModel.profileEditBlood) }
    var editDistrict by remember { mutableStateOf(viewModel.profileEditDistrict) }
    var editUpazila by remember { mutableStateOf(viewModel.profileEditUpazila) }
    var editLastDonation by remember { mutableStateOf(viewModel.profileEditLastDonation) }
    var editAvailable by remember { mutableStateOf(viewModel.profileEditAvailable) }
    var editCountry by remember { mutableStateOf(viewModel.profileEditCountry) }

    var expandedBlood by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedUpazila by remember { mutableStateOf(false) }
    var expandedCountry by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val districts = remember(editCountry) {
        when (editCountry) {
            "United States" -> listOf("New York", "California", "Texas")
            "India" -> listOf("Delhi", "Maharashtra", "Karnataka")
            "Saudi Arabia" -> listOf("Riyadh", "Makkah")
            "United Arab Emirates" -> listOf("Dubai", "Abu Dhabi")
            "United Kingdom" -> listOf("London", "Greater Manchester")
            else -> MockData.districts
        }
    }
    val availableUpazilas = remember(editCountry, editDistrict) {
        when (editCountry) {
            "United States" -> when (editDistrict) {
                "New York" -> listOf("Manhattan", "Queens", "Brooklyn")
                "California" -> listOf("San Francisco", "Los Angeles", "San Jose")
                "Texas" -> listOf("Houston", "Dallas", "Austin")
                else -> listOf("Manhattan")
            }
            "India" -> when (editDistrict) {
                "Delhi" -> listOf("Connaught Place")
                "Maharashtra" -> listOf("Mumbai Worli")
                "Karnataka" -> listOf("Bangalore Indiranagar")
                else -> listOf("Connaught Place")
            }
            "Saudi Arabia" -> when (editDistrict) {
                "Riyadh" -> listOf("Al-Olaya")
                "Makkah" -> listOf("Jeddah Al-Hamra")
                else -> listOf("Al-Olaya")
            }
            "United Arab Emirates" -> when (editDistrict) {
                "Dubai" -> listOf("Dubai Marina")
                "Abu Dhabi" -> listOf("Al-Reem Island")
                else -> listOf("Dubai Marina")
            }
            "United Kingdom" -> when (editDistrict) {
                "London" -> listOf("Westminster")
                "Greater Manchester" -> listOf("Deansgate")
                else -> listOf("Westminster")
            }
            else -> MockData.getUpazilasForDistrict(editDistrict)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Warning banner if account is warned by admin
        if (finalUser.isWarning) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(2.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.BAN) "⚠️ সতর্কবার্তা / WARNING" else "⚠️ ACCOUNT WARNING",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = finalUser.warningReason,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == AppLanguage.BAN) 
                                "এডমিন কর্তৃক আপনার একাউন্টটিতে সতর্কবার্তা দেওয়া হয়েছে। বিস্তারিত জানতে সাপোর্টে চ্যাটে যোগাযোগ করুন।" 
                                else "Your account has been issued a warning by the admin. Please contact support via support chat.",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // PROFILE HERO STATUS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BloodRed)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = finalUser.bloodGroup,
                        color = BloodRed,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = finalUser.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "User ID: ${finalUser.displayUserId}", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (finalUser.role == "Requester") {
                        Text(text = if (language == AppLanguage.BAN) "ভূমিকা: রক্ত গ্রহীতা (Seeker)" else "Role: Blood Seeker", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    } else {
                        Text(text = if (language == AppLanguage.BAN) "পদবী: গোল্ডেন ডোনার" else "Rank: Golden Blood Donor", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text(text = if (language == AppLanguage.BAN) "সর্বমোট রক্তদান: ${finalUser.donationCount} বার" else "Total Donations: ${finalUser.donationCount} Times", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ACCORDION QUICK ACTIONS
        if (finalUser.role == "Donor") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightPinkRed),
                border = BorderStroke(1.dp, CoralRed)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Did you donate blood recently? 🩸",
                        fontWeight = FontWeight.Bold,
                        color = DarkBloodRed,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Increment your live counter to notify hospitals of availability dates.",
                        fontSize = 12.sp,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.recordNewDonation()
                            editLastDonation = "2026-06-12"
                            Toast.makeText(context, "Donation record saved! Thank you, Hero!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("increment_donation_btn")
                    ) {
                        Icon(Icons.Filled.Add, "add")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record a Donation Today", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                border = BorderStroke(1.dp, Color(0xFF8BC34A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == AppLanguage.BAN) "আপনার কি জরুরি রক্ত প্রয়োজন? 🩸" else "Do you need blood urgently? 🩸",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF33691E),
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (language == AppLanguage.BAN) "একটি নতুন রক্তের রিকোয়েস্ট পোস্ট করুন এবং সরাসরি ডোনারদের সাথে যোগাযোগ করুন।" else "Post a new blood request and connect with active donors immediately.",
                        fontSize = 12.sp,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.navigateTo(AppScreen.REQUEST_BLOOD)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558B2F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("request_blood_quick_btn")
                    ) {
                        Icon(Icons.Filled.Add, "add")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BAN) "রক্তের আবেদন করুন" else "Request Blood Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = BorderStroke(1.dp, Color(0xFFFFB74D))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AirportShuttle,
                        contentDescription = "Ambulance",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) "অ্যাম্বুলেন্স সার্ভিস প্যানেল" else "Ambulance Service Panel",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.BAN) 
                        "জরুরি প্রয়োজনে সরাসরি অ্যাপ থেকে অ্যাম্বুলেন্স বুকিং করুন, বুকিংয়ের রিয়েল-টাইম ইতিহাস ও পেমেন্ট স্ট্যাটাস ট্র্যাক করুন অথবা আপনার নিজের অ্যাম্বুলেন্স সার্ভিসটি যুক্ত করুন।" 
                        else "Book an urgent ambulance directly from the app, track real-time booking history and payment status, or list/register your own ambulance service.",
                    fontSize = 12.sp,
                    color = DarkText,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button 1: Book Now
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.BOOK_AMBULANCE) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.BAN) "বুক করুন" else "Book Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Button 2: Booking History
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.AMBULANCE_BOOKINGS) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFF57C00)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.BAN) "বুকিং ইতিহাস" else "History",
                            color = Color(0xFFE65100),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Button 3: Add Ambulance Service
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.ADD_AMBULANCE) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) "নতুন অ্যাম্বুলেন্স যুক্ত করুন" else "Add Ambulance Service",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val isAdminUser = finalUser.email.equals("Alifsheenshopping@gmail.com", ignoreCase = true) || finalUser.email.equals("help.alifshen.ltd@gmail.com", ignoreCase = true) || finalUser.email.contains("admin") || finalUser.name.contains("Alif")
        val isAdminMode by viewModel.isAdminMode.collectAsState()

        if (isAdminUser) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.BAN) "অ্যাডমিন মোড" else "Admin Mode",
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = if (language == AppLanguage.BAN) "অ্যাডমিন প্যানেল সক্ষম করুন" else "Enable Admin Panel",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isAdminMode,
                        onCheckedChange = { viewModel.setAdminMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BloodRed, checkedTrackColor = LightPinkRed)
                    )
                }
                
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBloodRed)
                ) {
                    Icon(Icons.Filled.AdminPanelSettings, "admin")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == AppLanguage.BAN) "অ্যাডমিন ড্যাশবোর্ডে যান" else "Go to Admin Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // EDIT PROFILE COMPOSABLE FORMS
        Text(
            text = strings["edit_profile"] ?: "Edit Profile Details",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = BloodRed,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_name_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = editPhone,
            onValueChange = { editPhone = it },
            label = { Text("Phone Number") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_phone_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = editEmail,
            onValueChange = { editEmail = it },
            label = { Text("Email Address") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_email_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Dropdown for Edit Blood Group
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = editBlood,
                onValueChange = {},
                readOnly = true,
                label = { Text("Blood Group") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "down") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedBlood = true },
                shape = RoundedCornerShape(12.dp)
            )
            DropdownMenu(expanded = expandedBlood, onDismissRequest = { expandedBlood = false }) {
                bloodGroups.forEach { bg ->
                    DropdownMenuItem(
                        text = { Text(bg) },
                        onClick = {
                            editBlood = bg
                            expandedBlood = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = editCountry,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = DarkText,
                    disabledBorderColor = LightBorder,
                    disabledLabelColor = SecondaryText,
                    disabledLeadingIconColor = BloodRed,
                    disabledTrailingIconColor = SecondaryText,
                    disabledContainerColor = Color.White
                ),
                label = { Text(if (language == AppLanguage.BAN) "দেশ (Country)" else "Country (দেশ)") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "down") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_country_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = "Country") }
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expandedCountry = true }
            )
            DropdownMenu(
                expanded = expandedCountry,
                onDismissRequest = { expandedCountry = false }
            ) {
                val countryList by viewModel.customCountries.collectAsState()
                countryList.forEach { (ctyName, ctyCode) ->
                    val flag = try {
                        val firstChar = Character.codePointAt(ctyCode.uppercase(), 0) - 0x41 + 0x1F1E6
                        val secondChar = Character.codePointAt(ctyCode.uppercase(), 1) - 0x41 + 0x1F1E6
                        String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
                    } catch (e: Exception) {
                        "🌐"
                    }
                    DropdownMenuItem(
                        text = { Text("$flag $ctyName", fontSize = 14.sp) },
                        onClick = {
                            editCountry = ctyName
                            editDistrict = ""
                            editUpazila = ""
                            expandedCountry = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val isBD = editCountry.equals("Bangladesh", ignoreCase = true)

        // Dynamic location inputs based on country
        Row(modifier = Modifier.fillMaxWidth()) {
            if (isBD) {
                // Bangladesh District selection dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = editDistrict,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings["district_label"] ?: "District") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expandedDistrict = true })
                    DropdownMenu(
                        expanded = expandedDistrict,
                        onDismissRequest = { expandedDistrict = false }
                    ) {
                        districts.forEach { dist ->
                            DropdownMenuItem(
                                text = { Text(dist) },
                                onClick = {
                                    editDistrict = dist
                                    editUpazila = ""
                                    expandedDistrict = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Bangladesh Upazila selection dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = editUpazila,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings["upazila_label"] ?: "Upazila") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expandedUpazila = true })
                    DropdownMenu(
                        expanded = expandedUpazila,
                        onDismissRequest = { expandedUpazila = false }
                    ) {
                        availableUpazilas.forEach { upz ->
                            DropdownMenuItem(
                                text = { Text(upz) },
                                onClick = {
                                    editUpazila = upz
                                    expandedUpazila = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Foreign Country freeform text input
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = editDistrict,
                        onValueChange = { editDistrict = it },
                        label = { Text(strings["city_state_label"] ?: "City / State") },
                        placeholder = { Text("e.g., New York") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = editUpazila,
                        onValueChange = { editUpazila = it },
                        label = { Text(if (language == AppLanguage.BAN) "অঞ্চল" else "Region") },
                        placeholder = { Text("e.g., Queens") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        if (finalUser.role == "Donor") {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = editLastDonation,
                onValueChange = { editLastDonation = it },
                label = { Text("Last Donation Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_last_donation_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Switch to toggle active status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Mark Me Available for Searches", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Switch(
                    checked = editAvailable,
                    onCheckedChange = { editAvailable = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = BloodRed)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.profileEditName = editName
                viewModel.profileEditPhone = editPhone
                viewModel.profileEditEmail = editEmail
                viewModel.profileEditBlood = editBlood
                viewModel.profileEditDistrict = editDistrict
                viewModel.profileEditUpazila = editUpazila
                viewModel.profileEditLastDonation = editLastDonation
                viewModel.profileEditAvailable = editAvailable
                viewModel.profileEditCountry = editCountry

                viewModel.triggerUpdateProfile()
                Toast.makeText(context, strings["msg_profile_updated"], Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_profile_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Profile Changes", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.triggerLogout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_profile_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BloodRed),
            border = BorderStroke(1.dp, BloodRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Logout, "out")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = strings["logout"] ?: "Logout", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AdminStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title.uppercase(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val donorsList by viewModel.donors.collectAsState()
    val requestsList by viewModel.requests.collectAsState()
    val scamReportsList by viewModel.scamReports.collectAsState()
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val messagesList by viewModel.messages.collectAsState()
    val ambulancesList by viewModel.ambulances.collectAsState()
    val ambulanceBookingsList by viewModel.ambulanceBookings.collectAsState()

    val context = LocalContext.current

    // Auto-sync remote data when Admin Dashboard is loaded and refresh every 15 seconds
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.triggerRemoteSync()
        while (true) {
            kotlinx.coroutines.delay(15000)
            viewModel.triggerRemoteSync()
        }
    }

    var activeTab by remember { mutableStateOf("DASHBOARD") } // DEFAULT tab is now "DASHBOARD"!

    // Let's compute real blood group segment counts dynamically
    val bgCounts = remember(donorsList) {
        val map = mutableMapOf<String, Int>()
        donorsList.forEach { d ->
            map[d.bloodGroup] = (map[d.bloodGroup] ?: 0) + 1
        }
        map
    }
    val totalDonorsCount = donorsList.size.coerceAtLeast(1)
    val oPlusCount = bgCounts["O+"] ?: 0
    val aPlusCount = bgCounts["A+"] ?: 0
    val bPlusCount = bgCounts["B+"] ?: 0
    val otherCount = (totalDonorsCount - oPlusCount - aPlusCount - bPlusCount).coerceAtLeast(0)

    val oPlusPct = (oPlusCount * 100f / totalDonorsCount).roundToInt()
    val aPlusPct = (aPlusCount * 100f / totalDonorsCount).roundToInt()
    val bPlusPct = (bPlusCount * 100f / totalDonorsCount).roundToInt()
    val otherPct = (100 - oPlusPct - aPlusPct - bPlusPct).coerceAtLeast(0)

    // Convert to angles
    val angleO = oPlusPct * 3.6f
    val angleA = aPlusPct * 3.6f
    val angleB = bPlusPct * 3.6f
    val angleOther = otherPct * 3.6f

    // Scale spline chart points dynamically based on real counts
    val maxVal = maxOf(donorsList.size, requestsList.size, 10).toFloat()
    val pointsDonors = listOf(
        (donorsList.size * 0.4f) / maxVal,
        (donorsList.size * 0.55f) / maxVal,
        (donorsList.size * 0.45f) / maxVal,
        (donorsList.size * 0.75f) / maxVal,
        (donorsList.size * 0.65f) / maxVal,
        (donorsList.size * 0.85f) / maxVal,
        (donorsList.size * 1.0f) / maxVal
    ).map { it.coerceIn(0.1f, 0.95f) }

    val pointsReqs = listOf(
        (requestsList.size * 0.2f) / maxVal,
        (requestsList.size * 0.4f) / maxVal,
        (requestsList.size * 0.35f) / maxVal,
        (requestsList.size * 0.5f) / maxVal,
        (requestsList.size * 0.45f) / maxVal,
        (requestsList.size * 0.6f) / maxVal,
        (requestsList.size * 1.0f) / maxVal
    ).map { it.coerceIn(0.1f, 0.95f) }
    
    // Live filter inputs
    var searchQuery by remember { mutableStateOf("") }
    var filterBloodGroup by remember { mutableStateOf("All") }
    var filterStatus by remember { mutableStateOf("All") }

    // Multi-criteria filtering logic
    val filteredDonors = remember(donorsList, searchQuery, filterBloodGroup, filterStatus) {
        donorsList.filter { donor ->
            val matchesSearch = searchQuery.isEmpty() ||
                donor.name.contains(searchQuery, ignoreCase = true) ||
                donor.phone.contains(searchQuery) ||
                donor.district.contains(searchQuery, ignoreCase = true) ||
                donor.upazila.contains(searchQuery, ignoreCase = true) ||
                donor.userId.contains(searchQuery, ignoreCase = true) ||
                donor.displayUserId.contains(searchQuery, ignoreCase = true)

            val matchesBlood = filterBloodGroup == "All" || donor.bloodGroup == filterBloodGroup

            val matchesStatus = when (filterStatus) {
                "Pending" -> !donor.isApproved
                "Approved" -> donor.isApproved
                else -> true
            }

            matchesSearch && matchesBlood && matchesStatus
        }
    }

    val filteredRequests = remember(requestsList, searchQuery, filterBloodGroup, filterStatus) {
        requestsList.filter { req ->
            val matchesSearch = searchQuery.isEmpty() ||
                req.patientName.contains(searchQuery, ignoreCase = true) ||
                req.contactNumber.contains(searchQuery) ||
                req.hospitalName.contains(searchQuery, ignoreCase = true)

            val matchesBlood = filterBloodGroup == "All" || req.bloodGroup == filterBloodGroup

            val matchesStatus = when (filterStatus) {
                "Active" -> req.status == "Active"
                "Resolved" -> req.status == "Resolved"
                else -> true
            }

            matchesSearch && matchesBlood && matchesStatus
        }
    }

    val filteredReports = remember(scamReportsList, searchQuery, filterStatus) {
        scamReportsList.filter { rep ->
            val matchesSearch = searchQuery.isEmpty() ||
                rep.scammerDonorName.contains(searchQuery, ignoreCase = true) ||
                rep.scammerDonorPhone.contains(searchQuery) ||
                rep.reporterName.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (filterStatus) {
                "Pending" -> rep.status == "Pending"
                "Banned" -> rep.status == "Banned"
                "Dismissed" -> rep.status == "Dismissed"
                else -> true
            }

            matchesSearch && matchesStatus
        }
    }

    // Modern Indigo Purple theme color palette for NOVUS
    val novusSidebarBg = Color(0xFF3F4FB5)
    val novusSidebarDark = Color(0xFF2E3280)
    val novusSidebarActive = Color(0xFF4F5EC7)
    val novusBg = Color(0xFFF3F4F6)
    val novusHeaderBg = Color.White
    val novusBorder = Color(0xFFE5E7EB)

    val adminMenus = listOf(
        Triple("DASHBOARD", if (language == AppLanguage.ENG) "Dashboard" else "ড্যাশবোর্ড", Icons.Default.Dashboard),
        Triple("DONORS", if (language == AppLanguage.ENG) "Donors List" else "রক্তদাতা তালিকা", Icons.Default.Person),
        Triple("REQUESTS", if (language == AppLanguage.ENG) "Blood Requests" else "রক্তের অনুরোধসমূহ", Icons.Default.Favorite),
        Triple("AMBULANCES", if (language == AppLanguage.ENG) "Ambulance Service" else "অ্যাম্বুলেন্স সার্ভিস", Icons.Default.LocalHospital),
        Triple("AMBULANCE_BOOKINGS", if (language == AppLanguage.ENG) "Ambulance Bookings" else "অ্যাম্বুলেন্স বুকিং সমূহ", Icons.Default.Book),
        Triple("SUPPORT", if (language == AppLanguage.ENG) "Live Support" else "লাইভ সাপোর্ট", Icons.Default.Chat),
        Triple("POLICIES", if (language == AppLanguage.ENG) "Page Policies" else "পৃষ্ঠা নীতিসমূহ", Icons.Default.List),
        Triple("REPORTS", if (language == AppLanguage.ENG) "Fraud Reports" else "প্রতারণা রিপোর্ট", Icons.Default.Warning),
        Triple("V9_SUBSCRIPTIONS", if (language == AppLanguage.ENG) "V9 Subscriptions" else "ভি৯ সাবস্ক্রিপশন", Icons.Default.Star)
    )

    val currentMenu = adminMenus.find { it.first == activeTab } ?: adminMenus[0]

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1E2230),
                drawerContentColor = Color.White,
                modifier = Modifier.width(280.dp)
            ) {
                // Header of Drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(novusSidebarBg, novusSidebarDark)))
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Logo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.ENG) "Admin Panel" else "এডমিন প্যানেল",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Alif Blood Bank",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Navigation Drawer Items
                adminMenus.forEach { (tag, label, icon) ->
                    val isSelected = activeTab == tag
                    NavigationDrawerItem(
                        label = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val badgeValue = when (tag) {
                                    "DONORS" -> "${donorsList.size}"
                                    "REQUESTS" -> "${requestsList.filter { it.status == "Active" }.size}"
                                    "REPORTS" -> "${scamReportsList.size}"
                                    else -> null
                                }
                                if (badgeValue != null) {
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSelected) Color.White else novusSidebarBg, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = badgeValue,
                                            color = if (isSelected) novusSidebarBg else Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            activeTab = tag
                            filterStatus = "All"
                        },
                        icon = { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = novusSidebarActive,
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                NavigationDrawerItem(
                    label = { Text(if (language == AppLanguage.ENG) "Go to Home Screen" else "হোমে ফিরে যান", fontWeight = FontWeight.Medium, fontSize = 13.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.navigateTo(AppScreen.HOME)
                    },
                    icon = { Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(if (language == AppLanguage.ENG) "Logout Admin" else "লগআউট করুন", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 13.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.triggerLogout()
                        viewModel.navigateTo(AppScreen.HOME)
                    },
                    icon = { Icon(imageVector = Icons.Filled.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(novusBg)
        ) {
            // --- NEW DYNAMIC HEADER WITH DRAWER OPEN BUTTON ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(novusSidebarBg, novusSidebarDark)))
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.size(40.dp).testTag("open_admin_drawer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Sidebar Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.ENG) "Admin Panel" else "এডমিন প্যানেল",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = currentMenu.third,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentMenu.second,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, "search", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.triggerRemoteSync() },
                        modifier = Modifier.size(32.dp).testTag("admin_sync_btn")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeTab == "DASHBOARD") {
                    // --- NOVUS DASHBOARD CONTENT VIEW ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Stat Cards Row - Responsive: 2x2 grid on phone, 1x4 row on tablet
                        val isStatWide = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
                        if (!isStatWide) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    AdminStatCard(
                                        modifier = Modifier.weight(1f).testTag("total_users_stat_card_compact"),
                                        title = if (language == AppLanguage.ENG) "Total App Users" else "সর্বমোট ব্যবহারকারী",
                                        value = "${donorsList.size}",
                                        subtitle = if (language == AppLanguage.ENG) "${donorsList.filter { it.isApproved }.size} Approved Donors" else "${donorsList.filter { it.isApproved }.size} জন অনুমোদিত",
                                        backgroundColor = Color(0xFFD32F2F),
                                        icon = Icons.Default.Person
                                    )
                                    AdminStatCard(
                                        modifier = Modifier.weight(1f),
                                        title = if (language == AppLanguage.ENG) "Support & Chats" else "মন্তব্য ও চ্যাট",
                                        value = "${messagesList.size}",
                                        subtitle = if (language == AppLanguage.ENG) "${messagesList.size} Live Chats" else "${messagesList.size}টি সাপোর্ট মেসেজ",
                                        backgroundColor = Color(0xFFFF9800),
                                        icon = Icons.Default.Chat
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    AdminStatCard(
                                        modifier = Modifier.weight(1f),
                                        title = if (language == AppLanguage.ENG) "Blood Requests" else "রক্তের অনুরোধ",
                                        value = "${requestsList.size}",
                                        subtitle = if (language == AppLanguage.ENG) "${requestsList.filter { it.status == "Active" }.size} Active Requests" else "${requestsList.filter { it.status == "Active" }.size}টি সক্রিয় অনুরোধ",
                                        backgroundColor = Color(0xFF00ACEE),
                                        icon = Icons.Default.LocalHospital
                                    )
                                    AdminStatCard(
                                        modifier = Modifier.weight(1f),
                                        title = if (language == AppLanguage.ENG) "Ambulance Services" else "অ্যাম্বুলেন্স সার্ভিস",
                                        value = "${ambulancesList.size}",
                                        subtitle = if (language == AppLanguage.ENG) "${ambulanceBookingsList.size} Bookings Total" else "${ambulanceBookingsList.size}টি বুকিং সম্পন্ন",
                                        backgroundColor = Color(0xFF4CAF50),
                                        icon = Icons.Default.AirportShuttle
                                    )
                                }
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AdminStatCard(
                                    modifier = Modifier.weight(1f).testTag("total_users_stat_card_wide"),
                                    title = if (language == AppLanguage.ENG) "Total App Users" else "সর্বমোট ব্যবহারকারী",
                                    value = "${donorsList.size}",
                                    subtitle = if (language == AppLanguage.ENG) "${donorsList.filter { it.isApproved }.size} Approved Accounts" else "${donorsList.filter { it.isApproved }.size} জন ভেরিফাইড",
                                    backgroundColor = Color(0xFFD32F2F),
                                    icon = Icons.Default.Person
                                )
                                AdminStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = if (language == AppLanguage.ENG) "Support & Chats" else "মন্তব্য ও চ্যাট",
                                    value = "${messagesList.size}",
                                    subtitle = if (language == AppLanguage.ENG) "${messagesList.size} Chat Messages" else "${messagesList.size}টি সাপোর্ট মেসেজ",
                                    backgroundColor = Color(0xFFFF9800),
                                    icon = Icons.Default.Chat
                                )
                                AdminStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = if (language == AppLanguage.ENG) "Blood Requests" else "রক্তের অনুরোধ",
                                    value = "${requestsList.size}",
                                    subtitle = if (language == AppLanguage.ENG) "${requestsList.filter { it.status == "Active" }.size} Active Requests" else "${requestsList.filter { it.status == "Active" }.size}টি সক্রিয় অনুরোধ",
                                    backgroundColor = Color(0xFF00ACEE),
                                    icon = Icons.Default.LocalHospital
                                )
                                AdminStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = if (language == AppLanguage.ENG) "Ambulance Services" else "অ্যাম্বুলেন্স সার্ভিস",
                                    value = "${ambulancesList.size}",
                                    subtitle = if (language == AppLanguage.ENG) "${ambulanceBookingsList.size} Bookings Registered" else "${ambulanceBookingsList.size}টি বুকিং সম্পন্ন",
                                    backgroundColor = Color(0xFF4CAF50),
                                    icon = Icons.Default.AirportShuttle
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Charts Section Row - Responsive: side-by-side on wide screens, stacked on compact screens
                        val isChartWide = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
                        if (isChartWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Left Widget: Donut Chart (represents blood groups)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(300.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, novusBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (language == AppLanguage.ENG) "BLOOD GROUP SEGMENTS" else "রক্তের গ্রুপ বিভাজন",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(140.dp)) {
                                                val strokeWidth = 32f
                                                // segment 1: O+
                                                drawArc(
                                                    color = Color(0xFF3B5998),
                                                    startAngle = -90f,
                                                    sweepAngle = angleO,
                                                    useCenter = false,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                                )
                                                // segment 2: A+
                                                drawArc(
                                                    color = Color(0xFFFF9800),
                                                    startAngle = -90f + angleO,
                                                    sweepAngle = angleA,
                                                    useCenter = false,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                                )
                                                // segment 3: B+
                                                drawArc(
                                                    color = Color(0xFF00ACEE),
                                                    startAngle = -90f + angleO + angleA,
                                                    sweepAngle = angleB,
                                                    useCenter = false,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                                )
                                                // segment 4: Others
                                                drawArc(
                                                    color = Color(0xFF4CAF50),
                                                    startAngle = -90f + angleO + angleA + angleB,
                                                    sweepAngle = angleOther,
                                                    useCenter = false,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("${donorsList.size}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF3B5998))
                                                Text(if (language == AppLanguage.ENG) "Donors" else "দাতা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF3B5998), CircleShape))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("O+ (${oPlusPct}%)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF9800), CircleShape))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("A+ (${aPlusPct}%)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF00ACEE), CircleShape))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("B+ (${bPlusPct}%)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Other (${otherPct}%)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                                
                                // Right Widget: Double Spline Area Chart (represents trends over months)
                                Card(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(300.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, novusBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.ENG) "UPDATED MONTHLY ACTIVITIES" else "মাসিক কার্যকলাপ আপডেট",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                letterSpacing = 0.5.sp
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF3B5998), CircleShape))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Donors", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF9800), CircleShape))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Requests", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                val gridLines = 5
                                                val hSpacing = size.height / (gridLines + 1)
                                                for (i in 1..gridLines) {
                                                    drawLine(
                                                        color = Color(0xFFF3F4F6),
                                                        start = androidx.compose.ui.geometry.Offset(0f, i * hSpacing),
                                                        end = androidx.compose.ui.geometry.Offset(size.width, i * hSpacing),
                                                        strokeWidth = 1.5f
                                                    )
                                                }
                                                
                                                val pointsDonors = listOf(0.35f, 0.55f, 0.45f, 0.75f, 0.65f, 0.85f, 0.70f)
                                                val pointsReqs = listOf(0.20f, 0.40f, 0.35f, 0.50f, 0.45f, 0.60f, 0.55f)
                                                val stepX = size.width / 6f
                                                
                                                val pDonors = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(0f, size.height - (pointsDonors[0] * size.height * 0.8f))
                                                    for (i in 1 until pointsDonors.size) {
                                                        val prevX = (i - 1) * stepX
                                                        val prevY = size.height - (pointsDonors[i - 1] * size.height * 0.8f)
                                                        val currX = i * stepX
                                                        val currY = size.height - (pointsDonors[i] * size.height * 0.8f)
                                                        cubicTo(
                                                            prevX + stepX/2, prevY,
                                                            currX - stepX/2, currY,
                                                            currX, currY
                                                        )
                                                    }
                                                }
                                                
                                                val pReqs = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(0f, size.height - (pointsReqs[0] * size.height * 0.8f))
                                                    for (i in 1 until pointsReqs.size) {
                                                        val prevX = (i - 1) * stepX
                                                        val prevY = size.height - (pointsReqs[i - 1] * size.height * 0.8f)
                                                        val currX = i * stepX
                                                        val currY = size.height - (pointsReqs[i] * size.height * 0.8f)
                                                        cubicTo(
                                                            prevX + stepX/2, prevY,
                                                            currX - stepX/2, currY,
                                                            currX, currY
                                                        )
                                                    }
                                                }
                                                
                                                val fillDonors = androidx.compose.ui.graphics.Path().apply {
                                                    addPath(pDonors)
                                                    lineTo(size.width, size.height)
                                                    lineTo(0f, size.height)
                                                    close()
                                                }
                                                
                                                val fillReqs = androidx.compose.ui.graphics.Path().apply {
                                                    addPath(pReqs)
                                                    lineTo(size.width, size.height)
                                                    lineTo(0f, size.height)
                                                    close()
                                                }
                                                
                                                drawPath(fillDonors, Color(0x183B5998))
                                                drawPath(fillReqs, Color(0x18FF9800))
                                                
                                                drawPath(pDonors, Color(0xFF3B5998), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                                                drawPath(pReqs, Color(0xFFFF9800), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                                                
                                                pointsDonors.forEachIndexed { index, value ->
                                                    val x = index * stepX
                                                    val y = size.height - (value * size.height * 0.8f)
                                                    drawCircle(Color.White, radius = 6f, center = androidx.compose.ui.geometry.Offset(x, y))
                                                    drawCircle(Color(0xFF3B5998), radius = 4f, center = androidx.compose.ui.geometry.Offset(x, y))
                                                }
                                                
                                                pointsReqs.forEachIndexed { index, value ->
                                                    val x = index * stepX
                                                    val y = size.height - (value * size.height * 0.8f)
                                                    drawCircle(Color.White, radius = 6f, center = androidx.compose.ui.geometry.Offset(x, y))
                                                    drawCircle(Color(0xFFFF9800), radius = 4f, center = androidx.compose.ui.geometry.Offset(x, y))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul").forEach {
                                                Text(it, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Stacked for Compact Screen Size
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, novusBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (language == AppLanguage.ENG) "BLOOD GROUP SEGMENTS" else "রক্তের গ্রুপ বিভাজন",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(125.dp)) {
                                                val strokeWidth = 26f
                                                // segment 1: O+
                                                drawArc(
                                                    color = Color(0xFF3B5998),
                                                    startAngle = -90f,
                                                    sweepAngle = angleO,
                                                    useCenter = false,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                                )
                                                // segment 2: A+
                                                drawArc(
                                                    color = Color(0xFFFF9800),
                                                    startAngle = -90f + angleO,
                                                    sweepAngle = angleA,
                                                    useCenter = false,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                                )
                                                // segment 3: B+
                                                drawArc(
                                                    color = Color(0xFF00ACEE),
                                                    startAngle = -90f + angleO + angleA,
                                                    sweepAngle = angleB,
                                                    useCenter = false,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                                )
                                                // segment 4: Others
                                                drawArc(
                                                    color = Color(0xFF4CAF50),
                                                    startAngle = -90f + angleO + angleA + angleB,
                                                    sweepAngle = angleOther,
                                                    useCenter = false,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("${donorsList.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF3B5998))
                                                Text(if (language == AppLanguage.ENG) "Donors" else "দাতা", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF3B5998), CircleShape))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("O+ (${oPlusPct}%)", fontSize = 8.sp, color = Color.Gray)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF9800), CircleShape))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("A+ (${aPlusPct}%)", fontSize = 8.sp, color = Color.Gray)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF00ACEE), CircleShape))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("B+ (${bPlusPct}%)", fontSize = 8.sp, color = Color.Gray)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF4CAF50), CircleShape))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Other (${otherPct}%)", fontSize = 8.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, novusBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.ENG) "UPDATED MONTHLY ACTIVITIES" else "মাসিক কার্যকলাপ আপডেট",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(5.dp).background(Color(0xFF3B5998), CircleShape))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Donors", fontSize = 8.sp, color = Color.Gray)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(5.dp).background(Color(0xFFFF9800), CircleShape))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Requests", fontSize = 8.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                val gridLines = 4
                                                val hSpacing = size.height / (gridLines + 1)
                                                for (i in 1..gridLines) {
                                                    drawLine(
                                                        color = Color(0xFFF3F4F6),
                                                        start = androidx.compose.ui.geometry.Offset(0f, i * hSpacing),
                                                        end = androidx.compose.ui.geometry.Offset(size.width, i * hSpacing),
                                                        strokeWidth = 1.5f
                                                    )
                                                }
                                                
                                                val pointsDonors = listOf(0.35f, 0.55f, 0.45f, 0.75f, 0.65f, 0.85f, 0.70f)
                                                val pointsReqs = listOf(0.20f, 0.40f, 0.35f, 0.50f, 0.45f, 0.60f, 0.55f)
                                                val stepX = size.width / 6f
                                                
                                                val pDonors = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(0f, size.height - (pointsDonors[0] * size.height * 0.8f))
                                                    for (i in 1 until pointsDonors.size) {
                                                        val prevX = (i - 1) * stepX
                                                        val prevY = size.height - (pointsDonors[i - 1] * size.height * 0.8f)
                                                        val currX = i * stepX
                                                        val currY = size.height - (pointsDonors[i] * size.height * 0.8f)
                                                        cubicTo(
                                                            prevX + stepX/2, prevY,
                                                            currX - stepX/2, currY,
                                                            currX, currY
                                                        )
                                                    }
                                                }
                                                
                                                val pReqs = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(0f, size.height - (pointsReqs[0] * size.height * 0.8f))
                                                    for (i in 1 until pointsReqs.size) {
                                                        val prevX = (i - 1) * stepX
                                                        val prevY = size.height - (pointsReqs[i - 1] * size.height * 0.8f)
                                                        val currX = i * stepX
                                                        val currY = size.height - (pointsReqs[i] * size.height * 0.8f)
                                                        cubicTo(
                                                            prevX + stepX/2, prevY,
                                                            currX - stepX/2, currY,
                                                            currX, currY
                                                        )
                                                    }
                                                }
                                                
                                                val fillDonors = androidx.compose.ui.graphics.Path().apply {
                                                    addPath(pDonors)
                                                    lineTo(size.width, size.height)
                                                    lineTo(0f, size.height)
                                                    close()
                                                }
                                                
                                                val fillReqs = androidx.compose.ui.graphics.Path().apply {
                                                    addPath(pReqs)
                                                    lineTo(size.width, size.height)
                                                    lineTo(0f, size.height)
                                                    close()
                                                }
                                                
                                                drawPath(fillDonors, Color(0x183B5998))
                                                drawPath(fillReqs, Color(0x18FF9800))
                                                
                                                drawPath(pDonors, Color(0xFF3B5998), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                                                drawPath(pReqs, Color(0xFFFF9800), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul").forEach {
                                                Text(it, fontSize = 9.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom Section Row (Browser Stats & Products/Actions Table)
                        val isBottomWide = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 650
                        if (isBottomWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Browser Stats / Blood Demand
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(280.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, novusBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (language == AppLanguage.ENG) "BLOOD DEMAND STATS" else "রক্তের চাহিদার পরিসংখ্যান",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        val browsers = listOf(
                                            Triple(if (language == AppLanguage.ENG) "Dhaka District" else "ঢাকা জেলা", 0.85f, Color(0xFF3B5998)),
                                            Triple(if (language == AppLanguage.ENG) "Chattogram District" else "চট্টগ্রাম জেলা", 0.65f, Color(0xFFFF9800)),
                                            Triple(if (language == AppLanguage.ENG) "Sylhet District" else "সিলেট জেলা", 0.45f, Color(0xFF00ACEE)),
                                            Triple(if (language == AppLanguage.ENG) "Rajshahi District" else "রাজশাহী জেলা", 0.55f, Color(0xFF4CAF50)),
                                            Triple(if (language == AppLanguage.ENG) "Khulna District" else "খুলনা জেলা", 0.30f, Color(0xFFE53935))
                                        )
                                        
                                        browsers.forEach { (name, progress, col) ->
                                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                    Text("${(progress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // Progress Bar
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(3.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(progress)
                                                            .height(6.dp)
                                                            .background(col, RoundedCornerShape(3.dp))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Products Table / Administrative Logs Table
                                Card(
                                    modifier = Modifier
                                        .weight(1.8f)
                                        .height(280.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, novusBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (language == AppLanguage.ENG) "RECENT PLATFORM ACTIONS" else "সাম্প্রতিক প্লাটফর্ম কার্যক্রম",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        // Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF9FAFB), RoundedCornerShape(4.dp))
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("#", modifier = Modifier.width(20.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            Text(if (language == AppLanguage.ENG) "ACTIVITY ACTION" else "কার্যক্রম বিবরণ", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            Text(if (language == AppLanguage.ENG) "STATUS" else "অবস্থা", modifier = Modifier.width(80.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center)
                                            Text(if (language == AppLanguage.ENG) "PROGRESS" else "অগ্রগতি", modifier = Modifier.width(70.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        }
                                        
                                        val rows = listOf(
                                            listOf("1", if (language == AppLanguage.ENG) "Approved Md. Alif as Verified Donor" else "মো: আলিফকে ভেরিফাইড রক্তদাতা অনুমোদন", "APPROVED", "1.0"),
                                            listOf("2", if (language == AppLanguage.ENG) "Fraud scam report warning on 017293..." else "প্রতারণার অভিযোগে সতর্কতা নোটিশ ইস্যু", "WARNED", "0.45"),
                                            listOf("3", if (language == AppLanguage.ENG) "System sync of 12 medical registers" else "১২টি মেডিকেল রেজিস্ট্রি সিঙ্ক সম্পন্ন", "COMPLETED", "1.0"),
                                            listOf("4", if (language == AppLanguage.ENG) "New urgent request Sir Salimullah" else "স্যার সলিমুল্লাহ জরুরি রক্ত রিকোয়েস্ট", "PENDING", "0.7")
                                        )
                                        
                                        rows.forEach { row ->
                                            val sno = row[0]
                                            val activity = row[1]
                                            val status = row[2]
                                            val prog = row[3].toFloatOrNull() ?: 1f
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(sno, modifier = Modifier.width(20.dp), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text(activity, modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                
                                                val badgeCol = when (status) {
                                                    "APPROVED", "COMPLETED" -> Color(0xFFE6F4EA)
                                                    "PENDING" -> Color(0xFFFEF7E0)
                                                    "WARNED" -> Color(0xFFFCE8E6)
                                                    else -> Color.LightGray
                                                }
                                                val txtCol = when (status) {
                                                    "APPROVED", "COMPLETED" -> Color(0xFF137333)
                                                    "PENDING" -> Color(0xFFB06000)
                                                    "WARNED" -> Color(0xFFC5221F)
                                                    else -> Color.DarkGray
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .width(80.dp)
                                                        .background(badgeCol, RoundedCornerShape(4.dp))
                                                        .padding(vertical = 3.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(status, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = txtCol)
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .width(70.dp)
                                                        .height(4.dp)
                                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(2.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(prog)
                                                            .height(4.dp)
                                                            .background(txtCol, RoundedCornerShape(2.dp))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Stacked on compact devices
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, novusBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (language == AppLanguage.ENG) "BLOOD DEMAND STATS" else "রক্তের চাহিদার পরিসংখ্যান",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        val browsers = listOf(
                                            Triple(if (language == AppLanguage.ENG) "Dhaka District" else "ঢাকা জেলা", 0.85f, Color(0xFF3B5998)),
                                            Triple(if (language == AppLanguage.ENG) "Chattogram District" else "চট্টগ্রাম জেলা", 0.65f, Color(0xFFFF9800)),
                                            Triple(if (language == AppLanguage.ENG) "Sylhet District" else "সিলেট জেলা", 0.45f, Color(0xFF00ACEE)),
                                            Triple(if (language == AppLanguage.ENG) "Rajshahi District" else "রাজশাহী জেলা", 0.55f, Color(0xFF4CAF50)),
                                            Triple(if (language == AppLanguage.ENG) "Khulna District" else "খুলনা জেলা", 0.30f, Color(0xFFE53935))
                                        )
                                        
                                        browsers.forEach { (name, progress, col) ->
                                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                    Text("${(progress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(3.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(progress)
                                                            .height(6.dp)
                                                            .background(col, RoundedCornerShape(3.dp))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, novusBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (language == AppLanguage.ENG) "RECENT PLATFORM ACTIONS" else "সাম্প্রতিক প্লাটফর্ম কার্যক্রম",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF9FAFB), RoundedCornerShape(4.dp))
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("#", modifier = Modifier.width(20.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            Text(if (language == AppLanguage.ENG) "ACTIVITY ACTION" else "কার্যক্রম বিবরণ", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            Text(if (language == AppLanguage.ENG) "STATUS" else "অবস্থা", modifier = Modifier.width(80.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center)
                                        }
                                        
                                        val rows = listOf(
                                            listOf("1", if (language == AppLanguage.ENG) "Approved Md. Alif as Verified Donor" else "মো: আলিফকে ভেরিফাইড রক্তদাতা অনুমোদন", "APPROVED"),
                                            listOf("2", if (language == AppLanguage.ENG) "Fraud scam report warning on 017293..." else "প্রতারণার অভিযোগে সতর্কতা নোটিশ ইস্যু", "WARNED"),
                                            listOf("3", if (language == AppLanguage.ENG) "System sync of 12 medical registers" else "১২টি মেডিকেল রেজিস্ট্রি সিঙ্ক সম্পন্ন", "COMPLETED"),
                                            listOf("4", if (language == AppLanguage.ENG) "New urgent request Sir Salimullah" else "স্যার সলিমুল্লাহ জরুরি রক্ত রিকোয়েস্ট", "PENDING")
                                        )
                                        
                                        rows.forEach { row ->
                                            val sno = row[0]
                                            val activity = row[1]
                                            val status = row[2]
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(sno, modifier = Modifier.width(20.dp), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text(activity, modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                
                                                val badgeCol = when (status) {
                                                    "APPROVED", "COMPLETED" -> Color(0xFFE6F4EA)
                                                    "PENDING" -> Color(0xFFFEF7E0)
                                                    "WARNED" -> Color(0xFFFCE8E6)
                                                    else -> Color.LightGray
                                                }
                                                val txtCol = when (status) {
                                                    "APPROVED", "COMPLETED" -> Color(0xFF137333)
                                                    "PENDING" -> Color(0xFFB06000)
                                                    "WARNED" -> Color(0xFFC5221F)
                                                    else -> Color.DarkGray
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .width(80.dp)
                                                        .background(badgeCol, RoundedCornerShape(4.dp))
                                                        .padding(vertical = 3.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(status, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = txtCol)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- NEW CARD: USERS BY COUNTRY / দেশভিত্তিক ব্যবহারকারী ---
                        val usersByCountry = remember(donorsList) {
                            donorsList.groupBy { it.country }
                                .mapValues { it.value.size }
                                .toList()
                                .sortedByDescending { it.second }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("country_users_card"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, novusBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ENG) "USERS BY COUNTRY" else "দেশভিত্তিক ব্যবহারকারী পরিসংখ্যান",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (language == AppLanguage.ENG) "Global donor distribution metrics" else "বিশ্বব্যাপী রক্তদাতাদের নিবন্ধিত সংখ্যা ও বিন্যাস",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(novusSidebarBg.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = novusSidebarBg,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "${usersByCountry.size} " + (if (language == AppLanguage.ENG) "Countries" else "দেশ"),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = novusSidebarBg
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    usersByCountry.forEach { (countryName, count) ->
                                        val flag = when (countryName.lowercase().trim()) {
                                            "bangladesh" -> "🇧🇩"
                                            "united states", "usa", "us" -> "🇺🇸"
                                            "india" -> "🇮🇳"
                                            "saudi arabia", "saudi" -> "🇸🇦"
                                            "united arab emirates", "uae" -> "🇦🇪"
                                            "united kingdom", "uk" -> "🇬🇧"
                                            "canada" -> "🇨🇦"
                                            "australia" -> "🇦🇺"
                                            "pakistan" -> "🇵🇰"
                                            else -> "🏳️"
                                        }

                                        val totalDonors = donorsList.size.coerceAtLeast(1)
                                        val ratio = count.toFloat() / totalDonors.toFloat()

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.width(130.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(flag, fontSize = 16.sp)
                                                Text(
                                                    text = countryName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.DarkGray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(8.dp)
                                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(ratio)
                                                        .height(8.dp)
                                                        .background(
                                                            brush = Brush.horizontalGradient(
                                                                colors = listOf(novusSidebarBg, Color(0xFFEF4444))
                                                            ),
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                )
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier.width(70.dp)
                                            ) {
                                                Text(
                                                    text = if (language == AppLanguage.ENG) "$count Users" else "$count জন ইউজার",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.DarkGray
                                                )
                                                Text(
                                                    text = "${(ratio * 100).toInt()}%",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    /*
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                                Text("${(progress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            // Progress Bar
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(3.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(progress)
                                                        .height(6.dp)
                                                        .background(col, RoundedCornerShape(3.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Products Table / Administrative Logs Table
                            Card(
                                modifier = Modifier
                                    .weight(1.8f)
                                    .height(260.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, novusBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("RECENT PLATFORM ACTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    
                                    // Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF9FAFB))
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("S.NO", modifier = Modifier.width(30.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Text("ACTIVITY ACTION", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Text("STATUS", modifier = Modifier.width(80.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center)
                                        Text("PROGRESS", modifier = Modifier.width(70.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    }
                                    
                                    val rows = listOf(
                                        listOf("1", "Approved Md. Alif as Verified Donor", "APPROVED", "1.0"),
                                        listOf("2", "Fraud scam report warning on 017293...", "WARNED", "0.45"),
                                        listOf("3", "System sync of 12 medical registers", "COMPLETED", "1.0"),
                                        listOf("4", "New urgent request Sir Salimullah Hospital", "PENDING", "0.7")
                                    )
                                    
                                    rows.forEach { row ->
                                        val sno = row[0]
                                        val activity = row[1]
                                        val status = row[2]
                                        val prog = row[3].toFloatOrNull() ?: 1f
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(sno, modifier = Modifier.width(30.dp), fontSize = 10.sp, color = Color.Gray)
                                            Text(activity, modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            
                                            // Badge Status
                                            val badgeCol = when (status) {
                                                "APPROVED", "COMPLETED" -> Color(0xFFE6F4EA)
                                                "PENDING" -> Color(0xFFFEF7E0)
                                                "WARNED" -> Color(0xFFFCE8E6)
                                                else -> Color.LightGray
                                            }
                                            val txtCol = when (status) {
                                                "APPROVED", "COMPLETED" -> Color(0xFF137333)
                                                "PENDING" -> Color(0xFFB06000)
                                                "WARNED" -> Color(0xFFC5221F)
                                                else -> Color.DarkGray
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .width(80.dp)
                                                    .background(badgeCol, RoundedCornerShape(4.dp))
                                                    .padding(vertical = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(status, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = txtCol)
                                            }
                                            
                                            // Progress Mini Bar
                                            Box(
                                                modifier = Modifier
                                                    .width(70.dp)
                                                    .height(4.dp)
                                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(2.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(prog)
                                                        .height(4.dp)
                                                        .background(txtCol, RoundedCornerShape(2.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    */
                } else {
                    // --- EXISTING PORTAL SYSTEM PAGES (with original styling inside the White Workspace Canvas) ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Interactive Search & Filters Section
                        val statusOptions = when (activeTab) {
                            "DONORS" -> listOf("All", "Pending", "Approved")
                            "REQUESTS" -> listOf("All", "Active", "Resolved")
                            "REPORTS" -> listOf("All", "Pending", "Banned", "Dismissed")
                            else -> listOf("All")
                        }

                        if (activeTab in listOf("DONORS", "REQUESTS", "REPORTS")) {
                            AdminFiltersCard(
                                language = language,
                                searchQuery = searchQuery,
                                onSearchChange = { searchQuery = it },
                                filterBloodGroup = filterBloodGroup,
                                onBloodGroupChange = { filterBloodGroup = it },
                                filterStatus = filterStatus,
                                onStatusChange = { filterStatus = it },
                                statusOptions = statusOptions
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (activeTab) {
                                "DONORS" -> {
                                    AdminDonorsTab(
                                        donors = filteredDonors,
                                        language = language,
                                        onApprove = { id ->
                                            viewModel.adminApproveDonor(id)
                                            Toast.makeText(context, "Donor Approved!", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = { id ->
                                            viewModel.adminDeleteDonor(id)
                                            Toast.makeText(context, "Donor Deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        onSupportChat = { phone, name ->
                                            viewModel.openChatRoom(phone, name, isSupport = true)
                                        },
                                        onWarnDonor = { id, isWarning, reason ->
                                            viewModel.adminWarnDonor(id, isWarning, reason)
                                            val msg = if (isWarning) "Donor Warned Successfully!" else "Warning Removed!"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                "REQUESTS" -> {
                                    AdminRequestsTab(
                                        requests = filteredRequests,
                                        language = language,
                                        onToggle = { id -> viewModel.adminToggleRequest(id) },
                                        onDelete = { id ->
                                            viewModel.adminDeleteRequest(id)
                                            Toast.makeText(context, "Request Deleted", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                "AMBULANCES" -> {
                                    AdminAmbulancesTab(
                                        ambulances = ambulancesList,
                                        language = language,
                                        onToggleAvailability = { id ->
                                            viewModel.triggerToggleAmbulanceAvailability(id)
                                            Toast.makeText(context, "Ambulance Status Updated!", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = { id ->
                                            viewModel.adminDeleteAmbulance(id)
                                            Toast.makeText(context, "Ambulance Registration Removed!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                "AMBULANCE_BOOKINGS" -> {
                                    AdminBookingsTab(
                                        bookings = ambulanceBookingsList,
                                        language = language,
                                        onDelete = { id ->
                                            viewModel.adminDeleteAmbulanceBooking(id)
                                            Toast.makeText(context, "Booking Request Dismissed!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                "SUPPORT" -> {
                                    AdminSupportTab(viewModel = viewModel, language = language)
                                }
                                "POLICIES" -> {
                                    val privacyEn by viewModel.privacyPolicyEn.collectAsState()
                                    val privacyBn by viewModel.privacyPolicyBn.collectAsState()
                                    val termsEn by viewModel.termsConditionsEn.collectAsState()
                                    val termsBn by viewModel.termsConditionsBn.collectAsState()
                                    val refundEn by viewModel.refundPolicyEn.collectAsState()
                                    val refundBn by viewModel.refundPolicyBn.collectAsState()

                                    AdminPoliciesTab(
                                        language = language,
                                        privacyEn = privacyEn,
                                        privacyBn = privacyBn,
                                        termsEn = termsEn,
                                        termsBn = termsBn,
                                        refundEn = refundEn,
                                        refundBn = refundBn,
                                        onSave = { pEn, pBn, tEn, tBn, rEn, rBn ->
                                            viewModel.updatePolicies(pEn, pBn, tEn, tBn, rEn, rBn)
                                            Toast.makeText(context, "Policy Pages Saved Successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                "REPORTS" -> {
                                    AdminReportsTab(
                                        reports = filteredReports,
                                        language = language,
                                        onDismiss = { id ->
                                            viewModel.adminActionOnScamReport(id, "Dismissed")
                                            Toast.makeText(context, "Report dismissed", Toast.LENGTH_SHORT).show()
                                        },
                                        onBan = { id ->
                                            viewModel.adminActionOnScamReport(id, "Banned")
                                            Toast.makeText(context, "Scammer suspended and banned!", Toast.LENGTH_LONG).show()
                                        },
                                        strings = strings,
                                        donors = donorsList,
                                        onUpdateReport = { id, name, phone, amount, reason, status ->
                                            viewModel.updateScamReport(id, name, phone, amount, reason, status)
                                        },
                                        viewModel = viewModel
                                    )
                                }
                                "V9_SUBSCRIPTIONS" -> {
                                    AdminSubscriptionsTab(viewModel = viewModel, language = language)
                                }
                                "DUMMY_SETTINGS_DELETED" -> {
                                    val appNameState by viewModel.appName.collectAsState()
                                    val homeNoticeState by viewModel.homeNotice.collectAsState()
                                    val popupNoticeState by viewModel.popupNotice.collectAsState()

                                    val emailNotifyEnabledState by viewModel.emailNotifyEnabled.collectAsState()
                                    val smtpHostState by viewModel.smtpHost.collectAsState()
                                    val smtpPortState by viewModel.smtpPort.collectAsState()
                                    val smtpUsernameState by viewModel.smtpUsername.collectAsState()
                                    val smtpPasswordState by viewModel.smtpPassword.collectAsState()
                                    val emailSubjectState by viewModel.emailSubjectTemplate.collectAsState()
                                    val emailBodyState by viewModel.emailBodyTemplate.collectAsState()

                                    val adMobEnabledState by viewModel.adMobEnabled.collectAsState()
                                    val adMobAppIdState by viewModel.adMobAppId.collectAsState()
                                    val adMobBannerIdState by viewModel.adMobBannerId.collectAsState()
                                    val adMobInterstitialIdState by viewModel.adMobInterstitialId.collectAsState()
                                    val adMobNativeIdState by viewModel.adMobNativeId.collectAsState()
                                    
                                    val useMockStatsState by viewModel.useMockStats.collectAsState()
                                    val mockTotalUsersState by viewModel.mockTotalUsers.collectAsState()
                                    val mockTotalDonorsState by viewModel.mockTotalDonors.collectAsState()

                                    AdminSettingsTab(
                                        viewModel = viewModel,
                                        language = language,
                                        appName = appNameState,
                                        onAppNameSave = { viewModel.updateAppName(it) },
                                        homeNotice = homeNoticeState,
                                        onHomeNoticeSave = { viewModel.updateHomeNotice(it) },
                                        popupNotice = popupNoticeState,
                                        onPopupNoticeSave = { viewModel.updatePopupNotice(it) },
                                        emailEnabled = emailNotifyEnabledState,
                                        smtpHost = smtpHostState,
                                        smtpPort = smtpPortState,
                                        smtpUsername = smtpUsernameState,
                                        smtpPassword = smtpPasswordState,
                                        emailSubject = emailSubjectState,
                                        emailBody = emailBodyState,
                                        onEmailConfigSave = { enabled, host, port, user, pass, subject, body ->
                                            viewModel.updateEmailConfig(context, enabled, host, port, user, pass, subject, body)
                                        },
                                        adMobEnabled = adMobEnabledState,
                                        adMobAppId = adMobAppIdState,
                                        adMobBannerId = adMobBannerIdState,
                                        adMobInterstitialId = adMobInterstitialIdState,
                                        adMobNativeId = adMobNativeIdState,
                                        onAdMobConfigSave = { enabled, appId, bannerId, interstitialId, nativeId ->
                                            viewModel.updateAdMobConfig(context, enabled, appId, bannerId, interstitialId, nativeId)
                                        },
                                        useMockStats = useMockStatsState,
                                        mockTotalUsers = mockTotalUsersState,
                                        mockTotalDonors = mockTotalDonorsState,
                                        onStatsConfigSave = { use, users, donors ->
                                            viewModel.setUseMockStats(use)
                                            viewModel.updateMockStats(users, donors)
                                        }
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}
}



@Composable
fun PrivacyPolicyScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsState()
    val privacyEn by viewModel.privacyPolicyEn.collectAsState()
    val privacyBn by viewModel.privacyPolicyBn.collectAsState()

    val currentPrivacy = if (language == AppLanguage.ENG) privacyEn else privacyBn

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Card Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = BloodRed)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Privacy Policy" else "প্রাইভেসি পলিসি",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Document Details Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(1.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Privacy Policy",
                        tint = BloodRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Data Protection & Usage" else "তথ্য সুরক্ষা ও ব্যবহার",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText
                    )
                }

                HorizontalDivider(color = LightBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentPrivacy,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightPinkRed, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) 
                            "Last updated: June 2026. For any queries, write to info@alifshenltd.com" 
                            else "সর্বশেষ আপডেট: জুন ২০২৬। যেকোনো জিজ্ঞাসায় মেইল করুন info@alifshenltd.com",
                        fontSize = 11.sp,
                        color = BloodRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun TermsConditionsScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsState()
    val termsEn by viewModel.termsConditionsEn.collectAsState()
    val termsBn by viewModel.termsConditionsBn.collectAsState()

    val currentTerms = if (language == AppLanguage.ENG) termsEn else termsBn

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Card Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = BloodRed)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Terms & Conditions" else "টার্মস এন্ড কন্ডিশন",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Document Details Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(1.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Terms and Conditions",
                        tint = BloodRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "User Agreement" else "ব্যবহারকারীর অঙ্গীকারনামা",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText
                    )
                }

                HorizontalDivider(color = LightBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentTerms,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightPinkRed, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) 
                            "By accessing or using our services, you indicate your direct consent to respect donor availability." 
                            else "আমাদের সেবা ব্যবহারের মাধ্যমে, আপনি রক্তদাতার গোপনীয়তা এবং প্ল্যাটফর্মের নিয়মাবলি মেনে চলতে বাধ্য থাকবেন।",
                        fontSize = 11.sp,
                        color = BloodRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun RefundPolicyScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsState()
    val refundEn by viewModel.refundPolicyEn.collectAsState()
    val refundBn by viewModel.refundPolicyBn.collectAsState()

    val currentRefund = if (language == AppLanguage.ENG) refundEn else refundBn

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Card Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = BloodRed)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Refund Policy" else "রিফান্ড পলিসি",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Document Details Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(1.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payment,
                        contentDescription = "Refund Policy",
                        tint = BloodRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Funding & Refunding" else "তহবিল এবং রিফান্ড পলিসি",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText
                    )
                }

                HorizontalDivider(color = LightBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentRefund,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightPinkRed, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) 
                            "This app does not ask for or collect monetary donations, and we do not process transactions." 
                            else "এই অ্যাপ্লিকেশনটি কোনো প্রকার আর্থিক লেনদেন বা সাহায্য গ্রহণ করে না। তাই কোনো রিফান্ড বা চার্জের প্রশ্নই নেই।",
                        fontSize = 11.sp,
                        color = BloodRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- IN-APP CHAT DIRECT MESSAGING CHANNELS ---

@Composable
fun ChatInboxScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsState()
    val strings by viewModel.strings.collectAsState()
    val appName by viewModel.appName.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val donors by viewModel.donors.collectAsState()

    if (userSession == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Forum,
                contentDescription = "Chat",
                tint = BloodRed,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (language == AppLanguage.ENG) "In-App Direct Messaging" else "সরাসরি চ্যাট",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (language == AppLanguage.ENG) 
                    "You must login or register to send and receive messages with blood donors or seekers." 
                    else "রক্তদাতা বা রক্ত গ্রহীতাদের সাথে সরাসরি চ্যাটে যোগাযোগ করতে আপনাকে অবস্যই লগইন করতে হবে।",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    viewModel.setShowRegistrationTab(false)
                    viewModel.navigateTo(AppScreen.LOGIN_REGISTER) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("chat_login_prompt_btn")
            ) {
                Text(
                    text = strings["btn_login"] ?: "Login / Register",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        return
    }

    val currentUser = userSession!!
    
    val myMessages = messages.filter { it.senderPhone == currentUser.phone || it.receiverPhone == currentUser.phone }
    val uniquePeers = myMessages
        .map { if (it.senderPhone == currentUser.phone) it.receiverPhone to it.receiverName else it.senderPhone to it.senderName }
        .distinctBy { it.first }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = null,
                tint = BloodRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings["chat_title"] ?: "Chat & Messaging",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uniquePeers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.ChatBubble,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "No active conversations yet." else "এখনো কোনো চ্যাট তালিকা নেই।",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.ENG) 
                        "Search direct donors or reply to emergency requests to start in-app chatting." 
                        else "রক্তদাতা খুঁজে বা রক্তের গুরুত্ব অনুযায়ী তাদের সাথে ইন-অ্যাপ চ্যাট চালু করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uniquePeers.size) { index ->
                    val (peerPhone, peerName) = uniquePeers[index]
                    
                    val donorPeer = donors.find { it.phone == peerPhone }
                    val bloodSymbol = donorPeer?.bloodGroup ?: "💬"
                    
                    val threadMsgs = myMessages.filter { 
                        (it.senderPhone == currentUser.phone && it.receiverPhone == peerPhone) || 
                        (it.senderPhone == peerPhone && it.receiverPhone == currentUser.phone)
                    }
                    val lastMsgObj = threadMsgs.lastOrNull()
                    val lastMsgText = lastMsgObj?.message ?: ""
                    val lastTimestamp = lastMsgObj?.timestamp ?: ""
                    
                    val unreadCount = threadMsgs.count { it.senderPhone == peerPhone && !it.isRead }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openChatRoom(peerPhone, peerName) }
                            .testTag("chat_thread_$index"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F1F1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (donorPeer != null) BloodRed else Color(0xFF42A5F5), 
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bloodSymbol,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = peerName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = lastTimestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastMsgText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (unreadCount > 0) DarkText else Color.Gray,
                                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(BloodRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val strings by viewModel.strings.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val peerPhone by viewModel.activeChatPeerPhone.collectAsState()
    val peerName by viewModel.activeChatPeerName.collectAsState()
    val bookings by viewModel.ambulanceBookings.collectAsState()
    val ambulances by viewModel.ambulances.collectAsState()

    var msgInput by remember { mutableStateOf("") }

    if (userSession == null || peerPhone == null || peerName == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = BloodRed)
        }
        return
    }

    val currentUser = userSession!!
    val peerPhoneStr = peerPhone!!
    val peerNameStr = peerName!!

    val senderPhone = if (viewModel.isSupportChatMode) "LIVE_SUPPORT" else currentUser.phone
    val senderName = if (viewModel.isSupportChatMode) "Live Support Admin" else currentUser.name

    // Check if logged-in user is an ambulance driver/owner and has unpaid commission
    val isAmbulanceDriver = currentUser.role == "Ambulance" || 
        ambulances.any { it.phone == currentUser.phone || it.ownerName == currentUser.name }
    val myBookings = bookings.filter { it.assignedAmbulancePhone == currentUser.phone }
    val userHasUnpaidCommission = isAmbulanceDriver && myBookings.any { it.status == "Completed" && !it.isCommissionPaid && it.fare > 0.0 }

    androidx.compose.runtime.LaunchedEffect(messages) {
        viewModel.markInAppChatRead(senderPhone, peerPhoneStr)
    }

    val threadMsgs = messages.filter { 
        (it.senderPhone == senderPhone && it.receiverPhone == peerPhoneStr) || 
        (it.senderPhone == peerPhoneStr && it.receiverPhone == senderPhone)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, 
                        contentDescription = "Back",
                        tint = BloodRed
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BloodRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = peerNameStr.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = peerNameStr,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkText
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings["chat_status_online"] ?: "Online",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                val donorsList by viewModel.donors.collectAsState()
                val peerDonor = donorsList.find { it.phone == peerPhoneStr }
                if (peerDonor != null) {
                    TextButton(
                        onClick = {
                            viewModel.selectDonorAndNavigate(peerDonor.id)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = BloodRed)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.BAN) "প্রোফাইল" else "Profile",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(threadMsgs.size) { index ->
                val msg = threadMsgs[index]
                val isMe = msg.senderPhone == currentUser.phone

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) BloodRed else Color.White
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isMe) 12.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 12.dp
                        ),
                        border = if (isMe) null else BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .widthIn(max = 260.dp)
                        ) {
                            Text(
                                text = msg.message,
                                color = if (isMe) Color.White else DarkText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.timestamp,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                fontSize = 9.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        if (userHasUnpaidCommission) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFEBEE)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) {
                            "বকেয়া ৫% কমিশন অপরিশোধিত থাকায় এসএমএস পাঠানো লক করা হয়েছে।"
                        } else {
                            "Sending messages is locked due to outstanding 5% commission dues."
                        },
                        fontSize = 11.sp,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = msgInput,
                    onValueChange = { msgInput = it },
                    enabled = !userHasUnpaidCommission,
                    placeholder = { 
                        Text(
                            text = if (userHasUnpaidCommission) {
                                if (language == AppLanguage.BAN) "বকেয়া কমিশন পরিশোধ করুন" else "Pay due commission"
                            } else {
                                strings["chat_placeholder"] ?: "Type a message..."
                            },
                            fontSize = 13.sp
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloodRed,
                        unfocusedBorderColor = Color.LightGray,
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Gray
                    ),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (msgInput.isNotBlank() && !userHasUnpaidCommission) {
                            if (peerPhoneStr == "LIVE_SUPPORT") {
                                AdManager.showRewarded(context) {
                                    viewModel.sendInAppChatMessage(
                                        context = context,
                                        senderPhone = senderPhone,
                                        senderName = senderName,
                                        receiverPhone = peerPhoneStr,
                                        receiverName = peerNameStr,
                                        messageText = msgInput.trim()
                                    )
                                    msgInput = ""
                                }
                            } else {
                                viewModel.sendInAppChatMessage(
                                    context = context,
                                    senderPhone = senderPhone,
                                    senderName = senderName,
                                    receiverPhone = peerPhoneStr,
                                    receiverName = peerNameStr,
                                    messageText = msgInput.trim()
                                )
                                msgInput = ""
                            }
                        }
                    },
                    enabled = !userHasUnpaidCommission && msgInput.isNotBlank(),
                    modifier = Modifier
                        .background(if (userHasUnpaidCommission) Color.Gray else BloodRed, CircleShape)
                        .size(44.dp)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send, 
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RequestDetailScreen(viewModel: MainViewModel) {
    val req by viewModel.selectedRequest.collectAsState()
    val language by viewModel.language.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val claims by viewModel.donationClaims.collectAsState()
    val context = LocalContext.current

    if (req == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = if (language == AppLanguage.BAN) "আবেদন পাওয়া যায়নি" else "Request not found", color = Color.Gray)
        }
        return
    }

    val request = req!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MedicalBackground)
    ) {
        // Header with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier
                    .size(40.dp)
                    .background(PureWhite, CircleShape)
                    .shadow(1.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = BloodRed
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (language == AppLanguage.BAN) "রক্তের আবেদনের বিবরণ" else "Blood Request Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BloodRed
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Blood Group and Emergency Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(BloodRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = request.bloodGroup,
                            color = PureWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (request.isEmergency) {
                        Surface(
                            color = BloodRed,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BAN) "জরুরী" else "EMERGENCY",
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Patient Name
                DetailRow(
                    icon = Icons.Default.Person,
                    label = if (language == AppLanguage.BAN) "রোগীর নাম" else "Patient Name",
                    value = request.patientName
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = LightBorder)

                // Patient Gender
                DetailRow(
                    icon = if (request.patientGender == "Male") Icons.Default.Male else Icons.Default.Female,
                    label = if (language == AppLanguage.BAN) "রোগীর লিঙ্গ" else "Patient Gender",
                    value = if (request.patientGender == "Male") (Loc.strings(language)["gender_male"] ?: "Male") else (Loc.strings(language)["gender_female"] ?: "Female")
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = LightBorder)

                // Medical Condition
                DetailRow(
                    icon = Icons.Default.Info,
                    label = if (language == AppLanguage.BAN) "রোগের সমস্যা" else "Medical Condition",
                    value = request.medicalCondition.ifBlank { "Not Specified" }
                )

                if (request.bloodAmount.isNotBlank()) {
                    Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = LightBorder)
                    DetailRow(
                        icon = Icons.Default.InvertColors,
                        label = if (language == AppLanguage.BAN) "রক্তের পরিমাণ" else "Blood Amount",
                        value = request.bloodAmount
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = LightBorder)

                // Hospital
                DetailRow(
                    icon = Icons.Default.LocalHospital,
                    label = if (language == AppLanguage.BAN) "হাসপাতাল" else "Hospital",
                    value = request.hospitalName
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = LightBorder)

                // Location
                DetailRow(
                    icon = Icons.Default.LocationOn,
                    label = if (language == AppLanguage.BAN) "অবস্থান" else "Location",
                    value = "${request.upazila}, ${request.district}"
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = LightBorder)

                // Date Requested
                DetailRow(
                    icon = Icons.Default.DateRange,
                    label = if (language == AppLanguage.BAN) "আবেদনের তারিখ" else "Requested Date",
                    value = request.dateRequested
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = LightBorder)

                // Details
                Text(
                    text = if (language == AppLanguage.BAN) "অতিরিক্ত তথ্য:" else "Additional Details:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = request.details,
                    fontSize = 15.sp,
                    color = DarkText,
                    lineHeight = 24.sp
                )
            }
        }

        // Contact Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.contactNumber}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = if (language == AppLanguage.BAN) "কল করুন" else "Call Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.openChatRoom(request.contactNumber, request.patientName)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = if (language == AppLanguage.BAN) "চ্যাট" else "Chat", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            val isRequestOwner = currentUser?.phone == request.contactNumber
            val requestClaims = claims.filter { it.requestId == request.id }

            if (isRequestOwner) {
                val pendingClaims = requestClaims.filter { it.status == "Pending" }
                val acceptedClaims = requestClaims.filter { it.status == "Accepted" }
                
                if (pendingClaims.isNotEmpty() || acceptedClaims.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                        border = BorderStroke(1.dp, Color(0xFFFFCCBC))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (language == AppLanguage.BAN) "রক্তদানকারী কনফার্মেশন অনুরোধ" else "Donation Confirmation Requests",
                                fontWeight = FontWeight.Bold,
                                color = BloodRed,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            pendingClaims.forEach { claim ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = claim.donorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                                        Text(text = claim.donorPhone, fontSize = 12.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.acceptDonationClaim(claim.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(text = if (language == AppLanguage.BAN) "হ্যাঁ, রক্ত দিয়েছেন" else "Yes, Donated", fontSize = 12.sp, color = Color.White)
                                            }
                                            Button(
                                                onClick = { viewModel.rejectDonationClaim(claim.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(text = if (language == AppLanguage.BAN) "বাতিল" else "Reject", fontSize = 12.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            acceptedClaims.forEach { claim ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = claim.donorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                            Text(text = if (language == AppLanguage.BAN) "রক্তদান কনফার্ম করা হয়েছে!" else "Donation confirmed!", fontSize = 12.sp, color = Color(0xFF2E7D32))
                                        }
                                        Icon(Icons.Filled.CheckCircle, contentDescription = "Confirmed", tint = Color(0xFF2E7D32))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Potential donor view
                val myClaim = requestClaims.find { it.donorPhone == currentUser?.phone }
                if (myClaim == null) {
                    if (currentUser != null) {
                        Button(
                            onClick = {
                                viewModel.submitDonationClaim(
                                    requestId = request.id,
                                    donorPhone = currentUser!!.phone,
                                    donorName = currentUser!!.name,
                                    contactNumber = request.contactNumber
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (language == AppLanguage.BAN) "আমি রক্ত দিয়েছি" else "I have donated blood", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // User not logged in, prompt to log in to report blood donation
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            border = BorderStroke(1.dp, Color(0xFFFFE0B2))
                        ) {
                            Text(
                                text = if (language == AppLanguage.BAN) "আপনি রক্ত দিয়ে থাকলে তা কনফার্ম করতে অ্যাকাউন্ট লগইন করুন।" else "Please log in to report and confirm if you have donated blood.",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = Color(0xFFE65100),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (myClaim.status) {
                                "Accepted" -> Color(0xFFE8F5E9)
                                "Rejected" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFFFF3E0)
                            }
                        ),
                        border = BorderStroke(1.dp, when (myClaim.status) {
                            "Accepted" -> Color(0xFFA5D6A7)
                            "Rejected" -> Color(0xFFEF9A9A)
                            else -> Color(0xFFFFCC80)
                        })
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (myClaim.status) {
                                    "Accepted" -> Icons.Filled.CheckCircle
                                    "Rejected" -> Icons.Filled.Cancel
                                    else -> Icons.Filled.Info
                                },
                                contentDescription = null,
                                tint = when (myClaim.status) {
                                    "Accepted" -> Color(0xFF2E7D32)
                                    "Rejected" -> Color(0xFFC62828)
                                    else -> Color(0xFFEF6C00)
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (myClaim.status) {
                                    "Accepted" -> if (language == AppLanguage.BAN) "আপনার রক্তদান কনফার্ম করা হয়েছে! আপনার প্রোফাইলে ডোনেশন সংখ্যা যোগ হয়েছে।" else "Your donation is confirmed! It has been added to your profile count."
                                    "Rejected" -> if (language == AppLanguage.BAN) "রক্তদান কনফার্মেশন অনুরোধ বাতিল করা হয়েছে।" else "Donation confirmation request was rejected."
                                    else -> if (language == AppLanguage.BAN) "রক্তদান কনফার্মেশনের আবেদন অপেক্ষমাণ। ব্যবহারকারী গ্রহণ করলে আপনার প্রোফাইলে তথ্যটি শো করবে।" else "Donation request is pending verification. Once accepted, it will show on your profile."
                                },
                                fontSize = 14.sp,
                                color = when (myClaim.status) {
                                    "Accepted" -> Color(0xFF2E7D32)
                                    "Rejected" -> Color(0xFFC62828)
                                    else -> Color(0xFFEF6C00)
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(LightPinkRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BloodRed,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = SecondaryText)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
        }
    }
}

// --- AMBULANCE FEATURES ---

@Composable
fun AmbulanceListScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val filteredAmbulances by viewModel.filteredAmbulances.collectAsState()
    val searchDist by viewModel.searchDistrict.collectAsState()
    val searchUpz by viewModel.searchUpazila.collectAsState()
    val searchType by viewModel.searchAmbulanceType.collectAsState()
    val context = LocalContext.current
    val userSession by viewModel.currentUser.collectAsState()
    val ambulances by viewModel.ambulances.collectAsState()

    val ambulanceTypes = listOf("All", "AC", "Non-AC", "ICU")

    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedUpazila by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    val detectedCountry by viewModel.detectedCountry.collectAsState()

    val districts = remember(detectedCountry) {
        listOf("All") + when (detectedCountry) {
            "United States" -> listOf("New York", "California", "Texas")
            "India" -> listOf("Delhi", "Maharashtra", "Karnataka")
            "Saudi Arabia" -> listOf("Riyadh", "Makkah")
            "United Arab Emirates" -> listOf("Dubai", "Abu Dhabi")
            "United Kingdom" -> listOf("London", "Greater Manchester")
            else -> MockData.districts
        }
    }
    val availableUpazilas = remember(detectedCountry, searchDist) {
        if (searchDist == "All") {
            listOf("All")
        } else {
            listOf("All") + when (detectedCountry) {
                "United States" -> when (searchDist) {
                    "New York" -> listOf("Manhattan", "Queens", "Brooklyn")
                    "California" -> listOf("San Francisco", "Los Angeles", "San Jose")
                    "Texas" -> listOf("Houston", "Dallas", "Austin")
                    else -> listOf("Manhattan")
                }
                "India" -> when (searchDist) {
                    "Delhi" -> listOf("Connaught Place")
                    "Maharashtra" -> listOf("Mumbai Worli")
                    "Karnataka" -> listOf("Bangalore Indiranagar")
                    else -> listOf("Connaught Place")
                }
                "Saudi Arabia" -> when (searchDist) {
                    "Riyadh" -> listOf("Al-Olaya")
                    "Makkah" -> listOf("Jeddah Al-Hamra")
                    else -> listOf("Al-Olaya")
                }
                "United Arab Emirates" -> when (searchDist) {
                    "Dubai" -> listOf("Dubai Marina")
                    "Abu Dhabi" -> listOf("Al-Reem Island")
                    else -> listOf("Dubai Marina")
                }
                "United Kingdom" -> when (searchDist) {
                    "London" -> listOf("Westminster")
                    "Greater Manchester" -> listOf("Deansgate")
                    else -> listOf("Westminster")
                }
                else -> MockData.getUpazilasForDistrict(searchDist)
            }
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = strings["ambulance_title"] ?: "Available Ambulances",
                currentLang = language,
                onLangToggle = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateTo(AppScreen.HOME) },
                showBack = true,
                userSession = userSession,
                onProfileClick = { viewModel.navigateTo(AppScreen.USER_PROFILE) },
                onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH_DONOR) },
                viewModel = viewModel
            )
        },
        floatingActionButton = {
            val hasAmbulance = userSession != null && (
                userSession?.role == "Ambulance" || 
                ambulances.any { it.phone == userSession?.phone || it.ownerName == userSession?.name }
            )
            if (hasAmbulance) {
                FloatingActionButton(
                    onClick = {
                        AdManager.showRewarded(context) {
                            viewModel.navigateTo(AppScreen.ADD_AMBULANCE)
                        }
                    },
                    containerColor = BloodRed,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Ambulance Booking Promo & Action Buttons Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Light orange
                border = BorderStroke(1.dp, Color(0xFFFFB74D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFFE0B2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AirportShuttle,
                                contentDescription = null,
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.BAN) "জরুরি অ্যাম্বুলেন্স বুকিং সেবা" else "Urgent Ambulance Booking Service",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = if (language == AppLanguage.BAN) "অ্যাপ থেকেই সরাসরি বুক করুন এবং ড্রাইভার বা অ্যাডমিনের সাথে ট্র্যাক করুন।" else "Book an ambulance directly inside the app and track status.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.BOOK_AMBULANCE) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings["amb_booking_title"] ?: "Book Ambulance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.AMBULANCE_BOOKINGS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1)),
                            border = BorderStroke(1.dp, Color(0xFF009688))
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF00796B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings["amb_booking_history"] ?: "Booking History",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF00796B)
                            )
                        }
                    }
                }
            }

            // Filters
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Filter Ambulances" else "অ্যাম্বুলেন্স ফিল্টার",
                        style = MaterialTheme.typography.titleSmall,
                        color = BloodRed,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // District Filter
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text(text = strings["district_label"] ?: "District", fontSize = 10.sp, color = SecondaryText)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedDistrict = true }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = searchDist, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedDistrict,
                                onDismissRequest = { expandedDistrict = false }
                            ) {
                                districts.forEach { dist ->
                                    DropdownMenuItem(
                                        text = { Text(dist) },
                                        onClick = {
                                            viewModel.updateAmbulanceFilters(dist, "All", searchType)
                                            expandedDistrict = false
                                        }
                                    )
                                }
                            }
                        }

                        // Upazila Filter
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text(text = strings["upazila_label"] ?: "Upazila", fontSize = 10.sp, color = SecondaryText)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedUpazila = true }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = searchUpz, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedUpazila,
                                onDismissRequest = { expandedUpazila = false }
                            ) {
                                availableUpazilas.forEach { upz ->
                                    DropdownMenuItem(
                                        text = { Text(upz) },
                                        onClick = {
                                            viewModel.updateAmbulanceFilters(searchDist, upz, searchType)
                                            expandedUpazila = false
                                        }
                                    )
                                }
                            }
                        }

                        // Type Filter
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text(text = strings["ambulance_type"] ?: "Type", fontSize = 10.sp, color = SecondaryText)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedType = true }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = searchType, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false }
                            ) {
                                ambulanceTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            viewModel.updateAmbulanceFilters(searchDist, searchUpz, type)
                                            expandedType = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredAmbulances.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (language == AppLanguage.ENG) "No ambulances found in this area." else "এই এলাকায় কোনো অ্যাম্বুলেন্স পাওয়া যায়নি।",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecondaryText
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAmbulances) { amb ->
                        AmbulanceCard(
                            ambulance = amb,
                            strings = strings,
                            language = language,
                            onCall = {
                                AdManager.showRewarded(context) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${amb.phone}"))
                                    context.startActivity(intent)
                                }
                            },
                            onChat = {
                                AdManager.showRewarded(context) {
                                    viewModel.openChatRoom(amb.phone, amb.serviceName)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmbulanceCard(
    ambulance: Ambulance,
    strings: Map<String, String>,
    language: AppLanguage,
    onCall: () -> Unit,
    onChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE3F2FD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AirportShuttle,
                        contentDescription = null,
                        tint = Color(0xFF2196F3)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ambulance.serviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "${ambulance.ambulanceType} Ambulance",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                Surface(
                    color = if (ambulance.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (ambulance.isAvailable) (if (language == AppLanguage.BAN) "সক্রিয়" else "Active") else (if (language == AppLanguage.BAN) "অফলাইন" else "Offline"),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ambulance.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = ambulance.description,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkText.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${ambulance.upazila}, ${ambulance.district} (${ambulance.country})", fontSize = 12.sp, color = SecondaryText)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings["ambulance_call_btn"] ?: "Call Service",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onChat,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                ) {
                    Icon(Icons.Default.Forum, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BAN) "চ্যাট করুন" else "In-App Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AddAmbulanceScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current
    
    var serviceName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var ambulanceType by remember { mutableStateOf("AC") }
    var district by remember { mutableStateOf("") }
    var upazila by remember { mutableStateOf("") }

    val detectedCountryFlow by viewModel.detectedCountry.collectAsState()
    var country by remember { mutableStateOf(detectedCountryFlow) }
    var expandedCountry by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedUpazila by remember { mutableStateOf(false) }

    val countries by viewModel.customCountries.collectAsState()
    val districts = remember(country) {
        when (country) {
            "United States" -> listOf("New York", "California", "Texas")
            "India" -> listOf("Delhi", "Maharashtra", "Karnataka")
            "Saudi Arabia" -> listOf("Riyadh", "Makkah")
            "United Arab Emirates" -> listOf("Dubai", "Abu Dhabi")
            "United Kingdom" -> listOf("London", "Greater Manchester")
            else -> MockData.districts
        }
    }
    val availableUpazilas = remember(country, district) {
        when (country) {
            "United States" -> when (district) {
                "New York" -> listOf("Manhattan", "Queens", "Brooklyn")
                "California" -> listOf("San Francisco", "Los Angeles", "San Jose")
                "Texas" -> listOf("Houston", "Dallas", "Austin")
                else -> listOf("Manhattan")
            }
            "India" -> when (district) {
                "Delhi" -> listOf("Connaught Place")
                "Maharashtra" -> listOf("Mumbai Worli")
                "Karnataka" -> listOf("Bangalore Indiranagar")
                else -> listOf("Connaught Place")
            }
            "Saudi Arabia" -> when (district) {
                "Riyadh" -> listOf("Al-Olaya")
                "Makkah" -> listOf("Jeddah Al-Hamra")
                else -> listOf("Al-Olaya")
            }
            "United Arab Emirates" -> when (district) {
                "Dubai" -> listOf("Dubai Marina")
                "Abu Dhabi" -> listOf("Al-Reem Island")
                else -> listOf("Dubai Marina")
            }
            "United Kingdom" -> when (district) {
                "London" -> listOf("Westminster")
                "Greater Manchester" -> listOf("Deansgate")
                else -> listOf("Westminster")
            }
            else -> MockData.getUpazilasForDistrict(district)
        }
    }

    androidx.compose.runtime.LaunchedEffect(detectedCountryFlow) {
        if (country == "Bangladesh" || country == "" || country == "International" || country == "United States") {
            country = detectedCountryFlow
            district = ""
            upazila = ""
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = strings["ambulance_add_title"] ?: "Add Ambulance",
                currentLang = language,
                onLangToggle = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateTo(AppScreen.AMBULANCE_LIST) },
                showBack = true,
                userSession = viewModel.currentUser.collectAsState().value,
                onProfileClick = { viewModel.navigateTo(AppScreen.USER_PROFILE) },
                onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH_DONOR) },
                viewModel = viewModel
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (language == AppLanguage.ENG) "Register your ambulance service to reach more patients." else "অ্যাম্বুলেন্স সার্ভিস নিবন্ধন করে জীবন বাঁচাতে সহায়তা করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
            
            OutlinedTextField(
                value = serviceName,
                onValueChange = { serviceName = it },
                label = { Text(strings["ambulance_service_name"] ?: "Service Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text(strings["ambulance_owner"] ?: "Owner Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(strings["phone_label"] ?: "Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            // Type Selection
            Column {
                Text(text = strings["ambulance_type"] ?: "Ambulance Type", style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("AC", "Non-AC", "ICU").forEach { type ->
                        val isSelected = ambulanceType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { ambulanceType = type },
                            color = if (isSelected) Color(0xFFE3F2FD) else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF2196F3) else LightBorder)
                        ) {
                            Text(
                                text = type,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF1565C0) else DarkText
                            )
                        }
                    }
                }
            }
            
            // Country Selection for Ambulance
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = country,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = DarkText,
                        disabledBorderColor = LightBorder,
                        disabledLabelColor = SecondaryText,
                        disabledLeadingIconColor = BloodRed,
                        disabledTrailingIconColor = SecondaryText,
                        disabledContainerColor = Color.White
                    ),
                    label = { Text(if (language == AppLanguage.BAN) "দেশ (Country)" else "Country (দেশ)") },
                    placeholder = { Text("e.g. Bangladesh") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "down") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = "Country") }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expandedCountry = true }
                )
                DropdownMenu(
                    expanded = expandedCountry,
                    onDismissRequest = { expandedCountry = false }
                ) {
                    countries.forEach { (ctyName, ctyCode) ->
                        val flag = try {
                            val firstChar = Character.codePointAt(ctyCode.uppercase(), 0) - 0x41 + 0x1F1E6
                            val secondChar = Character.codePointAt(ctyCode.uppercase(), 1) - 0x41 + 0x1F1E6
                            String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
                        } catch (e: Exception) {
                            "🌐"
                        }
                        DropdownMenuItem(
                            text = { Text("$flag $ctyName", fontSize = 14.sp) },
                            onClick = {
                                country = ctyName
                                district = ""
                                upazila = ""
                                expandedCountry = false
                            }
                        )
                    }
                }
            }

            val isBD = country.equals("Bangladesh", ignoreCase = true)

            // Freeform location inputs for all countries
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text(if (isBD) (strings["district_label"] ?: "District") else (strings["city_state_label"] ?: "City / State")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = upazila,
                        onValueChange = { upazila = it },
                        label = { Text(if (isBD) (strings["upazila_label"] ?: "Upazila") else (if (language == AppLanguage.BAN) "অঞ্চল" else "Region")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings["ambulance_desc"] ?: "Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (serviceName.isBlank() || phone.isBlank()) {
                        android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        AdManager.showRewarded(context) {
                            viewModel.ambServiceName = serviceName
                            viewModel.ambOwnerName = ownerName
                            viewModel.ambPhone = phone
                            viewModel.ambDescription = description
                            viewModel.ambType = ambulanceType
                            viewModel.ambDistrict = district
                            viewModel.ambUpazila = upazila
                            viewModel.ambCountry = country
                            viewModel.triggerRegisterAmbulance()
                            android.widget.Toast.makeText(context, strings["msg_ambulance_added"] ?: "Added!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
            ) {
                Text(text = strings["btn_register_ambulance"] ?: "Register", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BookAmbulanceScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current

    var patientName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var pickupAddress by remember { mutableStateOf("") }
    var destinationAddress by remember { mutableStateOf("") }
    var ambulanceType by remember { mutableStateOf("AC") }
    var urgencyLevel by remember { mutableStateOf("Emergency") }
    var notes by remember { mutableStateOf("") }
    
    // Auto-generate current or chosen date & time
    var dateStr by remember { mutableStateOf("") }
    if (dateStr.isEmpty()) {
        dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = strings["amb_booking_title"] ?: "Book Ambulance",
                currentLang = language,
                onLangToggle = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateTo(AppScreen.AMBULANCE_LIST) },
                showBack = true,
                userSession = viewModel.currentUser.collectAsState().value,
                onProfileClick = { viewModel.navigateTo(AppScreen.USER_PROFILE) },
                onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH_DONOR) },
                viewModel = viewModel
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (language == AppLanguage.ENG) "Fill in the details to request an ambulance instantly." else "তাৎক্ষণিকভাবে অ্যাম্বুলেন্স বুকিং করতে নিচের তথ্যগুলো পূরণ করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )

            OutlinedTextField(
                value = patientName,
                onValueChange = { patientName = it },
                label = { Text(strings["amb_patient_name"] ?: "Patient Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text(strings["amb_contact_phone"] ?: "Contact Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) }
            )

            OutlinedTextField(
                value = pickupAddress,
                onValueChange = { pickupAddress = it },
                label = { Text(strings["amb_pickup"] ?: "Pickup Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
            )

            OutlinedTextField(
                value = destinationAddress,
                onValueChange = { destinationAddress = it },
                label = { Text(strings["amb_destination"] ?: "Destination/Hospital") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) }
            )

            // Ambulance Type Selector
            Column {
                Text(
                    text = strings["ambulance_type"] ?: "Ambulance Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("AC", "Non-AC", "ICU", "Freezer").forEach { type ->
                        val isSelected = ambulanceType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { ambulanceType = type },
                            color = if (isSelected) Color(0xFFFFF3E0) else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFB74D) else LightBorder)
                        ) {
                            Text(
                                text = type,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFE65100) else DarkText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Urgency Selector
            Column {
                Text(
                    text = strings["amb_urgency"] ?: "Urgency",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Emergency", "General").forEach { urg ->
                        val isSelected = urgencyLevel == urg
                        val label = if (urg == "Emergency") (strings["amb_urgency_emergency"] ?: "Emergency") else (strings["amb_urgency_general"] ?: "General")
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { urgencyLevel = urg },
                            color = if (isSelected) {
                                if (urg == "Emergency") Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            } else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) {
                                if (urg == "Emergency") Color(0xFFEF5350) else Color(0xFF66BB6A)
                            } else LightBorder)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    if (urg == "Emergency") Color(0xFFC62828) else Color(0xFF2E7D32)
                                } else DarkText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = dateStr,
                onValueChange = { dateStr = it },
                label = { Text(if (language == AppLanguage.BAN) "তারিখ ও সময়" else "Date & Time") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(if (language == AppLanguage.BAN) "অতিরিক্ত নোট (ঐচ্ছিক)" else "Additional Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (patientName.isBlank() || contactPhone.isBlank() || pickupAddress.isBlank() || destinationAddress.isBlank()) {
                        android.widget.Toast.makeText(context, if (language == AppLanguage.BAN) "অনুগ্রহ করে সব তারকা চিহ্নিত ক্ষেত্র পূরণ করুন" else "Please fill all required fields", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        AdManager.showRewarded(context) {
                            viewModel.triggerSubmitAmbulanceBooking(
                                patientName = patientName,
                                contactPhone = contactPhone,
                                pickupAddress = pickupAddress,
                                destinationAddress = destinationAddress,
                                ambulanceType = ambulanceType,
                                urgencyLevel = urgencyLevel,
                                dateTime = dateStr,
                                notes = notes
                            )
                            android.widget.Toast.makeText(context, strings["amb_booking_success"] ?: "Booking request submitted!", android.widget.Toast.LENGTH_LONG).show()
                            viewModel.navigateTo(AppScreen.AMBULANCE_BOOKINGS)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = strings["amb_submit_booking"] ?: "Confirm Booking Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AmbulanceBookingsScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val bookings by viewModel.ambulanceBookings.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = strings["amb_booking_history"] ?: "Booking History",
                currentLang = language,
                onLangToggle = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateTo(AppScreen.AMBULANCE_LIST) },
                showBack = true,
                userSession = viewModel.currentUser.collectAsState().value,
                onProfileClick = { viewModel.navigateTo(AppScreen.USER_PROFILE) },
                onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH_DONOR) },
                viewModel = viewModel
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (isAdminMode) {
                val completedBookings = bookings.filter { it.status == "Completed" }
                val totalFareEarned = completedBookings.sumOf { it.fare }
                val totalCommissionEarned = completedBookings.sumOf { it.commission }

                Surface(
                    color = Color(0xFFE0F2F1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00796B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings["amb_admin_manage"] ?: "Manage Ambulance Bookings",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00796B),
                                fontSize = 15.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (language == AppLanguage.BAN) "মোট সম্পন্ন ভাড়া" else "Total Fare Completed",
                                        fontSize = 11.sp,
                                        color = SecondaryText
                                    )
                                    Text(
                                        text = "${totalFareEarned.toInt()} BDT",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkText
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = strings["amb_commission_label"] ?: "Commission (5%)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "${totalCommissionEarned.toInt()} BDT",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (bookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings["amb_no_bookings"] ?: "No ambulance bookings found.",
                            color = SecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings) { booking ->
                        BookingCard(
                            booking = booking,
                            strings = strings,
                            language = language,
                            isAdmin = isAdminMode,
                            viewModel = viewModel,
                            onUpdateStatus = { status, name, phone, notes, fare ->
                                viewModel.triggerUpdateBookingStatus(booking.id, status, name, phone, notes, fare)
                                android.widget.Toast.makeText(context, if (language == AppLanguage.BAN) "বুকিং আপডেট সফল হয়েছে!" else "Booking status updated!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onPayCommission = { method, txnId, phone ->
                                viewModel.triggerPayBookingCommission(booking.id, method, txnId, phone)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: AmbulanceBooking,
    strings: Map<String, String>,
    language: AppLanguage,
    isAdmin: Boolean,
    viewModel: MainViewModel,
    onUpdateStatus: (String, String?, String?, String?, Double?) -> Unit,
    onPayCommission: (String, String, String) -> Unit
) {
    var showAdminDialog by remember { mutableStateOf(false) }
    var showPaymentGatewayDialog by remember { mutableStateOf(false) }
    
    val statusColor = when (booking.status) {
        "Pending" -> Color(0xFFFF9800)
        "Confirmed" -> Color(0xFF2196F3)
        "On the Way" -> Color(0xFF009688)
        "Completed" -> Color(0xFF4CAF50)
        "Cancelled" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    val statusText = when (booking.status) {
        "Pending" -> strings["amb_status_pending"] ?: "Pending"
        "Confirmed" -> strings["amb_status_confirmed"] ?: "Confirmed"
        "On the Way" -> strings["amb_status_onway"] ?: "On the Way"
        "Completed" -> strings["amb_status_completed"] ?: "Completed"
        "Cancelled" -> strings["amb_status_cancelled"] ?: "Cancelled"
        else -> booking.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFFEBEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AirportShuttle, contentDescription = null, tint = BloodRed, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = booking.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightBorder)

            // Details list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BookingDetailRow(icon = Icons.Default.Call, label = strings["amb_contact_phone"] ?: "Contact", value = booking.contactPhone)
                BookingDetailRow(icon = Icons.Default.LocationOn, label = strings["amb_pickup"] ?: "Pickup", value = booking.pickupAddress)
                BookingDetailRow(icon = Icons.Default.LocalHospital, label = strings["amb_destination"] ?: "Destination", value = booking.destinationAddress)
                BookingDetailRow(icon = Icons.Default.AirportShuttle, label = strings["ambulance_type"] ?: "Type", value = "${booking.ambulanceType} (${if (booking.urgencyLevel == "Emergency") (strings["amb_urgency_emergency"] ?: "Emergency") else (strings["amb_urgency_general"] ?: "General")})")
                BookingDetailRow(icon = Icons.Default.AccessTime, label = if (language == AppLanguage.BAN) "নির্ধারিত সময়" else "Scheduled Time", value = booking.dateTime)
                if (booking.notes.isNotBlank()) {
                    BookingDetailRow(icon = Icons.Default.Notes, label = if (language == AppLanguage.BAN) "নোট" else "Notes", value = booking.notes)
                }
                if (booking.fare > 0.0) {
                    BookingDetailRow(
                        icon = Icons.Default.Payments,
                        label = strings["amb_fare_label"] ?: "Ambulance Fare (BDT)",
                        value = "${booking.fare.toInt()} BDT"
                    )
                    BookingDetailRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = strings["amb_commission_label"] ?: "Commission (5%)",
                        value = "${booking.commission.toInt()} BDT"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (booking.isCommissionPaid) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF81C784))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = strings["amb_commission_paid"] ?: "Commission Paid Online",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "${strings["amb_payment_method"] ?: "Method"}: ${booking.paymentMethod} | ${strings["amb_txn_id"] ?: "TxnID"}: ${booking.paymentTxnId}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                    if (!booking.paymentPhone.isNullOrBlank()) {
                                        Text(
                                            text = "${if (language == AppLanguage.BAN) "পরিশোধিত নম্বর" else "Sender Mobile"}: ${booking.paymentPhone}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings["amb_commission_unpaid"] ?: "Commission Unpaid",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFE65100)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (language == AppLanguage.BAN) 
                                        "অ্যাপ থেকে অনলাইনে বিকাশ, নগদ বা রকেট গেটওয়ের মাধ্যমে সরাসরি ৫% কমিশন পরিশোধ করুন।" 
                                        else "Pay the 5% commission instantly online using bKash, Nagad or Rocket.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showPaymentGatewayDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD12053)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings["amb_pay_now_btn"] ?: "Pay Now (5% Commission)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val bookingsList = viewModel.ambulanceBookings.collectAsState().value
            val currentUser = viewModel.currentUser.collectAsState().value

            val assignedAmbulancePhone = booking.assignedAmbulancePhone
            val assignedAmbulanceHasUnpaid = if (!assignedAmbulancePhone.isNullOrBlank()) {
                bookingsList.any { 
                    it.assignedAmbulancePhone == assignedAmbulancePhone && 
                    it.status == "Completed" && 
                    !it.isCommissionPaid && 
                    it.fare > 0.0 
                }
            } else {
                false
            }

            val isUserPatient = currentUser?.phone == booking.contactPhone

            // Assigned Ambulance/Driver Info
            if (!assignedAmbulancePhone.isNullOrBlank() || !booking.assignedAmbulanceName.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = strings["amb_assign_info"] ?: "Assigned Driver/Ambulance",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val context = androidx.compose.ui.platform.LocalContext.current

                        if (isUserPatient && assignedAmbulanceHasUnpaid) {
                            // Ambulance has unpaid commission, so hide details from the patient!
                            Text(
                                text = if (language == AppLanguage.BAN) {
                                    "বকেয়া কমিশন অপরিশোধিত থাকায় চালকের তথ্য এবং ফোন নম্বর দেখতে পারবেন না। তবে আপনি চ্যাট মেসেজ করতে পারবেন।"
                                } else {
                                    "Driver details and phone number are locked due to outstanding commission. You can only send messages."
                                },
                                fontSize = 12.sp,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Only show Chat button, NO Call button!
                            Button(
                                onClick = {
                                    viewModel.openChatRoom(
                                        peerPhone = assignedAmbulancePhone ?: "",
                                        peerName = booking.assignedAmbulanceName ?: "Driver"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Forum, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.BAN) "চালকের সাথে চ্যাট করুন" else "Chat with Driver",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            // Show all details, show both Chat and Call buttons!
                            if (!booking.assignedAmbulanceName.isNullOrBlank()) {
                                Text(text = "${if (language == AppLanguage.BAN) "চালক/এজেন্সি" else "Driver/Agency"}: ${booking.assignedAmbulanceName}", fontSize = 12.sp, color = DarkText)
                            }
                            if (!assignedAmbulancePhone.isNullOrBlank()) {
                                Text(text = "${if (language == AppLanguage.BAN) "ফোন নম্বর" else "Phone"}: $assignedAmbulancePhone", fontSize = 12.sp, color = DarkText)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Chat button
                                if (!assignedAmbulancePhone.isNullOrBlank()) {
                                    Button(
                                        onClick = {
                                            viewModel.openChatRoom(
                                                peerPhone = assignedAmbulancePhone ?: "",
                                                peerName = booking.assignedAmbulanceName ?: "Driver"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Forum, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.BAN) "চ্যাট করুন" else "Chat",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Call button
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$assignedAmbulancePhone"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Cannot place call", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.BAN) "কল করুন" else "Call",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showAdminDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (language == AppLanguage.BAN) "বুকিং আপডেট করুন" else "Update Booking", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    if (showAdminDialog) {
        var tempStatus by remember { mutableStateOf(booking.status) }
        var tempDriverName by remember { mutableStateOf(booking.assignedAmbulanceName ?: "") }
        var tempDriverPhone by remember { mutableStateOf(booking.assignedAmbulancePhone ?: "") }
        var tempNotes by remember { mutableStateOf(booking.notes) }
        var tempFare by remember { mutableStateOf(if (booking.fare > 0.0) booking.fare.toInt().toString() else "") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = { Text(text = strings["amb_booking_details"] ?: "Booking Details") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(text = "Patient: ${booking.patientName}", fontWeight = FontWeight.Bold)
                    
                    // Status Selection Row
                    Text(text = "Select Status:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Pending", "Confirmed", "On the Way", "Completed", "Cancelled").forEach { st ->
                            val isSelected = tempStatus == st
                            val stColor = when (st) {
                                "Pending" -> Color(0xFFFF9800)
                                "Confirmed" -> Color(0xFF2196F3)
                                "On the Way" -> Color(0xFF009688)
                                "Completed" -> Color(0xFF4CAF50)
                                "Cancelled" -> Color(0xFFF44336)
                                else -> Color.Gray
                            }
                            Surface(
                                modifier = Modifier.clickable { tempStatus = st },
                                color = if (isSelected) stColor else Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) stColor else LightBorder)
                            ) {
                                Text(
                                    text = st,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else DarkText
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tempDriverName,
                        onValueChange = { tempDriverName = it },
                        label = { Text("Driver/Agency Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempDriverPhone,
                        onValueChange = { tempDriverPhone = it },
                        label = { Text("Driver Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempFare,
                        onValueChange = { tempFare = it },
                        label = { Text(strings["amb_fare_label"] ?: "Ambulance Fare (BDT)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    val enteredFare = tempFare.toDoubleOrNull() ?: 0.0
                    if (enteredFare > 0.0) {
                        val calcCommission = enteredFare * 0.05
                        Surface(
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${strings["amb_commission_label"] ?: "Commission (5%)"}: ${calcCommission.toInt()} BDT",
                                modifier = Modifier.padding(10.dp),
                                color = Color(0xFFE65100),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = tempNotes,
                        onValueChange = { tempNotes = it },
                        label = { Text("Admin Notes / Equipment Info") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalFare = tempFare.toDoubleOrNull() ?: 0.0
                        onUpdateStatus(tempStatus, tempDriverName, tempDriverPhone, tempNotes, finalFare)
                        showAdminDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPaymentGatewayDialog) {
        var step by remember { mutableStateOf(1) } // 1: Method & Wallet, 2: OTP, 3: PIN, 4: Success
        var selectedMethod by remember { mutableStateOf("bKash") }
        var walletNumber by remember { mutableStateOf("") }
        var otpCode by remember { mutableStateOf("") }
        var pinCode by remember { mutableStateOf("") }
        var generatedTxnId by remember { mutableStateOf("") }

        val gatewayColor = when (selectedMethod) {
            "bKash" -> Color(0xFFD12053)
            "Nagad" -> Color(0xFFF04A23)
            "Rocket" -> Color(0xFF8C3494)
            "Google Play" -> Color(0xFF01875F)
            else -> Color(0xFFD12053)
        }

        val context = androidx.compose.ui.platform.LocalContext.current

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (step != 4) showPaymentGatewayDialog = false },
            title = null,
            text = {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Gateway Header / Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(gatewayColor, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (step == 4) "Payment Success" else "$selectedMethod Online Gateway",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (step == 1) {
                            // Amount Box
                            Surface(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BAN) "পরিশোধের পরিমাণ" else "Amount to Pay",
                                        fontSize = 12.sp,
                                        color = SecondaryText
                                    )
                                    Text(
                                        text = "${booking.commission.toInt()} BDT",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = gatewayColor
                                    )
                                    Text(
                                        text = if (selectedMethod == "Google Play") {
                                            if (language == AppLanguage.BAN) "গুগল প্লে সিকিউর মার্চেন্ট পেমেন্ট" else "Google Play Secure Merchant Payment"
                                        } else {
                                            if (language == AppLanguage.BAN) "মার্চেন্ট হিসাব নম্বর: ০১৭০০-০০০০০১" else "Merchant Wallet: 01700-000001"
                                        },
                                        fontSize = 11.sp,
                                        color = SecondaryText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Method Tabs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("bKash", "Nagad", "Rocket", "Google Play").forEach { m ->
                                    val isMSelected = selectedMethod == m
                                    val mColor = when (m) {
                                        "bKash" -> Color(0xFFD12053)
                                        "Nagad" -> Color(0xFFF04A23)
                                        "Rocket" -> Color(0xFF8C3494)
                                        "Google Play" -> Color(0xFF01875F)
                                        else -> Color.Gray
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { 
                                                selectedMethod = m
                                                if (m == "Google Play") {
                                                    walletNumber = "help.alifshen.ltd@gmail.com"
                                                } else {
                                                    walletNumber = ""
                                                }
                                            },
                                        color = if (isMSelected) mColor.copy(alpha = 0.12f) else Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.5.dp, if (isMSelected) mColor else LightBorder)
                                    ) {
                                        Text(
                                            text = m,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMSelected) mColor else DarkText,
                                            fontSize = 9.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (selectedMethod == "Google Play") {
                                Surface(
                                    color = Color(0xFFF1F3F4),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Color(0xFFDADCE0))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF5F6368),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "help.alifshen.ltd@gmail.com",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF3C4043)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                    contentDescription = null,
                                                    tint = Color(0xFF01875F),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (language == AppLanguage.BAN) "গুগল প্লে ব্যালেন্স" else "Google Play Balance",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF5F6368)
                                                )
                                            }
                                            Text(
                                                text = "5,000.00 BDT",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF202124)
                                            )
                                        }
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = walletNumber,
                                    onValueChange = { walletNumber = it },
                                    label = { Text(if (language == AppLanguage.BAN) "আপনার $selectedMethod নম্বর দিন" else "Enter your $selectedMethod wallet no.") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (selectedMethod == "Google Play") {
                                        generatedTxnId = "GPA." + (1000..9999).shuffled().first().toString() + "-" + (1000..9999).shuffled().first().toString() + "-" + (1000..9999).shuffled().first().toString() + "-" + (1000..9999).shuffled().first().toString()
                                        step = 4
                                    } else {
                                        if (walletNumber.length < 11) {
                                            android.widget.Toast.makeText(context, if (language == AppLanguage.BAN) "সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন" else "Enter a valid 11 digit number", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            step = 2
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = gatewayColor)
                            ) {
                                val btnText = if (selectedMethod == "Google Play") {
                                    if (language == AppLanguage.BAN) "১-ট্যাপে কিনুন (One-tap Buy)" else "One-tap Buy"
                                } else {
                                    if (language == AppLanguage.BAN) "পরবর্তী ধাপ" else "Next"
                                }
                                Text(text = btnText, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (step == 2) {
                            Text(
                                text = if (language == AppLanguage.BAN) "একটি ৬-ডিজিটের ওটিপি (OTP) আপনার নম্বরে পাঠানো হয়েছে।" else "A 6-digit verification code was sent to your wallet number.",
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = SecondaryText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text(if (language == AppLanguage.BAN) "ওটিপি (OTP) কোড দিন" else "Enter OTP Code") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (otpCode.isBlank()) {
                                        android.widget.Toast.makeText(context, "Please enter OTP code", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        step = 3
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = gatewayColor)
                            ) {
                                Text(text = if (language == AppLanguage.BAN) "যাচাই করুন" else "Verify", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (step == 3) {
                            Text(
                                text = if (language == AppLanguage.BAN) "নিরাপদ পেমেন্ট সম্পন্ন করতে আপনার গোপন পিন (PIN) নম্বরটি দিন।" else "Enter your secret transaction PIN to complete secure transaction.",
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = SecondaryText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { pinCode = it },
                                label = { Text(if (language == AppLanguage.BAN) "পিন (PIN) কোড" else "Enter PIN") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (pinCode.length < 4) {
                                        android.widget.Toast.makeText(context, "Enter valid PIN code", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Generate simulated TxnID
                                        val prefix = when (selectedMethod) {
                                            "bKash" -> "BK"
                                            "Nagad" -> "NG"
                                            "Rocket" -> "RC"
                                            else -> "TX"
                                        }
                                        generatedTxnId = prefix + (100000..999999).shuffled().first().toString() + ('A'..'Z').shuffled().take(2).joinToString("")
                                        step = 4
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = gatewayColor)
                            ) {
                                Text(text = if (language == AppLanguage.BAN) "পেমেন্ট নিশ্চিত করুন" else "Confirm Payment", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (step == 4) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (language == AppLanguage.BAN) "পেমেন্ট সফল হয়েছে!" else "Payment Successful!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF2E7D32)
                              )

                            Text(
                                text = "${booking.commission.toInt()} BDT Paid to Alif Admin Wallet",
                                fontSize = 12.sp,
                                color = SecondaryText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "TxnID: $generatedTxnId", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                    Text(text = "Method: $selectedMethod", fontSize = 12.sp, color = DarkText)
                                    Text(text = "Sender: $walletNumber", fontSize = 12.sp, color = DarkText)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onPayCommission(selectedMethod, generatedTxnId, walletNumber)
                                    showPaymentGatewayDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                val doneText = if (language == AppLanguage.BAN) "সম্পন্ন করুন" else "Done"
                                Text(text = doneText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun BookingDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = SecondaryText)
            Text(text = value, fontSize = 13.sp, color = DarkText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AmbulanceDashboardScreen(viewModel: MainViewModel) {
    val strings by viewModel.strings.collectAsState()
    val language by viewModel.language.collectAsState()
    val userSession by viewModel.currentUser.collectAsState()
    val ambulances by viewModel.ambulances.collectAsState()
    val bookings by viewModel.ambulanceBookings.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Find ambulances matching this user's profile
    val userAmbulances = userSession?.let { user ->
        ambulances.filter { it.phone == user.phone || it.ownerName == user.name }
    } ?: emptyList()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = if (language == AppLanguage.BAN) "অ্যাম্বুলেন্স ড্যাশবোর্ড" else "Ambulance Dashboard",
                currentLang = language,
                onLangToggle = { viewModel.toggleLanguage() },
                onBack = { viewModel.navigateTo(AppScreen.HOME) },
                showBack = true,
                userSession = userSession,
                onProfileClick = { viewModel.navigateTo(AppScreen.USER_PROFILE) },
                onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH_DONOR) },
                viewModel = viewModel
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9FAFB))
        ) {
            if (userSession == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (language == AppLanguage.BAN) "অনুগ্রহ করে ড্যাশবোর্ড দেখতে লগইন করুন" else "Please login to view dashboard",
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
                return@Scaffold
            }

            if (userAmbulances.isEmpty()) {
                OnboardingRegisterAmbulance(language, viewModel)
            } else {
                val activeAmbulance = userAmbulances.first()
                AmbulanceDashboardContent(
                    language = language,
                    strings = strings,
                    userSession = userSession!!,
                    ambulance = activeAmbulance,
                    bookings = bookings,
                    viewModel = viewModel,
                    context = context
                )
            }
        }
    }
}

@Composable
fun OnboardingRegisterAmbulance(language: AppLanguage, viewModel: MainViewModel) {
    val isBn = language == AppLanguage.BAN
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = LightPinkRed.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.AirportShuttle,
                    contentDescription = null,
                    tint = DarkBloodRed,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isBn) "অ্যাম্বুলেন্স পার্টনার হাব" else "Ambulance Partner Hub",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isBn) {
                "আপনার অ্যাম্বুলেন্সটি নিবন্ধিত করে আমাদের সক্রিয় অংশীদার হোন। আশেপাশের রোগীদের থেকে রিয়েল-টাইম বুকিংয়ের অনুরোধ পান এবং আপনার সেবা ছড়িয়ে দিন!"
            } else {
                "Become an active partner by registering your ambulance. Get real-time booking requests from patients nearby and grow your service!"
            },
            fontSize = 14.sp,
            color = SecondaryText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.navigateTo(AppScreen.ADD_AMBULANCE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBn) "আপনার অ্যাম্বুলেন্স যুক্ত করুন" else "Add Your Ambulance Now",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun AmbulanceDashboardContent(
    language: AppLanguage,
    strings: Map<String, String>,
    userSession: BloodDonor,
    ambulance: Ambulance,
    bookings: List<AmbulanceBooking>,
    viewModel: MainViewModel,
    context: android.content.Context
) {
    val isBn = language == AppLanguage.BAN

    // Filter bookings relevant to this ambulance / general pending ones
    val myBookings = bookings.filter { it.assignedAmbulancePhone == userSession.phone }
    val pendingBookings = bookings.filter { it.status == "Pending" }
    val hasUnpaidCommission = myBookings.any { it.status == "Completed" && !it.isCommissionPaid && it.fare > 0.0 }

    // Stats
    val totalTrips = myBookings.size
    val activeTrips = myBookings.filter { it.status in listOf("Confirmed", "On the Way") }.size
    val completedTrips = myBookings.filter { it.status == "Completed" }.size
    val totalEarnings = myBookings.filter { it.status == "Completed" }.sumOf { it.fare }

    var selectedTab by remember { mutableStateOf(0) } // 0: Incoming (Pending), 1: Active, 2: History

    // Dialog state for updating status
    var showUpdateStatusDialog by remember { mutableStateOf<AmbulanceBooking?>(null) }
    var showPaymentDialog by remember { mutableStateOf<AmbulanceBooking?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Ambulance Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ambulance.serviceName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "${ambulance.ambulanceType} Ambulance | ${ambulance.upazila}, ${ambulance.district}",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }

                    // Availability Status Switch
                    Surface(
                        color = if (ambulance.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEAEA),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.triggerToggleAmbulanceAvailability(ambulance.id) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (ambulance.isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (ambulance.isAvailable) {
                                    if (isBn) "সচল" else "Available"
                                } else {
                                    if (isBn) "ব্যস্ত" else "Busy"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ambulance.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFEEEEEE)))
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Phone, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = ambulance.phone, fontSize = 13.sp, color = DarkText)
                }
                
                if (ambulance.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ambulance.description,
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsGridCard(
                label = if (isBn) "মোট ট্রিপ" else "Total Trips",
                value = "$totalTrips",
                color = Color(0xFFE3F2FD),
                textColor = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            StatsGridCard(
                label = if (isBn) "চলমান ট্রিপ" else "Active Trips",
                value = "$activeTrips",
                color = Color(0xFFFFF3E0),
                textColor = Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )
            StatsGridCard(
                label = if (isBn) "সম্পন্ন ভাড়া" else "Earnings",
                value = "${totalEarnings.toInt()} ৳",
                color = Color(0xFFE8F5E9),
                textColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hasUnpaidCommission) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                border = BorderStroke(1.dp, Color(0xFFEF5350))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "বকেয়া কমিশন পেমেন্ট সতর্কতা!" else "Outstanding Commission Warning!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) {
                                "আপনার একটি সম্পন্ন ট্রিপের ৫% বকেয়া কমিশন পরিশোধ করা হয়নি। কমিশন পরিশোধ না করা পর্যন্ত আপনি নতুন ট্রিপ বুকিং গ্রহণ করতে পারবেন না। অনুগ্রহ করে নিচে 'ইতিহাস' ট্যাব থেকে বকেয়াটি পরিশোধ করুন।"
                            } else {
                                "You have a completed trip with unpaid 5% commission. You cannot accept new bookings until the commission is paid. Please pay from the 'History' tab."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFFB71C1C),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Booking Section Headers
        Text(
            text = if (isBn) "ভাড়া অনুরোধ ও ব্যবস্থাপনা" else "Trip Bookings & Management",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf(
                if (isBn) "অনুরোধ (${pendingBookings.size})" else "Requests (${pendingBookings.size})",
                if (isBn) "চলমান ($activeTrips)" else "Active ($activeTrips)",
                if (isBn) "ইতিহাস ($completedTrips)" else "History ($completedTrips)"
            )
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) BloodRed else DarkText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Incoming/Pending Bookings
                if (pendingBookings.isEmpty()) {
                    EmptyTripState(
                        message = if (isBn) "কোন নতুন বুকিং অনুরোধ পাওয়া যায়নি" else "No new booking requests found",
                        icon = Icons.Filled.AirportShuttle
                    )
                } else {
                    pendingBookings.forEach { booking ->
                        PendingBookingCard(
                            booking = booking,
                            language = language,
                            isBlocked = hasUnpaidCommission,
                            onAccept = {
                                viewModel.triggerUpdateBookingStatus(
                                    bookingId = booking.id,
                                    newStatus = "Confirmed",
                                    assignedName = ambulance.serviceName,
                                    assignedPhone = userSession.phone,
                                    fare = booking.fare
                                )
                                viewModel.sendSystemNotification(
                                    titleEn = "Booking Accepted!",
                                    titleBn = "ভাড়া গ্রহণ করা হয়েছে!",
                                    messageEn = "Ambulance driver ${ambulance.serviceName} accepted your trip to ${booking.destinationAddress}.",
                                    messageBn = "অ্যাম্বুলেন্স চালক ${ambulance.serviceName} আপনার ট্রিপটি গ্রহণ করেছেন।"
                                )
                                android.widget.Toast.makeText(context, if (isBn) "ভাড়া সফলভাবে গ্রহণ করা হয়েছে!" else "Trip successfully accepted!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            1 -> {
                // Active Trips
                val activeList = myBookings.filter { it.status in listOf("Confirmed", "On the Way") }
                if (activeList.isEmpty()) {
                    EmptyTripState(
                        message = if (isBn) "আপনার কোন চলমান ট্রিপ নেই" else "You have no active trips",
                        icon = Icons.Filled.DoneAll
                    )
                } else {
                    activeList.forEach { booking ->
                        ActiveTripCard(
                            booking = booking,
                            language = language,
                            onCall = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${booking.contactPhone}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Cannot place call", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onChat = {
                                viewModel.openChatRoom(peerPhone = booking.contactPhone, peerName = booking.patientName)
                            },
                            onUpdateStatus = {
                                showUpdateStatusDialog = booking
                            },
                            onPayCommission = {
                                showPaymentDialog = booking
                            }
                        )
                    }
                }
            }
            2 -> {
                // Past History Trips
                val pastList = myBookings.filter { it.status in listOf("Completed", "Cancelled") }
                if (pastList.isEmpty()) {
                    EmptyTripState(
                        message = if (isBn) "পূর্বের ট্রিপের কোন ইতিহাস পাওয়া যায়নি" else "No past trips history found",
                        icon = Icons.Filled.AirportShuttle
                    )
                } else {
                    pastList.forEach { booking ->
                        PastTripCard(
                            booking = booking,
                            language = language,
                            onPayCommission = {
                                showPaymentDialog = booking
                            }
                        )
                    }
                }
            }
        }
    }

    // Status Update Dialog
    if (showUpdateStatusDialog != null) {
        val booking = showUpdateStatusDialog!!
        var tempStatus by remember { mutableStateOf(booking.status) }
        var tempFare by remember { mutableStateOf(if (booking.fare > 0.0) booking.fare.toInt().toString() else "") }
        var notesInput by remember { mutableStateOf(booking.notes) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUpdateStatusDialog = null },
            title = {
                Text(
                    text = if (isBn) "ট্রিপ স্ট্যাটাস আপডেট" else "Update Trip Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "${if (isBn) "রোগী" else "Patient"}: ${booking.patientName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = if (isBn) "ধাপ নির্বাচন করুন:" else "Select Trip Stage:", fontSize = 12.sp, color = SecondaryText)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Confirmed", "On the Way", "Completed", "Cancelled").forEach { st ->
                            val isSelected = tempStatus == st
                            val color = when (st) {
                                "Confirmed" -> Color(0xFF2196F3)
                                "On the Way" -> Color(0xFF009688)
                                "Completed" -> Color(0xFF4CAF50)
                                "Cancelled" -> Color(0xFFF44336)
                                else -> Color.Gray
                            }
                            Surface(
                                modifier = Modifier.clickable { tempStatus = st },
                                color = if (isSelected) color else Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) color else LightBorder)
                            ) {
                                Text(
                                    text = if (isBn) {
                                        when (st) {
                                            "Confirmed" -> "নিশ্চিত"
                                            "On the Way" -> "পথে রয়েছে"
                                            "Completed" -> "সম্পন্ন"
                                            "Cancelled" -> "বাতিল"
                                            else -> st
                                        }
                                    } else st,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else DarkText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = tempFare,
                        onValueChange = { tempFare = it },
                        label = { Text(if (isBn) "মোট ভাড়া (টাকা)" else "Total Fare (BDT)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp)
                    )

                    val finalFare = tempFare.toDoubleOrNull() ?: 0.0
                    if (finalFare > 0.0) {
                        val calcCommission = finalFare * 0.05
                        Surface(
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${if (isBn) "সিস্টেম কমিশন (৫%)" else "Commission (5%)"}: ${calcCommission.toInt()} BDT",
                                modifier = Modifier.padding(10.dp),
                                color = Color(0xFFE65100),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text(if (isBn) "মন্তব্য / রোড নোট" else "Notes / Road Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalFareVal = tempFare.toDoubleOrNull() ?: booking.fare
                        viewModel.triggerUpdateBookingStatus(
                            bookingId = booking.id,
                            newStatus = tempStatus,
                            assignedName = booking.assignedAmbulanceName,
                            assignedPhone = booking.assignedAmbulancePhone,
                            adminNotes = notesInput,
                            fare = finalFareVal
                        )
                        viewModel.sendSystemNotification(
                            titleEn = "Trip Status Updated",
                            titleBn = "ট্রিপ স্ট্যাটাস আপডেট",
                            messageEn = "Your ambulance trip status has been updated to $tempStatus.",
                            messageBn = "আপনার অ্যাম্বুলেন্স ট্রিপের অবস্থা পরিবর্তন করে $tempStatus করা হয়েছে।"
                        )
                        showUpdateStatusDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                ) {
                    Text(if (isBn) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateStatusDialog = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Payment Dialog Flow
    if (showPaymentDialog != null) {
        val booking = showPaymentDialog!!
        var step by remember { mutableStateOf(1) } // 1: Method & Wallet, 2: OTP, 3: PIN, 4: Success
        var selectedMethod by remember { mutableStateOf("bKash") }
        var walletNumber by remember { mutableStateOf("") }
        var otpCode by remember { mutableStateOf("") }
        var pinCode by remember { mutableStateOf("") }
        var generatedTxnId by remember { mutableStateOf("") }

        val gatewayColor = when (selectedMethod) {
            "bKash" -> Color(0xFFD12053)
            "Nagad" -> Color(0xFFF04A23)
            "Rocket" -> Color(0xFF8C3494)
            "Google Play" -> Color(0xFF01875F)
            else -> Color(0xFFD12053)
        }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (step != 4) showPaymentDialog = null },
            title = null,
            text = {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Gateway Header / Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(gatewayColor, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (step == 4) {
                                    if (isBn) "পেমেন্ট সফল" else "Payment Success"
                                } else {
                                    "$selectedMethod Online Gateway"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (step == 1) {
                            // Amount Box
                            Surface(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isBn) "পরিশোধের কমিশন পরিমাণ (৫%)" else "Commission to Pay (5%)",
                                        fontSize = 11.sp,
                                        color = SecondaryText
                                    )
                                    Text(
                                        text = "${(booking.fare * 0.05).toInt()} BDT",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = gatewayColor
                                    )
                                    Text(
                                        text = if (isBn) "আলিফ মার্চেন্ট হিসাব নম্বর: ০১৭০০-০০০০০১" else "Merchant Wallet: 01700-000001",
                                        fontSize = 11.sp,
                                        color = SecondaryText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Methods List
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("bKash", "Nagad", "Rocket").forEach { m ->
                                    val isMSelected = selectedMethod == m
                                    val mColor = when (m) {
                                        "bKash" -> Color(0xFFD12053)
                                        "Nagad" -> Color(0xFFF04A23)
                                        "Rocket" -> Color(0xFF8C3494)
                                        else -> Color.Gray
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedMethod = m },
                                        color = if (isMSelected) mColor.copy(alpha = 0.12f) else Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.5.dp, if (isMSelected) mColor else LightBorder)
                                    ) {
                                        Text(
                                            text = m,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMSelected) mColor else DarkText,
                                            fontSize = 11.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = walletNumber,
                                onValueChange = { walletNumber = it },
                                label = { Text(if (isBn) "$selectedMethod পার্সোনাল নম্বর" else "$selectedMethod Wallet Number") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (walletNumber.length < 11) {
                                        android.widget.Toast.makeText(context, "Enter a valid wallet phone number", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        step = 2
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = gatewayColor)
                            ) {
                                Text(text = if (isBn) "পরবর্তী ধাপ" else "Next Step", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (step == 2) {
                            Text(
                                text = "A simulated 6-digit OTP code has been sent to your wallet number $walletNumber.",
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = SecondaryText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text("Enter OTP Code (Simulated: 123456)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    step = 3
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = gatewayColor)
                            ) {
                                Text(text = "Verify OTP", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (step == 3) {
                            Text(
                                text = "Enter your secret transaction PIN to complete the secure payment.",
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = SecondaryText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { pinCode = it },
                                label = { Text("Enter secret PIN") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val prefix = when (selectedMethod) {
                                        "bKash" -> "BK"
                                        "Nagad" -> "NG"
                                        "Rocket" -> "RC"
                                        else -> "TX"
                                    }
                                    generatedTxnId = prefix + (100000..999999).shuffled().first().toString() + ('A'..'Z').shuffled().take(2).joinToString("")
                                    step = 4
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = gatewayColor)
                            ) {
                                Text(text = "Confirm Secure Payment", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (step == 4) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(54.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (isBn) "পেমেন্ট সফল হয়েছে!" else "Payment Successful!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF2E7D32)
                            )

                            Text(
                                text = "TxnID: $generatedTxnId",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.triggerPayBookingCommission(
                                        bookingId = booking.id,
                                        method = selectedMethod,
                                        txnId = generatedTxnId,
                                        phone = walletNumber
                                    )
                                    showPaymentDialog = null
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text(text = if (isBn) "ড্যাশবোর্ডে ফিরে যান" else "Back to Dashboard", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun StatsGridCard(
    label: String,
    value: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyTripState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = SecondaryText.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = SecondaryText, fontSize = 13.sp)
    }
}

@Composable
fun PendingBookingCard(
    booking: AmbulanceBooking,
    language: AppLanguage,
    isBlocked: Boolean = false,
    onAccept: () -> Unit
) {
    val isBn = language == AppLanguage.BAN
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.patientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBn) "অপেক্ষমান" else "Pending",
                        fontSize = 10.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BookingLocationRow(
                from = booking.pickupAddress,
                to = booking.destinationAddress,
                language = language
            )

            Spacer(modifier = Modifier.height(10.dp))
            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFF0F0F0)))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = if (isBn) "ভাড়া (BDT)" else "Estimated Fare", fontSize = 11.sp, color = SecondaryText)
                    Text(text = "${booking.fare.toInt()} BDT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BloodRed)
                }

                Button(
                    onClick = onAccept,
                    enabled = !isBlocked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBlocked) Color.Gray else Color(0xFF2E7D32),
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isBlocked) Icons.Default.Lock else Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isBlocked) Color.DarkGray else Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBlocked) {
                            if (isBn) "সীমাবদ্ধ (বকেয়া কমিশন)" else "Blocked (Due Commission)"
                        } else {
                            if (isBn) "ভাড়া গ্রহণ করুন" else "Accept Booking"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBlocked) Color.DarkGray else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveTripCard(
    booking: AmbulanceBooking,
    language: AppLanguage,
    onCall: () -> Unit,
    onChat: () -> Unit,
    onUpdateStatus: () -> Unit,
    onPayCommission: () -> Unit
) {
    val isBn = language == AppLanguage.BAN

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BloodRed.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.patientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Surface(
                    color = when (booking.status) {
                        "On the Way" -> Color(0xFFE0F2F1)
                        else -> Color(0xFFE3F2FD)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBn) {
                            when (booking.status) {
                                "On the Way" -> "পথে রয়েছে"
                                "Confirmed" -> "নিশ্চিত"
                                else -> booking.status
                            }
                        } else booking.status,
                        fontSize = 10.sp,
                        color = when (booking.status) {
                            "On the Way" -> Color(0xFF00796B)
                            else -> Color(0xFF1565C0)
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BookingLocationRow(
                from = booking.pickupAddress,
                to = booking.destinationAddress,
                language = language
            )

            Spacer(modifier = Modifier.height(10.dp))
            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFF0F0F0)))
            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFECEFF1), CircleShape)
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = "Call", tint = Color(0xFF37474F), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onChat,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFECEFF1), CircleShape)
                    ) {
                        Icon(Icons.Filled.Chat, contentDescription = "Chat", tint = Color(0xFF37474F), modifier = Modifier.size(16.dp))
                    }
                }

                Button(
                    onClick = onUpdateStatus,
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isBn) "অবস্থা পরিবর্তন করুন" else "Update Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PastTripCard(
    booking: AmbulanceBooking,
    language: AppLanguage,
    onPayCommission: () -> Unit
) {
    val isBn = language == AppLanguage.BAN
    val isCancelled = booking.status == "Cancelled"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.patientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Surface(
                    color = if (isCancelled) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBn) {
                            if (isCancelled) "বাতিল" else "সম্পন্ন"
                        } else booking.status,
                        fontSize = 10.sp,
                        color = if (isCancelled) Color(0xFFC62828) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BookingLocationRow(
                from = booking.pickupAddress,
                to = booking.destinationAddress,
                language = language
            )

            if (!isCancelled) {
                Spacer(modifier = Modifier.height(10.dp))
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFF0F0F0)))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = if (isBn) "গৃহীত ভাড়া" else "Fare Earned", fontSize = 11.sp, color = SecondaryText)
                        Text(text = "${booking.fare.toInt()} BDT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    if (booking.isCommissionPaid) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isBn) "কমিশন পরিশোধিত" else "Commission Paid",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onPayCommission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isBn) "৫% কমিশন পরিশোধ করুন" else "Pay 5% Commission",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingLocationRow(from: String, to: String, language: AppLanguage) {
    val isBn = language == AppLanguage.BAN
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF2196F3), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${if (isBn) "यात्रा শুরু" else "From"}: $from",
                fontSize = 12.sp,
                color = DarkText,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(BloodRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${if (isBn) "গন্তব্য" else "To"}: $to",
                fontSize = 12.sp,
                color = DarkText,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AdminSubscriptionsTab(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val plans by viewModel.subscriptionPlans.collectAsState()
    val subscriptions by viewModel.userSubscriptions.collectAsState()

    val currentBkash by viewModel.bkashNumber.collectAsState()
    val currentNagad by viewModel.nagadNumber.collectAsState()
    val currentRocket by viewModel.rocketNumber.collectAsState()
    val currentGooglePlay by viewModel.googlePlayMerchant.collectAsState()

    var editBkash by remember(currentBkash) { mutableStateOf(currentBkash) }
    var editNagad by remember(currentNagad) { mutableStateOf(currentNagad) }
    var editRocket by remember(currentRocket) { mutableStateOf(currentRocket) }
    var editGooglePlay by remember(currentGooglePlay) { mutableStateOf(currentGooglePlay) }

    var showAddPlanDialog by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<V9SubscriptionPlan?>(null) }

    var planId by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf("") }
    var descEn by remember { mutableStateOf("") }
    var descBn by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Dynamic Global Payment Gateways Config Panel for Admin
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = BloodRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Global Payment Configs" else "গ্লোবাল পেমেন্ট কনফিগারেশন",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = DarkText
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = if (language == AppLanguage.ENG) "Configure the official mobile banking numbers for BD & Google Play for outside." else "বাংলাদেশী ব্যবহারকারীদের জন্য অফিশিয়াল মোবাইল ব্যাংকিং এবং বাইরের জন্য গুগল প্লে সেট করুন।",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = editBkash,
                    onValueChange = { editBkash = it },
                    label = { Text(if (language == AppLanguage.ENG) "bKash Number" else "বিকাশ নম্বর") },
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFFE91E63)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = editNagad,
                    onValueChange = { editNagad = it },
                    label = { Text(if (language == AppLanguage.ENG) "Nagad Number" else "নগদ নম্বর") },
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFFFF9800)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = editRocket,
                    onValueChange = { editRocket = it },
                    label = { Text(if (language == AppLanguage.ENG) "Rocket Number" else "রকেট নম্বর") },
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF673AB7)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = editGooglePlay,
                    onValueChange = { editGooglePlay = it },
                    label = { Text(if (language == AppLanguage.ENG) "Google Play Billing Merchant ID" else "গুগল প্লে বিলিং মার্চেন্ট আইডি") },
                    leadingIcon = { Icon(Icons.Default.Star, null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Button(
                    onClick = {
                        viewModel.updatePaymentConfig(editBkash, editNagad, editRocket, editGooglePlay)
                        android.widget.Toast.makeText(context, if (language == AppLanguage.ENG) "Payment configs saved successfully!" else "পেমেন্ট কনফিগারেশন সফলভাবে সংরক্ষিত হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(if (language == AppLanguage.ENG) "Save Configs" else "কনফিগারেশন সংরক্ষণ করুন", fontSize = 12.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.ENG) "V9 Subscription Plans" else "ভি৯ সাবস্ক্রিপশন প্ল্যানসমূহ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BloodRed
            )
            Button(
                onClick = {
                    editingPlan = null
                    planId = "v9_plan_${System.currentTimeMillis()}"
                    nameEn = ""
                    nameBn = ""
                    price = ""
                    durationDays = ""
                    descEn = ""
                    descBn = ""
                    showAddPlanDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
            ) {
                Text(if (language == AppLanguage.ENG) "+ Add Plan" else "+ প্ল্যান যোগ করুন", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (plans.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (language == AppLanguage.ENG) "No V9 subscription plans created yet." else "কোন প্ল্যান এখনও তৈরি করা হয়নি।",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            plans.forEach { plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.ENG) plan.nameEn else plan.nameBn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DarkText
                            )
                            Text(
                                text = "${plan.price} BDT",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = BloodRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (language == AppLanguage.ENG) "Duration" else "মেয়াদ"}: ${plan.durationDays} ${if (language == AppLanguage.ENG) "Days" else "দিন"}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.ENG) plan.descriptionEn else plan.descriptionBn,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    editingPlan = plan
                                    planId = plan.id
                                    nameEn = plan.nameEn
                                    nameBn = plan.nameBn
                                    price = plan.price.toString()
                                    durationDays = plan.durationDays.toString()
                                    descEn = plan.descriptionEn
                                    descBn = plan.descriptionBn
                                    showAddPlanDialog = true
                                }
                            ) {
                                Text(if (language == AppLanguage.ENG) "Edit" else "সম্পাদনা", color = Color(0xFF1976D2))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    viewModel.triggerDeleteSubscriptionPlan(plan.id)
                                    android.widget.Toast.makeText(context, "Plan Deleted", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(if (language == AppLanguage.ENG) "Delete" else "মুছুন", color = BloodRed)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (language == AppLanguage.ENG) "Registered V9 Subscribers" else "নিবন্ধিত ভি৯ গ্রাহকগণ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BloodRed,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (subscriptions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (language == AppLanguage.ENG) "No subscribers found." else "কোন গ্রাহক পাওয়া যায়নি।",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            subscriptions.forEach { sub ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (language == AppLanguage.ENG) "User Phone" else "ব্যবহারকারীর ফোন"}: ${sub.userPhone}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DarkText
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (sub.isExpired) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (sub.isExpired) 
                                        (if (language == AppLanguage.ENG) "Expired" else "মেয়াদোত্তীর্ণ")
                                    else 
                                        (if (language == AppLanguage.ENG) "Active" else "সক্রিয়"),
                                    color = if (sub.isExpired) Color.Red else Color(0xFF2E7D32),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (language == AppLanguage.ENG) "Plan" else "প্যাক"}: ${if (language == AppLanguage.ENG) sub.planNameEn else sub.planNameBn} (${sub.pricePaid} BDT)",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "${if (language == AppLanguage.ENG) "Duration" else "মেয়াদ"}: ${sub.startDate} to ${sub.endDate}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${if (language == AppLanguage.ENG) "Txn ID" else "ট্রানজেকশন আইডি"}: ${sub.transactionId} (${sub.paymentMethod})",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    if (showAddPlanDialog) {
        AlertDialog(
            onDismissRequest = { showAddPlanDialog = false },
            title = {
                Text(
                    text = if (editingPlan == null) 
                        (if (language == AppLanguage.ENG) "Create V9 Plan" else "ভি৯ প্ল্যান তৈরি করুন")
                    else 
                        (if (language == AppLanguage.ENG) "Edit V9 Plan" else "ভি৯ প্ল্যান সংশোধন")
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(if (language == AppLanguage.ENG) "Plan Name (English)" else "প্ল্যানের নাম (ইংরেজি)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nameBn,
                        onValueChange = { nameBn = it },
                        label = { Text(if (language == AppLanguage.ENG) "Plan Name (Bengali)" else "প্ল্যানের নাম (বাংলা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text(if (language == AppLanguage.ENG) "Price (BDT)" else "মূল্য (টাকা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = durationDays,
                        onValueChange = { durationDays = it },
                        label = { Text(if (language == AppLanguage.ENG) "Duration (Days)" else "মেয়াদ (দিন)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descEn,
                        onValueChange = { descEn = it },
                        label = { Text(if (language == AppLanguage.ENG) "Description (English)" else "বিবরণ (ইংরেজি)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descBn,
                        onValueChange = { descBn = it },
                        label = { Text(if (language == AppLanguage.ENG) "Description (Bengali)" else "বিবরণ (বাংলা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dPrice = price.toDoubleOrNull() ?: 0.0
                        val iDur = durationDays.toIntOrNull() ?: 30
                        if (nameEn.isBlank() || nameBn.isBlank() || dPrice <= 0.0 || iDur <= 0) {
                            android.widget.Toast.makeText(context, "Please fill in all fields correctly", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val plan = V9SubscriptionPlan(
                                id = planId,
                                nameEn = nameEn,
                                nameBn = nameBn,
                                price = dPrice,
                                durationDays = iDur,
                                descriptionEn = descEn,
                                descriptionBn = descBn
                            )
                            viewModel.triggerAddOrUpdateSubscriptionPlan(plan)
                            android.widget.Toast.makeText(context, "Subscription Plan Saved!", android.widget.Toast.LENGTH_SHORT).show()
                            showAddPlanDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                ) {
                    Text(if (language == AppLanguage.ENG) "Save" else "সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlanDialog = false }) {
                    Text(if (language == AppLanguage.ENG) "Cancel" else "বাতিল")
                }
            }
        )
    }
}
