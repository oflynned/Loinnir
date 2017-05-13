package com.syzible.loinnir.objects;

/**
 * Created by ed on 07/05/2017.
 */

public class Message {
    private User sender, recipient;
    private long time;
    private String contents;

    public Message(User sender, User recipient, long time, String contents) {
        this.sender = sender;
        this.recipient = recipient;
        this.time = time;
        this.contents = contents;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public long getTime() {
        return time;
    }

    public String getContents() {
        return contents;
    }
}
