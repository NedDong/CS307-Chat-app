package com.cs307group9.privatechatchat.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cs307group9.privatechatchat.MainScreenActivity;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.ui.dashboard.ContactFragment;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ProfileViewModel notificationsViewModel;
    Button deButton;

    ObjectOutputStream oos;
    ObjectInputStream ois;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_PASSWORD = "password";
    final String DEREGISTER = "DEREGISTER";
    final String KEY_PREF_FRIENDLIST = "friendlist";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

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