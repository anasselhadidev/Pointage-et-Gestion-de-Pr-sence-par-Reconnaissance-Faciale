package com.example.projets4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.model.Matiere;
import com.example.projets4.utile.MatiereAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ListeMatieresActivity extends AppCompatActivity {
    private RecyclerView recycler;
    private MatiereAdapter adapter;
    private List<Matiere> matieres = new ArrayList<>();
    private FirebaseFirestore db;
    private String filiereId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_matieres);

        filiereId = getIntent().getStringExtra("filiereId");
        String nom = getIntent().getStringExtra("filiereName");
        setTitle("Matières de " + nom);

        recycler = findViewById(R.id.recyclerMatieresList);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        adapter = new MatiereAdapter(matieres, this::showEditDialog);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddMatiere);
        fab.setOnClickListener(v -> showAddDialog());

        fetchMatieres();
    }

    private void fetchMatieres() {
        db.collection("filieres")
                .document(filiereId)
                .collection("matieres")
                .get()
                .addOnSuccessListener(qs -> {
                    matieres.clear();
                    for (DocumentSnapshot ds : qs) {
                        Matiere m = ds.toObject(Matiere.class);
                        m.setId(ds.getId());
                        matieres.add(m);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        var v = LayoutInflater.from(this).inflate(R.layout.dialog_add_matiere, null);
        EditText et = v.findViewById(R.id.editMatiereNom);
        b.setView(v)
                .setTitle("Nouvelle Matière")
                .setPositiveButton("Ajouter", (d, w) -> {
                    String t = et.getText().toString().trim();
                    if (t.isEmpty()) return;
                    Matiere m = new Matiere(t);
                    db.collection("filieres")
                            .document(filiereId)
                            .collection("matieres")
                            .add(m)
                            .addOnSuccessListener(r -> fetchMatieres());
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showEditDialog(Matiere m) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        var v = LayoutInflater.from(this).inflate(R.layout.dialog_add_matiere, null);
        EditText et = v.findViewById(R.id.editMatiereNom);
        et.setText(m.getNom());
        b.setView(v)
                .setTitle("Modifier Matière")
                .setPositiveButton("Enregistrer", (d, w) -> {
                    String t = et.getText().toString().trim();
                    if (t.isEmpty()) return;
                    db.collection("filieres")
                            .document(filiereId)
                            .collection("matieres")
                            .document(m.getId())
                            .update("nom", t)
                            .addOnSuccessListener(u -> fetchMatieres());
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
