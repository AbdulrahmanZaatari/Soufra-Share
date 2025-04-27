package com.example.project;

public class Order {
    private int orderId;
    private String orderDate;
    private double totalAmount;

    public Order(int orderId, String orderDate, double totalAmount) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
    }

    public int getOrderId() {
        return orderId;
    }
    public String getOrderDate() {
        return orderDate;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
}
