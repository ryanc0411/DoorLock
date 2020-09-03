package com.example.doorlock

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import kotlin.math.sqrt
import kotlin.system.exitProcess
import java.util.Timer
import kotlin.concurrent.schedule

class HomeActivity : AppCompatActivity() {
    private var sm: SensorManager? = null
    private var mp: MediaPlayer ?=null
    private lateinit var timer: CountDownTimer
    private lateinit var timer1: CountDownTimer
    private var acelVal = 0f
    private var acelLast = 0f
    private var shake = 0f
    private var count = 0
    private val doorMACADDRESSS = "PW5Y-9FTx-V9rE"


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
        mp!!.setVolume(1f,1f)

        val secondDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance("DoorLock"))
        secondDatabase.getReference("door").child(doorMACADDRESSS).child("currentLoginAttempt").setValue(0)


        timer1 =  object : CountDownTimer(6000,1000){
            override fun onFinish() {
                textView.text = "Door Locked Back!"
                FirebaseDatabase.getInstance()
                    .getReference("PI_07_CONTROL")
                    .child("relay")
                    .setValue("0")

                timer1.cancel()
                Timer("SettingUp", false).schedule(2000) {
                    exitProcess(-1)
                }


            }

            override fun onTick(p0: Long) {
                textView.text = "Open Count down: " + p0 /1000
            }

        }.start()

    }



    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            val secondDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance("DoorLock"))

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            acelLast = acelVal
            acelVal =
                sqrt((x * x).toDouble() + y * y + z * z).toFloat()
            val delta = acelVal - acelLast
            shake = shake * 0.9f + delta
            if (shake > 5 && count ==0) {
                timer1.cancel()
                imageView.visibility = View.GONE
                imageView2.visibility = View.VISIBLE
                count = 1
                mp!!.start()


                timer =  object : CountDownTimer(10000,1000){
                    override fun onFinish() {
                        textView.text = "Master, You Forgot to lock the door!"
                        FirebaseDatabase.getInstance()
                            .getReference("PI_07_CONTROL")
                            .child("buzzer")
                            .setValue("1")

                    }

                    override fun onTick(p0: Long) {
                        textView.text = "Close Count down: " + p0 /1000
                    }

                }.start()

            }
            else if(shake > 5 && count == 1)
            {
                FirebaseDatabase.getInstance()
                    .getReference("PI_07_CONTROL")
                    .child("buzzer")
                    .setValue("0")

                FirebaseDatabase.getInstance()
                    .getReference("PI_07_CONTROL")
                    .child("led")
                    .setValue("0")

                FirebaseDatabase.getInstance()
                    .getReference("PI_07_CONTROL")
                    .child("relay")
                    .setValue("0")

                count = 0
                imageView2.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                timer.cancel()
                textView.text = "Door Closed!"
               sm!!.unregisterListener(this)
                Timer("SettingUp", false).schedule(2000) {
                    exitProcess(-1)
                }


            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }

    }



}