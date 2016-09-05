package com.al70b.core.objects;

/**
 * Created by Naseem on 8/6/2015.
 */
public class EndMessage extends Message {
    // boolean to when to show profile picture
    private boolean profilePictureVisible;

    public EndMessage(long id, String message, long dateTime, int type) {
        super(id, message, dateTime, type);

        profilePictureVisible = true;
    }

    public boolean isProfilePictureVisible() {
        return profilePictureVisible;
    }

    public void setProfilePictureVisible() {
        profilePictureVisible = true;
    }

    public void setProfilePictureInvisible() {
        profilePictureVisible = false;
    }


}
