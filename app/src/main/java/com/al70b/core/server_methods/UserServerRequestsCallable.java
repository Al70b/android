package com.al70b.core.server_methods;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.al70b.core.misc.KEYS;
import com.al70b.core.misc.SHA512;

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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Created by Naseem on 5/4/2015.
 * Each object instantiated by this class represents a job to be executed by a thread.
 * The thread is unusable after his job is executed so is the thread's job. so there is
 * only one way to set the job's method which is via constructor
 */
public class UserServerRequestsCallable implements Callable<JSONObject> {

    // declare finals for user server requests
    private static final String REQUESTS_HANDLER = "request_handler.php";
    private static final String FILE_REQUESTS_URL = ServerConstants.CONSTANTS.SERVER_REQUESTS_URL + REQUESTS_HANDLER;

    // method to run, JSONObject to hold parameters depending on method to run
    private Method methodToRun;
    private JSONObject jsonData;
    private String methodName;

    public UserServerRequestsCallable(Method m, JSONObject jsonData) {
        this(m, null, jsonData);
    }


    public UserServerRequestsCallable(Method m, String methodName, JSONObject jsonData) {
        this.methodToRun = m;
        this.jsonData = jsonData;
        this.methodName = methodName;
    }

    public JSONObject call() throws JSONException {
        switch (methodToRun) {
            case LOGIN:
                return nonAuthenticatedServerMethods(jsonData, ServerConstants.FUNCTIONS.SERVER_FUNC_AUTH_USER);
            case REGISTER:
                return nonAuthenticatedServerMethods(jsonData, ServerConstants.FUNCTIONS.SERVER_FUNC_REGISTER_USER);
            case GET_USERS_PICTURES:
                return nonAuthenticatedServerMethods(jsonData, ServerConstants.FUNCTIONS.SERVER_FUNC_GET_USERS_STATIC);
            case FORGOT_PASSWORD:
                return nonAuthenticatedServerMethods(jsonData, ServerConstants.FUNCTIONS.SERVER_FUNC_FORGOT_PASSWORD);
            case GET_PROFILE_PICTURE:
                return getProfilePicture(jsonData);
            case REGULAR:
                boolean hashData = true;
                if (methodName.compareTo(ServerConstants.FUNCTIONS.SERVER_FUNC_UPLOAD_IMAGE) == 0)
                    hashData = false;    // TODO change to false maybe
                return regularServerMethod(jsonData, methodName, hashData);
            default:
                return null;
        }
    }

    /**
     * @param jsonData
     * @return JSONObject with the encoded result, null if connection failed before getting result
     */
    public JSONObject nonAuthenticatedServerMethods(JSONObject jsonData, String methodName) {
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
            URL url = new URL(FILE_REQUESTS_URL);

            // open a connection to the server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // add request method and properties for the connection
            connection.setRequestMethod(ServerConstants.CONSTANTS.METHOD_POST);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setConnectTimeout(10000);

            /** create the url parameters for post request **/
            // timestamp
            timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

            // create hash
            hash = ServerConstants.CONSTANTS.PUBLIC_KEY
                    .concat(ServerConstants.CONSTANTS.METHOD_POST.toLowerCase()
                            .concat(methodName.
                                    concat(timestamp.
                                            concat(jsonData.toString()))));

            hash = SHA512.hashText(hash);

            if (methodToRun == Method.REGISTER) {
                String temp = jsonData.getString(KEYS.SERVER.NAME);
                jsonData.remove(KEYS.SERVER.NAME);
                jsonData.put(KEYS.SERVER.NAME, String.format("%s", URLEncoder.encode(temp, "UTF-8")));

                temp = jsonData.getString(KEYS.SERVER.CITY);
                jsonData.remove(KEYS.SERVER.CITY);
                jsonData.put(KEYS.SERVER.CITY, String.format("%s", URLEncoder.encode(temp, "UTF-8")));
            }

            urlParameters = "method=" + ServerConstants.CONSTANTS.METHOD_POST.toLowerCase() + "&api_method=" + methodName +
                    "&timestamp=" + timestamp +
                    "&data=" + jsonData.toString() +
                    "&hash=" + hash;

            // TODO hash the password when authUser
            Log.d("parameters", urlParameters);

            // send post request
            connection.setDoOutput(true);
            dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(urlParameters);
            dos.flush();
            dos.close();
            // done sending post request

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
                jsonResult.put("TIMEOUT", true);
            } catch (JSONException ex2) {

            }
        } catch (IOException ex) {
            Log.d("IOException", ex.toString());
        } catch (Exception ex) {
            Log.d("Exception", ex.toString());
        }

        if (jsonResult != null)
            Log.d("result", response.toString());
        else
            Log.d("result", "No result");

        Log.d("response code", responseCode + "");
        return jsonResult;
    }

    public JSONObject regularServerMethod(JSONObject jsonObject, String methodName, boolean hashData) {
        int responseCode = -1;                  // -1 means no connection was established
        StringBuilder response = null;          // to receive server's response
        String timestamp;
        String hash, urlParameters;
        JSONObject jsonResult;
        String userID, accessToken;

        // in case connection failed, result object should be null
        jsonResult = null;

        // declare streams to read from and write to
        DataOutputStream dos;
        BufferedReader in;

        try {
            // create URL object with the appropriate server's requests url
            URL url = new URL(FILE_REQUESTS_URL);

            // open a connection to the server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // add request method and properties for the connection
            connection.setRequestMethod(ServerConstants.CONSTANTS.METHOD_POST);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setConnectTimeout(10000);

            userID = jsonObject.getString(KEYS.SHARED_PREFERENCES.USER_ID);
            jsonObject.remove(KEYS.SHARED_PREFERENCES.USER_ID);
            accessToken = jsonObject.getString(KEYS.SHARED_PREFERENCES.ACCESS_TOKEN);
            jsonObject.remove(KEYS.SHARED_PREFERENCES.ACCESS_TOKEN);

            /** create the url parameters for post request **/

            // timestamp
            timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());


            // create hash
            hash = ServerConstants.CONSTANTS.PUBLIC_KEY
                    .concat((accessToken.concat(ServerConstants.CONSTANTS.TOKEN_KEY))
                            .concat(ServerConstants.CONSTANTS.METHOD_POST.toLowerCase())
                            .concat(userID).concat(methodName)
                            .concat(timestamp));

            // if hash data also
            if (hashData)
                hash = hash.concat(jsonData.toString());

            Log.d("NotHashed", hash);
            hash = SHA512.hashText(hash);

            if (methodName.compareTo(ServerConstants.FUNCTIONS.SERVER_FUNC_UPDATE_USER_DATA_BASIC) == 0) {
                if (jsonData.has(KEYS.SERVER.NAME))
                    jsonData.put(KEYS.SERVER.NAME, String.format("%s", URLEncoder.encode(jsonData.getString(KEYS.SERVER.NAME), "UTF-8")));

                if (jsonData.has(KEYS.SERVER.CITY))
                    jsonData.put(KEYS.SERVER.CITY, String.format("%s", URLEncoder.encode(jsonData.getString(KEYS.SERVER.CITY), "UTF-8")));

            }

            if (methodName.compareTo(ServerConstants.FUNCTIONS.SERVER_FUNC_UPDATE_USER_DATA_ADVANCED) == 0) {
                if (jsonData.has(KEYS.SERVER.WORK))
                    jsonData.put(KEYS.SERVER.WORK, String.format("%s", URLEncoder.encode(jsonData.getString(KEYS.SERVER.WORK), "UTF-8")));

                if (jsonData.has(KEYS.SERVER.DESCRIPTION))
                    jsonData.put(KEYS.SERVER.DESCRIPTION, String.format("%s", URLEncoder.encode(jsonData.getString(KEYS.SERVER.DESCRIPTION), "UTF-8")));
            }

            if (methodName.compareTo(ServerConstants.FUNCTIONS.SERVER_FUNC_REPORT_USER) == 0) {
                if (jsonData.has("reason"))
                    jsonData.put("reason", String.format("%s", URLEncoder.encode(jsonData.getString("reason"), "UTF-8")));
            }

            if (methodName.compareTo(ServerConstants.FUNCTIONS.SERVER_FUNC_SEND_CONTACT_EMAIL) == 0) {
                if (jsonData.has(KEYS.SERVER.CONTENT))
                    jsonData.put(KEYS.SERVER.CONTENT, String.format("%s", URLEncoder.encode(jsonData.getString(KEYS.SERVER.CONTENT), "UTF-8")));
            }

            urlParameters = "method=" + ServerConstants.CONSTANTS.METHOD_POST.toLowerCase() +
                    "&user_id=" + userID +
                    "&api_method=" + methodName +
                    "&timestamp=" + timestamp +
                    "&data=" + jsonData.toString() +
                    "&hash=" + hash;

            Log.d("user-parameters", urlParameters);

            //urlParameters = URLEncoder.encode(urlParameters, "UTF-8");

            Log.d("user-parameters2", urlParameters);

            // send post request
            connection.setDoOutput(true);
            dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(urlParameters);
            dos.flush();
            dos.close();
            // done sending post request

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
                jsonResult.put("TIMEOUT", true);
            } catch (JSONException ex2) {

            }
        } catch (IOException ex) {
            Log.d("IOException", ex.toString());
        } catch (Exception ex) {
            Log.d("Exception", ex.toString());
        }

        if (jsonResult != null)
            Log.d("user-result", response.toString());
        else
            Log.d("user-result", "No result");

        Log.d("user-responseC", responseCode + "");

        return jsonResult;
    }

    private JSONObject getProfilePicture(JSONObject jsonObject) throws JSONException {
        JSONObject jsonResult = new JSONObject();
        String success, errMsg = null;
        Bitmap thumbnail = null;

        try {
            String profilePicture = jsonObject.getString(KEYS.SHARED_PREFERENCES.PROFILE_PICTURE);
            String path;

            if (profilePicture.compareTo(ServerConstants.CONSTANTS.SERVER_USER_DEFAULT_PHOTO) == 0)
                path = ServerConstants.CONSTANTS.SERVER_USER_DEFAULT_PHOTO_URL;
            else
                path = ServerConstants.CONSTANTS.SERVER_DATA_THUMBNAILS_PATH;

            URL url = new URL(ServerConstants.CONSTANTS.SERVER_FULL_URL
                    .concat(path)
                    .concat(jsonObject.getString(KEYS.SHARED_PREFERENCES.PROFILE_PICTURE)));

            thumbnail = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        } catch (IOException ex) {
            errMsg = "IOException: " + ex.toString();
        } finally {
            if (thumbnail != null) {
                // successfully retrieved user's profile picture
                success = KEYS.SERVER.SUCCESS;
                jsonResult.put(KEYS.SERVER.DATA, thumbnail);
            } else {
                success = KEYS.SERVER.FAILURE;
                if (errMsg == null)
                    errMsg = "Could not retrieve user's profile picture";
                jsonResult.put(KEYS.SERVER.ERROR_MSG, errMsg);
            }
            // put the result
            jsonResult.put(KEYS.SERVER.RESULT, success);
        }

        return jsonResult;
    }

    // declare method type for each specific method
    public enum Method {
        LOGIN, REGISTER, GET_USERS_PICTURES, GET_PROFILE_PICTURE, FORGOT_PASSWORD,
        REGULAR
    }
}
