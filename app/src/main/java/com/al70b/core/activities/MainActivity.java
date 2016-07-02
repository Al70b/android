package com.al70b.core.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.JSONHelper;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

import java.io.File;

public class MainActivity extends Activity {

    private Context context;

    // object to handle server response
    private ServerResponse<CurrentUser> serverResponse;

    // widgets
    private TextView textViewWelcomeUser;
    private RelativeLayout layoutMain;
    private ProgressBar progressBar;

    // handler to show toast messages in threads
    private int userID;
    private String accessToken, email, name;
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
        TextView textViewAnotherAccount = (TextView) findViewById(R.id.text_view_main_sign_in_with_another);
        textViewAnotherAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop login thread
                threadYield = true;

                // remove data
                UserHomeActivity.logout(context);

                startAppInGuestMode();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        // get user userID, access token, email, and saved profile via saved shared preferences, if exists
        SharedPreferences sharedPref = getSharedPreferences(AppConstants.SHARED_PREF_FILE, MainActivity.MODE_PRIVATE);

        userID = sharedPref.getInt(JSONHelper.USER_ID, -1);
        accessToken = sharedPref.getString(JSONHelper.ACCESS_TOKEN, null);
        email = sharedPref.getString(JSONHelper.USERNAME, null);
        name = sharedPref.getString(JSONHelper.NAME, null);

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onResume() {
        super.onResume();

        // start a new thread to avoid work on gui thread and let progress circle show
        final Runnable runnable = new Runnable() {
            public void run() {

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
                        Thread.sleep(1700);
                    } catch (InterruptedException ex) {

                    }

                    // create user object to hold data throughout the app
                    user = new CurrentUser(context, userID, accessToken, email);

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
                                    showAlertDialog();
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
        };

        new Thread(runnable).start();
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
        intent.putExtra(JSONHelper.USER, user);
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

        if (!appDirectory.exists())
            appDirectory.mkdirs();

        // get translations to use in the app
        getTranslations();
    }


    private void getTranslations() {
        // this creates the translator object, and gives it a context
        ((MyApplication)getApplication()).setTranslator(Translator.getInstance(context));
    }


    @Override
    public void onStop() {
        super.onStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }


}
