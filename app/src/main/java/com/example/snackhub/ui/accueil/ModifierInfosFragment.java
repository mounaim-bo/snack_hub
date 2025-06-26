package com.example.snackhub.ui.accueil;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.snackhub.models.Snack;
import com.example.snackhub.models.User;
import com.example.snackhub.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ModifierInfosFragment extends Fragment {

    private FragmentModifierInfosBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String userId;
    private User currentUser;
    private Snack userSnack;
    private Uri profileImageUri;
    private Uri snackMainImageUri;
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
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

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
        db.collection("SnacksActivity").document(snackId).get()
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

        // Charger l'image principale du snack depuis Firebase Storage
        if (userSnack.getMainImageUrl() != null && !userSnack.getMainImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(userSnack.getMainImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.images3)
                    .into(binding.snackMainImageView);
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

        // Charger l'image de profil depuis Firebase Storage
        if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(currentUser.getProfileImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.images3)
                    .into(binding.profileImageView);
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

        // Préparer les données utilisateur de base
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
            finalSnackId = db.collection("SnacksActivity").document().getId();
        } else {
            finalSnackId = null;
        }

        // Si une nouvelle image de profil a été sélectionnée, la télécharger sur Firebase Storage
        if (profileImageUri != null) {
            // Générer un nom unique pour l'image
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "profile_" + timeStamp + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference profileImageRef = storageRef.child("profile_images/" + userId + "/" + fileName);

            // Télécharger l'image
            profileImageRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obtenir l'URL de téléchargement
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImageUrl = uri.toString();
                            Log.d(TAG, "Image de profil téléchargée avec succès: " + profileImageUrl);

                            // Mettre à jour les données utilisateur avec la nouvelle URL
                            updateUserDataInFirestore(firstname, lastname, email, phone, address, birthdate, profileImageUrl, finalSnackId, shouldCreateSnack);
                        }).addOnFailureListener(e -> {
                            DialogUtils.hideLoadingDialog();
                            showError("Erreur lors de la récupération de l'URL de l'image: " + e.getMessage());
                        });
                    })
                    .addOnFailureListener(e -> {
                        DialogUtils.hideLoadingDialog();
                        showError("Erreur lors du téléchargement de l'image: " + e.getMessage());
                    });
        } else {
            // Pas de nouvelle image, conserver l'URL existante si elle existe
            String profileImageUrl = currentUser != null ? currentUser.getProfileImageUrl() : null;
            updateUserDataInFirestore(firstname, lastname, email, phone, address, birthdate, profileImageUrl, finalSnackId, shouldCreateSnack);
        }
    }

    private void updateUserDataInFirestore(String firstname, String lastname, String email, String phone,
                                           String address, String birthdate, String profileImageUrl,
                                           String finalSnackId, boolean shouldCreateSnack) {
        // Créer un objet avec les données utilisateur
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("firstName", firstname);
        userData.put("lastName", lastname);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("address", address);
        userData.put("birthdate", birthdate);
        userData.put("profileImageUrl", profileImageUrl);
        userData.put("snackId", finalSnackId);

        // Enregistrer les données dans Firestore
        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Mettre à jour l'objet utilisateur local
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
                    currentUser.setProfileImageUrl(profileImageUrl);
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

        // Récupérer les données du snack depuis les champs
        String snackName = binding.editSnackName.getText().toString().trim();
        String snackDescription = binding.editSnackDescription.getText().toString().trim();
        String snackPhone = binding.editSnackPhone.getText().toString().trim();
        String snackAddress = binding.editSnackAddress.getText().toString().trim();
        String snackEmail = binding.editSnackEmail.getText().toString().trim();
        String snackHour = binding.editSnackOpeningHours.getText().toString().trim();

        // Si une nouvelle image du snack a été sélectionnée, la télécharger sur Firebase Storage
        if (snackMainImageUri != null) {
            // Générer un nom unique pour l'image
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "snack_" + timeStamp + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference snackImageRef = storageRef.child("snack_images/" + snackId + "/" + fileName);

            // Télécharger l'image
            snackImageRef.putFile(snackMainImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obtenir l'URL de téléchargement
                        snackImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String snackImageUrl = uri.toString();
                            Log.d(TAG, "Image de snack téléchargée avec succès: " + snackImageUrl);

                            // Mettre à jour les données du snack avec la nouvelle URL
                            updateSnackDataInFirestore(snackId, snackName, snackDescription, snackPhone,
                                    snackAddress, snackEmail, snackHour, snackImageUrl);
                        }).addOnFailureListener(e -> {
                            DialogUtils.hideLoadingDialog();
                            showError("Erreur lors de la récupération de l'URL de l'image: " + e.getMessage());
                        });
                    })
                    .addOnFailureListener(e -> {
                        DialogUtils.hideLoadingDialog();
                        showError("Erreur lors du téléchargement de l'image: " + e.getMessage());
                    });
        } else {
            // Pas de nouvelle image, conserver l'URL existante si elle existe
            String snackImageUrl = userSnack != null ? userSnack.getMainImageUrl() : null;
            updateSnackDataInFirestore(snackId, snackName, snackDescription, snackPhone,
                    snackAddress, snackEmail, snackHour, snackImageUrl);
        }
    }

    private void updateSnackDataInFirestore(String snackId, String snackName, String snackDescription,
                                            String snackPhone, String snackAddress, String snackEmail,
                                            String snackHour, String snackImageUrl) {
        // Préparer les données du snack
        Map<String, Object> snackData = new HashMap<>();
        snackData.put("id", snackId);
        snackData.put("name", snackName);
        snackData.put("description", snackDescription);
        snackData.put("phone", snackPhone);
        snackData.put("address", snackAddress);
        snackData.put("email", snackEmail);
        snackData.put("openingHours", snackHour);
        snackData.put("mainImageUrl", snackImageUrl);
        snackData.put("ownerId", userId);

        // Enregistrer les données dans Firestore
        db.collection("SnacksActivity").document(snackId)
                .set(snackData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Mettre à jour l'objet snack local
                    if (userSnack == null) {
                        userSnack = new Snack();
                    }
                    userSnack.setId(snackId);
                    userSnack.setName(snackName);
                    userSnack.setDescription(snackDescription);
                    userSnack.setPhone(snackPhone);
                    userSnack.setAddress(snackAddress);
                    userSnack.setEmail(snackEmail);
                    userSnack.setOpeningHours(snackHour);
                    userSnack.setMainImageUrl(snackImageUrl);
                    userSnack.setUserId(userId);

                    DialogUtils.hideLoadingDialog();
                    Toast.makeText(requireContext(), "Informations du snack mises à jour", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la mise à jour du snack: " + e.getMessage());
                    DialogUtils.hideLoadingDialog();
                    showError("Erreur lors de la mise à jour du snack: " + e.getMessage());
                });
    }
}