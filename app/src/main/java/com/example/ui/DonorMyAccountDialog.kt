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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Complete Blood Donor My Account Dialog (ডোনার মাই একাউন্ট)
 * Covers all 12 key donor account sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorMyAccountDialog(
    user: BloodDonor,
    viewModel: MainViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBan = language == AppLanguage.BAN
    val allRequests by viewModel.allRequests.collectAsState()

    var activeTab by remember { mutableStateOf("Profile") }

    // --- State 1: Profile ---
    var donorName by remember { mutableStateOf(user.name) }
    var donorPhone by remember { mutableStateOf(user.phone) }
    var donorEmail by remember { mutableStateOf(user.email.ifBlank { "donor@gmail.com" }) }
    var donorBloodGroup by remember { mutableStateOf(user.bloodGroup) }
    var donorDistrict by remember { mutableStateOf(user.district) }
    var donorUpazila by remember { mutableStateOf(user.upazila) }
    var donorVillage by remember { mutableStateOf(user.village) }
    var gender by remember { mutableStateOf("Male") }
    var dob by remember { mutableStateOf("1998-05-15") }
    var emergencyContact by remember { mutableStateOf("01711000000") }
    var isEditingProfile by remember { mutableStateOf(false) }

    // --- State 2: Donation Status ---
    var isAvailableToDonate by remember { mutableStateOf(user.isAvailable) }
    var lastDonationDateStr by remember { mutableStateOf(if (user.lastDonationDate == "Available") "2026-04-10" else user.lastDonationDate) }
    var totalDonationsCount by remember { mutableStateOf(user.donationCount.coerceAtLeast(3)) }

    // Calc Next Eligible Date (90 days after last donation)
    val nextEligibleDateStr = remember(lastDonationDateStr) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(lastDonationDateStr) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, 90)
            sdf.format(cal.time)
        } catch (e: Exception) {
            "2026-07-10"
        }
    }

    val isEligibleNow = remember(lastDonationDateStr) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = sdf.parse(lastDonationDateStr) ?: Date()
            val now = Date()
            val diffMs = now.time - lastDate.time
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            diffDays >= 90
        } catch (e: Exception) {
            true
        }
    }

    // --- State 3: Donation History ---
    var donationHistoryList by remember {
        mutableStateOf(
            listOf(
                Triple("101", "ঢাকা মেডিকেল কলেজ হাসপাতাল", "2026-04-10 • B+ Blood"),
                Triple("102", "বঙ্গবন্ধু শেখ মুজিব মেডিকেল বিশ্ববিদ্যালয় (BSMMU)", "2025-12-15 • B+ Blood"),
                Triple("103", "স্কয়ার হাসপাতাল, পান্থপথ", "2025-08-01 • B+ Blood")
            )
        )
    }
    var showLogDonationModal by remember { mutableStateOf(false) }
    var logHospName by remember { mutableStateOf("") }
    var logDate by remember { mutableStateOf("2026-08-11") }

    // --- State 4: Blood Requests ---
    var acceptedRequestIds by remember { mutableStateOf(mutableSetOf<String>()) }

    // --- State 5: Location & Radius ---
    var currentArea by remember { mutableStateOf("${user.upazila}, ${user.district}") }
    var preferredArea by remember { mutableStateOf("ঢাকা সিটি & আশপাশ") }
    var maxRadiusKm by remember { mutableStateOf(15) }
    var preferredTimeSlot by remember { mutableStateOf("Anytime (২৪/৭)") }

    // --- State 8: Rating & Feedback ---
    val feedbacks = remember {
        listOf(
            Pair("মো: রফিকুল ইসলাম", "আপনার সময়মত রক্তদানের জন্য আমার বাবার অপারেশন সফল হয়েছে। অনেক কৃতজ্ঞতা!"),
            Pair("আফরোজা পারভীন", "জরুরি মুহূর্তে রক্ত দিয়ে জীবন বাঁচিয়েছেন। আল্লাহ আপনাকে দীর্ঘজীবী করুন।")
        )
    }

    // --- State 9: Preferred Hospitals ---
    var favoriteHospitals by remember {
        mutableStateOf(
            listOf(
                "ঢাকা মেডিকেল কলেজ হাসপাতাল (DMCH)",
                "জাতীয় হৃদরোগ ইনস্টিটিউট ও হাসপাতাল",
                "ইবনে সিনা ডায়াগনস্টিক অ্যান্ড কনসালটেশন সেন্টার"
            )
        )
    }

    // --- State 11: Privacy & Safety ---
    var phoneVisibility by remember { mutableStateOf("Registered Users Only") }
    var locationVisibility by remember { mutableStateOf("Public") }
    var whoCanContact by remember { mutableStateOf("Everyone") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFFD32F2F), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bloodtype, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBan) "🩸 ডোনার মাই একাউন্ট" else "🩸 Donor My Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFFB71C1C)
                            )
                            Text(
                                text = "${donorName} • Blood Group: ${donorBloodGroup.ifBlank { "A+" }}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
            ) {
                // Category Tabs Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "Profile" to if (isBan) "👤 প্রোফাইল" else "👤 Profile",
                        "Status" to if (isBan) "🩸 রক্তদানের স্ট্যাটাস" else "🩸 Status",
                        "History" to if (isBan) "📋 রক্তদান ইতিহাস" else "📋 History",
                        "Requests" to if (isBan) "🆘 রক্তের অনুরোধ" else "🆘 Requests",
                        "Location" to if (isBan) "📍 লোকেশন & রেডিয়াস" else "📍 Location",
                        "Badges" to if (isBan) "🏆 অর্জন & ব্যাজ" else "🏆 Badges",
                        "Card" to if (isBan) "📄 ডোনার কার্ড" else "📄 Donor Card",
                        "Feedback" to if (isBan) "⭐ ধন্যবাদ & রিভিউ" else "⭐ Reviews",
                        "Hospitals" to if (isBan) "🏥 পছন্দের হাসপাতাল" else "🏥 Hospitals",
                        "Privacy" to if (isBan) "🔐 প্রাইভেসি" else "🔐 Privacy",
                        "Notifications" to if (isBan) "🔔 নোটিফিকেশন" else "🔔 Alerts",
                        "Settings" to if (isBan) "⚙️ সেটিংস" else "⚙️ Settings"
                    ).forEach { (key, label) ->
                        val isSelected = activeTab == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeTab = key },
                            label = {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Contents
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeTab) {
                        // --- 1. MY PROFILE ---
                        "Profile" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .background(Color(0xFFD32F2F), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = donorBloodGroup.ifBlank { "A+" },
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 20.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(donorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFB71C1C))
                                            Text("ID: ${user.displayUserId}", fontSize = 11.sp, color = Color.Gray)
                                            Text("📍 $donorDistrict, $donorUpazila", fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (!isEditingProfile) {
                                        ProfileDetailRow(if (isBan) "মোবাইল নম্বর" else "Phone Number", donorPhone)
                                        ProfileDetailRow(if (isBan) "ইমেইল এড্রেস" else "Email Address", donorEmail)
                                        ProfileDetailRow(if (isBan) "লিঙ্গ (Gender)" else "Gender", gender)
                                        ProfileDetailRow(if (isBan) "জন্ম তারিখ" else "Date of Birth", dob)
                                        ProfileDetailRow(if (isBan) "জরুরি যোগাযোগ" else "Emergency Contact", emergencyContact)
                                        ProfileDetailRow(if (isBan) "গ্রাম / এলাকা" else "Village / Area", donorVillage)

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { isEditingProfile = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isBan) "প্রোফাইল সম্পাদন করুন" else "Edit Profile", fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        OutlinedTextField(value = donorName, onValueChange = { donorName = it }, label = { Text("নাম") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        OutlinedTextField(value = donorPhone, onValueChange = { donorPhone = it }, label = { Text("মোবাইল") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        OutlinedTextField(value = donorEmail, onValueChange = { donorEmail = it }, label = { Text("ইমেইল") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        OutlinedTextField(value = donorDistrict, onValueChange = { donorDistrict = it }, label = { Text("জেলা") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        OutlinedTextField(value = donorUpazila, onValueChange = { donorUpazila = it }, label = { Text("উপজেলা") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        OutlinedTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = { Text("জরুরি পরিচিত নম্বর") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    isEditingProfile = false
                                                    Toast.makeText(context, if (isBan) "প্রোফাইল সংরক্ষিত হয়েছে!" else "Profile saved!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                            ) {
                                                Text(if (isBan) "সেভ করুন" else "Save")
                                            }
                                            OutlinedButton(onClick = { isEditingProfile = false }, modifier = Modifier.weight(1f)) {
                                                Text(if (isBan) "বাতিল" else "Cancel")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- 2. DONATION STATUS ---
                        "Status" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (isAvailableToDonate) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                                border = BorderStroke(1.dp, if (isAvailableToDonate) Color(0xFF2E7D32) else Color(0xFFC62828))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(
                                                text = if (isAvailableToDonate) "গুগল রক্তদাতা: এভেলেবল (Available)" else "গুগল রক্তদাতা: এখন দিতে অক্ষম (Unavailable)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isAvailableToDonate) Color(0xFF2E7D32) else Color(0xFFC62828)
                                            )
                                            Text(
                                                text = if (isAvailableToDonate) "রক্তের অনুরোধ এলে আপনার সাথে যোগাযোগ করা হবে।" else "আপাতত নতুন অনুরোধ রিসিভ হবে না।",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                        Switch(
                                            checked = isAvailableToDonate,
                                            onCheckedChange = { isAvailableToDonate = it }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Eligibility Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isEligibleNow) Icons.Default.CheckCircle else Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = if (isEligibleNow) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isEligibleNow) (if (isBan) "রক্তদানে প্রস্তুত! (Eligible)" else "Eligible to Donate Blood!") else (if (isBan) "পরবর্তী রক্তদানের অপেক্ষায়" else "Resting Period Active"),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isEligibleNow) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                            )
                                            Text(
                                                text = if (isEligibleNow) "আপনার ৩ মাস পূর্ণ হয়েছে। যেকোনো প্রয়োজনে রক্ত দিতে পারবেন।" else "৩ মাস পূর্ণ হতে কিছু দিন বাকি আছে।",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    ProfileDetailRow(if (isBan) "সর্বশেষ রক্তদানের তারিখ" else "Last Donation Date", lastDonationDateStr)
                                    ProfileDetailRow(if (isBan) "পরবর্তী উপযুক্ত হওয়ার তারিখ" else "Next Eligible Date", nextEligibleDateStr)
                                    ProfileDetailRow(if (isBan) "সর্বমোট রক্তদান সংখ্যা" else "Total Donations", "$totalDonationsCount বার (Times)")
                                }
                            }
                        }

                        // --- 3. DONATION HISTORY ---
                        "History" -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isBan) "📋 রক্তদানের পূর্ববর্তী ইতিহাস" else "📋 Donation History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))
                                OutlinedButton(
                                    onClick = { showLogDonationModal = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBan) "নতুন এন্ট্রি যোগ করুন" else "Log Donation", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            donationHistoryList.forEach { (id, hospital, dateInfo) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFFFEBEE), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(hospital, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(dateInfo, fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                                            Text("Completed", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // --- 4. BLOOD REQUESTS ---
                        "Requests" -> {
                            Text(if (isBan) "🆘 জরুরি রক্তের অনুরোধসমূহ" else "🆘 Emergency Blood Requests", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))
                            Spacer(modifier = Modifier.height(8.dp))

                            val matchedRequests = remember(allRequests, donorBloodGroup) {
                                allRequests.filter { it.status == "Active" }
                            }

                            if (matchedRequests.isEmpty()) {
                                Text(if (isBan) "বর্তমানে কোনো রক্ত চাহিদা সক্রিয় নেই।" else "No active blood requests found.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                matchedRequests.take(4).forEach { req ->
                                    val isAccepted = acceptedRequestIds.contains(req.id)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, if (req.isEmergency) Color(0xFFFFCDD2) else Color(0xFFE0E0E0))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(color = Color(0xFFD32F2F), shape = RoundedCornerShape(4.dp)) {
                                                        Text(req.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(req.patientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                if (req.isEmergency) {
                                                    Text("🚨 EMERGENCY", color = Color(0xFFD32F2F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("🏥 ${req.hospitalName} • ${req.district}", fontSize = 11.sp, color = Color.DarkGray)
                                            Text("📞 Contact: ${req.contactNumber}", fontSize = 11.sp, color = Color.Gray)

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        acceptedRequestIds = (acceptedRequestIds + req.id).toMutableSet()
                                                        Toast.makeText(context, if (isBan) "অনুরোধ গ্রহণ করা হয়েছে! রিকুয়েস্টকারীকে জানানো হবে।" else "Blood Request Accepted!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    enabled = !isAccepted,
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = if (isAccepted) Color.Gray else Color(0xFF2E7D32))
                                                ) {
                                                    Text(if (isAccepted) (if (isBan) "গৃহীত (Accepted)" else "Accepted") else (if (isBan) "রক্ত দিতে রাজি ➔" else "Accept Request ➔"), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- 5. LOCATION & RADIUS ---
                        "Location" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(if (isBan) "📍 অবস্থান & দূরত্বের পরিধি" else "📍 Location & Search Radius", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = currentArea,
                                        onValueChange = { currentArea = it },
                                        label = { Text(if (isBan) "বর্তমান অবস্থান / গ্রাম" else "Current Location") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = preferredArea,
                                        onValueChange = { preferredArea = it },
                                        label = { Text(if (isBan) "পছন্দের রক্তদানের এলাকা" else "Preferred Donation Area") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(if (isBan) "সর্বোচ্চ কত কিলোমিটারের মধ্যে নোটিফিকেশন চান: $maxRadiusKm KM" else "Notification Radius: $maxRadiusKm KM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Slider(
                                        value = maxRadiusKm.toFloat(),
                                        onValueChange = { maxRadiusKm = it.toInt() },
                                        valueRange = 5f..50f,
                                        steps = 9
                                    )
                                }
                            }
                        }

                        // --- 6. BADGES & ACHIEVEMENTS ---
                        "Badges" -> {
                            Text(if (isBan) "🏆 রক্তদাতা সম্মাননা & ব্যাজ" else "🏆 Donor Achievements & Badges", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))
                            Spacer(modifier = Modifier.height(8.dp))

                            val badges = listOf(
                                Triple("🩸 1st Donation", "প্রথমবার রক্তদানের কৃতিত্ব", totalDonationsCount >= 1),
                                Triple("🩸 5 Donations", "৫ বার রক্তদানের বিশেষ মাইলফলক", totalDonationsCount >= 5),
                                Triple("🩸 10 Donations", "১০ বার রক্তদানের গোল্ডেন ব্যাজ", totalDonationsCount >= 10),
                                Triple("🏆 Regular Donor", "নিয়মিত স্বেচ্ছাসেবী রক্তদাতা", totalDonationsCount >= 3),
                                Triple("❤️ Life Saver Hero", "জীবন রক্ষাকারী হিরো সম্মাননা", totalDonationsCount >= 5)
                            )

                            badges.forEach { (badgeTitle, desc, isUnlocked) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = if (isUnlocked) Color(0xFFFFF8E1) else Color(0xFFF5F5F5)),
                                    border = BorderStroke(1.dp, if (isUnlocked) Color(0xFFFFB300) else Color(0xFFE0E0E0))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isUnlocked) "🥇" else "🔒", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(badgeTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isUnlocked) Color(0xFFE65100) else Color.Gray)
                                            Text(desc, fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                        Surface(
                                            color = if (isUnlocked) Color(0xFF2E7D32) else Color.Gray,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(if (isUnlocked) "Unlocked" else "Locked", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // --- 7. DONOR CARD ---
                        "Card" -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(colors = listOf(Color(0xFFB71C1C), Color(0xFFD32F2F), Color(0xFFE53935))))
                                        .padding(16.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("BLOOD DONOR CARD", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                            Text(donorBloodGroup.ifBlank { "A+" }, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        }

                                        Column {
                                            Text(donorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("ID: ${user.displayUserId} • $donorDistrict", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                            Text("Total Donations: $totalDonationsCount times", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("VERIFIED VOLUNTEER DONOR", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("STATUS: ACTIVE", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // --- 8. FEEDBACK ---
                        "Feedback" -> {
                            Text(if (isBan) "⭐ গ্রহীতাদের ধন্যবাদ বার্তা" else "⭐ Recipient Thank You Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            feedbacks.forEach { (author, msg) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Color(0xFFD32F2F))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(author, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(msg, fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }

                        // --- 9. HOSPITALS ---
                        "Hospitals" -> {
                            Text(if (isBan) "🏥 পছন্দের রক্তদান কেন্দ্রসমূহ" else "🏥 Favorite Donation Centers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            favoriteHospitals.forEach { hosp ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF1565C0))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(hosp, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // --- 10. PRIVACY ---
                        "Privacy" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(if (isBan) "🔐 প্রাইভেসি & নিরাপত্তা সেটিংস" else "🔐 Privacy & Safety Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(if (isBan) "মোবাইল নম্বর দৃশ্যমানতা:" else "Phone Visibility:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("Public", "Registered Users Only", "Hidden").forEach { opt ->
                                            FilterChip(
                                                selected = phoneVisibility == opt,
                                                onClick = { phoneVisibility = opt },
                                                label = { Text(opt, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- 11. NOTIFICATIONS ---
                        "Notifications" -> {
                            listOf(
                                Pair("🚨 জরুরি B+ রক্ত প্রয়োজন!", "ঢাকা মেডিকেল কলেজ হাসপাতালে আজই রক্ত প্রয়োজন।"),
                                Pair("🎉 ধন্যবাদ!", "আপনার রক্তদানের ৩ মাস পূর্ণ হয়েছে। এখন আপনি আবার প্রস্তুত।")
                            ).forEach { (title, body) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE65100))
                                        Text(body, fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }

                        // --- 12. SETTINGS ---
                        "Settings" -> {
                            Button(
                                onClick = { viewModel.toggleLanguage() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                            ) {
                                Text(if (isBan) "ভাষা পরিবর্তন (Switch to English)" else "Switch to Bangla")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )

    // Modal to Log Donation
    if (showLogDonationModal) {
        AlertDialog(
            onDismissRequest = { showLogDonationModal = false },
            title = { Text(if (isBan) "নতুন রক্তদান রেকর্ড যুক্ত করুন" else "Log New Blood Donation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = logHospName,
                        onValueChange = { logHospName = it },
                        label = { Text("হাসপাতাল / মেডিকেল সেন্টার") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = logDate,
                        onValueChange = { logDate = it },
                        label = { Text("রক্তদানের তারিখ (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (logHospName.isNotBlank()) {
                            donationHistoryList = listOf(Triple("${System.currentTimeMillis()}", logHospName, "$logDate • $donorBloodGroup Blood")) + donationHistoryList
                            lastDonationDateStr = logDate
                            totalDonationsCount += 1
                            showLogDonationModal = false
                            Toast.makeText(context, if (isBan) "রেকর্ড সংরক্ষিত হয়েছে!" else "Donation Logged!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(if (isBan) "সংরক্ষণ করুন" else "Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogDonationModal = false }) {
                    Text(if (isBan) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}
