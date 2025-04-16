package com.example.btl_androidnc.Collector;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.btl_androidnc.R;

import java.util.ArrayList;

public class ListBooking extends AppCompatActivity {
    ViewPager2 viewPager;
    TextView tabClassify, tabGift, tabConfirmed;
    BookingPagerAdapter adapter;
    private ActivityResultLauncher<Intent> detailLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_booking);

        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Có thể reload dữ liệu hoặc thông báo cập nhật
                        // Ví dụ: Gửi sự kiện tới Fragment qua interface hoặc LiveData
                    }
                });

        viewPager = findViewById(R.id.viewPager);
        tabClassify = findViewById(R.id.tabClassify);
        tabConfirmed = findViewById(R.id.tabConfirmedBok);
        tabGift = findViewById(R.id.tabGift);
        ImageView imgNextLeft = findViewById(R.id.imgNextLeft);
        imgNextLeft.setOnClickListener(v -> finish());

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new BookingPendingFragment(detailLauncher));
        fragments.add(new BookingCompletedFragment(detailLauncher));
        fragments.add(new BookingConfirmedFragment(detailLauncher));

        adapter = new BookingPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);

        tabClassify.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabGift.setOnClickListener(v -> viewPager.setCurrentItem(1));
        tabConfirmed.setOnClickListener(v -> viewPager.setCurrentItem(2));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    tabClassify.setTextColor(getColor(R.color.selected_item_color));
                    tabGift.setTextColor(getColor(R.color.black));
                    tabConfirmed.setTextColor(getColor(R.color.black));
                } else if (position == 1){
                    tabClassify.setTextColor(getColor(R.color.black));
                    tabGift.setTextColor(getColor(R.color.selected_item_color));
                    tabConfirmed.setTextColor(getColor(R.color.black));
                }else{
                    tabConfirmed.setTextColor(getColor(R.color.selected_item_color));
                    tabClassify.setTextColor(getColor(R.color.black));
                    tabGift.setTextColor(getColor(R.color.black));
                }
            }
        });
    }
}
