package com.al70b.core.fragments.Register;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.extended_widgets.ClearableEditText;
import com.al70b.core.fragments.GuestRegisterFragment;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.Address;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Naseem on 6/30/2015.
 */
public class RegisterFragment2 extends Fragment {

    private static String cityRetrieve, countryRetrieve;
    private Translator translator;
    private ClearableEditText clearableCity;
    private Spinner spinnerCountry;
    private boolean validCity;
    private String country;

    @Override
    public void onCreate(Bundle savedInstaneState) {
        super.onCreate(savedInstaneState);

        translator = Translator.getInstance(getActivity().getApplicationContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_register_2, container, false);
        clearableCity = (ClearableEditText) viewGroup.findViewById(R.id.clearable_edit_text_register_city);
        spinnerCountry = (Spinner) viewGroup.findViewById(R.id.spinner_register_country);
        Button btnNext = (Button) viewGroup.findViewById(R.id.btn_register_next_2);
        Button btnPrev = (Button) viewGroup.findViewById(R.id.btn_register_prev_2);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide soft keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(clearableCity.getWindowToken(), 0);

                cityRetrieve = clearableCity.getEditText().getText().toString();
                countryRetrieve = (String) spinnerCountry.getSelectedItem();

                getActivity().getSupportFragmentManager()
                        .popBackStack();
            }
        });

        clearableCity.setEditTextHint(R.string.register_city);

        clearableCity.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validCity = s.toString().trim().length() >= 3;
            }
        });

        // get countries list and sort it
        List<String> countriesList = translator.getValues(translator.getDictionary().COUNTRIES, false);
        Collections.sort(countriesList);

        // get country's code
        final String defaultCountry = getUserCountry(getActivity());
        country = defaultCountry;
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item_1,
                countriesList);
        spinnerCountry.setAdapter(adapter);
        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                country = adapter.getItem(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        String countryArabic = translator.translate(defaultCountry, translator.getDictionary().COUNTRIES);
        spinnerCountry.setSelection(adapter.getPosition(countryArabic));

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validCity) {    // and surely valid country because a default is set
                    String city = clearableCity.getEditText().getText().toString();
                    Address address = new Address(city, country);
                    GuestRegisterFragment.getRegisteringUser().setAddress(address);

                    // hide keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(clearableCity.getWindowToken(), 0);

                    GuestRegisterFragment.pickFragment(new RegisterFragment3(), true);
                } else {
                    Toast.makeText(getActivity(), R.string.error_fill_your_city, Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearableCity.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        if (cityRetrieve != null && country != null) {
            clearableCity.getEditText().setText(cityRetrieve);
            spinnerCountry.setSelection(adapter.getPosition(countryRetrieve));

            cityRetrieve = null;
            countryRetrieve = null;
        }

        return viewGroup;
    }

    private String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toUpperCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toUpperCase(Locale.US);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
