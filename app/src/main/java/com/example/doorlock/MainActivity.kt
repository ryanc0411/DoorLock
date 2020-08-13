package com.example.doorlock


import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    private val TAG = MainActivity::getLocalClassName.toString()

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricManager: BiometricManager
    var loginAttempt = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("Door",Activity.MODE_PRIVATE)
        loginAttempt = sharedPreferences.getInt("loginAttempts",0)
        loginAttempt =1
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
                        if(loginAttempt < 4) {
                            goToHomeActivity()
                        }
                        else
                        {
                            showToast("Alarm")
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        showToast("Authentication Failed")
                        loginAttempt++;
                        val editor: SharedPreferences.Editor =  sharedPreferences.edit()
                        editor.putInt("loginAttempts", loginAttempt)
                        editor.commit()
                        if(loginAttempt > 3)
                        {
                            unlock.visibility = View.VISIBLE
                            showToast("Alarm")
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


        unlock.setOnClickListener{
            goToUnlockActivity()
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

    private fun goToUnlockActivity(){
        val intent = Intent(this, UnlockActivity::class.java)
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



}
