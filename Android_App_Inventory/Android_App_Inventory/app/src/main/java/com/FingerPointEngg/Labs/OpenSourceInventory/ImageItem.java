package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String title;

    private int btn_id;

    public ImageItem(Bitmap image, String title, int btn_id) {
        super();
        this.image = image;
        this.title = title;
        this.btn_id = btn_id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
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
