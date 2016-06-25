package com.al70b.core.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.CustomViewPager;
import com.al70b.core.extended_widgets.SlidingTabLayout;
import com.al70b.core.objects.ServerResponse;

/**
 * Created by Naseem on 5/10/2015.
 */
public class UserDataFragment extends Fragment {

    public static final String TO_PICTURES_FRAGMENT = "com.al7ob.al7ob.core.fragments.UserDataFragment.ToPictures";

    private static boolean goToMyPictures;
    // number of pages in the view pager
    private final int PAGE_COUNT = 4;
    // view pager and sliding tab layout
    public CustomViewPager viewPager;
    public SlidingTabLayout slidingTabLayout;
    private TabsPagerAdapter adapter;
    // if info in a child fragment was updated
    private boolean infoUpdated;


    public boolean goToUserPictures() {
        goToMyPictures = true;

        if (viewPager != null) {
            viewPager.setCurrentItem(0);
            return true;
        }

        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // in order to add the edit menu item
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_data, container, false);
        viewPager = (CustomViewPager) viewGroup.findViewById(R.id.view_pager_user_data);
        adapter = new TabsPagerAdapter(getChildFragmentManager());

        // set adapter for view pager
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(PAGE_COUNT);

        // bind sliding tab layout with xml tag, and set the previously created view pager
        slidingTabLayout = (SlidingTabLayout) viewGroup.findViewById(R.id.sliding_tabs_user_data);
        slidingTabLayout.setDistributeEvenly(false);
        slidingTabLayout.setCustomTabView(R.layout.tab, 0);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.RED;
            }
        });
        slidingTabLayout.setViewPager(viewPager);

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (goToMyPictures) {
            // go to pictures fragment
            viewPager.setCurrentItem(0);

            goToMyPictures = false;
        } else {
            // set the first fragment to be the login info
            viewPager.setCurrentItem(PAGE_COUNT - 1);
        }

        Log.d("A7%StartFragment-Data", "UserDataFragment: " + this.getId());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = getActivity().isChangingConfigurations();

        if (!drawerOpen && menu.findItem(R.id.action_user_data_edit) == null) { // if drawer is closed add the content menu item
            menu.add(Menu.NONE, R.id.action_user_data_edit, Menu.NONE, R.string.edit)
                    .setIcon(R.drawable.ic_action_edit)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                // implementation is in the activity
                return false;
            case R.id.action_friends:
                // implementation is in the activity
                return false;
            case R.id.action_user_data_edit:
                // only invoked in editable data fragments
                EditableDataFragment currentFragment = (EditableDataFragment) adapter.getCurrentFragment();

                // edit texts need to have the values that their related text views has
                currentFragment.updateEditTexts();

                // close drawers if they're open
                ((UserHomeActivity) getActivity()).closeDrawers();

                // disable navigating to other tabs or open drawers until editing is finished
                slidingTabLayout.lockTabs(false);
                viewPager.setPagingEnabled(false);
                ((UserHomeActivity) getActivity()).lockDrawers(true);

                startActionMode();

                currentFragment.startEditMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // action mode is only invoked when in basic and advanced data
    private void startActionMode() {
        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle(getString(R.string.edit_user_data));

                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.menu_user_data_edit, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }


            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_mode_user_data_submit:
                        final EditableDataFragment f = (EditableDataFragment) adapter.getCurrentFragment();

                        // check entered values
                        if (f.validData()) {

                            // info was updated, get data entered by user and modify text views
                            f.getData(((MyApplication)getActivity().getApplication()).getCurrentUser());

                            // show a progress dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog, null, false);
                            (linearLayout.findViewById(R.id.dialog_progress_bar)).setVisibility(View.VISIBLE);
                            (linearLayout.findViewById(R.id.dialog_icon)).setVisibility(View.GONE);
                            ((TextView) linearLayout.findViewById(R.id.dialog_title)).setText(getString(R.string.please_wait));
                            final AlertDialog alert = builder.setCustomTitle(linearLayout)
                                    .setCancelable(false)
                                    .setMessage(R.string.updating_your_info)
                                    .create();
                            alert.show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        final ServerResponse<Boolean> sr = f.updateUser(((MyApplication)getActivity().getApplication()).getCurrentUser());

                                        if (sr.isSuccess()) {

                                            f.cacheValues();

                                            // info was updated, save boolean and finish action mode
                                            infoUpdated = true;

                                        } else {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), sr.getErrorMsg(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            infoUpdated = false;
                                        }
                                    } catch (final ServerResponseFailedException ex) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        infoUpdated = false;
                                    }

                                    alert.dismiss();

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mode.finish();
                                        }
                                    });
                                }
                            }).start();
                        } else {
                            f.showValidationError();
                        }
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // unlock drawers and navigation
                viewPager.setPagingEnabled(true);
                slidingTabLayout.lockTabs(true);
                ((UserHomeActivity) getActivity()).lockDrawers(false);

                // end edit mode by disabling\hiding editable views
                EditableDataFragment f = (EditableDataFragment) adapter.getCurrentFragment();

                f.endEditMode();

                if (!infoUpdated) {
                    // info was not updated, restore cached values
                    f.loadCachedValues();
                }

                // reset info update boolean
                infoUpdated = false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO photo thing
        adapter.fragments[0].onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("A7%PauseFragment-Data", "UserDataFragment: " + this.getId());
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d("A7%StopFragment-Data", "UserDataFragment: " + this.getId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("A7%DestroyFragment-Data", "UserDataFragment: " + this.getId());
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {

        private String tabTitles[] = getResources().getStringArray(R.array.user_data_titles);
        private Fragment[] fragments = new Fragment[PAGE_COUNT];

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
                        frag = new UserDataPicturesFragment();
                        break;
                    case 1:
                        frag = new UserDataAdvancedFragment();
                        break;
                    case 2:
                        frag = new UserDataBasicFragment();
                        break;
                    default:
                        frag = new UserDataAccountFragment();
                }

                fragments[position] = frag;
            }

            return frag;
        }

        public Fragment getCurrentFragment() {
            return fragments[viewPager.getCurrentItem()];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

}
