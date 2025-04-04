package com.example.btl_androidnc;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FragmentLogin extends BottomSheetDialogFragment {
    private FirebaseAuth mAuth;
    private Button btnLogin;
    private TextView txtRegister;
    private EditText edtUserName, edtPassword;
    FirebaseFirestore db;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView cancelButton = view.findViewById(R.id.txtCancel);
        ImageView imgNextLeft = view.findViewById(R.id.imgNextLeft);
        edtUserName = view.findViewById(R.id.edtUserName);
        edtPassword = view.findViewById(R.id.edtPassword);

        btnLogin = view.findViewById(R.id.btn_login);
        txtRegister = view.findViewById(R.id.txtRegister);

        // Nhận dữ liệu từ FragmentRegister
        Bundle bundle = getArguments();
        if (bundle != null) {
            String email = bundle.getString("email", "");
            String password = bundle.getString("password", "");
            edtUserName.setText(email);
            edtPassword.setText(password);
        }
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dismiss());
        }
        if(imgNextLeft != null){
            imgNextLeft.setOnClickListener(v -> dismiss());
        }
        btnLogin.setOnClickListener(v -> {
            String email = edtUserName.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                edtUserName.setError("Vui lòng nhập email!");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edtPassword.setError("Vui lòng nhập mật khẩu!");
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            checkAndSaveUserToFirestore(() -> {
                                Toast.makeText(getActivity(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                // Chuyển sang HomeActivity
                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                startActivity(intent);
                                // Đóng FragmentLogin
                                dismiss();
                            });
                        } else {
                            Toast.makeText(getActivity(), "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        txtRegister.setOnClickListener(v -> {
            dismiss(); // Đóng FragmentLogin trước khi mở FragmentRegister
            FragmentRegister fragmentRegister = new FragmentRegister();
            fragmentRegister.show(requireActivity().getSupportFragmentManager(), fragmentRegister.getTag());
        });
        return view;
    }

    public void checkAndSaveUserToFirestore(Runnable onComplete) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // Lấy UID của người dùng
            String email = user.getEmail();
            String name = user.getDisplayName();

            String gender = "Nam";
            String dob = "01/01/2000";
            String address = "Chưa cập nhật";
            String point = "0";

            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    // Nếu chưa tồn tại, tạo mới thông tin người dùng
                    Map<String, Object> newUser = new HashMap<>();
                    newUser.put("uid", userId);
                    newUser.put("email", email);
                    newUser.put("name", (name != null) ? name : "Người dùng mới");
                    newUser.put("gender", gender);
                    newUser.put("dob", dob);
                    newUser.put("address", address);
                    newUser.put("point",point);
                    newUser.put("created_at", System.currentTimeMillis());

                    userRef.set(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Người dùng mới đã được lưu vào Firestore.");
                                if (onComplete != null) onComplete.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Lỗi khi lưu người dùng", e);
                                if (onComplete != null) onComplete.run();
                            });
                } else {
                    Log.d("Firestore", "Người dùng đã tồn tại.");
                    if (onComplete != null) onComplete.run();
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Lỗi khi kiểm tra dữ liệu người dùng", e);
                if (onComplete != null) onComplete.run();
            });
        }else {
            if (onComplete != null) onComplete.run();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT; // Tràn toàn bộ màn hình
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true); // Không collapse khi vuốt xuống
            }
        }
    }
}
