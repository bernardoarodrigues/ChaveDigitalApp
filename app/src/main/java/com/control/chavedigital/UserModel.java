package com.control.chavedigital;

import androidx.annotation.Keep;

@Keep
public class UserModel {

    // Variable declaration

    // Values
    public String device, email, msgtoken, name, photo, solic;

    // Constructor
    public UserModel() {
    }

    // Object Constructor
    public UserModel(String device, String email, String msgtoken, String name, String photo, String solic) {
        this.device = device;
        this.email = email;
        this.msgtoken = msgtoken;
        this.name = name;
        this.photo = photo;
        this.solic = solic;
    }

}
