import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This thread handles connection for each connected client, so the server
 * can handle multiple clients at the same time.
 */

public class UserThread extends Thread implements Serializable{
    private transient Socket socket;
    private ChatServer server;
    // private PrintWriter writer;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private boolean isLogin = false;
    private String blockedUser;

    //creates a thread
    public UserThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    //the function that handles input
    public void run() {
        try {
            //reads input from the app
            InputStream input = socket.getInputStream();
            ObjectInputStream reader = new ObjectInputStream(input);
            Message initialHandshake;
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            boolean usernameDuplicated;
            boolean successLogin;

            //reads the input and determines what to do with them
            String type = (String) reader.readObject();
            String userName = (String) reader.readObject();
            String password = (String) reader.readObject();

            initialHandshake = new Message(type, userName, password);
            if (initialHandshake.getMessageType().equals("REG")) { //if "REG"(resgister) creates a new user
                do {
                    //checks if the username is taken
                    usernameDuplicated = false;
                    for (User user : server.getUserList()) {
                        if (user.getUsername().equals(initialHandshake.getUsername())) {
                            outputStream.writeObject("Username Duplicated. Try again.");
                            usernameDuplicated = true;
                            break;
                        }
                    }
                    //wait for further input
                    if (usernameDuplicated) initialHandshake = (Message) reader.readObject();
                } while (usernameDuplicated); //creates new users and add them to database, continues until no duplicate username
                int tempUID = server.getUid();
                List<User> temp = new ArrayList<User>();
                System.out.println(sql);
                server.runSQLCommand(sql);
                outputStream.writeObject("User Creation Successful.");
                System.out.println("User Created:   " + initialHandshake.getUsername() + "     @   " + getCurrentTime());
            } else if (initialHandshake.getMessageType().equals("LOG")) { //if message is "LOG" (log in), try to log in the user
                do {
                    successLogin = false;
                    for (User user : server.getUserList()) {
                        //checks login credentials of the user login if correct repeat if not
                        if (user.getUsername().equals(initialHandshake.getUsername()) &&
                                user.getPassword().equals(initialHandshake.getPassword())) {
                            isLogin = true;
                            outputStream.writeObject("Login Success");
                            successLogin = true;
                            break;
                        }
                        outputStream.writeObject("Incorrect username or password, Please try again");
                    }
                    if (!successLogin) {
                        type = (String) reader.readObject();
                        userName = (String) reader.readObject();
                        password = (String) reader.readObject();

                        initialHandshake = new Message(type, userName, password);
                    }
                } while (!successLogin);
            } else if (initialHandshake.getMessageType().equals("LIST")) { //if message is "LIST" will check and return list of blocked users
                //return number of users
                outputStream.writeObject(server.getUserList().size());
                //find all the users that are blocked and output them
                for (User user : server.getUserList()) {
                    try {
                        if (user.getUsername().equals(blockedUser)) {
                            continue;
                        }
                        outputStream.writeObject("Username: " + user.getUsername() + " UID: " + user.getUid() +
                                " Address: " + user.getInetAddress());
                        outputStream.writeObject(user.getUsername());
                        outputStream.writeObject(user.getUid());
                        outputStream.writeObject(user.getInetAddress());
                        outputStream.writeObject(user.getPassword());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return;

                else {
                    for (User user : server.getUserList()) {
                        if (user.getUid() == Integer.parseInt(tempUID)) {
                            System.out.println("Useronline");

                                    System.out.println(reqestuser.getUsername());
                                    user.addTowaitingList(reqestuser);
                                    System.out.println("added");
                                }
                            }
                        }
                    }
                }
                return;
            }
                //if found the receiver will add the sender to the waiting list of friend requests
            else if (initialHandshake.getMessageType().equals("DEREGISTER")) {//if message is "DEREGISTER" will remove the user;
                server.removeUser(initialHandshake.getUsername());
                return;
            }
            else if (initialHandshake.getMessageType().equals("UpdateUserName")) {//if message is "UpdateUserName" will change the uesrname of the user.
                server.changeUsername(initialHandshake.getUsername(), initialHandshake.getPassword());
                return;
            }
            else if (initialHandshake.getMessageType().equals("block")) {//if message is "block" will find add the user to blocked list.
                blockedUser = initialHandshake.getUsername();
                return;
            }
            else if (initialHandshake.getMessageType().equals("FriendList")) {//if message is"FriendList" will return friend list of the user
                String currUserName = initialHandshake.getUsername();
                //find if the user exists
                String checkUser = "SELECT * FROM Users WHERE UserName ='" + currUserName + "'";
                ResultSet resultset1 = server.runSQLQuery(checkUser);
                if (resultset1 == null) {
                    outputStream.writeObject("NO USER");
                    System.out.println("The user does not exit");
                } else {
                    //get the list of friends
                    String sql = "SELECT FriendUserName FROM FriendList WHERE UserName ='" + currUserName + "' AND Relationship = 'friend'";
                    ResultSet resultset = server.runSQLQuery(sql);
                    if (resultset == null) {
                        outputStream.writeObject("NO FRIENDS");
                        System.out.println("The user has no friends");
                    }
                    //send friends to app
                    else {
                        resultset.last();
                        int n = resultset.getRow();
                        outputStream.writeObject(n);
                        for(int i = 0; i < n; i++) {
                            for (User user : server.getUserList()) {
                                if (user.getUsername().equals(resultset.getString(i))) {
                                    outputStream.writeObject(user.getUsername());
                                    outputStream.writeObject(user.getUid());
                                    outputStream.writeObject(user.getInetAddress());
                                    outputStream.writeObject(user.getPassword());
                                    System.out.println("Friend: " + user.getUsername());
                                    i++;
                                }
                            }
                        }
                    }
                    return;
                }
            }
            else if (initialHandshake.getMessageType().equals("Blocked")) {//if message is"Blocked" will return blocked list of the user
                String currUserName = initialHandshake.getUsername();
                //find if the user exists
                String checkUser = "SELECT * FROM Users WHERE UserName ='" + currUserName + "'";
                ResultSet resultset1 = server.runSQLQuery(checkUser);
                if (resultset1 == null) {
                    outputStream.writeObject("NO USER");
                    System.out.println("The user does not exit");
                } else {
                    //get the blocked list
                    String sql = "SELECT FriendUserName FROM FriendList WHERE UserName ='" + currUserName + "' AND Relationship = 'blocked'";
                    ResultSet resultset = server.runSQLQuery(sql);
                    if (resultset == null) {
                        outputStream.writeObject("NO BLOCKED");
                        System.out.println("The user has no blocked list");
                    }
                    //return it to the app
                    else {
                        resultset.last();
                        int n = resultset.getRow();
                        outputStream.writeObject(n);
                        for(int i = 0; i < n; i++) {
                            for (User user : server.getUserList()) {
                                if (user.getUsername().equals(resultset.getString(i))) {
                                    outputStream.writeObject(user.getUsername());
                                    outputStream.writeObject(user.getUid());
                                    outputStream.writeObject(user.getInetAddress());
                                    outputStream.writeObject(user.getPassword());
                                    System.out.println("Blocked: " + user.getUsername());
                                    i++;
                                }
                            }
                        }
                    }
                    return;
                }
            }
            printUsers();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage()+ " @   "+getCurrentTime() ) ;
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
                try{
                    if(user.getUsername().equals(blockedUser))
                    {
                        continue;
                    }
                    outputStream.writeObject("Username: "+ user.getUsername() + " UID" +
                            user.getUid() +" Address:"+user.getInetAddress());
                    //outputStream.flush();

                    //String s = "" + user.getSocket();

                    outputStream.writeObject(user.getUsername());
                    outputStream.writeObject(user.getUid());
                    outputStream.writeObject(user.getInetAddress());
                    outputStream.writeObject(user.getPassword());

                    // outputStream.writeObject(new User(user.getUsername(),user.getUid(),user.getSocket(), user.getPassword()));
                    // outputStream.flush();
                } catch (IOException e) {
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