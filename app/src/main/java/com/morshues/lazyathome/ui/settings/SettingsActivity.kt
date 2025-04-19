package com.morshues.lazyathome.ui.settings

import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity
import com.morshues.lazyathome.R

class SettingsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commitNow()
        }

        onBackPressedDispatcher.addCallback {
            setResult(RESULT_OK)
            finish()
        }
    }
}