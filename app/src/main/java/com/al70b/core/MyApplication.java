package com.al70b.core;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.misc.Translator;
import com.al70b.core.objects.CurrentUser;
import com.orm.SugarContext;

import org.acra.*;
import org.acra.annotation.*;
import org.acra.sender.HttpSender;

/**
 * Created by Naseem on 10/24/2015.
 */
@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "http://naseem.cloudant.com/acra-al70b/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "auto",
        formUriBasicAuthPassword = "auto"
)
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
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

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}
