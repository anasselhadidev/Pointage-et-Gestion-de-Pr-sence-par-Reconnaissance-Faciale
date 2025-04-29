package com.example.projets4;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthentificationActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout layout1, layout2;
    Button bttauth, bttacc, bttconfAcc, bttAnnuler;
    EditText login, password, email, nom, prenom, pays, phone, ville, passwordreg;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        layout1 = findViewById(R.id.Layout1);
        layout2 = findViewById(R.id.Layout2);

        email = findViewById(R.id.emailEditText);
        passwordreg = findViewById(R.id.passwordregtext);
        nom = findViewById(R.id.nomEditText);
        prenom = findViewById(R.id.prenomEditText);
        ville = findViewById(R.id.villeEditText);
        pays = findViewById(R.id.paysEditText);
        phone = findViewById(R.id.phoneEditText);

        bttauth = findViewById(R.id.authButton);
        bttacc = findViewById(R.id.creerCompteButton);
        bttconfAcc = findViewById(R.id.confirmeButton);
        bttAnnuler = findViewById(R.id.annulerButton);

        login = findViewById(R.id.loginEditText);
        password = findViewById(R.id.passwordEditText);

        bttauth.setOnClickListener(this);
        bttacc.setOnClickListener(this);
        bttconfAcc.setOnClickListener(this);
        bttAnnuler.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == bttacc.getId()) {
            layout1.setVisibility(View.GONE);
            layout2.setVisibility(View.VISIBLE);
        } else if (view.getId() == bttauth.getId()) {
            SignIn();
        } else if (view.getId() == bttconfAcc.getId()) {
            signUp();
        } else if (view.getId() == bttAnnuler.getId()) {
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.GONE);
        }
    }

    private void SignIn() {
        mAuth.signInWithEmailAndPassword(login.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserType(user.getEmail());
                        }
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void signUp() {
        RadioGroup userTypeRadioGroup = findViewById(R.id.typeuserRadioGroup);
        int selectedId = userTypeRadioGroup.getCheckedRadioButtonId();
        final String userType;

        if (selectedId == R.id.etudiantRadio) {
            userType = "etudiant";
        } else if (selectedId == R.id.professeurRadio) {
            userType = "professeur";
        } else {
            Toast.makeText(this, "Please select a user type (Etudiant or Professeur)", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email.getText().toString(), passwordreg.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("nom", nom.getText().toString());
                            userData.put("prenom", prenom.getText().toString());
                            userData.put("ville", ville.getText().toString());
                            userData.put("pays", pays.getText().toString());
                            userData.put("phone", phone.getText().toString());
                            userData.put("email", email.getText().toString());

                            db.collection(userType + "s")
                                    .document(user.getEmail())
                                    .set(userData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            if (userType.equals("etudiant")) {
                                              //  navigateToEtudiantDashboard();
                                            } else {
                                                navigateToProfesseurDashboard();
                                            }
                                        } else {
                                            Log.w(TAG, "Error saving user data", task1.getException());
                                            Toast.makeText(this, "Error saving user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "signUpWithEmail:failure", task.getException());
                        Toast.makeText(this, "Sign up failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void checkUserType(String email) {
        db.collection("etudiants").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
              // navigateToEtudiantDashboard();
            } else {
                db.collection("professeurs").document(email).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && task1.getResult().exists()) {
                        navigateToProfesseurDashboard();
                    } else {
                        db.collection("admins").document(email).get().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful() && task2.getResult().exists()) {
                                navigateToAdminDashboard();
                            } else {
                                Toast.makeText(this, "No such user found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(this, AdminHomeActivity.class);
        startActivity(intent);
        finish();
    }

  /*  private void navigateToEtudiantDashboard() {
        Intent intent = new Intent(this, EtudiantHomeActivity.class);
        startActivity(intent);
        finish();
    }*/

    private void navigateToProfesseurDashboard() {
        Intent intent = new Intent(this, TeacherHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.GONE);
        }
    }
}
