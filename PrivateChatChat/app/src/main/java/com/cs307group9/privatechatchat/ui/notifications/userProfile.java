package com.cs307group9.privatechatchat.ui.notifications;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;
import com.cs307group9.privatechatchat.ui.notifications.ProfileFragment;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class userProfile extends Fragment {

    private ImageButton userAvatar;
    private Button changeAlias, changeSetting, deleteAccount, exitAccount;
    private TextView userAlias;
    private EditText newAlias;
    private ImageView imageView;
    private TextView userID;

    private static final int PHOTO_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PHOTO_CLIP = 3;

    ObjectOutputStream oos;
    ObjectInputStream ois;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_PASSWORD = "password";
    final String DEREGISTER = "DEREGISTER";
    final String KEY_PREF_FRIENDLIST = "friendlist";
    final String KEY_PREF_CHANGE = "UpdateUserName";
    final String KEY_PREF_MUTE = "mute";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    int deORch = 0; // 0 means deregister, 1 means chaneg name
    boolean switchImage = false;
    String sendMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

        userAvatar = v.findViewById(R.id.userAvatar);
        changeAlias = v.findViewById(R.id.changeAlias);
        changeSetting = v.findViewById(R.id.changeSetting);
        deleteAccount = v.findViewById(R.id.deleteAccount);
        exitAccount = v.findViewById(R.id.exitAccount);
        imageView = v.findViewById(R.id.imageView);
        userAlias = v.findViewById(R.id.userAlias);
        newAlias = v.findViewById(R.id.editTextPersonName);


        sharedPreferences = getContext().getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        userAlias.setText(sharedPreferences.getString(KEY_PREF_USERNAME, ""));



        changeAlias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deORch = 1;
                sendMsg = newAlias.getText().toString().trim();
                userAlias.setText(newAlias.getText());
                new Thread(new DeregisterThread()).start();
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deORch = 0;
                new Thread(new DeregisterThread()).start();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }

        });

        exitAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }

        });


        changeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserSettings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });



        return v;
    }

    public class DeregisterThread implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("==============");

                Socket socket = new Socket("10.0.2.2", 1111);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                HashMap<String, User> updateFriendList = new HashMap<>();

                if (deORch == 0) {
                    oos.writeObject(DEREGISTER);
                    oos.writeObject(sharedPreferences.getString(KEY_PREF_USERNAME, ""));
                    oos.writeObject(sharedPreferences.getString(KEY_PREF_PASSWORD, ""));
                } else {
                    oos.writeObject(KEY_PREF_CHANGE);
                    oos.writeObject(sharedPreferences.getString(KEY_PREF_USERNAME, ""));
                    oos.writeObject(sendMsg);
                    editor.putString(KEY_PREF_USERNAME, sendMsg);
                }
                // update Friendlist
//                int num = (int) ois.readObject();
//
//                System.out.println(num);
//
//                for (int i = 0; i < num; i++) {
//                    String response = (String) ois.readObject();
//                    System.out.println(response);
//
//                    String name = (String) ois.readObject();
//                    int uid = (int) ois.readObject();
//                    InetAddress inetAddress = (InetAddress) ois.readObject();
//                    String psw = (String) ois.readObject();
//                    User friend = new User(name, uid, inetAddress, psw);
//                    updateFriendList.put(name, friend);
//                    System.out.println("add friend successfully" + friend.getUsername());
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
        }
    }

}