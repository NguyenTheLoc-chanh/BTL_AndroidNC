package com.example.btl_androidnc.Gift;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RewardHistoryFragment extends Fragment {

    private RecyclerView recyclerViewHistory;
    private RewardHistoryAdapter adapter;
    private List<RewardHistoryItem> historyList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_reward_history_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewHistory = view.findViewById(R.id.recyclerViewRewardHistory);
        historyList = new ArrayList<>();
        adapter = new RewardHistoryAdapter(requireContext(), historyList);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewHistory.setAdapter(adapter);

        fetchRewardHistory();

        return view;
    }

    private void fetchRewardHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        db.collection("reward_history")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        historyList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String rewardName = document.getString("rewardName");
                            Long points = document.getLong("points");
                            String rewardCode = document.getString("rewardCode");
                            Long timestamp = document.getLong("timestamp");
                            String status = document.getString("status");

                            if (rewardName != null && points != null && rewardCode != null && timestamp != null) {
                                String formattedTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        .format(new Date(timestamp));
                                historyList.add(new RewardHistoryItem(
                                        rewardName,
                                        points.intValue(),
                                        rewardCode,
                                        formattedTime,
                                        status
                                ));
                            }
                        }
                        if (historyList.isEmpty()) {
                            Toast.makeText(requireContext(), "Không có lịch sử đổi quà", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Lỗi tải lịch sử đổi quà: ", task.getException());
                        Toast.makeText(requireContext(), "Lỗi tải lịch sử đổi quà", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    static class RewardHistoryItem {
        private String rewardName;
        private int points;
        private String rewardCode;
        private String timestamp;
        private String status;

        public RewardHistoryItem(String rewardName, int points, String rewardCode, String timestamp, String status) {
            this.rewardName = rewardName;
            this.points = points;
            this.rewardCode = rewardCode;
            this.timestamp = timestamp;
            this.status = status;
        }

        public String getRewardName() {
            return rewardName;
        }

        public int getPoints() {
            return points;
        }

        public String getRewardCode() {
            return rewardCode;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getStatus() {
            return status;
        }
    }

    static class RewardHistoryAdapter extends RecyclerView.Adapter<RewardHistoryAdapter.ViewHolder> {
        private Context context;
        private List<RewardHistoryItem> historyList;

        public RewardHistoryAdapter(Context context, List<RewardHistoryItem> historyList) {
            this.context = context;
            this.historyList = historyList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_reward_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RewardHistoryItem item = historyList.get(position);
            holder.tvRewardName.setText(item.getRewardName());
            holder.tvPoints.setText("Điểm: " + item.getPoints());
            holder.tvRewardCode.setText("Mã: " + item.getRewardCode());
            holder.tvTimestamp.setText("Thời gian: " + item.getTimestamp());
            holder.tvStatus.setText("Trạng thái: " + item.getStatus());
        }

        @Override
        public int getItemCount() {
            return historyList != null ? historyList.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRewardName, tvPoints, tvRewardCode, tvTimestamp, tvStatus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRewardName = itemView.findViewById(R.id.tvRewardName);
                tvPoints = itemView.findViewById(R.id.tvPoints);
                tvRewardCode = itemView.findViewById(R.id.tvRewardCode);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}