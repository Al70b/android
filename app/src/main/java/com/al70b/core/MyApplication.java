package com.al70b.core;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.CurrentUser;

/**
 * Created by Naseem on 10/24/2015.
 */
public class MyApplication extends MultiDexApplication { // MultiDexApplication

    private boolean mIsAppVisible;

    public boolean isAppVisible() {
        return mIsAppVisible;
    }

    public void setAppInvisible() {
        mIsAppVisible = false;
    }

    public void setAppVisible() {
        mIsAppVisible = true;
    }


    ///////   U S E R   ///////

    private CurrentUser currentUser;

    public void setCurrentUser(CurrentUser user) {
        this.currentUser = user;
    }

    public CurrentUser getCurrentUser() {
        if(currentUser == null) {
            Toast.makeText(this, getString(R.string.fatal_error_restarting), Toast.LENGTH_LONG).show();
            System.exit(0);
        }

        return currentUser;
    }


    ///////   T R A N S L A T O R   ///////
    private Translator myTranslator;

    public void setTranslator(Translator translator) {
        this.myTranslator = translator;
    }

    public Translator getTranslator() {
        if(myTranslator == null) {
            Toast.makeText(this, getString(R.string.fatal_error_restarting), Toast.LENGTH_LONG).show();
            System.exit(0);
        }

        return myTranslator;
    }
}
