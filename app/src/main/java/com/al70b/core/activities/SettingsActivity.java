package com.al70b.core.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.Dialogs.GeneralQuestionAlertDialog;

public class SettingsActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        String[] arr = getResources().getStringArray(R.array.settings_list);

        ListView listView = (ListView) findViewById(R.id.list_view_settingsA);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_settings, arr);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 0:
                        intent = new Intent(SettingsActivity.this, TermsActivity.class);
                        break;
                    case 1:
                        intent = new Intent(SettingsActivity.this, AboutUsActivity.class);
                        break;
                    case 2:
                        intent = new Intent(SettingsActivity.this, ContactUsActivity.class);
                        break;
                    case 3:
                        intent = new Intent(SettingsActivity.this, CloseAccountActivity.class);
                        break;
                    case 4:
                        final GeneralQuestionAlertDialog dialog = new GeneralQuestionAlertDialog(
                                SettingsActivity.this, getString(R.string.info),
                                getString(R.string.are_you_sure_you_want_to_log_out));

                        dialog.show();

                        dialog.setPositiveButton(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UserHomeActivity.loggingOut = true;
                                finish();
                            }
                        });

                        dialog.setNegativeButton(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                }
            }
        });
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
}
