package com.example.projets4.utile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    List<User> userList;

    public UserAdapter(List<User> list) {
        this.userList = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView email, role, nom, prenom, tel;

        public ViewHolder(View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.emailUser);
            role = itemView.findViewById(R.id.roleUser);
            nom = itemView.findViewById(R.id.nomUser);
            prenom = itemView.findViewById(R.id.prenomUser);
            tel = itemView.findViewById(R.id.telUser);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.email.setText("Email: " + user.getEmail());
        holder.role.setText("Rôle: " + user.getRole());
        holder.nom.setText("Nom: " + user.getNom());
        holder.prenom.setText("Prénom: " + user.getPrenom());
        holder.tel.setText("Téléphone: " + user.getTelephone());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
