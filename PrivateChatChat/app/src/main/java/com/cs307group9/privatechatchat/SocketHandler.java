package com.cs307group9.privatechatchat;

import java.net.Socket;

public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket() {
        return socket;
    }

    public static synchronized void setSocket(Socket socket) {
        SocketHandler.socket = socket;
    }
}
