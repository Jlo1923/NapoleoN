package com.naposystems.pepito.utility

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.ui.enterPin.EnterPinFragmentDirections

class BiometricsUtils {


//    private fun showBiometricPrompt() {
//
//        val biometricsManager = BiometricManager.from(context!!)
//        val executor = ContextCompat.getMainExecutor(context)
//
//        biometricPrompt = BiometricPrompt(this, executor,
//            object : BiometricPrompt.AuthenticationCallback(){
//                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                    super.onAuthenticationError(errorCode, errString)
//                    Toast.makeText(context,"Authentication error: $errString", Toast.LENGTH_SHORT)
//                        .show()
//                }
//
//                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                    super.onAuthenticationSucceeded(result)
//
//                    Toast.makeText(context,"Authentication Success: $result", Toast.LENGTH_SHORT)
//                        .show()
//                    findNavController().navigate(
//                        EnterPinFragmentDirections.actionEnterPinFragmentToHomeFragment()
//                    )
//                }
//
//                override fun onAuthenticationFailed() {
//                    super.onAuthenticationFailed()
//                    Toast.makeText(context, "Authentication failed",
//                        Toast.LENGTH_SHORT)
//                        .show()
//                }
//            })
//        promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Biometric login for my app")
//            .setSubtitle("Log in using your biometric credential")
//            .setConfirmationRequired(false)
//            .setNegativeButtonText("Cancel")
//            .build()
//
//        biometricPrompt.authenticate(promptInfo)
//    }
}