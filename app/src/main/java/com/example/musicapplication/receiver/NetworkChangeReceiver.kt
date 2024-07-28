package com.example.musicapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast

class NetworkChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (!isOnline(context)) {
            Toast.makeText(context, "Network is unavailable", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Network is online", Toast.LENGTH_LONG).show()
        }
    }

    fun isOnline(context: Context?): Boolean {
        context ?: return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        //should check null because in airplane mode it will be a null
        return netInfo != null && netInfo.isConnected
    }
}