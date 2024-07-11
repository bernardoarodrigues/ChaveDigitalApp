package com.control.chavedigital;

// Imports

import static androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Settings extends AppCompatActivity {

    // Variable declaration

    // APIs and others
    private FirebaseAuth mAuth;
    GetConnection getCon = null;
    SharedPreferences prefs = null;
    private BiometricPrompt biometricPrompt = null;
    private final Executor executor = Executors.newSingleThreadExecutor();

    // Views
    MaterialToolbar appBar;
    ImageView photo;
    TextView name, email, version;
    LinearLayout contDevice, btnUsers, btnWifiConfigs, contSafety, btnBlock, btnChangePass, btnLogout;
    ConstraintLayout btnProfile;
    SwitchMaterial swBloq;

    // Values
    private int animEnt, animExt;
    boolean block = false, wifiCon = true;
    String device = "";
    private final int TIME_DIFFERENCE_DEVICE_AND_PHONE = 10815;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        appBar = findViewById(R.id.appbar_settings);
        photo = findViewById(R.id.profilePhoto);
        name = findViewById(R.id.profileName);
        email = findViewById(R.id.profileEmail);
        btnProfile = findViewById(R.id.profile);
        contDevice = findViewById(R.id.device);
        btnUsers = findViewById(R.id.users);
        btnWifiConfigs = findViewById(R.id.wifiConfigs);
        contSafety = findViewById(R.id.safety);
        btnBlock = findViewById(R.id.block);
        swBloq = findViewById(R.id.swBlock);
        btnChangePass = findViewById(R.id.changePass);
        btnLogout = findViewById(R.id.logout);
        version = findViewById(R.id.versionLbl);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    // When the activity is started
    public void onStart() {
        super.onStart();

        try {
            String versionLbl = "Versão: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ".0";
            version.setText(versionLbl);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        // Gets internet connection status
        getCon = new GetConnection(this, (con, type) -> wifiCon = con);
        getCon.start();

        // Adds user data to the profile container
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        name.setText(user.getDisplayName());
        email.setText(user.getEmail());

        // Adds user photo
        Glide.with(this)
                .load(user.getPhotoUrl())
                .apply(new RequestOptions()
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL))
                .into(photo);

        // Gets button block from SharedPreferences
        block = prefs.getBoolean("blocked", false);

        // If button block is enabled
        if(block) {
            // Updates switch
            swBloq.setChecked(true);
            swBloq.setThumbTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.primary));
            swBloq.setTrackTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.dk_grey));
        }
        // If button block is disabled
        else {
            // Updates switch
            swBloq.setChecked(false);
            swBloq.setThumbTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.dk_grey));
            swBloq.setTrackTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.lt_grey));
        }

        // Creates biometric prompt
        if(biometricPrompt == null){
            biometricPrompt = new BiometricPrompt(this,executor,callback);
        }

        // Gets device and admin from SharedPreferences
        device = prefs.getString("device", "");
        boolean admin = prefs.getBoolean("admin", false);

        // If any device
        if(!device.equals("")) {
            contSafety.setVisibility(View.VISIBLE);

            if(admin) {
                contDevice.setVisibility(View.VISIBLE);
            }
            else {
                contDevice.setVisibility(View.GONE);
            }
        }
        else {
            contSafety.setVisibility(View.GONE);
        }

        // Sets onClickListeners
        initOnClick();
    }

    // Starts onClickListeners
    private void initOnClick() {
        // Appbar button
        appBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_left;
            animExt = R.anim.slide_out_right;
            finish();
        });
        // Profile Button
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_right;
            animExt = R.anim.slide_out_left;
            finish();
        });
        // Users Button
        btnUsers.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Users.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_right;
            animExt = R.anim.slide_out_left;
            finish();
        });
        // WifiConfigs Button
        btnWifiConfigs.setOnClickListener(v -> {
            // Creates dialog
            final AlertDialog dialog = new AlertDialog.Builder(Settings.this, R.style.AlertDialogTheme)
                    .setTitle(getApplicationContext().getResources().getString(R.string.configswifi))
                    .setMessage(getApplicationContext().getResources().getString(R.string.resetDesc))
                    .setPositiveButton(getApplicationContext().getResources().getString(R.string.avancar), null)
                    .create();

            // When dialog is created
            dialog.setOnShowListener(dialog1 -> {
                Button btn = (dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                btn.setOnClickListener(v1 -> resetWifi());
            });

            // Shows dialog
            dialog.show();
        });
        btnBlock.setOnClickListener(v -> setBloq());
        btnChangePass.setOnClickListener(v -> popupEditPass());
        btnLogout.setOnClickListener(v -> new MaterialAlertDialogBuilder(Settings.this, R.style.AlertDialogTheme)
                .setTitle(getApplicationContext().getResources().getString(R.string.sair))
                .setMessage(getApplicationContext().getResources().getString(R.string.sairCert))
                .setPositiveButton(getApplicationContext().getResources().getString(R.string.sair),
                        (dialog, which) -> logout())
                .show());
    }
    // Função para resetar credenciais Wifi do dispositivo
    private void resetWifi() {
        new FirebaseAction().onceDevice(device, deviceData -> {
            // If device isn't configured to the internet
            if(deviceData.config.equals("no")) {
                Toast.makeText(this, R.string.notConfig, Toast.LENGTH_SHORT).show();
                return;
            }

            // Gets epoch time from device and phone
            long scEsp = Long.parseLong(deviceData.lasttime);
            long scCel = System.currentTimeMillis()/1000;

            // If device is online
            if(scCel - scEsp > TIME_DIFFERENCE_DEVICE_AND_PHONE) {
                Toast.makeText(this, R.string.dispOff, Toast.LENGTH_SHORT).show();
                return;
            }

            // Resets network credentials from device
            new FirebaseAction().setDeviceChild(device, "reset", "yes", error -> {
                if(error == null) {
                    // Redirects to Home
                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    startActivity(intent);
                    animEnt = R.anim.slide_in_right;
                    animExt = R.anim.slide_out_left;
                    finish();
                }
            });
        });
    }

    // Calls BiometricPrompt
    private void setBloq() {
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
                    .setTitle("Validar sua identidade")
                    .setDescription("Confirme sua identidade para prosseguir")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build();
        }
        else {
            return new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Validar sua identidade")
                    .setDescription("Confirme sua identidade para prosseguir")
                    .setNegativeButtonText("Cancelar")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build();
        }
    }

    // BiometricPrompt Callback
    private final BiometricPrompt.AuthenticationCallback callback =
        new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                if(errorCode==ERROR_NEGATIVE_BUTTON && biometricPrompt!=null) {
                    // If any error or user cancel, closes prompt
                    biometricPrompt.cancelAuthentication();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                // If success, toggles switch and changes blocked
                SharedPreferences.Editor prefsEditor = prefs.edit();
                if(!prefs.getBoolean("blocked", false)) {
                    prefsEditor.putBoolean("blocked", true);
                    prefsEditor.apply();
                    block = true;
                    runOnUiThread(() -> {
                        // Updates switch
                        swBloq.setChecked(true);
                        swBloq.setThumbTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.primary));
                        swBloq.setTrackTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.dk_grey));
                    });
                }
                else {
                    prefsEditor.putBoolean("blocked", false);
                    prefsEditor.apply();
                    block = false;
                    runOnUiThread(() -> {
                        // Updates switch
                        swBloq.setChecked(false);
                        swBloq.setThumbTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.dk_grey));
                        swBloq.setTrackTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.lt_grey));
                    });
                }
            }

            @Override
            public void onAuthenticationFailed() {
            }
        };

    // ResetPassword Popup
    private void popupEditPass() {
        // Inflates popup
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_changepass, null);

        // Declares views
        TextInputEditText email = popupView.findViewById(R.id.emailTxt);
        TextInputLayout emailLt = popupView.findViewById(R.id.emailLt);

        // Creates dialog
        final AlertDialog dialog = new AlertDialog.Builder(Settings.this, R.style.AlertDialogTheme)
                .setView(popupView)
                .setTitle(R.string.alterar)
                .setMessage(R.string.insiraem)
                .setPositiveButton(getApplicationContext().getResources().getString(R.string.enviar), null)
                .create();

        // When dialog is created
        dialog.setOnShowListener(dialog1 -> {
            Button btn = (dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> resetPass(dialog, email, emailLt));
        });

        // Shows dialog
        dialog.show();
    }

    // ResetPassword Handler
    private void resetPass(DialogInterface dialog, TextInputEditText emailTxt, TextInputLayout emailLt) {
        // Cleans erros
        emailLt.setError(null);

        // Gets input
        String email = Objects.requireNonNull(emailTxt.getText()).toString();

        // Value validation

        // If email wasn't properly entered
        if(!email.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail())) {
            emailLt.setErrorEnabled(true);
            emailLt.setError(getResources().getString(R.string.insSeuEm));
        }
        else {
            // Sends reset password email
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                // If success
                if (task.isSuccessful()) {
                    // Dismisses dialog
                    dialog.dismiss();

                    // Confirms email was sent
                    Toast.makeText(getApplicationContext(), R.string.envEm, Toast.LENGTH_LONG).show();
                }
                // If any error occurred
                else {
                    // If the account doesn't exist
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        emailLt.setErrorEnabled(true);
                        emailLt.setError(getResources().getString(R.string.containex));
                    }
                    // If any other error
                    else {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), R.string.altFail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Logout Handler
    private void logout() {
        // Cleans user data from SharedPreferences
        prefs.edit().clear().apply();

        // Signs user out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();

        // Redirects to SignIn
        Intent intent = new Intent(getApplicationContext(), SignIn.class);
        startActivity(intent);
        animEnt = R.anim.slide_in_right;
        animExt = R.anim.slide_out_left;
        finish();
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

        // Stops getting internet connection
        if(getCon != null) getCon.Stop();

        // Adds transition
        overridePendingTransition(animEnt, animExt);
    }

}