package com.inouiw.quillo.data

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.inouiw.quillo.R

/** Fingerprint or face unlock through the system BiometricPrompt. */
object BiometricGate {
    private const val AUTH = BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun isAvailable(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(AUTH) == BiometricManager.BIOMETRIC_SUCCESS

    fun prompt(activity: FragmentActivity, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure(errString.toString())
            }
        }
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.unlock_quillo))
            .setSubtitle(activity.getString(R.string.biometric_subtitle))
            .setAllowedAuthenticators(AUTH)
            .setNegativeButtonText(activity.getString(R.string.use_password))
            .build()
        prompt.authenticate(info)
    }
}
