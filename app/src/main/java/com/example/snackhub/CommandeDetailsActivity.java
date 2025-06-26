package com.example.snackhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackhub.adapters.ArticlesDetailsAdapter;
import com.example.snackhub.utils.CommandeFirebaseUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandeDetailsActivity extends AppCompatActivity {

    // Vues
    private TextView tvNumeroCommande, tvStatutCommande, tvDateCommande;
    private TextView tvClientNom, tvClientTelephone, tvClientEmail;
    private TextView tvModeCommande, tvPrixTotal, tvNotes;
    private ImageView ivModeCommande;
    private Chip chipStatut;
    private RecyclerView recyclerViewArticles;
    private FloatingActionButton fabChangerStatut;
    private ImageButton btnPartager;

    // Données
    private Map<String, Object> commande;
    private String commandeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commande_details);

        // Récupérer les données de l'intent
        Intent intent = getIntent();
        if (intent.hasExtra("commande")) {
            commande = (Map<String, Object>) intent.getSerializableExtra("commande");
            commandeId = (String) commande.get("id");
        }

        if (commande == null) {
            Toast.makeText(this, "Erreur: Commande introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupData();
        setupActions();
    }

    private void initViews() {
        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Vues principales
        tvNumeroCommande = findViewById(R.id.tvNumeroCommande);
        tvStatutCommande = findViewById(R.id.tvStatutCommande);
        tvDateCommande = findViewById(R.id.tvDateCommande);
        tvClientNom = findViewById(R.id.tvClientNom);
        tvClientTelephone = findViewById(R.id.tvClientTelephone);
        tvClientEmail = findViewById(R.id.tvClientEmail);
        tvModeCommande = findViewById(R.id.tvModeCommande);
        tvPrixTotal = findViewById(R.id.tvPrixTotal);
        tvNotes = findViewById(R.id.tvNotes);
        ivModeCommande = findViewById(R.id.ivModeCommande);
        chipStatut = findViewById(R.id.chipStatut);
        recyclerViewArticles = findViewById(R.id.recyclerViewArticles);
        fabChangerStatut = findViewById(R.id.fabChangerStatut);
        btnPartager = findViewById(R.id.btnPartager);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Détails de la commande");
        }
    }

    private void setupData() {
        // Numéro de commande
        if (commandeId != null && commandeId.length() > 8) {
            tvNumeroCommande.setText("Commande #" + commandeId.substring(0, 8));
        } else {
            tvNumeroCommande.setText("Commande #" + commandeId);
        }

        // Statut
        String statut = (String) commande.get("statut");
        tvStatutCommande.setText(statut);
        chipStatut.setText(statut);
        setStatutStyle(chipStatut, statut);

        // Date de commande
        com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) commande.get("dateCommande");
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy à HH:mm", Locale.getDefault());
            tvDateCommande.setText(format.format(date));
        }

        // Informations client
        tvClientNom.setText((String) commande.get("clientNom"));
        tvClientTelephone.setText((String) commande.get("clientTelephone"));

        String clientEmail = (String) commande.get("clientEmail");
        if (clientEmail != null && !clientEmail.trim().isEmpty()) {
            tvClientEmail.setText(clientEmail);
            tvClientEmail.setVisibility(TextView.VISIBLE);
        } else {
            tvClientEmail.setVisibility(TextView.GONE);
        }

        // Mode de commande
        String modeCommande = (String) commande.get("modeCommande");
        tvModeCommande.setText(modeCommande);

        // Icône selon le mode
        switch (modeCommande) {
            case "Livraison via livreur":
                ivModeCommande.setImageResource(R.drawable.ic_delivery);
                break;
            case "Récupérer au snack":
                ivModeCommande.setImageResource(R.drawable.ic_store);
                break;
            case "Prendre à table":
                ivModeCommande.setImageResource(R.drawable.ic_table);
                break;
            default:
                ivModeCommande.setImageResource(R.drawable.ic_delivery);
        }

        // Prix total
        Double prixTotal = (Double) commande.get("prixTotal");
        if (prixTotal != null) {
            tvPrixTotal.setText(String.format(Locale.getDefault(), "%.0f DH", prixTotal));
        }

        // Notes
        String notes = (String) commande.get("notes");
        if (notes != null && !notes.trim().isEmpty()) {
            tvNotes.setText(notes);
            tvNotes.setVisibility(TextView.VISIBLE);
        } else {
            tvNotes.setVisibility(TextView.GONE);
        }

        // Articles
        setupArticlesRecyclerView();

        // FAB selon le statut
        setupFabStatut(statut);
    }

    private void setupArticlesRecyclerView() {
        List<Map<String, Object>> articles = (List<Map<String, Object>>) commande.get("articles");

        if (articles != null && !articles.isEmpty()) {
            ArticlesDetailsAdapter adapter = new ArticlesDetailsAdapter(articles);
            recyclerViewArticles.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewArticles.setAdapter(adapter);
        }
    }

    private void setupActions() {
        // FAB changer statut
        fabChangerStatut.setOnClickListener(v -> changerStatutCommande());

        // Bouton partager
        btnPartager.setOnClickListener(v -> partagerCommande());
    }

    private void setupFabStatut(String statut) {
        switch (statut) {
            case "En attente":
                fabChangerStatut.setImageResource(R.drawable.ic_play);
                fabChangerStatut.setVisibility(FloatingActionButton.VISIBLE);
                break;
            case "En préparation":
                fabChangerStatut.setImageResource(R.drawable.ic_check);
                fabChangerStatut.setVisibility(FloatingActionButton.VISIBLE);
                break;
            case "Prête":
                fabChangerStatut.setImageResource(R.drawable.ic_delivery);
                fabChangerStatut.setVisibility(FloatingActionButton.VISIBLE);
                break;
            case "Livrée":
                fabChangerStatut.setVisibility(FloatingActionButton.GONE);
                break;
            default:
                fabChangerStatut.setVisibility(FloatingActionButton.VISIBLE);
        }
    }

    private void changerStatutCommande() {
        String statutActuel = (String) commande.get("statut");
        String nouveauStatut = getNextStatut(statutActuel);

        CommandeFirebaseUtils.updateStatutCommande(commandeId, nouveauStatut,
                new CommandeFirebaseUtils.CommandeCallback() {
                    @Override
                    public void onSuccess(String commandeId) {
                        runOnUiThread(() -> {
                            // Mettre à jour localement
                            commande.put("statut", nouveauStatut);

                            // Rafraîchir l'affichage
                            tvStatutCommande.setText(nouveauStatut);
                            chipStatut.setText(nouveauStatut);
                            setStatutStyle(chipStatut, nouveauStatut);
                            setupFabStatut(nouveauStatut);

                            Toast.makeText(CommandeDetailsActivity.this,
                                    "Statut mis à jour: " + nouveauStatut,
                                    Toast.LENGTH_SHORT).show();

                            // Retourner le résultat à l'activité précédente
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("commande_updated", true);
                            resultIntent.putExtra("nouveau_statut", nouveauStatut);
                            resultIntent.putExtra("commande_id", commandeId);
                            setResult(RESULT_OK, resultIntent);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(CommandeDetailsActivity.this,
                                    "Erreur: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private String getNextStatut(String statutActuel) {
        switch (statutActuel) {
            case "En attente":
                return "En préparation";
            case "En préparation":
                return "Prête";
            case "Prête":
                return "Livrée";
            default:
                return statutActuel;
        }
    }

    private void setStatutStyle(Chip chip, String statut) {
        switch (statut) {
            case "En attente":
                chip.setChipBackgroundColorResource(R.color.orange_light);
                chip.setTextColor(Color.WHITE);
                break;
            case "En préparation":
                chip.setChipBackgroundColorResource(R.color.blue_light);
                chip.setTextColor(Color.WHITE);
                break;
            case "Prête":
                chip.setChipBackgroundColorResource(R.color.green_light);
                chip.setTextColor(Color.WHITE);
                break;
            case "Livrée":
                chip.setChipBackgroundColorResource(R.color.gray_dark);
                chip.setTextColor(Color.WHITE);
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.gray_dark);
                chip.setTextColor(Color.WHITE);
        }
    }

    private void partagerCommande() {
        StringBuilder message = new StringBuilder();
        message.append("📋 DÉTAILS DE LA COMMANDE\n\n");

        // Numéro et statut
        message.append("🔢 Numéro: #").append(commandeId != null && commandeId.length() > 8 ?
                commandeId.substring(0, 8) : commandeId).append("\n");
        message.append("📊 Statut: ").append(commande.get("statut")).append("\n\n");

        // Client
        message.append("👤 CLIENT\n");
        message.append("Nom: ").append(commande.get("clientNom")).append("\n");
        message.append("Tél: ").append(commande.get("clientTelephone")).append("\n");
        String email = (String) commande.get("clientEmail");
        if (email != null && !email.trim().isEmpty()) {
            message.append("Email: ").append(email).append("\n");
        }
        message.append("\n");

        // Mode de commande
        message.append("🚚 Mode: ").append(commande.get("modeCommande")).append("\n\n");

        // Articles
        message.append("🍽️ ARTICLES\n");
        List<Map<String, Object>> articles = (List<Map<String, Object>>) commande.get("articles");
        if (articles != null) {
            for (Map<String, Object> article : articles) {
                message.append("• ").append(article.get("nomRepas"))
                        .append(" x").append(article.get("quantite"))
                        .append(" - ").append(String.format(Locale.getDefault(), "%.0f DH",
                                (Double) article.get("prixTotal"))).append("\n");
            }
        }

        // Total
        message.append("\n💰 TOTAL: ").append(String.format(Locale.getDefault(), "%.0f DH",
                (Double) commande.get("prixTotal")));

        // Notes
        String notes = (String) commande.get("notes");
        if (notes != null && !notes.trim().isEmpty()) {
            message.append("\n\n📝 Notes: ").append(notes);
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Détails de la commande #" +
                (commandeId != null && commandeId.length() > 8 ? commandeId.substring(0, 8) : commandeId));

        startActivity(Intent.createChooser(shareIntent, "Partager la commande"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}