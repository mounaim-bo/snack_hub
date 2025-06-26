package com.example.snackhub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackhub.R;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ArticlesDetailsAdapter extends RecyclerView.Adapter<ArticlesDetailsAdapter.ArticleDetailsViewHolder> {

    private List<Map<String, Object>> articles;

    public ArticlesDetailsAdapter(List<Map<String, Object>> articles) {
        this.articles = articles;
    }

    @NonNull
    @Override
    public ArticleDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article_details, parent, false);
        return new ArticleDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleDetailsViewHolder holder, int position) {
        Map<String, Object> article = articles.get(position);
        holder.bind(article, position + 1);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ArticleDetailsViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroArticle, tvNomArticle, tvQuantiteArticle, tvPrixUnitaire, tvPrixTotal, tvDescriptionArticle;

        ArticleDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroArticle = itemView.findViewById(R.id.tvNumeroArticle);
            tvNomArticle = itemView.findViewById(R.id.tvNomArticle);
            tvQuantiteArticle = itemView.findViewById(R.id.tvQuantiteArticle);
            tvPrixUnitaire = itemView.findViewById(R.id.tvPrixUnitaire);
            tvPrixTotal = itemView.findViewById(R.id.tvPrixTotal);
            tvDescriptionArticle = itemView.findViewById(R.id.tvDescriptionArticle);
        }

        void bind(Map<String, Object> article, int numero) {
            // Numéro de l'article
            tvNumeroArticle.setText(String.valueOf(numero));

            // Nom du repas
            String nomRepas = (String) article.get("nomRepas");
            tvNomArticle.setText(nomRepas);

            // Quantité
            Long quantite = (Long) article.get("quantite");
            tvQuantiteArticle.setText("Quantité: " + quantite);

            // Prix unitaire
            Double prixUnitaire = (Double) article.get("prixUnitaire");
            if (prixUnitaire != null) {
                tvPrixUnitaire.setText("Prix unitaire: " + String.format(Locale.getDefault(), "%.0f DH", prixUnitaire));
            }

            // Prix total
            Double prixTotal = (Double) article.get("prixTotal");
            if (prixTotal != null) {
                tvPrixTotal.setText(String.format(Locale.getDefault(), "%.0f DH", prixTotal));
            }

            // Description
            String description = (String) article.get("description");
            if (description != null && !description.trim().isEmpty() && !description.equals("Aucune description")) {
                tvDescriptionArticle.setText(description);
                tvDescriptionArticle.setVisibility(View.VISIBLE);
            } else {
                tvDescriptionArticle.setVisibility(View.GONE);
            }
        }
    }
}