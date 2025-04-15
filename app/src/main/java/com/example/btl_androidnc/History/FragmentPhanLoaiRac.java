package com.example.btl_androidnc.History;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_androidnc.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentPhanLoaiRac extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phan_loai_rac, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        getHistoryFromFirebase();

        return view;
    }

    private void getHistoryFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.w("FIREBASE", "Người dùng chưa đăng nhập");
            return;
        }

        String userId = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        historyList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String name = document.getString("collectorName");
                            String phone = document.getString("collectorPhone");
                            String status = document.getString("status");
                            String method = document.getString("method");
                            String date = document.getString("date");
                            String time = document.getString("timeSlot");
                            String image = document.getString("photoBase64");
                            Map<String, Object> rawScrapData = (Map<String, Object>) document.get("scrapData");
                            Map<String, Integer> scrapData = new HashMap<>();

                            if (rawScrapData != null) {
                                for (Map.Entry<String, Object> entry : rawScrapData.entrySet()) {
                                    Object value = entry.getValue();
                                    if (value instanceof Long) {
                                        scrapData.put(entry.getKey(), ((Long) value).intValue());
                                    } else if (value instanceof Integer) {
                                        scrapData.put(entry.getKey(), (Integer) value);
                                    }
                                }
                            }
                            List<String> scrapTypes = new ArrayList<>();
                            Object rawScrapTypes = document.get("scrapTypes");

                            if (rawScrapTypes instanceof List<?>) {
                                for (Object item : (List<?>) rawScrapTypes) {
                                    if (item instanceof String) {
                                        scrapTypes.add((String) item);
                                    }
                                }
                            }



                            if (name != null && phone != null && date != null) {
                                HistoryItem item = new HistoryItem(id,name, phone,
                                        status != null ? status : "Đã hủy",
                                        method != null ? method : "Thu gom tại nhà",
                                        date, time, image, scrapData, scrapTypes);
                                historyList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w("FIREBASE", "Lỗi khi lấy dữ liệu: ", task.getException());
                    }
                });
    }
}


