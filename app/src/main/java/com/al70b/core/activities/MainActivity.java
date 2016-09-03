package com.al70b.core.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.KEYS;
import com.al70b.core.misc.Translator;
import com.al70b.core.notifications.GcmModule;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

import java.io.File;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    // object to handle server requests
    private RequestsInterface requests;

    private MyApplication myApp;

    private LoginAsyncTask loginAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myApp = (MyApplication) getApplication();
        requests = new RequestsInterface(this);
    }


    @Override
    public void onStart() {
        super.onStart();

        new LoginAsyncTask().execute();

        myApp.setAppVisible();
    }

    private class LoginAsyncTask extends AsyncTask<Void, String, Boolean> {
        // widgets
        private TextView tvUserWelcomeMessage, tvLoginToAnotherAccount;
        private ProgressBar progressBar;
        private CurrentUser user;

        private String error = null;

        protected LoginAsyncTask() {
            progressBar = (ProgressBar) findViewById(R.id.progress_ring_main_activity);
            tvUserWelcomeMessage = (TextView) findViewById(R.id.text_view_main_welcome_user);
            tvLoginToAnotherAccount = (TextView) findViewById(R.id.text_view_main_sign_in_with_another);
            tvLoginToAnotherAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvLoginToAnotherAccount.setText(getString(R.string.loggingout));
                    cancel(true);
                }
            });
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // initialize environment
            initializeEnvironment();

            // user userID, access token, email, and saved profile via saved shared preferences, if exists
            SharedPreferences sharedPref = getSharedPreferences(
                    AppConstants.SHARED_PREF_FILE,
                    MainActivity.MODE_PRIVATE);

            int userID = sharedPref.getInt(KEYS.SHARED_PREFERENCES.USER_ID, -1);
            String accessToken = sharedPref.getString(KEYS.SHARED_PREFERENCES.ACCESS_TOKEN, null);
            String email = sharedPref.getString(KEYS.SHARED_PREFERENCES.USERNAME, null);
            String name = sharedPref.getString(KEYS.SHARED_PREFERENCES.NAME, null);
            String password = sharedPref.getString(KEYS.SHARED_PREFERENCES.PASSWORD, null);

            // check if user is already logged in by checking if user_id and access token
            // are saved in shared preferences
            if (userID != -1 && accessToken != null) {
                // user details are saved, start application in user mode

                // update welcome message with user's name welcome message
                if(name != null) {
                    publishProgress(name);
                }

                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Log.e(TAG, ex.toString());
                }
                if(isCancelled()) {
                    return null;
                }*/

                // create user object to hold data throughout the app
                user = new CurrentUser(MainActivity.this,
                        userID, accessToken, email, password);

                // set current user somewhere safe
                myApp.setCurrentUser(user);

                ServerResponse<CurrentUser> serverResponse;
                try {
                    // load user's information from server
                    serverResponse = requests.getUserData(user);
                } catch (ServerResponseFailedException ex) {
                    error = ex.toString();
                    return false;
                }

                // server response is ok (not null)
                if (serverResponse.isSuccess()) {
                    // load user's data from server
                    user = serverResponse.getResult();
                    return true;
                } else {
                    error = serverResponse.getErrorMsg();
                }
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            String name = params[0];

            if(name != null) {
                // set welcome message to welcome user
                tvUserWelcomeMessage.setText(getString(R.string.welcome_user, name));
                tvUserWelcomeMessage.setOnClickListener(null);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                startAppInUserMode(user);
            } else {
                if(error != null) {
                    Toast.makeText(MainActivity.this, error,
                            Toast.LENGTH_LONG).show();
                }

                // start application in guest mode
                startAppInGuestMode();
            }
        }

        @Override
        protected void onCancelled(Boolean result) {
            // clear data of the current user
            logout();

            startAppInGuestMode();
        }
    }

    /**
     * Clear all data related to the previous user
     */
    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences(AppConstants.SHARED_PREF_FILE,
                MODE_PRIVATE);

        int userId = sharedPref.getInt(KEYS.SHARED_PREFERENCES.USER_ID, -1);
        String accessToken = sharedPref.getString(KEYS.SHARED_PREFERENCES.ACCESS_TOKEN, null);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(KEYS.SHARED_PREFERENCES.USER_ID);
        editor.remove(KEYS.SHARED_PREFERENCES.ACCESS_TOKEN);
        editor.remove(KEYS.SHARED_PREFERENCES.USERNAME);
        editor.remove(KEYS.SHARED_PREFERENCES.NAME);
        editor.remove(KEYS.SHARED_PREFERENCES.DONT_ASK_FOR_PROFILE_PICTURE_UPLOAD);
        editor.apply();

        if (userId >= 0 && accessToken != null) {
            new GcmModule(this, userId, accessToken).deleteRegistrationIdFromBackend();
        }

        if (myApp != null) {
            myApp.setCurrentUser(null);
        }
    }

    /**
     * Simply start the application in guest mode so user can log ing
     * again or register.
     */
    private void startAppInGuestMode() {
        // start application in guest mode
        startIntent(new Intent(MainActivity.this, GuestHomeActivity.class));
    }

    /**
     * Start the application in user mode given the user
     *
     * @param user: user object holding user's details
     */
    private void startAppInUserMode(CurrentUser user) {
        // start user home activity, pass user object to it
        Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
        intent.putExtra(KEYS.SHARED_PREFERENCES.USER, user);
        startIntent(intent);
    }

    /**
     * Start the given intent and finish the current one
     *
     * @param intent
     */
    private void startIntent(Intent intent) {
        // start the intended activity
        startActivity(intent);

        // finish this activity
        finish();
    }

    private void initializeEnvironment() {
        Context context = getApplicationContext();

        // create directory for this app if it doesn't already exist
        File appDirectory = new File(context.getFilesDir() + AppConstants.THUMBNAILS_FOLDER_PATH);

        if (!appDirectory.exists()) {
            if(!appDirectory.mkdirs()){
                Log.e(TAG, "Could not create directory for app al70b.");
            }
        }

        // get translations to use in the app
        myApp.setTranslator(Translator.getInstance(context));
    }

    @Override
    public void onStop() {
        super.onStop();

        myApp.setAppInvisible();
    }
}
