package com.al70b.core.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.CustomViewPager;
import com.al70b.core.extended_widgets.SlidingTabLayout;
import com.al70b.core.activities.Dialogs.PleaseWaitDialog;
import com.al70b.core.activities.Dialogs.SendMessageDialog;
import com.al70b.core.fragments.MemberDataAdvancedFragment;
import com.al70b.core.fragments.MemberDataBasicFragment;
import com.al70b.core.fragments.MemberDataPicturesFragment;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.FriendButtonHandler;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 6/18/2015.
 */
public class MemberProfileActivity extends FragmentActivity {

    public static final String CURRENT_USER = "com.al70b.core.activities.MemberProfileActivity.currentUser";
    public static final String OTHER_USER = "com.al70b.core.activities.MemberProfileActivity.otherUser";
    public static final String POSITION = "com.al70b.core.activities.MemberProfileActivity.position";
    public static final String FRIEND_STATUS = "com.al70b.core.activities.MemberProfileActivity.friend_status";

    // number of pages in the view pager
    private final int PAGE_COUNT = 3;
    SlidingTabLayout slidingTabLayout;
    // current user object
    private CurrentUser currentUser;
    // user who's profile is on
    private OtherUser otherUser;
    private CustomViewPager viewPager;
    private CircleImageView circleImageProfilePicture;
    private LinearLayout layoutResult, layoutLoading;
    private TextView textViewMessage;
    private ProgressBar progressBar;
    private TabsPagerAdapter pagerAdapter;
    private MemberProfileActivity thisActivity;
    private Fragment[] fragments = new Fragment[PAGE_COUNT];

    private int currentFragmentIdx = 2;


    private boolean friendStatusChanged;
    private int otherUserPosition;

    private MenuItem friendMenuItem;

    private RequestsInterface requestsInterface;
    private TextView textViewSubtitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thisActivity = this;

        final Handler handler = new Handler();

        initActivity();

        Bundle bundle = getIntent().getExtras();

        requestsInterface = new RequestsInterface(getApplicationContext());

        if (bundle != null) {
            currentUser = (CurrentUser) bundle.getSerializable(CURRENT_USER);
            otherUser = (OtherUser) bundle.getSerializable(OTHER_USER);
            otherUserPosition = bundle.getInt(POSITION, -1);

            currentUser.setContext(getApplicationContext());
            otherUser.setContext(getApplicationContext());


            inflateActionBar();

            Glide.with(thisActivity)
                    .load(otherUser.getProfilePictureThumbnailPath())
                    .asBitmap()
                    .placeholder(R.drawable.avatar)
                    .centerCrop()
                    .into(circleImageProfilePicture);

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        final ServerResponse<OtherUser> sr = requestsInterface.getOtherUserData(
                                currentUser.getUserID(), currentUser.getAccessToken(), otherUser);

                        if (sr.isSuccess()) {
                            // otherUser object already has the result

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    layoutLoading.setVisibility(View.GONE);
                                    layoutResult.setVisibility(View.VISIBLE);

                                    if (otherUser.getAddress() != null)
                                        updateSubtitle(otherUser.getAddress().toString());
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    textViewMessage.setText(sr.getErrorMsg());
                                }
                            });
                        }
                    } catch (final ServerResponseFailedException ex) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                textViewMessage.setText(ex.toString());
                            }
                        });
                    }

                }
            };

            // run the thread to get user's data
            new Thread(runnable).start();

            // in case something went wrong with server, clicking the layout would start
            // a new thread to get user's data
            layoutLoading.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (progressBar.getVisibility() == View.GONE) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textViewMessage.setText(getString(R.string.please_wait));
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        });
                        new Thread(runnable);
                    }
                }
            });
        }
    }

    private void inflateActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.actionbar_member_profile, null);

        TextView textViewTitle = (TextView) layout.findViewById(R.id.actionbar_member_profile_txtview_title);
        textViewSubtitle = (TextView) layout.findViewById(R.id.actionbar_member_profile_txtview_subtitle);
        ImageButton imgBtnBack = (ImageButton) layout.findViewById(R.id.actionbar_member_profile_imgbtn_back);
        circleImageProfilePicture = (CircleImageView) layout.findViewById(R.id.actionbar_member_profile_circleimg_profile);

        textViewTitle.setText(otherUser.getName());

        if (otherUser.getAddress() != null)
            textViewSubtitle.setText(otherUser.getAddress().toString());
        else
            textViewSubtitle.setVisibility(View.INVISIBLE);

        circleImageProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!otherUser.isProfilePictureSet())
                    return;

                Intent intent = new Intent(thisActivity, DisplayPictureActivity.class);
                intent.putExtra("DisplayPicture.image", otherUser.getProfilePictureThumbnailPath());
                startActivity(intent);
            }
        });

        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        actionBar.setCustomView(layout);
        actionBar.setDisplayShowCustomEnabled(true);

    }

    public void initActivity() {
        setContentView(R.layout.activity_member_profile);

        viewPager = (CustomViewPager) findViewById(R.id.view_pager_member_profile_data);
        pagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        layoutResult = (LinearLayout) findViewById(R.id.layout_member_profile_result);
        layoutLoading = (LinearLayout) findViewById(R.id.layout_member_profile_loading);

        textViewMessage = (TextView) findViewById(R.id.text_view_member_profile_loading);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_member_profile_loading);

        // set adapter for view pager
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(PAGE_COUNT);

        // bind sliding tab layout with xml tag, and set the previously created view pager
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs_member_profile);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setCustomTabView(R.layout.tab, R.id.tv_tab_in_view_pager);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.RED;
            }
        });
        slidingTabLayout.setViewPager(viewPager);

        // set the first fragment to be the login info
        viewPager.setCurrentItem(currentFragmentIdx);
    }

    private void updateSubtitle(String s) {
        textViewSubtitle.setText(s);
        textViewSubtitle.setVisibility(View.VISIBLE);
    }


    private void updateFriendStatusIcon() {
        int icon;
        String title;
        OtherUser.FriendStatus friendStatus = otherUser.getFriendStatus();

        icon = friendStatus.getDrawableResourceIDForActionBar();
        title = getString(friendStatus.getStringResourceID());

        friendMenuItem.setIcon(icon);
        friendMenuItem.setTitle(title);
    }

    @Override
    public void onBackPressed() {
        // pass the friend status if changed
        int resultCode = friendStatusChanged ? 1 : 0;
        Intent resultIntent = new Intent();
        resultIntent.putExtra(POSITION, otherUserPosition);
        resultIntent.putExtra(FRIEND_STATUS, otherUser.getFriendStatus());
        setResult(resultCode, resultIntent);

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_member_profile, menu);

        friendMenuItem = menu.findItem(R.id.menu_item_member_profile_friend);
        updateFriendStatusIcon();
        return super.onCreateOptionsMenu(menu);
    }


    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final OtherUser.FriendStatus friendStatus = otherUser.getFriendStatus();

        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.menu_item_member_profile_friend:

                final PleaseWaitDialog alertWait = new PleaseWaitDialog(thisActivity, getString(R.string.sending_request ));
                alertWait.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        FriendButtonHandler friendButtonHandler = new FriendButtonHandler();
                        friendStatusChanged = friendButtonHandler.handle(thisActivity, currentUser, otherUser);

                        //  OtherUser.FriendStatus friendStatusSet = otherUser.getFriendStatus();
                        // friend status probably was changed
                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                friendMenuItem.setIcon(friendStatus.getDrawableResourceIDForActionBar());
                                friendMenuItem.setTitle(friendStatus.getStringResourceID());
                                invalidateOptionsMenu();

                                alertWait.dismiss();
                            }
                        });
                    }
                }).start();

                return true;
            case R.id.menu_item_member_profile_send_message:

                SendMessageDialog alert = new SendMessageDialog(thisActivity, otherUser);
                alert.setCanceledOnTouchOutside(false);
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {

        private String tabTitles[] = getResources().getStringArray(R.array.user_data_titles);

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = fragments[position];

            if (frag == null) {
                switch (position) {
                    case 0:
                        frag = new MemberDataPicturesFragment();
                        break;
                    case 1:
                        frag = new MemberDataAdvancedFragment();
                        break;
                    default:
                        frag = new MemberDataBasicFragment();
                }
                fragments[position] = frag;
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable(OTHER_USER, otherUser);

            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
