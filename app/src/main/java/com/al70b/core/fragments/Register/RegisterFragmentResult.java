package com.al70b.core.fragments.Register;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.ScreenSlideHomeActivity;
import com.al70b.core.fragments.Alerts.DisplayInfoAlert;
import com.al70b.core.fragments.RegisterFragment;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

/**
 * Created by Naseem on 6/30/2015.
 */
public class RegisterFragmentResult extends Fragment {

    private CurrentUser currentUser;

    private ProgressBar progressBar;
    //private TextView textViewResult;

    private DisplayInfoAlert displayInfoAlert = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = RegisterFragment.getRegisteringUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_register_result, container, false);
        //textViewResult = (TextView) viewGroup.findViewById(R.id.text_view_register_result);
        progressBar = (ProgressBar) viewGroup.findViewById(R.id.progress_bar_register_done);

        return viewGroup;
    }

    @Override
    public void onResume() {
        super.onResume();

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "Something went wrong";
                RequestsInterface requestsInterface = new RequestsInterface(getActivity().getApplicationContext());

                ServerResponse<String> sr = requestsInterface.registerUser(currentUser);

                if (sr != null && sr.isSuccess()) {
                    result = getString(R.string.register_successfully);
                } else {
                    if (sr != null)
                        result = sr.getErrorMsg();
                }

                final String finalResult = result;
                final boolean resultOK = sr.isSuccess();
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);

                        int iconResID;
                        String title, message;
                        View.OnClickListener ocl;


                        if (resultOK) {
                            title = getString(R.string.register_message_success);
                            message = getString(R.string.register_successfully);
                            iconResID = -1;
                            ocl = new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // go to login activity
                                    ((ScreenSlideHomeActivity) getActivity()).goToLogin(currentUser.getEmail());

                                    // reload register fragment
                                    RegisterFragment.pickFragment(new RegisterFragmentIntro(), true);

                                    if (displayInfoAlert != null)
                                        displayInfoAlert.dismiss();
                                }
                            };
                        } else {
                            title = getString(R.string.error);
                            message = finalResult;
                            iconResID = -1;
                            ocl = new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    boolean emailAlreadyExists = finalResult.compareTo(getString(R.string.error_server_email_exists)) == 0;
                                    if (emailAlreadyExists) {
                                        // back to account info fragment
                                        //fragment = new RegisterFragment5();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("Email", currentUser.getEmail());
                                        getActivity().getSupportFragmentManager()
                                                .popBackStack();
                                        //fragment.setArguments(bundle);
                                    } else {
                                        // reload register fragment
                                        Fragment fragment = new RegisterFragmentIntro();
                                        getActivity().getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.frame_layout_register, fragment, fragment.getClass().getName())
                                                .commit();
                                    }


                                    if (displayInfoAlert != null)
                                        displayInfoAlert.dismiss();

                                    Toast.makeText(getActivity(), finalResult, Toast.LENGTH_SHORT).show();
                                }
                            };
                        }

                        displayInfoAlert = new DisplayInfoAlert(getActivity(),
                                title, message, iconResID);
                        displayInfoAlert.show();
                        displayInfoAlert.setCanceledOnTouchOutside(false);
                        displayInfoAlert.setButtonOkFunction(ocl);

                        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        //LinearLayout linearLayout = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.dialog, null, false);

                        //ImageView imgView = (ImageView)linearLayout.findViewById(R.id.dialog_icon);
                        //TextView textView = (TextView) linearLayout.findViewById(R.id.dialog_title);

                        /*if (resultOK) {
                            imgView.setImageResource(R.drawable.green_check);
                            textView.setText(getString(R.string.success_message_title));
                            builder.setMessage(getString(R.string.register_successfully))
                                    .setCancelable(false)
                                    .setTitle(getString(R.string.register_message_success))
                                    .setIcon(R.drawable.green_check)
                                    .setCustomTitle(linearLayout)
                                    .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            // go to login activity
                                            ((ScreenSlideHomeActivity) getActivity()).goToLogin(currentUser.getEmail());

                                            // reload register fragment
                                            RegisterFragment.pickFragment(new RegisterFragmentIntro());
                                        }
                                    });
                        } else {
                            imgView.setImageResource(R.drawable.attention_red_icon);
                            textView.setText(getString(R.string.register_oops));
                            builder.setMessage(finalResult)
                                    .setCancelable(false)
                                    .setTitle(getString(R.string.register_oops))
                                    .setIcon(R.drawable.attention_red_icon)
                                    .setCustomTitle(linearLayout)
                                    .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {

                                            boolean emailAlreadyExists = finalResult.compareTo(getString(R.string.error_server_email_exists)) == 0;
                                            if(emailAlreadyExists) {
                                                // reload register fragment
                                                Fragment fragment = new RegisterFragment5();
                                                Bundle bundle = new Bundle();
                                                bundle.putString("Email", currentUser.getEmail());
                                                fragment.setArguments(bundle);

                                                // get the user back to the account info fragment
                                                RegisterFragment.pickFragment(fragment);
                                            } else {
                                                // reload register fragment
                                                RegisterFragment.pickFragment(new RegisterFragmentIntro());
                                            }

                                            Toast.makeText(getActivity(), finalResult, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        AlertDialog alert = builder.create();
                        alert.show();*/

                    }
                });
            }
        }).start();
    }
}
