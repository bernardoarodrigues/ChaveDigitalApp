package com.control.chavedigital;

// Imports

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignIn extends AppCompatActivity {

    // Variable declaration

    // APIs and others
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // Views
    TextInputLayout emailLt, passLt;
    TextInputEditText emailTxt, passTxt;
    MaterialButton logBtn;
    LinearLayout googleBtn;
    TextView forgotPassBtn, errLbl, redSignupBtn;

    // Values
    private final static int RC_SIGN_IN = 123;
    boolean erro = false;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Variable definition
        mAuth = FirebaseAuth.getInstance();
        googleBtn = findViewById(R.id.logGoogle);
        redSignupBtn = findViewById(R.id.redSignup);
        logBtn = findViewById(R.id.loginBtn);
        forgotPassBtn = findViewById(R.id.forgotPass);
        emailLt = findViewById(R.id.emailLogLt);
        passLt = findViewById(R.id.passLogLt);
        emailTxt = findViewById(R.id.emailLogTxt);
        passTxt = findViewById(R.id.passLogTxt);
        errLbl = findViewById(R.id.errLog);

        // Configures Google SignIn request
        createRequest();
    }

    // When the activity starts
    @Override
    public void onStart() {
        super.onStart();

        // Sets onClick listeners
        logBtn.setOnClickListener(v -> login());
        forgotPassBtn.setOnClickListener(v -> popupChangePass());
        googleBtn.setOnClickListener(v -> googleSignin());
        redSignupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUp.class);
            startActivity(intent);
            finish();
        });
    }

    // SignIn Handler
    private void login() {
        // Cleans errors
        errLbl.setVisibility(View.GONE);
        erro = false;

        // Gets input values
        String em = Objects.requireNonNull(emailTxt.getText()).toString().trim();
        String sen = Objects.requireNonNull(passTxt.getText()).toString().trim();

        // Input validation

        // If password wasn't properly entered
        if(sen.isEmpty() || sen.length() < 6) {
            showErr(getResources().getString(R.string.insSen));
            passLt.requestFocus();
            erro = true;
        }

        // If email wasn't properly entered
        if(em.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            showErr(getResources().getString(R.string.insEm));
            emailLt.requestFocus();
            erro = true;
        }

        // If there were no errors
        if(!erro) {
            mAuth.signInWithEmailAndPassword(em, sen).addOnCompleteListener(task -> {
                // If login was successful
                if(task.isSuccessful()) {
                    // Gets user data
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    assert user != null;

                    // If user's email is verified
                    if(user.isEmailVerified()) {
                        // Saves data and redirects to Home
                        saveData();
                        Intent intent2 = new Intent(getApplicationContext(), Home.class);
                        startActivity(intent2);
                        finish();
                    }
                    // Else, sends verification email
                    else {
                        user.sendEmailVerification();
                        Toast.makeText(getApplicationContext(), R.string.verifEm, Toast.LENGTH_LONG).show();
                    }
                }
                // If any error occurred
                else {
                    // If the account doesn't exist
                    if(task.getException() instanceof FirebaseAuthInvalidUserException) {
                        showErr(getResources().getString(R.string.containex));
                        emailLt.requestFocus();
                    }
                    // If the password is wrong
                    else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            showErr(getResources().getString(R.string.senhaInc));
                            passLt.requestFocus();
                        }
                        // Other error
                        else {
                            Toast.makeText(getApplicationContext(), R.string.loginFail, Toast.LENGTH_SHORT).show();
                        }
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
    private void googleSignin() {
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
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
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

    // ResetPassword Popup
    public void popupChangePass() {
        // Inflates popup
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_changepass, null);

        // Declares views
        TextInputEditText email = popupView.findViewById(R.id.emailTxt);
        TextInputLayout emailLt = popupView.findViewById(R.id.emailLt);

        // Creates dialog
        final AlertDialog dialog = new AlertDialog.Builder(SignIn.this, R.style.AlertDialogTheme)
                .setView(popupView)
                .setTitle(R.string.alterar)
                .setMessage(R.string.insiraem)
                .setPositiveButton(getApplicationContext().getResources().getString(R.string.enviar), null)
                .create();

        // When dialog is created
        dialog.setOnShowListener(dialog1 -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
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
        if(email.equals("")) {
            emailLt.setErrorEnabled(true);
            emailLt.setError(getResources().getString(R.string.insSeuEm));
        }
        else {
            // Sends reset password email
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                // If success
                if(task.isSuccessful()) {
                    // Dismisses dialog
                    dialog.dismiss();

                    // Confirms email was sent
                    Toast.makeText(getApplicationContext(), R.string.envEm, Toast.LENGTH_LONG).show();
                }
                // If any error occurred
                else {
                    // If the account doesn't exist
                    if(task.getException() instanceof FirebaseAuthInvalidUserException) {
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

    // When the activity finishes
    @Override
    public void finish() {
        super.finish();
        // Adds transition
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}