package com.example.myapplication.logIn;

import com.example.myapplication.entity.Message;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * This thread is responsible for reading user's input and send it
 * to the server.
 * It runs in an infinite loop until the user types 'bye' to quit.
 *
 * @author www.codejava.net
 */
public class WriteThread extends Thread implements Runnable {
    private Socket socket;
    private LoginClient client;
    private ObjectOutputStream outputStream;

    public WriteThread(Socket socket, LoginClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            outputStream = new ObjectOutputStream(output);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        Scanner scan = new Scanner(System.in);
        System.out.print("Log in/New user: ");
        String type = scan.nextLine();
        System.out.print("Username: ");
        String username = scan.nextLine();
        System.out.print("Password: ");
        String password = scan.nextLine();
        Message send = new Message(type , username , password);
        try{
            outputStream.writeObject(send);
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}