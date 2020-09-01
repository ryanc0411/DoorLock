package com.example.doorlock


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.ActionMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    private val TAG = MainActivity::getLocalClassName.toString()

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricManager: BiometricManager
    private lateinit var sharedPreferences1: SharedPreferences
    var loginAttempt = 0
    var door = 0
    private val doorMACADDRESSS = "PW5Y-9FTx-V9rE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences1 = this.getSharedPreferences("Door",Activity.MODE_PRIVATE)
        door = sharedPreferences1.getInt("create",0)

        if(door == 0) {

            val options = FirebaseOptions.Builder()
                .setProjectId("doorlock-c8b93")
                .setApiKey("AIzaSyDQOcgPofryno-jiOPoAdSQXp-_jW3516s")
                .setApplicationId("com.example.doorlock")
                .setDatabaseUrl("https://doorlock-c8b93.firebaseio.com/")
                .build()

            FirebaseApp.initializeApp(this, options, "DoorLock");
            door ++;
        }
        val secondDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance("DoorLock"))
        secondDatabase.getReference("door").child(doorMACADDRESSS).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(ds: DataSnapshot) {
                loginAttempt = ds.child("currentLoginAttempt").getValue(Int::class.java)!!

            }

        })
            biometricManager = BiometricManager.from(this)
            val executor = ContextCompat.getMainExecutor(this)

            checkBiometricStatus(biometricManager)


            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        showToast("$errString")
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        if(loginAttempt < 3 ) {
                            goToHomeActivity()

                            val editor1: SharedPreferences.Editor = sharedPreferences1.edit()
                            editor1.putInt("create", door)
                            editor1.commit()

                        }
                        else
                        {

                            showToast("Use your Master APP to unlock the door")
                            FirebaseDatabase.getInstance()
                                .getReference("PI_07_CONTROL")
                                .child("buzzer")
                                .setValue("0")

                            FirebaseDatabase.getInstance()
                                .getReference("PI_07_CONTROL")
                                .child("led")
                                .setValue("0")
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        showToast("Authentication Failed")

                        loginAttempt++
                        if(loginAttempt < 4 ){
                            secondDatabase.getReference("door").child(doorMACADDRESSS).child("currentLoginAttempt").setValue(loginAttempt)
                        }
                        else{
                            showToast("Use your Master APP to unlock the door")
                        }

                        if (loginAttempt >= 3) {
                            FirebaseDatabase.getInstance()
                                .getReference("PI_07_CONTROL")
                                .child("buzzer")
                                .setValue("1")

                            FirebaseDatabase.getInstance()
                                .getReference("PI_07_CONTROL")
                                .child("led")
                                .setValue("1")

                            FirebaseDatabase.getInstance()
                                .getReference("PI_07_CONTROL")
                                .child("camera")
                                .setValue("1")

                            object : CountDownTimer(30000, 1000) {
                                override fun onFinish() {
                                    FirebaseDatabase.getInstance()
                                        .getReference("PI_07_CONTROL")
                                        .child("camera")
                                        .setValue("0")
                                }

                                override fun onTick(p0: Long) {

                                }

                            }.start()

                        }
                    }
                })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login for open the Door")
                .setDescription("Use device biometric feature for open the door")
                .setNegativeButtonText("Cancel")
                .build()



            login.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }


    }


    private fun showToast(message : String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    private fun goToHomeActivity(){
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

    }


    private fun checkBiometricStatus(biometricManager: BiometricManager){
        when(biometricManager.canAuthenticate()){
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d(TAG,"checkBiometricStatus: App can use biometric authenticate")

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e(TAG,"checkBiometricStatus: No biometric features available in this devices")

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e(TAG,"checkBiometricStatus: Biometric features currently unavailable")

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.e(TAG,"checkBiometricStatus: The user has'nt enrolled with any biometric configuration" +
                "in this device")



                    }
        }

    override fun onStart() {
        super.onStart()
        val editor1: SharedPreferences.Editor = sharedPreferences1.edit()
        editor1.putInt("create", 0)
        editor1.commit()

    }


}
