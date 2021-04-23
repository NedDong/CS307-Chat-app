package com.cs307group9.privatechatchat.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cs307group9.privatechatchat.MainActivity;
import com.cs307group9.privatechatchat.MainScreenActivity;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GroupChat extends AppCompatActivity {

    private ImageButton backButton;
    private ImageButton moreButton;

    private DatabaseReference myDatabase;

    //static String hostname = "cs307-chat-app.webredirect.org";
    static String hostname = "10.0.2.2";
    //"cs307-chat-app.webredirect.org";
    static int port = 1111;

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

    final String KEY_PREF_SOCKET = "socket";
    final String KEY_PREF_BLOCK = "block";
    final String LIST = "LIST";

    final String KEY_PREF_FRIENDLIST_NAME = "friendlist_name";
    final String KEY_PREF_FRIENDLIST_UID  = "friendlist_uid";
    final String KEY_PREF_FRIENDLIST_ADDR = "friendlist_addr";
    final String KEY_PREF_FRIENDLIST_PSW  = "friendlist_psw";

    private String[] userName;
    private String[] psw;
    private int[] uid;
    private InetAddress[] addr;
    private LinkedList<String> textString = new LinkedList<>();

    List<Map<String, Object>> list_item = new ArrayList<Map<String, Object>>();

    private int[] highlightNum = new int[100];

    boolean connectServer = true;
    String muteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_chat);

        sendButton = findViewById(R.id.g_sendButton);
        sendText   = findViewById(R.id.g_editText);
//        clientText = findViewById(R.id.text);
        moreButton = (ImageButton) findViewById(R.id.infoButton);
        backButton = (ImageButton) findViewById(R.id.g_ChatBackButton);

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, MODE_PRIVATE);

        username = sharedPreferences.getString(KEY_PREF_USERNAME, "");
        muteUser = sharedPreferences.getString(KEY_PREF_MUTE,"_____");

        if (connectServer) {
            connectServer = false;
//            clientText.setText("");
            ServerThread = new Thread(new ServerConnectThread());
            ServerThread.start();
        }

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChat.this, GroupInfo.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // output.println("bye");

                startActivity(intent);
                //finish()
            }
        });
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
        Intent intent = new Intent(GroupChat.this, MainScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

//        new Thread(new ClientThread("bye")).start();

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
                                // Get Current User
                                System.out.printf("MASSAGE: %s\n", msg);
                                if (msg.contains("connected") || msg.contains(muteUser)) {System.out.println("connected\n");}
                                else {
                                    System.out.println("HERE");
                                    User currentSender;
                                    String msg_end = msg.split("] ")[1];
                                    System.out.println(msg_end);

                                    String uName = msg.split("\\[")[1].split("] ")[0];

                                    if (uName.equals("HIGHLIGHT")) {
                                        highlightNum[Integer.parseInt(msg_end)] = 1;
                                        return;
                                    }
                                    else if (msg_end.equals(uName)) {}
                                    else {
                                        currentSender = getUsers(uName);
                                        System.out.println("AHA");
                                        if (currentSender != null) {
                                            UpdateChatList(currentSender, msg_end);
                                        } else {
                                            UpdateChatList(new User("UNKNOWN", -1, null, "N/A"), msg_end);
                                        }
                                    }
                                }
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

    void UpdateChatList(User user, String msg) {
        String name = user.getUsername();

        Map<String, Object> show_item = new HashMap<>();
        show_item.put("name", name);
        show_item.put("msg", msg);
        show_item.put("image", R.mipmap.ic_launcher);
//        show_item.put("background", R.color.black);
        list_item.add(show_item);

        textString.add(msg);

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, list_item, R.layout.message_adapter,
                new String[]{"name", "msg", "image"}, new int[]{R.id.name, R.id.msg, R.id.imgtou});
        ListView listView = (ListView) findViewById(R.id.g_send_list);
        if (listView == null) Log.d("dubug", "ListView Null");
        listView.setAdapter(simpleAdapter);

        for (int i = 0; i < listView.getCount(); i++) {
            if (listView.findViewWithTag(i) == null) continue;
            if (listView.findViewWithTag(i).isSelected())
                listView.findViewWithTag(i).setBackgroundColor(getResources().getColor(R.color.yellow));
        }

        listView.setOnItemClickListener(this::onItemClick);
//        listView.set
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        view.setSelected(true);
        view.setTag(position);
        if (highlightNum[position] == 1) {
            highlightNum[position] = 0;
            view.setBackgroundColor(getResources().getColor(R.color.white));

            return;
        }
        view.setBackgroundColor(getResources().getColor(R.color.yellow));
        highlightNum[position] = 1;
//        new Thread(new ClientThread("[HIGHLIGHT] " + position)).start();
    }


    class ClientThread implements Runnable {
        private String msg;
        ClientThread(String msg) { this.msg = msg; }
        @Override
        public void run() {
            System.out.println(msg);

            if (msg.contains("[HIGHLIGHT] ")) {
                int pos = Integer.parseInt(msg.split("] ")[1]);
                output.println("HIGHLIGHT");
                output.println(pos);
                return;
            }

            output.println(username);
            if (msg.equals(username)) {
                sendText.setText("");
                return;
            }
            output.println(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    User me = getUsers(username);
                    UpdateChatList(me, msg);
                    sendText.setText("");
                }
            });
        }
    }

    User getUsers(String find_user) {
        new Thread(new DatabaseConnect()).start();

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String jsonName = sharedPreferences.getString(KEY_PREF_FRIENDLIST_NAME, "");
        String jsonPsw  = sharedPreferences.getString(KEY_PREF_FRIENDLIST_PSW, "");
        String jsonUid  = sharedPreferences.getString(KEY_PREF_FRIENDLIST_UID, "");
        String jsonAddr = sharedPreferences.getString(KEY_PREF_FRIENDLIST_ADDR, "");

        userName = (String[]) gson.fromJson(jsonName, new TypeToken<String[]>(){}.getType());
        psw = (String[]) gson.fromJson(jsonPsw, new TypeToken<String[]>(){}.getType());
        uid = (int[]) gson.fromJson(jsonUid, new TypeToken<int[]>(){}.getType());
        addr = (InetAddress[]) gson.fromJson(jsonAddr, new TypeToken<InetAddress[]>(){}.getType());

        for (int i = 0; i < userName.length; i++) {
            System.out.printf("USER[%d] = %s\n", i, userName[i]);
            if (!userName[i].equals(find_user)) continue;
            return new User(userName[i], uid[i], addr[i], psw[i]);
        }

        return null;
    }

    private ObjectOutputStream outputData;
    private ObjectInputStream inputData;

    class DatabaseConnect implements Runnable {
        public void run() {
//            System.out.println("==== I Am Currently Running Thread 1===");
            Socket socket;
            try {
                socket = new Socket("cs307-chat-app.webredirect.org", 12345);
                outputData = new ObjectOutputStream(socket.getOutputStream());
                inputData  = new ObjectInputStream(socket.getInputStream());

                new Thread(new RecieveFriendList()).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class RecieveFriendList implements Runnable {
        @Override
        public void run() {
            try {
                outputData.writeObject("LIST");

                int num = (int) inputData.readObject();

                String[] name = new String[num];
                int[] uid = new int[num];
                InetAddress[] inetAddress = new InetAddress[num];
                String[] psw = new String[num];

                for (int i = 0; i < num; i++) {
                    String response = (String) inputData.readObject();
//                    System.out.println(response);

                    name[i] = (String) inputData.readObject();
                    uid[i] = (int) inputData.readObject();
                    inetAddress[i] = (InetAddress) inputData.readObject();
                    psw[i] = (String) inputData.readObject();
                }

                Gson gson = new Gson();

                String json = gson.toJson(name);
                editor.putString(KEY_PREF_FRIENDLIST_NAME, json);
                json = gson.toJson(uid);
                editor.putString(KEY_PREF_FRIENDLIST_UID, json);
                json = gson.toJson(inetAddress);
                editor.putString(KEY_PREF_FRIENDLIST_ADDR, json);
                json = gson.toJson(psw);
                editor.putString(KEY_PREF_FRIENDLIST_PSW, json);

                editor.commit();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
