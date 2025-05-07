package com.example.snackhub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.imageView.setImageResource(repas.getImageResource());
        holder.nom.setText(repas.getNom());
        holder.prix.setText(String.valueOf(repas.getPrix()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, single_repas.class);
                intent.putExtra("repasName", repas.getNom());
                //intent.putExtra("repasDescription", repas.getDescription());
                intent.putExtra("repasPrice", repas.getPrix());
                intent.putExtra("repasImage", repas.getImageResource());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return repasList.size();
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

