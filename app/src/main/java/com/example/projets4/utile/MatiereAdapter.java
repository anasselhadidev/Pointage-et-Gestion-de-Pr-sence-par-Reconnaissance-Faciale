package com.example.projets4.utile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Matiere;

import java.util.List;

public class MatiereAdapter extends RecyclerView.Adapter<MatiereAdapter.MatiereViewHolder> {
    private final List<Matiere> matieres;

    public MatiereAdapter(List<Matiere> matieres) {
        this.matieres = matieres;
    }

    @NonNull
    @Override
    public MatiereViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matiere, parent, false);
        return new MatiereViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MatiereViewHolder holder, int position) {
        holder.nom.setText(matieres.get(position).getNom());
    }

    @Override
    public int getItemCount() {
        return matieres.size();
    }

    static class MatiereViewHolder extends RecyclerView.ViewHolder {
        TextView nom;
        MatiereViewHolder(View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.textViewNomMatiere);
        }
    }
}