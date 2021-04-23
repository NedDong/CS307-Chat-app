package com.cs307group9.privatechatchat.group;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    final String KEY_PREF_GROUP_AVATAR  = "g_avatar";
    final String KEY_PREF_APP = "myPref";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private ImageButton back;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_change_avatar);

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

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
                int num = -1;
                switch (avatarNum) {
                    case R.id.radioButton1:
                        uri = Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.a1);

                        num = 1;
                        avatarNum = R.drawable.a1;


                        break;

                    case R.id.radioButton2:
                        uri = Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.a2);

                        num = 2;
                        avatarNum = R.drawable.a2;

                        break;

                    case R.id.radioButton3:
                        uri = Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.a3);

                        num = 3;
                        avatarNum = R.drawable.a3;

                        break;

                    case R.id.radioButton4:
                        uri = Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.a4);

                        num = 4;
                        avatarNum = R.drawable.a4;

                        break;

                    default:
                        break;

                }
                editor.putInt(KEY_PREF_GROUP_AVATAR, avatarNum);
                editor.commit();
                Log.e("Group avatar", "chose pic #" + num + "=" + avatarNum);
                Toast.makeText(GroupChangeAvatar.this, "Group Avatar Change Successful", Toast.LENGTH_SHORT).show();
//                Intent intent = getIntent();
//
//                intent.putExtra("image", uri.toString());
//                setResult(2, intent);
                finish();
            }
        });
    }
}