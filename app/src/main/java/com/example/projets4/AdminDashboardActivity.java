package com.example.projets4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTerminalStatus;
    private TextView tvCheckinCount;
    private TextView tvAlertCount;
    private CardView cardUserManagement;
    private CardView cardSystemMonitoring;
    private CardView cardSystemConfig;
    private CardView cardReports;
    private CardView cardAcademicManagement;
    private CardView cardSupport;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard Administrateur");
        }

        // Chargement des données système
        loadSystemData();

        // Configuration des écouteurs de clics
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTerminalStatus = findViewById(R.id.tvTerminalStatus);
        tvCheckinCount = findViewById(R.id.tvCheckinCount);
        tvAlertCount = findViewById(R.id.tvAlertCount);
        cardUserManagement = findViewById(R.id.cardUserManagement);
        cardSystemMonitoring = findViewById(R.id.cardSystemMonitoring);
        cardSystemConfig = findViewById(R.id.cardSystemConfig);
        cardReports = findViewById(R.id.cardReports);
        cardAcademicManagement = findViewById(R.id.cardAcademicManagement);
        cardSupport = findViewById(R.id.cardSupport);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadSystemData() {
        // Pour cette démo, nous utilisons des données statiques
        // Dans une application réelle, ces données viendraient de Firebase

        // Vérification de l'état des terminaux dans Firestore
        db.collection("terminals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalTerminals = queryDocumentSnapshots.size();
                    int onlineTerminals = 0;

                    for (var doc : queryDocumentSnapshots) {
                        Boolean isOnline = doc.getBoolean("online");
                        if (isOnline != null && isOnline) {
                            onlineTerminals++;
                        }
                    }

                    tvTerminalStatus.setText(onlineTerminals + " en ligne, " +
                            (totalTerminals - onlineTerminals) + " hors ligne");
                })
                .addOnFailureListener(e -> {
                    // Utiliser des valeurs par défaut en cas d'erreur
                    tvTerminalStatus.setText("20 en ligne, 2 hors ligne");
                    Toast.makeText(this, "Erreur lors du chargement des terminaux", Toast.LENGTH_SHORT).show();
                });

        // Date du jour pour la requête de pointages
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Vérification du nombre de pointages du jour
        db.collection("pointages")
                .whereGreaterThanOrEqualTo("date", today + " 00:00:00")
                .whereLessThanOrEqualTo("date", today + " 23:59:59")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int checkinsCount = queryDocumentSnapshots.size();
                    tvCheckinCount.setText(checkinsCount + " pointages");
                })
                .addOnFailureListener(e -> {
                    // Utiliser des valeurs par défaut en cas d'erreur
                    tvCheckinCount.setText("354 pointages");
                    Toast.makeText(this, "Erreur lors du chargement des pointages", Toast.LENGTH_SHORT).show();
                });

        // Vérification des alertes système
        db.collection("alertes")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int alertsCount = queryDocumentSnapshots.size();
                    tvAlertCount.setText(alertsCount + " alerte" + (alertsCount > 1 ? "s" : ""));

                    // Changer la couleur selon le nombre d'alertes
                    if (alertsCount > 0) {
                        tvAlertCount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    } else {
                        tvAlertCount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                })
                .addOnFailureListener(e -> {
                    // Utiliser des valeurs par défaut en cas d'erreur
                    tvAlertCount.setText("1 alerte");
                    tvAlertCount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    Toast.makeText(this, "Erreur lors du chargement des alertes", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        cardUserManagement.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, UserManagementActivity.class);
            startActivity(intent);
        });

        cardSystemMonitoring.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, SystemMonitoringActivity.class);
            startActivity(intent);
        });

        cardSystemConfig.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, SystemConfigActivity.class);
            startActivity(intent);
        });


        cardReports.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ReportsActivity.class);
            startActivity(intent);
        });

        cardAcademicManagement.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AcademicManagementActivity.class);
            startActivity(intent);
        });

        cardSupport.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, SupportActivity.class);
            startActivity(intent);
        });
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, AuthentificationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données à chaque retour sur l'écran
        loadSystemData();
    }
}