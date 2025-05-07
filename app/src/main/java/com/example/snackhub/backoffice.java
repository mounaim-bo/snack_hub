package com.example.snackhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snackhub.databinding.ActivityBackofficeBinding;

public class backoffice extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityBackofficeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBackofficeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                //est ce que ça marche comme ça ?
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
}