package com.cs307group9.privatechatchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference myDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDatabase = FirebaseDatabase.getInstance().getReference("Message");

        TextView myText = findViewById(R.id.text);

        myDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                myText.setText(""); // Cleaning the text Area

                System.out.println(snapshot.getValue().toString());

                String[] sendMsg = sortMsg(snapshot.getValue().toString());

                for (String i : sendMsg) myText.append(i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                myText.setText("CANCELLED");

            }
        });
    }

    public void sendMessage(View view) {
        EditText myEditText = findViewById(R.id.editText);

        myDatabase.child(Long.toString(System.currentTimeMillis())).setValue(myEditText.getText().toString());
        myEditText.setText("");
    }

    private String[] sortMsg(String msg) {
        if (msg.contains("=")) {
            String[] messages = new String[1];
            if (msg.contains(","))  messages = msg.split(",");
            else                    messages[0] = msg;

            int         size = messages.length;
            String[]    sendMessage = new String[size];
            long[]      order = new long[size];
            int         pos = 0;

            for (String i : messages) {
                String tmpMsg = i.split("=")[1];
                if (tmpMsg.contains("}"))
                    sendMessage[pos] = i.substring(0, i.length() - 1) + "\n";
                else
                    sendMessage[pos] = i + "\n";

                if (i.split("=")[0].contains("{") || i.split("=")[0].contains(" "))
                    order[pos] = Long.parseLong(i.split("=")[0].substring(1));
                else
                    order[pos] = Long.parseLong(i.split("=")[0]);

                System.out.printf("String: %s\n", tmpMsg);
                System.out.println("Order");
                System.out.println(order);

                pos++;
            }

            quickSort(order, sendMessage, 0, size - 1);

            return sendMessage;
        }
        String[] messages = {""};

        return messages;
    }


    private void quickSort(long arr[], String[] msg, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(arr, msg, begin, end);

            quickSort(arr, msg, begin, partitionIndex-1);
            quickSort(arr, msg, partitionIndex+1, end);
        }
    }

    private int partition(long arr[], String[] msg, int begin, int end) {
        long pivot = arr[end];
        int i = (begin-1);

        for (int j = begin; j < end; j++) {
            if (arr[j] <= pivot) {
                i++;

                long swapTemp = arr[i];
                String swapTempMsg = msg[i];
                arr[i] = arr[j];
                msg[i] = msg[j];
                arr[j] = swapTemp;
                msg[j] = swapTempMsg;
            }
        }

        long swapTemp = arr[i+1];
        String swapTempMsg = msg[i+1];
        arr[i+1] = arr[end];
        msg[i+1] = msg[end];
        arr[end] = swapTemp;
        msg[end] =swapTempMsg;

        return i+1;
    }

}