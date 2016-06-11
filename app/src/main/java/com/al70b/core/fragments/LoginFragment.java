package com.al70b.core.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.AutocompleteClearableEditText;
import com.al70b.core.extended_widgets.ClearableEditText;
import com.al70b.core.fragments.Alerts.PleaseWaitAlert;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.JSONHelper;
import com.al70b.core.misc.StringManp;
import com.al70b.core.notifications.GcmModule;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Naseem on 4/27/2015.
 */
public class LoginFragment extends Fragment {

    // json object to hold the result of the authentication
    // if it is null then authentication hasn't finished yet

    private boolean validEmailSyntax, validPasswordSyntax, validEmailSyntaxDialog; // last one is used for forgot password dialog

    private AutocompleteClearableEditText emailClearableEditText;
    private ClearableEditText passwordClearableEditText;

    // dialog for forgot password
    private Dialog forgotPasswordDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // requests interface handles the server requests
        final RequestsInterface requests = new RequestsInterface(getActivity().getApplicationContext());

        buildForgotPasswordDialog();

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_login, container, false);

        // find views and bind
        emailClearableEditText = (AutocompleteClearableEditText) viewGroup.findViewById(R.id.clearable_edit_text_email);
        passwordClearableEditText = (ClearableEditText) viewGroup.findViewById(R.id.clearable_edit_text_password);
        final Button btnLogin = (Button) viewGroup.findViewById(R.id.btn_login);
        final ImageButton btnValidEmailSyntax = (ImageButton) viewGroup.findViewById(R.id.img_btn_invalid_email_syntax);
        final ImageButton btnValidPasswordSyntax = (ImageButton) viewGroup.findViewById(R.id.img_btn_invalid_password_syntax);
        final TextView txtViewForgotPassword = (TextView) viewGroup.findViewById(R.id.text_view_login_forgot_password);


        final EditText passwordEditText = passwordClearableEditText.getEditText();

        // define clearable edit text special attributes
        emailClearableEditText.getEditText().setHint(R.string.edTxtEmail);


        setAutoCompleteValues();

        passwordClearableEditText.setEditTextHint(R.string.password);

        // set input type to be password support
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // handle email and password syntax validation
        emailClearableEditText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validEmailSyntax = StringManp.isEmailValid(s.toString());

                if (s.length() == 0)
                    btnValidEmailSyntax.setVisibility(View.INVISIBLE);
                else {
                    if (validEmailSyntax)
                        btnValidEmailSyntax.setImageResource(R.drawable.green_check);
                    else
                        btnValidEmailSyntax.setImageResource(R.drawable.attention_red_icon);

                    // show the email validation button
                    btnValidEmailSyntax.setVisibility(View.VISIBLE);
                }
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validPasswordSyntax = StringManp.isPasswordValid(s.toString());

                if (s.length() == 0)
                    btnValidPasswordSyntax.setVisibility(View.INVISIBLE);
                else {

                    if (validPasswordSyntax)
                        btnValidPasswordSyntax.setImageResource(R.drawable.green_check);
                    else
                        btnValidPasswordSyntax.setImageResource(R.drawable.attention_red_icon);

                    // show the button
                    btnValidPasswordSyntax.setVisibility(View.VISIBLE);
                }
            }
        });

        txtViewForgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (forgotPasswordDialog != null)
                    forgotPasswordDialog.show();
            }
        });

        btnLogin.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String message = null;

                if (emailClearableEditText.getText().length() == 0) {
                    message = getString(R.string.error_please_enter_email);

                    if (passwordClearableEditText.getEditText().getText().length() == 0)
                        message = message.concat(" " + getString(R.string.and_password));

                } else if (passwordClearableEditText.getEditText().getText().length() == 0) {
                    message = getString(R.string.error_please_enter_password);
                } else if (validEmailSyntax && validPasswordSyntax) {

                    // hide keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(emailClearableEditText.getWindowToken(), 0);

                    final PleaseWaitAlert alert = new PleaseWaitAlert(getActivity(), R.string.logging_in);
                    alert.show();

                    // handler to show toast in a thread
                    final Handler handler = new Handler();

                    // progress dialog runs in a thread
                    final Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String email, password;

                            // get the values from the components
                            email = emailClearableEditText.getText().toString();
                            password = passwordClearableEditText.getEditText().getText().toString();

                            try {
                                // authenticate given email and password
                                ServerResponse<CurrentUser> serverResponse = requests.authUser(email, password);

                                if (serverResponse.isSuccess()) {

                                    // create new user with user userID and access token from the server
                                    CurrentUser user = serverResponse.getResult();

                                    // register user with GCM service
                                    new GcmModule(getActivity().getApplicationContext(), user).registerToService();

                                    savedEmailToCache(email);

                                    // get user's data from the server
                                    serverResponse = requests.getUserData(user);

                                    if (serverResponse.isSuccess()) {
                                        // load user's data from server into a user object
                                        user = serverResponse.getResult();

                                        // login was successful, need to save access token, user userID, email, and profile picture name into a shared preferences
                                        SharedPreferences sharedPref = getActivity().getSharedPreferences(AppConstants.SHARED_PREF_FILE,
                                                Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();

                                        // save relevant data
                                        editor.putInt(JSONHelper.USER_ID, user.getUserID());
                                        editor.putString(JSONHelper.ACCESS_TOKEN, user.getAccessToken());
                                        editor.putString(JSONHelper.USERNAME, user.getEmail());
                                        editor.putString(JSONHelper.NAME, user.getName());
                                        editor.apply();
                                        // done saving

                                        // intent to show user's home activity
                                        Intent intent = new Intent(getActivity(), UserHomeActivity.class);

                                        // save user object
                                        intent.putExtra(JSONHelper.USER, user);

                                        // dismiss the wait dialog
                                        alert.dismiss();

                                        // start the new activity
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        final String errorMessage = serverResponse.getErrorMsg();

                                        // show the toast
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getActivity(), errorMessage,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    final String toastMessage = serverResponse.getErrorMsg();
                                    // show the toast
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getActivity(), toastMessage,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (ServerResponseFailedException ex) {
                                // show the toast
                                handler.post(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.error_server_connection_falied), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            // dismiss the wait dialog
                            alert.dismiss();
                        }
                    });

                    thread.start();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (alert.isShowing()) {
                                try {
                                    thread.join();
                                } catch (InterruptedException ex) {

                                }

                                alert.dismiss();

                                Toast.makeText(getActivity(), getString(R.string.error_something_went_wrong_try_again_later), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, 10000);

                } else {
                    if (!validEmailSyntax)
                        message = getString(R.string.error_please_enter_valid_email);
                    else if (!validPasswordSyntax)
                        message = getString(R.string.error_please_enter_valid_password);
                }

                if (message != null)
                    Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        btnValidEmailSyntax.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validEmailSyntax)
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_enter_valid_email), Toast.LENGTH_SHORT).show();
            }
        });

        btnValidPasswordSyntax.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validPasswordSyntax)
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_enter_valid_password), Toast.LENGTH_SHORT).show();
            }
        });

        return viewGroup;
    }

    private void savedEmailToCache(String email) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(AppConstants.CACHED_EMAILS_SHARED_PREF,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> set = sharedPref.getStringSet("Emails", new HashSet<String>());

        // add new email
        set.add(email);

        editor.putStringSet("Emails", set);
        editor.apply();
    }

    private void setAutoCompleteValues() {
        //Account[] accounts = AccountManager.get(getActivity()).getAccounts();
        Set<String> emailSet = new HashSet<String>();

        /*Pattern emailPattern = Pattern.compile(StringManp.EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

        for(Account account: accounts) {
            if(emailPattern.matcher(account.name).matches()) {
                emailSet.add(account.name);
            }
        }*/

        // read cached emails from shared preferences 
        SharedPreferences sharedPref = getActivity().getSharedPreferences(AppConstants.CACHED_EMAILS_SHARED_PREF,
                Context.MODE_PRIVATE);
        emailSet = sharedPref.getStringSet("Emails", emailSet);

        emailClearableEditText.getEditText().setAdapter(
                new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1, new ArrayList<String>(emailSet)));
    }

    public void setEmailEditText(String email) {
        emailClearableEditText.getEditText().setText(email);
        passwordClearableEditText.getEditText().requestFocus();
    }

    private void buildForgotPasswordDialog() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                forgotPasswordDialog = new Dialog(getActivity());

                forgotPasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.alert_forgot_password, null, false);
                forgotPasswordDialog.setCanceledOnTouchOutside(true);
                forgotPasswordDialog.setContentView(viewGroup);

                final ClearableEditText emailClearableEditTextDialog = (ClearableEditText) viewGroup.findViewById(R.id.dialog_forgot_password_clearable_edit_text);
                final ImageButton validEmailInDialog = (ImageButton) viewGroup.findViewById(R.id.dialog_forgot_password_img_btn_invalid_email_syntax);
                //final RelativeLayout layout = (RelativeLayout) viewGroup.findViewById(R.id.relative_layout_alert_forgot_password);
                //final ProgressBar sendingProgressBar = (ProgressBar) viewGroup.findViewById(R.id.progress_bar_alert_forgot_password);
                emailClearableEditTextDialog.setEditTextHint(R.string.edTxtEmail);

                // handle email syntax validation
                emailClearableEditTextDialog.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        validEmailSyntaxDialog = StringManp.isEmailValid(s.toString());

                        if (s.length() == 0)
                            validEmailInDialog.setVisibility(View.INVISIBLE);
                        else {
                            if (validEmailSyntaxDialog)
                                validEmailInDialog.setImageResource(R.drawable.green_check);
                            else
                                validEmailInDialog.setImageResource(R.drawable.attention_red_icon);

                            // show the email validation button
                            validEmailInDialog.setVisibility(View.VISIBLE);
                        }
                    }
                });

                final Button btnCancel = (Button) viewGroup.findViewById(R.id.dialog_cancel);
                final Button btnOk = (Button) viewGroup.findViewById(R.id.dialog_ok);
                btnOk.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (validEmailSyntaxDialog) {

                            //layout.setVisibility(View.GONE);
                            //sendingProgressBar.setVisibility(View.VISIBLE);
                            btnOk.setEnabled(false);
                            btnCancel.setEnabled(false);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String email = emailClearableEditTextDialog.getEditText().getText().toString();

                                    RequestsInterface requestsInterface = new RequestsInterface(getActivity().getApplicationContext());
                                    String msg;
                                    try {
                                        ServerResponse<String> sr = requestsInterface.forgotPassword(email);

                                        if (sr.isSuccess()) {
                                            msg = getString(R.string.email_was_sent_with_password);
                                            forgotPasswordDialog.dismiss();
                                        } else {
                                            msg = sr.getErrorMsg();
                                        }
                                    } catch (ServerResponseFailedException ex) {
                                        msg = ex.toString();
                                    }

                                    final String finalMsg = msg;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //layout.setVisibility(View.VISIBLE);
                                            //sendingProgressBar.setVisibility(View.GONE);
                                            btnOk.setEnabled(true);
                                            btnCancel.setEnabled(true);

                                            Toast.makeText(getActivity().getApplicationContext(), finalMsg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }).start();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_please_enter_valid_email), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                btnCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        forgotPasswordDialog.dismiss();
                    }
                });

                forgotPasswordDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        emailClearableEditTextDialog.getEditText().setText("");
                    }
                });
            }
        });
    }

}
