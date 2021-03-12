import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * @author www.codejava.net
 */
public class ReadThread extends Thread {
    //private BufferedReader reader;
    private ObjectInputStream inputStream;
    private Socket socket;
    private ChatClient client;

    public ReadThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream input = socket.getInputStream();
            //reader = new BufferedReader(new InputStreamReader(input));
            inputStream = new ObjectInputStream(input);
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                /*
                String response = reader.readLine();
                System.out.println("\n" + response);
                // prints the username after displaying the server's message
                if (client.getUserName() != null) {
                    System.out.print("[" + client.getUserName() + "]: ");
                }*/
                Object response;
                while ((response = inputStream.readObject()) != null){
                    User friend = (User) response;
                    client.addFriends(friend.getUsername() , friend);
                    System.out.println("add friend successfully" + friend.getUsername());
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }
}