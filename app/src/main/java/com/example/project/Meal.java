package com.example.project;

public class Meal {
    private int mealId;
    private int userId;
    private String name;
    private double price;
    private int quantity;
    private String location;
    private int deliveryOption;
    private String description;
    private String imagePaths;
    private String createdAt;
    private String username;
    private String profilePicture;
    private double rating;

    public Meal(int mealId, int userId, String name, double price, int quantity, String location, int deliveryOption, String description, String imagePaths, String createdAt, String username, String profilePicture, double rating) {
        this.mealId = mealId;
        this.userId = userId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.location = location;
        this.deliveryOption = deliveryOption;
        this.description = description;
        this.imagePaths = imagePaths;
        this.createdAt = createdAt;
        this.username = username;
        this.profilePicture = profilePicture;
        this.rating = rating;
    }

    // Add getters for all the fields
    public int getMealId() {
        return mealId;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getLocation() {
        return location;
    }

    public int getDeliveryOption() {
        return deliveryOption;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePaths() {
        return imagePaths;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public double getRating() {
        return rating;
    }
}