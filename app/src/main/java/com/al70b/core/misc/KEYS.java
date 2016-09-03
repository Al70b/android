package com.al70b.core.misc;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 5/6/2015.
 */
public final class KEYS {


    public static class SHARED_PREFERENCES {
        /**
         * Keys for shared preferences
         */

        public static final String USER_ID = "user_id";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String USER = "user";
        public static final String USERNAME = "username";
        public static final String NAME = "name";
        public static final String PASSWORD = "password";
        public static final String PROFILE_PICTURE = "profile_picture";
        public static final String DONT_ASK_FOR_PROFILE_PICTURE_UPLOAD = "DONT_ASK" ;
        public static final String CONNECTION_FAILED = "connection to server failed!";
    }


    public static class SERVER {

        /**
         * Constants for server side parsing
         */
        public static final String ID = "id";
        public static final String TRANSLATION_DATE = "last_edit";
        public static final String TRANSLATION_DATE_FORMAT = "y-M-d H:M:S";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String USERNAME = "username";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String SUCCESS = "success";
        public static final String FAILURE = "failure";
        public static final String DATA = "data";
        public static final String USER_ID = "user_id";
        public static final String RESULT = "result";
        public static final String PHOTOS = "photos";
        public static final String NAME = "name";
        public static final String GENDER = "sex";
        public static final String BIRTH_DATE = "birth_date";
        public static final String SOCIAL_STATUS = "relationship_status";
        public static final String CITY = "city";
        public static final String COUNTRY = "country";
        public static final String MATCH_GENDER = "match_sex";
        public static final String INTERESTED_PURPOSE = "relationship";
        public static final String HEIGHT = "height";
        public static final String BODY = "body";
        public static final String EYES = "eyes";
        public static final String WORK = "work";
        public static final String GENDER_MALE = "1";
        public static final String GENDER_FEMALE = "2";
        public static final String GENDER_BOTH = "3";
        public static final String CHAT = "chat";
        public static final String FRIENDSHIP = "friendship";
        public static final String LOVE = "love";
        public static final String DATE = "dating";
        public static final String MARRIAGE = "marriage";
        public static final String EDUCATION = "education";
        public static final String RELIGION = "religion";
        public static final String ALCOHOL = "alcohol";
        public static final String SMOKING = "smoking";
        public static final String DESCRIPTION = "description";
        public static final String ERROR_MSG = "error_msg";
        public static final String DAY = "day";
        public static final String MONTH = "month";
        public static final String YEAR = "year";
        public static final String PICTURE_ID = "id";
        public static final String PICTURE_PATH = "image";
        public static final String THUMBNAIL = "thumbnail";
        public static final String PICTURE_CREATED_DATE = "create_date";
        public static final String IS_PROFILE_PICTURE = "main";
        public static final String PROFILE_PICTURE_PATH = "thumbnail";
        public static final String ONLINE_STATUS = "online_status";
        public static final String MAIN_PHOTO = "main_photo";
        public static final String FRIEND_STATUS = "friend_status";
        public static final String FRIENDSHIP_FRIEND = "friend";
        public static final String FRIENDSHIP_SENT = "sent";
        public static final String FRIENDSHIP_RECEIVED = "received";
        public static final String FRIENDSHIP_NONE = "none";
        public static final String PAGE = "page";
        public static final String RESULT_PER_PAGE = "results_per_page";
        public static final String STATUS_AVAILABLE = "available";
        public static final String STATUS_OFFLINE = "offline";
        public static final String STATUS_AWAY = "away";
        public static final String STATUS_BUSY = "busy";
        public static final String LAST = "last";
        public static final String NEW_PASSWORD = "new_password";
    }

    public static final String PROFILE_PICTURE_BITMAP = "ProfilePicture Bitmap";

    /**
     * @param <T> content object of the json array
     * @return list of T type elements which are the content of the json array
     * @throws JSONException
     */
    public static <T> List<T> parseJSONArray(JSONArray jsonArray) throws JSONException {
        List<T> result = new ArrayList<T>();

        // fill created array with json data
        for (int i = 0; i < jsonArray.length(); i++) {
            result.add((T) jsonArray.get(i));
        }

        return result;
    }

}
