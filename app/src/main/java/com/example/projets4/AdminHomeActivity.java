package com.example.projets4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    private Button btnGererFilieres;
    private Button btnGererMatieres;
    private Button btnConsulterEmploi;
    private TextView adminWelcomeText;
    private Button manageUsersButton;
    private Button viewComplaintsButton;
    private Button logoutButton;
    private Button btnAdminDashboard; // Nouveau bouton pour le tableau de bord administrateur

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        btnGererFilieres     = findViewById(R.id.btnGererFilieres);
        btnGererMatieres     = findViewById(R.id.btnGererMatieres);
        btnConsulterEmploi   = findViewById(R.id.btnConsulterEmploi);

        adminWelcomeText     = findViewById(R.id.adminWelcomeText);
        manageUsersButton    = findViewById(R.id.manageUsersButton);
        viewComplaintsButton = findViewById(R.id.viewComplaintsButton);
        logoutButton         = findViewById(R.id.logoutButton);
        btnAdminDashboard    = findViewById(R.id.btnAdminDashboard); // Initialisation du nouveau bouton

        adminWelcomeText.setText("Bienvenue, Administrateur");

        btnGererFilieres.setOnClickListener(v ->
                startActivity(new Intent(this, GererFiliereActivity.class))
        );

        btnGererMatieres.setOnClickListener(v ->
                startActivity(new Intent(this, GererMatiereActivity.class))
        );

        btnConsulterEmploi.setOnClickListener(v ->
                startActivity(new Intent(this, ConsulterEmploiActivity.class))
        );

        manageUsersButton.setOnClickListener(v ->
                startActivity(new Intent(this, ListeUsersActivity.class))
        );

        viewComplaintsButton.setOnClickListener(v ->
                startActivity(new Intent(this, ListeReclamationsActivity.class))
        );

        // Nouveau bouton pour accéder au tableau de bord administrateur complet
        btnAdminDashboard.setOnClickListener(v ->
                startActivity(new Intent(this, AdminDashboardActivity.class))
        );

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AuthentificationActivity.class));
            finish();
        });
    }
}