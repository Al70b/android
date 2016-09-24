package com.al70b.core.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.MembersListActivity;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.KEYS;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.CurrentUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 6/26/2015.
 */
public class UserSearchAdvancedFragment extends Fragment {

    public static final String DISPLAY_DATA_TOKEN = "AdvancedSearch";
    public static final String GENDER = "gender" ;
    public static final String AGE_FROM = "age_from";
    public static final String AGE_TO = "age_to";
    public static final String COUNTRY = "country";
    public static final String HEIGHT_FROM = "height_from";
    public static final String HEIGHT_TO = "height_to";
    public static final String EDUCATION = "education";
    public static final String RELIGION = "religion";
    public static final String ALCOHOL = "alcohol";
    public static final String SMOKING = "smoking";
    public static final String PICTURES_ONLY = "pictures_only";
    public static final String ONLINE_ONLY = "online_only";
    public static final String NOT_IMPORTANT = "NOT_IMPORTANT";

    private Context context;

    private Translator translator;

    private CurrentUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity().getApplicationContext();
        translator = ((MyApplication)getActivity().getApplication()).getTranslator();
        currentUser = ((MyApplication)getActivity().getApplication()).getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup)
                inflater.inflate(R.layout.fragment_user_advanced_search, container, false);

        // gender
        final CheckBox checkBoxMale = (CheckBox)
                viewGroup.findViewById(R.id.check_box_user_advanced_search_genderMale);
        final CheckBox checkBoxFemale = (CheckBox)
                viewGroup.findViewById(R.id.check_box_user_advanced_search_genderFemale);

        // flags
        final CheckBox checkBoxPicturesOnly = (CheckBox)
                viewGroup.findViewById(R.id.check_box_user_advanced_search_pictures_only);
        final CheckBox checkBoxOnlineOnly = (CheckBox)
                viewGroup.findViewById(R.id.check_box_user_advanced_search_online_only);

        // country
        final Spinner spinnerCountry = (Spinner)
                viewGroup.findViewById(R.id.spinner_user_advanced_search_countryB);

        // age & height
        final EditText editTextAgeFrom = (EditText)
                viewGroup.findViewById(R.id.edit_text_user_advanced_search_ageFromB);
        final EditText editTextAgeTo = (EditText)
                viewGroup.findViewById(R.id.edit_text_user_advanced_search_ageToB);
        final EditText editTextHeightFrom = (EditText)
                viewGroup.findViewById(R.id.edit_text_user_advanced_search_heightFromB);
        final EditText editTextHeightTo = (EditText)
                viewGroup.findViewById(R.id.edit_text_user_advanced_search_heightToB);

        // more attributes
        TableLayout tableLayoutEducation = (TableLayout)
                viewGroup.findViewById(R.id.table_layout_user_advanced_search_education);
        TableLayout tableLayoutReligion = (TableLayout)
                viewGroup.findViewById(R.id.table_layout_user_advanced_search_religion);
        TableLayout tableLayoutAlcohol = (TableLayout)
                viewGroup.findViewById(R.id.table_layout_user_advanced_search_alcohol);
        TableLayout tableLayoutSmoking = (TableLayout)
                viewGroup.findViewById(R.id.table_layout_user_advanced_search_smoking);

        // search button
        Button btnSearch = (Button)
                viewGroup.findViewById(R.id.btn_user_advanced_search_go);

        final List<String> countries = translator.getValues(translator.getDictionary().COUNTRIES, true);
        countries.add(0, getString(R.string.all_countries));
        ArrayAdapter<String> countriesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                countries);
        spinnerCountry.setAdapter(countriesArrayAdapter);

        if(currentUser != null && currentUser.getAddress() != null) {
            spinnerCountry.setSelection(countriesArrayAdapter.getPosition(currentUser.getAddress().getCountry()));
        }


        // init list of checkboxes
        final List<CheckBox> checkBoxListEducation = new ArrayList<>();
        final List<CheckBox> checkBoxListReligion = new ArrayList<>();
        final List<CheckBox> checkBoxListAlcohol = new ArrayList<>();
        final List<CheckBox> checkBoxListSmoking = new ArrayList<>();

        // build the checkboxes
        buildInterestedPurposeLayout(tableLayoutEducation,
                translator.getDictionary().CHARACTERS.get(KEYS.SERVER.EDUCATION), checkBoxListEducation);
        buildInterestedPurposeLayout(tableLayoutReligion,
                translator.getDictionary().CHARACTERS.get(KEYS.SERVER.RELIGION), checkBoxListReligion);
        buildInterestedPurposeLayout(tableLayoutAlcohol,
                translator.getDictionary().CHARACTERS.get(KEYS.SERVER.ALCOHOL), checkBoxListAlcohol);
        buildInterestedPurposeLayout(tableLayoutSmoking,
                translator.getDictionary().CHARACTERS.get(KEYS.SERVER.SMOKING), checkBoxListSmoking);

        editTextAgeFrom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    String str = editTextAgeFrom.getText().toString().trim();
                    if(!str.isEmpty()) {
                        int age = Integer.parseInt(str);

                        if(age < AppConstants.MIN_MEMBER_AGE) {
                            editTextAgeFrom.setError(
                                    getString(R.string.error_should_be_older_than_min_age,
                                        String.valueOf(AppConstants.MIN_MEMBER_AGE)));
                        } else {
                            editTextAgeFrom.setError(null);
                        }
                    } else {
                        editTextAgeFrom.setError(null);
                    }
                }
            }
        });

        editTextAgeTo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    String str = editTextAgeTo.getText().toString().trim();
                    if(!str.isEmpty()) {
                        int age = Integer.parseInt(str);

                        if(age > AppConstants.MAX_MEMBER_AGE) {
                            editTextAgeTo.setError(getString(R.string.error_should_be_less_than_max_age,
                                    String.valueOf(AppConstants.MAX_MEMBER_AGE)));
                        } else {
                            editTextAgeTo.setError(null);
                        }
                    } else {
                        editTextAgeTo.setError(null);
                    }
                }
            }
        });

        editTextHeightFrom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    String str = editTextHeightFrom.getText().toString().trim();
                    if (!str.isEmpty()) {
                        int height = Integer.parseInt(str);

                        if (height < AppConstants.MIN_HEIGHT) {
                            editTextHeightFrom.setError(
                                    getString(R.string.error_should_be_bigger_than_min_height,
                                            String.valueOf(AppConstants.MIN_HEIGHT)));
                        } else {
                            editTextHeightFrom.setError(null);
                        }
                    } else {
                        editTextHeightFrom.setError(null);
                    }
                }
            }
        });

        editTextHeightTo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    String str = editTextHeightTo.getText().toString().trim();
                    if(!str.isEmpty()) {
                        int height = Integer.parseInt(str);

                        if(height > AppConstants.MAX_HEIGHT) {
                            editTextHeightTo.setError(
                                    getString(R.string.error_should_be_smaller_than_max_height,
                                            String.valueOf(AppConstants.MAX_HEIGHT)));
                        } else {
                            editTextHeightTo.setError(null);
                        }
                    } else {
                        editTextHeightTo.setError(null);
                    }
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // selected gender
                ArrayList<Integer> listGenderInterests = new ArrayList<Integer>();
                if (checkBoxMale.isChecked()) {
                    listGenderInterests.add(1);
                } if (checkBoxFemale.isChecked()) {
                    listGenderInterests.add(2);
                }

                if (listGenderInterests.size() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.error_choose_interest), Toast.LENGTH_SHORT).show();
                    return;
                }

                // selected country
                String country = spinnerCountry.getSelectedItem().toString();
                if (country.compareTo(getString(R.string.all_countries)) == 0) {
                    country = null;
                } else {
                    country = translator.translate(country, translator.getDictionary().COUNTRIES);
                }

                // age and height
                Integer ageFrom, ageTo, heightFrom, heightTo;
                if (editTextAgeFrom.getText().toString().trim().isEmpty()) {
                    ageFrom = AppConstants.MIN_MEMBER_AGE;
                } else {
                    ageFrom = Integer.parseInt(editTextAgeFrom.getText().toString());
                }

                if (editTextAgeTo.getText().toString().trim().isEmpty()) {
                    ageTo = AppConstants.MAX_MEMBER_AGE;
                } else {
                    ageTo = Integer.parseInt(editTextAgeTo.getText().toString());
                }

                if (editTextHeightFrom.getText().toString().trim().isEmpty()) {
                    heightFrom = AppConstants.MIN_HEIGHT;
                } else {
                    heightFrom = Integer.parseInt(editTextHeightFrom.getText().toString());
                }

                if (editTextHeightTo.getText().toString().isEmpty()) {
                    heightTo = AppConstants.MAX_HEIGHT;
                } else {
                    heightTo = Integer.parseInt(editTextHeightTo.getText().toString());
                }

                if (ageFrom < AppConstants.MIN_MEMBER_AGE) {
                    Toast.makeText(getActivity(), getString(R.string.error_should_be_older_than_min_age), Toast.LENGTH_SHORT).show();
                    return;
                }


                // selected education
                ArrayList<String> listEducation = new ArrayList<String>();
                for (CheckBox ch : checkBoxListEducation) {
                    if (ch.isChecked()) {
                        if (ch.getTag() != null
                                && ((String) ch.getTag()).compareTo(NOT_IMPORTANT) == 0) {
                            listEducation.clear();
                            break;
                        }
                        listEducation.add(ch.getText().toString());
                    }
                }
                listEducation = translator.translate(listEducation,
                        translator.getDictionary().CHARACTERS.get(KEYS.SERVER.EDUCATION));

                // selected religion
                ArrayList<String> listReligion = new ArrayList<String>();
                for (CheckBox ch : checkBoxListReligion) {
                    if (ch.isChecked()) {
                        if (ch.getTag() != null
                                && ((String) ch.getTag()).compareTo(NOT_IMPORTANT) == 0) {
                            listReligion.clear();
                            break;
                        }
                        listReligion.add(ch.getText().toString());
                    }
                }
                listReligion = translator.translate(listReligion,
                        translator.getDictionary().CHARACTERS.get(KEYS.SERVER.RELIGION));

                // selected alcohol
                ArrayList<String> listAlcohol = new ArrayList<String>();
                for (CheckBox ch : checkBoxListAlcohol) {
                    if (ch.isChecked()) {
                        if (ch.getTag() != null
                                && ((String) ch.getTag()).compareTo(NOT_IMPORTANT) == 0) {
                            listAlcohol.clear();
                            break;
                        }
                        listAlcohol.add(ch.getText().toString());
                    }
                }
                listAlcohol = translator.translate(listAlcohol,
                        translator.getDictionary().CHARACTERS.get(KEYS.SERVER.ALCOHOL));

                // selected smoking
                ArrayList<String> listSmoking = new ArrayList<String>();
                for (CheckBox ch : checkBoxListSmoking) {
                    if (ch.isChecked()) {
                        if (ch.getTag() != null
                                && ((String) ch.getTag()).compareTo(NOT_IMPORTANT) == 0) {
                            listSmoking.clear();
                            break;
                        }
                        listSmoking.add(ch.getText().toString());
                    }
                }
                listSmoking = translator.translate(listSmoking,
                        translator.getDictionary().CHARACTERS.get(KEYS.SERVER.SMOKING));

                // flags
                boolean withPicturesOnly, onlineOnly;
                withPicturesOnly = checkBoxPicturesOnly.isChecked();
                onlineOnly = checkBoxOnlineOnly.isChecked();


                Fragment fragment = new UserMembersSearchListFragment();

                // create bundle
                Bundle bundle = new Bundle();
                bundle.putString(UserMembersSearchListFragment.DATA_SOURCE, DISPLAY_DATA_TOKEN);
                bundle.putIntegerArrayList(GENDER, listGenderInterests);
                bundle.putSerializable(AGE_FROM, ageFrom);
                bundle.putSerializable(AGE_TO, ageTo);
                bundle.putString(COUNTRY, country);
                bundle.putSerializable(HEIGHT_FROM, heightFrom);
                bundle.putSerializable(HEIGHT_TO, heightTo);
                bundle.putStringArrayList(EDUCATION, listEducation);
                bundle.putStringArrayList(RELIGION, listReligion);
                bundle.putStringArrayList(ALCOHOL, listAlcohol);
                bundle.putStringArrayList(SMOKING, listSmoking);
                bundle.putBoolean(PICTURES_ONLY, withPicturesOnly);
                bundle.putBoolean(ONLINE_ONLY, onlineOnly);

                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content_frame, fragment, UserMembersSearchListFragment.FRAGMENT_TAG)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void buildInterestedPurposeLayout(TableLayout tableLayout, List<Translator.Word> listOfWords, final List<CheckBox> list) {

        int dp3 = (int) (3 / Resources.getSystem().getDisplayMetrics().density);

        // create first row with appropriate layout
        TableRow tableRow = new TableRow(context);
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(rowParams);
        tableRow.setPadding(dp3, dp3, dp3, dp3);


        // add it to the table
        tableLayout.addView(tableRow);

        // create appropriate check box with title
        final CheckBox chkBoxNotImportant = createCheckBox(getString(R.string.not_important));

        int i = 0;
        for (String s : translator.getValues(listOfWords, false)) {
            if (s.compareTo(getString(R.string.not_specified)) == 0 || s.trim().isEmpty())
                continue;

            if (i != 0 && i % 3 == 0) {
                // create a new row when 3 items were added to the previous one
                tableRow = new TableRow(context);
                tableRow.setLayoutParams(rowParams);
                tableRow.setPadding(dp3, dp3, dp3, dp3);
                tableLayout.addView(tableRow);
            }

            // create appropriate check box with title
            CheckBox chkBox = createCheckBox(s);

            // add it to the row
            tableRow.addView(chkBox);

            // add check box to the list of checkboxes for further use
            list.add(chkBox);

            // set userID for this check box to use with the map
            chkBox.setId(list.indexOf(chkBox));

            // increment i for dividing into rows of 3
            i++;
        }

        if (i % 3 == 0) {
            // create a new row when 3 items were added to the previous one
            tableRow = new TableRow(context);
            tableRow.setLayoutParams(rowParams);
            tableRow.setPadding(dp3, dp3, dp3, dp3);
            tableLayout.addView(tableRow);
        }

        // add the unimportant checkbox


        // add it to the row
        tableRow.addView(chkBoxNotImportant);
        // add check box to the list of checkboxes for further use
        list.add(chkBoxNotImportant);

        // set userID for this check box to use with the map
        chkBoxNotImportant.setId(list.indexOf(chkBoxNotImportant));
        chkBoxNotImportant.setTag(NOT_IMPORTANT);
        chkBoxNotImportant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (CheckBox ch : list) {
                    if (ch.getId() != buttonView.getId())
                        ch.setEnabled(!isChecked);
                }
            }
        });
        i++;
        // end of adding the unimportant checkbox


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
        chkBox.setText(s);
        chkBox.setEnabled(true);
        return chkBox;
    }
}
