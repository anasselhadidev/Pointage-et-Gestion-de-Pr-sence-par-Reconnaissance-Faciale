package com.example.projets4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.model.Filiere;
import com.example.projets4.model.Matiere;
import com.example.projets4.utile.FiliereAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GererFiliereActivity extends AppCompatActivity {
    private EditText editNomFiliere, editDescriptionFiliere;
    private Button btnAjouterFiliere;
    private RecyclerView recyclerFilieres;

    private FirebaseFirestore db;
    private FiliereAdapter adapter;
    private List<Filiere> filiereList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerer_filiere);

        editNomFiliere = findViewById(R.id.editNomFiliere);
        editDescriptionFiliere = findViewById(R.id.editDescriptionFiliere);
        btnAjouterFiliere = findViewById(R.id.btnAjouterFiliere);
        recyclerFilieres = findViewById(R.id.recyclerFilieres);

        db = FirebaseFirestore.getInstance();
        adapter = new FiliereAdapter(filiereList, db);
        recyclerFilieres.setLayoutManager(new LinearLayoutManager(this));
        recyclerFilieres.setAdapter(adapter);

        btnAjouterFiliere.setOnClickListener(v -> {
            String nom = editNomFiliere.getText().toString().trim();
            String desc = editDescriptionFiliere.getText().toString().trim();
            if (nom.isEmpty()) {
                Toast.makeText(this, "Nom requis", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create filière
            Map<String, Object> data = new HashMap<>();
            data.put("nom", nom);
            data.put("description", desc);
            db.collection("filieres").add(data)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Filière ajoutée", Toast.LENGTH_SHORT).show();
                        // reset inputs
                        editNomFiliere.setText("");
                        editDescriptionFiliere.setText("");

                        Filiere newFiliere = new Filiere(docRef.getId(), nom, desc);
                        filiereList.add(newFiliere);
                        adapter.notifyItemInserted(filiereList.size() - 1);

                        // now prompt for multiple matières
                        showAddMatieresDialog(newFiliere);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        fetchFilieres();
    }

    private void fetchFilieres() {
        filiereList.clear();
        db.collection("filieres").get()
                .addOnSuccessListener(query -> {
                    for (var doc : query) {
                        Filiere f = doc.toObject(Filiere.class);
                        if (f != null) {
                            f.setId(doc.getId());
                            filiereList.add(f);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddMatieresDialog(Filiere filiere) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_matieres, null);
        builder.setView(dialogView)
                .setTitle("Ajouter des Matières pour " + filiere.getNom());

        LinearLayout container = dialogView.findViewById(R.id.containerMatiereFields);
        Button btnAddField = dialogView.findViewById(R.id.btnAddField);
        // start with one field
        addMatiereField(container);

        btnAddField.setOnClickListener(v -> addMatiereField(container));

        builder.setPositiveButton("Valider", (dialog, which) -> {
            // collect all entries
            List<String> noms = new ArrayList<>();
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                if (child instanceof EditText) {
                    String txt = ((EditText) child).getText().toString().trim();
                    if (!txt.isEmpty()) noms.add(txt);
                }
            }
            if (noms.isEmpty()) {
                Toast.makeText(this, "Aucune matière saisie", Toast.LENGTH_SHORT).show();
                return;
            }
            // batch add
            for (String mNom : noms) {
                Matiere m = new Matiere(mNom);
                db.collection("filieres")
                        .document(filiere.getId())
                        .collection("matieres")
                        .add(m);
            }
            Toast.makeText(this, noms.size() + " matières ajoutées", Toast.LENGTH_SHORT).show();
            adapter.refreshMatiereList(filiere);
        });
        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }

    // helper to add a new EditText into container
    private void addMatiereField(LinearLayout container) {
        EditText et = new EditText(this);
        et.setHint("Nom Matière");
        et.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(et);
    }
}