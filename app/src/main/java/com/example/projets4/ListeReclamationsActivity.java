package com.example.projets4;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Reclamation;
import com.example.projets4.utile.ReclamationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListeReclamationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReclamationAdapter adapter;
    private List<Reclamation> reclamationList = new ArrayList<>();
    private FirebaseFirestore db;
    private ProgressDialog progdiag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offres); // Or create activity_reclamations.xml

        recyclerView = findViewById(R.id.listeOffres); // reuse the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        adapter = new ReclamationAdapter(reclamationList, this);
        recyclerView.setAdapter(adapter);

        loadReclamations();
    }

    private void loadReclamations() {
        showDialog();

        db.collection("reclamations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reclamationList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Reclamation r = new Reclamation(
                                    doc.getString("idClient"),
                                    doc.getString("objet"),
                                    doc.getString("message"),
                                    doc.getString("date"),
                                    doc.getString("etat")
                            );
                            reclamationList.add(r);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Erreur : ", task.getException());
                    }
                    hideDialog();
                });
    }

    void showDialog() {
        progdiag = new ProgressDialog(this);
        progdiag.setMessage("Chargement des réclamations...");
        progdiag.setIndeterminate(true);
        progdiag.show();
    }

    void hideDialog() {
        if (progdiag != null && progdiag.isShowing()) {
            progdiag.dismiss();
        }
    }
}
