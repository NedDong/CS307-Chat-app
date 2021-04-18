import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This is the chat server program.
 */
public class ChatServer implements Serializable {
    private transient int port;
    private transient Set<String> userNames = new HashSet<>();
    private transient Set<UserThread> userThreads = new HashSet<>();
    static int uid = 100;
    static int groupid = 100;
    public transient List<User> userList = new ArrayList<>();
    public transient List<GroupChat> chatList = new ArrayList<>();
    public ChatServer(int port) {
        this.port = port;
    }

    private static String dbUrl = "jdbc:mysql://localhost:3306/chatapp01";
    private static String dbUsername = "root";
    private static String dbPassword = "12345678";

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
    private Statement statement = null;
    public void dbConnect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            statement = connection.createStatement();
            //System.out.println("database");
        } catch (SQLException e) {
            System.err.print(e.getMessage() + " ARGH!");
//        } catch(Exception e) {
//            System.err.print(e.getMessage() + " FUUUUUUUUUU!");
//        }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Boolean runSQLCommand(String sql){
        try {
            Boolean result = statement.execute(sql);
            return result;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet runSQLQuery(String sql){
        try {
            ResultSet sqlResponse = statement.executeQuery(sql);
            return sqlResponse;
        }catch (Exception e)
        {
            return null;
        }
    }


    public static void main(String[] args) throws SQLException {
        ChatServer server = new ChatServer(12345);
        server.dbConnect();
        server.serverRestoration();
        server.execute();

    }

    public void serverRestoration() throws SQLException {
        String sql = "DROP TABLE IF EXISTS Users;";
        statement.execute(sql);
        sql = "DROP TABLE IF EXISTS FriendList;";
        statement.execute(sql);
        sql = "DROP TABLE IF EXISTS UserInfo;";
        statement.execute(sql);
        sql = "DROP TABLE IF EXISTS ChatGroup;";
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS Users(\n" +
                "    UID varchar(8) NOT NULL PRIMARY KEY,\n" +
                "    UserName varchar(32),\n" +
                "    Password varchar(32)\n" +
                ");";
        statement.execute(sql);
        sql = "CREATE TABLE IF NOT EXISTS UserInfo(\n" +
                "    UID varchar(8) NOT NULL PRIMARY KEY,\n" +
                "    UserName varchar(32),\n" +
                "    Birthday DATE,\n" +
                "    Gender varchar(8)\n" +
                ");";
        statement.execute(sql);
        sql = "CREATE TABLE IF NOT EXISTS FriendList(\n" +
                "    UID varchar(8) NOT NULL,\n" +
                "    UserName varchar(32),\n" +
                "    FriendUID varchar(8) NOT NULL,\n" +
                "    FriendUserName varchar(32),\n" +
                "    Relationship varchar(16)\n" +
                ");";
        statement.execute(sql);
        sql ="CREATE TABLE IF NOT EXISTS ChatGroup(GroupID VARCHAR( 8 ) ,\n" +
                "Member VARCHAR( 8 ) ,\n" +
                "MemberType VARCHAR( 8 )\n" +
                ");";
        statement.execute(sql);
    }

    /*public void serverRestoration() throws SQLException {
        String sql = "DROP TABLE IF EXISTS Users;";
        statement.execute(sql);
        sql = "DROP TABLE IF EXISTS FriendList;";
        statement.execute(sql);
        sql = "DROP TABLE IF EXISTS UserInfo;";
        statement.execute(sql);
        sql = "DROP TABLE IF EXISTS ChatGroup;";
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS Users(\n" +
                "    UID varchar(8) NOT NULL PRIMARY KEY,\n" +
                "    UserName varchar(32),\n" +
                "    Password varchar(32)\n" +
                ");";
        statement.execute(sql);
        sql = "CREATE TABLE IF NOT EXISTS UserInfo(\n" +
                "    UID varchar(8) NOT NULL PRIMARY KEY,\n" +
                "    UserName varchar(32),\n" +
                "    Birthday DATE,\n" +
                "    Gender varchar(8)\n" +
                ");";
        statement.execute(sql);
        sql = "CREATE TABLE IF NOT EXISTS FriendList(\n" +
                "    UID varchar(8) NOT NULL,\n" +
                "    UserName varchar(32),\n" +
                "    FriendUID varchar(8) NOT NULL,\n" +
                "    FriendUserName varchar(32),\n" +
                "    Relationship varchar(16)\n" +
                ");";
        statement.execute(sql);
        sql ="CREATE TABLE IF NOT EXISTS ChatGroup(GroupID VARCHAR( 8 ) NOT NULL PRIMARY KEY ,\n" +
                "Member VARCHAR( 8 ) ,\n" +
                "MemberType VARCHAR( 8 )\n" +
                ");";
        statement.execute(sql);
    }*/

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
                String sql = "DELETE FROM Users WHERE UID='"+user.getUid()+"'";
                runSQLCommand(sql);
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

    public static int getUid()
    {
        uid++;
        return uid;
    }

    public static int getGroupid() {
        groupid++;
        return groupid;
    }

    public void addGroup(GroupChat group) {
        chatList.add(group);
    }

    public List<GroupChat> getGroupList() {
        return chatList;
    }

    public static void addUserData(User user) {
        return;
    }
}