import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This thread handles connection for each connected client, so the server
 * can handle multiple clients at the same time.
 */
public class UserThread extends Thread implements Serializable{
    private transient Socket socket;
    private ChatServer server;
    // private PrintWriter writer;
    private ObjectOutputStream outputStream;
    public UserThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            ObjectInputStream reader = new ObjectInputStream(input);
            Message initialHandshake;
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            // OutputStream output = socket.getOutputStream();

            // writer = new PrintWriter(outputStream, true);
            boolean usernameDuplicated;
            boolean successLogin;

            String type = (String)reader.readObject();
            String userName = (String)reader.readObject();
            String password = (String)reader.readObject();

            initialHandshake = new Message(type, userName, password);
            //userList.add(new User("local",100, socket,"123456"));
            //writer.println("Type" + initialHandshake.getMessageType()+"Username"+initialHandshake.getUsername()+"Password"+initialHandshake.getPassword());
            if(initialHandshake.getMessageType().equals("REG")) {
                //writer.println("Here");
                do {
                    usernameDuplicated = false;
                    for(User user : server.getUserList())
                    {
                        if(user.getUsername().equals(initialHandshake.getUsername()))
                        {
                            outputStream.writeObject("Username Duplicated. Try again.");
                            usernameDuplicated = true;
                            break;
                        }
                    }
                    if(usernameDuplicated) initialHandshake = (Message)reader.readObject();
                } while (usernameDuplicated);
                server.getUserList().add(new User(initialHandshake.getUsername(),server.getUid(), socket.getInetAddress(),initialHandshake.getPassword()));
                outputStream.writeObject("User Creation Successful.");
                System.out.println("User Created:   "+initialHandshake.getUsername()+ "     @   "+getCurrentTime() );
            }
            if(initialHandshake.getMessageType().equals("LOG")) {
                do {
                    successLogin = false;
                    for(User user : server.getUserList())
                    {
                        if(user.getUsername().equals(initialHandshake.getUsername()) && user.getPassword().equals(initialHandshake.getPassword()))
                        {
                            outputStream.writeObject("Login Success");
                            successLogin = true;
                            break;
                        }
                        outputStream.writeObject("Incorrect username or password, Please try again");
                    }
                    if(!successLogin) {
                        type = (String) reader.readObject();
                        userName = (String) reader.readObject();
                        password = (String) reader.readObject();

                        initialHandshake = new Message(type, userName, password);
                    }
                } while (!successLogin);
            }
            printUsers();

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage()+ " @   "+getCurrentTime() ) ;
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
        if (!server.getUserList().isEmpty()) {
            //writer.println("These users are available to connect: ");
//            try{
//                outputStream.writeObject("These users are available to connect: ");
//                //outputStream.flush();
//            } catch (IOException e){
//                e.printStackTrace();
//            }
            System.out.println("=====NOT EMPTY====");


            // Pass the size of userlist

            try {
                outputStream.writeObject(server.getUserList().size());
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(User user: server.getUserList())
            {
                //writer.println("Username: "+ user.getUsername() + " UID" + user.getUid() +" Address:"+user.getSocket().getInetAddress());
                try{
                    outputStream.writeObject("Username: "+ user.getUsername() + " UID" + user.getUid() +" Address:"+user.getInetAddress());
                    //outputStream.flush();

                    //String s = "" + user.getSocket();

                    outputStream.writeObject(user.getUsername());
                    outputStream.writeObject(user.getUid());
                    outputStream.writeObject(user.getInetAddress());
                    outputStream.writeObject(user.getPassword());

                    // outputStream.writeObject(new User(user.getUsername(),user.getUid(),user.getSocket(), user.getPassword()));
                    // outputStream.flush();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("=====EMPTY====");
            try {
                outputStream.writeObject("No other users connected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}