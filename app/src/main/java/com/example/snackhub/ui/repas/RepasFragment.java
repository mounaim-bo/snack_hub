package com.example.snackhub.ui.repas;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackhub.R;
import com.example.snackhub.adapters.RepasBackAdapter;
import com.example.snackhub.models.Repas;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RepasFragment extends Fragment implements RepasBackAdapter.OnMealClickListener {

    private static final String TAG = "RepasFragment";

    private RecyclerView recyclerView;
    private RepasBackAdapter adapter;
    private TextView emptyStateText;
    private TextView totalMealsText;
    private EditText searchEditText;
    private Spinner filterSpinner;
    private List<Repas> allMeals = new ArrayList<>();
    private List<Repas> filteredMeals = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentSnackId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_repas, container, false);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialisation des vues
        recyclerView = root.findViewById(R.id.recyclerViewMeals);
        emptyStateText = root.findViewById(R.id.emptyStateText);
        totalMealsText = root.findViewById(R.id.totalMealsText);
        searchEditText = root.findViewById(R.id.searchEditText);
        filterSpinner = root.findViewById(R.id.filterSpinner);
        ExtendedFloatingActionButton ajoutRepasButton = root.findViewById(R.id.addMealButton);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RepasBackAdapter(this);
        recyclerView.setAdapter(adapter);

        // Configuration du spinner de filtrage
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterMeals(searchEditText.getText().toString(),
                        position == 0 ? "" : parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Configuration de la recherche
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String category = filterSpinner.getSelectedItemPosition() == 0 ?
                        "" : filterSpinner.getSelectedItem().toString();
                filterMeals(s.toString(), category);
            }
        });

        // Configuration du bouton d'ajout
        ajoutRepasButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.ajoutRepasFragment);
        });

        // Chargement des données Firebase UNIQUEMENT
        loadCurrentSnackId();

        return root;
    }

    private void loadCurrentSnackId() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Utilisateur non connecté");
            showEmptyState("Utilisateur non connecté");
            return;
        }

        Log.d(TAG, "Chargement des données pour l'utilisateur: " + auth.getCurrentUser().getUid());

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentSnackId = documentSnapshot.getString("snackId");
                        Log.d(TAG, "SnackId trouvé: " + currentSnackId);

                        if (currentSnackId != null && !currentSnackId.isEmpty()) {
                            // ✅ CHARGEMENT FIREBASE UNIQUEMENT
                            loadMealsFromFirestore();
                        } else {
                            Log.w(TAG, "Aucun snackId associé à l'utilisateur");
                            showEmptyState("Aucun restaurant associé à votre compte.\nContactez l'administrateur.");
                        }
                    } else {
                        Log.e(TAG, "Document utilisateur n'existe pas");
                        showEmptyState("Données utilisateur introuvables.\nVeuillez vous reconnecter.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors du chargement des données utilisateur", e);
                    showEmptyState("Erreur de connexion.\nVérifiez votre réseau.");
                });
    }

    private void loadMealsFromFirestore() {
        if (currentSnackId == null || currentSnackId.isEmpty()) {
            Log.e(TAG, "SnackId null ou vide");
            showEmptyState("ID du restaurant non disponible");
            return;
        }

        Log.d(TAG, "Chargement des repas pour snackId: " + currentSnackId);

        // ✅ LISTENER EN TEMPS RÉEL POUR FIREBASE
        db.collection("repas")
                .whereEqualTo("snackId", currentSnackId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Erreur lors du chargement des repas Firebase", error);
                        showEmptyState("Erreur lors du chargement des repas.\n" + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        allMeals.clear();

                        Log.d(TAG, "Nombre de documents trouvés: " + value.size());

                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Repas repas = doc.toObject(Repas.class);
                                repas.setId(doc.getId());
                                allMeals.add(repas);

                                Log.d(TAG, "Repas chargé: " + repas.getNom() + " (ID: " + doc.getId() + ")");
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors de la conversion du document: " + doc.getId(), e);
                            }
                        }

                        // ✅ MISE À JOUR DE L'INTERFACE
                        updateUI(allMeals);
                        Log.d(TAG, "Total repas Firebase chargés: " + allMeals.size());

                        if (allMeals.isEmpty()) {
                            showEmptyState("Aucun repas dans votre restaurant.\nCommencez par ajouter votre premier repas !");
                        }
                    } else {
                        Log.w(TAG, "Snapshot value est null");
                        showEmptyState("Aucune donnée trouvée");
                    }
                });
    }

    private void showEmptyState(String message) {
        allMeals.clear();
        recyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
        totalMealsText.setText("Total: 0 repas");
        adapter.submitList(new ArrayList<>());
    }

    private void filterMeals(String query, String category) {
        filteredMeals.clear();

        for (Repas meal : allMeals) {
            boolean matchesQuery = query.isEmpty() ||
                    meal.getNom().toLowerCase().contains(query.toLowerCase()) ||
                    meal.getDescription().toLowerCase().contains(query.toLowerCase());

            boolean matchesCategory = category.isEmpty() || meal.getCategorie().equals(category);

            if (matchesQuery && matchesCategory) {
                filteredMeals.add(meal);
            }
        }

        updateUI(filteredMeals);
    }

    private void updateUI(List<Repas> repas) {
        adapter.submitList(new ArrayList<>(repas));

        if (repas.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            if (allMeals.isEmpty()) {
                emptyStateText.setText("Aucun repas dans votre restaurant.\nCommencez par ajouter votre premier repas !");
            } else {
                emptyStateText.setText("Aucun repas ne correspond à votre recherche.");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }

        totalMealsText.setText("Total: " + repas.size() + " repas");
    }

    @Override
    public void onMealClick(Repas repas) {
        Toast.makeText(getContext(), "Repas sélectionné: " + repas.getNom(), Toast.LENGTH_SHORT).show();

        // Navigation vers modification
        Bundle bundle = new Bundle();
        bundle.putString("repasId", repas.getId());
        bundle.putString("repasNom", repas.getNom());
        bundle.putString("repasDescription", repas.getDescription());
        bundle.putString("repasCategorie", repas.getCategorie());
        bundle.putString("repasPrix", String.valueOf(repas.getPrix()));
        bundle.putString("repasImageUrl", repas.getImageUrl());
        bundle.putBoolean("repasDisponible", repas.isDisponible());

        try {
            Navigation.findNavController(requireView()).navigate(R.id.modifierRepasFragment, bundle);
        } catch (Exception e) {
            Log.e(TAG, "Navigation vers modification impossible", e);
            Toast.makeText(getContext(), "Navigation impossible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMealOptionsClick(Repas repas, View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.repas_options_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_edit) {
                onMealClick(repas);
                return true;

            } else if (itemId == R.id.action_delete) {
                supprimerRepas(repas);
                return true;

            } else if (itemId == R.id.action_toggle_availability) {
                toggleDisponibilite(repas);
                return true;
            }

            return false;
        });

        popup.show();
    }

    private void supprimerRepas(Repas repas) {
        if (repas.getId() == null || repas.getId().isEmpty()) {
            Toast.makeText(getContext(), "Impossible de supprimer ce repas", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Suppression du repas: " + repas.getId());

        db.collection("repas")
                .document(repas.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Repas supprimé avec succès: " + repas.getNom());
                    Toast.makeText(getContext(), "Repas \"" + repas.getNom() + "\" supprimé", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la suppression du repas: " + repas.getId(), e);
                    Toast.makeText(getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleDisponibilite(Repas repas) {
        if (repas.getId() == null || repas.getId().isEmpty()) {
            Toast.makeText(getContext(), "Impossible de modifier ce repas", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean nouvelleDisponibilite = !repas.isDisponible();

        Log.d(TAG, "Modification disponibilité: " + repas.getId() + " -> " + nouvelleDisponibilite);

        db.collection("repas")
                .document(repas.getId())
                .update("disponible", nouvelleDisponibilite)
                .addOnSuccessListener(aVoid -> {
                    String status = nouvelleDisponibilite ? "disponible" : "indisponible";
                    Log.d(TAG, "Disponibilité mise à jour: " + repas.getNom() + " -> " + status);
                    Toast.makeText(getContext(),
                            repas.getNom() + " est maintenant " + status,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la modification de disponibilité: " + repas.getId(), e);
                    Toast.makeText(getContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les données quand on revient sur le fragment
        if (currentSnackId != null && !currentSnackId.isEmpty()) {
            Log.d(TAG, "Fragment resumed - rechargement des données");
        }
    }
}