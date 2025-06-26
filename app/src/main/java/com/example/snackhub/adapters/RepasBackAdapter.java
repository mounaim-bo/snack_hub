package com.example.snackhub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.snackhub.R;
import com.example.snackhub.models.Repas;

public class RepasBackAdapter extends ListAdapter<Repas, RepasBackAdapter.RepasViewHolder> {

    public interface OnMealClickListener {
        void onMealClick(Repas repas);
        void onMealOptionsClick(Repas repas, View view);
    }

    private final OnMealClickListener listener;

    public RepasBackAdapter(OnMealClickListener listener) {
        super(new RepasDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RepasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_repas_backoffice, parent, false);
        return new RepasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepasViewHolder holder, int position) {
        Repas repas = getItem(position);
        holder.bind(repas, listener);
    }

    static class RepasViewHolder extends RecyclerView.ViewHolder {
        private final androidx.cardview.widget.CardView cardView;
        private final ImageView imageRepas;
        private final TextView nomRepas;
        private final TextView descriptionRepas;
        private final TextView prixRepas;
        private final TextView categorieRepas;
        private final TextView statutDisponibilite;
        private final ImageButton optionsButton;

        public RepasViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (androidx.cardview.widget.CardView) itemView;
            imageRepas = itemView.findViewById(R.id.repasImageItem);
            nomRepas = itemView.findViewById(R.id.repasNomItem);
            descriptionRepas = itemView.findViewById(R.id.repasDescriptionItem);
            prixRepas = itemView.findViewById(R.id.repasPrixItem);
            categorieRepas = itemView.findViewById(R.id.repasCategorieItem);
            statutDisponibilite = itemView.findViewById(R.id.repasDisponibleItem);
            optionsButton = itemView.findViewById(R.id.optionsButton);
        }

        public void bind(Repas repas, OnMealClickListener listener) {
            nomRepas.setText(repas.getNom());
            descriptionRepas.setText(repas.getDescription());
            prixRepas.setText(String.format("%.0f Dhs", repas.getPrix()));
            categorieRepas.setText(repas.getCategorie());
            statutDisponibilite.setText(repas.getStatutDisponibilite());

            // Configuration de l'image avec Glide
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.food1)
                    .error(R.drawable.food1);

            if (repas.getImageUrl() != null && !repas.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(repas.getImageUrl())
                        .apply(requestOptions)
                        .into(imageRepas);
            } else {
                int defaultImage = getDefaultImageForCategory(repas.getCategorie());
                imageRepas.setImageResource(defaultImage);
            }

            // Style selon la disponibilité
            float alpha = repas.isDisponible() ? 1.0f : 0.6f;
            cardView.setAlpha(alpha);

            // Couleur du statut disponibilité
            if (repas.isDisponible()) {
                statutDisponibilite.setBackgroundResource(R.drawable.stock_background);
                statutDisponibilite.setTextColor(itemView.getContext().getColor(android.R.color.white));
            } else {
                statutDisponibilite.setBackgroundResource(R.drawable.stock_background_unavailable);
                statutDisponibilite.setTextColor(itemView.getContext().getColor(android.R.color.white));
            }

            // Listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMealClick(repas);
                }
            });

            optionsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMealOptionsClick(repas, v);
                }
            });
        }

        private int getDefaultImageForCategory(String categorie) {
            switch (categorie.toLowerCase()) {
                case "sandwich":
                    return R.drawable.food1;
                case "plat chaud":
                    return R.drawable.food2;
                case "dessert":
                    return R.drawable.food3;
                case "boisson":
                    return R.drawable.food4;
                default:
                    return R.drawable.food5;
            }
        }
    }

    private static class RepasDiffCallback extends DiffUtil.ItemCallback<Repas> {
        @Override
        public boolean areItemsTheSame(@NonNull Repas oldItem, @NonNull Repas newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Repas oldItem, @NonNull Repas newItem) {
            return oldItem.getNom().equals(newItem.getNom()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getPrix() == newItem.getPrix() &&
                    oldItem.getCategorie().equals(newItem.getCategorie()) &&
                    oldItem.isDisponible() == newItem.isDisponible() &&
                    (oldItem.getImageUrl() != null ? oldItem.getImageUrl().equals(newItem.getImageUrl()) : newItem.getImageUrl() == null);
        }
    }
}