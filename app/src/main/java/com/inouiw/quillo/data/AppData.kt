package com.inouiw.quillo.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Profile(
    val name: String = "",
    val initials: String = "",
    val email: String = "",
    val phone: String = "",
)

data class Summary(
    val balance: Double = 0.0,
    val monthlyChange: Double = 0.0,
    val savedThisYear: Double = 0.0,
    val availableDebit: Double = 0.0,
    val nextBillName: String = "",
    val nextBillAmount: Double = 0.0,
)

/**
 * Everything the screens show. Data lives under users/{uid} in Firestore, so each
 * signed-in account only sees its own data. Nothing is hard-coded here.
 */
object AppData {
    var loaded by mutableStateOf(false)
        private set
    var loadFailed by mutableStateOf(false)
        private set
    var profile by mutableStateOf(Profile())
        private set
    var summary by mutableStateOf(Summary())
        private set
    var onlinePayments by mutableStateOf(true)
        private set

    val cards = mutableStateListOf<QCardInfo>()
    val goals = mutableStateListOf<Goal>()
    val transactions = mutableStateListOf<Txn>()
    val bills = mutableStateListOf<Bill>()
    val notices = mutableStateListOf<Notice>()

    private fun userDoc(): DocumentReference? {
        val uid = Firebase.auth.currentUser?.uid ?: return null
        return Firebase.firestore.collection("users").document(uid)
    }

    /** Reads this user's data from Firestore. Safe to call again to retry or after sign-in. */
    suspend fun refresh() {
        val user = userDoc()
        if (user == null) {
            clear()
            return
        }
        try {
            val summaryDoc = user.collection("summary").document("main").get().await()
            val profileDoc = user.collection("profile").document("main").get().await()
            val settingsDoc = user.collection("settings").document("main").get().await()
            val cardDocs = user.collection("cards").orderBy("order").get().await()
            val goalDocs = user.collection("goals").orderBy("order").get().await()
            val txnDocs = user.collection("transactions").orderBy("order").get().await()
            val billDocs = user.collection("bills").orderBy("order").get().await()
            val noticeDocs = user.collection("notifications").orderBy("order").get().await()

            summary = Summary(
                balance = summaryDoc.num("balance"),
                monthlyChange = summaryDoc.num("monthlyChange"),
                savedThisYear = summaryDoc.num("savedThisYear"),
                availableDebit = summaryDoc.num("availableDebit"),
                nextBillName = summaryDoc.str("nextBillName"),
                nextBillAmount = summaryDoc.num("nextBillAmount"),
            )
            profile = Profile(
                name = profileDoc.str("name"),
                initials = profileDoc.str("initials"),
                email = profileDoc.str("email"),
                phone = profileDoc.str("phone"),
            )
            onlinePayments = settingsDoc.getBoolean("onlinePayments") ?: true
            cards.replaceWith(cardDocs.documents.map { it.toCard() })
            goals.replaceWith(goalDocs.documents.map { doc ->
                Goal(doc.str("name"), doc.num("saved"), doc.num("target"), doc.id)
            })
            transactions.replaceWith(txnDocs.documents.map { doc ->
                Txn(
                    title = doc.str("title"),
                    subtitle = doc.str("subtitle"),
                    amount = doc.num("amount"),
                    category = doc.str("category"),
                    time = doc.str("time"),
                    day = doc.str("day"),
                )
            })
            bills.replaceWith(billDocs.documents.filter { !it.flag("paid") }.map { doc ->
                Bill(doc.str("name"), doc.num("amount"), doc.str("due"))
            })
            notices.replaceWith(noticeDocs.documents.map { doc ->
                Notice(
                    title = doc.str("title"),
                    body = doc.str("body"),
                    time = doc.str("time"),
                    group = doc.str("group"),
                    icon = doc.str("icon"),
                    unread = doc.flag("unread"),
                )
            })
            loadFailed = false
            loaded = true
        } catch (e: Exception) {
            loadFailed = true
        }
    }

    /** Forgets everything on screen. Called on sign-out. */
    fun clear() {
        cards.clear()
        goals.clear()
        transactions.clear()
        bills.clear()
        notices.clear()
        profile = Profile()
        summary = Summary()
        onlinePayments = true
        loaded = false
        loadFailed = false
    }

    /** Saves a card added in the app under this user, then shows it. */
    suspend fun addCard(card: QCardInfo) {
        val user = userDoc() ?: error("Not signed in")
        val ref = user.collection("cards").document()
        ref.set(
            mapOf(
                "label" to card.label,
                "last4" to card.last4,
                "network" to card.network,
                "kind" to card.kind,
                "isCredit" to card.isCredit,
                "detail" to card.detail,
                "holder" to card.holder,
                "expiry" to card.expiry,
                "frozen" to false,
                "order" to cards.size + 1,
            ),
        ).await()
        cards.add(card.copy(id = ref.id))
    }

    /** Deletes a card from this user's Firestore cards and removes it from the list. */
    suspend fun deleteCard(cardId: String) {
        val user = userDoc() ?: error("Not signed in")
        user.collection("cards").document(cardId).delete().await()
        cards.removeAll { it.id == cardId }
    }

    /** Saves a new savings goal under this user, then shows it in the list. */
    suspend fun addGoal(name: String, target: Double) {
        val user = userDoc() ?: error("Not signed in")
        val ref = user.collection("goals").document()
        ref.set(
            mapOf(
                "name" to name,
                "saved" to 0.0,
                "target" to target,
                "order" to goals.size + 1,
            ),
        ).await()
        goals.add(Goal(name, 0.0, target, ref.id))
    }

    /** Moves money from the balance into a goal and records it in Activity. */
    suspend fun addToGoal(goal: Goal, amount: Double) {
        val user = userDoc() ?: error("Not signed in")
        user.collection("goals").document(goal.id).update("saved", goal.saved + amount).await()
        user.collection("transactions").document()
            .set(transactionFields("Saved for ${goal.name}", "Goal transfer", -amount)).await()
        user.collection("summary").document("main").update(
            mapOf(
                "balance" to summary.balance - amount,
                "availableDebit" to summary.availableDebit - amount,
                "savedThisYear" to summary.savedThisYear + amount,
            ),
        ).await()
        refresh()
    }

    /** Records a Razorpay test top-up as a transaction and adds it to the balance. */
    suspend fun recordTopUp(amount: Double, cardLabel: String) {
        val user = userDoc() ?: error("Not signed in")
        user.collection("transactions").document()
            .set(transactionFields("Top up", "From $cardLabel", amount)).await()
        user.collection("summary").document("main")
            .update("balance", summary.balance + amount).await()
        refresh()
    }

    /** Records a Razorpay test bill payment, marks the bill paid and takes it from the balance. */
    suspend fun recordBillPayment(bill: Bill) {
        val user = userDoc() ?: error("Not signed in")
        val amount = bill.amount.toDouble()
        val matches = user.collection("bills").whereEqualTo("name", bill.name).get().await()
        for (doc in matches.documents) doc.reference.update("paid", true).await()
        user.collection("transactions").document()
            .set(transactionFields(bill.name, "Bill payment", -amount)).await()
        val next = bills.firstOrNull { it.name != bill.name }
        user.collection("summary").document("main").update(
            mapOf(
                "balance" to summary.balance - amount,
                "nextBillName" to (next?.name ?: ""),
                "nextBillAmount" to (next?.amount?.toDouble() ?: 0.0),
            ),
        ).await()
        refresh()
    }

    private fun transactionFields(title: String, subtitle: String, amount: Double): Map<String, Any> = mapOf(
        "title" to title,
        "subtitle" to subtitle,
        "amount" to amount,
        "category" to "Other",
        "time" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
        "day" to "Today",
        "order" to -(System.currentTimeMillis() / 1000),
    )

    /** Records a send to another person as a debit and takes it from the balance. */
    suspend fun recordSend(recipient: String, amount: Double, note: String) {
        val user = userDoc() ?: error("Not signed in")
        user.collection("transactions").document()
            .set(transactionFields(recipient, note.ifBlank { "Sent" }, -amount)).await()
        user.collection("summary").document("main").update(
            mapOf(
                "balance" to summary.balance - amount,
                "availableDebit" to summary.availableDebit - amount,
            ),
        ).await()
        refresh()
    }

    /** Saves a money request in the user's own requests collection. */
    suspend fun recordRequest(from: String, amount: Double, purpose: String) {
        val user = userDoc() ?: error("Not signed in")
        user.collection("requests").document().set(
            mapOf(
                "from" to from,
                "amount" to amount,
                "purpose" to purpose.ifBlank { "Request" },
                "status" to "Pending",
                "time" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                "day" to "Today",
                "order" to -(System.currentTimeMillis() / 1000),
            ),
        ).await()
    }

    /** Freezes or unfreezes a card and keeps the change in Firestore. */
    suspend fun setFrozen(cardId: String, frozen: Boolean) {
        val user = userDoc() ?: error("Not signed in")
        user.collection("cards").document(cardId).update("frozen", frozen).await()
        val index = cards.indexOfFirst { it.id == cardId }
        if (index >= 0) cards[index] = cards[index].copy(frozen = frozen)
    }

    /** Turns online payments on or off and keeps the change in Firestore. */
    suspend fun setOnlinePayments(enabled: Boolean) {
        val user = userDoc() ?: error("Not signed in")
        user.collection("settings").document("main")
            .set(mapOf("onlinePayments" to enabled), SetOptions.merge())
            .await()
        onlinePayments = enabled
    }

    private fun DocumentSnapshot.toCard() = QCardInfo(
        id = id,
        label = str("label"),
        last4 = str("last4"),
        network = str("network"),
        kind = str("kind"),
        isCredit = flag("isCredit"),
        detail = str("detail"),
        holder = str("holder"),
        expiry = str("expiry"),
        frozen = flag("frozen"),
    )

    private fun DocumentSnapshot.str(key: String): String = getString(key).orEmpty()
    private fun DocumentSnapshot.num(key: String): Double = getDouble(key) ?: 0.0
    private fun DocumentSnapshot.flag(key: String): Boolean = getBoolean(key) ?: false

    private fun <T> SnapshotStateList<T>.replaceWith(items: List<T>) {
        clear()
        addAll(items)
    }
}
