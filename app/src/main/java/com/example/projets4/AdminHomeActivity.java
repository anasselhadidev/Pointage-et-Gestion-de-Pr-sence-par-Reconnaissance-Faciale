package com.example.projets4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projets4.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminHomeActivity extends AppCompatActivity {

    private Button btnGererFilieres;
    private Button btnGererMatieres;
    private Button btnGererEmploi;
    private Button btnConsulterEmploi;
    private TextView adminWelcomeText;
    private Button manageUsersButton;
    private Button viewComplaintsButton;
    private Button viewStatsButton;
    private Button settingsButton;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        btnGererFilieres = findViewById(R.id.btnGererFilieres);
        btnGererMatieres = findViewById(R.id.btnGererMatieres);
        btnGererEmploi = findViewById(R.id.btnGererEmploi);
        btnConsulterEmploi = findViewById(R.id.btnConsulterEmploi);




        adminWelcomeText = findViewById(R.id.adminWelcomeText);
        manageUsersButton = findViewById(R.id.manageUsersButton);
        viewComplaintsButton = findViewById(R.id.viewComplaintsButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Optionally personalize the welcome text
        adminWelcomeText.setText("Bienvenue, Administrateur");


        btnGererFilieres.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, GererFiliereActivity.class);
            startActivity(intent);
        });

        btnGererMatieres.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, GererMatiereActivity.class);
            startActivity(intent);
        });

       /* btnGererEmploi.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, GererEmploiActivity.class);
            startActivity(intent);
        });*/

        btnConsulterEmploi.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, ConsulterEmploiActivity.class);
            startActivity(intent);
        });


        manageUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this, ListeUsersActivity.class);
                startActivity(intent);
            }
        });

        viewComplaintsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this, ListeReclamationsActivity.class);
                startActivity(intent);
            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                mAuth.signOut();
                Intent intent = new Intent(AdminHomeActivity.this, AuthentificationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
