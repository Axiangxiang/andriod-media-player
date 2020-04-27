package com.example.mediaplayerdemo

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import java.util.regex.Pattern

class SmsObserver(val handler: Handler, val context: Context) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        getValidateCode()
    }

    private fun getValidateCode() {
//        Telephony.Sms.CONTENT_URI
        val inboxUri = Uri.parse("content://sms/inbox")
        val selection = "${Telephony.Sms.ADDRESS}=?"
        val query = context.contentResolver.query(inboxUri, null, selection, arrayOf("18771020348"), "date desc")
        if (query != null) {
            if (query.moveToFirst()) {
                println("===address: ${query.getString(query.getColumnIndex("address"))}")
                val body = query.getString(query.getColumnIndex("body"))
                val pattern = Pattern.compile("(\\d{6})")
                val matcher = pattern.matcher(body)
                println("====body: $body")
                if (matcher.find()) {
                    val code = matcher.group(0)
                    println("====code: $code")
                    handler.obtainMessage(1, code).sendToTarget()
                }
            }
            query.close()
        }
    }


}