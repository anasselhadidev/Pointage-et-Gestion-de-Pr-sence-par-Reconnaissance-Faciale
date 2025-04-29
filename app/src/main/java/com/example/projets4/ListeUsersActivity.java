package com.example.projets4;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.User;
import com.example.projets4.utile.UserAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.LinkedList;

public class ListeUsersActivity extends AppCompatActivity {

    FirebaseFirestore db;
    LinkedList<User> users;
    RecyclerView userRecycler;
    ProgressDialog progdiag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_users);

        userRecycler = findViewById(R.id.listeUtilisateurs);
        users = new LinkedList<>();
        db = FirebaseFirestore.getInstance();

        fetchAllUsers();
    }

    void fetchAllUsers() {
        showDialog();
        users.clear();

        db.collection("clients")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = new User(
                                    doc.getId(),
                                    "client",
                                    doc.getString("nom"),
                                    doc.getString("prenom"),
                                    doc.getString("telephone")
                            );
                            users.add(user);
                        }
                        fetchAgents(); // continue with agents
                    } else {
                        Log.e("ListeUsers", "Error fetching clients", task.getException());
                        hideDialog();
                    }
                });
    }

    void fetchAgents() {
        db.collection("agents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = new User(
                                    doc.getId(),
                                    "agent",
                                    doc.getString("nom"),
                                    doc.getString("prenom"),
                                    doc.getString("telephone")
                            );
                            users.add(user);
                        }

                        userRecycler.setLayoutManager(new LinearLayoutManager(this));
                        UserAdapter adapter = new UserAdapter(users);
                        userRecycler.setAdapter(adapter);
                    } else {
                        Log.e("ListeUsers", "Error fetching agents", task.getException());
                    }
                    hideDialog();
                });
    }

    void showDialog() {
        progdiag = new ProgressDialog(this);
        progdiag.setMessage("Chargement des utilisateurs...");
        progdiag.setIndeterminate(true);
        progdiag.show();
    }

    void hideDialog() {
        if (progdiag != null && progdiag.isShowing()) {
            progdiag.dismiss();
        }
    }
}
