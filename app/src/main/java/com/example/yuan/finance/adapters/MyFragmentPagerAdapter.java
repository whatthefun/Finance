package com.example.yuan.finance.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.example.yuan.finance.fragments.ChartFragment;
import com.example.yuan.finance.fragments.DetailFragment;

/**
 * Created by YUAN on 2017/7/26.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private String[] mTitle = {"清單", "圖表"};

    public MyFragmentPagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new DetailFragment();
            case 1:
                return new ChartFragment();
            default:
                return new DetailFragment();
        }
    }

    @Override public int getCount() {
        return mTitle.length;
    }

    @Override public CharSequence getPageTitle(int position) {
        return mTitle[position];
    }
}
