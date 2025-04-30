package com.example.projets4.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Justificatif;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JustificationAdapter extends RecyclerView.Adapter<JustificationAdapter.ViewHolder> {

    public interface OnJustificationActionListener {
        void onViewDocument(Justificatif justificatif);
        void onApprove(Justificatif justificatif);
        void onReject(Justificatif justificatif);
    }

    private final List<Justificatif> justificatifList;
    private final OnJustificationActionListener listener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public JustificationAdapter(List<Justificatif> justificatifList, OnJustificationActionListener listener) {
        this.justificatifList = justificatifList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_justificatif, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Justificatif justificatif = justificatifList.get(position);

        // Chercher les informations de l'étudiant
        db.collection("etudiants").document(justificatif.getEtudiantEmail())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String nom = documentSnapshot.getString("nom");
                    String prenom = documentSnapshot.getString("prenom");

                    holder.tvStudentName.setText(prenom + " " + nom);
                });

        // Afficher les détails du justificatif
        holder.tvDateAbsence.setText("Absence le: " + formatDate(justificatif.getDateAbsence()));
        holder.tvDateSoumission.setText("Soumis le: " + formatDate(justificatif.getDateSoumission()));
        holder.tvMotif.setText("Motif: " + justificatif.getMotif());

        if (justificatif.getCommentaire() != null && !justificatif.getCommentaire().isEmpty()) {
            holder.tvCommentaire.setText("Commentaire: " + justificatif.getCommentaire());
            holder.tvCommentaire.setVisibility(View.VISIBLE);
        } else {
            holder.tvCommentaire.setVisibility(View.GONE);
        }

        // Définir la couleur et le texte en fonction du statut
        holder.tvStatus.setText(justificatif.getStatus());
        switch (justificatif.getStatus()) {
            case "En attente":
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                break;
            case "Accepté":
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Vert
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
                break;
            case "Refusé":
                holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Rouge
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
                break;
            default:
                holder.tvStatus.setTextColor(Color.parseColor("#757575")); // Gris
                break;
        }

        // Configuration des écouteurs de boutons
        holder.btnViewDoc.setOnClickListener(v -> listener.onViewDocument(justificatif));
        holder.btnApprove.setOnClickListener(v -> listener.onApprove(justificatif));
        holder.btnReject.setOnClickListener(v -> listener.onReject(justificatif));
    }

    @Override
    public int getItemCount() {
        return justificatifList.size();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName;
        TextView tvDateAbsence;
        TextView tvDateSoumission;
        TextView tvMotif;
        TextView tvCommentaire;
        TextView tvStatus;
        Button btnViewDoc;
        Button btnApprove;
        Button btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvDateAbsence = itemView.findViewById(R.id.tvDateAbsence);
            tvDateSoumission = itemView.findViewById(R.id.tvDateSoumission);
            tvMotif = itemView.findViewById(R.id.tvMotif);
            tvCommentaire = itemView.findViewById(R.id.tvCommentaire);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnViewDoc = itemView.findViewById(R.id.btnViewDoc);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}