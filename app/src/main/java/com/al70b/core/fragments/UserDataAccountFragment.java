package com.al70b.core.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.misc.StringManp;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

/**
 * Created by Naseem on 5/20/2015.
 */
public class UserDataAccountFragment extends Fragment {

    private TextView txtViewEmail;
    private TextView textViewEmailClickable, textViewPasswordClickable;
    private EditText etEmail, etPassword, etRetypePassword, editTextPassword1, editTextPassword2;
    private ImageButton btnValidEmailSyntax, btnValidPasswordSyntax, btnValidRetypePasswordSyntax;
    private LinearLayout layoutEmail, layoutPassword;

    private CurrentUser user;

    private Handler handler;
    private boolean emailON, passwordON, infoUpdated;
    private boolean validEmailSyntax, validPasswordSyntax, validRetypePassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = ((MyApplication)getActivity().getApplication()).getCurrentUser();

        handler = new Handler();
        // in order to add few menu items
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_data_account, container, false);

        txtViewEmail = (TextView) viewGroup.findViewById(R.id.text_view_user_data_account_emailB);
        etEmail = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_account_retype_emailB);
        etPassword = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_account_passwordB);
        etRetypePassword = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_account_retype_passwordB);
        btnValidEmailSyntax = (ImageButton) viewGroup.findViewById(R.id.btn_user_data_invalid_email_syntax);
        btnValidPasswordSyntax = (ImageButton) viewGroup.findViewById(R.id.btn_user_data_invalid_password_syntax);
        btnValidRetypePasswordSyntax = (ImageButton) viewGroup.findViewById(R.id.btn_user_data_invalid_retype_password_syntax);
        layoutEmail = (LinearLayout) viewGroup.findViewById(R.id.layout_user_data_account_email);
        layoutPassword = (LinearLayout) viewGroup.findViewById(R.id.layout_user_data_account_password);

        textViewEmailClickable = (TextView) viewGroup.findViewById(R.id.clickable_text_view_change_email);
        textViewPasswordClickable = (TextView) viewGroup.findViewById(R.id.clickable_text_view_change_password);
        editTextPassword1 = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_account_password_1);
        editTextPassword2 = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_account_password_2);

        textViewEmailClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailON)
                    return;

                // email update is on
                emailON = true;
                passwordON = false;

                // hide change password
                textViewPasswordClickable.setVisibility(View.GONE);

                layoutEmail.setVisibility(View.VISIBLE);
                etEmail.setVisibility(View.VISIBLE);
                txtViewEmail.setVisibility(View.GONE);

                // lock navigation
                lockForEdit();
            }
        });

        textViewPasswordClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordON)
                    return;


                // password update is on
                emailON = false;
                passwordON = true;

                // hide change email
                textViewEmailClickable.setVisibility(View.GONE);

                layoutPassword.setVisibility(View.VISIBLE);

                // lock navigation
                lockForEdit();
            }
        });


        // handle email and password syntax validation
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validEmailSyntax = StringManp.isValidEmail(s.toString());

                if (s.length() == 0)
                    btnValidEmailSyntax.setVisibility(View.INVISIBLE);
                else {
                    if (emailON) {
                        if (validEmailSyntax)

                            btnValidEmailSyntax.setImageResource(R.drawable.green_check);
                        else
                            btnValidEmailSyntax.setImageResource(R.drawable.attention_red_icon);
                    } else {
                        btnValidEmailSyntax.setVisibility(View.INVISIBLE);
                        return;
                    }


                    // show the email validation button
                    btnValidEmailSyntax.setVisibility(View.VISIBLE);
                }
            }
        });


        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validPasswordSyntax = StringManp.isPasswordValid(s.toString());
                validRetypePassword = (etRetypePassword.getText().toString().compareTo(s.toString()) == 0);

                if (s.length() == 0)
                    btnValidPasswordSyntax.setVisibility(View.INVISIBLE);
                else {

                    if (validPasswordSyntax)
                        btnValidPasswordSyntax.setImageResource(R.drawable.green_check);
                    else
                        btnValidPasswordSyntax.setImageResource(R.drawable.attention_red_icon);

                    // show the button
                    btnValidPasswordSyntax.setVisibility(View.VISIBLE);

                    // alert the user that there's a miss match between password and retype password
                    if (etRetypePassword.getText().toString().compareTo(etPassword.getText().toString()) != 0) {
                        btnValidRetypePasswordSyntax.setImageResource(R.drawable.attention_red_icon);
                        btnValidRetypePasswordSyntax.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        etRetypePassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validRetypePassword = (etPassword.getText().toString().compareTo(s.toString()) == 0);

                if (s.length() == 0)
                    btnValidRetypePasswordSyntax.setVisibility(View.INVISIBLE);
                else {
                    if (validRetypePassword)
                        btnValidRetypePasswordSyntax.setImageResource(R.drawable.green_check);
                    else
                        btnValidRetypePasswordSyntax.setImageResource(R.drawable.attention_red_icon);

                    // show the button
                    btnValidRetypePasswordSyntax.setVisibility(View.VISIBLE);
                }
            }
        });

        btnValidEmailSyntax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validEmailSyntax) {
                    String msg;

                    if (etEmail.getText().toString().length() == 0)
                        msg = getString(R.string.error_fill_your_email);
                    else
                        msg = getString(R.string.error_invalid_email_syntax);

                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnValidPasswordSyntax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validPasswordSyntax) {
                    String msg;

                    if (etPassword.getText().toString().length() == 0)
                        msg = getString(R.string.error_fill_your_password);
                    else
                        msg = getString(R.string.error_invalid_password_syntax);

                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnValidRetypePasswordSyntax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validRetypePassword) {
                    String msg;

                    if (etRetypePassword.getText().toString().length() == 0)
                        msg = getString(R.string.error_fill_your_retype_password);
                    else
                        msg = getString(R.string.passwords_dont_match);

                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });


        return viewGroup;
    }

    private void fillData() {
        txtViewEmail.setText(user.getEmail());
        etEmail.setText(txtViewEmail.getText().toString());
    }


    private void lockForEdit() {
        getActivity().startActionMode(new ActionMode.Callback() {

            boolean finishActionMode = false;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle(getString(R.string.edit_user_data));

                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.menu_user_data_edit, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {


                switch (item.getItemId()) {
                    case R.id.action_mode_user_data_submit:
                        final RequestsInterface requestsInterface = new RequestsInterface(getActivity());

                        if (emailON) {
                            // email change is on
                            final String email = etEmail.getText().toString();
                            final String password = editTextPassword1.getText().toString();

                            if (validEmailSyntax && password.trim().length() > 0) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ServerResponse<String> sr = requestsInterface.updateEmail(user, email, password);

                                            String msg;
                                            if (sr.isSuccess()) {
                                                msg = getString(R.string.done_changing_email_successfully);

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        layoutEmail.setVisibility(View.GONE);
                                                        etEmail.setVisibility(View.GONE);
                                                        txtViewEmail.setText(email);
                                                        txtViewEmail.setVisibility(View.VISIBLE);
                                                        editTextPassword1.setText("");
                                                    }
                                                });
                                                user.setEmail(email);

                                                finishActionMode = true;
                                            } else {
                                                msg = sr.getErrorMsg();
                                            }

                                            final String finalMSG = msg;
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), finalMSG, Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                            if (finishActionMode)
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mode.finish();
                                                    }
                                                });
                                        } catch (final ServerResponseFailedException ex) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).start();
                            } else if (!validEmailSyntax) {
                                Toast.makeText(getActivity(), getString(R.string.error_please_enter_valid_email), Toast.LENGTH_SHORT).show();
                            } else if (password.trim().length() <= 0) {
                                Toast.makeText(getActivity(), getString(R.string.error_fill_password_to_change_email), Toast.LENGTH_LONG).show();
                            }
                        } else if (passwordON) {
                            // password change is on
                            final String newPassword = etPassword.getText().toString();
                            final String password = editTextPassword2.getText().toString();

                            if (validPasswordSyntax && validRetypePassword && password.trim().length() > 0) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ServerResponse<String> sr = requestsInterface.updatePassword(user, newPassword, password);

                                            String msg;
                                            if (sr.isSuccess()) {
                                                msg = getString(R.string.done_changing_password_successfully);

                                                user.setPassword(newPassword);
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        layoutPassword.setVisibility(View.GONE);
                                                        editTextPassword2.setText("");
                                                        etRetypePassword.setText("");
                                                        etPassword.setText("");
                                                    }
                                                });

                                                finishActionMode = true;
                                            } else {
                                                msg = sr.getErrorMsg();
                                            }

                                            final String finalMSG = msg;
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), finalMSG, Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                            if (finishActionMode)
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mode.finish();
                                                    }
                                                });
                                        } catch (final ServerResponseFailedException ex) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).start();
                            } else if (password.trim().length() <= 0) {
                                Toast.makeText(getActivity(), getString(R.string.error_fill_password_to_change_it), Toast.LENGTH_SHORT).show();
                            } else if (!validPasswordSyntax) {
                                Toast.makeText(getActivity(), getString(R.string.error_fill_your_password), Toast.LENGTH_SHORT).show();
                            } else if (!validRetypePassword) {
                                Toast.makeText(getActivity(), getString(R.string.error_passwords_dont_match), Toast.LENGTH_SHORT).show();
                            }
                        }

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
                        btnValidEmailSyntax.setVisibility(View.INVISIBLE);

                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        // show the other change option
                        if (emailON) {
                            textViewPasswordClickable.setVisibility(View.VISIBLE);
                        } else if (passwordON) {
                            textViewEmailClickable.setVisibility(View.VISIBLE);
                        }

                        // unlock drawers and navigation
                        ((UserDataFragment) getParentFragment()).viewPager.setPagingEnabled(true);
                        ((UserDataFragment) getParentFragment()).slidingTabLayout.lockTabs(true);
                        ((UserHomeActivity) getActivity()).lockDrawers(false);

                        if (emailON) {
                            layoutEmail.setVisibility(View.GONE);

                            etEmail.setVisibility(View.GONE);
                            etEmail.setText(user.getEmail());
                            editTextPassword1.setText("");
                            txtViewEmail.setVisibility(View.VISIBLE);
                            btnValidEmailSyntax.setVisibility(View.INVISIBLE);
                            emailON = false;
                        } else if (passwordON) {
                            layoutPassword.setVisibility(View.GONE);
                            editTextPassword2.setText("");
                            etPassword.setText("");
                            etRetypePassword.setText("");
                            btnValidPasswordSyntax.setVisibility(View.INVISIBLE);
                            btnValidRetypePasswordSyntax.setVisibility(View.INVISIBLE);
                            passwordON = false;
                        }

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);

                    }
                });
            }
        });

        // lock drawers and navigation
        ((UserDataFragment) getParentFragment()).viewPager.setPagingEnabled(false);
        ((UserDataFragment) getParentFragment()).slidingTabLayout.lockTabs(false);
        ((UserHomeActivity) getActivity()).lockDrawers(true);
    }


    @Override
    public void onStart() {
        super.onStart();


        fillData();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = getActivity().isChangingConfigurations();

        if (!drawerOpen) { // if drawer is closed add the content menu item

        }

        if (menu.findItem(R.id.action_user_data_edit) != null)
            menu.removeItem(R.id.action_user_data_edit);

        super.onPrepareOptionsMenu(menu);
    }

    public void updateEditTexts() {

    }

    public ServerResponse<Boolean> updateUser(CurrentUser user) throws ServerResponseFailedException {
        RequestsInterface requestsInterface = new RequestsInterface(getActivity().getApplicationContext());

        //ServerResponse<Boolean> sr = requestsInterface.updateUserDataBasic(user);
        ServerResponse<Boolean> sr = new ServerResponse<>(false, "Account update not implemented yet", null);
        return sr;
    }

    public boolean validData() {
        if (etEmail.getText().toString().length() == 0)
            validEmailSyntax = false;

        if (etPassword.getText().toString().length() == 0)
            validPasswordSyntax = false;

        return validEmailSyntax && validPasswordSyntax && validRetypePassword;
    }

    public void showValidationError() {
        if (!validEmailSyntax) {
            btnValidEmailSyntax.setVisibility(View.VISIBLE);
            btnValidEmailSyntax.callOnClick();
        } else if (!validPasswordSyntax) {
            btnValidPasswordSyntax.setVisibility(View.VISIBLE);
            btnValidPasswordSyntax.callOnClick();
        } else if (!validRetypePassword) {
            btnValidPasswordSyntax.setVisibility(View.VISIBLE);
            btnValidRetypePasswordSyntax.callOnClick();
        }
    }

}
