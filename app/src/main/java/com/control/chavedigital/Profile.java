package com.control.chavedigital;

// Imports

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;
import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    // Variable declaration

    // APIs and others
    private FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageReference;

    // Views
    MaterialToolbar appBar;
    FloatingActionButton editPhoto;
    CircleImageView photo;
    LinearLayout btnName;
    TextView name;

    // Values
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;
    private final int TAKE_IMAGE_REQUEST = 11;
    private int animEnt, animExt;

    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        appBar = findViewById(R.id.appbar_profile);
        photo = findViewById(R.id.profilePhotoBtn);
        editPhoto = findViewById(R.id.editPhoto);
        btnName = findViewById(R.id.changeName);
        name = findViewById(R.id.name);
    }

    // When the activity starts
    public void onStart() {
        super.onStart();

        // Adds user name
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        name.setText(user.getDisplayName());

        // Adds user photo
        if(user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .apply(new RequestOptions()
                            .fitCenter()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL))
                    .into(photo);
        }

        // Sets onClickListeners
        initOnClick();
    }

    // Starts onClickListeners
    private void initOnClick() {
        // Appbar button
        appBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
            animEnt = R.anim.slide_in_left;
            animExt = R.anim.slide_out_right;
            finish();
        });
        // Photo Button
        photo.setOnClickListener(v -> popupPhoto());
        // EditPhoto Button
        editPhoto.setOnClickListener(v -> popupEditPhoto());
        // ChangeName Button
        btnName.setOnClickListener(v -> popupEditName());
    }

    // Photo Popup
    private void popupPhoto() {
        // Inflates popup
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_profilephoto, null);

        // Declares views and adds user photo
        ImageView ft = popupView.findViewById(R.id.profileShowPhoto);
        ft.setBackground(photo.getDrawable());

        // Creates dialog
        final AlertDialog dialog = new AlertDialog.Builder(Profile.this, R.style.AlertDialogTheme)
                .setView(popupView)
                .create();

        // Makes dialog background invisible
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Shows dialog
        dialog.show();
    }

    // Função para criar o popup de alteração do nome
    private void popupEditName() {
        // Gets user name
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String nm = user.getDisplayName();
        assert nm != null;

        // Inflates popup
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_name, null);

        // Declares views and adds user photo
        TextInputEditText name = popupView.findViewById(R.id.nameTxt);
        TextInputLayout nameLt = popupView.findViewById(R.id.nameLt);
        name.setText(nm);
        name.setSelection(nm.length());

        // Creates dialog
        final AlertDialog dialog = new AlertDialog.Builder(Profile.this, R.style.AlertDialogTheme)
                .setView(popupView)
                .setTitle(R.string.altNome)
                .setPositiveButton(getApplicationContext().getResources().getString(R.string.salvar), null)
                .create();

        // When dialog is created
        dialog.setOnShowListener(dialog1 -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> editName(dialog, name, nameLt));
        });

        // Shows dialog
        dialog.show();
    }

    // ChangeName Handler
    private void editName(AlertDialog dialog, TextInputEditText nameTxt, TextInputLayout nameLt) {
        // Cleans errors
        nameLt.setError(null);

        // Gets input
        String name = Objects.requireNonNull(nameTxt.getText()).toString();

        // Gets user profile data
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        // Value validation

        // If name wasn't properly entered
        if(name.equals("")) {
            nameLt.setErrorEnabled(true);
            nameLt.setError(getResources().getString(R.string.preench));
        }
        // If new name == current name
        else if (name.equals(user.getDisplayName())) {
            // Dismisses dialog
            dialog.dismiss();
        }
        else {
            // Changes user profile name
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    new FirebaseAction().setUserChild(user.getUid(), "name", name, error -> {
                        if(error == null) {
                            // Reloads activity
                            Intent intent = new Intent(getApplicationContext(), Profile.class);
                            startActivity(intent);
                            animEnt = 0;
                            animExt = 0;
                            finish();
                        }
                    });
                }
            });
        }
    }

    // ChangePhoto Popup
    private void popupEditPhoto() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Profile.this, R.style.AlertDialogTheme);

        // Inflates popup
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_photo, null);

        // Declares views
        LinearLayout btnCamEdit, btnGalEdit, btnDel;
        FloatingActionButton fabCam, fabGal, fabDel;
        btnCamEdit = popupView.findViewById(R.id.btnCamEdit);
        fabCam = popupView.findViewById(R.id.fabCamEdit);
        btnGalEdit = popupView.findViewById(R.id.btnGalEdit);
        fabGal = popupView.findViewById(R.id.fabGalEdit);
        btnDel = popupView.findViewById(R.id.btnDel);
        fabDel = popupView.findViewById(R.id.fabDel);

        // Sets onClickListeners
        btnCamEdit.setOnClickListener(v -> camera());
        fabCam.setOnClickListener(v -> camera());
        btnGalEdit.setOnClickListener(v -> gallery());
        fabGal.setOnClickListener(v -> gallery());
        btnDel.setOnClickListener(v -> delete());
        fabDel.setOnClickListener(v -> delete());

        // Creates dialog
        builder.setView(popupView);
        builder.setTitle(R.string.ftPerfil);

        // Shows dialog
        builder.show();
    }

    // Camera Intent
    private void camera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, TAKE_IMAGE_REQUEST);
    }

    // Gallery Intent
    private void gallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Selecione uma foto"),
                PICK_IMAGE_REQUEST);
    }

    // DeletePhoto Handler
    private void delete() {
        // Gets user basic photo
        String photoUrl = "https://firebasestorage.googleapis.com/v0/b/control-industries.appspot.com/o/user_photos%2Fbasic_user_photo%2Fbasic_user_photo.png?alt=media&token=3ab10e29-3bad-4920-b1bd-8efcb5cc1794";

        // Gets user profile data
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        if(Objects.requireNonNull(user.getPhotoUrl()).toString().equals(photoUrl)) {
            // Recarrega a Activity
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            animEnt = 0;
            animExt = 0;
            finish();
            return;
        }

        Toast.makeText(getApplicationContext(), R.string.aguarde, Toast.LENGTH_SHORT).show();

        // Updates user profile photo
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(photoUrl)).build();
        user.updateProfile(profileUpdates);

        // Changes photo in Firebase
        new FirebaseAction().setUserChild(user.getUid(), "photo", photoUrl, error -> {
            if(error == null) {
                // Confirma ao usuário
                Toast.makeText(getApplicationContext(), "Foto removida", Toast.LENGTH_SHORT).show();

                // Recarrega a Activity
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                startActivity(intent);
                animEnt = 0;
                animExt = 0;
                finish();
            }
        });
    }

    // Gets intent results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Camera Intent
        if (requestCode == TAKE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), R.string.aguarde, Toast.LENGTH_SHORT).show();

            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");

            // Saves image and upload to Firebase
            saveImage(bitmap);
            uploadImage();
        }

        // Gallery intent
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Toast.makeText(getApplicationContext(), R.string.aguarde, Toast.LENGTH_SHORT).show();

            // Saves image and upload to Firebase
            filePath = data.getData();
            uploadImage();
        }
    }

    // Saves image in device
    private void saveImage(Bitmap image) {
        // Creates image path
        String savedImagePath;
        String imageFileName = UUID.randomUUID().toString() + ".jpg";
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Chave Digital");

        // If it doesn't exist, creates
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        // Saves the photo
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception ignored) {
            }

            // Adds image to the system gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(savedImagePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            // Gets filepath
            filePath = Uri.fromFile(new File(savedImagePath));
        }
    }

    // Uploads image to FirebaseStorage
    private void uploadImage()
    {
        // If any filepath
        if (filePath != null) {
            // Gets user data
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;

            // Defines storage path
            StorageReference ref = storageReference.child("user_photos/" + user.getUid());

            // Uploads image
            ref.putFile(filePath)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    // If success

                    // Changes user profile photo
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri).build();
                    user.updateProfile(profileUpdates);

                    // Changes photo in Firebase
                    new FirebaseAction().setUserChild(user.getUid(), "photo", uri.toString(), error -> {
                        if(error == null) {
                            // Confirma ao usuário
                            Toast.makeText(getApplicationContext(), "Foto atualizada", Toast.LENGTH_SHORT).show();

                            // Recarrega a Activity
                            Intent intent = new Intent(getApplicationContext(), Profile.class);
                            startActivity(intent);
                            animEnt = 0;
                            animExt = 0;
                            finish();
                        }
                    });
                }))
                .addOnFailureListener(e ->  {
                    // If any error
                    Toast.makeText(Profile.this, "Erro ao atualizar a foto, tente novamente", Toast.LENGTH_LONG).show();
                });
        }
    }

    // When back button pressed
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

        // Adds transition
        overridePendingTransition(animEnt, animExt);
    }
}