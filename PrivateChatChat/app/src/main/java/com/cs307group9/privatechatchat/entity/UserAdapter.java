package com.cs307group9.privatechatchat.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.cs307group9.privatechatchat.R;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.LinkedList;

public class UserAdapter extends BaseAdapter {

    private LinkedList<User> userData;
    private Context userContext;

    public UserAdapter(LinkedList<User> userData, Context userContext) {
        this.userData = userData;
        this.userContext = userContext;
    }

    @Override
    public int getCount() { return userData.size(); }

    @Override
    public Object getItem(int position) { return null; }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(userContext).inflate(R.layout.fragment_contacts, parent, false);
//        TextView txt_userName = (TextView) convertView.findViewById(R.id.textView5);
//        txt_userName.setText(userData.get(position).getUsername());
        return convertView;
    }

}
