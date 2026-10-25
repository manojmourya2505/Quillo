package com.inouiw.quillo.data

import android.app.Activity
import com.inouiw.quillo.R
import com.razorpay.Checkout
import org.json.JSONObject
import kotlin.math.roundToInt

/** Opens Razorpay test checkout. MainActivity reports the outcome through deliver(). */
object RazorpayCheckout {
    private var onResult: ((Boolean) -> Unit)? = null

    fun open(activity: Activity, amountRupees: Double, description: String, onDone: (Boolean) -> Unit) {
        onResult = onDone
        try {
            val options = JSONObject()
                .put("name", activity.getString(R.string.app_name))
                .put("description", description)
                .put("currency", "INR")
                .put("amount", (amountRupees * 100).roundToInt())
                .put("theme", JSONObject().put("color", "#137A46"))
            Checkout().apply { setKeyID(activity.getString(R.string.razorpay_key_id)) }.open(activity, options)
        } catch (e: Exception) {
            onResult = null
            onDone(false)
        }
    }

    fun deliver(success: Boolean) {
        onResult?.invoke(success)
        onResult = null
    }
}
