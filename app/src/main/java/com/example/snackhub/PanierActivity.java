package com.example.snackhub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackhub.adapters.CommandeAdapter;
import com.example.snackhub.models.Commande;
import com.example.snackhub.utils.PanierManager;
import com.example.snackhub.utils.CommandeFirebaseUtils;

import java.util.List;

public class PanierActivity extends AppCompatActivity {
    private RecyclerView recyclerViewPanier;
    private TextView totalPriceTextView;
    private RadioGroup modeCommandeRadioGroup;
    private Button validerCommandeButton;
    private Button continuerAchatsButton;
    private Toolbar toolbar;

    private List<Commande> commandeList;
    private CommandeAdapter commandeAdapter;
    private PanierManager panierManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panier);

        panierManager = PanierManager.getInstance();

        initViews();
        setupToolbar();
        setupBackButton();
        setupRecyclerView();
        setupButtons();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_panier), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        recyclerViewPanier = findViewById(R.id.recyclerViewPanier);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        modeCommandeRadioGroup = findViewById(R.id.modeCommandeRadioGroup);
        validerCommandeButton = findViewById(R.id.validerCommandeButton);
        continuerAchatsButton = findViewById(R.id.continuerAchatsButton);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void getSnackDataFromIntent() {
        // Cette méthode n'est plus nécessaire car les données sont dans PanierManager
    }

    private void setupRecyclerView() {
        // Récupérer les vraies commandes du PanierActivity
        commandeList = panierManager.getCommandes();

        // Si le PanierActivity est vide, afficher un message
        if (commandeList.isEmpty()) {
            Toast.makeText(this, "Votre PanierActivity est vide", Toast.LENGTH_SHORT).show();
        }

        commandeAdapter = new CommandeAdapter(commandeList);
        recyclerViewPanier.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPanier.setAdapter(commandeAdapter);

        calculerTotal();
    }

    private void setupButtons() {
        // Bouton Valider commande
        validerCommandeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validerCommande();
            }
        });

        // Bouton Continuer les achats
        continuerAchatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retourner au menu du snack actuel
                Intent intent = new Intent(PanierActivity.this, MenuSnackActivity.class);

                // Récupérer les informations du snack depuis PanierManager
                intent.putExtra("snackId", panierManager.getCurrentSnackId());
                intent.putExtra("snackName", panierManager.getCurrentSnackName());
                intent.putExtra("snackDescription", panierManager.getCurrentSnackDescription());
                intent.putExtra("snackPhone", panierManager.getCurrentSnackPhone());
                intent.putExtra("snackAddress", panierManager.getCurrentSnackAddress());
                intent.putExtra("snackEmail", panierManager.getCurrentSnackEmail());
                intent.putExtra("snackOpeningHours", panierManager.getCurrentSnackOpeningHours());
                intent.putExtra("snackMainImageUrl", panierManager.getCurrentSnackMainImageUrl());

                startActivity(intent);
            }
        });
    }

    private void calculerTotal() {
        double total = panierManager.getTotalPrix();
        totalPriceTextView.setText("Total : " + total + " DH");
    }

    private void validerCommande() {
        // Vérifier que le PanierActivity n'est pas vide
        if (panierManager.estVide()) {
            Toast.makeText(this, "Votre PanierActivity est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier qu'un mode de commande est sélectionné
        int selectedId = modeCommandeRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Veuillez sélectionner un mode de commande", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        String modeCommande = selectedRadioButton.getText().toString();

        // Afficher le formulaire client
        afficherFormulaireClient(modeCommande);
    }

    private void afficherFormulaireClient(String modeCommande) {
        // Créer le dialog avec le formulaire
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_formulaire_client, null);

        // Récupérer les vues du formulaire
        EditText etNom = dialogView.findViewById(R.id.etClientNom);
        EditText etTelephone = dialogView.findViewById(R.id.etClientTelephone);
        EditText etEmail = dialogView.findViewById(R.id.etClientEmail);
        EditText etNotes = dialogView.findViewById(R.id.etClientNotes);

        builder.setView(dialogView)
                .setTitle("Finaliser la commande")
                .setPositiveButton("Confirmer", null)
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Gérer le clic sur "Confirmer"
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String telephone = etTelephone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            // Validation
            if (nom.isEmpty()) {
                etNom.setError("Nom requis");
                return;
            }
            if (telephone.isEmpty()) {
                etTelephone.setError("Téléphone requis");
                return;
            }

            // Sauvegarder la commande
            sauvegarderCommandeFirebase(nom, telephone, email, modeCommande, notes);
            dialog.dismiss();
        });
    }

    private void sauvegarderCommandeFirebase(String clientNom, String clientTelephone,
                                             String clientEmail, String modeCommande, String notes) {

        // Afficher un message de chargement
        Toast.makeText(this, "Sauvegarde de la commande...", Toast.LENGTH_SHORT).show();

        CommandeFirebaseUtils.sauvegarderCommande(
                panierManager.getCurrentSnackId(),
                panierManager.getCurrentSnackName(),
                clientNom,
                clientTelephone,
                clientEmail,
                modeCommande,
                panierManager.getCommandes(),
                panierManager.getTotalPrix(),
                notes,
                new CommandeFirebaseUtils.CommandeCallback() {
                    @Override
                    public void onSuccess(String commandeId) {
                        runOnUiThread(() -> {
                            Toast.makeText(PanierActivity.this,
                                    "Commande enregistrée avec succès !\nNuméro: " + commandeId.substring(0, 8),
                                    Toast.LENGTH_LONG).show();

                            // Vider le PanierActivity
                            panierManager.viderPanier();

                            // Retourner au menu ou à l'accueil
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(PanierActivity.this,
                                    "Erreur lors de l'enregistrement: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }
}