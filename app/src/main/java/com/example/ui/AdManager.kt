package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.OnUserEarnedRewardListener

object AdManager {
    private const val TAG = "AdManager"
    
    // Ad IDs provided by the user
    const val APP_ID = "ca-app-pub-1131981412237081~4182829725"
    const val BANNER_ID = "ca-app-pub-1131981412237081/4073807069"
    const val INTERSTITIAL_ID = "ca-app-pub-1131981412237081/8577370307"
    const val REWARDED_ID = "ca-app-pub-1131981412237081/8583897716"

    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null

    // Load Interstitial Ad
    fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "InterstitialAd failed to load: ${adError.message}")
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "InterstitialAd was loaded successfully.")
                    mInterstitialAd = interstitialAd
                }
            }
        )
    }

    // Show Interstitial Ad if ready, otherwise trigger load
    fun showInterstitial(context: Context, onAdDismissed: () -> Unit = {}) {
        val activity = context as? Activity
        if (activity != null && mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed.")
                    mInterstitialAd = null
                    onAdDismissed()
                    // Preload next interstitial
                    loadInterstitial(context)
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.d(TAG, "Interstitial ad failed to show: ${adError.message}")
                    mInterstitialAd = null
                    onAdDismissed()
                    loadInterstitial(context)
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad is not ready yet.")
            onAdDismissed()
            loadInterstitial(context)
        }
    }

    // Load Rewarded Ad
    fun loadRewarded(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "RewardedAd failed to load: ${adError.message}")
                    mRewardedAd = null
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "RewardedAd was loaded successfully.")
                    mRewardedAd = rewardedAd
                }
            }
        )
    }

    // Show Rewarded Ad if ready, otherwise load and notify
    fun showRewarded(context: Context, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit = {}) {
        val activity = context as? Activity
        if (activity != null && mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed.")
                    mRewardedAd = null
                    onAdDismissed()
                    // Preload next rewarded ad
                    loadRewarded(context)
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.d(TAG, "Rewarded ad failed to show: ${adError.message}")
                    mRewardedAd = null
                    onAdDismissed()
                    loadRewarded(context)
                }
            }
            mRewardedAd?.show(activity, OnUserEarnedRewardListener { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            })
        } else {
            Log.d(TAG, "Rewarded ad is not ready yet.")
            onAdDismissed()
            loadRewarded(context)
        }
    }
}

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adId: String = AdManager.BANNER_ID
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = adId
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Update code if needed
        }
    )
}
