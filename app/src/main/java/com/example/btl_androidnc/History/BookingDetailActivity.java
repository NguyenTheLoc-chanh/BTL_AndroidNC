package com.example.btl_androidnc.History;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_androidnc.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        ImageView imgNextLeft = findViewById(R.id.imgNextLeft);
        imgNextLeft.setOnClickListener(v -> finish());

        String bookingId = getIntent().getStringExtra("booking_id");

        // Dùng bookingId để truy vấn dữ liệu hiển thị
        loadBookingDetail(bookingId);
    }

    private void loadBookingDetail(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings").document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("collectorName");
                        String phone = documentSnapshot.getString("collectorPhone");
                        String date = documentSnapshot.getString("date");
                        String time = documentSnapshot.getString("timeSlot");
                        String status = documentSnapshot.getString("status");
                        String base64Image = documentSnapshot.getString("photoBase64");

                        Map<String, Object> rawScrapData = (Map<String, Object>) documentSnapshot.get("scrapData");
                        Map<String, Integer> scrapData = new HashMap<>();

                        if (rawScrapData != null) {
                            for (Map.Entry<String, Object> entry : rawScrapData.entrySet()) {
                                Object value = entry.getValue();
                                if (value instanceof Number) {
                                    scrapData.put(entry.getKey(), ((Number) value).intValue());
                                }
                            }
                        }


                        List<String> scrapTypes = new ArrayList<>();
                        Object rawScrapTypes = documentSnapshot.get("scrapTypes");

                        if (rawScrapTypes instanceof List<?>) {
                            for (Object item : (List<?>) rawScrapTypes) {
                                if (item instanceof String) {
                                    scrapTypes.add((String) item);
                                }
                            }
                        }

                        // Gán dữ liệu lên UI
                        ((TextView) findViewById(R.id.tvDetailName)).setText(name);
                        ((TextView) findViewById(R.id.tvDetailPhone)).setText(phone);
                        ((TextView) findViewById(R.id.tvDetailDate)).setText("Ngày: " + date);
                        ((TextView) findViewById(R.id.tvDetailTime)).setText("Thời gian: " + time);
                        ((TextView) findViewById(R.id.tvDetailStatus)).setText("Trạng thái: " + status);
                        // Hiển thị ảnh người thu gom
                        ImageView imgCollector = findViewById(R.id.imgCollector);
                        if (base64Image != null && !base64Image.isEmpty()) {
                            Bitmap bitmap = base64ToBitmap(base64Image);
                            if (bitmap != null) {
                                imgCollector.setImageBitmap(bitmap);
                            }
                        }

                        // Hiển thị danh sách loại phế liệu
                        LinearLayout layoutScrapItems = findViewById(R.id.layoutScrapItems);
                        layoutScrapItems.removeAllViews();

                        for (String typeVN : scrapTypes) {
                            String key = convertVietnameseToKey(typeVN); // Giả sử đã map được tên tiếng Anh
                            int quantity = scrapData.getOrDefault(key, 0);
                            Log.d("DEBUG_MATCH", "VN: " + typeVN + " | Key: " + key + " | Qty: " + quantity);

                            TextView itemView = new TextView(this);
                            itemView.setText("- " + typeVN + ": " + quantity + " kg");
                            itemView.setTextSize(14);
                            itemView.setPadding(8, 8, 8, 8);
                            layoutScrapItems.addView(itemView);
                        }
                    } else {
                        Log.w("FIREBASE", "Không tìm thấy thông tin booking.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Lỗi khi lấy chi tiết booking", e);
                });
    }
    // Hàm ánh xạ từ tên tiếng Việt sang key tiếng Anh
    private String convertVietnameseToKey(String vn) {
        switch (vn.trim().toLowerCase()) {
            case "giấy":
                return "paper";
            case "nhựa":
                return "plastic";
            case "sắt":
                return "metal";
            default:
                return vn.toLowerCase(); // fallback
        }
    }
    private Bitmap base64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

