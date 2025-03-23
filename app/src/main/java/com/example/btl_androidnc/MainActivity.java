package com.example.btl_androidnc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button nextButton;
    private int[] buttonTexts = {R.string.page1, R.string.page1, R.string.page2};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        nextButton = findViewById(R.id.nextButton);
        WormDotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);

        // Danh sách Fragment
        List<IntroFragment> fragmentList = new ArrayList<>();
        fragmentList.add(IntroFragment.newInstance(R.drawable.ic_launcher_background));
        fragmentList.add(IntroFragment.newInstance(R.drawable.ic_launcher_foreground));
        fragmentList.add(IntroFragment.newInstance(R.drawable.ic_launcher_foreground));

        // Adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(adapter);
        dotsIndicator.setViewPager2(viewPager);

        // Cập nhật button theo trang
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                nextButton.setText(getString(buttonTexts[position]));
                if (position == fragmentList.size() - 1) {
                    nextButton.setOnClickListener(v -> {
                        FragmentLogin fragmentLogin = new FragmentLogin();
                        fragmentLogin.show(getSupportFragmentManager(), fragmentLogin.getTag());
                    });
                } else {
                    nextButton.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
                }
            }
        });
    }
}