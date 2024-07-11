package com.control.chavedigital;

// Imports

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Users extends AppCompatActivity {

    // Variable declaration

    // Views
    MaterialToolbar appBar;
    TabLayout tabLt;
    ViewPager2 vp;
    UsersFragments adapter;

    // Values
    private int animEnt, animExt;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Checks if any user signed
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), SignIn.class);
            startActivity(intent);
            animEnt = 0;
            animExt = 0;
            finish();
        }

        // Variable definition
        appBar = findViewById(R.id.users_appbar);
        tabLt = findViewById(R.id.users_tabLt);
        vp = findViewById(R.id.users_vp);
        FragmentManager fm = getSupportFragmentManager();
        adapter = new UsersFragments(fm, getLifecycle());

        // Adds FragmentAdapter
        vp.setAdapter(adapter);

        // Adds tabs
        tabLt.addTab(tabLt.newTab().setText(getResources().getString(R.string.authUsers)));
        tabLt.addTab(tabLt.newTab().setText(getResources().getString(R.string.solicUsers)));
    }

    // When the activity starts
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onStart() {
        super.onStart();

        // Sets Appbar button onClickListener
        appBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_left;
            animExt = R.anim.slide_out_right;
            finish();
        });

        // When another tab is selected
        tabLt.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vp.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Slide to change tab
        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLt.selectTab(tabLt.getTabAt(position));
            }
        });
    }

    // When back button is pressed
    @Override
    public void onBackPressed() {
        // Redirects to Settings
        Intent startMain = new Intent(getApplicationContext(), Settings.class);
        startActivity(startMain);
        animEnt = R.anim.slide_in_left;
        animExt = R.anim.slide_out_right;
        finish();
    }

    // When the activity finishes
    @Override
    public void finish() {
        super.finish();

        // Adids transition
        overridePendingTransition(animEnt, animExt);
    }
}