package com.example.myapplication.logIn;

import com.example.myapplication.entity.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

public class LoginClient {
    private String hostname;
    private int port;
    private String userName;
    private HashMap<String , User> friendsMap = new HashMap<>();
    private LoginClient loginClient = this;
    public LoginClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    class execute {
        public void run() {
            try {
                Socket socket = new Socket(hostname, port);

                System.out.println("Connected to the chat server");

                new Thread(new WriteThread(socket, loginClient)).start();
                new Thread(new ReadThread(socket, loginClient)).start();

            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }
//    public void execute() {
//        try {
//            Socket socket = new Socket(hostname, port);
//
//            System.out.println("Connected to the chat server");
//
//            new Thread(new WriteThread(socket, this)).start();
//            new Thread(new ReadThread(socket,this)).start();
//
//        } catch (UnknownHostException ex) {
//            System.out.println("Server not found: " + ex.getMessage());
//        } catch (IOException ex) {
//            System.out.println("I/O Error: " + ex.getMessage());
//        }
//    }

    void addFriends(String username , User user){
        System.out.println("USER NAME:" + username);
        System.out.println(user.getUid());
        friendsMap.put(username , user);
    }

//    public static void main(String[] args) {
//        LoginClient client = new LoginClient("10.0.2.2", 12345);
//
//    }
}