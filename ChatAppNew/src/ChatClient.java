import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

public class ChatClient {
    private String hostname;
    private int port;
    private String userName;
    private String password;
    private HashMap< String, User> friendsMap = new HashMap<>();
    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);

            System.out.println("Connected to the chat server");
            System.out.println("Connected to the chat server");

            new Thread(new WriteThread(socket, this)).start();
            new Thread(new ReadThread(socket,this)).start();


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
        System.out.println("USER NAME:" + username);
        System.out.println(user.getUid());
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
        String hostname;
        hostname = "localhost";
        System.out.println("port: ");
        int port = 12345;
        ChatClient client = new ChatClient(hostname, port);
        client.execute();
    }
}