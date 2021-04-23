package com.cs307group9.privatechatchat.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cs307group9.privatechatchat.FriendProfile;
import com.cs307group9.privatechatchat.group.GroupChat;
import com.cs307group9.privatechatchat.MainActivity;
import com.cs307group9.privatechatchat.MainScreenActivity;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.entity.UserAdapter;
//import com.cs307group9.privatechatchat.group.GroupChat;
import com.cs307group9.privatechatchat.group.GroupCreate;
import com.cs307group9.privatechatchat.group.GroupInfo;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;
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

public class MessageFragment extends Fragment {

    private MessageViewModel homeViewModel;

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

    private String[] userName, groupUserName, groupName;
    private String[] psw;
    private int[] uid, groupList, groupUserID;
    private InetAddress[] addr;

    private String username;

    static String hostname = "cs307-chat-app.webredirect.org";
    //="cs307-chat-app.webredirect.org";
    //"10.0.2.2";
    int type = -1; // 0 means LogIn, 1 means Register
    static int port = 12345;

    int cur_gid;
    int cur_uid;

    private ObjectOutputStream output;
    private ObjectInputStream input;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Socket socket;

    boolean check = false;

    List<Map<String, Object>> list_item = new ArrayList<Map<String, Object>>();

    Button createGroup;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(MessageViewModel.class);
        view = inflater.inflate(R.layout.fragment_message, container, false);

        sharedPreferences = getContext().getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        username = sharedPreferences.getString(KEY_PREF_USERNAME, "");
        cur_uid = sharedPreferences.getInt(KEY_PREF_USERID, -1);

        new Thread(new ServerConnectThread()).start();

        Gson gson = new Gson();

        String jsonGroupUserId = sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_USERS_ID, "null");
        String jsonGroupUserName = sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_USERS_NAME, "null");


        groupList = (int[]) gson.fromJson(jsonGroupUserId, new TypeToken<int[]>(){}.getType());
        groupName = (String[]) gson.fromJson(jsonGroupUserName, new TypeToken<String[]>(){}.getType());



        createGroup = view.findViewById(R.id.CreateGroup);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GroupCreate.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        if (!jsonGroupUserId.equals("null")) {
            for (int i = 0; i < groupList.length; i++) {
                Map<String, Object> show_item = new HashMap<String, Object>();
                show_item.put("name", groupName[i]);
                System.out.printf("GROUP NAME: %s\n", groupName[i]);
                show_item.put("says", groupList[i]);
                System.out.printf("GROUP ID: %d\n", groupList[i]);
                show_item.put("image", R.mipmap.ic_launcher);
                list_item.add(show_item);
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), list_item, R.layout.friend_list_adapter,
                new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
        ListView listView = (ListView) view.findViewById(R.id.gg_list_item);
        if (listView == null) Log.d("dubug", "ListView Null");
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(this::onItemClick);

        return view;
    }

    void UpdateGroupList(int id, String name) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Map<String, Object> show_item = new HashMap<>();
                show_item.put("name", name);
                show_item.put("says", id + "");
                show_item.put("image", R.mipmap.ic_launcher);

                list_item.add(show_item);

                SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), list_item, R.layout.friend_list_adapter,
                        new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
                ListView listView = (ListView) view.findViewById(R.id.gg_list_item);
                if (listView == null) Log.d("dubug", "ListView Null");
                listView.setAdapter(simpleAdapter);
                // Stuff that updates the UI
                System.out.println("=============================");
                listView.setOnItemClickListener(this::onItemClick);
            }

            private void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Gson gson = new Gson();
                String json = gson.toJson(groupList[position]);
                cur_gid = groupList[position];
                editor.putInt(KEY_PREF_CURRENT_GROUP_ID, cur_gid);
                editor.commit();

                System.out.printf("CURRENT GROUP ID: %d\n", cur_gid);


//                new Thread(new ServerConnectThread()).start();
//                new Thread(new getGroupUserList()).start();

                Intent intent = new Intent(getActivity(), GroupChat.class);
                startActivity(intent);
            }
        });

    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Toast.makeText(getContext(), "You Click" + userName[position] + "~!", Toast.LENGTH_LONG).show();\

        Gson gson = new Gson();
        String json = gson.toJson(groupList[position]);
        cur_gid = groupList[position];
        editor.putInt(KEY_PREF_CURRENT_GROUP_ID, cur_gid);
        editor.commit();

        System.out.printf("CURRENT GROUP ID: %d\n", cur_gid);

//        new Thread(new ServerConnectThread()).start();
//        new Thread(new getGroupUserList()).start();

        Intent intent = new Intent(getActivity(), GroupChat.class);
        startActivity(intent);
    }

    class ServerConnectThread implements Runnable {
        public void run() {
            System.out.println("==== I Am Currently Running Thread 1===");

            try {
                socket = new Socket(hostname, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());

                new Thread(new RecieveGroupList()).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class RecieveGroupList implements Runnable {
        @Override
        public void run() {
            try {
                output.writeObject("GetGroupList");
                output.writeObject("" + cur_uid);
                output.writeObject("-1");

                System.out.println(username);

                ArrayList<Integer> arr_groupList = new ArrayList<>();
                ArrayList<String>  arr_groupName = new ArrayList<>();

                int num = (int)input.readObject();
                if (num == -1) input.readObject();

                System.out.println(num);

                for (int i = 0; i < num; i++) {
                    arr_groupList.add((int)input.readObject());
                    System.out.printf("========%s\n", arr_groupList.get(i));
                    arr_groupName.add((String) input.readObject());
                    System.out.printf("========%s\n", arr_groupName.get(i));
                }

                groupList = new int[arr_groupList.size()];
                groupName = new String[arr_groupList.size()];

                for (int j = 0; j < arr_groupList.size(); j++) {
                    groupList[j] = arr_groupList.get(j);
                    groupName[j] = arr_groupName.get(j);
                    UpdateGroupList(groupList[j], groupName[j]);
                }
                Gson gson = new Gson();
                String json = gson.toJson(groupList);
                editor.putString(KEY_PREF_GROUPLIST_GID, json);
                json = gson.toJson(groupName);
                editor.putString(KEY_PREF_GROUPLIST_NAME, json);
                editor.commit();

                new Thread(new getUserList()).start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
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

                int num = (int) input.readObject();
                System.out.printf("RESULT: %d\n", num);
                groupUserID = new int[num];
                groupUserName = new String[num];

                for (int i = 0; i < num; i++) {
                    groupUserID[i] = (int) input.readObject();
                    groupUserName[i] = userName[groupUserID[i]];
                }

                Gson gson = new Gson();
                String json = gson.toJson(groupUserID);
                editor.putString(KEY_PREF_CURRENT_GROUP_USERS_ID, json);
                json = gson.toJson(groupUserName);
                editor.putString(KEY_PREF_CURRENT_GROUP_USERS_NAME, json);

                check = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class getUserList implements Runnable {
        @Override
        public void run() {
            try {
                output.writeObject("LIST");
                output.writeObject(username);
                output.writeObject("null");

                int num = (int) input.readObject();

                String[] name = new String[num];
                int[] uid = new int[num];
                InetAddress[] inetAddress = new InetAddress[num];
                String[] psw = new String[num];

                for (int i = 0; i < num; i++) {
                    String response = (String) input.readObject();
                    System.out.println(response);

                    name[i] = (String) input.readObject();
                    uid[i] = (int) input.readObject();
                    inetAddress[i] = (InetAddress) input.readObject();
                    psw[i] = (String) input.readObject();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



//        Button chatButton = (Button) view.findViewById(R.id.chatButton);
//        chatButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//                // output.println("bye");
//
//                startActivity(intent);
//                //finish();
//            }
//        });

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    public void chatButton(View view) {
//
//    }


}