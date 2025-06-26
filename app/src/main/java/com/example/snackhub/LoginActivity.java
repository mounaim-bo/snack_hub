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

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textViewRegister;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Récupération des vues
        editTextEmail = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.loginButton);
        textViewRegister = findViewById(R.id.create_account);

        // Configuration du bouton de connexion
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Configuration du lien pour créer un compte
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView createAccountTextView = findViewById(R.id.create_account);

        createAccountTextView.setOnClickListener(v -> {
            // Naviguer vers l'activité de création de compte
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });
    }

    private void loginUser() {
        // Récupérer les valeurs saisies
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validation des champs
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Veuillez entrer votre email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Veuillez entrer votre mot de passe");
            return;
        }

        // Connexion avec email et mot de passe
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Connexion réussie
                            Toast.makeText(LoginActivity.this, "Connexion réussie",
                                    Toast.LENGTH_SHORT).show();

                            // Rediriger vers la page d'accueil ou le dashboard du snack
                            Intent intent = new Intent(LoginActivity.this, BackofficeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Si la connexion échoue, afficher un message à l'utilisateur
                            Toast.makeText(LoginActivity.this, "Échec de la connexion: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}