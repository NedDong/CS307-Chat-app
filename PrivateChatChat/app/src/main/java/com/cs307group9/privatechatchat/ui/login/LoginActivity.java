package com.cs307group9.privatechatchat.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs307group9.privatechatchat.MainActivity;
import com.cs307group9.privatechatchat.MainScreenActivity;
import com.cs307group9.privatechatchat.OutputInputHandler;
import com.cs307group9.privatechatchat.PrefManager;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.SocketHandler;
import com.cs307group9.privatechatchat.entity.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_PASSWORD = "password";
    final String KEY_PREF_FRIENDLIST = "friendlist";
    final String KEY_PREF_ISLOGIN = "islogin";
    final String KEY_PREF_SOCKET = "socket";

    static String hostname = "10.0.2.2";
    //="cs307-chat-app.webredirect.org";
            //= "10.0.2.2";
    int type = -1; // 0 means LogIn, 1 means Register
    static int port = 1111;

    String username;
    String password;

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
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);
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

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
//                if (loginResult.getError() != null) {
//                    showLoginFailed(loginResult.getError());
//                }
                if (!sharedPreferences.getBoolean(KEY_PREF_ISLOGIN, true)) {
                    System.out.println("=====WRONG!!====");
                    showLoginFailed("Wrong Username/Password");
                }
                else if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    try {
//                        output.close();
//                        input.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    startActivity(intent);

                    finish();
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                // finish();
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
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
                type = 0;
                new Thread(new ServerConnectThread()).start();
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

                new Thread(new LoginActivity.SendUserInfoThread()).start();
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
                if (!result.contains("Successful")) {
                    editor.putBoolean(KEY_PREF_ISLOGIN, false);
                    editor.commit();
                    return;
                }
                editor.putBoolean(KEY_PREF_ISLOGIN, true);
                editor.commit();
                int num = (int) input.readObject();

                for (int i = 0; i < num; i++) {
                    String response = (String) input.readObject();
                    System.out.println(response);

                    String name = (String) input.readObject();
                    int uid = (int) input.readObject();
                    InetAddress inetAddress = (InetAddress) input.readObject();
                    String psw = (String) input.readObject();
                    User friend = new User(name, uid, inetAddress, psw);
                    frindlist.put(name, friend);
                    System.out.println("add friend successfully" + friend.getUsername());
                }

                Gson gson = new Gson();
                //Type hashMapType = new TypeToken<HashMap<String, User>>() {}.getType();
                //HashMap<String, User> frindMap = gson.fromJson(gson.toJson(frindlist), hashMapType);
                String json = gson.toJson(frindlist);

                editor.putString(KEY_PREF_FRIENDLIST, json);
                editor.commit();

                Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//                try {
//                    output.close();
//                    input.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                startActivity(intent);
                // finish();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}