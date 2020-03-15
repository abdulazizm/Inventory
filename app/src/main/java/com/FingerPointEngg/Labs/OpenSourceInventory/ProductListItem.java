package com.FingerPointEngg.Labs.OpenSourceInventory;

public class ProductListItem {

    private String title,imageurl,price;

    private int btn_id;

    public ProductListItem(String imageurl, String title, int btn_id, String price) {
        super();
        this.imageurl = imageurl;
        this.title = title;
        this.btn_id = btn_id;
        this.price = price;
    }

    public String getImageUrl() {
        return imageurl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price= price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return btn_id;
    }

    public void setId(int btn_id) {
        this.btn_id = btn_id;
    }
}
