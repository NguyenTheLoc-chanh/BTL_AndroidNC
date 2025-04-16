package com.example.btl_androidnc.Gift;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_androidnc.R;

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
        if (gift == null) return;

        holder.tvGiftName.setText(gift.getName() != null ? gift.getName() : "Không có tên");
        holder.tvAddress.setText(gift.getAddress() != null ? gift.getAddress() : "Không có địa chỉ");
        holder.tvPhone.setText(gift.getPhone() != null ? gift.getPhone() : "Không có số điện thoại");
        holder.tvOperatingHours.setText(gift.getOperatingHours() != null ? "Thời gian hoạt động: " + gift.getOperatingHours() : "Không có giờ hoạt động");
        holder.tvStatus.setText(gift.getStatus() != null ? gift.getStatus() : "Không có trạng thái");

        // Load ảnh từ URL nếu có
        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(gift.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Sự kiện click vào số điện thoại
        holder.tvPhone.setOnClickListener(v -> {
            if (gift.getPhone() != null && !gift.getPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + gift.getPhone()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện click vào item để mở Google Maps
        holder.itemView.setOnClickListener(v -> {
            String address = gift.getAddress() != null ? gift.getAddress() : gift.getName();
            if (address != null && !address.isEmpty()) {
                Log.d("GoogleMaps", "Mở Google Maps với địa chỉ: " + address);
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                    context.startActivity(webIntent);
                    Toast.makeText(context, "Google Maps không được cài đặt, mở trình duyệt", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Địa chỉ không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return giftList != null ? giftList.size() : 0;
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvGiftName, tvAddress, tvPhone, tvOperatingHours, tvStatus;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvOperatingHours = itemView.findViewById(R.id.tvOperatingHours);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}