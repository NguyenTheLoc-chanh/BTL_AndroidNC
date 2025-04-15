package com.example.btl_androidnc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_androidnc.History.HistoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText edtName, edtGender, edtDob, edtAddress;
    private TextView txtName;
    RadioGroup radioGroupGender;
    RadioButton rbFemale, rbMale;
    private Button btnUpdate;
    ImageView imgNextLeft;
    private TextView navLichSu;
    private ImageView navHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        AnhXa();

        loadUserData();
        // Đặt sự kiện chung cho các mục
        setNavClickListener(navLichSu, HistoryActivity.class,"HISTORY");
        setNavHomeClickListener(navHome, HomeActivity.class);

        imgNextLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay về trang trước
            }
        });

        btnUpdate.setOnClickListener(v -> updateUserData());
    }

    private void AnhXa() {
        txtName = findViewById(R.id.txtName);
        edtName = findViewById(R.id.edtUdName);
        radioGroupGender = findViewById(R.id.rdgGender);
        rbFemale = findViewById(R.id.rbFemale);
        rbMale = findViewById(R.id.rbMale);
        edtDob = findViewById(R.id.edtUdBob);
        edtAddress = findViewById(R.id.edtAddress);
        btnUpdate = findViewById(R.id.btnUdProfile);

        imgNextLeft = findViewById(R.id.ImgNext);

        navLichSu = findViewById(R.id.nav_lichsu);
        navHome = findViewById(R.id.nav_logo);
    }
    private void setNavClickListener(TextView textView, Class<?> destinationActivity, String tag) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllNavItems();
                textView.setSelected(true);

                Intent intent = new Intent(ProfileActivity.this, destinationActivity);
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
                startActivity(new Intent(ProfileActivity.this, destinationActivity));
            }
        });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            edtName.setText(documentSnapshot.getString("name"));
                            edtDob.setText(documentSnapshot.getString("dob"));
                            edtAddress.setText(documentSnapshot.getString("address"));
                            // Xử lý chọn giới tính đúng cách
                            String gender = documentSnapshot.getString("gender");
                            if ("Nữ".equals(gender)) {
                                rbFemale.setChecked(true);
                            } else {
                                rbMale.setChecked(true);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Lỗi khi lấy thông tin người dùng", e);
                    });
        }
    }

    private void updateUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        String gender = rbFemale.isChecked() ? "Nữ" : "Nam";
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", edtName.getText().toString().trim());
            updates.put("gender", gender);
            updates.put("dob", edtDob.getText().toString().trim());
            updates.put("address", edtAddress.getText().toString().trim());

            db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

