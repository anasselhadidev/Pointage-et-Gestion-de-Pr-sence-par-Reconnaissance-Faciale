package com.example.projets4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Rapport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private final List<Rapport> reportList;

    public ReportAdapter(List<Rapport> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rapport rapport = reportList.get(position);

        // Affichage du titre et de la date de génération
        holder.tvTitle.setText(rapport.getTitre());

        // Formatter la date de génération
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(rapport.getDateGeneration());
            holder.tvGenDate.setText("Généré le " + outputFormat.format(date));
        } catch (ParseException e) {
            holder.tvGenDate.setText("Généré le " + rapport.getDateGeneration());
        }

        // Affichage de la période concernée
        holder.tvPeriod.setText("Période: " + rapport.getDateDebut() + " au " + rapport.getDateFin());

        // Affichage des statistiques
        String statsText = String.format(Locale.getDefault(),
                "Séances: %d | Étudiants: %d\n" +
                        "Présents: %d | Retards: %d | Absents: %d\n" +
                        "Taux de présence: %.1f%%",
                rapport.getNombreSeances(), rapport.getNombreEtudiants(),
                rapport.getNombrePresents(), rapport.getNombreRetards(),
                rapport.getNombreAbsents(), rapport.getTauxPresence());

        holder.tvStats.setText(statsText);

        // Configuration du bouton d'exportation
        holder.btnExport.setOnClickListener(v -> {
            // Simuler l'export (à implémenter pour une véritable exportation PDF/Excel)
            Toast.makeText(v.getContext(),
                    "Export du rapport " + rapport.getTitre(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvGenDate;
        TextView tvPeriod;
        TextView tvStats;
        Button btnExport;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReportTitle);
            tvGenDate = itemView.findViewById(R.id.tvGenerationDate);
            tvPeriod = itemView.findViewById(R.id.tvReportPeriod);
            tvStats = itemView.findViewById(R.id.tvReportStats);
            btnExport = itemView.findViewById(R.id.btnExport);
        }
    }
}