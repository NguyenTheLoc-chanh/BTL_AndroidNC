package com.example.btl_androidnc.Collector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_androidnc.History.BookingDetailActivity;
import com.example.btl_androidnc.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private Context context;
    private List<BookingModel> list;
    private ActivityResultLauncher<Intent> launcher;

    public BookingAdapter(Context context, List<BookingModel> list, ActivityResultLauncher<Intent> launcher) {
        this.context = context;
        this.list = list;
        this.launcher = launcher;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel model = list.get(position);
        holder.txtName.setText(model.getUserName());
//        holder.txtPhone.setText("SĐT: " + model.getId());
        holder.tvAddress.setText("Địa chỉ: " + model.getUserAddress());
        holder.txtDate.setText("Ngày: " + model.getDate() + " - " + model.getTimeSlot());
        holder.txtStatus.setText(model.getStatus());
        // Tuỳ chỉnh hiển thị trạng thái nếu cần
        if ("Đã hủy".equals(model.getStatus())) {
            holder.txtStatus.setTextColor(Color.RED);
            holder.imgStatus.setImageResource(R.drawable.forbidden);
        } else if ("Chờ xác nhận".equals(model.getStatus())) {
            holder.txtStatus.setTextColor(Color.YELLOW);
            holder.imgStatus.setImageResource(R.drawable.pending);
        } else {
            holder.txtStatus.setTextColor(Color.GREEN);
            holder.imgStatus.setImageResource(R.drawable.checked);
        }

        holder.layoutBookingItem.setOnClickListener(v -> {
            if (launcher != null) {
                Intent intent = new Intent(v.getContext(), BookingDetailsCollector.class);
                intent.putExtra("booking_id", model.getId());
                launcher.launch(intent);
            } else {
                Toast.makeText(v.getContext(), "Không thể mở chi tiết: launcher null", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutBookingItem;
        TextView txtName, txtPhone, txtDate, txtStatus, tvAddress;
        ImageView imgStatus;
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.tvNameColBok);
            txtPhone = itemView.findViewById(R.id.tvPhoneBok);
            tvAddress = itemView.findViewById(R.id.tvAddressBok);
            txtDate = itemView.findViewById(R.id.tvDateThuGomBok);
            txtStatus = itemView.findViewById(R.id.tvStatusBok);
            imgStatus = itemView.findViewById(R.id.imgStatusIconBok);

            layoutBookingItem = itemView.findViewById(R.id.layoutBookingItem);
        }
    }
}

