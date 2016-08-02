package com.al70b.core.objects;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Naseem on 5/26/2015.
 * <p/>
 * A class representing message object, where each conversation is compound of list
 * of message objects.
 * There are two constructors, one user for user's message and the other is used for
 * member's message
 */
public class Message implements Serializable {

    public class Type {

        // as in CometChat API
        public static final int REGULAR = 10;
        // 11, 12, 13, 14 are ignored
        public static final int CALL_ACCEPTED = 31;
        public static final int INCOMING_CALL = 32;
        public static final int OUTGOING_BUSY_TONE = 33;
        public static final int END_CALL = 34;
        public static final int CALL_REJECTED = 35;
        public static final int CANCEL_CALL = 36;
        public static final int NO_ANSWER = 37;
        public static final int INCOMING_BUSY_TONE = 38;

        // mine
        public static final int CALL_SENT = 39;
    }

    // message id
    private long id;

    // string holding the message
    private String message;

    // date + time of the message when it was created
    private long dateTime;

    // the message type
    private int messageType;

    // if this message still valid
    private boolean active;

    // if message is fetched from server
    private boolean fetched;

    public Status status;

    public Message(long id, String message, long dateTime, int messageType) {
        this.id = id;
        this.message = message;
        this.dateTime = dateTime;
        this.messageType = messageType;

        active = true;
        status = Status.NONE;
    }

    public enum Status {
        NONE,
        SENDING,
        SENT,
        FAILED_TO_SEND
    }

    public long getMessageID() {
        return id;
    }

    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public long getDateTime() {
        return dateTime;
    }

    /**
     * @return return a string representing message DateTime
     */
    public String getDateTimeString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(dateTime * 1000));
    }

    public int getMessageType() {
        return messageType;
    }

    public boolean isUserMessage() {
        return !(this instanceof EndMessage);
    }

    public boolean isMessageActive() {
        return active;
    }

    public void setMessageInactive() {
        active = false;
    }

    public boolean isMessageFetched() {
        return fetched;
    }

    public void setMessageFetched() {
        fetched = true;
    }


}
