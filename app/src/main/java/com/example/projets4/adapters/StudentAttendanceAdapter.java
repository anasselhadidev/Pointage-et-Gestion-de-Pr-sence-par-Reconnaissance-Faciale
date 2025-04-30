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
import com.example.projets4.model.Etudiant;

import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder> {

    public interface OnStatusChangeListener {
        void onStatusChangeRequested(Etudiant etudiant, String newStatus);
    }

    private final List<Etudiant> studentList;
    private final OnStatusChangeListener listener;

    public StudentAttendanceAdapter(List<Etudiant> studentList, OnStatusChangeListener listener) {
        this.studentList = studentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Etudiant etudiant = studentList.get(position);

        holder.tvName.setText(etudiant.getPrenom() + " " + etudiant.getNom());
        holder.tvStatus.setText(etudiant.getStatut());

        // Afficher l'heure de pointage si disponible
        if (etudiant.getHeurePointage() != null && !etudiant.getHeurePointage().isEmpty()) {
            holder.tvTime.setText("Pointage à: " + etudiant.getHeurePointage());
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        // Appliquer la couleur en fonction du statut
        switch (etudiant.getStatut()) {
            case "Présent":
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Vert
                break;
            case "Retard":
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "Absent":
                holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Rouge
                break;
            default:
                holder.tvStatus.setTextColor(Color.parseColor("#757575")); // Gris
                break;
        }

        // Configuration des boutons d'action
        holder.btnPresent.setOnClickListener(v -> listener.onStatusChangeRequested(etudiant, "Présent"));
        holder.btnRetard.setOnClickListener(v -> listener.onStatusChangeRequested(etudiant, "Retard"));
        holder.btnAbsent.setOnClickListener(v -> listener.onStatusChangeRequested(etudiant, "Absent"));

        // Désactiver le bouton correspondant au statut actuel
        holder.btnPresent.setEnabled(!etudiant.getStatut().equals("Présent"));
        holder.btnRetard.setEnabled(!etudiant.getStatut().equals("Retard"));
        holder.btnAbsent.setEnabled(!etudiant.getStatut().equals("Absent"));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvStatus;
        TextView tvTime;
        Button btnPresent;
        Button btnRetard;
        Button btnAbsent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnPresent = itemView.findViewById(R.id.btnPresent);
            btnRetard = itemView.findViewById(R.id.btnRetard);
            btnAbsent = itemView.findViewById(R.id.btnAbsent);
        }
    }
}