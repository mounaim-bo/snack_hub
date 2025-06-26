package com.example.snackhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snackhub.adapters.ImageAdapter;
import com.example.snackhub.models.Repas;
import com.example.snackhub.utils.DialogUtils;
import com.example.snackhub.utils.PanierManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MenuSnackActivity extends AppCompatActivity {

    // Views
    private ImageView snackImage;
    private TextView snackName, snackDescription, snackPhone, snackAddress, snackEmail, snackOpeningHours;
    private RecyclerView recyclerView;
    private FloatingActionButton fabCall;
    private ExtendedFloatingActionButton fabPanier;
    private ImageButton btnPanier;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    // Data
    private ImageAdapter adapter;
    private List<Repas> imageList;
    private FirebaseFirestore db;
    private String snackId;
    private String phoneNumber;
    private PanierManager panierManager;

    // Variables pour stocker les informations du snack
    private String currentSnackName;
    private String currentSnackDescription;
    private String currentSnackPhone;
    private String currentSnackAddress;
    private String currentSnackEmail;
    private String currentSnackOpeningHours;
    private String currentSnackMainImageUrl;

    private static final String TAG = "MenuSnackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_snack);

        // Initialisation
        panierManager = PanierManager.getInstance();
        initFirebase();
        initViews();
        setupToolbar();
        getSnackDataFromIntent();
        initRecyclerView();
        setupFabCall();
        setupPanierButtons();

        // Charger les repas du snack depuis Firebase
        if (snackId != null) {
            loadRepasFromFirebase();
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        snackImage = findViewById(R.id.snackImageDetail);
        snackName = findViewById(R.id.snackNameDetail);
        snackDescription = findViewById(R.id.snackDescriptionDetail);
        snackPhone = findViewById(R.id.snackPhoneDetail);
        snackAddress = findViewById(R.id.snackAddressDetail);
        snackEmail = findViewById(R.id.snackEmailDetail);
        snackOpeningHours = findViewById(R.id.snackOpeningHoursDetail);
        recyclerView = findViewById(R.id.recyclerView);
        fabCall = findViewById(R.id.fabCall);
        fabPanier = findViewById(R.id.fabPanier);
        btnPanier = findViewById(R.id.btnPanier);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getSnackDataFromIntent() {
        Intent intent = getIntent();

        // Récupérer toutes les données du snack
        snackId = intent.getStringExtra("snackId");
        currentSnackName = intent.getStringExtra("snackName");
        currentSnackDescription = intent.getStringExtra("snackDescription");
        currentSnackPhone = intent.getStringExtra("snackPhone");
        currentSnackAddress = intent.getStringExtra("snackAddress");
        currentSnackEmail = intent.getStringExtra("snackEmail");
        currentSnackOpeningHours = intent.getStringExtra("snackOpeningHours");
        currentSnackMainImageUrl = intent.getStringExtra("snackMainImageUrl");

        // Stocker le numéro de téléphone pour le FAB
        phoneNumber = currentSnackPhone;

        // Mettre à jour le titre de la CollapsingToolbar
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle(currentSnackName != null ? currentSnackName : "Menu");
        }

        // Afficher les données dans les vues
        displaySnackInfo(currentSnackName, currentSnackDescription, currentSnackPhone,
                currentSnackAddress, currentSnackEmail, currentSnackOpeningHours,
                currentSnackMainImageUrl);

        // Stocker les informations du snack dans PanierManager
        panierManager.setSnackInfo(snackId, currentSnackName, currentSnackDescription,
                currentSnackPhone, currentSnackAddress, currentSnackEmail,
                currentSnackOpeningHours, currentSnackMainImageUrl);

        Log.d(TAG, "Snack ID: " + snackId + ", Name: " + currentSnackName);
    }

    private void displaySnackInfo(String name, String description, String phone,
                                  String address, String email, String openingHours, String mainImageUrl) {

        // Nom et description dans l'en-tête
        snackName.setText(name != null ? name : "Nom non disponible");
        snackDescription.setText(description != null ? description : "Description non disponible");

        // Informations de contact
        snackPhone.setText(phone != null ? phone : "Non disponible");
        snackAddress.setText(address != null ? address : "Non disponible");
        snackEmail.setText(email != null ? email : "Non disponible");
        snackOpeningHours.setText(openingHours != null ? openingHours : "Non disponibles");

        // Charger l'image avec Glide
        loadSnackImage(mainImageUrl);
    }

    private void loadSnackImage(String mainImageUrl) {
        if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(mainImageUrl)
                    .placeholder(R.drawable.images3)
                    .error(R.drawable.images3)
                    .centerCrop()
                    .into(snackImage);
        } else {
            snackImage.setImageResource(R.drawable.images3);
        }
    }

    private void initRecyclerView() {
        // Initialiser avec des données d'exemple
        imageList = new ArrayList<>();

        adapter = new ImageAdapter(this, imageList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Désactiver le scroll imbriqué pour de meilleures performances
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupFabCall() {
        fabCall.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                // Ouvrir l'application de téléphone avec le numéro
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            } else {
                Toast.makeText(this, "Numéro de téléphone non disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Nouvelle méthode pour gérer les boutons PanierActivity
    private void setupPanierButtons() {
        // Bouton PanierActivity dans la toolbar
        btnPanier.setOnClickListener(v -> goToPanier());

        // Bouton PanierActivity flottant (si vous décidez de l'utiliser)
        if (fabPanier != null) {
            fabPanier.setOnClickListener(v -> goToPanier());
        }
    }

    // Méthode pour aller au PanierActivity avec les infos du snack
    private void goToPanier() {
        Intent intent = new Intent(this, PanierActivity.class);
        startActivity(intent);

        // Toast pour confirmer l'action
        int nbArticles = panierManager.getNombreArticles();
        if (nbArticles > 0) {
            Toast.makeText(this, "Panier (" + nbArticles + " article(s))", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Panier vide", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRepasFromFirebase() {
        if (snackId == null || snackId.isEmpty()) {
            Log.w(TAG, "Snack ID est null, impossible de charger les repas");
            return;
        }

        DialogUtils.showLoadingDialog(this, "Chargement du menu...");

        // Chercher les repas qui appartiennent à ce snack
        db.collection("repas")
                .whereEqualTo("snackId", snackId)
                .get()
                .addOnCompleteListener(task -> {
                    DialogUtils.hideLoadingDialog();

                    if (task.isSuccessful()) {
                        List<Repas> firebaseRepasList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Repas repas = document.toObject(Repas.class);
                                if (repas != null) {
                                    firebaseRepasList.add(repas);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors de la conversion du repas: " + e.getMessage());
                            }
                        }

                        if (!firebaseRepasList.isEmpty()) {
                            // Remplacer les données d'exemple par les vraies données
                            imageList.clear();
                            imageList.addAll(firebaseRepasList);
                            adapter.notifyDataSetChanged();

                            showToast(firebaseRepasList.size() + " plat(s) chargé(s)");
                        } else {
                            Log.d(TAG, "Aucun repas trouvé pour ce snack, garder les exemples");
                            showToast("Menu d'exemple affiché");
                        }

                    } else {
                        Log.e(TAG, "Erreur lors du chargement des repas: ", task.getException());
                        showToast("Erreur lors du chargement du menu");
                    }
                })
                .addOnFailureListener(e -> {
                    DialogUtils.hideLoadingDialog();
                    Log.e(TAG, "Échec du chargement des repas: " + e.getMessage());
                    showToast("Erreur: " + e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nettoyer les ressources si nécessaire
    }
}