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
import com.al70b.core.objects.OtherUser;
import com.al70b.core.server_methods.ServerConstants;
import com.bumptech.glide.Glide;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.interfaces.Callbacks;

import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 7/1/2015.
 * This is an alert with a title: please wait
 * a progress bar on the right side and a var message to the left of the progress bar
 * no buttons or icons
 */
public class SendMessageDialog extends Dialog {

    private Context context;
    private int otherUserID;
    private String otherUserName, thumbnailPath;
    private WindowManager.LayoutParams lp;

    public SendMessageDialog(Context context) {
        super(context);

        this.context = context;
    }

    public SendMessageDialog(Context context, OtherUser otherUser) {
        this(context);

        this.otherUserID = otherUser.getUserID();
        this.otherUserName = otherUser.getName();
        this.thumbnailPath = otherUser.getProfilePictureThumbnailPath();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_send_message);
        TextView txtViewName = (TextView) findViewById(R.id.text_view_alert_send_message_name);
        CircleImageView imgView = (CircleImageView) findViewById(R.id.circle_image_alert_send_message);
        final EditText editText = (EditText) findViewById(R.id.edit_text_alert_send_message_message);
        Button btnSend = (Button) findViewById(R.id.btn_alert_send_message_positive);
        Button btnCancel = (Button) findViewById(R.id.btn_alert_send_message_negative);

        txtViewName.setText(otherUserName);

        Glide.with(context)
                .load(thumbnailPath)
                .asBitmap()
                .placeholder(R.drawable.avatar)
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
                    CometChat chat = CometChat.getInstance(context,
                            ServerConstants.CONSTANTS.COMET_CHAT_API_KEY);

                    if (chat.isLoggedIn()) {
                        chat.sendMessage(String.valueOf(otherUserID), message, new Callbacks() {
                            @Override
                            public void successCallback(JSONObject jsonObject) {
                                dismiss();

                                Toast.makeText(context, context.getString(R.string.message_was_sent, otherUserName), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failCallback(JSONObject jsonObject) {
                                Toast.makeText(context, context.getString(R.string.error_couldnt_send_message), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.error_empty_message), Toast.LENGTH_SHORT).show();
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
