package com.example.snackhub;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snackhub.R;

import java.text.NumberFormat;
import java.util.Locale;
public class RepasBackAdapter extends ListAdapter<Repas, RepasBackAdapter.MealViewHolder> {

    private final OnMealClickListener listener;
    private final NumberFormat currencyFormatter;

    public interface OnMealClickListener {
        void onMealClick(Repas meal);
        void onMealOptionsClick(Repas meal, View view);
    }

    public RepasBackAdapter(OnMealClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    }

    private static final DiffUtil.ItemCallback<Repas> DIFF_CALLBACK = new DiffUtil.ItemCallback<Repas>() {
        @Override
        public boolean areItemsTheSame(@NonNull Repas oldItem, @NonNull Repas newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Repas oldItem, @NonNull Repas newItem) {
            return oldItem.getNom().equals(newItem.getNom()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getPrix() == newItem.getPrix() &&
                    oldItem.getCategorie().equals(newItem.getCategorie()) &&
                    oldItem.getImageResource() == (newItem.getImageResource());
        }
    };

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_repas_backoffice, parent, false);
        return new MealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Repas repas = getItem(position);
        holder.bind(repas, listener);
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private final ImageView repasImageItem;
        private final TextView repasNomItem;
        private final TextView repasDescriptionItem;
        private final TextView repasCategorieItem;
        private final TextView repasDisponibleItem;
        private final TextView repasPrixItem;
        private final ImageButton optionsButton;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            repasImageItem = itemView.findViewById(R.id.repasImageItem);
            repasNomItem = itemView.findViewById(R.id.repasNomItem);
            repasDescriptionItem = itemView.findViewById(R.id.repasDescriptionItem);
            repasCategorieItem = itemView.findViewById(R.id.repasCategorieItem);
            repasDisponibleItem = itemView.findViewById(R.id.repasDisponibleItem);
            repasPrixItem = itemView.findViewById(R.id.repasPrixItem);
            optionsButton = itemView.findViewById(R.id.optionsButton);
        }

        public void bind(final Repas repas, final OnMealClickListener listener) {
            repasNomItem.setText(repas.getNom());
            repasDescriptionItem.setText(repas.getDescription());
            repasCategorieItem.setText(repas.getCategorie());
            repasDisponibleItem.setText(repas.isAvailable() ? "Disponible" : "Indisponible");
            repasPrixItem.setText(repas.getPrix()+" Dhs");

            // Chargement de l'image avec Glide
            if (repas.getImageResource() != 0 && !(repas.getImageResource() == 0)) {
                Glide.with(itemView.getContext())
                        .load(repas.getImageResource())
                        .placeholder(R.drawable.food1)
                        .error(R.drawable.food1)
                        .centerCrop()
                        .into(repasImageItem);
            } else {
                repasImageItem.setImageResource(R.drawable.food1);
            }

            // Mise en place des listeners
            itemView.setOnClickListener(v -> {
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
    }
}