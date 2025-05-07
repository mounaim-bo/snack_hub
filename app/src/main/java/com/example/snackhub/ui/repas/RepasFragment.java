package com.example.snackhub.ui.repas;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.snackhub.Repas;
import com.example.snackhub.RepasBackAdapter;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RepasFragment extends Fragment implements RepasBackAdapter.OnMealClickListener{

    private RecyclerView recyclerView;
    private RepasBackAdapter adapter;
    private TextView emptyStateText;
    private TextView totalMealsText;
    private EditText searchEditText;
    private Spinner filterSpinner;
    private List<Repas> allMeals = new ArrayList<>();
    private List<Repas> filteredMeals = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_repas, container, false);

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
            public void onNothingSelected(AdapterView<?> parent) {
                // Ne rien faire
            }
        });

        // Configuration de la recherche
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Ne rien faire
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Ne rien faire
            }

            @Override
            public void afterTextChanged(Editable s) {
                String category = filterSpinner.getSelectedItemPosition() == 0 ?
                        "" : filterSpinner.getSelectedItem().toString();
                filterMeals(s.toString(), category);
            }
        });

        // Configuration du bouton d'ajout
        ajoutRepasButton.setOnClickListener(v -> {
            // Navigation vers le fragment d'ajout de repas
            Navigation.findNavController(v).navigate(R.id.ajoutRepasFragment);
        });

        // Chargement des données
        loadMeals();

        return root;
    }

    // Méthode pour charger les repas (remplacer par votre propre implémentation)
    private void loadMeals() {
        // Pour l'exemple, on crée des données fictives
        allMeals = createSampleMeals();
        updateUI(allMeals);
    }

    // Méthode pour filtrer les repas
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

    // Méthode pour mettre à jour l'interface
    private void updateUI(List<Repas> repas) {
        adapter.submitList(repas);

        if (repas.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }

        totalMealsText.setText("Total: " + repas.size() + " repas");
    }

    // Données d'exemple pour tester
    private List<Repas> createSampleMeals() {
        List<Repas> repas = new ArrayList<>();

        repas.add(new Repas("1", "Sandwich Poulet", "Poulet grillé, salade, tomate, sauce maison dans un pain frais",
                65, R.drawable.food5, "Sandwich", true));

        repas.add(new Repas("2", "Croque Monsieur", "Jambon, fromage, béchamel, pain de mie toasté",
                50, R.drawable.food4, "Sandwich", true));

        repas.add(new Repas("3", "Salade César", "Salade, poulet, croûtons, parmesan, sauce césar",
                75, R.drawable.food3, "Plat chaud", true));

        repas.add(new Repas("4", "Tiramisu", "Biscuits, café, mascarpone, cacao",
                40, R.drawable.food2, "Dessert", false));

        repas.add(new Repas("5", "Coca-Cola", "Canette 33cl",
                20, R.drawable.food1, "Boisson", true));

        return repas;
    }

    @Override
    public void onMealClick(Repas repas) {
        // Navigation vers les détails du repas
        Toast.makeText(getContext(), "Repas sélectionné: " + repas.getNom(), Toast.LENGTH_SHORT).show();
        // Navigation.findNavController(getView()).navigate(
        //      RepasAdminFragmentDirections.actionRepasAdminFragmentToMealDetailFragment(meal.getId()));
    }

    @Override
    public void onMealOptionsClick(Repas repas, View view) {
        // Affichage d'un menu popup pour les options
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.repas_options_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_edit) {
                ImageView imageView = view.findViewById(R.id.repasImageItem);
                TextView repasNomItem = view.findViewById(R.id.repasNomItem);
                TextView repasDescriptionItem = view.findViewById(R.id.repasDescriptionItem);
                TextView repasCategorieItem = view.findViewById(R.id.repasCategorieItem);
                TextView repasPrixItem = view.findViewById(R.id.repasPrixItem);
                TextView repasDisponibleItem = view.findViewById(R.id.repasDisponibleItem);

                Bundle bundle = new Bundle();
                bundle.putString("repasId", repas.getId());
                bundle.putString("repasNom", repas.getNom());
                bundle.putString("repasDescription", repas.getDescription());
                bundle.putString("repasCategorie", repas.getCategorie());
                bundle.putString("repasPrix", repas.getPrix()+" Dhs");
                bundle.putInt("repasImage", repas.getImageResource());
                bundle.putBoolean("repasDisponible", repas.isAvailable());

                // Utilise la Navigation Component pour naviguer
                Navigation.findNavController(view).navigate(R.id.modifierRepasFragment, bundle);

                return true;
            } else if (itemId == R.id.action_delete) {
                // Action pour supprimer
                Toast.makeText(getContext(), "Supprimer: " + repas.getNom(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_toggle_availability) {
                // Action pour changer la disponibilité
                repas.setAvailable(!repas.isAvailable());
                adapter.notifyItemChanged(allMeals.indexOf(repas));
                String status = repas.isAvailable() ? "disponible" : "indisponible";
                Toast.makeText(getContext(), repas.getNom() + " est maintenant " + status, Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });

        popup.show();
    }
}