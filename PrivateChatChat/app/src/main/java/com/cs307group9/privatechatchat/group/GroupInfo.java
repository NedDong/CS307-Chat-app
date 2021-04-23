package com.cs307group9.privatechatchat.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.ImageView;
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
    private ImageView avatar;

    private Button settings;

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
    final String KEY_PREF_CURRENT_SELECT_USER_ID = "select_user_id";

    LinearLayout lin;

    private LinkedList<User> userData = null;
    private Context userContext;
    private UserAdapter userAdapter = null;
    private ListView listView;

    private String[] userName, group_username;
    private String[] psw;
    private int[] uid, group_uid, groupUserID, groupListId;
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

    int select_uid;

    String[] groupListName;

    TextView idText, nameText;

    LinkedList<User> users;
    List<Map<String, Object>> list_item = new ArrayList<Map<String, Object>>();

    boolean first_in = false;

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
                finish();
            }
        });

        settings = (Button) findViewById(R.id.groupSettings);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfo.this, GroupSettings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        avatar = (ImageView) findViewById(R.id.GroupAvatar);

        Gson gson = new Gson();

        String jsonName = sharedPreferences.getString(KEY_PREF_FRIENDLIST_NAME, "");
        String jsonPsw  = sharedPreferences.getString(KEY_PREF_FRIENDLIST_PSW, "");
        String jsonUid  = sharedPreferences.getString(KEY_PREF_FRIENDLIST_UID, "");
        String jsonAddr = sharedPreferences.getString(KEY_PREF_FRIENDLIST_ADDR, "");
        String jsonGroupListName = sharedPreferences.getString(KEY_PREF_GROUPLIST_NAME, "");
        String jsonGroupListID = sharedPreferences.getString(KEY_PREF_GROUPLIST_GID, "");

        String jsonGroupUserId = sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_USERS_ID, "");
        String jsonGroupUserName = sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_USERS_NAME, "");

        userName = (String[]) gson.fromJson(jsonName, new TypeToken<String[]>(){}.getType());
        psw = (String[]) gson.fromJson(jsonPsw, new TypeToken<String[]>(){}.getType());
        uid = (int[]) gson.fromJson(jsonUid, new TypeToken<int[]>(){}.getType());
        addr = (InetAddress[]) gson.fromJson(jsonAddr, new TypeToken<InetAddress[]>(){}.getType());
        group_uid = (int[]) gson.fromJson(jsonGroupUserId, new TypeToken<int[]>(){}.getType());
        group_username = (String[]) gson.fromJson(jsonGroupUserName, new TypeToken<String[]>(){}.getType());

        groupListName = (String[]) gson.fromJson(jsonGroupListName, new TypeToken<String[]>(){}.getType());
        groupListId = (int[]) gson.fromJson(jsonGroupListID, new TypeToken<int[]>(){}.getType());
        cur_gid = sharedPreferences.getInt(KEY_PREF_CURRENT_GROUP_ID, -1);
        Log.e("GroupInfo", "CURRENT GROUP ID = " + groupListId[cur_gid]);

        for (int i = 0; i < groupListName.length; i++) {
            Log.e("GroupInfo", "CURRENT GROUP NAME = " + groupListName[i]);
        }

        if (cur_gid != -1) cur_gName = groupListName[cur_gid];
        else cur_gName = "null";

        cur_gid = groupListId[cur_gid];

        idText = findViewById(R.id.GroupID);
        nameText = findViewById(R.id.GroupInfoName);

        idText.setText("" + cur_gid);
        nameText.setText(cur_gName);

        Log.e("GroupInfo","------In GROUP INFO ------");

        if (!first_in) {
            new Thread(new ServerConnectThreadMember()).start();
            first_in = true;
        }

//        users = new LinkedList<>();
//
//        if (group_uid == null) {
//            group_uid = new int[0];
//            group_username = new String[0];
//        }
//
//        for (int i = 0; i < group_uid.length; i++) {
//            users.add(new User(group_username[i], group_uid[i], addr[0], ""));
//            users.get(i).printUser();
//        }
//
//        for (int i = 0; i < group_uid.length; i++) {
//            Map<String, Object> show_item = new HashMap<String, Object>();
//            show_item.put("name", group_username[i]);
//            show_item.put("says", group_uid[i]);
//            show_item.put("image", R.mipmap.ic_launcher);
//            list_item.add(show_item);
//        }
//
//        SimpleAdapter simpleAdapter = new SimpleAdapter(this, list_item, R.layout.friend_list_adapter,
//                new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
//        ListView listView = (ListView) findViewById(R.id.GroupUserList);
//        if (listView == null) Log.d("dubug", "ListView Null");
//        listView.setAdapter(simpleAdapter);
//
//        listView.setOnItemClickListener(this::onItemClick);
    }

    public void checkAdministrators(View view) {
        list_item = new ArrayList<Map<String, Object>>();
        new Thread(new ServerConnectThreadManager()).start();
    }

    public void checkMembers(View view) {
        list_item = new ArrayList<Map<String, Object>>();
        new Thread(new ServerConnectThreadMember()).start();
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(GroupInfo.this, FriendProfile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    class ServerConnectThreadMember implements Runnable {
        public void run() {
            Log.e("GroupInfo","===== Find User Thread ====");
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

    class ServerConnectThreadManager implements Runnable {
        public void run() {
            Log.e("GroupInfo","===== Find Administrator Thread ====");

            try {
                socket = new Socket(hostname, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());

                new Thread(new getGroupAdministratorList()).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    void UpdateGroupList(int id, String name) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Map<String, Object> show_item = new HashMap<String, Object>();
                show_item.put("name", name);
                show_item.put("says", id);
                show_item.put("image", R.mipmap.ic_launcher);
                list_item.add(show_item);

                SimpleAdapter simpleAdapter = new SimpleAdapter(GroupInfo.this, list_item, R.layout.friend_list_adapter,
                        new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
                ListView listView = (ListView) findViewById(R.id.GroupUserList);
                if (listView == null) Log.d("dubug", "ListView Null");
                listView.setAdapter(simpleAdapter);

                listView.setOnItemClickListener(this::onItemClick);
            }

            private void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Gson gson = new Gson();
                String json = gson.toJson(groupUserID[position]);
                select_uid = groupUserID[position];
                editor.putInt(KEY_PREF_CURRENT_SELECT_USER_ID, select_uid);
                editor.commit();

                System.out.printf("CURRENT GROUP ID: %d\n", cur_gid);

                Intent intent = new Intent(GroupInfo.this, GroupChat.class);
                startActivity(intent);
            }
        });
    }

    class getGroupAdministratorList implements Runnable {
        @Override
        public void run() {
            try {
                output.writeObject("GetGroupManagers");
                output.writeObject("" + cur_gid);
                output.writeObject("-1");

                String read_user;

                ArrayList<Integer> temp_groupUserId = new ArrayList<>();
                ArrayList<String> temp_groupUserName = new ArrayList<>();
                ArrayList<String> temp_groupUserAvatar = new ArrayList<>();

                Log.e("GroupInfo", "START SEARCHING MANAGER IN GROUP[" + cur_gName + "]");

                int k = 0;
                while (!(read_user = "" + input.readObject()).equals("**FINISHED**")) {
                    if (read_user.equals("NO SUCH GROUP")) {
                        Log.e("GroupInfo","NO GROUP");
                        input.readObject();
                        return;
                    }
                    temp_groupUserId.add(Integer.parseInt(read_user));
                    temp_groupUserName.add("" + input.readObject());
                    temp_groupUserAvatar.add("" + input.readObject());

                    Log.e("GroupInfo", "GROUP LIST: USERID[" + temp_groupUserId.get(k)
                            + "] USERNAME[" + temp_groupUserName.get(k)
                            + "] AVATAR[" + temp_groupUserAvatar.get(k) + "]");
                    UpdateGroupList(temp_groupUserId.get(k), temp_groupUserName.get(k));
                    k++;
                }
            } catch (Exception e) {
                e.printStackTrace();
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

                String read_user;

                ArrayList<Integer> temp_groupUserId = new ArrayList<>();
                ArrayList<String> temp_groupUserName = new ArrayList<>();
                ArrayList<String> temp_groupUserAvatar = new ArrayList<>();

                Log.e("GroupInfo", "Start Searching With ID = " + cur_gid);

                while (!(read_user = "" + input.readObject()).equals("**FINISHED**")) {
                    if (read_user.equals("NO SUCH GROUP")) {
                        Log.e("GroupInfo","NO GROUP");
                        input.readObject();
                        return;
                    }
                    temp_groupUserId.add(Integer.parseInt(read_user));
                    temp_groupUserName.add("" + input.readObject());
                    temp_groupUserAvatar.add("" + input.readObject());
                }

                int num = temp_groupUserId.size();

                groupUserID = new int[num];
                group_username = new String[num];

                Log.e("GroupInfo","GROUP MEMBER NUM = " + num);

                for (int i = 0; i < num; i++) {
                    groupUserID[i] = temp_groupUserId.get(i);
                    group_username[i] = temp_groupUserName.get(i);
                    Log.e("GroupInfo", "GROUP LIST: USERID[" + groupUserID[i]
                            + "] USERNAME[" + group_username[i] + "]"
                            + "] AVATAR[" + temp_groupUserAvatar.get(i) + "]");
                    UpdateGroupList(groupUserID[i], group_username[i]);
                }

                Gson gson = new Gson();
                String json = gson.toJson(groupUserID);
                editor.putString(KEY_PREF_CURRENT_GROUP_USERS_ID, json);
                json = gson.toJson(group_username);
                editor.putString(KEY_PREF_CURRENT_GROUP_USERS_NAME, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    void UpdateGroupList(int id, String name) {
//        runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                Map<String, Object> show_item = new HashMap<>();
//                show_item.put("name", name);
//                show_item.put("says", id + "");
//                show_item.put("image", R.mipmap.ic_launcher);
//
//                list_item.add(show_item);
//
//                SimpleAdapter simpleAdapter = new SimpleAdapter(GroupInfo.this, list_item, R.layout.friend_list_adapter,
//                        new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
//                ListView listView = (ListView) findViewById(R.id.GroupUserList);
//                if (listView == null) Log.d("dubug", "ListView Null");
//                listView.setAdapter(simpleAdapter);
//                // Stuff that updates the UI
//                System.out.println("=============================");
//                listView.setOnItemClickListener(this::onItemClick);
//            }
//
//            private void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                Gson gson = new Gson();
//                String json = gson.toJson(group_uid[position]);
//                cur_gid = group_uid[position];
//                editor.putString(KEY_PREF_CURRENT_GROUP_ID, json);
//                editor.commit();
//
//                System.out.printf("CURRENT GROUP ID: %d\n", cur_gid);
//
//
////                new Thread(new ServerConnectThread()).start();
////                new Thread(new getGroupUserList()).start();
//
//                Intent intent = new Intent(GroupInfo.this, GroupChat.class);
//                startActivity(intent);
//            }
//        });
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 110 && resultCode == 2) {
            if (data != null) {
                avatar.setImageURI(Uri.parse(getIntent().getStringExtra("GroupAvatar")));

            }
        }
    }

}