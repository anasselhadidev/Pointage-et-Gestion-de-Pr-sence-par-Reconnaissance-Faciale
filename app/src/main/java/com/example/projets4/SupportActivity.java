package com.example.projets4;

import android.os.Bundle;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SupportActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CardView cardNewTicket, cardFaq, cardGuides;
    private RecyclerView recyclerTickets;
    private TextView tvNoTickets;
    private Button btnNewTicket;

    // Vues pour la création de tickets
    private EditText etTicketTitle, etTicketDescription;
    private Spinner spinnerTicketCategory;
    private Button btnSubmitTicket, btnCancelTicket;
    private View layoutNewTicket;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String adminEmail;

    private List<SupportTicket> ticketList = new ArrayList<>();
    private TicketAdapter ticketAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        adminEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";

        // Initialisation des vues
        initViews();

        // Configuration de la toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Support technique");
        }

        // Configuration du RecyclerView
        recyclerTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketAdapter = new TicketAdapter(ticketList);
        recyclerTickets.setAdapter(ticketAdapter);

        // Configuration des écouteurs
        setupListeners();

        // Chargement des tickets existants
        loadTickets();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardNewTicket = findViewById(R.id.cardNewTicket);
        cardFaq = findViewById(R.id.cardFaq);
        cardGuides = findViewById(R.id.cardGuides);
        recyclerTickets = findViewById(R.id.recyclerTickets);
        tvNoTickets = findViewById(R.id.tvNoTickets);
        btnNewTicket = findViewById(R.id.btnNewTicket);

        // Vues pour la création de tickets
        layoutNewTicket = findViewById(R.id.layoutNewTicket);
        etTicketTitle = findViewById(R.id.etTicketTitle);
        etTicketDescription = findViewById(R.id.etTicketDescription);
        spinnerTicketCategory = findViewById(R.id.spinnerTicketCategory);
        btnSubmitTicket = findViewById(R.id.btnSubmitTicket);
        btnCancelTicket = findViewById(R.id.btnCancelTicket);

        // Masquer initialement le formulaire de création de ticket
        layoutNewTicket.setVisibility(View.GONE);

        // Configurer le spinner des catégories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.ticket_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTicketCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        btnNewTicket.setOnClickListener(v -> {
            // Afficher le formulaire de création de ticket
            layoutNewTicket.setVisibility(View.VISIBLE);
            recyclerTickets.setVisibility(View.GONE);
            tvNoTickets.setVisibility(View.GONE);
            btnNewTicket.setVisibility(View.GONE);
        });

        btnSubmitTicket.setOnClickListener(v -> {
            String title = etTicketTitle.getText().toString().trim();
            String description = etTicketDescription.getText().toString().trim();
            String category = spinnerTicketCategory.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(SupportActivity.this,
                        "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Création du ticket dans Firestore
            createNewTicket(title, description, category);
        });

        btnCancelTicket.setOnClickListener(v -> {
            // Masquer le formulaire et réinitialiser les champs
            resetTicketForm();
        });

        cardFaq.setOnClickListener(v -> {
            // Afficher la FAQ
            showFaq();
        });

        cardGuides.setOnClickListener(v -> {
            // Afficher les guides d'utilisation
            showGuides();
        });
    }

    private void loadTickets() {
        // Chargement des tickets depuis Firestore
        db.collection("support_tickets")
                .whereEqualTo("adminEmail", adminEmail)
                .orderBy("dateCreation", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ticketList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SupportTicket ticket = document.toObject(SupportTicket.class);
                        ticket.setId(document.getId());
                        ticketList.add(ticket);
                    }

                    if (ticketList.isEmpty()) {
                        tvNoTickets.setVisibility(View.VISIBLE);
                        recyclerTickets.setVisibility(View.GONE);
                    } else {
                        tvNoTickets.setVisibility(View.GONE);
                        recyclerTickets.setVisibility(View.VISIBLE);
                    }

                    ticketAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SupportActivity.this,
                            "Erreur lors du chargement des tickets: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewTicket(String title, String description, String category) {
        // Création d'un nouveau ticket dans Firestore
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("titre", title);
        ticketData.put("description", description);
        ticketData.put("categorie", category);
        ticketData.put("statut", "Ouvert");
        ticketData.put("adminEmail", adminEmail);
        ticketData.put("dateCreation", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        ticketData.put("reponse", "");

        db.collection("support_tickets")
                .add(ticketData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(SupportActivity.this,
                            "Ticket créé avec succès", Toast.LENGTH_SHORT).show();

                    // Réinitialiser le formulaire et afficher la liste mise à jour
                    resetTicketForm();
                    loadTickets();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SupportActivity.this,
                            "Erreur lors de la création du ticket: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void resetTicketForm() {
        // Réinitialiser les champs du formulaire
        etTicketTitle.setText("");
        etTicketDescription.setText("");
        spinnerTicketCategory.setSelection(0);

        // Masquer le formulaire et afficher la liste
        layoutNewTicket.setVisibility(View.GONE);
        recyclerTickets.setVisibility(View.VISIBLE);
        btnNewTicket.setVisibility(View.VISIBLE);

        // Afficher le message "Aucun ticket" si la liste est vide
        if (ticketList.isEmpty()) {
            tvNoTickets.setVisibility(View.VISIBLE);
        } else {
            tvNoTickets.setVisibility(View.GONE);
        }
    }

    private void showFaq() {
        // Afficher une activité ou un fragment FAQ
        Toast.makeText(this, "Fonctionnalité FAQ à venir", Toast.LENGTH_SHORT).show();
    }

    private void showGuides() {
        // Afficher une activité ou un fragment des guides d'utilisation
        Toast.makeText(this, "Fonctionnalité guides d'utilisation à venir", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Classe modèle pour les tickets de support
    public static class SupportTicket {
        private String id;
        private String titre;
        private String description;
        private String categorie;
        private String statut;
        private String adminEmail;
        private String dateCreation;
        private String reponse;

        // Constructeur vide requis pour Firestore
        public SupportTicket() {
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

        public String getCategorie() {
            return categorie;
        }

        public void setCategorie(String categorie) {
            this.categorie = categorie;
        }

        public String getStatut() {
            return statut;
        }

        public void setStatut(String statut) {
            this.statut = statut;
        }

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }

        public String getDateCreation() {
            return dateCreation;
        }

        public void setDateCreation(String dateCreation) {
            this.dateCreation = dateCreation;
        }

        public String getReponse() {
            return reponse;
        }

        public void setReponse(String reponse) {
            this.reponse = reponse;
        }
    }

    // Adaptateur pour les tickets de support
    private class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {

        private final List<SupportTicket> ticketList;

        public TicketAdapter(List<SupportTicket> ticketList) {
            this.ticketList = ticketList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_support_ticket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SupportTicket ticket = ticketList.get(position);

            holder.tvTicketTitle.setText(ticket.getTitre());
            holder.tvTicketCategory.setText("Catégorie: " + ticket.getCategorie());
            holder.tvTicketStatus.setText("Statut: " + ticket.getStatut());
            holder.tvTicketDate.setText("Créé le: " + formatDate(ticket.getDateCreation()));

            // Configurer la couleur du statut
            if (ticket.getStatut().equals("Ouvert")) {
                holder.tvTicketStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else if (ticket.getStatut().equals("En cours")) {
                holder.tvTicketStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            } else if (ticket.getStatut().equals("Résolu")) {
                holder.tvTicketStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            }

            // Afficher ou masquer la réponse
            if (ticket.getReponse() != null && !ticket.getReponse().isEmpty()) {
                holder.tvTicketResponse.setVisibility(View.VISIBLE);
                holder.tvTicketResponse.setText("Réponse: " + ticket.getReponse());
            } else {
                holder.tvTicketResponse.setVisibility(View.GONE);
            }

            // Configurer le bouton pour afficher les détails
            holder.btnViewDetails.setOnClickListener(v -> {
                // Afficher les détails du ticket
                showTicketDetails(ticket);
            });

            // Configurer le bouton pour fermer le ticket
            holder.btnCloseTicket.setOnClickListener(v -> {
                // Fermer le ticket
                closeTicket(ticket);
            });

            // Masquer le bouton de fermeture si le ticket est déjà résolu
            if (ticket.getStatut().equals("Résolu")) {
                holder.btnCloseTicket.setVisibility(View.GONE);
            } else {
                holder.btnCloseTicket.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return ticketList.size();
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateString;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTicketTitle, tvTicketCategory, tvTicketStatus, tvTicketDate, tvTicketResponse;
            Button btnViewDetails, btnCloseTicket;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTicketTitle = itemView.findViewById(R.id.tvTicketTitle);
                tvTicketCategory = itemView.findViewById(R.id.tvTicketCategory);
                tvTicketStatus = itemView.findViewById(R.id.tvTicketStatus);
                tvTicketDate = itemView.findViewById(R.id.tvTicketDate);
                tvTicketResponse = itemView.findViewById(R.id.tvTicketResponse);
                btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
                btnCloseTicket = itemView.findViewById(R.id.btnCloseTicket);
            }
        }
    }

    private void showTicketDetails(SupportTicket ticket) {
        // Afficher une boîte de dialogue avec les détails du ticket
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_ticket_details, null);

        TextView tvTitle = view.findViewById(R.id.tvDialogTicketTitle);
        TextView tvDescription = view.findViewById(R.id.tvDialogTicketDescription);
        TextView tvCategory = view.findViewById(R.id.tvDialogTicketCategory);
        TextView tvStatus = view.findViewById(R.id.tvDialogTicketStatus);
        TextView tvDate = view.findViewById(R.id.tvDialogTicketDate);
        TextView tvResponse = view.findViewById(R.id.tvDialogTicketResponse);

        tvTitle.setText(ticket.getTitre());
        tvDescription.setText(ticket.getDescription());
        tvCategory.setText("Catégorie: " + ticket.getCategorie());
        tvStatus.setText("Statut: " + ticket.getStatut());

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date date = inputFormat.parse(ticket.getDateCreation());
            tvDate.setText("Créé le: " + outputFormat.format(date));
        } catch (Exception e) {
            tvDate.setText("Créé le: " + ticket.getDateCreation());
        }

        if (ticket.getReponse() != null && !ticket.getReponse().isEmpty()) {
            tvResponse.setText(ticket.getReponse());
            tvResponse.setVisibility(View.VISIBLE);
        } else {
            tvResponse.setVisibility(View.GONE);
        }

        builder.setView(view)
                .setTitle("Détails du ticket")
                .setPositiveButton("Fermer", null)
                .show();
    }

    private void closeTicket(SupportTicket ticket) {
        // Demander confirmation avant de fermer le ticket
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Fermer le ticket")
                .setMessage("Êtes-vous sûr de vouloir marquer ce ticket comme résolu ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    // Mettre à jour le statut du ticket dans Firestore
                    db.collection("support_tickets").document(ticket.getId())
                            .update("statut", "Résolu")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SupportActivity.this,
                                        "Ticket marqué comme résolu", Toast.LENGTH_SHORT).show();
                                loadTickets();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SupportActivity.this,
                                        "Erreur lors de la mise à jour du ticket: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Non", null)
                .show();
    }
}