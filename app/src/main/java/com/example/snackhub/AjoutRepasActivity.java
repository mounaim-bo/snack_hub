package com.example.snackhub;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.snackhub.models.Repas;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AjoutRepasActivity extends AppCompatActivity {

    private static final String TAG = "AjoutRepasActivity";

    private Toolbar toolbar;
    private TextInputEditText editNomRepas;
    private TextInputEditText editPrixRepas;
    private AutoCompleteTextView dropdownCategorie;
    private TextInputEditText editDescriptionRepas;
    private ImageView imageRepas;
    private MaterialButton btnChoisirImage;
    private MaterialButton btnEnregistrerRepas;

    private Uri imageUri;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String currentSnackId;

    // Gestionnaire pour récupérer l'image sélectionnée
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            imageRepas.setImageURI(imageUri);
                            btnChoisirImage.setText("Changer l'image");
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_repas);

        // Initialisation Firebase
        initializeFirebase();

        // Initialisation des vues
        initViews();

        // Configuration de la toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ajouter un repas");
        }

        // Configuration du dropdown pour les catégories
        setupCategoriesDropdown();

        // Configuration des listeners
        setupListeners();

        // Charger l'ID du snack actuel
        loadCurrentSnackId();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_ajout_repas);
        editNomRepas = findViewById(R.id.edit_nom_repas);
        editPrixRepas = findViewById(R.id.edit_prix_repas);
        dropdownCategorie = findViewById(R.id.dropdown_categorie);
        editDescriptionRepas = findViewById(R.id.edit_description_repas);
        imageRepas = findViewById(R.id.image_repas);
        btnChoisirImage = findViewById(R.id.btn_choisir_image);
        btnEnregistrerRepas = findViewById(R.id.btn_enregistrer_repas);
    }

    private void setupCategoriesDropdown() {
        // Catégories adaptées à votre système
        String[] categories = {"Sandwich", "Plat chaud", "Dessert", "Boisson", "Salade", "Pizza", "Burger", "Pasta"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        dropdownCategorie.setAdapter(adapter);
    }

    private void setupListeners() {
        // Configuration du bouton pour choisir une image
        btnChoisirImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImage.launch(galleryIntent);
            }
        });

        // Configuration du bouton d'enregistrement
        btnEnregistrerRepas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    sauvegarderRepas();
                }
            }
        });
    }

    private void loadCurrentSnackId() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        currentSnackId = documentSnapshot.getString("snackId");
                        if (currentSnackId == null || currentSnackId.isEmpty()) {
                            Toast.makeText(this, "Aucun snack associé à ce compte", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Données utilisateur introuvables", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Erreur lors du chargement des données utilisateur", e);
                    Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du nom
        String nom = editNomRepas.getText() != null ? editNomRepas.getText().toString().trim() : "";
        if (TextUtils.isEmpty(nom)) {
            editNomRepas.setError("Le nom du repas est requis");
            isValid = false;
        } else {
            editNomRepas.setError(null);
        }

        // Validation du prix
        String prixStr = editPrixRepas.getText() != null ? editPrixRepas.getText().toString().trim() : "";
        if (TextUtils.isEmpty(prixStr)) {
            editPrixRepas.setError("Le prix est requis");
            isValid = false;
        } else {
            try {
                double prix = Double.parseDouble(prixStr);
                if (prix <= 0) {
                    editPrixRepas.setError("Le prix doit être supérieur à 0");
                    isValid = false;
                } else {
                    editPrixRepas.setError(null);
                }
            } catch (NumberFormatException e) {
                editPrixRepas.setError("Prix invalide");
                isValid = false;
            }
        }

        // Validation de la description
        String description = editDescriptionRepas.getText() != null ?
                editDescriptionRepas.getText().toString().trim() : "";
        if (TextUtils.isEmpty(description)) {
            editDescriptionRepas.setError("La description est requise");
            isValid = false;
        } else {
            editDescriptionRepas.setError(null);
        }

        // Validation de la catégorie
        String categorie = dropdownCategorie.getText() != null ?
                dropdownCategorie.getText().toString().trim() : "";
        if (TextUtils.isEmpty(categorie)) {
            Toast.makeText(this, "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validation du snack ID
        if (currentSnackId == null || currentSnackId.isEmpty()) {
            Toast.makeText(this, "Impossible d'ajouter le repas : snack non identifié", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void sauvegarderRepas() {
        showLoading(true);

        if (imageUri != null) {
            // Upload de l'image puis création du repas
            uploadImageAndCreateRepas();
        } else {
            // Création directe du repas sans image
            createRepas(null);
        }
    }

    private void uploadImageAndCreateRepas() {
        String imageFileName = "repas/" + currentSnackId + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imageFileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Récupérer l'URL de téléchargement
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(this::createRepas)
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erreur lors de la récupération de l'URL", e);
                                showLoading(false);
                                Toast.makeText(this, "Erreur lors de l'upload de l'image", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de l'upload de l'image", e);
                    showLoading(false);
                    Toast.makeText(this, "Erreur lors de l'upload de l'image", Toast.LENGTH_SHORT).show();
                });
    }

    private void createRepas(Uri downloadUrl) {
        String nom = editNomRepas.getText().toString().trim();
        String description = editDescriptionRepas.getText().toString().trim();
        double prix = Double.parseDouble(editPrixRepas.getText().toString().trim());
        String categorie = dropdownCategorie.getText().toString().trim();
        String imageUrl = downloadUrl != null ? downloadUrl.toString() : "";

        // Créer un objet Repas avec votre classe existante
        Repas nouveauRepas = new Repas(
                nom,
                description,
                prix,
                imageUrl,
                categorie,
                true, // Disponible par défaut
                currentSnackId
        );

        // Ajouter à Firestore
        db.collection("repas")
                .add(nouveauRepas)
                .addOnSuccessListener(documentReference -> {
                    showLoading(false);
                    Log.d(TAG, "Repas ajouté avec l'ID: " + documentReference.getId());

                    // Envoyer le résultat à l'activité précédente
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("REPAS_AJOUTE", true);
                    resultIntent.putExtra("REPAS_NOM", nom);
                    setResult(Activity.RESULT_OK, resultIntent);

                    Toast.makeText(this, "Repas ajouté avec succès!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Erreur lors de l'ajout du repas", e);
                    Toast.makeText(this, "Erreur lors de l'ajout du repas", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        btnEnregistrerRepas.setEnabled(!show);
        btnChoisirImage.setEnabled(!show);

        if (show) {
            btnEnregistrerRepas.setText("Ajout en cours...");
        } else {
            btnEnregistrerRepas.setText("Enregistrer");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup si nécessaire
    }
}