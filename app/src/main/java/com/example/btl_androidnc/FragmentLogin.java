package com.example.btl_androidnc;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
public class FragmentLogin extends BottomSheetDialogFragment {
    private Button btnLogin;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        TextView cancelButton = view.findViewById(R.id.txtCancel);
        ImageView imgNextLeft = view.findViewById(R.id.imgNextLeft);

        btnLogin = view.findViewById(R.id.btn_login);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dismiss());
        }
        if(imgNextLeft != null){
            imgNextLeft.setOnClickListener(v -> dismiss());
        }
        btnLogin.setOnClickListener(v -> {
            // Chuyển sang HomeActivity
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);

            // Đóng BottomSheet sau khi đăng nhập
            dismiss();
        });
        return view;
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
