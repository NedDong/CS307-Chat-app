package com.cs307group9.privatechatchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cs307group9.privatechatchat.ui.login.LoginActivity;
import com.google.firebase.database.DatabaseReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * https://www.tutorialspoint.com/sending-and-receiving-data-with-sockets-in-android
 **/

public class MainActivity extends AppCompatActivity {

    private DatabaseReference myDatabase;

    //static String hostname = "cs307-chat-app.webredirect.org";
    static String hostname = "cs307-chat-app.webredirect.org";
    static int port = 12345;

    Button sendButton;
    EditText sendText;
    TextView clientText;
    Button serverButton;

    String username;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Thread ServerThread = null;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_PASSWORD = "password";
    final String KEY_PREF_FRIENDLIST = "friendlist";
    final String KEY_PREF_ISLOGIN = "islogin";
    final String KEY_PREF_MUTE = "mute";

    boolean connectServer = true;
    String muteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.sendButton);
        sendText   = findViewById(R.id.editText);
        clientText = findViewById(R.id.text);
        serverButton = findViewById(R.id.serverButton);

        serverButton.setVisibility(View.INVISIBLE);

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, MODE_PRIVATE);

        username = sharedPreferences.getString(KEY_PREF_USERNAME, "");
        muteUser = sharedPreferences.getString(KEY_PREF_MUTE,"______");

        if (connectServer) {
            connectServer = false;
            clientText.setText("");
            ServerThread = new Thread(new ServerConnectThread());
            ServerThread.start();
        }
    }

    public void sendMessage(View view) {
        String sendMsg = sendText.getText().toString().trim();
        if (!sendMsg.isEmpty()) {
            new Thread(new ClientThread(sendMsg)).start();
        }
    }

    private PrintWriter output;
    private BufferedReader input;

    public void backButton(View view) {
        Intent intent = new Intent(MainActivity.this, MainScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        new Thread(new ClientThread("bye")).start();

        startActivity(intent);
        finish();
    }

    class ServerConnectThread implements Runnable {
        public void run() {
            System.out.println("==== I Am Currently Running Thread 1===");
            Socket socket;
            try {
                socket = new Socket(hostname, port);
                output = new PrintWriter(socket.getOutputStream(), true);
                input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { clientText.setText("Connected\n");
                    }
                });
                new Thread(new ServerMsgThread()).start();
                new Thread(new ClientThread(username)).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class ServerMsgThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String msg = input.readLine();
                    if (msg != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(msg + "\n");
                                System.out.println(muteUser + "\n");
                                if (!msg.contains("connected") && !msg.contains(muteUser))
                                    clientText.append(msg + "\n");
                            }
                        });
                    }
                    else {
                        ServerThread = new Thread(new ServerConnectThread());
                        ServerThread.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ClientThread implements Runnable {
        private String msg;
        ClientThread(String msg) { this.msg = msg; }
        @Override
        public void run() {
            System.out.println(msg);
            output.println(msg);
            if (msg == username) {
                sendText.setText("");
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clientText.append("[" + username + "] : " + msg + "\n");
                    sendText.setText("");
                }
            });
        }

    }


/*
    private String[] sortMsg(String msg) {
        if (msg.contains("=")) {
            String[] messages = new String[1];
            if (msg.contains(","))  messages = msg.split(",");
            else                    messages[0] = msg;

            int         size = messages.length;
            String[]    sendMessage = new String[size];
            long[]      order = new long[size];
            int         pos = 0;

            for (String i : messages) {
                String tmpMsg = i.split("=")[1];
                if (tmpMsg.contains("}"))
                    sendMessage[pos] = i.substring(0, i.length() - 1) + "\n";
                else
                    sendMessage[pos] = i + "\n";

                if (i.split("=")[0].contains("{") || i.split("=")[0].contains(" "))
                    order[pos] = Long.parseLong(i.split("=")[0].substring(1));
                else
                    order[pos] = Long.parseLong(i.split("=")[0]);

//                System.out.printf("String: %s\n", tmpMsg);
//                System.out.println("Order");
//                System.out.println(order);

                pos++;
            }

            quickSort(order, sendMessage, 0, size - 1);

            System.out.println("====================");
            for (String i : sendMessage) System.out.println(i);
            System.out.println("====================");

            return sendMessage;
        }
        String[] messages = {""};

        System.out.println("++++++++++++++++++++");
        System.out.println(messages);
        System.out.println("++++++++++++++++++++");

        return messages;
    }


    private void quickSort(long arr[], String[] msg, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(arr, msg, begin, end);

            quickSort(arr, msg, begin, partitionIndex-1);
            quickSort(arr, msg, partitionIndex+1, end);
        }
    }

    private int partition(long arr[], String[] msg, int begin, int end) {
        long pivot = arr[end];
        int i = (begin-1);

        for (int j = begin; j < end; j++) {
            if (arr[j] <= pivot) {
                i++;

                long swapTemp = arr[i];
                String swapTempMsg = msg[i];
                arr[i] = arr[j];
                msg[i] = msg[j];
                arr[j] = swapTemp;
                msg[j] = swapTempMsg;
            }
        }

        long swapTemp = arr[i+1];
        String swapTempMsg = msg[i+1];
        arr[i+1] = arr[end];
        msg[i+1] = msg[end];
        arr[end] = swapTemp;
        msg[end] = swapTempMsg;

        return i+1;
    }
    */

}