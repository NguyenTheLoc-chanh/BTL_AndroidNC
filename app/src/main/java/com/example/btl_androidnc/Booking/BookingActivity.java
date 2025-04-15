package com.example.btl_androidnc.Booking;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_androidnc.Collector.Collector;
import com.example.btl_androidnc.Collector.CollectorAdapter;
import com.example.btl_androidnc.History.HistoryActivity;
import com.example.btl_androidnc.HomeActivity;
import com.example.btl_androidnc.ProfileActivity;
import com.example.btl_androidnc.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class BookingActivity extends AppCompatActivity{
    // UI Components
    private RecyclerView recyclerView;
    private Button btnSelectDate;
    private Button btnTime1, btnTime2, btnTime3, btnTime4;
    private TextView tvSelectedDate, tvSelectedTime;
    // Thêm các biến mới
    private Button btnSelectScrapTypes;
    private TextView tvSelectedScraps;
    private LinearLayout containerPaper, containerPlastic, containerMetal;
    private List<String> selectedScraps = new ArrayList<>();

    // Thêm các biến mới
    private EditText etPaper, etPlastic, etMetal;
    private Button btnTakePhoto, btnChoosePhoto;
    private ImageView ivScrapPhoto;
    private Uri photoUri;

    // Adapter and Data
    private CollectorAdapter adapter;
    private List<Collector> collectorList;

    // Firebase
    private FirebaseFirestore db;

    // Selected values
    private String selectedDate = "";
    private String selectedTimeSlot = "";

    // Chụp ảnh
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private String currentPhotoPath;

    // Trong phương thức initViews()
    private Button btnConfirmBooking, btnClose;

    //Nav
    private ImageView imgNextLeft;
    private TextView navLichSu;
    private TextView navTaiKhoan;
    private ImageView navHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        initViews();
        setupRecyclerView();
        setupDatePicker();
        setupTimeSlots();
        setupScrapSelection();
        setupPhotoSelection();
        fetchCollectorsFromFirestore();

        // Đặt sự kiện chung cho các mục
        setNavClickListener(navLichSu, HistoryActivity.class,"HISTORY");
        setNavClickListener(navTaiKhoan, ProfileActivity.class,"PROFILE");
        setNavHomeClickListener(navHome, HomeActivity.class);

        imgNextLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay về trang trước
            }
        });
        btnClose.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        }));
    }
    private void setNavClickListener(TextView textView, Class<?> destinationActivity, String tag) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllNavItems();
                textView.setSelected(true);

                Intent intent = new Intent(BookingActivity.this, destinationActivity);
                intent.putExtra("NAV_TAG", tag); // Truyền tag để xác định nav item
                startActivity(intent);
            }
        });
    }
    // Reset trạng thái selected của tất cả nav items
    private void resetAllNavItems() {
//        navTichDiem.setSelected(false);
//        navDoiQua.setSelected(false);
        navLichSu.setSelected(false);
        navTaiKhoan.setSelected(false);
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
//                case "GIFTS":
//                    navDoiQua.setSelected(true);
//                case "EARN_POINTS":
//                    navTichDiem.setSelected(true);

            }
        }
    }
    private void setNavHomeClickListener(ImageView textView, Class<?> destinationActivity) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BookingActivity.this, destinationActivity));
            }
        });
    }
    // Cảm biến ánh sáng
    @Override
    protected void onResume() {
        super.onResume();
        highlightCurrentNavItem();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnTime1 = findViewById(R.id.btnTime1);
        btnTime2 = findViewById(R.id.btnTime2);
        btnTime3 = findViewById(R.id.btnTime3);
        btnTime4 = findViewById(R.id.btnTime4);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        // Thêm các view mới
        btnSelectScrapTypes = findViewById(R.id.btnSelectScrapTypes);
        tvSelectedScraps = findViewById(R.id.tvSelectedScraps);
        containerPaper = findViewById(R.id.containerPaper);
        containerPlastic = findViewById(R.id.containerPlastic);
        containerMetal = findViewById(R.id.containerMetal);

        // Thêm các view liên quan đến ảnh
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        ivScrapPhoto = findViewById(R.id.ivScrapPhoto);
        etPaper = findViewById(R.id.etPaper);
        etPlastic = findViewById(R.id.etPlastic);
        etMetal = findViewById(R.id.etMetal);
        // Btn Xác nhận và Close
        btnClose = findViewById(R.id.btnClose);
        btnConfirmBooking = findViewById(R.id.btnConfirm);
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());

        //Nav
        imgNextLeft = findViewById(R.id.ImgNext);

        navLichSu = findViewById(R.id.nav_lichsu);
        navHome = findViewById(R.id.nav_logo);
        navTaiKhoan = findViewById(R.id.nav_taikhoan);
    }

    private void setupPhotoSelection() {
        // Xử lý chọn ảnh từ thư viện
        btnChoosePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_IMAGE_PICK);
            } else {
                openGallery();
            }
        });

        // Xử lý chụp ảnh mới
        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraCaptureActivity.class);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE); // Sử dụng cùng request code với camera intent
        });
    }
    private void openGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_PICK);
        }
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);

                // Cấp quyền tạm thời cho camera
                List<ResolveInfo> resInfoList = getPackageManager()
                        .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    grantUriPermission(resolveInfo.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Sử dụng getExternalFilesDir thay vì getExternalStorageDirectory
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // Xử lý chọn ảnh từ thư viện
                photoUri = data.getData();
                try {
                    getContentResolver().takePersistableUriPermission(photoUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    setImageToImageView(photoUri);
                } catch (SecurityException e) {
                    grantUriPermission(getPackageName(), photoUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    setImageToImageView(photoUri);
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Xử lý ảnh từ CameraCaptureActivity
                if (data != null && data.getStringExtra("photoUri") != null) {
                    photoUri = Uri.parse(data.getStringExtra("photoUri"));
                    setImageToImageView(photoUri);
                } else if (photoUri != null) {
                    // Fallback nếu có photoUri từ camera hệ thống
                    setImageToImageView(photoUri);
                    galleryAddPic();
                }
            }
        }
    }

//    private Uri saveBitmapToFile(Bitmap bitmap) {
//        try {
//            File photoFile = createImageFile();
//            FileOutputStream fos = new FileOutputStream(photoFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
//            fos.flush();
//            fos.close();
//            return FileProvider.getUriForFile(this,
//                    getPackageName() + ".fileprovider",
//                    photoFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
    private void setImageToImageView(Uri imageUri) {
        try {
            // Mở luồng đọc với quyền tạm thời
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            // Đọc thông tin ảnh
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Tính toán tỷ lệ giảm kích thước
            options.inSampleSize = calculateInSampleSize(options,
                    ivScrapPhoto.getWidth(),
                    ivScrapPhoto.getHeight());
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            // Đọc lại ảnh với kích thước đã giảm
            inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            if (bitmap != null) {
                ivScrapPhoto.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(photoUri);
        this.sendBroadcast(mediaScanIntent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    dispatchTakePictureIntent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this, "Cần cấp quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_PICK) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }
    private void setupScrapSelection() {
        btnSelectScrapTypes.setOnClickListener(v -> showScrapTypesDialog());
    }
    private void showScrapTypesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_scrap_types, null);
        builder.setView(dialogView);

        CheckBox cbPaper = dialogView.findViewById(R.id.cbPaper);
        CheckBox cbPlastic = dialogView.findViewById(R.id.cbPlastic);
        CheckBox cbMetal = dialogView.findViewById(R.id.cbMetal);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // Khôi phục trạng thái đã chọn trước đó
        cbPaper.setChecked(selectedScraps.contains("Giấy"));
        cbPlastic.setChecked(selectedScraps.contains("Nhựa"));
        cbMetal.setChecked(selectedScraps.contains("Sắt"));

        AlertDialog dialog = builder.create();

        btnConfirm.setOnClickListener(v -> {
            selectedScraps.clear();

            if (cbPaper.isChecked()) selectedScraps.add("Giấy");
            if (cbPlastic.isChecked()) selectedScraps.add("Nhựa");
            if (cbMetal.isChecked()) selectedScraps.add("Sắt");

            updateScrapSelectionUI();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateScrapSelectionUI() {
        // Hiển thị các loại đã chọn
        if (selectedScraps.isEmpty()) {
            tvSelectedScraps.setText("Chưa chọn loại phế liệu nào");
        } else {
            tvSelectedScraps.setText("Đã chọn: " + TextUtils.join(", ", selectedScraps));
        }

        // Hiển thị/ẩn các trường nhập số lượng tương ứng
        containerPaper.setVisibility(selectedScraps.contains("Giấy") ? View.VISIBLE : View.GONE);
        containerPlastic.setVisibility(selectedScraps.contains("Nhựa") ? View.VISIBLE : View.GONE);
        containerMetal.setVisibility(selectedScraps.contains("Sắt") ? View.VISIBLE : View.GONE);
    }


    // Phương thức lấy dữ liệu phế liệu
    public Map<String, Double> getScrapData() {
        Map<String, Double> scrapData = new HashMap<>();

        try {
            if (selectedScraps.contains("Giấy") && !etPaper.getText().toString().isEmpty()) {
                scrapData.put("paper", Double.parseDouble(etPaper.getText().toString()));
            }
            if (selectedScraps.contains("Nhựa") && !etPlastic.getText().toString().isEmpty()) {
                scrapData.put("plastic", Double.parseDouble(etPlastic.getText().toString()));
            }
            if (selectedScraps.contains("Sắt") && !etMetal.getText().toString().isEmpty()) {
                scrapData.put("metal", Double.parseDouble(etMetal.getText().toString()));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return scrapData;
    }

    private void setupRecyclerView() {
        collectorList = new ArrayList<>();
        adapter = new CollectorAdapter(this, collectorList);
        // Thêm click listener cho item
        adapter.setOnCollectorClickListener((collector, position) -> {
            adapter.setSelectedPosition(position);
            // Lưu collector được chọn nếu cần
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void setupDatePicker() {
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    tvSelectedDate.setText(getString(R.string.selected_date, selectedDate));
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        datePickerDialog.show();
    }

    private void setupTimeSlots() {
        View.OnClickListener timeSlotClickListener = v -> {
            resetTimeSlotButtons();

            Button selectedButton = (Button) v;
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

            selectedTimeSlot = selectedButton.getText().toString();
            tvSelectedTime.setText(getString(R.string.selected_time_slot, selectedTimeSlot));
        };

        btnTime1.setOnClickListener(timeSlotClickListener);
        btnTime2.setOnClickListener(timeSlotClickListener);
        btnTime3.setOnClickListener(timeSlotClickListener);
        btnTime4.setOnClickListener(timeSlotClickListener);
    }

    private void resetTimeSlotButtons() {
        int defaultColor = Color.parseColor("#FFCFD6CF");
        btnTime1.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
        btnTime2.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
        btnTime3.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
        btnTime4.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
    }

    private void fetchCollectorsFromFirestore() {
        db = FirebaseFirestore.getInstance();
        db.collection("collector")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        processCollectors(task.getResult().getDocuments());
                    } else {
                        Log.e("Firestore", "Error getting documents", task.getException());
                    }
                });
    }

    private void processCollectors(List<DocumentSnapshot> documents) {
        collectorList.clear();
        for (DocumentSnapshot doc : documents) {
            Collector collector = doc.toObject(Collector.class);
            if (collector != null) {
                collector.setId(doc.getId());
                collectorList.add(collector);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Thêm phương thức validate trước khi submit
    public boolean validateBooking() {
        if (selectedDate.isEmpty()) {
            showError("Vui lòng chọn ngày thu gom");
            return false;
        }

        if (selectedTimeSlot.isEmpty()) {
            showError("Vui lòng chọn khung giờ thu gom");
            return false;
        }

        if (selectedScraps.isEmpty()) {
            showError("Vui lòng chọn ít nhất một loại phế liệu");
            return false;
        }

        // Kiểm tra số lượng phế liệu đã nhập
        try {
            if (selectedScraps.contains("Giấy") && etPaper.getText().toString().isEmpty()) {
                showError("Vui lòng nhập số lượng giấy");
                return false;
            }
            if (selectedScraps.contains("Nhựa") && etPlastic.getText().toString().isEmpty()) {
                showError("Vui lòng nhập số lượng nhựa");
                return false;
            }
            if (selectedScraps.contains("Sắt") && etMetal.getText().toString().isEmpty()) {
                showError("Vui lòng nhập số lượng sắt");
                return false;
            }
        } catch (Exception e) {
            showError("Số lượng không hợp lệ");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Chuyển đổi ảnh sang base 4
    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void confirmBooking() {
        if (!validateBooking()) {
            return;
        }

        // Kiểm tra đã chọn người thu gom chưa
        if (adapter.getSelectedPosition() == -1) {
            Toast.makeText(this, "Vui lòng chọn người thu gom", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin người thu gom được chọn
        Collector selectedCollector = collectorList.get(adapter.getSelectedPosition());

        // Tạo đối tượng booking
        Map<String, Object> booking = new HashMap<>();
        booking.put("collectorName", selectedCollector.getName());
        booking.put("collectorPhone", selectedCollector.getPhone());
        booking.put("collectorId", selectedCollector.getId());
        booking.put("date", selectedDate);
        booking.put("timeSlot", selectedTimeSlot);
        booking.put("scrapTypes", selectedScraps);
        booking.put("scrapData", getScrapData());
        booking.put("status", "Chờ xác nhận"); // Trạng thái ban đầu
        booking.put("createdAt", new Date());
        booking.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        if (photoUri != null) {
            String imageBase64 = convertImageToBase64(photoUri);
            if (imageBase64 != null) {
                booking.put("photoBase64", imageBase64);
            } else {
                Toast.makeText(this, "Không thể xử lý ảnh", Toast.LENGTH_SHORT).show();
            }
        }

        saveBookingToFirestore(booking);
    }

    private void saveBookingToFirestore(Map<String, Object> booking) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    DocumentReference userRef = db.collection("users").document(userId);
                    db.runTransaction(transaction -> {
                        DocumentSnapshot snapshot = transaction.get(userRef);
                        Long currentPoints = snapshot.contains("point") ? snapshot.getLong("point") : 0L;
                        long newPoints = currentPoints + 5;
                        transaction.update(userRef, "point", newPoints);
                        return null;
                    }).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đặt thu gom thành công! Bạn được cộng 5 điểm 🎉", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e ->{
                        Toast.makeText(this, "Đặt lịch thành công, nhưng lỗi khi cộng điểm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi đặt thu gom: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}