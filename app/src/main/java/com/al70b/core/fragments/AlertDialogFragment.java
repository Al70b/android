package com.al70b.core.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;

/**
 * Created by Naseem on 5/11/2015.
 */
public class AlertDialogFragment extends DialogFragment {


    private static Type type;
    private String title, info;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_alert_dialog, container, false);

        title = getArguments().getString("Title");
        info = getArguments().getString("Info");
        type = (Type) getArguments().get("Type");

        getDialog().setTitle(title);
        this.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        LinearLayout layout = (LinearLayout) root.findViewById(R.id.layout_alert_dialog);
        TextView textView = (TextView) root.findViewById(R.id.text_view_alert_dialog_title);
        Button btnOk = (Button) root.findViewById(R.id.btn_alert_dialog_ok);

        textView.setText(info);

        switch (type) {
            case OK: {
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Just Ok was clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getResources().getDisplayMetrics()), LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                btnOk.setLayoutParams(params);

                break;
            }
            case OK_CANCEL:
                Button btnCancel = new Button(getActivity());
                btnCancel.setText(getResources().getString(R.string.button_cancel));

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Ok was clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Cancel was clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT, 1.0f);

                btnOk.setLayoutParams(params);
                btnCancel.setLayoutParams(params);

                layout.addView(btnCancel);
                break;
        }

        return root;
    }


    public enum Type {
        OK_CANCEL, OK;
    }

    /*@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Dialog title")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .create();
    }*/
}
