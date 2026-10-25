package com.inouiw.quillo.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Talks to the Quillo agent server, which runs on the developer's Mac on the home Wi-Fi. */
object AgentApi {
    // Change this if the Mac's IP address on the Wi-Fi changes.
    private const val BASE_URL = "http://192.168.29.213:8787"

    data class Turn(val role: String, val content: String)

    suspend fun ask(question: String, history: List<Turn>): String = withContext(Dispatchers.IO) {
        val token = fetchToken()
        val body = JSONObject()
            .put("question", question)
            .put("history", JSONArray(history.map { JSONObject().put("role", it.role).put("content", it.content) }))
        val conn = (URL("$BASE_URL/ask").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 10_000
            readTimeout = 90_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            if (conn.responseCode != 200) error("Agent returned ${conn.responseCode}")
            JSONObject(conn.inputStream.bufferedReader().use { it.readText() }).getString("answer")
        } finally {
            conn.disconnect()
        }
    }

    /** The token is stored in the user's own Firestore data (users/{uid}/agent/config). */
    private suspend fun fetchToken(): String {
        val uid = Firebase.auth.currentUser?.uid ?: error("Not signed in")
        val doc = Firebase.firestore.collection("users").document(uid)
            .collection("agent").document("config").get().await()
        return doc.getString("token") ?: error("Agent token is missing")
    }
}
