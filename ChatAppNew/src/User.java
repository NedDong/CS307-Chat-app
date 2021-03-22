
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class User implements Serializable {

    private transient String username;
    private transient int uid;
    // private transient Socket socket;
    private transient String password;
    private transient InetAddress inetAddress;
    private transient int port;

    public User(String username, int uid, InetAddress inetAddress, String password)
    {
        this.username = username;
        this.password = password;
        this.inetAddress = inetAddress;
        this.password = password;
        this.uid = uid;
    }

    public void setUsername(String newUsername)
    {
        this.username = newUsername;
    }

    public int getUid() {
        return uid;
    }


    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }
}