package com.cs307group9.privatechatchat.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cs307group9.privatechatchat.MainActivity;
import com.cs307group9.privatechatchat.MainScreenActivity;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.entity.UserAdapter;
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

public class Contacts extends Fragment {

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
    final String KEY_PREF_FRIENDLIST_UID = "friendlist_uid";
    final String KEY_PREF_FRIENDLIST_ADDR = "friendlist_addr";
    final String KEY_PREF_FRIENDLIST_PSW = "friendlist_psw";

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
    //=
//    "10.0.2.2";

    static int port = 12345;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dashboardViewModel =
                new ViewModelProvider(this).get(ContactViewModel.class);
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        sharedPreferences = getContext().getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

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
        Toast.makeText(getContext(), "You Click" + userName[position] + "~!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // output.println("bye");

        startActivity(intent);
        //finish();
    }

    private ObjectOutputStream output;
    private ObjectInputStream input;

    class ServerConnectThread implements Runnable {
        public void run() {
            Log.e("Contacts", "CONNECT SERVER");
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

    class RecieveFriendList implements Runnable {
        @Override
        public void run() {
            try {
                output.writeObject("GetUserList");

                int num = (int)input.readObject();

                String[] name = new String[num];
                int[] uid = new int[num];
                InetAddress[] inetAddress = new InetAddress[num];
                String[] psw = new String[num];
                String[] avatar = new String[num];

                for (int i = 0; i < num; i++) {
                    String response = (String) input.readObject();
                    Log.e("Contacts", response);
                    name[i] = (String) input.readObject();
                    uid[i] = (int) input.readObject();
                    inetAddress[i] = (InetAddress) input.readObject();
                    psw[i] = (String) input.readObject();
                    avatar[i] = (String) input.readObject();

                }

                input.readObject();


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