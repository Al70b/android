package com.al70b.core.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

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

public class RegisterActivity extends FragmentActivity {

    protected CurrentUser registeringUser;

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
            case R.id.action_register_close:
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

    public void registerName(String name) {
        registeringUser.setName(name);

        pickFragment(new RegisterFragment2());
    }

    public void registerAddress(Address address) {
        registeringUser.setAddress(address);

        pickFragment(new RegisterFragment3());
    }

    public void register3Fragment(User.Gender gender, Calendar birthDate, UserInterest userInterest) {
        registeringUser.setGender(gender);
        registeringUser.setDateOfBirth(birthDate);
        registeringUser.setUserInterest(userInterest);

        pickFragment(new RegisterFragment4());
    }

    public void registerEmailAndPassword(String email, String password) {
        registeringUser.setEmail(email);
        registeringUser.setPassword(password);

        pickFragment(new RegisterFragmentResult());
    }

    public void registerAcceptedAdvertisement(boolean isChecked) {
        registeringUser.setAcceptAdvertisement(isChecked);
    }

    public CurrentUser getRegisteringUser() {
        return registeringUser;
    }
}
