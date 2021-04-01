package com.cs307group9.privatechatchat.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cs307group9.privatechatchat.MainActivity;
import com.cs307group9.privatechatchat.R;
import com.cs307group9.privatechatchat.entity.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Contacts extends Fragment {

    private ContactViewModel dashboardViewModel;

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    final String KEY_PREF_APP = "myPref";
    final String KEY_PREF_USERNAME = "username";
    final String KEY_PREF_PASSWORD = "password";
    final String KEY_PREF_FRIENDLIST = "friendlist";
    final String KEY_PREF_ISLOGIN = "islogin";
    final String KEY_PREF_SOCKET = "socket";
    final String KEY_PREF_MUTE = "mute";
    final String KEY_PREF_BLOCK = "block";
    final String LIST = "LIST";

    View view;
    LinearLayout lin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dashboardViewModel =
                new ViewModelProvider(this).get(ContactViewModel.class);
        view = inflater.inflate(R.layout.fragment_contact, container, false);

        lin = new LinearLayout(getActivity());

        sharedPreferences = getContext().getSharedPreferences(KEY_PREF_APP, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_PREF_FRIENDLIST, "");

        Type type = new TypeToken<HashMap<String, User>>(){}.getType();

        HashMap<String, User> friendList = gson.fromJson(json, type);

        User[] users = new User[friendList.size()];
        String[] names = new String[friendList.size()];

        int i = 0;
        for (Map.Entry<String, User> entry : friendList.entrySet()) {
            names[i] = entry.getKey();
            users[i] = friendList.get(names[i]);
            i++;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        for (int j = 0; j <= i; j++) {
            Button button = new Button(getContext());
            button.setText(names[j]);
            button.setLayoutParams(params);
            button.setTextSize(20);
            button.setBackgroundColor(0xFF7384DF);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public  void onClick(View v) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });

            lin.addView(button);

        }

        ViewGroup viewGroup = (ViewGroup) view;
        viewGroup.addView(lin);
        return viewGroup;
    }

}