package com.example.btl_androidnc.Gift;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_androidnc.History.HistoryActivity;
import com.example.btl_androidnc.HomeActivity;
import com.example.btl_androidnc.ProfileActivity;
import com.example.btl_androidnc.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GiftsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewGifts;
    private GiftAdapter adapter;
    private List<GiftLocation> giftLocationList;
    private FirebaseFirestore db;
    private ImageView imgNextLeft;
    private TextView navLichSu, navTaiKhoan;
    private ImageView navHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts);

        db = FirebaseFirestore.getInstance();
        recyclerViewGifts = findViewById(R.id.recyclerViewGifts);
        imgNextLeft = findViewById(R.id.imgNextLeft);
        navLichSu = findViewById(R.id.nav_lichsu);
        navTaiKhoan = findViewById(R.id.nav_taikhoan);
        navHome = findViewById(R.id.nav_logo);

        giftLocationList = new ArrayList<>();
        adapter = new GiftAdapter(this, giftLocationList);
        recyclerViewGifts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGifts.setAdapter(adapter);

        fetchGiftLocationsFromFirestore();

        imgNextLeft.setOnClickListener(v -> finish());

        // Đặt sự kiện cho các mục điều hướng
        setNavClickListener(navLichSu, HistoryActivity.class, "HISTORY");
        setNavClickListener(navTaiKhoan, ProfileActivity.class, "PROFILE");
        setNavHomeClickListener(navHome, HomeActivity.class);

        Button btnEarnPoints = findViewById(R.id.btnEarnPoints);
        btnEarnPoints.setOnClickListener(v -> {
            // Có thể mở một Activity hoặc xử lý sự kiện để hiển thị thêm các địa điểm khác
        });
    }

    private void fetchGiftLocationsFromFirestore() {
        db.collection("gift_locations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        giftLocationList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            GiftLocation giftLocation = document.toObject(GiftLocation.class);
                            if (giftLocation != null) {
                                giftLocationList.add(giftLocation);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
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
                    // Đánh dấu mục "Đổi quà" là đang chọn
                    findViewById(R.id.nav_doiqua).setSelected(true);
                    break;
            }
        } else {
            findViewById(R.id.nav_doiqua).setSelected(true); // Mặc định chọn mục "Đổi quà"
        }
    }
}