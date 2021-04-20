package com.cs307group9.privatechatchat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class OutputInputHandler {
    private static ObjectOutputStream output;
    private static ObjectInputStream input;

    public static synchronized ObjectOutputStream getOutput() {
        return output;
    }

    public static synchronized ObjectInputStream getInput() {
        return input;
    }

    public static synchronized void setOutput(ObjectOutputStream output) {
        OutputInputHandler.output = output;
    }

    public static synchronized void setInput(ObjectInputStream input) {
        OutputInputHandler.input = input;
    }
}
