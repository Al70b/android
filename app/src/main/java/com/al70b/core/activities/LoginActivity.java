package com.al70b.core.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.Dialogs.ForgotPasswordDialog;
import com.al70b.core.activities.Dialogs.PleaseWaitDialog;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.AutocompleteClearableEditText;
import com.al70b.core.extended_widgets.ClearableEditText;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.KEYS;
import com.al70b.core.misc.StringManp;
import com.al70b.core.notifications.GcmModule;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Naseem on 8/25/2016.
 */
public class LoginActivity extends Activity {

    public static final String CACHED_EMAILS_KEY = "LoginActivity.CachedEmails";
    public static final String EMAIL = "LoginActivity.Email";


    private boolean validEmailSyntax, validPasswordSyntax;

    private AutocompleteClearableEditText emailClearableEditText;
    private ClearableEditText passwordClearableEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // find views and bind
        emailClearableEditText = (AutocompleteClearableEditText) findViewById(R.id.clearable_edit_text_email);
        passwordClearableEditText = (ClearableEditText) findViewById(R.id.clearable_edit_text_password);
        final ImageButton btnValidEmailSyntax = (ImageButton) findViewById(R.id.img_btn_invalid_email_syntax);
        final ImageButton btnValidPasswordSyntax = (ImageButton) findViewById(R.id.img_btn_invalid_password_syntax);

        setAutoCompleteValues(emailClearableEditText.getEditText());

        Intent intent = getIntent();
        if(intent != null) {
            emailClearableEditText.getEditText().setText(intent.getStringExtra(EMAIL));
        }

        // define clearable edit text special attributes
        emailClearableEditText.getEditText().setHint(R.string.edTxtEmail);
        emailClearableEditText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validEmailSyntax = StringManp.isValidEmail(s.toString());

                if (s.length() == 0) {
                    btnValidEmailSyntax.setVisibility(View.INVISIBLE);
                } else {
                    if (validEmailSyntax) {
                        btnValidEmailSyntax.setImageResource(R.drawable.green_check);
                    } else {
                        btnValidEmailSyntax.setImageResource(R.drawable.attention_red_icon);
                    }

                    // show the email validation button
                    btnValidEmailSyntax.setVisibility(View.VISIBLE);
                }
            }
        });

        // set input type to be password support, and add text change listener
        passwordClearableEditText.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordClearableEditText.setEditTextHint(R.string.password);
        passwordClearableEditText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validPasswordSyntax = StringManp.isPasswordValid(s.toString());

                if (s.length() == 0) {
                    btnValidPasswordSyntax.setVisibility(View.INVISIBLE);
                } else {

                    if (validPasswordSyntax) {
                        btnValidPasswordSyntax.setImageResource(R.drawable.green_check);
                    } else {
                        btnValidPasswordSyntax.setImageResource(R.drawable.attention_red_icon);
                    }

                    // show the button
                    btnValidPasswordSyntax.setVisibility(View.VISIBLE);
                }
            }
        });


        btnValidEmailSyntax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validEmailSyntax) {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_please_enter_valid_email),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnValidPasswordSyntax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validPasswordSyntax) {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_please_enter_valid_password),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_guest_login, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_login_close:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savedEmailToCache(String email) {
        SharedPreferences sharedPref = getSharedPreferences(AppConstants.CACHED_EMAILS_SHARED_PREF,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> set = sharedPref.getStringSet(CACHED_EMAILS_KEY, new HashSet<String>());

        // add new email
        set.add(email);

        editor.putStringSet(CACHED_EMAILS_KEY, set);
        editor.apply();
    }

    private void setAutoCompleteValues(AutoCompleteTextView editText) {
        Set<String> emailSet = new HashSet<String>();

        // read cached emails from shared preferences
        SharedPreferences sharedPref = getSharedPreferences(AppConstants.CACHED_EMAILS_SHARED_PREF,
                Context.MODE_PRIVATE);
        emailSet = sharedPref.getStringSet(CACHED_EMAILS_KEY, emailSet);
        editText.setAdapter(
                new ArrayAdapter<String>(this, R.layout.simple_list_item_1, new ArrayList<String>(emailSet)));
    }

    public void onClickLoginButton(View view) {
        String message;
        // get the values from the widgets
        String email = emailClearableEditText.getText().toString();
        String password = passwordClearableEditText.getText().toString();

        if (email.isEmpty()) {
            message = getString(R.string.error_please_enter_email);

            if (password.isEmpty()) {
                message = message.concat(" " + getString(R.string.and_password));
            }
        } else if (password.isEmpty()) {
            message = getString(R.string.error_please_enter_password);
        } else {
            // email and password are not empty, check if valid
            if (validEmailSyntax && validPasswordSyntax) {
                // hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(emailClearableEditText.getWindowToken(), 0);

                // login
                new LoginAsyncTask(email, password).execute();
                return;
            } else {
                if (!validEmailSyntax) {
                    message = getString(R.string.error_please_enter_valid_email);
                } else {
                    message = getString(R.string.error_please_enter_valid_password);
                }
            }
        }

        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }

    public void onClickForgotPassword(View view) {
        ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog(LoginActivity.this
                , emailClearableEditText.getText().toString());
        forgotPasswordDialog.show();
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, Pair<Boolean, String>> {

        private String email, password;
        private PleaseWaitDialog alert;
        private CurrentUser user;

        public LoginAsyncTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            alert = new PleaseWaitDialog(LoginActivity.this,
                    R.string.logging_in);
            alert.show();
        }

        @Override
        protected Pair<Boolean, String> doInBackground(Void... params) {
            // requests interface handles the server requests
            RequestsInterface requests = new RequestsInterface(LoginActivity.this);

            Pair<Boolean, String> result;
            try {
                // authenticate given email and password
                ServerResponse<CurrentUser> serverResponse = requests.authUser(email, password);

                if (serverResponse.isSuccess()) {
                    // create new user with user userID and access token from the server
                    user = serverResponse.getResult();

                    // register user with GCM service
                    new GcmModule(LoginActivity.this, user).registerToService();

                    savedEmailToCache(email);

                    // get user's data from the server
                    serverResponse = requests.getUserData(user);

                    if (serverResponse.isSuccess()) {
                        // load user's data from server into a user object
                        user = serverResponse.getResult();

                        // login was successful, need to save access token, user userID, email, and profile picture name into a shared preferences
                        SharedPreferences sharedPref = getSharedPreferences(AppConstants.SHARED_PREF_FILE,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();

                        // save relevant data
                        editor.putInt(KEYS.SHARED_PREFERENCES.USER_ID, user.getUserID());
                        editor.putString(KEYS.SHARED_PREFERENCES.ACCESS_TOKEN, user.getAccessToken());
                        editor.putString(KEYS.SHARED_PREFERENCES.USERNAME, user.getEmail());
                        editor.putString(KEYS.SHARED_PREFERENCES.NAME, user.getName());
                        editor.putString(KEYS.SHARED_PREFERENCES.PASSWORD, user.getPassword());
                        editor.apply();
                        // done saving

                        result = new Pair<>(true, "Success");
                    } else {
                        user = null;
                        result = new Pair<>(false, serverResponse.getErrorMsg());
                    }
                } else {
                    result = new Pair<>(false, serverResponse.getErrorMsg());
                }
            } catch (ServerResponseFailedException ex) {
                user = null;
                result = new Pair<>(false, getString(R.string.error_server_connection_falied)
                        .concat(ex.toString()));
            }

            return result;
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> result) {

            if(result.first && user != null) {
                // Login was successful, move to user home activity
                // intent to show user's home activity
                Intent intent = new Intent(LoginActivity.this, UserHomeActivity.class);

                // save user object
                intent.putExtra(KEYS.SHARED_PREFERENCES.USER, user);

                ((MyApplication) getApplication()).setCurrentUser(user);

                // start the new activity
                startActivity(intent);

                finish();
            } else {
                // Failed to login
                Toast.makeText(LoginActivity.this, result.second, Toast.LENGTH_SHORT).show();
            }

            // dismiss the wait dialog
            alert.dismiss();
        }
    }
}
