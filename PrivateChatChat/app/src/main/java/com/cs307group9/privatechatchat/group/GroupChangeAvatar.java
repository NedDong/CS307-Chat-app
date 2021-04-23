package com.cs307group9.privatechatchat.group;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cs307group9.privatechatchat.R;

public class GroupChangeAvatar extends AppCompatActivity {

    private RadioGroup radioAvatars;
    private Button applyButton;

//    final String KEY_PREF_

    private ImageButton back;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_change_avatar);

        radioAvatars = (RadioGroup) findViewById(R.id.groupRadioAvatar);
        applyButton = (Button) findViewById(R.id.g_ApplyAvatar);


        back = (ImageButton) findViewById(R.id.groupChangeAvatarBack1);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int avatarNum = radioAvatars.getCheckedRadioButtonId();
                switch (avatarNum) {
                    case R.id.radioButton1:
                        uri = Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.a1);

                        break;

                    case R.id.radioButton2:
                        uri = Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.a2);
                        break;

                    case R.id.radioButton3:
                        uri = Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.a3);
                        break;

                    case R.id.radioButton4:
                        uri = Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.a4);
                        break;

                    default:
                        break;

                }

                Toast.makeText(GroupChangeAvatar.this, "Group Avatar Change Successful", Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();

                intent.putExtra("image", uri.toString());
                setResult(2, intent);
                finish();
            }
        });
    }
}