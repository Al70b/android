package com.al70b.core.fragments.Register;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.RegisterActivity;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.User;
import com.al70b.core.objects.UserInterest;

import java.util.Calendar;

/**
 * Created by Naseem on 6/30/2015.
 */
public class RegisterFragment3 extends Fragment {

    private static Boolean genderMaleRetrieve;
    private static Calendar birthdateRetrieve;
    private static Boolean genderInterestMaleRetrieve;

    private boolean validBirthDate;
    private TextView txtViewBirthDate;
    private RadioGroup rd;
    private RadioGroup rdInterest;

    // hold user's birth date
    private Calendar birthDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final RegisterActivity activity = (RegisterActivity) getActivity();

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_general_register_3, container, false);
        txtViewBirthDate = (TextView) viewGroup.findViewById(R.id.text_view_register_birthdate_pick);
        rd = (RadioGroup) viewGroup.findViewById(R.id.radio_group_register_gender);
        rdInterest = (RadioGroup) viewGroup.findViewById(R.id.radio_group_register_gender_interest);
        Button btnNext = (Button) viewGroup.findViewById(R.id.btn_register_next_3);
        Button btnPrev = (Button) viewGroup.findViewById(R.id.btn_register_prev_3);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getSupportFragmentManager()
                        .popBackStack();
                activity.currentStep--;
                activity.updateTitle();
            }
        });

        // default
        ((RadioButton) rd.getChildAt(0)).setChecked(true);
        // default
        ((RadioButton) rdInterest.getChildAt(1)).setChecked(true);

        rd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                genderMaleRetrieve = rd.getCheckedRadioButtonId() == R.id.radiobutton_register_gender_male;
            }
        });

        rdInterest.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                genderInterestMaleRetrieve = rdInterest.getCheckedRadioButtonId() == R.id.radiobutton_register_gender_interest_male;
            }
        });

        // set on click listener for both the button and the text view
        txtViewBirthDate.setClickable(true);
        txtViewBirthDate.setOnClickListener(new OnBirthDateClickListener(txtViewBirthDate));

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validBirthDate) {
                    User.Gender gender;
                    User.Gender interestGender;
                    UserInterest userInterest;

                    // user gender
                    if (rd.getCheckedRadioButtonId() == R.id.radiobutton_register_gender_male) {
                        gender = new User.Gender(CurrentUser.Gender.MALE);
                    } else {
                        gender = new User.Gender(CurrentUser.Gender.FEMALE);
                    }

                    // user gender interest
                    if (rdInterest.getCheckedRadioButtonId() ==
                            R.id.radiobutton_register_gender_interest_male) {
                        interestGender = new User.Gender(CurrentUser.Gender.MALE);
                    } else {
                        interestGender = new User.Gender(CurrentUser.Gender.FEMALE);
                    }

                    // create user interest object
                    userInterest = new UserInterest(interestGender);
                    activity.register3Fragment(gender, birthDate, userInterest);
                } else {
                    String message = getString(R.string.error_choose_date);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(genderMaleRetrieve != null) {
            if (genderMaleRetrieve)
                rd.check(R.id.radiobutton_register_gender_male);
            else
                rd.check(R.id.radiobutton_register_gender_female);
        }

        if(genderInterestMaleRetrieve != null) {
            if (genderInterestMaleRetrieve)
                rdInterest.check(R.id.radiobutton_register_gender_interest_male);
            else
                rdInterest.check(R.id.radiobutton_register_gender_interest_female);
        }

        if(birthdateRetrieve != null) {
            txtViewBirthDate.setText(getDateAsString(birthdateRetrieve));
            validBirthDate = true;
        } else {
            txtViewBirthDate.setText(R.string.register_choose_date);
        }
        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private String getDateAsString(Calendar c) {
        if (c == null) {
            return "";
        }

        return String.format("%d/%d/%d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }


    private class OnBirthDateClickListener implements View.OnClickListener,
            DatePickerDialog.OnDateSetListener {

        private TextView txtViewToChange;

        public OnBirthDateClickListener(TextView txtView) {
            this.txtViewToChange = txtView;
        }

        @Override
        public void onClick(View view) {
            OnBirthDateClickListener thisListener = this;

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

            year = c.get(Calendar.YEAR) - AppConstants.MIN_MEMBER_AGE;
            day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    thisListener, year, month, day);
            datePickerDialog.show();
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if ((Calendar.getInstance()).get(Calendar.YEAR) - year < 18) {// user should be 18+
                Toast.makeText(getActivity(), getResources().getString(R.string.error_should_be_above_18), Toast.LENGTH_SHORT).show();
                txtViewToChange.setText(getString(R.string.register_choose_date));
                validBirthDate = false;
                birthdateRetrieve = null;
            } else {
                if (birthDate == null) {
                    birthDate = Calendar.getInstance();
                }

                birthDate.set(year, month, day);

                txtViewToChange.setText(getDateAsString(birthDate));

                validBirthDate = true;
                birthdateRetrieve = birthDate;
            }
        }
    }
}
