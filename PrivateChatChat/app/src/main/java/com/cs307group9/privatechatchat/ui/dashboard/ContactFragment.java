package com.cs307group9.privatechatchat.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cs307group9.privatechatchat.OutputInputHandler;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.SocketHandler;
import com.cs307group9.privatechatchat.entity.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ContactFragment extends Fragment {

    private ContactViewModel dashboardViewModel;

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_PASSWORD = "password";
    final String KEY_PREF_FRIENDLIST = "friendlist";
    final String KEY_PREF_ISLOGIN = "islogin";
    final String KEY_PREF_SOCKET = "socket";
    final String LIST = "LIST";

    ObjectOutputStream oos;
    ObjectInputStream ois;

    TextView nameA, nameB, nameC;

    View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(ContactViewModel.class);
        view = inflater.inflate(R.layout.fragment_contact, container, false);

        sharedPreferences = getContext().getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Button reloadButton = view.findViewById(R.id.reloadButton);

        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_PREF_FRIENDLIST, "");

        Type type = new TypeToken<HashMap<String, User>>(){}.getType();

        HashMap<String, User> friendList = gson.fromJson(json, type);

//        nameA = view.findViewById(R.id.friendA);
//        nameB = view.findViewById(R.id.friendB);
//        nameC = view.findViewById(R.id.friendC);
//
//        User userList[] = new User[3];
//        String nameList[] = new String[3];

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new ReloadThread()).start();
            }
        });

//        int i = 0;
//
//        for(Map.Entry<String, User> entry : friendList.entrySet()) {
//            nameList[i] = entry.getKey();
//            userList[i] = friendList.get(nameList[i]);
//            i++;
//            if (i == 3) break;
//        }
//
//        if (i > 0) nameA.setText(nameList[0]);
//        if (i > 1) nameB.setText(nameList[1]);
//        if (i > 2) nameC.setText(nameList[2]);

        return view;
    }

    class ReloadThread implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("==============");

                Socket socket = new Socket("10.0.2.2", 1111);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                HashMap<String, User> updateFriendList = new HashMap<>();

                oos.writeObject(LIST);
                oos.writeObject(sharedPreferences.getString(KEY_PREF_USERNAME, ""));
                oos.writeObject(sharedPreferences.getString(KEY_PREF_PASSWORD, ""));
                // update Friendlist
                int num = (int) ois.readObject();

                System.out.println(num);

                for (int i = 0; i < num; i++) {
                    String response = (String) ois.readObject();
                    System.out.println(response);

                    String name = (String) ois.readObject();
                    int uid = (int) ois.readObject();
                    InetAddress inetAddress = (InetAddress) ois.readObject();
                    String psw = (String) ois.readObject();
                    User friend = new User(name, uid, inetAddress, psw);
                    updateFriendList.put(name, friend);
                    System.out.println("add friend successfully" + friend.getUsername());
                }

                Gson gson = new Gson();
                String json = gson.toJson(updateFriendList);

                editor.putString(KEY_PREF_FRIENDLIST, json);
                editor.commit();

                nameA = view.findViewById(R.id.friendA);
                nameB = view.findViewById(R.id.friendB);
                nameC = view.findViewById(R.id.friendC);

                User userList[] = new User[3];
                String nameList[] = new String[3];

                int i = 0;

                for(Map.Entry<String, User> entry : updateFriendList.entrySet()) {
                    nameList[i] = entry.getKey();
                    userList[i] = updateFriendList.get(nameList[i]);
                    i++;
                    if (i == 3) break;
                }

                if (i > 0) nameA.setText(nameList[0]);
                if (i > 1) nameB.setText(nameList[1]);
                if (i > 2) nameC.setText(nameList[2]);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}