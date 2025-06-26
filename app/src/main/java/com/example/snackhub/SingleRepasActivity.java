package com.example.snackhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.snackhub.models.Commande;
import com.example.snackhub.utils.PanierManager;

public class SingleRepasActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText quantityInput, descriptionInput;
    private TextView snackNameDetail;
    private PanierManager panierManager;

    // Variables pour stocker les informations du snack
    private String snackId;
    private String snackName;
    private String snackDescription;
    private String snackPhone;
    private String snackAddress;
    private String snackEmail;
    private String snackOpeningHours;
    private String snackMainImageUrl;

    // Variables pour le repas actuel
    private String repasNom;
    private double repasPrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_repas);

        panierManager = PanierManager.getInstance();

        initViews();
        setupToolbar();
        setupBackButton();
        getSnackDataFromIntent();
        getRepasDataFromIntent();

        // Stocker les infos du snack dans le PanierManager
        panierManager.setSnackInfo(snackId, snackName, snackDescription, snackPhone,
                snackAddress, snackEmail, snackOpeningHours, snackMainImageUrl);

        Button addToCartButton = findViewById(R.id.addToCartButton);
        addToCartButton.setOnClickListener(v -> ajouterAuPanier());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.single_repas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        quantityInput = findViewById(R.id.quantityInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        snackNameDetail = findViewById(R.id.snackNameDetail);
    }

    private void getSnackDataFromIntent() {
        Intent intent = getIntent();

        // Récupérer toutes les données du snack depuis l'intent
        snackId = intent.getStringExtra("snackId");
        snackName = intent.getStringExtra("snackName");
        snackDescription = intent.getStringExtra("snackDescription");
        snackPhone = intent.getStringExtra("snackPhone");
        snackAddress = intent.getStringExtra("snackAddress");
        snackEmail = intent.getStringExtra("snackEmail");
        snackOpeningHours = intent.getStringExtra("snackOpeningHours");
        snackMainImageUrl = intent.getStringExtra("snackMainImageUrl");
    }

    private void getRepasDataFromIntent() {
        Intent intent = getIntent();

        // Récupérer les données du repas (utilisant VOS noms d'extras existants)
        repasNom = intent.getStringExtra("repasName"); // Votre nom existant
        double tempPrix = intent.getDoubleExtra("repasPrice", 0.0); // Votre nom existant
        repasPrix = tempPrix;

        // Afficher les informations du repas
        if (repasNom != null && repasPrix > 0) {
            snackNameDetail.setText(repasNom + " : " + repasPrix + " Dhs");
        }
    }

    private void ajouterAuPanier() {
        // Récupérer les valeurs saisies
        String quantiteStr = quantityInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        // Validation
        if (quantiteStr.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une quantité", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantite;
        try {
            quantite = Integer.parseInt(quantiteStr);
            if (quantite <= 0) {
                Toast.makeText(this, "La quantité doit être supérieure à 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantité invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer la commande
        Commande nouvelleCommande = new Commande(
                R.drawable.food1, // Image par défaut, vous pouvez la personnaliser
                repasNom != null ? repasNom : "Repas",
                description.isEmpty() ? "Aucune description" : description,
                quantite,
                repasPrix // double, pas int
        );

        // Ajouter au PanierActivity
        panierManager.ajouterCommande(nouvelleCommande);

        // Message de confirmation
        Toast.makeText(this, quantite + " x " + repasNom + " ajouté(s) au PanierActivity",
                Toast.LENGTH_SHORT).show();

        // Aller au PanierActivity
        Intent intent = new Intent(this, PanierActivity.class);
        startActivity(intent);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}