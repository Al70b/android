package com.al70b.core.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.Dialogs.GeneralQuestionAlertDialog;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

public class CloseAccountActivity extends Activity {

    private static final String TAG = CloseAccountActivity.class.getSimpleName();
    private Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_account);

        thisActivity = this;
        final CurrentUser currentUser = ((MyApplication)getApplication()).getCurrentUser();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        Button btnCloseAccount = (Button) findViewById(R.id.btn_closeAccountA_close_account);
        btnCloseAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final GeneralQuestionAlertDialog dialog = new GeneralQuestionAlertDialog(
                        thisActivity, getString(R.string.close_account),
                        getString(R.string.are_you_sure_you_want_to_close_your_account));

                dialog.show();

                dialog.setPositiveButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            ServerResponse<String> sr = new RequestsInterface(CloseAccountActivity.this)
                                    .closeUserAccount(currentUser);

                            if(sr.isSuccess()) {
                                Toast.makeText(CloseAccountActivity.this,
                                        getString(R.string.close_account_was_successfull), Toast.LENGTH_SHORT).show();

                                UserHomeActivity.deleteSavedUserData(thisActivity);
                                Intent intent = new Intent(CloseAccountActivity.this, GuestHomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(CloseAccountActivity.this,
                                        sr.getErrorMsg(), Toast.LENGTH_SHORT).show();
                            }
                        } catch(ServerResponseFailedException ex) {
                            Log.e(TAG, ex.toString());
                        }
                    }
                });

                dialog.setNegativeButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


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
