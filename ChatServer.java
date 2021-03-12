import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.mysql.cj.jdbc.Driver;

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

                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();

            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage()+ " @    "+getCurrentTime() );
            ex.printStackTrace();
        }
    }
    private static String dbUrl = "jdbc:mysql://127.0.0.1:3306/CS307-Chat-Database";
    private static String dbUsername = "root";
    private static String dbPassword = "12345678";

    private Statement statement = null;

//    public void dbConnect() {
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
//            statement = connection.createStatement();
//            //System.out.println("database");
//        } catch(SQLException e) {
//            System.err.print(e.getMessage() + " ARGH!");
//        } catch(Exception e) {
//            System.err.print(e.getMessage() + " FUUUUUUUUUU!");
//        }
//    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(12345);
        //server.dbConnect();
        server.execute();


    }

    public String getCurrentTime()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }


    /**
     * When a client is disconneted, removes the associated username and UserThread
     */
    void removeUser(String userName, UserThread aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(aUser);
            System.out.println("The user " + userName + " quitted");
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