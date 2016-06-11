package com.al70b.core.extended_widgets;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.al70b.R;

/**
 * Created by Naseem on 4/28/2015.
 */
public class ClearableEditText extends RelativeLayout {
    LayoutInflater inflater = null;
    EditText edit_text;
    Button btn_clear;
    String edit_text_hint;

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ClearableEditText(Context context) {
        super(context);
        initViews();
    }

    void initViews() {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.clearable_edit_text, this, true);
        edit_text = (EditText) findViewById(R.id.clearable_edit);
        btn_clear = (Button) findViewById(R.id.clearable_button_clear);
        btn_clear.setVisibility(RelativeLayout.INVISIBLE);

        edit_text.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
               /*if(hasFocus)
                    edit_text.setHint("");
                else if(edit_text_hint != null)
                    edit_text.setHint(edit_text_hint);*/
            }
        });

        clearText();
        showHideClearButton();
    }

    void clearText() {
        btn_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_text.setText("");
                edit_text.requestFocus();
            }
        });
    }

    void showHideClearButton() {
        edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    btn_clear.setVisibility(RelativeLayout.VISIBLE);
                else
                    btn_clear.setVisibility(RelativeLayout.INVISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public EditText getEditText() {
        return edit_text;
    }

    public void setEditTextHint(int res_id) {
        String hint = getResources().getString(res_id);
        edit_text.setHint(edit_text_hint = hint);
    }

    public Editable getText() {
        return edit_text.getText();
    }
}
