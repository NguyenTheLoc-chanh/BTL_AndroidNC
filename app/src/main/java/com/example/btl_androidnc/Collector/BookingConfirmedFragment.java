package com.example.btl_androidnc.Collector;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.btl_androidnc.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class BookingConfirmedFragment extends Fragment {
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private ArrayList<BookingModel> bookingList;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> detailLauncher;

    public BookingConfirmedFragment(ActivityResultLauncher<Intent> detailLauncher) {
        this.detailLauncher = detailLauncher;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(getContext(), bookingList, detailLauncher);
        recyclerView.setAdapter(adapter);

        loadBookings("Đã xác nhận");
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        loadBookings("Đã xác nhận");
    }

    private void loadBookings(String status) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Bước 1: Tìm collector theo userID
        db.collection("collector")
                .whereEqualTo("userID", userID)
                .get()
                .addOnSuccessListener(collectorSnapshots -> {
                    if (!collectorSnapshots.isEmpty()) {
                        DocumentSnapshot collectorDoc = collectorSnapshots.getDocuments().get(0);
                        String collectorID = collectorDoc.getId(); // ID của document = collectorID

                        // Bước 2: Lấy bookings theo collectorID và status
                        db.collection("bookings")
                                .whereEqualTo("collectorId", collectorID)
                                .whereEqualTo("status", status)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    bookingList.clear();
                                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                        BookingModel booking = doc.toObject(BookingModel.class);

                                        booking.setId(doc.getId());
                                        String userIDFromBooking = booking.getUserId();
                                        db.collection("users")
                                                .document(userIDFromBooking)
                                                .get()
                                                .addOnSuccessListener(userDoc -> {
                                                    if (userDoc.exists()) {
                                                        String name = userDoc.getString("name");
                                                        String address = userDoc.getString("address");
                                                        String phone = userDoc.getString("phone");

                                                        booking.setUserPhone(phone);
                                                        booking.setUserName(name);
                                                        booking.setUserAddress(address);
                                                    }
                                                    bookingList.add(booking);
                                                    adapter.notifyDataSetChanged();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Lỗi khi lấy thông tin người đặt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                });

                    } else {
                        // Không tìm thấy collector tương ứng với userID
                        Toast.makeText(getContext(), "Không tìm thấy thông tin người thu gom.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi lấy dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
