package com.example.projets4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileEtudiantActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivProfilePhoto;
    private TextView tvNomPrenom;
    private TextView tvNumeroEtudiant;
    private TextView tvFiliere;
    private EditText etEmail;
    private EditText etTelephone;
    private Button btnSelectPhoto;
    private Button btnSave;
    private Button btnChangePassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String etudiantEmail;

    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivProfilePhoto.setImageURI(selectedImageUri);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission refusée. Impossible de sélectionner une photo.",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_etudiant);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        etudiantEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mon profil");
        }

        // Chargement des données du profil
        loadProfileData();

        // Configuration des écouteurs de clics
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvNomPrenom = findViewById(R.id.tvNomPrenom);
        tvNumeroEtudiant = findViewById(R.id.tvNumeroEtudiant);
        tvFiliere = findViewById(R.id.tvFiliere);
        etEmail = findViewById(R.id.etEmail);
        etTelephone = findViewById(R.id.etTelephone);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnSave = findViewById(R.id.btnSave);
        btnChangePassword = findViewById(R.id.btnChangePassword);
    }

    private void setupClickListeners() {
        btnSelectPhoto.setOnClickListener(v -> checkPermissionAndOpenImagePicker());

        btnSave.setOnClickListener(v -> saveProfileChanges());

        btnChangePassword.setOnClickListener(v -> {
            if (etudiantEmail.isEmpty()) {
                Toast.makeText(this, "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            // Envoi d'un email de réinitialisation du mot de passe
            mAuth.sendPasswordResetEmail(etudiantEmail)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileEtudiantActivity.this,
                                "Email de réinitialisation envoyé à " + etudiantEmail,
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileEtudiantActivity.this,
                                "Erreur: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadProfileData() {
        if (etudiantEmail.isEmpty()) {
            return;
        }

        db.collection("etudiants").document(etudiantEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Informations générales
                        String nom = documentSnapshot.getString("nom");
                        String prenom = documentSnapshot.getString("prenom");
                        String numeroEtudiant = documentSnapshot.getString("numeroEtudiant");
                        String filiere = documentSnapshot.getString("filiere");
                        String email = documentSnapshot.getString("email");
                        String telephone = documentSnapshot.getString("phone");
                        String photoUrl = documentSnapshot.getString("photoUrl");

                        // Affichage des informations
                        tvNomPrenom.setText(prenom + " " + nom);
                        tvNumeroEtudiant.setText("N° Étudiant: " + (numeroEtudiant != null ? numeroEtudiant : "Non défini"));
                        tvFiliere.setText("Filière: " + (filiere != null ? filiere : "Non définie"));
                        etEmail.setText(email);
                        etTelephone.setText(telephone);

                        // Chargement de la photo si disponible
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            // Ici, nous aurions besoin d'une bibliothèque comme Glide ou Picasso
                            // pour charger l'image à partir de l'URL
                            // Pour simplifier, nous n'implémentons pas cette partie
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileEtudiantActivity.this,
                            "Erreur lors du chargement du profil: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfileChanges() {
        if (etudiantEmail.isEmpty()) {
            Toast.makeText(this, "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupération des valeurs modifiées
        String newEmail = etEmail.getText().toString().trim();
        String newTelephone = etTelephone.getText().toString().trim();

        // Validation des champs
        if (newEmail.isEmpty() || newTelephone.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactivation du bouton pendant la sauvegarde
        btnSave.setEnabled(false);

        // Préparation des données à mettre à jour
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", newEmail);
        updates.put("phone", newTelephone);

        // Si une nouvelle photo a été sélectionnée
        if (selectedImageUri != null) {
            // Chemin de stockage du fichier dans Firebase Storage
            String storagePath = "photos_profil/" + etudiantEmail;
            StorageReference storageRef = storage.getReference(storagePath);

            // Téléchargement de la photo
            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obtenir l'URL de téléchargement
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            updates.put("photoUrl", uri.toString());

                            // Mise à jour du profil dans Firestore
                            updateProfile(updates);
                        });
                    })
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(ProfileEtudiantActivity.this,
                                "Erreur lors du téléchargement de la photo: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Mise à jour du profil sans nouvelle photo
            updateProfile(updates);
        }
    }

    private void updateProfile(Map<String, Object> updates) {
        db.collection("etudiants").document(etudiantEmail)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(ProfileEtudiantActivity.this,
                            "Profil mis à jour avec succès",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(ProfileEtudiantActivity.this,
                            "Erreur lors de la mise à jour du profil: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void checkPermissionAndOpenImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}