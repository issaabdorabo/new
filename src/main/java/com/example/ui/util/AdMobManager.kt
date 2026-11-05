package com.example.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"

    // Official Google Test Ad Unit IDs
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var isShowingInterstitial = false

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob initialized: $status")
                loadInterstitialAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob init error: ${e.message}")
        }
    }

    /**
     * Preloads an Interstitial Ad in the background in accordance with AdMob / AdSense policies,
     * ensuring smooth user experience without UI lag or unexpected interruptions.
     */
    fun loadInterstitialAd(context: Context) {
        if (isLoadingInterstitial || interstitialAd != null) return
        isLoadingInterstitial = true
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    Log.d(TAG, "AdMob Interstitial Ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingInterstitial = false
                    interstitialAd = null
                    Log.d(TAG, "AdMob Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Records an export event (PDF or Excel report).
     * If export count reaches a multiple of 3 (after 3 exports),
     * shows the preloaded Interstitial Ad with strict policy compliance.
     */
    fun notifyExportCompleted(context: Context, onFinished: () -> Unit = {}) {
        val prefs = context.getSharedPreferences("admob_prefs", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt("export_count", 0) + 1
        prefs.edit().putInt("export_count", currentCount).apply()
        Log.d(TAG, "Export completed. Total exports: $currentCount")

        // Trigger interstitial ad after every 3 exports
        if (currentCount % 3 == 0) {
            val activity = findActivity(context)
            if (activity != null) {
                showInterstitialIfAvailable(activity, onFinished)
                return
            }
        }
        onFinished()
    }

    fun showInterstitialIfAvailable(activity: Activity, onFinished: () -> Unit = {}) {
        if (isShowingInterstitial) {
            onFinished()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            // Ad not ready yet - trigger background load and continue without blocking user
            loadInterstitialAd(activity)
            onFinished()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                isShowingInterstitial = false
                loadInterstitialAd(activity)
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                isShowingInterstitial = false
                loadInterstitialAd(activity)
                onFinished()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingInterstitial = true
            }
        }

        ad.show(activity)
    }

    fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}

/**
 * AdMob Banner Composable using AndroidView.
 * Renders the banner ad smoothly with graceful fallback.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.TEST_BANNER_AD_UNIT_ID
) {
    var isLoaded by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isLoaded = true
                            hasError = false
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isLoaded = false
                            hasError = true
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )

        if (!isLoaded && !hasError) {
            Text(
                text = "جاري تحميل إعلان AdMob...",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
