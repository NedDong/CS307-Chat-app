package com.cs307group9.privatechatchat.group;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.cs307group9.privatechatchat.ui.notifications.userProfile;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class GroupChangeName extends AppCompatActivity {


    final String KEY_PREF_CURRENT_GROUP_ID = "current_gid";

    final String KEY_PREF_CHANGE = "UpdateGroupName";

    ObjectOutputStream oos;
    ObjectInputStream ois;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    TextInputEditText newName;
    ImageButton back;
    Button apply;

    String inputName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_change_name);

        newName = (TextInputEditText) findViewById(R.id.g_NewNameInput);
        apply = (Button) findViewById(R.id.g_ApplyNewNameButton);
        back = (ImageButton) findViewById(R.id.groupChangeNameBack);

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
                finish();
            }
        });

    }

    public class ChangeGroupName implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("==============");

                Socket socket = new Socket("10.0.2.2", 1111);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                oos.writeObject(KEY_PREF_CHANGE);
                oos.writeObject(sharedPreferences.getString(KEY_PREF_CURRENT_GROUP_ID, ""));
                oos.writeObject(newName);
                editor.putString(KEY_PREF_CURRENT_GROUP_ID, inputName);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}