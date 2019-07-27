package com.example.none.pigmanbox.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * viewpager adaper
 */
public class MyViewPagerAdapter extends FragmentStatePagerAdapter {
    private List<String> title;
    private List<Fragment> fragments;

    public MyViewPagerAdapter(FragmentManager fm, List<String> title, List<Fragment> fragments) {
        super(fm);
        this.title = title;
        this.fragments = fragments;
    }


    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return title.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }

}
