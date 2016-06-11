package com.al70b.core.server_methods;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by Naseem on 5/4/2015.
 * Each object instantiated by this class represents a job to be executed by a thread.
 * The thread is unusable after his job is executed so is the thread's job. so there is
 * only one way to set the job's method which is via constructor
 */
// TODO clear debugging logs
public class UserServerTranslationCallable implements Callable<JSONObject> {

    // declare finals for user server requests
    private static final String REQUESTS_HANDLER = "data_handler.php";
    private static final String FILE_REQUESTS_URL = ServerConstants.SERVER_REQUESTS_URL + REQUESTS_HANDLER;

    public UserServerTranslationCallable() {
    }

    public JSONObject call() throws JSONException {
        return getJSONObjectOfTranslation();
    }

    public JSONObject getJSONObjectOfTranslation() throws JSONException {
        int responseCode = -1;                  // -1 means no connection was established
        StringBuilder response = null;          // to receive server's response
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
            connection.setRequestMethod(ServerConstants.METHOD_GET);
            //connection.setRequestProperty("User-Agent", USER_AGENT);


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
        } catch (IOException ex) {
            Log.d("IOException", ex.toString());
        }

        if (jsonResult != null)
            Log.d("R-Translate", response.toString());
        else
            Log.d("R-Translate", "No result");

        Log.d("RC-Translate", responseCode + "");
        return jsonResult;
    }
}