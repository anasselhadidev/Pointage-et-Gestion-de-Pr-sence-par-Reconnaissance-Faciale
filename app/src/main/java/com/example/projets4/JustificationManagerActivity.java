package com.example.projets4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.adapters.JustificationAdapter;
import com.example.projets4.model.Justificatif;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JustificationManagerActivity extends AppCompatActivity implements JustificationAdapter.OnJustificationActionListener {

    private Toolbar toolbar;
    private TextView tvNoJustifications;
    private RecyclerView recyclerJustifications;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String professorEmail;

    private List<Justificatif> justificatifList = new ArrayList<>();
    private JustificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_justification_manager);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        professorEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Configuration de la toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestion des justificatifs");
        }

        // Configuration du RecyclerView
        recyclerJustifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JustificationAdapter(justificatifList, this);
        recyclerJustifications.setAdapter(adapter);

        // Chargement des justificatifs
        loadJustifications();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvNoJustifications = findViewById(R.id.tvNoJustifications);
        recyclerJustifications = findViewById(R.id.recyclerJustifications);
    }

    private void loadJustifications() {
        if (professorEmail.isEmpty()) {
            return;
        }

        // Chargement des justificatifs pour les cours de ce professeur
        db.collection("cours")
                .whereEqualTo("professeurEmail", professorEmail)
                .get()
                .addOnSuccessListener(coursQuerySnapshot -> {
                    // Liste des IDs des cours du professeur
                    List<String> coursIds = new ArrayList<>();
                    for (QueryDocumentSnapshot coursDoc : coursQuerySnapshot) {
                        coursIds.add(coursDoc.getId());
                    }

                    if (coursIds.isEmpty()) {
                        tvNoJustifications.setVisibility(View.VISIBLE);
                        recyclerJustifications.setVisibility(View.GONE);
                        return;
                    }

                    // Chargement des justificatifs pour ces cours
                    db.collection("justificatifs")
                            .whereIn("coursId", coursIds)
                            .get()
                            .addOnSuccessListener(justificatifsQuerySnapshot -> {
                                justificatifList.clear();

                                for (QueryDocumentSnapshot justifDoc : justificatifsQuerySnapshot) {
                                    Justificatif justificatif = justifDoc.toObject(Justificatif.class);
                                    justificatif.setId(justifDoc.getId());
                                    justificatifList.add(justificatif);
                                }

                                // Tri par date de soumission (le plus récent d'abord)
                                Collections.sort(justificatifList, (j1, j2) -> j2.getDateSoumission().compareTo(j1.getDateSoumission()));

                                adapter.notifyDataSetChanged();

                                // Afficher un message si aucun justificatif n'est disponible
                                if (justificatifList.isEmpty()) {
                                    tvNoJustifications.setVisibility(View.VISIBLE);
                                    recyclerJustifications.setVisibility(View.GONE);
                                } else {
                                    tvNoJustifications.setVisibility(View.GONE);
                                    recyclerJustifications.setVisibility(View.VISIBLE);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(JustificationManagerActivity.this,
                                        "Erreur lors du chargement des justificatifs: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JustificationManagerActivity.this,
                            "Erreur lors du chargement des cours: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onViewDocument(Justificatif justificatif) {
        // Ouvrir le document dans une application externe
        if (justificatif.getDocumentUrl() != null && !justificatif.getDocumentUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(justificatif.getDocumentUrl()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "URL du document invalide", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onApprove(Justificatif justificatif) {
        // Approuver le justificatif
        db.collection("justificatifs").document(justificatif.getId())
                .update("status", "Accepté",
                        "professeurValidateurEmail", professorEmail,
                        "dateValidation", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(JustificationManagerActivity.this,
                            "Justificatif approuvé", Toast.LENGTH_SHORT).show();

                    // Mettre à jour les pointages correspondants
                    updateStudentAttendance(justificatif, true);

                    // Mettre à jour la liste
                    justificatif.setStatus("Accepté");
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JustificationManagerActivity.this,
                            "Erreur lors de l'approbation: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onReject(Justificatif justificatif) {
        // Rejeter le justificatif
        db.collection("justificatifs").document(justificatif.getId())
                .update("status", "Refusé",
                        "professeurValidateurEmail", professorEmail,
                        "dateValidation", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(JustificationManagerActivity.this,
                            "Justificatif refusé", Toast.LENGTH_SHORT).show();

                    // Mettre à jour la liste
                    justificatif.setStatus("Refusé");
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JustificationManagerActivity.this,
                            "Erreur lors du refus: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStudentAttendance(Justificatif justificatif, boolean isApproved) {
        if (isApproved) {
            // Si le justificatif est approuvé, mettre à jour les pointages correspondants à la date d'absence
            db.collection("pointages")
                    .whereEqualTo("etudiantEmail", justificatif.getEtudiantEmail())
                    .whereEqualTo("date", justificatif.getDateAbsence())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Marquer l'absence comme justifiée mais pas comme présence
                            document.getReference().update(
                                    "absenceJustifiee", true,
                                    "justificatifId", justificatif.getId());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(JustificationManagerActivity.this,
                                "Erreur lors de la mise à jour des pointages: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
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