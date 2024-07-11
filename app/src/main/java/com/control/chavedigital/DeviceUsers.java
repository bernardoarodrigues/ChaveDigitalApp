package com.control.chavedigital;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class DeviceUsers extends Fragment {

    // Variable declaration

    // APIs and others
    ValueEventListener deviceListener = null;

    // Views
    LinearLayout contUsers;
    FloatingActionButton addUser;
    LayoutInflater userInflater;
    CoordinatorLayout coordinatorLayout;

    // Values
    String device = "";

    // Constructor
    public DeviceUsers() {
    }

    // When the fragment is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // When the fragment view is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_users, container, false);

        // Variable definition
        contUsers = view.findViewById(R.id.contUsers);
        addUser = view.findViewById(R.id.addUser);
        userInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        coordinatorLayout = view.findViewById(R.id.coordLayout_users);

        return view;
    }

    // When the fragment starts
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onStart() {
        super.onStart();
        // Gets user device
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        device = prefs.getString("device", "");

        // Sets addUser button onClickListener
        addUser.setOnClickListener(v -> {
            // Encodes device and admin
            String deviceEnc = Encryption.encode(device);
            String admin = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            String adminEnc = Encryption.encode(admin);

            // Creates link share intent
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Acesse esse link para usar minha Chave Digital: https://chave-digital-br.web.app/authUser?d=" + deviceEnc + "&a=" + adminEnc);
            sendIntent.setType("text/plain");

            // Starts intent
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });

        // Starts getting users from device
        deviceListener = new FirebaseAction().onDeviceChild(device, "users", deviceChildData -> {
            if(!isAdded()) return;
            contUsers.removeAllViews();
            listUsers(deviceChildData);
        });
    }

    // Lists users
    private void listUsers(String usersStr) {
        // Split users
        String[] users = usersStr.split("/");

        // Creates first divider
        View dv = new View(requireActivity().getApplicationContext());
        int i = 0;
        dv.setId(i + 1);
        ConstraintLayout.LayoutParams paramsDv = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 1);
        dv.setLayoutParams(paramsDv);
        dv.setBackgroundResource(R.color.lt_grey);
        contUsers.addView(dv);

        // Gets user profile data
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        if(!isAdded()) return;

        // Adds admin
        addUserView(usersStr, user.getUid());

        // Loops through users
        for (String userId: users) {
            // Adds other users
            if(!userId.equals(user.getUid()) && !userId.equals("")) addUserView(usersStr, userId);
        }
    }

    // Adds user view in container
    private void addUserView(String usersStr, String userId) {
        // Inflates user view
        View userItem = userInflater.inflate(R.layout.user_item, null);
        contUsers.addView(userItem);

        // Declares views
        CircleImageView photoUser = userItem.findViewById(R.id.userPhoto);
        TextView nameUser = userItem.findViewById(R.id.userName);
        TextView adminUser = userItem.findViewById(R.id.userAdmin);
        CircleImageView rmvUser = userItem.findViewById(R.id.userRemove);

        // Gets user data
        new FirebaseAction().onceUser(userId, userData -> {
            // Adds user photo
            Glide.with(requireActivity().getApplicationContext()).load(userData.photo).into(photoUser);

            // If userId is the current's
            if(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(userId)) {
                nameUser.setText(R.string.vc);
                adminUser.setVisibility(View.VISIBLE);
            }
            else {
                nameUser.setText(userData.name);
                rmvUser.setVisibility(View.VISIBLE);
                rmvUser.setOnClickListener(v -> removeUser(usersStr, userId));
            }
        });
    }

    // RemoveUser Handler
    private void removeUser(String usersStr, String userId) {
        // Asks admin confirmation
        Snackbar temcert = Snackbar.make(coordinatorLayout, R.string.temcert, Snackbar.LENGTH_LONG);
        temcert.setAction(R.string.deletar, v -> {
            // Dismisses Snackbar
            temcert.dismiss();

            // Removes device from user
            new FirebaseAction().setUserChild(userId, "device", "", errorUser -> {
                if(errorUser == null) {
                    // Gets new users string
                    String newUsers = usersStr.replace(userId, "");
                    if(newUsers.equals("/")) { newUsers = ""; }
                    else if(newUsers.equals("//")) { newUsers = ""; }
                    else { newUsers = newUsers.replace("//", "/"); }

                    // Removes user from device
                    new FirebaseAction().setDeviceChild(device, "users", newUsers, error -> {
                        if(error == null) {
                            // Confirms operation
                            Snackbar.make(coordinatorLayout, R.string.userrm, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
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