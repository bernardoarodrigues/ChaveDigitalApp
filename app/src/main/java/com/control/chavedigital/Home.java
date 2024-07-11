package com.control.chavedigital;

// Imports

import static androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Home extends AppCompatActivity {

    // Variable declaration

    // APIs and others
    private FirebaseAuth mAuth;
    GetConnection getCon = null;
    SharedPreferences prefs;
    private BiometricPrompt biometricPrompt = null;
    private final Executor executor = Executors.newSingleThreadExecutor();
    ValueEventListener userListener = null, deviceListener = null;
    private final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

    // Views
    MaterialToolbar appBar;
    ConstraintLayout cont;
    CoordinatorLayout coordinatorLayout;
    LinearLayout btnCont;
    TextView lblStatus, lblAc, sttLbl, sttDevice;
    FloatingActionButton openButton;
    LottieAnimationView animLoad, animStatus;

    // Values
    boolean admin, online, fetchedConfigs, handledFCM;
    String status = "", device = "", lastDevice = "", conType = "";
    private int animEnt;
    private int animExt;
    private final int TIME_DIFFERENCE_DEVICE_AND_PHONE = 10815;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
        appBar = findViewById(R.id.appbar_home);
        cont = findViewById(R.id.cont);
        lblStatus = findViewById(R.id.lblStatus);
        lblAc = findViewById(R.id.lblAc);
        sttLbl = findViewById(R.id.sttLbl);
        openButton = findViewById(R.id.openButton);
        sttDevice = findViewById(R.id.sttDevice);
        animLoad = findViewById(R.id.animLoad_home);
        animStatus = findViewById(R.id.animStatus_home);
        coordinatorLayout = findViewById(R.id.coordLayout_home);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        btnCont = findViewById(R.id.btnCont);

        // Firebase RemoteConfig Settings
        HashMap<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("app_version", getCurrentVersionCode());
        remoteConfig.setDefaultsAsync(defaultMap);

        // Sets loading status
        setStatus("loading");
    }

    // When the activity starts
    public void onStart() {
        super.onStart();

        // Creates biometric prompt
        if(biometricPrompt == null) {
            biometricPrompt = new BiometricPrompt(this,executor,callback);
        }

        // Sets onClick listeners
        initOnClick();

        // Gets internet connection status
        getCon = new GetConnection(this, (con, type) -> {
            // Sets connection type
            conType = type;

            // If phone isn't connected
            if(!con) {
                setStatus("notCon");
                return;
            }

            // If not getting user data from Firebase
            if(userListener == null) {
                // Preloads device container if device was configured
                if(prefs.getBoolean("config", false)) {
                    openButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.primary));
                    openButton.setEnabled(true);
                    setStatus("config");
                }

                // Gets user profile data
                FirebaseUser user = mAuth.getCurrentUser();
                assert user != null;
                String userId = user.getUid();

                // Starts getting user data
                userListener = new FirebaseAction().onUser(userId, userData -> {
                    // Gets user device
                    device = userData.device;

                    // Saves device in SharedPreferences
                    SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    prefsEditor.putString("device", device);

                    // If any device and not getting device data from Firebase
                    if(!device.equals("") && deviceListener == null) {
                        deviceListener = new FirebaseAction().onDevice(device, deviceData -> {
                            // Checks if user is admin
                            admin = deviceData.admin.equals(userId);

                            // Saves admin in SharedPreferences
                            prefsEditor.putBoolean("admin", admin);

                            // If device isn't configured to the internet
                            if(deviceData.config.equals("no")) {
                                if(admin) lblAc.setOnClickListener(v -> {
                                    // Redirects to PreConfig
                                    Intent intent = new Intent(getApplicationContext(), PreConfig.class);
                                    startActivity(intent);
                                    animEnt = R.anim.slide_in_right;
                                    animExt = R.anim.slide_out_left;
                                    finish();
                                });
                                // Sets not configured status
                                setStatus("notConfig");
                                prefsEditor.putBoolean("config", false);
                                prefsEditor.apply();
                                return;
                            }

                            // Device is configured, sets configured status
                            setStatus("config");
                            prefsEditor.putBoolean("config", true);
                            prefsEditor.apply();

                            // Checks if device is online
                            long scEsp = Long.parseLong(deviceData.lasttime);
                            long scCel = System.currentTimeMillis()/1000;
                            online = scCel - scEsp <= TIME_DIFFERENCE_DEVICE_AND_PHONE;

                            // If device is online
                            if(online) {
                                sttLbl.setText(R.string.online);
                                openButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.primary));
                                openButton.setEnabled(true);
                            }
                            // If device is offline
                            else {
                                sttLbl.setText(R.string.offline);
                                openButton.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.lt_grey));
                                openButton.setEnabled(false);
                            }

                            // Sets openButton's onClick
                            openButton.setOnClickListener(v -> {
                                // If button is blocked with Biometrics
                                if(prefs.getBoolean("blocked", false)) {
                                    BiometricPrompt.PromptInfo promptInfo = buildPrompt();
                                    biometricPrompt.authenticate(promptInfo);
                                }
                                // Else, opens door
                                else {
                                    open();
                                }
                            });

                            // Fetches RemoteConfig to check app updates
                            fetchRemoteConfig();

                            // Handles FCM if admin
                            if(!handledFCM) handleFCM(device, deviceData.users, admin);
                        });

                        // Saves device in lastDevice
                        lastDevice = device;
                    }
                    // If no device
                    else {
                        // If any device saved and getting device data from Firebase, stops
                        if(!lastDevice.equals("") && deviceListener != null) {
                            new FirebaseAction().offDevice(lastDevice, deviceListener);
                            deviceListener = null;
                        }

                        // Removes admin role
                        prefsEditor.putBoolean("admin", false);
                        prefsEditor.putBoolean("config", false);
                        prefsEditor.apply();

                        // Sets not found device status
                        setStatus("notFound");

                        // Cleans lastDevice
                        lastDevice = "";

                        // Fetches RemoteConfig to check app updates
                        fetchRemoteConfig();
                    }
                });
            }
        });
        // Starts connection listener
        getCon.start();
    }

    // Updates UI according to device status
    private void setStatus(String newStatus) {
        // If status didn't change
        if(status.equals(newStatus)) return;
        status = newStatus;

        // Switch through status cases:
        // loading - notCon - notFound - notConfig - config
        switch (status) {
            case "loading":
                // Shows loading animation
                animLoad.setVisibility(View.VISIBLE);
                animStatus.setVisibility(View.GONE);
                cont.setVisibility(View.GONE);

                break;
            case "notCon":
                // Shows phone not connected to the internet
                animLoad.setVisibility(View.GONE);
                animStatus.setVisibility(View.VISIBLE);
                animStatus.setAnimation(R.raw.nocon);
                animStatus.playAnimation();
                cont.setVisibility(View.GONE);

                lblStatus.setText(R.string.noCon);
                lblStatus.setVisibility(View.VISIBLE);
                lblAc.setVisibility(View.GONE);

                stopListeners();
                break;
            case "notFound":
                // Shows user doesn't have any device
                animLoad.setVisibility(View.GONE);
                animStatus.setVisibility(View.VISIBLE);
                animStatus.setAnimation(R.raw.notfound);
                animStatus.playAnimation();
                cont.setVisibility(View.GONE);

                lblStatus.setText(R.string.nenhum);
                lblStatus.setVisibility(View.VISIBLE);
                lblAc.setText(R.string.addDisp);
                lblAc.setVisibility(View.VISIBLE);
                lblAc.setOnClickListener(v -> addDisp());

                break;
            case "notConfig":
                // Shows device isn't configured to the internet
                animLoad.setVisibility(View.GONE);
                animStatus.setVisibility(View.VISIBLE);
                animStatus.setAnimation(R.raw.notfound);
                animStatus.playAnimation();
                cont.setVisibility(View.GONE);

                lblStatus.setText(R.string.notConfig);
                lblStatus.setVisibility(View.VISIBLE);

                // If user is the admin, shows configure action
                if(admin) {
                    lblAc.setText(R.string.confDisp);
                } else {
                    lblAc.setText(R.string.askAdmin);
                }
                lblAc.setVisibility(View.VISIBLE);

                break;
            case "config":
                // Shows device container
                animLoad.setVisibility(View.GONE);
                animStatus.setVisibility(View.GONE);
                lblStatus.setVisibility(View.GONE);
                lblAc.setVisibility(View.GONE);

                cont.setVisibility(View.VISIBLE);
                break;
        }
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
            // If success, opens door
            runOnUiThread(() -> open());
        }

        @Override
        public void onAuthenticationFailed() {
        }
    };

    // Opens the door
    private void open() {
        // If the device is online
        if(online) {
            // Sets device state -> "1"
            new FirebaseAction().setDeviceChild(device, "state", "1", error -> {
                if(error == null) {
                    // Confirms door opening
                    sttDevice.setText(R.string.portAb);
                    // Hides after 2 seconds
                    Handler h = new Handler();
                    h.postDelayed(() -> sttDevice.setText(""), 2000);
                }
            });
        }
    }

    // Starts onClickListeners
    private void initOnClick() {
        // Appbar button
        appBar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.configsBtn) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
                animEnt = R.anim.slide_in_right;
                animExt = R.anim.slide_out_left;
                finish();
                return true;
            }
            return false;
        });
    }

    // Config Popup
    private void addDisp() {
        // Options
        final Item[] items = {
                new Item("Não, vou configurar agora", R.drawable.cancel_questionform),
                new Item("Sim, quero me autenticar nela", R.drawable.confirm_questionform)
        };

        // Options ListAdapter
        ListAdapter adapter = new ArrayAdapter<Item>(
                this,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items){
            public View getView(int position, View convertView, ViewGroup parent) {
                // Usa super class para criar a View
                View v = super.getView(position, convertView, parent);
                TextView tv = v.findViewById(android.R.id.text1);
                tv.setTextSize(17);

                // Adiciona o Ícone
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                // Adiciona margem entre o ícone e o texto
                int dp12 = (int) (12 * getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp12);

                return v;
            }
        };

        // Creates popup
        final AlertDialog dialog = new AlertDialog.Builder(Home.this, R.style.AlertDialogTheme)
                .setTitle(getApplicationContext().getResources().getString(R.string.japos_chave))
                .setAdapter(adapter, (dialog1, item) -> {
                    // If user wants to add a device
                    if(item==0) {
                        // Redirects to PreConfig
                        Intent intent = new Intent(getApplicationContext(), PreConfig.class);
                        startActivity(intent);
                        animEnt = R.anim.slide_in_right;
                        animExt = R.anim.slide_out_left;
                        finish();
                    }
                    // If user wants to authenticate in a device
                    else {
                        Snackbar.make(coordinatorLayout, "Peça o link para o administrador do dispositivo", Snackbar.LENGTH_LONG).show();
                    }
                })
                .create();

        // Shows dialog
        dialog.show();
    }

    // Item Class
    public static class Item{
        public final String text;
        public final int icon;
        public Item(String text, Integer icon) {
            this.text = text;
            this.icon = icon;
        }
        @NonNull
        @Override
        public String toString() {
            return text;
        }
    }

    // Fetches Remote Config
    private void fetchRemoteConfig() {
        // If hadn't fetched RemoteConfig
        if(!fetchedConfigs) {
            // Fetches RemoteConfig values
            remoteConfig.fetch().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    remoteConfig.activate();
                    checkForUpdate();
                }
            });
            fetchedConfigs = true;
        }
    }

    // Gets current app version
    private int getCurrentVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Checks for app update
    private void checkForUpdate() {
        int latestAppVersion = (int)remoteConfig.getDouble("app_version");
        if (latestAppVersion > getCurrentVersionCode()) {
            new MaterialAlertDialogBuilder(Home.this, R.style.AlertDialogTheme)
                .setTitle("Atualize seu aplicativo")
                .setMessage("Uma nova versão do app está disponível, atualize para continuar")
                .setPositiveButton("Atualizar", (dialog, which) -> {
                    String url = remoteConfig.getString("app_url");
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                })
                .setCancelable(false)
                .show();
        }
    }

    private void handleFCM(String deviceId, String usersStr, boolean isAdmin) {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    return;
                }
                String token = task.getResult();

                // Sets msgtoken in Firebase database
                String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                new FirebaseAction().setUserChild(uid, "msgtoken", token, err -> {});
            });

        // If admin, adds device's users msgtokens
        if(isAdmin) {
            // Splits users
            String[] users = usersStr.split("/");
            AtomicReference<String> msgtokens = new AtomicReference<>("");

            // Gets how many users
            int nUsers = users.length;
            AtomicInteger count = new AtomicInteger(0);

            // Loops through users
            for (String userId : users) {
                new FirebaseAction().onceUser(userId, userData -> {
                    // If user msgtoken not "", adds its token to the list
                    if (!Objects.equals(userData.msgtoken, ""))
                        msgtokens.set(msgtokens.get() + userData.msgtoken + "/");
                    // Increases counter
                    count.getAndAdd(1);

                    // If looped through every user, sets users msgtokens
                    if (count.get() == nUsers) {
                        new FirebaseAction().setDeviceChild(deviceId, "msgtokens", msgtokens.get(), err -> {});
                        handledFCM = true;
                    }
                });
            }
        }
    }

    // Stops Firebase listeners
    private void stopListeners() {
        // Gets user profile data
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null) {
            // If getting user data from Firebase, stops
            if (userListener != null) {
                new FirebaseAction().offUser(user.getUid(), userListener);
                userListener = null;
            }
            // If getting device data from Firebase, stops
            if (deviceListener != null) {
                new FirebaseAction().offDevice(lastDevice, deviceListener);
                deviceListener = null;
            }
        }
    }

    // When back button is pressed
    @Override
    public void onBackPressed() {
        // Closes app
        animEnt = 0;
        animExt = 0;
        finish();
    }

    // When the activity is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stops Firebase listeners
        stopListeners();

        // Stops getting internet connection
        if(getCon != null) getCon.Stop();
    }

    // When the activity finishes
    @Override
    public void finish() {
        super.finish();

        // Stops Firebase listeners
        stopListeners();

        // Stops getting internet connection
        if(getCon != null) getCon.Stop();

        // Adds transition
        overridePendingTransition(animEnt, animExt);
    }
}