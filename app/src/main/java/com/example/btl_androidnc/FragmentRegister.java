package com.example.btl_androidnc;

import android.os.Bundle;
import android.text.TextUtils;
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

public class FragmentRegister extends BottomSheetDialogFragment {
    private FirebaseAuth mAuth;
    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView txtLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các thành phần giao diện
        TextView cancelButton = view.findViewById(R.id.txtCancel);
        ImageView imgNextLeft = view.findViewById(R.id.imgNextLeft);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirmPassword = view.findViewById(R.id.edtAgainPass);
        btnRegister = view.findViewById(R.id.btn_register);
        txtLogin = view.findViewById(R.id.txtLogin);

        // Đóng Fragment khi nhấn hủy
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dismiss());
        }
        if (imgNextLeft != null) {
            imgNextLeft.setOnClickListener(v -> dismiss());
        }

        // Xử lý khi nhấn vào nút "Đăng ký"
        btnRegister.setOnClickListener(v -> registerUser());

        // Chuyển về FragmentLogin khi bấm "Đăng nhập"
        txtLogin.setOnClickListener(v -> {
            dismiss(); // Đóng FragmentRegister trước khi mở FragmentLogin
            FragmentLogin fragmentLogin = new FragmentLogin();
            fragmentLogin.show(requireActivity().getSupportFragmentManager(), fragmentLogin.getTag());
        });

        return view;
    }
    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập Email!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu!");
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không khớp!");
            return;
        }

        // Đăng ký tài khoản với Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                        // Đóng FragmentRegister trước khi mở FragmentLogin
                        dismiss();

                        // Mở FragmentLogin và truyền email + password
                        FragmentLogin fragmentLogin = new FragmentLogin();
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        bundle.putString("password", password);
                        fragmentLogin.setArguments(bundle);
                        fragmentLogin.show(requireActivity().getSupportFragmentManager(), fragmentLogin.getTag());

                    } else {
                        Toast.makeText(getActivity(), "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

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
