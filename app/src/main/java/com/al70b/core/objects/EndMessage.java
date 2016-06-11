package com.al70b.core.objects;

import android.graphics.Bitmap;

/**
 * Created by Naseem on 8/6/2015.
 */
public class EndMessage extends Message {


    // boolean to when to show profile picture
    private boolean profilePictureVisible;


    private Bitmap profilePictureBitmap;


    public EndMessage(long id, String message, long dateTime, int type) {
        super(id, message, dateTime, type);

        profilePictureVisible = false;
    }

    public EndMessage(long id, String message, long dateTime, int type, Bitmap bitmap) {
        super(id, message, dateTime, type);

        profilePictureVisible = true;
        profilePictureBitmap = bitmap;
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

    public Bitmap getProfilePictureBitmap() {
        return profilePictureBitmap;
    }

    public void setProfilePictureBitmap(Bitmap bitmap) {
        this.profilePictureBitmap = bitmap;
    }

    // this is called to free memory
    public void removeProfilePictureBitmap() {
        setProfilePictureBitmap(null);
    }

}
