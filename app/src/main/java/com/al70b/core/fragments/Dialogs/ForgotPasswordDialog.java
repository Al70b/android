package com.al70b.core.fragments.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.ClearableEditText;
import com.al70b.core.misc.StringManp;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

/**
 * Created by Naseem on 6/14/2016.
 */
public class ForgotPasswordDialog extends Dialog {

    private Context context;
    private String passedEmail;
    private Dialog thisDialog;
    private boolean validEmailSyntaxDialog;
    private ClearableEditText emailClearableEditTextDialog;

    public ForgotPasswordDialog(Context context) {
        super(context);
        this.context = context;
    }

    public ForgotPasswordDialog(Context context, String passedEmail) {
        this(context);
        this.passedEmail = passedEmail;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisDialog = this;

        // Dialog properties
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.alert_forgot_password);

        emailClearableEditTextDialog = (ClearableEditText)findViewById(
                R.id.dialog_forgot_password_clearable_edit_text);
        final ImageButton validEmailInDialog = (ImageButton) findViewById(
                R.id.dialog_forgot_password_img_btn_invalid_email_syntax);
        final Button btnCancel = (Button) findViewById(R.id.dialog_cancel);
        final Button btnOk = (Button) findViewById(R.id.dialog_ok);

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
                validEmailSyntaxDialog = StringManp.isValidEmail(s.toString());

                if (s.length() == 0) {
                    validEmailInDialog.setVisibility(View.INVISIBLE);
                } else {
                    if (validEmailSyntaxDialog)
                        validEmailInDialog.setImageResource(R.drawable.green_check);
                    else
                        validEmailInDialog.setImageResource(R.drawable.attention_red_icon);

                    // show the email validation button
                    validEmailInDialog.setVisibility(View.VISIBLE);
                }
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailClearableEditTextDialog.getEditText().getText().toString();

                if(email.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.need_to_provide_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (validEmailSyntaxDialog) {
                    // Disable buttons
                    btnOk.setEnabled(false);
                    btnCancel.setEnabled(false);
                    thisDialog.setCanceledOnTouchOutside(false);

                    RequestsInterface requestsInterface = new RequestsInterface(context);

                    String msg;
                    try {
                        ServerResponse<String> sr = requestsInterface.forgotPassword(email);

                        if (sr.isSuccess()) {
                            msg = context.getString(R.string.email_was_sent_with_password);
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                            thisDialog.dismiss();
                        } else {
                            msg = sr.getErrorMsg();
                        }
                    } catch (ServerResponseFailedException ex) {
                        msg = ex.toString();
                    }

                    // re-enable buttons
                    btnOk.setEnabled(true);
                    btnCancel.setEnabled(true);
                    thisDialog.setCanceledOnTouchOutside(true);

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.error_please_enter_valid_email),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisDialog.dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();

        if (emailClearableEditTextDialog != null && passedEmail != null)
            emailClearableEditTextDialog.getEditText().setText(passedEmail);
    }

    public void openWithEmail(String email) {
        passedEmail = email;
    }



}
