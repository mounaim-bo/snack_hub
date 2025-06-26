package com.example.snackhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.snackhub.R;
import com.example.snackhub.adapters.CommandesAdapter;
import com.example.snackhub.utils.CommandeFirebaseUtils;
import com.example.snackhub.utils.UserManager;
import com.google.android.material.chip.Chip;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandesFragment extends Fragment {

    private static final int REQUEST_CODE_DETAILS = 1001;

    private RecyclerView recyclerViewCommandes;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNombreCommandes;
    private LinearLayout layoutEmpty;
    private ImageButton btnRefresh;

    // Filtres
    private Chip chipTous, chipEnAttente, chipEnPreparation, chipPrete, chipLivree;

    private CommandesAdapter adapter;
    private List<Map<String, Object>> commandesList;
    private List<Map<String, Object>> commandesFiltered;
    private String currentFilter = "Toutes";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_commandes, container, false);

        initViews(root);
        setupRecyclerView();
        setupFilters();
        setupRefresh();

        // Charger les commandes
        loadCommandes();

        return root;
    }

    private void initViews(View root) {
        recyclerViewCommandes = root.findViewById(R.id.recyclerViewCommandes);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        tvNombreCommandes = root.findViewById(R.id.tvNombreCommandes);
        layoutEmpty = root.findViewById(R.id.layoutEmpty);
        btnRefresh = root.findViewById(R.id.btnRefresh);

        // Chips de filtres
        chipTous = root.findViewById(R.id.chipTous);
        chipEnAttente = root.findViewById(R.id.chipEnAttente);
        chipEnPreparation = root.findViewById(R.id.chipEnPreparation);
        chipPrete = root.findViewById(R.id.chipPrete);
        chipLivree = root.findViewById(R.id.chipLivree);
    }

    private void setupRecyclerView() {
        commandesList = new ArrayList<>();
        commandesFiltered = new ArrayList<>();

        adapter = new CommandesAdapter(commandesFiltered, new CommandesAdapter.OnCommandeActionListener() {
            @Override
            public void onChangerStatut(Map<String, Object> commande, int position) {
                changerStatutCommande(commande, position);
            }

            @Override
            public void onVoirDetails(Map<String, Object> commande) {
                // Implémentation de la fonction onVoirDetails
                ouvrirDetailsCommande(commande);
            }
        });

        recyclerViewCommandes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCommandes.setAdapter(adapter);
    }

    private void setupFilters() {
        chipTous.setOnClickListener(v -> applyFilter("Toutes"));
        chipEnAttente.setOnClickListener(v -> applyFilter("En attente"));
        chipEnPreparation.setOnClickListener(v -> applyFilter("En préparation"));
        chipPrete.setOnClickListener(v -> applyFilter("Prête"));
        chipLivree.setOnClickListener(v -> applyFilter("Livrée"));
    }

    private void setupRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadCommandes);
        btnRefresh.setOnClickListener(v -> loadCommandes());
    }

    private void loadCommandes() {
        swipeRefreshLayout.setRefreshing(true);

        // TEST TEMPORAIRE - utilisez directement l'ID visible dans Firebase
        String snackId = "mSwHNEUQW1G0dEqSSuOS";

        Log.d("CommandesFragment", "SnackId utilisé (forcé): " + snackId);

        CommandeFirebaseUtils.getCommandesSnack(snackId, new CommandeFirebaseUtils.CommandeSnackCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> commandes) {
                Log.d("CommandesFragment", "Nombre de commandes reçues: " + commandes.size());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        commandesList.clear();
                        commandesList.addAll(commandes);

                        updateNombreCommandes();
                        applyFilter(currentFilter);

                        Log.d("CommandesFragment", "Commandes affichées: " + commandesList.size());
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("CommandesFragment", "Erreur lors du chargement: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        showEmptyState();
                    });
                }
            }
        });
    }

    private void applyFilter(String filter) {
        currentFilter = filter;

        // Réinitialiser tous les chips
        chipTous.setChecked(false);
        chipEnAttente.setChecked(false);
        chipEnPreparation.setChecked(false);
        chipPrete.setChecked(false);
        chipLivree.setChecked(false);

        // Activer le chip sélectionné
        switch (filter) {
            case "Toutes":
                chipTous.setChecked(true);
                break;
            case "En attente":
                chipEnAttente.setChecked(true);
                break;
            case "En préparation":
                chipEnPreparation.setChecked(true);
                break;
            case "Prête":
                chipPrete.setChecked(true);
                break;
            case "Livrée":
                chipLivree.setChecked(true);
                break;
        }

        // Filtrer les commandes
        commandesFiltered.clear();

        if (filter.equals("Toutes")) {
            commandesFiltered.addAll(commandesList);
        } else {
            for (Map<String, Object> commande : commandesList) {
                String statut = (String) commande.get("statut");
                if (filter.equals(statut)) {
                    commandesFiltered.add(commande);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Afficher/masquer l'état vide
        if (commandesFiltered.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void changerStatutCommande(Map<String, Object> commande, int position) {
        String commandeId = (String) commande.get("id");
        String statutActuel = (String) commande.get("statut");

        // Vérification de sécurité
        if (commandeId == null || commandeId.trim().isEmpty()) {
            Toast.makeText(getContext(), "Erreur: ID de commande manquant", Toast.LENGTH_SHORT).show();
            Log.e("CommandesFragment", "CommandeId est null pour la commande: " + commande.toString());
            return;
        }

        if (statutActuel == null) {
            Toast.makeText(getContext(), "Erreur: Statut actuel manquant", Toast.LENGTH_SHORT).show();
            return;
        }

        String nouveauStatut = getNextStatut(statutActuel);

        Log.d("CommandesFragment", "Changement de statut - ID: " + commandeId + ", Ancien: " + statutActuel + ", Nouveau: " + nouveauStatut);

        CommandeFirebaseUtils.updateStatutCommande(commandeId, nouveauStatut,
                new CommandeFirebaseUtils.CommandeCallback() {
                    @Override
                    public void onSuccess(String commandeId) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // Mettre à jour localement
                                commande.put("statut", nouveauStatut);
                                adapter.notifyItemChanged(position);

                                Toast.makeText(getContext(),
                                        "Statut mis à jour: " + nouveauStatut,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(),
                                        "Erreur: " + error,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
    }

    // Nouvelle méthode pour ouvrir les détails de la commande
    private void ouvrirDetailsCommande(Map<String, Object> commande) {
        try {
            Intent intent = new Intent(getContext(), CommandeDetailsActivity.class);
            intent.putExtra("commande", (Serializable) commande);
            startActivityForResult(intent, REQUEST_CODE_DETAILS);
        } catch (Exception e) {
            Log.e("CommandesFragment", "Erreur lors de l'ouverture des détails", e);
            Toast.makeText(getContext(), "Erreur lors de l'ouverture des détails", Toast.LENGTH_SHORT).show();
        }
    }

    // Gérer le retour de l'activité des détails
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DETAILS && resultCode == getActivity().RESULT_OK && data != null) {
            boolean commandeUpdated = data.getBooleanExtra("commande_updated", false);

            if (commandeUpdated) {
                String nouveauStatut = data.getStringExtra("nouveau_statut");
                String commandeId = data.getStringExtra("commande_id");

                // Mettre à jour la commande dans la liste locale
                updateCommandeInList(commandeId, nouveauStatut);

                // Rafraîchir l'affichage
                applyFilter(currentFilter);
                updateNombreCommandes();

                Toast.makeText(getContext(),
                        "Statut mis à jour: " + nouveauStatut,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Méthode pour mettre à jour une commande dans la liste locale
    private void updateCommandeInList(String commandeId, String nouveauStatut) {
        for (Map<String, Object> commande : commandesList) {
            String id = (String) commande.get("id");
            if (commandeId.equals(id)) {
                commande.put("statut", nouveauStatut);
                break;
            }
        }
    }

    private String getNextStatut(String statutActuel) {
        switch (statutActuel) {
            case "En attente":
                return "En préparation";
            case "En préparation":
                return "Prête";
            case "Prête":
                return "Livrée";
            default:
                return statutActuel;
        }
    }

    private void updateNombreCommandes() {
        int nombreTotal = commandesList.size();

        // Compter les commandes d'aujourd'hui
        int nombreAujourdhui = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String aujourdhui = dateFormat.format(new Date());

        for (Map<String, Object> commande : commandesList) {
            // Récupérer la date de commande et la comparer
            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) commande.get("dateCommande");
            if (timestamp != null) {
                String dateCommande = dateFormat.format(timestamp.toDate());
                if (aujourdhui.equals(dateCommande)) {
                    nombreAujourdhui++;
                }
            }
        }

        tvNombreCommandes.setText(nombreAujourdhui + " commande(s) aujourd'hui • " + nombreTotal + " au total");
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerViewCommandes.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
        recyclerViewCommandes.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les commandes quand on revient sur le fragment
        loadCommandes();
    }
}