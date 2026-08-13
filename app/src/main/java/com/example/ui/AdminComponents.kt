package com.example.ui

import android.content.Context
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.BloodDonor
import com.example.data.BloodRequest
import com.example.data.ScamReport
import com.example.data.CustomAdConfig
import com.example.data.V9SubscriptionPlan
import com.example.data.RegisteredHospital
import com.example.data.HospitalOffer
import com.example.data.HospitalSubscriptionPayment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// Design System Colors for Admin Dark Theme
val AdminDarkBg = Color(0xFF0F121D)
val AdminCardBg = Color(0xFF1E2230)
val AdminBorder = Color(0xFF2C3248)
val AdminTextWhite = Color.White
val AdminTextMuted = Color(0xFF8F9BB3)
val AdminPrimaryBlue = Color(0xFF2563EB)
val AdminAccRed = Color(0xFFEF4444)
val AdminAccGreen = Color(0xFF10B981)
val AdminAccOrange = Color(0xFFF59E0B)
val AdminAccPink = Color(0xFFEC4899)

fun saveMediaUriToInternalStorage(context: Context, uri: Uri, isVideo: Boolean): String {
    return try {
        val timeStamp = System.currentTimeMillis()
        val ext = if (isVideo) "mp4" else "jpg"
        val dir = java.io.File(context.filesDir, "cpa_ads")
        if (!dir.exists()) dir.mkdirs()
        val destFile = java.io.File(dir, "ad_media_$timeStamp.$ext")
        context.contentResolver.openInputStream(uri)?.use { input ->
            java.io.FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(destFile).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        uri.toString()
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        border = BorderStroke(1.dp, AdminBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AdminTextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = AdminTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AdminCountryStatsCard(
    donorsList: List<BloodDonor>,
    ambulancesList: List<com.example.data.Ambulance>,
    language: AppLanguage
) {
    val isBn = language == AppLanguage.BAN
    val countryOptions = listOf(
        "All Countries", "Bangladesh", "India", "Saudi Arabia", 
        "United Arab Emirates", "United States", "United Kingdom"
    )
    var selectedCountry by remember { mutableStateOf("All Countries") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val filteredList = remember(donorsList, selectedCountry) {
        donorsList.filter { donor ->
            when (selectedCountry) {
                "All Countries" -> true
                "Bangladesh" -> donor.country.isEmpty() || donor.country.equals("Bangladesh", ignoreCase = true)
                else -> donor.country.equals(selectedCountry, ignoreCase = true)
            }
        }
    }

    val totalCount = filteredList.size
    val donorCount = filteredList.count { it.role == "Donor" }
    val ambulanceCount = filteredList.count { it.role == "Ambulance" || ambulancesList.any { amb -> amb.phone == it.phone } }
    val regularUserCount = filteredList.count { it.role != "Donor" && it.role != "Ambulance" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        border = BorderStroke(1.dp, AdminBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Country Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "ইউজার পরিসংখ্যান ও দেশ ফিল্টার" else "User Statistics & Country Filter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AdminTextWhite
                    )
                    Text(
                        text = if (isBn) "কোন দেশে কতজন ডোনার ও ইউজার আছে দেখুন" else "View user & donor distribution by country",
                        fontSize = 11.sp,
                        color = AdminTextMuted
                    )
                }

                // Dropdown Button
                Box {
                    Button(
                        onClick = { expandedDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (selectedCountry == "All Countries" && isBn) "সকল দেশ" else selectedCountry,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.background(AdminCardBg).border(1.dp, AdminBorder)
                    ) {
                        countryOptions.forEach { c ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (c == "All Countries" && isBn) "সকল দেশ (All)" else c,
                                        color = AdminTextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (c == selectedCountry) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedCountry = c
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Users Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2563EB).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "$totalCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = AdminTextWhite
                        )
                        Text(
                            text = if (isBn) "মোট ইউজার" else "Total Users",
                            fontSize = 10.sp,
                            color = AdminTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Donors Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "$donorCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = AdminAccRed
                        )
                        Text(
                            text = if (isBn) "রক্তদাতা" else "Donors",
                            fontSize = 10.sp,
                            color = AdminTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Ambulance Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "$ambulanceCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = AdminAccGreen
                        )
                        Text(
                            text = if (isBn) "অ্যাম্বুলেন্স" else "Ambulances",
                            fontSize = 10.sp,
                            color = AdminTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Regular Users / Patients Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "$regularUserCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = AdminAccOrange
                        )
                        Text(
                            text = if (isBn) "সাধারণ ইউজার" else "Patients",
                            fontSize = 10.sp,
                            color = AdminTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Blood Group Breakdown Grid for selected country
            Text(
                text = if (isBn) "রক্তের গ্রুপ অনুযায়ী ডোনার সংখ্যা ($selectedCountry):" else "Blood Group Breakdown ($selectedCountry):",
                fontSize = 11.sp,
                color = AdminTextMuted,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                bloodGroups.forEach { bg ->
                    val bgCount = filteredList.count { it.bloodGroup == bg }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(AdminDarkBg, RoundedCornerShape(6.dp))
                            .border(1.dp, AdminBorder, RoundedCornerShape(6.dp))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(bg, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AdminAccRed)
                            Text("$bgCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminTextWhite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFiltersCard(
    language: AppLanguage,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterBloodGroup: String,
    onBloodGroupChange: (String) -> Unit,
    filterStatus: String,
    onStatusChange: (String) -> Unit,
    statusOptions: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        border = BorderStroke(1.dp, AdminBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (language == AppLanguage.ENG) "Interactive Live Filters" else "লাইভ ফিল্টার ও সার্চ",
                fontWeight = FontWeight.Bold,
                color = AdminTextWhite,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1.5f).height(46.dp),
                    placeholder = { 
                        Text(
                            text = if (language == AppLanguage.ENG) "Type name or phone..." else "নাম বা ফোন লিখুন...", 
                            fontSize = 11.sp, 
                            color = Color.Gray
                        ) 
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = AdminTextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder,
                        focusedContainerColor = AdminDarkBg,
                        unfocusedContainerColor = AdminDarkBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp)) }
                )

                // Blood Group dropdown
                var showBloodDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(AdminDarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, AdminBorder, RoundedCornerShape(8.dp))
                            .clickable { showBloodDropdown = true }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (filterBloodGroup == "All") (if (language == AppLanguage.ENG) "Blood: All" else "রক্ত: সব") else "Blood: $filterBloodGroup",
                            fontSize = 10.sp,
                            color = AdminTextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = showBloodDropdown,
                        onDismissRequest = { showBloodDropdown = false },
                        modifier = Modifier.background(AdminCardBg).border(1.dp, AdminBorder)
                    ) {
                        listOf("All", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").forEach { bg ->
                            DropdownMenuItem(
                                text = { Text(bg, color = AdminTextWhite, fontSize = 12.sp) },
                                onClick = {
                                    onBloodGroupChange(bg)
                                    showBloodDropdown = false
                                }
                            )
                        }
                    }
                }

                // Status Dropdown
                var showStatusDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(AdminDarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, AdminBorder, RoundedCornerShape(8.dp))
                            .clickable { showStatusDropdown = true }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (filterStatus == "All") (if (language == AppLanguage.ENG) "Status: All" else "অবস্থা: সব") else "Status: $filterStatus",
                            fontSize = 10.sp,
                            color = AdminTextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false },
                        modifier = Modifier.background(AdminCardBg).border(1.dp, AdminBorder)
                    ) {
                        statusOptions.forEach { stat ->
                            DropdownMenuItem(
                                text = { Text(stat, color = AdminTextWhite, fontSize = 12.sp) },
                                onClick = {
                                    onStatusChange(stat)
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.ENG) "Configure and filter resources instantly" else "তাত্ক্ষণিকভাবে রিসোর্স এডিট বা মডারেট করুন",
                    fontSize = 10.sp,
                    color = AdminTextMuted
                )
                
                Button(
                    onClick = {
                        onSearchChange("")
                        onBloodGroupChange("All")
                        onStatusChange("All")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AdminAccRed, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Reset Filters" else "ফিল্টার মুছুন",
                        color = AdminAccRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDonorsTab(
    donors: List<BloodDonor>,
    language: AppLanguage,
    onApprove: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSupportChat: (String, String) -> Unit,
    onWarnDonor: (String, Boolean, String) -> Unit
) {
    val context = LocalContext.current
    var showWarnDialog by remember { mutableStateOf(false) }
    var selectedDonorForWarning by remember { mutableStateOf<BloodDonor?>(null) }
    var warningReasonInput by remember { mutableStateOf("") }

    if (showWarnDialog && selectedDonorForWarning != null) {
        val selectedDonor = selectedDonorForWarning!!
        AlertDialog(
            onDismissRequest = { showWarnDialog = false },
            title = {
                Text(
                    text = if (selectedDonor.isWarning) "Modify / Remove Warning" else "Give Account Warning",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "You are warning ${selectedDonor.name} (${selectedDonor.displayUserId}). Users will see this warning and the reason when they view this profile.",
                        color = AdminTextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = warningReasonInput,
                        onValueChange = { warningReasonInput = it },
                        label = { Text("Warning Reason / সতর্ককরণের কারণ", color = AdminTextMuted) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminCardBg,
                            unfocusedContainerColor = AdminCardBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedDonor.isWarning) {
                        Button(
                            onClick = {
                                onWarnDonor(selectedDonor.id, false, "")
                                showWarnDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen)
                        ) {
                            Text("Remove Warning", fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = {
                            onWarnDonor(selectedDonor.id, true, warningReasonInput)
                            showWarnDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminAccRed),
                        enabled = warningReasonInput.isNotBlank()
                    ) {
                        Text(if (selectedDonor.isWarning) "Update Warning" else "Submit Warning", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarnDialog = false }) {
                    Text("Cancel", color = AdminTextMuted)
                }
            },
            containerColor = AdminCardBg,
            titleContentColor = Color.White,
            textContentColor = AdminTextMuted
        )
    }

    if (donors.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (language == AppLanguage.BAN) "কোনো ডোনার পাওয়া যায়নি।" else "No donors match this search.",
                color = AdminTextMuted,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 160.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(donors) { donor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    border = BorderStroke(1.dp, AdminBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(AdminAccRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(donor.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = donor.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AdminTextWhite,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (!donor.isApproved) {
                                    Box(
                                        modifier = Modifier
                                            .background(AdminAccOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Pending", 
                                            color = AdminAccOrange, 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(AdminAccGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Approved", 
                                            color = AdminAccGreen, 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .background(if (donor.role == "Requester") Color(0xFFE8F5E9).copy(alpha = 0.15f) else Color(0xFFECEFF1).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (donor.role == "Requester") "Seeker" else "Donor",
                                        color = if (donor.role == "Requester") Color(0xFF81C784) else Color(0xFFB0BEC5),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "User ID: ${donor.displayUserId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminAccOrange)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${donor.upazila}, ${donor.district}", fontSize = 11.sp, color = AdminTextMuted)
                            Text(text = "Phone: ${donor.phone}", fontSize = 11.sp, color = AdminTextWhite.copy(alpha = 0.8f))
                            
                            if (donor.isWarning) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(AdminAccRed.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .border(1.dp, AdminAccRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "⚠️ WARNED: ${donor.warningReason}", 
                                        color = AdminAccRed, 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!donor.isApproved) {
                                IconButton(
                                    onClick = { onApprove(donor.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(AdminAccGreen.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(Icons.Filled.Check, "Approve", tint = AdminAccGreen, modifier = Modifier.size(15.dp))
                                }
                            }

                            // Support Chat Button
                            IconButton(
                                onClick = { onSupportChat(donor.phone, donor.name) },
                                modifier = Modifier
                                        .size(32.dp)
                                        .background(AdminPrimaryBlue.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Filled.Forum, "Support Chat", tint = AdminPrimaryBlue, modifier = Modifier.size(15.dp))
                            }

                            // Warning Trigger Button
                            IconButton(
                                onClick = {
                                    selectedDonorForWarning = donor
                                    warningReasonInput = donor.warningReason
                                    showWarnDialog = true
                                },
                                modifier = Modifier
                                        .size(32.dp)
                                        .background(AdminAccOrange.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Filled.Warning, "Warn/Unwarn", tint = AdminAccOrange, modifier = Modifier.size(15.dp))
                            }

                            IconButton(
                                onClick = { onDelete(donor.id) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AdminAccRed.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Filled.Delete, "Delete", tint = AdminAccRed, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRequestsTab(
    requests: List<BloodRequest>,
    language: AppLanguage,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (language == AppLanguage.BAN) "কোনো রক্তের অনুরোধ পাওয়া যায়নি।" else "No blood requests match this search.",
                color = AdminTextMuted,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 160.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(requests) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    border = BorderStroke(1.dp, AdminBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Patient: ${req.patientName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminTextWhite)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (req.status == "Active") AdminAccRed.copy(alpha = 0.15f) else AdminAccGreen.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = req.status,
                                    color = if (req.status == "Active") AdminAccRed else AdminAccGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Needs ${req.bloodGroup} at ${req.hospitalName}", fontSize = 12.sp, color = AdminTextWhite.copy(alpha = 0.9f))
                        Text(text = "Contact: ${req.contactNumber}", fontSize = 11.sp, color = AdminTextMuted)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onToggle(req.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (req.status == "Active") AdminAccGreen.copy(alpha = 0.15f) else AdminAccRed.copy(alpha = 0.15f),
                                    contentColor = if (req.status == "Active") AdminAccGreen else AdminAccRed
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (req.status == "Active") "Mark Resolved" else "Activate",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { onDelete(req.id) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AdminAccRed.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Filled.Delete, "Delete", tint = AdminAccRed, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPoliciesTab(
    language: AppLanguage,
    privacyEn: String,
    privacyBn: String,
    privacyUrl: String = "",
    termsEn: String,
    termsBn: String,
    refundEn: String,
    refundBn: String,
    donorEn: String = "",
    donorBn: String = "",
    docEn: String = "",
    docBn: String = "",
    bloodEn: String = "",
    bloodBn: String = "",
    ambEn: String = "",
    ambBn: String = "",
    hospEn: String = "",
    hospBn: String = "",
    onSave: (
        privacyEn: String, privacyBn: String, privacyUrl: String,
        termsEn: String, termsBn: String,
        refundEn: String, refundBn: String,
        donorEn: String, donorBn: String,
        docEn: String, docBn: String,
        bloodEn: String, bloodBn: String,
        ambEn: String, ambBn: String,
        hospEn: String, hospBn: String
    ) -> Unit
) {
    var draftPrivacyEn by remember { mutableStateOf(privacyEn) }
    var draftPrivacyBn by remember { mutableStateOf(privacyBn) }
    var draftPrivacyUrl by remember { mutableStateOf(privacyUrl) }
    var draftTermsEn by remember { mutableStateOf(termsEn) }
    var draftTermsBn by remember { mutableStateOf(termsBn) }
    var draftRefundEn by remember { mutableStateOf(refundEn) }
    var draftRefundBn by remember { mutableStateOf(refundBn) }
    var draftDonorEn by remember { mutableStateOf(donorEn) }
    var draftDonorBn by remember { mutableStateOf(donorBn) }
    var draftDocEn by remember { mutableStateOf(docEn) }
    var draftDocBn by remember { mutableStateOf(docBn) }
    var draftBloodEn by remember { mutableStateOf(bloodEn) }
    var draftBloodBn by remember { mutableStateOf(bloodBn) }
    var draftAmbEn by remember { mutableStateOf(ambEn) }
    var draftAmbBn by remember { mutableStateOf(ambBn) }
    var draftHospEn by remember { mutableStateOf(hospEn) }
    var draftHospBn by remember { mutableStateOf(hospBn) }

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (language == AppLanguage.BAN) "পলিসি, টার্মস এন্ড কন্ডিশন ম্যানাজমেন্ট" else "Policy, Terms & Conditions Management",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AdminTextWhite
                )
                Text(
                    text = if (language == AppLanguage.BAN) "সকল সেবার পৃথক নীতিমালা ও শর্তাবলী এখান থেকে পরিবর্তন করুন" else "Manage separate rules & guidelines for each entity",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
            }
            Button(
                onClick = {
                    onSave(
                        draftPrivacyEn, draftPrivacyBn, draftPrivacyUrl,
                        draftTermsEn, draftTermsBn,
                        draftRefundEn, draftRefundBn,
                        draftDonorEn, draftDonorBn,
                        draftDocEn, draftDocBn,
                        draftBloodEn, draftBloodBn,
                        draftAmbEn, draftAmbBn,
                        draftHospEn, draftHospBn
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdminAccRed),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (language == AppLanguage.BAN) "সেভ করুন" else "Save All", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "ALL" to if (language == AppLanguage.BAN) "সবগুলো (All)" else "All Policies",
                "DONOR" to if (language == AppLanguage.BAN) "🩸 ডোনার পলিসি" else "Donor Policy",
                "DOCTOR" to if (language == AppLanguage.BAN) "🩺 ডাক্তার পলিসি" else "Doctor Policy",
                "BLOOD" to if (language == AppLanguage.BAN) "🆘 রক্তগ্রহীতা পলিসি" else "Requester Policy",
                "AMBULANCE" to if (language == AppLanguage.BAN) "🚑 অ্যাম্বুলেন্স পলিসি" else "Ambulance Policy",
                "HOSPITAL" to if (language == AppLanguage.BAN) "🏥 হাসপাতাল পলিসি" else "Hospital Policy",
                "PRIVACY" to if (language == AppLanguage.BAN) "🔒 প্রাইভেসি পলিসি" else "Privacy Policy",
                "TERMS" to if (language == AppLanguage.BAN) "📜 টার্মস এন্ড কন্ডিশন" else "Terms & Conditions",
                "REFUND" to if (language == AppLanguage.BAN) "💸 রিফান্ড পলিসি" else "Refund Policy"
            )

            filters.forEach { (key, label) ->
                val isSelected = selectedCategoryFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AdminPrimaryBlue,
                        selectedLabelColor = Color.White,
                        containerColor = AdminCardBg,
                        labelColor = AdminTextWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = AdminBorder,
                        selectedBorderColor = AdminPrimaryBlue
                    )
                )
            }
        }

        // 1. Donor Policy (ডোনার পলিসি)
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "DONOR") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🩸 1. Donor Policy (ডোনার পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                    }
                    Text("রক্তদাতা রেজিস্ট্রেশন ও রক্তদানের নিয়মাবলী", fontSize = 10.sp, color = AdminTextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("English Text", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftDonorEn,
                        onValueChange = { draftDonorEn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Bengali Text (বাংলা লেখা)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftDonorBn,
                        onValueChange = { draftDonorBn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }
            }
        }

        // 2. Doctor Policy (ডাক্তার পলিসি)
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "DOCTOR") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🩺 2. Doctor Policy (ডাক্তার পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                    Text("চিকিৎসক প্রোফাইল, বিএমডিসি ও চেম্বার সেবার শর্তাবলী", fontSize = 10.sp, color = AdminTextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("English Text", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftDocEn,
                        onValueChange = { draftDocEn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Bengali Text (বাংলা লেখা)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftDocBn,
                        onValueChange = { draftDocBn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }
            }
        }

        // 3. Blood Requester Policy (রক্তের প্রয়োজন গ্রহীতা পলিসি)
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "BLOOD") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🆘 3. Blood Requester Policy (রক্তের প্রয়োজন গ্রহীতা / রোগীর পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                    Text("জরুরি রক্তের অনুরোধ পোস্ট করার নিয়মাবলী", fontSize = 10.sp, color = AdminTextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("English Text", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftBloodEn,
                        onValueChange = { draftBloodEn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Bengali Text (বাংলা লেখা)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftBloodBn,
                        onValueChange = { draftBloodBn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }
            }
        }

        // 4. Ambulance Policy (অ্যাম্বুলেন্স সার্ভিস পলিসি)
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "AMBULANCE") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🚑 4. Ambulance Service Policy (অ্যাম্বুলেন্স সার্ভিস পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                    Text("অ্যাম্বুলেন্স চালক ও বুকিং সেবার নীতিমালা", fontSize = 10.sp, color = AdminTextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("English Text", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftAmbEn,
                        onValueChange = { draftAmbEn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Bengali Text (বাংলা লেখা)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftAmbBn,
                        onValueChange = { draftAmbBn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }
            }
        }

        // 5. Hospital Policy (হাসপাতাল ও ডায়াগনস্টিক পলিসি)
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "HOSPITAL") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🏥 5. Hospital & Diagnostic Policy (হাসপাতাল ও ডায়াগনস্টিক পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                    Text("হাসপাতাল পার্টনারশিপ ও বুকিং পলিসি", fontSize = 10.sp, color = AdminTextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("English Text", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftHospEn,
                        onValueChange = { draftHospEn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Bengali Text (বাংলা লেখা)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftHospBn,
                        onValueChange = { draftHospBn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }
            }
        }

        // 6. Privacy Policy (প্রাইভেসি পলিসি)
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "PRIVACY") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🔒 6. Privacy Policy (প্রাইভেসি পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                    Text("ব্যবহারকারীর তথ্যের সুরক্ষা ও ডাটা প্রাইভেসি নীতি", fontSize = 10.sp, color = AdminTextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("English Text", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftPrivacyEn,
                        onValueChange = { draftPrivacyEn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Bengali Text (বাংলা লেখা)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftPrivacyBn,
                        onValueChange = { draftPrivacyBn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Privacy Policy Web Link / ইউআরএল লিংক (https://...)", fontSize = 11.sp, color = AdminAccGreen, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = draftPrivacyUrl,
                        onValueChange = { draftPrivacyUrl = it },
                        placeholder = { Text("https://alifshengroup.com/privacy-policy", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminAccGreen,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }
            }
        }

        // 7. Terms & Conditions (টার্মস এন্ড কন্ডিশন)
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "TERMS") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("📜 7. General Terms & Conditions (সাধারণ টার্মস এন্ড কন্ডিশন)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                    Text("সমগ্র অ্যাপ্লিকেশন ব্যবহারের মূল শর্তাবলী", fontSize = 10.sp, color = AdminTextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("English Text", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftTermsEn,
                        onValueChange = { draftTermsEn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Bengali Text (বাংলা লেখা)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftTermsBn,
                        onValueChange = { draftTermsBn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }
            }
        }

        // 8. Refund Policy (রিফান্ড পলিসি)
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "REFUND") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💸 8. Refund Policy (রিফান্ড পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                    Text("বুকিং বাতিল ও অর্থ ফেরতের নীতি", fontSize = 10.sp, color = AdminTextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("English Text", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftRefundEn,
                        onValueChange = { draftRefundEn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Text("Bengali Text (বাংলা লেখা)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftRefundBn,
                        onValueChange = { draftRefundBn = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onSave(
                    draftPrivacyEn, draftPrivacyBn, draftPrivacyUrl,
                    draftTermsEn, draftTermsBn,
                    draftRefundEn, draftRefundBn,
                    draftDonorEn, draftDonorBn,
                    draftDocEn, draftDocBn,
                    draftBloodEn, draftBloodBn,
                    draftAmbEn, draftAmbBn,
                    draftHospEn, draftHospBn
                )
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AdminAccRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (language == AppLanguage.BAN) "সকল পলিসি ও টার্মস পেজ সংরক্ষণ করুন" else "Save All Policy Pages", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun AdminReportsTab(
    reports: List<ScamReport>,
    language: AppLanguage,
    onDismiss: (String) -> Unit,
    onBan: (String) -> Unit,
    strings: Map<String, String>,
    donors: List<BloodDonor> = emptyList(),
    onUpdateReport: ((id: String, scammerName: String, scammerPhone: String, amount: String, reason: String, status: String) -> Unit)? = null,
    viewModel: MainViewModel? = null
) {
    val context = LocalContext.current
    var selectedReport by remember { mutableStateOf<ScamReport?>(null) }
    var fullscreenImageUri by remember { mutableStateOf<String?>(null) }

    // Helper functions for dial and WhatsApp/SMS intents
    val makeCall: (String) -> Unit = { phone ->
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:$phone")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    val copyToClipboard: (String, String) -> Unit = { label, text ->
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy", Toast.LENGTH_SHORT).show()
        }
    }

    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (language == AppLanguage.BAN) "কোনো প্রতারণা বা স্ক্যাম রিপোর্ট পাওয়া যায়নি।" else "No fraud or scam reports registered.",
                color = AdminTextMuted,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(reports) { rep ->
                val accusedDonor = donors.find { it.id == rep.scammerDonorId || it.phone == rep.scammerDonorPhone }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedReport = rep },
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    border = BorderStroke(
                        1.dp,
                        when (rep.status) {
                            "Banned" -> AdminBorder
                            "Dismissed" -> AdminAccGreen.copy(alpha = 0.3f)
                            else -> AdminAccRed.copy(alpha = 0.4f)
                        }
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Report,
                                    contentDescription = "Alert",
                                    tint = AdminAccRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.BAN) "স্ক্যাম রিপোর্ট" else "Scam Report",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AdminTextWhite
                                )
                            }

                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (rep.status) {
                                            "Banned" -> AdminAccRed.copy(alpha = 0.2f)
                                            "Dismissed" -> AdminAccGreen.copy(alpha = 0.2f)
                                            else -> AdminAccOrange.copy(alpha = 0.2f)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = when (rep.status) {
                                        "Banned" -> if (language == AppLanguage.BAN) "নিষিদ্ধ" else "Banned"
                                        "Dismissed" -> if (language == AppLanguage.BAN) "বাতিল" else "Dismissed"
                                        else -> if (language == AppLanguage.BAN) "অপেক্ষমান" else "Pending"
                                    },
                                    color = when (rep.status) {
                                        "Banned" -> AdminAccRed
                                        "Dismissed" -> AdminAccGreen
                                        else -> AdminAccOrange
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Accused/Scammer info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${strings["admin_report_scammer"] ?: "Accused:"} ${rep.scammerDonorName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AdminAccRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Phone: ${rep.scammerDonorPhone}",
                                    fontSize = 11.sp,
                                    color = AdminTextMuted
                                )
                                if (accusedDonor != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(AdminPrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "Registered: ${accusedDonor.bloodGroup}",
                                                color = AdminPrimaryBlue,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${accusedDonor.district}, ${accusedDonor.upazila}",
                                            color = AdminTextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            // Quick Call Accused Button
                            IconButton(
                                onClick = { makeCall(rep.scammerDonorPhone) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AdminAccRed.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call Accused",
                                    tint = AdminAccRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = AdminBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Reporter info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${strings["admin_report_reporter"] ?: "Reporter:"} ${rep.reporterName}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = AdminTextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Phone: ${rep.reporterPhone}",
                                    fontSize = 11.sp,
                                    color = AdminTextMuted
                                )
                            }

                            // Quick Call Reporter Button
                            IconButton(
                                onClick = { makeCall(rep.reporterPhone) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AdminPrimaryBlue.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call Reporter",
                                    tint = AdminPrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Amount Involved
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "Money",
                                tint = AdminAccOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${strings["admin_report_amount"] ?: "Amount involved:"} ${rep.amountDemanded}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminAccOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Reason Description (brief)
                        Text(
                            text = "${strings["admin_report_desc"] ?: "Details:"} ${rep.reason}",
                            fontSize = 11.sp,
                            color = AdminTextWhite.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Bar inside Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { selectedReport = rep },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "View",
                                        tint = AdminPrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.BAN) "সম্পূর্ণ রিপোর্ট দেখুন" else "View Full Details",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AdminPrimaryBlue
                                    )
                                }
                            }

                            if (rep.status == "Pending") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = { onDismiss(rep.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = strings["btn_action_dismiss"] ?: "Dismiss",
                                            color = AdminTextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Button(
                                        onClick = { onBan(rep.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminAccRed),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = "Ban",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = strings["btn_action_ban"] ?: "Ban Scammer",
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

    // Detail Dialog showing ALL report details, reporter, accused, call options, proofs, and action panel
    selectedReport?.let { rep ->
        val accusedDonor = donors.find { it.id == rep.scammerDonorId || it.phone == rep.scammerDonorPhone }

        var isEditing by remember(rep.id) { mutableStateOf(false) }
        var editedName by remember(rep.id) { mutableStateOf(rep.scammerDonorName) }
        var editedPhone by remember(rep.id) { mutableStateOf(rep.scammerDonorPhone) }
        var editedAmount by remember(rep.id) { mutableStateOf(rep.amountDemanded) }
        var editedReason by remember(rep.id) { mutableStateOf(rep.reason) }
        var editedStatus by remember(rep.id) { mutableStateOf(rep.status) }

        var smsRecipient by remember(rep.id) { mutableStateOf("Reporter") }
        var smsText by remember(rep.id) { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedReport = null },
            containerColor = AdminCardBg,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isEditing) {
                                if (language == AppLanguage.BAN) "রিপোর্ট এডিট করুন" else "Edit Scam Report"
                            } else {
                                if (language == AppLanguage.BAN) "বিস্তারিত স্ক্যাম রিপোর্ট" else "Detailed Scam Report"
                            },
                            color = AdminTextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = { isEditing = !isEditing }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = "Toggle Edit Mode",
                                tint = AdminPrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(onClick = { selectedReport = null }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AdminTextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isEditing) {
                        // EDIT MODE VIEW
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Accused Scammer Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedLabelColor = AdminPrimaryBlue,
                                unfocusedLabelColor = AdminTextMuted
                            )
                        )

                        OutlinedTextField(
                            value = editedPhone,
                            onValueChange = { editedPhone = it },
                            label = { Text("Accused Scammer Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedLabelColor = AdminPrimaryBlue,
                                unfocusedLabelColor = AdminTextMuted
                            )
                        )

                        OutlinedTextField(
                            value = editedAmount,
                            onValueChange = { editedAmount = it },
                            label = { Text("Amount Demanded / Scammed") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedLabelColor = AdminPrimaryBlue,
                                unfocusedLabelColor = AdminTextMuted
                            )
                        )

                        OutlinedTextField(
                            value = editedReason,
                            onValueChange = { editedReason = it },
                            label = { Text("Scam Details / Reason") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedLabelColor = AdminPrimaryBlue,
                                unfocusedLabelColor = AdminTextMuted
                            )
                        )

                        Text(
                            text = "Set Status:",
                            color = AdminTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Pending", "Dismissed", "Banned").forEach { st ->
                                val isSel = editedStatus == st
                                Button(
                                    onClick = { editedStatus = st },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) {
                                            when (st) {
                                                "Banned" -> AdminAccRed
                                                "Dismissed" -> AdminAccGreen
                                                else -> AdminAccOrange
                                            }
                                        } else AdminBorder.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(st, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { isEditing = false },
                                colors = ButtonDefaults.buttonColors(containerColor = AdminBorder),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel", color = Color.White, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    onUpdateReport?.invoke(rep.id, editedName, editedPhone, editedAmount, editedReason, editedStatus)
                                    isEditing = false
                                    Toast.makeText(context, "Report updated successfully!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save Changes", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // VIEW MODE (NORMAL REPORT DETAILS)

                        // Status Badge Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    when (rep.status) {
                                        "Banned" -> AdminAccRed.copy(alpha = 0.15f)
                                        "Dismissed" -> AdminAccGreen.copy(alpha = 0.15f)
                                        else -> AdminAccOrange.copy(alpha = 0.15f)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    when (rep.status) {
                                        "Banned" -> AdminAccRed.copy(alpha = 0.3f)
                                        "Dismissed" -> AdminAccGreen.copy(alpha = 0.3f)
                                        else -> AdminAccOrange.copy(alpha = 0.3f)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (rep.status) {
                                        "Banned" -> if (language == AppLanguage.BAN) "🛑 অভিযুক্তকে নিষিদ্ধ করা হয়েছে" else "🛑 Accused is banned from the platform"
                                        "Dismissed" -> if (language == AppLanguage.BAN) "🟢 এই রিপোর্টটি বাতিল করা হয়েছে" else "🟢 This report was dismissed"
                                        else -> if (language == AppLanguage.BAN) "⚠️ রিপোর্টটি পর্যালোচনার জন্য অপেক্ষমান" else "⚠️ Report is pending review"
                                    },
                                    color = when (rep.status) {
                                        "Banned" -> AdminAccRed
                                        "Dismissed" -> AdminAccGreen
                                        else -> AdminAccOrange
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ID: ${rep.id} • ${rep.timestamp}",
                                    color = AdminTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // REPORTER DETAILS BOX
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AdminDarkBg),
                            border = BorderStroke(1.dp, AdminBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Reporter",
                                            tint = AdminPrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.BAN) "রিপোর্টকারী (The Reporter)" else "The Reporter (Who Submitted)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = AdminPrimaryBlue
                                        )
                                    }

                                    IconButton(
                                        onClick = { copyToClipboard("Reporter Phone", rep.reporterPhone) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, "copy", tint = AdminTextMuted, modifier = Modifier.size(12.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${strings["admin_report_reporter"] ?: "Name:"} ${rep.reporterName}",
                                    fontWeight = FontWeight.Bold,
                                    color = AdminTextWhite,
                                    fontSize = 13.sp
                                )

                                Text(
                                    text = "Phone: ${rep.reporterPhone}",
                                    color = AdminTextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Reporter Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { makeCall(rep.reporterPhone) },
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, "call", tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (language == AppLanguage.BAN) "কল করুন" else "Call Reporter", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // ACCUSED DETAILS BOX
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AdminDarkBg),
                            border = BorderStroke(1.dp, AdminBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Accused",
                                            tint = AdminAccRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.BAN) "অভিযুক্ত ব্যক্তি (The Accused)" else "The Accused (Suspect)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = AdminAccRed
                                        )
                                    }

                                    IconButton(
                                        onClick = { copyToClipboard("Accused Phone", rep.scammerDonorPhone) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, "copy", tint = AdminTextMuted, modifier = Modifier.size(12.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${strings["admin_report_scammer"] ?: "Name:"} ${rep.scammerDonorName}",
                                    fontWeight = FontWeight.Bold,
                                    color = AdminTextWhite,
                                    fontSize = 13.sp
                                )

                                Text(
                                    text = "Phone: ${rep.scammerDonorPhone}",
                                    color = AdminTextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                if (accusedDonor != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(AdminBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = if (language == AppLanguage.BAN) "🟢 ডাটাবেসে ডোনার প্রোফাইল পাওয়া গেছে" else "🟢 Registered Profile Found",
                                                color = AdminAccGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Blood Group: ${accusedDonor.bloodGroup} • District: ${accusedDonor.district} • Upazila: ${accusedDonor.upazila}",
                                                color = AdminTextWhite,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = "Email: ${accusedDonor.email} • Donation Count: ${accusedDonor.donationCount}",
                                                color = AdminTextMuted,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.BAN) "ℹ️ এই ফোন নম্বর দিয়ে কোনো ডোনার একাউন্ট নিবন্ধিত নেই।" else "ℹ️ No registered donor account with this phone number.",
                                        color = AdminTextMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Accused Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { makeCall(rep.scammerDonorPhone) },
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminAccRed),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, "call", tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (language == AppLanguage.BAN) "কল করুন" else "Call Accused", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // FRAUD DETAILS AND PROOFS
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AdminBorder.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .border(1.dp, AdminBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BAN) "অভিযোগের বিবরণ ও প্রমাণাদি" else "Report Details & Evidence",
                                fontWeight = FontWeight.Bold,
                                color = AdminTextWhite,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Text(
                                text = "${strings["admin_report_amount"] ?: "Amount involved:"} ${rep.amountDemanded}",
                                fontWeight = FontWeight.Bold,
                                color = AdminAccOrange,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Text(
                                text = "${strings["admin_report_desc"] ?: "Description:"} ${rep.reason}",
                                color = AdminTextWhite.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Evidence Image
                            if (rep.scammerPhotoUri != null) {
                                Text(
                                    text = if (language == AppLanguage.BAN) "সংযুক্ত প্রমাণ (স্ক্রিনশট):" else "Attached Screenshot/Proof:",
                                    fontWeight = FontWeight.SemiBold,
                                    color = AdminTextMuted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                AsyncImage(
                                    model = rep.scammerPhotoUri,
                                    contentDescription = "Scam Evidence Screenshot",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, AdminBorder, RoundedCornerShape(8.dp))
                                        .clickable { fullscreenImageUri = rep.scammerPhotoUri },
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )

                                Text(
                                    text = if (language == AppLanguage.BAN) "🔍 পূর্ণ স্ক্রিনে দেখতে ছবিতে ক্লিক করুন" else "🔍 Click image to view fullscreen",
                                    color = AdminTextMuted,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = if (language == AppLanguage.BAN) "🚫 কোনো অতিরিক্ত প্রমাণ/স্ক্রিনশট আপলোড করা হয়নি।" else "🚫 No evidence screenshot uploaded.",
                                    color = AdminTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // SMS COMMUNICATION PANEL
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AdminPrimaryBlue.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .border(1.dp, AdminPrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = "SMS", tint = AdminPrimaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.BAN) "এসএমএস ও যোগাযোগ প্যানেল" else "SMS & Communication Panel",
                                    fontWeight = FontWeight.Bold,
                                    color = AdminPrimaryBlue,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Choose Recipient
                            Text(
                                text = if (language == AppLanguage.BAN) "প্রাপক নির্বাচন করুন:" else "Choose Recipient:",
                                color = AdminTextMuted,
                                fontSize = 10.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Reporter", "Accused").forEach { role ->
                                    val label = if (role == "Reporter") {
                                        if (language == AppLanguage.BAN) "রিপোর্টকারী (${rep.reporterName})" else "Reporter (${rep.reporterName})"
                                    } else {
                                        if (language == AppLanguage.BAN) "অভিযুক্ত (${rep.scammerDonorName})" else "Accused (${rep.scammerDonorName})"
                                    }
                                    val isSelected = smsRecipient == role
                                    Button(
                                        onClick = { smsRecipient = role },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) AdminPrimaryBlue else AdminBorder.copy(alpha = 0.5f),
                                            contentColor = if (isSelected) Color.White else AdminTextMuted
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick templates
                            Text(
                                text = if (language == AppLanguage.BAN) "কুইক টেমপ্লেট:" else "Quick Templates:",
                                color = AdminTextMuted,
                                fontSize = 10.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val templates = if (smsRecipient == "Reporter") {
                                    listOf(
                                        "তদন্ত চলছে" to "Your scam report is under investigation. We are reviewing the details.",
                                        "প্রমাণ লাগবে" to "Please provide additional screenshot proofs of your scam report to our support.",
                                        "সমাধান হয়েছে" to "The accused donor has been banned. Thank you for making our community safe!"
                                    )
                                } else {
                                    listOf(
                                        "সতর্কবার্তা" to "Warning: A fraud report has been submitted against you. Clarify with support.",
                                        "নিষিদ্ধ ঘোষণা" to "You have been banned from BloodConnect due to fraudulent money requests.",
                                        "তথ্য যাচাই" to "Please contact BloodConnect support regarding pending transaction disputes."
                                    )
                                }

                                templates.forEach { (lbl, body) ->
                                    Button(
                                        onClick = { smsText = body },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminDarkBg),
                                        border = BorderStroke(1.dp, AdminBorder),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(lbl, fontSize = 9.sp, color = AdminTextWhite)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // SMS Input Field
                            OutlinedTextField(
                                value = smsText,
                                onValueChange = { smsText = it },
                                label = { Text(if (language == AppLanguage.BAN) "এসএমএস লিখুন" else "Type SMS message") },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AdminPrimaryBlue,
                                    unfocusedBorderColor = AdminBorder,
                                    focusedLabelColor = AdminPrimaryBlue,
                                    unfocusedLabelColor = AdminTextMuted
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Send Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val targetPhone = if (smsRecipient == "Reporter") rep.reporterPhone else rep.scammerDonorPhone

                                // Send via Phone (Direct Intent)
                                Button(
                                    onClick = {
                                        if (smsText.isBlank()) {
                                            Toast.makeText(context, "Please enter some message", Toast.LENGTH_SHORT).show()
                                        } else {
                                            try {
                                                val uri = android.net.Uri.parse("smsto:$targetPhone")
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, uri).apply {
                                                    putExtra("sms_body", smsText)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Failed to launch SMS app", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Phone, "phone_sms", tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (language == AppLanguage.BAN) "ফোনে পাঠান" else "Send via App", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // Send via Server Gateway (Simulated)
                                Button(
                                    onClick = {
                                        if (smsText.isBlank()) {
                                            Toast.makeText(context, "Please enter some message", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "SMS sent to $targetPhone via BloodConnect SMS Gateway!", Toast.LENGTH_LONG).show()
                                            // Save a system notification to simulate real SMS notification delivery in-app!
                                            viewModel?.sendSystemNotification(
                                                titleEn = "BloodConnect SMS Notification",
                                                titleBn = "ব্লাডকানেক্ট এসএমএস বিজ্ঞপ্তি",
                                                messageEn = "To $targetPhone: $smsText",
                                                messageBn = "$targetPhone নম্বরে: $smsText"
                                            )
                                            smsText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Send, "gateway_sms", tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (language == AppLanguage.BAN) "সার্ভার দিয়ে পাঠান" else "Send via Gateway", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!isEditing && rep.status == "Pending") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismiss(rep.id)
                                selectedReport = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminTextMuted),
                            border = BorderStroke(1.dp, AdminBorder),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, "dismiss", tint = AdminTextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings["btn_action_dismiss"] ?: "Dismiss", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                onBan(rep.id)
                                selectedReport = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccRed),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Block, "ban", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings["btn_action_ban"] ?: "Ban Scammer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )
    }

    // Zoomed/Fullscreen Evidence Screenshot Viewer Dialog
    fullscreenImageUri?.let { uri ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { fullscreenImageUri = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { fullscreenImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Fullscreen Screenshot Proof",
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .fillMaxHeight(0.8f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { fullscreenImageUri = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(if (language == AppLanguage.BAN) "বন্ধ করুন" else "Close Viewer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    viewModel: MainViewModel,
    language: AppLanguage,
    appName: String,
    onAppNameSave: (String) -> Unit,
    homeNotice: String,
    onHomeNoticeSave: (String) -> Unit,
    popupNotice: String,
    onPopupNoticeSave: (String) -> Unit,
    emailEnabled: Boolean,
    smtpHost: String,
    smtpPort: String,
    smtpUsername: String,
    smtpPassword: String,
    emailSubject: String,
    emailBody: String,
    onEmailConfigSave: (Boolean, String, String, String, String, String, String) -> Unit,
    adMobEnabled: Boolean,
    adMobAppId: String,
    adMobBannerId: String,
    adMobInterstitialId: String,
    adMobNativeId: String,
    onAdMobConfigSave: (Boolean, String, String, String, String) -> Unit,
    useMockStats: Boolean,
    mockTotalUsers: Int,
    mockTotalDonors: Int,
    onStatsConfigSave: (Boolean, Int, Int) -> Unit
) {
    var draftAppName by remember { mutableStateOf(appName) }
    var draftHomeNotice by remember { mutableStateOf(homeNotice) }
    var draftPopupNotice by remember { mutableStateOf(popupNotice) }
 
    var draftEmailEnabled by remember { mutableStateOf(emailEnabled) }
    var draftSmtpHost by remember { mutableStateOf(smtpHost) }
    var draftSmtpPort by remember { mutableStateOf(smtpPort) }
    var draftSmtpUsername by remember { mutableStateOf(smtpUsername) }
    var draftSmtpPassword by remember { mutableStateOf(smtpPassword) }
    var draftEmailSubject by remember { mutableStateOf(emailSubject) }
    var draftEmailBody by remember { mutableStateOf(emailBody) }

    var draftAdMobEnabled by remember { mutableStateOf(adMobEnabled) }
    var draftAdMobAppId by remember { mutableStateOf(adMobAppId) }
    var draftAdMobBannerId by remember { mutableStateOf(adMobBannerId) }
    var draftAdMobInterstitialId by remember { mutableStateOf(adMobInterstitialId) }
    var draftAdMobNativeId by remember { mutableStateOf(adMobNativeId) }

    var draftUseMockStats by remember { mutableStateOf(useMockStats) }
    var draftMockTotalUsers by remember { mutableStateOf(mockTotalUsers.toString()) }
    var draftMockTotalDonors by remember { mutableStateOf(mockTotalDonors.toString()) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val activeApiUrl by viewModel.apiUrl.collectAsState()
        val activeApiKey by viewModel.remoteApiKey.collectAsState()
        val isRemoteConnected by viewModel.isRemoteConnected.collectAsState()
        val isSyncing by viewModel.isSyncing.collectAsState()
        val syncError by viewModel.syncError.collectAsState()
        var editApiUrl by remember(activeApiUrl) { mutableStateOf(activeApiUrl) }
        var editApiKey by remember(activeApiKey) { mutableStateOf(activeApiKey) }

        // Firebase Realtime Database Configuration & Sync Card
        val isFbConnected by viewModel.isFirebaseConnected.collectAsState()
        var testResultMessage by remember { mutableStateOf<String?>(null) }
        var isTesting by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp).testTag("firebase_sync_card_admin"),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudSync,
                        contentDescription = "Firebase",
                        tint = if (isFbConnected) AdminAccGreen else AdminAccRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Firebase Realtime Sync" else "ফায়ারবেস ডাটাবেস স্ট্যাটাস ও টেস্ট (Firebase Connection)",
                        fontWeight = FontWeight.Bold,
                        color = AdminAccOrange,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) {
                        "Firebase Project: alif-blood-bank-2bc68 | Test live connection or re-sync all database nodes."
                    } else {
                        "ফায়ারবেস প্রজেক্ট: alif-blood-bank-2bc68 | ডাটাবেস কানেকশন সঠিক আছে কিনা টেস্ট করুন এবং ডাটাবেস সিঙ্ক করুন।"
                    },
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminDarkBg, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isFbConnected) AdminAccGreen else AdminAccRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFbConnected) 
                            (if (language == AppLanguage.ENG) "Status: Connected to Firebase" else "স্ট্যাটাস: ফায়ারবেসের সাথে কানেক্টেড (Connected)")
                        else 
                            (if (language == AppLanguage.ENG) "Status: Disconnected" else "স্ট্যাটাস: ডিসকানেক্টেড ( disconnected / rule issue)"),
                        color = if (isFbConnected) AdminAccGreen else AdminAccRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            isTesting = true
                            testResultMessage = null
                            viewModel.testFirebaseConnection { success, message ->
                                isTesting = false
                                testResultMessage = message
                            }
                        },
                        modifier = Modifier.weight(1f).height(42.dp).testTag("test_firebase_conn_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFbConnected) AdminAccGreen else AdminAccOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "test", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ENG) "Test Connection" else "কানেকশন টেস্ট করুন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.syncFirebaseData()
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.ENG) "Firebase sync triggered!" else "ফায়ারবেস সিঙ্ক চালু হয়েছে!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f).height(42.dp).testTag("firebase_sync_btn_admin"),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = "sync", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.ENG) "Re-Sync Data" else "ডাটা পুনঃসিঙ্ক করুন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val currentResult = testResultMessage
                if (currentResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentResult.contains("✅")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        border = BorderStroke(1.dp, if (currentResult.contains("✅")) Color(0xFFA5D6A7) else Color(0xFFFFCDD2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = currentResult,
                                fontSize = 12.sp,
                                color = if (currentResult.contains("✅")) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Ambulance Booking Acceptance Fee Settings
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            val bookingAcceptanceFeeState by viewModel.bookingAcceptanceFee.collectAsState()
            var feeDraft by remember(bookingAcceptanceFeeState) { mutableStateOf(bookingAcceptanceFeeState.toString()) }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "Ambulance Booking Acceptance Fee" else "অ্যাম্বুলেন্স বুকিং একসেপ্ট ফি (টাকা)",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccOrange,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) {
                        "Set the wallet deduction price required when an ambulance provider accepts a booking request."
                    } else {
                        "একজন অ্যাম্বুলেন্স ড্রাইভার/মালিক যখন বুকিং একসেপ্ট করবেন, তখন তার ওয়ালেট থেকে নির্ধারিত এই ফি কেটে নেওয়া হবে।"
                    },
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Text(
                        text = if (language == AppLanguage.ENG) "Booking Acceptance Fee (BDT)" else "বুকিং একসেপ্ট ফি (টাকা)",
                        fontSize = 11.sp,
                        color = AdminTextMuted
                    )
                    OutlinedTextField(
                        value = feeDraft,
                        onValueChange = { feeDraft = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val fee = feeDraft.toDoubleOrNull() ?: 50.0
                        viewModel.updateBookingAcceptanceFee(fee)
                        Toast.makeText(
                            context,
                            if (language == AppLanguage.ENG) "Booking Acceptance Fee Updated!" else "বুকিং একসেপ্ট ফি সফলভাবে সংরক্ষিত হয়েছে!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Save Acceptance Fee" else "বুকিং একসেপ্ট ফি সংরক্ষণ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // App Name Configuration
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "App Configuration" else "অ্যাপ কনফিগারেশন",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccRed,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == AppLanguage.ENG) "App Name (Top Display)" else "অ্যাপের নাম (উপরে প্রদর্শিত)",
                    fontSize = 12.sp,
                    color = AdminTextMuted
                )
                OutlinedTextField(
                    value = draftAppName,
                    onValueChange = { draftAppName = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder,
                        focusedContainerColor = AdminDarkBg,
                        unfocusedContainerColor = AdminDarkBg
                    )
                )
                
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onAppNameSave(draftAppName)
                        Toast.makeText(context, "App Name Updated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Update Name" else "নাম পরিবর্তন করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Home Announcement configuration
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "Home Announcement Notice" else "হোম এনাউন্সমেন্ট নোটিশ",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccRed,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "Appears prominently at top of home screen" else "ইউজারদের হোম পেজের উপরে এটি দেখা যাবে",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = draftHomeNotice,
                    onValueChange = { draftHomeNotice = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder,
                        focusedContainerColor = AdminDarkBg,
                        unfocusedContainerColor = AdminDarkBg
                    )
                )
                
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onHomeNoticeSave(draftHomeNotice)
                        Toast.makeText(context, "Notice Updated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Save Notice" else "ঘোষণা সংরক্ষণ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Popup notice configuration
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "Popup Alert (Gift Box Style)" else "পপ-আপ অ্যালার্ট (গিফট বক্স এলার্ট)",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccRed,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "Shows when the app first launches" else "অ্যাপ্লিকেশন চালু করার সাথে সাথে সামনে আসবে",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = draftPopupNotice,
                    onValueChange = { draftPopupNotice = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder,
                        focusedContainerColor = AdminDarkBg,
                        unfocusedContainerColor = AdminDarkBg
                    )
                )
                
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onPopupNoticeSave(draftPopupNotice)
                        Toast.makeText(context, "Popup Updated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Save Popup Alert" else "পপ-আপ সংরক্ষণ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Gmail SMTP Email notifications config
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "Gmail/Email SMTP Config" else "জিমেইল/SMTP ইমেইল সেটিংস",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccRed,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "Automatically alerts users in Gmail on SMS inquiries" else "ইন-অ্যাপ মেসেজ পেলে তার জিমেইলে অটো নোটিফিকেশন যাবে",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminDarkBg, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.ENG) "Gmail Alerts Status" else "জিমেইল নোটিফিকেশন অবস্থা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AdminTextWhite
                        )
                    }
                    Switch(
                        checked = draftEmailEnabled,
                        onCheckedChange = { draftEmailEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AdminAccRed,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = AdminBorder
                        )
                    )
                }

                if (draftEmailEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("SMTP Host", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftSmtpHost,
                        onValueChange = { draftSmtpHost = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("SMTP Port", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftSmtpPort,
                        onValueChange = { draftSmtpPort = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Gmail / SMTP Username", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftSmtpUsername,
                        onValueChange = { draftSmtpUsername = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("App Password", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftSmtpPassword,
                        onValueChange = { draftSmtpPassword = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = AdminBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Subject Template", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftEmailSubject,
                        onValueChange = { draftEmailSubject = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Email Body Template", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftEmailBody,
                        onValueChange = { draftEmailBody = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        minLines = 4,
                        maxLines = 8,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Use: \$senderName, \$receiverName, \$senderPhone, \$messageText", fontSize = 9.sp, color = AdminAccRed, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onEmailConfigSave(
                            draftEmailEnabled,
                            draftSmtpHost,
                            draftSmtpPort,
                            draftSmtpUsername,
                            draftSmtpPassword,
                            draftEmailSubject,
                            draftEmailBody
                        )
                        Toast.makeText(context, "Gmail SMTP Config Saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Save SMTP Settings" else "SMTP সেটিংস সেভ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Google AdMob configuration Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "Google AdMob Config" else "গুগল এডমোব কনফিগারেশন",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccGreen,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "Manage banner, interstitial and native ad unit IDs" else "ব্যানার, ইন্টারস্টিশিয়াল এবং নেটিভ বিজ্ঞাপনের আইডি ও সেটিংস নিয়ন্ত্রণ করুন",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminDarkBg, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.ENG) "AdMob Ads Status" else "বিজ্ঞাপন প্রদর্শন অবস্থা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AdminTextWhite
                        )
                    }
                    Switch(
                        checked = draftAdMobEnabled,
                        onCheckedChange = { draftAdMobEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AdminAccGreen,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = AdminBorder
                        )
                    )
                }

                if (draftAdMobEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("AdMob App ID", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftAdMobAppId,
                        onValueChange = { draftAdMobAppId = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Banner Ad Unit ID", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftAdMobBannerId,
                        onValueChange = { draftAdMobBannerId = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Interstitial Ad Unit ID", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftAdMobInterstitialId,
                        onValueChange = { draftAdMobInterstitialId = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Native Ad Unit ID", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = draftAdMobNativeId,
                        onValueChange = { draftAdMobNativeId = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onAdMobConfigSave(
                            draftAdMobEnabled,
                            draftAdMobAppId,
                            draftAdMobBannerId,
                            draftAdMobInterstitialId,
                            draftAdMobNativeId
                        )
                        Toast.makeText(context, "Google AdMob Config Saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Save AdMob Settings" else "এডমোব সেটিংস সেভ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Live Statistics Configuration (Mock Data)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "Live Statistics (Display)" else "লাইভ স্ট্যাটিসটিকস (ডিসপ্লে)",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccRed,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "Show large mock numbers to attract users" else "ইউজার আকর্ষন করার জন্য বড় ফেইক নাম্বার দেখাতে পারেন",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = draftUseMockStats,
                        onCheckedChange = { draftUseMockStats = it },
                        colors = CheckboxDefaults.colors(checkedColor = AdminPrimaryBlue, uncheckedColor = AdminBorder)
                    )
                    Text(
                        text = if (language == AppLanguage.ENG) "Use Custom (Mock) Stats" else "কাস্টম (ফেইক) স্ট্যাটাস ব্যবহার করুন",
                        color = AdminTextWhite,
                        fontSize = 13.sp
                    )
                }

                if (draftUseMockStats) {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text("Total Customers:", color = AdminTextMuted, fontSize = 11.sp)
                    OutlinedTextField(
                        value = draftMockTotalUsers,
                        onValueChange = { draftMockTotalUsers = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Total Donors:", color = AdminTextMuted, fontSize = 11.sp)
                    OutlinedTextField(
                        value = draftMockTotalDonors,
                        onValueChange = { draftMockTotalDonors = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val users = draftMockTotalUsers.toIntOrNull() ?: 80424
                        val donors = draftMockTotalDonors.toIntOrNull() ?: 12300
                        onStatsConfigSave(draftUseMockStats, users, donors)
                        Toast.makeText(context, "Statistics Settings Saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Save Statistics" else "পরিসংখ্যান সংরক্ষণ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Custom CPA/Affiliate Ads Config Card (Affmine, CPA, Banner Network)
        val customAdsEnabledState by viewModel.customAdsEnabled.collectAsState()
        val customAdConfigsState by viewModel.customAdConfigs.collectAsState()

        var draftCustomAdsEnabled by remember(customAdsEnabledState) { mutableStateOf(customAdsEnabledState) }
        var currentAdConfigsList by remember(customAdConfigsState) { mutableStateOf(customAdConfigsState) }

        // Editing state for updating existing ads
        var editingAdId by remember { mutableStateOf<String?>(null) }

        // State variables for adding/editing an ad config
        var newAdNetworkName by remember { mutableStateOf("") }
        var newAdTitle by remember { mutableStateOf("") }
        var newAdWeight by remember { mutableStateOf("1") }
        var newAdTargetUrl by remember { mutableStateOf("") }
        var newAdTargetCountries by remember { mutableStateOf("All") }
        var cpaCountryDropdownExpanded by remember { mutableStateOf(false) }
        
        // Media upload mode state: "url" or "gallery"
        var mediaSourceType by remember { mutableStateOf("url") } // "url" or "gallery"
        var isVideoType by remember { mutableStateOf(false) } // true for video, false for image
        var customMediaUrlInput by remember { mutableStateOf("") }
        var selectedGalleryUri by remember { mutableStateOf<Uri?>(null) }

        // Setup image/video picker launcher from gallery with persistent storage saving
        val adMediaPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                val persistentPath = saveMediaUriToInternalStorage(context, uri, isVideoType)
                selectedGalleryUri = Uri.parse(persistentPath)
                mediaSourceType = "gallery"
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "Affiliate & CPA Ads Config (Affmine etc.)" else "অ্যাফিলিয়েট এবং CPA বিজ্ঞাপন কনফিগারেশন (Affmine ইত্যাদি)",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccGreen,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "Add network banner ads, weights, and image/video uploads" else "বিজ্ঞাপন নেটওয়ার্ক, ওয়েট (Weight), এবং ইমেজ/ভিডিও গ্যালারি আপলোড যুক্ত করুন",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminDarkBg, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.ENG) "CPA Banner Ads Status" else "CPA ব্যানার বিজ্ঞাপন অবস্থা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AdminTextWhite
                        )
                    }
                    Switch(
                        checked = draftCustomAdsEnabled,
                        onCheckedChange = { draftCustomAdsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AdminAccGreen,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = AdminBorder
                        )
                    )
                }

                if (draftCustomAdsEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 1: LIST OF CURRENT NETWORKS
                    Text(
                        text = if (language == AppLanguage.ENG) "Active CPA Ad Networks" else "সক্রিয় CPA বিজ্ঞাপন নেটওয়ার্কসমূহ",
                        fontWeight = FontWeight.Bold,
                        color = AdminTextWhite,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentAdConfigsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AdminDarkBg, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.ENG) "No ad networks configured yet." else "কোনো বিজ্ঞাপন নেটওয়ার্ক যুক্ত করা হয়নি।",
                                fontSize = 11.sp,
                                color = AdminTextMuted
                            )
                        }
                    } else {
                        currentAdConfigsList.forEach { ad ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(AdminDarkBg.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .border(1.dp, AdminBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = ad.networkName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AdminAccGreen
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Weight: ${ad.weight}",
                                            fontSize = 10.sp,
                                            color = AdminAccOrange,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .background(AdminAccOrange.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (ad.isVideo) "Video" else "Image",
                                            fontSize = 10.sp,
                                            color = AdminPrimaryBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .background(AdminPrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ad.title,
                                        fontSize = 11.sp,
                                        color = AdminTextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = ad.targetUrl,
                                        fontSize = 9.sp,
                                        color = AdminTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // EDIT BUTTON
                                    IconButton(
                                        onClick = {
                                            editingAdId = ad.id
                                            newAdNetworkName = ad.networkName
                                            newAdTitle = ad.title
                                            newAdWeight = ad.weight.toString()
                                            newAdTargetUrl = ad.targetUrl
                                            newAdTargetCountries = ad.targetCountries
                                            isVideoType = ad.isVideo
                                            val mediaPath = if (ad.isVideo) ad.videoUrl else ad.bannerUrl
                                            if (mediaPath.startsWith("http://") || mediaPath.startsWith("https://")) {
                                                mediaSourceType = "url"
                                                customMediaUrlInput = mediaPath
                                                selectedGalleryUri = null
                                            } else {
                                                mediaSourceType = "gallery"
                                                customMediaUrlInput = mediaPath
                                                selectedGalleryUri = if (mediaPath.isNotEmpty()) Uri.parse(mediaPath) else null
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = AdminPrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // DELETE BUTTON
                                    IconButton(
                                        onClick = {
                                            if (editingAdId == ad.id) {
                                                editingAdId = null
                                            }
                                            val updatedList = currentAdConfigsList.filter { it.id != ad.id }
                                            currentAdConfigsList = updatedList
                                            viewModel.updateCustomAdConfigsList(context, updatedList)
                                            Toast.makeText(context, "Ad removed and saved!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = AdminAccRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = AdminBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 2: ADD / EDIT AD FORM
                    if (editingAdId != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AdminPrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, AdminPrimaryBlue, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.ENG) "Editing Ad (ID: ${editingAdId?.take(8)})" else "বিজ্ঞাপন এডিট করা হচ্ছে (আইডি: ${editingAdId?.take(8)})",
                                color = AdminPrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            TextButton(
                                onClick = {
                                    editingAdId = null
                                    newAdNetworkName = ""
                                    newAdTitle = ""
                                    newAdWeight = "1"
                                    newAdTargetUrl = ""
                                    newAdTargetCountries = "All"
                                    customMediaUrlInput = ""
                                    selectedGalleryUri = null
                                    mediaSourceType = "url"
                                    isVideoType = false
                                }
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ENG) "Cancel Edit" else "বাতিল করুন",
                                    color = AdminAccRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Text(
                            text = if (language == AppLanguage.ENG) "Add New Network / Offer" else "নতুন নেটওয়ার্ক / অফার যোগ করুন",
                            fontWeight = FontWeight.Bold,
                            color = AdminTextWhite,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(if (language == AppLanguage.ENG) "Ad Network Name" else "বিজ্ঞাপন নেটওয়ার্কের নাম (যেমন: Affmine)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = newAdNetworkName,
                        onValueChange = { newAdNetworkName = it },
                        placeholder = { Text("e.g. Affmine", color = AdminTextMuted.copy(alpha = 0.5f), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(if (language == AppLanguage.ENG) "Promotional Title / Headline" else "প্রোমোশনাল শিরোনাম / হেডলাইন", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = newAdTitle,
                        onValueChange = { newAdTitle = it },
                        placeholder = { Text("e.g. Join the best offer and earn!", color = AdminTextMuted.copy(alpha = 0.5f), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (language == AppLanguage.ENG) "Ad Rotation Weight / Priority" else "রোটেশন ওয়েট / অগ্রাধিকার (Weight)", fontSize = 11.sp, color = AdminTextMuted)
                            OutlinedTextField(
                                value = newAdWeight,
                                onValueChange = { newAdWeight = it },
                                placeholder = { Text("1", color = AdminTextMuted.copy(alpha = 0.5f), fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                            )
                        }

                        Column(modifier = Modifier.weight(1.2f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (language == AppLanguage.ENG) "Target Countries" else "টার্গেট দেশসমূহ", fontSize = 11.sp, color = AdminTextMuted)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.ENG) "(Select ▾)" else "(ড্রপডাউন ▾)",
                                    fontSize = 10.sp,
                                    color = AdminPrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { cpaCountryDropdownExpanded = true }
                                )
                            }
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = newAdTargetCountries,
                                    onValueChange = { newAdTargetCountries = it },
                                    placeholder = { Text("e.g. Bangladesh, All", color = AdminTextMuted.copy(alpha = 0.5f), fontSize = 12.sp) },
                                    trailingIcon = {
                                        IconButton(onClick = { cpaCountryDropdownExpanded = !cpaCountryDropdownExpanded }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Countries Dropdown", tint = AdminPrimaryBlue)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                                )

                                DropdownMenu(
                                    expanded = cpaCountryDropdownExpanded,
                                    onDismissRequest = { cpaCountryDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(AdminCardBg)
                                        .border(1.dp, AdminBorder, RoundedCornerShape(8.dp))
                                ) {
                                    val cpaCountryOptions = listOf(
                                        Pair("All", if (language == AppLanguage.ENG) "All Countries (সকল দেশ)" else "সকল দেশ (All)"),
                                        Pair("Bangladesh", if (language == AppLanguage.ENG) "Bangladesh (বাংলাদেশ)" else "বাংলাদেশ (Bangladesh)"),
                                        Pair("India", "India (ভারত)"),
                                        Pair("Pakistan", "Pakistan (পাকিস্তান)"),
                                        Pair("Saudi Arabia", "Saudi Arabia (সৌদি আরব)"),
                                        Pair("United Arab Emirates", "UAE (ইউএই)"),
                                        Pair("Qatar", "Qatar (কাতার)"),
                                        Pair("Kuwait", "Kuwait (কুয়েত)"),
                                        Pair("Oman", "Oman (ওমান)"),
                                        Pair("Malaysia", "Malaysia (মালয়েশিয়া)"),
                                        Pair("United States", "USA (যুক্তরাষ্ট্র)"),
                                        Pair("United Kingdom", "UK (যুক্তরাজ্য)"),
                                        Pair("Canada", "Canada (কানাডা)"),
                                        Pair("Australia", "Australia (অস্ট্রেলিয়া)"),
                                        Pair("International", "International (অন্যান্য সকল দেশ)")
                                    )

                                    cpaCountryOptions.forEach { (code, label) ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val isSelected = newAdTargetCountries.split(",").map { it.trim() }.any { it.equals(code, ignoreCase = true) }
                                                    Text(
                                                        text = label,
                                                        color = if (isSelected) AdminAccGreen else AdminTextWhite,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    if (isSelected) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = AdminAccGreen, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            },
                                            onClick = {
                                                cpaCountryDropdownExpanded = false
                                                if (code == "All") {
                                                    newAdTargetCountries = "All"
                                                } else {
                                                    val currentList = newAdTargetCountries.split(",").map { it.trim() }.filter { it.isNotBlank() && !it.equals("All", ignoreCase = true) }.toMutableList()
                                                    if (currentList.any { it.equals(code, ignoreCase = true) }) {
                                                        currentList.removeAll { it.equals(code, ignoreCase = true) }
                                                    } else {
                                                        currentList.add(code)
                                                    }
                                                    newAdTargetCountries = if (currentList.isEmpty()) "All" else currentList.joinToString(", ")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quick Country Chips Selection
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Bangladesh", "Saudi Arabia", "United Arab Emirates", "India", "United States", "International").forEach { country ->
                            val isSel = newAdTargetCountries.split(",").map { it.trim() }.any { it.equals(country, ignoreCase = true) }
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSel) AdminPrimaryBlue else AdminDarkBg,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(1.dp, if (isSel) AdminPrimaryBlue else AdminBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (country == "All") {
                                            newAdTargetCountries = "All"
                                        } else {
                                            val currentList = newAdTargetCountries.split(",").map { it.trim() }.filter { it.isNotBlank() && !it.equals("All", ignoreCase = true) }.toMutableList()
                                            if (currentList.any { it.equals(country, ignoreCase = true) }) {
                                                currentList.removeAll { it.equals(country, ignoreCase = true) }
                                            } else {
                                                currentList.add(country)
                                            }
                                            newAdTargetCountries = if (currentList.isEmpty()) "All" else currentList.joinToString(", ")
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (country == "All") "সকল দেশ (All)" else country,
                                    fontSize = 10.sp,
                                    color = if (isSel) Color.White else AdminTextMuted,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(if (language == AppLanguage.ENG) "Target CPA Redirect Link (Affiliate Link)" else "টার্গেট CPA রিডাইরেক্ট লিংক (অ্যাফিলিয়েট লিংক)", fontSize = 11.sp, color = AdminTextMuted)
                    OutlinedTextField(
                        value = newAdTargetUrl,
                        onValueChange = { newAdTargetUrl = it },
                        placeholder = { Text("https://...", color = AdminTextMuted.copy(alpha = 0.5f), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // MEDIA TYPE SELECTOR: Image or Video
                    Text(
                        text = if (language == AppLanguage.ENG) "Select Banner Media Type" else "ব্যানার মিডিয়ার ধরণ নির্বাচন করুন",
                        fontSize = 11.sp,
                        color = AdminTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isVideoType = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isVideoType) AdminPrimaryBlue else AdminDarkBg
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .border(1.dp, if (!isVideoType) AdminPrimaryBlue else AdminBorder, RoundedCornerShape(6.dp)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ENG) "Image Banner" else "ছবি ব্যানার",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { isVideoType = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVideoType) AdminPrimaryBlue else AdminDarkBg
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .border(1.dp, if (isVideoType) AdminPrimaryBlue else AdminBorder, RoundedCornerShape(6.dp)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ENG) "Video Banner" else "ভিডিও ব্যানার",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // MEDIA SOURCE TYPE: URL or Gallery Upload
                    Text(
                        text = if (language == AppLanguage.ENG) "Media Source Option" else "মিডিয়া সোর্সের অপশন",
                        fontSize = 11.sp,
                        color = AdminTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { mediaSourceType = "url" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mediaSourceType == "url") AdminPrimaryBlue else AdminDarkBg
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .border(1.dp, if (mediaSourceType == "url") AdminPrimaryBlue else AdminBorder, RoundedCornerShape(6.dp)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.ENG) "Enter custom URL" else "যেকোনো ইউআরএল দিন",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { mediaSourceType = "gallery" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mediaSourceType == "gallery") AdminPrimaryBlue else AdminDarkBg
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .border(1.dp, if (mediaSourceType == "gallery") AdminPrimaryBlue else AdminBorder, RoundedCornerShape(6.dp)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ENG) "Gallery Upload" else "গ্যালারি থেকে আপলোড",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (mediaSourceType == "url") {
                        Text(if (language == AppLanguage.ENG) "Media URL" else "মিডিয়া ইউআরএল লিংক (ছবি বা ভিডিও লিংক)", fontSize = 11.sp, color = AdminTextMuted)
                        OutlinedTextField(
                            value = customMediaUrlInput,
                            onValueChange = { customMediaUrlInput = it },
                            placeholder = { Text("https://example.com/banner.jpg", color = AdminTextMuted.copy(alpha = 0.5f), fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                        )
                    } else {
                        // Gallery selection button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AdminDarkBg, RoundedCornerShape(8.dp))
                                .border(1.dp, AdminBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val typeStr = if (isVideoType) "video/*" else "image/*"
                                    adMediaPickerLauncher.launch(typeStr)
                                }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isVideoType) Icons.Default.Videocam else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = AdminAccGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedGalleryUri == null) {
                                        if (language == AppLanguage.ENG) "Click to Pick File from Gallery" else "গ্যালারি থেকে ফাইল নির্বাচন করতে ক্লিক করুন"
                                    } else {
                                        if (language == AppLanguage.ENG) "File Selected!" else "ফাইল সফলভাবে সিলেক্ট হয়েছে!"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedGalleryUri == null) AdminTextWhite else AdminAccGreen
                                )
                            }
                        }

                        if (selectedGalleryUri != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "URI: ${selectedGalleryUri.toString()}",
                                fontSize = 9.sp,
                                color = AdminTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // BUTTON TO ADD / UPDATE NETWORK IN LIST
                    Button(
                        onClick = {
                            if (newAdNetworkName.isBlank() || newAdTitle.isBlank() || newAdTargetUrl.isBlank()) {
                                Toast.makeText(context, "Please fill in Network Name, Title and Target Link!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val finalMediaUrl = if (mediaSourceType == "url") {
                                customMediaUrlInput
                            } else {
                                selectedGalleryUri?.toString() ?: customMediaUrlInput
                            }

                            if (finalMediaUrl.isBlank()) {
                                Toast.makeText(context, "Please provide a media URL or select from gallery!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val weightVal = newAdWeight.toIntOrNull() ?: 1

                            val adConfig = CustomAdConfig(
                                id = editingAdId ?: java.util.UUID.randomUUID().toString(),
                                networkName = newAdNetworkName,
                                title = newAdTitle,
                                bannerUrl = finalMediaUrl,
                                isVideo = isVideoType,
                                videoUrl = if (isVideoType) finalMediaUrl else "",
                                targetUrl = newAdTargetUrl,
                                targetCountries = newAdTargetCountries,
                                weight = weightVal
                            )

                            val updatedList = if (editingAdId != null) {
                                currentAdConfigsList.map {
                                    if (it.id == editingAdId) adConfig else it
                                }
                            } else {
                                currentAdConfigsList + adConfig
                            }
                            currentAdConfigsList = updatedList
                            viewModel.updateCustomAdConfigsList(context, updatedList)
                            viewModel.updateCustomAdsConfig(
                                context,
                                draftCustomAdsEnabled,
                                if (updatedList.isNotEmpty()) updatedList.first().networkName else "Affmine",
                                if (updatedList.isNotEmpty()) updatedList.first().title else "Earn with Affmine CPA Network!",
                                if (updatedList.isNotEmpty()) (if (updatedList.first().isVideo) updatedList.first().videoUrl else updatedList.first().bannerUrl) else "",
                                if (updatedList.isNotEmpty()) updatedList.first().targetUrl else "https://www.affmine.com",
                                if (updatedList.isNotEmpty()) updatedList.first().targetCountries else "All"
                            )
                            Toast.makeText(context, "Ad saved & synced to server!", Toast.LENGTH_SHORT).show()
                            editingAdId = null

                            // Clear add form fields
                            newAdNetworkName = ""
                            newAdTitle = ""
                            newAdWeight = "1"
                            newAdTargetUrl = ""
                            customMediaUrlInput = ""
                            selectedGalleryUri = null
                        },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (editingAdId != null) AdminAccGreen else AdminPrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (editingAdId != null) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Action",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (editingAdId != null) {
                                if (language == AppLanguage.ENG) "Update Ad Network in List (✓)" else "বিজ্ঞাপন নেটওয়ার্ক আপডেট করুন (✓)"
                            } else {
                                if (language == AppLanguage.ENG) "Add Ad Network to List (+)" else "বিজ্ঞাপন নেটওয়ার্ক রোটেশন তালিকায় যোগ করুন (+)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updateCustomAdsConfig(
                            context,
                            draftCustomAdsEnabled,
                            if (currentAdConfigsList.isNotEmpty()) currentAdConfigsList.first().networkName else "Affmine",
                            if (currentAdConfigsList.isNotEmpty()) currentAdConfigsList.first().title else "Earn with Affmine CPA Network!",
                            if (currentAdConfigsList.isNotEmpty()) (if (currentAdConfigsList.first().isVideo) currentAdConfigsList.first().videoUrl else currentAdConfigsList.first().bannerUrl) else "",
                            if (currentAdConfigsList.isNotEmpty()) currentAdConfigsList.first().targetUrl else "https://www.affmine.com",
                            if (currentAdConfigsList.isNotEmpty()) currentAdConfigsList.first().targetCountries else "All"
                        )
                        viewModel.updateCustomAdConfigsList(context, currentAdConfigsList)
                        Toast.makeText(context, "All Affiliate Ad Settings Saved Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Save All CPA Ads Settings" else "সকল বিজ্ঞাপন রোটেশন সেটিংস সেভ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Country Management Section (নতুন কার্ড দেশসমূহ পরিচালনা করার জন্য)
        val customCountriesList by viewModel.customCountries.collectAsState()
        var newCountryName by remember { mutableStateOf("") }
        var newCountryCode by remember { mutableStateOf("") }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.ENG) "Manage System Countries" else "সিস্টেমের দেশসমূহ পরিচালনা করুন",
                    fontWeight = FontWeight.Bold,
                    color = AdminAccRed,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) "Add or remove countries supported by the registration and search networks." else "রেজিস্ট্রেশন ও সার্চ নেটওয়ার্কের জন্য সমর্থিত দেশের তালিকা যোগ করুন বা বাদ দিন।",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                // List of current countries with simple UI and delete button
                Text(
                    text = if (language == AppLanguage.ENG) "Active Countries List (${customCountriesList.size})" else "সক্রিয় দেশের তালিকা (${customCountriesList.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminTextWhite
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminDarkBg, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    customCountriesList.forEach { (ctyName, ctyCode) ->
                        val flag = try {
                            val firstChar = Character.codePointAt(ctyCode.uppercase(), 0) - 0x41 + 0x1F1E6
                            val secondChar = Character.codePointAt(ctyCode.uppercase(), 1) - 0x41 + 0x1F1E6
                            String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
                        } catch (e: Exception) {
                            "🌐"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(flag, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("$ctyName ($ctyCode)", color = AdminTextWhite, fontSize = 13.sp)
                            }

                            if (!ctyName.equals("Bangladesh", ignoreCase = true)) {
                                IconButton(
                                    onClick = {
                                        viewModel.deleteCountry(context, ctyName)
                                        Toast.makeText(context, "$ctyName deleted successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = AdminAccRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = if (language == AppLanguage.ENG) "Required" else "আবশ্যক",
                                    color = AdminTextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Form to Add Country
                Text(
                    text = if (language == AppLanguage.ENG) "Add New Country" else "নতুন দেশ যুক্ত করুন",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminTextWhite
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newCountryName,
                    onValueChange = { newCountryName = it },
                    label = { Text(if (language == AppLanguage.ENG) "Country Name" else "দেশের নাম", color = AdminTextMuted) },
                    placeholder = { Text("e.g. Canada", color = AdminTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder,
                        focusedContainerColor = AdminDarkBg,
                        unfocusedContainerColor = AdminDarkBg
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newCountryCode,
                    onValueChange = { newCountryCode = it },
                    label = { Text(if (language == AppLanguage.ENG) "Country Code (2 letters)" else "দেশের কোড (২ অক্ষর)", color = AdminTextMuted) },
                    placeholder = { Text("e.g. CA", color = AdminTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder,
                        focusedContainerColor = AdminDarkBg,
                        unfocusedContainerColor = AdminDarkBg
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val name = newCountryName.trim()
                        val code = newCountryCode.trim().uppercase()
                        if (name.isBlank() || code.length != 2) {
                            Toast.makeText(context, if (language == AppLanguage.ENG) "Please enter valid name and 2-letter code" else "অনুগ্রহ করে সঠিক নাম এবং ২ অক্ষরের দেশের কোড দিন", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addCountry(context, name, code)
                            Toast.makeText(context, "$name Added successfully!", Toast.LENGTH_SHORT).show()
                            newCountryName = ""
                            newCountryCode = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ENG) "Add Country" else "দেশ যুক্ত করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSupportTab(viewModel: MainViewModel, language: AppLanguage) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BAN
    val messagesList by viewModel.messages.collectAsState()

    val currentPhone by viewModel.supportPhone.collectAsState()
    val currentEmail by viewModel.supportEmail.collectAsState()
    val currentTelegram by viewModel.supportTelegram.collectAsState()
    val currentWhatsapp by viewModel.supportWhatsapp.collectAsState()
    val currentHours by viewModel.supportHours.collectAsState()
    val currentAddress by viewModel.supportAddress.collectAsState()

    var phoneInput by remember(currentPhone) { mutableStateOf(currentPhone) }
    var emailInput by remember(currentEmail) { mutableStateOf(currentEmail) }
    var telegramInput by remember(currentTelegram) { mutableStateOf(currentTelegram) }
    var whatsappInput by remember(currentWhatsapp) { mutableStateOf(currentWhatsapp) }
    var hoursInput by remember(currentHours) { mutableStateOf(currentHours) }
    var addressInput by remember(currentAddress) { mutableStateOf(currentAddress) }

    var activeSubTab by remember { mutableStateOf(0) } // 0: Config Channels, 1: Live Chats

    val supportChats = remember(messagesList) {
        messagesList
            .filter { it.receiverPhone == "LIVE_SUPPORT" || it.senderPhone == "LIVE_SUPPORT" }
            .groupBy { 
                if (it.senderPhone == "LIVE_SUPPORT") it.receiverPhone else it.senderPhone 
            }
            .map { (userPhone, msgs) ->
                val lastMsg = msgs.last()
                val userName = if (lastMsg.senderPhone == "LIVE_SUPPORT") lastMsg.receiverName else lastMsg.senderName
                userPhone to (userName to lastMsg)
            }
            .sortedByDescending { it.second.second.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-Tab Switcher Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AdminCardBg, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Pair(if (isBn) "📞 কন্টাক্ট ইনফো & চানেল সেটিংস" else "📞 Contact & Support Config", 0),
                Pair(if (isBn) "💬 লাইভ চ্যাট মেসেজেস (${supportChats.size})" else "💬 Live Chat Room (${supportChats.size})", 1)
            )

            tabs.forEach { (label, idx) ->
                val isSelected = activeSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AdminPrimaryBlue else Color.Transparent)
                        .clickable { activeSubTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else AdminTextMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                // CONFIG FORM
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    border = BorderStroke(1.dp, AdminBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = AdminPrimaryBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "কাস্টমার সাপোর্ট কন্টাক্ট ইনফরমেশন কনফিগারেশন" else "Customer Support Contact Information Config",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AdminTextWhite
                            )
                        }

                        Text(
                            text = if (isBn) "এখানে দেওয়া তথ্যগুলো ব্যবহারকারীদের সাপোর্ট ডায়ালগ ও হেল্পডেস্কে হুবহু দেখানো হবে:" else "The information updated here will be displayed dynamically in the Support Modal for all users:",
                            fontSize = 11.sp,
                            color = AdminTextMuted
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text(if (isBn) "অফিশিয়াল সাপোর্ট ফোন নম্বর(সমূহ)" else "Official Support Phone Number(s)", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text(if (isBn) "অফিশিয়াল সাপোর্ট ইমেইল এড্রেস" else "Official Support Email Address", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = telegramInput,
                            onValueChange = { telegramInput = it },
                            label = { Text(if (isBn) "টেলিগ্রাম গ্রুপ / চ্যানেল লিংক" else "Telegram Group / Channel Link", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = whatsappInput,
                            onValueChange = { whatsappInput = it },
                            label = { Text(if (isBn) "হোয়াটসঅ্যাপ সাপোর্ট নম্বর" else "WhatsApp Support Number", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = hoursInput,
                            onValueChange = { hoursInput = it },
                            label = { Text(if (isBn) "সাপোর্ট সার্ভিস টাইম / কাজের সময়" else "Support Working Hours", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text(if (isBn) "অফিসিয়াল ঠিকানা / হেডাঅফিস" else "Official Head Office Address", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.updateSupportConfig(
                                    context = context,
                                    phone = phoneInput,
                                    email = emailInput,
                                    telegram = telegramInput,
                                    whatsapp = whatsappInput,
                                    hours = hoursInput,
                                    address = addressInput
                                )
                                Toast.makeText(context, if (isBn) "সাপোর্ট তথ্য সফলভাবে সংরক্ষিত হয়েছে!" else "Support information saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "সাপোর্ট তথ্য সংরক্ষণ করুন" else "Save Support Details",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            1 -> {
                // LIVE CHAT ROOMS LIST
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    if (supportChats.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    if (isBn) "এখনও কোনো লাইভ সাপোর্ট মেসেজ নেই।" else "No live support messages yet.",
                                    color = AdminTextMuted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    items(supportChats) { chat ->
                        val phone = chat.first
                        val (name, lastMsg) = chat.second
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openChatRoom(phone, name, isSupport = true) },
                            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                            border = BorderStroke(1.dp, AdminBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(AdminPrimaryBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(name, color = AdminTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(lastMsg.message, color = AdminTextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(lastMsg.timestamp.split(" ").last(), color = AdminTextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAmbulancesTab(
    ambulances: List<com.example.data.Ambulance>,
    language: AppLanguage,
    onToggleAvailability: (String) -> Unit,
    onDelete: (String) -> Unit,
    onUpdatePlan: (String, String) -> Unit = { _, _ -> }
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        if (ambulances.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (language == AppLanguage.ENG) "No ambulances registered yet." else "এখনও কোনো অ্যাম্বুলেন্স নিবন্ধিত হয়নি।",
                        color = AdminTextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        items(ambulances) { amb ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(amb.serviceName, color = AdminTextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                val isAdv = amb.planType.lowercase().contains("advance") || amb.planType.lowercase().contains("premium")
                                Surface(
                                    color = if (isAdv) Color(0xFFFF8F00) else Color(0xFF616161),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isAdv) "ADVANCE PLAN" else "FREE PLAN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("${amb.ownerName} (${amb.phone})", color = AdminTextMuted, fontSize = 12.sp)
                        }
                        
                        // Availability Status Indicator Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (amb.isAvailable) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (amb.isAvailable) {
                                    if (language == AppLanguage.ENG) "Available" else "সক্রিয়"
                                } else {
                                    if (language == AppLanguage.ENG) "Busy" else "ব্যস্ত"
                                },
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = AdminBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(if (language == AppLanguage.ENG) "LOCATION" else "ঠিকানা", color = AdminTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${amb.upazila}, ${amb.district}", color = AdminTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Column {
                            Text(if (language == AppLanguage.ENG) "VEHICLE TYPE" else "গাড়ির ধরন", color = AdminTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(amb.ambulanceType, color = AdminTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    if (amb.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(amb.description, color = AdminTextMuted, fontSize = 11.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Admin actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Toggle Availability Action Button
                        Button(
                            onClick = { onToggleAvailability(amb.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (language == AppLanguage.ENG) "Toggle Status" else "অবস্থা পরিবর্তন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        // Switch Plan (Free vs Advance)
                        val isAdv = amb.planType.lowercase().contains("advance") || amb.planType.lowercase().contains("premium")
                        Button(
                            onClick = {
                                if (!isAdv) {
                                    onUpdatePlan(amb.id, "Advance (Monthly)")
                                    Toast.makeText(context, if (language == AppLanguage.ENG) "Upgraded to Advance Plan" else "অ্যাডভান্স প্ল্যানে আপগ্রেড করা হয়েছে", Toast.LENGTH_SHORT).show()
                                } else {
                                    onUpdatePlan(amb.id, "Free")
                                    Toast.makeText(context, if (language == AppLanguage.ENG) "Set to Free Plan" else "ফ্রি প্ল্যানে সেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isAdv) Color(0xFF616161) else Color(0xFFE65100)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (!isAdv) (if (language == AppLanguage.ENG) "Set Advance" else "অ্যাডভান্স করুন") else (if (language == AppLanguage.ENG) "Set Free" else "ফ্রি করুন"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        // Direct call Action Button
                        IconButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:${amb.phone}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot launch dialer", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.background(AdminAccGreen, RoundedCornerShape(8.dp)).size(40.dp)
                        ) {
                            Icon(Icons.Default.Phone, "Call Owner", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        
                        // Delete Registry Button
                        IconButton(
                            onClick = { onDelete(amb.id) },
                            modifier = Modifier.background(Color(0xFFD32F2F), RoundedCornerShape(8.dp)).size(40.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete Ambulance", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBookingsTab(
    bookings: List<com.example.data.AmbulanceBooking> = emptyList(),
    serviceBookings: List<com.example.data.ServiceBooking> = emptyList(),
    language: AppLanguage,
    viewModel: MainViewModel? = null,
    onDelete: ((String) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips Row
        val totalCount = serviceBookings.size + bookings.size
        val hospCount = serviceBookings.count { it.bookingType.contains("Hospital", ignoreCase = true) }
        val docCount = serviceBookings.count { it.bookingType.contains("Doctor", ignoreCase = true) }
        val ambCount = bookings.size

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to (if (language == AppLanguage.BAN) "সব বুকিং ($totalCount)" else "All Bookings ($totalCount)"),
                "HOSPITAL" to (if (language == AppLanguage.BAN) "হাসপাতাল সার্ভিস ($hospCount)" else "Hospital ($hospCount)"),
                "DOCTOR" to (if (language == AppLanguage.BAN) "ডাক্তার অ্যাপয়েন্টমেন্ট ($docCount)" else "Doctor Appointments ($docCount)"),
                "AMBULANCE" to (if (language == AppLanguage.BAN) "অ্যাম্বুলেন্স ট্রিপ ($ambCount)" else "Ambulance Trips ($ambCount)")
            ).forEach { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AdminAccGreen,
                        selectedLabelColor = Color.White,
                        containerColor = AdminCardBg,
                        labelColor = AdminTextMuted
                    )
                )
            }
        }

        val showService = selectedFilter == "ALL" || selectedFilter == "HOSPITAL" || selectedFilter == "DOCTOR"
        val showAmbulance = selectedFilter == "ALL" || selectedFilter == "AMBULANCE"

        val filteredService = serviceBookings.filter {
            when (selectedFilter) {
                "HOSPITAL" -> it.bookingType.contains("Hospital", ignoreCase = true)
                "DOCTOR" -> it.bookingType.contains("Doctor", ignoreCase = true)
                else -> true
            }
        }

        val hasAny = (showService && filteredService.isNotEmpty()) || (showAmbulance && bookings.isNotEmpty())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            if (!hasAny) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (language == AppLanguage.ENG) "No booking requests found." else "এখনও কোনো বুকিং অনুরোধ আসেনি।",
                            color = AdminTextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Display Service Bookings (Doctor / Hospital)
            if (showService) {
                items(filteredService) { book ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                        border = BorderStroke(1.dp, AdminBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "[${book.bookingType}] ${book.providerName}",
                                        color = AdminTextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "রোগী: ${book.patientName} (${book.patientPhone})",
                                        color = AdminTextMuted,
                                        fontSize = 12.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (book.status.lowercase()) {
                                                "confirmed" -> Color(0xFF1B5E20)
                                                "completed" -> Color(0xFF0288D1)
                                                "cancelled" -> Color(0xFFB71C1C)
                                                else -> Color(0xFFE65100)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = book.status,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = AdminBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row {
                                    Text(text = "সার্ভিস/ডাক্তার: ", color = AdminTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(book.serviceName, color = AdminTextWhite, fontSize = 11.sp)
                                }
                                Row {
                                    Text(text = "বুকিং তারিখ: ", color = AdminTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(book.bookingDate, color = AdminTextWhite, fontSize = 11.sp)
                                }
                                Row {
                                    Text(text = "বুকিংকারী: ", color = AdminTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${book.userName} (${book.userPhone})", color = AdminTextWhite, fontSize = 11.sp)
                                }
                                if (book.notes.isNotBlank()) {
                                    Row {
                                        Text(text = "নোট: ", color = AdminTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(book.notes, color = AdminTextWhite, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                data = android.net.Uri.parse("tel:${book.patientPhone.ifBlank { book.userPhone }}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot launch dialer", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রোগীকে কল", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (book.status == "Pending") {
                                    Button(
                                        onClick = {
                                            viewModel?.updateServiceBookingStatus(book.id, "Confirmed")
                                            Toast.makeText(context, "Booking Confirmed!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("কনফার্ম করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel?.deleteServiceBooking(book.id)
                                        Toast.makeText(context, "Booking Deleted!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.background(Color(0xFFD32F2F), RoundedCornerShape(8.dp)).size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Delete Booking", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Display Ambulance Bookings
            if (showAmbulance) {
                items(bookings) { book ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                        border = BorderStroke(1.dp, AdminBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("[Ambulance] " + book.pickupAddress, color = AdminTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("${book.patientName} (${book.contactPhone})", color = AdminTextMuted, fontSize = 12.sp)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (book.status.lowercase()) {
                                                "confirmed" -> Color(0xFF1B5E20)
                                                "cancelled" -> Color(0xFFB71C1C)
                                                else -> Color(0xFFE65100)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = book.status,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = AdminBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row {
                                    Text(
                                        text = (if (language == AppLanguage.ENG) "Destination: " else "গন্তব্য: "),
                                        color = AdminTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(book.destinationAddress, color = AdminTextWhite, fontSize = 11.sp)
                                }
                                Row {
                                    Text(
                                        text = (if (language == AppLanguage.ENG) "Urgency: " else "জরুরি অবস্থা: "),
                                        color = AdminTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(book.urgencyLevel, color = AdminTextWhite, fontSize = 11.sp)
                                }
                                Row {
                                    Text(
                                        text = (if (language == AppLanguage.ENG) "Booking Date: " else "বুকিং তারিখ: "),
                                        color = AdminTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(book.dateTime, color = AdminTextWhite, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                data = android.net.Uri.parse("tel:${book.contactPhone}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot launch dialer", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (language == AppLanguage.ENG) "Call Booker" else "বুককারীকে কল", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { onDelete?.invoke(book.id) },
                                    modifier = Modifier.background(Color(0xFFD32F2F), RoundedCornerShape(8.dp)).size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Delete Booking", tint = Color.White, modifier = Modifier.size(16.dp))
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
fun AdminHospitalsTab(viewModel: MainViewModel, language: AppLanguage) {
    val hospitals by viewModel.registeredHospitals.collectAsState()
    val offers by viewModel.hospitalOffers.collectAsState()
    val payments by viewModel.hospitalPayments.collectAsState()
    val context = LocalContext.current
    val isBn = language == AppLanguage.BAN

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Hospitals, 1: Offers, 2: Payments
    var showAddOfferDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-navigation tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(AdminCardBg, RoundedCornerShape(10.dp))
                .border(1.dp, AdminBorder, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subTabTitles = if (isBn) {
                listOf("হাসপাতাল / ডায়াগনস্টিক", "অফারসমূহ", "পেমেন্ট হিস্ট্রি")
            } else {
                listOf("Hospitals & Diagnostics", "Offers", "Payment History")
            }

            subTabTitles.forEachIndexed { index, title ->
                Button(
                    onClick = { selectedSubTab = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSubTab == index) AdminPrimaryBlue else Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSubTab == index) Color.White else AdminTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        when (selectedSubTab) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    if (hospitals.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    if (isBn) "কোনো নিবন্ধিত হাসপাতাল বা ডায়াগনস্টিক সেন্টার পাওয়া যায়নি।" else "No hospitals or diagnostics registered yet.",
                                    color = AdminTextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    items(hospitals) { hosp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                            border = BorderStroke(1.dp, AdminBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(AdminPrimaryBlue.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = AdminPrimaryBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = hosp.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = AdminTextWhite
                                            )
                                            if (hosp.isFeatured) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "FEATURED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = AdminAccOrange,
                                                    modifier = Modifier
                                                        .background(AdminAccOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${hosp.type} • ${hosp.upazila}, ${hosp.district}",
                                            fontSize = 11.sp,
                                            color = AdminTextMuted
                                        )
                                        Text(
                                            text = "Plan: ${hosp.planType} | Contact: ${hosp.phone}",
                                            fontSize = 10.sp,
                                            color = AdminAccGreen
                                        )
                                    }
                                }

                                if (hosp.services.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Services: ${hosp.services}",
                                        fontSize = 11.sp,
                                        color = AdminTextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = AdminBorder)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Toggle Featured
                                    Button(
                                        onClick = {
                                            viewModel.toggleHospitalFeatured(hosp.id, !hosp.isFeatured)
                                            Toast.makeText(context, if (hosp.isFeatured) "Featured mode OFF" else "Featured mode ON", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (hosp.isFeatured) AdminAccOrange else AdminCardBg
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, AdminAccOrange),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (hosp.isFeatured) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = if (hosp.isFeatured) Color.Black else AdminAccOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (hosp.isFeatured) "Featured ON" else "Make Featured",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hosp.isFeatured) Color.Black else AdminAccOrange
                                        )
                                    }

                                    // Switch Plan (Free vs Premium)
                                    Button(
                                        onClick = {
                                            if (hosp.planType == "Free") {
                                                viewModel.updateHospitalPlan(hosp.id, "Premium (Monthly)", "2026-12-31")
                                                Toast.makeText(context, "Upgraded to Premium Monthly", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.updateHospitalPlan(hosp.id, "Free", "Lifetime Free")
                                                Toast.makeText(context, "Set to Free Plan", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (hosp.planType == "Free") "Set Premium" else "Set Free",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Delete Hospital
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteHospital(hosp.id)
                                            Toast.makeText(context, "Hospital deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .background(AdminAccRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AdminAccRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "হাসপাতাল অফারসমূহ (" + offers.size + ")" else "Hospital Offers (${offers.size})",
                            fontWeight = FontWeight.Bold,
                            color = AdminTextWhite,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { showAddOfferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "নতুন অফার যোগ করুন" else "Add Offer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        if (offers.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                                    Text(if (isBn) "কোনো একটিভ অফার নেই।" else "No active offers available.", color = AdminTextMuted, fontSize = 13.sp)
                                }
                            }
                        }

                        items(offers) { offer ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                                border = BorderStroke(1.dp, AdminBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = offer.title, fontWeight = FontWeight.Bold, color = AdminTextWhite, fontSize = 13.sp)
                                        Text(text = offer.hospitalName, fontSize = 11.sp, color = AdminAccOrange)
                                        Text(text = offer.description, fontSize = 10.sp, color = AdminTextMuted)
                                        Text(text = "Discount: ${offer.discountPercent} | Valid: ${offer.validUntil}", fontSize = 10.sp, color = AdminAccGreen)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteHospitalOffer(offer.id)
                                            Toast.makeText(context, "Offer deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.background(AdminAccRed.copy(alpha = 0.2f), CircleShape).size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = AdminAccRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    if (payments.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                                Text(if (isBn) "কোনো পেমেন্ট রেকর্ড পাওয়া যায়নি।" else "No payment records found.", color = AdminTextMuted, fontSize = 13.sp)
                            }
                        }
                    }

                    items(payments) { pay ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                            border = BorderStroke(1.dp, AdminBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = pay.hospitalName, fontWeight = FontWeight.Bold, color = AdminTextWhite, fontSize = 13.sp)
                                    Text(
                                        text = "৳ ${pay.amount.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AdminAccGreen,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(text = "Plan: ${pay.planType} | Gateway: ${pay.paymentMethod}", fontSize = 11.sp, color = AdminTextMuted)
                                Text(text = "TrxID: ${pay.transactionId} | Sender: ${pay.senderPhone}", fontSize = 11.sp, color = AdminPrimaryBlue)
                                Text(text = "Date: ${pay.paymentDate} | Status: ${pay.status}", fontSize = 10.sp, color = AdminTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddOfferDialog) {
        var offerTitleInput by remember { mutableStateOf("") }
        var offerDescInput by remember { mutableStateOf("") }
        var offerDiscountInput by remember { mutableStateOf("20%") }
        var selectedHospName by remember { mutableStateOf(hospitals.firstOrNull()?.name ?: "ABC Hospital") }

        AlertDialog(
            onDismissRequest = { showAddOfferDialog = false },
            title = {
                Text(
                    text = if (isBn) "হাসপাতাল অফার যুক্ত করুন" else "Add Hospital Offer",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = offerTitleInput,
                        onValueChange = { offerTitleInput = it },
                        label = { Text("Offer Title (অফারের শিরোনাম)", color = AdminTextMuted) },
                        placeholder = { Text("e.g. CBC Test 20% Discount", color = AdminTextMuted) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = offerDescInput,
                        onValueChange = { offerDescInput = it },
                        label = { Text("Offer Description (বিবরণ)", color = AdminTextMuted) },
                        placeholder = { Text("e.g. Full blood count test discount for app users", color = AdminTextMuted) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = offerDiscountInput,
                        onValueChange = { offerDiscountInput = it },
                        label = { Text("Discount % or Amount", color = AdminTextMuted) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (offerTitleInput.isNotBlank()) {
                            viewModel.addHospitalOffer(
                                HospitalOffer(
                                    id = "offer_" + System.currentTimeMillis(),
                                    hospitalId = "hosp_custom",
                                    hospitalName = selectedHospName,
                                    title = offerTitleInput.trim(),
                                    description = offerDescInput.trim(),
                                    discountPercent = offerDiscountInput.trim(),
                                    validUntil = "2026-12-31",
                                    bannerUrl = "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=600&auto=format&fit=crop&q=80",
                                    isApproved = true
                                )
                            )
                            Toast.makeText(context, "Offer added successfully!", Toast.LENGTH_SHORT).show()
                            showAddOfferDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen)
                ) {
                    Text(if (isBn) "সংরক্ষণ করুন" else "Save Offer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOfferDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = AdminTextMuted)
                }
            },
            containerColor = AdminCardBg
        )
    }
}

@Composable
fun AdminBookingFeesTab(viewModel: MainViewModel, language: AppLanguage) {
    val isBn = language == AppLanguage.BAN
    val context = LocalContext.current
    val countryFeesList by viewModel.countryBookingFees.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "BD", "INT"
    var showAddCountryDialog by remember { mutableStateOf(false) }

    // Dialog state for new country
    var newCode by remember { mutableStateOf("") }
    var newNameBn by remember { mutableStateOf("") }
    var newNameEn by remember { mutableStateOf("") }
    var newCurrency by remember { mutableStateOf("৳") }
    var newAmbFee by remember { mutableStateOf("50") }
    var newDocFee by remember { mutableStateOf("30") }
    var newHospFee by remember { mutableStateOf("50") }
    var newAccFreeFee by remember { mutableStateOf("50") }
    var newAccAdvFee by remember { mutableStateOf("30") }
    var newBloodReqFee by remember { mutableStateOf("0") }

    val filteredList = remember(countryFeesList, selectedFilter) {
        when (selectedFilter) {
            "BD" -> countryFeesList.filter { it.countryCode == "BD" }
            "INT" -> countryFeesList.filter { it.countryCode != "BD" }
            else -> countryFeesList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(AdminPrimaryBlue.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = AdminPrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "বুকিং ফি সেটিংস ও কান্ট্রি কাস্টমাইজেশন" else "Booking Fees & Country Customization",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminTextWhite
                        )
                        Text(
                            text = if (isBn) "বাংলাদেশের জন্য ও বাহিরের প্রত্যেকটি দেশের জন্য আলাদা ফি নির্ধারণ করুন" else "Customize booking fees for Bangladesh & International countries",
                            fontSize = 11.sp,
                            color = AdminTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AdminBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Quick statistics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (isBn) "মোট কনফিগার করা দেশ:" else "Total Countries:", fontSize = 11.sp, color = AdminTextMuted)
                        Text("${countryFeesList.size} ${if (isBn) "টি দেশ" else "Countries"}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminAccOrange)
                    }
                    val bdFeeObj = countryFeesList.find { it.countryCode == "BD" }
                    Column {
                        Text(if (isBn) "বাংলাদেশ অ্যাম্বুলেন্স ফি:" else "BD Ambulance Fee:", fontSize = 11.sp, color = AdminTextMuted)
                        Text("${bdFeeObj?.currencySymbol ?: "৳"}${bdFeeObj?.ambulanceFee?.toInt() ?: 50}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminAccGreen)
                    }
                    val intlFeeObj = countryFeesList.find { it.countryCode == "OTHER" }
                    Column {
                        Text(if (isBn) "ইন্টারন্যাশনাল ফি:" else "International Fee:", fontSize = 11.sp, color = AdminTextMuted)
                        Text("${intlFeeObj?.currencySymbol ?: "$"}${intlFeeObj?.ambulanceFee ?: 1.0}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryBlue)
                    }
                }
            }
        }

        // Sub Navigation Filter Buttons & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("ALL", if (isBn) "সকল দেশ (${countryFeesList.size})" else "All (${countryFeesList.size})", Icons.Default.Public),
                Triple("BD", if (isBn) "🇧🇩 বাংলাদেশ" else "🇧🇩 Bangladesh", Icons.Default.Flag),
                Triple("INT", if (isBn) "🌐 বাহিরের দেশসমূহ" else "🌐 International", Icons.Default.Language)
            ).forEach { (key, title, icon) ->
                val isSel = selectedFilter == key
                Button(
                    onClick = { selectedFilter = key },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) AdminPrimaryBlue else AdminCardBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSel) AdminPrimaryBlue else AdminBorder),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else AdminTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Add Custom Country Button
        Button(
            onClick = { showAddCountryDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isBn) "নতুন দেশ ও কাস্টম বুকিং ফি যুক্ত করুন" else "Add New Country & Custom Booking Fees",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp
            )
        }

        // Country Fee Editing Cards
        filteredList.forEach { countryFee ->
            CountryFeeItemCard(
                fee = countryFee,
                isBn = isBn,
                onSave = { updated ->
                    viewModel.updateSingleCountryBookingFee(updated)
                    Toast.makeText(context, "${updated.countryNameBn} - বুকিং ফি আপডেট সম্পন্ন!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Add Country Dialog
    if (showAddCountryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCountryDialog = false },
            title = {
                Text(
                    if (isBn) "নতুন দেশ ও ফি কনফিগারেশন যোগ করুন" else "Add Country Booking Fee Configuration",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newCode,
                        onValueChange = { newCode = it.uppercase() },
                        label = { Text("Country Code (e.g. OM, SG, UK)", color = AdminTextMuted) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newNameBn,
                        onValueChange = { newNameBn = it },
                        label = { Text("Country Name (বাংলা)", color = AdminTextMuted) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newNameEn,
                        onValueChange = { newNameEn = it },
                        label = { Text("Country Name (English)", color = AdminTextMuted) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCurrency,
                        onValueChange = { newCurrency = it },
                        label = { Text("Currency Symbol (e.g. ৳, $, OMR, SGD)", color = AdminTextMuted) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newAmbFee,
                            onValueChange = { newAmbFee = it },
                            label = { Text("Ambulance Fee", color = AdminTextMuted, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newDocFee,
                            onValueChange = { newDocFee = it },
                            label = { Text("Doctor Fee", color = AdminTextMuted, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newHospFee,
                            onValueChange = { newHospFee = it },
                            label = { Text("Hospital Fee", color = AdminTextMuted, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newBloodReqFee,
                            onValueChange = { newBloodReqFee = it },
                            label = { Text("Blood Req Fee", color = AdminTextMuted, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCode.isNotBlank() && newNameBn.isNotBlank()) {
                            val item = com.example.data.CountryBookingFee(
                                countryCode = newCode.trim(),
                                countryNameBn = newNameBn.trim(),
                                countryNameEn = newNameEn.trim().ifBlank { newNameBn },
                                currencySymbol = newCurrency.trim(),
                                ambulanceFee = newAmbFee.toDoubleOrNull() ?: 50.0,
                                doctorFee = newDocFee.toDoubleOrNull() ?: 30.0,
                                hospitalFee = newHospFee.toDoubleOrNull() ?: 50.0,
                                hospitalAcceptFeeFree = newAccFreeFee.toDoubleOrNull() ?: 50.0,
                                hospitalAcceptFeeAdvance = newAccAdvFee.toDoubleOrNull() ?: 30.0,
                                bloodRequestFee = newBloodReqFee.toDoubleOrNull() ?: 0.0
                            )
                            viewModel.updateSingleCountryBookingFee(item)
                            Toast.makeText(context, "${item.countryNameBn} যুক্ত করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            showAddCountryDialog = false
                            // Reset
                            newCode = ""
                            newNameBn = ""
                            newNameEn = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen)
                ) {
                    Text(if (isBn) "সংরক্ষণ করুন" else "Save Country Fee", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCountryDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = AdminTextMuted)
                }
            },
            containerColor = AdminCardBg
        )
    }
}

@Composable
fun CountryFeeItemCard(
    fee: com.example.data.CountryBookingFee,
    isBn: Boolean,
    onSave: (com.example.data.CountryBookingFee) -> Unit
) {
    var ambFreeFeeStr by remember(fee) { mutableStateOf(fee.ambulanceFeeFree.toString()) }
    var ambAdvFeeStr by remember(fee) { mutableStateOf(fee.ambulanceFeeAdv.toString()) }
    var hospFreeFeeStr by remember(fee) { mutableStateOf(fee.hospitalFeeFree.toString()) }
    var hospAdvFeeStr by remember(fee) { mutableStateOf(fee.hospitalFeeAdv.toString()) }
    var docCommFreeStr by remember(fee) { mutableStateOf(fee.doctorCommPctFree.toString()) }
    var docCommAdvStr by remember(fee) { mutableStateOf(fee.doctorCommPctAdv.toString()) }
    var ambFeeStr by remember(fee) { mutableStateOf(fee.ambulanceFee.toString()) }
    var docFeeStr by remember(fee) { mutableStateOf(fee.doctorFee.toString()) }
    var hospFeeStr by remember(fee) { mutableStateOf(fee.hospitalFee.toString()) }
    var freeAccFeeStr by remember(fee) { mutableStateOf(fee.hospitalAcceptFeeFree.toString()) }
    var advAccFeeStr by remember(fee) { mutableStateOf(fee.hospitalAcceptFeeAdvance.toString()) }
    var bloodReqFeeStr by remember(fee) { mutableStateOf(fee.bloodRequestFee.toString()) }
    var currSym by remember(fee) { mutableStateOf(fee.currencySymbol) }

    val flagEmoji = when (fee.countryCode) {
        "BD" -> "🇧🇩"
        "IN" -> "🇮🇳"
        "SA" -> "🇸🇦"
        "AE" -> "🇦🇪"
        "QA" -> "🇶🇦"
        "KW" -> "🇰🇼"
        "MY" -> "🇲🇾"
        "US" -> "🇺🇸"
        else -> "🌐"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        border = BorderStroke(1.dp, if (fee.countryCode == "BD") AdminPrimaryBlue else AdminBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(flagEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isBn) fee.countryNameBn else fee.countryNameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AdminTextWhite
                        )
                        Text(
                            text = "Code: ${fee.countryCode} | Symbol: ${currSym}",
                            fontSize = 11.sp,
                            color = AdminTextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (fee.countryCode == "BD") AdminPrimaryBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (fee.countryCode == "BD") (if (isBn) "প্রধান দেশ (BD)" else "Main (BD)") else (if (isBn) "ইন্টারন্যাশনাল" else "International"),
                        color = if (fee.countryCode == "BD") AdminPrimaryBlue else AdminTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = AdminBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                if (isBn) "ফি রেট সেটিংস (${currSym}):" else "Booking Fees Rates (${currSym}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AdminAccOrange
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fee Inputs Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Ambulance Booking Fee (Free vs Adv)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ambFreeFeeStr,
                        onValueChange = { ambFreeFeeStr = it },
                        label = { Text(if (isBn) "অ্যাম্বুলেন্স ফি (ফ্রি) ৳" else "Ambulance Fee (Free)", color = AdminTextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ambAdvFeeStr,
                        onValueChange = { ambAdvFeeStr = it },
                        label = { Text(if (isBn) "অ্যাম্বুলেন্স ফি (এডভান্স) ৳" else "Ambulance Fee (Adv)", color = AdminTextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Hospital Booking Fee (Free vs Adv)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hospFreeFeeStr,
                        onValueChange = { hospFreeFeeStr = it },
                        label = { Text(if (isBn) "হসপিটাল বুকিং ফি (ফ্রি) ৳" else "Hosp Booking Fee (Free)", color = AdminTextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = hospAdvFeeStr,
                        onValueChange = { hospAdvFeeStr = it },
                        label = { Text(if (isBn) "হসপিটাল বুকিং ফি (এডভান্স) ৳" else "Hosp Booking Fee (Adv)", color = AdminTextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Doctor Commission % (Free vs Adv)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = docCommFreeStr,
                        onValueChange = { docCommFreeStr = it },
                        label = { Text(if (isBn) "ডাক্তার ভিজিট চার্জ (ফ্রি %)" else "Doctor Comm % (Free)", color = AdminTextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = docCommAdvStr,
                        onValueChange = { docCommAdvStr = it },
                        label = { Text(if (isBn) "ডাক্তার ভিজিট চার্জ (এডভান্স %)" else "Doctor Comm % (Adv)", color = AdminTextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Hospital Accept Fee & Blood Request Fee
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = freeAccFeeStr,
                        onValueChange = { freeAccFeeStr = it },
                        label = { Text(if (isBn) "হাসপাতাল একসেপ্ট (ফ্রি)" else "Hosp Accept (Free)", color = AdminTextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = advAccFeeStr,
                        onValueChange = { advAccFeeStr = it },
                        label = { Text(if (isBn) "হাসপাতাল একসেপ্ট (এডভান্স)" else "Hosp Accept (Adv)", color = AdminTextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminDarkBg,
                            unfocusedContainerColor = AdminDarkBg
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val updated = fee.copy(
                        ambulanceFeeFree = ambFreeFeeStr.toDoubleOrNull() ?: fee.ambulanceFeeFree,
                        ambulanceFeeAdv = ambAdvFeeStr.toDoubleOrNull() ?: fee.ambulanceFeeAdv,
                        hospitalFeeFree = hospFreeFeeStr.toDoubleOrNull() ?: fee.hospitalFeeFree,
                        hospitalFeeAdv = hospAdvFeeStr.toDoubleOrNull() ?: fee.hospitalFeeAdv,
                        doctorCommPctFree = docCommFreeStr.toDoubleOrNull() ?: fee.doctorCommPctFree,
                        doctorCommPctAdv = docCommAdvStr.toDoubleOrNull() ?: fee.doctorCommPctAdv,
                        ambulanceFee = ambAdvFeeStr.toDoubleOrNull() ?: fee.ambulanceFee,
                        doctorFee = docCommAdvStr.toDoubleOrNull() ?: fee.doctorFee,
                        hospitalFee = hospAdvFeeStr.toDoubleOrNull() ?: fee.hospitalFee,
                        hospitalAcceptFeeFree = freeAccFeeStr.toDoubleOrNull() ?: fee.hospitalAcceptFeeFree,
                        hospitalAcceptFeeAdvance = advAccFeeStr.toDoubleOrNull() ?: fee.hospitalAcceptFeeAdvance,
                        bloodRequestFee = bloodReqFeeStr.toDoubleOrNull() ?: fee.bloodRequestFee,
                        currencySymbol = currSym
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (isBn) "${fee.countryNameBn}-এর ফি সংরক্ষণ করুন" else "Save ${fee.countryNameEn} Fees",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdsBannerTab(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val context = LocalContext.current
    val customAdConfigs by viewModel.customAdConfigs.collectAsState()
    val companySubscriptions by viewModel.companySubscriptions.collectAsState()
    val advertiserCompanies by viewModel.advertiserCompanies.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Order, 1: Payment History, 2: User Details

    var searchQuery by remember { mutableStateOf("") }
    var showCreateAdDialog by remember { mutableStateOf(false) }

    val isBn = language == AppLanguage.BAN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp)
    ) {
        // Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isBn) "📢 অ্যাডস ব্যানার ও অর্ডার ড্যাশবোর্ড" else "📢 Ads Banner & Orders Dashboard",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isBn) "ভিডিও/ফটো বিজ্ঞাপন অর্ডার, পেমেন্ট হিস্ট্রি ও ইউজার কোম্পানি অ্যাকাউন্টসমূহ" else "Manage video/photo ad orders, payment history & advertiser user accounts",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
            }

            Button(
                onClick = { showCreateAdDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) "নতুন অ্যাড অর্ডার" else "New Ad Order",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Sub-Tab Switcher Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AdminCardBg, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Pair(if (isBn) "📦 অর্ডারস (${customAdConfigs.size})" else "📦 Order (${customAdConfigs.size})", 0),
                Pair(if (isBn) "💳 পেমেন্ট হিস্ট্রি (${companySubscriptions.size})" else "💳 Payment History (${companySubscriptions.size})", 1),
                Pair(if (isBn) "👥 ইউজার ডিটেইলস (${advertiserCompanies.size})" else "👥 User Details (${advertiserCompanies.size})", 2)
            )

            tabs.forEach { (label, idx) ->
                val isSelected = activeSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AdminPrimaryBlue else Color.Transparent)
                        .clickable { activeSubTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else AdminTextMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = when (activeSubTab) {
                        0 -> if (isBn) "অর্ডার শিরোনাম, কোম্পানি বা লিংক খুঁজুন..." else "Search ad order, company or URL..."
                        1 -> if (isBn) "ট্রানজেকশন আইডি (TxnID) বা কোম্পানি নাম..." else "Search Txn ID or Company..."
                        else -> if (isBn) "কোম্পানির নাম, ইমেইল বা ফোন নম্বর খুঁজুন..." else "Search company name, email or phone..."
                    },
                    color = AdminTextMuted,
                    fontSize = 12.sp
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AdminTextMuted) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = AdminTextMuted)
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AdminPrimaryBlue,
                unfocusedBorderColor = AdminBorder,
                focusedContainerColor = AdminCardBg,
                unfocusedContainerColor = AdminCardBg,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // TAB CONTENT
        when (activeSubTab) {
            0 -> {
                // ORDER TAB CONTENT
                val filteredAds = customAdConfigs.filter {
                    searchQuery.isEmpty() ||
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.networkName.contains(searchQuery, ignoreCase = true) ||
                            it.companyName.contains(searchQuery, ignoreCase = true)
                }

                if (filteredAds.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = AdminTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBn) "কোনো বিজ্ঞাপন অর্ডার পাওয়া যায়নি" else "No Ad Orders Found",
                                color = AdminTextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 160.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredAds, key = { it.id }) { ad ->
                            AdminAdOrderCard(
                                ad = ad,
                                isBn = isBn,
                                onDelete = {
                                    val updated = customAdConfigs.filter { it.id != ad.id }
                                    viewModel.updateCustomAdConfigsList(context, updated)
                                    Toast.makeText(context, if (isBn) "অর্ডার মুছে ফেলা হয়েছে" else "Ad order deleted", Toast.LENGTH_SHORT).show()
                                },
                                onToggleStatus = {
                                    val newStatus = if (ad.status == "LIVE") "PAUSED" else "LIVE"
                                    val updated = customAdConfigs.map {
                                        if (it.id == ad.id) it.copy(status = newStatus) else it
                                    }
                                    viewModel.updateCustomAdConfigsList(context, updated)
                                }
                            )
                        }
                    }
                }
            }

            1 -> {
                // PAYMENT HISTORY TAB CONTENT
                val filteredPayments = companySubscriptions.filter {
                    searchQuery.isEmpty() ||
                            it.transactionId.contains(searchQuery, ignoreCase = true) ||
                            it.companyName.contains(searchQuery, ignoreCase = true) ||
                            it.paymentMethod.contains(searchQuery, ignoreCase = true)
                }

                if (filteredPayments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = AdminTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBn) "কোনো পেমেন্ট হিস্ট্রি পাওয়া যায়নি" else "No Payment History Found",
                                color = AdminTextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 160.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredPayments, key = { it.id }) { sub ->
                            AdminPaymentHistoryCard(sub = sub, isBn = isBn)
                        }
                    }
                }
            }

            2 -> {
                // USER DETAILS TAB CONTENT
                val filteredCompanies = advertiserCompanies.filter {
                    searchQuery.isEmpty() ||
                            it.companyName.contains(searchQuery, ignoreCase = true) ||
                            it.email.contains(searchQuery, ignoreCase = true) ||
                            it.phone.contains(searchQuery, ignoreCase = true)
                }

                if (filteredCompanies.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = AdminTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBn) "কোনো ইউজার / কোম্পানি অ্যাকাউন্ট নিবন্ধিত নেই" else "No Registered Advertiser Users Found",
                                color = AdminTextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 160.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredCompanies, key = { it.id }) { company ->
                            val userAdsCount = customAdConfigs.count { it.companyId == company.id || it.networkName.equals(company.companyName, ignoreCase = true) }
                            AdminAdvertiserUserCard(
                                company = company,
                                adsCount = userAdsCount,
                                isBn = isBn
                            )
                        }
                    }
                }
            }
        }
    }

    // Create New Ad Order Dialog
    if (showCreateAdDialog) {
        AdminCreateAdOrderDialog(
            isBn = isBn,
            onDismiss = { showCreateAdDialog = false },
            onSubmit = { companyName, title, mediaUrl, isVideo, targetUrl, planType ->
                viewModel.addCompanyBannerSubscription(
                    context = context,
                    companyId = "comp_admin",
                    companyName = companyName,
                    planType = planType,
                    adTitle = title,
                    bannerMediaUrl = mediaUrl,
                    isVideo = isVideo,
                    targetUrl = targetUrl,
                    paymentMethod = "Admin Manual",
                    transactionId = "ADMIN_" + System.currentTimeMillis()
                )
                showCreateAdDialog = false
                Toast.makeText(context, if (isBn) "নতুন অ্যাড সফলভাবে যোগ করা হয়েছে!" else "New Ad order added successfully!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun AdminCreateAdOrderDialog(
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (companyName: String, title: String, mediaUrl: String, isVideo: Boolean, targetUrl: String, planType: String) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var isVideo by remember { mutableStateOf(false) }
    var targetUrl by remember { mutableStateOf("https://") }
    var planType by remember { mutableStateOf("Monthly (৳1800)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "নতুন অ্যাড ব্যানার যোগ করুন" else "Create New Ad Banner",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text(if (isBn) "কোম্পানি / স্পন্সর নাম" else "Company / Sponsor Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder
                    )
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBn) "বিজ্ঞাপনের শিরোনাম (Ad Title)" else "Ad Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder
                    )
                )

                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text(if (isBn) "ব্যানার ইমেজ / ভিডিও URL" else "Banner Image / Video URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isVideo,
                        onCheckedChange = { isVideo = it },
                        colors = CheckboxDefaults.colors(checkedColor = AdminPrimaryBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "ভিডিও বিজ্ঞাপন (Video Ad)" else "Is Video Ad",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                OutlinedTextField(
                    value = targetUrl,
                    onValueChange = { targetUrl = it },
                    label = { Text(if (isBn) "টার্গেট লিংক (Click Destination URL)" else "Target URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder
                    )
                )

                Text(
                    text = if (isBn) "প্ল্যান নির্বাচন করুন:" else "Select Ad Plan:",
                    fontSize = 13.sp,
                    color = AdminTextMuted,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Weekly (৳500)", "Monthly (৳1800)").forEach { plan ->
                        val isSel = planType == plan
                        FilterChip(
                            selected = isSel,
                            onClick = { planType = plan },
                            label = { Text(plan, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AdminPrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = AdminCardBg,
                                labelColor = AdminTextMuted
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (companyName.isNotBlank() && title.isNotBlank()) {
                        onSubmit(companyName, title, mediaUrl, isVideo, targetUrl, planType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue)
            ) {
                Text(if (isBn) "পাবলিশ করুন" else "Publish Ad")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel", color = AdminTextMuted)
            }
        },
        containerColor = AdminDarkBg
    )
}

@Composable
fun AdminAdOrderCard(
    ad: CustomAdConfig,
    isBn: Boolean,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        border = BorderStroke(1.dp, AdminBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (ad.isVideo) Color(0xFFFFF3E0) else Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (ad.isVideo) "🎬 VIDEO AD" else "📷 PHOTO BANNER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ad.isVideo) Color(0xFFE65100) else Color(0xFF1565C0),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (ad.companyName.isNotBlank()) ad.companyName else ad.networkName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AdminPrimaryBlue
                    )
                }

                Surface(
                    color = if (ad.status == "LIVE") Color(0xFF1B5E20) else Color(0xFF424242),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (ad.status == "LIVE") "🟢 LIVE" else "⏸️ PAUSED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AdminDarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (ad.bannerUrl.isNotBlank() || ad.videoUrl.isNotBlank()) {
                        AsyncImage(
                            model = if (ad.bannerUrl.isNotBlank()) ad.bannerUrl else ad.videoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = if (ad.isVideo) Icons.Default.OndemandVideo else Icons.Default.Image,
                            contentDescription = null,
                            tint = AdminTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ad.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Plan: ${ad.planType} • Expiry: ${ad.expiryDate}",
                        fontSize = 11.sp,
                        color = AdminTextMuted
                    )
                    Text(
                        text = "Target: ${ad.targetUrl}",
                        fontSize = 10.sp,
                        color = AdminPrimaryBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row (Views & Clicks)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AdminDarkBg, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isBn) "👁️ ভিউ সংখ্যা" else "👁️ Views", fontSize = 9.sp, color = AdminTextMuted)
                    Text("${ad.viewsCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isBn) "🖱️ ক্লিক সংখ্যা" else "🖱️ Clicks", fontSize = 9.sp, color = AdminTextMuted)
                    Text("${ad.clicksCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminAccGreen)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val ctrStr = if (ad.viewsCount > 0) String.format("%.1f", (ad.clicksCount.toDouble() / ad.viewsCount) * 100) else "0.0"
                    Text("🎯 CTR %", fontSize = 9.sp, color = AdminTextMuted)
                    Text("$ctrStr%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminAccOrange)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, AdminBorder),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (ad.status == "LIVE") (if (isBn) "⏸️ পজ করুন" else "⏸️ Pause") else (if (isBn) "🟢 লাইভ করুন" else "🟢 Make Live"),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminAccRed),
                    border = BorderStroke(1.dp, AdminAccRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBn) "ডিলিট" else "Delete", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AdminPaymentHistoryCard(
    sub: com.example.data.CompanyAdSubscription,
    isBn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        border = BorderStroke(1.dp, AdminBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sub.companyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Txn ID: ${sub.transactionId.ifBlank { "N/A" }}",
                        fontSize = 11.sp,
                        color = AdminPrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = Color(0xFF1B5E20),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "✓ VERIFIED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AdminDarkBg, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(if (isBn) "পেমেন্ট মেথড:" else "Gateway:", fontSize = 10.sp, color = AdminTextMuted)
                    Text(sub.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminAccOrange)
                }

                Column {
                    Text(if (isBn) "প্ল্যান প্যাকেজ:" else "Plan:", fontSize = 10.sp, color = AdminTextMuted)
                    Text(sub.planType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(if (isBn) "পরিশোধিত টাকা:" else "Amount Paid:", fontSize = 10.sp, color = AdminTextMuted)
                    Text("৳${sub.pricePaid.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminAccGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ad Title: ${sub.adTitle}",
                    fontSize = 11.sp,
                    color = AdminTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Date: ${sub.startDate}",
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
            }
        }
    }
}

@Composable
fun AdminAdvertiserUserCard(
    company: com.example.data.AdvertiserCompany,
    adsCount: Int,
    isBn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        border = BorderStroke(1.dp, AdminBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AdminPrimaryBlue.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = company.companyName.take(1).uppercase(),
                            color = AdminPrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = company.companyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Category: ${company.businessType}",
                            fontSize = 10.sp,
                            color = AdminTextMuted
                        )
                    }
                }

                Surface(
                    color = AdminPrimaryBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBn) "$adsCount টি বিজ্ঞাপন" else "$adsCount Active Ads",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminPrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AdminDarkBg, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "ইমেইল:" else "Email:", fontSize = 11.sp, color = AdminTextMuted)
                    Text(company.email.ifBlank { "N/A" }, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "ফোন নম্বর:" else "Phone:", fontSize = 11.sp, color = AdminTextMuted)
                    Text(company.phone.ifBlank { "N/A" }, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "অ্যাকাউন্ট পাসওয়ার্ড:" else "Password:", fontSize = 11.sp, color = AdminTextMuted)
                    Text(company.password.ifBlank { "••••••" }, fontSize = 11.sp, color = AdminAccOrange, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "ওয়েবসাইট লিংক:" else "Website:", fontSize = 11.sp, color = AdminTextMuted)
                    Text(company.websiteUrl.ifBlank { "N/A" }, fontSize = 11.sp, color = AdminPrimaryBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isBn) "রেজিস্ট্রেশন তারিখ:" else "Joined Date:", fontSize = 11.sp, color = AdminTextMuted)
                    Text(company.registrationDate.ifBlank { "Recent" }, fontSize = 11.sp, color = AdminTextMuted)
                }
            }
        }
    }
}




@Composable
fun AdminPlansAndPricingTab(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BAN
    val plans by viewModel.subscriptionPlans.collectAsState()

    var showAddPlanDialog by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<V9SubscriptionPlan?>(null) }

    var planId by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var durationDaysStr by remember { mutableStateOf("") }
    var descEn by remember { mutableStateOf("") }
    var descBn by remember { mutableStateOf("") }
    var targetRole by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(AdminPrimaryBlue.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Sell, contentDescription = null, tint = AdminPrimaryBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBn) "🏷️ অল প্ল্যান ও মূল্য তালিকা ব্যবস্থাপনা" else "🏷️ All Plans & Pricing Management",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isBn) "ডাক্তার, হাসপাতাল, অ্যাম্বুলেন্স, ডোনার ও বিজ্ঞাপন প্যাকসমূহের রেট পরিবর্তন করুন" else "Manage prices, durations & descriptions for Doctor, Hospital, Ambulance & Ad plans",
                                fontSize = 11.sp,
                                color = AdminTextMuted
                            )
                        }
                    }

                    Button(
                        onClick = {
                            editingPlan = null
                            planId = "plan_" + System.currentTimeMillis()
                            nameEn = ""
                            nameBn = ""
                            priceStr = "499"
                            durationDaysStr = "-1"
                            descEn = ""
                            descBn = ""
                            targetRole = "All"
                            showAddPlanDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "নতুন প্ল্যান যোগ করুন" else "Add New Plan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Plans Grid / List
        plans.forEach { plan ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                border = BorderStroke(1.dp, AdminBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) plan.nameBn else plan.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Category: ${plan.targetRole} | ID: ${plan.id}",
                                fontSize = 11.sp,
                                color = AdminTextMuted
                            )
                        }

                        Surface(
                            color = AdminAccOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AdminAccOrange.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "৳${plan.price.toInt()} / ${if (plan.durationDays <= 0 || plan.durationDays == -1) (if (isBn) "আজীবন (Lifetime)" else "Lifetime") else "${plan.durationDays} days"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AdminAccOrange,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = AdminBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBn) plan.descriptionBn else plan.descriptionEn,
                        fontSize = 12.sp,
                        color = AdminTextMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                editingPlan = plan
                                planId = plan.id
                                nameEn = plan.nameEn
                                nameBn = plan.nameBn
                                priceStr = plan.price.toInt().toString()
                                durationDaysStr = plan.durationDays.toString()
                                descEn = plan.descriptionEn
                                descBn = plan.descriptionBn
                                targetRole = plan.targetRole
                                showAddPlanDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminPrimaryBlue),
                            border = BorderStroke(1.dp, AdminPrimaryBlue.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "এডিট করুন" else "Edit Plan", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                val updated = plans.filter { it.id != plan.id }
                                viewModel.updateSubscriptionPlansList(context, updated)
                                Toast.makeText(context, if (isBn) "প্ল্যান মুছে ফেলা হয়েছে" else "Plan deleted", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminAccRed),
                            border = BorderStroke(1.dp, AdminAccRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "ডিলিট" else "Delete", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Plan Dialog
    if (showAddPlanDialog) {
        AlertDialog(
            onDismissRequest = { showAddPlanDialog = false },
            title = {
                Text(
                    text = if (editingPlan == null) (if (isBn) "নতুন প্ল্যান প্যাক যোগ করুন" else "Add New Subscription Plan") else (if (isBn) "প্ল্যান পরিবর্তন করুন" else "Edit Subscription Plan"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            containerColor = AdminCardBg,
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameBn,
                        onValueChange = { nameBn = it },
                        label = { Text(if (isBn) "প্ল্যানের নাম (বাংলা) *" else "Plan Name (Bangla) *") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(if (isBn) "প্ল্যানের নাম (English) *" else "Plan Name (English) *") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text(if (isBn) "মূল্য (৳)" else "Price (BDT)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = durationDaysStr,
                            onValueChange = { durationDaysStr = it },
                            label = { Text(if (isBn) "মেয়াদ (দিন, -১ = আজীবন)" else "Days (-1 = Lifetime)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = targetRole,
                        onValueChange = { targetRole = it },
                        label = { Text(if (isBn) "ক্যাটাগরি / টার্গেট রুল (Doctor, Hospital, Ambulance, Donor, Advertiser)" else "Target Category") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = descBn,
                        onValueChange = { descBn = it },
                        label = { Text(if (isBn) "সুবিধাসমূহ (বাংলা)" else "Features (Bangla)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = descEn,
                        onValueChange = { descEn = it },
                        label = { Text(if (isBn) "সুবিধাসমূহ (English)" else "Features (English)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameBn.isNotBlank()) {
                            val newPlan = V9SubscriptionPlan(
                                id = planId,
                                nameEn = nameEn.ifBlank { nameBn },
                                nameBn = nameBn,
                                price = priceStr.toDoubleOrNull() ?: 499.0,
                                durationDays = durationDaysStr.toIntOrNull() ?: -1,
                                descriptionEn = descEn.ifBlank { descBn },
                                descriptionBn = descBn,
                                targetRole = targetRole.ifBlank { "All" }
                            )

                            val existingList = viewModel.subscriptionPlans.value
                            val updatedList = if (existingList.any { it.id == newPlan.id }) {
                                existingList.map { if (it.id == newPlan.id) newPlan else it }
                            } else {
                                existingList + newPlan
                            }

                            viewModel.updateSubscriptionPlansList(context, updatedList)
                            showAddPlanDialog = false
                            Toast.makeText(context, if (isBn) "প্ল্যান তথ্য সফলভাবে সংরক্ষিত হয়েছে!" else "Plan updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen)
                ) {
                    Text(if (isBn) "সংরক্ষণ করুন" else "Save Plan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlanDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = AdminTextMuted)
                }
            }
        )
    }
}

@Composable
fun AdminPaymentPortalTab(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BAN

    val currentBkash by viewModel.bkashNumber.collectAsState()
    val currentNagad by viewModel.nagadNumber.collectAsState()
    val currentRocket by viewModel.rocketNumber.collectAsState()
    val currentUpay by viewModel.upayNumber.collectAsState()
    val currentWise by viewModel.wiseAccount.collectAsState()
    val currentPayoneer by viewModel.payoneerAccount.collectAsState()
    val currentUsdt by viewModel.usdtWalletAddress.collectAsState()
    val currentGooglePlay by viewModel.googlePlayMerchant.collectAsState()
    val currentInstructions by viewModel.paymentInstructionsText.collectAsState()

    val allPayments by viewModel.allPayments.collectAsState()

    var editBkash by remember(currentBkash) { mutableStateOf(currentBkash) }
    var editNagad by remember(currentNagad) { mutableStateOf(currentNagad) }
    var editRocket by remember(currentRocket) { mutableStateOf(currentRocket) }
    var editUpay by remember(currentUpay) { mutableStateOf(currentUpay) }
    var editWise by remember(currentWise) { mutableStateOf(currentWise) }
    var editPayoneer by remember(currentPayoneer) { mutableStateOf(currentPayoneer) }
    var editUsdt by remember(currentUsdt) { mutableStateOf(currentUsdt) }
    var editGooglePlay by remember(currentGooglePlay) { mutableStateOf(currentGooglePlay) }
    var editInstructions by remember(currentInstructions) { mutableStateOf(currentInstructions) }

    var activeSubTab by remember { mutableStateOf(0) } // 0: Gateways & Instructions Config, 1: All Payment History
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Tab Switcher Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AdminCardBg, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Pair(if (isBn) "💳 পেমেন্ট গেটওয়ে & নিয়মাবলী" else "💳 Gateways & Instructions", 0),
                Pair(if (isBn) "📜 অল পেমেন্ট হিস্টোরি (${allPayments.size})" else "📜 All Payment History (${allPayments.size})", 1)
            )

            tabs.forEach { (label, idx) ->
                val isSelected = activeSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AdminPrimaryBlue else Color.Transparent)
                        .clickable { activeSubTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else AdminTextMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                // GATEWAYS & INSTRUCTIONS CONFIG
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    border = BorderStroke(1.dp, AdminBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AdminPrimaryBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "পেমেন্ট পদ্ধতি ও কাস্টম নির্দেশাবলী সেটিংস" else "Payment Methods & Custom Instructions Config",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AdminTextWhite
                            )
                        }

                        Text(
                            text = if (isBn) "এখানে দেওয়া পেমেন্ট নম্বরসমূহ এবং অফিশিয়াল ম্যানুয়াল পেমেন্ট করার বিস্তারিত নিয়ম নিচে লিখে দিন:" else "Configure payment numbers & step-by-step payment instructions for users below:",
                            fontSize = 11.sp,
                            color = AdminTextMuted
                        )

                        OutlinedTextField(
                            value = editBkash,
                            onValueChange = { editBkash = it },
                            label = { Text(if (isBn) "বিকাশ মার্চেন্ট/পার্সোনাল নম্বর (bKash)" else "bKash Merchant/Personal Number", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editNagad,
                            onValueChange = { editNagad = it },
                            label = { Text(if (isBn) "নগদ মার্চেন্ট/পার্সোনাল নম্বর (Nagad)" else "Nagad Merchant/Personal Number", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editRocket,
                            onValueChange = { editRocket = it },
                            label = { Text(if (isBn) "রকেট নম্বর (Rocket)" else "Rocket Number", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editUpay,
                            onValueChange = { editUpay = it },
                            label = { Text(if (isBn) "উপায় অ্যাকাউন্ট নম্বর (Upay)" else "Upay Account Number", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editWise,
                            onValueChange = { editWise = it },
                            label = { Text(if (isBn) "ওয়াইজ ইমেইল/ট্যাগ (Wise)" else "Wise Email/Tag", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editPayoneer,
                            onValueChange = { editPayoneer = it },
                            label = { Text(if (isBn) "পায়োনিয়ার আইডি/ইমেইল (Payoneer)" else "Payoneer ID/Email", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editUsdt,
                            onValueChange = { editUsdt = it },
                            label = { Text(if (isBn) "ইউএসডিটি ওয়ালেট এড্রেস (USDT TRC20/BEP20)" else "USDT Wallet Address", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editGooglePlay,
                            onValueChange = { editGooglePlay = it },
                            label = { Text(if (isBn) "গুগল প্লে মার্চেন্ট / ব্যাংক আইডি" else "Google Play / Bank Merchant ID", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        HorizontalDivider(color = AdminBorder, modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = if (isBn) "📝 কিভাবে কিভাবে পেমেন্ট করবে সেটির বিস্তারিত গাইডলাইন লিখুন:" else "📝 Write step-by-step payment instructions for users:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminAccOrange
                        )

                        OutlinedTextField(
                            value = editInstructions,
                            onValueChange = { editInstructions = it },
                            label = { Text(if (isBn) "পেমেন্ট করার নিয়মাবলী ও ইন্সট্রাকশন..." else "Detailed Payment Instructions...", color = AdminTextMuted) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AdminPrimaryBlue,
                                unfocusedBorderColor = AdminBorder,
                                focusedContainerColor = AdminDarkBg,
                                unfocusedContainerColor = AdminDarkBg
                            ),
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.updatePaymentGatewayAccounts(
                                    context = context,
                                    bkash = editBkash,
                                    nagad = editNagad,
                                    rocket = editRocket,
                                    upay = editUpay,
                                    wise = editWise,
                                    payoneer = editPayoneer,
                                    usdt = editUsdt,
                                    googlePlay = editGooglePlay,
                                    instructions = editInstructions
                                )
                                Toast.makeText(context, if (isBn) "পেমেন্ট সেটিংস ও গাইডলাইন সংরক্ষিত হয়েছে!" else "Payment settings & guidelines saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "পেমেন্ট সেটিংস সংরক্ষণ করুন" else "Save Payment Gateways & Guidelines",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            1 -> {
                // ALL PAYMENT HISTORY LIST
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isBn) "ট্রানজেকশন আইডি, ফোন নম্বর, ইউজার নাম বা পেমেন্ট মেথড খুঁজুন..." else "Search Txn ID, Phone, User or Gateway...", color = AdminTextMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AdminTextMuted) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null, tint = AdminTextMuted) } }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPrimaryBlue,
                            unfocusedBorderColor = AdminBorder,
                            focusedContainerColor = AdminCardBg,
                            unfocusedContainerColor = AdminCardBg,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    val filteredPayments = remember(allPayments, searchQuery) {
                        allPayments.filter {
                            searchQuery.isEmpty() ||
                                    it.transactionId.contains(searchQuery, ignoreCase = true) ||
                                    it.userNameOrCompany.contains(searchQuery, ignoreCase = true) ||
                                    it.userPhoneOrEmail.contains(searchQuery, ignoreCase = true) ||
                                    it.paymentGateway.contains(searchQuery, ignoreCase = true) ||
                                    it.paymentType.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredPayments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBn) "কোনো পেমেন্ট হিস্টোরি পাওয়া যায়নি" else "No payment history found",
                                color = AdminTextMuted,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredPayments, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                                    border = BorderStroke(1.dp, AdminBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = item.userNameOrCompany,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${item.paymentType} • ${item.planOrItemTitle}",
                                                    fontSize = 11.sp,
                                                    color = AdminTextMuted
                                                )
                                            }

                                            Surface(
                                                color = when (item.status) {
                                                    "Approved" -> Color(0xFF1B5E20)
                                                    "Rejected" -> Color(0xFFB71C1C)
                                                    else -> Color(0xFFE65100)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = item.status.uppercase(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(AdminDarkBg, RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(if (isBn) "মেথড:" else "Gateway:", fontSize = 10.sp, color = AdminTextMuted)
                                                Text(item.paymentGateway, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminAccOrange)
                                            }

                                            Column {
                                                Text(if (isBn) "ট্রানজেকশন আইডি:" else "Txn ID:", fontSize = 10.sp, color = AdminTextMuted)
                                                Text(item.transactionId.ifBlank { "N/A" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryBlue)
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(if (isBn) "পরিমাণ:" else "Amount:", fontSize = 10.sp, color = AdminTextMuted)
                                                Text("৳${item.amount.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminAccGreen)
                                            }
                                        }

                                        if (item.senderInfo.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Sender Mobile/Info: ${item.senderInfo}",
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }

                                        if (item.screenshotUrl.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Screenshot Link: ${item.screenshotUrl}",
                                                fontSize = 10.sp,
                                                color = AdminPrimaryBlue,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Date: ${item.date}", fontSize = 10.sp, color = AdminTextMuted)

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (item.status != "Approved") {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.updatePaymentStatus(context, item.id, "Approved")
                                                            Toast.makeText(context, if (isBn) "পেমেন্ট অনুমোদন করা হয়েছে" else "Payment approved", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminAccGreen),
                                                        border = BorderStroke(1.dp, AdminAccGreen.copy(alpha = 0.5f)),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(if (isBn) "🟢 অনুমোদন" else "Approve", fontSize = 10.sp)
                                                    }
                                                }

                                                if (item.status != "Rejected") {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.updatePaymentStatus(context, item.id, "Rejected")
                                                            Toast.makeText(context, if (isBn) "পেমেন্ট বাতিল করা হয়েছে" else "Payment rejected", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminAccRed),
                                                        border = BorderStroke(1.dp, AdminAccRed.copy(alpha = 0.5f)),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(if (isBn) "🔴 বাতিল" else "Reject", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
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
fun AdminTeamsTab(viewModel: MainViewModel? = null) {
    val context = LocalContext.current
    val teams by (viewModel?.donorTeams?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val language by (viewModel?.language?.collectAsState() ?: remember { mutableStateOf(AppLanguage.BAN) })
    val isBn = language == AppLanguage.BAN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isBn) "🚩 ডোনার ও ভলান্টিয়ার টিমসমূহ" else "🚩 Donor & Volunteer Teams",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (teams.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBn) "কোনো টিম নিবন্ধিত হয়নি" else "No teams registered yet",
                    color = AdminTextMuted
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(teams, key = { it.id }) { team ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                        border = BorderStroke(1.dp, AdminBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(team.teamName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    Text("${team.district} • ${team.members.size} ${if (isBn) "জন সদস্য" else "Members"}", fontSize = 12.sp, color = AdminTextMuted)
                                }
                                IconButton(
                                    onClick = {
                                        viewModel?.deleteDonorTeam(team.id)
                                        Toast.makeText(context, if (isBn) "টিম মুছে ফেলা হয়েছে" else "Team deleted", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.background(Color(0xFFD32F2F), RoundedCornerShape(8.dp)).size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
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
fun AdminSubscriptionsTab(viewModel: MainViewModel? = null, language: AppLanguage = AppLanguage.BAN) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BAN
    val allPayments by (viewModel?.allPayments?.collectAsState() ?: remember { mutableStateOf(emptyList()) })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isBn) "👑 অ্যাডভান্স ও সাবস্ক্রিপশন ব্যবস্থাপনা" else "👑 Advance & Subscriptions Management",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (allPayments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isBn) "কোনো সাবস্ক্রিপশন বা পেমেন্ট রিকোয়েস্ট নেই" else "No subscription or payment requests found",
                    color = AdminTextMuted
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(allPayments, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                        border = BorderStroke(1.dp, AdminBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.userNameOrCompany, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                    Text("${item.paymentType} • ৳${item.amount.toInt()}", fontSize = 12.sp, color = AdminAccOrange)
                                }
                                Surface(
                                    color = when (item.status) {
                                        "Approved" -> Color(0xFF1B5E20)
                                        "Rejected" -> Color(0xFFB71C1C)
                                        else -> Color(0xFFE65100)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.status.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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




