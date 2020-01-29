package com.example.optimas.firebaseconsole.Model;

import java.util.List;

public class SendPackageRequest {
    private String phone;
    private String name;
    private String pickup_address;
    private String drop_address;
    private String content;
    private String contact_name;
    private String contact_number;


    public SendPackageRequest() {
    }

    public SendPackageRequest(String phone, String name, String pickup_address, String drop_address, String content, String contact_name, String contact_number) {
        this.phone = phone;
        this.name = name;
        this.pickup_address = pickup_address;
        this.drop_address = drop_address;
        this.content = content;
        this.contact_name = contact_name;
        this.contact_number = contact_number;
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

    public String getPickup_address() {
        return pickup_address;
    }

    public void setPickup_address(String pickup_address) {
        this.pickup_address = pickup_address;
    }

    public String getDrop_address() {
        return drop_address;
    }

    public void setDrop_address(String drop_address) {
        this.drop_address = drop_address;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }
}
