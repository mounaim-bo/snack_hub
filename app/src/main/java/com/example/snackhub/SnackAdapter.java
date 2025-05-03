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
        holder.snackName.setText(snack.getName());
        holder.snackPhone.setText(snack.getPhone());
        holder.snackDescription.setText(snack.getDescription());
        holder.snackImage.setImageResource(snack.getImageResource());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, menu_snack.class);
                intent.putExtra("snackName", snack.getName());
                intent.putExtra("snackPhone", snack.getPhone());
                intent.putExtra("snackDescription", snack.getDescription());
                intent.putExtra("snackImage", snack.getImageResource());
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
        TextView snackName, snackDescription, snackPhone;

        public SnackViewHolder(@NonNull View itemView) {
            super(itemView);
            snackImage = itemView.findViewById(R.id.snackImage);
            snackName = itemView.findViewById(R.id.snackName);
            snackPhone = itemView.findViewById(R.id.snackPhone);
            snackDescription = itemView.findViewById(R.id.snackDescription);
        }
    }
}
