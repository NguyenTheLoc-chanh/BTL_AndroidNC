package com.example.btl_androidnc.Collector;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.btl_androidnc.Booking.BookingActivity;
import com.example.btl_androidnc.Gift.GiftsActivity;
import com.example.btl_androidnc.History.HistoryActivity;
import com.example.btl_androidnc.ProfileActivity;
import com.example.btl_androidnc.R;
import com.example.btl_androidnc.VPAdapterBanner;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.Arrays;
import java.util.List;

public class CollectorHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView txtName, txtUserPoints;
    private LinearLayout btnBooking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        txtUserPoints = findViewById(R.id.txtUserPoints);

        AnhXa();
        loadUserData();

        // Đặt sự kiện chung cho các mục
        setNavClickListener(navLichSu, HistoryActivity.class,"HISTORY");
        setNavClickListener(navTaiKhoan, ProfileActivity.class,"PROFILE");
        setNavClickListener(navDoiQua, GiftsActivity.class, "GIFTS");

        btnBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollectorHomeActivity.this, ListBooking.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
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
                R.drawable.bn1,
                R.drawable.bn2,
                R.drawable.bn3
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
        listenToNotifications();
    }
    private void AnhXa() {
        navTichDiem = findViewById(R.id.nav_tichdiem);
        navDoiQua = findViewById(R.id.nav_doiqua);
        navLichSu = findViewById(R.id.nav_lichsu);
        navTaiKhoan = findViewById(R.id.nav_taikhoan);

        bannerViewPager = findViewById(R.id.bannerViewPager);
        dotsIndicator = findViewById(R.id.dotsIndicatorhome);

        drawerLayout = findViewById(R.id.drawerLayout);
        menuIcon = findViewById(R.id.menuIcon);
        navigationView = findViewById(R.id.navigationView);

        txtUserPoints = findViewById(R.id.txtUserPoints);
        txtName = findViewById(R.id.txtName);

        btnBooking = findViewById(R.id.btn_booking);
    }
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .addSnapshotListener((documentSnapshot, e) -> { // Thay get() bằng addSnapshotListener
                        if (e != null) {
                            Log.e("Firestore", "Lỗi theo dõi dữ liệu", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            Long points = documentSnapshot.getLong("point");
                            if (points != null && name != null) {
                                txtUserPoints.setText("Điểm: " + points);
                                txtName.setText(name);
                            }
                        }
                    });
        }
    }

    private void setNavClickListener(TextView textView, Class<?> destinationActivity, String tag) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllNavItems();
                textView.setSelected(true);

                Intent intent = new Intent(CollectorHomeActivity.this, destinationActivity);
                intent.putExtra("NAV_TAG", tag); // Truyền tag để xác định nav item
                startActivity(intent);
            }
        });
    }
    // Reset trạng thái selected của tất cả nav items
    private void resetAllNavItems() {
        navTichDiem.setSelected(false);
        navDoiQua.setSelected(false);
        navLichSu.setSelected(false);
        navTaiKhoan.setSelected(false);
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
                    navDoiQua.setSelected(true);
                case "EARN_POINTS":
                    navTichDiem.setSelected(true);

            }
        }
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

    // Lắng nghe hiện thông báo notification
    private void listenToNotifications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        // Bước 1: Tìm collectorId từ userId trong collection "collectors"
        db.collection("collector")
                .whereEqualTo("userID", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy documentId đầu tiên (collectorId)
                        String collectorId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Bước 2: Lắng nghe các thông báo dành cho collector
                        db.collection("notifications")
                                .whereEqualTo("userId", collectorId)
                                .whereEqualTo("seen", false)
                                .addSnapshotListener((snapshots, e) -> {
                                    if (e != null) {
                                        Log.w("NOTIFY", "Listen failed.", e);
                                        return;
                                    }

                                    if (snapshots != null && !snapshots.isEmpty()) {
                                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                                String message = dc.getDocument().getString("message");
                                                String bookingId = dc.getDocument().getString("bookingId");
                                                showLocalNotification(message, bookingId);

                                                // Đánh dấu đã xem
                                                dc.getDocument().getReference().update("seen", true);
                                            }
                                        }
                                    }
                                });
                    } else {
                        Log.d("NOTIFY", "Không tìm thấy collector với userId hiện tại");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NOTIFY", "Lỗi khi tìm collectorId từ userId", e);
                });
    }

    private void showLocalNotification(String message, String bookingId) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "default_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Thông báo",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }
        // Tạo Intent để mở BookingDetailActivity khi click vào thông báo
        Intent intent = new Intent(this, BookingDetailsCollector.class);
        intent.putExtra("bookingId", bookingId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(), // unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logoapp) // bạn nên có icon trong res/drawable
                .setContentTitle("Thông báo mới")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

