package com.example.projets4;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoriquePresenceActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CalendarView calendarView;
    private RecyclerView recyclerPointages;
    private TextView tvAucunPointage;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String etudiantEmail;

    private List<Pointage> pointageList = new ArrayList<>();
    private PointageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique_presence);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        etudiantEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Historique de présence");
        }

        // Configuration du recyclerView
        adapter = new PointageAdapter(pointageList);
        recyclerPointages.setLayoutManager(new LinearLayoutManager(this));
        recyclerPointages.setAdapter(adapter);

        // Configuration du calendrier
        setupCalendarView();

        // Chargement des pointages pour la date du jour
        loadPointagesForDate(new Date());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendarView);
        recyclerPointages = findViewById(R.id.recyclerPointages);
        tvAucunPointage = findViewById(R.id.tvAucunPointage);
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            loadPointagesForDate(calendar.getTime());
        });
    }

    private void loadPointagesForDate(Date date) {
        if (etudiantEmail.isEmpty()) {
            return;
        }

        // Formatage de la date pour la requête
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = sdf.format(date);

        // Requête Firebase pour obtenir les pointages de la date sélectionnée
        db.collection("pointages")
                .whereEqualTo("etudiantEmail", etudiantEmail)
                .whereGreaterThanOrEqualTo("date", dateString + " 00:00:00")
                .whereLessThanOrEqualTo("date", dateString + " 23:59:59")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pointageList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        tvAucunPointage.setVisibility(android.view.View.VISIBLE);
                        recyclerPointages.setVisibility(android.view.View.GONE);
                    } else {
                        tvAucunPointage.setVisibility(android.view.View.GONE);
                        recyclerPointages.setVisibility(android.view.View.VISIBLE);

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Pointage pointage = document.toObject(Pointage.class);
                            if (pointage != null) {
                                pointageList.add(pointage);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HistoriquePresenceActivity.this,
                            "Erreur lors du chargement des pointages: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Classe interne pour les données de pointage
    public static class Pointage {
        private String cours;
        private String date;
        private String typePointage; // "Entrée" ou "Sortie"
        private String terminal;
        private String statut; // "Présent", "Retard", etc.

        // Constructeur vide requis pour Firestore
        public Pointage() {}

        public Pointage(String cours, String date, String typePointage, String terminal, String statut) {
            this.cours = cours;
            this.date = date;
            this.typePointage = typePointage;
            this.terminal = terminal;
            this.statut = statut;
        }

        // Getters
        public String getCours() { return cours; }
        public String getDate() { return date; }
        public String getTypePointage() { return typePointage; }
        public String getTerminal() { return terminal; }
        public String getStatut() { return statut; }
    }

    // Adapter pour afficher la liste des pointages
    private class PointageAdapter extends RecyclerView.Adapter<PointageAdapter.ViewHolder> {

        private List<Pointage> localDataSet;

        public PointageAdapter(List<Pointage> dataSet) {
            localDataSet = dataSet;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvCours;
            private final TextView tvDate;
            private final TextView tvTypePointage;
            private final TextView tvTerminal;
            private final TextView tvStatut;

            public ViewHolder(android.view.View view) {
                super(view);
                tvCours = view.findViewById(R.id.tvCours);
                tvDate = view.findViewById(R.id.tvDate);
                tvTypePointage = view.findViewById(R.id.tvTypePointage);
                tvTerminal = view.findViewById(R.id.tvTerminal);
                tvStatut = view.findViewById(R.id.tvStatut);
            }

            public TextView getTvCours() { return tvCours; }
            public TextView getTvDate() { return tvDate; }
            public TextView getTvTypePointage() { return tvTypePointage; }
            public TextView getTvTerminal() { return tvTerminal; }
            public TextView getTvStatut() { return tvStatut; }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pointage, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Pointage pointage = localDataSet.get(position);
            holder.getTvCours().setText(pointage.getCours());
            holder.getTvDate().setText(pointage.getDate());
            holder.getTvTypePointage().setText(pointage.getTypePointage());
            holder.getTvTerminal().setText("Terminal: " + pointage.getTerminal());
            holder.getTvStatut().setText(pointage.getStatut());

            // Coloration du statut
            if (pointage.getStatut().equals("Présent")) {
                holder.getTvStatut().setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (pointage.getStatut().equals("Retard")) {
                holder.getTvStatut().setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                holder.getTvStatut().setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }

        @Override
        public int getItemCount() {
            return localDataSet.size();
        }
    }
}