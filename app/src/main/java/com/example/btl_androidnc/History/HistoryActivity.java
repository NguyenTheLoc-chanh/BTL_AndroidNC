package com.example.btl_androidnc.History;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.btl_androidnc.R;

public class HistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        ImageView imgNextLeft = findViewById(R.id.imgNextLeft);
        TextView tabClassify = findViewById(R.id.tabClassify);
        TextView tabGift = findViewById(R.id.tabGift);
        imgNextLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay về trang trước
            }
        });

        // Thiết lập sự kiện click cho các tab
        tabClassify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển fragment khi nhấn vào "Phân loại rác"
                loadFragment(new FragmentPhanLoaiRac());
            }
        });

        tabGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển fragment khi nhấn vào "Đổi quà"
                loadFragment(new FragmentDoiQua());
            }
        });

        // Hiển thị fragment mặc định khi activity khởi tạo
        loadFragment(new FragmentPhanLoaiRac());
    }

    // Hàm chuyển đổi fragment
    private void loadFragment(Fragment fragment) {
        // Gọi FragmentManager để thực hiện giao dịch thay thế fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}
