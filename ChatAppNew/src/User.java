
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.HashMap;

public class User implements Serializable {

    private transient String username;
    private transient int uid;
    // private transient Socket socket;
    private transient String password;
    private transient InetAddress inetAddress;
    private transient int port;
    private transient HashMap<Integer, User> friendList;
    private transient HashMap<Integer, User> blockedList;

    public User(String username, int uid, InetAddress inetAddress, String password)
    {
        this.username = username;
        this.password = password;
        this.inetAddress = inetAddress;
        this.password = password;
        this.uid = uid;
    }

    public User(String username, int uid, String password) {
        this.username = username;
        this.uid = uid;
        this.password = password;
    }

    public void setUsername(String newUsername)
    {
        this.username = newUsername;
    }

    public int getUid() {
        return uid;
    }

    public HashMap<Integer, User> getFriendList() {return friendList;}

    public HashMap<Integer, User> getBlockedList() {return blockedList;}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {this.password = password;}

    public String getUsername() {
        return username;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void addFriend(User friend) {
        friendList.put(friend.getUid(),friend);
    }

    public void removeFriend(User friend) throws NoSuchObjectException {
        if(friendList.containsKey(friend.getUid())) {
            friendList.remove(friend.getUid());
        } else {
            throw new NoSuchObjectException("No such user in friend list");
        }
    }

    public void addBlockedList(User user) {
        if(friendList.containsKey(user.getUid())) {
            try {
                removeFriend(user);
            } catch (NoSuchObjectException e) {
                e.printStackTrace();
            }
            blockedList.put(user.getUid(), user);
        } else {
            blockedList.put(user.getUid(), user);
        }
    }
}