package com.al70b.core.fragments.Register;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.fragments.GuestRegisterFragment;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.User;
import com.al70b.core.objects.UserInterest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 6/30/2015.
 */
public class RegisterFragment4 extends Fragment {

    private static Boolean genderInterestMale;
    private static SparseArray<Boolean> interestPurposeRetrieve;
    private RadioGroup rd;
    private Translator translator;
    private boolean validChoices = true;
    // list of check boxes in the interested purpose table
    private SparseArray<CheckBox> listOfCheckBoxes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        translator = Translator.getInstance(getActivity().getApplicationContext());

        // if fragment is newly created
        if (genderInterestMale == null && interestPurposeRetrieve == null)
            listOfCheckBoxes = new SparseArray<CheckBox>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_register_4, container, false);
        rd = (RadioGroup) viewGroup.findViewById(R.id.radio_group_register_gender_interest);
        TableLayout tableLayout = (TableLayout) viewGroup.findViewById(R.id.table_layout_user_data_basic_interestedPurposes);
        Button btnNext = (Button) viewGroup.findViewById(R.id.btn_register_next_4);
        Button btnPrev = (Button) viewGroup.findViewById(R.id.btn_register_prev_4);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (rd.getCheckedRadioButtonId() == R.id.radiobutton_register_gender_interest_male)
                    genderInterestMale = true;
                else
                    genderInterestMale = false;

                interestPurposeRetrieve = new SparseArray<>();
                for (int i = 0; i < listOfCheckBoxes.size(); i++) {
                    CheckBox ch = listOfCheckBoxes.get(i);
                    interestPurposeRetrieve.put(ch.getId(), ch.isChecked());
                }

                getActivity().getSupportFragmentManager()
                        .popBackStack();
            }
        });

        // default
        ((RadioButton) rd.getChildAt(0)).setChecked(true);

        // build contents of table
        buildInterestedPurposeLayout(tableLayout);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean atLeastOneCheckBox = false;
                List<String> list = new ArrayList<>();


                for (int i = 0; i < listOfCheckBoxes.size(); i++) {
                    CheckBox ch = listOfCheckBoxes.get(i);
                    if (ch.isChecked()) {
                        atLeastOneCheckBox |= true;
                        list.add(ch.getText().toString());
                    }
                }

                validChoices = rd.getCheckedRadioButtonId() != -1
                        && atLeastOneCheckBox;

                if (validChoices) {
                    UserInterest userInterest;
                    CurrentUser.Gender gender;

                    // user gender interest
                    if (rd.getCheckedRadioButtonId() == R.id.radiobutton_register_gender_interest_male)
                        gender = new User.Gender(CurrentUser.Gender.MALE);
                    else
                        gender = new User.Gender(CurrentUser.Gender.FEMALE);

                    // create user interest object
                    userInterest = new UserInterest(gender, list);

                    // set user interest object
                    GuestRegisterFragment.getRegisteringUser().setUserInterest(userInterest);

                    GuestRegisterFragment.pickFragment(new RegisterFragment5(), true);
                } else {
                    Toast.makeText(getActivity(), R.string.error_choose_interest_purpose, Toast.LENGTH_SHORT).show();
                }
            }
        });


        if (genderInterestMale != null && interestPurposeRetrieve != null) {
            if (genderInterestMale)
                rd.check(R.id.radiobutton_register_gender_interest_male);
            else
                rd.check(R.id.radiobutton_register_gender_interest_female);

            for (int i = 0; i < listOfCheckBoxes.size(); i++) {
                CheckBox ch = listOfCheckBoxes.get(i);
                ch.setChecked(interestPurposeRetrieve.get(i));
            }

            genderInterestMale = null;
            interestPurposeRetrieve = null;
        }

        return viewGroup;
    }


    /**
     * build the table rows for the interested purpose, this way the table can grow or shrink
     * automatically depending only on the data from the server
     *
     * @param tableLayout
     */
    private void buildInterestedPurposeLayout(TableLayout tableLayout) {

        int dp5 = (int) (5 / Resources.getSystem().getDisplayMetrics().density);

        // create first row with appropriate layout
        TableRow tableRow = new TableRow(getActivity().getBaseContext());
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(rowParams);
        tableRow.setPadding(dp5, dp5, dp5, dp5);

        // add it to the table
        tableLayout.addView(tableRow);

        int i = 0;

        if (listOfCheckBoxes == null)
            listOfCheckBoxes = new SparseArray<CheckBox>();
        boolean listIsEmpty = listOfCheckBoxes.size() == 0;

        for (String s : translator.getValues(translator.getDictionary().RELATIONSHIP, false)) {
            if (i != 0 && i % 3 == 0) {
                // create a new row when 3 items were added to the previous one
                tableRow = new TableRow(getActivity().getBaseContext());
                tableRow.setLayoutParams(rowParams);
                tableRow.setPadding(dp5, dp5, dp5, dp5);
                tableLayout.addView(tableRow);
            }

            // create appropriate check box with title
            CheckBox chkBox = createCheckBox(s);

            // add it to the row
            tableRow.addView(chkBox);

            if (listIsEmpty) {
                // add check box to the list of checkboxes for further use
                listOfCheckBoxes.put(i, chkBox);

                // set userID for this check box to use with the map
                chkBox.setId(i);
            } else    // check appropriate box according to list of check boxes
                chkBox.setChecked(listOfCheckBoxes.get(i).isChecked());


            // increment i for dividing into rows of 3
            i++;
        }

        // to keep the order of the rows when a row had 1 or 2 items, add invisible check box\es
        if (i % 3 == 1) {
            CheckBox chkBox = createCheckBox("");
            chkBox.setVisibility(View.INVISIBLE);
            tableRow.addView(chkBox);

            // increment i to enter the second if (2 checkboxes needed)
            i++;
        }
        if (i % 3 == 2) {
            // add invisible check box
            CheckBox chkBox = createCheckBox("");
            chkBox.setVisibility(View.INVISIBLE);
            tableRow.addView(chkBox);
        }

    }

    public CheckBox createCheckBox(String s) {
        CheckBox chkBox = (CheckBox) getActivity().getLayoutInflater().inflate(R.layout.checkbox, null);
        chkBox.setEnabled(true);
        chkBox.setTextColor(getResources().getColor(R.color.black));
        chkBox.setText(s);
        return chkBox;
    }

    @Override
    public void onStart() {
        super.onStart();

        /*if(interestPurposeRetrieve == null && !prevWasClicked) {
            UserInterest userInterest = GuestRegisterFragment.getRegisteringUser().getUserInterest();

            if(userInterest != null) {
                List<String> list = userInterest.getPurposesOfInterest();
                for (int i = 0; i < listOfCheckBoxes.size(); i++) {
                    CheckBox ch = listOfCheckBoxes.get(i);

                    for (String s : list) {
                        if (s.compareTo(ch.getText().toString()) == 0) {
                            ch.setChecked(true);
                        }
                    }
                }
            }
        }*/

    }


}
