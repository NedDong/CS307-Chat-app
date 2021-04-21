package com.cs307group9.privatechatchat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

// https://stackoverflow.com/questions/46221989/android-socket-connect-in-background

public class MyService extends Service {

    public static final String START_SERVER = "startserver";
    public static final String STOP_SERVER = "stopserver";
    public static final int SERVERPORT = 8080;

    Thread serverThread;
    ServerSocket serverSocket;

    public MyService() {

    }

    //called when the services starts
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //action set by setAction() in activity
        String action = intent.getAction();
        if (action.equals(START_SERVER)) {
            //start your server thread from here
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
        }
        if (action.equals(STOP_SERVER)) {
            //stop server
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {}
            }
        }

        //configures behaviour if service is killed by system, see documentation
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            try {

                String read = input.readLine();

                //update ui
                //best way I found is to save the text somewhere and notify the MainActivity
                //e.g. with a Broadcast
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}