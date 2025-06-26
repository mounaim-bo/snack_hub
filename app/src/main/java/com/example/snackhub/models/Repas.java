package com.example.snackhub.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class Repas {
    @DocumentId
    private String id;
    private String nom;
    private String description;
    private double prix;
    private String imageUrl;
    private String categorie;
    private boolean disponible;
    private String snackId; // ID du snack propriétaire
    private long timestamp; // Pour l'ordre chronologique

    // Constructeur vide requis pour Firebase
    public Repas() {}

    // Constructeur complet
    public Repas(String nom, String description, double prix, String imageUrl,
                 String categorie, boolean disponible, String snackId) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.imageUrl = imageUrl;
        this.categorie = categorie;
        this.disponible = disponible;
        this.snackId = snackId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @PropertyName("snackId")
    public String getSnackId() {
        return snackId;
    }

    @PropertyName("snackId")
    public void setSnackId(String snackId) {
        this.snackId = snackId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Méthodes utilitaires
    public String getPrixFormate() {
        return String.format("%.2f DH", prix);
    }

    public String getStatutDisponibilite() {
        return disponible ? "Disponible" : "Indisponible";
    }
}