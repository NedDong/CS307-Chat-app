package com.cs307group9.privatechatchat.group;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.entity.UserAdapter;
import com.cs307group9.privatechatchat.ui.dashboard.ContactViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

public class GroupAddAdministrator extends AppCompatActivity {
    private ImageButton back;

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

    private String[] userName, group_username, avatarList;
    private String[] psw;
    private int[] uid, group_uid, groupUserID, groupListId;
    private InetAddress[] addr;

    private String current_gName, select_name, select_avatar;

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
        setContentView(R.layout.activity_group_administrator);

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        back = (ImageButton) findViewById(R.id.groupAdministratorBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Gson gson = new Gson();

        String jsonName = sharedPreferences.getString(KEY_PREF_FRIENDLIST_NAME, "");
        String jsonPsw = sharedPreferences.getString(KEY_PREF_FRIENDLIST_PSW, "");
        String jsonUid = sharedPreferences.getString(KEY_PREF_FRIENDLIST_UID, "");
        String jsonAddr = sharedPreferences.getString(KEY_PREF_FRIENDLIST_ADDR, "");
        String jsonGroupListName = sharedPreferences.getString(KEY_PREF_GROUPLIST_NAME, "");
        String jsonGroupListID = sharedPreferences.getString(KEY_PREF_GROUPLIST_GID, "");

        String jsonGroupUserId = sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_USERS_ID, "");
        String jsonGroupUserName = sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_USERS_NAME, "");

        userName = (String[]) gson.fromJson(jsonName, new TypeToken<String[]>() {
        }.getType());
        psw = (String[]) gson.fromJson(jsonPsw, new TypeToken<String[]>() {
        }.getType());
        uid = (int[]) gson.fromJson(jsonUid, new TypeToken<int[]>() {
        }.getType());
        addr = (InetAddress[]) gson.fromJson(jsonAddr, new TypeToken<InetAddress[]>() {
        }.getType());
        group_uid = (int[]) gson.fromJson(jsonGroupUserId, new TypeToken<int[]>() {
        }.getType());
        group_username = (String[]) gson.fromJson(jsonGroupUserName, new TypeToken<String[]>() {
        }.getType());

        groupListName = (String[]) gson.fromJson(jsonGroupListName, new TypeToken<String[]>() {
        }.getType());
        groupListId = (int[]) gson.fromJson(jsonGroupListID, new TypeToken<int[]>() {
        }.getType());
        cur_gid = sharedPreferences.getInt(KEY_PREF_CURRENT_GROUP_ID, -1);
        Log.e("GroupInfo", "CURRENT GROUP ID = " + groupListId[cur_gid]);

        for (int i = 0; i < groupListName.length; i++) {
            Log.e("GroupInfo", "CURRENT GROUP NAME = " + groupListName[i]);
        }

        if (cur_gid != -1) cur_gName = groupListName[cur_gid];
        else cur_gName = "null";

        cur_gid = groupListId[cur_gid];

        new Thread(new ServerConnectThread()).start();
    }

    class ServerConnectThread implements Runnable {
        public void run() {
            Log.e("GroupAddMember", "==== I Am Currently Running Loading Users===");
            Socket socket;
            try {
                socket = new Socket(hostname, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());

                new Thread(new RecieveFriendList()).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class ServerConnectThreadAddManager implements Runnable {
        public void run() {
            Log.e("GroupDeleteMember", "==== I Am Currently Deleting [" + select_uid + "]");
            Socket socket;
            try {
                socket = new Socket(hostname, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());

                new Thread(new AddManager()).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class AddManager implements Runnable {
        public void run() {
            try {
                output.writeObject("AddManager");
                output.writeObject("" + cur_gName);
                output.writeObject("" + select_uid);

                Log.e("GroupAddMember", "ADDING MANAGER...." + "[" + cur_gName + "]" + cur_gid + ": " + select_uid);
                String response = (String) input.readObject();
                Log.e("GroupAddMember", "RECEIVING..." + response);

                runOnUiThread(new Runnable() {
                    public void run() {
//                        if (response.equals("Max num exceeded")) {
//                            final Toast toast = Toast.makeText(GroupDeleteMember.this, response, Toast.LENGTH_SHORT);
//                            toast.show();
//                        }
//                        if (response.equals("Success")){
                        final Toast toast = Toast.makeText(GroupAddAdministrator.this, "Success", Toast.LENGTH_SHORT);
                        toast.show();
//                        }
//                        else {
//                            final Toast toast = Toast.makeText(GroupDeleteMember.this, "Cannot Add Same User", Toast.LENGTH_SHORT);
//                            toast.show();
//                        }
                    }
                });

                input.readObject();

//                Map<String, Object> show_item = new HashMap<String, Object>();
//                show_item.put("name", select_name);
//                show_item.put("says", select_uid);
//                show_item.put("image", R.mipmap.ic_launcher);
//                list_item.remove(show_item);
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        listView.invalidateViews();
//                    }
//                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void UpdateUserList(int id, String name) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> show_item = new HashMap<String, Object>();
                show_item.put("name", name);
                show_item.put("says", id);
                show_item.put("image", R.mipmap.ic_launcher);
                list_item.add(show_item);

                SimpleAdapter simpleAdapter = new SimpleAdapter(GroupAddAdministrator.this, list_item, R.layout.friend_list_adapter,
                        new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
                listView = (ListView) findViewById(R.id.A_GroupUserList);
                if (listView == null) Log.d("dubug", "ListView Null");
                listView.setAdapter(simpleAdapter);

                listView.setOnItemClickListener(this::onItemClick);
            }

            private void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                select_uid = groupUserID[position];
                select_name = group_username[position];
                select_avatar = avatarList[position];

                Log.e("GroupDeleteMember", "[" + select_uid + "]" + select_name);

                new Thread(new ServerConnectThreadAddManager()).start();
            }
        });
    }

    class RecieveFriendList implements Runnable {
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
                avatarList = new String[num];

                Log.e("GroupInfo","GROUP MEMBER NUM = " + num);

                for (int i = 0; i < num; i++) {
                    groupUserID[i] = temp_groupUserId.get(i);
                    group_username[i] = temp_groupUserName.get(i);
                    avatarList[i] = temp_groupUserAvatar.get(i);
                    Log.e("GroupInfo", "GROUP LIST: USERID[" + groupUserID[i]
                            + "] USERNAME[" + group_username[i] + "]"
                            + "] AVATAR[" + temp_groupUserAvatar.get(i) + "]");
                    UpdateUserList(groupUserID[i], group_username[i]);
                }

                Gson gson = new Gson();
                String json = gson.toJson(groupUserID);
                editor.putString(KEY_PREF_CURRENT_GROUP_USERS_ID, json);
                json = gson.toJson(group_username);
                editor.putString(KEY_PREF_CURRENT_GROUP_USERS_NAME, json);
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
