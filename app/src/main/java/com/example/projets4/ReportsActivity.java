package com.example.projets4;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.app.DatePickerDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinnerReportType;
    private TextView tvStartDate, tvEndDate;
    private Button btnStartDate, btnEndDate, btnGenerateReport;
    private RecyclerView recyclerReports;
    private TextView tvNoReports;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String adminEmail;

    private Calendar startDateCal = Calendar.getInstance();
    private Calendar endDateCal = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        adminEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Rapports institutionnels");
        }

        // Configuration du spinner
        setupSpinner();

        // Initialisation des dates
        initDates();

        // Configuration des écouteurs
        setupListeners();

        // Chargement des rapports existants
        loadReports();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerReportType = findViewById(R.id.spinnerReportType);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        recyclerReports = findViewById(R.id.recyclerReports);
        tvNoReports = findViewById(R.id.tvNoReports);

        // Configuration du RecyclerView
        recyclerReports.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupSpinner() {
        // Types de rapports disponibles
        List<String> reportTypes = new ArrayList<>();
        reportTypes.add("Sélectionner un type de rapport");
        reportTypes.add("Rapport global de présence");
        reportTypes.add("Rapport par filière");
        reportTypes.add("Rapport par enseignant");
        reportTypes.add("Rapport d'utilisation des terminaux");
        reportTypes.add("Rapport d'anomalies");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, reportTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportType.setAdapter(adapter);
    }

    private void initDates() {
        // Date de début : premier jour du mois courant
        startDateCal.set(Calendar.DAY_OF_MONTH, 1);

        // Date de fin : aujourd'hui
        // endDateCal est déjà initialisé à la date du jour

        // Mise à jour des TextView
        tvStartDate.setText(dateFormat.format(startDateCal.getTime()));
        tvEndDate.setText(dateFormat.format(endDateCal.getTime()));
    }

    private void setupListeners() {
        btnStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        btnEndDate.setOnClickListener(v -> showDatePickerDialog(false));
        btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCal : endDateCal;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    // Vérifier que la date de début est avant la date de fin
                    if (isStartDate && selectedDate.after(endDateCal)) {
                        Toast.makeText(ReportsActivity.this,
                                "La date de début doit être avant la date de fin",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!isStartDate && selectedDate.before(startDateCal)) {
                        Toast.makeText(ReportsActivity.this,
                                "La date de fin doit être après la date de début",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Mettre à jour la date
                    if (isStartDate) {
                        startDateCal = selectedDate;
                        tvStartDate.setText(dateFormat.format(startDateCal.getTime()));
                    } else {
                        endDateCal = selectedDate;
                        tvEndDate.setText(dateFormat.format(endDateCal.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadReports() {
        // Chargement des rapports existants depuis Firestore
        db.collection("rapports_institutionnels")
                .whereEqualTo("generatedBy", adminEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoReports.setVisibility(android.view.View.VISIBLE);
                        recyclerReports.setVisibility(android.view.View.GONE);
                    } else {
                        tvNoReports.setVisibility(android.view.View.GONE);
                        recyclerReports.setVisibility(android.view.View.VISIBLE);

                        List<InstitutionalReport> reports = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            InstitutionalReport report = document.toObject(InstitutionalReport.class);
                            report.setId(document.getId());
                            reports.add(report);
                        }

                        // Tri par date de génération (plus récent en premier)
                        reports.sort((r1, r2) -> r2.getGenerationDate().compareTo(r1.getGenerationDate()));

                        // Configurer l'adaptateur et l'attacher au RecyclerView
                        InstitutionalReportAdapter adapter = new InstitutionalReportAdapter(reports);
                        recyclerReports.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReportsActivity.this,
                            "Erreur lors du chargement des rapports: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateReport() {
        String reportType = spinnerReportType.getSelectedItem().toString();

        // Vérifier qu'un type de rapport est sélectionné
        if (reportType.equals("Sélectionner un type de rapport")) {
            Toast.makeText(this, "Veuillez sélectionner un type de rapport", Toast.LENGTH_SHORT).show();
            return;
        }

        String startDate = tvStartDate.getText().toString();
        String endDate = tvEndDate.getText().toString();

        Toast.makeText(this, "Génération du rapport en cours...", Toast.LENGTH_SHORT).show();

        // Préparation des données du rapport
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("title", "Rapport - " + reportType);
        reportData.put("generatedBy", adminEmail);
        reportData.put("generationDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        reportData.put("startDate", startDate);
        reportData.put("endDate", endDate);
        reportData.put("reportType", reportType);

        // Collecter les données selon le type de rapport
        switch (reportType) {
            case "Rapport global de présence":
                generateGlobalReport(reportData);
                break;
            case "Rapport par filière":
                generateFiliereReport(reportData);
                break;
            case "Rapport par enseignant":
                generateEnseignantReport(reportData);
                break;
            case "Rapport d'utilisation des terminaux":
                generateTerminalReport(reportData);
                break;
            case "Rapport d'anomalies":
                generateAnomaliesReport(reportData);
                break;
            default:
                Toast.makeText(this, "Type de rapport non supporté", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void generateGlobalReport(Map<String, Object> reportData) {
        // Requête pour obtenir les données globales de présence
        db.collection("pointages")
                .whereGreaterThanOrEqualTo("date", reportData.get("startDate").toString())
                .whereLessThanOrEqualTo("date", reportData.get("endDate").toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalPointages = queryDocumentSnapshots.size();
                    int presents = 0;
                    int retards = 0;
                    int absents = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String statut = doc.getString("statut");
                        if (statut != null) {
                            switch (statut) {
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
                    }

                    // Ajouter les statistiques au rapport
                    reportData.put("totalPointages", totalPointages);
                    reportData.put("totalPresents", presents);
                    reportData.put("totalRetards", retards);
                    reportData.put("totalAbsents", absents);
                    reportData.put("tauxPresence", totalPointages > 0 ? (double) (presents + retards) / totalPointages * 100 : 0);

                    // Enregistrer le rapport dans Firestore
                    saveReport(reportData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReportsActivity.this,
                            "Erreur lors de la collecte des données: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void generateFiliereReport(Map<String, Object> reportData) {
        // Collecter les données par filière
        db.collection("filieres")
                .get()
                .addOnSuccessListener(filieresSnapshot -> {
                    Map<String, Object> statsByFiliere = new HashMap<>();

                    // Pour chaque filière, collecter les statistiques
                    for (QueryDocumentSnapshot filiereDoc : filieresSnapshot) {
                        String filiereId = filiereDoc.getId();
                        String filiereName = filiereDoc.getString("nom");

                        // Collect des données pour cette filière
                        collectFiliereData(filiereId, filiereName, statsByFiliere, reportData);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReportsActivity.this,
                            "Erreur lors de la collecte des filières: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void collectFiliereData(String filiereId, String filiereName, Map<String, Object> statsByFiliere, Map<String, Object> reportData) {
        // Requête pour obtenir les pointages pour les étudiants de cette filière
        db.collection("etudiants")
                .whereEqualTo("filiereId", filiereId)
                .get()
                .addOnSuccessListener(etudiantsSnapshot -> {
                    List<String> etudiantEmails = new ArrayList<>();
                    for (QueryDocumentSnapshot etudiantDoc : etudiantsSnapshot) {
                        etudiantEmails.add(etudiantDoc.getId());
                    }

                    if (etudiantEmails.isEmpty()) {
                        Map<String, Object> filiereStats = new HashMap<>();
                        filiereStats.put("nom", filiereName);
                        filiereStats.put("totalPointages", 0);
                        filiereStats.put("totalPresents", 0);
                        filiereStats.put("totalRetards", 0);
                        filiereStats.put("totalAbsents", 0);
                        filiereStats.put("tauxPresence", 0);

                        statsByFiliere.put(filiereId, filiereStats);

                        // Vérifier si c'est la dernière filière et sauvegarder si c'est le cas
                        if (statsByFiliere.size() == filieresCount) {
                            reportData.put("statsByFiliere", statsByFiliere);
                            saveReport(reportData);
                        }
                        return;
                    }

                    // Requête pour les pointages de ces étudiants
                    db.collection("pointages")
                            .whereIn("etudiantEmail", etudiantEmails)
                            .whereGreaterThanOrEqualTo("date", reportData.get("startDate").toString())
                            .whereLessThanOrEqualTo("date", reportData.get("endDate").toString())
                            .get()
                            .addOnSuccessListener(pointagesSnapshot -> {
                                int totalPointages = pointagesSnapshot.size();
                                int presents = 0;
                                int retards = 0;
                                int absents = 0;

                                for (QueryDocumentSnapshot doc : pointagesSnapshot) {
                                    String statut = doc.getString("statut");
                                    if (statut != null) {
                                        switch (statut) {
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
                                }

                                // Créer les statistiques pour cette filière
                                Map<String, Object> filiereStats = new HashMap<>();
                                filiereStats.put("nom", filiereName);
                                filiereStats.put("totalPointages", totalPointages);
                                filiereStats.put("totalPresents", presents);
                                filiereStats.put("totalRetards", retards);
                                filiereStats.put("totalAbsents", absents);
                                filiereStats.put("tauxPresence", totalPointages > 0 ? (double) (presents + retards) / totalPointages * 100 : 0);

                                statsByFiliere.put(filiereId, filiereStats);

                                // Vérifier si c'est la dernière filière et sauvegarder si c'est le cas
                                if (statsByFiliere.size() == filieresCount) {
                                    reportData.put("statsByFiliere", statsByFiliere);
                                    saveReport(reportData);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ReportsActivity.this,
                                        "Erreur lors de la collecte des pointages: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReportsActivity.this,
                            "Erreur lors de la collecte des étudiants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Méthodes de génération des autres types de rapports
    private void generateEnseignantReport(Map<String, Object> reportData) {
        // TODO: Implémenter la génération de rapport par enseignant
        // Version simple pour l'exemple
        saveReport(reportData);
    }

    private void generateTerminalReport(Map<String, Object> reportData) {
        // TODO: Implémenter la génération de rapport d'utilisation des terminaux
        // Version simple pour l'exemple
        saveReport(reportData);
    }

    private void generateAnomaliesReport(Map<String, Object> reportData) {
        // TODO: Implémenter la génération de rapport d'anomalies
        // Version simple pour l'exemple
        saveReport(reportData);
    }

    private void saveReport(Map<String, Object> reportData) {
        // Enregistrement du rapport dans Firestore
        db.collection("rapports_institutionnels")
                .add(reportData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ReportsActivity.this,
                            "Rapport généré avec succès",
                            Toast.LENGTH_SHORT).show();

                    // Recharger la liste des rapports
                    loadReports();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReportsActivity.this,
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

    // Classe interne pour représenter un rapport institutionnel
    public static class InstitutionalReport {
        private String id;
        private String title;
        private String generatedBy;
        private String generationDate;
        private String startDate;
        private String endDate;
        private String reportType;

        // Constructeur vide requis pour Firestore
        public InstitutionalReport() {}

        // Getters et setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getGeneratedBy() { return generatedBy; }
        public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

        public String getGenerationDate() { return generationDate; }
        public void setGenerationDate(String generationDate) { this.generationDate = generationDate; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
    }

    // Adaptateur pour les rapports institutionnels
    private class InstitutionalReportAdapter extends RecyclerView.Adapter<InstitutionalReportAdapter.ViewHolder> {

        private final List<InstitutionalReport> reportList;

        public InstitutionalReportAdapter(List<InstitutionalReport> reportList) {
            this.reportList = reportList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_institutional_report, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            InstitutionalReport report = reportList.get(position);

            holder.tvReportTitle.setText(report.getTitle());
            holder.tvGenerationDate.setText("Généré le: " + formatDate(report.getGenerationDate()));
            holder.tvReportPeriod.setText("Période: " + formatDate(report.getStartDate()) + " au " + formatDate(report.getEndDate()));
            holder.tvReportType.setText("Type: " + report.getReportType());

            holder.btnViewReport.setOnClickListener(v -> {
                // Afficher les détails du rapport
                Toast.makeText(ReportsActivity.this, "Affichage du rapport " + report.getId(), Toast.LENGTH_SHORT).show();
                // TODO: Implémenter l'affichage détaillé du rapport
            });

            holder.btnExportReport.setOnClickListener(v -> {
                // Exporter le rapport
                Toast.makeText(ReportsActivity.this, "Export du rapport " + report.getId(), Toast.LENGTH_SHORT).show();
                // TODO: Implémenter l'exportation du rapport
            });
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateString;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvReportTitle, tvGenerationDate, tvReportPeriod, tvReportType;
            Button btnViewReport, btnExportReport;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvReportTitle = itemView.findViewById(R.id.tvReportTitle);
                tvGenerationDate = itemView.findViewById(R.id.tvGenerationDate);
                tvReportPeriod = itemView.findViewById(R.id.tvReportPeriod);
                tvReportType = itemView.findViewById(R.id.tvReportType);
                btnViewReport = itemView.findViewById(R.id.btnViewReport);
                btnExportReport = itemView.findViewById(R.id.btnExportReport);
            }
        }
    }
}