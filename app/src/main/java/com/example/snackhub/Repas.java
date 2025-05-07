package com.example.snackhub;

public class Repas {

    private String id;
    private String nom;
    private String description;
    private double prix;
    private int imageResource;

    private String categorie;

    private boolean available;

    private String imagePath;

    public Repas(long l, String nom, double prixDouble, String categorie, String description, Boolean available) {
    }

    public Repas(String id, String nom, String description, double prix, int imageResource, String categorie, boolean available) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.imageResource = imageResource;
        this.categorie = categorie;
        this.available = available;
    }

    public Repas(String nom, String description, double prix, int imageResource, String categorie, boolean available) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.imageResource = imageResource;
        this.categorie = categorie;
        this.available = available;
    }

    // Constructeur pour images externes (URI)
    public Repas(String id, String nom, double prix, String categorie, String description, String imagePath) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.categorie = categorie;
        this.description = description;
        this.imagePath = imagePath;
        this.available = true; // Par défaut
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

    public String getCategorie() {
        return categorie;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "id='" + id + '\'' +
                ", name='" + nom + '\'' +
                ", price=" + prix +
                ", category='" + categorie + '\'' +
                ", available=" + available +
                '}';
    }
}
