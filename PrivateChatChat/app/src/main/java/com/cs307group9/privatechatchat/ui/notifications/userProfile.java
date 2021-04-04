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
    private Button changeAlias, changeBackground, deleteAccount, exitAccount;
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
        changeBackground = v.findViewById(R.id.changeBackground);
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


        changeBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setItems(new String[]{"Take Photo", "Album"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击后，具体处理，
                                Log.i("tag","which " + which);
                                Log.i("tag","dialog " + dialog);
                                // 判断 which 从而判断用户点击的是第几个
                                if (which == 0) {
                                    // 调用系统相机权限 弹出是否同意授权
                                    requestPermissions(new String[]{
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    },10);// 动态申请权限
                                } else if (which == 1) {
                                    // 调用系统相机 直接打开
                                    Intent intent;
                                    if (Build.VERSION.SDK_INT < 19) {
                                        intent = new Intent(Intent.ACTION_GET_CONTENT);
                                        intent.setType("image/*");
                                    } else {
                                        intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    }

//                                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                            "image/*"); // 设置类型 选图片
                                    startActivityForResult(intent,PHOTO_REQUEST);
                                }
                            }
                        });
                dialog.show();
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

    // 动态权限回调 requestCode:10 上面的requestCode
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // TODO 相机activity
        if (requestCode == 10) { // 系统相机申请权限返回10
            // 判断用户是否同意， PERMISSION_GRANTED = 0 : =>同意
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 调用相机activity
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE); // 参数="android.media.action.IMAGE_CAPTURE"
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "test.jpg")));
                startActivityForResult(intent,CAMERA_REQUEST); // 相机获取的数据的返回值
            }
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_REQUEST:
                switch (resultCode) {
                    case -1://-1表示拍照成功
                        File file = new File(Environment.getExternalStorageDirectory()
                                + "/test.jpg");
                        if (file.exists()) {
                            photoClip(Uri.fromFile(file));
                        }
                        break;
                    default:
                        break;
                }
                break;
            case PHOTO_REQUEST:
                if (data != null) {
                    photoClip(data.getData());
                }
                break;
            case PHOTO_CLIP:
                if (data != null) {
                    Toast toast = Toast.makeText(getContext(),"Image set", Toast.LENGTH_SHORT);
                    toast.show();
                    imageView.setImageURI(data.getData());
//                    Bundle extras = data.getExtras();
//                    if (extras != null) {
//                        Log.w("test", "data");
//                        Bitmap photo = extras.getParcelable("data");
//                        imageView.setImageBitmap(photo);
//                    }
                }
                break;
            default:
                break;
        }

    }

    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent();
        intent.setDataAndType(uri, "image/*");
//        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
//        intent.putExtra("crop", "true");
//        // aspectX aspectY 是宽高的比例
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        // outputX outputY 是裁剪图片宽高
//        intent.putExtra("outputX", 150);
//        intent.putExtra("outputY", 150);
//        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_CLIP);

    }


    public ImageView getImageView() {
        return imageView;
    }

    //    版权声明：本文为CSDN博主「Zichen1016」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//    原文链接：https://blog.csdn.net/weixin_48430685/article/details/110293040
}