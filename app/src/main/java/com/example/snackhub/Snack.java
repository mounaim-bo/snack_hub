package com.example.snackhub;

public class Snack {
    private String name;
    private String phone;
    private String description;
    private int imageResource;

    public Snack(String name, String phone, String description, int imageResource) {
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.imageResource = imageResource;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getImageResource() { return imageResource; }

    public String getPhone() { return phone; }
}
