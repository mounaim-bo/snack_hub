package com.example.snackhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommandeAdapter extends RecyclerView.Adapter<CommandeAdapter.CommandeViewHolder> {

    private List<Commande> commandeList;

    public CommandeAdapter(List<Commande> commandeList) {
        this.commandeList = commandeList;
    }

    @NonNull
    @Override
    public CommandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panier, parent, false);
        return new CommandeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandeViewHolder holder, int position) {
        Commande commande = commandeList.get(position);
        holder.imageRepas.setImageResource(commande.getImageResId());
        holder.nomRepas.setText(commande.getNomRepas());
        holder.descriptionRepas.setText(commande.getDescription());
        holder.quantiteRepas.setText("Quantité : " + commande.getQuantite());
        holder.prixRepas.setText("Prix : " + commande.getPrixTotal() + " DH");
    }

    @Override
    public int getItemCount() {
        return commandeList.size();
    }

    public static class CommandeViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRepas;
        TextView nomRepas, descriptionRepas, quantiteRepas, prixRepas;

        public CommandeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRepas = itemView.findViewById(R.id.imageRepas);
            nomRepas = itemView.findViewById(R.id.nomRepas);
            descriptionRepas = itemView.findViewById(R.id.descriptionRepas);
            quantiteRepas = itemView.findViewById(R.id.quantiteRepas);
            prixRepas = itemView.findViewById(R.id.prixRepas);
        }
    }
}

