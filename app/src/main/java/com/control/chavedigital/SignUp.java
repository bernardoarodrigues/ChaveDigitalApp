package com.control.chavedigital;

// Imports

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUp extends AppCompatActivity {

    // Variable declaration

    // APIs and others
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // Views
    TextInputLayout nameLt, emailLt, passLt;
    TextInputEditText nameTxt, emailTxt, passTxt;
    MaterialButton signupBtn;
    LinearLayout googleBtn;
    TextView errLbl, redSigninBtn;

    // Values
    public int animEnt, animExt;
    private final static int RC_SIGN_IN = 123;
    boolean erro = false;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Variable definition
        mAuth = FirebaseAuth.getInstance();
        googleBtn = findViewById(R.id.cadGoogle);
        redSigninBtn = findViewById(R.id.redLog);
        signupBtn = findViewById(R.id.cadBtn);
        nameLt = findViewById(R.id.nameSignupLt);
        emailLt = findViewById(R.id.emailSignupLt);
        passLt = findViewById(R.id.passSignupLt);
        nameTxt = findViewById(R.id.nameSignupTxt);
        emailTxt = findViewById(R.id.emailSignupTxt);
        passTxt = findViewById(R.id.passSignupTxt);
        errLbl = findViewById(R.id.errCad);

        // Configures Google SignIn request
        createRequest();
    }

    // When the activity starts
    @Override
    public void onStart() {
        super.onStart();

        // Sets onClick listeners
        signupBtn.setOnClickListener(v -> register());
        googleBtn.setOnClickListener(v -> googleSignup());
        redSigninBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignIn.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_left;
            animExt = R.anim.slide_out_right;
            finish();
        });
    }

    // SignUp Handler
    private void register() {
        // Cleans errors
        errLbl.setVisibility(View.GONE);
        erro = false;

        // Gets input values
        String name = Objects.requireNonNull(nameTxt.getText()).toString().trim();
        String email = Objects.requireNonNull(emailTxt.getText()).toString().trim();
        String pass = Objects.requireNonNull(passTxt.getText()).toString().trim();

        // Input validation

        // If password wasn't properly entered
        if(pass.isEmpty() || pass.length() < 6) {
            showErr(getResources().getString(R.string.insSen));
            passLt.requestFocus();
            erro = true;
        }

        // If email wasn't properly entered
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErr(getResources().getString(R.string.insEm));
            emailLt.requestFocus();
            erro = true;
        }

        // If name wasn't properly entered
        if(name.isEmpty()) {
            showErr(getResources().getString(R.string.insNm));
            nameLt.requestFocus();
            erro = true;
        }

        // If there were no errors
        if(!erro) {
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        // If login was successful
                        if (task.isSuccessful()) {
                            // Gets user data
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            assert user != null;

                            // Adds basic user photo to profile
                            String photo = "https://firebasestorage.googleapis.com/v0/b/control-industries.appspot.com/o/user_photos%2Fbasic_user_photo%2Fbasic_user_photo.png?alt=media&token=3ab10e29-3bad-4920-b1bd-8efcb5cc1794";

                            // Adds user data to Firebase
                            UserModel dbUser = new UserModel("", email, "", name, photo, "");
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(user.getUid())
                                    .setValue(dbUser).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // Updates user profile (name and photo)
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name).setPhotoUri(Uri.parse(photo)).build();
                                            user.updateProfile(profileUpdates);

                                            // Redirects to SignIn
                                            Intent intent2 = new Intent(getApplicationContext(), SignIn.class);
                                            startActivity(intent2);
                                            animEnt = R.anim.slide_in_right;
                                            animExt = R.anim.slide_out_left;
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), R.string.cadFail, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        // If any error occurred
                        else {
                            // If account already exists
                            if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                showErr(getResources().getString(R.string.emJaCad));
                                emailLt.requestFocus();
                            }
                            // If any other error
                            else {
                                Toast.makeText(getApplicationContext(), R.string.cadFail, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // Creates Google SignIn request
    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Creates Google SignIn intent
    private void googleSignup() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Gets intent results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If Google SignIn intent
        if (requestCode == RC_SIGN_IN) {
            // Tries to sign user
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // If success, signs in Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // If any error occurred
                if (e.getStatusCode() != 12501) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Signs to Firebase with Google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                // If any error occurred
                if (!task.isSuccessful()) {
                    // Shows error
                    Toast.makeText(getApplicationContext(), "Login falhou, tente novamente", Toast.LENGTH_SHORT).show();
                }
                // If success
                else {
                    // If new User, adds data to Firebase
                    if (Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser()) {
                        // Obtém dados do usuário
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        assert user != null;

                        UserModel dbUser = new UserModel("", user.getEmail(), "", user.getDisplayName(), Objects.requireNonNull(user.getPhotoUrl()).toString(), "");
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(user.getUid())
                                .setValue(dbUser);
                    }

                    // Saves data and redirects to Home
                    saveData();
                    Intent intent2 = new Intent(getApplicationContext(), Home.class);
                    startActivity(intent2);
                    finish();
                }
            });
    }

    // Adds data to SharedPreferences
    public void saveData() {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        prefsEditor.putBoolean("blocked", false);
        prefsEditor.apply();
    }

    // Shows error
    private void showErr(String erro) {
        errLbl.setVisibility(View.VISIBLE);
        errLbl.setText(erro);
    }

    // When back button is pressed
    @Override
    public void onBackPressed() {
        // Redirects to SignIn
        Intent intent = new Intent(getApplicationContext(), SignIn.class);
        startActivity(intent);
        animEnt = R.anim.slide_in_left;
        animExt = R.anim.slide_out_right;
        finish();
    }

    // When the activity finishes
    @Override
    public void finish() {
        super.finish();
        // Adiciona transição
        overridePendingTransition(animEnt, animExt);
    }
}