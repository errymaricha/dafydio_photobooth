@file:Suppress("DEPRECATION")
package com.errymaricha.dafydiobooth.station.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface TokenStore {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clear()
}

class SecureTokenStore(private val context: Context) : TokenStore {
    private val newPrefs = context.getSharedPreferences("secure_station_prefs_v2", Context.MODE_PRIVATE)

    init {
        migrateIfNeeded()
    }

    @Suppress("DEPRECATION")
    private fun migrateIfNeeded() {
        val legacyPrefsName = "secure_station_prefs"
        val hasLegacyPrefs = context.getSharedPreferences(legacyPrefsName, Context.MODE_PRIVATE).all.isNotEmpty()
        if (hasLegacyPrefs && !newPrefs.contains(KEY_TOKEN)) {
            try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val legacyPrefs = EncryptedSharedPreferences.create(
                    context,
                    legacyPrefsName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
                val token = legacyPrefs.getString(KEY_TOKEN, null)
                if (token != null) {
                    saveToken(token)
                    Log.i("SecureTokenStore", "Migrasi token dari EncryptedSharedPreferences ke v2 sukses.")
                }
                // Hapus token lama
                legacyPrefs.edit().remove(KEY_TOKEN).commit()
            } catch (e: Exception) {
                Log.e("SecureTokenStore", "Gagal migrasi token lama: ${e.message}")
            }
        }
    }

    override fun saveToken(token: String) {
        val encrypted = CryptoManager.encrypt(token.normalizeBearerToken())
        newPrefs.edit().putString(KEY_TOKEN, encrypted).commit()
    }

    override fun getToken(): String? {
        val encrypted = newPrefs.getString(KEY_TOKEN, null) ?: return null
        return try {
            CryptoManager.decrypt(encrypted).normalizeBearerToken()
        } catch (e: Exception) {
            Log.e("SecureTokenStore", "Gagal dekripsi token: ${e.message}")
            null
        }
    }

    override fun clear() {
        newPrefs.edit().remove(KEY_TOKEN).apply()
    }

    companion object {
        private const val KEY_TOKEN = "sanctum_token"
    }
}

private fun String.normalizeBearerToken(): String {
    return trim().removePrefix("Bearer ").removePrefix("bearer ").trim()
}
