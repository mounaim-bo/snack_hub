package com.example.snackhub.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackhub.R;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandesAdapter extends RecyclerView.Adapter<CommandesAdapter.CommandeViewHolder> {

    private List<Map<String, Object>> commandesList;
    private OnCommandeActionListener listener;

    public interface OnCommandeActionListener {
        void onChangerStatut(Map<String, Object> commande, int position);
        void onVoirDetails(Map<String, Object> commande);
    }

    public CommandesAdapter(List<Map<String, Object>> commandesList, OnCommandeActionListener listener) {
        this.commandesList = commandesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commande, parent, false);
        return new CommandeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandeViewHolder holder, int position) {
        Map<String, Object> commande = commandesList.get(position);
        holder.bind(commande, position);
    }

    @Override
    public int getItemCount() {
        return commandesList.size();
    }

    class CommandeViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroCommande, tvClientNom, tvClientTelephone, tvModeCommande;
        TextView tvDateCommande, tvPrixTotal, tvNotes;
        Chip chipStatut;
        Button btnChangerStatut;
        ImageView ivModeCommande;
        RecyclerView recyclerViewArticles;

        public CommandeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroCommande = itemView.findViewById(R.id.tvNumeroCommande);
            tvClientNom = itemView.findViewById(R.id.tvClientNom);
            tvClientTelephone = itemView.findViewById(R.id.tvClientTelephone);
            tvModeCommande = itemView.findViewById(R.id.tvModeCommande);
            tvDateCommande = itemView.findViewById(R.id.tvDateCommande);
            tvPrixTotal = itemView.findViewById(R.id.tvPrixTotal);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            chipStatut = itemView.findViewById(R.id.chipStatut);
            btnChangerStatut = itemView.findViewById(R.id.btnChangerStatut);
            ivModeCommande = itemView.findViewById(R.id.ivModeCommande);
            recyclerViewArticles = itemView.findViewById(R.id.recyclerViewArticles);
        }

        public void bind(Map<String, Object> commande, int position) {
            // Numéro de commande (raccourci)
            String commandeId = (String) commande.get("id");
            if (commandeId != null && commandeId.length() > 8) {
                tvNumeroCommande.setText("Commande #" + commandeId.substring(0, 8));
            } else {
                tvNumeroCommande.setText("Commande #" + commandeId);
            }

            // Informations client
            tvClientNom.setText((String) commande.get("clientNom"));
            tvClientTelephone.setText((String) commande.get("clientTelephone"));

            // Mode de commande
            String modeCommande = (String) commande.get("modeCommande");
            tvModeCommande.setText(modeCommande);

            // Icône selon le mode
            switch (modeCommande) {
                case "Livraison via livreur":
                    ivModeCommande.setImageResource(R.drawable.ic_delivery);
                    break;
                case "Récupérer au snack":
                    ivModeCommande.setImageResource(R.drawable.ic_store);
                    break;
                case "Prendre à table":
                    ivModeCommande.setImageResource(R.drawable.ic_table);
                    break;
                default:
                    ivModeCommande.setImageResource(R.drawable.ic_delivery);
            }

            // Date de commande
            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) commande.get("dateCommande");
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat format = new SimpleDateFormat("dd/MM à HH:mm", Locale.getDefault());
                tvDateCommande.setText(format.format(date));
            }

            // Prix total
            Double prixTotal = (Double) commande.get("prixTotal");
            if (prixTotal != null) {
                tvPrixTotal.setText("Total: " + String.format(Locale.getDefault(), "%.0f DH", prixTotal));
            }

            // Statut avec couleurs
            String statut = (String) commande.get("statut");
            chipStatut.setText(statut);
            setStatutStyle(chipStatut, statut);

            // Bouton changer statut
            String texteBouton = getTexteBoutonStatut(statut);
            btnChangerStatut.setText(texteBouton);

            if (statut.equals("Livrée")) {
                btnChangerStatut.setEnabled(false);
                btnChangerStatut.setAlpha(0.5f);
            } else {
                btnChangerStatut.setEnabled(true);
                btnChangerStatut.setAlpha(1.0f);
                btnChangerStatut.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onChangerStatut(commande, position);
                    }
                });
            }

            // Articles
            setupArticlesRecyclerView(commande);

            // Notes
            String notes = (String) commande.get("notes");
            if (notes != null && !notes.trim().isEmpty()) {
                tvNotes.setText("Notes: " + notes);
                tvNotes.setVisibility(View.VISIBLE);
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            // Clic sur l'item pour voir les détails
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoirDetails(commande);
                }
            });
        }

        private void setStatutStyle(Chip chip, String statut) {
            switch (statut) {
                case "En attente":
                    chip.setChipBackgroundColorResource(R.color.orange_light);
                    chip.setTextColor(Color.WHITE);
                    break;
                case "En préparation":
                    chip.setChipBackgroundColorResource(R.color.blue_light);
                    chip.setTextColor(Color.WHITE);
                    break;
                case "Prête":
                    chip.setChipBackgroundColorResource(R.color.green_light);
                    chip.setTextColor(Color.WHITE);
                    break;
                case "Livrée":
                    chip.setChipBackgroundColorResource(R.color.gray_dark);
                    chip.setTextColor(Color.WHITE);
                    break;
                default:
                    chip.setChipBackgroundColorResource(R.color.gray_dark);
                    chip.setTextColor(Color.WHITE);
            }
        }

        private String getTexteBoutonStatut(String statut) {
            switch (statut) {
                case "En attente":
                    return "Préparer";
                case "En préparation":
                    return "Prête";
                case "Prête":
                    return "Livrer";
                case "Livrée":
                    return "Terminé";
                default:
                    return "Traiter";
            }
        }

        private void setupArticlesRecyclerView(Map<String, Object> commande) {
            List<Map<String, Object>> articles = (List<Map<String, Object>>) commande.get("articles");

            if (articles != null && !articles.isEmpty()) {
                ArticlesAdapter articlesAdapter = new ArticlesAdapter(articles);
                recyclerViewArticles.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                recyclerViewArticles.setAdapter(articlesAdapter);
                recyclerViewArticles.setVisibility(View.VISIBLE);
            } else {
                recyclerViewArticles.setVisibility(View.GONE);
            }
        }
    }

    // Adapter pour les articles dans chaque commande
    static class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder> {
        private List<Map<String, Object>> articles;

        ArticlesAdapter(List<Map<String, Object>> articles) {
            this.articles = articles;
        }

        @NonNull
        @Override
        public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article_commande, parent, false);
            return new ArticleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
            Map<String, Object> article = articles.get(position);
            holder.bind(article);
        }

        @Override
        public int getItemCount() {
            return articles.size();
        }

        static class ArticleViewHolder extends RecyclerView.ViewHolder {
            TextView tvNomArticle, tvQuantiteArticle, tvPrixArticle, tvDescriptionArticle;

            ArticleViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNomArticle = itemView.findViewById(R.id.tvNomArticle);
                tvQuantiteArticle = itemView.findViewById(R.id.tvQuantiteArticle);
                tvPrixArticle = itemView.findViewById(R.id.tvPrixArticle);
                tvDescriptionArticle = itemView.findViewById(R.id.tvDescriptionArticle);
            }

            void bind(Map<String, Object> article) {
                String nomRepas = (String) article.get("nomRepas");
                Long quantite = (Long) article.get("quantite");
                Double prixTotal = (Double) article.get("prixTotal");
                String description = (String) article.get("description");

                tvNomArticle.setText(nomRepas);
                tvQuantiteArticle.setText("x" + quantite);
                tvPrixArticle.setText(String.format(Locale.getDefault(), "%.0f DH", prixTotal));

                if (description != null && !description.trim().isEmpty() && !description.equals("Aucune description")) {
                    tvDescriptionArticle.setText(description);
                    tvDescriptionArticle.setVisibility(View.VISIBLE);
                } else {
                    tvDescriptionArticle.setVisibility(View.GONE);
                }
            }
        }
    }
}