package com.al70b.core.fragments.Register;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.fragments.RegisterFragment;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.User;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Naseem on 6/30/2015.
 */
public class RegisterFragment3 extends Fragment {

    private static Boolean genderMale;
    private static String socialStatusRetrieve;
    private static Calendar birthdateRetrieve;
    private boolean validSocialStatus, validBirthDate;
    private TextView txtViewBirthDate;
    private RadioGroup rd;
    private Spinner spinnerSocial;
    private Translator translator;
    // hold user's birth date
    private Calendar birthDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        translator = Translator.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_register_3, container, false);
        txtViewBirthDate = (TextView) viewGroup.findViewById(R.id.text_view_register_birthdate_pick);
        rd = (RadioGroup) viewGroup.findViewById(R.id.radio_group_register_gender);
        spinnerSocial = (Spinner) viewGroup.findViewById(R.id.spinner_register_social_status);
        Button btnNext = (Button) viewGroup.findViewById(R.id.btn_register_next_3);
        Button btnPrev = (Button) viewGroup.findViewById(R.id.btn_register_prev_3);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (rd.getCheckedRadioButtonId() == R.id.radiobutton_register_gender_male)
                    genderMale = true;
                else
                    genderMale = false;

                socialStatusRetrieve = (String) spinnerSocial.getSelectedItem();

                birthdateRetrieve = birthDate;

                getActivity().getSupportFragmentManager()
                        .popBackStack();
            }
        });

        // default
        ((RadioButton) rd.getChildAt(0)).setChecked(true);

        // set on click listener for both the button and the text view
        txtViewBirthDate.setClickable(true);
        txtViewBirthDate.setOnClickListener(new OnBirthDateClickListener(txtViewBirthDate));

        ArrayList<String> listOfSocial = (ArrayList<String>) translator.getValues(translator.getDictionary().SOCIAL_STATUS, false);
        listOfSocial.add(0, getString(R.string.choose_from_list));
        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_list_item_1, listOfSocial);
        spinnerSocial.setAdapter(adapter);

        spinnerSocial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView adapterView) {

            }

            @Override
            public void onItemSelected(AdapterView adapterView, View view, int i, long j) {
                validSocialStatus = ((String) spinnerSocial.getSelectedItem())
                        .compareTo(getString(R.string.choose_from_list)) != 0;
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validSocialStatus && validBirthDate) {
                    CurrentUser.Gender gender;
                    String socialStatus;

                    CurrentUser user = RegisterFragment.getRegisteringUser();

                    // user gender
                    if (rd.getCheckedRadioButtonId() == R.id.radiobutton_register_gender_male)
                        gender = new User.Gender(CurrentUser.Gender.MALE);
                    else
                        gender = new User.Gender(CurrentUser.Gender.FEMALE);
                    user.setGender(gender);

                    // set birth date
                    user.setDateOfBirth(birthDate);

                    // set social status
                    socialStatus = (String) spinnerSocial.getSelectedItem();
                    user.setSocialStatus(socialStatus);


                    RegisterFragment.pickFragment(new RegisterFragment4(), true);
                } else {
                    String message = "";
                    if (!validSocialStatus)
                        message = getString(R.string.error_choose_social_status);
                    else if (!validBirthDate)
                        message = getString(R.string.error_choose_date);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (genderMale != null && socialStatusRetrieve != null && birthdateRetrieve != null) {
            if (genderMale)
                rd.check(R.id.radiobutton_register_gender_male);
            else
                rd.check(R.id.radiobutton_register_gender_female);

            spinnerSocial.setSelection(adapter.getPosition(socialStatusRetrieve));

            birthDate = birthdateRetrieve;
            if (birthDate == null)
                txtViewBirthDate.setText(R.string.register_choose_date);
            else {
                txtViewBirthDate.setText(birthDate.get(Calendar.YEAR) + "/"
                        + (birthDate.get(Calendar.MONTH) + 1) + "/"
                        + birthDate.get(Calendar.DAY_OF_MONTH));
                validBirthDate = true;
            }

            genderMale = null;
            socialStatusRetrieve = null;
            birthdateRetrieve = null;
        }

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (birthDate != null) {
            txtViewBirthDate.setText(birthDate.get(Calendar.YEAR) + "/"
                    + (birthDate.get(Calendar.MONTH) + 1) + "/"
                    + birthDate.get(Calendar.DAY_OF_MONTH));
            validBirthDate = true;
        }
    }


    private class OnBirthDateClickListener implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

        private TextView txtViewToChange;

        public OnBirthDateClickListener(TextView txtView) {
            this.txtViewToChange = txtView;
        }

        @Override
        public void onClick(View view) {

            final OnBirthDateClickListener thisListener = this;
            DialogFragment df = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    // Use the current date as the default date in the picker
                    int year, month, day;
                    Calendar c;

                    if (birthDate == null) {
                        c = Calendar.getInstance();
                        month = c.get(Calendar.MONTH);
                    } else {
                        c = birthDate;
                        month = c.get(Calendar.MONTH);
                    }

                    year = c.get(Calendar.YEAR);
                    day = c.get(Calendar.DAY_OF_MONTH);

                    // Create a new instance of DatePickerDialog and return it
                    return new DatePickerDialog(getActivity(), thisListener, year, month, day);
                }

            };
            df.show(getFragmentManager(), "datePicker");
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if ((Calendar.getInstance()).get(Calendar.YEAR) - year < 18) {// user should be 18+
                Toast.makeText(getActivity(), getResources().getString(R.string.error_should_be_above_18), Toast.LENGTH_SHORT).show();
                txtViewToChange.setText(getString(R.string.register_choose_date));
                validBirthDate = false;
            } else {
                if (birthDate == null)
                    birthDate = Calendar.getInstance();

                birthDate.set(year, month, day);
                txtViewToChange.setText(year + "/" + (month + 1) + "/" + day);

                validBirthDate = true;
            }
        }
    }
}
