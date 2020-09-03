package com.example.doorlock

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hanks.passcodeview.PasscodeView
import com.hanks.passcodeview.PasscodeView.PasscodeViewListener
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class PinLoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var passcodeView: PasscodeView
    var loginAttempt = 0
    private val doorMACADDRESSS = "PW5Y-9FTx-V9rE"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        val secondDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance("DoorLock"))

        secondDatabase.getReference("door")
            .child(doorMACADDRESSS)
            .child("currentLoginAttempt")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("No")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    loginAttempt = snapshot.getValue(Int::class.java)!!.toInt()
                }


            })

        sharedPreferences = this.getSharedPreferences("PinNumber", Activity.MODE_PRIVATE)
        var pinNum = sharedPreferences.getString("PIN","").toString()
        passcodeView = findViewById(R.id.passcode_view)

        passcodeView.setLocalPasscode(pinNum).listener = object : PasscodeViewListener {
            override fun onFail() {
//                when (loginAttempt) {
//                    0 -> {
//                        Toast.makeText(applicationContext,"Password is Wrong! Attempt Remaining : 2", Toast.LENGTH_SHORT).show()
//                    }
//                    1 -> {
//                        Toast.makeText(applicationContext,"Password is Wrong! Attempt Remaining: 1", Toast.LENGTH_SHORT).show()
//                    }
//                    2 -> {
//                        Toast.makeText(applicationContext,"Password is Wrong! Attempt Remaining: 0", Toast.LENGTH_SHORT).show()
//                        sendNotification1()
//                        secondDatabase.getReference("door").child(doorMACADDRESSS).child("BlockStatus").setValue(1)
//
//                        FirebaseDatabase.getInstance()
//                            .getReference("PI_07_CONTROL")
//                            .child("buzzer")
//                            .setValue("1")
//
//                        FirebaseDatabase.getInstance()
//                            .getReference("PI_07_CONTROL")
//                            .child("led")
//                            .setValue("1")
//
//                        FirebaseDatabase.getInstance()
//                            .getReference("PI_07_CONTROL")
//                            .child("camera")
//                            .setValue("1")
//
//                        object : CountDownTimer(30000, 1000) {
//                            override fun onFinish() {
//                                FirebaseDatabase.getInstance()
//                                    .getReference("PI_07_CONTROL")
//                                    .child("camera")
//                                    .setValue("0")
//                            }
//
//                            override fun onTick(p0: Long) {
//
//                            }
//
//                        }.start()
//                    }
//                    else -> {
//                        Toast.makeText(applicationContext,"Your Acc have been BLOCKED!", Toast.LENGTH_SHORT).show()
//                        goToMainActivity()
//
//                    }
//                }
//                loginAttempt ++
//                Toast.makeText(applicationContext,"Your Acc have been BLOCKED!", Toast.LENGTH_SHORT).show()
//                secondDatabase.getReference("door").child(doorMACADDRESSS).child("currentLoginAttempt").setValue(loginAttempt)

           }
            override fun onSuccess(number: String?) {
                if(loginAttempt<3)
                    goToHomeActivity()
                else
                    Toast.makeText(applicationContext,"Your Acc have been BLOCKED!", Toast.LENGTH_SHORT).show()

            }

        }


    }

    private fun goToHomeActivity(){
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

    }

    private fun goToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

    }

    private fun sendNotification1() {
        AsyncTask.execute {
            val SDK_INT = Build.VERSION.SDK_INT
            if (SDK_INT > 8) {
                val policy =
                    StrictMode.ThreadPolicy.Builder()
                        .permitAll().build()
                StrictMode.setThreadPolicy(policy)

                //This is a Simple Logic to Send Notification different Device Programmatically....
                val send_email: String = "PW5Y-9FTx-V9rE-1"
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