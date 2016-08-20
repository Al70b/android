package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.misc.Utils;
import com.bumptech.glide.Glide;

/**
 * Created by Naseem on 7/11/2015.
 */
public class DisplayPictureDialog extends Dialog {

    private static final String TAG = "DisplayPictureDialog";
    public static final String THUMBNAIL_KEY = "DisplayPictureDialog.THUMBNAIL";

    private final String path;
    private Context context;

    public DisplayPictureDialog(Context context, String path) {
        super(context);
        this.context = context;
        this.path = path;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_display_picture);
        ImageView imgView = (ImageView) findViewById(R.id.img_view_display_picture_dialog);

        Glide.with(context)
                .load(path)
                .asBitmap()
                .fitCenter()
                .into(imgView);
    }

    @Override
    public void show() {
        super.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.dimAmount = 0.3f;
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.width = (int)Utils.convertDpToPixel(320, context);
        lp.height = (int)Utils.convertDpToPixel(360, context);
        lp.horizontalMargin = (int)Utils.convertDpToPixel(12, context);
        getWindow().setAttributes(lp);
    }
}
