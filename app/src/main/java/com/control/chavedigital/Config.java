package com.control.chavedigital;

// Imports

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import org.json.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Config extends AppCompatActivity {

    // Variable declaration

    // APIs and others
    private FirebaseAuth mAuth;
    GetConnection getCon = null;
    ValueEventListener deviceListener = null;

    // Views
    MaterialToolbar appBar;
    LinearLayout cont, contError;
    LottieAnimationView animError, animLoad;
    TextView errorLbl;
    LayoutInflater wifiInflater;

    // Values
    private int animEnt, animExt;
    boolean wifiCon = true, onError, onDialog, onConfig = false, runTask = false;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

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
        appBar = findViewById(R.id.config_appbar);
        cont = findViewById(R.id.container);
        animLoad = findViewById(R.id.animLoadC);
        contError = findViewById(R.id.contErro_config);
        animError = findViewById(R.id.animErro_config);
        errorLbl = findViewById(R.id.erroLbl);
        wifiInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // When the activity starts
    @Override
    public void onStart() {
        super.onStart();

        // Sets Appbar button onClickListener
        appBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PreConfig.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_left;
            animExt = R.anim.slide_out_right;
            finish();
        });

        // Gets internet connection status
        getCon = new GetConnection(this, (con, type) -> wifiCon = con);
        getCon.start();

        // Starts Task
        JsonTask();
    }

    // Handler to gets networks from device
    private void JsonTask() {
        // Hides error
        contError.setVisibility(View.GONE);

        // Task Loop
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // If phone is connected
                if(wifiCon && !onDialog) {
                    onError = false;
                    if(!runTask) {
                        runTask = true;

                        // Envia GET request
                        HttpRequest httpRequest = new HttpRequest(Config.this, "http://192.168.4.1/networks", result -> listNetworks(result));
                        httpRequest.start();
                    }
                }
                // If phone isn't connected
                if(!wifiCon && !onDialog) {
                    if(!onError) {
                        onError = true;
                        runTask = false;
                        // Cleans networks and shows error
                        limpaRedes();
                        semWifi();
                    }
                }
                // Starts loop
                h.postDelayed(this, 5000);
            }
        }, 1);
    }

    // Lists networks
    private void listNetworks(String strJ) {
        // Updates UI
        animLoad.setVisibility(View.GONE);
        contError.setVisibility(View.GONE);

        try {
            JSONObject netsObj = new JSONObject(strJ);
            JSONArray networks = netsObj.getJSONArray("networks");

            networks = sortNetworks(networks);

            // If no network found
            if (networks.length() == 0) {
                // Altera UI
                limpaRedes();
                errorLbl.setText(R.string.nenhRede);
                contError.setVisibility(View.VISIBLE);
                animError.playAnimation();
            }
            // If any network
            else {
                // If not first scan, clean networks
                limpaRedes();

                // Includes divider
                View dv = new View(getApplicationContext());
                int id = 2;
                dv.setId(id + 1);
                ConstraintLayout.LayoutParams paramsDv = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 1);
                dv.setLayoutParams(paramsDv);
                dv.setBackgroundResource(R.color.lt_grey);
                cont.addView(dv);

                // Loops through networks
                for (int i = 0; i < networks.length(); i++) {
                    // Gets network at i index
                    JSONObject network = networks.getJSONObject(i);

                    String name = network.getString("name");
                    if(name.equals("Chave Digital")) return;

                    // Adds network container
                    View wifiItem = wifiInflater.inflate(R.layout.wifi_item, null);
                    cont.addView(wifiItem);

                    ImageView icWifi = wifiItem.findViewById(R.id.icWifi);
                    TextView nmWifi = wifiItem.findViewById(R.id.nmWifi);

                    // Adds network name
                    nmWifi.setText(name);

                    // Adds network icon
                    String iconP = "";
                    String strength = network.getString("strength");
                    String secure = network.getString("secure");
                    if (secure.equals("0")) {
                        iconP = "wifi_l" + strength;
                    }
                    if (secure.equals("1")) {
                        iconP = "wifi_lc" + strength;
                    }
                    icWifi.setImageResource(getResources().getIdentifier(iconP, "drawable", getPackageName()));

                    // Sets network onClickListener
                    wifiItem.setOnClickListener(v -> showPopupSenha(name));
                }
            }
        } catch (JSONException err) { Toast.makeText(Config.this, err.toString(), Toast.LENGTH_SHORT).show();}
    }

    // Sorts networks by strength
    private JSONArray sortNetworks(JSONArray networks) {
        JSONArray sortedNetworks = null;
        try {
            sortedNetworks = new JSONArray();

            List<JSONObject> jsonValues = new ArrayList<>();
            for (int i = 0; i < networks.length(); i++) {
                jsonValues.add(networks.getJSONObject(i));
            }
            Collections.sort(jsonValues, new Comparator<JSONObject>() {
                private static final String KEY_NAME = "strength";

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = "";
                    String valB = "";

                    try {
                        valA = (String) a.get(KEY_NAME);
                        valB = (String) b.get(KEY_NAME);
                    } catch (JSONException ignored) {}

                    return -valA.compareTo(valB);
                }
            });

            for (int i = 0; i < networks.length(); i++) {
                sortedNetworks.put(jsonValues.get(i));
            }
        } catch (JSONException err) { Toast.makeText(this, err.toString(), Toast.LENGTH_SHORT).show(); }

        return sortedNetworks;
    }

    // Popup NetworkPass
    private void showPopupSenha(String nomeRede) {
        // Stops getting networks
        onDialog = true;

        // Inflates popup
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_pass, null);

        // Declares views
        TextInputEditText senhaTxt = popupView.findViewById(R.id.senhaTxt);
        TextInputLayout senhaLt = popupView.findViewById(R.id.senhaLt);

        // Creates dialog
        final AlertDialog dialog = new AlertDialog.Builder(Config.this, R.style.AlertDialogTheme)
                .setView(popupView)
                .setTitle(nomeRede)
                .setPositiveButton(getApplicationContext().getResources().getString(R.string.enviar), null)
                .create();

        // When dialog is created
        dialog.setOnShowListener(dialog1 -> {
            Button btn = (dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> sendCredentials(nomeRede, Objects.requireNonNull(senhaTxt.getText()).toString().trim(), senhaLt, dialog));
        });

        // Shows dialog
        dialog.show();
    }

    // Sends wifi credentials to the device
    private void sendCredentials(String rede, String senha, TextInputLayout senhaLt, DialogInterface dialog) {
        // Cleans errors
        senhaLt.setError(null);

        // Input validation

        // If password wasn't properly entered
        if(senha.equals("")) {
            senhaLt.setErrorEnabled(true);
            senhaLt.setError(getResources().getString(R.string.preench));
        }
        else {
            // If phone is connected to the internet
            if (wifiCon) {
                // Gets user profile data
                FirebaseUser user = mAuth.getCurrentUser();
                assert user != null;

                // Request parameters
                String[] field = new String[3];
                field[0] = "ssid";
                field[1] = "pass";
                field[2] = "admin";
                // Request data
                String[] data = new String[3];
                data[0] = rede;
                data[1] = senha;
                data[2] = user.getUid();
                // Sends POST request
                HttpRequest httpRequest = new HttpRequest(Config.this, "http://192.168.4.1/settings", field, data, result -> {
                    // Dismisses password popup and starts config popup
                    onDialog = true;
                    onConfig = true;
                    dialog.dismiss();

                    int pos = result.indexOf("/");
                    String device = result.substring(pos+1);

                    showPopupConfig(device);
                });
                httpRequest.start();
            }
            // If no connection
            else {
                // Dismisses popup and updates UI
                dialog.dismiss();
                semWifi();
                onDialog = false;
            }
        }
    }

    // Popup Config
    private void showPopupConfig(String device) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Config.this, R.style.AlertDialogTheme);

        // Inflates popup
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_config, null);

        // Declares views
        TextView txtTt = popupView.findViewById(R.id.txtTtC);
        LottieAnimationView loadAnim = popupView.findViewById(R.id.spinnerC);
        LottieAnimationView erroAnim = popupView.findViewById(R.id.erroC);
        MaterialButton btnVoltar = popupView.findViewById(R.id.voltarConf);
        View footer = popupView.findViewById(R.id.voltarConfMg);

        // Creates dialog
        builder.setView(popupView);
        builder.setCancelable(false);

        // Shows dialog
        builder.show();

        // Waits for the device configuration
        deviceListener = new FirebaseAction().onDevice(device, deviceData -> {
            if(!deviceData.config.equals("yes")) return;

            // Stops deviceListener from Firebase
            if(deviceListener != null) {
                new FirebaseAction().offDevice(device, deviceListener);
                deviceListener = null;
            }

            // Gets user profile data
            FirebaseUser user = mAuth.getCurrentUser();
            assert user != null;

            // If current user isn't added to the device
            if (!deviceData.users.contains(user.getUid())) {
                // Adds user to the device
                new FirebaseAction().setDeviceChild(device, "users", deviceData.users + user.getUid() + "/", error -> {
                });
            }

            // Adds device to user data
            new FirebaseAction().setUserChild(user.getUid(), "device", device, error -> {
                if(error == null) {
                    // Redirects to PosConfig
                    Intent intent = new Intent(getApplicationContext(), PosConfig.class);
                    startActivity(intent);
                    animEnt = R.anim.slide_in_right;
                    animExt = R.anim.slide_out_left;
                    finish();
                }
            });
        });

        // If after 40s it doesn't configure, sets an error
        final Handler h = new Handler();
        h.postDelayed(() -> {
            // Stops deviceListener from Firebase
            if(deviceListener != null) {
                new FirebaseAction().offDevice(device, deviceListener);
                deviceListener = null;
            }

            // Sets back button onClickListener
            btnVoltar.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), PreConfig.class);
                startActivity(intent);
                animEnt = R.anim.slide_in_left;
                animExt = R.anim.slide_out_right;
                finish();
            });

            // Updates popup
            txtTt.setText(R.string.error);
            loadAnim.setVisibility(View.GONE);
            erroAnim.setVisibility(View.VISIBLE);
            erroAnim.playAnimation();
            btnVoltar.setVisibility(View.VISIBLE);
            footer.setVisibility(View.VISIBLE);
        }, 40000);
    }

    // Cleans networks
    private void limpaRedes() {
        cont.removeAllViews();
    }

    // Shows no connection error
    private void semWifi() {
        limpaRedes();
        animLoad.pauseAnimation();
        animLoad.setVisibility(View.GONE);
        errorLbl.setText(R.string.semConex);
        contError.setVisibility(View.VISIBLE);
        animError.playAnimation();
    }

    // When back button is pressed
    @Override
    public void onBackPressed() {
        // If not configuring device
        if(!onConfig) {
            // Redirects to PreConfig
            Intent startMain = new Intent(getApplicationContext(), PreConfig.class);
            startActivity(startMain);
            animEnt = R.anim.slide_in_left;
            animExt = R.anim.slide_out_right;
            finish();
        }
    }

    // When the activity finishes
    @Override
    public void finish() {
        super.finish();
        // Adds transition
        overridePendingTransition(animEnt, animExt);
    }
}