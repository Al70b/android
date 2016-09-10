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
import com.al70b.core.activities.user_home_activity_underlying.ChatHandler;
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
public class BlockUserDialog extends Dialog {

    private Context context;
    private CurrentUser user;
    private OtherUser otherUser;
    private WindowManager.LayoutParams lp;
    private ChatHandler chatHandler;

    public BlockUserDialog(Context context, CurrentUser user, OtherUser otherUser,
                           ChatHandler chatHandler) {
        super(context);
        this.context = context;
        this.user = user;
        this.otherUser = otherUser;
        this.chatHandler = chatHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_block_user);
        TextView txtViewName = (TextView) findViewById(R.id.text_view_block_user_dialog_title);
        CircleImageView imgView = (CircleImageView) findViewById(R.id.image_view_block_user_icon);
        Button btnSend = (Button) findViewById(R.id.btn_alert_block_user_positive);
        Button btnCancel = (Button) findViewById(R.id.btn_alert_block_user_negative);

        txtViewName.setText(context.getString(R.string.block_the_user, otherUser.getName()));

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
                chatHandler.blockUser(otherUser);
                dismiss();
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
