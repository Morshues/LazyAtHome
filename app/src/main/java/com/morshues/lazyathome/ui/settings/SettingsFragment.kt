package com.morshues.lazyathome.ui.settings

import android.os.Bundle
import androidx.leanback.preference.LeanbackEditTextPreferenceDialogFragmentCompat
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.morshues.lazyathome.R

class SettingsFragment : LeanbackPreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        findPreference<EditTextPreference>("server_path")?.summaryProvider =
            Preference.SummaryProvider<EditTextPreference> { it.text ?: "" }

        findPreference<Preference>("row_order_editor")?.setOnPreferenceClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.settings_container, RowOrderFragment())
                .addToBackStack(null)
                .commit()
            true
        }

        findPreference<SeekBarPreference>("seek_amount_ms")?.summaryProvider =
            Preference.SummaryProvider<SeekBarPreference> { pref ->
                "${(pref.value / 1000)} 秒"
            }

        val versionPref = findPreference<Preference>("app_version")
        val versionName = try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName
        } catch (e: Exception) {
            "Unknown"
        }
        versionPref?.summary = "v$versionName"
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val fragment = when (preference) {
            is EditTextPreference -> LeanbackEditTextPreferenceDialogFragmentCompat.newInstance(preference.key)
            else -> null
        }
        if (fragment != null) {
            fragment.setTargetFragment(this, 0)
            parentFragmentManager.beginTransaction()
                .add(R.id.settings_container, fragment)
                .addToBackStack(null)
                .commit()
            return
        }
        return super.onDisplayPreferenceDialog(preference)
    }

}