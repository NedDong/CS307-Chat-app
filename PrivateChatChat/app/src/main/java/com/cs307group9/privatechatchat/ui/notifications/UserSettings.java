package com.cs307group9.privatechatchat.ui.notifications;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import com.cs307group9.privatechatchat.MainActivity;
import com.cs307group9.privatechatchat.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class UserSettings extends AppCompatActivity {

    private Switch appNote;

    Button changeBG, deleteHist, viewHist, searchHist;
    ImageButton back;
    EditText chatT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        appNote = (Switch) findViewById(R.id.appNotification);
        changeBG = (Button) findViewById(R.id.changeBG);
        deleteHist = (Button) findViewById(R.id.deleteChatHist);
        back = (ImageButton) findViewById(R.id.userSettingBack);
        viewHist = (Button) findViewById(R.id.viewChatHist);
        chatT = (EditText) findViewById(R.id.chatText);
        searchHist = (Button) findViewById(R.id.searchButton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changeBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSettings.this, UserChangeBackground.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        deleteHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteChathistory();
                finish();
            }
        });

        viewHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewchathistory();
                //finish();
            }
        });

        searchHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = chatT.getText().toString().trim();
                searchchathistory(a);
                //finish();
            }
        });

    }

    public void searchchathistory(String format) {
        FileInputStream fis = null;
        try {
            fis = openFileInput("chathistory.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String chat;
            while ((chat = br.readLine()) != null) {
                sb.append(chat).append("\n");
            }
            String result = sb.toString();
            if (result.contains(format)) {
                chatT.setText("Find!");
            } else {
                chatT.setText("Not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewchathistory() {
        FileInputStream fis = null;
        try {
            fis = openFileInput("chathistory.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String chat;
            while ((chat = br.readLine()) != null) {
                sb.append(chat).append("\n");
            }
            chatT.setText(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteChathistory() {
        String filename = "chathistory.txt";
        //String fileContents = "Hello world!";
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(filename, MODE_PRIVATE);

            //Toast.makeText(this, "Saved to " + getFilesDir() + "/" + filename, Toast.LENGTH_SHORT).show();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}