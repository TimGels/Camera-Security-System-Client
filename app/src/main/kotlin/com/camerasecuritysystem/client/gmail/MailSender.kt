package com.camerasecuritysystem.client.gmail

import android.util.Log
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.MailjetResponse
import com.mailjet.client.resource.Emailv31

import org.json.JSONObject

import org.json.JSONArray


class MailSender {



    var client: MailjetClient? = MailjetClient(
        "ea9e024e06334a62fe835625e11ba1de",
        "2dcac4348fb3c01a48af4e6f9fe8a35f",
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
                                        .put("Email", "jochembrans@gmail.com")
                                        .put("Name", "Jochem")
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

    var response: MailjetResponse? = null

    fun sendEmail() {
        try {
            response = client?.post(request)
            Log.e("MAIL", response?.status.toString())
            Log.e("MAIL", response?.data.toString())
        } catch (e: Exception) {
            Log.e("MAIL EXC", e.toString())
        }

    }
}