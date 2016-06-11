package com.al70b.core.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.GeneralUI;
import com.al70b.core.misc.JSONHelper;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.objects.User;
import com.al70b.core.objects.UserInterest;
import com.al70b.core.server_methods.RequestsInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Naseem on 5/20/2015.
 */
public class UserDataBasicFragment extends EditableDataFragment {

    private Context context;

    private TextView textViewName, textViewCity, textViewCountry, textViewSocial;
    private EditText editTextName, editTextCity;
    private Spinner spinnerCountry, spinnerSocial;
    private RadioGroup rdGroupGenderInterest;

    // list of check boxes in the interested purpose table
    private List<CheckBox> listOfCheckBoxes = new ArrayList<CheckBox>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity().getBaseContext();

        user = ((MyApplication)getActivity().getApplication()).getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_data_basic, container, false);

        Translator translator = Translator.getInstance(context);

        // relate widgets from xml
        textViewName = (TextView) viewGroup.findViewById(R.id.text_view_user_data_basic_nameB);
        editTextName = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_basic_nameB);
        TextView txtViewGender = (TextView) viewGroup.findViewById(R.id.text_view_user_data_basic_genderB);
        TextView txtViewBirthdate = (TextView) viewGroup.findViewById(R.id.text_view_user_data_basic_birthdateB);
        textViewCity = (TextView) viewGroup.findViewById(R.id.text_view_user_data_basic_cityB);
        editTextCity = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_basic_cityB);
        textViewCountry = (TextView) viewGroup.findViewById(R.id.text_view_user_data_basic_countryB);
        spinnerCountry = (Spinner) viewGroup.findViewById(R.id.spinner_user_data_basic_countryB);
        textViewSocial = (TextView) viewGroup.findViewById(R.id.text_view_user_data_basic_socialB);
        spinnerSocial = (Spinner) viewGroup.findViewById(R.id.spinner_user_data_basic_socialB);
        rdGroupGenderInterest = (RadioGroup) viewGroup.findViewById(R.id.radio_group_user_data_basic_genderInterestB);
        TableLayout tableLayout = (TableLayout) viewGroup.findViewById(R.id.table_layout_user_data_basic_interestedPurposes);

        // build contents of table
        GeneralUI.buildInterestedPurposeLayout(getActivity(), tableLayout, 3, listOfCheckBoxes,
                translator.getValues(translator.getDictionary().RELATIONSHIP, false));

        // add these views to the editable views list
        listOfEditableViews.add(textViewName);
        listOfEditableViews.add(editTextName);
        listOfEditableViews.add(textViewCity);
        listOfEditableViews.add(editTextCity);
        listOfEditableViews.add(textViewCountry);
        listOfEditableViews.add(spinnerCountry);
        listOfEditableViews.add(textViewSocial);
        listOfEditableViews.add(spinnerSocial);
        listOfEditableViews.add(rdGroupGenderInterest.getChildAt(0));
        listOfEditableViews.add(rdGroupGenderInterest.getChildAt(1));
        listOfEditableViews.addAll(listOfCheckBoxes);


        // spinners handle
        // create array adapters for spinners
        ArrayAdapter<String> countriesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                translator.getValues(translator.getDictionary().COUNTRIES, true));
        ArrayAdapter<String> socialArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                translator.getValues(translator.getDictionary().SOCIAL_STATUS, true));

        // set array adapters to spinners
        spinnerCountry.setAdapter(countriesArrayAdapter);
        spinnerSocial.setAdapter(socialArrayAdapter);


        // set spinners value according to user's data
        spinnerCountry.setSelection(countriesArrayAdapter.getPosition(user.getAddress().getCountry()));
        spinnerSocial.setSelection(socialArrayAdapter.getPosition(user.getSocialStatus()));

        // parse user's data to widgets
        textViewName.setText(user.getName());
        txtViewGender.setText(user.getGender().toString(context));
        Calendar dateOfBirth = user.getDateOfBirth();
        txtViewBirthdate.setText(dateOfBirth.get(Calendar.DAY_OF_MONTH) + "/"
                + (dateOfBirth.get(Calendar.MONTH) + 1) + "/"
                + dateOfBirth.get(Calendar.YEAR));
        textViewSocial.setText(user.getSocialStatus());
        textViewCountry.setText(user.getAddress().getCountry());
        textViewCity.setText(user.getAddress().isCityEmpty() ? getString(R.string.not_specified) : user.getAddress().getCity());

        // retrieve user's interested in and purposes data
        CurrentUser.Gender userGenderInterest = user.getUserInterest().getGenderInterest();
        List<String> userInterestPurpose = user.getUserInterest().getPurposesOfInterest();

        // check the appropriate check boxes
        switch (userGenderInterest.getValue()) {
            case User.Gender.MALE:
                rdGroupGenderInterest.check(R.id.radio_button_user_data_basic_male);
                break;
            case User.Gender.FEMALE:
                rdGroupGenderInterest.check(R.id.radio_button_user_data_basic_female);
                break;
        }

        // check appropriate interest purpose checkboxes
        for (String s : userInterestPurpose) {
            for (CheckBox cb : listOfCheckBoxes) {
                if (s.compareTo(cb.getText().toString()) == 0) {
                    cb.setChecked(true);
                    break;
                }
            }
        }

        // cache values
        cacheValues();

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    public void getData(CurrentUser user) {
        String name, city, country, social;
        CurrentUser.Gender userGenderInterest;
        List<String> userInterestPurpose = new ArrayList<>();

        name = editTextName.getText().toString();
        city = editTextCity.getText().toString();
        country = (String) spinnerCountry.getSelectedItem();
        social = (String) spinnerSocial.getSelectedItem();

        switch (rdGroupGenderInterest.getCheckedRadioButtonId()) {
            case R.id.radio_button_user_data_basic_male:
                userGenderInterest = new User.Gender(User.Gender.MALE);
                break;
            case R.id.radio_button_user_data_basic_female:
                userGenderInterest = new User.Gender(User.Gender.FEMALE);
                break;
            default:
                userGenderInterest = new User.Gender(User.Gender.NOT_SET);
        }

        // parse data
        user.setName(name);
        user.setAddress(city, country);
        user.setSocialStatus(social);
        user.setUserInterest(new UserInterest(userGenderInterest, userInterestPurpose));

        // display data on widgets
        textViewName.setText(name);
        textViewSocial.setText(social);
        textViewCountry.setText(country);
        textViewCity.setText(city.isEmpty() ? getString(R.string.not_specified) : city);

        // check appropriate interest purpose checkboxes
        for (CheckBox cb : listOfCheckBoxes)
            if (cb.isChecked())
                userInterestPurpose.add(cb.getText().toString());

    }

    @Override
    public void updateEditTexts() {
        editTextName.setText(textViewName.getText().toString());
        editTextCity.setText(textViewCity.getText().toString());

        spinnerCountry.setSelection(((ArrayAdapter<String>) spinnerCountry.getAdapter()).getPosition(textViewCountry.getText().toString()));
        spinnerSocial.setSelection(((ArrayAdapter<String>) spinnerSocial.getAdapter()).getPosition(textViewSocial.getText().toString()));
    }

    @Override
    public ServerResponse<Boolean> updateUser(CurrentUser user) throws ServerResponseFailedException {
        RequestsInterface requestsInterface = new RequestsInterface(context);

        ServerResponse<Boolean> sr = requestsInterface.updateUserDataBasic(user);

        // update shared preferences name
        if (sr != null && sr.isSuccess()) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(AppConstants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(JSONHelper.NAME);
            editor.putString(JSONHelper.NAME, user.getName());
            editor.commit();
        }

        return sr;
    }

    @Override
    public boolean validData() {
        boolean name, city, oneInterestAtLeast;


        if (editTextName.getText().toString().length() < 2) {
            validationErrors.add(getString(R.string.error_fill_your_name));
            name = false;
        } else
            name = true;

        if (editTextCity.getText().toString().length() < 3) {
            validationErrors.add(getString(R.string.error_fill_your_city));
            city = false;
        } else
            city = true;

        oneInterestAtLeast = false;
        for (CheckBox ch : listOfCheckBoxes) {
            if (ch.isChecked()) {
                oneInterestAtLeast = true;
                break;
            }
        }

        if (!oneInterestAtLeast)
            validationErrors.add(getString(R.string.error_choose_interest_purpose));

        return name && city && oneInterestAtLeast;
    }
}
