package com.example.project_pemob_techie.ui.history

import android.os.AsyncTask
import android.util.Log
import org.slf4j.MDC.put
import java.io.File
import java.util.Properties
import javax.mail.*
import javax.mail.internet.*

class SendMailTask(
    private val senderEmail: String,
    private val appPassword: String,
    private val recipientEmail: String,
    private val subject: String,
    private val htmlContent: String, // The HTML body
    private val attachmentFile: File?, // The PDF attachment
    private val onSuccess: () -> Unit,
    private val onFailure: (Exception) -> Unit
) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void?): Boolean {
        return try {
            val properties = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, appPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                setSubject(subject)
                setContent(htmlContent, "text/html")
            }

            if (attachmentFile != null) {
                val multipart = MimeMultipart()
                val messageBodyPart = MimeBodyPart()
                messageBodyPart.setContent(htmlContent, "text/html")
                multipart.addBodyPart(messageBodyPart)

                val attachmentPart = MimeBodyPart()
                attachmentPart.attachFile(attachmentFile)
                multipart.addBodyPart(attachmentPart)

                message.setContent(multipart)
            }

            Transport.send(message)
            true
        } catch (e: Exception) {
            Log.e("SendMailTask", "Error sending email", e)
            false
        }
    }

    override fun onPostExecute(result: Boolean) {
        if (result) {
            onSuccess()
        } else {
            onFailure(Exception("Failed to send email"))
        }
    }
}
