package com.example.doorlock

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlin.math.sqrt

class HomeActivity : AppCompatActivity() {
    private var sm: SensorManager? = null
    private var mp: MediaPlayer ?=null
    private var acelVal = 0f
    private var acelLast = 0f
    private var shake = 0f
    private var count = 0;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm!!.registerListener(
            sensorListener,
            sm!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        acelVal = SensorManager.GRAVITY_EARTH
        acelLast = SensorManager.GRAVITY_EARTH
        shake = 0.00f
        count = 0
        mp  = MediaPlayer.create(this,R.raw.welcome)
        mp!!.isLooping = false
        mp!!.setVolume(0.8f,0.8f)



    }



    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            acelLast = acelVal
            acelVal =
                sqrt((x * x).toDouble() + y * y + z * z).toFloat()
            val delta = acelVal - acelLast
            shake = shake * 0.9f + delta
            if (shake > 5 && count ==0) {

                imageView.visibility = View.GONE
                imageView2.visibility = View.VISIBLE
                count = 1
                mp!!.start()
            }
            else if(shake > 5 && count == 1)
            {
                count = 0
                imageView2.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                goToMainActivity()
               sm!!.unregisterListener(this)

            }
        }

        override fun onAccuracyChanged(
            sensor: Sensor,
            accuracy: Int
        ) {
        }
    }
    private fun goToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }



}