package com.example.projets4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.model.Filiere;
import com.example.projets4.utile.FiliereSimpleAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GererMatiereActivity extends AppCompatActivity {
    private RecyclerView recyclerFilieres;
    private FiliereSimpleAdapter adapter;
    private List<Filiere> filieres = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerer_matiere);

        recyclerFilieres = findViewById(R.id.recyclerFilieresMatieres);
        recyclerFilieres.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        adapter = new FiliereSimpleAdapter(filieres, filiere -> {
            // Ouvre la liste de matières
            Intent i = new Intent(GererMatiereActivity.this, ListeMatieresActivity.class);
            i.putExtra("filiereId", filiere.getId());
            i.putExtra("filiereName", filiere.getNom());
            startActivity(i);
        });
        recyclerFilieres.setAdapter(adapter);

        fetchFilieres();
    }

    private void fetchFilieres() {
        db.collection("filieres")
                .get()
                .addOnSuccessListener(qs -> {
                    filieres.clear();
                    for (DocumentSnapshot ds : qs) {
                        Filiere f = ds.toObject(Filiere.class);
                        if (f != null) {
                            f.setId(ds.getId());
                            filieres.add(f);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur chargement filières : " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
