package com.example.proexm.models;

public class BarcodeItemsModel {
    private int id;
    private String itemName;
    private String itemImage;
    private String barcodeCode;
    public String itemManufacturer;
    private String AddedTimeStamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getBarcodeCode() {
        return barcodeCode;
    }

    public void setBarcodeCode(String barcodeCode) {
        this.barcodeCode = barcodeCode;
    }

    public String getItemManufacturer() {
        return itemManufacturer;
    }

    public void setItemManufacturer(String itemManufacturer) {
        this.itemManufacturer = itemManufacturer;
    }

    public String getAddedTimeStamp() {
        return AddedTimeStamp;
    }

    public void setAddedTimeStamp(String addedTimeStamp) {
        AddedTimeStamp = addedTimeStamp;
    }
}
