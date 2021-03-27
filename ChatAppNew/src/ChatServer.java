import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This is the chat server program.
 */
public class ChatServer implements Serializable {
    private transient int port;
    private transient Set<String> userNames = new HashSet<>();
    private transient Set<UserThread> userThreads = new HashSet<>();
    int uid = 0;
    public transient List<User> userList = new ArrayList<>();
    public ChatServer(int port) {
        this.port = port;
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Chat Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("New user connected        @   "+getCurrentTime());
                for(User user : userList)
                {
                    System.out.println("Username: "+ user.getUsername() + " UID" + user.getUid() +" Address:"+user.getInetAddress());
                }
                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();

            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage()+ " @    "+getCurrentTime() );
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(12345);
        server.execute();
    }

    public String getCurrentTime()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public void changeUsername(String previousUsername, String newUsername)
    {
        userNames.remove(previousUsername);
        userNames.add(newUsername);
        for(User user : userList)
        {
            if(user.getUsername().equals(previousUsername))
            {
                user.setUsername(newUsername);
                System.out.println("User"+newUsername+"changed.");
                break;
            }
        }
    }

    /**
     * When a client is disconneted, removes the associated username and UserThread
     */
    public void removeUser(String username)
    {
        userNames.remove(username);
        for(User user : userList)
        {
            if(user.getUsername().equals(username))
            {
                userList.remove(user);
                System.out.println("User"+username+"removed.");
                break;
            }
        }
    }
    public void addToUserList(User user)
    {
        userList.add(user);
    }

    public List<User> getUserList(){
        return userList;
    }

    int getUid()
    {
        uid++;
        return uid;
    }
}