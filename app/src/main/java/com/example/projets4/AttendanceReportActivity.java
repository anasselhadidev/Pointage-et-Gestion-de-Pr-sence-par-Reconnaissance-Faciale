package com.example.projets4;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.adapters.ReportAdapter;
import com.example.projets4.model.Rapport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceReportActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinnerTypeRapport;
    private TextView tvDateDebut;
    private TextView tvDateFin;
    private Button btnDateDebut;
    private Button btnDateFin;
    private Button btnGenererRapport;
    private RecyclerView recyclerRapports;
    private TextView tvNoReports;
    private TextView tvStatsSummary;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String professorEmail;

    private List<Rapport> reportsList = new ArrayList<>();
    private ReportAdapter adapter;

    private Calendar dateDebutCal = Calendar.getInstance();
    private Calendar dateFinCal = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

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
            getSupportActionBar().setTitle("Rapports de présence");
        }

        // Configuration du spinner des types de rapport
        setupSpinner();

        // Configuration du RecyclerView
        recyclerRapports.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(reportsList);
        recyclerRapports.setAdapter(adapter);

        // Initialisation des dates
        initDates();

        // Configuration des écouteurs
        setupListeners();

        // Chargement des rapports existants
        loadReports();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerTypeRapport = findViewById(R.id.spinnerTypeRapport);
        tvDateDebut = findViewById(R.id.tvDateDebut);
        tvDateFin = findViewById(R.id.tvDateFin);
        btnDateDebut = findViewById(R.id.btnDateDebut);
        btnDateFin = findViewById(R.id.btnDateFin);
        btnGenererRapport = findViewById(R.id.btnGenererRapport);
        recyclerRapports = findViewById(R.id.recyclerRapports);
        tvNoReports = findViewById(R.id.tvNoReports);
        tvStatsSummary = findViewById(R.id.tvStatsSummary);
    }

    private void setupSpinner() {
        // Types de rapports disponibles
        List<String> reportTypes = new ArrayList<>();
        reportTypes.add("Tous les rapports");
        reportTypes.add("Rapport par cours");
        reportTypes.add("Rapport par période");
        reportTypes.add("Rapport d'assiduité");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, reportTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeRapport.setAdapter(adapter);
    }

    private void initDates() {
        // Date de début : 30 jours avant aujourd'hui
        dateDebutCal.add(Calendar.DAY_OF_MONTH, -30);

        // Mise à jour des TextView
        tvDateDebut.setText(dateFormat.format(dateDebutCal.getTime()));
        tvDateFin.setText(dateFormat.format(dateFinCal.getTime()));
    }

    private void setupListeners() {
        btnDateDebut.setOnClickListener(v -> showDatePickerDialog(true));
        btnDateFin.setOnClickListener(v -> showDatePickerDialog(false));
        btnGenererRapport.setOnClickListener(v -> generateReport());
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = isStartDate ? dateDebutCal : dateFinCal;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    // Vérifier que la date de début est avant la date de fin
                    if (isStartDate && selectedDate.after(dateFinCal)) {
                        Toast.makeText(AttendanceReportActivity.this,
                                "La date de début doit être avant la date de fin",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!isStartDate && selectedDate.before(dateDebutCal)) {
                        Toast.makeText(AttendanceReportActivity.this,
                                "La date de fin doit être après la date de début",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Mettre à jour la date
                    if (isStartDate) {
                        dateDebutCal = selectedDate;
                        tvDateDebut.setText(dateFormat.format(dateDebutCal.getTime()));
                    } else {
                        dateFinCal = selectedDate;
                        tvDateFin.setText(dateFormat.format(dateFinCal.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadReports() {
        if (professorEmail.isEmpty()) {
            return;
        }

        // Chargement des rapports du professeur
        db.collection("rapports")
                .whereEqualTo("professeurEmail", professorEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportsList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Rapport rapport = doc.toObject(Rapport.class);
                        rapport.setId(doc.getId());
                        reportsList.add(rapport);
                    }

                    // Tri par date de génération (le plus récent d'abord)
                    Collections.sort(reportsList, (r1, r2) -> {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            Date date1 = sdf.parse(r1.getDateGeneration());
                            Date date2 = sdf.parse(r2.getDateGeneration());
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            return 0;
                        }
                    });

                    adapter.notifyDataSetChanged();

                    // Afficher un message si aucun rapport n'est disponible
                    if (reportsList.isEmpty()) {
                        tvNoReports.setVisibility(View.VISIBLE);
                        recyclerRapports.setVisibility(View.GONE);
                    } else {
                        tvNoReports.setVisibility(View.GONE);
                        recyclerRapports.setVisibility(View.VISIBLE);

                        // Calculer et afficher les statistiques globales
                        calculateGlobalStats();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AttendanceReportActivity.this,
                            "Erreur lors du chargement des rapports: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void calculateGlobalStats() {
        int totalSeances = reportsList.size();
        int totalEtudiants = 0;
        int totalPresents = 0;
        int totalRetards = 0;
        int totalAbsents = 0;

        // Collecter les données de tous les rapports
        for (Rapport rapport : reportsList) {
            totalEtudiants += rapport.getNombreEtudiants();
            totalPresents += rapport.getNombrePresents();
            totalRetards += rapport.getNombreRetards();
            totalAbsents += rapport.getNombreAbsents();
        }

        // Calculer les taux
        double tauxPresence = totalEtudiants > 0 ?
                (double) (totalPresents) / totalEtudiants * 100 : 0;
        double tauxRetard = totalPresents > 0 ?
                (double) totalRetards / totalPresents * 100 : 0;
        double tauxAbsence = totalEtudiants > 0 ?
                (double) totalAbsents / totalEtudiants * 100 : 0;

        // Afficher les statistiques
        String statsText = String.format(Locale.getDefault(),
                "Séances analysées: %d\n" +
                        "Taux de présence global: %.1f%%\n" +
                        "Taux de retard: %.1f%%\n" +
                        "Taux d'absence: %.1f%%",
                totalSeances, tauxPresence, tauxRetard, tauxAbsence);

        tvStatsSummary.setText(statsText);
    }

    private void generateReport() {
        String reportType = spinnerTypeRapport.getSelectedItem().toString();
        String dateDebut = tvDateDebut.getText().toString();
        String dateFin = tvDateFin.getText().toString();

        Toast.makeText(this, "Génération du rapport en cours...", Toast.LENGTH_SHORT).show();

        // Préparation des données du rapport
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("titre", "Rapport - " + reportType);
        reportData.put("professeurEmail", professorEmail);
        reportData.put("dateGeneration", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        reportData.put("dateDebut", dateDebut);
        reportData.put("dateFin", dateFin);
        reportData.put("typeRapport", reportType);

        // Requête Firestore pour recueillir les données nécessaires
        db.collection("seances")
                .whereEqualTo("professeurEmail", professorEmail)
                .whereGreaterThanOrEqualTo("date", dateDebut)
                .whereLessThanOrEqualTo("date", dateFin)
                .get()
                .addOnSuccessListener(seancesSnapshot -> {
                    int totalEtudiants = 0;
                    int totalPresents = 0;
                    int totalRetards = 0;
                    int totalAbsents = 0;
                    int totalSeances = seancesSnapshot.size();

                    // Traitement de chaque séance
                    for (QueryDocumentSnapshot seanceDoc : seancesSnapshot) {
                        Integer nombreEtudiants = seanceDoc.getLong("nombreEtudiants") != null ?
                                seanceDoc.getLong("nombreEtudiants").intValue() : 0;
                        Integer nombrePresents = seanceDoc.getLong("nombrePresents") != null ?
                                seanceDoc.getLong("nombrePresents").intValue() : 0;
                        Integer nombreRetards = seanceDoc.getLong("nombreRetards") != null ?
                                seanceDoc.getLong("nombreRetards").intValue() : 0;
                        Integer nombreAbsents = seanceDoc.getLong("nombreAbsents") != null ?
                                seanceDoc.getLong("nombreAbsents").intValue() : 0;

                        totalEtudiants += nombreEtudiants;
                        totalPresents += nombrePresents;
                        totalRetards += nombreRetards;
                        totalAbsents += nombreAbsents;
                    }

                    // Calcul des statistiques globales
                    double tauxPresence = totalEtudiants > 0 ?
                            (double) totalPresents / totalEtudiants * 100 : 0;
                    double tauxRetard = totalPresents > 0 ?
                            (double) totalRetards / totalPresents * 100 : 0;
                    double tauxAbsence = totalEtudiants > 0 ?
                            (double) totalAbsents / totalEtudiants * 100 : 0;

                    // Ajout des statistiques au rapport
                    reportData.put("nombreSeances", totalSeances);
                    reportData.put("nombreEtudiants", totalEtudiants);
                    reportData.put("nombrePresents", totalPresents);
                    reportData.put("nombreRetards", totalRetards);
                    reportData.put("nombreAbsents", totalAbsents);
                    reportData.put("tauxPresence", tauxPresence);
                    reportData.put("tauxRetard", tauxRetard);
                    reportData.put("tauxAbsence", tauxAbsence);

                    // Enregistrement du rapport dans Firestore
                    db.collection("rapports")
                            .add(reportData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AttendanceReportActivity.this,
                                        "Rapport généré avec succès",
                                        Toast.LENGTH_SHORT).show();

                                // Recharger la liste des rapports
                                loadReports();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AttendanceReportActivity.this,
                                        "Erreur lors de la génération du rapport: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AttendanceReportActivity.this,
                            "Erreur lors de la collecte des données: " + e.getMessage(),
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