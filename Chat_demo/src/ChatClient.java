import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

public class ChatClient {
    private String hostname;
    private int port;
    private String userName;
    private HashMap<String , User> friendsMap;
    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);

            System.out.println("Connected to the chat server");

            new ReadThread(socket, this).start();
            new WriteThread(socket, this).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
    }
    void setUserName(String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return this.userName;
    }
    void addFriends(String username , User user){
        friendsMap.put(username , user);
    }
    public static void main(String[] args) {
        /*
        if (args.length < 2) return;
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        */
        Scanner scan = new Scanner(System.in);
        System.out.println("Hostname: ");
        String hostname = scan.nextLine();
        System.out.println("port: ");
        int port = scan.nextInt();
        ChatClient client = new ChatClient(hostname, port);
        client.execute();
    }
}