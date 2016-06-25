package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.al70b.R;

/**
 * Created by Naseem on 7/1/2015.
 * This is an alert with a title: please wait
 * a progress bar on the right side and a var message to the left of the progress bar
 * no buttons or icons
 */
public class DisplayInfoAlert extends Dialog {

    private Context context;
    private String title, message;
    private int icon = -1;
    private WindowManager.LayoutParams lp;
    private Button btnOK;

    public DisplayInfoAlert(Context context) {
        super(context);

        this.context = context;
    }

    public DisplayInfoAlert(Context context, String title, String message) {
        this(context);

        this.title = title;
        this.message = message;
    }

    public DisplayInfoAlert(Context context, String title, String message, int icon) {
        this(context, title, message);

        this.icon = icon;
    }

    public DisplayInfoAlert(Context context, int titleID, int stringID) {
        this(context, context.getString(titleID), context.getString(stringID));
    }

    public DisplayInfoAlert(Context context, int titleID, int stringID, int icon) {
        this(context, context.getString(titleID), context.getString(stringID), icon);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_display_info);
        TextView txtViewTitle = (TextView) findViewById(R.id.text_view_alert_display_info_title);
        TextView txtViewMessage = (TextView) findViewById(R.id.text_view_alert_display_info_message);
        ImageView imgView = (ImageView) findViewById(R.id.image_view_alert_display_info_icon);
        btnOK = (Button) findViewById(R.id.btn_alert_display_info_ok);

        if (icon != -1)
            imgView.setImageResource(icon);

        txtViewTitle.setText(title);
        txtViewMessage.setText(message);

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

    public void setButtonOkFunction(View.OnClickListener l) {
        btnOK.setOnClickListener(l);
    }

}
