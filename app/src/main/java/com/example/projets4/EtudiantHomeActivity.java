package com.example.projets4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EtudiantHomeActivity extends AppCompatActivity {

    private TextView tvBienvenue;
    private CardView cardHistorique, cardJustificatif, cardStatistiques, cardEmploiTemps, cardProfile;
    private Button btnDeconnexion;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String etudiantEmail;
    private String etudiantNom;
    private String etudiantPrenom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiant_home);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        etudiantEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Chargement des données de l'étudiant
        loadEtudiantData();

        // Configuration des écouteurs de clics
        setupClickListeners();
    }

    private void initViews() {
        tvBienvenue = findViewById(R.id.tvBienvenue);
        cardHistorique = findViewById(R.id.cardHistorique);
        cardJustificatif = findViewById(R.id.cardJustificatif);
        cardStatistiques = findViewById(R.id.cardStatistiques);
        cardEmploiTemps = findViewById(R.id.cardEmploiTemps);
        cardProfile = findViewById(R.id.cardProfile);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);
    }

    private void loadEtudiantData() {
        if (etudiantEmail.isEmpty()) {
            return;
        }

        db.collection("etudiants").document(etudiantEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etudiantNom = documentSnapshot.getString("nom");
                        etudiantPrenom = documentSnapshot.getString("prenom");
                        tvBienvenue.setText("Bienvenue, " + etudiantPrenom + " " + etudiantNom);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EtudiantHomeActivity.this,
                            "Erreur lors du chargement des données: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        cardHistorique.setOnClickListener(v -> {
            Intent intent = new Intent(EtudiantHomeActivity.this, HistoriquePresenceActivity.class);
            startActivity(intent);
        });

        cardJustificatif.setOnClickListener(v -> {
            Intent intent = new Intent(EtudiantHomeActivity.this, SoumettreJustificatifActivity.class);
            startActivity(intent);
        });

        cardStatistiques.setOnClickListener(v -> {
            Intent intent = new Intent(EtudiantHomeActivity.this, StatistiquesActivity.class);
            startActivity(intent);
        });

        cardEmploiTemps.setOnClickListener(v -> {
            Intent intent = new Intent(EtudiantHomeActivity.this, ConsulterEmploiActivity.class);
            startActivity(intent);
        });

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(EtudiantHomeActivity.this, ProfileEtudiantActivity.class);
            startActivity(intent);
        });

        btnDeconnexion.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(EtudiantHomeActivity.this, AuthentificationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}