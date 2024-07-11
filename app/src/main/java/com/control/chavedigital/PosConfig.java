package com.control.chavedigital;

// Imports

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;

public class PosConfig extends AppCompatActivity {

    // Variable declaration

    // Views
    MaterialButton next;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posconfig);

        // Variable definition
        next = findViewById(R.id.nextPosConfig);
    }

    // When the activity starts
    @Override
    public void onStart() {
        super.onStart();

        // Sets Next button onClickListener
        next.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            finish();
        });
    }

    // When back button is pressed
    @Override
    public void onBackPressed() {}

    // When the activity finishes
    @Override
    public void finish() {
        super.finish();

        // Adds transition
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}