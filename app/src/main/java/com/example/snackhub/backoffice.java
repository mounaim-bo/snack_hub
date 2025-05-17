package com.example.snackhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snackhub.databinding.ActivityBackofficeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class backoffice extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityBackofficeBinding binding;

    private TextView text_email, textViewSnackName, textViewSnackDescription;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBackofficeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialiser Firebase Auth et Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Récupération des vues
        text_email = findViewById(R.id.text_email);
        textViewSnackName = findViewById(R.id.nom_snack_home);
        textViewSnackDescription = findViewById(R.id.snackDescriptionText);

        // Vérifier si l'utilisateur est connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadSnackData(currentUser.getUid());
        }

        setSupportActionBar(binding.appBarBackoffice.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_accueil, R.id.nav_repas, R.id.nav_params)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_share) {
                //PARTAGE :
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = "Découvrez notre super application qui vous permet de commander vos repas préférés dans votre snack préféré.";
                String shareSubject = "SnackHub";

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                startActivity(Intent.createChooser(shareIntent, "Partager via"));

            } else if (id == R.id.nav_logout) {
                // CODE DE DÉCONNEXION
            } else if (id == R.id.ajoutRepasFragment) {
                Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice).navigate(R.id.ajoutRepasFragment);
            } else if (id == R.id.nav_contact) {
                Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice).navigate(R.id.action_to_contactNousFragment);
            } else {
                NavController navController1 = Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice);
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController1);
                if (handled) {
                    DrawerLayout drawer1 = binding.drawerLayout;
                    drawer1.closeDrawer(GravityCompat.START);
                }
                return handled;
            }
            DrawerLayout drawer2 = binding.drawerLayout;
            drawer2.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.backoffice, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void loadSnackData(String userId) {
        db.collection("snacks").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String snackName = document.getString("snackName");
                                String snackDescription = document.getString("description");
                                String email = document.getString("email");

                                // Afficher les informations
                                text_email.setText(email);
                                textViewSnackName.setText("Nom du snack: " + snackName);
                                textViewSnackDescription.setText("Description: " + snackDescription);
                            }
                        }
                    }
                });
    }
}