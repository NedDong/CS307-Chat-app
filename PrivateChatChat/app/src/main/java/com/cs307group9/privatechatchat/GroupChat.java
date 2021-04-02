package com.cs307group9.privatechatchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class GroupChat extends AppCompatActivity {

    private ImageButton backButton;
    private ImageButton moreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        backButton = (ImageButton) findViewById(R.id.groupChatBack);
        moreButton = (ImageButton) findViewById(R.id.groupChatMore);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}