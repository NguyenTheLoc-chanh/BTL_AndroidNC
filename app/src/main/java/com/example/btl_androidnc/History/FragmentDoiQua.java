package com.example.btl_androidnc.History;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.btl_androidnc.R;

public class FragmentDoiQua extends Fragment {

    private Button buttonDoiQua;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_doi_qua, container, false);

        // Ánh xạ nút đổi quà
        buttonDoiQua = view.findViewById(R.id.buttonDoiQua);

        // Thiết lập sự kiện click cho nút
        buttonDoiQua.setOnClickListener(v -> {
            // Ví dụ, bạn có thể hiển thị một thông báo khi nút được nhấn
            Toast.makeText(getActivity(), "Đổi quà thành công!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
