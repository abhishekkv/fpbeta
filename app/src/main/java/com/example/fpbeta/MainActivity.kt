package com.example.fpbeta


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Create a thread pool with a single thread//
        val newExecutor: Executor = Executors.newSingleThreadExecutor()
        val activity: FragmentActivity = this
        //Start listening for authentication events//
        val myBiometricPrompt = BiometricPrompt(activity, newExecutor, object : BiometricPrompt.AuthenticationCallback() {
            //onAuthenticationError is called when a fatal error occurrs//
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                } else { //Print a message to Logcat//
                    Log.d(TAG, "An unrecoverable error occurred")
                }
            }

            //onAuthenticationSucceeded is called when a fingerprint is matched successfully//
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                //Print a message to Logcat//
                Log.d(TAG, "Fingerprint recognised successfully")




                startActivity(Intent(baseContext, Main2Activity::class.java))
         //       Start the second activity for next things
            }

            //onAuthenticationFailed is called when the fingerprint doesn’t match//
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                //Print a message to Logcat//
                Log.d(TAG, "Fingerprint not recognised")
            }
        })
        //Create the BiometricPrompt instance//
        val promptInfo = PromptInfo.Builder() //Add some text to the dialog//
                .setTitle("Title text goes here")
                .setSubtitle("Subtitle goes here")
                .setDescription("This is the description")
                .setNegativeButtonText("Cancel") //Build the dialog//
                .build()
        //Assign an onClickListener to the app’s “Authentication” button//
        findViewById<View>(R.id.launchAuthentication).setOnClickListener { myBiometricPrompt.authenticate(promptInfo) }
    }


    companion object {
        val TAG = MainActivity::class.java.name
    }
}