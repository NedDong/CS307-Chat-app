package com.cs307group9.privatechatchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * https://www.tutorialspoint.com/sending-and-receiving-data-with-sockets-in-android
 **/

public class MainActivity extends AppCompatActivity {

    private DatabaseReference myDatabase;

    static String hostname = "10.0.2.2"; //"cs307-chat-app.webredirect.org";
    //    static String hostname = "10.0.2.2";
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
    final String KEY_PREF_FRIENDLIST_UID = "friendlist_uid";
    final String KEY_PREF_FRIENDLIST_ADDR = "friendlist_addr";
    final String KEY_PREF_FRIENDLIST_PSW = "friendlist_psw";
    final String KEY_PREF_USER_AVATAR = "user_avatar";
    final String KEY_PREF_USER_BG = "user_bg";

    private String[] userName;
    private String[] psw;
    private int[] uid;
    private InetAddress[] addr;
    private LinkedList<String> textString = new LinkedList<>();

    List<Map<String, Object>> list_item = new ArrayList<Map<String, Object>>();

    private int[] highlightNum = new int[100];

    boolean connectServer = true;
    String muteUser;

    boolean ban = false;
    ImageView bg;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);

        init();
        String uri = sharedPreferences.getString(KEY_PREF_USER_BG, "");
        if (uri.length() > 0) {
            getIntent().addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                getContentResolver().takePersistableUriPermission(Uri.parse(uri), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            }
            bg.setImageURI(Uri.parse(uri));
        }
        Log.e("Chat Background Now is ", uri);

    }

    private void init() {
        sendButton = findViewById(R.id.sendButton);
        sendText = findViewById(R.id.editText);
        bg = findViewById(R.id.chatBackground);
        back = findViewById(R.id.ChatBackButton);
//        clientText = findViewById(R.id.text);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        ban = sharedPreferences.getBoolean("BAN", false);

        String uri = sharedPreferences.getString(KEY_PREF_USER_BG, "");
        if (uri.length() > 0) {
            getIntent().addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                getContentResolver().takePersistableUriPermission(Uri.parse(uri), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            }
            bg.setImageURI(Uri.parse(uri));
        }
        username = sharedPreferences.getString(KEY_PREF_USERNAME, "");
        muteUser = sharedPreferences.getString(KEY_PREF_MUTE, "_____");

        if (connectServer) {
            connectServer = false;
//            clientText.setText("");
            ServerThread = new Thread(new ServerConnectThread());
            ServerThread.start();
        }
    }


    public void sendMessage(View view) {
        String message = sendText.getText().toString().trim();
        message = message.replaceAll("shit", "****");
        message = message.replaceAll("fuck", "****");
        message = message.replaceAll("bitch", "****");
        if (!message.isEmpty()) {
            new Thread(new ClientThread(message)).start();
        }
    }

    public void writeToFile(String chat, String name) {
        /*try {
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(
                    "/PrivateChatChat/app/src/main/java/com/cs307group9/privatechat/chatchathistory.txt",
                    Context.MODE_PRIVATE));
            System.out.println("Inside write to file!");
            osw.write(chat);
            osw.close();
        }
        catch (IOException e) {
            System.out.println("File write failed!");
        }*/
        //File file = getFileStreamPath("chatchathistory.txt");
        /*
        try {
            File root = new File("/Users/xuzijuan/Documents/GitHub/CS307-Chat-app/PrivateChatChat/app/src/main/java/com/cs307group9/privatechatchat/chathistory.txt");


            //String nf = "chathistory.txt";

            //File file = new File(root, nf);

            FileWriter fw = new FileWriter(root);
            fw.append(chat);
            fw.flush();
            fw.close();

        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }*/
        String filename = "chathistory.txt";
        //String fileContents = "Hello world!";
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(filename, MODE_APPEND);
            chat = name + ": " + chat + "\n";
            fos.write(chat.getBytes());
            //Toast.makeText(this, "Saved to " + getFilesDir() + "/" + filename, Toast.LENGTH_SHORT).show();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
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
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() { clientText.setText("Connected\n"); }
//                });
                new Thread(new ServerMsgThread()).start();
                if (!ban) new Thread(new ClientThread(username)).start();
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
                                if (msg.contains("connected") || msg.contains(muteUser)) {
                                    System.out.println("connected\n");
                                } else {
                                    System.out.println("HERE");
                                    User currentSender;
                                    String msg_end = msg.split("] ")[1];
                                    System.out.println(msg_end);

                                    String uName = msg.split("\\[")[1].split("] ")[0];

                                    if (uName.equals("HIGHLIGHT")) {
                                        highlightNum[Integer.parseInt(msg_end)] = 1;
                                        return;
                                    } else if (msg_end.equals(uName)) {
                                    } else {
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
                    } else {
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

        writeToFile(msg, name);
        //deleteChathistory();

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, list_item, R.layout.message_adapter,
                new String[]{"name", "msg", "image"}, new int[]{R.id.name, R.id.msg, R.id.imgtou});
        ListView listView = (ListView) findViewById(R.id.send_list);
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

        ClientThread(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            System.out.println(msg);
            //writeToFile(msg);

            if (msg.contains("[HIGHLIGHT] ")) {
                int pos = Integer.parseInt(msg.split("] ")[1]);
                output.println("HIGHLIGHT");
                output.println(pos);
                return;
            }

            if (msg.contains("BAD WORD")) {
                ban = true;
                editor.putBoolean("BAN", true);
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
        String jsonPsw = sharedPreferences.getString(KEY_PREF_FRIENDLIST_PSW, "");
        String jsonUid = sharedPreferences.getString(KEY_PREF_FRIENDLIST_UID, "");
        String jsonAddr = sharedPreferences.getString(KEY_PREF_FRIENDLIST_ADDR, "");

        userName = (String[]) gson.fromJson(jsonName, new TypeToken<String[]>() {
        }.getType());
        psw = (String[]) gson.fromJson(jsonPsw, new TypeToken<String[]>() {
        }.getType());
        uid = (int[]) gson.fromJson(jsonUid, new TypeToken<int[]>() {
        }.getType());
        addr = (InetAddress[]) gson.fromJson(jsonAddr, new TypeToken<InetAddress[]>() {
        }.getType());

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
                inputData = new ObjectInputStream(socket.getInputStream());

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
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
