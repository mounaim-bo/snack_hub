package com.example.snackhub;

public class Commande {
    private int imageResId;
    private String nomRepas;
    private String description;
    private int quantite;
    private double prixUnitaire;

    public Commande(int imageResId, String nomRepas, String description, int quantite, double prixUnitaire) {
        this.imageResId = imageResId;
        this.nomRepas = nomRepas;
        this.description = description;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getNomRepas() {
        return nomRepas;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public double getPrixTotal() {
        return prixUnitaire * quantite;
    }
}
