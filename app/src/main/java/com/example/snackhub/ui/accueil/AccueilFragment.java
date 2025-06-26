package com.example.snackhub.ui.accueil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.snackhub.R;
import com.example.snackhub.utils.CommandeFirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class AccueilFragment extends Fragment {

    private static final String TAG = "AccueilFragment";

    // Views essentielles
    private TextView userNameHome, nomSnackHome, textEmail, statsText;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView started");

        try {
            View root = inflater.inflate(R.layout.fragment_accueil, container, false);

            // Initialiser Firebase
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Initialiser seulement les vues essentielles
            initEssentialViews(root);

            Log.d(TAG, "onCreateView completed successfully");
            return root;

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);

            // Retourner une vue d'erreur simple
            TextView errorView = new TextView(getContext());
            errorView.setText("Erreur de chargement. Vérifiez le layout.");
            errorView.setPadding(50, 50, 50, 50);
            return errorView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume started");

        try {
            loadUserDataSafely();
            loadStatsData(); // Charger les statistiques
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
            showError("Erreur lors du chargement des données");
        }
    }

    private void initEssentialViews(View root) {
        try {
            // Chercher les vues une par une avec vérification
            userNameHome = root.findViewById(R.id.user_name_home);
            if (userNameHome == null) {
                Log.w(TAG, "user_name_home not found in layout");
            }

            nomSnackHome = root.findViewById(R.id.nom_snack_home);
            if (nomSnackHome == null) {
                Log.w(TAG, "nom_snack_home not found in layout");
            }

            textEmail = root.findViewById(R.id.text_email);
            if (textEmail == null) {
                Log.w(TAG, "text_email not found in layout");
            }

            // Ajouter le TextView des statistiques
            statsText = root.findViewById(R.id.statsText);
            if (statsText == null) {
                Log.w(TAG, "statsText not found in layout");
            }

            // Configurer le bouton edit s'il existe
            View editButton = root.findViewById(R.id.editButton);
            if (editButton != null) {
                editButton.setOnClickListener(v -> {
                    try {
                        // Navigation vers ModifierInfosFragment
                        androidx.navigation.NavController navController =
                                androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_backoffice);
                        navController.navigate(R.id.modifierInfosFragment);
                    } catch (Exception e) {
                        Log.e(TAG, "Navigation error: " + e.getMessage());
                        showError("Erreur de navigation");
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    private void loadUserDataSafely() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No current user");
            showError("Utilisateur non connecté");
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Loading data for user: " + userId);

        // Charger les données utilisateur avec gestion d'erreur
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "User document found");

                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String email = documentSnapshot.getString("email");

                            // Mettre à jour l'interface de manière sécurisée
                            updateUISafely(firstName, lastName, email);

                        } else {
                            Log.w(TAG, "User document does not exist");
                            showError("Profil utilisateur introuvable");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing user data: " + e.getMessage(), e);
                        showError("Erreur lors du traitement des données");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user data: " + e.getMessage(), e);
                    showError("Erreur de connexion à la base de données");
                });
    }

    private void loadStatsData() {
        // TEST TEMPORAIRE - utilisez directement l'ID visible dans Firebase
        // Remplacez par votre vraie logique pour obtenir l'ID du snack
        String snackId = "mSwHNEUQW1G0dEqSSuOS";

        Log.d(TAG, "Loading stats for snack: " + snackId);

        // Mettre un texte de chargement
        if (statsText != null) {
            statsText.setText("Chargement...");
        }

        CommandeFirebaseUtils.getCommandesSnack(snackId, new CommandeFirebaseUtils.CommandeSnackCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> commandes) {
                Log.d(TAG, "Commandes loaded for stats: " + commandes.size());

                try {
                    int totalRepasServis = calculateRepasServis(commandes);
                    int commandesLivrees = countCommandesLivrees(commandes);

                    // Mettre à jour l'interface sur le thread principal
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateStatsUI(totalRepasServis, commandesLivrees);
                        });
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error calculating stats: " + e.getMessage(), e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (statsText != null) {
                                statsText.setText("Erreur de calcul");
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load stats: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (statsText != null) {
                            statsText.setText("Données non disponibles");
                        }
                        showError("Erreur lors du chargement des statistiques");
                    });
                }
            }
        });
    }

    private int calculateRepasServis(List<Map<String, Object>> commandes) {
        int totalRepas = 0;

        for (Map<String, Object> commande : commandes) {
            String statut = (String) commande.get("statut");

            // Compter seulement les commandes livrées
            if ("Livrée".equals(statut)) {
                List<Map<String, Object>> articles = (List<Map<String, Object>>) commande.get("articles");

                if (articles != null) {
                    for (Map<String, Object> article : articles) {
                        Object quantiteObj = article.get("quantite");
                        int quantite = 0;

                        // Gérer les différents types de quantité (Long, Integer, String)
                        if (quantiteObj instanceof Long) {
                            quantite = ((Long) quantiteObj).intValue();
                        } else if (quantiteObj instanceof Integer) {
                            quantite = (Integer) quantiteObj;
                        } else if (quantiteObj instanceof String) {
                            try {
                                quantite = Integer.parseInt((String) quantiteObj);
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "Invalid quantity format: " + quantiteObj);
                            }
                        }

                        totalRepas += quantite;
                        Log.d(TAG, "Article: " + article.get("nomRepas") + " - Quantité: " + quantite);
                    }
                }
            }
        }

        Log.d(TAG, "Total repas servis calculé: " + totalRepas);
        return totalRepas;
    }

    private int countCommandesLivrees(List<Map<String, Object>> commandes) {
        int commandesLivrees = 0;

        for (Map<String, Object> commande : commandes) {
            String statut = (String) commande.get("statut");
            if ("Livrée".equals(statut)) {
                commandesLivrees++;
            }
        }

        return commandesLivrees;
    }

    private void updateStatsUI(int totalRepasServis, int commandesLivrees) {
        try {
            if (statsText != null) {
                if (totalRepasServis > 0) {
                    String statsMessage = totalRepasServis + " repas servis";
                    if (commandesLivrees > 0) {
                        statsMessage += " (" + commandesLivrees + " commandes)";
                    }
                    statsText.setText(statsMessage);
                } else {
                    statsText.setText("Aucun repas servi");
                }
            }

            Log.d(TAG, "Stats UI updated: " + totalRepasServis + " repas, " + commandesLivrees + " commandes");

        } catch (Exception e) {
            Log.e(TAG, "Error updating stats UI: " + e.getMessage(), e);
        }
    }

    private void updateUISafely(String firstName, String lastName, String email) {
        try {
            // Mettre à jour le nom d'utilisateur
            if (userNameHome != null) {
                if (firstName != null && lastName != null && !firstName.isEmpty() && !lastName.isEmpty()) {
                    userNameHome.setText("Bonjour " + firstName + " " + lastName + " !");
                } else {
                    userNameHome.setText("Bonjour ! Complétez votre profil");
                }
            }

            // Mettre à jour l'email
            if (textEmail != null) {
                textEmail.setText(email != null ? email : "Email non disponible");
            }

            // Mettre à jour le nom du snack (valeur par défaut)
            if (nomSnackHome != null) {
                nomSnackHome.setText("Mon Snack");
            }

            Log.d(TAG, "UI updated successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "Error: " + message);
    }
}