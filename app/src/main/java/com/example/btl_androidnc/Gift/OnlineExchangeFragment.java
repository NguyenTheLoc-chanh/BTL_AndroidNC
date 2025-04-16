package com.example.btl_androidnc.Gift;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_androidnc.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OnlineExchangeFragment extends Fragment {

    private RecyclerView recyclerViewRewards;
    private RewardAdapter adapter;
    private List<Reward> rewardList;
    private TextView tvCurrentPoints;
    private FirebaseFirestore db;
    private int userPoints = 0; // Sẽ được lấy từ Firestore
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online_exchange, container, false);

        // Khởi tạo Firestore và Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Khởi tạo giao diện
        tvCurrentPoints = view.findViewById(R.id.tvCurrentPoints);
        recyclerViewRewards = view.findViewById(R.id.recyclerViewRewards);

        if (recyclerViewRewards == null) {
            Log.e("OnlineExchangeFragment", "RecyclerView is null");
            Toast.makeText(requireContext(), "Không tìm thấy RecyclerView", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Lấy dữ liệu từ Firestore
        fetchUserPoints();
        fetchRewards();

        return view;
    }

    private void fetchUserPoints() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Log.e("Firestore", "Lỗi theo dõi điểm", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Long points = documentSnapshot.getLong("point"); // Field "point" trong Firestore
                            userPoints = (points != null) ? points.intValue() : 0;
                            tvCurrentPoints.setText("Điểm hiện tại: " + userPoints);
                            if (adapter != null) {
                                adapter.updateUserPoints(userPoints); // Cập nhật điểm trong adapter
                            }
                        } else {
                            Toast.makeText(requireContext(), "Không tìm thấy thông tin điểm", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchRewards() {
        rewardList = new ArrayList<>();
        db.collection("rewards")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            Long points = document.getLong("points");
                            if (name != null && points != null) {
                                rewardList.add(new Reward(name, points.intValue()));
                            }
                        }
                        if (rewardList.isEmpty()) {
                            Toast.makeText(requireContext(), "Không tìm thấy phần quà", Toast.LENGTH_SHORT).show();
                        }
                        // Khởi tạo hoặc cập nhật adapter
                        if (adapter == null) {
                            adapter = new RewardAdapter(requireContext(), rewardList, userPoints, this);
                            recyclerViewRewards.setLayoutManager(new LinearLayoutManager(requireContext()));
                            recyclerViewRewards.setAdapter(adapter);
                        } else {
                            adapter.updateRewardList(rewardList);
                            adapter.updateUserPoints(userPoints);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("Firestore", "Lỗi tải phần quà: ", task.getException());
                        Toast.makeText(requireContext(), "Lỗi tải phần quà: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm xử lý đổi quà
    public void exchangeReward(int position) {
        if (rewardList == null || position < 0 || position >= rewardList.size()) return;

        Reward reward = rewardList.get(position);
        if (userPoints >= reward.getPoints()) {
            userPoints -= reward.getPoints();
            tvCurrentPoints.setText("Điểm hiện tại: " + userPoints);
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Đổi thành công: " + reward.getName(), Toast.LENGTH_SHORT).show();

            // Cập nhật điểm vào Firestore
            updateUserPointsInFirestore();
        } else {
            Toast.makeText(requireContext(), "Không đủ điểm để đổi", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserPointsInFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId)
                    .update("point", userPoints)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Cập nhật điểm thành công"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Lỗi cập nhật điểm: ", e));
        }
    }

    // Model cho phần quà
    static class Reward {
        private String name;
        private int points;

        public Reward(String name, int points) {
            this.name = name;
            this.points = points;
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }
    }

    // Adapter cho danh sách quà
    static class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

        private Context context;
        private List<Reward> rewardList;
        private int userPoints;
        private OnlineExchangeFragment fragment;

        public RewardAdapter(Context context, List<Reward> rewardList, int userPoints, OnlineExchangeFragment fragment) {
            this.context = context;
            this.rewardList = rewardList;
            this.userPoints = userPoints;
            this.fragment = fragment;
        }

        // Cập nhật danh sách phần quà
        public void updateRewardList(List<Reward> newRewardList) {
            this.rewardList = newRewardList;
        }

        // Cập nhật điểm người dùng
        public void updateUserPoints(int newPoints) {
            this.userPoints = newPoints;
        }

        @NonNull
        @Override
        public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_reward, parent, false);
            return new RewardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
            Reward reward = rewardList.get(position);
            holder.tvRewardName.setText(reward.getName());
            holder.tvRewardPoints.setText("Điểm cần: " + reward.getPoints());
            holder.btnExchange.setEnabled(userPoints >= reward.getPoints());
            holder.btnExchange.setOnClickListener(v -> {
                if (fragment != null) {
                    fragment.exchangeReward(position);
                } else {
                    Toast.makeText(context, "Lỗi: Không thể xử lý đổi quà", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return rewardList != null ? rewardList.size() : 0;
        }

        static class RewardViewHolder extends RecyclerView.ViewHolder {
            TextView tvRewardName, tvRewardPoints;
            Button btnExchange;

            public RewardViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRewardName = itemView.findViewById(R.id.tvRewardName);
                tvRewardPoints = itemView.findViewById(R.id.tvRewardPoints);
                btnExchange = itemView.findViewById(R.id.btnExchange);
            }
        }
    }
}