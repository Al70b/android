package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.al70b.R;

/**
 * Created by Naseem on 7/1/2015.
 * This is an alert with a title: please wait
 * a progress bar on the right side and a var message to the left of the progress bar
 * no buttons or icons
 */
public class QuestionAlert2 extends Dialog {

    private Context context;
    private String title, message;
    private int positive, negative;
    private int icon = -1;
    private WindowManager.LayoutParams lp;
    private Button btnPositive, btnNegative;
    private CheckBox chkBoxDonot;

    public QuestionAlert2(Context context) {
        super(context);

        this.context = context;
    }

    public QuestionAlert2(Context context, String title, String message) {
        this(context);

        this.title = title;
        this.message = message;
    }

    public QuestionAlert2(Context context, String title, String message, int icon) {
        this(context, title, message);

        this.icon = icon;
    }

    public QuestionAlert2(Context context, String title, String message, int icon, int positive, int negative) {
        this(context, title, message);

        this.positive = positive;
        this.negative = negative;
        this.icon = icon;
    }

    public QuestionAlert2(Context context, int titleID, int stringID) {
        this(context, context.getString(titleID), context.getString(stringID));
    }

    public QuestionAlert2(Context context, int titleID, int stringID, int icon) {
        this(context, context.getString(titleID), context.getString(stringID), icon);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.alert_question2);
        TextView txtViewTitle = (TextView) findViewById(R.id.text_view_dialog_question2_title);
        TextView txtViewMessage = (TextView) findViewById(R.id.text_view_dialog_question_message);
        ImageView imgView = (ImageView) findViewById(R.id.image_view_alert_question2_icon);
        chkBoxDonot = (CheckBox) findViewById(R.id.chk_box_dialog_question_dont_ask_me);
        btnPositive = (Button) findViewById(R.id.btn_dialog_question_positive);
        btnNegative = (Button) findViewById(R.id.btn_dialog_question_negative);

        if (icon != -1)
            imgView.setImageResource(icon);

        txtViewTitle.setText(title);
        txtViewMessage.setText(message);
        btnPositive.setText(positive);
        btnNegative.setText(negative);

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

    public void setPositiveButton(View.OnClickListener l) {
        if (btnPositive != null)
            btnPositive.setOnClickListener(l);
    }

    public void setNegativeButton(View.OnClickListener l) {
        if (btnNegative != null)
            btnNegative.setOnClickListener(l);
    }

    public boolean dontAskMeChecked() {
        return chkBoxDonot.isChecked();
    }

}
