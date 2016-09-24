package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
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
import com.al70b.core.server_methods.ServerConstants;
import com.bumptech.glide.Glide;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.interfaces.Callbacks;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.hdodenhof.circleimageview.CircleImageView;
import fm.icelink.Server;

/**
 * Created by Naseem on 7/1/2015.
 * This is an alert with a title: please wait
 * a progress bar on the right side and a var message to the left of the progress bar
 * no buttons or icons
 */
public class QuickUserMessageDialog extends Dialog {

    private static final String TAG = QuickUserMessageDialog.class.getSimpleName();
    private Context context;
    private CurrentUser currentUser;
    private OtherUser otherUser;
    private WindowManager.LayoutParams lp;

    public QuickUserMessageDialog(Context context, CurrentUser user, OtherUser otherUser) {
        super(context);
        this.context = context;
        this.currentUser = user;
        this.otherUser = otherUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_quick_user_message);
        TextView txtViewName = (TextView) findViewById(R.id.text_view_quick_user_message_dialog_title);
        CircleImageView imgView = (CircleImageView) findViewById(R.id.image_view_quick_user_message_icon);
        final EditText editText = (EditText) findViewById(R.id.edit_text_alert_quick_user_message_message);
        Button btnSend = (Button) findViewById(R.id.btn_alert_quick_user_message_positive);
        Button btnCancel = (Button) findViewById(R.id.btn_alert_quick_user_message_negative);

        txtViewName.setText(context.getString(R.string.send_message_to, otherUser.getName()));

        Glide.with(context)
                .load(otherUser.getProfilePictureThumbnailPath())
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

                    if(otherUser.getFriendStatus().isFriend()) {
                        CometChat chat = CometChat.getInstance(context,
                                ServerConstants.CONSTANTS.COMET_CHAT_API_KEY);

                        chat.sendMessage(String.valueOf(otherUser.getUserID()), message, new Callbacks() {
                            @Override
                            public void successCallback(JSONObject jsonObject) {
                                dismiss();

                                Toast.makeText(context,
                                        context.getString(R.string.message_was_sent, otherUser.getName()),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failCallback(JSONObject jsonObject) {
                                Toast.makeText(context,
                                        context.getString(R.string.error_couldnt_send_message),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        try {
                            ServerResponse sr = new RequestsInterface(context)
                                    .sendMessage(currentUser, otherUser.getUserID(), message);

                            if (sr.isSuccess()) {
                                dismiss();

                                Toast.makeText(context,
                                        context.getString(R.string.message_was_sent, otherUser.getName()),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context,
                                        context.getString(R.string.error_couldnt_send_message),
                                        Toast.LENGTH_SHORT).show();
                                Log.e(TAG, sr.getErrorMsg());
                            }
                        } catch(ServerResponseFailedException ex) {
                            Log.e(TAG, ex.toString());
                        }
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
