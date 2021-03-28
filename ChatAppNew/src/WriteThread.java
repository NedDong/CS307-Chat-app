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
public class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;
    private ObjectOutputStream outputStream;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            outputStream = new ObjectOutputStream(output);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        /*
        Console console = System.console();
        if (console == null){
            System.out.println("console is null");
            System.exit(0);
        }*/
        //String userName = console.readLine("\nEnter your name: ");
        /*
        Scanner scan = new Scanner(System.in);
        System.out.print("\nEnter your name: ");
        String userName = scan.nextLine();
        client.setUserName(userName);
        writer.println(userName);
        String text;
        do {
            //text = console.readLine("[" + userName + "]: ");
            System.out.print("[" + userName + "]: ");
            text = scan.nextLine();
            writer.println(text);
        } while (!text.equals("bye"));
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error writing to server: " + ex.getMessage());
        }
         */
        Scanner scan = new Scanner(System.in);
        System.out.print("Log in/New user: ");
        String type = scan.nextLine();
        System.out.print("Username: ");
        String username = scan.nextLine();
        System.out.print("Password: ");
        String password = scan.nextLine();

        // Message send = new Message(type , username , password);
        try{
            User newUser = new User(username, ChatServer.getUid(), password);
            ChatServer.addUserData(newUser);
            outputStream.writeObject(type);
            outputStream.writeObject(username);
            outputStream.writeObject(password);
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}