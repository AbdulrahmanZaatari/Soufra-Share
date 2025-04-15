package com.example.project;

public class SalesRecord {
    private int id;
    private String saleDate;
    private double totalSales;

    public SalesRecord(int id, String saleDate, double totalSales) {
        this.id = id;
        this.saleDate = saleDate;
        this.totalSales = totalSales;
    }

    public int getId() {
        return id;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public double getTotalSales() {
        return totalSales;
    }
}