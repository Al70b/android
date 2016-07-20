package com.al70b.core.objects;

import java.io.Serializable;

/**
 * Created by Naseem on 5/17/2015.
 */
public class FriendsDrawerItem implements Serializable, Comparable {

    public int id;
    public long timestamp;
    public String name, profilePicture;
    public String status, statusMessage;
    public boolean isMessageUnread;

    public FriendsDrawerItem(int id, long timestamp, String name, String profilePicture,
                             String status, String statusMessage) {
        this.id = id;
        this.timestamp = timestamp;
        this.name = name;
        this.profilePicture = profilePicture;
        this.status = status;
        this.statusMessage = statusMessage;
    }

    public boolean getMessageUnread() {
        return isMessageUnread;
    }

    public void setMessageUnread(boolean b) {
        this.isMessageUnread = isMessageUnread;
    }

    @Override
    public int compareTo(Object another) {
        return -String.valueOf(this.timestamp).compareTo(String.valueOf(((FriendsDrawerItem) another).timestamp));
    }
}
