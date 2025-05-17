package com.example.snackhub.ui.accueil;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.snackhub.R;
import com.example.snackhub.databinding.FragmentModifierInfosBinding;
import com.example.snackhub.model.Snack;
import com.example.snackhub.model.User;
import com.example.snackhub.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ModifierInfosFragment extends Fragment {

    private FragmentModifierInfosBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private User currentUser;
    private Snack userSnack;
    private Uri profileImageUri;
    private Uri snackMainImageUri;
    private String profileImageLocalPath;
    private String snackImageLocalPath;
    private static final String TAG = "ModifierInfosFragment";

    public ModifierInfosFragment() {}

    private final ActivityResultLauncher<Intent> profileImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    profileImageUri = result.getData().getData();
                    Glide.with(requireContext())
                            .load(profileImageUri)
                            .centerCrop()
                            .into(binding.profileImageView);
                }
            });

    private final ActivityResultLauncher<Intent> snackImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    snackMainImageUri = result.getData().getData();
                    Glide.with(requireContext())
                            .load(snackMainImageUri)
                            .centerCrop()
                            .into(binding.snackMainImageView);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialisation des variables Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            loadUserData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentModifierInfosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.editUserBirthdate.setOnClickListener(v -> showDatePickerDialog());

        binding.changeProfileImageButton.setOnClickListener(v -> openImagePicker(profileImagePickerLauncher));
        binding.changeSnackImageButton.setOnClickListener(v -> openImagePicker(snackImagePickerLauncher));

        binding.changePasswordButton.setOnClickListener(v -> navigateToChangePassword());

        binding.saveButton.setOnClickListener(v -> saveUserData());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dateString = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    binding.editUserBirthdate.setText(dateString);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void openImagePicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    private void navigateToChangePassword() {
        Toast.makeText(requireContext(), "Fonctionnalité à implémenter", Toast.LENGTH_SHORT).show();
    }

    private void loadUserData() {
        DialogUtils.showLoadingDialog(requireContext(), "Chargement de vos données...");

        // Charger les données utilisateur depuis Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            populateUserFields();

                            if (currentUser.getSnackId() != null && !currentUser.getSnackId().isEmpty()) {
                                loadSnackData(currentUser.getSnackId());
                            } else {
                                DialogUtils.hideLoadingDialog();
                            }
                        } else {
                            DialogUtils.hideLoadingDialog();
                            showError("Erreur lors du chargement des données utilisateur");
                        }
                    } else {
                        DialogUtils.hideLoadingDialog();
                        showError("Utilisateur non trouvé");
                    }
                })
                .addOnFailureListener(e -> {
                    DialogUtils.hideLoadingDialog();
                    showError("Erreur: " + e.getMessage());
                });
    }

    private void loadSnackData(String snackId) {
        db.collection("snacks").document(snackId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userSnack = documentSnapshot.toObject(Snack.class);
                        if (userSnack != null) {
                            populateSnackFields();
                        }
                    }
                    DialogUtils.hideLoadingDialog();
                })
                .addOnFailureListener(e -> {
                    DialogUtils.hideLoadingDialog();
                    showError("Erreur lors du chargement des données du snack: " + e.getMessage());
                });
    }

    private void populateSnackFields() {
        if (userSnack == null) return;

        // Remplir les champs texte
        binding.editSnackName.setText(userSnack.getName());
        binding.editSnackDescription.setText(userSnack.getDescription());
        binding.editSnackPhone.setText(userSnack.getPhone());
        binding.editSnackAddress.setText(userSnack.getAddress());
        binding.editSnackEmail.setText(userSnack.getEmail());
        binding.editSnackOpeningHours.setText(userSnack.getOpeningHours());

        // Charger l'image principale du snack (si chemin local disponible)
        if (userSnack.getMainImageUrl() != null && !userSnack.getMainImageUrl().isEmpty()) {
            // Vérifier si c'est un URI local ou une URL distante
            if (userSnack.getMainImageUrl().startsWith("http")) {
                // C'est une URL distante
                Glide.with(requireContext())
                        .load(userSnack.getMainImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.images3)
                        .into(binding.snackMainImageView);
            } else {
                // C'est un chemin local
                File imageFile = new File(userSnack.getMainImageUrl());
                if (imageFile.exists()) {
                    Glide.with(requireContext())
                            .load(imageFile)
                            .centerCrop()
                            .placeholder(R.drawable.images3)
                            .into(binding.snackMainImageView);

                    // Stocker le chemin existant
                    snackImageLocalPath = userSnack.getMainImageUrl();
                }
            }
        }
    }

    private void populateUserFields() {
        if (currentUser == null) return;

        binding.editUserFirstName.setText(currentUser.getFirstName());
        binding.editUserLastName.setText(currentUser.getLastName());
        binding.editUserEmail.setText(currentUser.getEmail());
        binding.editUserPhone.setText(currentUser.getPhone());
        binding.editUserAddress.setText(currentUser.getAddress());
        binding.editUserBirthdate.setText(currentUser.getBirthdate());

        // Charger l'image de profil si disponible
        if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            // Vérifier si c'est un URI local ou une URL distante
            if (currentUser.getProfileImageUrl().startsWith("http")) {
                Glide.with(requireContext())
                        .load(currentUser.getProfileImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.images3)
                        .into(binding.profileImageView);
            } else {
                // C'est un chemin local
                File imageFile = new File(currentUser.getProfileImageUrl());
                if (imageFile.exists()) {
                    Glide.with(requireContext())
                            .load(imageFile)
                            .centerCrop()
                            .placeholder(R.drawable.images3)
                            .into(binding.profileImageView);

                    // Stocker le chemin existant
                    profileImageLocalPath = currentUser.getProfileImageUrl();
                }
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void saveUserData() {
        if (userId == null) {
            showError("Erreur: utilisateur non connecté");
            return;
        }

        DialogUtils.showLoadingDialog(requireContext(), "Enregistrement des modifications...");

        // Si une nouvelle image de profil a été sélectionnée, la sauvegarder localement
        if (profileImageUri != null) {
            try {
                profileImageLocalPath = saveImageLocally(profileImageUri, "profile_" + userId);
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de l'enregistrement de l'image de profil", e);
                showError("Erreur lors de l'enregistrement de l'image de profil: " + e.getMessage());
                DialogUtils.hideLoadingDialog();
                return;
            }
        }

        // Sauvegarder les données utilisateur
        String firstname = binding.editUserFirstName.getText().toString().trim();
        String lastname = binding.editUserLastName.getText().toString().trim();
        String email = binding.editUserEmail.getText().toString().trim();
        String phone = binding.editUserPhone.getText().toString().trim();
        String address = binding.editUserAddress.getText().toString().trim();
        String birthdate = binding.editUserBirthdate.getText().toString().trim();

        // Conserver ou mettre à jour le snackId
        final String currentSnackId = currentUser != null ? currentUser.getSnackId() : null;
        final boolean shouldCreateSnack = binding.editSnackName.getText().toString().trim().length() > 0;

        // Préparer le snackId final
        final String finalSnackId;

        if (currentSnackId != null && !currentSnackId.isEmpty()) {
            finalSnackId = currentSnackId;
        } else if (shouldCreateSnack) {
            finalSnackId = db.collection("snacks").document().getId();
        } else {
            finalSnackId = null;
        }

        // Créer un objet User avec toutes les données
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("firstName", firstname);
        userData.put("lastName", lastname);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("address", address);
        userData.put("birthdate", birthdate);
        userData.put("profileImageUrl", profileImageLocalPath);
        userData.put("snackId", finalSnackId);

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (currentUser == null) {
                        currentUser = new User();
                        currentUser.setId(userId);
                    }
                    currentUser.setFirstName(firstname);
                    currentUser.setLastName(lastname);
                    currentUser.setEmail(email);
                    currentUser.setPhone(phone);
                    currentUser.setAddress(address);
                    currentUser.setBirthdate(birthdate);
                    currentUser.setProfileImageUrl(profileImageLocalPath);
                    currentUser.setSnackId(finalSnackId);

                    if (shouldCreateSnack && finalSnackId != null) {
                        saveSnackData(finalSnackId);
                    } else {
                        DialogUtils.hideLoadingDialog();
                        Toast.makeText(requireContext(), "Informations utilisateur mises à jour", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    DialogUtils.hideLoadingDialog();
                    showError("Erreur lors de la mise à jour des informations utilisateur: " + e.getMessage());
                });
    }

    private void saveSnackData(String snackId) {
        if (snackId == null || snackId.isEmpty()) {
            DialogUtils.hideLoadingDialog();
            showError("ID de snack invalide");
            return;
        }

        // Si une nouvelle image du snack a été sélectionnée, la sauvegarder localement
        if (snackMainImageUri != null) {
            try {
                snackImageLocalPath = saveImageLocally(snackMainImageUri, "snack_" + snackId);
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de l'enregistrement de l'image du snack", e);
                showError("Erreur lors de l'enregistrement de l'image du snack: " + e.getMessage());
                DialogUtils.hideLoadingDialog();
                return;
            }
        }

        // Récupérer les données du snack depuis les champs
        String snackName = binding.editSnackName.getText().toString().trim();
        String snackDescription = binding.editSnackDescription.getText().toString().trim();
        String snackPhone = binding.editSnackPhone.getText().toString().trim();
        String snackAddress = binding.editSnackAddress.getText().toString().trim();
        String snackEmail = binding.editSnackEmail.getText().toString().trim();
        String snackHour = binding.editSnackOpeningHours.getText().toString().trim();

        // Préparer les données du snack
        Map<String, Object> snackData = new HashMap<>();
        snackData.put("id", snackId);
        snackData.put("name", snackName);
        snackData.put("description", snackDescription);
        snackData.put("phone", snackPhone);
        snackData.put("address", snackAddress);
        snackData.put("email", snackEmail);
        snackData.put("openingHours", snackHour);
        snackData.put("mainImageUrl", snackImageLocalPath); // Utiliser le chemin local
        snackData.put("ownerId", userId);

        db.collection("snacks").document(snackId)
                .set(snackData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    updateLocalSnackObject(snackId, snackData);
                    DialogUtils.hideLoadingDialog();
                    Toast.makeText(requireContext(), "Informations du snack mises à jour", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la mise à jour du snack: " + e.getMessage());
                    DialogUtils.hideLoadingDialog();
                    showError("Erreur lors de la mise à jour du snack: " + e.getMessage());
                });
    }

    private void updateLocalSnackObject(String snackId, Map<String, Object> snackData) {
        if (userSnack == null) {
            userSnack = new Snack();
        }

        userSnack.setId(snackId);
        userSnack.setName((String) snackData.get("name"));
        userSnack.setDescription((String) snackData.get("description"));
        userSnack.setPhone((String) snackData.get("phone"));
        userSnack.setAddress((String) snackData.get("address"));
        userSnack.setEmail((String) snackData.get("email"));
        userSnack.setOpeningHours((String) snackData.get("openingHours"));
        userSnack.setMainImageUrl((String) snackData.get("mainImageUrl"));
        userSnack.setUserId((String) snackData.get("ownerId"));
    }

    /**
     * Sauvegarde une image localement sur l'appareil de l'utilisateur
     * @param imageUri URI de l'image à sauvegarder
     * @param name Nom à donner au fichier
     * @return Chemin absolu du fichier sauvegardé
     * @throws IOException En cas d'erreur d'écriture
     */
    private String saveImageLocally(Uri imageUri, String name) throws IOException {
        Context context = requireContext();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = name + "_" + timeStamp + ".jpg";

        File destFile = new File(context.getFilesDir(), fileName);

        // Enregistrer dans le dossier Pictures (nécessite des permissions)
        // File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // File destFile = new File(storageDir, fileName);

        try (InputStream in = context.getContentResolver().openInputStream(imageUri);
             OutputStream out = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[8192];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            out.flush();

            Log.d(TAG, "Image sauvegardée avec succès à : " + destFile.getAbsolutePath());

            return destFile.getAbsolutePath();
        }
    }
}