package com.example.btl_androidnc.Collector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class BookingPagerAdapter extends FragmentStateAdapter {
    private final ArrayList<Fragment> fragments;

    public BookingPagerAdapter(@NonNull AppCompatActivity activity, ArrayList<Fragment> fragments) {
        super(activity);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}

