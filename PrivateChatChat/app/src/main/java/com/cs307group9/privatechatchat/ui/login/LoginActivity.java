package com.cs307group9.privatechatchat.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs307group9.privatechatchat.MainScreenActivity;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_USERID   = "userid";
    final String KEY_PREF_PASSWORD = "password";
    final String KEY_PREF_FEEDBACK = "feedback";
    final String KEY_PREF_USER_AVATAR = "cur_user_avatar";

    final String KEY_PREF_FRIENDLIST_NAME = "friendlist_name";
    final String KEY_PREF_FRIENDLIST_UID  = "friendlist_uid";
    final String KEY_PREF_FRIENDLIST_ADDR = "friendlist_addr";
    final String KEY_PREF_FRIENDLIST_PSW  = "friendlist_psw";

    final String KEY_PREF_GROUPLIST_NAME = "grouplist_name";
    final String KEY_PREF_GROUPLIST_GID = "grouplist_gid";
    final String KEY_PREF_GROUPLIST_USERS = "grouplist_users";

    final String KEY_PREF_ISLOGIN = "islogin";
    final String KEY_PREF_SOCKET = "socket";

    private boolean requestGroup = false;

    static String hostname = "cs307-chat-app.webredirect.org";
    //="cs307-chat-app.webredirect.org";
            //=
//    "10.0.2.2";

    static int port = 12345;
    int type = -1; // 0 means LogIn, 1 means Register

    boolean isLogIN = false;

    String username;
    String password;
    String feedback;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    boolean connectServer = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        sharedPreferences = getSharedPreferences(KEY_PREF_APP, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText feedbackEditText = findViewById(R.id.feedback);
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);
        final Button feedbackButton = findViewById(R.id.sendFeedback);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                type = 1;
                new Thread(new ServerConnectThread()).start();

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();

                loadingProgressBar.setVisibility(View.VISIBLE);

                type = 0;
                editor.putBoolean(KEY_PREF_ISLOGIN, false);
                editor.commit();
                new Thread(new ServerConnectThread()).start();

                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());

            }
        });

        feedbackButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString();
                feedback = feedbackEditText.getText().toString();

                //loadingProgressBar.setVisibility(View.VISIBLE);

                type = 0;
                editor.putBoolean(KEY_PREF_ISLOGIN, false);
                editor.commit();
                new Thread(new feedbackThread()).start();


            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(String errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
    }

    private ObjectOutputStream output;
    private ObjectInputStream input;

    class ServerConnectThread implements Runnable {
        public void run() {
            System.out.println("==== I Am Currently Running Thread 1===");
            Socket socket;
            try {
                socket = new Socket(hostname, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());

                if (!requestGroup)
                    new Thread(new LoginActivity.SendUserInfoThread()).start();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class feedbackThread implements Runnable {
        public void run() {
            System.out.println("==== I Am Currently Running Thread feedback===");

            try {
                Socket socket;
                socket = new Socket(hostname, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input  = new ObjectInputStream(socket.getInputStream());

                //new Thread(new LoginActivity.SendUserInfoThread()).start();
                String typeStr = "FED";


                // Message msg = new Message(typeStr, username, password);

                System.out.println(username);
                System.out.println(feedback);

//                editor.putString(KEY_PREF_USERNAME, username);
//                editor.putString(KEY_PREF_FEEDBACK, feedback);
                editor.commit();

                try {
                    output.writeObject(typeStr);
                    output.writeObject(username);
                    output.writeObject(feedback);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O Error: " + ex.getMessage());
            }
        }
    }

    class SendUserInfoThread implements Runnable {
        @Override
        public void run() {
            String typeStr;
            if (type == 0) typeStr = "LOG";
            else typeStr = "REG";

            // Message msg = new Message(typeStr, username, password);

            System.out.println(username);
            System.out.println(password);

            editor.putString(KEY_PREF_USERNAME, username);
            editor.putString(KEY_PREF_PASSWORD, password);
            editor.commit();

            try {
                output.writeObject(typeStr);
                output.writeObject(username);
                output.writeObject(password);
                new Thread(new RecieveFriendList()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    HashMap<String, User> frindlist = new HashMap<>();

    class RecieveFriendList implements Runnable {
        @Override
        public void run() {
            try {
                String result = (String) input.readObject();
                System.out.printf("============RESULT: %s\n", result);
                if (!result.contains("uccess")) {
                    editor.putBoolean(KEY_PREF_ISLOGIN, false);
                    editor.commit();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            final Toast toast = Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                    return;
                }
                editor.putBoolean(KEY_PREF_ISLOGIN, true);
                editor.commit();

                runOnUiThread(new Runnable() {
                    public void run() {
                        final Toast toast = Toast.makeText(LoginActivity.this, "LOG IN SUCCESS, WELCOME" + username, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });

                int num = (int) input.readObject();

                String[] name = new String[num];
                int[] uid = new int[num];
                InetAddress[] inetAddress = new InetAddress[num];
                String[] psw = new String[num];
                String[] avatar = new String[num];

                for (int i = 0; i < num; i++) {
                    String response = (String) input.readObject();
                    System.out.println(response);

                    name[i] = (String) input.readObject();
                    uid[i] = (int) input.readObject();
                    inetAddress[i] = (InetAddress) input.readObject();
                    psw[i] = (String) input.readObject();
                    avatar[i] = (String) input.readObject();
                    Log.e("LOG", "AVATAR : " + avatar[i]);

                    if (name[i].contains(username)) {
                        editor.putString(KEY_PREF_USER_AVATAR, avatar[i]);
                        editor.putInt(KEY_PREF_USERID, uid[i]);
                        editor.commit();
                    }
                }



                Gson gson = new Gson();

                String json = gson.toJson(name);
                editor.putString(KEY_PREF_FRIENDLIST_NAME, json);
                json = gson.toJson(uid);
                editor.putString(KEY_PREF_FRIENDLIST_UID, json);
                json = gson.toJson(inetAddress);
                editor.putString(KEY_PREF_FRIENDLIST_ADDR, json);
                json = gson.toJson(psw);
                editor.putString(KEY_PREF_FRIENDLIST_PSW, json);

                editor.commit();

//                new Thread(new RecieveGroupList()).start();

                Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                finish();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

//    class RecieveGroupList implements Runnable {
//        @Override
//        public void run() {
//            try {
//                requestGroup = true;
//
//                output.writeObject("GetGroupList");
//                System.out.println("+++++++++++++++++++++++++++++++++++");
//
//                output.writeObject(username);
//
//                // Receive the number of groups
//                int num = (int) input.readObject();
//
//                if (num <= 0) {
//                    System.out.println("========THERE IS NO GROUP========");
//                    return;
//                }
//                else {
//                    System.out.printf("========THERE IS %d GROUPS========\n", num);
//                }
//
//                int[] groupList = new int[num];
//                String[] groupName = new String[num];
//
//                for (int i = 0; i < num; i++) {
//                    groupList[i] = (int) input.readObject();
//                    groupName[i] = (String) input.readObject();
//                }
//
//                Gson gson = new Gson();
//                String json = gson.toJson(groupList);
//                editor.putString(KEY_PREF_GROUPLIST_GID, json);
//                json = gson.toJson(groupName);
//                editor.putString(KEY_PREF_GROUPLIST_NAME, json);
//                editor.commit();
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//            catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}