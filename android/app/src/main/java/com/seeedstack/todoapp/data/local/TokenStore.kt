package com.seeedstack.todoapp.data.local

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        val isAdmin = decodeRole(token) == "admin"
        prefs.edit().putBoolean(KEY_IS_ADMIN, isAdmin).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isAdmin(): Boolean = prefs.getBoolean(KEY_IS_ADMIN, false)

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_IS_ADMIN).apply()
    }

    fun hasToken(): Boolean = getToken() != null

    /** Decode the JWT payload segment (base64url) and extract the "role" claim. */
    private fun decodeRole(token: String): String? {
        return try {
            val payload = token.split(".").getOrNull(1) ?: return null
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING)
            @Suppress("UNCHECKED_CAST")
            val map = Gson().fromJson(String(decoded), Map::class.java) as Map<String, Any>
            map["role"] as? String
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_IS_ADMIN = "is_admin"
    }
}
