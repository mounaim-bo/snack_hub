package com.example.snackhub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snackhub.R;
import com.example.snackhub.MenuSnackActivity;
import com.example.snackhub.models.Snack;

import java.util.List;

public class SnackAdapter extends RecyclerView.Adapter<SnackAdapter.SnackViewHolder> {

    private Context context;
    private List<Snack> snackList;

    public SnackAdapter(Context context, List<Snack> snackList) {
        this.context = context;
        this.snackList = snackList;
    }

    @NonNull
    @Override
    public SnackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.snack_item, parent, false);
        return new SnackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SnackViewHolder holder, int position) {
        Snack snack = snackList.get(position);

        // Remplir les champs avec vos données existantes
        holder.snackName.setText(snack.getName() != null ? snack.getName() : "Nom non disponible");
        holder.snackPhone.setText(snack.getPhone() != null ? snack.getPhone() : "Non disponible");
        holder.snackDescription.setText(snack.getDescription() != null ? snack.getDescription() : "Description non disponible");
        holder.snackAddress.setText(snack.getAddress() != null ? snack.getAddress() : "Adresse non disponible");

        // Charger l'image avec Glide
        if (snack.getMainImageUrl() != null && !snack.getMainImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(snack.getMainImageUrl())
                    .placeholder(R.drawable.images3)
                    .error(R.drawable.images3)
                    .centerCrop()
                    .into(holder.snackImage);
        } else {
            holder.snackImage.setImageResource(R.drawable.images3);
        }

        // Gestion du clic pour ouvrir le menu du snack
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MenuSnackActivity.class);

                // Passer les informations du snack
                intent.putExtra("snackId", snack.getId());
                intent.putExtra("snackName", snack.getName());
                intent.putExtra("snackPhone", snack.getPhone());
                intent.putExtra("snackDescription", snack.getDescription());
                intent.putExtra("snackAddress", snack.getAddress());
                intent.putExtra("snackEmail", snack.getEmail());
                intent.putExtra("snackOpeningHours", snack.getOpeningHours());
                intent.putExtra("snackMainImageUrl", snack.getMainImageUrl());
                intent.putExtra("snackUserId", snack.getUserId());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return snackList.size();
    }

    public static class SnackViewHolder extends RecyclerView.ViewHolder {
        ImageView snackImage;
        TextView snackName, snackDescription, snackPhone, snackAddress;

        public SnackViewHolder(@NonNull View itemView) {
            super(itemView);

            snackImage = itemView.findViewById(R.id.snackImage);
            snackName = itemView.findViewById(R.id.snackName);
            snackDescription = itemView.findViewById(R.id.snackDescription);
            snackPhone = itemView.findViewById(R.id.snackPhone);
            snackAddress = itemView.findViewById(R.id.snackAddress);
        }
    }
}