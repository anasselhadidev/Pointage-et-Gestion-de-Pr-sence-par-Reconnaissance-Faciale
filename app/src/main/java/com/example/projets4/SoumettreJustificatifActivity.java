package com.example.projets4;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class SoumettreJustificatifActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvDateAbsence;
    private Button btnSelectDate;
    private Spinner spinnerMotif;
    private EditText etCommentaire;
    private Button btnSelectDocument;
    private TextView tvDocumentSelected;
    private Button btnSoumettre;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String etudiantEmail;

    private Calendar selectedDate;
    private Uri selectedDocumentUri;

    private final ActivityResultLauncher<Intent> documentPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedDocumentUri = result.getData().getData();
                    String fileName = getFileNameFromUri(selectedDocumentUri);
                    tvDocumentSelected.setText("Document sélectionné: " + fileName);
                    btnSoumettre.setEnabled(true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soumettre_justificatif);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        etudiantEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Soumettre un justificatif");
        }

        // Initialisation de la date
        selectedDate = Calendar.getInstance();
        updateDateDisplay();

        // Configuration des écouteurs de clics
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvDateAbsence = findViewById(R.id.tvDateAbsence);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        spinnerMotif = findViewById(R.id.spinnerMotif);
        etCommentaire = findViewById(R.id.etCommentaire);
        btnSelectDocument = findViewById(R.id.btnSelectDocument);
        tvDocumentSelected = findViewById(R.id.tvDocumentSelected);
        btnSoumettre = findViewById(R.id.btnSoumettre);

        // Désactiver le bouton soumettre jusqu'à ce qu'un document soit sélectionné
        btnSoumettre.setEnabled(false);
    }

    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        btnSelectDocument.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            documentPickerLauncher.launch(intent);
        });

        btnSoumettre.setOnClickListener(v -> soumettreJustificatif());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDateAbsence.setText(sdf.format(selectedDate.getTime()));
    }

    private void soumettreJustificatif() {
        if (etudiantEmail.isEmpty()) {
            Toast.makeText(this, "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDocumentUri == null) {
            Toast.makeText(this, "Veuillez sélectionner un document", Toast.LENGTH_SHORT).show();
            return;
        }

        String motif = spinnerMotif.getSelectedItem().toString();
        String commentaire = etCommentaire.getText().toString().trim();

        // Désactiver le bouton pour éviter les soumissions multiples
        btnSoumettre.setEnabled(false);

        // Générer un ID unique pour le document
        String documentId = UUID.randomUUID().toString();

        // Chemin de stockage du fichier dans Firebase Storage
        String storagePath = "justificatifs/" + etudiantEmail + "/" + documentId;
        StorageReference storageRef = storage.getReference(storagePath);

        // Télécharger le fichier
        storageRef.putFile(selectedDocumentUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtenir l'URL de téléchargement
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Créer l'objet justificatif
                        Map<String, Object> justificatif = new HashMap<>();
                        justificatif.put("etudiantEmail", etudiantEmail);
                        justificatif.put("dateAbsence", getFormattedDate(selectedDate.getTime()));
                        justificatif.put("dateSoumission", getFormattedDate(new Date()));
                        justificatif.put("motif", motif);
                        justificatif.put("commentaire", commentaire);
                        justificatif.put("documentUrl", uri.toString());
                        justificatif.put("status", "En attente");

                        // Enregistrer dans Firestore
                        db.collection("justificatifs")
                                .document(documentId)
                                .set(justificatif)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SoumettreJustificatifActivity.this,
                                            "Justificatif soumis avec succès",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnSoumettre.setEnabled(true);
                                    Toast.makeText(SoumettreJustificatifActivity.this,
                                            "Erreur lors de la soumission: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    btnSoumettre.setEnabled(true);
                    Toast.makeText(SoumettreJustificatifActivity.this,
                            "Erreur lors du téléchargement du document: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String getFormattedDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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