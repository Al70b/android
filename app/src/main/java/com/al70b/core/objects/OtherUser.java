package com.al70b.core.objects;

import android.content.Context;
import android.util.Log;

import com.al70b.R;
import com.al70b.core.misc.KEYS;
import com.al70b.core.misc.Translator;
import com.al70b.core.server_methods.RequestsInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Naseem on 6/19/2015.
 */
public class OtherUser extends User {

    private static final String TAG = "OtherUser";
    private FriendStatus friendStatus;

    public OtherUser(Context context) {
        super(context);
        friendStatus = new FriendStatus(FriendStatus.NONE);
    }

    public OtherUser(Context context, int userID) {
        this(context);

        this.userID = userID;
    }

    public FriendStatus getFriendStatus() {
        return friendStatus;
    }

    public OtherUser basicParseJSONToUser(JSONObject jsonObject, final RequestsInterface.ResponseCallback<Object> responseCallback) {
        try {
            Translator translator = Translator.getInstance(context);

            // parse basic user data
            name = jsonObject.getString(KEYS.SERVER.NAME);
            dateOfBirth = super.parseDate(jsonObject.getString(KEYS.SERVER.BIRTH_DATE));

            if (jsonObject.has(KEYS.SERVER.COUNTRY))
                address = new Address(jsonObject.getString(KEYS.SERVER.CITY),
                        translator.translate(jsonObject.getString(KEYS.SERVER.COUNTRY), translator.getDictionary().COUNTRIES));

            // parse additional data required for this user
            email = jsonObject.getString(KEYS.SERVER.EMAIL);
            userID = jsonObject.getInt(KEYS.SERVER.ID);

            if (jsonObject.has(KEYS.SERVER.FRIEND_STATUS))
                friendStatus.setValue(jsonObject.getString(KEYS.SERVER.FRIEND_STATUS));

            onlineStatus = new OnlineStatus(jsonObject.getString(KEYS.SERVER.ONLINE_STATUS));

            String thumbPath = jsonObject.getString("main_photo");
            String thumbName = thumbPath.substring(thumbPath.lastIndexOf('/') + 1);
            if (thumbPath.trim().length() == 0 || thumbPath.compareTo("null") == 0) {
                profilePicture = null;
            } else {
                profilePicture = new Picture(userID, thumbName);
            }
        } catch (JSONException ex) {
            Log.e(TAG, ex.toString());
        }
        return this;
    }

    public OtherUser parseJSONToUser(JSONObject jsonObject) {
        return (OtherUser) super.parseJSONToUser(jsonObject);
    }

    public static class FriendStatus implements Serializable {
        public static final int FRIENDS = 1;
        public static final int RECEIVED_REQUEST_PENDING = 2;
        public static final int SENT_REQUEST_PENDING = 3;
        public static final int NONE = 4;

        private int value;
        private int drawableResID;
        private int drawableResIDActionBar;
        private int stringResID;

        public FriendStatus(int value) {
            setValue(value);
        }

        public FriendStatus(String valueStr) {
            setValue(valueStr);
        }

        public void setValue(String valueStr) {
            if (valueStr.compareTo(KEYS.SERVER.FRIENDSHIP_FRIEND) == 0) {
                value = FRIENDS;
            } else if (valueStr.compareTo(KEYS.SERVER.FRIENDSHIP_RECEIVED) == 0) {
                value = RECEIVED_REQUEST_PENDING;
            } else if (valueStr.compareTo(KEYS.SERVER.FRIENDSHIP_SENT) == 0) {
                value = SENT_REQUEST_PENDING;
            } else {
                value = NONE;
            }

            setResourcesID();
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
            setResourcesID();
        }

        public boolean isFriend() {
            return value == FRIENDS;
        }

        public boolean isReceivedRequestPending() {
            return value == RECEIVED_REQUEST_PENDING;
        }

        public boolean isSentRequestPending() {
            return value == SENT_REQUEST_PENDING;
        }

        public boolean isNoFriendRequest() {
            return value == NONE;
        }

        public int getDrawableResourceID() {
            return drawableResID;
        }

        public int getDrawableResourceIDForActionBar() {
            return drawableResIDActionBar;
        }

        public int getStringResourceID() {
            return stringResID;
        }

        private void setResourcesID() {
            switch (value) {
                case FRIENDS:
                    drawableResID = R.drawable.ic_action_delete_person;
                    drawableResIDActionBar = R.drawable.ic_action_delete_person_holo_light;
                    stringResID = R.string.unfriend;
                    break;
                case RECEIVED_REQUEST_PENDING:
                    drawableResID = R.drawable.ic_action_accept_person;
                    drawableResIDActionBar = R.drawable.ic_action_accept_person_holo_light;
                    stringResID = R.string.accept_friend_request;
                    break;
                case SENT_REQUEST_PENDING:
                    drawableResID = R.drawable.ic_action_delete_person;
                    drawableResIDActionBar = R.drawable.ic_action_delete_person_holo_light;
                    stringResID = R.string.cancel_friend_request;
                    break;
                case NONE:
                    drawableResID = R.drawable.ic_action_add_person;
                    drawableResIDActionBar = R.drawable.ic_action_add_person_holo_light;
                    stringResID = R.string.send_friend_request;
                    break;
            }
        }
    }
}
