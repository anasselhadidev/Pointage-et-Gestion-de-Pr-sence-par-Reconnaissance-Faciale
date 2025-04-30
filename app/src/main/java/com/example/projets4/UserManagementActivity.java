package com.example.projets4;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.adapters.UserAdapter;
import com.example.projets4.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etSearchUser;
    private Spinner spinnerRole;
    private Spinner spinnerDepartment;
    private Button btnSearch;
    private RecyclerView recyclerUsers;
    private TextView tvNoUsers;
    private Button btnImport;
    private Button btnExport;
    private Button btnBulkAction;
    private TextView tvPendingPhotos;
    private Button btnViewPendingPhotos;
    private FloatingActionButton fabAddUser;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<User> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestion des Utilisateurs");
        }

        // Configuration des spinners
        setupSpinners();

        // Configuration du RecyclerView
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList, this::showUserActionDialog);
        recyclerUsers.setAdapter(adapter);

        // Chargement des utilisateurs
        loadUsers();

        // Configuration des écouteurs
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearchUser = findViewById(R.id.etSearchUser);
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        tvNoUsers = findViewById(R.id.tvNoUsers);
        btnImport = findViewById(R.id.btnImport);
        btnExport = findViewById(R.id.btnExport);
        btnBulkAction = findViewById(R.id.btnBulkAction);
        tvPendingPhotos = findViewById(R.id.tvPendingPhotos);
        btnViewPendingPhotos = findViewById(R.id.btnViewPendingPhotos);
        fabAddUser = findViewById(R.id.fabAddUser);
    }

    private void setupSpinners() {
        // Configuration du spinner de rôles
        List<String> roles = Arrays.asList("Tous les rôles", "Étudiant", "Enseignant", "Admin");
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Configuration du spinner de départements
        List<String> departments = Arrays.asList("Tous les départements", "Informatique", "Mathématiques", "Biologie", "Chimie", "Physique");
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, departments);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(departmentAdapter);
    }

    private void loadUsers() {
        // Afficher un message de chargement
        tvNoUsers.setText("Chargement des utilisateurs...");
        tvNoUsers.setVisibility(View.VISIBLE);
        recyclerUsers.setVisibility(View.GONE);

        // Construction des requêtes en fonction des filtres
        String role = spinnerRole.getSelectedItem().toString();
        String department = spinnerDepartment.getSelectedItem().toString();
        String search = etSearchUser.getText().toString().trim().toLowerCase();

        // Requête de base pour tous les utilisateurs
        db.collection("etudiants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Vérifier si l'utilisateur correspond aux filtres
                        boolean matchesRole = role.equals("Tous les rôles") || "Étudiant".equals(role);
                        boolean matchesDepartment = department.equals("Tous les départements")
                                || department.equals(doc.getString("filiere"));

                        String nom = doc.getString("nom");
                        String prenom = doc.getString("prenom");
                        String email = doc.getString("email");

                        boolean matchesSearch = search.isEmpty()
                                || (nom != null && nom.toLowerCase().contains(search))
                                || (prenom != null && prenom.toLowerCase().contains(search))
                                || (email != null && email.toLowerCase().contains(search));

                        if (matchesRole && matchesDepartment && matchesSearch) {
                            User user = new User();
                            user.setEmail(doc.getId());
                            user.setNom(nom);
                            user.setPrenom(prenom);
                            user.setRole("etudiant");
                            user.setTelephone(doc.getString("phone"));
                            userList.add(user);
                        }
                    }

                    // Charger aussi les professeurs
                    loadProfessors();
                })
                .addOnFailureListener(e -> {
                    tvNoUsers.setText("Erreur lors du chargement des utilisateurs");
                    tvNoUsers.setVisibility(View.VISIBLE);
                    recyclerUsers.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProfessors() {
        String role = spinnerRole.getSelectedItem().toString();
        String department = spinnerDepartment.getSelectedItem().toString();
        String search = etSearchUser.getText().toString().trim().toLowerCase();

        // Si on ne cherche pas les professeurs, on passe directement à la mise à jour de l'interface
        if (!role.equals("Tous les rôles") && !role.equals("Enseignant")) {
            updateUI();
            return;
        }

        db.collection("professeurs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Vérifier si l'utilisateur correspond aux filtres
                        boolean matchesDepartment = department.equals("Tous les départements")
                                || department.equals(doc.getString("departement"));

                        String nom = doc.getString("nom");
                        String prenom = doc.getString("prenom");
                        String email = doc.getString("email");

                        boolean matchesSearch = search.isEmpty()
                                || (nom != null && nom.toLowerCase().contains(search))
                                || (prenom != null && prenom.toLowerCase().contains(search))
                                || (email != null && email.toLowerCase().contains(search));

                        if (matchesDepartment && matchesSearch) {
                            User user = new User();
                            user.setEmail(doc.getId());
                            user.setNom(nom);
                            user.setPrenom(prenom);
                            user.setRole("professeur");
                            user.setTelephone(doc.getString("phone"));
                            userList.add(user);
                        }
                    }

                    // Charger aussi les admins
                    loadAdmins();
                })
                .addOnFailureListener(e -> {
                    tvNoUsers.setText("Erreur lors du chargement des utilisateurs");
                    tvNoUsers.setVisibility(View.VISIBLE);
                    recyclerUsers.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAdmins() {
        String role = spinnerRole.getSelectedItem().toString();
        String search = etSearchUser.getText().toString().trim().toLowerCase();

        // Si on ne cherche pas les admins, on passe directement à la mise à jour de l'interface
        if (!role.equals("Tous les rôles") && !role.equals("Admin")) {
            updateUI();
            return;
        }

        db.collection("admins")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nom = doc.getString("nom");
                        String prenom = doc.getString("prenom");
                        String email = doc.getString("email");

                        boolean matchesSearch = search.isEmpty()
                                || (nom != null && nom.toLowerCase().contains(search))
                                || (prenom != null && prenom.toLowerCase().contains(search))
                                || (email != null && email.toLowerCase().contains(search));

                        if (matchesSearch) {
                            User user = new User();
                            user.setEmail(doc.getId());
                            user.setNom(nom);
                            user.setPrenom(prenom);
                            user.setRole("admin");
                            user.setTelephone(doc.getString("phone"));
                            userList.add(user);
                        }
                    }

                    // Mettre à jour l'interface
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    tvNoUsers.setText("Erreur lors du chargement des utilisateurs");
                    tvNoUsers.setVisibility(View.VISIBLE);
                    recyclerUsers.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();

        if (userList.isEmpty()) {
            tvNoUsers.setText("Aucun utilisateur trouvé");
            tvNoUsers.setVisibility(View.VISIBLE);
            recyclerUsers.setVisibility(View.GONE);
        } else {
            tvNoUsers.setVisibility(View.GONE);
            recyclerUsers.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> loadUsers());

        btnImport.setOnClickListener(v -> {
            // Ouvrir une boîte de dialogue pour sélectionner un fichier CSV/Excel
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Sélectionner un fichier"), 1);
        });

        btnExport.setOnClickListener(v -> {
            Toast.makeText(this, "Export des utilisateurs en cours...", Toast.LENGTH_SHORT).show();
            // Logique d'export ici
        });

        btnBulkAction.setOnClickListener(v -> {
            // Afficher les options d'actions groupées
            String[] options = {"Activer les comptes sélectionnés", "Désactiver les comptes sélectionnés", "Supprimer les comptes sélectionnés"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choisir une action groupée")
                    .setItems(options, (dialog, which) -> {
                        // Action en fonction du choix
                        Toast.makeText(this, "Action sélectionnée: " + options[which], Toast.LENGTH_SHORT).show();
                    });
            builder.create().show();
        });

        btnViewPendingPhotos.setOnClickListener(v -> {
            Toast.makeText(this, "Affichage des photos en attente...", Toast.LENGTH_SHORT).show();
            // Naviguer vers l'activité de validation des photos
        });

        fabAddUser.setOnClickListener(v -> {
            showAddUserDialog();
        });
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        builder.setView(dialogView);

        // Récupérer les vues de la boîte de dialogue
        Spinner spinnerUserRole = dialogView.findViewById(R.id.spinnerUserRole);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etNom = dialogView.findViewById(R.id.etNom);
        EditText etPrenom = dialogView.findViewById(R.id.etPrenom);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);

        // Configuration du spinner de rôles
        List<String> roles = Arrays.asList("Étudiant", "Enseignant", "Admin");
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserRole.setAdapter(roleAdapter);

        // Configurer les boutons
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            // Récupérer les valeurs
            String role = spinnerUserRole.getSelectedItem().toString();
            String email = etEmail.getText().toString().trim();
            String nom = etNom.getText().toString().trim();
            String prenom = etPrenom.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validation des champs
            if (email.isEmpty() || nom.isEmpty() || prenom.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            // Créer l'utilisateur dans Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        // Créer le document utilisateur dans Firestore
                        String collection = role.equals("Étudiant") ? "etudiants" : role.equals("Enseignant") ? "professeurs" : "admins";

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("nom", nom);
                        userData.put("prenom", prenom);
                        userData.put("email", email);

                        db.collection(collection).document(email)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Utilisateur créé avec succès", Toast.LENGTH_SHORT).show();
                                    // Recharger la liste
                                    loadUsers();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erreur lors de la création de l'utilisateur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur lors de la création de l'utilisateur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUserActionDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(user.getPrenom() + " " + user.getNom())
                .setItems(new String[]{"Modifier", "Désactiver", "Réinitialiser le mot de passe", "Supprimer"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Modifier
                            showEditUserDialog(user);
                            break;
                        case 1: // Désactiver
                            toggleUserStatus(user);
                            break;
                        case 2: // Réinitialiser le mot de passe
                            resetUserPassword(user);
                            break;
                        case 3: // Supprimer
                            confirmDeleteUser(user);
                            break;
                    }
                });
        builder.create().show();
    }

    private void showEditUserDialog(User user) {
        // Implémenter l'édition d'utilisateur
        Toast.makeText(this, "Édition de l'utilisateur " + user.getEmail(), Toast.LENGTH_SHORT).show();
    }

    private void toggleUserStatus(User user) {
        // Implémenter la désactivation/activation d'utilisateur
        Toast.makeText(this, "Changement de statut pour " + user.getEmail(), Toast.LENGTH_SHORT).show();
    }

    private void resetUserPassword(User user) {
        // Implémenter la réinitialisation de mot de passe
        Toast.makeText(this, "Réinitialisation du mot de passe pour " + user.getEmail(), Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer l'utilisateur")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // Déterminer la collection en fonction du rôle
                    String collection = "etudiants";
                    if ("professeur".equals(user.getRole())) {
                        collection = "professeurs";
                    } else if ("admin".equals(user.getRole())) {
                        collection = "admins";
                    }

                    // Supprimer le document dans Firestore
                    db.collection(collection).document(user.getEmail())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Utilisateur supprimé avec succès", Toast.LENGTH_SHORT).show();
                                // Supprimer de la liste locale et mettre à jour l'interface
                                userList.remove(user);
                                adapter.notifyDataSetChanged();

                                if (userList.isEmpty()) {
                                    tvNoUsers.setText("Aucun utilisateur trouvé");
                                    tvNoUsers.setVisibility(View.VISIBLE);
                                    recyclerUsers.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur lors de la suppression: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            // Traiter le fichier d'import
            Toast.makeText(this, "Fichier sélectionné pour l'import", Toast.LENGTH_SHORT).show();
            // Implémenter la logique d'import des utilisateurs
        }
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