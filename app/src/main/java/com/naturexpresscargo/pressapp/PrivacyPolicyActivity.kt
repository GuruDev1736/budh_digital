package com.naturexpresscargo.pressapp

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.naturexpresscargo.pressapp.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        const val PRIVACY_POLICY_URL = "https://budh-digital.anjamhelp.org/privacy-policy/"
        const val TERMS_CONDITIONS_URL = "https://budh-digital.anjamhelp.org/terms-and-conditions/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra(EXTRA_URL) ?: PRIVACY_POLICY_URL

        openCustomTab(url)
    }

    private fun openCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()

        // Set toolbar color to match app theme
        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(this, R.color.dark_background))
            .setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_background))
            .build()

        builder.setDefaultColorSchemeParams(colorSchemeParams)
        builder.setShowTitle(true)
        builder.setUrlBarHidingEnabled(true)

        val customTabsIntent = builder.build()

        try {
            customTabsIntent.launchUrl(this, Uri.parse(url))
            finish() // Close this activity after launching Custom Tab
        } catch (e: Exception) {
            // If Custom Tabs not available, finish activity
            finish()
        }
    }
}

