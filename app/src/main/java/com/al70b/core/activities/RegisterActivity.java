package com.al70b.core.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.fragments.Register.RegisterFragment1;
import com.al70b.core.fragments.Register.RegisterFragment2;
import com.al70b.core.fragments.Register.RegisterFragment3;
import com.al70b.core.fragments.Register.RegisterFragment4;
import com.al70b.core.fragments.Register.RegisterFragmentResult;
import com.al70b.core.objects.Address;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.User;
import com.al70b.core.objects.UserInterest;

import java.util.Calendar;

/**
 * Created by Naseem on 8/25/2016.
 */

public class RegisterActivity extends FragmentActivity {

    private static final int NUMBER_OF_STEPS = 4;

    protected CurrentUser registeringUser;
    public int currentStep = 1;
    private TextView txtViewTitle;

    public void pickFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_register, fragment,
                        fragment.getClass().getName())
                .addToBackStack(fragment.getTag())
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtViewTitle = (TextView) findViewById(R.id.tv_registerA_title);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
        }
        updateTitle();

        pickFragment(new RegisterFragment1());
        registeringUser = new CurrentUser(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_guest_register, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    public void updateTitle() {
        if(currentStep > NUMBER_OF_STEPS || currentStep < 1) {
            txtViewTitle.setVisibility(View.INVISIBLE);
        } else {
            txtViewTitle.setText(getString(R.string.step_x_out_of_y, currentStep, NUMBER_OF_STEPS));
            txtViewTitle.setVisibility(View.VISIBLE);
        }
    }

    public void registerName(String name) {
        registeringUser.setName(name);

        pickFragment(new RegisterFragment2());
        currentStep++;
        updateTitle();
    }

    public void registerAddress(Address address) {
        registeringUser.setAddress(address);

        pickFragment(new RegisterFragment3());
        currentStep++;
        updateTitle();
    }

    public void register3Fragment(User.Gender gender, Calendar birthDate, UserInterest userInterest) {
        registeringUser.setGender(gender);
        registeringUser.setDateOfBirth(birthDate);
        registeringUser.setUserInterest(userInterest);

        pickFragment(new RegisterFragment4());
        currentStep++;
        updateTitle();
    }

    public void registerEmailAndPassword(String email, String password) {
        registeringUser.setEmail(email);
        registeringUser.setPassword(password);

        pickFragment(new RegisterFragmentResult());
        currentStep++;
        updateTitle();
    }

    public CurrentUser getRegisteringUser() {
        return registeringUser;
    }
}
