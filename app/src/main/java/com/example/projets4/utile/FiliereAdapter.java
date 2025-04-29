package com.example.projets4.utile;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Filiere;
import com.example.projets4.model.Matiere;
import com.example.projets4.utile.MatiereAdapter;
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

        // load nested matieres
        loadMatieres(f, holder);

        // long-press: open multi-add dialog
        holder.itemView.setOnLongClickListener(v -> {
            View dlg = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.dialog_add_matieres, null);
            AlertDialog.Builder b = new AlertDialog.Builder(v.getContext())
                    .setView(dlg)
                    .setTitle("Ajouter des Matières pour " + f.getNom())
                    .setNegativeButton("Annuler", null);

            LinearLayout container = dlg.findViewById(R.id.containerMatiereFields);
            dlg.findViewById(R.id.btnAddField)
                    .setOnClickListener(x -> {
                        EditText et = new EditText(v.getContext());
                        et.setHint("Nom Matière");
                        container.addView(et);
                    });
            // add initial
            container.addView(new androidx.appcompat.widget.AppCompatEditText(v.getContext()) {{ setHint("Nom Matière"); }});

            b.setPositiveButton("Valider", (d, w) -> {
                List<String> names = new ArrayList<>();
                for (int i = 0; i < container.getChildCount(); i++) {
                    View c = container.getChildAt(i);
                    if (c instanceof EditText) {
                        String s = ((EditText) c).getText().toString().trim();
                        if (!s.isEmpty()) names.add(s);
                    }
                }
                for (String nm : names) {
                    db.collection("filieres")
                            .document(f.getId())
                            .collection("matieres")
                            .add(new Matiere(nm));
                }
                // refresh nested list
                refreshMatiereList(f);
            });
            b.show();
            return true;
        });
    }

    @Override
    public int getItemCount() { return filieres.size(); }

    // helper to (re)load matieres under a filiere
    public void refreshMatiereList(Filiere f) {
        // find its adapter position
        int pos = filieres.indexOf(f);
        if (pos >= 0) notifyItemChanged(pos);
    }

    private void loadMatieres(Filiere f, FiliereViewHolder h) {
        db.collection("filieres")
                .document(f.getId())
                .collection("matieres")
                .get()
                .addOnSuccessListener(q -> {
                    List<Matiere> list = new ArrayList<>();
                    for (var ds : q) list.add(ds.toObject(Matiere.class));
                    MatiereAdapter ma = new MatiereAdapter(list);
                    h.recyclerMatieres.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
                    h.recyclerMatieres.setAdapter(ma);
                });
    }

    static class FiliereViewHolder extends RecyclerView.ViewHolder {
        TextView nom, desc;
        Button btnDelete;
        RecyclerView recyclerMatieres;
        FiliereViewHolder(View v) {
            super(v);
            nom = v.findViewById(R.id.textViewNomFiliere);
            desc = v.findViewById(R.id.textViewDescriptionFiliere);
            btnDelete = v.findViewById(R.id.btnDeleteFiliere);
            recyclerMatieres = v.findViewById(R.id.recyclerMatieres);
        }
    }
}