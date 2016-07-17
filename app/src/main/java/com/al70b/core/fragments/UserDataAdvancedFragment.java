package com.al70b.core.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.misc.KEYS;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.Characteristics;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Naseem on 5/20/2015.
 */
public class UserDataAdvancedFragment extends EditableDataFragment {

    private TextView textViewHeight, textViewBodyShape, textViewEyesColors, textViewWork, textViewEducation;
    private TextView textViewReligion, textViewAlcohol, textViewSmoking, textViewMore;
    private EditText editTextHeight, editTextWork, editTextMore;
    private Spinner spinnerBodyShape, spinnerEyesColors, spinnerEducation, spinnerReligion;
    private Spinner spinnerAlcohol, spinnerSmoking;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = ((MyApplication)getActivity().getApplication()).getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_data_advanced, container, false);
        textViewHeight = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_heightB);
        editTextHeight = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_advanced_heightB);
        textViewBodyShape = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_bodyShapeB);
        spinnerBodyShape = (Spinner) viewGroup.findViewById(R.id.spinner_user_data_advanced_bodyShapeB);
        textViewEyesColors = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_eyesColorsB);
        spinnerEyesColors = (Spinner) viewGroup.findViewById(R.id.spinner_user_data_advanced_eyesColorsB);
        textViewWork = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_workB);
        editTextWork = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_advanced_workB);
        textViewEducation = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_educationB);
        spinnerEducation = (Spinner) viewGroup.findViewById(R.id.spinner_user_data_advanced_educationB);
        textViewReligion = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_religionB);
        spinnerReligion = (Spinner) viewGroup.findViewById(R.id.spinner_user_data_advanced_religionB);
        textViewAlcohol = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_alcoholB);
        spinnerAlcohol = (Spinner) viewGroup.findViewById(R.id.spinner_user_data_advanced_alcoholB);
        textViewSmoking = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_smokingB);
        spinnerSmoking = (Spinner) viewGroup.findViewById(R.id.spinner_user_data_advanced_smokingB);
        textViewMore = (TextView) viewGroup.findViewById(R.id.text_view_user_data_advanced_moreB);
        editTextMore = (EditText) viewGroup.findViewById(R.id.edit_text_user_data_advanced_moreB);


        // add these views to the editable views list
        listOfEditableViews.add(textViewHeight);
        listOfEditableViews.add(editTextHeight);
        listOfEditableViews.add(textViewBodyShape);
        listOfEditableViews.add(spinnerBodyShape);
        listOfEditableViews.add(textViewEyesColors);
        listOfEditableViews.add(spinnerEyesColors);
        listOfEditableViews.add(textViewWork);
        listOfEditableViews.add(editTextWork);
        listOfEditableViews.add(textViewEducation);
        listOfEditableViews.add(spinnerEducation);
        listOfEditableViews.add(textViewReligion);
        listOfEditableViews.add(spinnerReligion);
        listOfEditableViews.add(textViewAlcohol);
        listOfEditableViews.add(spinnerAlcohol);
        listOfEditableViews.add(textViewSmoking);
        listOfEditableViews.add(spinnerSmoking);
        listOfEditableViews.add(textViewMore);
        listOfEditableViews.add(editTextMore);


        Translator translator = Translator.getInstance(getActivity().getApplicationContext());

        // spinners handle
        // create array adapters for spinners
        ArrayAdapter<String> bodyShapeArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                translator.getValues(translator.getDictionary().CHARACTERS.get(KEYS.SERVER.BODY), false));
        ArrayAdapter<String> eyesColorsArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                translator.getValues(translator.getDictionary().CHARACTERS.get(KEYS.SERVER.EYES), false));
        ArrayAdapter<String> educationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                translator.getValues(translator.getDictionary().CHARACTERS.get(KEYS.SERVER.EDUCATION), false));
        ArrayAdapter<String> religionArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                translator.getValues(translator.getDictionary().CHARACTERS.get(KEYS.SERVER.RELIGION), false));
        ArrayAdapter<String> alcoholArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                translator.getValues(translator.getDictionary().CHARACTERS.get(KEYS.SERVER.ALCOHOL), false));
        ArrayAdapter<String> smokingArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                translator.getValues(translator.getDictionary().CHARACTERS.get(KEYS.SERVER.SMOKING), false));

        // set array adapters to spinners
        spinnerBodyShape.setAdapter(bodyShapeArrayAdapter);
        spinnerEyesColors.setAdapter(eyesColorsArrayAdapter);
        spinnerEducation.setAdapter(educationArrayAdapter);
        spinnerReligion.setAdapter(religionArrayAdapter);
        spinnerAlcohol.setAdapter(alcoholArrayAdapter);
        spinnerSmoking.setAdapter(smokingArrayAdapter);

        Characteristics ch = user.getUserChar();

        // parse data to widgets
        if (ch != null) {
            textViewHeight.setText(ch.displayHeight(getResources()).compareTo(getString(R.string.not_specified)) == 0 ?
                    ch.displayHeight(getResources()) :
                    ch.displayHeight(getResources()) + " " + getString(R.string.cm));
            editTextHeight.setHint(textViewHeight.getText().toString());
            editTextHeight.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    editTextHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable == null || editable.toString().isEmpty())
                        editTextHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
                }
            });
            editTextHeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        // focus is off
                        if (((EditText) view).getText().toString().length() == 0)
                            ((EditText) view).setHint(getString(R.string.not_specified));
                    }
                }
            });

            textViewBodyShape.setText(ch.getBody());
            spinnerBodyShape.setSelection(bodyShapeArrayAdapter.getPosition(ch.getBody()));

            textViewEyesColors.setText(ch.getEyes());
            spinnerEyesColors.setSelection(eyesColorsArrayAdapter.getPosition(ch.getEyes()));

            textViewWork.setText(ch.getWork());
            editTextWork.setHint(ch.getWork());

            textViewEducation.setText(ch.getEducation());
            spinnerEducation.setSelection(eyesColorsArrayAdapter.getPosition(ch.getEducation()));

            textViewReligion.setText(ch.getReligion());
            spinnerReligion.setSelection(religionArrayAdapter.getPosition(ch.getReligion()));

            textViewAlcohol.setText(ch.getAlcohol());
            spinnerAlcohol.setSelection(alcoholArrayAdapter.getPosition(ch.getAlcohol()));

            textViewSmoking.setText(ch.getSmoking());
            spinnerSmoking.setSelection(smokingArrayAdapter.getPosition(ch.getSmoking()));

            textViewMore.setText(ch.getDescription());
            editTextMore.setText(ch.getDescription());
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
        String height, work, more;
        String bodyShape, eyesColors, education, religion, alcohol, smoking;

        height = editTextHeight.getText().toString();
        work = editTextWork.getText().toString();
        more = editTextMore.getText().toString();


        try {
            more = more.replace("\n", URLEncoder.encode("\n", "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            more = "";
        }

        bodyShape = (String) spinnerBodyShape.getSelectedItem();
        eyesColors = (String) spinnerEyesColors.getSelectedItem();
        education = (String) spinnerEducation.getSelectedItem();
        religion = (String) spinnerReligion.getSelectedItem();
        alcohol = (String) spinnerAlcohol.getSelectedItem();
        smoking = (String) spinnerSmoking.getSelectedItem();

        Characteristics ch = new Characteristics(getResources(), height, bodyShape,
                eyesColors, alcohol, smoking, work, education, religion, more);

        user.setUserChar(ch);

        // display the data that was changed
        textViewHeight.setText(ch.displayHeight(getResources()).compareTo(getString(R.string.not_specified)) == 0 ?
                ch.displayHeight(getResources()) :
                ch.displayHeight(getResources()) + " " + getString(R.string.cm));
        textViewBodyShape.setText(ch.getBody());
        textViewEyesColors.setText(ch.getEyes());
        textViewWork.setText(ch.getWork());
        textViewEducation.setText(ch.getEducation());
        textViewReligion.setText(ch.getReligion());
        textViewAlcohol.setText(ch.getAlcohol());
        textViewSmoking.setText(ch.getSmoking());
        textViewMore.setText(editTextMore.getText().toString());

    }

    @Override
    public void updateEditTexts() {
        String height = user.getUserChar().getHeight();
        if (user.getUserChar().attributeNotSet(height))
            editTextHeight.setText("");
        else
            editTextHeight.setText(height);

        editTextWork.setText(textViewWork.getText().toString());

        spinnerBodyShape.setSelection(((ArrayAdapter<String>) spinnerBodyShape.getAdapter()).getPosition(textViewBodyShape.getText().toString()));
        spinnerEyesColors.setSelection(((ArrayAdapter<String>) spinnerEyesColors.getAdapter()).getPosition(textViewEyesColors.getText().toString()));
        spinnerEducation.setSelection(((ArrayAdapter<String>) spinnerEducation.getAdapter()).getPosition(textViewEducation.getText().toString()));
        spinnerReligion.setSelection(((ArrayAdapter<String>) spinnerReligion.getAdapter()).getPosition(textViewReligion.getText().toString()));
        spinnerAlcohol.setSelection(((ArrayAdapter<String>) spinnerAlcohol.getAdapter()).getPosition(textViewAlcohol.getText().toString()));
        spinnerSmoking.setSelection(((ArrayAdapter<String>) spinnerSmoking.getAdapter()).getPosition(textViewSmoking.getText().toString()));
    }

    @Override
    public ServerResponse<Boolean> updateUser(CurrentUser user) throws ServerResponseFailedException {
        RequestsInterface requestsInterface = new RequestsInterface(getActivity().getApplicationContext());

        ServerResponse<Boolean> sr = requestsInterface.updateUserDataAdvanced(user);
        return sr;
    }


}
