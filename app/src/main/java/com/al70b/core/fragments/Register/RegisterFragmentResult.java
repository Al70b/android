package com.al70b.core.fragments.Register;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.Dialogs.DisplayInfoAlert;
import com.al70b.core.activities.LoginActivity;
import com.al70b.core.activities.RegisterActivity;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

/**
 * Created by Naseem on 6/30/2015.
 */
public class RegisterFragmentResult extends Fragment {

    private CurrentUser currentUser;

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RegisterActivity activity = (RegisterActivity) getActivity();
        currentUser = activity.getRegisteringUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_general_register_result, container, false);
        progressBar = (ProgressBar) viewGroup.findViewById(R.id.progress_bar_register_done);

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();
        new RegisterAsyncTask().execute();
    }

    private class RegisterAsyncTask extends AsyncTask<Void, Void, Pair<Boolean, String>> {

        private DisplayInfoAlert displayInfoAlert;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Pair<Boolean, String> doInBackground(Void... params) {
            Pair<Boolean, String> result;

            RequestsInterface requestsInterface = new RequestsInterface(getActivity().getApplicationContext());
            try {
                ServerResponse<String> sr = requestsInterface.registerUser(currentUser);

                if (sr.isSuccess()) {
                    result = new Pair<>(true, getString(R.string.register_successfully));
                } else {
                    result = new Pair<>(false, sr.getErrorMsg());
                }
            } catch(ServerResponseFailedException ex) {
                result = new Pair<>(false, ex.toString());
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Pair<Boolean, String> result) {
            progressBar.setVisibility(View.INVISIBLE);

            int iconResID;
            String title, message;
            View.OnClickListener ocl;

            if (result.first) {
                title = getString(R.string.register_message_success);
                message = getString(R.string.register_successfully);
                iconResID = -1;
                ocl = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Go to login activity
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.putExtra(LoginActivity.EMAIL, currentUser.getEmail());
                        getActivity().startActivity(intent);

                        if (displayInfoAlert != null) {
                            displayInfoAlert.dismiss();
                        }
                        getActivity().finish();
                    }
                };
            } else {
                title = getString(R.string.error);
                message = result.second;
                iconResID = -1;
                ocl = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        boolean emailAlreadyExists = result.second.compareTo(getString(R.string.error_server_email_exists)) == 0;
                        if (emailAlreadyExists) {
                            // back to account info fragment
                            Bundle bundle = new Bundle();
                            bundle.putString("Email", currentUser.getEmail());
                            getActivity().getSupportFragmentManager()
                                    .popBackStack();
                        } else {
                            // reload register fragment
                            Fragment fragment = new RegisterFragment1();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.frame_layout_register, fragment, fragment.getClass().getName())
                                    .commit();
                        }


                        if (displayInfoAlert != null) {
                            displayInfoAlert.dismiss();
                        }

                        Toast.makeText(getActivity(), result.second, Toast.LENGTH_SHORT).show();
                    }
                };
            }

            displayInfoAlert = new DisplayInfoAlert(getActivity(),
                    title, message, iconResID);
            displayInfoAlert.show();
            displayInfoAlert.setCanceledOnTouchOutside(false);
            displayInfoAlert.setButtonOkFunction(ocl);

        }
    }

}
