package com.mediapickerlib.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class CustomPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> mFragmentList = new ArrayList<Fragment>();
    private ArrayList<String> mFragmentTitleList = new ArrayList<String>();

    public CustomPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public ArrayList<Fragment> getFragments() {
        return mFragmentList;
    }

    public void remove(int position) {
        mFragmentList.remove(position);
        mFragmentTitleList.remove(position);
        notifyDataSetChanged();
    }

    /**
     * In getItemPosition()
     * This logic for restrict to load all
     *  Fragment when notifyDataSetChanged() call
     */
    @Override
    public int getItemPosition(@NonNull Object object) {
        int index = mFragmentList.indexOf (object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

}