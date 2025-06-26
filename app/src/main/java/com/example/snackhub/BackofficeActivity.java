package com.example.snackhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.snackhub.databinding.ActivityBackofficeBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class BackofficeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityBackofficeBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBackofficeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialiser Firebase Auth seulement
        mAuth = FirebaseAuth.getInstance();

        // Vérifier si l'utilisateur est connecté
        if (mAuth.getCurrentUser() == null) {
            // Rediriger vers login si pas connecté
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Configuration de la toolbar et navigation
        setSupportActionBar(binding.appBarBackoffice.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Configuration de la navigation
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_accueil, R.id.nav_repas, R.id.nav_commandes,R.id.nav_params)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Configuration du menu de navigation
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_share) {
                // PARTAGE
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = "Découvrez notre super application qui vous permet de commander vos repas préférés dans votre snack préféré.";
                String shareSubject = "SnackHub";

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                startActivity(Intent.createChooser(shareIntent, "Partager via"));

            } else if (id == R.id.nav_logout) {
                // DÉCONNEXION
                mAuth.signOut();
                Intent intent = new Intent(BackofficeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else if (id == R.id.ajoutRepasFragment) {
                Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice)
                        .navigate(R.id.ajoutRepasFragment);

            } else if (id == R.id.nav_contact) {
                Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice)
                        .navigate(R.id.action_to_contactNousFragment);

            } else {
                NavController navController1 = Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice);
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController1);
                if (handled) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                return handled;
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.backoffice, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_backoffice);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding = null;
        }
    }
}