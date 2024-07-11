package com.control.chavedigital;

// Imports

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthUser extends AppCompatActivity {

    // Variable declaration

    // APIs and others
    private FirebaseAuth mAuth;
    private UserModel userData;

    // Views
    TextView waitAuth, descAuth;
    LottieAnimationView animLoad, animStt;
    MaterialButton nextButton;

    // Values
    private int animEnt, animExt;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authuser);

        // Checks if any user signed
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), SignIn.class);
            startActivity(intent);
            animEnt = 0;
            animExt = 0;
            finish();
        }

        // Variable definition
        waitAuth = findViewById(R.id.waitAuth);
        descAuth = findViewById(R.id.descAuth);
        animLoad = findViewById(R.id.animLoad_authUser);
        animStt = findViewById(R.id.animStt);
        nextButton = findViewById(R.id.nextAuth);
    }

    // When the activity starts
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onStart() {
        super.onStart();

        // Gets user profile data
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;

        // Gets user data
        new FirebaseAction().onceUser(user.getUid(), userInfo -> {
            if (userInfo != null) {
                userData = userInfo;

                // If user already added a device
                if(!userData.device.equals("")) {
                    // Updates UI
                    waitAuth.setText(R.string.pronto);
                    animLoad.setVisibility(View.GONE);
                    animStt.setVisibility(View.VISIBLE);
                    animStt.playAnimation();
                    descAuth.setText(R.string.alrHaveDev);
                    nextButton.setVisibility(View.VISIBLE);

                    // Sets next button onClickListener
                    nextButton.setOnClickListener(v -> {
                        // Redirects to Home
                        Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                        startActivity(homeIntent);
                        animEnt = R.anim.slide_in_right;
                        animExt = R.anim.slide_out_left;
                        finish();
                    });
                }
                // Checks link
                else verifyIds();
            }
        });
    }

    // Verifies if ids provided are valid
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void verifyIds() {
        // Current intent
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        // If any data
        if(action.equals(Intent.ACTION_VIEW) && data != null) {
            // Gets user profile data
            FirebaseUser user = mAuth.getCurrentUser();
            assert user != null;

            // Gets data from intent and decodes it
            String device = Encryption.decode(data.getQueryParameter("d"));
            String admin = Encryption.decode(data.getQueryParameter("a"));

            // Gets device data
            new FirebaseAction().onceDevice(device, deviceData -> {
                if(deviceData == null) {
                    setError(getResources().getString(R.string.error), getResources().getString(R.string.invLink));
                    return;
                }
                if(!deviceData.admin.equals(admin)) {
                    setError(getResources().getString(R.string.error), getResources().getString(R.string.invLink));
                    return;
                }

                // Executed after 2 seconds
                final Handler h = new Handler();
                h.postDelayed(() -> {
                    // If user was already added to the device
                    if (deviceData.users.contains(user.getUid())) {
                        // Updates UI
                        waitAuth.setText(R.string.pronto);
                        animLoad.setVisibility(View.GONE);
                        animStt.setVisibility(View.VISIBLE);
                        animStt.playAnimation();
                        descAuth.setText(R.string.alrHave);
                        nextButton.setVisibility(View.VISIBLE);

                        // Sets next button onClickListener
                        nextButton.setOnClickListener(v -> {
                            // Redirects to Home
                            Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                            startActivity(homeIntent);
                            animEnt = R.anim.slide_in_right;
                            animExt = R.anim.slide_out_left;
                            finish();
                        });
                    }
                    // If solic was already sent
                    if(deviceData.solic.contains(user.getUid())) {
                        // Updates UI
                        waitAuth.setText(R.string.pronto);
                        animLoad.setVisibility(View.GONE);
                        animStt.setVisibility(View.VISIBLE);
                        animStt.playAnimation();
                        descAuth.setText(R.string.alrSentSolic);
                        nextButton.setVisibility(View.VISIBLE);

                        // Sets next button onClickListener
                        nextButton.setOnClickListener(v -> {
                            // Redirects to Home
                            Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                            startActivity(homeIntent);
                            animEnt = R.anim.slide_in_right;
                            animExt = R.anim.slide_out_left;
                            finish();
                        });
                    }
                    // Else, adds user to the device
                    else {
                        // If user had already made a solicitation
                        if(!userData.solic.equals("")) {
                            // Removes old solicitation
                            new FirebaseAction().onceDevice(userData.solic, oldDeviceData -> {
                                // Gets new solic string
                                String newSolic = oldDeviceData.solic.replace(user.getUid(), "");
                                if (newSolic.equals("/")) {
                                    newSolic = "";
                                } else if (newSolic.equals("//")) {
                                    newSolic = "";
                                } else {
                                    newSolic = newSolic.replace("//", "/");
                                }

                                // Removes solic from old device
                                new FirebaseAction().setDeviceChild(userData.solic, "solic", newSolic, error -> {});
                            });
                        }

                        // Adds solic to user data
                        new FirebaseAction().setUserChild(user.getUid(), "solic", device, err -> {
                            // Adds user to device solicitations
                            new FirebaseAction().setDeviceChild(device, "solic", deviceData.solic + user.getUid() + "/", error2 -> {
                                if(error2 == null) {
                                    // Updates UI
                                    waitAuth.setText(R.string.pronto);
                                    animLoad.setVisibility(View.GONE);
                                    animStt.setVisibility(View.VISIBLE);
                                    animStt.playAnimation();
                                    descAuth.setText(R.string.solicSent);
                                    nextButton.setVisibility(View.VISIBLE);

                                    // Sets next button onClickListener
                                    nextButton.setOnClickListener(v -> {
                                        // Redirects to Home
                                        Intent intent2 = new Intent(getApplicationContext(), Home.class);
                                        startActivity(intent2);
                                        animEnt = R.anim.slide_in_right;
                                        animExt = R.anim.slide_out_left;
                                        finish();
                                    });
                                }
                            });
                        });
                    }
                }, 1500);
            });
        }
    }

    // Shows error
    private void setError(String error, String description) {
        // Updates UI
        waitAuth.setText(error);
        animLoad.setVisibility(View.GONE);
        animStt.setScale(0.7f);
        animStt.setAnimation(R.raw.cancel);
        animStt.setVisibility(View.VISIBLE);
        animStt.playAnimation();
        descAuth.setText(description);
        nextButton.setText(R.string.voltar);
        nextButton.setVisibility(View.VISIBLE);

        // Sets back button onClickListener
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_right;
            animExt = R.anim.slide_out_left;
            finish();
        });
    }

    // When activity finishes
    @Override
    public void finish() {
        super.finish();
        // Adiciona transição
        overridePendingTransition(animEnt, animExt);
    }
}