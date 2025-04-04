package com.example.mapease;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mapease.Remote.RouteData;

import java.util.ArrayList;
import java.util.List;

public class RoutePagerAdapter extends FragmentStateAdapter {
    private final List<RouteData> routeDataList;

    public RoutePagerAdapter(@NonNull FragmentActivity fragmentActivity, List<RouteData> routeData) {
        super(fragmentActivity);
        this.routeDataList = routeData;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        RouteData data = routeDataList.get(position);
        return RouteFragment.newInstance(data.getDuration(), data.getDistance(), data.getSteps());
    }

    @Override
    public int getItemCount() {
        return routeDataList.size();
    }
}

