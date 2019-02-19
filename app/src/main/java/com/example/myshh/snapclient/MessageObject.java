package com.example.myshh.snapclient;

import java.io.Serializable;

public class MessageObject implements Serializable {

    public MessageObject() {
        this.command = 0;
        this.name = "Default";
        this.message = null;
    }

    public int command;
    public String name;
    public String message;
    public byte[] imageBytes;
}
