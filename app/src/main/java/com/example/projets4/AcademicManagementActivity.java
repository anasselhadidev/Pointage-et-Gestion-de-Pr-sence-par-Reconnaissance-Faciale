package com.example.projets4;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AcademicManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private TextView tvEmptyList;
    private FloatingActionButton fabAdd;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Listes pour stocker les données
    private List<Filiere> filiereList = new ArrayList<>();
    private List<Groupe> groupeList = new ArrayList<>();
    private List<Cours> coursList = new ArrayList<>();
    private List<CalendarEvent> calendarList = new ArrayList<>();

    // Adaptateurs
    private FiliereAdapter filiereAdapter;
    private GroupeAdapter groupeAdapter;
    private CoursAdapter coursAdapter;
    private CalendarAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_management);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        initViews();

        // Configuration de la toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestion Académique");
        }

        // Configuration du TabLayout
        setupTabLayout();

        // Configuration du RecyclerView
        setupRecyclerView();

        // Configuration du bouton flottant
        setupFloatingActionButton();

        // Chargement des données initiales (Filières par défaut)
        loadFilieres();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupTabLayout() {
        // Ajouter les onglets
        tabLayout.addTab(tabLayout.newTab().setText("Filières"));
        tabLayout.addTab(tabLayout.newTab().setText("Groupes"));
        tabLayout.addTab(tabLayout.newTab().setText("Cours"));
        tabLayout.addTab(tabLayout.newTab().setText("Calendrier"));

        // Configurer les écouteurs d'événements
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadFilieres();
                        break;
                    case 1:
                        loadGroupes();
                        break;
                    case 2:
                        loadCours();
                        break;
                    case 3:
                        loadCalendar();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Rien à faire
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optionnel: recharger les données
                onTabSelected(tab);
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialiser les adaptateurs
        filiereAdapter = new FiliereAdapter(filiereList);
        groupeAdapter = new GroupeAdapter(groupeList);
        coursAdapter = new CoursAdapter(coursList);
        calendarAdapter = new CalendarAdapter(calendarList);

        // Définir l'adaptateur par défaut (filières)
        recyclerView.setAdapter(filiereAdapter);
    }

    private void setupFloatingActionButton() {
        fabAdd.setOnClickListener(v -> {
            // Action différente selon l'onglet sélectionné
            int selectedTabPosition = tabLayout.getSelectedTabPosition();
            switch (selectedTabPosition) {
                case 0:
                    showAddFiliereDialog();
                    break;
                case 1:
                    showAddGroupeDialog();
                    break;
                case 2:
                    showAddCoursDialog();
                    break;
                case 3:
                    showAddCalendarEventDialog();
                    break;
            }
        });
    }

    private void loadFilieres() {
        db.collection("filieres")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    filiereList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Filiere filiere = document.toObject(Filiere.class);
                        filiere.setId(document.getId());
                        filiereList.add(filiere);
                    }

                    if (filiereList.isEmpty()) {
                        tvEmptyList.setText("Aucune filière disponible");
                        tvEmptyList.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyList.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(filiereAdapter);
                        filiereAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AcademicManagementActivity.this,
                            "Erreur lors du chargement des filières: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadGroupes() {
        db.collection("groupes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    groupeList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Groupe groupe = document.toObject(Groupe.class);
                        groupe.setId(document.getId());
                        groupeList.add(groupe);
                    }

                    if (groupeList.isEmpty()) {
                        tvEmptyList.setText("Aucun groupe disponible");
                        tvEmptyList.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyList.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(groupeAdapter);
                        groupeAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AcademicManagementActivity.this,
                            "Erreur lors du chargement des groupes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCours() {
        db.collection("cours")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    coursList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Cours cours = document.toObject(Cours.class);
                        cours.setId(document.getId());
                        coursList.add(cours);
                    }

                    if (coursList.isEmpty()) {
                        tvEmptyList.setText("Aucun cours disponible");
                        tvEmptyList.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyList.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(coursAdapter);
                        coursAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AcademicManagementActivity.this,
                            "Erreur lors du chargement des cours: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCalendar() {
        db.collection("calendrier_academique")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    calendarList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CalendarEvent event = document.toObject(CalendarEvent.class);
                        event.setId(document.getId());
                        calendarList.add(event);
                    }

                    if (calendarList.isEmpty()) {
                        tvEmptyList.setText("Aucun événement disponible");
                        tvEmptyList.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyList.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(calendarAdapter);
                        calendarAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AcademicManagementActivity.this,
                            "Erreur lors du chargement du calendrier: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddFiliereDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_filiere, null);

        EditText etNom = view.findViewById(R.id.etNomFiliere);
        EditText etDescription = view.findViewById(R.id.etDescriptionFiliere);

        builder.setView(view)
                .setTitle("Ajouter une filière")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String nom = etNom.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();

                    if (nom.isEmpty()) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Le nom est requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Création de la filière dans Firestore
                    Map<String, Object> filiereData = new HashMap<>();
                    filiereData.put("nom", nom);
                    filiereData.put("description", description);

                    db.collection("filieres")
                            .add(filiereData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Filière ajoutée avec succès", Toast.LENGTH_SHORT).show();
                                loadFilieres();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Erreur lors de l'ajout de la filière: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAddGroupeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_group, null);

        EditText etNom = view.findViewById(R.id.etNomGroupe);
        Spinner spinnerFiliere = view.findViewById(R.id.spinnerFiliere);

        // Charger les filières dans le spinner
        List<String> filiereNames = new ArrayList<>();
        List<String> filiereIds = new ArrayList<>();

        filiereNames.add("Sélectionner une filière");
        filiereIds.add(""); // Placeholder pour l'option par défaut

        for (Filiere filiere : filiereList) {
            filiereNames.add(filiere.getNom());
            filiereIds.add(filiere.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, filiereNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiliere.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Ajouter un groupe")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String nom = etNom.getText().toString().trim();
                    int selectedFilierePosition = spinnerFiliere.getSelectedItemPosition();

                    if (nom.isEmpty()) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Le nom est requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedFilierePosition == 0) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Veuillez sélectionner une filière", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String filiereId = filiereIds.get(selectedFilierePosition);

                    // Création du groupe dans Firestore
                    Map<String, Object> groupeData = new HashMap<>();
                    groupeData.put("nom", nom);
                    groupeData.put("filiereId", filiereId);

                    db.collection("groupes")
                            .add(groupeData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Groupe ajouté avec succès", Toast.LENGTH_SHORT).show();
                                loadGroupes();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Erreur lors de l'ajout du groupe: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAddCoursDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_cours, null);

        EditText etTitre = view.findViewById(R.id.etTitreCours);
        EditText etEnseignant = view.findViewById(R.id.etEnseignantCours);
        Spinner spinnerGroupe = view.findViewById(R.id.spinnerGroupe);

        // Charger les groupes dans le spinner
        List<String> groupeNames = new ArrayList<>();
        List<String> groupeIds = new ArrayList<>();

        groupeNames.add("Sélectionner un groupe");
        groupeIds.add(""); // Placeholder pour l'option par défaut

        for (Groupe groupe : groupeList) {
            groupeNames.add(groupe.getNom());
            groupeIds.add(groupe.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, groupeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroupe.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Ajouter un cours")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String titre = etTitre.getText().toString().trim();
                    String enseignant = etEnseignant.getText().toString().trim();
                    int selectedGroupePosition = spinnerGroupe.getSelectedItemPosition();

                    if (titre.isEmpty() || enseignant.isEmpty()) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedGroupePosition == 0) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Veuillez sélectionner un groupe", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String groupeId = groupeIds.get(selectedGroupePosition);

                    // Création du cours dans Firestore
                    Map<String, Object> coursData = new HashMap<>();
                    coursData.put("titre", titre);
                    coursData.put("enseignant", enseignant);
                    coursData.put("groupeId", groupeId);

                    db.collection("cours")
                            .add(coursData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Cours ajouté avec succès", Toast.LENGTH_SHORT).show();
                                loadCours();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Erreur lors de l'ajout du cours: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAddCalendarEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_calendar_event, null);

        EditText etTitre = view.findViewById(R.id.etTitreEvent);
        EditText etDescription = view.findViewById(R.id.etDescriptionEvent);
        TextView tvDateDebut = view.findViewById(R.id.tvDateDebut);
        TextView tvDateFin = view.findViewById(R.id.tvDateFin);
        Button btnDateDebut = view.findViewById(R.id.btnDateDebut);
        Button btnDateFin = view.findViewById(R.id.btnDateFin);
        Spinner spinnerType = view.findViewById(R.id.spinnerTypeEvent);

        // Initialiser les dates
        Calendar dateDebutCal = Calendar.getInstance();
        Calendar dateFinCal = Calendar.getInstance();
        dateFinCal.add(Calendar.DAY_OF_MONTH, 7); // Par défaut, fin 7 jours après

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvDateDebut.setText(dateFormat.format(dateDebutCal.getTime()));
        tvDateFin.setText(dateFormat.format(dateFinCal.getTime()));

        // Configuration des boutons de date
        btnDateDebut.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view1, year, month, dayOfMonth) -> {
                        dateDebutCal.set(year, month, dayOfMonth);
                        tvDateDebut.setText(dateFormat.format(dateDebutCal.getTime()));
                    },
                    dateDebutCal.get(Calendar.YEAR),
                    dateDebutCal.get(Calendar.MONTH),
                    dateDebutCal.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnDateFin.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view1, year, month, dayOfMonth) -> {
                        dateFinCal.set(year, month, dayOfMonth);
                        tvDateFin.setText(dateFormat.format(dateFinCal.getTime()));
                    },
                    dateFinCal.get(Calendar.YEAR),
                    dateFinCal.get(Calendar.MONTH),
                    dateFinCal.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Configuration du spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Ajouter un événement")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String titre = etTitre.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String dateDebut = tvDateDebut.getText().toString();
                    String dateFin = tvDateFin.getText().toString();
                    String type = spinnerType.getSelectedItem().toString();

                    if (titre.isEmpty()) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Le titre est requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Vérifier que la date de fin est après la date de début
                    try {
                        Date debut = dateFormat.parse(dateDebut);
                        Date fin = dateFormat.parse(dateFin);

                        if (fin.before(debut)) {
                            Toast.makeText(AcademicManagementActivity.this,
                                    "La date de fin doit être après la date de début",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Erreur de format de date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Création de l'événement dans Firestore
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("titre", titre);
                    eventData.put("description", description);
                    eventData.put("dateDebut", dateDebut);
                    eventData.put("dateFin", dateFin);
                    eventData.put("type", type);

                    db.collection("calendrier_academique")
                            .add(eventData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Événement ajouté avec succès", Toast.LENGTH_SHORT).show();
                                loadCalendar();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Erreur lors de l'ajout de l'événement: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Classes internes pour les modèles de données
    public static class Filiere {
        private String id;
        private String nom;
        private String description;

        // Constructeur vide requis pour Firestore
        public Filiere() {
        }

        // Getters et setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Groupe {
        private String id;
        private String nom;
        private String filiereId;
        private String filiereName; // Pour simplifier l'affichage

        // Constructeur vide requis pour Firestore
        public Groupe() {
        }

        // Getters et setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getFiliereId() {
            return filiereId;
        }

        public void setFiliereId(String filiereId) {
            this.filiereId = filiereId;
        }

        public String getFiliereName() {
            return filiereName;
        }

        public void setFiliereName(String filiereName) {
            this.filiereName = filiereName;
        }
    }

    public static class Cours {
        private String id;
        private String titre;
        private String enseignant;
        private String groupeId;
        private String groupeName; // Pour simplifier l'affichage

        // Constructeur vide requis pour Firestore
        public Cours() {
        }

        // Getters et setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitre() {
            return titre;
        }

        public void setTitre(String titre) {
            this.titre = titre;
        }

        public String getEnseignant() {
            return enseignant;
        }

        public void setEnseignant(String enseignant) {
            this.enseignant = enseignant;
        }

        public String getGroupeId() {
            return groupeId;
        }

        public void setGroupeId(String groupeId) {
            this.groupeId = groupeId;
        }

        public String getGroupeName() {
            return groupeName;
        }

        public void setGroupeName(String groupeName) {
            this.groupeName = groupeName;
        }
    }

    public static class CalendarEvent {
        private String id;
        private String titre;
        private String description;
        private String dateDebut;
        private String dateFin;
        private String type;

        // Constructeur vide requis pour Firestore
        public CalendarEvent() {
        }

        // Getters et setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitre() {
            return titre;
        }

        public void setTitre(String titre) {
            this.titre = titre;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDateDebut() {
            return dateDebut;
        }

        public void setDateDebut(String dateDebut) {
            this.dateDebut = dateDebut;
        }

        public String getDateFin() {
            return dateFin;
        }

        public void setDateFin(String dateFin) {
            this.dateFin = dateFin;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    // Adaptateurs pour le RecyclerView
    private class FiliereAdapter extends RecyclerView.Adapter<FiliereAdapter.ViewHolder> {

        private final List<Filiere> filiereList;

        public FiliereAdapter(List<Filiere> filiereList) {
            this.filiereList = filiereList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_filiere_academic, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Filiere filiere = filiereList.get(position);
            holder.tvNom.setText(filiere.getNom());
            holder.tvDescription.setText(filiere.getDescription());

            holder.btnEdit.setOnClickListener(v -> {
                // Afficher la boîte de dialogue de modification
                showEditFiliereDialog(filiere);
            });

            holder.btnDelete.setOnClickListener(v -> {
                // Confirmer la suppression
                new AlertDialog.Builder(AcademicManagementActivity.this)
                        .setTitle("Confirmer la suppression")
                        .setMessage("Voulez-vous vraiment supprimer cette filière ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            // Supprimer la filière de Firestore
                            db.collection("filieres").document(filiere.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AcademicManagementActivity.this,
                                                "Filière supprimée avec succès", Toast.LENGTH_SHORT).show();
                                        loadFilieres();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AcademicManagementActivity.this,
                                                "Erreur lors de la suppression: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return filiereList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNom, tvDescription;
            Button btnEdit, btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNom = itemView.findViewById(R.id.tvFiliereNom);
                tvDescription = itemView.findViewById(R.id.tvFiliereDescription);
                btnEdit = itemView.findViewById(R.id.btnEditFiliere);
                btnDelete = itemView.findViewById(R.id.btnDeleteFiliere);
            }
        }
    }

    private void showEditFiliereDialog(Filiere filiere) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_filiere, null);

        EditText etNom = view.findViewById(R.id.etNomFiliere);
        EditText etDescription = view.findViewById(R.id.etDescriptionFiliere);

        // Pré-remplir avec les valeurs actuelles
        etNom.setText(filiere.getNom());
        etDescription.setText(filiere.getDescription());

        builder.setView(view)
                .setTitle("Modifier la filière")
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String nom = etNom.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();

                    if (nom.isEmpty()) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Le nom est requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Mise à jour de la filière dans Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("nom", nom);
                    updates.put("description", description);

                    db.collection("filieres").document(filiere.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Filière mise à jour avec succès", Toast.LENGTH_SHORT).show();
                                loadFilieres();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Erreur lors de la mise à jour: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Les autres adaptateurs (GroupeAdapter, CoursAdapter, CalendarAdapter) suivraient 
    // une structure similaire mais sont omis ici pour des raisons de concision
    private class GroupeAdapter extends RecyclerView.Adapter<GroupeAdapter.ViewHolder> {
        private final List<Groupe> groupeList;

        public GroupeAdapter(List<Groupe> groupeList) {
            this.groupeList = groupeList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Implémentation pour lier les données à la vue
            Groupe groupe = groupeList.get(position);
            holder.tvNom.setText(groupe.getNom());

            // Charger le nom de la filière
            if (groupe.getFiliereId() != null) {
                db.collection("filieres").document(groupe.getFiliereId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String filiereName = documentSnapshot.getString("nom");
                                holder.tvFiliere.setText("Filière: " + filiereName);
                                groupe.setFiliereName(filiereName);
                            }
                        });
            }

            // Configurer les boutons
            holder.btnEdit.setOnClickListener(v -> {
                // TODO: Implémenter la modification du groupe
            });

            holder.btnDelete.setOnClickListener(v -> {
                // TODO: Implémenter la suppression du groupe
            });
        }

        @Override
        public int getItemCount() {
            return groupeList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNom, tvFiliere;
            Button btnEdit, btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNom = itemView.findViewById(R.id.tvGroupeNom);
                tvFiliere = itemView.findViewById(R.id.tvGroupeFiliere);
                btnEdit = itemView.findViewById(R.id.btnEditGroupe);
                btnDelete = itemView.findViewById(R.id.btnDeleteGroupe);
            }
        }
    }

    private class CoursAdapter extends RecyclerView.Adapter<CoursAdapter.ViewHolder> {
        private final List<Cours> coursList;

        public CoursAdapter(List<Cours> coursList) {
            this.coursList = coursList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cours, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Implémentation pour lier les données à la vue
            Cours cours = coursList.get(position);
            holder.tvTitre.setText(cours.getTitre());
            holder.tvEnseignant.setText("Enseignant: " + cours.getEnseignant());

            // Charger le nom du groupe
            if (cours.getGroupeId() != null) {
                db.collection("groupes").document(cours.getGroupeId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String groupeName = documentSnapshot.getString("nom");
                                holder.tvGroupe.setText("Groupe: " + groupeName);
                                cours.setGroupeName(groupeName);
                            }
                        });
            }

            // Configuration des boutons
            holder.btnEdit.setOnClickListener(v -> {
                showEditCoursDialog(cours);
            });

            holder.btnDelete.setOnClickListener(v -> {
                // Confirmation de suppression
                new AlertDialog.Builder(AcademicManagementActivity.this)
                        .setTitle("Confirmer la suppression")
                        .setMessage("Voulez-vous vraiment supprimer ce cours ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            // Supprimer le cours
                            db.collection("cours").document(cours.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AcademicManagementActivity.this,
                                                "Cours supprimé avec succès", Toast.LENGTH_SHORT).show();
                                        loadCours();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AcademicManagementActivity.this,
                                                "Erreur lors de la suppression: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return coursList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitre, tvEnseignant, tvGroupe;
            Button btnEdit, btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitre = itemView.findViewById(R.id.tvCoursTitre);
                tvEnseignant = itemView.findViewById(R.id.tvCoursEnseignant);
                tvGroupe = itemView.findViewById(R.id.tvCoursGroupe);
                btnEdit = itemView.findViewById(R.id.btnEditCours);
                btnDelete = itemView.findViewById(R.id.btnDeleteCours);
            }
        }
    }

    // Méthode pour modifier un cours
    private void showEditCoursDialog(Cours cours) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_cours, null);

        EditText etTitre = view.findViewById(R.id.etTitreCours);
        EditText etEnseignant = view.findViewById(R.id.etEnseignantCours);
        Spinner spinnerGroupe = view.findViewById(R.id.spinnerGroupe);

        // Pré-remplir les champs
        etTitre.setText(cours.getTitre());
        etEnseignant.setText(cours.getEnseignant());

        // Charger les groupes dans le spinner
        List<String> groupeNames = new ArrayList<>();
        List<String> groupeIds = new ArrayList<>();

        groupeNames.add("Sélectionner un groupe");
        groupeIds.add(""); // Placeholder pour l'option par défaut

        for (Groupe groupe : groupeList) {
            groupeNames.add(groupe.getNom());
            groupeIds.add(groupe.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, groupeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroupe.setAdapter(adapter);

        // Sélectionner le groupe actuel
        int groupePosition = 0;
        for (int i = 0; i < groupeIds.size(); i++) {
            if (groupeIds.get(i).equals(cours.getGroupeId())) {
                groupePosition = i;
                break;
            }
        }
        spinnerGroupe.setSelection(groupePosition);

        builder.setView(view)
                .setTitle("Modifier un cours")
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String titre = etTitre.getText().toString().trim();
                    String enseignant = etEnseignant.getText().toString().trim();
                    int selectedGroupePosition = spinnerGroupe.getSelectedItemPosition();

                    if (titre.isEmpty() || enseignant.isEmpty()) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedGroupePosition == 0) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Veuillez sélectionner un groupe", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String groupeId = groupeIds.get(selectedGroupePosition);

                    // Mise à jour du cours
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("titre", titre);
                    updates.put("enseignant", enseignant);
                    updates.put("groupeId", groupeId);

                    db.collection("cours").document(cours.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Cours mis à jour avec succès", Toast.LENGTH_SHORT).show();
                                loadCours();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Erreur lors de la mise à jour: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Adapter pour le calendrier
    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
        private final List<CalendarEvent> calendarList;

        public CalendarAdapter(List<CalendarEvent> calendarList) {
            this.calendarList = calendarList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_event, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CalendarEvent event = calendarList.get(position);
            holder.tvTitre.setText(event.getTitre());
            holder.tvPeriode.setText("Du " + formatDate(event.getDateDebut()) + " au " + formatDate(event.getDateFin()));
            holder.tvType.setText("Type: " + event.getType());

            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                holder.tvDescription.setText(event.getDescription());
                holder.tvDescription.setVisibility(View.VISIBLE);
            } else {
                holder.tvDescription.setVisibility(View.GONE);
            }

            // Configuration des boutons
            holder.btnEdit.setOnClickListener(v -> {
                showEditCalendarEventDialog(event);
            });

            holder.btnDelete.setOnClickListener(v -> {
                // Confirmation de suppression
                new AlertDialog.Builder(AcademicManagementActivity.this)
                        .setTitle("Confirmer la suppression")
                        .setMessage("Voulez-vous vraiment supprimer cet événement ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            // Supprimer l'événement
                            db.collection("calendrier_academique").document(event.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AcademicManagementActivity.this,
                                                "Événement supprimé avec succès", Toast.LENGTH_SHORT).show();
                                        loadCalendar();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AcademicManagementActivity.this,
                                                "Erreur lors de la suppression: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return calendarList.size();
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateString;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitre, tvPeriode, tvType, tvDescription;
            Button btnEdit, btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitre = itemView.findViewById(R.id.tvEventTitle);
                tvPeriode = itemView.findViewById(R.id.tvEventPeriod);
                tvType = itemView.findViewById(R.id.tvEventType);
                tvDescription = itemView.findViewById(R.id.tvEventDescription);
                btnEdit = itemView.findViewById(R.id.btnEditEvent);
                btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
            }
        }
    }

    // Méthode pour modifier un événement du calendrier
    private void showEditCalendarEventDialog(CalendarEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_calendar_event, null);

        EditText etTitre = view.findViewById(R.id.etTitreEvent);
        EditText etDescription = view.findViewById(R.id.etDescriptionEvent);
        TextView tvDateDebut = view.findViewById(R.id.tvDateDebut);
        TextView tvDateFin = view.findViewById(R.id.tvDateFin);
        Button btnDateDebut = view.findViewById(R.id.btnDateDebut);
        Button btnDateFin = view.findViewById(R.id.btnDateFin);
        Spinner spinnerType = view.findViewById(R.id.spinnerTypeEvent);

        // Initialiser les dates
        Calendar dateDebutCal = Calendar.getInstance();
        Calendar dateFinCal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            dateDebutCal.setTime(dateFormat.parse(event.getDateDebut()));
            dateFinCal.setTime(dateFormat.parse(event.getDateFin()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Pré-remplir les champs
        etTitre.setText(event.getTitre());
        etDescription.setText(event.getDescription());
        tvDateDebut.setText(dateFormat.format(dateDebutCal.getTime()));
        tvDateFin.setText(dateFormat.format(dateFinCal.getTime()));

        // Configuration des boutons de date
        btnDateDebut.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view1, year, month, dayOfMonth) -> {
                        dateDebutCal.set(year, month, dayOfMonth);
                        tvDateDebut.setText(dateFormat.format(dateDebutCal.getTime()));
                    },
                    dateDebutCal.get(Calendar.YEAR),
                    dateDebutCal.get(Calendar.MONTH),
                    dateDebutCal.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnDateFin.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view1, year, month, dayOfMonth) -> {
                        dateFinCal.set(year, month, dayOfMonth);
                        tvDateFin.setText(dateFormat.format(dateFinCal.getTime()));
                    },
                    dateFinCal.get(Calendar.YEAR),
                    dateFinCal.get(Calendar.MONTH),
                    dateFinCal.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Configuration du spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // Sélectionner le type actuel
        String[] types = getResources().getStringArray(R.array.event_types);
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(event.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }

        builder.setView(view)
                .setTitle("Modifier un événement")
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String titre = etTitre.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String dateDebut = tvDateDebut.getText().toString();
                    String dateFin = tvDateFin.getText().toString();
                    String type = spinnerType.getSelectedItem().toString();

                    if (titre.isEmpty()) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Le titre est requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Vérifier que la date de fin est après la date de début
                    try {
                        Date debut = dateFormat.parse(dateDebut);
                        Date fin = dateFormat.parse(dateFin);

                        if (fin.before(debut)) {
                            Toast.makeText(AcademicManagementActivity.this,
                                    "La date de fin doit être après la date de début",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        Toast.makeText(AcademicManagementActivity.this,
                                "Erreur de format de date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Mise à jour de l'événement
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("titre", titre);
                    updates.put("description", description);
                    updates.put("dateDebut", dateDebut);
                    updates.put("dateFin", dateFin);
                    updates.put("type", type);

                    db.collection("calendrier_academique").document(event.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Événement mis à jour avec succès", Toast.LENGTH_SHORT).show();
                                loadCalendar();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AcademicManagementActivity.this,
                                        "Erreur lors de la mise à jour: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}