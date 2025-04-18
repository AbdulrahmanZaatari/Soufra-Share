package com.example.project;

public class CartItem {
    private int cartId;
    private int mealId;
    private String mealName;
    private String imageUrl;
    private int quantity;
    private double price;

    public CartItem(int cartId, int mealId, String mealName, String imageUrl, int quantity, double price) {
        this.cartId = cartId;
        this.mealId = mealId;
        this.mealName = mealName;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.price = price;
    }

    public int getCartId() {
        return cartId;
    }

    public int getMealId() {
        return mealId;
    }

    public String getMealName() {
        return mealName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}