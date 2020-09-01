package com.example.doorlock

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MyfirebaseMessagingService : FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        Log.d("TAG","The token refreshed: $token")

    }


}