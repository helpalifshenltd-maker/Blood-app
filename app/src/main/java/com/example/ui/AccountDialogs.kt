package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

// Data class for Family Members
data class FamilyMember(
    val id: String,
    val name: String,
    val relation: String, // Father, Mother, Spouse, Child, Other
    val phone: String,
    val dob: String,
    val gender: String,
    val bloodGroup: String
)

// Data class for User Reviews
data class UserReview(
    val id: String,
    val type: String, // Hospital, Doctor, Service
    val targetName: String,
    val rating: Float,
    val comment: String,
    val date: String
)

// Data class for Medical Records
data class MedicalRecord(
    val id: String,
    val type: String, // Prescription, Lab Report, Medical Document, Previous Report
    val title: String,
    val doctorOrHospital: String,
    val date: String,
    val note: String = ""
)

// Data class for Diagnostic Reports
data class DiagnosticReport(
    val id: String,
    val testType: String, // Blood Test, X-Ray, MRI, CT Scan, Diagnostic
    val title: String,
    val centerName: String,
    val date: String,
    val status: String // Ready, Processing
)

// Data class for Support Ticket
data class SupportTicket(
    val id: String,
    val subject: String,
    val category: String, // Booking, Hospital, General
    val description: String,
    val status: String, // Open, Answered, Closed
    val date: String
)

// --- 1. DIGITAL HEALTH CARD DIALOG ---
@Composable
fun DigitalHealthCardDialog(
    user: BloodDonor,
    language: AppLanguage,
    isAdvancePlanUser: Boolean = false,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "ডিজিটাল হেলথ কার্ড" else "Digital Health Card",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card Graphic
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("DIGITAL HEALTH CARD", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = user.bloodGroup.ifBlank { "A+" },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0D47A1), modifier = Modifier.size(36.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("ID: ${user.displayUserId}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                                    Text("Phone: ${user.phone}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    val memText = if (isAdvancePlanUser) {
                                        if (isBan) "সদস্যপদ: অ্যাডভান্স প্ল্যান" else "Membership: Advance Plan"
                                    } else {
                                        if (isBan) "সদস্যপদ: ফ্রি প্ল্যান (Free)" else "Membership: Free Plan"
                                    }
                                    Text(memText, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                                    Text(if (isBan) "জরুরি হটলাইন: ৯৯৯" else "Emergency: 999", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                // QR Code placeholder
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color.White, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = Color.Black, modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isBan) "মেম্বারশিপ সুবিধা & ডিসকাউন্ট:" else "Membership Discounts & Benefits:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                listOf(
                    Pair(if (isBan) "পার্টনার হাসপাতালে টেস্টে ২০% ছাড়" else "20% Discount on Diagnostic Tests", Icons.Default.Discount),
                    Pair(if (isBan) "জরুরি অ্যাম্বুলেন্স ভাড়ায় বিশেষ ছাড়" else "Special Discount on Ambulance Fare", Icons.Default.AirportShuttle),
                    Pair(if (isBan) "প্রাইওরিটি ডক্টর অ্যাপয়েন্টমেন্ট সুবিধা" else "Priority Doctor Appointment Access", Icons.Default.Star)
                ).forEach { (benefit, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(benefit, fontSize = 12.sp, color = Color.Black)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text(if (isBan) "ঠিক আছে" else "Close")
            }
        }
    )
}

// --- 2. FAMILY MEMBERS DIALOG ---
@Composable
fun FamilyMembersDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current

    var membersList by remember {
        mutableStateOf(
            listOf(
                FamilyMember("1", "মো: আব্দুর রহিম", if (isBan) "বাবা" else "Father", "01711122334", "1965-05-12", "Male", "A+"),
                FamilyMember("2", "মোসাম্মাৎ রহিমা বেগম", if (isBan) "মা" else "Mother", "01722233445", "1970-08-20", "Female", "O+")
            )
        )
    }

    var showAddForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRelation by remember { mutableStateOf(if (isBan) "বাবা" else "Father") }
    var newPhone by remember { mutableStateOf("") }
    var newDob by remember { mutableStateOf("") }
    var newGender by remember { mutableStateOf("Male") }
    var newBloodGroup by remember { mutableStateOf("A+") }

    val relations = listOf(
        if (isBan) "বাবা" else "Father",
        if (isBan) "মা" else "Mother",
        if (isBan) "স্ত্রী/স্বামী" else "Spouse",
        if (isBan) "সন্তান" else "Child",
        if (isBan) "অন্যান্য" else "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "পরিবারের সদস্যগণ" else "Family Members",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                if (!showAddForm) {
                    Text(
                        text = if (isBan) "পরিবারের প্রতিটি সদস্যের প্রোফাইল থেকে সহজেই ডাক্তার অ্যাপয়েন্টমেন্ট বুকিং করতে পারবেন:"
                        else "Manage profiles for your family members to easily book appointments for them:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    membersList.forEach { member ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                            border = BorderStroke(1.dp, Color(0xFFAED581))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1B5E20))
                                    Surface(
                                        color = Color(0xFF2E7D32),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = member.relation,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("ফোন: ${member.phone} • ব্লাড গ্রুপ: ${member.bloodGroup}", fontSize = 12.sp, color = Color.DarkGray)
                                Text("জন্ম তারিখ: ${member.dob} • লিঙ্গ: ${member.gender}", fontSize = 11.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        Toast.makeText(context, if (isBan) "${member.name}-এর জন্য বুকিং পেজ খুলছে..." else "Opening appointment booking for ${member.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(34.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBan) "এনার জন্য ডাক্তার বুকিং করুন" else "Book Appointment for ${member.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showAddForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBan) "+ নতুন সদস্য যোগ করুন" else "+ Add Family Member", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Add form
                    Text(if (isBan) "নতুন সদস্যের তথ্য:" else "New Member Details:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text(if (isBan) "নাম *" else "Full Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text(if (isBan) "মোবাইল নম্বর *" else "Phone Number *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newDob, onValueChange = { newDob = it }, label = { Text(if (isBan) "জন্ম তারিখ (YYYY-MM-DD)" else "Date of Birth") }, modifier = Modifier.fillMaxWidth())

                    Text(if (isBan) "সম্পর্ক *" else "Relation *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        relations.forEach { rel ->
                            FilterChip(
                                selected = newRelation == rel,
                                onClick = { newRelation = rel },
                                label = { Text(rel, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (newName.isBlank() || newPhone.isBlank()) {
                                    Toast.makeText(context, if (isBan) "নাম ও মোবাইল নম্বর দিন" else "Enter name and phone", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newMem = FamilyMember(
                                        id = System.currentTimeMillis().toString(),
                                        name = newName,
                                        relation = newRelation,
                                        phone = newPhone,
                                        dob = newDob.ifBlank { "1990-01-01" },
                                        gender = newGender,
                                        bloodGroup = newBloodGroup
                                    )
                                    membersList = membersList + newMem
                                    showAddForm = false
                                    newName = ""
                                    newPhone = ""
                                    Toast.makeText(context, if (isBan) "সদস্য যুক্ত হয়েছে!" else "Family member added!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text(if (isBan) "সংরক্ষণ করুন" else "Save Member")
                        }
                        OutlinedButton(
                            onClick = { showAddForm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isBan) "ফিরে যান" else "Cancel")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!showAddForm) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                    Text(if (isBan) "বন্ধ করুন" else "Close")
                }
            }
        }
    )
}

// --- 3. BOOKINGS AND APPOINTMENTS DIALOG ---
@Composable
fun BookingsAndAppointmentsDialog(
    language: AppLanguage,
    viewModel: MainViewModel,
    userPhone: String = "",
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("All") }

    val serviceBookings by viewModel.serviceBookings.collectAsState()
    val ambulanceBookings by viewModel.ambulanceBookings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val bookingAcceptanceFee by viewModel.bookingAcceptanceFee.collectAsState()

    val activePhone = if (userPhone.isNotBlank()) userPhone else (currentUser?.phone ?: "")
    val normalizedTarget = activePhone.filter { it.isDigit() }.takeLast(10)

    fun matchesPhone(targetPhone: String): Boolean {
        if (normalizedTarget.isBlank()) return true
        val clean = targetPhone.filter { it.isDigit() }.takeLast(10)
        return clean.isNotEmpty() && (clean == normalizedTarget || clean.endsWith(normalizedTarget) || normalizedTarget.endsWith(clean))
    }

    val isProviderOrAdmin = currentUser?.role in listOf("Doctor", "Hospital", "Ambulance", "Admin") || userPhone.isBlank()

    val userMatchedServiceBookings = if (isProviderOrAdmin && normalizedTarget.isBlank()) {
        serviceBookings
    } else {
        val matches = serviceBookings.filter {
            matchesPhone(it.userPhone) ||
            matchesPhone(it.patientPhone) ||
            matchesPhone(it.providerPhone) ||
            (currentUser?.name?.isNotBlank() == true && (it.providerName.contains(currentUser?.name ?: "", ignoreCase = true) || it.patientName.contains(currentUser?.name ?: "", ignoreCase = true)))
        }
        if (matches.isEmpty() && isProviderOrAdmin) serviceBookings else matches
    }

    val userMatchedAmbulanceBookings = if (isProviderOrAdmin && normalizedTarget.isBlank()) {
        ambulanceBookings
    } else {
        val matches = ambulanceBookings.filter {
            matchesPhone(it.contactPhone) ||
            matchesPhone(it.assignedAmbulancePhone ?: "") ||
            (currentUser?.name?.isNotBlank() == true && it.patientName.contains(currentUser?.name ?: "", ignoreCase = true))
        }
        if (matches.isEmpty() && isProviderOrAdmin) ambulanceBookings else matches
    }

    val filteredServiceBookings = userMatchedServiceBookings.filter {
        val matchesCat = selectedCategory == "ALL" ||
                (selectedCategory == "DOCTOR" && it.bookingType.contains("Doctor", ignoreCase = true)) ||
                (selectedCategory == "HOSPITAL" && it.bookingType.contains("Hospital", ignoreCase = true))
        val matchesStat = selectedStatus == "All" || it.status.equals(selectedStatus, ignoreCase = true)
        matchesCat && matchesStat
    }

    val filteredAmbulanceBookings = userMatchedAmbulanceBookings.filter {
        val matchesCat = selectedCategory == "ALL" || selectedCategory == "AMBULANCE"
        val matchesStat = selectedStatus == "All" || it.status.equals(selectedStatus, ignoreCase = true)
        matchesCat && matchesStat
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "আমার বুকিং ও অ্যাপয়েন্টমেন্ট" else "My Bookings & Appointments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
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
                // Category Chips (All, Doctor, Hospital, Ambulance)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val totalCount = userMatchedServiceBookings.size + userMatchedAmbulanceBookings.size
                    val docCount = userMatchedServiceBookings.count { it.bookingType.contains("Doctor", ignoreCase = true) }
                    val hospCount = userMatchedServiceBookings.count { it.bookingType.contains("Hospital", ignoreCase = true) }
                    val ambCount = userMatchedAmbulanceBookings.size

                    listOf(
                        "ALL" to (if (isBan) "সব ($totalCount)" else "All ($totalCount)"),
                        "DOCTOR" to (if (isBan) "ডাক্তার ($docCount)" else "Doctor ($docCount)"),
                        "HOSPITAL" to (if (isBan) "হাসপাতাল ($hospCount)" else "Hospital ($hospCount)"),
                        "AMBULANCE" to (if (isBan) "অ্যাম্বুলেন্স ($ambCount)" else "Ambulance ($ambCount)")
                    ).forEach { (catKey, catLabel) ->
                        val isSelected = selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = catKey },
                            label = { Text(catLabel, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Pending", "Confirmed", "Completed", "Cancelled").forEach { tab ->
                        val isSelected = selectedStatus == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatus = tab },
                            label = {
                                Text(
                                    when (tab) {
                                        "All" -> if (isBan) "সকল অবস্থা" else "All Status"
                                        "Pending" -> if (isBan) "অপেক্ষমান" else "Pending"
                                        "Confirmed" -> if (isBan) "কনফার্মড" else "Confirmed"
                                        "Completed" -> if (isBan) "সম্পন্ন" else "Completed"
                                        else -> if (isBan) "বাতিলকৃত" else "Cancelled"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                val hasBookings = filteredServiceBookings.isNotEmpty() || filteredAmbulanceBookings.isNotEmpty()

                if (!hasBookings) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBan) "কোনো বুকিং বা অ্যাপয়েন্টমেন্ট পাওয়া যায়নি।" else "No bookings or appointments found.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Display Service Bookings (Doctor & Hospital)
                    filteredServiceBookings.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "[${item.bookingType}] ${item.providerName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1565C0)
                                    )
                                    Surface(
                                        color = when (item.status) {
                                            "Confirmed" -> Color(0xFF2E7D32)
                                            "Pending" -> Color(0xFFED6C02)
                                            "Completed" -> Color(0xFF1976D2)
                                            else -> Color(0xFFD32F2F)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = item.status,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("রোগী: ${item.patientName} (${item.patientPhone})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                                Text("তারিখ: ${item.bookingDate} • সার্ভিস: ${item.serviceName}", fontSize = 12.sp, color = Color.DarkGray)
                                if (item.notes.isNotBlank()) {
                                    Text("নোট: ${item.notes}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            try {
                                                val callPhone = item.patientPhone.ifBlank { item.providerPhone }
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:$callPhone")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Cannot launch dialer", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBan) "কল করুন" else "Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (item.status == "Pending" && (matchesPhone(item.providerPhone) || currentUser?.role in listOf("Doctor", "Hospital", "Admin") || userPhone.isBlank())) {
                                        Button(
                                            onClick = {
                                                viewModel.updateServiceBookingStatus(item.id, "Confirmed")
                                                Toast.makeText(context, if (isBan) "বুকিং সফলভাবে একসেপ্ট ও কনফার্ম করা হয়েছে!" else "Booking accepted and confirmed!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(0.dp)
                                         ) {
                                            Text(if (isBan) "একসেপ্ট" else "Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (item.status == "Confirmed" && (matchesPhone(item.providerPhone) || currentUser?.role in listOf("Doctor", "Hospital", "Admin") || userPhone.isBlank())) {
                                        Button(
                                            onClick = {
                                                viewModel.updateServiceBookingStatus(item.id, "Completed")
                                                Toast.makeText(context, if (isBan) "বুকিং সম্পন্ন হয়েছে!" else "Booking marked as completed!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (isBan) "সম্পন্ন" else "Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (item.status == "Pending" || item.status == "Confirmed") {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.updateServiceBookingStatus(item.id, "Cancelled")
                                                Toast.makeText(context, if (isBan) "বুকিং বাতিল করা হয়েছে" else "Booking cancelled", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                            border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (isBan) "বাতিল" else "Cancel", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Display Ambulance Bookings
                    filteredAmbulanceBookings.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "[Ambulance] ${item.ambulanceType}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF00796B)
                                    )
                                    Surface(
                                        color = when (item.status) {
                                            "Confirmed", "On the Way" -> Color(0xFF2E7D32)
                                            "Pending" -> Color(0xFFED6C02)
                                            "Completed" -> Color(0xFF1976D2)
                                            else -> Color(0xFFD32F2F)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = item.status,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("রোগী: ${item.patientName} (${item.contactPhone})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                                Text("পিকআপ: ${item.pickupAddress} ➔ গন্তব্য: ${item.destinationAddress}", fontSize = 11.sp, color = Color.DarkGray)
                                Text("তারিখ/সময়: ${item.dateTime}", fontSize = 11.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            try {
                                                val callPhone = item.contactPhone.ifBlank { item.assignedAmbulancePhone ?: "" }
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:$callPhone")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Cannot launch dialer", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBan) "কল করুন" else "Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (item.status == "Pending" && (currentUser?.phone == item.assignedAmbulancePhone || currentUser?.role in listOf("Ambulance", "Admin") || userPhone.isBlank())) {
                                        Button(
                                            onClick = {
                                                val currentBalance = currentUser?.walletBalance ?: 0.0
                                                val requiredFee = bookingAcceptanceFee
                                                if (currentBalance >= requiredFee) {
                                                    val success = viewModel.deductWalletBalance(requiredFee)
                                                    if (success) {
                                                        viewModel.triggerUpdateBookingStatus(
                                                            bookingId = item.id,
                                                            newStatus = "Confirmed",
                                                            assignedName = currentUser?.name ?: "",
                                                            assignedPhone = currentUser?.phone ?: "",
                                                            fare = item.fare
                                                        )
                                                        Toast.makeText(context, if (isBan) "অ্যাম্বুলেন্স ট্রিপ গ্রহণ করা হয়েছে! ৳${requiredFee.toInt()} ওয়ালেট থেকে কাটা হয়েছে।" else "Ambulance trip accepted! ৳${requiredFee.toInt()} deducted from wallet.", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(context, if (isBan) "ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই! প্রয়োজন ৳${requiredFee.toInt()}" else "Insufficient wallet balance! Required ৳${requiredFee.toInt()}", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, if (isBan) "ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই! প্রয়োজন ৳${requiredFee.toInt()}। অনুগ্রহ করে রিচার্জ করুন।" else "Insufficient balance! Required ৳${requiredFee.toInt()}. Please recharge wallet.", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (isBan) "একসেপ্ট" else "Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (item.status == "Pending" || item.status == "Confirmed") {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.triggerUpdateBookingStatus(bookingId = item.id, newStatus = "Cancelled")
                                                Toast.makeText(context, if (isBan) "বুকিং বাতিল করা হয়েছে" else "Booking cancelled", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                            border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (isBan) "বাতিল" else "Cancel", fontSize = 11.sp)
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
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// --- 4. SUGGEST & ADD HOSPITAL DIALOG WITH ADMIN APPROVAL FLOW ---
@Composable
fun SuggestAddHospitalDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current

    var hospitalName by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var emergencyNumber by remember { mutableStateOf("") }
    var hospitalType by remember { mutableStateOf("Hospital") }
    var departments by remember { mutableStateOf("Cardiology, ICU, Emergency, Surgery") }
    var websiteFb by remember { mutableStateOf("") }
    var googleMapLocation by remember { mutableStateOf("") }
    var services by remember { mutableStateOf("24/7 Emergency, Blood Bank, Ambulance") }
    var hasIcu by remember { mutableStateOf(true) }
    var hasCcu by remember { mutableStateOf(true) }
    var hasNicu by remember { mutableStateOf(false) }

    var submitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "নতুন হাসপাতাল যুক্ত / প্রস্তাব করুন" else "Suggest / Add New Hospital",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
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
                if (!submitted) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBan) "গুরুত্বপূর্ণ: আপনার জমা দেওয়া হাসপাতাল তথ্য আগে অ্যাডমিন (Admin) যাচাই করবে। অ্যাডমিন অনুমোদনের পর এটি পাবলিক অ্যাপে প্রদর্শিত হবে।"
                                else "Important: Your submitted hospital details will be verified by Admin. Once approved, it will be visible publicly.",
                                fontSize = 11.sp,
                                color = Color(0xFFE65100),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    OutlinedTextField(value = hospitalName, onValueChange = { hospitalName = it }, label = { Text(if (isBan) "হাসপাতালের নাম (Hospital Name) *" else "Hospital Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text(if (isBan) "লোগো / ছবি লিংক (Logo/Image URL)" else "Logo / Photo URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text(if (isBan) "ঠিকানা (Address) *" else "Address *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(if (isBan) "ফোন (Phone) *" else "Phone *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = emergencyNumber, onValueChange = { emergencyNumber = it }, label = { Text(if (isBan) "জরুরি নম্বর (Emergency Hotline)" else "Emergency Hotline") }, modifier = Modifier.fillMaxWidth())

                    Text(if (isBan) "হাসপাতালের ধরন:" else "Hospital Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Hospital", "Diagnostic", "Clinic", "Specialized").forEach { type ->
                            FilterChip(
                                selected = hospitalType == type,
                                onClick = { hospitalType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(value = departments, onValueChange = { departments = it }, label = { Text(if (isBan) "বিভাগসমূহ (Departments)" else "Departments") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = services, onValueChange = { services = it }, label = { Text(if (isBan) "সেবাসমূহ (Services)" else "Services") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = websiteFb, onValueChange = { websiteFb = it }, label = { Text(if (isBan) "ওয়েবসাইট / ফেসবুক পেজ লিংক" else "Website / Facebook Link") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = googleMapLocation, onValueChange = { googleMapLocation = it }, label = { Text(if (isBan) "গুগল ম্যাপ লোকেশন লিংক" else "Google Map Location URL") }, modifier = Modifier.fillMaxWidth())

                    Text(if (isBan) "বিশেষ সুবিধাসমূহ:" else "Special Facilities:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasIcu, onCheckedChange = { hasIcu = it })
                            Text("ICU", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasCcu, onCheckedChange = { hasCcu = it })
                            Text("CCU", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasNicu, onCheckedChange = { hasNicu = it })
                            Text("NICU", fontSize = 12.sp)
                        }
                    }
                } else {
                    // Success notice
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.5.dp, Color(0xFF2E7D32))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBan) "আপনার হাসপাতালের প্রস্তাবটি সফলভাবে জমা হয়েছে!" else "Hospital suggestion submitted successfully!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1B5E20),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBan) "স্ট্যাটাস: অ্যাডমিন ভেরিফিকেশন অপেক্ষমান (Pending Admin Approval)\nঅ্যাডমিন যাচাইকরণের পর আপনার তথ্য প্রকাশিত হবে।"
                                else "Status: Pending Admin Approval\nYour listing will be published once verified.",
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!submitted) {
                Button(
                    onClick = {
                        if (hospitalName.isBlank() || phone.isBlank() || address.isBlank()) {
                            Toast.makeText(context, if (isBan) "হাসপাতালের নাম, ফোন ও ঠিকানা পূরণ করুন" else "Enter hospital name, phone & address", Toast.LENGTH_SHORT).show()
                        } else {
                            submitted = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text(if (isBan) "জমা দিন (Submit for Approval)" else "Submit for Approval")
                }
            } else {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                    Text(if (isBan) "বন্ধ করুন" else "Close")
                }
            }
        },
        dismissButton = {
            if (!submitted) {
                TextButton(onClick = onDismiss) {
                    Text(if (isBan) "বাতিল" else "Cancel")
                }
            }
        }
    )
}

// --- 5. MY HOSPITALS DIALOG ---
@Composable
fun MyHospitalsDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSuggestNew: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    var selectedTab by remember { mutableStateOf("Favorite") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "আমার হাসপাতালসমূহ" else "My Hospitals",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Favorite", "Recently Viewed", "Saved").forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = {
                                Text(
                                    when (tab) {
                                        "Favorite" -> if (isBan) "প্রিয় হাসপাতাল" else "Favorite"
                                        "Recently Viewed" -> if (isBan) "সাম্প্রতিক দেখা" else "Recently Viewed"
                                        else -> if (isBan) "সংরক্ষিত" else "Saved"
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }

                listOf(
                    Pair("ঢাকা মেডিকেল কলেজ হাসপাতাল", "জরুরি লাইন: 01700000000 • ধানমন্ডি, ঢাকা"),
                    Pair("স্কয়ার হাসপাতাল লিমিটেড", "জরুরি লাইন: 01800000000 • পান্থপথ, ঢাকা")
                ).forEach { (name, info) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                        border = BorderStroke(1.dp, Color(0xFFC5CAE9))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A237E))
                                Text(info, fontSize = 11.sp, color = Color.DarkGray)
                            }
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        onDismiss()
                        onSuggestNew()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBan) "+ নতুন হাসপাতাল যোগ / প্রস্তাব করুন" else "+ Add / Suggest Hospital", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// --- 6. MY PAYMENTS & INVOICES DIALOG ---
@Composable
fun MyPaymentsDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current

    val payments = listOf(
        Triple("TXN-908123", "৳500 - Doctor Booking Payment", "Completed"),
        Triple("TXN-881274", "৳1,200 - Diagnostic Test Payment", "Completed"),
        Triple("TXN-712399", "৳350 - Ambulance Booking Charge", "Refunded"),
        Triple("TXN-611234", "৳850 - Medicine Order Payment", "Completed")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "আমার পেমেন্ট ও ইনভয়েস" else "My Payments & Invoices",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                payments.forEach { (txn, title, status) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B5E20))
                                Surface(
                                    color = if (status == "Completed") Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Transaction ID: $txn • bKash Payment", fontSize = 11.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, if (isBan) "ইনভয়েস/রশিদ ডাউনলোড হচ্ছে..." else "Downloading invoice receipt...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBan) "ইনভয়েস / ক্যাশ মেমো" else "View / Download Invoice", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// --- 7. MEDICAL RECORDS & UPLOAD DOCUMENT DIALOG ---
@Composable
fun MedicalRecordsDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Prescription") }
    var showUploadForm by remember { mutableStateOf(false) }

    var docTitle by remember { mutableStateOf("") }
    var docDoctor by remember { mutableStateOf("") }
    var docDate by remember { mutableStateOf("") }

    var recordsList by remember {
        mutableStateOf(
            listOf(
                MedicalRecord("1", "Prescription", "প্রেসক্রিপশন - ল্যাবএইড হাসপাতাল", "ডাঃ আব্দুর রহমান", "২০২৬-০৭-১০"),
                MedicalRecord("2", "Lab Report", "ব্লাড সুগার & লিপিড প্রোফাইল টেস্ট", "পপুলার ডায়াগনস্টিক", "২০২৬-০৬-১৫"),
                MedicalRecord("3", "Medical Document", "হাসপাতাল ডিসচার্জ সামারি", "স্কয়ার হাসপাতাল", "২০২৬-০৫-২০")
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "মেডিকেল রেকর্ডস & প্রেসক্রিপশন" else "My Medical Records",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
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
                if (!showUploadForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Prescription", "Lab Report", "Medical Document", "Previous Reports").forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        when (cat) {
                                            "Prescription" -> if (isBan) "প্রেসক্রিপশন" else "Prescription"
                                            "Lab Report" -> if (isBan) "ল্যাব রিপোর্ট" else "Lab Report"
                                            "Medical Document" -> if (isBan) "মেডিকেল ডকুমেন্ট" else "Medical Docs"
                                            else -> if (isBan) "পূর্ববর্তী রিপোর্ট" else "Previous Reports"
                                        },
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }

                    val filtered = recordsList.filter { it.type.equals(selectedCategory, ignoreCase = true) }

                    if (filtered.isEmpty()) {
                        Text(if (isBan) "কোনো আপলোডকৃত নথি পাওয়া যায়নি।" else "No uploaded records found in this category.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        filtered.forEach { rec ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                                border = BorderStroke(1.dp, Color(0xFFCE93D8))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rec.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4A148C))
                                        Text("${rec.doctorOrHospital} • ${rec.date}", fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                    IconButton(onClick = { Toast.makeText(context, if (isBan) "ডকুমেন্ট ভিউ করা হচ্ছে..." else "Opening document file...", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.Visibility, contentDescription = "View", tint = Color(0xFF673AB7))
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showUploadForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBan) "+ নতুন মেডিকেল ডকুমেন্ট আপলোড করুন" else "+ Upload New Document", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Upload document form
                    Text(if (isBan) "নথি আপলোড ফর্ম:" else "Upload Medical File:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(value = docTitle, onValueChange = { docTitle = it }, label = { Text(if (isBan) "ডকুমেন্টের শিরোনাম *" else "Document Title *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = docDoctor, onValueChange = { docDoctor = it }, label = { Text(if (isBan) "ডাক্তার / হাসপাতালের নাম" else "Doctor / Hospital Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = docDate, onValueChange = { docDate = it }, label = { Text(if (isBan) "তারিখ (YYYY-MM-DD)" else "Date") }, modifier = Modifier.fillMaxWidth())

                    OutlinedButton(
                        onClick = { Toast.makeText(context, if (isBan) "গ্যালারি/ক্যামেরা থেকে ফটো নেওয়া হচ্ছে..." else "Select image/pdf from device gallery", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBan) "ছবি / পিডিএফ নির্বাচন করুন" else "Select File / Photo")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (docTitle.isBlank()) {
                                    Toast.makeText(context, if (isBan) "শিরোনাম প্রদান করুন" else "Provide title", Toast.LENGTH_SHORT).show()
                                } else {
                                    recordsList = recordsList + MedicalRecord(
                                        id = System.currentTimeMillis().toString(),
                                        type = selectedCategory,
                                        title = docTitle,
                                        doctorOrHospital = docDoctor.ifBlank { "Personal Record" },
                                        date = docDate.ifBlank { "2026-08-11" }
                                    )
                                    showUploadForm = false
                                    docTitle = ""
                                    docDoctor = ""
                                    Toast.makeText(context, if (isBan) "ডকুমেন্ট আপলোড সফল!" else "Document uploaded successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                        ) {
                            Text(if (isBan) "আপলোড করুন" else "Save & Upload")
                        }
                        OutlinedButton(onClick = { showUploadForm = false }, modifier = Modifier.weight(1f)) {
                            Text(if (isBan) "বাতিল" else "Cancel")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!showUploadForm) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))) {
                    Text(if (isBan) "বন্ধ করুন" else "Close")
                }
            }
        }
    )
}

// --- 8. FAVORITE DOCTORS DIALOG ---
@Composable
fun FavoriteDoctorsDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current

    val doctors = listOf(
        Triple("ডাঃ মোহাম্মদ জহিরুল ইসলাম", "মেডিসিন বিশেষজ্ঞ • স্কয়ার হাসপাতাল", "পরবর্তী অ্যাপয়েন্টমেন্ট: ২৬ আগস্ট"),
        Triple("ডাঃ সারাহ আহমেদ", "গাইনি & প্রসূতি বিশেষজ্ঞ • ল্যাবএইড", "পরবর্তী অ্যাপয়েন্টমেন্ট: ৩০ আগস্ট")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "আমার পছন্দের ডাক্তারগণ" else "Favorite Doctors",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                doctors.forEach { (name, spec, appt) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFFAED581))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1B5E20))
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(spec, fontSize = 12.sp, color = Color.DarkGray)
                            Text(appt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    Toast.makeText(context, if (isBan) "${name}-এর জন্য কুইক অ্যাপয়েন্টমেন্ট স্লট বুক করা হচ্ছে..." else "Booking appointment with $name", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBan) "কুইক বুকিং করুন" else "Quick Booking", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// --- 9. MY DIAGNOSTIC REPORTS DIALOG ---
@Composable
fun MyReportsDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Blood Test") }

    val reports = listOf(
        DiagnosticReport("1", "Blood Test", "সিবিসি (CBC) & হিমোগ্লোবিন টেস্ট", "পপুলার ডায়াগনস্টিক", "২০২৬-০৮-০১", "Ready"),
        DiagnosticReport("2", "X-Ray", "চেস্ট এক্স-রে (Chest X-Ray P/A)", "ইবনে সিনা ডায়াগনস্টিক", "২০২৬-০৭-২৫", "Ready"),
        DiagnosticReport("3", "MRI", "ব্রেইন এমআরআই (Brain MRI with Contrast)", "ল্যাবএইড স্পেশালাইজড", "২০২৬-০৮-১০", "Processing"),
        DiagnosticReport("4", "CT Scan", "হোল অ্যাবডোমেন সিটি স্ক্যান", "স্কয়ার হাসপাতাল", "২০২৬-০৬-১২", "Ready")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Biotech, contentDescription = null, tint = Color(0xFF00838F), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "আমার ডায়াগনস্টিক টেস্ট রিপোর্ট" else "My Diagnostic Reports",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
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
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Blood Test", "X-Ray", "MRI", "CT Scan").forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                val filtered = reports.filter { it.testType.equals(selectedCategory, ignoreCase = true) }

                if (filtered.isEmpty()) {
                    Text(if (isBan) "কোনো টেস্ট রিপোর্ট পাওয়া যায়নি।" else "No reports found for this test type.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    filtered.forEach { rep ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                            border = BorderStroke(1.dp, Color(0xFF80DEEA))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(rep.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF006064))
                                    Surface(
                                        color = if (rep.status == "Ready") Color(0xFF00838F) else Color(0xFFED6C02),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (rep.status == "Ready") (if (isBan) "রেডি" else "Ready") else (if (isBan) "প্রসেসিং" else "Processing"),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${rep.centerName} • ${rep.date}", fontSize = 11.sp, color = Color.DarkGray)

                                if (rep.status == "Ready") {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = { Toast.makeText(context, if (isBan) "রিপোর্ট পিডিএফ ডাউনলোড হচ্ছে..." else "Downloading report PDF...", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBan) "রিপোর্ট দেখুন / ডাউনলোড" else "View / Download Report PDF", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// --- 10. MY ORDERS DIALOG (PHARMACY / MEDICINE) ---
@Composable
fun MyOrdersDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "আমার ফার্মেসী অর্ডারস" else "My Medicine Orders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("অর্ডার #MED-8819", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))
                            Surface(color = Color(0xFF2E7D32), shape = RoundedCornerShape(8.dp)) {
                                Text(if (isBan) "ডেলিভারি সম্পন্ন" else "Delivered", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("আইটেম: নাপা এক্সট্রা, সেফ-৩, ভিটামিন সি • মোট ৳৪৫০", fontSize = 12.sp, color = Color.DarkGray)
                        Text("তারিখ: ০৫ আগস্ট, ২০২৬", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { Toast.makeText(context, if (isBan) "ইনভয়েস মেমো খোলা হচ্ছে..." else "Opening invoice memo...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(if (isBan) "ক্যাশ মেমো দেখুন" else "View Memo Invoice", fontSize = 10.sp)
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
}

// --- 11. MY REVIEWS DIALOG ---
@Composable
fun MyReviewsDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN

    val reviews = listOf(
        UserReview("1", "Hospital", "ঢাকা মেডিকেল কলেজ হাসপাতাল", 4.5f, "জরুরি ওয়ার্ডের সেবা খুবই দ্রুত ছিল। ডাক্তার ও নার্সরা আন্তরিক।", "২০২৬-০৮-০২"),
        UserReview("2", "Doctor", "ডাঃ আব্দুর রহমান", 5.0f, "খুব মন দিয়ে সমস্যা শুনেছেন এবং সঠিক পরামর্শ দিয়েছেন।", "২০২৬-০৭-১৮")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RateReview, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "আমার দেওয়া রিভিউসমূহ" else "My Reviews",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                reviews.forEach { rev ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        border = BorderStroke(1.dp, Color(0xFFFFE082))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rev.targetName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                    Text(" ${rev.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE65100))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(rev.comment, fontSize = 12.sp, color = Color.DarkGray)
                            Text("তারিখ: ${rev.date}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// --- 12. REFERRAL & INVITE FRIENDS DIALOG ---
@Composable
fun ReferralDialog(
    user: BloodDonor,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current
    val refCode = "HERO-${user.displayUserId}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "রেফার করুন & বোনাস জিতুন" else "Refer & Earn Bonus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                    border = BorderStroke(1.5.dp, Color(0xFF0288D1))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isBan) "আপনার ইউনিক রেফারেল কোড" else "Your Unique Referral Code", fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFF0288D1),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = refCode,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, if (isBan) "কোড কপি করা হয়েছে!" else "Referral code copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBan) "কপি করুন" else "Copy Code", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    Toast.makeText(context, if (isBan) "বন্ধুদের আমন্ত্রণ জানানো হচ্ছে..." else "Inviting friends...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBan) "শেয়ার করুন" else "Share App", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(modifier = Modifier.weight(1f).padding(end = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট আমন্ত্রিত", fontSize = 11.sp, color = Color.Gray)
                            Text("৫ জন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                        }
                    }
                    Card(modifier = Modifier.weight(1f).padding(start = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("অর্জিত বোনাস", fontSize = 11.sp, color = Color.Gray)
                            Text("৳২৫০", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE65100))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// --- 13. NOTIFICATIONS CENTER DIALOG ---
@Composable
fun NotificationsCenterDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val notifications by remember {
        mutableStateOf(
            listOf(
                Triple("বুকিং কনফার্মেশন", "আপনার ডাক্তার অ্যাপয়েন্টমেন্ট সফলভাবে কনফার্ম হয়েছে।", "১০ মি. আগে"),
                Triple("টেস্ট রিপোর্ট রেডি", "আপনার ল্যাব টেস্ট রিপোর্ট রেডি হয়েছে। ডাউনলোড করুন।", "১ ঘণ্টা আগে"),
                Triple("পেমেন্ট সফল", "৳৫০০ ওয়ালেট পেমেন্ট সফল হয়েছে।", "গতকাল")
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "নোটিফিকেশন সেন্টার" else "Notifications Center",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (notifications.isEmpty()) {
                    Text(if (isBan) "কোনো নতুন নোটিফিকেশন নেই।" else "No notifications.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    notifications.forEach { (title, desc, time) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                            border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD32F2F))
                                    Text(time, fontSize = 10.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(desc, fontSize = 11.sp, color = Color.DarkGray)
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
}

// --- 14. MESSAGES & SUPPORT DIALOG ---
@Composable
fun MessagesSupportDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current
    var showTicketForm by remember { mutableStateOf(false) }

    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "মেসেজ & কাস্টমার সাপোর্ট" else "Support & Help Center",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
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
                if (!showTicketForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        border = BorderStroke(1.dp, Color(0xFF90CAF9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(if (isBan) "লাইভ এডমিন চ্যাট" else "Live Admin Chat Support", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1565C0))
                            Text(if (isBan) "যে কোনো সাহায্য বা অনুসন্ধানের জন্য এডমিনের সাথে সরাসরি কথা বলুন।" else "Chat directly with support admin for assistance.", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }

                    OutlinedButton(
                        onClick = { showTicketForm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBan) "+ অভিযোগ / সাপোর্ট টিকিট খুলুন" else "+ Create Support Ticket")
                    }
                } else {
                    Text(if (isBan) "সাপোর্ট টিকিট ফর্ম:" else "Support Ticket Form:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text(if (isBan) "বিষয় (Subject) *" else "Subject *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(if (isBan) "বিস্তারিত বর্ণনা *" else "Description *") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (subject.isBlank()) {
                                    Toast.makeText(context, if (isBan) "বিষয় প্রদান করুন" else "Enter subject", Toast.LENGTH_SHORT).show()
                                } else {
                                    showTicketForm = false
                                    subject = ""
                                    description = ""
                                    Toast.makeText(context, if (isBan) "সাপোর্ট টিকিট সফলভাবে জমা হয়েছে!" else "Support ticket submitted!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                        ) {
                            Text(if (isBan) "জমা দিন" else "Submit Ticket")
                        }
                        OutlinedButton(onClick = { showTicketForm = false }, modifier = Modifier.weight(1f)) {
                            Text(if (isBan) "বাতিল" else "Cancel")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!showTicketForm) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))) {
                    Text(if (isBan) "বন্ধ করুন" else "Close")
                }
            }
        }
    )
}

// --- 15. APP SETTINGS & SECURITY DIALOG ---
@Composable
fun SettingsAndSecurityDialog(
    viewModel: MainViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current
    var notificationsOn by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBan) "অ্যাপ সেটিংস & সিকিউরিটি" else "Settings & Privacy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Language Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isBan) "অ্যাপ ভাষা (Language)" else "App Language", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(if (isBan) "বর্তমান: বাংলা" else "Current: English", fontSize = 11.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            viewModel.toggleLanguage()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text(if (isBan) "English" else "বাংলা", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider()

                // Notification Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBan) "পুশ নোটিফিকেশন" else "Push Notifications", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Switch(checked = notificationsOn, onCheckedChange = { notificationsOn = it })
                }

                HorizontalDivider()

                // Change Password / PIN
                OutlinedButton(
                    onClick = { Toast.makeText(context, if (isBan) "পাসওয়ার্ড/পিন পরিবর্তনের লিঙ্ক পাঠানো হয়েছে" else "Password reset link sent to phone", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBan) "পাসওয়ার্ড / পিন পরিবর্তন করুন" else "Change Password / PIN")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// --- 16. DOCTOR MY ACCOUNT COMPREHENSIVE DIALOG ---
@Composable
fun DoctorMyAccountDialog(
    user: BloodDonor,
    viewModel: MainViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf("Profile") }

    // State variables for Doctor Profile
    var docName by remember { mutableStateOf(user.name) }
    var docSpecialization by remember { mutableStateOf("কার্ডিওলজিস্ট (Cardiologist)") }
    var docDegree by remember { mutableStateOf("MBBS, FCPS (Medicine), MD (Cardiology)") }
    var docExperience by remember { mutableStateOf("12 Years") }
    var docBmdcReg by remember { mutableStateOf("A-78902") }
    var docAbout by remember { mutableStateOf("বিএসএমএমইউ (পিজি হাসপাতাল) এর অভিজ্ঞ হৃদরোগ বিশেষজ্ঞ।") }
    var docFee by remember { mutableStateOf("800 BDT") }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Schedule States
    var isAvailable by remember { mutableStateOf(true) }
    var visitingDays by remember { mutableStateOf(setOf("Sat", "Sun", "Mon", "Tue", "Wed")) }
    var visitingTime by remember { mutableStateOf("05:00 PM - 09:00 PM") }
    var slotDuration by remember { mutableStateOf("20 Minutes") }
    var isLeaveMode by remember { mutableStateOf(false) }
    var leaveReason by remember { mutableStateOf("") }

    // Commission State (System's Most Important Feature)
    // Active / Pending / Expired
    var commissionStatus by remember { mutableStateOf("Active") } // Try toggling to test
    var currentCommissionRate by remember { mutableStateOf("10% per booking (৳80)") }
    var commissionDueAmount by remember { mutableStateOf(1200) }
    var commissionValidUntil by remember { mutableStateOf("2026-08-31") }
    var showPayCommissionModal by remember { mutableStateOf(false) }

    // Live Bookings Data
    val allServiceBookings by viewModel.serviceBookings.collectAsState()
    val registeredDoctors by viewModel.registeredDoctors.collectAsState()
    val myDoc = registeredDoctors.find { 
        it.phone == user.phone || it.name.equals(user.name, ignoreCase = true) 
    }
    
    var appointmentFilter by remember { mutableStateOf("ALL") }
    
    val doctorBookings = remember(allServiceBookings, user.phone, user.name, myDoc) {
        val cleanDocPhone = user.phone.filter { it.isDigit() }.takeLast(10)
        val docNameStr = (myDoc?.name ?: user.name).trim()
        val matches = allServiceBookings.filter { b ->
            val isDoc = b.bookingType.contains("Doctor", ignoreCase = true)
            val cleanProvPhone = b.providerPhone.filter { it.isDigit() }.takeLast(10)
            val matchPhone = cleanDocPhone.isNotBlank() && cleanProvPhone.isNotBlank() && 
                (cleanDocPhone == cleanProvPhone || cleanProvPhone.endsWith(cleanDocPhone) || cleanDocPhone.endsWith(cleanProvPhone))
            val matchName = docNameStr.isNotBlank() && b.providerName.isNotBlank() && 
                (b.providerName.contains(docNameStr, ignoreCase = true) || docNameStr.contains(b.providerName, ignoreCase = true))
            (isDoc && (matchPhone || matchName)) || matchPhone
        }
        if (matches.isEmpty()) {
            allServiceBookings.filter { it.bookingType.contains("Doctor", ignoreCase = true) }
        } else {
            matches
        }
    }

    // Prescription Generator State
    var pPatientName by remember { mutableStateOf("") }
    var pPatientAge by remember { mutableStateOf("") }
    var pDiagnosis by remember { mutableStateOf("") }
    var pMedicines by remember { mutableStateOf("1. Tab. Napa Extra (1+0+1) 5 days\n2. Cap. Seclo 20mg (1+0+1) 7 days") }
    var pFollowUpDate by remember { mutableStateOf("2026-08-25") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBan) "👨‍⚕️ ডাক্তার মাই একাউন্ট (Doctor My Account)" else "👨‍⚕️ Doctor My Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF1B5E20)
                    )
                }
                Text(
                    text = "${docName} • ${docSpecialization}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                // Top Scrollable Category Navigation Menu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .background(Color(0xFFF1F8E9), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "Profile" to if (isBan) "প্রোফাইল" else "Profile",
                        "Appointments" to if (isBan) "অ্যাপয়েন্টমেন্ট" else "Appointments",
                        "Commission" to if (isBan) "কমিশন 💰" else "Commission 💰",
                        "Schedule" to if (isBan) "সময়সূচী" else "Schedule",
                        "Hospitals" to if (isBan) "হাসপাতাল" else "Hospitals",
                        "Patients" to if (isBan) "রোগীগণ" else "Patients",
                        "Prescriptions" to if (isBan) "প্রেসক্রিপশন" else "Prescriptions",
                        "Reports" to if (isBan) "মেডিকেল রিপোর্ট" else "Reports",
                        "Reviews" to if (isBan) "রিভিউ & রেটিং" else "Reviews",
                        "Notifications" to if (isBan) "নোটিফিকেশন" else "Notifications",
                        "Messages" to if (isBan) "মেসেজ" else "Messages",
                        "Verification" to if (isBan) "ভেরিফিকেশন" else "Verification",
                        "Documents" to if (isBan) "ডকুমেন্টস" else "Documents",
                        "Support" to if (isBan) "সাপোর্ট" else "Support",
                        "Settings" to if (isBan) "সেটিংস" else "Settings"
                    ).forEach { (key, label) ->
                        val isSelected = activeTab == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeTab = key },
                            label = {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (key == "Commission") Color(0xFFD32F2F) else Color.Unspecified
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Body Section Based on activeTab
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (activeTab) {
                        // --- 1. MY PROFILE ---
                        "Profile" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                                border = BorderStroke(1.dp, Color(0xFFAED581))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .background(Color(0xFF2E7D32), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(docName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1B5E20))
                                            Text(docDegree, fontSize = 11.sp, color = Color.DarkGray)
                                            Text("BMDC Reg: $docBmdcReg • Exp: $docExperience", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (!isEditingProfile) {
                                        Text("বিশেষজ্ঞতা: $docSpecialization", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("রোগী দেখার ফি: $docFee", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                                        Text("আমার সম্পর্কে: $docAbout", fontSize = 11.sp, color = Color.DarkGray)

                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { isEditingProfile = true },
                                            modifier = Modifier.fillMaxWidth().height(34.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBan) "প্রোফাইল সম্পাদন করুন (Edit Profile)" else "Edit Profile", fontSize = 11.sp)
                                        }
                                    } else {
                                        OutlinedTextField(value = docName, onValueChange = { docName = it }, label = { Text("নাম") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = docSpecialization, onValueChange = { docSpecialization = it }, label = { Text("বিশেষজ্ঞতা") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = docDegree, onValueChange = { docDegree = it }, label = { Text("ডিগ্রী") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = docBmdcReg, onValueChange = { docBmdcReg = it }, label = { Text("BMDC রেজিস্ট্রেশন নম্বর") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = docFee, onValueChange = { docFee = it }, label = { Text("কনসাল্টেশন ফি") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = docAbout, onValueChange = { docAbout = it }, label = { Text("আমার বিবরণ") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = {
                                                    isEditingProfile = false
                                                    Toast.makeText(context, if (isBan) "প্রোফাইল সেভ হয়েছে" else "Profile saved", Toast.LENGTH_SHORT).show()
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

                        // --- 2. MY APPOINTMENTS ---
                        "Appointments" -> {
                            // Filter row for doctor appointments
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "ALL" to (if (isBan) "সব (${doctorBookings.size})" else "All (${doctorBookings.size})"),
                                    "PENDING" to (if (isBan) "অপেক্ষমান (${doctorBookings.count { it.status.equals("Pending", true) }})" else "Pending (${doctorBookings.count { it.status.equals("Pending", true) }})"),
                                    "CONFIRMED" to (if (isBan) "কনফার্মড (${doctorBookings.count { it.status.equals("Confirmed", true) }})" else "Confirmed (${doctorBookings.count { it.status.equals("Confirmed", true) }})"),
                                    "COMPLETED" to (if (isBan) "সম্পন্ন (${doctorBookings.count { it.status.equals("Completed", true) }})" else "Completed (${doctorBookings.count { it.status.equals("Completed", true) }})"),
                                    "CANCELLED" to (if (isBan) "বাতিল (${doctorBookings.count { it.status.equals("Cancelled", true) }})" else "Cancelled (${doctorBookings.count { it.status.equals("Cancelled", true) }})")
                                ).forEach { (fKey, label) ->
                                    FilterChip(
                                        selected = appointmentFilter == fKey,
                                        onClick = { appointmentFilter = fKey },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = if (appointmentFilter == fKey) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF1B5E20),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            val filteredAppointments = doctorBookings.filter { b ->
                                when (appointmentFilter) {
                                    "PENDING" -> b.status.equals("Pending", true)
                                    "CONFIRMED" -> b.status.equals("Confirmed", true)
                                    "COMPLETED" -> b.status.equals("Completed", true)
                                    "CANCELLED" -> b.status.equals("Cancelled", true)
                                    else -> true
                                }
                            }

                            if (filteredAppointments.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            if (isBan) "কোনো অ্যাপয়েন্টমেন্ট পাওয়া যায়নি" else "No appointments found",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            if (isBan) "রোগীরা আপনার প্রোফাইল থেকে বুকিং করলে এখানে দেখা যাবে।" else "Incoming bookings from patients will appear here.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                filteredAppointments.forEach { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = req.patientName.ifBlank { req.userName },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = Color(0xFF1B5E20)
                                                    )
                                                    if (req.patientAge.isNotBlank()) {
                                                        Text("বয়স: ${req.patientAge} বছর", fontSize = 11.sp, color = Color.DarkGray)
                                                    }
                                                }
                                                Surface(
                                                    color = when (req.status.lowercase()) {
                                                        "pending" -> Color(0xFFED6C02)
                                                        "confirmed" -> Color(0xFF2E7D32)
                                                        "completed" -> Color(0xFF1976D2)
                                                        else -> Color(0xFFD32F2F)
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = when (req.status.lowercase()) {
                                                            "pending" -> if (isBan) "নতুন অনুরোধ" else "Pending"
                                                            "confirmed" -> if (isBan) "কনফার্মড" else "Confirmed"
                                                            "completed" -> if (isBan) "সম্পন্ন" else "Completed"
                                                            else -> if (isBan) "বাতিল" else "Cancelled"
                                                        },
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("সেবা: ${req.serviceName}", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                            Text("তারিখ: ${req.bookingDate}", fontSize = 12.sp, color = Color(0xFF0D47A1))
                                            if (req.notes.isNotBlank()) {
                                                Text("বিবরণ: ${req.notes}", fontSize = 11.sp, color = Color.DarkGray)
                                            }
                                            Text("ফোন: ${req.patientPhone}", fontSize = 11.sp, color = Color.Gray)
                                            Text("আইডি: #${req.id}", fontSize = 10.sp, color = Color.LightGray)

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Call Patient Button
                                                Button(
                                                    onClick = {
                                                        try {
                                                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                                data = android.net.Uri.parse("tel:${req.patientPhone}")
                                                            }
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Cannot open phone dialer", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f).height(34.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (isBan) "রোগীকে কল" else "Call", fontSize = 11.sp)
                                                }

                                                if (req.status.equals("Pending", true)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateServiceBookingStatus(req.id, "Confirmed")
                                                            Toast.makeText(context, if (isBan) "অ্যাপয়েন্টমেন্ট গ্রহণ ও কনফার্ম করা হয়েছে!" else "Appointment Accepted & Confirmed!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (isBan) "Accept" else "Accept", fontSize = 11.sp)
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.updateServiceBookingStatus(req.id, "Cancelled")
                                                            Toast.makeText(context, if (isBan) "অ্যাপয়েন্টমেন্ট বাতিল করা হয়েছে" else "Appointment Cancelled", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (isBan) "Reject" else "Reject", fontSize = 11.sp)
                                                    }
                                                } else if (req.status.equals("Confirmed", true)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateServiceBookingStatus(req.id, "Completed")
                                                            Toast.makeText(context, if (isBan) "অ্যাপয়েন্টমেন্ট সম্পন্ন করা হয়েছে!" else "Appointment Completed!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (isBan) "সম্পন্ন" else "Complete", fontSize = 11.sp)
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.updateServiceBookingStatus(req.id, "Cancelled")
                                                            Toast.makeText(context, if (isBan) "অ্যাপয়েন্টমেন্ট বাতিল করা হয়েছে" else "Appointment Cancelled", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(if (isBan) "বাতিল" else "Cancel", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- 3. COMMISSION (CRITICAL SYSTEM PART) ---
                        "Commission" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (commissionStatus) {
                                        "Active" -> Color(0xFFE8F5E9)
                                        "Pending" -> Color(0xFFFFF3E0)
                                        else -> Color(0xFFFFEBEE)
                                    }
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    when (commissionStatus) {
                                        "Active" -> Color(0xFF2E7D32)
                                        "Pending" -> Color(0xFFED6C02)
                                        else -> Color(0xFFD32F2F)
                                    }
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(if (isBan) "কমিশন স্ট্যাটাস (Commission Status)" else "Commission Status", fontSize = 12.sp, color = Color.DarkGray)
                                            Text(
                                                text = when (commissionStatus) {
                                                    "Active" -> if (isBan) "🟢 ACTIVE (সক্রিয়)" else "🟢 ACTIVE"
                                                    "Pending" -> if (isBan) "🟠 PENDING (অপেক্ষমান)" else "🟠 PENDING"
                                                    else -> if (isBan) "🔴 EXPIRED (মেয়াদউত্তীর্ণ)" else "🔴 EXPIRED"
                                                },
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = when (commissionStatus) {
                                                    "Active" -> Color(0xFF2E7D32)
                                                    "Pending" -> Color(0xFFED6C02)
                                                    else -> Color(0xFFD32F2F)
                                                }
                                            )
                                        }

                                        // Toggle for testing
                                        Surface(
                                            onClick = {
                                                commissionStatus = when (commissionStatus) {
                                                    "Active" -> "Pending"
                                                    "Pending" -> "Expired"
                                                    else -> "Active"
                                                }
                                            },
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color.Gray),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("টোঙ্গল স্ট্যাটাস 🔄", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(if (isBan) "বর্তমান কমিশন রেট:" else "Current Commission:", fontSize = 12.sp)
                                        Text(currentCommissionRate, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(if (isBan) "বকেয়া কমিশন (Due):" else "Commission Due:", fontSize = 12.sp)
                                        Text("৳$commissionDueAmount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(if (isBan) "মেয়াদ (Valid Until):" else "Valid Until:", fontSize = 12.sp)
                                        Text(commissionValidUntil, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { showPayCommissionModal = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBan) "কমিশন পরিশোধ করুন (Pay Commission Due)" else "Pay Commission Due", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(if (isBan) "কমিশন পেমেন্ট হিস্ট্রি:" else "Commission History:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            listOf(
                                Triple("TXN-COM-8812", "৳1,000 Payment via bKash", "2026-07-31"),
                                Triple("TXN-COM-7710", "৳1,500 Payment via Nagad", "2026-06-30")
                            ).forEach { (txn, desc, date) ->
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE0E0E0))) {
                                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(desc, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                            Text("Txn: $txn", fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Text(date, fontSize = 10.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }

                        // --- 4. MY SCHEDULE ---
                        "Schedule" -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isBan) "রোগী দেখার স্ট্যাটাস" else "Available Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(if (isBan) "রোগী দেখার দিনসমূহ:" else "Visiting Days:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri").forEach { day ->
                                            val isSel = visitingDays.contains(day)
                                            FilterChip(
                                                selected = isSel,
                                                onClick = {
                                                    visitingDays = if (isSel) visitingDays - day else visitingDays + day
                                                },
                                                label = { Text(day, fontSize = 10.sp) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(value = visitingTime, onValueChange = { visitingTime = it }, label = { Text("সময় (Visiting Hours)") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = slotDuration, onValueChange = { slotDuration = it }, label = { Text("প্রতিটি স্লটের সময় (Slot Duration)") }, modifier = Modifier.fillMaxWidth())

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isBan) "ছুটি / Leave Mode" else "Leave / Vacation Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Switch(checked = isLeaveMode, onCheckedChange = { isLeaveMode = it })
                                    }
                                    if (isLeaveMode) {
                                        OutlinedTextField(value = leaveReason, onValueChange = { leaveReason = it }, label = { Text("ছুটির কারণ / তারিখ") }, modifier = Modifier.fillMaxWidth())
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { Toast.makeText(context, if (isBan) "সময়সূচী সেভ করা হয়েছে" else "Schedule updated", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Text(if (isBan) "সময়সূচী আপডেট করুন" else "Update Schedule")
                                    }
                                }
                            }
                        }

                        // --- 5. MY HOSPITALS ---
                        "Hospitals" -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE0E0E0))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(if (isBan) "বর্তমান হাসপাতাল & চেম্বার" else "Attached Hospital & Chamber", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1565C0))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("হাসপাতাল: স্কয়ার হাসপাতাল লিমিটেড", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("বিভাগ: কার্ডিওলজি বিভাগ", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("চেম্বার: রুম ৪০৪, পান্থপথ, ঢাকা", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            OutlinedButton(
                                onClick = { Toast.makeText(context, if (isBan) "হাসপাতাল যোগ করার রিকোয়েস্ট এডমিনে পাঠানো হয়েছে" else "Hospital request sent to admin", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AddBusiness, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBan) "+ নতুন হাসপাতাল / চেম্বার রিকোয়েস্ট" else "+ Request Add Hospital / Chamber")
                            }
                        }

                        // --- 6. MY PATIENTS ---
                        "Patients" -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("মোট রোগী", fontSize = 11.sp, color = Color.Gray)
                                        Text("১৪২ জন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                                    }
                                }
                                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("আজকের রোগী", fontSize = 11.sp, color = Color.Gray)
                                        Text("৫ জন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1565C0))
                                    }
                                }
                            }

                            listOf("মো: জামিল হোসেন - ইতিহাস: উচ্চ রক্তচাপ", "কাজী রফিকুল ইসলাম - ইতিহাস: ডায়াবেটিস").forEach { pat ->
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE0E0E0))) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(pat, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // --- 7. PRESCRIPTIONS ---
                        "Prescriptions" -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(if (isBan) "ডিজিটাল প্রেসক্রিপশন তৈরি করুন" else "Create Digital Prescription", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(value = pPatientName, onValueChange = { pPatientName = it }, label = { Text("রোগীর নাম *") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = pPatientAge, onValueChange = { pPatientAge = it }, label = { Text("বয়স / লিঙ্গ") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = pDiagnosis, onValueChange = { pDiagnosis = it }, label = { Text("ডায়াগনোসিস / লক্ষণ") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = pMedicines, onValueChange = { pMedicines = it }, label = { Text("ঔষধের তালিকা (Rx)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                                    OutlinedTextField(value = pFollowUpDate, onValueChange = { pFollowUpDate = it }, label = { Text("ফলো-আপ তারিখ") }, modifier = Modifier.fillMaxWidth())

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (pPatientName.isBlank()) {
                                                Toast.makeText(context, if (isBan) "রোগীর নাম দিন" else "Enter patient name", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, if (isBan) "প্রেসক্রিপশন সফলভাবে তৈরি করা হয়েছে!" else "Prescription Created!", Toast.LENGTH_SHORT).show()
                                                pPatientName = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBan) "প্রেসক্রিপশন প্রিন্ট / সেভ করুন" else "Save & Print Prescription")
                                    }
                                }
                            }
                        }

                        // --- 8. MEDICAL REPORTS ---
                        "Reports" -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE0E0E0))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("রোগীর আপলোড করা টেস্ট রিপোর্টস", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("• রক্ত পরীক্ষা (CBC) - ১০ আগস্ট ২০২৬", fontSize = 11.sp, color = Color.DarkGray)
                                    Text("• ইসিজি রিপোর্ট (ECG) - ০৮ আগস্ট ২০২৬", fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }
                        }

                        // --- 9. REVIEWS & RATING ---
                        "Reviews" -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("★ 4.9", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFFE65100))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Overall Rating", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("মোট ৮৬ জন রোগীর রিভিউ ভিত্তিক", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // --- 10. NOTIFICATIONS ---
                        "Notifications" -> {
                            listOf(
                                Triple("নতুন বুকিং নোটিফিকেশন", "মো: জামিল হোসেন নতুন বুকিং করেছেন।", "১০ মি. আগে"),
                                Triple("কমিশন স্ট্যাটাস নোটিফিকেশন", "আপনার আগস্ট মাসের কমিশন একটিভ রয়েছে।", "আজ")
                            ).forEach { (t, d, time) ->
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(t, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD32F2F))
                                        Text(d, fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }

                        // --- 11. MESSAGES ---
                        "Messages" -> {
                            OutlinedButton(
                                onClick = { Toast.makeText(context, "রোগীর সাথে চ্যাট চালু হচ্ছে...", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("রোগীর মেসেজ ইনবক্স")
                            }
                        }

                        // --- 12. VERIFICATION ---
                        "Verification" -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("BMDC & Doctor Verification: VERIFIED", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                    }
                                    Text("বিএমডিসি নম্বর A-78902 সফলভাবে এডমিন দ্বারা ভেরিফাইড হয়েছে।", fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }
                        }

                        // --- 13. DOCUMENTS ---
                        "Documents" -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE0E0E0))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("আপলোডকৃত সার্টিফিকেটসমূহ:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("1. MBBS Certificate.pdf (Verified)", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                    Text("2. BMDC License.pdf (Verified)", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                }
                            }
                        }

                        // --- 14. SUPPORT ---
                        "Support" -> {
                            OutlinedButton(
                                onClick = { Toast.makeText(context, "এডমিন সাপোর্ট সেন্টার খোলা হয়েছে", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("এডমিন সাপোর্ট & রিপোর্ট প্রবলেম")
                            }
                        }

                        // --- 15. SETTINGS ---
                        "Settings" -> {
                            SettingsAndSecurityDialogContent(viewModel = viewModel, language = language)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )

    // Pay Commission Modal Simulator
    if (showPayCommissionModal) {
        AlertDialog(
            onDismissRequest = { showPayCommissionModal = false },
            title = { Text(if (isBan) "কমিশন পরিশোধ করুন (bKash/Nagad)" else "Pay Commission Due") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    var selectedCommissionMethod by remember { mutableStateOf("bKash") }
                    Text("বকেয়া পরিমাণ: ৳$commissionDueAmount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("পেমেন্ট পদ্ধতি নির্বাচন করুন:", fontSize = 12.sp)
                    UnifiedPaymentMethodSelector(
                        selectedMethod = selectedCommissionMethod,
                        onMethodSelected = { selectedCommissionMethod = it },
                        availableMethods = listOf("bKash", "Google Pay", "Nagad", "Others")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            commissionStatus = "Active"
                            commissionDueAmount = 0
                            showPayCommissionModal = false
                            Toast.makeText(context, if (isBan) "কমিশন পেমেন্ট সফল! অ্যাকাউন্ট এক্টিভ করা হয়েছে।" else "Commission Payment Successful! Account Activated.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBan) "$selectedCommissionMethod দিয়ে ৳$commissionDueAmount পরিশোধ করুন" else "Pay ৳$commissionDueAmount via $selectedCommissionMethod")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPayCommissionModal = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun HospitalMyAccountDialog(
    user: BloodDonor,
    viewModel: MainViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBan = language == AppLanguage.BAN
    val context = LocalContext.current

    val registeredHospitals by viewModel.registeredHospitals.collectAsState()
    val allServiceBookings by viewModel.serviceBookings.collectAsState()

    val myHospital = registeredHospitals.find {
        it.phone == user.phone || it.email.equals(user.email, ignoreCase = true) || it.name.equals(user.name, ignoreCase = true)
    }

    var activeTab by remember { mutableStateOf("Bookings") } // Bookings, Profile, Notice, Support
    var bookingFilter by remember { mutableStateOf("ALL") }

    // Hospital Info Form State
    var hkName by remember(myHospital) { mutableStateOf(myHospital?.name ?: user.name) }
    var hkType by remember(myHospital) { mutableStateOf(myHospital?.type ?: "General Hospital") }
    var hkPhone by remember(myHospital) { mutableStateOf(myHospital?.phone ?: user.phone) }
    var hkAddress by remember(myHospital) { mutableStateOf(myHospital?.address ?: "") }
    var hkDistrict by remember(myHospital) { mutableStateOf(myHospital?.district ?: "Dhaka") }
    var hkUpazila by remember(myHospital) { mutableStateOf(myHospital?.upazila ?: "Dhanmondi") }
    var hkServices by remember(myHospital) { mutableStateOf(myHospital?.services ?: "Emergency, ICU, CCU, Pathology, Blood Bank, Doctor Consultation, Cabin, Ward") }
    var hkBloodAvail by remember(myHospital) { mutableStateOf(myHospital?.bloodAvailability ?: "A+, B+, O+, AB+ Available") }
    var hkEmergencyNotice by remember(myHospital) { mutableStateOf(myHospital?.urgentNotice ?: "২৪ ঘণ্টা জরুরি সেবা, আইসিইউ ও অ্যাম্বুলেন্স সার্ভিস চালু রয়েছে।") }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Filter hospital bookings
    val hospitalBookings = remember(allServiceBookings, user.phone, user.name, myHospital) {
        val cleanHospPhone = (myHospital?.phone ?: user.phone).filter { it.isDigit() }.takeLast(10)
        val hospNameStr = (myHospital?.name ?: user.name).trim()
        val matches = allServiceBookings.filter { b ->
            val isHosp = b.bookingType.contains("Hospital", ignoreCase = true)
            val cleanProvPhone = b.providerPhone.filter { it.isDigit() }.takeLast(10)
            val matchPhone = cleanHospPhone.isNotBlank() && cleanProvPhone.isNotBlank() && 
                (cleanHospPhone == cleanProvPhone || cleanProvPhone.endsWith(cleanHospPhone) || cleanHospPhone.endsWith(cleanProvPhone))
            val matchName = hospNameStr.isNotBlank() && b.providerName.isNotBlank() && 
                (b.providerName.contains(hospNameStr, ignoreCase = true) || hospNameStr.contains(b.providerName, ignoreCase = true))
            (isHosp && (matchPhone || matchName)) || matchPhone
        }
        if (matches.isEmpty()) {
            allServiceBookings.filter { it.bookingType.contains("Hospital", ignoreCase = true) }
        } else {
            matches
        }
    }

    val filteredList = hospitalBookings.filter { b ->
        when (bookingFilter) {
            "PENDING" -> b.status.equals("Pending", true)
            "CONFIRMED" -> b.status.equals("Confirmed", true)
            "COMPLETED" -> b.status.equals("Completed", true)
            "CANCELLED" -> b.status.equals("Cancelled", true)
            else -> true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBan) "🏥 হাসপাতাল মাই একাউন্ট" else "🏥 Hospital My Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFFB71C1C)
                    )
                }
                Text(
                    text = "${hkName.ifBlank { user.name }} • ${hkDistrict}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                // Category tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "Bookings" to if (isBan) "বুকিং রিকোয়েস্ট (${hospitalBookings.size})" else "Bookings (${hospitalBookings.size})",
                        "Profile" to if (isBan) "হাসপাতাল প্রোফাইল" else "Profile",
                        "Notice" to if (isBan) "জরুরি নোটিশ & ব্লাড" else "Notice & Blood",
                        "Support" to if (isBan) "সাপোর্ট ও যোগাযোগ" else "Support"
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
                                selectedContainerColor = Color(0xFFC62828),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (activeTab) {
                        "Bookings" -> {
                            // Status Filter Chips
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "ALL" to (if (isBan) "সব (${hospitalBookings.size})" else "All (${hospitalBookings.size})"),
                                    "PENDING" to (if (isBan) "অপেক্ষমান (${hospitalBookings.count { it.status.equals("Pending", true) }})" else "Pending (${hospitalBookings.count { it.status.equals("Pending", true) }})"),
                                    "CONFIRMED" to (if (isBan) "কনফার্মড (${hospitalBookings.count { it.status.equals("Confirmed", true) }})" else "Confirmed (${hospitalBookings.count { it.status.equals("Confirmed", true) }})"),
                                    "COMPLETED" to (if (isBan) "সম্পন্ন (${hospitalBookings.count { it.status.equals("Completed", true) }})" else "Completed (${hospitalBookings.count { it.status.equals("Completed", true) }})"),
                                    "CANCELLED" to (if (isBan) "বাতিল (${hospitalBookings.count { it.status.equals("Cancelled", true) }})" else "Cancelled (${hospitalBookings.count { it.status.equals("Cancelled", true) }})")
                                ).forEach { (fKey, label) ->
                                    FilterChip(
                                        selected = bookingFilter == fKey,
                                        onClick = { bookingFilter = fKey },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = if (bookingFilter == fKey) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFC62828),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            if (filteredList.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            if (isBan) "কোনো হাসপাতাল বুকিং পাওয়া যায়নি" else "No hospital bookings found",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            if (isBan) "রোগীরা আপনার হাসপাতালে আইসিইউ, কেবিন বা সেবার বুকিং করলে এখানে দেখতে পাবেন।" else "Incoming patient bookings will appear here.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                filteredList.forEach { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = req.patientName.ifBlank { req.userName },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = Color(0xFFB71C1C)
                                                    )
                                                    if (req.patientAge.isNotBlank()) {
                                                        Text("বয়স: ${req.patientAge} বছর", fontSize = 11.sp, color = Color.DarkGray)
                                                    }
                                                }
                                                Surface(
                                                    color = when (req.status.lowercase()) {
                                                        "pending" -> Color(0xFFED6C02)
                                                        "confirmed" -> Color(0xFF2E7D32)
                                                        "completed" -> Color(0xFF1976D2)
                                                        else -> Color(0xFFD32F2F)
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = when (req.status.lowercase()) {
                                                            "pending" -> if (isBan) "নতুন অনুরোধ" else "Pending"
                                                            "confirmed" -> if (isBan) "কনফার্মড" else "Confirmed"
                                                            "completed" -> if (isBan) "সম্পন্ন" else "Completed"
                                                            else -> if (isBan) "বাতিল" else "Cancelled"
                                                        },
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("সেবা: ${req.serviceName}", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                            Text("ভর্তির তারিখ: ${req.bookingDate}", fontSize = 12.sp, color = Color(0xFF0D47A1))
                                            if (req.notes.isNotBlank()) {
                                                Text("বিবরণ: ${req.notes}", fontSize = 11.sp, color = Color.DarkGray)
                                            }
                                            Text("রোগীর ফোন: ${req.patientPhone}", fontSize = 11.sp, color = Color.Gray)
                                            Text("আইডি: #${req.id}", fontSize = 10.sp, color = Color.LightGray)

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Call Patient Button
                                                Button(
                                                    onClick = {
                                                        try {
                                                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                                data = android.net.Uri.parse("tel:${req.patientPhone}")
                                                            }
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f).height(34.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (isBan) "রোগীকে কল" else "Call", fontSize = 11.sp)
                                                }

                                                if (req.status.equals("Pending", true)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateServiceBookingStatus(req.id, "Confirmed")
                                                            Toast.makeText(context, if (isBan) "বুকিং সফলভাবে কনফার্ম করা হয়েছে!" else "Booking Accepted & Confirmed!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (isBan) "কনফার্ম" else "Accept", fontSize = 11.sp)
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.updateServiceBookingStatus(req.id, "Cancelled")
                                                            Toast.makeText(context, if (isBan) "বুকিং বাতিল করা হয়েছে" else "Booking Cancelled", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (isBan) "বাতিল" else "Reject", fontSize = 11.sp)
                                                    }
                                                } else if (req.status.equals("Confirmed", true)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateServiceBookingStatus(req.id, "Completed")
                                                            Toast.makeText(context, if (isBan) "সেবা সম্পন্ন করা হয়েছে!" else "Booking Marked as Completed!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (isBan) "সম্পন্ন" else "Complete", fontSize = 11.sp)
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.updateServiceBookingStatus(req.id, "Cancelled")
                                                            Toast.makeText(context, if (isBan) "বুকিং বাতিল করা হয়েছে" else "Booking Cancelled", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(if (isBan) "বাতিল" else "Cancel", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Profile" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!isEditingProfile) {
                                        Text(hkName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFB71C1C))
                                        Text("ধরন: $hkType", fontSize = 12.sp, color = Color.DarkGray)
                                        Text("হটলাইন: $hkPhone", fontSize = 12.sp, color = Color(0xFF0D47A1), fontWeight = FontWeight.SemiBold)
                                        Text("ঠিকানা: $hkAddress, $hkUpazila, $hkDistrict", fontSize = 12.sp, color = Color.DarkGray)
                                        Text("সেবাসমূহ: $hkServices", fontSize = 12.sp, color = Color.DarkGray)
                                        Text("ব্লাড ব্যাংক তথ্য: $hkBloodAvail", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { isEditingProfile = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isBan) "তথ্য সম্পাদন করুন (Edit Details)" else "Edit Details")
                                        }
                                    } else {
                                        OutlinedTextField(value = hkName, onValueChange = { hkName = it }, label = { Text("হাসপাতালের নাম") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = hkType, onValueChange = { hkType = it }, label = { Text("ধরন (সরকারি/বেসরকারি/ক্লিনিক)") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = hkPhone, onValueChange = { hkPhone = it }, label = { Text("জরুরি হটলাইন নম্বর") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = hkAddress, onValueChange = { hkAddress = it }, label = { Text("বিস্তারিত ঠিকানা") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = hkDistrict, onValueChange = { hkDistrict = it }, label = { Text("জেলা") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = hkUpazila, onValueChange = { hkUpazila = it }, label = { Text("উপজেলা") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = hkServices, onValueChange = { hkServices = it }, label = { Text("সেবাসমূহ") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                                        OutlinedTextField(value = hkBloodAvail, onValueChange = { hkBloodAvail = it }, label = { Text("ব্লাড ব্যাংক প্রাপ্যতা") }, modifier = Modifier.fillMaxWidth())

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    isEditingProfile = false
                                                    if (myHospital != null) {
                                                        viewModel.updateHospitalDetails(myHospital.id, hkName, hkPhone, hkAddress, hkBloodAvail, hkEmergencyNotice)
                                                    } else {
                                                        viewModel.triggerHospitalSignup(
                                                            name = hkName,
                                                            type = hkType,
                                                            address = hkAddress,
                                                            district = hkDistrict,
                                                            upazila = hkUpazila,
                                                            phone = hkPhone,
                                                            email = user.email,
                                                            password = "123456",
                                                            services = hkServices,
                                                            bloodAvailability = hkBloodAvail
                                                        )
                                                    }
                                                    Toast.makeText(context, if (isBan) "হাসপাতালের তথ্য সফলভাবে সংরক্ষিত হয়েছে!" else "Hospital details saved successfully!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
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

                        "Notice" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                border = BorderStroke(1.dp, Color(0xFFFFE082))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(if (isBan) "জরুরি নোটিশ ও বার্তা" else "Emergency Notice", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFF57F17))
                                    OutlinedTextField(
                                        value = hkEmergencyNotice,
                                        onValueChange = { hkEmergencyNotice = it },
                                        label = { Text("রোগীদের জন্য জরুরি নোটিশ") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                    OutlinedTextField(
                                        value = hkBloodAvail,
                                        onValueChange = { hkBloodAvail = it },
                                        label = { Text("রক্তের বর্তমান স্টক / প্রাপ্যতা") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            if (myHospital != null) {
                                                viewModel.updateHospitalDetails(myHospital.id, hkName, hkPhone, hkAddress, hkBloodAvail, hkEmergencyNotice)
                                            } else {
                                                viewModel.triggerHospitalSignup(
                                                    name = hkName,
                                                    type = hkType,
                                                    address = hkAddress,
                                                    district = hkDistrict,
                                                    upazila = hkUpazila,
                                                    phone = hkPhone,
                                                    email = user.email,
                                                    password = "123456",
                                                    services = hkServices,
                                                    bloodAvailability = hkBloodAvail
                                                )
                                            }
                                            Toast.makeText(context, if (isBan) "জরুরি নোটিশ আপডেট সম্পন্ন হয়েছে!" else "Notice updated successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17))
                                    ) {
                                        Text(if (isBan) "নোটিশ পাবলিশ করুন" else "Publish Notice", color = Color.White)
                                    }
                                }
                            }
                        }

                        "Support" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(if (isBan) "📞 হাসপাতাল হেল্পডেস্ক ও সাপোর্ট" else "📞 Hospital Helpdesk & Support", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                    Text(if (isBan) "যেকোনো কারিগরি সমস্যা বা হাসপাতাল ভেরিফিকেশনের জন্য সরাসরি এডমিন সেন্টারে যোগাযোগ করুন।" else "Contact admin center for any support or verification.", fontSize = 12.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:01700000000")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.SupportAgent, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBan) "এডমিন সাপোর্টে কল করুন" else "Call Admin Support")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBan) "বন্ধ করুন" else "Close")
            }
        }
    )
}

// Helper composable for Quadruple mapping
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun SettingsAndSecurityDialogContent(viewModel: MainViewModel, language: AppLanguage) {
    val isBan = language == AppLanguage.BAN
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (isBan) "অ্যাপের ভাষা পরিবর্তন" else "App Language", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Button(onClick = { viewModel.toggleLanguage() }) {
                Text(if (isBan) "English" else "বাংলা")
            }
        }
    }
}

