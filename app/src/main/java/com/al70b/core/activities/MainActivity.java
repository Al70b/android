package com.al70b.core.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

    private Context context;

    // object to handle server response
    private ServerResponse<CurrentUser> serverResponse;

    // widgets
    private TextView textViewWelcomeUser;
    private RelativeLayout layoutMain;
    private ProgressBar progressBar;

    // handler to show toast messages in threads
    private int userID;
    private String accessToken, email, name, password;
    private CurrentUser user;

    private boolean threadYield;

    // object to handle server requests
    private RequestsInterface requests;

    // if server connection failed show a dialog with appropriate message and
    // ask for retrying connection
    private boolean tryAgain = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize environment
        initializeEnvironment();

        progressBar = (ProgressBar) findViewById(R.id.progress_ring_main_activity);
        textViewWelcomeUser = (TextView) findViewById(R.id.text_view_main_welcome_user);
        final TextView textViewAnotherAccount = (TextView) findViewById(R.id.text_view_main_sign_in_with_another);
        textViewAnotherAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewAnotherAccount.setText(getString(R.string.loggingout));

                // stop login thread
                threadYield = true;

                // clear data of the current user
                logout();

                startAppInGuestMode();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onResume() {
        super.onResume();

        // start a new thread to avoid work on gui thread and let progress circle show
        LoginTask task = new LoginTask();
        Thread t = new Thread(task);
        t.start();
    }

    private class LoginTask implements Runnable {

        public void run() {

            // user userID, access token, email, and saved profile via saved shared preferences, if exists
            SharedPreferences sharedPref = getSharedPreferences(
                    AppConstants.SHARED_PREF_FILE,
                    MainActivity.MODE_PRIVATE);

            userID = sharedPref.getInt(KEYS.SHARED_PREFERENCES.USER_ID, -1);
            accessToken = sharedPref.getString(KEYS.SHARED_PREFERENCES.ACCESS_TOKEN, null);
            email = sharedPref.getString(KEYS.SHARED_PREFERENCES.USERNAME, null);
            name = sharedPref.getString(KEYS.SHARED_PREFERENCES.NAME, null);
            password = sharedPref.getString(KEYS.SHARED_PREFERENCES.PASSWORD, null);

            // check if user is already logged in by checking if user_id and access token
            // are saved in shared preferences
            if (userID != -1 && accessToken != null) {
                // user details are saved, start application in user mode

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setVisibility(View.VISIBLE);

                        // set welcome message to welcome user
                        textViewWelcomeUser.setText(getString(R.string.welcome_user, name));
                        textViewWelcomeUser.setOnClickListener(null);
                    }
                });

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Log.e(TAG, ex.toString());
                }

                // create user object to hold data throughout the app
                user = new CurrentUser(context, userID, accessToken, email, password);

                // set current user somewhere safe
                ((MyApplication)getApplication()).setCurrentUser(user);

                boolean serverResponseOk;
                do {
                    try {
                        // load user's information from server
                        serverResponse = requests.getUserData(user);
                    } catch (ServerResponseFailedException ex) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                // show dialog, if user chooses yes, try connecting again
                                // else stop trying and jump to guest mode
                                //showAlertDialog();
                                handleNoConnectionOrTimedOut(this);
                            }
                        });
                    }
                    serverResponseOk = serverResponse != null;
                } while (!serverResponseOk && tryAgain && !threadYield);

                // server response is ok (not null)
                if (serverResponse != null && serverResponse.isSuccess()) {
                    // load user's data from server
                    user = serverResponse.getResult();

                    if (!threadYield)
                        // start application in user mode
                        startAppInUserMode(user);
                } else {
                    // show the toast
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, serverResponse.getErrorMsg(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    if (serverResponse.getErrorMsg().compareTo(getString(R.string.error_no_response_time_out)) == 0)
                        handleNoConnectionOrTimedOut(this);
                }

            } else {
                // start application in guest mode
                startAppInGuestMode();
            }
        }
    }

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
        editor.remove("DONT_ASK");
        editor.apply();

        if (userId >= 0 && accessToken != null) {
            new GcmModule(this, userId, accessToken).deleteRegistrationIdFromBackend();
        }

        MyApplication myApp = ((MyApplication)getApplication());
        if (myApp != null) {
            myApp.setCurrentUser(null);
        }
    }

    private void handleNoConnectionOrTimedOut(final Runnable runnable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                textViewWelcomeUser.setText(getString(R.string.press_here_to_try_again));
                threadYield = true;
                textViewWelcomeUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        threadYield = false;
                        onResume();
                    }
                });

            }
        });
    }

    private void setTryAgain(boolean tryAgain) {
        this.tryAgain = tryAgain;
    }

    /**
     * Simply start the application in guest mode so user can log ing
     * again or register.
     */
    private void startAppInGuestMode() {
        // start application in guest mode
        startIntent(new Intent(context, ScreenSlideHomeActivity.class));
    }

    /**
     * Start the application in user mode given the user
     *
     * @param user: user object holding user's details
     */
    private void startAppInUserMode(CurrentUser user) {
        // start user home activity, pass user object to it
        Intent intent = new Intent(this.context, UserHomeActivity.class);
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

    private void showAlertDialog() {
       /* new AlertDialog.Builder(context)
                .setTitle("Connection Failed")
                .setMessage(getResources().getString(R.string.error_server_connection_falied))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mainActivity.setTryAgain(true);
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mainActivity.setTryAgain(false);
                        mainActivity.startAppInGuestMode();
                    }
                })
                .show();*/
    }


    private void initializeEnvironment() {
        context = getApplicationContext();

        requests = new RequestsInterface(context.getApplicationContext());

        // create directory for this app if it doesn't already exist
        File appDirectory = new File(context.getFilesDir() + AppConstants.THUMBNAILS_FOLDER_PATH);

        if (!appDirectory.exists()) {
            appDirectory.mkdirs();
        }

        // get translations to use in the app
        ((MyApplication)getApplication()).setTranslator(Translator.getInstance(context));
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }
}
