package com.cs307group9.privatechatchat.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    final String KEY_PREF_PASSWORD = "password";
    final String KEY_PREF_FEEDBACK = "feedback";

    final String KEY_PREF_FRIENDLIST_NAME = "friendlist_name";
    final String KEY_PREF_FRIENDLIST_UID  = "friendlist_uid";
    final String KEY_PREF_FRIENDLIST_ADDR = "friendlist_addr";
    final String KEY_PREF_FRIENDLIST_PSW  = "friendlist_psw";

    final String KEY_PREF_GROUPLIST_NAME = "grouplist_name";
    final String KEY_PREF_GROUPLIST_GID = "grouplist_gid";
    final String KEY_PREF_GROUPLIST_USERS = "grouplist_users";

    final String KEY_PREF_CURRENT_GROUP_ID = "current_group_id";

    View view;

    private LinkedList<User> userData = null;
    private Context userContext;
    private UserAdapter userAdapter = null;
    private ListView listView;

    private String[] userName;
    private String[] psw;
    private int[] uid;
    private InetAddress[] addr;

    static String hostname = "cs307-chat-app.webredirect.org";
    //="cs307-chat-app.webredirect.org";
    //= "10.0.2.2";
    int type = -1; // 0 means LogIn, 1 means Register
    static int port = 12345;

    private ObjectOutputStream output;
    private ObjectInputStream input;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(MessageViewModel.class);
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        sharedPreferences = getContext().getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Gson gson = new Gson();

        String jsonName = sharedPreferences.getString(KEY_PREF_FRIENDLIST_NAME, "");
        String jsonPsw  = sharedPreferences.getString(KEY_PREF_FRIENDLIST_PSW, "");
        String jsonUid  = sharedPreferences.getString(KEY_PREF_FRIENDLIST_UID, "");
        String jsonAddr = sharedPreferences.getString(KEY_PREF_FRIENDLIST_ADDR, "");

        userName = (String[]) gson.fromJson(jsonName, new TypeToken<String[]>(){}.getType());
        psw = (String[]) gson.fromJson(jsonPsw, new TypeToken<String[]>(){}.getType());
        uid = (int[]) gson.fromJson(jsonUid, new TypeToken<int[]>(){}.getType());
        addr = (InetAddress[]) gson.fromJson(jsonAddr, new TypeToken<InetAddress[]>(){}.getType());

        LinkedList<User> users = new LinkedList<>();

        for (int i = 0; i < userName.length; i++) {
            users.add(new User(userName[i], uid[i], addr[i], psw[i]));
            users.get(i).printUser();
        }

        List<Map<String, Object>> list_item = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < userName.length; i++) {
            Map<String, Object> show_item = new HashMap<String, Object>();
            show_item.put("name", userName[i]);
            show_item.put("says", uid[i]);
            show_item.put("image", R.mipmap.ic_launcher);
            list_item.add(show_item);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), list_item, R.layout.friend_list_adapter,
                new String[]{"name", "says", "image"}, new int[]{R.id.name, R.id.says, R.id.imgtou});
        ListView listView = (ListView) view.findViewById(R.id.list_item);
        if (listView == null) Log.d("dubug", "ListView Null");
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(this::onItemClick);

        return view;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Toast.makeText(getContext(), "You Click" + userName[position] + "~!", Toast.LENGTH_LONG).show();\

        Gson gson = new Gson();
        String json = gson.toJson(uid[position]);
        editor.putString(KEY_PREF_CURRENT_GROUP_ID, json);
        editor.commit();

        Intent intent = new Intent(getActivity(), GroupInfo.class);
        startActivity(intent);
    }
//
//    class ServerConnectThread implements Runnable {
//        public void run() {
//            System.out.println("==== I Am Currently Running Thread 1===");
//            Socket socket;
//            try {
//                socket = new Socket(hostname, port);
//                output = new ObjectOutputStream(socket.getOutputStream());
//                input  = new ObjectInputStream(socket.getInputStream());
//
//                new Thread(new getGroupUserList()).start();
//            } catch (UnknownHostException ex) {
//                System.out.println("Server not found: " + ex.getMessage());
//            } catch (IOException ex) {
//                System.out.println("I/O Error: " + ex.getMessage());
//            }
//        }
//    }
//
//    class RecieveGroupList implements Runnable {
//        @Override
//        public void run() {
//            try {
//                output.writeObject("GetGroups");
//                System.out.println("+++++++++++++++++++++++++++++++++++");
//
////                output.writeObject(username);
//
//                // Receive the number of groups
//                Object numCheck = input.readObject();
//                int num = 0;
//
//                if (numCheck == null) {
//                    System.out.println("========THERE IS NO GROUP========");
//                    return;
//                }
//                else {
//                    num = (int) numCheck;
//                    System.out.printf("========THERE IS %d GROUPS========\n", num);
//                }
//
//                int[] groupList = new int[num];
//                String[] groupName = new String[num];
//
//                for (int i = 0; i < num; i++) {
//                    groupList[i] = (int) input.readObject();
////                   groupName[i] = (String) input.readObject();
//                }
//
//                Gson gson = new Gson();
//                String json = gson.toJson(groupList);
//                editor.putString(KEY_PREF_GROUPLIST_GID, json);
////                json = gson.toJson(groupName);
////                editor.putString(KEY_PREF_GROUPLIST_NAME, json);
//                editor.commit();
//
//                Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//                startActivity(intent);
//                finish();
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//            catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    class getGroupUserList implements Runnable {
//        @Override
//        public void run() {
//            try {
//                output;
//            } catch (Exception e) {
//
//            }
//        }
//    }



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