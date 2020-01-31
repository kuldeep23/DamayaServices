package com.example.optimas.firebaseconsole.Model;

public class UploadImageList {

    private String phone;
    private String name;
    private String comments;
    private String category;
    private String imageURL;
    private String status;


    public UploadImageList() {
    }

    public UploadImageList(String phone, String name, String comments, String category, String imageURL, String status) {
        this.phone = phone;
        this.name = name;
        this.comments = comments;
        this.category = category;
        this.imageURL = imageURL;
        this.status = status;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
