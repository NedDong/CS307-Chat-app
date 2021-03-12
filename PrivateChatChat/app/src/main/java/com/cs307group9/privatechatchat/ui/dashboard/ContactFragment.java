package com.cs307group9.privatechatchat.ui.dashboard;

import android.content.Context;
import android.content.Intent;
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

import com.cs307group9.privatechatchat.MainActivity;
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
    final String KEY_PREF_MUTE = "mute";
    final String KEY_PREF_BLOCK = "block";
    final String LIST = "LIST";

    ObjectOutputStream oos;
    ObjectInputStream ois;

    TextView nameA, nameB, nameC;
    Button buttonA, buttonB, buttonC, groupButton, mA, mB, mC, bA, bB, bC;;

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

        nameA = view.findViewById(R.id.friendA);
        nameB = view.findViewById(R.id.friendB);
        nameC = view.findViewById(R.id.friendC);

        buttonA = view.findViewById(R.id.chatButtonA);
        buttonB = view.findViewById(R.id.chatButtonB);
        buttonC = view.findViewById(R.id.chatButtonC);
        groupButton = view.findViewById(R.id.groupButton);

        User userList[] = new User[3];
        String nameList[] = new String[3];

        buttonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        buttonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        buttonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new ReloadThread()).start();
            }
        });

        int i = 0;

        for(Map.Entry<String, User> entry : friendList.entrySet()) {
            nameList[i] = entry.getKey();
            userList[i] = friendList.get(nameList[i]);
            i++;
            if (i == 3) break;
        }

        if (i > 0) nameA.setText(nameList[0]);
        if (i > 1) nameB.setText(nameList[1]);
        if (i > 2) nameC.setText(nameList[2]);


        mA = view.findViewById(R.id.muteA);
        mA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(KEY_PREF_MUTE, nameA.getText().toString());
                editor.commit();
            }
        });

        mB = view.findViewById(R.id.muteB);
        mB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(KEY_PREF_MUTE, nameB.getText().toString());
                editor.commit();
            }
        });

        mC = view.findViewById(R.id.muteC);
        mC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(KEY_PREF_MUTE, nameC.getText().toString());
                editor.commit();
            }
        });

        bA = view.findViewById(R.id.blockA);
        bA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(KEY_PREF_BLOCK, nameA.getText().toString());
                editor.commit();
            }
        });
        bB = view.findViewById(R.id.blockB);
        bB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(KEY_PREF_BLOCK, nameB.getText().toString());
                editor.commit();
            }
        });
        bC = view.findViewById(R.id.blockC);
        bC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(KEY_PREF_BLOCK, nameC.getText().toString());
                editor.commit();
            }
        });

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

                String blockName = sharedPreferences.getString(KEY_PREF_BLOCK, "");

                for (int i = 0; i < num; i++) {
                    String response = (String) ois.readObject();
                    System.out.println(response);

                    String name = (String) ois.readObject();
                    int uid = (int) ois.readObject();
                    InetAddress inetAddress = (InetAddress) ois.readObject();
                    String psw = (String) ois.readObject();
                    User friend = new User(name, uid, inetAddress, psw);

                    if (name.equals(blockName)) continue;

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
                String nof = "NullFriend";

                if (i > 0) nameA.setText(nameList[0]);
                else nameA.setText(nof);
                if (i > 1) nameB.setText(nameList[1]);
                else nameB.setText(nof);
                if (i > 2) nameC.setText(nameList[2]);
                else nameC.setText(nof);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}