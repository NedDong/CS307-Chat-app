package com.cs307group9.privatechatchat.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cs307group9.privatechatchat.MainScreenActivity;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.entity.UserAdapter;
import com.cs307group9.privatechatchat.ui.home.MessageFragment;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;
import com.cs307group9.privatechatchat.ui.notifications.ProfileFragment;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class GroupCreate extends AppCompatActivity {

    Button createButton, backButton;
    EditText groupName;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_USERID   = "userid";
    final String KEY_PREF_PASSWORD = "password";
    final String KEY_PREF_FEEDBACK = "feedback";

    final String KEY_PREF_FRIENDLIST_NAME = "friendlist_name";
    final String KEY_PREF_FRIENDLIST_UID  = "friendlist_uid";
    final String KEY_PREF_FRIENDLIST_ADDR = "friendlist_addr";
    final String KEY_PREF_FRIENDLIST_PSW  = "friendlist_psw";

    final String KEY_PREF_GROUPLIST_NAME = "grouplist_name";
    final String KEY_PREF_GROUPLIST_GID = "grouplist_gid";
    final String KEY_PREF_CURRENT_GROUP_USERS_ID = "grouplist_users_id";
    final String KEY_PREF_CURRENT_GROUP_USERS_NAME = "groplist_users_name";

    final String KEY_PREF_CURRENT_GROUP_ID = "current_gid";

    View view;

    private LinkedList<User> userData = null;
    private Context userContext;
    private UserAdapter userAdapter = null;
    private ListView listView;

    private String[] userName, groupUserName;
    private String[] psw;
    private int[] uid, groupList, groupUserID;
    private InetAddress[] addr;

    private String username;

    static String hostname = //"cs307-chat-app.webredirect.org";
            //="cs307-chat-app.webredirect.org";
            "10.0.2.2";
    int type = -1; // 0 means LogIn, 1 means Register
    static int port = 12345;

    int cur_gid;

    private ObjectOutputStream output;
    private ObjectInputStream input;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    int cur_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_page);

        backButton = findViewById(R.id.backToGroupList);
        createButton = findViewById(R.id.CreateGroupButton);
        groupName = findViewById(R.id.CreateGroupName);

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupCreate.this, MainScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        cur_uid = sharedPreferences.getInt(KEY_PREF_USERID, -1);

        //if (!groupName.getText().toString().equals("")) {
            createButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    System.out.printf("GROUPNAME + %s\n", groupName.getText().toString());
                    new Thread(new ServerConnectThread()).start();
                }
            });
        //}
        //else {
        //    Toast.makeText(getApplicationContext(), "PLEASE ENTER GROUP NAME!", Toast.LENGTH_SHORT);
        //}
    }

    class ServerConnectThread implements Runnable {
        public void run() {
            System.out.println("==== I Am Currently Running Thread 1===");

            try {
                Socket socket = new Socket(hostname, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());

                new Thread(new CreateGroupThread()).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class CreateGroupThread implements Runnable {
        @Override
        public void run() {
            try {
                output.writeObject("CreateGroup");
                output.writeObject(groupName.getText().toString());
                output.writeObject("" + cur_uid);

                String result = (String) input.readObject();

                if (result.contains("DUPLICATED")) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            final Toast toast = Toast.makeText(GroupCreate.this, result, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                    return;
                }

                cur_gid = (int) input.readObject();

                editor.putInt(KEY_PREF_CURRENT_GROUP_ID, cur_gid);

                runOnUiThread(new Runnable() {
                    public void run() {
                        final Toast toast = Toast.makeText(GroupCreate.this, "SUCCESSFULLY CREATE", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });

                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
