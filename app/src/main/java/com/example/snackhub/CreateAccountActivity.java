package com.example.snackhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.snackhub.ui.accueil.AccueilFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {
    private TextInputEditText editTextUsername, editTextPassword, editTextPasswordConfirm,
            editTextSnackName, editTextSnackDescription;
    private MaterialButton buttonCreateAccount;
    private TextView textViewLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextUsername = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        editTextPasswordConfirm = findViewById(R.id.password_confirm);
        editTextSnackName = findViewById(R.id.nom_snack_input);
        editTextSnackDescription = findViewById(R.id.snackDescription_input);
        buttonCreateAccount = findViewById(R.id.loginButton);
        textViewLogin = findViewById(R.id.seconnecter);

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Rediriger vers la page de connexion
                startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create_account), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView seConnecter = findViewById(R.id.seconnecter);

        seConnecter.setOnClickListener(v -> {
            // Naviguer vers l'activité de création de compte
            startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
        });
    }

    private void createAccount() {
        // Récupérer les valeurs saisies
        String email = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextPasswordConfirm.getText().toString().trim();
        String snackName = editTextSnackName.getText().toString().trim();
        String snackDescription = editTextSnackDescription.getText().toString().trim();

        // Validation des champs
        if (TextUtils.isEmpty(email)) {
            editTextUsername.setError("Veuillez entrer un email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Veuillez entrer un mot de passe");
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextPasswordConfirm.setError("Les mots de passe ne correspondent pas");
            return;
        }

        if (TextUtils.isEmpty(snackName)) {
            editTextSnackName.setError("Veuillez entrer le nom du snack");
            return;
        }

        // Créer un compte avec email et mot de passe
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inscription réussie
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Enregistrer les informations du snack dans Firestore
                            saveSnackInfo(user.getUid(), snackName, snackDescription, email);

                        } else {
                            // Si l'inscription échoue, afficher un message à l'utilisateur
                            editTextSnackName.setError("Échec de la création du compte: " + task.getException().getMessage());
                            Toast.makeText(CreateAccountActivity.this, "Échec de la création du compte: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveSnackInfo(String userId, String snackName, String snackDescription, String email) {
        // Créer un objet Map pour stocker les données du snack
        Map<String, Object> snackData = new HashMap<>();
        snackData.put("snackName", snackName);
        snackData.put("description", snackDescription);
        snackData.put("email", email);
        snackData.put("userId", userId);
        snackData.put("createdAt", System.currentTimeMillis());

        // Ajouter à la collection "snacks" dans Firestore
        db.collection("snacks").document(userId)
                .set(snackData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateAccountActivity.this, "Compte créé avec succès!",
                                    Toast.LENGTH_SHORT).show();

                            // Rediriger vers la page d'accueil ou le dashboard du snack
                            Intent intent = new Intent(CreateAccountActivity.this, AccueilFragment.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CreateAccountActivity.this, "Erreur lors de l'enregistrement des données du snack",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}