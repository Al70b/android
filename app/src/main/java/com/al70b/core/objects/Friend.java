package com.al70b.core.objects;

import android.graphics.Bitmap;

/**
 * Created by Naseem on 6/2/2015.
 */
public class Friend {

    private String userID, name, profilePictureTitle;
    private Bitmap profilePictureBitmap;

    public Friend(String userID, String name, String profilePictureTitle, Bitmap profilePictureBitmap) {
        this.userID = userID;
        this.name = name;
        this.profilePictureTitle = profilePictureTitle;
        this.profilePictureBitmap = profilePictureBitmap;
    }

    public Friend(String userID, String name, String profilePictureTitle) {
        //this(userID, name, profilePictureTitle);
        // read picture from storage and set it here
        //this.profilePicture = profilePicture;
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getProfilePictureTitle() {
        return profilePictureTitle;
    }

    public Bitmap getProfilePictureBitmap() {
        return profilePictureBitmap;
    }
}
