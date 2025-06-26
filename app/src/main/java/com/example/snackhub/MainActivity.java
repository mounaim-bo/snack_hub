package com.example.snackhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.snackhub.utils.UserManager;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView commander = findViewById(R.id.btnCommander);
        //TextView commander_ = findViewById(R.id.secondcommande);
        TextView seconnecter = findViewById(R.id.seconnecter);

        commander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SnacksActivity.class);
                startActivity(intent);
            }
        });

        /*commander_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SnacksActivity.class);
                startActivity(intent);
            }
        });*/

        seconnecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Initialiser UserManager
        UserManager.getInstance().init(this);

        // IMPORTANT: Définir le snackId du snack connecté
        // Remplacez par votre vraie logique de récupération du snackId
        UserManager.getInstance().setSnackId("mSwHNEUQW1G0dEqSSuOS"); // L'ID visible dans Firebase

    }
}