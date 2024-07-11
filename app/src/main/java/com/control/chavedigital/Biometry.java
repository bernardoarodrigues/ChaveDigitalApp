package com.control.chavedigital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Biometry extends AppCompatActivity {

    // Variable declaration

    SharedPreferences prefs = null;
    private BiometricPrompt biometricPrompt = null;
    private final Executor executor = Executors.newSingleThreadExecutor();
    Context context = null;

    // Values
    String device = "";
    private final int TIME_DIFFERENCE_DEVICE_AND_PHONE = 10815;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometry);

        // Checks if any user signed
        // APIs and others
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        // Variable definition
        context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    // When the activity starts
    public void onStart() {
        super.onStart();

        // Creates biometric prompt
        if(biometricPrompt == null){
            biometricPrompt = new BiometricPrompt(this,executor,callback);
        }

        // Gets device from SharedPreferences
        device = prefs.getString("device", "");

        // Calls BiometricPrompt
        BiometricPrompt.PromptInfo promptInfo = buildPrompt();
        biometricPrompt.authenticate(promptInfo);
    }

    // Creates BiometricPrompt
    private BiometricPrompt.PromptInfo buildPrompt()
    {
        BiometricManager biometricManager = BiometricManager.from(this);
        boolean allowed;
        allowed = biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;

        if(allowed) {
            return new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getResources().getString(R.string.biometricTitle))
                    .setDescription(getResources().getString(R.string.biometricDesc))
                    .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build();
        }
        else {
            return new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getResources().getString(R.string.biometricTitle))
                    .setDescription(getResources().getString(R.string.biometricDesc))
                    .setNegativeButtonText(getResources().getString(R.string.cancel))
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build();
        }
    }

    // BiometricPrompt Callback
    private final BiometricPrompt.AuthenticationCallback callback =
    new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            runOnUiThread(() -> {
                // If any error or user cancel
                if(biometricPrompt != null) biometricPrompt.cancelAuthentication();

                moveTaskToBack(true);
                finish();
            });
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            // If success
            runOnUiThread(() -> open());
        }

        @Override
        public void onAuthenticationFailed() {
        }
    };

    // Opens the door
    private void open() {
        new FirebaseAction().onceDevice(device, deviceData -> {
            // If device isn't configured to the internet
            if(deviceData.config.equals("no")) {
                Toast.makeText(context, R.string.notConfig, Toast.LENGTH_SHORT).show();
                return;
            }

            // Checks if device is online
            long scEsp = Long.parseLong(deviceData.lasttime);
            long scCel = System.currentTimeMillis()/1000;
            boolean online = scCel - scEsp <= TIME_DIFFERENCE_DEVICE_AND_PHONE;

            // If the device is online
            if(online) {
                // Sets device state -> "1"
                new FirebaseAction().setDeviceChild(device, "state", "1", error -> {
                    if(error == null) {
                        Toast.makeText(this, R.string.portAb, Toast.LENGTH_SHORT).show();

                        // Closes activity
                        runOnUiThread(() -> {
                            moveTaskToBack(true);
                            finish();
                        });
                    }
                });
            }
            else {
                Toast.makeText(context, R.string.dispOff, Toast.LENGTH_SHORT).show();

                // Closes activity
                runOnUiThread(() -> {
                    moveTaskToBack(true);
                    finish();
                });
            }
        });
    }

    // When back button is pressed
    @Override
    public void onBackPressed() {
        // Closes activity
        runOnUiThread(() -> {
            moveTaskToBack(true);
            finish();
        });
    }
}