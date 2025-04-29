package com.example.projets4.utile;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Filiere;
import com.example.projets4.model.Matiere;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FiliereAdapter extends RecyclerView.Adapter<FiliereAdapter.FiliereViewHolder> {
    private final List<Filiere> filieres;
    private final FirebaseFirestore db;

    public FiliereAdapter(List<Filiere> filieres, FirebaseFirestore db) {
        this.filieres = filieres;
        this.db = db;
    }

    @NonNull
    @Override
    public FiliereViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filiere, parent, false);
        return new FiliereViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FiliereViewHolder holder, int pos) {
        Filiere f = filieres.get(pos);
        holder.nom.setText(f.getNom());
        holder.desc.setText(f.getDescription());

        // On (re)charge les matières pour cette filière
        loadMatieres(f, holder);

        // ... votre code de suppression / édition de filière ici ...
        // 2) Bouton Supprimer
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Supprimer la filière")
                    .setMessage("Voulez-vous vraiment supprimer \"" + f.getNom() + "\" ?")
                    .setPositiveButton("Oui", (d, w) -> {
                        db.collection("filieres")
                                .document(f.getId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    int posNow = holder.getAdapterPosition();
                                    filieres.remove(posNow);
                                    notifyItemRemoved(posNow);
                                });
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return filieres.size();
    }

    /** Rafraîchit la carte de la filière pour recharger onBind */
    public void refreshMatiereList(Filiere f) {
        int pos = filieres.indexOf(f);
        if (pos >= 0) notifyItemChanged(pos);
    }



    /** Charge les matières et remplace l’adapter vide */
    private void loadMatieres(Filiere filiere, FiliereViewHolder h) {
        db.collection("filieres")
                .document(filiere.getId())
                .collection("matieres")
                .get()
                .addOnSuccessListener(q -> {
                    List<Matiere> list = new ArrayList<>();
                    for (var ds : q) {
                        Matiere m = ds.toObject(Matiere.class);
                        m.setId(ds.getId());
                        list.add(m);
                    }
                    // Remplacement de l’adapter par celui-ci
                    MatiereAdapter ma = new MatiereAdapter(list, m -> {
                        showEditMatiereDialog(h.itemView.getContext(), filiere, m);
                    });
                    h.recyclerMatieres.setAdapter(ma);
                });
    }

    /** Affiche la boîte de dialogue d’édition d’une matière */
    private void showEditMatiereDialog(Context ctx, Filiere filiere, Matiere m) {
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.dialog_add_matiere, null);
        EditText et = v.findViewById(R.id.editMatiereNom);
        et.setText(m.getNom());

        b.setView(v)
                .setTitle("Modifier Matière")
                .setPositiveButton("Enregistrer", (d, w) -> {
                    String nouveauNom = et.getText().toString().trim();
                    if (nouveauNom.isEmpty()) return;
                    db.collection("filieres")
                            .document(filiere.getId())
                            .collection("matieres")
                            .document(m.getId())
                            .update("nom", nouveauNom)
                            .addOnSuccessListener(u -> refreshMatiereList(filiere));
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    static class FiliereViewHolder extends RecyclerView.ViewHolder {
        TextView nom, desc;
        RecyclerView recyclerMatieres;
        Button btnDelete; // si vous avez un bouton supprimer

        FiliereViewHolder(View v) {
            super(v);
            nom = v.findViewById(R.id.textViewNomFiliere);
            desc = v.findViewById(R.id.textViewDescriptionFiliere);
            recyclerMatieres = v.findViewById(R.id.recyclerMatieres);

            // ● ATTACHE IMMÉDIATEMENT un layoutManager + adapter vide :
            recyclerMatieres.setLayoutManager(
                    new LinearLayoutManager(v.getContext()));
            // adapter vide avec callback no-op
            MatiereAdapter empty = new MatiereAdapter(
                    new ArrayList<>(),
                    m -> { /* rien */ }
            );
            recyclerMatieres.setAdapter(empty);

            btnDelete = v.findViewById(R.id.btnDeleteFiliere);
        }
    }
}