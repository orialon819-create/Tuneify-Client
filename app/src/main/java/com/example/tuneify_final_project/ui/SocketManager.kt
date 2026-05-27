package com.example.tuneify_final_project.ui

import android.util.Base64
import android.util.Log
import com.example.tuneify_final_project.ui.utils.CryptoUtils
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import kotlin.concurrent.thread

object SocketManager {

    private var sessionToken: String? = null
    private const val TAG = "SocketManager"

    fun setSessionToken(token: String) {
        sessionToken = token
    }

    fun clearSession() {
        sessionToken = null
    }

    fun sendCommand(
        command: String,
        parameters: JSONObject,
        onResponse: (String?) -> Unit
    ) {
        thread {
            var socket: Socket? = null

            try {
                Log.d(TAG, "STEP 0 CONNECT")

                socket = Socket(NetworkConfig.serverIp, NetworkConfig.serverPort)

                val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // ──────────────── STEP 1 RSA ────────────────
                val rsaRaw = reader.readLine() ?: throw Exception("No RSA packet")
                val rsaJson = JSONObject(rsaRaw)

                val rsaPublicKey = CryptoUtils.loadRsaPublicKey(rsaJson.getString("public_key"))

// Read DH parameters sent by server


                Log.d(TAG, "RSA + DH params OK")

                // ──────────────── STEP 2 DH ────────────────
                // ──────────────── STEP 2 DH ────────────────
                val p = java.math.BigInteger(
                    "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                            "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                            "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                            "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                            "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
                            "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
                            "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
                            "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
                            "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
                            "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
                            "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16
                )
                val g = java.math.BigInteger.valueOf(2)

                val dhParamSpec = javax.crypto.spec.DHParameterSpec(p, g)
                val keyPairGen = KeyPairGenerator.getInstance("DH")
                keyPairGen.initialize(dhParamSpec)

                val keyPair = keyPairGen.generateKeyPair()

                val clientPublicB64 = Base64.encodeToString(
                    keyPair.public.encoded,
                    Base64.NO_WRAP
                )

                writer.println(
                    JSONObject()
                        .put("type", "CLIENT_DH")
                        .put("dh_public", clientPublicB64)
                        .toString()
                )

                Log.d(TAG, "DH SENT")

                // ──────────────── STEP 3 SERVER DH ────────────────
                val dhRaw = reader.readLine() ?: throw Exception("No DH response")
                Log.d(TAG, "DH RAW: $dhRaw")

                val dhJson = JSONObject(dhRaw)

                val serverDh = dhJson.getString("dh_public")
                val signature = dhJson.getString("signature")

                val valid = CryptoUtils.rsaVerify(serverDh, signature, rsaPublicKey)

                Log.d(TAG, "SIGNATURE VALID = $valid")

                if (!valid) throw Exception("RSA FAIL")

                // ──────────────── STEP 4 SHARED SECRET ────────────────
                val serverKey = CryptoUtils.loadDhPublicKey(serverDh)

                val ka = KeyAgreement.getInstance("DH")
                ka.init(keyPair.private)
                ka.doPhase(serverKey, true)

                val aesKey = CryptoUtils.deriveAesKey(ka.generateSecret())

                Log.d(TAG, "AES READY")

                // ──────────────── STEP 5 HANDSHAKE ────────────────
                val handshakeRaw = reader.readLine() ?: throw Exception("No handshake")
                Log.d(TAG, "HANDSHAKE RAW: $handshakeRaw")

                val handshakeJson = JSONObject(handshakeRaw)

                val decrypted = CryptoUtils.aesDecrypt(
                    mapOf(
                        "nonce" to handshakeJson.getString("nonce"),
                        "ciphertext" to handshakeJson.getString("ciphertext")
                    ),
                    aesKey
                )

                Log.d(TAG, "HANDSHAKE = $decrypted")

                if (!decrypted.contains("HANDSHAKE_OK")) {
                    throw Exception("Handshake failed")
                }

                // ──────────────── STEP 6 COMMAND ────────────────
                sessionToken?.let { parameters.put("session_token", it) }

                val payload = JSONObject()
                    .put("command", command)
                    .put("parameters", parameters)

                val encrypted = CryptoUtils.aesEncrypt(payload.toString(), aesKey)

                writer.println(JSONObject(encrypted).toString())

                // ──────────────── STEP 7 RESPONSE ────────────────
                val responseRaw = reader.readLine()
                Log.d(TAG, "RESPONSE RAW: $responseRaw")

                val response = responseRaw?.let {
                    val json = JSONObject(it)

                    CryptoUtils.aesDecrypt(
                        mapOf(
                            "nonce" to json.getString("nonce"),
                            "ciphertext" to json.getString("ciphertext")
                        ),
                        aesKey
                    )
                }

                Log.d(TAG, "RESPONSE = $response")
                Log.d(TAG, "FINAL RESPONSE = $response")  // add this line here


                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onResponse(response)
                }

            } catch (e: Exception) {
                Log.e(TAG, "SOCKET ERROR", e)

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onResponse("ERROR|${e.message}")
                }

            } finally {
                socket?.close()
                Log.d(TAG, "SOCKET CLOSED")
            }
        }
    }
}