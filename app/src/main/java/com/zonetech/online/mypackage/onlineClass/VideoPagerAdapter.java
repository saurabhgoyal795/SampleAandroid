package com.zonetech.online.mypackage.onlineClass;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.zonetech.online.R;
import com.zonetech.online.mypackage.onlineTestSeries.TestSeriesFragment;

import java.util.ArrayList;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class VideoPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_video_online, R.string.tab_video_offline};
    private final Context mContext;

    private ArrayList<FragmentVideoList> fragmentVideoLists;
    public VideoPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        fragmentVideoLists = new ArrayList<>();
        fragmentVideoLists.add(new FragmentVideoList());
        fragmentVideoLists.add(new FragmentVideoList());
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = fragmentVideoLists.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("type", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 2;
    }

    public void refreshList(){
        fragmentVideoLists.get(0).setList(0);
        fragmentVideoLists.get(1).setList(1);
    }
}