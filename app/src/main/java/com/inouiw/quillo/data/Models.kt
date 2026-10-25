package com.inouiw.quillo.data

import kotlin.math.abs

/** A card on the account. Amounts are in rupees. The id is the Firestore document id. */
data class QCardInfo(
    val id: String,
    val label: String,
    val last4: String,
    val network: String,
    val kind: String,
    val isCredit: Boolean,
    val detail: String,
    val holder: String = "",
    val expiry: String = "",
    val frozen: Boolean = false,
)

data class Goal(val name: String, val saved: Double, val target: Double, val id: String = "")

data class Txn(
    val title: String,
    val subtitle: String,
    val amount: Double,
    val category: String,
    val time: String,
    val day: String,
)

data class Bill(val name: String, val amount: Double, val due: String)

data class Notice(
    val title: String,
    val body: String,
    val time: String,
    val group: String,
    val icon: String,
    val unread: Boolean,
)

/** Indian number format: 8,41,260.60. */
fun formatInr(value: Double, paise: Boolean = false): String {
    val negative = value < 0
    val fixed = String.format("%.2f", abs(value))
    val parts = fixed.split(".")
    val whole = parts[0]
    val grouped = if (whole.length <= 3) {
        whole
    } else {
        val last3 = whole.takeLast(3)
        val rest = whole.dropLast(3)
        rest.reversed().chunked(2).joinToString(",").reversed() + "," + last3
    }
    val sign = if (negative) "-" else ""
    return sign + "₹" + grouped + if (paise) "." + parts[1] else ""
}
