package com.example.projets4;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.model.Filiere;
import com.example.projets4.utile.FiliereAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
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

        // RecyclerView setup
        adapter = new FiliereAdapter(filiereList, db);
        recyclerFilieres.setLayoutManager(new LinearLayoutManager(this));
        recyclerFilieres.setAdapter(adapter);

        btnAjouterFiliere.setOnClickListener(v -> {
            String nom = editNomFiliere.getText().toString().trim();
            String desc = editDescriptionFiliere.getText().toString().trim();
            if (!nom.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("nom", nom);
                data.put("description", desc);

                db.collection("filieres")
                        .add(data)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(this, "Filière ajoutée", Toast.LENGTH_SHORT).show();
                            editNomFiliere.setText("");
                            editDescriptionFiliere.setText("");
                            fetchFilieres();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Nom requis", Toast.LENGTH_SHORT).show();
            }
        });

        fetchFilieres();
    }

    private void fetchFilieres() {
        filiereList.clear();
        db.collection("filieres").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Filiere f = doc.toObject(Filiere.class);
                f.setId(doc.getId()); // important to know document ID for edit/delete
                filiereList.add(f);
            }
            adapter.notifyDataSetChanged();
        });
    }
}
