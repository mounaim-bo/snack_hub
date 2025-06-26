package com.example.snackhub.ui.repas;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.snackhub.R;
import com.example.snackhub.models.Repas;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AjoutRepasFragment extends Fragment {

    private static final String TAG = "AjoutRepasFragment";

    // Views
    private TextInputEditText editNomRepas;
    private TextInputEditText editPrixRepas;
    private TextInputEditText editDescriptionRepas;
    private Spinner categorieSpinner;
    private ImageView imageRepas;
    private MaterialButton btnChoisirImage;
    private MaterialButton btnEnregistrerRepas;
    private View loadingView;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Image
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private String currentSnackId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebase();
        setupImagePicker();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ajout_repas, container, false);

        initializeViews(rootView);
        setupSpinner();
        setupListeners();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCurrentSnackId();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void initializeViews(View rootView) {
        editNomRepas = rootView.findViewById(R.id.edit_nom_repas);
        editPrixRepas = rootView.findViewById(R.id.edit_prix_repas);
        editDescriptionRepas = rootView.findViewById(R.id.edit_description_repas);
        categorieSpinner = rootView.findViewById(R.id.filterSpinner);
        imageRepas = rootView.findViewById(R.id.image_repas);
        btnChoisirImage = rootView.findViewById(R.id.btn_choisir_image);
        btnEnregistrerRepas = rootView.findViewById(R.id.btn_enregistrer_repas);
        // loadingView = rootView.findViewById(R.id.loadingView); // Ajoutez si nécessaire
    }

    private void setupSpinner() {
        String[] categories = {"Sandwich", "Plat chaud", "Dessert", "Boisson", "Salade", "Pizza"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item, categories);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        categorieSpinner.setAdapter(adapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Afficher l'image sélectionnée
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .centerCrop()
                                    .into(imageRepas);

                            btnChoisirImage.setText("Changer l'image");
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        btnChoisirImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnEnregistrerRepas.setOnClickListener(v -> {
            if (validateForm()) {
                ajouterRepas();
            }
        });
    }

    private void loadCurrentSnackId() {
        if (auth.getCurrentUser() == null) {
            showError("Utilisateur non connecté");
            return;
        }

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentSnackId = documentSnapshot.getString("snackId");
                        if (currentSnackId == null || currentSnackId.isEmpty()) {
                            showError("Aucun snack associé à ce compte");
                        }
                    } else {
                        showError("Données utilisateur introuvables");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors du chargement des données utilisateur", e);
                    showError("Erreur lors du chargement des données");
                });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du nom
        if (TextUtils.isEmpty(editNomRepas.getText())) {
            editNomRepas.setError("Le nom du repas est requis");
            isValid = false;
        } else {
            editNomRepas.setError(null);
        }

        // Validation du prix
        if (TextUtils.isEmpty(editPrixRepas.getText())) {
            editPrixRepas.setError("Le prix est requis");
            isValid = false;
        } else {
            try {
                double prix = Double.parseDouble(editPrixRepas.getText().toString());
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
        if (TextUtils.isEmpty(editDescriptionRepas.getText())) {
            editDescriptionRepas.setError("La description est requise");
            isValid = false;
        } else {
            editDescriptionRepas.setError(null);
        }

        // Validation du snack ID
        if (currentSnackId == null || currentSnackId.isEmpty()) {
            showError("Impossible d'ajouter le repas : snack non identifié");
            isValid = false;
        }

        return isValid;
    }

    private void ajouterRepas() {
        showLoading(true);

        if (selectedImageUri != null) {
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

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Récupérer l'URL de téléchargement
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(this::createRepas)
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erreur lors de la récupération de l'URL", e);
                                showLoading(false);
                                showError("Erreur lors de l'upload de l'image");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de l'upload de l'image", e);
                    showLoading(false);
                    showError("Erreur lors de l'upload de l'image");
                });
    }

    private void createRepas(Uri imageUrl) {
        String nom = editNomRepas.getText().toString().trim();
        double prix = Double.parseDouble(editPrixRepas.getText().toString().trim());
        String description = editDescriptionRepas.getText().toString().trim();
        String categorie = categorieSpinner.getSelectedItem().toString();
        String imageUrlString = imageUrl != null ? imageUrl.toString() : "";

        Repas nouveauRepas = new Repas(
                nom,
                description,
                prix,
                imageUrlString,
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
                    Toast.makeText(getContext(), "Repas ajouté avec succès!", Toast.LENGTH_SHORT).show();

                    // Navigation de retour
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Erreur lors de l'ajout du repas", e);
                    showError("Erreur lors de l'ajout du repas");
                });
    }

    private void showLoading(boolean show) {
        btnEnregistrerRepas.setEnabled(!show);
        btnChoisirImage.setEnabled(!show);

        if (show) {
            btnEnregistrerRepas.setText("Ajout en cours...");
        } else {
            btnEnregistrerRepas.setText("Ajouter un repas");
        }

        if (loadingView != null) {
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void clearForm() {
        editNomRepas.setText("");
        editPrixRepas.setText("");
        editDescriptionRepas.setText("");
        categorieSpinner.setSelection(0);
        imageRepas.setImageResource(R.drawable.placeholder_food);
        btnChoisirImage.setText("Choisir une image");
        selectedImageUri = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup si nécessaire
    }
}