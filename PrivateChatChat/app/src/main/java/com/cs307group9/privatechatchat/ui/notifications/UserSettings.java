package com.cs307group9.privatechatchat.ui.notifications;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;

import com.cs307group9.privatechatchat.R;

public class UserSettings extends AppCompatActivity {

    private Switch appNote;

    Button changeBG, deleteHist;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        appNote = (Switch) findViewById(R.id.appNotification);
        changeBG = (Button) findViewById(R.id.changeBG);
        deleteHist = (Button) findViewById(R.id.deleteChatHist);
        back = (ImageButton) findViewById(R.id.groupChangeNameBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changeBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}