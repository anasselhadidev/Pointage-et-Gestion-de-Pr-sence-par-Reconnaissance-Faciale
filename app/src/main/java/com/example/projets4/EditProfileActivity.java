package com.example.projets4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projets4.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEditText, firstNameEditText, emailEditText, phoneEditText, cityEditText, countryEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI components
        nameEditText = findViewById(R.id.name_edit_text);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        cityEditText = findViewById(R.id.city_edit_text);
        countryEditText = findViewById(R.id.country_edit_text);
        saveButton = findViewById(R.id.save_button);

        // Get the data passed from the previous activity (user profile data)
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String firstName = intent.getStringExtra("firstName");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone");
        String city = intent.getStringExtra("city");
        String country = intent.getStringExtra("country");

        // Set the data in the EditTexts to display the user's current profile
        nameEditText.setText(name);
        firstNameEditText.setText(firstName);
        emailEditText.setText(email);
        phoneEditText.setText(phone);
        cityEditText.setText(city);
        countryEditText.setText(country);

        // Handle the save button click
        saveButton.setOnClickListener(v -> saveProfileData());
    }

    private void saveProfileData() {
        // Get the updated data from the EditText fields
        String updatedName = nameEditText.getText().toString();
        String updatedFirstName = firstNameEditText.getText().toString();
        String updatedEmail = emailEditText.getText().toString();
        String updatedPhone = phoneEditText.getText().toString();
        String updatedCity = cityEditText.getText().toString();
        String updatedCountry = countryEditText.getText().toString();

        // Get the current user's email to update the correct Firestore document
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Create a map with the updated profile data
        Map<String, Object> updatedProfile = new HashMap<>();
        updatedProfile.put("nom", updatedName);
        updatedProfile.put("prenom", updatedFirstName);
        updatedProfile.put("email", updatedEmail);
        updatedProfile.put("telephone", updatedPhone);
        updatedProfile.put("ville", updatedCity);
        updatedProfile.put("pays", updatedCountry);

        // Save the updated data to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference agentRef = db.collection("agents").document(currentUserEmail);
        DocumentReference clientRef = db.collection("clients").document(currentUserEmail);

        agentRef.get().addOnSuccessListener(agentSnapshot -> {
            if (agentSnapshot.exists()) {
                agentRef.set(updatedProfile)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            finish();  // Go back to the previous activity
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Check if user is a client
                clientRef.get().addOnSuccessListener(clientSnapshot -> {
                    if (clientSnapshot.exists()) {
                        clientRef.set(updatedProfile)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                                    finish();  // Go back to the previous activity
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Not found in either collection
                        Log.d("ROLE_CHECK", "User not found in either collection");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("ROLE_CHECK", "Error checking client collection", e);
                });
            }
        }).addOnFailureListener(e -> {
            Log.e("ROLE_CHECK", "Error checking agent collection", e);
        });

    }
}

