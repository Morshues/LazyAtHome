package com.morshues.lazyathome.ui.settings

import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity
import com.morshues.lazyathome.R
import com.morshues.lazyathome.di.AppModule

class SettingsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment(
                    AppModule.authRepository
                ))
                .commitNow()
        }

        onBackPressedDispatcher.addCallback {
            setResult(RESULT_OK)
            finish()
        }
    }
}