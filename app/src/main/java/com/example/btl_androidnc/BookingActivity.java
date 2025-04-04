package com.example.btl_androidnc;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {
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
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    // Adapter and Data
    private CollectorAdapter adapter;
    private List<Collector> collectorList;

    // Firebase
    private FirebaseFirestore db;

    // Selected values
    private String selectedDate = "";
    private String selectedTimeSlot = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        initViews();
        setupRecyclerView();
        setupDatePicker();
        setupTimeSlots();
        fetchCollectorsFromFirestore();
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