package com.al70b.core.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.al70b.R;


public class ScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;

    // this fragment's page view group
    private ViewGroup viewGroup;


    public ScreenSlidePageFragment() {
    }

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(int pageNumber) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int fragment_id;

        // choose the appropriate layout
        if (mPageNumber == getResources().getInteger(com.al70b.R.integer.leftMostScreenSlide))
            fragment_id = R.layout.fragment_general_register;
        else if (mPageNumber == getResources().getInteger(com.al70b.R.integer.centerScreenSlide))
            fragment_id = R.layout.fragment_general_login;
        else
            fragment_id = R.layout.fragment_general_welcome;

        viewGroup = (ViewGroup) inflater.inflate(fragment_id, container, false);

        return viewGroup;
    }


    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }

    // return this fragment page view group to handle later
    public ViewGroup getPageViewGroup() {
        return viewGroup;
    }
}
