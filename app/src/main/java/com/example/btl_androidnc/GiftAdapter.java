package com.example.btl_androidnc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {

    private Context context;
    private List<GiftLocation> giftList;

    public GiftAdapter(Context context, List<GiftLocation> giftList) {
        this.context = context;
        this.giftList = giftList;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gift, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftLocation gift = giftList.get(position);

        holder.tvGiftName.setText(gift.getName());
        holder.tvPhone.setText(gift.getPhone());
        holder.tvOperatingHours.setText("Thời gian hoạt động: " + gift.getOperatingHours());
        holder.tvStatus.setText(gift.getStatus());

        // Load ảnh từ URL nếu có
        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(gift.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imgAvatar);
        }

        // Sự kiện click vào số điện thoại
        holder.tvPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + gift.getPhone()));
            context.startActivity(intent);
        });

        // Sự kiện click vào item để mở Google Maps
        holder.itemView.setOnClickListener(v -> {
            String address = gift.getName(); // Địa chỉ từ Firebase
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                Toast.makeText(context, "Không tìm thấy ứng dụng Google Maps", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvGiftName, tvPhone, tvOperatingHours, tvStatus;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvOperatingHours = itemView.findViewById(R.id.tvOperatingHours);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
