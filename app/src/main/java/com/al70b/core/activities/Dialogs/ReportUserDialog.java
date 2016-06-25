package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 7/1/2015.
 * This is an alert with a title: please wait
 * a progress bar on the right side and a var message to the left of the progress bar
 * no buttons or icons
 */
public class ReportUserDialog extends Dialog {

    private Context context;
    private CurrentUser user;
    private int otherUserID;
    private String otherUserName, thumbnailPath;
    private WindowManager.LayoutParams lp;

    public ReportUserDialog(Context context) {
        super(context);

        this.context = context;
    }

    public ReportUserDialog(Context context, CurrentUser user, OtherUser otherUser) {
        this(context);

        this.user = user;
        this.otherUserID = otherUser.getUserID();
        this.otherUserName = otherUser.getName();
        this.thumbnailPath = otherUser.getProfilePicture().getThumbnailFullPath();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_report_user);
        TextView txtViewName = (TextView) findViewById(R.id.text_view_alert_report_user_name);
        CircleImageView imgView = (CircleImageView) findViewById(R.id.circle_image_alert_report_user);
        final EditText editText = (EditText) findViewById(R.id.edit_text_alert_report_user_message);
        Button btnSend = (Button) findViewById(R.id.btn_alert_report_user_positive);
        Button btnCancel = (Button) findViewById(R.id.btn_alert_report_user_negative);

        txtViewName.setText(otherUserName);

        Glide.with(context)
                .load(thumbnailPath)
                .asBitmap()
                .fitCenter()
                .centerCrop()
                .placeholder(R.drawable.default_user_photo)
                .into(imgView);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editText.getText().toString().trim();

                if (!message.isEmpty()) {

                    try {
                        message = message.replace("\n", URLEncoder.encode("\n", "UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        message = editText.getText().toString().trim();
                    }

                    RequestsInterface requestsInterface = new RequestsInterface(context);

                    try {
                        ServerResponse<JSONObject> sr = requestsInterface.reportUser(user, otherUserID, message);

                        if (sr.isSuccess()) {
                            Toast.makeText(context, context.getString(R.string.report_user_was_successfull, otherUserName), Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_couldnt_report), Toast.LENGTH_SHORT).show();
                        }
                    } catch (ServerResponseFailedException ex) {
                        Toast.makeText(context, context.getString(R.string.error_server_connection_falied), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.error_report_cant_be_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });

        lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.horizontalMargin = (int) (12 / Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void show() {
        super.show();

        getWindow().setAttributes(lp);
    }

}
