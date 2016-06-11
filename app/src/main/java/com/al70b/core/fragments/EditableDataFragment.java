package com.al70b.core.fragments;

import android.app.Service;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 5/24/2015.
 * <p/>
 * Class represents Editable fragments that has the edit functionality, mainly user
 * for user basic data and advanced data
 */
public abstract class EditableDataFragment extends Fragment {

    // this fragment is only called in the user home activity
    protected CurrentUser user;

    // list of views that are editable
    protected List<View> listOfEditableViews = new ArrayList<View>();

    // cache editable views values in case update is canceled
    protected SparseArray<String> cachedValues = new SparseArray<>();

    // list of validation errors messages
    protected List<String> validationErrors = new ArrayList<>();

    public void startEditMode() {
        for (View v : listOfEditableViews) {
            if (v instanceof ImageButton)
                v.setVisibility(View.INVISIBLE);
            else if (v instanceof EditText || v instanceof Spinner) {
                v.setVisibility(View.VISIBLE);
            } else if (v instanceof CheckBox || v instanceof RadioButton)
                v.setEnabled(true);
            else if (v instanceof TextView)      // hide display views
                v.setVisibility(View.GONE);
        }
    }

    public void endEditMode() {
        if (listOfEditableViews.size() > 0) {    // if soft keyboard is shown, hide it
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(listOfEditableViews.get(0).getWindowToken(), 0);
        }

        for (View v : listOfEditableViews) {
            if (v instanceof ImageButton)
                v.setVisibility(View.GONE);
            else if (v instanceof EditText || v instanceof Spinner || v instanceof ImageButton)
                v.setVisibility(View.GONE);
            else if (v instanceof CheckBox || v instanceof RadioButton)
                v.setEnabled(false);
            else if (v instanceof TextView)
                v.setVisibility(View.VISIBLE);
        }
    }

    public void cacheValues() {
        String value;
        for (View v : listOfEditableViews) {
            if (v instanceof CheckBox)
                value = String.valueOf(((CheckBox) v).isChecked());
            else if (v instanceof RadioButton)
                value = String.valueOf(((RadioButton) v).isChecked());
            else if (v instanceof EditText)
                continue;
                //  value = ((EditText) v).getText().toString();
            else if (v instanceof TextView)
                value = ((TextView) v).getText().toString();
            else
                continue;

            // if key doesn't exist, then create new values
            if (cachedValues.indexOfKey(v.getId()) < 0)
                cachedValues.put(v.getId(), value);
            else { // replace values
                cachedValues.remove(v.getId());
                cachedValues.put(v.getId(), value);
            }

        }
    }

    public void loadCachedValues() {
        String value;
        for (View v : listOfEditableViews) {
            // get the saved value
            value = cachedValues.get(v.getId());

            if (value != null) {
                if (v instanceof CheckBox)
                    ((CheckBox) v).setChecked(Boolean.valueOf(value));
                else if (v instanceof RadioButton)
                    ((RadioButton) v).setChecked(Boolean.valueOf(value));
                else if (v instanceof EditText)
                    continue;
                    //  ((EditText) v).setText(value);
                else if (v instanceof TextView)
                    ((TextView) v).setText(value);
            }
        }
    }

    public void showValidationError() {
        if (validationErrors.size() > 0) {
            Toast.makeText(getActivity(), validationErrors.remove(0), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validData() {
        return true;
    }


    abstract public void getData(CurrentUser user);

    abstract public void updateEditTexts();

    abstract public ServerResponse<Boolean> updateUser(CurrentUser user) throws ServerResponseFailedException;


}
