package com.control.chavedigital;

import androidx.annotation.Keep;

@Keep
public class DeviceModel {

    // Variable declaration

    // Values
    public String admin, config, email, lasttime, model, msgtokens, name, reset, solic, state, users;

    // Constructor
    public DeviceModel() {
    }

    // Object Constructor
    public DeviceModel(String admin, String config, String email, String lasttime, String model, String msgtokens, String name, String reset, String solic, String state, String users) {
        this.admin = admin;
        this.config = config;
        this.email = email;
        this.lasttime = lasttime;
        this.model = model;
        this.msgtokens = msgtokens;
        this.name = name;
        this.reset = reset;
        this.solic = solic;
        this.state = state;
        this.users = users;
    }

}
