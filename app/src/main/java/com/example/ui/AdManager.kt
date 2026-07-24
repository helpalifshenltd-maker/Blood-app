package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Use official AdMob test ad unit IDs
    private const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.e(TAG, "Failed to load Interstitial ad: ${error.message}")
                }
            }
        )
    }

    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_TEST_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.e(TAG, "Failed to load Rewarded ad: ${error.message}")
                }
            }
        )
    }

    private var lastInterstitialShowTime = 0L
    private const val INTERSTITIAL_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

    fun showInterstitial(context: Context, forceShow: Boolean = false, onDismiss: () -> Unit) {
        val activity = context as? Activity
        if (activity == null) {
            Log.d(TAG, "Activity is null, calling onDismiss immediately.")
            onDismiss()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (!forceShow && currentTime - lastInterstitialShowTime < INTERSTITIAL_INTERVAL_MS) {
            Log.d(TAG, "Cooldown active. Interstitial ad skipped.")
            onDismiss()
            return
        }

        if (interstitialAd == null) {
            Log.d(TAG, "Interstitial ad not ready, calling onDismiss immediately.")
            onDismiss()
            loadInterstitial(context)
            return
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastInterstitialShowTime = System.currentTimeMillis()
                onDismiss()
                loadInterstitial(context)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                onDismiss()
                loadInterstitial(context)
            }
        }

        interstitialAd?.show(activity)
    }

    fun showRewarded(context: Context, onRewardEarned: () -> Unit) {
        val activity = context as? Activity
        if (activity == null || rewardedAd == null) {
            Log.d(TAG, "Rewarded ad not ready, granting reward immediately.")
            onRewardEarned()
            loadRewarded(context)
            return
        }

        var rewardTriggered = false
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded(context)
                if (!rewardTriggered) {
                    rewardTriggered = true
                    onRewardEarned()
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewarded(context)
                if (!rewardTriggered) {
                    rewardTriggered = true
                    onRewardEarned()
                }
            }
        }

        rewardedAd?.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            if (!rewardTriggered) {
                rewardTriggered = true
                onRewardEarned()
            }
        }
    }
}

@Composable
fun AdBanner(
    modifier: Modifier = Modifier
) {
    // Official test Banner ad unit ID
    val bannerTestId = "ca-app-pub-3940256099942544/6300978111"
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                FrameLayout(context).apply {
                    val adView = AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = bannerTestId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.d("AdBanner", "Banner loaded successfully.")
                                isAdLoaded = true
                            }
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.e("AdBanner", "Banner failed to load: ${error.message}")
                                isAdLoaded = false
                            }
                        }
                    }
                    addView(adView)
                    adView.loadAd(AdRequest.Builder().build())
                }
            },
            update = {}
        )

        // Overlay a very clean, attractive blood donation campaign banner if ad fails to render
        // This keeps the screen beautifully balanced and provides a functional experience.
        if (!isAdLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF1F1))
                    .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Blood Donation Icon",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "রক্তদান করুন, জীবন বাঁচান! 🩸",
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "আপনার এক ব্যাগ রক্ত বাঁচাতে পারে একটি মুমূর্ষু জীবন।",
                            color = Color(0xFF5D4037),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
