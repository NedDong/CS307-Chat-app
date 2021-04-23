package com.cs307group9.privatechatchat.ui.notifications;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.group.GroupChangeAvatar;

public class ChangeBackgroundDefault extends AppCompatActivity {

    ImageButton back;
    RadioGroup radioBG;
    ImageView bg;
    Button applyButton;

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USER_BG = "user_bg";

    String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_background_default);

        radioBG = (RadioGroup) findViewById(R.id.radioBGGroup);
        applyButton = (Button) findViewById(R.id.UserApplyBG);
        //bg = (ImageView) findViewById(R.id.chatBackground);
        back = (ImageButton) findViewById(R.id.changeBGDefaultBack);

        uri = "";
        sharedPreferences = getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int avatarNum = radioBG.getCheckedRadioButtonId();
                switch (avatarNum) {
                    case R.id.radioBG1:
                        uri += "android.resource://" + getPackageName()
                                + "/" + R.drawable.b1;

                        break;

                    case R.id.radioBG2:
                        uri += "android.resource://" + getPackageName()
                                + "/" + R.drawable.b2;

                        break;

                    case R.id.radioBG3:
                        uri += "android.resource://" + getPackageName()
                                + "/" + R.drawable.b3;

                        break;

                    case R.id.radioBG4:
                        uri += "android.resource://" + getPackageName()
                                + "/" + R.drawable.b4;

                        break;

                    default:
                        break;
                }
                editor.putString(KEY_PREF_USER_BG, uri);
                editor.commit();
                Log.e("User chat bg changed", uri);
                Toast.makeText(ChangeBackgroundDefault.this, "Chat Background Change Successful", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}