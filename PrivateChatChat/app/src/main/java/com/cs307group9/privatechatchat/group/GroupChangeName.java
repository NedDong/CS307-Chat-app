package com.cs307group9.privatechatchat.group;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cs307group9.privatechatchat.FriendProfile;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;
import com.cs307group9.privatechatchat.ui.notifications.userProfile;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

public class GroupChangeName extends AppCompatActivity {

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_CURRENT_GROUP_ID = "current_gid";
    final String KEY_PREF_USERID = "userid";

    final String KEY_PREF_CHANGE = "ChangeGroupName";
    final String KEY_PREF_GROUPLIST_GID = "grouplist_gid";
    final String KEY_PREF_GROUPLIST_NAME = "grouplist_name";

    ObjectOutputStream oos;
    ObjectInputStream ois;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    TextInputEditText newName;
    ImageButton back;
    Button apply;

    String inputName;

    int cur_id = 0;
    int cur_pos = 0;
    int cur_gid = 0;
    int[] g_list;
    String[] g_list_name;

    boolean check_manager_bool = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_change_name);

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        newName = (TextInputEditText) findViewById(R.id.g_NewNameInput);
        apply = (Button) findViewById(R.id.g_ApplyNewNameButton);
        back = (ImageButton) findViewById(R.id.groupChangeNameBack);

        Gson gson = new Gson();

        String jsonGroupListID = sharedPreferences.getString(KEY_PREF_GROUPLIST_GID, "null GID");
        Log.e("GroupChangeName", jsonGroupListID);
        String jsonGroupListName = sharedPreferences.getString(KEY_PREF_GROUPLIST_NAME, "null GNAME");
        Log.e("GroupChangeName", jsonGroupListName);
        g_list = (int[]) gson.fromJson(jsonGroupListID, new TypeToken<int[]>(){}.getType());
        g_list_name = (String[]) gson.fromJson(jsonGroupListName, new TypeToken<String[]>(){}.getType());

        cur_id = (int)sharedPreferences.getInt(KEY_PREF_USERID, 0);
        cur_pos = (int)sharedPreferences.getInt(KEY_PREF_CURRENT_GROUP_ID, 0);
        cur_gid = g_list[cur_pos];

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputName = newName.getText().toString().trim();
                new Thread(new ChangeGroupName()).start();
//                finish();
            }
        });

    }

    Socket socket;

    public class ChangeGroupName implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("==============");

                socket = new Socket("cs307-chat-app.webredirect.org", 12345);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                Log.e("GroupChangeName", "GROUP NAME: " + newName.getText().toString());
                Log.e("GroupChangeName", "GROUP ID: " + cur_gid);

                new Thread(new check_manager()).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ChangeGroupNameUpdate implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("==============");

                Socket socket = new Socket("cs307-chat-app.webredirect.org", 12345);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                new Thread(new updateName()).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class check_manager implements Runnable {
        @Override
        public void run() {
            try {
                Log.e("GroupChangeName", "CHECK MANAGER" + cur_gid + "|" + cur_id);
                oos.writeObject("CheckManager");
                oos.writeObject("" + cur_gid);
                oos.writeObject("" + cur_id);
                String ans = (String) ois.readObject();
                if (ans.equals("")) Log.e("GroupChangeName", "xxxxxx");
                Log.e("GroupChangeName", ans);
                if (ans.equals("IS MANAGER")) {
                    ois.readObject();
                    Log.e("GroupChangeName", "IS MANAGER");
                    check_manager_bool = true;
                    socket.close();
                    new Thread(new ChangeGroupNameUpdate()).start();
                } else {
                    Log.e("GroupChangeName", "NOT A MANAGER");
                    ois.readObject();
                    check_manager_bool = false;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            final Toast toast = Toast.makeText(GroupChangeName.this, "YOU ARE NOT MANAGER", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("============", "=============");
                e.printStackTrace();
            }

        }
    }

    class updateName implements Runnable {
        @Override
        public void run() {
            try {
                String updatedname = newName.getText().toString();

                oos.writeObject(KEY_PREF_CHANGE);
                oos.writeObject("" + updatedname);
                oos.writeObject("" + cur_gid);

                String response = (String) ois.readObject();
                Log.e("GroupChangeName", response);
                if (response.equals("SUCCESS")) {
                    g_list_name[cur_pos] = updatedname;
                    Gson gson = new Gson();
                    String json = gson.toJson(g_list_name);
                    editor.putString(KEY_PREF_GROUPLIST_NAME, json);
                    editor.commit();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            final Toast toast = Toast.makeText(GroupChangeName.this, "SUCCESSFULLY UPDATE", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                    finish();
                }
                else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Log.e("GroupChangeName", response);
                            final Toast toast = Toast.makeText(GroupChangeName.this, "DUPLICATE GROUP NAME", Toast.LENGTH_LONG);
                            toast.show();
                        }

                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}