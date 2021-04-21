package com.cs307group9.privatechatchat.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.cs307group9.privatechatchat.FriendProfile;
import com.cs307group9.privatechatchat.MainScreenActivity;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.entity.UserAdapter;
import com.cs307group9.privatechatchat.ui.dashboard.ContactViewModel;
import com.cs307group9.privatechatchat.ui.home.MessageFragment;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GroupInfo extends AppCompatActivity {

    private ImageButton back;

    private ContactViewModel dashboardViewModel;

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_PASSWORD = "password";
    final String KEY_PREF_ISLOGIN = "islogin";
    final String KEY_PREF_SOCKET = "socket";
    final String KEY_PREF_MUTE = "mute";
    final String KEY_PREF_BLOCK = "block";
    final String LIST = "LIST";

    final String KEY_PREF_FRIENDLIST_NAME = "friendlist_name";
    final String KEY_PREF_FRIENDLIST_UID  = "friendlist_uid";
    final String KEY_PREF_FRIENDLIST_ADDR = "friendlist_addr";
    final String KEY_PREF_FRIENDLIST_PSW  = "friendlist_psw";

    final String KEY_PREF_GROUPLIST_NAME = "grouplist_name";
    final String KEY_PREF_GROUPLIST_GID = "grouplist_gid";
    final String KEY_PREF_CURRENT_GROUP_USERS_ID = "grouplist_users_id";
    final String KEY_PREF_CURRENT_GROUP_USERS_NAME = "groplist_users_name";

    final String KEY_PREF_CURRENT_GROUP_ID = "current_gid";

    LinearLayout lin;

    private LinkedList<User> userData = null;
    private Context userContext;
    private UserAdapter userAdapter = null;
    private ListView listView;

    private String[] userName, group_username;
    private String[] psw;
    private int[] uid, group_uid;
    private InetAddress[] addr;

    Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    static String hostname = "cs307-chat-app.webredirect.org";
            //="cs307-chat-app.webredirect.org";
            //"10.0.2.2";
    int type = -1; // 0 means LogIn, 1 means Register
    static int port = 12345;

    int cur_gid;
    String cur_gName;

    String[] groupListName;

    TextView idText, nameText;

    LinkedList<User> users;
    List<Map<String, Object>> list_item = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        back = (ImageButton) findViewById(R.id.groupInfoBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfo.this, GroupChat.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        Gson gson = new Gson();

        String jsonName = sharedPreferences.getString(KEY_PREF_FRIENDLIST_NAME, "");
        String jsonPsw  = sharedPreferences.getString(KEY_PREF_FRIENDLIST_PSW, "");
        String jsonUid  = sharedPreferences.getString(KEY_PREF_FRIENDLIST_UID, "");
        String jsonAddr = sharedPreferences.getString(KEY_PREF_FRIENDLIST_ADDR, "");
        String jsonGroupListName = sharedPreferences.getString(KEY_PREF_GROUPLIST_NAME, "");

        String jsonGroupUserId = sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_USERS_ID, "");
        String jsonGroupUserName = sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_USERS_NAME, "");

        userName = (String[]) gson.fromJson(jsonName, new TypeToken<String[]>(){}.getType());
        psw = (String[]) gson.fromJson(jsonPsw, new TypeToken<String[]>(){}.getType());
        uid = (int[]) gson.fromJson(jsonUid, new TypeToken<int[]>(){}.getType());
        addr = (InetAddress[]) gson.fromJson(jsonAddr, new TypeToken<InetAddress[]>(){}.getType());
        group_uid = (int[]) gson.fromJson(jsonGroupUserId, new TypeToken<int[]>(){}.getType());
        group_username = (String[]) gson.fromJson(jsonGroupUserName, new TypeToken<String[]>(){}.getType());

        groupListName = (String[]) gson.fromJson(jsonGroupListName, new TypeToken<String[]>(){}.getType());

        cur_gid = sharedPreferences.getInt(KEY_PREF_CURRENT_GROUP_ID, -1);
        if (cur_gid != -1) cur_gName = groupListName[cur_gid - 1];
        else cur_gName = "null";

        idText = findViewById(R.id.GroupID);
        nameText = findViewById(R.id.GroupInfoName);

        idText.setText("" + cur_gid);
        nameText.setText(cur_gName);

        new Thread(new ServerConnectThread());

        users = new LinkedList<>();

        if (group_uid == null) {
            group_uid = new int[0];
            group_username = new String[0];
        }

        for (int i = 0; i < group_uid.length; i++) {
            users.add(new User(group_username[i], group_uid[i], addr[0], ""));
            users.get(i).printUser();
        }

        for (int i = 0; i < group_uid.length; i++) {
            Map<String, Object> show_item = new HashMap<String, Object>();
            show_item.put("name", group_username[i]);
            show_item.put("says", group_uid[i]);
            show_item.put("image", R.mipmap.ic_launcher);
            list_item.add(show_item);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, list_item, R.layout.friend_list_adapter,
                new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
        ListView listView = (ListView) findViewById(R.id.GroupUserList);
        if (listView == null) Log.d("dubug", "ListView Null");
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(this::onItemClick);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(GroupInfo.this, FriendProfile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    class ServerConnectThread implements Runnable {
        public void run() {
            System.out.println("==== I Am Currently Running Thread 1===");

            try {
                socket = new Socket(hostname, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());

                new Thread(new getGroupUserList()).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class getGroupUserList implements Runnable {
        @Override
        public void run() {
            try {
                output.writeObject("GetGroupMembers");
                output.writeObject("" + cur_gid);
                output.writeObject("-1");

                int num = (int) input.readObject();
                System.out.printf("RESULT: %d\n", num);
                group_uid = new int[num];
                group_username = new String[num];

                for (int i = 0; i < num; i++) {
                    group_uid[i] = (int) input.readObject();
                    System.out.printf("==================\n group_uid: %d\n===========\n", group_uid[i]);
                    group_username[i] = userName[group_uid[i]];
                    UpdateGroupList(group_uid[i], group_username[i]);
                }

                Gson gson = new Gson();
                String json = gson.toJson(group_uid);
                editor.putString(KEY_PREF_CURRENT_GROUP_USERS_ID, json);
                json = gson.toJson(group_username);
                editor.putString(KEY_PREF_CURRENT_GROUP_USERS_NAME, json);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void UpdateGroupList(int id, String name) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Map<String, Object> show_item = new HashMap<>();
                show_item.put("name", name);
                show_item.put("says", id + "");
                show_item.put("image", R.mipmap.ic_launcher);

                list_item.add(show_item);

                SimpleAdapter simpleAdapter = new SimpleAdapter(GroupInfo.this, list_item, R.layout.friend_list_adapter,
                        new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
                ListView listView = (ListView) findViewById(R.id.GroupUserList);
                if (listView == null) Log.d("dubug", "ListView Null");
                listView.setAdapter(simpleAdapter);
                // Stuff that updates the UI
                System.out.println("=============================");
                listView.setOnItemClickListener(this::onItemClick);
            }

            private void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Gson gson = new Gson();
                String json = gson.toJson(group_uid[position]);
                cur_gid = group_uid[position];
                editor.putString(KEY_PREF_CURRENT_GROUP_ID, json);
                editor.commit();

                System.out.printf("CURRENT GROUP ID: %d\n", cur_gid);


//                new Thread(new ServerConnectThread()).start();
//                new Thread(new getGroupUserList()).start();

                Intent intent = new Intent(GroupInfo.this, GroupChat.class);
                startActivity(intent);
            }
        });

    }

}