package com.cs307group9.privatechatchat.ui.notifications;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.group.GroupChangeName;
import com.cs307group9.privatechatchat.group.GroupSettings;
import com.cs307group9.privatechatchat.ui.login.LoginActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class UserChangeBackground extends AppCompatActivity {

    private ImageButton back;
    private Button toDefault, custom;

    private static final int PIC_HEIGHT = 200;
    String file_str = Environment.getExternalStorageDirectory().getPath();
    File mars_file = new File(file_str + "/my_camera");
    File file_go = new File(file_str + "/my_camera/file.jpg");
    private final static int TAKE_PHOTO = 2123;
    final public static int REQUEST_PERMISSION_CAMERA_CODE = 123;


    private static final int CROP_PHOTO = 2;
    private static final int REQUEST_CODE_PICK_IMAGE=3;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 6;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE2 = 7;
    private  File output;
    private Uri imageUri;

    private ImageView bg;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_change_background);

        back = (ImageButton) findViewById(R.id.changeBGBack);
        toDefault = (Button) findViewById(R.id.toDefaultBG);
        custom = (Button) findViewById(R.id.customBG);
        bg = (ImageView) findViewById(R.id.chatBackground);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserChangeBackground.this, ChangeBackgroundDefault.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePicDialog();

                //write uri to local file
                File f = new File("backgroundURI.txt");
                try {
                    FileWriter fw = new FileWriter(f, false);
                    fw.write(imageUri.toString());
                    fw.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    /**弹出选择框
     * 1、拍照
     * 2、选择本地相片
     */
    private void showChoosePicDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");
        String[] items = {"Take a photo","Choose from library"};
        builder.setNegativeButton("Cancel",null);
        builder.setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch(which){
                    case 0://Choose from library
                        Log.e("Dialog","TAKE picture");
                        takePhoto();
                        break;
                    case 1://Take photo
                        Log.e("Dialog","CHOOSE picture");
                        choosePhoto();
                        break;
                }
            }

        });
        builder.show();
    }

    public void takePhoto(){

        //判断当前系统是否高于或等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("VERSION","System version >= 6.0");
            //当前系统大于等于6.0
            if (ContextCompat.checkSelfPermission(UserChangeBackground.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.i("PEMISSION","Has camera permission");
                //具有拍照权限，直接调用相机
                //具体调用代码
                take_Photos();
            } else {
                Log.i("PEMISSION","Does not have camera permission, requesting");
                //不具有拍照权限，需要进行权限申请
                ActivityCompat.requestPermissions(UserChangeBackground.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_CAMERA_CODE);
            }
        } else {
            Log.i("VERSION","System version < 6.0");
            //当前系统小于6.0，直接调用拍照
        }
    }

    public void choosePhoto(){

        //判断当前系统是否高于或等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("VERSION","System version >= 6.0");
            //当前系统大于等于6.0
            if (ContextCompat.checkSelfPermission(UserChangeBackground.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.i("PEMISSION","Can read from library");
                //具有拍照权限，直接调用相机
                //具体调用代码
                choose_Photos();
            } else {
                Log.i("PEMISSION","Cannot read from library, requesting");
                //不具有拍照权限，需要进行权限申请
                ActivityCompat.requestPermissions(UserChangeBackground.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_CAMERA_CODE);
            }
        } else {
            Log.i("VERSION","System version < 6.0");
            //当前系统小于6.0，直接调用拍照
        }
    }

    /**
     * 拍照
     */
    void take_Photos(){
        //最后一个参数是文件夹的名称，可以随便起
        File file=new File(Environment.getExternalStorageDirectory(),"Take a photo");
        if(!file.exists()){
            file.mkdir();
        }
        output=new File(file,System.currentTimeMillis()+".jpg");//这里将时间作为不同照片的名称
        try {
            if (output.exists()) {
                output.delete();//如果该文件夹已经存在，则删除它，否则创建一个
            }
            output.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageUri = Uri.fromFile(output);//隐式打开拍照的Activity，并且传入CROP_PHOTO常量作为拍照结束后回调的标志
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CROP_PHOTO);

    }

    /**
     * 从相册选取图片
     */
    void choose_Photos(){
        /**
         * 打开选择图片的界面
         */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);

    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        switch (req) {
            //拍照的请求标志
            case CROP_PHOTO:
                if (res == RESULT_OK) {
                    try {
                        //该uri就是照片文件夹对应的uri
                        bg.setImageURI(imageUri);

                        Toast.makeText(this, "Changed", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(this, "Program Crashed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.i("Take photo", "Failed");
                }

                break;
            /**
             * 从相册中选取图片的请求标志
             */

            case REQUEST_CODE_PICK_IMAGE:
                if (res == RESULT_OK) {
                    try {
                        /**
                         * 该uri是上一个Activity返回的
                         */
                        imageUri = data.getData();
                        bg.setImageURI(imageUri);
                        Toast.makeText(this, "Changed", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("tag", e.getMessage());
                        Toast.makeText(this, "Program Crashed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.i("Pick image", "Failed");
                }

                break;

            default:
                break;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                takePhoto();
            } else
            {
                // Permission Denied
                Toast.makeText(UserChangeBackground.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE2)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                choosePhoto();
            } else
            {
                // Permission Denied
                Toast.makeText(UserChangeBackground.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}