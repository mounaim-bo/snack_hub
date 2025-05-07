package com.example.snackhub;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.snackhub.Repas;

public class AjoutRepasActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText editNomRepas;
    private TextInputEditText editPrixRepas;
    private AutoCompleteTextView dropdownCategorie;
    private TextInputEditText editDescriptionRepas;
    private ImageView imageRepas;
    private MaterialButton btnChoisirImage;
    private MaterialButton btnEnregistrerRepas;

    private Uri imageUri;

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

        // Initialisation des vues
        initViews();

        // Configuration de la toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ajouter un repas");
        }

        // Configuration du dropdown pour les catégories
        String[] categories = {"Petit-déjeuner", "Déjeuner", "Dîner", "Collation", "Dessert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        dropdownCategorie.setAdapter(adapter);

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
                sauvegarderRepas();
            }
        });
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

    private void sauvegarderRepas() {
        String nom = editNomRepas.getText() != null ? editNomRepas.getText().toString().trim() : "";
        String prix = editPrixRepas.getText() != null ? editPrixRepas.getText().toString().trim() : "";
        String categorie = dropdownCategorie.getText() != null ? dropdownCategorie.getText().toString().trim() : "";
        String description = editDescriptionRepas.getText() != null ?
                editDescriptionRepas.getText().toString().trim() : "";

        // Vérification des données obligatoires
        if (nom.isEmpty()) {
            editNomRepas.setError("Le nom est obligatoire");
            return;
        }

        if (prix.isEmpty()) {
            editPrixRepas.setError("Le prix est obligatoire");
            return;
        }

        if (categorie.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse le prix en double
        double prixDouble;
        try {
            prixDouble = Double.parseDouble(prix);
        } catch (NumberFormatException e) {
            editPrixRepas.setError("Prix invalide");
            return;
        }

        // Créer un nouvel objet Repas
        Repas nouveauRepas = new Repas(
                String.valueOf((System.currentTimeMillis())), // ID temporaire
                nom,
                prixDouble,
                categorie,
                description,
                imageUri != null ? imageUri.toString() : null
        );

        // TODO: Sauvegardez le repas dans votre base de données
        // Par exemple: repasDAO.insert(nouveauRepas);

        // Envoyer le résultat à l'activité précédente
        Intent resultIntent = new Intent();
        resultIntent.putExtra("NOUVEAU_REPAS", (CharSequence) nouveauRepas);
        setResult(Activity.RESULT_OK, resultIntent);

        // Afficher un message de confirmation
        Toast.makeText(this, "Repas ajouté avec succès", Toast.LENGTH_SHORT).show();

        // Terminer l'activité et retourner à l'écran précédent
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}