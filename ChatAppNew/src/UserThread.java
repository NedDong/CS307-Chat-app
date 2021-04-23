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

public class UserThread extends Thread implements Serializable {
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
//        while (true) {
        try {

            //reads input from the app
            InputStream input = socket.getInputStream();
            ObjectInputStream reader = new ObjectInputStream(input);
            Message initialHandshake;
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                boolean usernameDuplicated;
                boolean successLogin;

                //reads the input and determines what to do with them
                String type = (String) reader.readObject();
                String userName = (String) reader.readObject();
                String password = (String) reader.readObject();
                int failed = 0;
                initialHandshake = new Message(type, userName, password);
                if (initialHandshake.getMessageType().equals("REG")) { //if "REG"(resgister) creates a new user
                    //do {
                        //checks if the username is taken
                        usernameDuplicated = false;
                        for (User user : server.getUserList()) {
                            if (user.getUsername().equals(initialHandshake.getUsername())) {
                                outputStream.writeObject("Username Duplicated. Try again.");
                                usernameDuplicated = true;
                                failed = 1;
                                break;
                            }

                        }
                        //wait for further inputPrime x570
                        //if (usernameDuplicated) initialHandshake = (Message) reader.readObject();
                    //} while (usernameDuplicated); //creates new users and add them to database, continues until no duplicate username
                    if(failed!=1) {
                        int tempUID = server.getUid();
                        List<User> temp = new ArrayList<User>();
                        List<Integer> gidList = new ArrayList<>();
                        server.getUserList().add(new User(initialHandshake.getUsername(), tempUID, socket.getInetAddress(), initialHandshake.getPassword(), temp, gidList, "0"));
                        String sql = "INSERT INTO Users(UID, UserName, Password) VALUES('" + tempUID + "','" + initialHandshake.getUsername() + "','" + initialHandshake.getPassword() + "')";
                        System.out.println(sql);
                        server.runSQLCommand(sql);
                        outputStream.writeObject("User Creation Successful.");
                        printUsers();
                        System.out.println("User Created:   " + initialHandshake.getUsername() + "     @   " + getCurrentTime());
                    }
                    continue;
                } else if (initialHandshake.getMessageType().equals("LOG")) { //if message is "LOG" (log in), try to log in the user
                    //do {
                        successLogin = false;
                        for (User user : server.getUserList()) {
                            //checks login credentials of the user login if correct repeat if not
                            if (user.getUsername().equals(initialHandshake.getUsername()) &&
                                    user.getPassword().equals(initialHandshake.getPassword())) {
                                isLogin = true;
                                outputStream.writeObject("Login Success");
                                successLogin = true;
                                printUsers();
                                break;
                            }

                        }
                        outputStream.writeObject("Incorrect username or password, Please try again");
                        if (!successLogin) {
                            type = (String) reader.readObject();
                            userName = (String) reader.readObject();
                            password = (String) reader.readObject();

                            initialHandshake = new Message(type, userName, password);
                        }
                    //} while (!successLogin);
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
                                System.out.println("User on line");
                                for (User reqestuser : server.getUserList()) {
                                    if (reqestuser.getUid() == Integer.parseInt(requser)) {
                                        System.out.println(reqestuser.getUsername());
                                        user.addTowaitingList(reqestuser);
                                        System.out.println("added");
                                    }
                                }
                            }
                        }
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                }
                //if found the receiver will add the sender to the waiting list of friend requests
                else if (initialHandshake.getMessageType().equals("DEREGISTER")) {//if message is "DEREGISTER" will remove the user;
                    String name = initialHandshake.getUsername();
                    boolean found = false;
                    for(User user : server.getUserList()) {
                        if(user.getUsername().equals(name)) {
                            found = true;
                            int id = user.getUid();
                            String sql = "DELETE FROM Users WHERE UID ='" + id + "'";
                            server.runSQLCommand(sql);
                            //server.removeUser(initialHandshake.getUsername());
                            outputStream.writeObject("SUCCESS");
                            break;
                        }
                    }
                    if(!found) {
                        outputStream.writeObject("NO SUCH USER");
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("UpdateUserName")) {//if message is "UpdateUserName" will change the uesrname of the user.
                    String name = initialHandshake.getUsername();
                    boolean found = false;
                    for(User user : server.getUserList()) {
                        if(user.getUsername().equals(name)) {
                            found = true;
                            int id = user.getUid();
                            user.setUsername(name);
                            String sql = "UPDATE Users SET UserName = '" + name + "' WHERE UID ='" + id + "'";
                            server.runSQLCommand(sql);
                            //server.removeUser(initialHandshake.getUsername());
                            outputStream.writeObject("SUCCESS");
                            break;
                        }
                    }
                    if(!found) {
                        outputStream.writeObject("NO SUCH USER");
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                    /*server.changeUsername(initialHandshake.getUsername(), initialHandshake.getPassword());
                    return;*/
                } else if (initialHandshake.getMessageType().equals("block")) {//if message is "block" will find add the user to blocked list.
                    blockedUser = initialHandshake.getUsername();
                    return;
                } else if (initialHandshake.getMessageType().equals("FriendList")) {//if message is"FriendList" will return friend list of the user
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
                            while (resultset.next()) {
                                for (User user : server.getUserList()) {
                                    if (user.getUsername().equals(resultset.getString("FriendUserName"))) {
                                        outputStream.writeObject(user.getUsername());
                                        outputStream.writeObject(user.getUid());
                                        outputStream.writeObject(user.getInetAddress());
                                        outputStream.writeObject(user.getPassword());
                                        outputStream.writeObject(user.getAvatarId());
                                        System.out.println("Friend: " + user.getUsername());
                                    }
                                }
                            }
                        }
                        outputStream.writeObject("**FINISHED**");
                        return;
                    }
                } else if (initialHandshake.getMessageType().equals("Blocked")) {//if message is"Blocked" will return blocked list of the user
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
                            while (resultset.next()) {
                                for (User user : server.getUserList()) {
                                    if (user.getUsername().equals(resultset.getString("FriendUserName"))) {
                                        outputStream.writeObject(user.getUsername());
                                        outputStream.writeObject(user.getUid());
                                        outputStream.writeObject(user.getInetAddress());
                                        outputStream.writeObject(user.getPassword());
                                        System.out.println("Blocked: " + user.getUsername());
                                    }
                                }
                            }
                        }
                        return;
                    }
                } else if (initialHandshake.getMessageType().equals("FED")) {
                    System.out.println(initialHandshake.getUsername());
                    System.out.println(initialHandshake.getPassword());
                } else if (initialHandshake.getMessageType().equals("CreateGroup")) { //creates a new group in database
                    ArrayList<User> member = new ArrayList<>();
                    String groupNmae = initialHandshake.getUsername();
                    int ownerUid = Integer.parseInt(initialHandshake.getPassword());
                    List<User> temp = new ArrayList<>();
                    User owner = null;
                    for (GroupChat chat : server.getGroupList()) {
                        if (chat.getGroupName().equals(groupNmae)) {
                            outputStream.writeObject("DUPLICATED GROUP NAME");
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    for (User user : server.getUserList()) {
                        if (user.getUid() == ownerUid) {
                            owner = user;
                            break;
                        }
                    }
                    member.add(owner);
                    int groupId = server.getGroupid();
                    owner.addGidList(groupId);
                    GroupChat group = new GroupChat(groupId, owner, member, groupNmae, "0");
                    server.addGroup(group);
                    String sql = "INSERT INTO ChatGroup(GroupID, Member, MemberType) VALUES('" + groupId + "','" + ownerUid + "','owner')";
                    System.out.println(sql);
                    server.runSQLCommand(sql);
                    outputStream.writeObject("SUCCESS");
                    outputStream.writeObject(groupId);
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("GetGroupList")) { //return groups that the user is in
                    String username = initialHandshake.getUsername();
                    String temp = initialHandshake.getPassword();
                    User user = null;
                    for (User user1 : server.getUserList()) {
                        if (user1.getUsername().equals(username)) {
                            user = user1;
                            break;
                        }
                    }

                    if (user == null) {
                        outputStream.writeObject(-1);
                        break;
                    }

                    List<Integer> groupList = user.getGidList();
                    int num = groupList.size();

                    outputStream.writeObject(num);

                    for (int i = 0; i < num; i++) {
                        outputStream.writeObject(groupList.get(i));
                        outputStream.writeObject(server.getGroupList().get(groupList.get(i)).getGroupName());
                        System.out.println(server.getGroupList().get(groupList.get(i)).getGroupName());
                    }

                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("AddMember")) { //add a member to a group
                    String groupName = initialHandshake.getUsername();
                    int memberId = Integer.parseInt(initialHandshake.getPassword());
                    User member = null;
                    GroupChat group = null;
                    for (User user : server.getUserList()) {
                        if (user.getUid() == memberId) {
                            member = user;
                            break;
                        }
                    }
                    for (GroupChat chat : server.getGroupList()) {
                        if (chat.getGroupName().equals(groupName)) {
                            group = chat;
                            break;
                        }
                    }
                    ArrayList<User> members = group.getGroupMembers();
                    for(int i = 0; i < members.size(); i++) {
                        if(members.get(i).getUid() == member.getUid()) {
                            outputStream.writeObject("SAME USER");
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    if(!group.addMember(member)) {
                        System.out.println("Max num exceeded");
                        outputStream.writeObject("Max num exceeded");
                    } else {
                        int groupId = group.getGroupID();
                        String sql = "INSERT INTO ChatGroup(GroupID, Member, MemberType) VALUES('" + groupId + "','" + memberId + "','member')";
                        System.out.println(sql);
                        server.runSQLCommand(sql);
                        System.out.println("Success");
                        outputStream.writeObject("Success");
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("AddManager")) { //add a manager to a group
                    String groupName = initialHandshake.getUsername();
                    int memberId = Integer.parseInt(initialHandshake.getPassword());
                    User member = null;
                    GroupChat group = null;
                    for (User user : server.getUserList()) {
                        if (user.getUid() == memberId) {
                            member = user;
                            break;

                        }
                    }
                    for (GroupChat chat : server.getGroupList()) {
                        if (chat.getGroupName().equals(groupName)) {
                            ArrayList<User> list = new ArrayList<>();
                            list.add(member);
                            boolean pos = chat.addManager(list);
                            if(!pos) {
                                outputStream.writeObject("MAX NUMBER EXCEEDED");
                                outputStream.writeObject("**FINISHED**");
                                return;
                            }
                            group = chat;
                            break;
                        }
                    }
                    int groupId = group.getGroupID();
                    String sql = "UPDATE ChatGroup SET MemberType = 'manager' WHERE Member ='" + memberId + "'";
                    System.out.println(sql);
                    server.runSQLCommand(sql);
                    outputStream.writeObject("SUCCESS");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("ChangeGroupName")) { //Change the name of the group
                    String newName = initialHandshake.getUsername();
                    int groupId = Integer.parseInt(initialHandshake.getPassword());
                    for (GroupChat chat : server.getGroupList()) {
                        if (chat.getGroupName().equals(newName)) {
                            outputStream.writeObject("DUPLICATE GROUP NAME");
                            System.out.println("DUPLICATE GROUP NAME");
                            return;
                        }
                    }
                    for (GroupChat chat : server.getGroupList()) {
                        if (chat.getGroupID() == groupId) {
                            chat.setGroupName(newName);
                            outputStream.writeObject("SUCCESS");
                        }
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("ChangeGroupOwner")) { //change the owner of the group
                    int groupId = Integer.parseInt(initialHandshake.getUsername());
                    int ownerId = Integer.parseInt(initialHandshake.getPassword());
                    User owner = null;
                    User old = null;
                    GroupChat chat = null;
                    for (User user : server.getUserList()) {
                        if (user.getUid() == ownerId) owner = user;
                    }
                    for (GroupChat group : server.getGroupList()) {
                        if (group.getGroupID() == groupId) {
                            old = group.getGroupOwner();
                            group.setGroupOwner(owner);
                            group.removeManager(old);
                            ArrayList<User> own = new ArrayList<>();
                            own.add(owner);
                            group.addManager(own);
                            chat = group;
                        }
                    }
                    String sql = "UPDATE ChatGroup SET MemberType = 'member' WHERE Member = '" + old.getUid() + "' AND GroupID = '" + groupId + "'";
                    System.out.println(sql);
                    server.runSQLCommand(sql);
                    String set = "UPDATE ChatGroup SET MemberType = 'owner' WHERE Member = '" + ownerId + "' AND GroupID = '" + groupId + "'";
                    System.out.println(set);
                    server.runSQLCommand(sql);
                    outputStream.writeObject("SUCCESS");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("GetGroups")) { //return a list of groups
                    if (server.getGroupList() == null) {
                        outputStream.writeObject("NO GROUPS");
                    } else {
                        outputStream.writeObject(server.getGroupList().size());
                        for (GroupChat group : server.getGroupList()) {
                            outputStream.writeObject(group.getGroupID());
                            outputStream.writeObject(group.getGroupName());
                            outputStream.writeObject(group.getAvatarID());
                        }
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("GetGroupMembers")) { //return all members of the group
                    String temp = initialHandshake.getUsername();
                    int groupId = Integer.parseInt(temp);
                    String temp2 = initialHandshake.getPassword();
                    for (GroupChat group : server.getGroupList()) {
                        if (group.getGroupID() == groupId) {
                            ArrayList<User> users = group.getGroupMembers();
                            //outputStream.writeObject(users.size());
                            for (int i = 0; i < users.size(); i++) {
                                outputStream.writeObject(users.get(i).getUid());
                                outputStream.writeObject(users.get(i).getUsername());
                                outputStream.writeObject(users.get(i).getAvatarId());
                            }
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    outputStream.writeObject("NO SUCH GROUP");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("GetGroupManagers")) {//return managers of a group
                    int groupId = Integer.parseInt(initialHandshake.getUsername());
                    for (GroupChat group : server.getGroupList()) {
                        if (group.getGroupID() == groupId) {
                            ArrayList<User> mana = group.getManagers();
                            //outputStream.writeObject(mana.size());
                            for (int i = 0; i < mana.size(); i++) {
                                outputStream.writeObject(mana.get(i).getUid());
                                outputStream.writeObject(mana.get(i).getUsername());
                                outputStream.writeObject(mana.get(i).getAvatarId());
                            }
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    outputStream.writeObject("NO SUCH GROUP");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("GetUserGroups")) { //return all groups of a user
                    int uid = Integer.parseInt(initialHandshake.getUsername());
                    String sql = "SELECT DISTINCT GroupID FROM ChatGroup WHERE Member ='" + uid + "'";
                    System.out.println(sql);
                    ResultSet rs = server.runSQLQuery(sql);
                    if (rs == null) {
                        outputStream.writeObject("NO GROUPS");
                    } else {
                        while (rs.next()) {
                            String Id = rs.getString("GroupID");
                            System.out.println(Id);
                            outputStream.writeObject(Id);
                            for (GroupChat chat : server.getGroupList()) {
                                if (String.valueOf(chat.getGroupID()).equals(Id)) {
                                    System.out.println(chat.getGroupName());
                                    outputStream.writeObject(chat.getGroupName());
                                    outputStream.writeObject(chat.getAvatarID());
                                    break;
                                }
                            }
                        }
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("DeleteFromGroup")) { //delete user from a group
                    int groupID = Integer.parseInt(initialHandshake.getUsername());
                    int memberID = Integer.parseInt(initialHandshake.getPassword());
                    String sql = "DELETE FROM ChatGroup WHERE Member = '" + memberID + "' AND GroupID ='" + groupID + "'";
                    server.runSQLQuery(sql);
                    for(GroupChat group : server.getGroupList()) {
                        if(group.getGroupID() == groupID) {
                            for(int i = 0; i < group.getGroupMembers().size(); i++) {
                                if(group.getGroupMembers().get(i).getUid() == memberID) {
                                    group.removeMember(group.getGroupMembers().get(i));
                                }
                            }
                        }
                    }
                    outputStream.writeObject("SUCCESS");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("ChangeUserAvatar")) {//return managers of a group
                    int userId = Integer.parseInt(initialHandshake.getUsername());
                    String avatarId = initialHandshake.getPassword();
                    for (User user : server.getUserList()) {
                        if (user.getUid() == userId) {
                            user.setAvatarId(avatarId);
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    outputStream.writeObject("NO SUCH USER");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("ChangeGroupAvatar")) {//return managers of a group
                    int groupId = Integer.parseInt(initialHandshake.getUsername());
                    String avatarId = initialHandshake.getPassword();
                    for (GroupChat group : server.getGroupList()) {
                        if (group.getGroupID() == groupId) {
                            group.setAvatarID(avatarId);
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    outputStream.writeObject("NO SUCH GROUP");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("GetGroupAvatar")) {//return managers of a group
                    int groupId = Integer.parseInt(initialHandshake.getUsername());
                    //int avatarId = Integer.parseInt(initialHandshake.getPassword());
                    for (GroupChat group : server.getGroupList()) {
                        if (group.getGroupID() == groupId) {
                            outputStream.writeObject(group.getAvatarID());
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    outputStream.writeObject("NO SUCH GROUP");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if (initialHandshake.getMessageType().equals("GetUserAvatar")) {//return managers of a group
                    int userId = Integer.parseInt(initialHandshake.getUsername());
                    //int avatarId = Integer.parseInt(initialHandshake.getPassword());
                    for (User user : server.getUserList()) {
                        if (user.getUid() == userId) {
                            outputStream.writeObject(user.getAvatarId());
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    outputStream.writeObject("NO SUCH USER");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if(initialHandshake.getMessageType().equals("Ban")) {
                    int userId = Integer.parseInt(initialHandshake.getUsername());
                    for(User user : server.getUserList()) {
                        if(user.getUid() == userId) {
                            user.report();
                            if(user.getReportCount() >= 2) {
                                user.ban();
                                outputStream.writeObject("USER BANNED");
                            } else {
                                outputStream.writeObject("USER NOT BANNED");
                            }
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    outputStream.writeObject("NO SUCH USER");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if(initialHandshake.getMessageType().equals("CheckManager")) {
                    int groupId = Integer.parseInt(initialHandshake.getUsername());
                    int userId = Integer.parseInt(initialHandshake.getPassword());
                    for(GroupChat group : server.getGroupList()) {
                        if(group.getGroupID() == groupId) {
                            ArrayList<User> man = group.getManagers();
                            for(int i = 0; i < man.size(); i++) {
                                if(man.get(i).getUid() == userId) {
                                    outputStream.writeObject("IS MANAGER");
                                    outputStream.writeObject("**FINISHED**");
                                    return;
                                }
                            }
                        }
                    }
                    outputStream.writeObject("NOT A MANAGER");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if(initialHandshake.getMessageType().equals("BanList")) {
                    //int userId = Integer.parseInt(initialHandshake.getUsername());
                    boolean found = false;
                    for(User user : server.getUserList()) {
                        if(user.isBan()) {
                            found = true;
                            outputStream.writeObject(user.getUid());
                            outputStream.writeObject(user.getUsername());
                            outputStream.writeObject(user.getAvatarId());
                        }
                    }
                    if(!found) {
                        outputStream.writeObject("NO USERS BANNED");
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if(initialHandshake.getMessageType().equals("CheckUserBan")) {
                    int userId = Integer.parseInt(initialHandshake.getUsername());
                    boolean found = false;
                    for(User user : server.getUserList()) {
                        if(user.getUid() == userId) {
                            if(user.isBan()) {
                                outputStream.writeObject("USER BANNED");
                            } else {
                                outputStream.writeObject("USER NOT BANNED");
                            }
                            break;
                        }
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if(initialHandshake.getMessageType().equals("Post")) {
                    int userId = Integer.parseInt(initialHandshake.getUsername());
                    String post = initialHandshake.getPassword();
                    if(post.length() >= 50) {
                        outputStream.writeObject("POST TOO LONG");
                        outputStream.writeObject("**FINISHED**");
                        return;
                    }
                    String sql = "INSERT INTO Posts(UserID, Post) VALUES ('" + userId +
                            "', '" + post + "')";
                    server.runSQLCommand(sql);
                    outputStream.writeObject("SUCCESS");
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if(initialHandshake.getMessageType().equals("GetPosts")) {
                    int userId = Integer.parseInt(initialHandshake.getUsername());
                    String sql = "SELECT Post FROM Posts WHERE UserID = '" + userId + "';";
                    ResultSet rs = server.runSQLQuery(sql);
                    if(rs == null) {
                        outputStream.writeObject("NO POSTS");
                        outputStream.writeObject("**FINISHED**");
                        return;
                    }
                    while (rs.next()) {
                        outputStream.writeObject(rs.getString("Post"));
                    }
                    outputStream.writeObject("**FINISHED**");
                    return;
                } else if(initialHandshake.getMessageType().equals("GetGroupOwner")) {
                    int groupId = Integer.parseInt(initialHandshake.getUsername());
                    for(GroupChat group : server.getGroupList()) {
                        if(group.getGroupID() == groupId) {
                            User owner = group.getGroupOwner();
                            outputStream.writeObject(owner.getUid());
                            outputStream.writeObject(owner.getUsername());
                            outputStream.writeObject(owner.getAvatarId());
                            outputStream.writeObject("**FINISHED**");
                            return;
                        }
                    }
                    outputStream.writeObject("NO SUCH GROUP");
                    outputStream.writeObject("**FINISHED**");
                    return;
                }

            }
            // printUsers();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage() + " @   " + getCurrentTime());
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    /**
     * Sends a list of online users to the newly connected user.
     */
    void printUsers() {
        if (!server.getUserList().isEmpty()) {

            System.out.println("=====NOT EMPTY====");


            // Pass the size of userlist

            try {
                outputStream.writeObject(server.getUserList().size());
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (User user : server.getUserList()) {
                try {
                    if (user.getUsername().equals(blockedUser)) {
                        continue;
                    }
                    outputStream.writeObject("Username: " + user.getUsername() + " UID" +
                            user.getUid() + " Address:" + user.getInetAddress());
                    //outputStream.flush();

                    //String s = "" + user.getSocket();

                    outputStream.writeObject(user.getUsername());
                    outputStream.writeObject(user.getUid());
                    outputStream.writeObject(user.getInetAddress());
                    outputStream.writeObject(user.getPassword());
                    outputStream.writeObject(user.getAvatarId());

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