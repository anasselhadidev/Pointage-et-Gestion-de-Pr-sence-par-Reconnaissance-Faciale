package com.example.projets4.utile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Filiere;

import java.util.List;

public class FiliereSimpleAdapter
        extends RecyclerView.Adapter<FiliereSimpleAdapter.VH> {

    public interface OnClick { void onClick(Filiere f); }

    private final List<Filiere> data;
    private final OnClick callback;

    public FiliereSimpleAdapter(List<Filiere> data, OnClick callback) {
        this.data = data;
        this.callback = callback;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filiere_simple, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Filiere f = data.get(pos);
        holder.text.setText(f.getNom());
        holder.itemView.setOnClickListener(v -> callback.onClick(f));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView text;
        VH(@NonNull View v) {
            super(v);
            text = v.findViewById(R.id.textViewFiliere);
        }
    }
}
