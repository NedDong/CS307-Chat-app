package com.cs307group9.privatechatchat.entity;

import java.io.Serializable;

public class Message implements Serializable {
    private transient String messageType;
    private transient String username;
    private transient String password;

    public Message(String messageType, String username, String password){
        this.messageType = messageType;
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getMessageType() {
        return messageType;
    }
}