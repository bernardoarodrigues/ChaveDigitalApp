package com.control.chavedigital;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OpenBdReceiver extends BroadcastReceiver {

    // Values
    private final int TIME_DIFFERENCE_DEVICE_AND_PHONE = 10815;

    // When Widget is pressed
    @Override
    public void onReceive(Context context, Intent intent) {
        // Gets user profile info
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Gets device info
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String device = prefs.getString("device", "");
        boolean block = prefs.getBoolean("blocked", false);

        // If any user signed in
        if(user != null) {
            // If any device saved
            if (!device.equals("")) {
                // If button is blocked, redirects to Biometry
                if(block) {
                    Intent intentMain = new Intent(context, Biometry.class);
                    intentMain.addCategory(Intent.CATEGORY_DEFAULT);
                    intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intentMain);
                }
                // If button isn't blocked, opens the door
                else {
                    open(device, context);
                }
            } else {
                Toast.makeText(context, "Nenhum dispositivo foi adicionado", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(context, "Nenhum usuário logado", Toast.LENGTH_SHORT).show();
        }
    }

    // Opens the door
    private void open(String device, Context context) {
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
                        Toast.makeText(context, R.string.portAb, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                Toast.makeText(context, R.string.dispOff, Toast.LENGTH_SHORT).show();
            }
        });
    }
}