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
import com.bumptech.glide.request.RequestOptions;
import com.example.snackhub.R;
import com.example.snackhub.models.Repas;
import com.example.snackhub.SingleRepasActivity;
import com.example.snackhub.utils.PanierManager;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Repas> repasList;
    private Context context;

    public ImageAdapter(Context context, List<Repas> repasList) {
        this.context = context;
        this.repasList = repasList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.repa_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Repas repas = repasList.get(position);

        // Charger l'image avec Glide ou image par défaut
        loadImage(holder.imageView, repas);

        holder.nom.setText(repas.getNom());
        holder.prix.setText(repas.getPrixFormate()); // Utilise la méthode formatée

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SingleRepasActivity.class);

                // VOS DONNÉES EXISTANTES (inchangées)
                intent.putExtra("repasId", repas.getId());
                intent.putExtra("repasName", repas.getNom());
                intent.putExtra("repasDescription", repas.getDescription());
                intent.putExtra("repasPrice", repas.getPrix());
                intent.putExtra("repasImageUrl", repas.getImageUrl());
                intent.putExtra("repasCategorie", repas.getCategorie());
                intent.putExtra("repasDisponible", repas.isDisponible());

                // AJOUT MINIMAL : Données du snack pour le PanierActivity
                PanierManager panierManager = PanierManager.getInstance();
                intent.putExtra("snackId", panierManager.getCurrentSnackId());
                intent.putExtra("snackName", panierManager.getCurrentSnackName());
                intent.putExtra("snackDescription", panierManager.getCurrentSnackDescription());
                intent.putExtra("snackPhone", panierManager.getCurrentSnackPhone());
                intent.putExtra("snackAddress", panierManager.getCurrentSnackAddress());
                intent.putExtra("snackEmail", panierManager.getCurrentSnackEmail());
                intent.putExtra("snackOpeningHours", panierManager.getCurrentSnackOpeningHours());
                intent.putExtra("snackMainImageUrl", panierManager.getCurrentSnackMainImageUrl());

                context.startActivity(intent);
            }
        });
    }

    private void loadImage(ImageView imageView, Repas repas) {
        // Si l'image URL existe, utilise Glide pour la charger
        if (repas.getImageUrl() != null && !repas.getImageUrl().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(getDefaultImageForCategory(repas.getCategorie()))
                    .error(getDefaultImageForCategory(repas.getCategorie()));

            Glide.with(context)
                    .load(repas.getImageUrl())
                    .apply(requestOptions)
                    .into(imageView);
        } else {
            // Sinon, utilise une image par défaut selon la catégorie
            imageView.setImageResource(getDefaultImageForCategory(repas.getCategorie()));
        }
    }

    private int getDefaultImageForCategory(String categorie) {
        if (categorie == null) return R.drawable.food1;

        switch (categorie.toLowerCase()) {
            case "sandwich":
                return R.drawable.food1;
            case "plat chaud":
                return R.drawable.food2;
            case "dessert":
                return R.drawable.food3;
            case "boisson":
                return R.drawable.food4;
            case "salade":
                return R.drawable.food5;
            case "pizza":
                return R.drawable.food1; // ou une image spécifique pour pizza
            default:
                return R.drawable.food1;
        }
    }

    @Override
    public int getItemCount() {
        return repasList != null ? repasList.size() : 0;
    }

    // Méthode pour mettre à jour la liste
    public void updateList(List<Repas> newRepasList) {
        this.repasList = newRepasList;
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nom, prix;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nom = itemView.findViewById(R.id.mealName);
            prix = itemView.findViewById(R.id.mealPrice);
        }
    }
}