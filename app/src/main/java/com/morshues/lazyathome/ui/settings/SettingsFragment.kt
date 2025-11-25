package com.morshues.lazyathome.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.leanback.preference.LeanbackEditTextPreferenceDialogFragmentCompat
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.morshues.lazyathome.R
import com.morshues.lazyathome.data.network.AuthRetrofitClient
import com.morshues.lazyathome.data.repository.AuthRepository
import com.morshues.lazyathome.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : LeanbackPreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        findPreference<EditTextPreference>("server_path")?.summaryProvider =
            Preference.SummaryProvider<EditTextPreference> { it.text ?: "" }

        // Login/Logout preference
        findPreference<Preference>("login_logout")?.setOnPreferenceClickListener {
            if (SettingsManager.isLoggedIn(requireContext())) {
                handleLogout()
            } else {
                showLoginDialog()
            }
            true
        }

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

        // Update login status
        updateLoginStatus()
    }

    override fun onResume() {
        super.onResume()
        updateLoginStatus()
    }

    private fun updateLoginStatus() {
        val loginLogoutPref = findPreference<Preference>("login_logout")

        if (SettingsManager.isLoggedIn(requireContext())) {
            val userName = SettingsManager.getUserName(requireContext()) ?:
                          SettingsManager.getUserEmail(requireContext()) ?: "User"
            loginLogoutPref?.title = getString(R.string.settings_pref_logout_title)
            loginLogoutPref?.summary = getString(R.string.settings_pref_login_summary_logged_in, userName)
        } else {
            loginLogoutPref?.title = getString(R.string.settings_pref_login_title)
            loginLogoutPref?.summary = getString(R.string.settings_pref_login_summary_logged_out)
        }
    }

    private fun showLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.email_input)
        val passwordInput = dialogView.findViewById<EditText>(R.id.password_input)
        val loginProgress = dialogView.findViewById<ProgressBar>(R.id.login_progress)
        val loginError = dialogView.findViewById<TextView>(R.id.login_error)
        val loginButton = dialogView.findViewById<Button>(R.id.login_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                loginError.text = getString(R.string.login_error_empty)
                loginError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            performLogin(email, password, loginProgress, loginError, loginButton, cancelButton, dialog)
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun performLogin(
        email: String,
        password: String,
        progressBar: ProgressBar,
        errorText: TextView,
        loginButton: Button,
        cancelButton: Button,
        dialog: AlertDialog
    ) {
        // Show loading state
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        loginButton.isEnabled = false
        cancelButton.isEnabled = false

        val authApi = AuthRetrofitClient.getService(requireContext())
        val authRepository = AuthRepository(authApi)
        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    authRepository.login(email, password, deviceId)
                }

                if (response.ok) {
                    // Save auth data
                    SettingsManager.saveAuthData(
                        requireContext(),
                        response.accessToken,
                        response.refreshToken,
                        response.user.email,
                        response.user.name
                    )

                    // Update UI
                    updateLoginStatus()
                    Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    throw Exception("Login failed")
                }
            } catch (e: Exception) {
                // Show error
                errorText.text = getString(R.string.login_error_failed, e.message ?: "Unknown error")
                errorText.visibility = View.VISIBLE
                loginButton.isEnabled = true
                cancelButton.isEnabled = true
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleLogout() {
        SettingsManager.clearAuthData(requireContext())
        updateLoginStatus()
        Toast.makeText(requireContext(), R.string.logout_success, Toast.LENGTH_SHORT).show()
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