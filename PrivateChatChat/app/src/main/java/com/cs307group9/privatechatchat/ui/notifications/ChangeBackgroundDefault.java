package com.cs307group9.privatechatchat.ui.notifications;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_background_default);

        radioBG = (RadioGroup) findViewById(R.id.radioBGGroup);
        applyButton = (Button) findViewById(R.id.UserApplyBG);
        bg = (ImageView) findViewById(R.id.chatBackground);
        back = (ImageButton) findViewById(R.id.changeBGDefaultBack);

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
                    case R.id.radioButton1:
                        bg.setImageURI(Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.b1));
                        break;

                    case R.id.radioButton2:
                        bg.setImageURI(Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.b2));
                        break;

                    case R.id.radioButton3:
                        bg.setImageURI(Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.b3));
                        break;

                    case R.id.radioButton4:
                        bg.setImageURI(Uri.parse("android.resource://" + getPackageName()
                                + "/" + R.drawable.b4));
                        break;

                    default:
                        break;

                }

                Toast.makeText(ChangeBackgroundDefault.this, "Chat Background Change Successful", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}