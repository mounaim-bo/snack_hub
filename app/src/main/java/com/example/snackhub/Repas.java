package com.example.snackhub;

public class Repas {
    private String nom;
    private String description;
    private double prix;
    private int imageResource;

    public Repas() {
    }

    public Repas(String nom, String description, double prix, int imageResource) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.imageResource = imageResource;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public double getPrix() {
        return prix;
    }

    public int getImageResource() {
        return imageResource;
    }
}
