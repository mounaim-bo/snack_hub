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
    private TextInputEditText editTextUsername, editTextPassword, editTextPasswordConfirm;
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
            startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
        });
    }

    private void createAccount() {
        // Récupérer les valeurs saisies
        String email = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextPasswordConfirm.getText().toString().trim();

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

        // Créer un compte avec email et mot de passe
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inscription réussie
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Créer le profil utilisateur de base
                            createUserProfile(user.getUid(), email);

                        } else {
                            // Si l'inscription échoue, afficher un message à l'utilisateur
                            Toast.makeText(CreateAccountActivity.this,
                                    "Échec de la création du compte: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createUserProfile(String userId, String email) {
        // Créer le document utilisateur de base dans la collection "users"
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("firstName", ""); // Sera rempli dans ModifierInfos
        userData.put("lastName", ""); // Sera rempli dans ModifierInfos
        userData.put("email", email);
        userData.put("phone", ""); // Sera rempli dans ModifierInfos
        userData.put("address", ""); // Sera rempli dans ModifierInfos
        userData.put("birthdate", ""); // Sera rempli dans ModifierInfos
        userData.put("profileImageUrl", ""); // Sera rempli dans ModifierInfos
        userData.put("snackId", ""); // Sera créé plus tard si l'utilisateur le souhaite

        db.collection("users").document(userId)
                .set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateAccountActivity.this,
                                    "Compte créé avec succès!", Toast.LENGTH_SHORT).show();

                            // Rediriger vers le BackofficeActivity
                            Intent intent = new Intent(CreateAccountActivity.this, BackofficeActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(CreateAccountActivity.this,
                                    "Erreur lors de la création du profil", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}