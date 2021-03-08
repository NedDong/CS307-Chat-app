package net.codejava.networking.chat.server;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This thread handles connection for each connected client, so the server
 * can handle multiple clients at the same time.
 *
 * @author www.codejava.net
 */
public class UserThread extends Thread {
    private Socket socket;
    private ChatServer server;
    private PrintWriter writer;
    public List<User> userList;
    public UserThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            ObjectInputStream reader = new ObjectInputStream(input);
            Message initialHandshake;
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            boolean usernameDuplicated;
            boolean successLogin;
            initialHandshake = (Message)reader.readObject();
            if(initialHandshake.getMessageType()=="REG") {
                do {
                    usernameDuplicated = false;
                    for(User user : userList)
                    {
                        if(user.getUsername().equals(initialHandshake.getUsername()))
                        {
                            writer.println("Username Duplicated. Try again.");
                            usernameDuplicated = true;
                            break;
                        }
                    }
                    if(usernameDuplicated) initialHandshake = (Message)reader.readObject();
                } while (usernameDuplicated);
                userList.add(new User(initialHandshake.getUsername(),server.getUid(), socket,initialHandshake.getPassword()));
                writer.println("User Creation Successful.");
            }
            if(initialHandshake.getMessageType()=="LOG") {
                do {
                    successLogin = false;
                    for(User user : userList)
                    {
                        if(user.getUsername().equals(initialHandshake.getUsername()) && user.getPassword().equals(initialHandshake.getPassword()))
                        {
                            writer.println("Login Success");
                            successLogin = true;
                            break;
                        }
                        writer.println("Incorrect username or password, Please try again");
                    }
                    if(!successLogin) initialHandshake = (Message)reader.readObject();
                } while (!successLogin);
            }
            //userList.add(User())
            //server.addUserName(initialHandshake.getUsername());

//            String serverMessage = "New user connected: " + initialHandshake.getUsername();
//            //server.broadcast(serverMessage, this);
            printUsers();
//            String clientMessage;
//
//            do {
//                clientMessage = reader.readLine();
//                serverMessage = "[" + initialHandshake.getUsername() + "] @"+ getCurrentTime() + " :" + clientMessage;
//                server.broadcast(serverMessage, this);
//
//            } while (!clientMessage.equals("bye"));
//
//            server.removeUser(initialHandshake.getUsername(), this);
//            socket.close();
//
//            serverMessage = initialHandshake.getUsername() + " has quitted.";
//            server.broadcast(serverMessage, this);

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    public String getCurrentTime()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
    /**
     * Sends a list of online users to the newly connected user.
     */
    void printUsers() {
        if (!userList.isEmpty()) {

            writer.println("These users are available to connect " + userList);
        } else {
            writer.println("No other users connected");
        }
    }

    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        writer.println(message);
    }
}
