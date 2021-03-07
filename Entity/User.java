import java.awt.Image;
import java.awt.image.*;
import java.util.ArrayList;


public class User {
    // The uid is generated in registration and the user is not allowed to change it.
    private int uid;
    private String password;
    private String name;
    private String email;
    // The account is the account used to log in and the user can change it.
    private String account;

    private ArrayList<User> friendList;
    private ArrayList<User> blackList;
    private ArrayList<Group> groupList;
    private Image avatar;
    private int birthdate;
    // True if the user is an administrator
    private boolean isAdmin;
    private boolean privacy;
    private Gender gender;
    private Status status;

    public User(int uid, String password, String name, String email, String account, Image avatar, int birthdate,
                boolean privacy, Gender gender) {
        this.uid = uid;
        this.password = password;
        this.name = name;
        this.email = email;
        this.account = account;
        this.avatar = avatar;
        this.birthdate = birthdate;
        this.privacy = privacy;
        this.gender = gender;

        this.friendList = new ArrayList<User>();
        this.blackList = new ArrayList<User>();
        this.groupList = new ArrayList<Group>();

        this.isAdmin = false;
        this.status = Status.Offline;
    }

    public User(int uid, String password, String name, String email, String account, boolean privacy, Gender gender) {
        this.uid = uid;
        this.password = password;
        this.name = name;
        this.email = email;
        this.account = account;
        this.privacy = privacy;
        this.gender = gender;

        //Set the avatar to default avatar
        //this.avatar = default avatar;
        //keep birth date empty to let the user set it later
        this.birthdate = 0;

        this.friendList = new ArrayList<User>();
        this.blackList = new ArrayList<User>();
        this.groupList = new ArrayList<Group>();

        this.isAdmin = false;
        this.status = Status.Offline;
    }

    public User(int uid, String password, String name, String email, String account, boolean privacy) {
        this.uid = uid;
        this.password = password;
        this.name = name;
        this.email = email;
        this.account = account;
        this.privacy = privacy;
        this.gender = new Gender();

        //Set the avatar to default avatar
        //this.avatar = default avatar;
        //keep birth date empty to let the user set it later
        this.birthdate = 0;

        this.friendList = new ArrayList<User>();
        this.blackList = new ArrayList<User>();
        this.groupList = new ArrayList<Group>();

        this.isAdmin = false;
        this.status = Status.Offline;
    }

    //getters No getter to password

    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAccount() {
        return account;
    }

    public ArrayList<User> getFriendList() {
        return friendList;
    }

    public ArrayList<User> getBlackList() {
        return blackList;
    }

    public ArrayList<Group> getGroupList() {
        return groupList;
    }

    public Image getAvatar() {
        return avatar;
    }

    public int getBirthdate() {
        return birthdate;
    }

    public Gender getGender() {
        return gender;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    //setters No setters for uid, friendList, blackList and groupList

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;

    }

    public void setAccount(String account) {
        //need to connect to the database to check whether the account exist
        this.account = account;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setAvatar(Image avatar) {
        this.avatar = avatar;
    }

    public void setBirthdate(int birthdate) {
        this.birthdate = birthdate;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    //Function to manage friendList, blackList, GroupList

    public void addFriend(User newFriend) {
        this.friendList.add(newFriend);
    }

    public void deleteFriend(User fakeFriend){
        this.friendList.remove(fakeFriend);
    }

    public void addToBlackList(User newBlock) {
        this.blackList.add(newBlock);
    }

    public void deleteFromBlackList(User block) {
        this.blackList.remove(block);
    }

    public void addGroup(Group newGroup) {
        this.groupList.add(newGroup);
    }

    public void quitGroup(Group oldGroup) {
        this.groupList.remove(oldGroup);
    }

}
