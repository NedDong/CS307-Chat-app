package com.cs307group9.privatechatchat.entity;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class User implements Serializable {

    private transient String username;
    private transient int uid;
    private transient String password;
    private transient InetAddress inetAddress;

    public User(String username, int uid, InetAddress inetAddress, String password)
    {
        this.username = username;
        this.password = password;
        this.password = password;
        this.uid = uid;
        this.inetAddress = inetAddress;
    }

    public int getUid() {
        return uid;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }
}