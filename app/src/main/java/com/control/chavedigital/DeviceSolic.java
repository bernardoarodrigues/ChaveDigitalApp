package com.control.chavedigital;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class DeviceSolic extends Fragment {

    // Variable declaration

    // APIs and others
    ValueEventListener deviceListener = null;

    // Views
    LinearLayout contSolic, anySolic;
    LayoutInflater userInflater;
    CoordinatorLayout coordinatorLayout;

    // Values
    String device = "";

    // Constructor
    public DeviceSolic() {
    }

    // When the fragment is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // When the fragment's view is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_solic, container, false);

        // Variable definition
        contSolic = view.findViewById(R.id.contSolic);
        userInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        coordinatorLayout = view.findViewById(R.id.coordLayout_solic);
        anySolic = view.findViewById(R.id.anySolic);
        return view;
    }

    // When the fragment starts
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onStart() {
        super.onStart();
        // Gets user device
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        device = prefs.getString("device", "");

        // Starts getting users from device
        deviceListener = new FirebaseAction().onDeviceChild(device, "solic", deviceChildData -> {
            if(!isAdded()) return;
            contSolic.removeAllViews();
            listSolic(deviceChildData);
        });
    }

    // Lists solicitations
    private void listSolic(String solicStr) {
        // If no solic
        if(solicStr.equals("")) {
            // Shows any solic view
            anySolic.setVisibility(View.VISIBLE);
        }
        else anySolic.setVisibility(View.GONE);

        // Split solic
        String[] solic = solicStr.split("/");

        // Creates first divider
        View dv = new View(requireActivity().getApplicationContext());
        int i = 0;
        dv.setId(i + 1);
        ConstraintLayout.LayoutParams paramsDv = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 1);
        dv.setLayoutParams(paramsDv);
        dv.setBackgroundResource(R.color.lt_grey);
        contSolic.addView(dv);

        // Loops through solicitations
        for (String solicId: solic) {
            // Skips if solicId == ""
            if(solicId.equals("")) continue;

            // Inflates solic view
            View solicItem = userInflater.inflate(R.layout.user_item, null);
            contSolic.addView(solicItem);

            // Declares views
            CircleImageView photoSolic = solicItem.findViewById(R.id.userPhoto);
            TextView nameSolic = solicItem.findViewById(R.id.userName);
            CircleImageView acceptSolic = solicItem.findViewById(R.id.userAccept);
            CircleImageView rmvSolic = solicItem.findViewById(R.id.userRemove);

            // Gets user data
            new FirebaseAction().onceUser(solicId, userData -> {
                // Adds user photo
                Glide.with(requireActivity().getApplicationContext()).load(userData.photo).into(photoSolic);

                // Adds name and buttons
                nameSolic.setText(userData.name);
                acceptSolic.setVisibility(View.VISIBLE);
                acceptSolic.setOnClickListener(v -> acceptSolic(solicStr, solicId));
                rmvSolic.setVisibility(View.VISIBLE);
                rmvSolic.setOnClickListener(v -> removeSolic(solicStr, solicId));
            });
        }
    }

    // AcceptSolic Handler
    private void acceptSolic(String solicStr, String solicId) {
        // Gets device data
        new FirebaseAction().onceDevice(device, deviceData -> {
            if(deviceData != null) {
                // Adds device to new user
                new FirebaseAction().setUserChild(solicId, "device", device, err2 -> {
                    // Adds new user to device
                    new FirebaseAction().setDeviceChild(device, "users", deviceData.users + solicId + "/", error -> {
                        if (error == null) {
                            // Gets new solic string
                            String newSolic = solicStr.replace(solicId, "");
                            if(newSolic.equals("/")) { newSolic = ""; }
                            else if(newSolic.equals("//")) { newSolic = ""; }
                            else { newSolic = newSolic.replace("//", "/"); }

                            // Removes solic from device
                            new FirebaseAction().setDeviceChild(device, "solic", newSolic, error2 -> {
                                if (error2 == null) {
                                    // Removes solic from user data
                                    new FirebaseAction().setUserChild(solicId, "solic", "", err -> {
                                        // Confirms operation
                                        if (err == null)
                                            Snackbar.make(coordinatorLayout, R.string.solicac, Snackbar.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        }
                    });
                });
            }
        });
    }

    // RemoveSolic Handler
    private void removeSolic(String solicStr, String solicId) {
        // Asks admin confirmation
        Snackbar temcert = Snackbar.make(coordinatorLayout, R.string.temcert, Snackbar.LENGTH_LONG);
        temcert.setAction(R.string.deletar, v -> {
            // Dismisses Snackbar
            temcert.dismiss();

            // Removes solic from user data
            new FirebaseAction().setUserChild(solicId, "solic", "", err -> {
                // Gets new solic string
                String newSolic = solicStr.replace(solicId, "");
                if(newSolic.equals("/")) { newSolic = ""; }
                else if(newSolic.equals("//")) { newSolic = ""; }
                else { newSolic = newSolic.replace("//", "/"); }

                // Removes solic from device
                new FirebaseAction().setDeviceChild(device, "solic", newSolic, error -> {
                    if(error == null) {
                        // Confirms operation
                        if(err == null) Snackbar.make(coordinatorLayout, R.string.solicrm, Snackbar.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // Shows Snackbar
        temcert.show();
    }

    // When the fragment is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Removes listener
        if(deviceListener != null) {
            new FirebaseAction().offDeviceChild(device, "users", deviceListener);
            deviceListener = null;
        }
    }
}