package com.cs307group9.privatechatchat.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private ProfileViewModel notificationsViewModel;
    Button deButton, changeButton, quitButton;
    View avatar01, avatar02;

    ObjectOutputStream oos;
    ObjectInputStream ois;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_PASSWORD = "password";
    final String DEREGISTER = "DEREGISTER";
    final String KEY_PREF_FRIENDLIST = "friendlist";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    boolean switchImage = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        avatar01 = view.findViewById(R.id.avatar01);
        avatar02 = view.findViewById(R.id.avatar02);

        avatar02.setVisibility(View.INVISIBLE);
        avatar01.setVisibility(View.VISIBLE);

        sharedPreferences = getContext().getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        deButton = view.findViewById(R.id.deregisterButton);
        deButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new Thread(new DeregisterThread()).start();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }

        });

        changeButton = view.findViewById(R.id.change);
        changeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!switchImage) {
                    avatar02.setVisibility(View.VISIBLE);
                    avatar01.setVisibility(View.INVISIBLE);
                    switchImage = true;
                } else {
                    switchImage = false;
                    avatar01.setVisibility(View.VISIBLE);
                    avatar02.setVisibility(View.INVISIBLE);
                }
            }

        });

        quitButton = view.findViewById(R.id.quitButton);
        quitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }

        });

        return view;
    }

    class DeregisterThread implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("==============");

                Socket socket = new Socket("10.0.2.2", 1111);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                HashMap<String, User> updateFriendList = new HashMap<>();

                oos.writeObject(DEREGISTER);
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



//                Gson gson = new Gson();
//                String json = gson.toJson(updateFriendList);
//
//                editor.putString(KEY_PREF_FRIENDLIST, json);
//                editor.commit();
//
//                nameA = view.findViewById(R.id.friendA);
//                nameB = view.findViewById(R.id.friendB);
//                nameC = view.findViewById(R.id.friendC);
//
//                User userList[] = new User[3];
//                String nameList[] = new String[3];
//
//                int i = 0;
//
//                for(Map.Entry<String, User> entry : updateFriendList.entrySet()) {
//                    nameList[i] = entry.getKey();
//                    userList[i] = updateFriendList.get(nameList[i]);
//                    i++;
//                    if (i == 3) break;
//                }
//
//                if (i > 0) nameA.setText(nameList[0]);
//                if (i > 1) nameB.setText(nameList[1]);
//                if (i > 2) nameC.setText(nameList[2]);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}