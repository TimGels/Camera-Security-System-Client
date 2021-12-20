package com.camerasecuritysystem.client.gmail

import android.content.Context
import android.util.Log
import com.camerasecuritysystem.client.KeyStoreHelper
import com.camerasecuritysystem.client.R
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.MailjetResponse
import com.mailjet.client.resource.Emailv31

import org.json.JSONObject

import org.json.JSONArray


class MailSender(val context: Context) {

    //Get access to all shared preferences
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
    );
    var request: MailjetRequest? = MailjetRequest(Emailv31.resource)
        .property(
            Emailv31.MESSAGES, JSONArray()
                .put(
                    JSONObject()
                        .put(
                            Emailv31.Message.FROM, JSONObject()
                                .put("Email", "jochembrans@gmail.com")
                                .put("Name", "CameraSecuritySystem")
                        )
                        .put(
                            Emailv31.Message.TO, JSONArray()
                                .put(
                                    JSONObject()
                                        .put("Email", getEmail())
                                        .put("Name", "CSS user")
                                )
                        )
                        .put(Emailv31.Message.SUBJECT, "About your camera.")
                        .put(Emailv31.Message.TEXTPART, "My first Mailjet email")
                        .put(
                            Emailv31.Message.HTMLPART,
                            "<h3>A notification about your camera</h3><br />This is a test email"
                        )
                        .put(Emailv31.Message.CUSTOMID, "AppGettingStartedTest")
                )
        )

    private fun getEmail(): String? {
        return sharedPreferences.getString(context.resources.getString(R.string.email), null)
    }

    fun sendEmail() {
        try {
            response = client?.post(request)
            Log.e("MAIL", response?.status.toString())
            Log.e("MAIL", response?.data.toString())
        } catch (e: Exception) {
            Log.e("MAIL EXC", e.toString())
        }

    }

    private fun getKey(): String? {
        val keyBytes =
            sharedPreferences.getString(context.resources.getString(R.string.mailKeyIVByte), null)
        val keyEnc =
            sharedPreferences.getString(context.resources.getString(R.string.mail_key), null)

        return keyStoreHelper.decryptData(
            keyBytes!!.toByteArray(Charsets.ISO_8859_1),
            keyEnc!!.toByteArray(Charsets.ISO_8859_1)
        )
    }

    private fun getSecret() : String?{
        val secretBytes =
            sharedPreferences.getString(context.resources.getString(R.string.secretIVByte), null)
        val secretEnc =
            sharedPreferences.getString(context.resources.getString(R.string.mail_secret), null)

        return keyStoreHelper.decryptData(
            secretBytes!!.toByteArray(Charsets.ISO_8859_1),
            secretEnc!!.toByteArray(Charsets.ISO_8859_1)
        )
    }


}