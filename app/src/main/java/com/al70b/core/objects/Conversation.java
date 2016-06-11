package com.al70b.core.objects;

/**
 * Created by Naseem on 5/25/2015.
 */
public class Conversation {

    private String name, lastMessage, lastMessageDate;
    private String profilePicture;

    public Conversation(String name, String lastMessage, String lastMessageDate, String profilePicture) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
        this.profilePicture = profilePicture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
