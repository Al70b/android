package com.al70b.core.fragments.Items;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Naseem on 6/1/2015.
 * <p/>
 * This class represents conversations in messages
 */
public class ConversationItem {
    public int userID;
    public String name, lastMessage, lastMessageDate;
    public String profilePicture;
    public boolean highlight;
    long conversationID;

    public ConversationItem(long conversationID, int userID, String name, String lastMessage, String lastMessageDate, String profilePicture) {
        this.conversationID = conversationID;
        this.userID = userID;
        this.name = name;
        this.lastMessage = lastMessage;
        this.profilePicture = profilePicture;
        this.lastMessageDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(Long.parseLong(lastMessageDate) * 1000));
        ;
        this.highlight = false;
    }
}
