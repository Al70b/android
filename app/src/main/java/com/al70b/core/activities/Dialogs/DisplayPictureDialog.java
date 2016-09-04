package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.misc.Utils;
import com.al70b.core.objects.OtherUser;
import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

/**
 * Created by Naseem on 7/11/2015.
 */
public class DisplayPictureDialog extends Dialog {

    private static final String TAG = "DisplayPictureDialog";

    private String path;
    private Context context;
    private OtherUser otherUser;

    public DisplayPictureDialog(Context context, String path) {
        super(context);
        this.context = context;
        this.path = path;
    }

    public DisplayPictureDialog(Context context, OtherUser otherUser) {
        super(context);
        this.context = context;
        this.otherUser = otherUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_display_picture);
        ImageView imgView = (ImageView) findViewById(R.id.img_view_display_picture_dialog);
        TextView txtViewName = (TextView) findViewById(R.id.tv_displayPictureD_username);
        ImageButton imgBtnClose = (ImageButton) findViewById(R.id.img_btn_displayPictureD_close);

        if(otherUser != null) {
            txtViewName.setText(otherUser.getName());
            path = otherUser.getProfilePictureThumbnailPath();
        }

        Glide.with(context)
                .load(path)
                .asBitmap()
                .placeholder(R.drawable.avatar)
                .into(imgView);
        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.dimAmount = 0.2f;
        lp.gravity = Gravity.CENTER;

        int widthInDP = (int)context.getResources().getDisplayMetrics().xdpi;
        lp.width = (int)Utils.convertDpToPixel(widthInDP - 70, context);
        lp.height = (int)Utils.convertDpToPixel(widthInDP - 50, context);
        lp.y = -1 * (int)Utils.convertDpToPixel(24, context);
        getWindow().setAttributes(lp);
    }
}
