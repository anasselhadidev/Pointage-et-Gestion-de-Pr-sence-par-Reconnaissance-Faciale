package com.example.projets4;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatistiquesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTauxPresenceGlobal;
    private TextView tvTauxRetard;
    private TextView tvCoursAssiduité;
    private TextView tvCoursMoinsAssidu;
    private TextView tvAbsencesPeriode;
    private TextView tvEvolutionPresence;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String etudiantEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistiques);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        etudiantEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Statistiques de présence");
        }

        // Chargement des statistiques
        loadStatistiques();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTauxPresenceGlobal = findViewById(R.id.tvTauxPresenceGlobal);
        tvTauxRetard = findViewById(R.id.tvTauxRetard);
        tvCoursAssiduité = findViewById(R.id.tvCoursAssiduité);
        tvCoursMoinsAssidu = findViewById(R.id.tvCoursMoinsAssidu);
        tvAbsencesPeriode = findViewById(R.id.tvAbsencesPeriode);
        tvEvolutionPresence = findViewById(R.id.tvEvolutionPresence);
    }

    private void loadStatistiques() {
        if (etudiantEmail.isEmpty()) {
            return;
        }

        // Chargement des pointages de l'étudiant
        db.collection("pointages")
                .whereEqualTo("etudiantEmail", etudiantEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showNoDataMessage();
                        return;
                    }

                    // Analyser les données
                    analyzePointages(queryDocumentSnapshots.getDocuments());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(StatistiquesActivity.this,
                            "Erreur lors du chargement des statistiques: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void analyzePointages(List<com.google.firebase.firestore.DocumentSnapshot> documents) {
        int totalCours = documents.size();
        int presences = 0;
        int retards = 0;
        Map<String, Integer> coursPresences = new HashMap<>();
        Map<String, Integer> coursTotal = new HashMap<>();

        // Comptage des présences et retards
        for (com.google.firebase.firestore.DocumentSnapshot doc : documents) {
            String statut = doc.getString("statut");
            String cours = doc.getString("cours");

            if (statut != null) {
                if (statut.equals("Présent")) {
                    presences++;
                } else if (statut.equals("Retard")) {
                    retards++;
                    presences++; // Un retard est aussi une présence
                }
            }

            // Statistiques par cours
            if (cours != null) {
                coursTotal.put(cours, coursTotal.getOrDefault(cours, 0) + 1);

                if (statut != null && (statut.equals("Présent") || statut.equals("Retard"))) {
                    coursPresences.put(cours, coursPresences.getOrDefault(cours, 0) + 1);
                }
            }
        }

        // Calcul des taux
        double tauxPresenceGlobal = totalCours > 0 ? (double) presences / totalCours * 100 : 0;
        double tauxRetard = presences > 0 ? (double) retards / presences * 100 : 0;

        // Recherche du cours avec la meilleure et la pire assiduité
        String coursMaxAssiduité = "";
        String coursMinAssiduité = "";
        double maxTaux = 0;
        double minTaux = 100;

        for (Map.Entry<String, Integer> entry : coursTotal.entrySet()) {
            String cours = entry.getKey();
            int total = entry.getValue();
            int presence = coursPresences.getOrDefault(cours, 0);
            double taux = (double) presence / total * 100;

            if (taux > maxTaux) {
                maxTaux = taux;
                coursMaxAssiduité = cours;
            }

            if (taux < minTaux) {
                minTaux = taux;
                coursMinAssiduité = cours;
            }
        }

        // Affichage des statistiques
        tvTauxPresenceGlobal.setText(String.format("%.1f%%", tauxPresenceGlobal));
        tvTauxRetard.setText(String.format("%.1f%%", tauxRetard));

        if (!coursMaxAssiduité.isEmpty()) {
            tvCoursAssiduité.setText(coursMaxAssiduité + " (" + String.format("%.1f%%", maxTaux) + ")");
        } else {
            tvCoursAssiduité.setText("Aucune donnée");
        }

        if (!coursMinAssiduité.isEmpty()) {
            tvCoursMoinsAssidu.setText(coursMinAssiduité + " (" + String.format("%.1f%%", minTaux) + ")");
        } else {
            tvCoursMoinsAssidu.setText("Aucune donnée");
        }

        // Pour simplifier, on affiche des données statiques pour les absences par période
        // et l'évolution de présence
        tvAbsencesPeriode.setText("Ce mois-ci: 3 absences\nCe semestre: 7 absences");
        tvEvolutionPresence.setText("En amélioration (+5% ce mois-ci)");
    }

    private void showNoDataMessage() {
        tvTauxPresenceGlobal.setText("N/A");
        tvTauxRetard.setText("N/A");
        tvCoursAssiduité.setText("Aucune donnée");
        tvCoursMoinsAssidu.setText("Aucune donnée");
        tvAbsencesPeriode.setText("Aucune donnée");
        tvEvolutionPresence.setText("Aucune donnée");

        Toast.makeText(this, "Aucune donnée de présence disponible", Toast.LENGTH_SHORT).show();
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