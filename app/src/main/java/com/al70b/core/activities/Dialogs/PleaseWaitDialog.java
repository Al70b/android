package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.al70b.R;

/**
 * Created by Naseem on 7/1/2015.
 * This is an alert with a title: please wait
 * a progress bar on the right side and a var message to the left of the progress bar
 * no buttons or icons
 */
public class PleaseWaitDialog extends Dialog {

    private Context context;
    private String message;
    private WindowManager.LayoutParams lp;

    public PleaseWaitDialog(Context context) {
        super(context);

        this.context = context;
    }

    public PleaseWaitDialog(Context context, String message) {
        this(context);

        this.message = message;
    }

    public PleaseWaitDialog(Context context, int stringID) {
        this(context, context.getString(stringID));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_please_wait);
        TextView txtViewMessage = (TextView) findViewById(R.id.text_view_alert_please_wait_message);

        if (message == null)
            message = context.getString(R.string.wait_few_seconds);

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

}
