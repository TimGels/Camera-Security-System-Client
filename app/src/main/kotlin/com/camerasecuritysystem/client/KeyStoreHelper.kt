package com.camerasecuritysystem.client

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class KeyStoreHelper(private val keystoreAlias: String) {

    private val keyProvider = "AndroidKeyStore"

    private val keyGenerator: KeyGenerator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyProvider)
    private val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        keystoreAlias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()

    init {
        if (getKey() == null) {
            Log.e("KEY", "key is null")
            generateKey()
        }
    }

    /**
     * Generate a new key
     */
    private fun generateKey() {
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Return existing key. If not exists, it returns null
     */
    private fun getKey(): SecretKey? {
        val keyStore: KeyStore = KeyStore.getInstance(keyProvider)
        keyStore.load(null)

        if (keyStore.getEntry(keystoreAlias, null) == null) {
            return null
        }
        val secretKeyEntry: KeyStore.SecretKeyEntry =
            keyStore.getEntry(keystoreAlias, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    /**
     * Encrypt a string
     */
    fun encryptData(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")

        var temp: String = data
        while (temp.toByteArray().size % 16 != 0) {
            temp += "\u0020"
        }

        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val ivBytes: ByteArray = cipher.iv
        val encryptedBytes: ByteArray = cipher.doFinal(temp.toByteArray(Charsets.ISO_8859_1))

        return Pair(ivBytes, encryptedBytes)
    }

    /**
     * Decrypt a string
     */
    fun decryptData(ivBytes: ByteArray, data: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")

        val spec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return cipher.doFinal(data).toString(Charsets.ISO_8859_1).trim()
    }
}
