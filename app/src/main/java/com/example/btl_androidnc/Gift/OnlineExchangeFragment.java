package com.example.btl_androidnc.Gift;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_androidnc.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OnlineExchangeFragment extends Fragment {

    private RecyclerView recyclerViewRewards;
    private RewardAdapter adapter;
    private List<Reward> rewardList;
    private TextView tvCurrentPoints;
    private FirebaseFirestore db;
    private int userPoints = 0;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online_exchange, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvCurrentPoints = view.findViewById(R.id.tvCurrentPoints);
        recyclerViewRewards = view.findViewById(R.id.recyclerViewRewards);

        if (recyclerViewRewards == null) {
            Log.e("OnlineExchangeFragment", "RecyclerView is null");
            Toast.makeText(requireContext(), "Không tìm thấy RecyclerView", Toast.LENGTH_SHORT).show();
            return view;
        }

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
                            Long points = documentSnapshot.getLong("point");
                            userPoints = (points != null) ? points.intValue() : 0;
                            tvCurrentPoints.setText("Điểm hiện tại: " + userPoints);
                            if (adapter != null) {
                                adapter.updateUserPoints(userPoints);
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
                            String imageUrl = document.getString("imageUrl");
                            if (name != null && points != null) {
                                rewardList.add(new Reward(name, points.intValue(), imageUrl));
                            }
                        }
                        if (rewardList.isEmpty()) {
                            Toast.makeText(requireContext(), "Không tìm thấy phần quà", Toast.LENGTH_SHORT).show();
                        }
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
                        Toast.makeText(requireContext(), "Lỗi tải phần quà", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void exchangeReward(int position) {
        if (rewardList == null || position < 0 || position >= rewardList.size()) return;

        Reward reward = rewardList.get(position);
        if (userPoints < reward.getPoints()) {
            Toast.makeText(requireContext(), "Không đủ điểm để đổi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xử lý đổi quà
        String rewardCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        userPoints -= reward.getPoints();
        tvCurrentPoints.setText("Điểm hiện tại: " + userPoints);
        adapter.notifyDataSetChanged();

        // Lưu lịch sử đổi quà vào Firestore
        saveRewardHistory(reward, rewardCode);

        // Hiển thị thông báo thành công
        Toast.makeText(requireContext(), "Đổi thành công: " + reward.getName(), Toast.LENGTH_LONG).show();
    }

    private void saveRewardHistory(Reward reward, String rewardCode) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("userId", userId);
        historyData.put("rewardName", reward.getName());
        historyData.put("points", reward.getPoints());
        historyData.put("rewardCode", rewardCode);
        historyData.put("timestamp", System.currentTimeMillis());
        historyData.put("status", "Chưa sử dụng");

        db.collection("reward_history")
                .add(historyData)
                .addOnSuccessListener(documentReference -> {
                    updateUserPointsInFirestore();
                    Log.d("Firestore", "Lưu lịch sử đổi quà thành công");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi lưu lịch sử đổi quà: ", e);
                    Toast.makeText(requireContext(), "Lỗi lưu lịch sử đổi quà", Toast.LENGTH_SHORT).show();
                });
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

    static class Reward {
        private String name;
        private int points;
        private String imageUrl;

        public Reward(String name, int points, String imageUrl) {
            this.name = name;
            this.points = points;
            this.imageUrl = imageUrl;
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

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

        public void updateRewardList(List<Reward> newRewardList) {
            this.rewardList = newRewardList;
        }

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

            // Tải ảnh bằng Glide
            if (reward.getImageUrl() != null && !reward.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(reward.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder) // Ảnh placeholder khi đang tải
                        .error(R.drawable.error_image) // Ảnh hiển thị nếu lỗi
                        .into(holder.ivRewardImage);
            } else {
                holder.ivRewardImage.setImageResource(R.drawable.ic_placeholder);
            }

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
            ImageView ivRewardImage;
            Button btnExchange;

            public RewardViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRewardName = itemView.findViewById(R.id.tvRewardName);
                tvRewardPoints = itemView.findViewById(R.id.tvRewardPoints);
                ivRewardImage = itemView.findViewById(R.id.ivRewardImage);
                btnExchange = itemView.findViewById(R.id.btnExchange);
            }
        }
    }
}