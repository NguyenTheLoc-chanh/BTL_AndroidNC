package com.example.btl_androidnc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ViewPager2 bannerViewPager;
    private WormDotsIndicator dotsIndicator;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private int currentPage = 0;

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private NavigationView navigationView;

    //Khai báo navigation_bottom
    private TextView navTichDiem;
    private TextView navDoiQua ;
    private TextView navLichSu;
    private TextView navTaiKhoan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

         navTichDiem = findViewById(R.id.nav_tichdiem);
         navDoiQua = findViewById(R.id.nav_doiqua);
         navLichSu = findViewById(R.id.nav_lichsu);
         navTaiKhoan = findViewById(R.id.nav_taikhoan);

        bannerViewPager = findViewById(R.id.bannerViewPager);
        dotsIndicator = findViewById(R.id.dotsIndicatorhome);

        drawerLayout = findViewById(R.id.drawerLayout);
        menuIcon = findViewById(R.id.menuIcon);
        navigationView = findViewById(R.id.navigationView);

        // Đặt sự kiện chung cho các mục
        setNavClickListener(navLichSu, HistoryActivity.class);
//        setNavClickListener(navTaiKhoan, TaiKhoanActivity.class);

        // Bắt sự kiện mở Navigation Drawer khi nhấn vào menuIcon
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);

        // Ds ảnh banner
        List<Integer> bannerList = Arrays.asList(
                R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_foreground,
                R.drawable.list
        );
        // Thiết lập adapter cho ViewPager2
        VPAdapterBanner bannerAdapter = new VPAdapterBanner(this, bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        dotsIndicator.attachTo(bannerViewPager);
        // Auto scroll mỗi 3s
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == bannerList.size()) {
                    currentPage = 0;
                }
                bannerViewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 3000); // Chuyển sau 3s
            }
        };

        handler.postDelayed(runnable, 3000);
    }

    private void setNavClickListener(TextView textView, Class<?> destinationActivity) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, destinationActivity));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_home){

        } else if (id == R.id.nav_his) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_feedback) {

        }else if (id == R.id.nav_logout) {
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }
}

