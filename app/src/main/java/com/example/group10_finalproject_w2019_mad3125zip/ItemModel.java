package com.example.group10_finalproject_w2019_mad3125zip;
public class ItemModel {

    String name, description, price, status,urlyoImg;

    public ItemModel(){}

    public ItemModel(String name, String description, String price, String status,String urltoimg) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        urlyoImg = urltoimg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUrlyoImg() {
        return urlyoImg;
    }

    public void setUrlyoImg(String urlyoImg) {
        this.urlyoImg = urlyoImg;
    }
}
