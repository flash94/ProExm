package com.example.proexm.models;

//Model Class for items
public class ModelItems {

    //variaables
    String id, itemName, itemImage, itemPrice, itemManufacturer, itemDesc, itemExp, itemMfd, itemStatus, addedTime, updatedTime;
    String daysToExpiry;


    //Alt + insert to generate constructor
    public ModelItems(String id, String itemName, String itemImage, String itemPrice, String itemManufacturer, String itemDesc, String itemExp, String itemMfd, int daysToExpiry, String itemStatus, String addedTime, String updatedTime) {
        this.id = id;
        this.itemName = itemName;
        this.itemImage = itemImage;
        this.itemPrice = itemPrice;
        this.itemManufacturer = itemManufacturer;
        this.itemDesc = itemDesc;
        this.itemExp = itemExp;
        this.itemMfd = itemMfd;
        this.daysToExpiry = Integer.toString(daysToExpiry);
        this.itemStatus = itemStatus;
        this.addedTime = addedTime;
        this.updatedTime = updatedTime;
    }

    //getters and setters Alt + insert

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemManufacturer() {
        return itemManufacturer;
    }

    public void setItemManufacturer(String itemManufacturer) {
        this.itemManufacturer = itemManufacturer;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public String getItemExp() {
        return itemExp;
    }

    public void setItemExp(String itemExp) {
        this.itemExp = itemExp;
    }

    public String getItemMfd() {
        return itemMfd;
    }

    public void setItemMfd(String itemMfd) {
        this.itemMfd = itemMfd;
    }

    public String getDaysToExpiry() {
        return daysToExpiry;
    }

    public void setDaysToExpiry(String daysToExpiry) {
        this.daysToExpiry = daysToExpiry;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(String addedTime) {
        this.addedTime = addedTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }
}
