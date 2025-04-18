package com.example.project;

import java.io.Serializable; // Import Serializable
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Log;

// Implement Serializable and Comparable
public class Meal implements Serializable, Comparable<Meal> {
    private static final long serialVersionUID = 1L;
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

    public Meal(int mealId, int userId, String name, double price, int quantity, String location, int deliveryOption, String description, String imagePaths, String createdAt) {
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
        this.username = null;
        this.profilePicture = null;
        this.rating = 0.0;
    }


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

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    @Override
    public int compareTo(Meal other) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date thisDate = sdf.parse(this.createdAt);
            Date otherDate = sdf.parse(other.createdAt);
            // Compare in descending order (newest first)
            return otherDate.compareTo(thisDate);
        } catch (ParseException e) {
            Log.e("Meal", "Error parsing date", e);
            return 0; // Handle error by considering them equal
        }
    }
}