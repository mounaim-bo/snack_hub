package com.example.snackhub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class panier extends AppCompatActivity {
    private RecyclerView recyclerViewPanier;
    private TextView totalPriceTextView;
    private RadioGroup modeCommandeRadioGroup;
    private Button validerCommandeButton;

    private List<Commande> commandeList = new ArrayList<>();
    private CommandeAdapter commandeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panier);

        recyclerViewPanier = findViewById(R.id.recyclerViewPanier);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        modeCommandeRadioGroup = findViewById(R.id.modeCommandeRadioGroup);
        validerCommandeButton = findViewById(R.id.validerCommandeButton);

        // Exemples de données pour tester
        commandeList.add(new Commande(R.drawable.food1, "Tacos", "Suppléments : fromage", 2, 35));
        commandeList.add(new Commande(R.drawable.food2, "Burger", "Sans oignons", 1, 45));
        commandeList.add(new Commande(R.drawable.food3, "Plat", "Boissans", 1, 56));
        commandeList.add(new Commande(R.drawable.food4, "Sandwish", "Boissans", 1, 35));

        commandeAdapter = new CommandeAdapter(commandeList);
        recyclerViewPanier.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPanier.setAdapter(commandeAdapter);

        calculerTotal();

        validerCommandeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = modeCommandeRadioGroup.getCheckedRadioButtonId();
                String modeCommande = "";

                if (selectedId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    modeCommande = selectedRadioButton.getText().toString();
                }

                Toast.makeText(panier.this,
                        "Commande validée en mode : " + modeCommande,
                        Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_panier), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void calculerTotal() {
        double total = 0;
        for (Commande commande : commandeList) {
            total += commande.getPrixTotal();
        }
        totalPriceTextView.setText("Total : " + total + " DH");
    }
}