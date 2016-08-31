package com.al70b.core.server_methods;

/**
 * Created by Naseem on 5/5/2015.
 */
public final class ServerConstants {

    public static final class FUNCTIONS {

        // functions api on server side
        public static final String SERVER_FUNC_AUTH_USER = "authUser";
        public static final String SERVER_FUNC_REGISTER_USER = "registerUser";
        public static final String SERVER_FUNC_GET_USERS_STATIC = "getUsersStatic";
        public static final String SERVER_FUNC_GET_USER_DATA = "getUserData";
        public static final String SERVER_FUNC_GET_CONVERSATIONS = "getConversations";
        public static final String SERVER_FUNC_GET_MEMBERS = "getUsers";
        public static final String SERVER_FUNC_SEND_APPROVE_FRIEND_REQUEST = "sendApproveFriendRequestById";
        public static final String SERVER_FUNC_GET_USER_PROFILE = "getUserProfile";
        public static final String SERVER_FUNC_SEND_REMOVE_FRIEND_REQUEST = "removeFriendById";
        public static final String SERVER_FUNC_UPDATE_USER_DATA_BASIC = "updateUserDataBasic";
        public static final String SERVER_FUNC_UPDATE_USER_DATA_ADVANCED = "updateUserDataAdvanced";
        public static final String SERVER_FUNC_UPDATE_PASSWORD = "updatePassword";
        public static final String SERVER_FUNC_UPDATE_EMAIL = "updateEmail";
        public static final String SERVER_FUNC_LOAD_MORE_MESSAGES = "getMessagesByUserId";
        public static final String SERVER_FUNC_GET_USER_STAT = "getUserStats";
        public static final String SERVER_FUNC_MARK_MESSAGE_AS_READ = "markMessageAsReadBySenderId";
        public static final String SERVER_FUNC_GET_PENDING_RECEIVED_FRIEND_REQUESTS = "getUserPendingReceivedFriendRequests";
        public static final String SERVER_FUNC_GET_PENDING_SENT_FRIEND_REQUESTS = "getUserPendingSentFriendRequests";
        public static final String SERVER_FUNC_GET_ONLINE_FRIENDS = "getOnlineFriends";
        public static final String SERVER_FUNC_GET_USERS_ADVANCED = "getUsersAdvanced";
        public static final String SERVER_FUNC_GET_MATCHING_PROFILE = "getMatchingProfile";
        public static final String SERVER_FUNC_UPDATE_MATCHING_PROFILE = "updateMatchingProfile";
        public static final String SERVER_FUNC_UPLOAD_IMAGE = "uploadPhoto";
        public static final String SERVER_FUNC_SET_MAIN_PHOTO = "setMainPhoto";
        public static final String SERVER_FUNC_REPORT_USER = "reportUser";
        public static final String SERVER_FUNC_DELETE_PHOTO = "deletePhoto";
        public static final String SERVER_FUNC_FORGOT_PASSWORD = "forgotPassword";
        public static final String SERVER_FUNC_GET_MATCHING_PROFILES = "getMatchingProfiles";
        public static final String SERVER_FUNC_REGISTER_CLIEND_ID = "registerClientId";
        public static final String SERVER_FUNC_UNREGISTER_CLIEND_ID = "unregisterClientId";
    }

    public static final class CONSTANTS {

        public static final String PUBLIC_KEY = "h%zNotxt1";
        public static final String TOKEN_KEY = "fp&pPrxw5";
        public static final String METHOD_POST = "POST";
        public static final String METHOD_GET = "GET";

        public static final String PICTURES_PATH = "data/uploads/";
        public static final String THUMBNAILS_PATH = "data/uploads/thumbnail/";
        public static final String SERVER_DATA_PATH = "data/";
        public static final String SERVER_DATA_THUMBNAILS_PATH = SERVER_DATA_PATH + "uploads/thumbnail/";
        public static final String SERVER_USER_DEFAULT_PHOTO = "avatar";
        public static final String SERVER_USER_DEFAULT_PHOTO_URL = "img/avatar.png";
        public static final String SERVER_TERMS_PAGE = "app-content/terms.php";
        //public static final String COMET_CHAT_API_KEY = "a69d2122ce0bade61cff62b03fd744a5";
        public static final String COMET_CHAT_API_KEY = "";

        // declare server finals
        protected static final String CONNECTION_PROTOCOL = "http://";
        protected static final String SERVER_NAME = "al70b.com/";
        protected static final String APPLICATION_PATH = "";
        public static final String CHAT_URL = CONNECTION_PROTOCOL + SERVER_NAME + APPLICATION_PATH + "cometchat/";
        public static final String SERVER_FULL_URL = CONNECTION_PROTOCOL + SERVER_NAME + APPLICATION_PATH;
        public static final String SERVER_PICTURES_FULL_URL = SERVER_FULL_URL + PICTURES_PATH;
        public static final String SERVER_THUMBNAILS_FULL_URL = SERVER_FULL_URL + THUMBNAILS_PATH;
        public static final String SERVER_TERMS_FULL_URL = SERVER_FULL_URL + SERVER_TERMS_PAGE;
        protected static final String SERVER_APP_FILE_PATH = "app-api/";
        public static final String SERVER_REQUESTS_URL = SERVER_FULL_URL + SERVER_APP_FILE_PATH;
    }


}
