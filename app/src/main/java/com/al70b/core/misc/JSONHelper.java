package com.al70b.core.misc;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 5/6/2015.
 */
public class JSONHelper {


    public static final String USER_ID = "user_id";
    public static final String JSON_OBJECT = "json object";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String USER = "user";
    public static final String USERNAME = "username";
    public static final String NAME = "name";
    public static final String CONNECTION_FAILED = "connection to server failed!";
    public static final String PROFILE_PICTURE = "profile_picture";
    public static final String GENDER_MALE = "Male";
    public static final String GENDER_FEMALE = "Female";
    public static final String GENDER_BOTH = "Both";

    /**
     * Constants for server side parsing *
     */
    public static final String SERVER_ID = "id";
    public static final String SERVER_TRANSLATION_DATE = "last_edit";
    public static final String SERVER_TRANSLATION_DATE_FORMAT = "y-M-d H:M:S";
    public static final String SERVER_ACCESS_TOKEN = "access_token";
    public static final String SERVER_USERNAME = "username";
    public static final String SERVER_EMAIL = "email";
    public static final String SERVER_PASSWORD = "password";
    public static final String SERVER_SUCCESS = "success";
    public static final String SERVER_FAILURE = "failure";
    public static final String SERVER_DATA = "data";
    public static final String SERVER_USER_ID = "user_id";
    public static final String SERVER_RESULT = "result";
    public static final String SERVER_PHOTOS = "photos";
    public static final String SERVER_NAME = "name";
    public static final String SERVER_GENDER = "sex";
    public static final String SERVER_BIRTH_DATE = "birth_date";
    public static final String SERVER_SOCIAL_STATUS = "relationship_status";
    public static final String SERVER_CITY = "city";
    public static final String SERVER_COUNTRY = "country";
    public static final String SERVER_MATCH_GENDER = "match_sex";
    public static final String SERVER_INTERESTED_PURPOSE = "relationship";
    public static final String SERVER_HEIGHT = "height";
    public static final String SERVER_BODY = "body";
    public static final String SERVER_EYES = "eyes";
    public static final String SERVER_WORK = "work";
    public static final String SERVER_GENDER_MALE = "1";
    public static final String SERVER_GENDER_FEMALE = "2";
    public static final String SERVER_GENDER_BOTH = "3";
    public static final String SERVER_CHAT = "chat";
    public static final String SERVER_FRIENDSHIP = "friendship";
    public static final String SERVER_LOVE = "love";
    public static final String SERVER_DATE = "dating";
    public static final String SERVER_MARRIAGE = "marriage";
    public static final String SERVER_EDUCATION = "education";
    public static final String SERVER_RELIGION = "religion";
    public static final String SERVER_ALCOHOL = "alcohol";
    public static final String SERVER_SMOKING = "smoking";
    public static final String SERVER_DESCRIPTION = "description";
    public static final String SERVER_ERROR_MSG = "error_msg";
    public static final String SERVER_DAY = "day";
    public static final String SERVER_MONTH = "month";
    public static final String SERVER_YEAR = "year";
    public static final String SERVER_PICTURE_ID = "id";
    public static final String SERVER_PICTURE_PATH = "image";
    public static final String SERVER_THUMBNAIL = "thumbnail";
    public static final String SERVER_PICTURE_CREATED_DATE = "create_date";
    public static final String SERVER_IS_PROFILE_PICTURE = "main";
    public static final String SERVER_PROFILE_PICTURE_PATH = "thumbnail";
    public static final String SERVER_ONLINE_STATUS = "online_status";
    public static final String SERVER_MAIN_PHOTO = "main_photo";
    public static final String SERVER_FRIEND_STATUS = "friend_status";
    public static final String SERVER_FRIENDSHIP_FRIEND = "friend";
    public static final String SERVER_FRIENDSHIP_SENT = "sent";
    public static final String SERVER_FRIENDSHIP_RECEIVED = "received";
    public static final String SERVER_FRIENDSHIP_NONE = "none";
    public static final String SERVER_PAGE = "page";
    public static final String SERVER_RESULT_PER_PAGE = "results_per_page";

    public static final String SERVER_STATUS_AVAILABLE = "available";
    public static final String SERVER_STATUS_OFFLINE = "offline";
    public static final String SERVER_STATUS_AWAY = "away";
    public static final String SERVER_STATUS_BUSY = "busy";
    public static final String SERVER_LAST = "last";
    public static final String PROFILE_PICTURE_BITMAP = "ProfilePicture Bitmap";
    public static final String SERVER_NEW_PASSWORD = "new_password";

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
