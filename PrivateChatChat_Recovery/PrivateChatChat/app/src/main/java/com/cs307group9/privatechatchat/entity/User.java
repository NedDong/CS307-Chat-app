package com.cs307group9.privatechatchat.entity;

import android.view.View;
import android.view.ViewGroup;

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
        this.uid = uid;
        this.inetAddress = inetAddress;
        this.password = password;
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

    public void printUser() {
        System.out.printf(" NAME[%s] | ID[%d] | ADDR[%s] | PSW[%s]\n", username, uid, inetAddress, password);
    }
}