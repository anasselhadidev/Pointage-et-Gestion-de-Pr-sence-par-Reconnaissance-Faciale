package com.example.projets4.utile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Matiere;

import java.util.List;

public class MatiereAdapter
        extends RecyclerView.Adapter<MatiereAdapter.VH> {

    public interface OnEdit { void onEdit(Matiere m); }

    private final List<Matiere> data;
    private final OnEdit callback;

    public MatiereAdapter(List<Matiere> data, OnEdit callback) {
        this.data = data;
        this.callback = callback;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_matiere, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Matiere m = data.get(pos);
        holder.nom.setText(m.getNom());
        holder.btnEdit.setOnClickListener(v -> callback.onEdit(m));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView nom;
        ImageButton btnEdit;
        VH(@NonNull View v) {
            super(v);
            nom = v.findViewById(R.id.textViewMatiereNom);
            btnEdit = v.findViewById(R.id.btnEditMatiere);
        }
    }
}