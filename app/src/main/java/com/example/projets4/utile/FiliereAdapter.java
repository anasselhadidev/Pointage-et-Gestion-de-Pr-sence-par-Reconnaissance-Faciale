package com.example.projets4.utile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Filiere;
import com.example.projets4.model.Matiere;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FiliereAdapter extends RecyclerView.Adapter<FiliereAdapter.FiliereViewHolder> {
    private List<Filiere> filieres;
    private FirebaseFirestore db;

    public FiliereAdapter(List<Filiere> filieres, FirebaseFirestore db) {
        this.filieres = filieres;
        this.db = db;
    }

    @NonNull
    @Override
    public FiliereViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filiere, parent, false);
        return new FiliereViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FiliereViewHolder holder, int position) {
        Filiere f = filieres.get(position);
        holder.nom.setText(f.getNom());
        holder.desc.setText(f.getDescription());

        // Edit on click
        holder.itemView.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(holder.itemView.getContext());
            View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_edit_filiere, null);
            builder.setView(dialogView);
            builder.setTitle("Modifier la filière");

            // Set existing values
            final android.widget.EditText editNom = dialogView.findViewById(R.id.editNom);
            final android.widget.EditText editDesc = dialogView.findViewById(R.id.editDescription);
            editNom.setText(f.getNom());
            editDesc.setText(f.getDescription());

            builder.setPositiveButton("Enregistrer", (dialog, which) -> {
                String nouveauNom = editNom.getText().toString().trim();
                String nouvelleDesc = editDesc.getText().toString().trim();

                if (!nouveauNom.isEmpty()) {
                    db.collection("filieres").document(f.getId())
                            .update("nom", nouveauNom, "description", nouvelleDesc)
                            .addOnSuccessListener(aVoid -> {
                                f.setNom(nouveauNom);
                                f.setDescription(nouvelleDesc);
                                notifyItemChanged(position);
                                Toast.makeText(holder.itemView.getContext(), "Filière modifiée", Toast.LENGTH_SHORT).show();
                            });
                }
            });

            builder.setNegativeButton("Annuler", null);
            builder.create().show();
        });

        // Delete on button click
        holder.btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Supprimer la filière")
                    .setMessage("Voulez-vous vraiment supprimer cette filière ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        db.collection("filieres").document(f.getId()).delete()
                                .addOnSuccessListener(unused -> {
                                    filieres.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(holder.itemView.getContext(), "Filière supprimée", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });

        // Add Matières for this Filière (via a Dialog)
        holder.itemView.setOnLongClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(holder.itemView.getContext());
            View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_add_matiere, null);
            builder.setView(dialogView);
            builder.setTitle("Ajouter des Matières");

            final android.widget.EditText editMatiereNom = dialogView.findViewById(R.id.editMatiereNom);
            builder.setPositiveButton("Ajouter", (dialog, which) -> {
                String matiereNom = editMatiereNom.getText().toString().trim();
                if (!matiereNom.isEmpty()) {
                    Matiere newMatiere = new Matiere(matiereNom);

                    // Add Matière to Firestore under the Filière's subcollection
                    db.collection("filieres")
                            .document(f.getId())
                            .collection("matieres")
                            .add(newMatiere)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(holder.itemView.getContext(), "Matière ajoutée", Toast.LENGTH_SHORT).show();
                            });
                }
            });

            builder.setNegativeButton("Annuler", null);
            builder.create().show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filieres.size();
    }

    static class FiliereViewHolder extends RecyclerView.ViewHolder {
        TextView nom, desc;
        Button btnDelete;

        FiliereViewHolder(View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.textViewNomFiliere);
            desc = itemView.findViewById(R.id.textViewDescriptionFiliere);
            btnDelete = itemView.findViewById(R.id.btnDeleteFiliere);
        }
    }
}
