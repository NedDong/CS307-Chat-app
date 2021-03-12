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
    private Socket socket;
    private ChatServer server;
    private PrintWriter writer;
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
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            boolean usernameDuplicated;
            boolean successLogin;
            initialHandshake = (Message)reader.readObject();
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
                            writer.println("Username Duplicated. Try again.");
                            usernameDuplicated = true;
                            break;
                        }
                    }
                    if(usernameDuplicated) initialHandshake = (Message)reader.readObject();
                } while (usernameDuplicated);
                server.getUserList().add(new User(initialHandshake.getUsername(),server.getUid(), socket,initialHandshake.getPassword()));
                writer.println("User Creation Successful.");
                System.out.println("User Created:   "+initialHandshake.getUsername()+ "     @   "+getCurrentTime() );
            }
            if(initialHandshake.getMessageType().equals("LOG")) {
                do {
                    successLogin = false;
                    for(User user : server.getUserList())
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
            try{
                outputStream.writeChars("These users are available to connect: ");
            } catch (IOException e){
                e.printStackTrace();
            }
            for(User user: server.getUserList())
            {
                //writer.println("Username: "+ user.getUsername() + " UID" + user.getUid() +" Address:"+user.getSocket().getInetAddress());
                try{
                    outputStream.writeChars("Username: "+ user.getUsername() + " UID" + user.getUid() +" Address:"+user.getSocket().getInetAddress());
                    outputStream.flush();
                    outputStream.writeObject(user);
                    outputStream.flush();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        } else {
            writer.println("No other users connected");
        }
    }
}
