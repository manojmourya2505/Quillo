package com.inouiw.quillo

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.inouiw.quillo.ui.QuilloApp
import com.inouiw.quillo.data.RazorpayCheckout
import com.inouiw.quillo.ui.theme.QuilloTheme
import com.razorpay.PaymentResultListener

class MainActivity : FragmentActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuilloTheme {
                QuilloApp()
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        RazorpayCheckout.deliver(true)
    }

    override fun onPaymentError(code: Int, response: String?) {
        RazorpayCheckout.deliver(false)
    }
}
