package com.al70b.core.notifications;

import android.content.Context;
import android.os.AsyncTask;

import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Naseem on 10/9/2015.
 */
public class GcmModule {

    private static final String TAG = "GCMRelated";
    GoogleCloudMessaging gcm;
    private String regID;
    private Context context;
    private CurrentUser currentUser;

    public GcmModule(Context context, CurrentUser currentUser) {
        this.context = context;
        this.gcm = GoogleCloudMessaging.getInstance(context);
        this.currentUser = currentUser;
    }


    public void registerToService() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }

                    regID = gcm.register(AppConstants.PROJECT_NUMBER);

                    msg = "Device registered, registration ID=" + regID;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }


            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                //Log.v(TAG, result);
            }

        }.execute(null, null);
    }

    private void sendRegistrationIdToBackend() {
        RequestsInterface requestsInterface = new RequestsInterface(context);

        try {
            ServerResponse<String> sr = requestsInterface.registerClientID(currentUser, regID);

            if (sr.isSuccess()) {
                //Log.d("RegisterToBackend", "register client ID to back end was successful");
            } else {
                //Log.d("RegisterToBackend", "register client ID to back end failed");
            }
        } catch (ServerResponseFailedException ex) {

        }
    }

    public void deleteRegistrationIdFromBackend() {
        RequestsInterface requestsInterface = new RequestsInterface(context);
        try {
            ServerResponse<String> sr = requestsInterface.unregisterClientID(currentUser);

            if (sr.isSuccess()) {
                // Log.d("RegisterToBackend", "deleting registered client ID from back end was successful");
            } else {
                //Log.d("RegisterToBackend", "deleting registered client ID from back end failed");
            }
        } catch (ServerResponseFailedException ex) {

        }
    }

}
