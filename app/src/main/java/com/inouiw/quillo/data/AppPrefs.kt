package com.inouiw.quillo.data

import android.content.Context

/** Small saved flags: whether the intro was seen and whether fingerprint/face unlock is on. */
object AppPrefs {
    private const val FILE = "quillo_prefs"
    private const val INTRO_SEEN = "intro_seen"
    private const val BIOMETRIC_ASKED = "biometric_asked"
    private const val BIOMETRIC_ENABLED = "biometric_enabled"

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun introSeen(context: Context): Boolean = prefs(context).getBoolean(INTRO_SEEN, false)

    fun setIntroSeen(context: Context) {
        prefs(context).edit().putBoolean(INTRO_SEEN, true).apply()
    }

    fun biometricAsked(context: Context): Boolean = prefs(context).getBoolean(BIOMETRIC_ASKED, false)

    fun biometricEnabled(context: Context): Boolean = prefs(context).getBoolean(BIOMETRIC_ENABLED, false)

    fun setBiometric(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(BIOMETRIC_ASKED, true)
            .putBoolean(BIOMETRIC_ENABLED, enabled)
            .apply()
    }
}
