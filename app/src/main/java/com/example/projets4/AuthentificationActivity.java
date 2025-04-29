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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projets4.R;
import com.example.projets4.dataAccess.PasswordEncryption;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthentificationActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout layout1;
    LinearLayout layout2;
    LinearLayout layout3;
    Button bttauth;
    Button bttacc;
    Button bttconfAcc;
    Button bttAnnuler;
    Button bttDeconnecxion;
    Button bttOffres;
    Button bttDemandes;
    Button bttProfil;
    TextView userinfo;
    EditText login;
    EditText password;
    EditText email;
    EditText nom;
    EditText prenom;
    EditText pays;
    EditText adresse;
    EditText phone;
    EditText ville;
    EditText passwordreg;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    String nomUser;
    String prenomUser;

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


        bttauth.setOnClickListener(this);
        bttacc.setOnClickListener(this);
        bttconfAcc.setOnClickListener(this);
        bttAnnuler.setOnClickListener(this);

        login = findViewById(R.id.loginEditText);
        password = findViewById(R.id.passwordEditText);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
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
        String pass = PasswordEncryption.hash(password.getText().toString());
        mAuth.signInWithEmailAndPassword(login.getText().toString(), pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if the user is a client or agent based on their email
                            checkUserType(user.getEmail());
                        }
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    private void signUp() {
        // Get the selected user type (Agent or Client)
        RadioGroup userTypeRadioGroup = findViewById(R.id.typeuserRadioGroup);
        int selectedId = userTypeRadioGroup.getCheckedRadioButtonId();
        final String userType; // Make userType final

        if (selectedId == R.id.etudiantRadio) {
            userType = "etudiant"; // The user is registering as an agent
        } else if (selectedId == R.id.professeurRadio) {
            userType = "professeur"; // The user is registering as a client
        } else {
            // If no radio button is selected, show an error message
            Toast.makeText(AuthentificationActivity.this, "Please select a user type (Agent or Client)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Encrypt the password
        String encryptedPassword = PasswordEncryption.hash(passwordreg.getText().toString());

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), encryptedPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Create user data to be saved to Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("nom", nom.getText().toString());
                            userData.put("prenom", prenom.getText().toString());
                            userData.put("ville", ville.getText().toString());
                            userData.put("pays", pays.getText().toString());
                            userData.put("phone", phone.getText().toString());
                            userData.put("email", email.getText().toString());

                            // Save user data to Firestore in the corresponding collection (agents/clients)
                            db.collection(userType + "s") // 'agents' or 'clients'
                                    .document(user.getEmail()) // Use the email as document ID
                                    .set(userData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // After successfully saving the user, navigate to the appropriate dashboard
                                            if (userType.equals("etudiants")) {
                                               // navigateToAgentDashboard();
                                            } else {
                                               // navigateToClientDashboard();
                                            }
                                        } else {
                                            Log.w(TAG, "Error saving user data", task1.getException());
                                            Toast.makeText(AuthentificationActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "signUpWithEmail:failure", task.getException());
                        updateUI(null);
                    }
                });
    }


    private void checkUserType(String email) {
        // First, check if the user exists in the "agents" collection
      /*  db.collection("agents").document(email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // User is an agent
                        navigateToAgentDashboard();
                    } else {
                        // If not an agent, check if they are a client
                        db.collection("clients").document(email)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful() && task1.getResult().exists()) {
                                        // User is a client
                                        navigateToClientDashboard();
                                    } else {
                                        System.out.println( "No such user found in clients \n");
                                    }
                                }); */
                        db.collection("admins").document(email)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful() && task1.getResult().exists()) {
                                        // User is a client
                                        navigateToAdminDashboard();
                                    } else {
                                        Toast.makeText(AuthentificationActivity.this, "No such user found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

    private void navigateToAdminDashboard() {
        // Redirect to Agent's specific activity
        Intent agentIntent = new Intent(this, AdminHomeActivity.class);
        startActivity(agentIntent);
        finish(); // Ensure the login screen is not accessible after login
    }

    /*   private void navigateToAgentDashboard() {
        // Redirect to Agent's specific activity
        Intent agentIntent = new Intent(this, AgentHomeActivity.class);
        startActivity(agentIntent);
        finish(); // Ensure the login screen is not accessible after login
    }

    private void navigateToClientDashboard() {
        // Redirect to Client's specific activity
        Intent clientIntent = new Intent(this, ClientHomeActivity.class);
        startActivity(clientIntent);
        finish(); // Ensure the login screen is not accessible after login
    } */

    void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.GONE);
        }
    }
}