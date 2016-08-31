package com.al70b.core.server_methods;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.al70b.R;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.fragments.Items.ConversationItem;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.KEYS;
import com.al70b.core.misc.StorageOperations;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.Characteristics;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.Picture;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.UserServerRequestsCallable.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Naseem on 5/9/2015.
 */
public class RequestsInterface {

    ExecutorService executor;
    private Context context;

    public RequestsInterface(Context context) {
        executor = Executors.newSingleThreadExecutor();
        this.context = context.getApplicationContext();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private <T> ServerResponse<T> doTheWork(Method method, JSONObject jsonArgs, ParseResultInterface<T> p) throws JSONException {
        return doTheWork(method, null, jsonArgs, p);
    }

    private <T> ServerResponse<T> doTheWork(Method method, String methodName, JSONObject jsonArgs, ParseResultInterface<T> p) throws JSONException {
        Callable<JSONObject> callable;
        Future<JSONObject> future;
        JSONObject jsonResult;
        ServerResponse<T> sr = null;
        T result = null;

        if (!isNetworkAvailable())
            return new ServerResponse<>(false, context.getString(R.string.error_server_connection_falied), null);

        try {
            // call the real server function
            if (method == null)
                callable = new UserServerTranslationCallable();
            else
                callable = new UserServerRequestsCallable(method, methodName, jsonArgs);
            future = executor.submit(callable);
            jsonResult = future.get();

            boolean success = false;
            String errMsg = null;

            if (jsonResult != null) {

                if (jsonResult.has("TIMEOUT"))
                    return new ServerResponse<>(false, context.getString(R.string.error_no_response_time_out), null);

                if (method == null) {
                    // translation request
                    success = true;

                    // parse result
                    result = p.parseResult(jsonResult);
                } else {
                    // get the result's status
                    success = jsonResult.getString(KEYS.SERVER.RESULT).compareTo(KEYS.SERVER.SUCCESS) == 0;

                    if (success) {
                        Object object = jsonResult.get(KEYS.SERVER.DATA);

                        if (object instanceof Bitmap || object instanceof Boolean) {
                            // if this is Bitmap object just return it as a result
                            result = (T) object;
                        } else if (object instanceof JSONArray) {
                            object = jsonResult.getJSONArray(KEYS.SERVER.DATA);
                            result = p.parseResult((JSONArray) object);
                        } else if (object instanceof JSONObject) {
                            // json object for sure
                            object = jsonResult.getJSONObject(KEYS.SERVER.DATA);
                            result = p.parseResult((JSONObject) object);
                        }
                    } else {
                        if (method == Method.GET_PROFILE_PICTURE)
                            errMsg = context.getResources().getString(R.string.error_downloading_profile_picture);
                        else
                            errMsg = jsonResult.getString(KEYS.SERVER.ERROR_MSG);
                    }
                }
            } else {
                errMsg = KEYS.SHARED_PREFERENCES.CONNECTION_FAILED;
            }

            sr = new ServerResponse<T>(success, errMsg, result);
        } catch (InterruptedException ex) {

        } catch (ExecutionException ex) {

        }

        return sr;
    }


    public ServerResponse<List<String>> getUsersStatic(int numberOfPages) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<List<String>> sr = null;
        try {

            jsonArgs.put(KEYS.SERVER.MATCH_GENDER, 3);
            jsonArgs.put("age_from", 18);
            jsonArgs.put("age_to", 100);
            jsonArgs.put("image_flag", true);
            jsonArgs.put("online_flag", false);
            jsonArgs.put("friends_flag", false);
            jsonArgs.put(KEYS.SERVER.PAGE, 1);
            jsonArgs.put(KEYS.SERVER.RESULT_PER_PAGE, numberOfPages);

            sr = doTheWork(Method.GET_USERS_PICTURES, jsonArgs, new ParseResultInterface<List<String>>() {
                @Override
                public List<String> parseResult(JSONObject jsonResult) throws JSONException {
                    List<String> listOfPictures = new ArrayList<String>();

                    Iterator keys = jsonResult.keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();

                        if (key.compareTo("last") == 0)
                            continue;

                        JSONObject temp = jsonResult.getJSONObject(key);
                        String path = temp.getString("main_photo");
                        path = ServerConstants.CONSTANTS.SERVER_FULL_URL + path;
                        listOfPictures.add(path);
                    }

                    return listOfPictures;
                }

                @Override
                public List<String> parseResult(JSONArray jsonResult) throws JSONException {
                    return new ArrayList<String>();
                }
            });
        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<CurrentUser> authUser(final String email, final String password) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<CurrentUser> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USERNAME, email);
            jsonArgs.put(KEYS.SERVER.PASSWORD, password);

            sr = doTheWork(Method.LOGIN, jsonArgs, new ParseResultInterface<CurrentUser>() {
                @Override
                public CurrentUser parseResult(JSONObject jsonResult) throws JSONException {
                    int userID = jsonResult.getInt(KEYS.SERVER.USER_ID);
                    String accessToken = jsonResult.getString(KEYS.SERVER.ACCESS_TOKEN);

                    // create new user with previous values
                    return new CurrentUser(context, userID, accessToken, email, password);
                }

                @Override
                public CurrentUser parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });
        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }


    public ServerResponse<CurrentUser> getUserData(final CurrentUser user) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<CurrentUser> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_USER_DATA, jsonArgs, new ParseResultInterface<CurrentUser>() {
                @Override
                public CurrentUser parseResult(JSONObject jsonResult) throws JSONException {
                    return user.parseJSONToUser(jsonResult);
                }

                @Override
                public CurrentUser parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<OtherUser> getOtherUserData(int userID, String accessToken, final OtherUser otherUser) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<OtherUser> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, userID);
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, accessToken);
            jsonArgs.put(KEYS.SERVER.ID, otherUser.getUserID());

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_USER_PROFILE, jsonArgs, new ParseResultInterface<OtherUser>() {
                @Override
                public OtherUser parseResult(JSONObject jsonResult) throws JSONException {
                    return otherUser.parseJSONToUser(jsonResult);
                }

                @Override
                public OtherUser parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Bitmap> getThumbnail(String thumbnail) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Bitmap> sr = null;
        try {
            jsonArgs.put(KEYS.SHARED_PREFERENCES.PROFILE_PICTURE, thumbnail);

            sr = doTheWork(Method.GET_PROFILE_PICTURE, jsonArgs, new ParseResultInterface<Bitmap>() {
                @Override
                public Bitmap parseResult(JSONObject jsonResult) throws JSONException {
                    return (Bitmap) jsonResult.get(KEYS.SERVER.THUMBNAIL);
                }

                @Override
                public Bitmap parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();
        return sr;
    }


    /**
     * @return object holding translations of specific values
     */
    public ServerResponse<JSONObject> getTranslations() {
        ServerResponse<JSONObject> sr = null;

        try {
            sr = doTheWork(null, null, new ParseResultInterface<JSONObject>() {
                @Override
                public JSONObject parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult;
                }

                @Override
                public JSONObject parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });
        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        // if(sr == null)
        //  throw new ServerResponseFailedException();

        return sr;
    }


    public ServerResponse<String> registerUser(CurrentUser user) throws ServerResponseFailedException{
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        Translator translator = Translator.getInstance(context);

        ServerResponse<String> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.GENDER, user.getGender().getValue());
            jsonArgs.put(KEYS.SERVER.MATCH_GENDER, user.getUserInterest().getGenderInterest().getValue());
            jsonArgs.put(KEYS.SERVER.NAME, user.getName());
            JSONArray jsonArray = new JSONArray();

            jsonArgs.put(KEYS.SERVER.INTERESTED_PURPOSE, jsonArray);
            jsonArgs.put(KEYS.SERVER.COUNTRY, translator.translate(user.getAddress().getCountry(), translator.getDictionary().COUNTRIES));
            jsonArgs.put(KEYS.SERVER.CITY, user.getAddress().getCity());
            jsonArgs.put(KEYS.SERVER.DAY, user.getDateOfBirth().get(Calendar.DAY_OF_MONTH));
            jsonArgs.put(KEYS.SERVER.MONTH, user.getDateOfBirth().get(Calendar.MONTH) + 1);
            jsonArgs.put(KEYS.SERVER.YEAR, user.getDateOfBirth().get(Calendar.YEAR));
            jsonArgs.put(KEYS.SERVER.EMAIL, user.getEmail());
            jsonArgs.put(KEYS.SERVER.PASSWORD, user.getPassword());
            jsonArgs.put("advertisements_flag", user.isAcceptAdvertisement());

            sr = doTheWork(Method.REGISTER, jsonArgs, new ParseResultInterface<String>() {
                @Override
                public String parseResult(JSONObject jsonResult) throws JSONException {
                    return null;
                }

                @Override
                public String parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if(sr == null) {
            throw new ServerResponseFailedException();
        }
        return sr;
    }

        // turn arguments to JSONObject of arguments
    public ServerResponse<ConversationItem[]> getConversations(final CurrentUser user, int page, int resultsPerPage) throws ServerResponseFailedException {
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<ConversationItem[]> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.PAGE, page);
            jsonArgs.put(KEYS.SERVER.RESULT_PER_PAGE, resultsPerPage);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_CONVERSATIONS, jsonArgs, new ParseResultInterface<ConversationItem[]>() {
                @Override
                public ConversationItem[] parseResult(JSONObject jsonResult) throws JSONException {
                    return new ConversationItem[0];
                }

                @Override
                public ConversationItem[] parseResult(JSONArray jsonResult) throws JSONException {
                    ConversationItem[] arr = new ConversationItem[jsonResult.length()];

                    JSONObject jsonTemp;
                    int otherUserID, conversationID;
                    String otherUserName, msg, timeSent;
                    String thumbnail;
                    final List<String> profilePictures = new ArrayList<String>();

                    for (int i = 0; i < jsonResult.length(); i++) {
                        jsonTemp = jsonResult.getJSONObject(i);

                        // parse json object
                        conversationID = jsonTemp.getInt("id");
                        otherUserID = jsonTemp.getInt("from");
                        otherUserName = jsonTemp.getString("name");
                        thumbnail = ServerConstants.CONSTANTS.SERVER_FULL_URL + jsonTemp.getString("photo");
                        profilePictures.add(thumbnail);

                        if (otherUserID == user.getUserID())   // if "this" user is the sender
                            otherUserID = jsonTemp.getInt("to");
                        msg = jsonTemp.getString("message");
                        timeSent = jsonTemp.getString("sent");


                        arr[i] = new ConversationItem(conversationID, otherUserID, otherUserName, msg, timeSent, thumbnail);
                    }
                    return arr;
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        } catch (Exception ex) {
            Log.d("Execution - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();
        return sr;
    }

    public List<Bitmap> getProfilePictures(List<String> thumbnailsList, boolean saveToStorage) {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();
        boolean defaultPhoto, alreadyDownloaded = false;
        ServerResponse<Bitmap> sr;
        Map<String, Bitmap> listOfBitmaps = new HashMap<>();

        try {
            for (String thumbnail : thumbnailsList) {

                if (thumbnail.compareTo(ServerConstants.CONSTANTS.SERVER_USER_DEFAULT_PHOTO) == 0) {
                    defaultPhoto = true;
                    if (alreadyDownloaded)
                        continue;
                } else {
                    defaultPhoto = false;
                }

                // get thumbnail
                jsonArgs.put(KEYS.SHARED_PREFERENCES.PROFILE_PICTURE, thumbnail);

                sr = doTheWork(Method.GET_PROFILE_PICTURE, jsonArgs, new ParseResultInterface<Bitmap>() {
                    @Override
                    public Bitmap parseResult(JSONObject jsonResult) throws JSONException {
                        return (Bitmap) jsonResult.get(KEYS.SERVER.THUMBNAIL);
                    }

                    @Override
                    public Bitmap parseResult(JSONArray jsonResult) throws JSONException {
                        return null;
                    }
                });

                if (sr != null && sr.isSuccess()) {
                    if (defaultPhoto)        // if this is the default photo
                        alreadyDownloaded = true;   // don't download any further versions of it
                    listOfBitmaps.put(thumbnail, sr.getResult());
                }

                if (saveToStorage) {
                    Log.d("thrd-pics", "done downloading pictures");

                    StorageOperations so = new StorageOperations(context);
                    Iterator iterator = listOfBitmaps.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry<String, Bitmap> e = (Map.Entry<String, Bitmap>) iterator.next();
                        so.saveImageToInternalStorage(e.getKey(), e.getValue());
                    }

                    Log.d("thrd-pics", "done saving them");
                }

            }
        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        List<Bitmap> list = new ArrayList<Bitmap>();
        list.addAll(listOfBitmaps.values());
        return list;
    }

    /*public void downloadRandomUsersPhotos(final List<String> thumbnailsList) {
        // turn arguments to JSONObject of arguments


        for (final String thumbnail : thumbnailsList) {
            // start a new thread to handle this picture
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        JSONObject jsonArgs = new JSONObject();
                        jsonArgs.put(KEYS.SHARED_PREFERENCES.PROFILE_PICTURE, thumbnail);
                        ServerResponse<Bitmap> sr = doTheWork(Method.GET_PROFILE_PICTURE, jsonArgs, new ParseResultInterface<Bitmap>() {
                            @Override
                            public Bitmap parseResult(JSONObject jsonResult) throws JSONException {
                                return (Bitmap) jsonResult.get(KEYS.SERVER.THUMBNAIL);
                            }

                            @Override
                            public Bitmap parseResult(JSONArray jsonResult) throws JSONException {
                                return null;
                            }
                        });

                        if (sr != null && sr.isSuccess()) {
                            GuestWelcomeFragment.addImageViewToLayout(sr.getResult());
                        }
                    } catch (JSONException ex) {
                        Log.d("JSON - Requests", ex.toString());
                    }
                }
            }).start();
        }
    }*/


    public List<Bitmap> getUserThumbnails(List<Picture> listOfPictures, int row, int numInRow) {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Bitmap> sr;
        List<Bitmap> listOfBitmaps = new ArrayList<>();

        try {
            for (int i = 0; i < listOfPictures.size(); i++) {

                Picture pic = listOfPictures.get(i);
                String thumbnail = pic.getThumbnailName();

                // get thumbnail
                jsonArgs.put(KEYS.SHARED_PREFERENCES.PROFILE_PICTURE, thumbnail);

                sr = doTheWork(Method.GET_PROFILE_PICTURE, jsonArgs, new ParseResultInterface<Bitmap>() {
                    @Override
                    public Bitmap parseResult(JSONObject jsonResult) throws JSONException {
                        return (Bitmap) jsonResult.get(KEYS.SERVER.THUMBNAIL);
                    }

                    @Override
                    public Bitmap parseResult(JSONArray jsonResult) throws JSONException {
                        return null;
                    }

                });

                if (sr != null && sr.isSuccess())
                    listOfBitmaps.add(sr.getResult());
            }

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        return listOfBitmaps;
    }

    public ServerResponse<Pair<Boolean, List<OtherUser>>> getUsers(int userID, String accessToken, int gender, int from, int to, boolean withPictures,
                                                                   boolean onlineOnly, boolean friendsOnly, int page, int resultPerPage,
                                                                   final ResponseCallback<Object> responseCallback) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Pair<Boolean, List<OtherUser>>> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, userID);
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, accessToken);
            jsonArgs.put(KEYS.SERVER.MATCH_GENDER, gender);
            jsonArgs.put("age_from", from);
            jsonArgs.put("age_to", to);
            jsonArgs.put("image_flag", withPictures);
            jsonArgs.put("online_flag", onlineOnly);
            jsonArgs.put("friends_flag", friendsOnly);
            jsonArgs.put(KEYS.SERVER.PAGE, page);
            jsonArgs.put(KEYS.SERVER.RESULT_PER_PAGE, resultPerPage);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_MEMBERS, jsonArgs, new ParseResultInterface<Pair<Boolean, List<OtherUser>>>() {
                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONObject jsonResult) throws JSONException {
                    List<OtherUser> users = new ArrayList<>();

                    boolean last = jsonResult.getBoolean(KEYS.SERVER.LAST);
                    jsonResult.remove(KEYS.SERVER.LAST);
                    Iterator<String> iterator = jsonResult.keys();

                    while (iterator.hasNext()) {
                        JSONObject jsonUser = jsonResult.getJSONObject(iterator.next());
                        OtherUser user = new OtherUser(context).basicParseJSONToUser(jsonUser, responseCallback);
                        users.add(user);
                    }

                    return new Pair(last, users);
                }

                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONArray jsonResult) throws JSONException {
                    if (jsonResult == null || jsonResult.length() == 0)
                        return new Pair(true, new ArrayList<OtherUser>());
                    else
                        return parseResult(jsonResult.toJSONObject(null));
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }


    /**
     * Advanced Users Search
     */
    public ServerResponse<Pair<Boolean, List<OtherUser>>> getUsersAdvanced(CurrentUser user,
                                                                           List<Integer> matchGender, Integer ageFrom, Integer ageTo,
                                                                           String country,
                                                                           Integer heightFrom, Integer heightTo,
                                                                           List<String> education, List<String> religion, List<String> alcohol,
                                                                           List<String> smoking,
                                                                           boolean picturesOnly, boolean onlineOnly,
                                                                           int page, int resultPerPage,
                                                                           final ResponseCallback<Object> responseCallback) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Pair<Boolean, List<OtherUser>>> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.MATCH_GENDER, new JSONArray(matchGender));
            jsonArgs.put("age_from", ageFrom);
            jsonArgs.put("age_to", ageTo);
            jsonArgs.put("country", country);
            jsonArgs.put("height_from", heightFrom);
            jsonArgs.put("height_to", heightTo);
            jsonArgs.put("education", new JSONArray(education));
            jsonArgs.put("religion", new JSONArray(religion));
            jsonArgs.put("alcohol", new JSONArray(alcohol));
            jsonArgs.put("smoking", new JSONArray(smoking));
            // TODO: verify the required keys in API for these two values
            jsonArgs.put("image_flag", picturesOnly);
            jsonArgs.put("online_flag", onlineOnly);
            jsonArgs.put(KEYS.SERVER.PAGE, page);
            jsonArgs.put(KEYS.SERVER.RESULT_PER_PAGE, resultPerPage);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_USERS_ADVANCED, jsonArgs,
                    new ParseResultInterface<Pair<Boolean, List<OtherUser>>>() {
                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONObject jsonResult) throws JSONException {
                    List<OtherUser> users = new ArrayList<>();

                    boolean last = jsonResult.getBoolean(KEYS.SERVER.LAST);
                    jsonResult.remove(KEYS.SERVER.LAST);
                    Iterator<String> iterator = jsonResult.keys();

                    while (iterator.hasNext()) {
                        JSONObject jsonUser = jsonResult.getJSONObject(iterator.next());
                        OtherUser user = new OtherUser(context).basicParseJSONToUser(jsonUser, responseCallback);
                        users.add(user);
                    }

                    return new Pair(last, users);
                }

                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONArray jsonResult) throws JSONException {
                    if (jsonResult == null || jsonResult.length() == 0)
                        return new Pair(true, new ArrayList<OtherUser>());
                    else
                        return parseResult(jsonResult.toJSONObject(null));
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Integer> sendApproveFriendRequest(
            int userID, String accessToken, int friendID) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Integer> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, userID);
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, accessToken);
            jsonArgs.put(KEYS.SERVER.ID, friendID);

            sr = doTheWork(Method.REGULAR,
                    ServerConstants.FUNCTIONS.SERVER_FUNC_SEND_APPROVE_FRIEND_REQUEST,
                    jsonArgs, new ParseResultInterface<Integer>() {
                @Override
                public Integer parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult.getInt(KEYS.SERVER.RESULT);
                }

                @Override
                public Integer parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Integer> removeFriendRequest(int userID, String accessToken, int friendID) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Integer> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, userID);
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, accessToken);
            jsonArgs.put(KEYS.SERVER.ID, friendID);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_SEND_REMOVE_FRIEND_REQUEST, jsonArgs, new ParseResultInterface<Integer>() {
                @Override
                public Integer parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult.getInt(KEYS.SERVER.RESULT);
                }

                @Override
                public Integer parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Boolean> updateUserDataBasic(final CurrentUser user) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        Translator translator = Translator.getInstance(context);

        ServerResponse<Boolean> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.NAME, user.getName());
            jsonArgs.put(KEYS.SERVER.COUNTRY, translator.translate(user.getAddress().getCountry(), translator.getDictionary().COUNTRIES));
            jsonArgs.put(KEYS.SERVER.CITY, user.getAddress().getCity());

            jsonArgs.put(KEYS.SERVER.MATCH_GENDER, user.getUserInterest().getGenderInterest().getValue());
            JSONArray jsonArray = new JSONArray();
            jsonArgs.put(KEYS.SERVER.INTERESTED_PURPOSE, jsonArray);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_UPDATE_USER_DATA_BASIC, jsonArgs, new ParseResultInterface<Boolean>() {
                @Override
                public Boolean parseResult(JSONObject jsonResult) throws JSONException {
                    return true;//jsonResult.getInt(KEYS.RESULT);
                }

                @Override
                public Boolean parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Boolean> updateUserDataAdvanced(final CurrentUser user) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        Translator translator = Translator.getInstance(context);

        Characteristics ch = user.getUserChar();

        ServerResponse<Boolean> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.HEIGHT, ch.attributeNotSet(ch.getHeight()) ? null : ch.getHeight());
            jsonArgs.put(KEYS.SERVER.WORK, ch.attributeNotSet(ch.getWork()) ? null : ch.getWork());
            jsonArgs.put(KEYS.SERVER.EDUCATION, translator.translate(ch.attributeNotSet(ch.getEducation()) ? null : ch.getEducation(), translator.getDictionary().CHARACTERS.get(KEYS.SERVER.EDUCATION)));
            jsonArgs.put(KEYS.SERVER.RELIGION, translator.translate(ch.attributeNotSet(ch.getReligion()) ? null : ch.getReligion(), translator.getDictionary().CHARACTERS.get(KEYS.SERVER.RELIGION)));
            jsonArgs.put(KEYS.SERVER.ALCOHOL, translator.translate(ch.attributeNotSet(ch.getAlcohol()) ? null : ch.getAlcohol(), translator.getDictionary().CHARACTERS.get(KEYS.SERVER.ALCOHOL)));
            jsonArgs.put(KEYS.SERVER.SMOKING, translator.translate(ch.attributeNotSet(ch.getSmoking()) ? null : ch.getSmoking(), translator.getDictionary().CHARACTERS.get(KEYS.SERVER.SMOKING)));
            jsonArgs.put(KEYS.SERVER.DESCRIPTION, ch.attributeNotSet(ch.getDescription()) ? null : ch.getDescription());

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_UPDATE_USER_DATA_ADVANCED, jsonArgs, new ParseResultInterface<Boolean>() {
                @Override
                public Boolean parseResult(JSONObject jsonResult) throws JSONException {
                    return true;//jsonResult.getInt(KEYS.RESULT);
                }

                @Override
                public Boolean parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<String> updateEmail(final CurrentUser user, String email, String password) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<String> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.EMAIL, email);
            jsonArgs.put(KEYS.SERVER.PASSWORD, password);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_UPDATE_EMAIL, jsonArgs, new ParseResultInterface<String>() {
                @Override
                public String parseResult(JSONObject jsonResult) throws JSONException {
                    return null;//jsonResult.getInt(KEYS.RESULT);
                }

                @Override
                public String parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<String> updatePassword(final CurrentUser user, String newPassword, String password) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<String> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.NEW_PASSWORD, newPassword);
            jsonArgs.put(KEYS.SERVER.NEW_PASSWORD + "_2", newPassword);
            jsonArgs.put(KEYS.SERVER.PASSWORD, password);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_UPDATE_PASSWORD
                    , jsonArgs, new ParseResultInterface<String>() {
                @Override
                public String parseResult(JSONObject jsonResult) throws JSONException {
                    return null;//jsonResult.getInt(KEYS.RESULT);
                }

                @Override
                public String parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Pair<Integer, Integer>> getUserStats(CurrentUser user) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Pair<Integer, Integer>> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_USER_STAT
                    , jsonArgs, new ParseResultInterface<Pair<Integer, Integer>>() {
                @Override
                public Pair<Integer, Integer> parseResult(JSONObject jsonResult) throws JSONException {

                    int friendsRequest = jsonResult.optInt("friend_requests", 0);
                    int unreadMessages = jsonResult.optInt("unread_messages", 0);

                    return new Pair<>(friendsRequest, unreadMessages);
                }

                @Override
                public Pair<Integer, Integer> parseResult(JSONArray jsonResult) throws JSONException {
                    return new Pair<>(-1, -1);
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<String> markMessageAsRead(CurrentUser user, long otherUserId) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<String> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.ID, otherUserId);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_MARK_MESSAGE_AS_READ
                    , jsonArgs, new ParseResultInterface<String>() {
                @Override
                public String parseResult(JSONObject jsonResult) throws JSONException {

                    int friendsRequest = jsonResult.optInt("friend_requests", -1);
                    int unreadMessages = jsonResult.optInt("unread_messages", -1);

                    return "";
                }

                @Override
                public String parseResult(JSONArray jsonResult) throws JSONException {
                    return "";
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Pair<Boolean, List<OtherUser>>> getUserPendingReceivedRequests(CurrentUser user,
                                                                                         int page, int resultsPerPage,
                                                                                         final ResponseCallback<Object> responseCallback) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Pair<Boolean, List<OtherUser>>> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.PAGE, page);
            jsonArgs.put(KEYS.SERVER.RESULT_PER_PAGE, resultsPerPage);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_PENDING_RECEIVED_FRIEND_REQUESTS, jsonArgs, new ParseResultInterface<Pair<Boolean, List<OtherUser>>>() {
                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONObject jsonResult) throws JSONException {

                    boolean last = jsonResult.getBoolean(KEYS.SERVER.LAST);
                    jsonResult.remove(KEYS.SERVER.LAST);
                    List<OtherUser> users = new ArrayList<>();

                    Iterator<String> iterator = jsonResult.keys();

                    while (iterator.hasNext()) {
                        JSONObject jsonUser = jsonResult.getJSONObject(iterator.next());
                        OtherUser user = new OtherUser(context).basicParseJSONToUser(jsonUser, responseCallback);
                        user.getFriendStatus().setValue(OtherUser.FriendStatus.RECEIVED_REQUEST_PENDING);
                        users.add(user);
                    }

                    return new Pair<Boolean, List<OtherUser>>(last, users);
                }

                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONArray jsonResult) throws JSONException {
                    if (jsonResult == null || jsonResult.length() == 0)
                        return new Pair<Boolean, List<OtherUser>>(true, new ArrayList<OtherUser>());
                    else
                        return parseResult(jsonResult.toJSONObject(null));
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }
    public ServerResponse<Pair<Boolean, List<OtherUser>>> getUserPendingSentRequests(CurrentUser user,
                                                                                         int page, int resultsPerPage,
                                                                                         final ResponseCallback<Object> responseCallback) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Pair<Boolean, List<OtherUser>>> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.PAGE, page);
            jsonArgs.put(KEYS.SERVER.RESULT_PER_PAGE, resultsPerPage);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_PENDING_SENT_FRIEND_REQUESTS, jsonArgs, new ParseResultInterface<Pair<Boolean, List<OtherUser>>>() {
                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONObject jsonResult) throws JSONException {

                    boolean last = jsonResult.getBoolean(KEYS.SERVER.LAST);
                    jsonResult.remove(KEYS.SERVER.LAST);
                    List<OtherUser> users = new ArrayList<>();

                    Iterator<String> iterator = jsonResult.keys();

                    while (iterator.hasNext()) {
                        JSONObject jsonUser = jsonResult.getJSONObject(iterator.next());
                        OtherUser user = new OtherUser(context).basicParseJSONToUser(jsonUser, responseCallback);
                        user.getFriendStatus().setValue(OtherUser.FriendStatus.SENT_REQUEST_PENDING);
                        users.add(user);
                    }

                    return new Pair<Boolean, List<OtherUser>>(last, users);
                }

                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONArray jsonResult) throws JSONException {
                    if (jsonResult == null || jsonResult.length() == 0)
                        return new Pair<Boolean, List<OtherUser>>(true, new ArrayList<OtherUser>());
                    else
                        return parseResult(jsonResult.toJSONObject(null));
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<JSONObject> getMatchingProfile(CurrentUser user) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<JSONObject> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_MATCHING_PROFILE, jsonArgs, new ParseResultInterface<JSONObject>() {
                @Override
                public JSONObject parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult;
                }

                @Override
                public JSONObject parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<JSONObject> updateMatchingProfile(CurrentUser user, JSONObject data) throws ServerResponseFailedException {


        ServerResponse<JSONObject> sr = null;
        try {
            data.put(KEYS.SERVER.USER_ID, user.getUserID());
            data.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_UPDATE_MATCHING_PROFILE, data, new ParseResultInterface<JSONObject>() {
                @Override
                public JSONObject parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult;
                }

                @Override
                public JSONObject parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Picture> uploadImageToServer(CurrentUser user, String encodedImage, String imageName) {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Picture> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put("name", imageName);
            jsonArgs.put("image", encodedImage);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_UPLOAD_IMAGE, jsonArgs, new ParseResultInterface<Picture>() {
                @Override
                public Picture parseResult(JSONObject jsonResult) throws JSONException {

                    String pictureName, thumbnailName;
                    pictureName = jsonResult.getString("image");
                    pictureName = pictureName.substring(pictureName.lastIndexOf('/') + 1);

                    thumbnailName = jsonResult.getString("thumbnail");
                    thumbnailName = thumbnailName.substring(thumbnailName.lastIndexOf('/') + 1);

                    return new Picture(jsonResult.getInt("id"), jsonResult.getInt("user_id"),
                            pictureName, thumbnailName, jsonResult.getString("create_date"), false);
                }

                @Override
                public Picture parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }


        return sr;
    }

    public ServerResponse<Boolean> setProfilePicture(CurrentUser user, long photoID) {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Boolean> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put("photo_id", String.valueOf(photoID));

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_SET_MAIN_PHOTO, jsonArgs, null);

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        return sr;
    }

    public ServerResponse<Boolean> deletePhoto(CurrentUser user, long photoID) {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Boolean> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put("id", String.valueOf(photoID));

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_DELETE_PHOTO, jsonArgs, null);

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        return sr;
    }

    public ServerResponse<JSONObject> reportUser(CurrentUser user, int userID, String reason)
            throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<JSONObject> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put("report_id", String.valueOf(userID));
            jsonArgs.put("reason", reason);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_REPORT_USER, jsonArgs, new ParseResultInterface<JSONObject>() {
                @Override
                public JSONObject parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult;
                }

                @Override
                public JSONObject parseResult(JSONArray jsonResult) throws JSONException {
                    return null;
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<String> forgotPassword(String email) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<String> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.EMAIL, email);

            sr = doTheWork(Method.FORGOT_PASSWORD, ServerConstants.FUNCTIONS.SERVER_FUNC_UPDATE_USER_DATA_ADVANCED, jsonArgs, new ParseResultInterface<String>() {
                @Override
                public String parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult.toString();
                }

                @Override
                public String parseResult(JSONArray jsonResult) throws JSONException {
                    return "";
                }

            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }
        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<Pair<Boolean, List<OtherUser>>> getMatchingProfiles(final CurrentUser user, int page, int resultPerPage,
                                                                              final ResponseCallback<Object> responseCallback) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Pair<Boolean, List<OtherUser>>> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, user.getUserID());
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, user.getAccessToken());
            jsonArgs.put(KEYS.SERVER.PAGE, page);
            jsonArgs.put(KEYS.SERVER.RESULT_PER_PAGE, resultPerPage);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_MATCHING_PROFILES, jsonArgs, new ParseResultInterface<Pair<Boolean, List<OtherUser>>>() {
                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONObject jsonResult) throws JSONException {
                    List<OtherUser> users = new ArrayList<>();

                    boolean last = jsonResult.getBoolean(KEYS.SERVER.LAST);
                    jsonResult.remove(KEYS.SERVER.LAST);
                    Iterator<String> iterator = jsonResult.keys();

                    while (iterator.hasNext()) {
                        JSONObject jsonUser = jsonResult.getJSONObject(iterator.next());
                        OtherUser user = new OtherUser(context).basicParseJSONToUser(jsonUser, responseCallback);
                        users.add(user);
                    }

                    return new Pair(last, users);
                }

                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONArray jsonResult) throws JSONException {
                    if (jsonResult == null || jsonResult.length() == 0)
                        return new Pair(true, new ArrayList<OtherUser>());
                    else
                        return parseResult(jsonResult.toJSONObject(null));
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public boolean sendRegistrationIdToBackend(String regID) {
        int responseCode = -1;                  // -1 means no connection was established
        StringBuilder response = null;          // to receive server's response
        String timestamp;
        String hash, urlParameters;
        JSONObject jsonResult;

        // in case connection failed, result object should be null
        jsonResult = null;

        // declare streams to read from and write to
        DataOutputStream dos;
        BufferedReader in;

        try {
            // create URL object with the appropriate server's requests url
            URL url = new URL("http://10.0.0.1/register.php?regId=" + regID);

            // open a connection to the server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // get server's response
            responseCode = connection.getResponseCode();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            // close connection
            in.close();

            // connection closed and was successful, a JSONObject was received
            jsonResult = new JSONObject(response.toString());

        } catch (MalformedURLException ex) {
            Log.d("MalformedURLException", ex.toString());
        } catch (SocketTimeoutException ex) {
            try {
                jsonResult = new JSONObject();
                jsonResult.put("TIMEOUT", true);
            } catch (JSONException ex2) {

            }
        } catch (IOException ex) {
            Log.d("IOException", ex.toString());
        } catch (Exception ex) {
            Log.d("Exception", ex.toString());
        }

        if (jsonResult != null && response != null)
            Log.d("result", response.toString());
        else
            Log.d("result", "No result");

        Log.d("response code", responseCode + "");
        return true;
    }

    public ServerResponse<Pair<Boolean, List<OtherUser>>> getUserFriends(int userID, String accessToken, int page, int resultPerPage,
                                                                         final ResponseCallback<Object> responseCallback) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<Pair<Boolean, List<OtherUser>>> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, userID);
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, accessToken);
            jsonArgs.put(KEYS.SERVER.MATCH_GENDER, 3);
            jsonArgs.put("age_from", AppConstants.MIN_MEMBER_AGE);
            jsonArgs.put("age_to", AppConstants.MAX_MEMBER_AGE);
            jsonArgs.put("image_flag", false);
            jsonArgs.put("online_flag", false);
            jsonArgs.put("friends_flag", true);
            jsonArgs.put(KEYS.SERVER.PAGE, page);
            jsonArgs.put(KEYS.SERVER.RESULT_PER_PAGE, resultPerPage);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_MEMBERS, jsonArgs, new ParseResultInterface<Pair<Boolean, List<OtherUser>>>() {
                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONObject jsonResult) throws JSONException {
                    List<OtherUser> users = new ArrayList<>();

                    boolean last = jsonResult.getBoolean(KEYS.SERVER.LAST);
                    jsonResult.remove(KEYS.SERVER.LAST);
                    Iterator<String> iterator = jsonResult.keys();

                    while (iterator.hasNext()) {
                        JSONObject jsonUser = jsonResult.getJSONObject(iterator.next());
                        OtherUser user = new OtherUser(context).basicParseJSONToUser(jsonUser, responseCallback);
                        users.add(user);
                    }

                    return new Pair<>(last, users);
                }

                @Override
                public Pair<Boolean, List<OtherUser>> parseResult(JSONArray jsonResult) throws JSONException {
                    if (jsonResult == null || jsonResult.length() == 0)
                        return new Pair<Boolean, List<OtherUser>>(true, new ArrayList<OtherUser>());
                    else
                        return parseResult(jsonResult.toJSONObject(null));
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<String> registerClientID(String userId, String accessToken,
                                                   String registrationID) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<String> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, userId);
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, accessToken);
            jsonArgs.put("registration_id", registrationID);


            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_REGISTER_CLIEND_ID, jsonArgs, new ParseResultInterface<String>() {
                @Override
                public String parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult.toString();
                }

                @Override
                public String parseResult(JSONArray jsonResult) throws JSONException {
                    return jsonResult.toString();
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }

    public ServerResponse<String> unregisterClientID(String userId, String accessToken) throws ServerResponseFailedException {
        // turn arguments to JSONObject of arguments
        JSONObject jsonArgs = new JSONObject();

        ServerResponse<String> sr = null;
        try {
            jsonArgs.put(KEYS.SERVER.USER_ID, userId);
            jsonArgs.put(KEYS.SERVER.ACCESS_TOKEN, accessToken);

            sr = doTheWork(Method.REGULAR, ServerConstants.FUNCTIONS.SERVER_FUNC_UNREGISTER_CLIEND_ID, jsonArgs, new ParseResultInterface<String>() {
                @Override
                public String parseResult(JSONObject jsonResult) throws JSONException {
                    return jsonResult.toString();
                }

                @Override
                public String parseResult(JSONArray jsonResult) throws JSONException {
                    return jsonResult.toString();
                }
            });

        } catch (JSONException ex) {
            Log.d("JSON - Requests", ex.toString());
        }

        // in case server response wasn't parsed appropriately
        if (sr == null)
            throw new ServerResponseFailedException();

        return sr;
    }


    /**
     * each method calling for server needs to implement this callback in order to
     * parse server's json response
     */
    private static interface ParseResultInterface<T> {

        T parseResult(JSONObject jsonResult) throws JSONException;

        T parseResult(JSONArray jsonResult) throws JSONException;

    }

    public static abstract class ResponseCallback<T> {

        // call this function after response received
        public void call(){}

        // call this function after response received
        public T call(T result){return result;}
    }


}
