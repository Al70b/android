package com.al70b.core.notifications;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by Naseem on 8/13/2016.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegistrationIntentServ";
    public static final String USER_ID = "RegistrationIntentServer.USER_ID";
    public static final String ACCESS_TOKEN = "RegistrationIntentServer.ACCESS_TOKEN";

    private String userID;
    private String accessToken;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RegistrationIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);

        try {
            String token = instanceID.getToken(AppConstants.GCM_SENDER_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationIdToBackend(token);
        } catch(IOException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private void sendRegistrationIdToBackend(String token) {
        Log.d(TAG, "Sending token to server");
        RequestsInterface requestsInterface = new RequestsInterface(this);

        try {
            ServerResponse<String> sr = requestsInterface
                    .registerClientID(String.valueOf(userID),
                            accessToken, token);

            if (sr != null && sr.isSuccess()) {
                Log.d(TAG, "register client ID to back end was successful");
            } else {
                Log.d(TAG, "register client ID to back end failed");
            }
        } catch (ServerResponseFailedException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public void deleteRegistrationIdFromBackend() {
        RequestsInterface requestsInterface = new RequestsInterface(this);
        try {
            ServerResponse<String> sr = requestsInterface.unregisterClientID(String.valueOf(userID),
                    accessToken);

            if (sr != null && sr.isSuccess()) {
                Log.d(TAG, "deleting registered client ID from back end was successful");
            } else {
                Log.d(TAG, "deleting registered client ID from back end failed");
            }
        } catch (ServerResponseFailedException ex) {
            Log.e(TAG, ex.toString());
        }
    }
}
