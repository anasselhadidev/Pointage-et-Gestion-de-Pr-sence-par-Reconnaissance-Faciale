package com.example.projets4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.model.Matiere;
import com.example.projets4.utile.CoursesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeacherDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String professorEmail;
    private String professorNom;
    private String professorPrenom;

    private RecyclerView recyclerTodayCourses;
    private RecyclerView recyclerUpcomingCourses;
    private CoursesAdapter todayAdapter;
    private CoursesAdapter upcomingAdapter;
    private List<Matiere> todayCoursesList = new ArrayList<>();
    private List<Matiere> upcomingCoursesList = new ArrayList<>();

    private TextView tvBienvenue;
    private TextView tvDate;
    private TextView tvStats;

    // Cards pour les différentes fonctionnalités
    private CardView cardGestionSeance;
    private CardView cardRapports;
    private CardView cardJustificatifs;
    private CardView cardEmploiTemps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        professorEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Configuration de la date
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE);
        tvDate.setText(sdf.format(new Date()));

        // Chargement des données du professeur
        loadProfessorData();

        // Chargement des cours
        setupRecyclerViews();
        loadCourses();

        // Configuration des écouteurs de clics
        setupClickListeners();
    }

    private void initViews() {
        tvBienvenue = findViewById(R.id.tvBienvenue);
        tvDate = findViewById(R.id.tvDate);
        tvStats = findViewById(R.id.tvStats);

        recyclerTodayCourses = findViewById(R.id.recyclerTodayCourses);
        recyclerUpcomingCourses = findViewById(R.id.recyclerUpcomingCourses);

        cardGestionSeance = findViewById(R.id.cardGestionSeance);
        cardRapports = findViewById(R.id.cardRapports);
        cardJustificatifs = findViewById(R.id.cardJustificatifs);
        cardEmploiTemps = findViewById(R.id.cardEmploiTemps);
    }

    private void setupRecyclerViews() {
        // Configuration des RecyclerViews
        recyclerTodayCourses.setLayoutManager(new LinearLayoutManager(this));
        recyclerUpcomingCourses.setLayoutManager(new LinearLayoutManager(this));

        todayAdapter = new CoursesAdapter(todayCoursesList);
        upcomingAdapter = new CoursesAdapter(upcomingCoursesList);

        recyclerTodayCourses.setAdapter(todayAdapter);
        recyclerUpcomingCourses.setAdapter(upcomingAdapter);
    }

    private void loadProfessorData() {
        if (professorEmail.isEmpty()) {
            return;
        }

        db.collection("professeurs").document(professorEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        professorNom = documentSnapshot.getString("nom");
                        professorPrenom = documentSnapshot.getString("prenom");
                        tvBienvenue.setText("Bienvenue, " + professorPrenom + " " + professorNom);

                        // Chargement des statistiques personnelles
                        loadTeacherStats();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TeacherDashboardActivity.this,
                            "Erreur lors du chargement des données: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTeacherStats() {
        // Requête pour obtenir les statistiques d'assiduité des cours du professeur
        db.collection("cours")
                .whereEqualTo("professeurEmail", professorEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalCourses = queryDocumentSnapshots.size();
                    int totalStudents = 0;
                    int totalPresent = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Récupérer les statistiques de chaque cours
                        Long  studentCount = Long.parseLong(doc.getString("nombreEtudiants"));
                        Long  presentCount = Long.parseLong(doc.getString("nombrePresents"));

                        if (studentCount != null) totalStudents += studentCount.intValue();
                        if (presentCount != null) totalPresent += presentCount.intValue();
                    }

                    double presenceRate = totalStudents > 0 ? (double) totalPresent / totalStudents * 100 : 0;

                    tvStats.setText(String.format(Locale.getDefault(),
                            "Cours enseignés: %d | Taux de présence moyen: %.1f%%",
                            totalCourses, presenceRate));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TeacherDashboardActivity.this,
                            "Erreur lors du chargement des statistiques: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCourses() {
        if (professorEmail.isEmpty()) {
            return;
        }

        // Date du jour au format "yyyy-MM-dd"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Chargement des cours du jour
        db.collection("seances")
                .whereEqualTo("professeurEmail", professorEmail)
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todayCoursesList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String coursId = doc.getString("coursId");
                        String titre = doc.getString("titre");
                        String heureDebut = doc.getString("heureDebut");
                        String heureFin = doc.getString("heureFin");
                        String salle = doc.getString("salle");

                        String schedule = "📅 " + today + ", " + heureDebut + " - " + heureFin;
                        String room = "📍 Salle " + salle;

                        // Récupération du taux de présence si la séance est en cours ou terminée
                        Long  nombreEtudiants = Long.parseLong(doc.getString("nombreEtudiants"));
                        Long  nombrePresents = Long.parseLong(doc.getString("nombrePresents"));
                        int progress = 0;
                        String status = "À venir";

                        if (nombreEtudiants != null && nombreEtudiants > 0 && nombrePresents != null) {
                            progress = (int) ((double) nombrePresents / nombreEtudiants * 100);
                            status = doc.getBoolean("terminee") ? "Terminé" : "En cours";
                        }

                        Matiere matiere = new Matiere(titre, schedule, room, progress, status);
                        todayCoursesList.add(matiere);
                    }

                    todayAdapter.notifyDataSetChanged();

                    // S'il n'y a pas de cours aujourd'hui, afficher un message
                    if (todayCoursesList.isEmpty()) {
                        // Ajouter une note "Pas de cours aujourd'hui"
                        findViewById(R.id.tvNoCoursesToday).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.tvNoCoursesToday).setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TeacherDashboardActivity.this,
                            "Erreur lors du chargement des cours: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        // Chargement des prochains cours (à venir)
        db.collection("seances")
                .whereEqualTo("professeurEmail", professorEmail)
                .whereGreaterThan("date", today)
                .orderBy("date")
                .limit(3) // Limiter aux 3 prochains cours
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    upcomingCoursesList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String titre = doc.getString("titre");
                        String date = doc.getString("date");
                        String heureDebut = doc.getString("heureDebut");
                        String heureFin = doc.getString("heureFin");
                        String salle = doc.getString("salle");

                        String schedule = "📅 " + date + ", " + heureDebut + " - " + heureFin;
                        String room = "📍 Salle " + salle;

                        Matiere matiere = new Matiere(titre, schedule, room, 0, "À venir");
                        upcomingCoursesList.add(matiere);
                    }

                    upcomingAdapter.notifyDataSetChanged();

                    // S'il n'y a pas de cours à venir, afficher un message
                    if (upcomingCoursesList.isEmpty()) {
                        findViewById(R.id.tvNoUpcomingCourses).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.tvNoUpcomingCourses).setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TeacherDashboardActivity.this,
                            "Erreur lors du chargement des cours à venir: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        cardGestionSeance.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, SessionManagementActivity.class);
            startActivity(intent);
        });

        cardRapports.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, AttendanceReportActivity.class);
            startActivity(intent);
        });

        cardJustificatifs.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, JustificationManagerActivity.class);
            startActivity(intent);
        });

        cardEmploiTemps.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, ConsulterEmploiActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnDeconnexion).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(TeacherDashboardActivity.this, AuthentificationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données à chaque retour sur l'écran
        loadCourses();
        loadTeacherStats();
    }
}