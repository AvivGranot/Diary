package com.proactivediary.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the SQLCipher database encryption key.
 *
 * - On first launch, generates a random 256-bit key.
 * - Encrypts the key with an Android Keystore-backed AES/GCM key.
 * - Stores the encrypted key + IV in SharedPreferences.
 * - On subsequent launches, decrypts and returns the key.
 */
@Singleton
class EncryptedDatabaseKeyManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val KEYSTORE_ALIAS = "proactive_diary_db_key"
        private const val PREFS_NAME = "db_key_prefs"
        private const val PREF_ENCRYPTED_KEY = "encrypted_db_key"
        private const val PREF_IV = "encrypted_db_iv"
        private const val KEY_SIZE_BYTES = 32 // 256-bit
        private const val GCM_TAG_LENGTH = 128
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Returns the database encryption key, creating one if it doesn't exist yet.
     */
    fun getOrCreateDatabaseKey(): ByteArray {
        val storedKey = prefs.getString(PREF_ENCRYPTED_KEY, null)
        val storedIv = prefs.getString(PREF_IV, null)

        return if (storedKey != null && storedIv != null) {
            decryptKey(
                Base64.decode(storedKey, Base64.NO_WRAP),
                Base64.decode(storedIv, Base64.NO_WRAP)
            )
        } else {
            val newKey = generateRandomKey()
            encryptAndStoreKey(newKey)
            newKey
        }
    }

    private fun generateRandomKey(): ByteArray {
        val key = ByteArray(KEY_SIZE_BYTES)
        SecureRandom().nextBytes(key)
        return key
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        keyStore.getKey(KEYSTORE_ALIAS, null)?.let {
            return it as SecretKey
        }

        val keyGen = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGen.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGen.generateKey()
    }

    private fun encryptAndStoreKey(plainKey: ByteArray) {
        val keystoreKey = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey)

        val encrypted = cipher.doFinal(plainKey)
        val iv = cipher.iv

        prefs.edit()
            .putString(PREF_ENCRYPTED_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(PREF_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()
    }

    private fun decryptKey(encryptedKey: ByteArray, iv: ByteArray): ByteArray {
        val keystoreKey = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keystoreKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encryptedKey)
    }
}
