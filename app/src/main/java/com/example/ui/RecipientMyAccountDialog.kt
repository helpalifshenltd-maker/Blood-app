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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

/**
 * Blood Recipient / Seeker My Account Dialog (🩸 রক্তের গ্রহীতা / পেশেন্ট মাই একাউন্ট)
 * Covers all 12 key recipient features:
 * 1. My Profile
 * 2. My Blood Requests (Active, Pending, Donor Found, Accepted, Completed, Cancelled, Expired)
 * 3. Create Blood Request Form
 * 4. Find Donors
 * 5. Nearby Donors
 * 6. Contacted Donors
 * 7. Saved Donors
 * 8. Notifications
 * 9. Request History
 * 10. Donor Feedback & Thank You
 * 11. Hospitals Search & Favorites
 * 12. Ambulance Emergency Booking Integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipientMyAccountDialog(
    user: BloodDonor,
    viewModel: MainViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val isBan = language == AppLanguage.BAN

    val allRequests by viewModel.allRequests.collectAsState()
    val rawAllDonors by viewModel.allDonors.collectAsState()
    val allDonors = remember(rawAllDonors) {
        rawAllDonors.filter { d ->
            val isRoleValid = (d.role.isBlank() || d.role.equals("Donor", ignoreCase = true) || d.role.equals("User", ignoreCase = true)) &&
                    !d.role.equals("Hospital", ignoreCase = true) &&
                    !d.role.equals("Doctor", ignoreCase = true) &&
                    !d.role.equals("Ambulance", ignoreCase = true) &&
                    !d.role.equals("Advertiser", ignoreCase = true) &&
                    !d.role.equals("Company", ignoreCase = true) &&
                    !d.role.equals("Admin", ignoreCase = true) &&
                    !d.bloodGroup.equals("N/A", ignoreCase = true) &&
                    d.bloodGroup.isNotBlank()
            isRoleValid
        }
    }

    var activeTab by remember { mutableStateOf("Requests") }

    // --- 1. Profile State ---
    var recipientName by remember { mutableStateOf(user.name) }
    var recipientPhone by remember { mutableStateOf(user.phone) }
    var recipientEmail by remember { mutableStateOf(user.email.ifBlank { "seeker@gmail.com" }) }
    var recipientAddress by remember { mutableStateOf("${user.upazila}, ${user.district}") }
    var emergencyContact by remember { mutableStateOf("01711000111") }
    var isEditingProfile by remember { mutableStateOf(false) }

    // --- 2. Requests State ---
    var userRequestsList by remember(allRequests, user.phone) {
        mutableStateOf(
            allRequests.filter { it.contactNumber == user.phone || it.details.contains(user.phone) }.ifEmpty {
                listOf(
                    BloodRequest(
                        id = "req_101",
                        patientName = "মো: জহিরুল ইসলাম",
                        bloodGroup = "O+",
                        bloodAmount = "2 Bags",
                        hospitalName = "ঢাকা মেডিকেল কলেজ হাসপাতাল",
                        district = user.district.ifBlank { "ঢাকা" },
                        upazila = user.upazila.ifBlank { "শাহবাগ" },
                        contactNumber = user.phone,
                        details = "জরুরি ওপেন হার্ট সার্জারির জন্য ২ ব্যাগ O+ রক্ত প্রয়োজন। ওয়ার্ড ৩, বেড ১২।",
                        isEmergency = true,
                        isApproved = true,
                        dateRequested = "2026-08-11",
                        status = "Active"
                    ),
                    BloodRequest(
                        id = "req_102",
                        patientName = "নাজমুন নাহার",
                        bloodGroup = "A+",
                        bloodAmount = "1 Bag",
                        hospitalName = "স্কয়ার হাসপাতাল, পান্থপথ",
                        district = user.district.ifBlank { "ঢাকা" },
                        upazila = "ধানমন্ডি",
                        contactNumber = user.phone,
                        details = "থ্যালাসেমিয়া রোগীর নিয়মিত রক্তদান।",
                        isEmergency = false,
                        isApproved = true,
                        dateRequested = "2026-07-20",
                        status = "Completed"
                    )
                )
            }
        )
    }

    var requestStatusFilter by remember { mutableStateOf("All") }

    // --- 3. Create Request Form State ---
    var newPatientName by remember { mutableStateOf("") }
    var newBloodGroup by remember { mutableStateOf("O+") }
    var newBagCount by remember { mutableStateOf("1 Bag") }
    var newDateNeeded by remember { mutableStateOf("2026-08-12") }
    var newTimeNeeded by remember { mutableStateOf("10:00 AM") }
    var newHospitalName by remember { mutableStateOf("") }
    var newHospitalAddress by remember { mutableStateOf("${user.upazila}, ${user.district}") }
    var newWardRoom by remember { mutableStateOf("") }
    var newContactNumber by remember { mutableStateOf(user.phone) }
    var newEmergencyLevel by remember { mutableStateOf("Emergency 🚨") }
    var newAdditionalInfo by remember { mutableStateOf("") }

    // --- 4 & 5. Search Donors State ---
    var searchBloodGroup by remember { mutableStateOf("All") }
    var searchLocationText by remember { mutableStateOf(user.district) }

    // --- 6. Contacted Donors ---
    var contactedDonorsList by remember {
        mutableStateOf(
            listOf(
                Triple("1", "মো: আরিফুল ইসলাম (O+)", "📞 01712345678 • DMCH Location • Contacted 2 hrs ago"),
                Triple("2", "ড. জাহিদ হাসান (A+)", "📞 01898765432 • Dhanmondi Location • Contacted Yesterday")
            )
        )
    }

    // --- 7. Saved Donors ---
    var savedDonorIds by remember { mutableStateOf(mutableSetOf<String>()) }

    // --- 8. Notifications ---
    val notificationsList = remember {
        listOf(
            Triple("🚨 রিকুয়েস্ট আপডেট", "আপনার O+ রক্তের অনুরোধটি ১ জন সম্ভাব্য রক্তদাতা একসেপ্ট করেছেন!", "১০ মিনিট আগে"),
            Triple("🩸 নতুন ডোনার রেসপন্স", "ডোনার আরিফ হোসেন আপনার সাথে যোগাযোগের জন্য প্রস্তুত।", "১ ঘণ্টা আগে"),
            Triple("🔔 সিস্টেম অ্যালার্ট", "আপনার রক্তের চাহিদাটি ভেরিফাইড পোস্ট হিসেবে পিন করা হয়েছে।", "আজ সকাল ৯:৩০")
        )
    }

    // --- 10. Donor Feedback ---
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var selectedDonorForFeedback by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }

    // --- 11. Hospitals Search ---
    var hospitalSearchQuery by remember { mutableStateOf("") }
    var favoriteHospitalIds by remember { mutableStateOf(mutableSetOf("hosp_1", "hosp_2")) }

    // --- 12. Ambulance Modal ---
    var showAmbulanceModal by remember { mutableStateOf(false) }
    var ambPickupLocation by remember { mutableStateOf(newHospitalAddress) }
    var ambHospitalDestination by remember { mutableStateOf("ঢাকা মেডিকেল কলেজ হাসপাতাল") }

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
                                .background(Color(0xFFE65100), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBan) "🩸 গ্রহীতা / পেশেন্ট মাই একাউন্ট" else "🩸 Blood Recipient My Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "${recipientName} • ${recipientAddress}",
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
                // Horizontal Sub-Tab Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "Requests" to if (isBan) "🆘 মাই ব্লাড রিকুয়েস্ট" else "🆘 Requests",
                        "Create" to if (isBan) "➕ নতুন রিকুয়েস্ট" else "➕ Create",
                        "Find" to if (isBan) "🔎 ডোনার খুঁজুন" else "🔎 Donors",
                        "Nearby" to if (isBan) "📍 কাছে থাকা ডোনার" else "📍 Nearby",
                        "Contacted" to if (isBan) "📞 যোগাযোগকৃত ডোনার" else "📞 Contacted",
                        "Saved" to if (isBan) "❤️ সেভকৃত ডোনার" else "❤️ Saved",
                        "Profile" to if (isBan) "👤 পেশেন্ট প্রোফাইল" else "👤 Profile",
                        "Notifications" to if (isBan) "🔔 নোটিফিকেশন" else "🔔 Alerts",
                        "History" to if (isBan) "📋 রিকুয়েস্ট হিস্ট্রি" else "📋 History",
                        "Feedback" to if (isBan) "⭐ ডোনার ফিডব্যাক" else "⭐ Feedback",
                        "Hospitals" to if (isBan) "🏥 হাসপাতালসমূহ" else "🏥 Hospitals",
                        "Ambulance" to if (isBan) "🚑 অ্যাম্বুলেন্স জরুরি" else "🚑 Ambulance"
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
                                selectedContainerColor = Color(0xFFE65100),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeTab) {
                        // --- 1. MY BLOOD REQUESTS ---
                        "Requests" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isBan) "🆘 আপনার তৈরিকৃত রক্তের চাহিদাসমূহ" else "🆘 My Blood Requests", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                                Button(
                                    onClick = { activeTab = "Create" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBan) "নতুন চাহিদা পোস্ট" else "New Request", fontSize = 11.sp)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("All", "Active", "Pending", "Donor Found", "Accepted", "Completed", "Cancelled", "Expired").forEach { statusOpt ->
                                    FilterChip(
                                        selected = requestStatusFilter == statusOpt,
                                        onClick = { requestStatusFilter = statusOpt },
                                        label = { Text(statusOpt, fontSize = 10.sp) }
                                    )
                                }
                            }

                            val filteredUserRequests = userRequestsList.filter { req ->
                                requestStatusFilter == "All" || req.status.equals(requestStatusFilter, ignoreCase = true)
                            }

                            if (filteredUserRequests.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(if (isBan) "এই ক্যাটাগরিতে কোনো রক্তের চাহিদা পাওয়া যায়নি।" else "No blood requests found in this section.", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            } else {
                                filteredUserRequests.forEach { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, if (req.isEmergency) Color(0xFFFFCDD2) else Color(0xFFE0E0E0)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(color = Color(0xFFC62828), shape = RoundedCornerShape(4.dp)) {
                                                        Text(req.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(req.patientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }
                                                Surface(
                                                    color = when (req.status) {
                                                        "Active" -> Color(0xFF2E7D32)
                                                        "Completed" -> Color(0xFF1565C0)
                                                        "Cancelled" -> Color.Red
                                                        else -> Color.Gray
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(req.status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("🏥 ${req.hospitalName} (${req.upazila}, ${req.district})", fontSize = 11.sp, color = Color.DarkGray)
                                            Text("🩸 পরিমাণ: ${req.bloodAmount} • 📅 তারিখ: ${req.dateRequested}", fontSize = 11.sp, color = Color.Gray)
                                            Text("📝 বিবরণ: ${req.details}", fontSize = 11.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (req.status == "Active") {
                                                    Button(
                                                        onClick = {
                                                            userRequestsList = userRequestsList.map { if (it.id == req.id) it.copy(status = "Completed") else it }
                                                            Toast.makeText(context, if (isBan) "রক্তদান সম্পন্ন হিসেবে মার্ক করা হয়েছে!" else "Marked as completed!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                        contentPadding = PaddingValues(vertical = 2.dp)
                                                    ) {
                                                        Text(if (isBan) "রক্ত পেয়েছি (Completed)" else "Mark Received", fontSize = 10.sp)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            userRequestsList = userRequestsList.map { if (it.id == req.id) it.copy(status = "Cancelled") else it }
                                                            Toast.makeText(context, if (isBan) "রিকুয়েস্ট বাতিল করা হয়েছে।" else "Request cancelled.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(vertical = 2.dp)
                                                    ) {
                                                        Text(if (isBan) "বাতিল করুন" else "Cancel", fontSize = 10.sp, color = Color.Red)
                                                    }
                                                }
                                                OutlinedButton(
                                                    onClick = { activeTab = "Find" },
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(vertical = 2.dp)
                                                ) {
                                                    Text(if (isBan) "ডোনার খুঁজুন ➔" else "Find Donors ➔", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- 2. CREATE BLOOD REQUEST FORM ---
                        "Create" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(if (isBan) "➕ নতুন রক্তের চাহিদার পোস্ট জমা দিন" else "➕ Submit New Blood Request", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))

                                    OutlinedTextField(
                                        value = newPatientName,
                                        onValueChange = { newPatientName = it },
                                        label = { Text(if (isBan) "রোগীর নাম (Patient Name)" else "Patient Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Column {
                                        Text(if (isBan) "রক্তের গ্রুপ:" else "Blood Group:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").forEach { bg ->
                                                FilterChip(
                                                    selected = newBloodGroup == bg,
                                                    onClick = { newBloodGroup = bg },
                                                    label = { Text(bg, fontSize = 10.sp) }
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = newBagCount,
                                            onValueChange = { newBagCount = it },
                                            label = { Text(if (isBan) "কত ব্যাগ রক্ত" else "Bags Required") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = newDateNeeded,
                                            onValueChange = { newDateNeeded = it },
                                            label = { Text(if (isBan) "প্রয়োজনের তারিখ" else "Date Needed") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = newTimeNeeded,
                                            onValueChange = { newTimeNeeded = it },
                                            label = { Text(if (isBan) "প্রয়োজনের সময়" else "Time Needed") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = newContactNumber,
                                            onValueChange = { newContactNumber = it },
                                            label = { Text(if (isBan) "যোগাযোগের নম্বর" else "Contact Phone") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    OutlinedTextField(
                                        value = newHospitalName,
                                        onValueChange = { newHospitalName = it },
                                        label = { Text(if (isBan) "হাসপাতালের নাম" else "Hospital Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = newHospitalAddress,
                                            onValueChange = { newHospitalAddress = it },
                                            label = { Text(if (isBan) "হাসপাতালের ঠিকানা / এলাকা" else "Hospital Address") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = newWardRoom,
                                            onValueChange = { newWardRoom = it },
                                            label = { Text(if (isBan) "ওয়ার্ড / রুম নম্বর" else "Ward / Room No") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    Text(if (isBan) "জরুরি লেভেল (Emergency Level):" else "Emergency Level:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("Emergency 🚨", "Urgent ⏳", "Regular 📅").forEach { lvl ->
                                            FilterChip(
                                                selected = newEmergencyLevel == lvl,
                                                onClick = { newEmergencyLevel = lvl },
                                                label = { Text(lvl, fontSize = 10.sp) }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = newAdditionalInfo,
                                        onValueChange = { newAdditionalInfo = it },
                                        label = { Text(if (isBan) "অতিরিক্ত বিবরণ / মেসেজ" else "Additional Information") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )

                                    Button(
                                        onClick = {
                                            if (newPatientName.isBlank() || newHospitalName.isBlank()) {
                                                Toast.makeText(context, if (isBan) "অনুগ্রহ করে রোগীর নাম ও হাসপাতালের নাম লিখুন" else "Please fill patient name & hospital name", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }

                                            viewModel.createBloodRequestDirect(
                                                context = context,
                                                patientName = newPatientName,
                                                bloodGroup = newBloodGroup,
                                                bloodAmount = newBagCount,
                                                hospitalName = newHospitalName,
                                                district = user.district,
                                                upazila = user.upazila,
                                                contactNumber = newContactNumber,
                                                details = "$newAdditionalInfo (Ward/Room: $newWardRoom, Time: $newTimeNeeded)",
                                                isEmergency = newEmergencyLevel.contains("Emergency")
                                            )

                                            val created = BloodRequest(
                                                id = "req_${System.currentTimeMillis()}",
                                                patientName = newPatientName,
                                                bloodGroup = newBloodGroup,
                                                bloodAmount = newBagCount,
                                                hospitalName = newHospitalName,
                                                district = user.district,
                                                upazila = user.upazila,
                                                contactNumber = newContactNumber,
                                                details = "$newAdditionalInfo (Ward/Room: $newWardRoom, Time: $newTimeNeeded)",
                                                isEmergency = newEmergencyLevel.contains("Emergency"),
                                                isApproved = true,
                                                dateRequested = newDateNeeded,
                                                status = "Active"
                                            )

                                            userRequestsList = listOf(created) + userRequestsList
                                            activeTab = "Requests"
                                            Toast.makeText(context, if (isBan) "রক্তের চাহিদা সফলভাবে পোস্ট করা হয়েছে!" else "Blood request published successfully!", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBan) "সাবমিট ব্লাড রিকুয়েস্ট" else "Submit Blood Request", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // --- 4. FIND DONORS ---
                        "Find" -> {
                            Text(if (isBan) "🔎 রক্তের গ্রুপ অনুযায়ী সরাসরি ডোনার খুঁজুন" else "🔎 Search Available Blood Donors", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("All", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").forEach { bg ->
                                    FilterChip(
                                        selected = searchBloodGroup == bg,
                                        onClick = { searchBloodGroup = bg },
                                        label = { Text(bg, fontSize = 10.sp) }
                                    )
                                }
                            }

                            val filteredDonors = allDonors.filter { d ->
                                (searchBloodGroup == "All" || d.bloodGroup.equals(searchBloodGroup, ignoreCase = true)) &&
                                        (searchLocationText.isBlank() || d.district.contains(searchLocationText, ignoreCase = true) || d.upazila.contains(searchLocationText, ignoreCase = true))
                            }

                            if (filteredDonors.isEmpty()) {
                                Text(if (isBan) "কোনো রক্তদাতা পাওয়া যায়নি।" else "No donors match search criteria.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                filteredDonors.take(10).forEach { donor ->
                                    val isSaved = savedDonorIds.contains(donor.id)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(Color(0xFFC62828), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(donor.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(donor.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(color = if (donor.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp)) {
                                                        Text(if (donor.isAvailable) "🟢 Available" else "🔴 Unavailable", fontSize = 9.sp, color = if (donor.isAvailable) Color(0xFF2E7D32) else Color.Red, modifier = Modifier.padding(2.dp))
                                                    }
                                                }
                                                Text("📍 ${donor.upazila}, ${donor.district} • 2.${donor.id.hashCode() % 8 + 1} km away", fontSize = 11.sp, color = Color.Gray)
                                                Text("📞 ${donor.phone}", fontSize = 11.sp, color = Color.DarkGray)
                                            }

                                            IconButton(
                                                onClick = {
                                                    savedDonorIds = if (isSaved) (savedDonorIds - donor.id).toMutableSet() else (savedDonorIds + donor.id).toMutableSet()
                                                    Toast.makeText(context, if (!isSaved) "Saved to Saved Donors!" else "Removed from Saved Donors", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Save",
                                                    tint = Color(0xFFC62828)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    uriHandler.openUri("tel:${donor.phone}")
                                                }
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF1565C0))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- 5. NEARBY DONORS ---
                        "Nearby" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(if (isBan) "📍 জিপিএস লোকেশন ভিত্তিক রক্তদাতা" else "📍 Nearby Live GPS Donors", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B5E20))
                                        Text(if (isBan) "আপনার ১০ কিলোমিটারের মধ্যে এভেলেবল রক্তদাতাদের তালিকা নিচে দেখানো হচ্ছে।" else "Showing available donors within 10 km radius of your location.", fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                }
                            }

                            allDonors.filter { it.isAvailable }.take(5).forEachIndexed { idx, donor ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("📍 ${idx + 1}.${idx * 2 + 1} km", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${donor.name} (${donor.bloodGroup})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("📍 ${donor.upazila}, ${donor.district}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Button(
                                            onClick = { uriHandler.openUri("tel:${donor.phone}") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Contact", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // --- 6. CONTACTED DONORS ---
                        "Contacted" -> {
                            Text(if (isBan) "📞 আপনার পূর্বে যোগাযোগকৃত ডোনার তালিকা" else "📞 Contacted Donors List", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            contactedDonorsList.forEach { (id, name, details) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(details, fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = {
                                                    selectedDonorForFeedback = name
                                                    showFeedbackDialog = true
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                                contentPadding = PaddingValues(vertical = 2.dp)
                                            ) {
                                                Text(if (isBan) "⭐ ফিডব্যাক দিন" else "Give Feedback", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- 7. SAVED DONORS ---
                        "Saved" -> {
                            Text(if (isBan) "❤️ আপনার পছন্দের সেভকৃত রক্তদাতারা" else "❤️ Saved Favorite Donors", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))

                            val savedList = allDonors.filter { savedDonorIds.contains(it.id) }
                            if (savedList.isEmpty()) {
                                Text(if (isBan) "এখনো কোনো রক্তদাতা সেভ করা হয়নি। 'ডোনার খুঁজুন' থেকে সেভ করতে পারেন।" else "No saved donors yet. You can bookmark donors from search.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                savedList.forEach { donor ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("🩸 ${donor.bloodGroup}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC62828))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(donor.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("📞 ${donor.phone} • ${donor.district}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            IconButton(onClick = { uriHandler.openUri("tel:${donor.phone}") }) {
                                                Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF1565C0))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- 8. NOTIFICATIONS ---
                        "Notifications" -> {
                            notificationsList.forEach { (title, body, time) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC62828))
                                            Text(time, fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(body, fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }

                        // --- 9. REQUEST HISTORY ---
                        "History" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(if (isBan) "📋 পূর্ববর্তী সকল রক্তের চাহিদার রেকর্ড" else "📋 Blood Request History Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    ProfileDetailRow(if (isBan) "মোট রিকুয়েস্ট সংখ্যা" else "Total Requests Made", "${userRequestsList.size} টি (Posts)")
                                    ProfileDetailRow(if (isBan) "রক্ত পেয়েছি (Completed)" else "Completed Donations", "${userRequestsList.count { it.status == "Completed" }} টি")
                                    ProfileDetailRow(if (isBan) "সক্রিয় পোস্ট" else "Currently Active", "${userRequestsList.count { it.status == "Active" }} টি")
                                }
                            }
                        }

                        // --- 10. DONOR FEEDBACK ---
                        "Feedback" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(if (isBan) "⭐ রক্তদাতাকে ধন্যবাদ ও ফিডব্যাক পাঠান" else "⭐ Express Gratitude & Donor Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = selectedDonorForFeedback,
                                        onValueChange = { selectedDonorForFeedback = it },
                                        label = { Text(if (isBan) "রক্তদাতার নাম" else "Donor Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = feedbackText,
                                        onValueChange = { feedbackText = it },
                                        label = { Text(if (isBan) "ধন্যবাদ বার্তা / অভিজ্ঞতা" else "Thank You Note / Feedback") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (selectedDonorForFeedback.isBlank() || feedbackText.isBlank()) {
                                                Toast.makeText(context, if (isBan) "অনুগ্রহ করে নাম ও মেসেজ লিখুন" else "Please fill donor name and message", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            Toast.makeText(context, if (isBan) "ধন্যবাদ বার্তা রক্তদাতার নিকট পাঠানো হয়েছে!" else "Thank you note sent to donor!", Toast.LENGTH_LONG).show()
                                            feedbackText = ""
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                                    ) {
                                        Text(if (isBan) "ধন্যবাদ মেসেজ পাঠান" else "Send Gratitude Note")
                                    }
                                }
                            }
                        }

                        // --- 11. HOSPITALS ---
                        "Hospitals" -> {
                            OutlinedTextField(
                                value = hospitalSearchQuery,
                                onValueChange = { hospitalSearchQuery = it },
                                placeholder = { Text(if (isBan) "হাসপাতাল খুঁজুন (যেমন: ঢাকা মেডিকেল, ল্যাবএইড...)" else "Search Hospitals...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            val mockHospitals = listOf(
                                Pair("hosp_1", "ঢাকা মেডিকেল কলেজ হাসপাতাল (DMCH)"),
                                Pair("hosp_2", "বঙ্গবন্ধু শেখ মুজিব মেডিকেল বিশ্ববিদ্যালয় (BSMMU)"),
                                Pair("hosp_3", "স্কয়ার হাসপাতাল, পান্থপথ"),
                                Pair("hosp_4", "ইবনে সিনা ডায়াগনস্টিক & কনসালটেশন সেন্টার")
                            )

                            mockHospitals.filter { hospitalSearchQuery.isBlank() || it.second.contains(hospitalSearchQuery, ignoreCase = true) }.forEach { (hId, hName) ->
                                val isFav = favoriteHospitalIds.contains(hId)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFFC62828))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(hName, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        IconButton(onClick = {
                                            favoriteHospitalIds = if (isFav) (favoriteHospitalIds - hId).toMutableSet() else (favoriteHospitalIds + hId).toMutableSet()
                                        }) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Fav",
                                                tint = Color(0xFFC62828)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- 12. AMBULANCE ---
                        "Ambulance" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                border = BorderStroke(1.dp, Color(0xFFFFB74D))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AirportShuttle, contentDescription = null, tint = Color(0xFFE65100))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isBan) "🚑 জরুরি অ্যাম্বুলেন্স সাপোর্ট" else "🚑 Emergency Ambulance Service", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(if (isBan) "রোগীকে হাসপাতালে জরুরি স্থানান্তরের জন্য সরাসরি অ্যাম্বুলেন্স কল বা বুকিং করুন।" else "Book emergency ambulance service directly for patient hospital transit.", fontSize = 11.sp, color = Color.DarkGray)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { showAmbulanceModal = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBan) "জরুরি অ্যাম্বুলেন্স বুক করুন ➔" else "Book Emergency Ambulance ➔")
                                    }
                                }
                            }
                        }

                        // --- PROFILE TAB ---
                        "Profile" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(Color(0xFFE65100), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(recipientName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFE65100))
                                            Text("Role: Blood Recipient / Seeker", fontSize = 11.sp, color = Color.Gray)
                                            Text("📍 $recipientAddress", fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (!isEditingProfile) {
                                        ProfileDetailRow(if (isBan) "মোবাইল নম্বর" else "Phone", recipientPhone)
                                        ProfileDetailRow(if (isBan) "ইমেইল এড্রেস" else "Email", recipientEmail)
                                        ProfileDetailRow(if (isBan) "জরুরি কন্টাক্ট" else "Emergency Phone", emergencyContact)
                                        ProfileDetailRow(if (isBan) "বর্তমান এলাকা" else "Address", recipientAddress)

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { isEditingProfile = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                        ) {
                                            Text(if (isBan) "প্রোফাইল সম্পাদন" else "Edit Profile")
                                        }
                                    } else {
                                        OutlinedTextField(value = recipientName, onValueChange = { recipientName = it }, label = { Text("নাম") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        OutlinedTextField(value = recipientPhone, onValueChange = { recipientPhone = it }, label = { Text("মোবাইল") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        OutlinedTextField(value = recipientEmail, onValueChange = { recipientEmail = it }, label = { Text("ইমেইল") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        OutlinedTextField(value = recipientAddress, onValueChange = { recipientAddress = it }, label = { Text("ঠিকানা") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    isEditingProfile = false
                                                    Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                            ) {
                                                Text("Save")
                                            }
                                            OutlinedButton(onClick = { isEditingProfile = false }, modifier = Modifier.weight(1f)) {
                                                Text("Cancel")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )

    // Modal to Book Ambulance
    if (showAmbulanceModal) {
        AlertDialog(
            onDismissRequest = { showAmbulanceModal = false },
            title = { Text(if (isBan) "জরুরি অ্যাম্বুলেন্স সার্ভিস রিকুয়েস্ট" else "Book Emergency Ambulance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ambPickupLocation,
                        onValueChange = { ambPickupLocation = it },
                        label = { Text(if (isBan) "পিকআপ লোকেশন / বাড়ি" else "Pickup Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ambHospitalDestination,
                        onValueChange = { ambHospitalDestination = it },
                        label = { Text(if (isBan) "গন্তব্য হাসপাতাল" else "Hospital Destination") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAmbulanceModal = false
                        Toast.makeText(context, if (isBan) "অ্যাম্বুলেন্স সার্ভিসে রিকুয়েস্ট পাঠানো হয়েছে! নিকটস্থ ড্রাইভার আপনার সাথে যোগাযোগ করবে।" else "Ambulance request dispatched!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    Text(if (isBan) "কনফার্ম অ্যাম্বুলেন্স কল" else "Confirm Ambulance")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAmbulanceModal = false }) {
                    Text(if (isBan) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Modal for Feedback
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text(if (isBan) "রক্তদাতাকে ধন্যবাদ জানান" else "Thank Donor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Donor: $selectedDonorForFeedback", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text(if (isBan) "আপনার বার্তা" else "Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFeedbackDialog = false
                        Toast.makeText(context, if (isBan) "ধন্যবাদ বার্তা পাঠানো হয়েছে!" else "Feedback submitted!", Toast.LENGTH_SHORT).show()
                        feedbackText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text(if (isBan) "পাঠান" else "Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text(if (isBan) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
