package com.example.snackhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class menu_snack extends AppCompatActivity {
    ImageView snackImage;
    TextView snackName, snackDescription;

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private List<Repas> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_snack);

        snackImage = findViewById(R.id.snackImageDetail);
        snackName = findViewById(R.id.snackNameDetail);
        //snackDescription = findViewById(R.id.snackDescriptionDetail);

        // Récupérer les données de l’intent :
        Intent intent = getIntent();
        String name = intent.getStringExtra("snackName");
        String phone = intent.getStringExtra("snackPhone");
        int imageRes = intent.getIntExtra("snackImage", 0);

        snackName.setText(name+": "+phone);
        //snackDescription.setText(description);
        snackImage.setImageResource(imageRes);

        recyclerView = findViewById(R.id.recyclerView);

        // Exemple : images selon le snack
        imageList = new ArrayList<>();
        imageList.add(new Repas("Repas 1", "Description du repas 1", 65, R.drawable.food1));
        imageList.add(new Repas("Repas 2", "Description du repas 2", 65, R.drawable.food2));
        imageList.add(new Repas("Repas 3", "Description du repas 3", 65, R.drawable.food3));
        imageList.add(new Repas("Repas 4", "Description du repas 4", 65, R.drawable.food4));
        imageList.add(new Repas("Repas 5", "Description du repas 5", 65, R.drawable.food5));
        imageList.add(new Repas("Repas 6", "Description du repas 6", 65, R.drawable.food6));
        imageList.add(new Repas("Repas 7", "Description du repas 7", 65, R.drawable.food7));
        imageList.add(new Repas("Repas 8", "Description du repas 8", 65, R.drawable.food8));
        // Ajoute dynamiquement selon ton snack

        adapter = new ImageAdapter(this, imageList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 colonnes
        recyclerView.setAdapter(adapter);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu_snack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}