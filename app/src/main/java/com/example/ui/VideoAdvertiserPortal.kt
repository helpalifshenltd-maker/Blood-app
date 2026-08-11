package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*

/**
 * Complete Video Advertisement & Advertiser System Portal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoAdvertiserPortalDialog(
    viewModel: MainViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val currentVideoAdvertiser by viewModel.currentVideoAdvertiser.collectAsState()
    val videoAdvertisements by viewModel.videoAdvertisements.collectAsState()
    val videoAdPackages by viewModel.videoAdPackages.collectAsState()
    val videoAdPayments by viewModel.videoAdPayments.collectAsState()
    val bkashNumber by viewModel.bkashNumber.collectAsState()
    val nagadNumber by viewModel.nagadNumber.collectAsState()

    var activeTab by remember { mutableStateOf("Dashboard") }

    // Login/Register Form States
    var isRegisterMode by remember { mutableStateOf(false) }
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    var regBusinessName by remember { mutableStateOf("") }
    var regCategory by remember { mutableStateOf("Hospital") }
    var regContactPerson by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regWebsite by remember { mutableStateOf("") }
    var regDescription by remember { mutableStateOf("") }
    var regDocUrl by remember { mutableStateOf("") }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF1565C0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.BAN) "ভিডিও এডভারটাইজার পোর্টাল" else "Video Advertiser Portal",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                        Text(
                            text = if (language == AppLanguage.BAN) "ব্যবসা ও কোম্পানির ভিডিও বিজ্ঞাপন ম্যানেজার" else "Business Video Ads & Campaign Manager",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 580.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (currentVideoAdvertiser == null) {
                    // --- AUTHENTICATION (LOGIN / REGISTER) ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
                        border = BorderStroke(1.dp, Color(0xFFE0E6ED))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Auth Mode Switcher Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                                    .padding(4.dp)
                            ) {
                                Button(
                                    onClick = { isRegisterMode = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isRegisterMode) Color(0xFF1565C0) else Color.Transparent,
                                        contentColor = if (!isRegisterMode) Color.White else Color(0xFF1565C0)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = null
                                ) {
                                    Text(if (language == AppLanguage.BAN) "লগইন (Login)" else "Login", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { isRegisterMode = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRegisterMode) Color(0xFF1565C0) else Color.Transparent,
                                        contentColor = if (isRegisterMode) Color.White else Color(0xFF1565C0)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = null
                                ) {
                                    Text(if (language == AppLanguage.BAN) "রেজিস্টার (Register)" else "Register", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!isRegisterMode) {
                                // --- LOGIN FORM ---
                                Text(
                                    text = if (language == AppLanguage.BAN) "এডভারটাইজার অ্যাকাউন্ট লগইন" else "Advertiser Account Login",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = loginIdentifier,
                                    onValueChange = { loginIdentifier = it },
                                    label = { Text(if (language == AppLanguage.BAN) "ইমেইল অথবা ফোন নম্বর *" else "Email or Phone Number *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1565C0)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    label = { Text(if (language == AppLanguage.BAN) "পাসওয়ার্ড *" else "Password *") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1565C0)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val ok = viewModel.loginVideoAdvertiser(loginIdentifier, loginPassword)
                                        if (ok) {
                                            snackbarMessage = if (language == AppLanguage.BAN) "সফলভাবে লগইন হয়েছে!" else "Login successful!"
                                        } else {
                                            snackbarMessage = if (language == AppLanguage.BAN) "লগইন তথ্য সঠিক নয়!" else "Invalid credentials!"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (language == AppLanguage.BAN) "লগইন করুন ➔" else "Login to Dashboard ➔", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // --- REGISTER FORM ---
                                Text(
                                    text = if (language == AppLanguage.BAN) "নতুন বিজনেস প্রোফাইল তৈরি করুন" else "Create New Business Profile",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = regBusinessName,
                                    onValueChange = { regBusinessName = it },
                                    label = { Text(if (language == AppLanguage.BAN) "প্রতিষ্ঠানের নাম *" else "Business Name *") },
                                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF1565C0)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(if (language == AppLanguage.BAN) "ক্যাটাগরি নির্বাচন করুন *" else "Select Business Category *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                var categoryExpanded by remember { mutableStateOf(false) }
                                val categories = listOf("Hospital", "Diagnostic Center", "Pharmacy", "Corporate", "E-commerce", "Local Business", "Other")
                                Box {
                                    OutlinedButton(
                                        onClick = { categoryExpanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(regCategory, color = Color.Black)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                    }
                                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    regCategory = cat
                                                    categoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = regContactPerson,
                                    onValueChange = { regContactPerson = it },
                                    label = { Text(if (language == AppLanguage.BAN) "যোগাযোগকারীর নাম *" else "Contact Person *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1565C0)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = regPhone,
                                        onValueChange = { regPhone = it },
                                        label = { Text(if (language == AppLanguage.BAN) "ফোন নম্বর *" else "Phone *") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = regEmail,
                                        onValueChange = { regEmail = it },
                                        label = { Text(if (language == AppLanguage.BAN) "ইমেইল *" else "Email *") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = regPassword,
                                    onValueChange = { regPassword = it },
                                    label = { Text(if (language == AppLanguage.BAN) "পাসওয়ার্ড *" else "Password *") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1565C0)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = regAddress,
                                    onValueChange = { regAddress = it },
                                    label = { Text(if (language == AppLanguage.BAN) "ব্যবসার ঠিকানা" else "Business Address") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = regWebsite,
                                    onValueChange = { regWebsite = it },
                                    label = { Text(if (language == AppLanguage.BAN) "ওয়েবসাইট (Website URL)" else "Website URL") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = regDescription,
                                    onValueChange = { regDescription = it },
                                    label = { Text(if (language == AppLanguage.BAN) "ব্যবসার সংক্ষিপ্ত বিবরণ" else "Business Description") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (regBusinessName.isBlank() || regPhone.isBlank() || regPassword.isBlank()) {
                                            snackbarMessage = if (language == AppLanguage.BAN) "দয়া করে প্রয়োজনীয় ঘরগুলো পূরণ করুন!" else "Please fill required fields!"
                                        } else {
                                            viewModel.registerVideoAdvertiser(
                                                businessName = regBusinessName,
                                                category = regCategory,
                                                contactPerson = regContactPerson,
                                                phone = regPhone,
                                                email = regEmail,
                                                pass = regPassword,
                                                address = regAddress,
                                                website = regWebsite,
                                                description = regDescription,
                                                verificationDocUrl = regDocUrl
                                            )
                                            snackbarMessage = if (language == AppLanguage.BAN) "রেজিস্ট্রেশন সফল হয়েছে!" else "Registration completed!"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (language == AppLanguage.BAN) "অ্যাকাউন্ট তৈরি করুন ➔" else "Register Business Account ➔", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // --- LOGGED-IN ADVERTISER PORTAL ---
                    val adv = currentVideoAdvertiser!!
                    val myAds = remember(videoAdvertisements, adv) {
                        videoAdvertisements.filter { it.advertiserId == adv.id || it.advertiserName.contains(adv.businessName, ignoreCase = true) }
                    }

                    // Top Profile Card & Verification Badge
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D47A1)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                AsyncImage(
                                    model = adv.logoUrl.ifBlank { "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?auto=format&fit=crop&w=200&q=80" },
                                    contentDescription = adv.businessName,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = adv.businessName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = when (adv.verificationStatus) {
                                                "Verified" -> Color(0xFF2E7D32)
                                                "Rejected" -> Color(0xFFC62828)
                                                else -> Color(0xFFEF6C00)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = when (adv.verificationStatus) {
                                                    "Verified" -> if (language == AppLanguage.BAN) "✓ ভেরিফাইড বিজনেস" else "✓ Verified Business"
                                                    "Rejected" -> if (language == AppLanguage.BAN) "✕ ভেরিফিকেশন বাতিল" else "✕ Verification Rejected"
                                                    else -> if (language == AppLanguage.BAN) "⏳ ভেরিফিকেশন পেন্ডিং" else "⏳ Verification Pending"
                                                },
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "• " + adv.category, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.logoutVideoAdvertiser() },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Navigation Tabs Row
                    val tabs = listOf(
                        "Dashboard" to (if (language == AppLanguage.BAN) "ড্যাশবোর্ড" else "Dashboard"),
                        "MyAds" to (if (language == AppLanguage.BAN) "আমার বিজ্ঞাপন" else "My Ads"),
                        "CreateAd" to (if (language == AppLanguage.BAN) "নতুন বিজ্ঞাপন" else "Create Ad"),
                        "Analytics" to (if (language == AppLanguage.BAN) "অ্যানালিটিক্স" else "Analytics"),
                        "Profile" to (if (language == AppLanguage.BAN) "প্রোফাইল" else "Profile"),
                        "Payments" to (if (language == AppLanguage.BAN) "পেমেন্ট" else "Payments")
                    )

                    ScrollableTabRow(
                        selectedTabIndex = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        tabs.forEach { (key, label) ->
                            Tab(
                                selected = activeTab == key,
                                onClick = { activeTab = key },
                                text = { Text(label, fontWeight = if (activeTab == key) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (activeTab) {
                        "Dashboard" -> {
                            // --- DASHBOARD OVERVIEW MODULE ---
                            val activeCampaigns = myAds.filter { it.status == "Active" }
                            val totalImpressionsCount = myAds.sumOf { it.impressionsCount }
                            val totalUniques = myAds.sumOf { it.uniqueViewersCount }
                            val totalClicks = myAds.sumOf { it.clicksCount }
                            val totalCompleted = myAds.sumOf { it.completedViewsCount }

                            Text(
                                text = if (language == AppLanguage.BAN) "📊 ক্যাম্পেইন ওভারভিউ" else "📊 Campaign Overview",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DashboardStatCard(
                                    title = if (language == AppLanguage.BAN) "সক্রিয় ক্যাম্পেইন" else "Active Ads",
                                    value = "${activeCampaigns.size}",
                                    icon = Icons.Default.Campaign,
                                    bgColor = Color(0xFFE3F2FD),
                                    textColor = Color(0xFF1565C0),
                                    modifier = Modifier.weight(1f)
                                )
                                DashboardStatCard(
                                    title = if (language == AppLanguage.BAN) "মোট ইমপ্রেশন" else "Impressions",
                                    value = String.format("%,d", totalImpressionsCount),
                                    icon = Icons.Default.Visibility,
                                    bgColor = Color(0xFFE8F5E9),
                                    textColor = Color(0xFF2E7D32),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DashboardStatCard(
                                    title = if (language == AppLanguage.BAN) "ইউনিক ভিউয়ার" else "Unique Viewers",
                                    value = String.format("%,d", totalUniques),
                                    icon = Icons.Default.Group,
                                    bgColor = Color(0xFFFFF3E0),
                                    textColor = Color(0xFFE65100),
                                    modifier = Modifier.weight(1f)
                                )
                                DashboardStatCard(
                                    title = if (language == AppLanguage.BAN) "মোট ক্লিক" else "Total Clicks",
                                    value = String.format("%,d", totalClicks),
                                    icon = Icons.Default.TouchApp,
                                    bgColor = Color(0xFFF3E5F5),
                                    textColor = Color(0xFF6A1B9A),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (language == AppLanguage.BAN) "🎬 সমসাময়িক সক্রিয় বিজ্ঞাপনসমূহ" else "🎬 Active Video Campaigns",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (myAds.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57F17))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (language == AppLanguage.BAN) "আপনার কোনো বিজ্ঞাপন ক্যাম্পেইন নেই। এখনই তৈরি করুন!" else "No advertisements submitted yet. Create one now!",
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { activeTab = "CreateAd" },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                                        ) {
                                            Text(if (language == AppLanguage.BAN) "+ নতুন এড জমা দিন" else "+ Submit New Ad")
                                        }
                                    }
                                }
                            } else {
                                myAds.take(3).forEach { ad ->
                                    AdvertiserAdCardItem(
                                        ad = ad,
                                        language = language,
                                        onPauseToggle = { viewModel.pauseVideoAdvertisement(ad.id, ad.status == "Active") },
                                        onViewAnalytics = { activeTab = "Analytics" }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        "MyAds" -> {
                            // --- MY ADVERTISEMENTS MODULE WITH FILTERS ---
                            var filterStatus by remember { mutableStateOf("All") }
                            val filterOptions = listOf("All", "Active", "Pending Payment", "Pending Review", "Paused", "Rejected", "Expired")

                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                                filterOptions.forEach { opt ->
                                    FilterChip(
                                        selected = filterStatus == opt,
                                        onClick = { filterStatus = opt },
                                        label = { Text(opt, fontSize = 11.sp) },
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val filteredAds = remember(myAds, filterStatus) {
                                if (filterStatus == "All") myAds else myAds.filter { it.status.equals(filterStatus, ignoreCase = true) }
                            }

                            if (filteredAds.isEmpty()) {
                                Text(
                                    text = if (language == AppLanguage.BAN) "এই ক্যাটাগরিতে কোনো বিজ্ঞাপন পাওয়া যায়নি।" else "No ads found in this filter.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                filteredAds.forEach { ad ->
                                    AdvertiserAdCardItem(
                                        ad = ad,
                                        language = language,
                                        onPauseToggle = { viewModel.pauseVideoAdvertisement(ad.id, ad.status == "Active") },
                                        onViewAnalytics = { activeTab = "Analytics" }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        "CreateAd" -> {
                            // --- CREATE ADVERTISEMENT MODULE ---
                            // Check if profile is complete first
                            if (adv.businessName.isBlank() || adv.phone.isBlank()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (language == AppLanguage.BAN) "⚠️ বিজ্ঞাপন তৈরির আগে আপনার বিজনেস প্রোফাইল সম্পূর্ণ করুন।" else "⚠️ Complete business profile before creating advertisement.",
                                            fontSize = 12.sp,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            CreateVideoAdForm(
                                advertiser = adv,
                                packages = videoAdPackages,
                                language = language,
                                bkashNumber = bkashNumber,
                                nagadNumber = nagadNumber,
                                onSubmitAd = { title, videoUrl, thumbUrl, desc, cat, ctaText, ctaUrl, contact, pkgId, pkgName, pMethod, trxId, proofUrl, placements ->
                                    viewModel.createVideoAdvertisement(
                                        advertiserId = adv.id,
                                        advertiserName = adv.businessName,
                                        packageId = pkgId,
                                        packageName = pkgName,
                                        title = title,
                                        videoUrl = videoUrl,
                                        thumbnailUrl = thumbUrl,
                                        description = desc,
                                        category = cat,
                                        ctaText = ctaText,
                                        ctaUrl = ctaUrl,
                                        contactNumber = contact,
                                        placementSections = placements,
                                        paymentMethod = pMethod,
                                        transactionId = trxId,
                                        paymentProofUrl = proofUrl
                                    )
                                    snackbarMessage = if (language == AppLanguage.BAN) "বিজ্ঞাপন সফলভাবে জমা দেওয়া হয়েছে! এডমিন শীঘ্রই রিভিউ করবেন।" else "Ad submitted successfully! Admin will review shortly."
                                    activeTab = "MyAds"
                                }
                            )
                        }

                        "Analytics" -> {
                            // --- VIDEO ANALYTICS MODULE ---
                            Text(
                                text = if (language == AppLanguage.BAN) "📈 ভিডিও বিজ্ঞাপন অ্যানালিটিক্স & ট্র্যাক" else "📈 Video Advertisement Analytics",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            myAds.forEach { ad ->
                                VideoAnalyticsCard(ad = ad, language = language)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        "Profile" -> {
                            // --- BUSINESS PROFILE MODULE ---
                            BusinessProfileForm(
                                advertiser = adv,
                                language = language,
                                onSave = { updatedProfile ->
                                    viewModel.updateVideoAdvertiserProfile(updatedProfile)
                                    snackbarMessage = if (language == AppLanguage.BAN) "বিজনেস প্রোফাইল আপডেট হয়েছে!" else "Business Profile Updated!"
                                }
                            )
                        }

                        "Payments" -> {
                            // --- PAYMENT HISTORY MODULE ---
                            Text(
                                text = if (language == AppLanguage.BAN) "💳 পেমেন্ট হিস্ট্রি & রসিদ" else "💳 Payment History & Invoices",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val myPayments = remember(videoAdPayments, adv) {
                                videoAdPayments.filter { it.advertiserId == adv.id }
                            }

                            if (myPayments.isEmpty()) {
                                Text(
                                    text = if (language == AppLanguage.BAN) "কোনো পেমেন্ট রেকর্ড পাওয়া যায়নি।" else "No payment records found.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            } else {
                                myPayments.forEach { pay ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
                                        border = BorderStroke(1.dp, Color(0xFFE0E6ED))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Package: ${pay.packageName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Surface(
                                                    color = if (pay.status == "Verified") Color(0xFF2E7D32) else Color(0xFFEF6C00),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(pay.status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Amount: ৳${pay.amount.toInt()} • Method: ${pay.paymentMethod}", fontSize = 12.sp, color = Color.DarkGray)
                                            Text("TrxID: ${pay.transactionId}", fontSize = 11.sp, color = Color.Gray)
                                            Text("Date: ${pay.paymentDate}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                snackbarMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFF323232),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontSize = 10.sp, color = textColor.copy(alpha = 0.8f))
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
fun AdvertiserAdCardItem(
    ad: VideoAdvertisement,
    language: AppLanguage,
    onPauseToggle: () -> Unit,
    onViewAnalytics: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ad.thumbnailUrl.ifBlank { "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?auto=format&fit=crop&w=200&q=80" },
                    contentDescription = ad.title,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(ad.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Package: ${ad.packageName}", fontSize = 11.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = when (ad.status) {
                                "Active" -> Color(0xFF2E7D32)
                                "Pending Payment", "Pending Review" -> Color(0xFFEF6C00)
                                "Rejected" -> Color(0xFFC62828)
                                "Paused" -> Color(0xFF757575)
                                else -> Color(0xFFD81B60)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(ad.status, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${ad.startDate} - ${ad.endDate}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Impression Progress Bar
            val progress = (ad.impressionsCount.toFloat() / ad.impressionLimit.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (language == AppLanguage.BAN) "ইমপ্রেশন অগ্রগতি" else "Impression Progress", fontSize = 10.sp, color = Color.Gray)
                    Text("${ad.impressionsCount} / ${ad.impressionLimit} (${(progress * 100).toInt()}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF1565C0),
                    trackColor = Color(0xFFE3F2FD)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (ad.status == "Active" || ad.status == "Paused") {
                    OutlinedButton(
                        onClick = onPauseToggle,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (ad.status == "Active") "Pause ⏸" else "Resume ▶", fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Button(
                    onClick = onViewAnalytics,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (language == AppLanguage.BAN) "অ্যানালিটিক্স 📊" else "Analytics 📊", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun CreateVideoAdForm(
    advertiser: VideoAdvertiser,
    packages: List<VideoAdPackage>,
    language: AppLanguage,
    bkashNumber: String,
    nagadNumber: String,
    onSubmitAd: (title: String, videoUrl: String, thumbUrl: String, desc: String, cat: String, ctaText: String, ctaUrl: String, contact: String, pkgId: String, pkgName: String, pMethod: String, trxId: String, proofUrl: String, placements: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(advertiser.category) }
    var ctaText by remember { mutableStateOf("Learn More") }
    var ctaUrl by remember { mutableStateOf(advertiser.website) }
    var contactNumber by remember { mutableStateOf(advertiser.phone) }
    var selectedPackageId by remember { mutableStateOf(packages.firstOrNull()?.id ?: "pkg_basic") }

    var paymentMethod by remember { mutableStateOf("bKash") }
    var transactionId by remember { mutableStateOf("") }
    var paymentProofUrl by remember { mutableStateOf("") }

    val ctaOptions = listOf("Learn More", "Call Now", "Visit Website", "Contact Us", "Book Now")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (language == AppLanguage.BAN) "➕ নতুন ভিডিও বিজ্ঞাপন জমা দিন" else "➕ Submit New Video Advertisement",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1)
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (language == AppLanguage.BAN) "বিজ্ঞাপনের শিরোনাম *" else "Ad Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text(if (language == AppLanguage.BAN) "ভিডিও লিঙ্ক / URL (Video URL) *" else "Video Link / URL *") },
                placeholder = { Text("https://example.com/video.mp4") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = thumbnailUrl,
                onValueChange = { thumbnailUrl = it },
                label = { Text(if (language == AppLanguage.BAN) "থাম্বনেইল ইমেজ URL (Thumbnail)" else "Thumbnail Image URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(if (language == AppLanguage.BAN) "সংক্ষিপ্ত বিবরণ" else "Short Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (language == AppLanguage.BAN) "CTA বোতাম" else "Call-To-Action", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    var ctaExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { ctaExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(ctaText, fontSize = 11.sp)
                        }
                        DropdownMenu(expanded = ctaExpanded, onDismissRequest = { ctaExpanded = false }) {
                            ctaOptions.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = { ctaText = opt; ctaExpanded = false })
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = ctaUrl,
                        onValueChange = { ctaUrl = it },
                        label = { Text(if (language == AppLanguage.BAN) "ল্যান্ডিং পেজ / URL" else "Landing Page URL") },
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- PACKAGE SELECTION ---
            Text(
                text = if (language == AppLanguage.BAN) "📦 বিজ্ঞাপন প্যাকেজ নির্বাচন করুন *" else "📦 Select Advertisement Package *",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1)
            )
            Spacer(modifier = Modifier.height(6.dp))

            packages.forEach { pkg ->
                val isSelected = selectedPackageId == pkg.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clickable { selectedPackageId = pkg.id },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF1565C0) else Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0D47A1))
                            Text("⏱ Duration: ${pkg.durationDays} Days • 👁 ${String.format("%,d", pkg.impressionLimit)} Impressions", fontSize = 11.sp, color = Color.DarkGray)
                        }
                        Text("৳${pkg.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- PAYMENT INFORMATION MODULE ---
            val selectedPkgObj = packages.find { it.id == selectedPackageId }
            val amountPayable = selectedPkgObj?.price ?: 500.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = BorderStroke(1.dp, Color(0xFFFFE082)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (language == AppLanguage.BAN) "💳 পেমেন্ট করার তথ্য (Manual Payment)" else "💳 Payment Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "bKash Merchant: $bkashNumber | Nagad Merchant: $nagadNumber\nপরিমাণ: ৳${amountPayable.toInt()} (Send Money / Payment করুন)",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    label = { Text("Transaction ID (TrxID) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank() || transactionId.isBlank()) {
                        // validation warning
                    } else {
                        onSubmitAd(
                            title,
                            videoUrl.ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4" },
                            thumbnailUrl.ifBlank { "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?auto=format&fit=crop&w=600&q=80" },
                            description,
                            category,
                            ctaText,
                            ctaUrl,
                            contactNumber,
                            selectedPackageId,
                            selectedPkgObj?.name ?: "Basic",
                            paymentMethod,
                            transactionId,
                            paymentProofUrl,
                            listOf("Home Screen", "Hospital Section", "Doctor Section", "Ambulance Section", "Search Result")
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (language == AppLanguage.BAN) "বিজ্ঞাপন জমা দিন ➔" else "Submit Advertisement ➔", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VideoAnalyticsCard(
    ad: VideoAdvertisement,
    language: AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(ad.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D47A1))
            Text("Campaign: ${ad.packageName} • Status: ${ad.status}", fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            val ctr = if (ad.impressionsCount > 0) (ad.clicksCount.toFloat() / ad.impressionsCount.toFloat()) * 100f else 0f

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnalyticsMiniStat("Impressions", String.format("%,d", ad.impressionsCount), Color(0xFFE3F2FD), Color(0xFF1565C0), Modifier.weight(1f))
                AnalyticsMiniStat("Unique Viewers", String.format("%,d", ad.uniqueViewersCount), Color(0xFFFFF3E0), Color(0xFFE65100), Modifier.weight(1f))
                AnalyticsMiniStat("Clicks", String.format("%,d", ad.clicksCount), Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f))
                AnalyticsMiniStat("CTR %", String.format("%.2f%%", ctr), Color(0xFFF3E5F5), Color(0xFF6A1B9A), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(if (language == AppLanguage.BAN) "📊 ভিডিও প্লেয়ার ওয়াচ টাইম ফানেল (Watch Funnel)" else "📊 Video Funnel Analytics", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            val started = ad.videoStartedCount.coerceAtLeast(1)
            VideoFunnelRow("Video Started", ad.videoStartedCount, 1.0f)
            VideoFunnelRow("25% Watched", ad.video25Count, ad.video25Count.toFloat() / started)
            VideoFunnelRow("50% Watched", ad.video50Count, ad.video50Count.toFloat() / started)
            VideoFunnelRow("75% Watched", ad.video75Count, ad.video75Count.toFloat() / started)
            VideoFunnelRow("Completed Views", ad.completedViewsCount, ad.completedViewsCount.toFloat() / started)
        }
    }
}

@Composable
fun VideoFunnelRow(label: String, count: Int, ratio: Float) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, modifier = Modifier.width(110.dp), color = Color.DarkGray)
        LinearProgressIndicator(
            progress = ratio.coerceIn(0f, 1f),
            modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF1565C0),
            trackColor = Color(0xFFE0E0E0)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("${String.format("%,d", count)} (${(ratio * 100).toInt()}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
    }
}

@Composable
fun AnalyticsMiniStat(label: String, value: String, bgColor: Color, textColor: Color, modifier: Modifier) {
    Surface(color = bgColor, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = textColor.copy(alpha = 0.8f))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun BusinessProfileForm(
    advertiser: VideoAdvertiser,
    language: AppLanguage,
    onSave: (VideoAdvertiser) -> Unit
) {
    var businessName by remember { mutableStateOf(advertiser.businessName) }
    var logoUrl by remember { mutableStateOf(advertiser.logoUrl) }
    var category by remember { mutableStateOf(advertiser.category) }
    var contactPerson by remember { mutableStateOf(advertiser.contactPerson) }
    var phone by remember { mutableStateOf(advertiser.phone) }
    var email by remember { mutableStateOf(advertiser.email) }
    var address by remember { mutableStateOf(advertiser.address) }
    var website by remember { mutableStateOf(advertiser.website) }
    var description by remember { mutableStateOf(advertiser.description) }
    var verificationDocUrl by remember { mutableStateOf(advertiser.verificationDocUrl) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (language == AppLanguage.BAN) "🏢 বিজনেস প্রোফাইল তথ্য সম্পাদনা" else "🏢 Edit Business Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1)
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text(if (language == AppLanguage.BAN) "প্রতিষ্ঠানের নাম *" else "Business Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = logoUrl,
                onValueChange = { logoUrl = it },
                label = { Text(if (language == AppLanguage.BAN) "লোগো ইমেজ URL" else "Logo Image URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contactPerson,
                onValueChange = { contactPerson = it },
                label = { Text(if (language == AppLanguage.BAN) "যোগাযোগকারীর নাম" else "Contact Person") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f), singleLine = true)
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Website URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Business Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = verificationDocUrl, onValueChange = { verificationDocUrl = it }, label = { Text("Verification Document URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val updated = advertiser.copy(
                        businessName = businessName,
                        logoUrl = logoUrl,
                        category = category,
                        contactPerson = contactPerson,
                        phone = phone,
                        email = email,
                        address = address,
                        website = website,
                        description = description,
                        verificationDocUrl = verificationDocUrl
                    )
                    onSave(updated)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (language == AppLanguage.BAN) "সংরক্ষণ করুন" else "Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * In-App Video Ad Player Component (for Home Screen, Hospital, Doctor, Ambulance, etc.)
 */
@Composable
fun InAppVideoAdCard(
    videoAd: VideoAdvertisement,
    language: AppLanguage,
    onAdClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFFFD700), shape = RoundedCornerShape(4.dp)) {
                        Text("SPONSORED AD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(videoAd.advertiserName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
            }

            // Video Preview Image & Play Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = videoAd.thumbnailUrl.ifBlank { "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?auto=format&fit=crop&w=600&q=80" },
                    contentDescription = videoAd.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay Dark Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )

                // Play Button
                IconButton(
                    onClick = {
                        onAdClick()
                        if (videoAd.ctaUrl.isNotBlank()) {
                            try { uriHandler.openUri(videoAd.ctaUrl) } catch (e: Exception) {}
                        }
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFF1565C0).copy(alpha = 0.85f), CircleShape)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }

            // Footer Info & Call To Action
            Column(modifier = Modifier.padding(12.dp)) {
                Text(videoAd.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (videoAd.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(videoAd.description, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(videoAd.contactNumber.ifBlank { "01700000000" }, color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onAdClick()
                            if (videoAd.ctaUrl.isNotBlank()) {
                                try { uriHandler.openUri(videoAd.ctaUrl) } catch (e: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(videoAd.ctaText.ifBlank { "Learn More" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
