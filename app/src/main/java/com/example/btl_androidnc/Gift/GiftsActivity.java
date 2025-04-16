package com.example.btl_androidnc.Gift;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.btl_androidnc.History.HistoryActivity;
import com.example.btl_androidnc.HomeActivity;
import com.example.btl_androidnc.ProfileActivity;
import com.example.btl_androidnc.R;

public class GiftsActivity extends AppCompatActivity {

    private ImageView imgNextLeft;
    private TextView navLichSu, navTaiKhoan;
    private ImageView navHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts);

        // Khởi tạo giao diện
        imgNextLeft = findViewById(R.id.imgNextLeft);
        navLichSu = findViewById(R.id.nav_lichsu);
        navTaiKhoan = findViewById(R.id.nav_taikhoan);
        navHome = findViewById(R.id.nav_logo);

        // Tải GiftListFragment mặc định
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new GiftListFragment());
        transaction.commit();

        // Sự kiện nút quay lại
        imgNextLeft.setOnClickListener(v -> finish());

        // Đặt sự kiện điều hướng
        setNavClickListener(navLichSu, HistoryActivity.class, "HISTORY");
        setNavClickListener(navTaiKhoan, ProfileActivity.class, "PROFILE");
        setNavHomeClickListener(navHome, HomeActivity.class);

        // Sự kiện cho nút Đổi quà Offline
        Button btnOfflineExchange = findViewById(R.id.btnOfflineExchange);
        btnOfflineExchange.setOnClickListener(v -> {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new GiftListFragment());
            ft.commit();
        });

        // Sự kiện cho nút Đổi quà Online
        Button btnOnlineExchange = findViewById(R.id.btnOnlineExchange);
        btnOnlineExchange.setOnClickListener(v -> {
            // Chuyển sang OnlineExchangeFragment hoặc Activity mới
            // Hiện tại, tôi sẽ tạo một Fragment mới, bạn có thể thay bằng Activity nếu muốn
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new OnlineExchangeFragment());
            ft.addToBackStack(null); // Cho phép quay lại
            ft.commit();
        });
    }

    private void setNavClickListener(TextView textView, Class<?> destinationActivity, String tag) {
        textView.setOnClickListener(v -> {
            resetAllNavItems();
            textView.setSelected(true);

            Intent intent = new Intent(GiftsActivity.this, destinationActivity);
            intent.putExtra("NAV_TAG", tag);
            startActivity(intent);
        });
    }

    private void resetAllNavItems() {
        navLichSu.setSelected(false);
        navTaiKhoan.setSelected(false);
    }

    private void setNavHomeClickListener(ImageView imageView, Class<?> destinationActivity) {
        imageView.setOnClickListener(v -> {
            startActivity(new Intent(GiftsActivity.this, destinationActivity));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        highlightCurrentNavItem();
    }

    private void highlightCurrentNavItem() {
        resetAllNavItems();
        String navTag = getIntent().getStringExtra("NAV_TAG");

        if (navTag != null) {
            switch (navTag) {
                case "HISTORY":
                    navLichSu.setSelected(true);
                    break;
                case "PROFILE":
                    navTaiKhoan.setSelected(true);
                    break;
                case "GIFTS":
                    findViewById(R.id.nav_doiqua).setSelected(true);
                    break;
            }
        } else {
            findViewById(R.id.nav_doiqua).setSelected(true);
        }
    }
}