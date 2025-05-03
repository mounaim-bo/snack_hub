package com.example.snackhub;

import android.os.Bundle;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class snacks extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SnackAdapter adapter;
    private List<Snack> snackList;
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_snacks);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.snacks), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.snackRecyclerView);
        searchView = findViewById(R.id.searchView);
        snackList = new ArrayList<>();

        // Exemple de données
        snackList.add(new Snack("Snack Test 1", "06 06 06 06 06", "Lorem ipsum dolor sit amet.", R.drawable.images3));
        snackList.add(new Snack("Snack Test 2", "06 06 06 06 06", "Lorem ipsum dolor sit amet.", R.drawable.image4));
        snackList.add(new Snack("Snack Test 3", "06 06 06 06 06", "Lorem ipsum dolor sit amet.", R.drawable.images5));
        snackList.add(new Snack("Snack Test 4", "06 06 06 06 06", "Lorem ipsum dolor sit amet.", R.drawable.image6));
        snackList.add(new Snack("Snack Test 5", "06 06 06 06 06", "Lorem ipsum dolor sit amet.", R.drawable.image7));
        snackList.add(new Snack("Snack Test 6", "06 06 06 06 06", "Lorem ipsum dolor sit amet.", R.drawable.images1));
        snackList.add(new Snack("Snack Test 7", "06 06 06 06 06", "Lorem ipsum dolor sit amet.", R.drawable.image2));


        adapter = new SnackAdapter(this, snackList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Filtrage de la recherche
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        List<Snack> filteredList = new ArrayList<>();
        for (Snack snack : snackList) {
            if (snack.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(snack);
            }
        }
        adapter = new SnackAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);
    }
}