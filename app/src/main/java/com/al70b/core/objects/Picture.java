package com.al70b.core.objects;

import com.al70b.core.misc.KEYS;
import com.al70b.core.server_methods.ServerConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Naseem on 5/11/2015.
 */
public class Picture implements Serializable {

    // picture's ID
    protected int id;

    // user's ID
    protected int userID;

    // picture name in the server
    protected String pictureName;

    // full path to picture on server
    protected String pictureFullPath;

    // picture thumbnailName name
    protected String thumbnailName;

    // full path to thumbnail on server
    protected String thumbnailFullPath;

    // created date of the picture
    protected String createDate;

    protected boolean isProfilePicture;

    public Picture(int id, int userID, String pictureName, String thumbnailName, String createDate, boolean isProfilePicture) {
        this.id = id;
        this.userID = userID;
        this.pictureName = pictureName;
        this.pictureFullPath = ServerConstants.CONSTANTS.SERVER_PICTURES_FULL_URL + pictureName;
        this.thumbnailName = thumbnailName;
        this.thumbnailFullPath = ServerConstants.CONSTANTS.SERVER_THUMBNAILS_FULL_URL + thumbnailName;
        this.createDate = createDate;
        this.isProfilePicture = isProfilePicture;
    }

    public Picture(int userID, String thumbnailName) {
        this.id = -1;
        this.userID = userID;
        this.thumbnailName = thumbnailName;
        this.thumbnailFullPath = ServerConstants.CONSTANTS.SERVER_THUMBNAILS_FULL_URL + thumbnailName;
    }

    public Picture(String pictureName, String pictureUri, String createDate) {
        this.id = -1;
        this.pictureName = pictureName;
        this.pictureFullPath = pictureUri;
        this.createDate = createDate;
    }

    public static Picture parseJSONToPicture(JSONObject jsonObject) throws JSONException {
        int id, userID;
        boolean isProfilePicture;
        String picturePath, pictureName, thumbPath, thumbName, createDate;

        // parse data from json object
        id = jsonObject.getInt(KEYS.SERVER.PICTURE_ID);
        userID = jsonObject.getInt(KEYS.SERVER.USER_ID);

        // get picture's name
        picturePath = jsonObject.getString(KEYS.SERVER.PICTURE_PATH);
        pictureName = picturePath.substring(picturePath.lastIndexOf('/') + 1);

        // get thumbnail's name
        thumbPath = jsonObject.getString(KEYS.SERVER.THUMBNAIL);
        thumbName = thumbPath.substring(thumbPath.lastIndexOf('/') + 1);

        // get create date
        createDate = jsonObject.getString(KEYS.SERVER.PICTURE_CREATED_DATE);

        // wither profile picture or not
        isProfilePicture = jsonObject.getInt(KEYS.SERVER.IS_PROFILE_PICTURE) == 1;

        Picture pic = new Picture(id, userID, pictureName, thumbName, createDate, isProfilePicture);

        // return picture object
        return pic;
    }

    public int getId() {
        return id;
    }

    public int getUserID() {
        return userID;
    }

    public String getThumbnailName() {
        return thumbnailName;
    }

    public String getThumbnailFullPath() {
        return thumbnailFullPath;
    }

    public boolean isProfilePicture() {
        return isProfilePicture;
    }

    public String getPictureFullPath() {

        return pictureFullPath;
    }
}