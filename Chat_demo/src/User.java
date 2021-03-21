
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

    /*
    public static void getUserPassword(Connection con) throws SQLException {
    String query = "select Password from Users where UserName = '" + username + "'";
    try (Statement stmt = con.createStatement()) {
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        String password = rs.getString("Password");
        System.out.println(password);
      }
    } catch (SQLException e) {
      JDBCTutorialUtilities.printSQLException(e);
    }
  }

    public static void getUserPasswordUID(Connection con) throws SQLException {
        String query = "select Password from Users where UID = '" + Uid + "'";
        try(Statement stmt = con.createStetement()) {
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                String password = rs.getString("Password");
                System.out.println(password);
            }
        } catch (SQLException e) {
            JDBCTutorialUtilities.printSQLException(e);
        }
     }
     */
}