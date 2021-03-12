
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class User implements Serializable {

    private transient String username;
    private transient int uid;
    private transient Socket socket;
    private transient String password;
    private transient InetAddress inetAddress;
    private transient int port;

    public User(String username, int uid, Socket socket, String password)
    {
        this.username = username;
        this.password = password;
        this.socket = socket;
        this.password = password;
        this.uid = uid;
//        this.inetAddress = socket.getInetAddress();
//        this.port = socket.getLocalPort();
    }

    public int getUid() {
        return uid;
    }

    public Socket getSocket() { return socket; }


    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public int getPort() {
        return socket.getLocalPort();
    }
}