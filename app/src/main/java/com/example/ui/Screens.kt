package com.example.ui

import androidx.compose.ui.text.withStyle
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    val unreadChatCount by viewModel.unreadChatCount.collectAsState()
    val isAdvancePlanUser by viewModel.isAdvancePlanUser.collectAsState()
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
    var showSupportDialog by remember { mutableStateOf(false) }

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
                                        unreadChatCount = unreadChatCount,
                                        onItemClick = { targetScreen ->
                                            if (targetScreen == AppScreen.SUPPORT_CHAT) {
                                                scope.launch { drawerState.close() }
                                                showSupportDialog = true
                                            } else if (targetScreen == AppScreen.ADMIN_DASHBOARD) {
                                                showAdminPasswordDialog = true
                                            } else {
                                                if (targetScreen == AppScreen.LOGIN_REGISTER) {
                                                    viewModel.setShowRegistrationTab(false)
                                                }
                                                scope.launch { drawerState.close() }
                                                AdManager.showInterstitial(context, isAdvanceUser = isAdvancePlanUser) {
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
                                        isAdmin = currentUserSession?.email?.equals("Alifsheenshopping@gmail.com", ignoreCase = true) == true || currentUserSession?.email?.equals("help.alifshen.ltd@gmail.com", ignoreCase = true) == true
                                    )
                                }
                            }
                        ) {
                            // All general application screens have a common scaffold with navigation
                            Scaffold(
                                topBar = {
                                    CommonTopAppBar(
                                        title = when (screen) {
                                            AppScreen.AMBULANCE_LIST -> strings["ambulance_title"] ?: "Available Ambulances"
                                            AppScreen.ADD_AMBULANCE -> if (language == AppLanguage.BAN) "অ্যাম্বুলেন্স যুক্ত করুন" else "Add Ambulance"
                                            AppScreen.BOOK_AMBULANCE -> strings["amb_booking_title"] ?: "Book Ambulance"
                                            AppScreen.AMBULANCE_BOOKINGS -> strings["amb_booking_history"] ?: "Booking History"
                                            AppScreen.AMBULANCE_DASHBOARD -> if (language == AppLanguage.BAN) "অ্যাম্বুলেন্স ড্যাশবোর্ড" else "Ambulance Dashboard"
                                            AppScreen.DONOR_TEAMS -> if (language == AppLanguage.BAN) "স্বেচ্ছাসেবী রক্তদাতা টিম" else "Volunteer Donor Teams"
                                            AppScreen.TEAM_DETAIL -> if (language == AppLanguage.BAN) "টিমের বিবরণ ও সদস্য" else "Team Details & Members"
                                            else -> appName
                                        },
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
                                                viewModel.clearBackStackAndNavigateTo(targetScreen)
                                            } else {
                                                AdManager.showInterstitial(context) {
                                                    viewModel.navigateTo(targetScreen)
                                                }
                                            }
                                        },
                                        isAdmin = viewModel.isAdminMode.collectAsState().value,
                                        strings = strings,
                                        unreadChatCount = unreadChatCount
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
                                            .padding(vertical = 4.dp),
                                        isAdvanceUser = isAdvancePlanUser
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
                                            AppScreen.DONOR_TEAMS -> DonorTeamsScreen(viewModel)
                                            AppScreen.TEAM_DETAIL -> TeamDetailScreen(viewModel)
                                            AppScreen.DONOR_POLICY -> DonorPolicyScreen(viewModel)
                                            AppScreen.REGISTRATION_POLICY -> RegistrationPolicyScreen(viewModel)
                                            AppScreen.HOSPITAL_DIRECTORY -> HospitalDirectoryScreen(viewModel)
                                            AppScreen.DOCTOR_DIRECTORY -> DoctorDirectoryScreen(viewModel)
                                            AppScreen.ADVERTISER_PORTAL -> AdvertiserPortalScreen(viewModel)
                                            else -> {}
                                        }
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
                        .padding(bottom = 80.dp, end = 16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = {
                            showSupportDialog = true
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
                        if (unreadChatCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFFFD700),
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            text = if (unreadChatCount > 99) "99+" else unreadChatCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.HeadsetMic, contentDescription = "Support Chat")
                            }
                        } else {
                            Icon(Icons.Filled.HeadsetMic, contentDescription = "Support Chat")
                        }
                    }
                }
            }

            // Support Modal Dialog
            if (showSupportDialog) {
                SupportModalDialog(
                    language = language,
                    viewModel = viewModel,
                    onDismiss = { showSupportDialog = false }
                )
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

            // Offline Screen
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
                                "আলিফ ব্লাড ব্যাংক অ্যাপটি ব্যবহার করতে সচল মোবাইল ডাটা অথবা ওয়াই-ফাই সংযোগ প্রয়োজন। দয়া করে ইন্টারনেট চালু করুন এবং আবার চেষ্টা করুন।",
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
                                    Toast.makeText(
                                        context, 
                                        if (language == AppLanguage.ENG) "Internet connected successfully!" else "ইন্টারনেট সফলভাবে সংযুক্ত হয়েছে!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context, 
                                        if (language == AppLanguage.ENG) "Still offline. Please check your network connection." else "এখনো অফলাইন। দয়া করে আপনার নেটওয়ার্ক সংযোগ পরীক্ষা করুন।",
                                        Toast.LENGTH_SHORT
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
