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
                server.getUserList().add(new User(initialHandshake.getUsername(), tempUID, socket.getInetAddress(), initialHandshake.getPassword(), temp));
                String sql = "INSERT INTO Users(UID, UserName, Password) VALUES('" + tempUID + "','" + initialHandshake.getUsername() + "','" + initialHandshake.getPassword() + "')";
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
            } else if (initialHandshake.getMessageType().equals("ADDFRIEND")) { //if the message is "ADDFRIEND" will add friend to the current user
                String tempUID = initialHandshake.getUsername(); //uid of the user sending the request
                String requser = initialHandshake.getPassword(); //uid of the user receving the request
                //find the sender's information from database
                String sql = "SELECT Username FROM Users WHERE UID ='" + tempUID + "'";
                ResultSet resultset = server.runSQLQuery(sql);
                if (resultset == null) {
                    System.out.println("User with Specified UID cant be found");
                }
                //if cannot find the receiver
                else {
                    for (User user : server.getUserList()) {
                        if (user.getUid() == Integer.parseInt(tempUID)) {
                            System.out.println("Useronline");
                            for (User reqestuser : server.getUserList()) {
                                if (reqestuser.getUsername().equals(requser)) {
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
                        for(int i = 1; i <= n; i++) {
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
                        for(int i = 1; i <= n; i++) {
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
            else if(initialHandshake.getMessageType().equals("Group")) { //creates a new group in database
                ArrayList<User> member = new ArrayList<>();
                String groupNmae = initialHandshake.getUsername();
                int ownerUid = Integer.parseInt(initialHandshake.getPassword());
                User owner = null;
                for(GroupChat chat : server.getGroupList()) {
                    if(chat.getGroupName().equals(groupNmae)) {
                        outputStream.writeObject("DUPLICATED GROUP NAME");
                        return;
                    }
                }
                for(User user : server.getUserList()) {
                    if (user.getUid() == ownerUid) owner = user;
                    break;
                }
                member.add(owner);
                int groupId = server.getGroupid();
                GroupChat group = new GroupChat(groupId, owner, member, groupNmae);
                server.addGroup(group);
                String sql = "INSERT INTO Groups(GroupID, Member, MemberType) VALUES('" + groupId + "','" + ownerUid + "','owner')";
                System.out.println(sql);
                server.runSQLCommand(sql);
                outputStream.writeObject("SUCCESS");
                outputStream.writeObject(groupId);
            }
            else if(initialHandshake.getMessageType().equals("AddMember")) { //add a member to a group
                String groupName = initialHandshake.getUsername();
                int memberId = Integer.parseInt(initialHandshake.getPassword());
                User member = null;
                GroupChat group = null;
                for(User user : server.getUserList()) {
                    if(user.getUid() == memberId) {
                        member = user;
                        break;
                    }
                }
                for(GroupChat chat : server.getGroupList()) {
                    if(chat.getGroupName().equals(groupName)) {
                        ArrayList<User> list = new ArrayList<>();
                        list.add(member);
                        chat.addGroupMembers(list);
                        group = chat;
                        break;
                    }
                }
                int groupId = group.getGroupID();
                String sql = "INSERT INTO Groups(GroupID, Member, MemberType) VALUES('" + groupId + "','" + memberId + "','member')";
                System.out.println(sql);
                server.runSQLCommand(sql);
                outputStream.writeObject("SUCCESS");
            }
            else if (initialHandshake.getMessageType().equals("AddManager")) { //add a manager to a group
                String groupName = initialHandshake.getUsername();
                int memberId = Integer.parseInt(initialHandshake.getPassword());
                User member = null;
                GroupChat group = null;
                for(User user : server.getUserList()) {
                    if(user.getUid() == memberId) {
                        member = user;
                        break;
                    }
                }
                for(GroupChat chat : server.getGroupList()) {
                    if(chat.getGroupName().equals(groupName)) {
                        ArrayList<User> list = new ArrayList<>();
                        list.add(member);
                        chat.addManager(list);
                        group = chat;
                        break;
                    }
                }
                int groupId = group.getGroupID();
                String sql = "INSERT INTO Groups(GroupID, Member, MemberType) VALUES('" + groupId + "','" + memberId + "','manager')";
                System.out.println(sql);
                server.runSQLCommand(sql);
                outputStream.writeObject("SUCCESS");
            }
            else if(initialHandshake.getMessageType().equals("ChangeGroupName")) {
                String newName = initialHandshake.getUsername();
                int groupId = Integer.parseInt(initialHandshake.getPassword());
                for(GroupChat chat : server.getGroupList()) {
                    if(chat.getGroupName().equals(newName)) {
                        outputStream.writeObject("DUPLICATE GROUP NAME");
                        break;
                    }
                }
                for(GroupChat chat : server.getGroupList()) {
                    if(chat.getGroupID() == groupId) {
                        chat.setGroupName(newName);
                        outputStream.writeObject("SUCCESS");
                    }
                }
            }
            else if(initialHandshake.getMessageType().equals("ChangeGroupOwner")) {
                int groupId = Integer.parseInt(initialHandshake.getUsername());
                int ownerId = Integer.parseInt(initialHandshake.getPassword());
                User owner = null;
                GroupChat chat = null;
                for(User user : server.getUserList()) {
                    if(user.getUid() == ownerId) owner = user;
                }
                for(GroupChat group : server.getGroupList()) {
                    if(group.getGroupID() == groupId) {
                        User old = chat.getGroupOwner();
                        chat.setGroupOwner(owner);
                        chat.removeManager(old);
                        ArrayList<User> own = new ArrayList<>();
                        own.add(owner);
                        chat.addManager(own);
                        chat = group;
                    }
                }
                String sql = "UPDATE Groups SET MemberType = 'member' WHERE MemberType = 'owner'";
                System.out.println(sql);
                server.runSQLCommand(sql);
                String set = "UPDATE Groups SET MemberType = 'owner' WHERE Member = '" + ownerId + "'";
                System.out.println(set);
                server.runSQLCommand(sql);
                outputStream.writeObject("SUCCESS");
            }
            else if(initialHandshake.getMessageType().equals("GetGroups")) {
                if(server.getGroupList() == null) {
                    outputStream.writeObject("NO GROUPS");
                } else {
                    outputStream.writeObject(server.getGroupList().size());
                    for(GroupChat group : server.getGroupList()) {
                        outputStream.writeObject(group.getGroupID());
                    }
                }
            }
            else if(initialHandshake.getMessageType().equals("GetGroupMembers")) {
                int groupId = Integer.parseInt(initialHandshake.getUsername());
                for(GroupChat group : server.getGroupList()) {
                    if(group.getGroupID() == groupId) {
                        ArrayList<User> users = group.getGroupMembers();
                        outputStream.writeObject(users.size());
                        for(int i = 0; i < users.size(); i++) {
                            outputStream.writeObject(users.get(i).getUid());
                        }
                        return;
                    }
                }
                outputStream.writeObject("NO SUCH GROUP");
            }
            else if(initialHandshake.getMessageType().equals("GetGroupManagers")) {
                int groupId = Integer.parseInt(initialHandshake.getUsername());
                for(GroupChat group : server.getGroupList()) {
                    if(group.getGroupID() == groupId) {
                        ArrayList<User> mana = group.getManagers();
                        outputStream.writeObject(mana.size());
                        for(int i = 0; i < mana.size(); i++) {
                            outputStream.writeObject(mana.get(i).getUid());
                        }
                        return;
                    }
                }
                outputStream.writeObject("NO SUCH GROUP");
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