import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * @author www.codejava.net
 */
public class ReadThread extends Thread {
    //private BufferedReader reader;
    private ObjectInputStream inputStream;
    private Socket socket;
    private ChatClient client;

    public ReadThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            // InputStream input = socket.getInputStream();
            //reader = new BufferedReader(new InputStreamReader(input));
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            try {
                System.out.println(inputStream.readObject());
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            } catch (ClassNotFoundException ex) {
                System.out.println("Class Not Found reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
        return;
        /*while (true) {
            try {*/
                /*
                String response = reader.readLine();
                System.out.println("\n" + response);
                // prints the username after displaying the server's message
                if (client.getUserName() != null) {
                    System.out.print("[" + client.getUserName() + "]: ");
                }*/
                /*String response = (String)inputStream.readObject(); //= inputStream.read();
                System.out.println(response);

                int num = (int) inputStream.readObject();


                for (int i = 0; i < num; i++) {
                    response = (String) inputStream.readObject();
                    System.out.println(response);

                    String name = (String) inputStream.readObject();
                    int uid = (int) inputStream.readObject();
                    InetAddress inetAddress = (InetAddress) inputStream.readObject();
                    String psw = (String) inputStream.readObject();
                    List<User> temp = null;
                    User friend = new User(name, uid, inetAddress, psw, temp, null);


                    client.addFriends(name, friend);
                    System.out.println("add friend successfully" + friend.getUsername());
                }

                // User friend = (User) inputStream.readObject();

                // if (friend != null) {

                // }

                // while ((a = inputStream.readObject()) != null){
                //    User friend = (User) a;
//                    client.addFriends(friend.getUsername() , friend);
//                    System.out.println("add friend successfully" + friend.getUsername());
                // }
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            } catch (ClassNotFoundException ex) {
                System.out.println("Class Not Found reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }*/
    }
}