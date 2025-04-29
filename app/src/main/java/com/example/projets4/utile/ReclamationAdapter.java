package com.example.projets4.utile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Reclamation;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReclamationAdapter extends RecyclerView.Adapter<ReclamationAdapter.ViewHolder> {
    private List<Reclamation> reclamationList;
    private Context context;

    public ReclamationAdapter(List<Reclamation> reclamationList, Context context) {
        this.reclamationList = reclamationList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView idClient, objet, message, date, etat;
        Button btnChangeStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            idClient = itemView.findViewById(R.id.idClient);
            objet = itemView.findViewById(R.id.objet);
            message = itemView.findViewById(R.id.message);
            date = itemView.findViewById(R.id.date);
            etat = itemView.findViewById(R.id.etat);
            btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reclamation_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reclamation r = reclamationList.get(position);

        holder.idClient.setText("Client: " + r.getIdClient());
        holder.objet.setText("Objet: " + r.getObjet());
        holder.message.setText("Message: " + r.getMessage());
        holder.date.setText("Date: " + r.getDate());

        boolean resolved = r.getEtat() != null && r.getEtat().equalsIgnoreCase("traitée");
        String statusText = resolved ? "✔ Résolue" : "✘ Non Résolue";
        int statusColor = resolved ? android.R.color.holo_green_dark : android.R.color.holo_red_dark;

        holder.etat.setText("Statut: " + statusText);
        holder.etat.setTextColor(context.getResources().getColor(statusColor));
        holder.btnChangeStatus.setVisibility(resolved ? View.GONE : View.VISIBLE);

        holder.btnChangeStatus.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("reclamations")
                    .whereEqualTo("idClient", r.getIdClient())
                    .whereEqualTo("date", r.getDate())
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String docId = query.getDocuments().get(0).getId();
                            FirebaseFirestore.getInstance().collection("reclamations")
                                    .document(docId)
                                    .update("etat", "traitée")
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(context, "Réclamation résolue", Toast.LENGTH_SHORT).show();
                                        r.setEtat("traitée");
                                        notifyItemChanged(position);
                                    });
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return reclamationList.size();
    }
}
