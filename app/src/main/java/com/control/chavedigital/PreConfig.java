package com.control.chavedigital;

// Imports

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class PreConfig extends AppCompatActivity {

    // Variable declaration

    // Views
    MaterialToolbar appBar;
    MaterialButton next;

    // Values
    private int animEnt, animExt;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preconfig);

        // Variable definition
        appBar = findViewById(R.id.appbar_preConfig);
        next = findViewById(R.id.nextPreConfig);
    }

    // When the activity starts
    @Override
    public void onStart() {
        super.onStart();

        // Sets onClickListeners
        // Appbar button
        appBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_left;
            animExt = R.anim.slide_out_right;
            finish();
        });
        // Next button
        next.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Config.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_right;
            animExt = R.anim.slide_out_left;
            finish();
        });
    }

    // When back button is pressed
    @Override
    public void onBackPressed() {
        // Redirects to Home
        Intent startMain = new Intent(getApplicationContext(), Home.class);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        animEnt = R.anim.slide_in_left;
        animExt = R.anim.slide_out_right;
        finish();
    }

    // When the activity finishes
    @Override
    public void finish() {
        super.finish();

        // Adds transition
        overridePendingTransition(animEnt, animExt);
    }
}