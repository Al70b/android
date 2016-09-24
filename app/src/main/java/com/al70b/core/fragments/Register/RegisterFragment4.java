package com.al70b.core.fragments.Register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.RegisterActivity;
import com.al70b.core.activities.TermsActivity;
import com.al70b.core.extended_widgets.ClearableEditText;
import com.al70b.core.misc.StringManp;
import com.al70b.core.objects.CurrentUser;

/**
 * Created by Naseem on 6/30/2015.
 */
public class RegisterFragment4 extends Fragment {

    private static String emailRetrieve;
    private boolean validEmailUsage, validEmailSyntax, validPasswordSyntax, validRetypePassword, emailAlreadyExists;
    private ClearableEditText clearableEmail, clearablePassword, clearableRetypePassword;
    private String prevEmailInput;
    private boolean acceptedTermsOfUse;
    private boolean nextClicked;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        final RegisterActivity activity = (RegisterActivity) getActivity();

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_general_register_4, container, false);
        clearableEmail = (ClearableEditText) viewGroup.findViewById(R.id.clearable_edit_text_register_email);
        clearablePassword = (ClearableEditText) viewGroup.findViewById(R.id.clearable_edit_text_register_password);
        clearableRetypePassword = (ClearableEditText) viewGroup.findViewById(R.id.clearable_edit_text_register_retype_password);
        final ImageButton btnValidEmailSyntax = (ImageButton) viewGroup.findViewById(R.id.btn_register_invalid_email_syntax);
        final ImageButton btnValidPasswordSyntax = (ImageButton) viewGroup.findViewById(R.id.btn_register_invalid_password_syntax);
        final ImageButton btnValidRetypePassword = (ImageButton) viewGroup.findViewById(R.id.btn_register_invalid_retype_password_syntax);
        final CheckBox chkBoxAcceptTermsOfUse = (CheckBox) viewGroup.findViewById(R.id.check_box_register_accept_terms_of_use);
        final Button btnNext = (Button) viewGroup.findViewById(R.id.btn_register_next_5);
        Button btnPrev = (Button) viewGroup.findViewById(R.id.btn_register_prev_5);

        String termsOfUse = getString(R.string.terms_of_use_and_privacy);
        String str = getString(R.string.accept_terms_of_use_message, termsOfUse);

        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getActivity(), TermsActivity.class);
                startActivity(intent);
            }
        }, str.indexOf(termsOfUse), str.indexOf(termsOfUse) + termsOfUse.length(),
                0);

        chkBoxAcceptTermsOfUse.setMovementMethod(LinkMovementMethod.getInstance());
        chkBoxAcceptTermsOfUse.setText(spannableString);
        chkBoxAcceptTermsOfUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                acceptedTermsOfUse = isChecked;
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide soft keyboard
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(clearableEmail.getWindowToken(), 0);

                emailRetrieve = clearableEmail.getEditText().getText().toString();

                activity.getSupportFragmentManager()
                        .popBackStack();
                activity.currentStep--;
                activity.updateTitle();
            }
        });

        clearablePassword.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());
        clearableRetypePassword.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());

        // set hint for clearable edit text
        clearableEmail.setEditTextHint(R.string.edTxtEmail);
        clearablePassword.setEditTextHint(R.string.password);
        clearableRetypePassword.setEditTextHint(R.string.retype_password);

        clearableEmail.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (prevEmailInput != null) {
                    emailAlreadyExists = prevEmailInput.compareTo(s.toString()) == 0;
                }

                emailRetrieve = clearableEmail.getEditText().getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validEmailSyntax = StringManp.isValidEmail(s.toString());
                validEmailUsage = validEmailSyntax && !emailAlreadyExists;

                if (s.length() == 0)
                    btnValidEmailSyntax.setVisibility(View.INVISIBLE);
                else {
                    if (validEmailUsage) {
                        btnValidEmailSyntax.setImageResource(R.drawable.green_check);
                    } else {
                        btnValidEmailSyntax.setImageResource(R.drawable.attention_red_icon);
                    }

                    // show the email validation button
                    btnValidEmailSyntax.setVisibility(View.VISIBLE);
                }
            }
        });

        clearablePassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (btnValidRetypePassword.getVisibility() == View.VISIBLE) {
                    btnValidRetypePassword.setVisibility(View.INVISIBLE);
                    clearableRetypePassword.getEditText().setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validPasswordSyntax = StringManp.isPasswordValid(s.toString());

                if (s.length() == 0) {
                    btnValidPasswordSyntax.setVisibility(View.INVISIBLE);
                    btnValidRetypePassword.setImageResource(R.drawable.attention_red_icon);
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

        clearableRetypePassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validRetypePassword = clearablePassword.getEditText().getText().toString().compareTo(s.toString()) == 0;

                if (s.length() == 0)
                    btnValidRetypePassword.setVisibility(View.INVISIBLE);
                else {

                    if (validRetypePassword) {
                        btnValidRetypePassword.setImageResource(R.drawable.green_check);
                    } else {
                        btnValidRetypePassword.setImageResource(R.drawable.attention_red_icon);
                    }

                    // show the button
                    btnValidRetypePassword.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validEmailUsage && validPasswordSyntax && validRetypePassword && acceptedTermsOfUse) {

                    // hide keyboard
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(clearableEmail.getWindowToken(), 0);

                    nextClicked = true;

                    // set email and password
                    activity.registerEmailAndPassword(clearableEmail.getText().toString(),
                            clearablePassword.getText().toString());
                } else {
                    String message = null;

                    if (clearableEmail.getEditText().getText().length() == 0) {
                        message = getString(R.string.error_please_enter_email);
                    } else if (!validEmailSyntax) {
                        message = getString(R.string.error_please_enter_valid_email);
                    } else if (!validEmailUsage) {
                        message = getString(R.string.error_email_already_exists);
                    } else if (clearablePassword.getEditText().getText().length() == 0) {
                        message = getString(R.string.error_please_enter_password);
                    } else if (!validPasswordSyntax) {
                        message = getString(R.string.error_please_enter_valid_email);
                    } else if (clearableRetypePassword.getEditText().getText().length() == 0) {
                        message = getString(R.string.error_please_enter_retyped_password);
                    } else if (!validRetypePassword) {
                        message = getString(R.string.error_passwords_dont_match);
                    } else if (!acceptedTermsOfUse) {
                        message = getString(R.string.you_have_to_accept_terms_of_use);
                    }

                    if (message != null) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("Email")) {
            prevEmailInput = bundle.getString("Email");
            clearableEmail.getEditText().setText(prevEmailInput);
            emailAlreadyExists = true;
            btnValidEmailSyntax.setImageResource(R.drawable.attention_red_icon);
        }

        // retrieve value of email if back was pressed
        if (emailRetrieve != null) {
            clearableEmail.getEditText().setText(emailRetrieve);
            emailRetrieve = null;
        }

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}
