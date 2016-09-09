package com.al70b.core.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

public class ContactUsActivity extends Activity {

    private static final int MINIMUM_CONTACT_MESSAGE_LENGTH = 20;
    private static final String TAG = ContactUsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        final CurrentUser currentUser = ((MyApplication)getApplication()).getCurrentUser();
        assert currentUser != null;

        final EditText etSubject = (EditText) findViewById(R.id.et_contactUsA_subject);
        final EditText etContent = (EditText) findViewById(R.id.et_contactUsA_content);
        Button btnSend = (Button) findViewById(R.id.btn_contactUsA_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = etSubject.getText().toString();
                String content = etContent.getText().toString();

                if(subject.trim().isEmpty() || content.trim().isEmpty()
                        || content.trim().length() < MINIMUM_CONTACT_MESSAGE_LENGTH) {
                    Toast.makeText(ContactUsActivity.this, getString(R.string.please_provide_subect_and_content),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    ServerResponse<String> sr = new RequestsInterface(ContactUsActivity.this)
                            .sendContactEmail(currentUser, subject, content);

                    String msg;
                    if(sr.isSuccess()) {
                        msg = getString(R.string.your_message_was_sent_successfully);
                    } else {
                        msg = getString(R.string.failed_to_send_your_message);
                        Log.e(TAG, sr.getErrorMsg());
                    }

                    Toast.makeText(ContactUsActivity.this, msg, Toast.LENGTH_SHORT).show();
                } catch (ServerResponseFailedException ex) {
                    Log.e(TAG, ex.toString());
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
