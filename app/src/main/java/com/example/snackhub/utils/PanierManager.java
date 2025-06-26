package com.example.snackhub.utils;

import com.example.snackhub.models.Commande;
import java.util.ArrayList;
import java.util.List;

public class PanierManager {
    private static PanierManager instance;
    private List<Commande> commandeList;

    // Informations du snack actuel
    private String currentSnackId;
    private String currentSnackName;
    private String currentSnackDescription;
    private String currentSnackPhone;
    private String currentSnackAddress;
    private String currentSnackEmail;
    private String currentSnackOpeningHours;
    private String currentSnackMainImageUrl;

    private PanierManager() {
        commandeList = new ArrayList<>();
    }

    public static synchronized PanierManager getInstance() {
        if (instance == null) {
            instance = new PanierManager();
        }
        return instance;
    }

    // Méthodes pour gérer les commandes
    public void ajouterCommande(Commande commande) {
        // Vérifier si le repas existe déjà dans le PanierActivity
        for (int i = 0; i < commandeList.size(); i++) {
            Commande existante = commandeList.get(i);
            if (existante.getNomRepas().equals(commande.getNomRepas())) {
                // Mettre à jour la quantité et la description
                existante.setQuantite(existante.getQuantite() + commande.getQuantite());
                existante.setDescription(commande.getDescription());
                return;
            }
        }
        // Si le repas n'existe pas, l'ajouter
        commandeList.add(commande);
    }

    public void supprimerCommande(int position) {
        if (position >= 0 && position < commandeList.size()) {
            commandeList.remove(position);
        }
    }

    public void viderPanier() {
        commandeList.clear();
    }

    public List<Commande> getCommandes() {
        return new ArrayList<>(commandeList);
    }

    public int getNombreArticles() {
        int total = 0;
        for (Commande commande : commandeList) {
            total += commande.getQuantite();
        }
        return total;
    }

    public double getTotalPrix() {
        double total = 0;
        for (Commande commande : commandeList) {
            total += commande.getPrixTotal();
        }
        return total;
    }

    public boolean estVide() {
        return commandeList.isEmpty();
    }

    // Méthodes pour gérer les informations du snack
    public void setSnackInfo(String snackId, String snackName, String snackDescription,
                             String snackPhone, String snackAddress, String snackEmail,
                             String snackOpeningHours, String snackMainImageUrl) {
        this.currentSnackId = snackId;
        this.currentSnackName = snackName;
        this.currentSnackDescription = snackDescription;
        this.currentSnackPhone = snackPhone;
        this.currentSnackAddress = snackAddress;
        this.currentSnackEmail = snackEmail;
        this.currentSnackOpeningHours = snackOpeningHours;
        this.currentSnackMainImageUrl = snackMainImageUrl;
    }

    // Getters pour les informations du snack
    public String getCurrentSnackId() { return currentSnackId; }
    public String getCurrentSnackName() { return currentSnackName; }
    public String getCurrentSnackDescription() { return currentSnackDescription; }
    public String getCurrentSnackPhone() { return currentSnackPhone; }
    public String getCurrentSnackAddress() { return currentSnackAddress; }
    public String getCurrentSnackEmail() { return currentSnackEmail; }
    public String getCurrentSnackOpeningHours() { return currentSnackOpeningHours; }
    public String getCurrentSnackMainImageUrl() { return currentSnackMainImageUrl; }
}