package net.codejava.networking.chat.server;

import java.net.Socket;

public class User {

    private String username;
    private int uid;
    private Socket socket;
    private String password;

    public User(String username, int uid, Socket socket, String password)
    {
        this.username = username;
        this.password = password;
        this.socket = socket;
        this.password = password;
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
