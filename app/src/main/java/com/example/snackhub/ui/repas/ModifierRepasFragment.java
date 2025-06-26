package com.example.snackhub.ui.repas;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.snackhub.R;
import com.example.snackhub.models.Repas;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModifierRepasFragment extends Fragment {

    private static final String TAG = "ModifierRepasFragment";

    // Views
    private EditText editTextNom;
    private EditText editTextDescription;
    private EditText editPrixRepas;
    private Spinner spinnerCategorie;
    private ImageView imageRepas;
    private Switch switchDisponible;
    private MaterialButton btnChoisirImage;
    private MaterialButton btnSauvegarder;
    private MaterialButton btnAnnuler;

    // Données du repas
    private String repasId;
    private String repasNom;
    private String repasDescription;
    private String repasCategorie;
    private String repasPrix;
    private String repasImageUrl;
    private boolean repasDisponible;

    // Image
    private Uri nouvelleImageUri;
    private boolean imageModifiee = false;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Gestionnaire pour la sélection d'image
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            nouvelleImageUri = data.getData();
                            imageModifiee = true;

                            // Afficher la nouvelle image
                            Glide.with(ModifierRepasFragment.this)
                                    .load(nouvelleImageUri)
                                    .centerCrop()
                                    .into(imageRepas);

                            btnChoisirImage.setText("Changer l'image");
                        }
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modifier_repas, container, false);

        // Initialisation des vues
        initViews(view);

        // Récupération des arguments
        recupererArguments();

        // Configuration du spinner
        setupSpinner();

        // Remplissage des champs
        remplirChamps();

        // Configuration des listeners
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        editTextNom = view.findViewById(R.id.edit_nom_repas);
        editTextDescription = view.findViewById(R.id.edit_description_repas);
        editPrixRepas = view.findViewById(R.id.edit_prix_repas);
        spinnerCategorie = view.findViewById(R.id.filterSpinner);
        imageRepas = view.findViewById(R.id.image_repas);
        switchDisponible = view.findViewById(R.id.switchDisponible);
        btnChoisirImage = view.findViewById(R.id.btn_choisir_image);
        btnSauvegarder = view.findViewById(R.id.btn_sauvegarder);
        btnAnnuler = view.findViewById(R.id.btn_annuler);
    }

    private void recupererArguments() {
        Bundle args = getArguments();
        if (args != null) {
            repasId = args.getString("repasId");
            repasNom = args.getString("repasNom");
            repasDescription = args.getString("repasDescription");
            repasCategorie = args.getString("repasCategorie");
            repasPrix = args.getString("repasPrix");
            repasImageUrl = args.getString("repasImageUrl");
            repasDisponible = args.getBoolean("repasDisponible");

            Log.d(TAG, "Modification du repas: " + repasId + " - " + repasNom);
        }
    }

    private void setupSpinner() {
        String[] categories = {"Sandwich", "Plat chaud", "Dessert", "Boisson", "Salade", "Pizza", "Burger", "Pasta"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorie.setAdapter(adapter);
    }

    private void remplirChamps() {
        if (repasNom != null) editTextNom.setText(repasNom);
        if (repasDescription != null) editTextDescription.setText(repasDescription);
        if (repasPrix != null) editPrixRepas.setText(repasPrix);

        // Sélection de la catégorie dans le spinner
        if (repasCategorie != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategorie.getAdapter();
            int position = adapter.getPosition(repasCategorie);
            if (position >= 0) {
                spinnerCategorie.setSelection(position);
            }
        }

        // Affichage de l'image
        if (repasImageUrl != null && !repasImageUrl.isEmpty()) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.food1)
                    .error(R.drawable.food1);

            Glide.with(this)
                    .load(repasImageUrl)
                    .apply(options)
                    .into(imageRepas);
        } else {
            imageRepas.setImageResource(R.drawable.food1);
        }

        // État de disponibilité
        switchDisponible.setChecked(repasDisponible);
    }

    private void setupListeners() {
        // Bouton pour choisir une nouvelle image
        btnChoisirImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage.launch(galleryIntent);
        });

        // Bouton sauvegarder
        btnSauvegarder.setOnClickListener(v -> {
            if (validerFormulaire()) {
                sauvegarderModifications();
            }
        });

        // Bouton annuler
        btnAnnuler.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    private boolean validerFormulaire() {
        boolean isValid = true;

        // Validation du nom
        String nom = editTextNom.getText().toString().trim();
        if (TextUtils.isEmpty(nom)) {
            editTextNom.setError("Le nom du repas est requis");
            isValid = false;
        } else {
            editTextNom.setError(null);
        }

        // Validation du prix
        String prixStr = editPrixRepas.getText().toString().trim();
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
        String description = editTextDescription.getText().toString().trim();
        if (TextUtils.isEmpty(description)) {
            editTextDescription.setError("La description est requise");
            isValid = false;
        } else {
            editTextDescription.setError(null);
        }

        return isValid;
    }

    private void sauvegarderModifications() {
        showLoading(true);

        if (imageModifiee && nouvelleImageUri != null) {
            // Upload de la nouvelle image puis mise à jour
            uploadImageAndUpdateRepas();
        } else {
            // Mise à jour directe sans changer l'image
            updateRepasInFirestore(repasImageUrl);
        }
    }

    private void uploadImageAndUpdateRepas() {
        String imageFileName = "repas/" + repasId + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imageFileName);

        imageRef.putFile(nouvelleImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Récupérer l'URL de téléchargement
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                updateRepasInFirestore(downloadUri.toString());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erreur lors de la récupération de l'URL", e);
                                showLoading(false);
                                Toast.makeText(getContext(), "Erreur lors de l'upload de l'image", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de l'upload de l'image", e);
                    showLoading(false);
                    Toast.makeText(getContext(), "Erreur lors de l'upload de l'image", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRepasInFirestore(String imageUrl) {
        String nom = editTextNom.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        double prix = Double.parseDouble(editPrixRepas.getText().toString().trim());
        String categorie = spinnerCategorie.getSelectedItem().toString();
        boolean disponible = switchDisponible.isChecked();

        // Créer les données à mettre à jour
        Map<String, Object> updates = new HashMap<>();
        updates.put("nom", nom);
        updates.put("description", description);
        updates.put("prix", prix);
        updates.put("categorie", categorie);
        updates.put("disponible", disponible);
        if (imageUrl != null) {
            updates.put("imageUrl", imageUrl);
        }

        // Mettre à jour dans Firestore
        db.collection("repas")
                .document(repasId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d(TAG, "Repas mis à jour avec succès: " + repasId);
                    Toast.makeText(getContext(), "Repas modifié avec succès!", Toast.LENGTH_SHORT).show();

                    // Retour à la liste des repas
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Erreur lors de la mise à jour du repas", e);
                    Toast.makeText(getContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        btnSauvegarder.setEnabled(!show);
        btnChoisirImage.setEnabled(!show);

        if (show) {
            btnSauvegarder.setText("Modification en cours...");
        } else {
            btnSauvegarder.setText("Sauvegarder les modifications");
        }
    }
}