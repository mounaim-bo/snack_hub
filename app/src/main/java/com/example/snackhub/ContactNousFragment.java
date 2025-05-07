package com.example.snackhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ContactNousFragment extends Fragment {

    private TextInputEditText editNomPrenom, editEmail, editTelephone, editMessage;
    private AutoCompleteTextView dropdownSujet;
    private MaterialButton btnEnvoyerMessage;

    public ContactNousFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflater le layout pour ce fragment
        return inflater.inflate(R.layout.contact_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        editNomPrenom = view.findViewById(R.id.edit_nom_prenom);
        editEmail = view.findViewById(R.id.edit_email);
        editTelephone = view.findViewById(R.id.edit_telephone);
        editMessage = view.findViewById(R.id.edit_message);
        dropdownSujet = view.findViewById(R.id.dropdown_sujet);
        btnEnvoyerMessage = view.findViewById(R.id.btn_envoyer_message);

        // Configuration du dropdown pour le sujet
        String[] sujets = new String[]{"Question générale", "Support technique", "Suggestion", "Réclamation", "Partenariat"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                sujets);
        dropdownSujet.setAdapter(adapter);

        // Configurer le bouton d'envoi
        btnEnvoyerMessage.setOnClickListener(v -> envoyerMessage());
    }

    private void envoyerMessage() {
        // Récupérer les valeurs
        String nomPrenom = editNomPrenom.getText() != null ? editNomPrenom.getText().toString() : "";
        String email = editEmail.getText() != null ? editEmail.getText().toString() : "";
        String telephone = editTelephone.getText() != null ? editTelephone.getText().toString() : "";
        String sujet = dropdownSujet.getText().toString();
        String message = editMessage.getText() != null ? editMessage.getText().toString() : "";

        // Validation simple
        if (nomPrenom.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation email basique
        if (!email.contains("@") || !email.contains(".")) {
            Toast.makeText(requireContext(), "Veuillez entrer une adresse email valide", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Envoyer le message au serveur
        // Ici, vous ajouterez le code pour envoyer les données à votre serveur

        // Afficher un message de confirmation
        Toast.makeText(requireContext(), "Message envoyé avec succès", Toast.LENGTH_SHORT).show();

        // Réinitialiser les champs
        editNomPrenom.setText("");
        editEmail.setText("");
        editTelephone.setText("");
        dropdownSujet.setText("");
        editMessage.setText("");
    }
}