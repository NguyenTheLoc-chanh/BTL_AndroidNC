package com.example.btl_androidnc.Gift;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_androidnc.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GiftListFragment extends Fragment {

    private RecyclerView recyclerViewGifts;
    private GiftAdapter adapter;
    private List<GiftLocation> giftLocationList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_gift_list, container, false);
        Log.d("GiftListFragment", "Layout inflated: " + (view != null));

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // Khởi tạo giao diện
        recyclerViewGifts = view.findViewById(R.id.recyclerViewGifts);
        progressBar = view.findViewById(R.id.progressBar);

        // Kiểm tra null cho RecyclerView
        if (recyclerViewGifts == null) {
            Log.e("GiftListFragment", "RecyclerView is null. Check fragment_gift_list.xml for recyclerViewGifts ID.");
            Toast.makeText(requireContext(), "Không tìm thấy RecyclerView trong layout", Toast.LENGTH_LONG).show();
            return view;
        }

        // Kiểm tra null cho ProgressBar
        if (progressBar == null) {
            Log.e("GiftListFragment", "ProgressBar is null. Check fragment_gift_list.xml for progressBar ID.");
        }

        giftLocationList = new ArrayList<>();
        adapter = new GiftAdapter(requireContext(), giftLocationList);
        recyclerViewGifts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewGifts.setAdapter(adapter);

        // Tải dữ liệu từ Firestore
        fetchGiftLocationsFromFirestore();

        return view;
    }

    private void fetchGiftLocationsFromFirestore() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        db.collection("gift_locations")
                .get()
                .addOnCompleteListener(task -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (task.isSuccessful()) {
                        giftLocationList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            GiftLocation giftLocation = document.toObject(GiftLocation.class);
                            if (giftLocation != null) {
                                giftLocationList.add(giftLocation);
                            }
                        }
                        if (giftLocationList.isEmpty()) {
                            Toast.makeText(requireContext(), "Không tìm thấy địa điểm đổi quà", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("Firestore", "Đã tải: " + giftLocationList.size() + " mục");
                    } else {
                        Log.e("Firestore", "Lỗi tải dữ liệu: ", task.getException());
                        Toast.makeText(requireContext(), "Tải dữ liệu thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}