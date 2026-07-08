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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
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
    termsEn: String,
    termsBn: String,
    refundEn: String,
    refundBn: String,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var draftPrivacyEn by remember { mutableStateOf(privacyEn) }
    var draftPrivacyBn by remember { mutableStateOf(privacyBn) }
    var draftTermsEn by remember { mutableStateOf(termsEn) }
    var draftTermsBn by remember { mutableStateOf(termsBn) }
    var draftRefundEn by remember { mutableStateOf(refundEn) }
    var draftRefundBn by remember { mutableStateOf(refundBn) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Edit Core App Policy Pages",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AdminTextWhite,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Privacy Policy
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("1. Privacy Policy (প্রাইভেসি পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                Spacer(modifier = Modifier.height(8.dp))
                
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

        // Terms & Conditions
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("2. Terms & Conditions (টার্মস এন্ড কন্ডিশন)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                Spacer(modifier = Modifier.height(8.dp))
                
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

        // Refund Policy
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("3. Refund Policy (রিফান্ড পলিসি)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminAccRed)
                Spacer(modifier = Modifier.height(8.dp))
                
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

        Button(
            onClick = {
                onSave(draftPrivacyEn, draftPrivacyBn, draftTermsEn, draftTermsBn, draftRefundEn, draftRefundBn)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AdminAccRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save All Policy Pages", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

        // Cloud Web API Configuration Card (Admin Only)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp).testTag("api_sync_card_admin"),
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
                        contentDescription = "Sync",
                        tint = if (isRemoteConnected) AdminAccGreen else AdminAccRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Cloud Web API Sync (Retrofit)" else "ক্লাউড এপিআই সিঙ্ক (Retrofit সেটিংস)",
                        fontWeight = FontWeight.Bold,
                        color = AdminAccOrange,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.ENG) {
                        "Specify a Base REST API URL to perform live uploads and sync blood requests."
                    } else {
                        "আপনার রক্তদাতা ও রক্ত অনুরোধের তথ্য রিমোট ডাটাবেজে লাইভ সিঙ্ক করার জন্য Base REST API URL প্রদান করুন।"
                    },
                    fontSize = 11.sp,
                    color = AdminTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editApiUrl,
                    onValueChange = { editApiUrl = it },
                    label = { Text(if (language == AppLanguage.ENG) "Base API URL (HTTP/HTTPS)" else "এপিআই বেস ইউআরএল", color = AdminTextMuted, fontSize = 12.sp) },
                    placeholder = { Text("https://myapi.example.com/api/", color = AdminTextMuted.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().testTag("api_url_input_admin"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder,
                        focusedContainerColor = AdminDarkBg,
                        unfocusedContainerColor = AdminDarkBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = editApiKey,
                    onValueChange = { editApiKey = it },
                    label = { Text(if (language == AppLanguage.ENG) "API Key / Auth Header (Optional)" else "এপিআই কী (ঐচ্ছিক)", color = AdminTextMuted, fontSize = 12.sp) },
                    placeholder = { Text("Bearer ... / Supabase Anon Key", color = AdminTextMuted.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().testTag("api_key_input_admin"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = AdminTextWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimaryBlue,
                        unfocusedBorderColor = AdminBorder,
                        focusedContainerColor = AdminDarkBg,
                        unfocusedContainerColor = AdminDarkBg
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val success = viewModel.updateRemoteApiUrl(context, editApiUrl, editApiKey)
                            if (success) {
                                Toast.makeText(
                                    context,
                                    if (language == AppLanguage.ENG) "API settings updated successfully!" else "এপিআই সেটিংস আপডেট সম্পন্ন হয়েছে!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    if (language == AppLanguage.ENG) "Connection/URL formatting error!" else "ভুল ইউআরএল ফরম্যাট! অবশ্যই সঠিক হতে হবে।",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(42.dp).testTag("save_api_url_btn_admin"),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRemoteConnected) AdminAccGreen else AdminPrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = "link", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == AppLanguage.ENG) "Connect API" else "সংযুক্ত করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (isRemoteConnected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.triggerRemoteSync() },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("sync_now_btn_admin"),
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Filled.Sync, contentDescription = "sync", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == AppLanguage.ENG) "Sync Now" else "সিঙ্ক করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (syncError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "Sync Error: ${syncError}" else "ত্রুটি: ${syncError}",
                        color = AdminAccRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (isRemoteConnected && !isSyncing) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.ENG) "🟢 Connected. Cloud syncing is active and ready." else "🟢 রিমোট এপিআই সিঙ্ক সক্রিয় এবং ডাটাবেস প্রস্তুত।",
                        color = AdminAccGreen,
                        fontSize = 11.sp
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

        var draftCustomAdsEnabled by remember { mutableStateOf(customAdsEnabledState) }
        var currentAdConfigsList by remember(customAdConfigsState) { mutableStateOf(customAdConfigsState) }

        // State variables for adding a new ad config
        var newAdNetworkName by remember { mutableStateOf("") }
        var newAdTitle by remember { mutableStateOf("") }
        var newAdWeight by remember { mutableStateOf("1") }
        var newAdTargetUrl by remember { mutableStateOf("") }
        var newAdTargetCountries by remember { mutableStateOf("All") }
        
        // Media upload mode state: "url" or "gallery"
        var mediaSourceType by remember { mutableStateOf("url") } // "url" or "gallery"
        var isVideoType by remember { mutableStateOf(false) } // true for video, false for image
        var customMediaUrlInput by remember { mutableStateOf("") }
        var selectedGalleryUri by remember { mutableStateOf<Uri?>(null) }

        // Setup image/video picker launcher from gallery
        val adMediaPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedGalleryUri = uri
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
                                IconButton(
                                    onClick = {
                                        currentAdConfigsList = currentAdConfigsList.filter { it.id != ad.id }
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

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = AdminBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 2: ADD NEW AD FORM
                    Text(
                        text = if (language == AppLanguage.ENG) "Add New Network / Offer" else "নতুন নেটওয়ার্ক / অফার যোগ করুন",
                        fontWeight = FontWeight.Bold,
                        color = AdminTextWhite,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

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
                            Text(if (language == AppLanguage.ENG) "Target Countries" else "টার্গেট দেশসমূহ", fontSize = 11.sp, color = AdminTextMuted)
                            OutlinedTextField(
                                value = newAdTargetCountries,
                                onValueChange = { newAdTargetCountries = it },
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AdminTextWhite),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminPrimaryBlue, unfocusedBorderColor = AdminBorder, focusedContainerColor = AdminDarkBg, unfocusedContainerColor = AdminDarkBg)
                            )
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

                    // BUTTON TO ADD THIS NETWORK TO LIST (+)
                    Button(
                        onClick = {
                            if (newAdNetworkName.isBlank() || newAdTitle.isBlank() || newAdTargetUrl.isBlank()) {
                                Toast.makeText(context, "Please fill in Network Name, Title and Target Link!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val finalMediaUrl = if (mediaSourceType == "url") {
                                customMediaUrlInput
                            } else {
                                selectedGalleryUri?.toString() ?: ""
                            }

                            if (finalMediaUrl.isBlank()) {
                                Toast.makeText(context, "Please provide a media URL or select from gallery!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val weightVal = newAdWeight.toIntOrNull() ?: 1

                            val newAd = CustomAdConfig(
                                id = java.util.UUID.randomUUID().toString(),
                                networkName = newAdNetworkName,
                                title = newAdTitle,
                                bannerUrl = if (isVideoType) "" else finalMediaUrl,
                                isVideo = isVideoType,
                                videoUrl = if (isVideoType) finalMediaUrl else "",
                                targetUrl = newAdTargetUrl,
                                targetCountries = newAdTargetCountries,
                                weight = weightVal
                            )

                            currentAdConfigsList = currentAdConfigsList + newAd

                            // Clear add form fields
                            newAdNetworkName = ""
                            newAdTitle = ""
                            newAdWeight = "1"
                            newAdTargetUrl = ""
                            customMediaUrlInput = ""
                            selectedGalleryUri = null
                            Toast.makeText(context, "Ad added to list! Save settings below to apply.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.ENG) "Add Ad Network to List (+)" else "বিজ্ঞাপন নেটওয়ার্ক রোটেশন তালিকায় যোগ করুন (+)",
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
    val messagesList by viewModel.messages.collectAsState()
    
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        if (supportChats.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (language == AppLanguage.ENG) "No live support messages yet." else "এখনও কোনো লাইভ সাপোর্ট মেসেজ নেই।",
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
