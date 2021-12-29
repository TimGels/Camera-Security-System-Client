package com.camerasecuritysystem.client.mailjet

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.camerasecuritysystem.client.KeyStoreHelper
import com.camerasecuritysystem.client.R
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.MailjetResponse
import com.mailjet.client.resource.Emailv31
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MailSender(val context: Context) {

    // Get access to all shared preferences
    val sharedPreferences = context.getSharedPreferences(
        "com.camerasecuritysystem.client",
        Context.MODE_PRIVATE
    )

    var response: MailjetResponse? = null

    val keyStoreHelper = KeyStoreHelper(context.resources.getString(R.string.keyStoreAliasMail))

    var client: MailjetClient? = MailjetClient(
        getKey(),
        getSecret(),
        ClientOptions("v3.1")
    )
    var request: MailjetRequest? = MailjetRequest(Emailv31.resource)
        .property(
            Emailv31.MESSAGES,
            JSONArray()
                .put(
                    JSONObject()
                        .put(
                            Emailv31.Message.FROM,
                            JSONObject()
                                .put("Email", getFromEmail())
                                .put("Name", "CameraSecuritySystem")
                        )
                        .put(
                            Emailv31.Message.TO,
                            JSONArray()
                                .put(
                                    JSONObject()
                                        .put("Email", getToEmail())
                                        .put("Name", "CSS user")
                                )
                        )
                        .put(Emailv31.Message.SUBJECT, "About your camera recording.")
                        .put(Emailv31.Message.TEXTPART, "New recording")
                        .put(
                            Emailv31.Message.HTMLPART,
                            """<h3>A new recording has been saved on your device.</h3><br />
                                Device: ${android.os.Build.DEVICE}<br/>
                                Model: ${android.os.Build.MODEL}<br/>
                                Brand: ${android.os.Build.BRAND}<br/>
                            """.trimMargin()
                        )
                        .put(Emailv31.Message.CUSTOMID, "AppGettingStartedTest")
                )
        )

    private fun getToEmail(): String? {
        return sharedPreferences.getString(context.resources.getString(R.string.toEmail), null)
    }

    private fun getFromEmail(): String? {
        return sharedPreferences.getString(context.resources.getString(R.string.fromEmail), null)
    }

    fun sendEmail() {
        try {
            if (getKey() !== null &&
                getSecret() !== null &&
                getFromEmail() !== null &&
                getToEmail() !== null
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    response = client?.post(request)
                    Log.e("MAIL", response?.status.toString())
                    Log.e("MAIL", response?.data.toString())
                }
            }
        } catch (e: Exception) {
            Log.e("MAIL EXC", e.toString())
            Toast.makeText(context, "MailJet not configured correctly", Toast.LENGTH_LONG).show()
        }
    }

    private fun getKey(): String? {
        val keyBytes =
            sharedPreferences.getString(context.resources.getString(R.string.mailKeyIVByte), null)
        val keyEnc =
            sharedPreferences.getString(context.resources.getString(R.string.mail_key), null)

        if (keyBytes !== null && keyEnc !== null) {
            return keyStoreHelper.decryptData(
                keyBytes.toByteArray(Charsets.ISO_8859_1),
                keyEnc.toByteArray(Charsets.ISO_8859_1)
            )
        }
        return null
    }

    private fun getSecret(): String? {
        val secretBytes =
            sharedPreferences.getString(context.resources.getString(R.string.secretIVByte), null)
        val secretEnc =
            sharedPreferences.getString(context.resources.getString(R.string.mail_secret), null)

        if (secretBytes !== null && secretEnc !== null) {
            return keyStoreHelper.decryptData(
                secretBytes.toByteArray(Charsets.ISO_8859_1),
                secretEnc.toByteArray(Charsets.ISO_8859_1)
            )
        }
        return null
    }
}
