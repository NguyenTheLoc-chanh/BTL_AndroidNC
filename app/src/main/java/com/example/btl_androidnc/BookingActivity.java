package com.example.btl_androidnc;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class BookingActivity extends AppCompatActivity implements SensorEventListener{
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
    //private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private String currentPhotoPath;
    // Cảm biến
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private float currentLightLevel;

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

        // Khởi tạo cảm biến ánh sáng
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (lightSensor == null) {
                Toast.makeText(this, "Thiết bị không hỗ trợ cảm biến ánh sáng", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Cảm biến ánh sáng
    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký listener với kiểm tra null
        if (sensorManager != null && lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký listener nếu sensorManager không null
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            currentLightLevel = event.values[0];

            // Cảnh báo nếu ánh sáng yếu (chạy trên UI thread)
            runOnUiThread(() -> {
                if (currentLightLevel < 10) { // Giá trị ngưỡng có thể điều chỉnh
                    Toast.makeText(this, "Môi trường quá tối, hãy bật đèn flash", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Xử lý thay đổi độ chính xác nếu cần
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
            } else {
                dispatchTakePictureIntent();
            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), REQUEST_IMAGE_PICK);
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Lỗi khi tạo file ảnh", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.your.package.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                photoUri = data.getData();
                ivScrapPhoto.setImageURI(photoUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Hiển thị ảnh vừa chụp
                ivScrapPhoto.setImageURI(photoUri);

                // Thêm ảnh vào MediaStore để hiển thị trong thư viện
                galleryAddPic();
            }
        }
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
                dispatchTakePictureIntent();
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

        // Thêm các validate khác nếu cần
        return true;
    }

    private void showError(String message) {
        // Hiển thị thông báo lỗi (có thể dùng Toast hoặc Snackbar)
    }
}