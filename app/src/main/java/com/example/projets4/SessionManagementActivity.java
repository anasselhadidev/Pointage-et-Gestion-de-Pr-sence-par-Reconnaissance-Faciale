package com.example.projets4;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.adapters.StudentAttendanceAdapter;
import com.example.projets4.model.Etudiant;
import com.example.projets4.model.Seance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SessionManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinnerCours;
    private TextView tvNoSession;
    private TextView tvSessionInfo;
    private Button btnStartSession;
    private Button btnEndSession;
    private RecyclerView recyclerStudents;
    private FloatingActionButton fabRefresh;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String professorEmail;

    private List<Seance> seancesList = new ArrayList<>();
    private List<String> coursNames = new ArrayList<>();
    private List<Etudiant> studentsList = new ArrayList<>();
    private StudentAttendanceAdapter adapter;

    private Seance currentSeance;
    private boolean sessionActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_management);

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
            getSupportActionBar().setTitle("Gestion de séance");
        }

        // Initialisation du RecyclerView
        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAttendanceAdapter(studentsList, this::onStatusChangeRequested);
        recyclerStudents.setAdapter(adapter);

        // Chargement des cours du professeur
        loadProfessorCourses();

        // Configuration des écouteurs
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerCours = findViewById(R.id.spinnerCours);
        tvNoSession = findViewById(R.id.tvNoSession);
        tvSessionInfo = findViewById(R.id.tvSessionInfo);
        btnStartSession = findViewById(R.id.btnStartSession);
        btnEndSession = findViewById(R.id.btnEndSession);
        recyclerStudents = findViewById(R.id.recyclerStudents);
        fabRefresh = findViewById(R.id.fabRefresh);

        // Par défaut, cacher les éléments de session active
        tvSessionInfo.setVisibility(View.GONE);
        btnEndSession.setVisibility(View.GONE);
        recyclerStudents.setVisibility(View.GONE);
        fabRefresh.setVisibility(View.GONE);
    }

    private void setupListeners() {
        spinnerCours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Ignorer l'item "Sélectionner un cours"
                    Seance selectedSeance = seancesList.get(position - 1);
                    checkSessionStatus(selectedSeance);
                } else {
                    // Réinitialiser l'interface
                    tvNoSession.setVisibility(View.VISIBLE);
                    tvSessionInfo.setVisibility(View.GONE);
                    btnStartSession.setVisibility(View.GONE);
                    btnEndSession.setVisibility(View.GONE);
                    recyclerStudents.setVisibility(View.GONE);
                    fabRefresh.setVisibility(View.GONE);
                    studentsList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ne rien faire
            }
        });

        btnStartSession.setOnClickListener(v -> startSession());
        btnEndSession.setOnClickListener(v -> confirmEndSession());
        fabRefresh.setOnClickListener(v -> refreshStudentList());
    }

    private void loadProfessorCourses() {
        if (professorEmail.isEmpty()) {
            return;
        }

        // Date du jour au format "yyyy-MM-dd"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Chargement des séances du jour pour ce professeur
        db.collection("seances")
                .whereEqualTo("professeurEmail", professorEmail)
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    seancesList.clear();
                    coursNames.clear();

                    // Ajouter l'élément par défaut
                    coursNames.add("Sélectionner un cours");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Seance seance = doc.toObject(Seance.class);
                        seance.setId(doc.getId());
                        seancesList.add(seance);

                        // Format: Titre (Heure début - Heure fin, Salle)
                        String courseDisplay = seance.getTitre() + " (" +
                                seance.getHeureDebut() + " - " +
                                seance.getHeureFin() + ", Salle " +
                                seance.getSalle() + ")";
                        coursNames.add(courseDisplay);
                    }

                    // Adapter pour le spinner
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            SessionManagementActivity.this,
                            android.R.layout.simple_spinner_item,
                            coursNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCours.setAdapter(adapter);

                    // Si aucune séance aujourd'hui
                    if (seancesList.isEmpty()) {
                        tvNoSession.setText("Vous n'avez pas de cours aujourd'hui");
                        tvNoSession.setVisibility(View.VISIBLE);
                        btnStartSession.setVisibility(View.GONE);
                    } else {
                        tvNoSession.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Erreur lors du chargement des cours: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void checkSessionStatus(Seance seance) {
        currentSeance = seance;

        // Vérifier si la séance est déjà commencée
        if (currentSeance.isCommencee()) {
            // La séance est déjà commencée
            sessionActive = true;

            // Mise à jour de l'interface
            tvNoSession.setVisibility(View.GONE);
            tvSessionInfo.setVisibility(View.VISIBLE);
            tvSessionInfo.setText("Séance en cours: " + currentSeance.getTitre() + "\n" +
                    "Démarrée à: " + currentSeance.getHeureDebutEffective());

            btnStartSession.setVisibility(View.GONE);
            btnEndSession.setVisibility(View.VISIBLE);
            recyclerStudents.setVisibility(View.VISIBLE);
            fabRefresh.setVisibility(View.VISIBLE);

            // Charger la liste des étudiants présents
            loadStudentList();
        } else if (currentSeance.isTerminee()) {
            // La séance est terminée
            sessionActive = false;

            // Mise à jour de l'interface
            tvNoSession.setVisibility(View.VISIBLE);
            tvNoSession.setText("Cette séance est déjà terminée");
            tvSessionInfo.setVisibility(View.GONE);
            btnStartSession.setVisibility(View.GONE);
            btnEndSession.setVisibility(View.GONE);
            recyclerStudents.setVisibility(View.GONE);
            fabRefresh.setVisibility(View.GONE);
        } else {
            // La séance n'est pas encore commencée
            sessionActive = false;

            // Mise à jour de l'interface
            tvNoSession.setVisibility(View.GONE);
            tvSessionInfo.setVisibility(View.GONE);
            btnStartSession.setVisibility(View.VISIBLE);
            btnEndSession.setVisibility(View.GONE);
            recyclerStudents.setVisibility(View.GONE);
            fabRefresh.setVisibility(View.GONE);
        }
    }

    private void startSession() {
        if (currentSeance == null) {
            Toast.makeText(this, "Veuillez sélectionner un cours", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enregistrer l'heure de début effective
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());

        // Mettre à jour le document dans Firestore
        db.collection("seances").document(currentSeance.getId())
                .update("commencee", true,
                        "heureDebutEffective", currentTime)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Séance démarrée avec succès", Toast.LENGTH_SHORT).show();

                    // Mettre à jour l'objet local
                    currentSeance.setCommencee(true);
                    currentSeance.setHeureDebutEffective(currentTime);

                    // Mettre à jour l'interface
                    sessionActive = true;
                    tvSessionInfo.setVisibility(View.VISIBLE);
                    tvSessionInfo.setText("Séance en cours: " + currentSeance.getTitre() + "\n" +
                            "Démarrée à: " + currentTime);

                    btnStartSession.setVisibility(View.GONE);
                    btnEndSession.setVisibility(View.VISIBLE);
                    recyclerStudents.setVisibility(View.VISIBLE);
                    fabRefresh.setVisibility(View.VISIBLE);

                    // Charger la liste des étudiants (vide au début)
                    loadStudentList();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Erreur lors du démarrage de la séance: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmEndSession() {
        new AlertDialog.Builder(this)
                .setTitle("Terminer la séance")
                .setMessage("Êtes-vous sûr de vouloir terminer cette séance ? Les données de présence seront figées.")
                .setPositiveButton("Terminer", (dialog, which) -> endSession())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void endSession() {
        if (currentSeance == null) {
            return;
        }

        // Enregistrer l'heure de fin effective
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());

        // Calculer les statistiques
        int totalEtudiants = studentsList.size();
        int presents = 0;
        int retards = 0;
        int absents = 0;

        for (Etudiant etudiant : studentsList) {
            switch (etudiant.getStatut()) {
                case "Présent":
                    presents++;
                    break;
                case "Retard":
                    retards++;
                    presents++; // Un retard est aussi une présence
                    break;
                case "Absent":
                    absents++;
                    break;
            }
        }

        // Préparation des données à mettre à jour
        Map<String, Object> updates = new HashMap<>();
        updates.put("terminee", true);
        updates.put("heureFinEffective", currentTime);
        updates.put("nombreEtudiants", totalEtudiants);
        updates.put("nombrePresents", presents);
        updates.put("nombreRetards", retards);
        updates.put("nombreAbsents", absents);

        // Mettre à jour le document dans Firestore
        int finalPresents = presents;
        db.collection("seances").document(currentSeance.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Séance terminée avec succès", Toast.LENGTH_SHORT).show();

                    // Mettre à jour l'interface
                    sessionActive = false;
                    tvNoSession.setVisibility(View.VISIBLE);
                    tvNoSession.setText("Séance terminée: " + currentSeance.getTitre() + "\n" +
                            "Taux de présence: " + (finalPresents * 100 / totalEtudiants) + "%");

                    tvSessionInfo.setVisibility(View.GONE);
                    btnStartSession.setVisibility(View.GONE);
                    btnEndSession.setVisibility(View.GONE);
                    recyclerStudents.setVisibility(View.GONE);
                    fabRefresh.setVisibility(View.GONE);

                    // Générer un rapport automatique
                    generateAttendanceReport();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Erreur lors de la fin de séance: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadStudentList() {
        if (currentSeance == null) {
            return;
        }

        // Charger les étudiants inscrits au cours
        db.collection("cours").document(currentSeance.getCoursId())
                .collection("etudiants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentsList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String etudiantEmail = doc.getId();

                        // Récupérer les informations de l'étudiant
                        db.collection("etudiants").document(etudiantEmail)
                                .get()
                                .addOnSuccessListener(etudiantDoc -> {
                                    if (etudiantDoc.exists()) {
                                        Etudiant etudiant = new Etudiant();
                                        etudiant.setEmail(etudiantEmail);
                                        etudiant.setNom(etudiantDoc.getString("nom"));
                                        etudiant.setPrenom(etudiantDoc.getString("prenom"));

                                        // Vérifier si l'étudiant est déjà présent dans la séance
                                        checkStudentAttendance(etudiant);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Erreur lors du chargement des étudiants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void checkStudentAttendance(Etudiant etudiant) {
        if (currentSeance == null) {
            return;
        }

        // Vérifier si l'étudiant a un pointage pour cette séance
        db.collection("pointages")
                .whereEqualTo("seanceId", currentSeance.getId())
                .whereEqualTo("etudiantEmail", etudiant.getEmail())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // L'étudiant a déjà un pointage
                        DocumentSnapshot pointageDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String statut = pointageDoc.getString("statut");
                        etudiant.setStatut(statut);
                        etudiant.setHeurePointage(pointageDoc.getString("heurePointage"));
                    } else {
                        // L'étudiant n'a pas encore de pointage
                        etudiant.setStatut("Absent");
                        etudiant.setHeurePointage("");
                    }

                    // Ajouter l'étudiant à la liste
                    studentsList.add(etudiant);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Erreur lors de la vérification des présences: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void refreshStudentList() {
        Toast.makeText(this, "Actualisation des présences...", Toast.LENGTH_SHORT).show();
        loadStudentList();
    }

    private void onStatusChangeRequested(Etudiant etudiant, String newStatus) {
        if (currentSeance == null || !sessionActive) {
            return;
        }

        // Afficher une boîte de dialogue de confirmation
        new AlertDialog.Builder(this)
                .setTitle("Modifier le statut")
                .setMessage("Voulez-vous changer le statut de " + etudiant.getPrenom() + " " +
                        etudiant.getNom() + " à " + newStatus + " ?")
                .setPositiveButton("Confirmer", (dialog, which) -> updateStudentStatus(etudiant, newStatus))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void updateStudentStatus(Etudiant etudiant, String newStatus) {
        if (currentSeance == null) {
            return;
        }

        // Heure actuelle
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());

        // Préparer les données du pointage
        Map<String, Object> pointageData = new HashMap<>();
        pointageData.put("etudiantEmail", etudiant.getEmail());
        pointageData.put("seanceId", currentSeance.getId());
        pointageData.put("coursId", currentSeance.getCoursId());
        pointageData.put("statut", newStatus);
        pointageData.put("heurePointage", currentTime);
        pointageData.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        pointageData.put("professeurEmail", professorEmail);
        pointageData.put("modifieManuel", true);

        // Vérifier si l'étudiant a déjà un pointage
        db.collection("pointages")
                .whereEqualTo("seanceId", currentSeance.getId())
                .whereEqualTo("etudiantEmail", etudiant.getEmail())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Mise à jour du pointage existant
                        String pointageId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("pointages").document(pointageId)
                                .update("statut", newStatus,
                                        "heurePointage", currentTime,
                                        "modifieManuel", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SessionManagementActivity.this,
                                            "Statut mis à jour", Toast.LENGTH_SHORT).show();

                                    // Mettre à jour l'interface
                                    etudiant.setStatut(newStatus);
                                    etudiant.setHeurePointage(currentTime);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SessionManagementActivity.this,
                                            "Erreur lors de la mise à jour: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Création d'un nouveau pointage
                        db.collection("pointages")
                                .add(pointageData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(SessionManagementActivity.this,
                                            "Présence enregistrée", Toast.LENGTH_SHORT).show();

                                    // Mettre à jour l'interface
                                    etudiant.setStatut(newStatus);
                                    etudiant.setHeurePointage(currentTime);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SessionManagementActivity.this,
                                            "Erreur lors de l'enregistrement: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Erreur lors de la vérification: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateAttendanceReport() {
        if (currentSeance == null) {
            return;
        }

        // Génération d'un rapport automatique
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("seanceId", currentSeance.getId());
        reportData.put("coursId", currentSeance.getCoursId());
        reportData.put("titre", "Rapport de présence - " + currentSeance.getTitre());
        reportData.put("dateGeneration", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        reportData.put("professeurEmail", professorEmail);
        reportData.put("nombreEtudiants", studentsList.size());

        int presents = 0;
        int retards = 0;
        int absents = 0;

        for (Etudiant etudiant : studentsList) {
            switch (etudiant.getStatut()) {
                case "Présent":
                    presents++;
                    break;
                case "Retard":
                    retards++;
                    break;
                case "Absent":
                    absents++;
                    break;
            }
        }

        reportData.put("nombrePresents", presents);
        reportData.put("nombreRetards", retards);
        reportData.put("nombreAbsents", absents);
        reportData.put("tauxPresence", (presents + retards) * 100 / studentsList.size());

        // Enregistrer le rapport dans Firestore
        db.collection("rapports")
                .add(reportData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Rapport généré automatiquement", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SessionManagementActivity.this,
                            "Erreur lors de la génération du rapport: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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