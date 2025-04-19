package com.example.btl_androidnc.Collector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.btl_androidnc.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDetailsCollector extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details_collector);

        ImageView imgNextLeft = findViewById(R.id.imgNextLeft);
        Button btnConfirm = findViewById(R.id.btnConfirmCol);
        Button btnCancel = findViewById(R.id.btnCancelConfirmCol);
        imgNextLeft.setOnClickListener(v -> {
            if (isTaskRoot()) {
                // Nếu đây là Activity gốc (không có màn trước), mở MainActivity
                Intent intent = new Intent(BookingDetailsCollector.this, ListBooking.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                // Ngược lại, quay lại màn hình trước như bình thường
                finish();
            }
        });

        String idFromIntent = getIntent().getStringExtra("booking_id");
        String idFromNotification = getIntent().getStringExtra("bookingId");
        final String finalBookingId = (idFromNotification != null) ? idFromNotification : idFromIntent;

        btnConfirm.setOnClickListener(v -> updateStatusAndNotify(finalBookingId));
        btnCancel.setOnClickListener(v -> updateStatusAndNotifyCancel(finalBookingId));

        loadBookingDetail(finalBookingId);

    }
    private void loadBookingDetail(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings").document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String phone = documentSnapshot.getString("collectorPhone");
                        String date = documentSnapshot.getString("date");
                        String time = documentSnapshot.getString("timeSlot");
                        String status = documentSnapshot.getString("status");
                        String base64Image = documentSnapshot.getString("photoBase64");
                        String userId = documentSnapshot.getString("userId");

                        if(userId != null){
                            db.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String userName = userDoc.getString("name");
                                            String userAddress = userDoc.getString("address");
                                            String userPhone = userDoc.getString("phone");

                                            // Gán dữ liệu người dùng lên UI
                                            ((TextView) findViewById(R.id.tvDetailNameCol)).setText(userName);
                                            ((TextView) findViewById(R.id.tvDetailPhoneCol)).setText("SĐT: "+ userPhone);
                                            ((TextView) findViewById(R.id.tvDetailAddressCol)).setText("Địa chỉ: " + userAddress);
                                        } else {
                                            Log.w("FIREBASE", "Không tìm thấy người dùng với userId: " + userId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FIREBASE", "Lỗi khi lấy thông tin người dùng", e);
                                    });
                        }

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
//                        ((TextView) findViewById(R.id.tvDetailPhoneCol)).setText(phone);
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
                        // ==== GÁN SỰ KIỆN THEO TRẠNG THÁI ====
                        Button btnConfirm = findViewById(R.id.btnConfirmCol);
                        Button btnCancel = findViewById(R.id.btnCancelConfirmCol);
                        LinearLayout layoutButtons = findViewById(R.id.layoutButtons);
                        layoutButtons.setVisibility(View.VISIBLE);

                        if ("Chờ xác nhận".equalsIgnoreCase(status)) {
                            btnConfirm.setText("Xác nhận");
                            btnCancel.setText("Hủy xác nhận");

                            btnConfirm.setOnClickListener(v -> updateStatusAndNotify(bookingId));
                            btnCancel.setOnClickListener(v -> updateStatusAndNotifyCancel(bookingId));
                        } else if ("Đã xác nhận".equalsIgnoreCase(status)) {
                            btnConfirm.setText("Hoàn thành");
                            btnCancel.setText("Hủy đơn");

                            btnConfirm.setOnClickListener(v -> markAsCompleted(bookingId));
                            btnCancel.setOnClickListener(v -> updateStatusAndNotifyCancel(bookingId));
                        } else {
                            layoutButtons.setVisibility(View.GONE);
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

    private void updateStatusAndNotify(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings").document(bookingId)
                .update("status", "Đã xác nhận")
                .addOnSuccessListener(unused -> {
                    // Gửi thông báo đến người dùng
                    db.collection("bookings").document(bookingId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    String userId = doc.getString("userId");
                                    if (userId != null) {
                                        sendNotificationToUser(userId, "Đơn thu gom của bạn đã được xác nhận!", bookingId);
                                    }
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Lỗi cập nhật trạng thái", e);
                });
    }
    private void updateStatusAndNotifyCancel(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings").document(bookingId)
                .update("status", "Đã hủy")
                .addOnSuccessListener(unused -> {
                    db.collection("bookings").document(bookingId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    String userId = doc.getString("userId");
                                    if (userId != null) {
                                        sendNotificationToUser(userId, "Đơn hàng của bạn đã bị hủy.", bookingId);
                                    }
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Lỗi khi hủy xác nhận", e);
                });
    }
    private void markAsCompleted(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings").document(bookingId)
                .update("status", "Hoàn thành")
                .addOnSuccessListener(unused -> {
                    db.collection("bookings").document(bookingId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    String userId = doc.getString("userId");
                                    if (userId != null) {
                                        sendNotificationToUser(userId, "Đơn thu gom của bạn đã hoàn thành. Cảm ơn bạn đã sử dụng dịch vụ!", bookingId);
                                    }
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Lỗi khi hủy xác nhận", e);
                });
    }

    private void sendNotificationToUser(String userId, String message, String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Giả sử bạn có collection "notifications" lưu theo userId
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("timestamp", new Date());
        notification.put("seen", false);
        notification.put("userId", userId);
        notification.put("bookingId", bookingId);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("NOTIFY", "Lỗi khi gửi thông báo", e);
                });
    }
}