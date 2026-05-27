package com.example.tuneify_final_project.ui.utils

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.security.SecureRandom

object CryptoUtils {

    // ─────────────────────────────────────────────
    // AES-GCM (ANDROID <-> PYTHON COMPATIBLE)
    // ─────────────────────────────────────────────

    fun aesEncrypt(plaintext: String, aesKey: ByteArray): Map<String, String> {
        val nonce = ByteArray(12)
        SecureRandom().nextBytes(nonce)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = SecretKeySpec(aesKey, "AES")
        val spec = GCMParameterSpec(128, nonce)

        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        // IMPORTANT: cipher output already includes authentication tag
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return mapOf(
            "nonce" to Base64.encodeToString(nonce, Base64.NO_WRAP),
            "ciphertext" to Base64.encodeToString(encrypted, Base64.NO_WRAP)
        )
    }

    fun aesDecrypt(data: Map<String, String>, aesKey: ByteArray): String {
        val nonce = Base64.decode(data["nonce"], Base64.NO_WRAP)
        val ciphertext = Base64.decode(data["ciphertext"], Base64.NO_WRAP)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = SecretKeySpec(aesKey, "AES")
        val spec = GCMParameterSpec(128, nonce)

        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    // ─────────────────────────────────────────────
    // AES KEY DERIVATION (DH → AES-256)
    // ─────────────────────────────────────────────

    fun deriveAesKey(sharedSecret: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(sharedSecret)
    }
    // ─────────────────────────────────────────────
    // RSA PUBLIC KEY LOADER
    // ─────────────────────────────────────────────


    fun loadDhPublicKey(b64PemOrDer: String): javax.crypto.interfaces.DHPublicKey {
        val decoded = Base64.decode(b64PemOrDer, Base64.NO_WRAP)
        val asString = String(decoded, Charsets.UTF_8)

        val derBytes = if (asString.contains("-----BEGIN PUBLIC KEY-----")) {
            // it's a PEM — strip headers and decode the inner Base64
            val stripped = asString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "")
                .trim()
            Base64.decode(stripped, Base64.DEFAULT)
        } else {
            // already raw DER
            decoded
        }

        return KeyFactory.getInstance("DH")
            .generatePublic(X509EncodedKeySpec(derBytes)) as javax.crypto.interfaces.DHPublicKey
    }

    fun loadRsaPublicKey(pemString: String): PublicKey {
        val cleaned = pemString
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\n", "")
            .replace("\n", "")
            .trim()

        val decoded = Base64.decode(cleaned, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(decoded)

        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }

    // ─────────────────────────────────────────────
    // RSA SIGNATURE VERIFY
    // ─────────────────────────────────────────────

    fun rsaVerify(
        data: String,
        signatureB64: String,
        publicKey: PublicKey
    ): Boolean {
        return try {
            val signature = Signature.getInstance("SHA256withRSA/PSS")
            signature.initVerify(publicKey)
            signature.update(data.toByteArray(Charsets.UTF_8))

            val sigBytes = Base64.decode(signatureB64, Base64.NO_WRAP)
            signature.verify(sigBytes)
        } catch (e: Exception) {
            false
        }
    }
}