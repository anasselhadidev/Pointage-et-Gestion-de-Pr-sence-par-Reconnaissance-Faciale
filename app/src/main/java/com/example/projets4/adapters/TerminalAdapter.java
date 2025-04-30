package com.example.projets4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Terminal;

import java.util.List;

public class TerminalAdapter extends RecyclerView.Adapter<TerminalAdapter.TerminalViewHolder> {

    public interface OnTerminalClickListener {
        void onTerminalClick(Terminal terminal);
    }

    private final List<Terminal> terminalList;
    private final OnTerminalClickListener listener;

    public TerminalAdapter(List<Terminal> terminalList, OnTerminalClickListener listener) {
        this.terminalList = terminalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TerminalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_terminal, parent, false);
        return new TerminalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TerminalViewHolder holder, int position) {
        Terminal terminal = terminalList.get(position);
        holder.bind(terminal, listener);
    }

    @Override
    public int getItemCount() {
        return terminalList.size();
    }

    static class TerminalViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTerminalName;
        private final TextView tvTerminalLocation;
        private final TextView tvTerminalStatus;
        private final TextView tvBatteryLevel;

        TerminalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTerminalName = itemView.findViewById(R.id.tvTerminalName);
            tvTerminalLocation = itemView.findViewById(R.id.tvTerminalLocation);
            tvTerminalStatus = itemView.findViewById(R.id.tvTerminalStatus);
            tvBatteryLevel = itemView.findViewById(R.id.tvBatteryLevel);
        }

        void bind(Terminal terminal, OnTerminalClickListener listener) {
            tvTerminalName.setText(terminal.getName());
            tvTerminalLocation.setText(terminal.getLocation());

            // Status avec couleur
            if (terminal.isInMaintenance()) {
                tvTerminalStatus.setText("En maintenance");
                tvTerminalStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
            } else if (terminal.isOnline()) {
                tvTerminalStatus.setText("En ligne");
                tvTerminalStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvTerminalStatus.setText("Hors ligne");
                tvTerminalStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }

            // Niveau de batterie
            tvBatteryLevel.setText(terminal.getBatteryLevel() + "%");

            // Couleur selon le niveau de batterie
            if (terminal.getBatteryLevel() < 20) {
                tvBatteryLevel.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            } else if (terminal.getBatteryLevel() < 50) {
                tvBatteryLevel.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                tvBatteryLevel.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            }

            // Click listener
            itemView.setOnClickListener(v -> listener.onTerminalClick(terminal));
        }
    }
}