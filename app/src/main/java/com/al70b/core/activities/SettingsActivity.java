package com.al70b.core.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.al70b.R;

public class SettingsActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String[] arr = getResources().getStringArray(R.array.settings_list);

        ListView listView = (ListView) findViewById(R.id.list_view_settingsA);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                String message = null;
                switch (position) {
                    case 0:
                        intent = new Intent(SettingsActivity.this, TermsActivity.class);
                        //intent.putExtra(TermsActivity.DISPLAYING, )
                        message = "Show Terms Activity";
                        break;
                    case 1:
                        intent = new Intent(SettingsActivity.this, TermsActivity.class);
                        message = "Show Contact Us";
                        break;
                    case 2:
                        intent = new Intent(SettingsActivity.this, TermsActivity.class);
                        message = "Show Who are we";
                        break;
                    case 3:
                        intent = new Intent(SettingsActivity.this, TermsActivity.class);
                        message = "Show Close account page";
                        break;
                    case 4:
                        UserHomeActivity.loggingOut = true;
                        finish();
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                }
                if(message != null) {
                    Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
