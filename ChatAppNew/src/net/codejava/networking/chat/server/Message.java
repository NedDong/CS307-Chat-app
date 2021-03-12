package net.codejava.networking.chat.server;

public class Message {
    private String messageType;
    private String username;
    private String password;

    public Message(String messageType, String username, String password){
        this.messageType = messageType;
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getMessageType() {
        return messageType;
    }
}
