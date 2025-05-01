package com.example.projets4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final List<User> userList;
    private final OnUserClickListener listener;

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvEmail;
        private final TextView tvRole;
        private final TextView tvStatus;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(User user, OnUserClickListener listener) {
            tvName.setText(user.getPrenom() + " " + user.getNom());
            tvEmail.setText(user.getEmail());

            // Convertir le rôle pour l'affichage
            String displayRole = "Étudiant";
            if ("professeur".equals(user.getRole())) {
                displayRole = "Enseignant";
            } else if ("admin".equals(user.getRole())) {
                displayRole = "Administrateur";
            }
            tvRole.setText(displayRole);

            // Statut (actif par défaut)
            tvStatus.setText("Actif");
            tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));

            // Réagir au clic sur l'élément
            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }
}