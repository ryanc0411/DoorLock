package com.example.doorlock


import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
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
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_main.*
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity(){

    private val TAG = MainActivity::getLocalClassName.toString()


    private lateinit var sharedPreferences: SharedPreferences
    var LoggedIn_User_Email = "PW5Y-9FTx-V9rE-1"
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricManager: BiometricManager
    var pinNum = ""
    var loginAttempt = 0
    private val doorMACADDRESSS = "PW5Y-9FTx-V9rE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = this.getSharedPreferences("PinNumber", Activity.MODE_PRIVATE)

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        OneSignal.sendTag("User_ID","PW5Y-9FTx-V9rE-1")

            val options = FirebaseOptions.Builder()
                .setProjectId("doorlock-c8b93")
                .setApiKey("AIzaSyDQOcgPofryno-jiOPoAdSQXp-_jW3516s")
                .setApplicationId("com.example.doorlock")
                .setDatabaseUrl("https://doorlock-c8b93.firebaseio.com/")
                .build()

            FirebaseApp.initializeApp(this, options, "DoorLock");



        FirebaseDatabase.getInstance()
            .getReference("PI_07_CONTROL")
            .child("relay")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.getValue(String::class.java)=="1")
                    {
                        goToHomeActivity()
                    }
                }

            })

        val secondDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance("DoorLock"))

        secondDatabase.getReference("door")
            .child(doorMACADDRESSS)
            .child("pinNumber")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val editor1: SharedPreferences.Editor = sharedPreferences.edit()
                    editor1.putString("PIN", snapshot.getValue(String::class.java).toString() )
                    editor1.apply()

                }

            })

        secondDatabase.getReference("door")
            .child(doorMACADDRESSS)
            .child("currentLoginAttempt")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    loginAttempt = snapshot.getValue(Int::class.java)!!.toInt()
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

                            FirebaseDatabase.getInstance()
                                .getReference("PI_07_CONTROL")
                                .child("relay")
                                .setValue("1")

                        }
                        else
                        {
                            showToast("You used your DoorLock MaterAPP to Blocked your dock! Use your MasterAPP to unblock it =)")
                            sendNotification1()
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

                            sendNotification()

                            secondDatabase.getReference("door").child(doorMACADDRESSS).child("BlockStatus").setValue(1)

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

        pinLogin.setOnClickListener {
            gotoPinLoginActivity()
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

    private fun gotoPinLoginActivity(){
        val intent = Intent(this, PinLoginActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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


    private fun sendNotification() {
        AsyncTask.execute {
            val SDK_INT = Build.VERSION.SDK_INT
            if (SDK_INT > 8) {
                val policy =
                    StrictMode.ThreadPolicy.Builder()
                        .permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val send_email: String

                //This is a Simple Logic to Send Notification different Device Programmatically....
                send_email = if (LoggedIn_User_Email.equals("PW5Y-9FTx-V9rE-1")) {
                    "PW5Y-9FTx-V9rE-2"
                } else {
                    "PW5Y-9FTx-V9rE-1"
                }
                try {
                    val jsonResponse: String
                    val url = URL("https://onesignal.com/api/v1/notifications")
                    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                    con.useCaches = false
                    con.doOutput = true
                    con.doInput = true
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    con.setRequestProperty("Authorization", "Basic NjAxZTc4YTQtNTM5NS00MDM1LThjOGUtYzAzOWVmYThkMTcz"
                    )
                    con.requestMethod = "POST"
                    val strJsonBody = ("{"
                            + "\"app_id\": \"fc13ef57-ca11-497a-b114-a9ee37089691\","
                            + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"
                            + "\"data\": {\"foo\": \"bar\"},"
                            + "\"contents\": {\"en\": \"Someone are tried to open your Door! Watch up !\"}"
                            + "}")
                    println("strJsonBody:\n$strJsonBody")
                    val sendBytes = strJsonBody.toByteArray(charset("UTF-8"))
                    con.setFixedLengthStreamingMode(sendBytes.size)
                    val outputStream: OutputStream = con.outputStream
                    outputStream.write(sendBytes)
                    val httpResponse: Int = con.responseCode
                    println("httpResponse: $httpResponse")
                    if (httpResponse >= HttpURLConnection.HTTP_OK
                        && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST
                    ) {
                        val scanner = Scanner(con.inputStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    } else {
                        val scanner = Scanner(con.errorStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    }
                    println("jsonResponse:\n$jsonResponse")
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    private fun sendNotification1() {
        AsyncTask.execute {
            val SDK_INT = Build.VERSION.SDK_INT
            if (SDK_INT > 8) {
                val policy =
                    StrictMode.ThreadPolicy.Builder()
                        .permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val send_email: String

                //This is a Simple Logic to Send Notification different Device Programmatically....
                send_email = if (LoggedIn_User_Email.equals("PW5Y-9FTx-V9rE-1")) {
                    "PW5Y-9FTx-V9rE-2"
                } else {
                    "PW5Y-9FTx-V9rE-1"
                }
                try {
                    val jsonResponse: String
                    val url = URL("https://onesignal.com/api/v1/notifications")
                    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                    con.useCaches = false
                    con.doOutput = true
                    con.doInput = true
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    con.setRequestProperty("Authorization", "Basic NjAxZTc4YTQtNTM5NS00MDM1LThjOGUtYzAzOWVmYThkMTcz"
                    )
                    con.requestMethod = "POST"
                    val strJsonBody = ("{"
                            + "\"app_id\": \"fc13ef57-ca11-497a-b114-a9ee37089691\","
                            + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"
                            + "\"data\": {\"foo\": \"bar\"},"
                            + "\"contents\": {\"en\": \"You used your DoorLock MaterAPP to Blocked your dock! Use your MasterAPP to unblock it =)\"}"
                            + "}")
                    println("strJsonBody:\n$strJsonBody")
                    val sendBytes = strJsonBody.toByteArray(charset("UTF-8"))
                    con.setFixedLengthStreamingMode(sendBytes.size)
                    val outputStream: OutputStream = con.outputStream
                    outputStream.write(sendBytes)
                    val httpResponse: Int = con.responseCode
                    println("httpResponse: $httpResponse")
                    if (httpResponse >= HttpURLConnection.HTTP_OK
                        && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST
                    ) {
                        val scanner = Scanner(con.inputStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    } else {
                        val scanner = Scanner(con.errorStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    }
                    println("jsonResponse:\n$jsonResponse")
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }


}
