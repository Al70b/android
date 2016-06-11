package com.al70b.core.fragments.Register;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.extended_widgets.ClearableEditText;
import com.al70b.core.fragments.RegisterFragment;

/**
 * Created by Naseem on 6/30/2015.
 */
public class RegisterFragment1 extends Fragment {

    private static String nameRetrieve;
    private boolean validName;
    private ClearableEditText clearableName;
    private ImageButton btnEmptyName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_register_1, container, false);

        clearableName = (ClearableEditText) viewGroup.findViewById(R.id.clearable_edit_text_register_name);
        btnEmptyName = (ImageButton) viewGroup.findViewById(R.id.btn_register_empty_name);
        Button btnNext = (Button) viewGroup.findViewById(R.id.btn_register_next_1);
        Button btnPrev = (Button) viewGroup.findViewById(R.id.btn_register_prev_1);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide soft keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(clearableName.getWindowToken(), 0);

                nameRetrieve = clearableName.getEditText().getText().toString();

                getActivity().getSupportFragmentManager()
                        .popBackStack();
            }
        });

        clearableName.setEditTextHint(R.string.register_name);

        clearableName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validName = s.toString().trim().length() > 1;

                if (s.length() == 0)
                    btnEmptyName.setVisibility(View.INVISIBLE);
                else {
                    if (validName)
                        btnEmptyName.setImageResource(R.drawable.green_check);
                    else
                        btnEmptyName.setImageResource(R.drawable.attention_red_icon);

                    // show the name validation button
                    btnEmptyName.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validName) {
                    String name = clearableName.getEditText().getText().toString();
                    RegisterFragment.getRegisteringUser().setName(name);

                    RegisterFragment.pickFragment(new RegisterFragment2(), true);
                } else {
                    Toast.makeText(getActivity(), R.string.error_fill_your_name, Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearableName.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});

        if (nameRetrieve != null) {
            clearableName.getEditText().setText(nameRetrieve);
            nameRetrieve = null;
        }

        return viewGroup;
    }
}
