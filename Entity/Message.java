import java.util.*;

public class Message {
    private String message_type;
    private String message_data;
    //the sender and receiver should not be changed and can only be viewed
    private User sender;
    private User receiver;

    public Message(String type, String data, User sender, User receiver) {
        message_type = type;
        message_data = data;
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String type) {
        message_type = type;
    }

    public String getMessage_data() {
        return message_data;
    }

    public void setMessage_data(String data) {
        message_data = data;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }
}
