package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Unified Payment Method Selector
 * Designed to mirror the exact UI from user screenshot:
 * - Rounded white cards on a light lavender container background
 * - Selected card with bold green border + checkmark badge
 * - "✨ New!" purple floating badge on G Pay
 * - Authentic brand representations for bKash, G Pay, Nagad, Cards/Others, Rocket, Wise, USDT, Wallet.
 */
@Composable
fun UnifiedPaymentMethodSelector(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit,
    availableMethods: List<String> = listOf("bKash", "Google Pay", "Nagad", "Others"),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF0F3FA), // Light bluish-gray container
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Group top options (bKash, G Pay, Nagad or first 3) vs bottom wide options (Others / Cards)
            val topRowMethods = availableMethods.filter { it != "Others" && it != "Card / Google Pay" && it != "Cards / Others" && it != "Card" }
            val bottomRowMethods = availableMethods.filter { it == "Others" || it == "Card / Google Pay" || it == "Cards / Others" || it == "Card" }

            // Top Row Options
            if (topRowMethods.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topRowMethods.forEach { method ->
                        val isSelected = selectedMethod.contains(method, ignoreCase = true) || 
                                       (method == "Google Pay" && selectedMethod.contains("GPay", ignoreCase = true)) ||
                                       (method == "bKash" && selectedMethod == "বিকাশ") ||
                                       (method == "Nagad" && selectedMethod == "নগদ")
                        
                        PaymentOptionCard(
                            method = method,
                            isSelected = isSelected,
                            onClick = { onMethodSelected(method) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bottom Wide Option (Others / Cards)
            if (bottomRowMethods.isNotEmpty()) {
                bottomRowMethods.forEach { method ->
                    val isSelected = selectedMethod.contains("Others", ignoreCase = true) ||
                                   selectedMethod.contains("Card", ignoreCase = true) ||
                                   selectedMethod == method

                    PaymentOptionCard(
                        method = method,
                        isSelected = isSelected,
                        onClick = { onMethodSelected(method) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionCard(
    method: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF10B981) else Color.Transparent,
        animationSpec = tween(200), label = "borderColor"
    )

    Box(
        modifier = modifier
            .padding(vertical = 2.dp)
    ) {
        // Main Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFF10B981) else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onClick() },
            color = Color.White,
            shadowElevation = if (isSelected) 3.dp else 1.dp,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Brand Content Representation
                when (method) {
                    "bKash", "বিকাশ" -> BKashLogoContent()
                    "Google Pay", "G Pay", "GPay" -> GPayLogoContent()
                    "Nagad", "নগদ" -> NagadLogoContent()
                    "Others", "Card / Google Pay", "Cards / Others", "Card" -> CardsOthersLogoContent()
                    "Rocket", "রকেট" -> RocketLogoContent()
                    "Wise" -> WiseLogoContent()
                    "USDT" -> UsdtLogoContent()
                    "Wallet", "ওয়ালেট" -> WalletLogoContent()
                    else -> GenericMethodLogoContent(method)
                }

                // Selected Green Checkmark Badge inside Card
                if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Top-Right Floating "✨ New!" Badge for G Pay / Google Pay
        if (method == "Google Pay" || method == "G Pay" || method == "GPay") {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (4).dp, y = (-8).dp)
                    .background(Color(0xFF7C4DFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                    Text(
                        text = "New!",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ================= BRAND LOGO COMPOSABLES =================

@Composable
private fun BKashLogoContent() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        // bKash Origami Origami Emblem
        Canvas(modifier = Modifier.size(18.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.2f)
                lineTo(size.width * 0.9f, size.height * 0.1f)
                lineTo(size.width * 0.6f, size.height * 0.5f)
                lineTo(size.width * 0.85f, size.height * 0.9f)
                lineTo(size.width * 0.35f, size.height * 0.65f)
                close()
            }
            drawPath(path, color = Color(0xFFE2136E))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "bKash",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
private fun GPayLogoContent() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF212121)),
        color = Color.White,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Google G Multi-Color Emblem
            Text(
                text = "G",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4285F4)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "Pay",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3C4043)
            )
        }
    }
}

@Composable
private fun NagadLogoContent() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        // Nagad Flame / Circle emblem
        Canvas(modifier = Modifier.size(18.dp)) {
            drawCircle(color = Color(0xFFF44336), radius = size.minDimension / 2.2f)
            drawCircle(color = Color(0xFFFF9800), radius = size.minDimension / 3.8f, center = Offset(size.width * 0.6f, size.height * 0.4f))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Nagad",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
private fun CardsOthersLogoContent() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        // Visa Emblem
        Text(
            text = "VISA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1A1F71)
        )
        Spacer(modifier = Modifier.width(6.dp))
        
        // Mastercard overlapping circles
        Canvas(modifier = Modifier.size(height = 14.dp, width = 22.dp)) {
            drawCircle(color = Color(0xFFEB001B), radius = 7.dp.toPx(), center = Offset(6.dp.toPx(), 7.dp.toPx()))
            drawCircle(color = Color(0xFFFF5F00), radius = 7.dp.toPx(), center = Offset(14.dp.toPx(), 7.dp.toPx()))
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Others",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
private fun RocketLogoContent() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color(0xFF8E24AA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Rocket",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
private fun WiseLogoContent() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color(0xFF37D67A), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "W",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Wise",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
private fun UsdtLogoContent() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color(0xFF26A17B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "₮",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "USDT",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
private fun WalletLogoContent() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Wallet",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
private fun GenericMethodLogoContent(method: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = null,
            tint = Color(0xFF4A5568),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = method,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}
