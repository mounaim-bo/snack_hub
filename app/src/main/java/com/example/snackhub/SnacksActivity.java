package com.example.snackhub;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackhub.adapters.SnackAdapter;
import com.example.snackhub.models.Snack;
import com.example.snackhub.utils.DialogUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SnacksActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SnackAdapter adapter;
    private List<Snack> snackList;
    private List<Snack> originalSnackList;
    private SearchView searchView;
    private FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        setupSearchView();
        loadSnacksFromFirebase();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.snackRecyclerView);
        searchView = findViewById(R.id.searchView);
        snackList = new ArrayList<>();
        originalSnackList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new SnackAdapter(this, snackList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void loadSnacksFromFirebase() {
        DialogUtils.showLoadingDialog(this, "Chargement des restaurants...");

        db.collection("snacks")
                .limit(100)
                .get()
                .addOnCompleteListener(task -> {
                    DialogUtils.hideLoadingDialog();

                    if (task.isSuccessful()) {
                        snackList.clear();
                        originalSnackList.clear();

                        int successCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Snack snack = document.toObject(Snack.class);
                                if (snack != null) {
                                    snack.setId(document.getId());
                                    snackList.add(snack);
                                    originalSnackList.add(snack);
                                    successCount++;
                                }
                            } catch (Exception e) {
                                // Erreur de conversion ignorée
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (snackList.isEmpty()) {
                            showEmptyState();
                        } else {
                            String message = successCount + " restaurant(s) disponible(s)";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        handleFirestoreError(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    DialogUtils.hideLoadingDialog();
                    handleFirestoreError(e);
                });
    }

    private void handleFirestoreError(Exception e) {
        if (e == null) {
            showError("Erreur inconnue lors du chargement");
            return;
        }

        String errorMessage = e.getMessage();

        if (errorMessage != null) {
            if (errorMessage.contains("PERMISSION_DENIED")) {
                showError("Règles Firestore incorrectes. Les restaurants doivent être accessibles publiquement.");
            } else if (errorMessage.contains("UNAVAILABLE")) {
                showError("Service temporairement indisponible. Vérifiez votre connexion internet.");
            } else if (errorMessage.contains("DEADLINE_EXCEEDED")) {
                showError("Délai de connexion dépassé. Vérifiez votre connexion internet.");
            } else {
                showError("Erreur de connexion. Vérifiez votre internet et réessayez.");
            }
        } else {
            showError("Impossible de charger les restaurants. Vérifiez votre connexion.");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showEmptyState() {
        Toast.makeText(this, "Aucun restaurant disponible pour le moment. Revenez plus tard !",
                Toast.LENGTH_LONG).show();
    }

    private void filter(String text) {
        List<Snack> filteredList = new ArrayList<>();

        if (text.isEmpty()) {
            filteredList.addAll(originalSnackList);
        } else {
            String searchText = text.toLowerCase().trim();
            for (Snack snack : originalSnackList) {
                if ((snack.getName() != null && snack.getName().toLowerCase().contains(searchText)) ||
                        (snack.getDescription() != null && snack.getDescription().toLowerCase().contains(searchText)) ||
                        (snack.getAddress() != null && snack.getAddress().toLowerCase().contains(searchText))) {
                    filteredList.add(snack);
                }
            }
        }

        snackList.clear();
        snackList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSnacksFromFirebase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogUtils.hideLoadingDialog();
    }

    public void refreshData() {
        loadSnacksFromFirebase();
    }
}