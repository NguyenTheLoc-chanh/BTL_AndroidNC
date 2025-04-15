package com.example.btl_androidnc.History;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_androidnc.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);

        holder.txtName.setText(item.getName());
        holder.txtPhone.setText(item.getPhone());
        holder.txtStatus.setText(item.getStatus());
        holder.txtMethod.setText(item.getMethod());
        holder.txtDate.setText("Ngày đặt lịch: " + item.getDate() + item.getTime());

        // Tuỳ chỉnh hiển thị trạng thái nếu cần
        if ("Đã hủy".equals(item.getStatus())) {
            holder.txtStatus.setTextColor(Color.RED);
            holder.imgStatus.setImageResource(R.drawable.forbidden); // icon hủy (nên đặt icon vào drawable)
        } else if ("Chờ xác nhận".equals(item.getStatus())) {
            holder.txtStatus.setTextColor(Color.YELLOW);
            holder.imgStatus.setImageResource(R.drawable.pending);
        } else {
            holder.txtStatus.setTextColor(Color.GREEN);
            holder.imgStatus.setImageResource(R.drawable.checked); // icon thành công
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgStatus;
        TextView txtName, txtPhone, txtStatus, txtMethod, txtDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatarHis);
            imgStatus = itemView.findViewById(R.id.imgStatusIcon);
            txtName = itemView.findViewById(R.id.tvNameColHis);
            txtPhone = itemView.findViewById(R.id.tvPhoneHis);
            txtStatus = itemView.findViewById(R.id.tvStatusHis);
            txtMethod = itemView.findViewById(R.id.tvMethodHis);
            txtDate = itemView.findViewById(R.id.tvDateThuGom);
        }
    }
}

