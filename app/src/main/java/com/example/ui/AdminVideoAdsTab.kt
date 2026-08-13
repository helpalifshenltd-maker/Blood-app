package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*

/**
 * Admin Panel - Complete Video Advertisement & Advertiser Management Tab
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVideoAdsTab(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val isBn = language == AppLanguage.BAN

    val videoAdvertisers by viewModel.videoAdvertisers.collectAsState()
    val videoAdvertisements by viewModel.videoAdvertisements.collectAsState()
    val videoAdPackages by viewModel.videoAdPackages.collectAsState()
    val videoAdPayments by viewModel.videoAdPayments.collectAsState()
    val videoAdConfig by viewModel.videoAdConfig.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Advertisements, 1: Advertisers, 2: Payments, 3: Packages, 4: Placements & Caps
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("All") }

    // Dialog state variables
    var rejectingAdId by remember { mutableStateOf<String?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }

    var extendingAdId by remember { mutableStateOf<String?>(null) }
    var extraDaysInput by remember { mutableStateOf("7") }
    var extraImpressionsInput by remember { mutableStateOf("2500") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .padding(16.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isBn) "📹 ভিডিও এডভারটাইজমেন্ট & এডভারটাইজার ম্যানেজমেন্ট" else "📹 Video Ads & Advertiser Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isBn) "ভিডিও বিজ্ঞাপন অনুমোদন, ম্যানুয়াল পেমেন্ট যাচাই, প্যাকেজ ও প্লেসমেন্ট নিয়ন্ত্রণ" else "Approve video ads, verify manual payments, packages & placement controls",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // --- Sub-Tab Navigation Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(Color(0xFF2A2A3C), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val pendingPaymentCount = videoAdPayments.count { it.status == "Pending" }
            val pendingAdCount = videoAdvertisements.count { it.status == "Pending Review" || it.status == "Pending Payment" }

            val subTabs = listOf(
                Pair(if (isBn) "📹 বিজ্ঞাপনসমূহ (${videoAdvertisements.size})" else "📹 Ads (${videoAdvertisements.size})", 0),
                Pair(if (isBn) "💳 পেমেন্ট যাচাই ($pendingPaymentCount)" else "💳 Payments ($pendingPaymentCount)", 2),
                Pair(if (isBn) "🏢 এডভারটাইজার ($pendingAdCount)" else "🏢 Advertisers", 1),
                Pair(if (isBn) "📦 প্যাকেজসমূহ" else "📦 Packages", 3),
                Pair(if (isBn) "⚙️ ক্যাপ & প্লেসমেন্ট" else "⚙️ Caps & Placements", 4)
            )

            subTabs.forEach { (label, idx) ->
                val isSelected = activeSubTab == idx
                FilterChip(
                    selected = isSelected,
                    onClick = { activeSubTab = idx },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3F51B5),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Analytics Summary Cards ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminStatBox("মোট এডভারটাইজার", "${videoAdvertisers.size}", Color(0xFF1976D2))
            AdminStatBox("মোট বিজ্ঞাপন", "${videoAdvertisements.size}", Color(0xFF7B1FA2))
            AdminStatBox("সক্রিয় ক্যাম্পেইন", "${videoAdvertisements.count { it.status == "Active" }}", Color(0xFF2E7D32))
            AdminStatBox("পেন্ডিং রিভিউ", "${videoAdvertisements.count { it.status == "Pending Review" || it.status == "Pending Payment" }}", Color(0xFFEF6C00))
            AdminStatBox("মোট ইমপ্রেশন", "${videoAdvertisements.sumOf { it.impressionsCount }}", Color(0xFF00838F))
            AdminStatBox("মোট ক্লিক", "${videoAdvertisements.sumOf { it.clicksCount }}", Color(0xFFD84315))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Search Bar ---
        if (activeSubTab in listOf(0, 1, 2)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBn) "খুঁজুন (কোম্পানির নাম, শিরোনাম, ট্রানজেকশন আইডি...)" else "Search (Company name, title, Trx ID...)", fontSize = 12.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedContainerColor = Color(0xFF2A2A3C),
                    unfocusedContainerColor = Color(0xFF2A2A3C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- Tab Contents ---
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                // 0: ADVERTISEMENTS MANAGEMENT TAB
                0 -> {
                    Column {
                        // Filter Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("All", "Active", "Pending Review", "Pending Payment", "Paused", "Rejected", "Expired").forEach { statusOpt ->
                                FilterChip(
                                    selected = filterStatus == statusOpt,
                                    onClick = { filterStatus = statusOpt },
                                    label = { Text(statusOpt, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2196F3),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val filteredAds = videoAdvertisements.filter { ad ->
                            val matchesSearch = searchQuery.isEmpty() ||
                                    ad.title.contains(searchQuery, ignoreCase = true) ||
                                    ad.description.contains(searchQuery, ignoreCase = true) ||
                                    ad.id.contains(searchQuery, ignoreCase = true)

                            val matchesStatus = filterStatus == "All" || ad.status.equals(filterStatus, ignoreCase = true)
                            matchesSearch && matchesStatus
                        }

                        if (filteredAds.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(if (isBn) "কোনো ভিডিও বিজ্ঞাপন পাওয়া যায়নি।" else "No video advertisements found.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredAds) { ad ->
                                    val advertiser = videoAdvertisers.find { it.id == ad.advertiserId }
                                    AdminVideoAdCard(
                                        ad = ad,
                                        advertiserName = advertiser?.businessName ?: "Subscribed Company",
                                        isBn = isBn,
                                        onApprove = {
                                            viewModel.approveVideoAdvertisement(ad.id, true)
                                            Toast.makeText(context, "Advertisement Approved & Active!", Toast.LENGTH_SHORT).show()
                                        },
                                        onReject = {
                                            rejectingAdId = ad.id
                                            rejectionReasonText = ""
                                        },
                                        onPauseToggle = {
                                            val isPaused = ad.status == "Paused"
                                            viewModel.pauseVideoAdvertisement(ad.id, !isPaused)
                                            Toast.makeText(context, if (!isPaused) "Ad Paused" else "Ad Resumed", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = {
                                            viewModel.deleteVideoAdvertisement(ad.id)
                                            Toast.makeText(context, "Ad Deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        onExtend = {
                                            extendingAdId = ad.id
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 1: ADVERTISERS & BUSINESS PROFILES
                1 -> {
                    val filteredAdvertisers = videoAdvertisers.filter {
                        searchQuery.isEmpty() ||
                                it.businessName.contains(searchQuery, ignoreCase = true) ||
                                it.phone.contains(searchQuery) ||
                                it.email.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredAdvertisers) { adv ->
                            AdminAdvertiserProfileCard(
                                advertiser = adv,
                                isBn = isBn,
                                onApproveVerification = {
                                    viewModel.updateVideoAdvertiserVerification(adv.id, "Verified")
                                    Toast.makeText(context, "Business Profile Verified!", Toast.LENGTH_SHORT).show()
                                },
                                onRejectVerification = {
                                    viewModel.updateVideoAdvertiserVerification(adv.id, "Rejected")
                                    Toast.makeText(context, "Verification Rejected", Toast.LENGTH_SHORT).show()
                                },
                                onBlockToggle = {
                                    viewModel.blockVideoAdvertiser(adv.id, !adv.isBlocked)
                                    Toast.makeText(context, if (!adv.isBlocked) "Advertiser Blocked" else "Advertiser Unblocked", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // 2: MANUAL PAYMENT PROOFS VERIFICATION
                2 -> {
                    val filteredPayments = videoAdPayments.filter {
                        searchQuery.isEmpty() ||
                                it.transactionId.contains(searchQuery, ignoreCase = true) ||
                                it.advertiserId.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredPayments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (isBn) "কোনো পেমেন্ট রেকর্ড পাওয়া যায়নি।" else "No payment records found.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredPayments) { pay ->
                                val ad = videoAdvertisements.find { it.id == pay.advertisementId }
                                val adv = videoAdvertisers.find { it.id == pay.advertiserId }

                                AdminPaymentProofCard(
                                    payment = pay,
                                    adTitle = ad?.title ?: "Video Campaign",
                                    advertiserName = adv?.businessName ?: "Subscribed Company",
                                    isBn = isBn,
                                    onVerify = {
                                        viewModel.verifyVideoAdPayment(pay.id, true)
                                        Toast.makeText(context, "Payment Verified & Ad Activated!", Toast.LENGTH_SHORT).show()
                                    },
                                    onReject = {
                                        viewModel.verifyVideoAdPayment(pay.id, false)
                                        Toast.makeText(context, "Payment Proof Rejected", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                // 3: PACKAGE MANAGEMENT
                3 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(videoAdPackages) { pkg ->
                            AdminPackageEditCard(
                                pkg = pkg,
                                isBn = isBn,
                                onSave = { updatedPkg ->
                                    viewModel.updateVideoAdPackage(updatedPkg)
                                    Toast.makeText(context, "Package Updated Successfully!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // 4: FREQUENCY CAPS & PLACEMENTS
                4 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2A2A3C), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (isBn) "🎯 Frequency Cap & Display Placement Rules" else "🎯 Frequency Cap & Display Placement Rules", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)

                        HorizontalDivider(color = Color(0xFF424242))

                        Text(if (isBn) "প্রতিজন ইউজারকে দিনে সর্বোচ্চ কতবার ভিডিও বিজ্ঞাপন দেখাবে (Frequency Cap):" else "Max Impressions per User per Day (Frequency Cap):", color = Color.LightGray, fontSize = 12.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 5, 10).forEach { cap ->
                                FilterChip(
                                    selected = videoAdConfig.frequencyCapPerDay == cap,
                                    onClick = {
                                        viewModel.updateVideoAdConfig(videoAdConfig.copy(frequencyCapPerDay = cap))
                                        Toast.makeText(context, "Frequency cap set to $cap impressions/day", Toast.LENGTH_SHORT).show()
                                    },
                                    label = { Text("$cap / Day", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2E7D32),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF424242))

                        Text(if (isBn) "বিজ্ঞাপন প্রদর্শনের সক্রিয় সেকশনসমূহ (App Placement Slots):" else "Active Placement Slots:", color = Color.LightGray, fontSize = 12.sp)

                        val placements = listOf(
                            "Home Screen" to Pair("হোম স্ক্রিন মেইন ফিড", videoAdConfig.isHomeEnabled),
                            "Hospital Section" to Pair("হাসপাতাল ও ডায়াগনস্টিক পেজ", videoAdConfig.isHospitalEnabled),
                            "Doctor Section" to Pair("ডাক্তার বুকিং ফিড", videoAdConfig.isDoctorEnabled),
                            "Ambulance Section" to Pair("জরুরি অ্যাম্বুলেন্স সার্ভিস পেজ", videoAdConfig.isAmbulanceEnabled),
                            "Search Result" to Pair("অনুসন্ধান ফলাফল পেজ", videoAdConfig.isSearchEnabled)
                        )

                        placements.forEach { (slot, pair) ->
                            val (slotBn, isEnabled) = pair
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isBn) slotBn else slot, color = Color.White, fontSize = 13.sp)
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        val updatedConfig = when (slot) {
                                            "Home Screen" -> videoAdConfig.copy(isHomeEnabled = checked)
                                            "Hospital Section" -> videoAdConfig.copy(isHospitalEnabled = checked)
                                            "Doctor Section" -> videoAdConfig.copy(isDoctorEnabled = checked)
                                            "Ambulance Section" -> videoAdConfig.copy(isAmbulanceEnabled = checked)
                                            else -> videoAdConfig.copy(isSearchEnabled = checked)
                                        }
                                        viewModel.updateVideoAdConfig(updatedConfig)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to Reject Ad
    if (rejectingAdId != null) {
        AlertDialog(
            onDismissRequest = { rejectingAdId = null },
            title = { Text(if (isBn) "বিজ্ঞাপন প্রত্যাখ্যান করুন" else "Reject Advertisement") },
            text = {
                Column {
                    Text(if (isBn) "প্রত্যাখ্যানের কারণ লিখুন (বিজ্ঞাপনদাতা দেখতে পাবে):" else "Reason for rejection:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val adId = rejectingAdId ?: return@Button
                        viewModel.approveVideoAdvertisement(adId, false, rejectionReasonText)
                        rejectingAdId = null
                        Toast.makeText(context, "Advertisement Rejected", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (isBn) "প্রত্যাখ্যান সেভ করুন" else "Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingAdId = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Modal to Extend Campaign
    if (extendingAdId != null) {
        AlertDialog(
            onDismissRequest = { extendingAdId = null },
            title = { Text(if (isBn) "ক্যাম্পেইন বাড়ান (Extend Campaign)" else "Extend Campaign") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = extraDaysInput,
                        onValueChange = { extraDaysInput = it },
                        label = { Text(if (isBn) "অতিরিক্ত দিন সংখ্যা" else "Extra Days") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = extraImpressionsInput,
                        onValueChange = { extraImpressionsInput = it },
                        label = { Text(if (isBn) "অতিরিক্ত ইমপ্রেশন লিমিট" else "Extra Impression Limit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val adId = extendingAdId ?: return@Button
                        val days = extraDaysInput.toIntOrNull() ?: 7
                        val imps = extraImpressionsInput.toIntOrNull() ?: 2500
                        viewModel.extendVideoAdCampaign(adId, days, imps)
                        extendingAdId = null
                        Toast.makeText(context, "Campaign Extended!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(if (isBn) "বর্ধিত করুন" else "Extend")
                }
            },
            dismissButton = {
                TextButton(onClick = { extendingAdId = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminStatBox(label: String, value: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, fontSize = 10.sp, color = Color.LightGray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminVideoAdCard(
    ad: VideoAdvertisement,
    advertiserName: String,
    isBn: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPauseToggle: () -> Unit,
    onDelete: () -> Unit,
    onExtend: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3C)),
        border = BorderStroke(1.dp, Color(0xFF424242))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(ad.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("🏢 $advertiserName • Package: ${ad.packageName}", fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = when (ad.status) {
                        "Active" -> Color(0xFF2E7D32)
                        "Pending Review", "Pending Payment" -> Color(0xFFEF6C00)
                        "Paused" -> Color.Gray
                        "Rejected" -> Color.Red
                        else -> Color.DarkGray
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(ad.status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Video Preview & Link Verification Box
            Surface(
                color = Color(0xFF1E1E2E),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = if (isBn) "📹 ভিডিও লিঙ্ক ও মিডিয়া প্রিভিউ:" else "📹 Video Media & Target Preview:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80D8FF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (ad.videoUrl.isNotBlank()) {
                            Button(
                                onClick = {
                                    try { uriHandler.openUri(ad.videoUrl) } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBn) "▶️ প্লে / ভিডিও রিভিউ" else "▶️ Play & Review", fontSize = 10.sp)
                            }
                        }
                        if (ad.ctaUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    try { uriHandler.openUri(ad.ctaUrl) } catch (e: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBn) "🌐 টার্গেট পেজ" else "🌐 Target Link", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Analytics progress
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Impressions: ${ad.impressionsCount} / ${ad.impressionLimit}", fontSize = 11.sp, color = Color.LightGray)
                Text("Unique: ${ad.uniqueViewersCount} • Clicks: ${ad.clicksCount}", fontSize = 11.sp, color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (ad.status == "Pending Review" || ad.status == "Pending Payment" || ad.status == "Rejected") {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "⚡ এপ্রুভ করুন & CPA ফিডে লাইভ করুন" else "⚡ Approve & Publish to CPA Feed", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onReject, modifier = Modifier.weight(0.8f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text(if (isBn) "প্রত্যাখ্যান" else "Reject", fontSize = 10.sp)
                    }
                } else if (ad.status == "Active" || ad.status == "Paused") {
                    OutlinedButton(onClick = onPauseToggle, modifier = Modifier.weight(1f)) {
                        Text(if (ad.status == "Paused") "Resume" else "Pause", fontSize = 10.sp, color = Color.White)
                    }
                    OutlinedButton(onClick = onExtend, modifier = Modifier.weight(1f)) {
                        Text("Extend", fontSize = 10.sp, color = Color.White)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AdminAdvertiserProfileCard(
    advertiser: VideoAdvertiser,
    isBn: Boolean,
    onApproveVerification: () -> Unit,
    onRejectVerification: () -> Unit,
    onBlockToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3C)),
        border = BorderStroke(1.dp, Color(0xFF424242))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(advertiser.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("Category: ${advertiser.category} • Contact: ${advertiser.contactPerson}", fontSize = 11.sp, color = Color.Gray)
                    Text("📞 ${advertiser.phone} • ✉️ ${advertiser.email}", fontSize = 11.sp, color = Color.LightGray)
                }
                Surface(
                    color = if (advertiser.verificationStatus == "Verified") Color(0xFF2E7D32) else Color(0xFFEF6C00),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(advertiser.verificationStatus, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (advertiser.verificationStatus != "Verified") {
                    Button(onClick = onApproveVerification, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                        Text(if (isBn) "ভেরিফাই করুন" else "Verify", fontSize = 10.sp)
                    }
                }
                OutlinedButton(onClick = onBlockToggle, modifier = Modifier.weight(1f)) {
                    Text(if (advertiser.isBlocked) "Unblock" else "Block", fontSize = 10.sp, color = if (advertiser.isBlocked) Color.Green else Color.Red)
                }
            }
        }
    }
}

@Composable
fun AdminPaymentProofCard(
    payment: VideoAdPayment,
    adTitle: String,
    advertiserName: String,
    isBn: Boolean,
    onVerify: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3C)),
        border = BorderStroke(1.dp, Color(0xFF424242))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(advertiserName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("Ad: $adTitle • Method: ${payment.paymentMethod}", fontSize = 11.sp, color = Color.Gray)
                    Text("TrxID: ${payment.transactionId} • Amount: ৳${payment.amount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
                Surface(
                    color = if (payment.status == "Verified") Color(0xFF2E7D32) else Color(0xFFEF6C00),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(payment.status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            if (payment.status == "Pending") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onVerify, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                        Text(if (isBn) "পেমেন্ট ভেরিফাই করুন" else "Verify Payment")
                    }
                    Button(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text(if (isBn) "বাতিল" else "Reject")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPackageEditCard(
    pkg: VideoAdPackage,
    isBn: Boolean,
    onSave: (VideoAdPackage) -> Unit
) {
    var priceText by remember(pkg) { mutableStateOf(pkg.price.toInt().toString()) }
    var durationText by remember(pkg) { mutableStateOf(pkg.durationDays.toString()) }
    var limitText by remember(pkg) { mutableStateOf(pkg.impressionLimit.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3C)),
        border = BorderStroke(1.dp, Color(0xFF424242))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${pkg.name} Package Configuration", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price (৳)", fontSize = 10.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = durationText, onValueChange = { durationText = it }, label = { Text("Days", fontSize = 10.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = limitText, onValueChange = { limitText = it }, label = { Text("Impressions", fontSize = 10.sp) }, modifier = Modifier.weight(1f), singleLine = true)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: pkg.price
                    val d = durationText.toIntOrNull() ?: pkg.durationDays
                    val l = limitText.toIntOrNull() ?: pkg.impressionLimit
                    onSave(pkg.copy(price = p, durationDays = d, impressionLimit = l))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text(if (isBn) "প্যাকেজ সেভ করুন" else "Save Package")
            }
        }
    }
}
