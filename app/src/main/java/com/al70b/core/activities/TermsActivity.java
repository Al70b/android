package com.al70b.core.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.al70b.R;
import com.al70b.core.misc.AppConstants;

/**
 * Created by Naseem on 7/7/2016.
 */
public class TermsActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_terms_of_use);

        WebView webView = (WebView) findViewById(R.id.web_view_terms_of_use);
        webView.loadUrl(AppConstants.TERMS_OF_USE_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_terms_of_use, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_terms_of_use_close:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}