package com.example.btl_androidnc;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CollectorAdapter extends RecyclerView.Adapter<CollectorAdapter.ViewHolder> {
    private Context context;
    private List<Collector> collectorList;

    public CollectorAdapter(Context context, List<Collector> collectorList) {
        this.context = context;
        this.collectorList = collectorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collector collector = collectorList.get(position);
        holder.tvCollectorName.setText(collector.getName());
        holder.tvPhone.setText(collector.getPhone());
        holder.tvAddress.setText("Địa chỉ: "+collector.getAddress());
        holder.tvBirthYear.setText("Năm sinh:" + collector.getBirthYear());

        Glide.with(context)
                .load(collector.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return collectorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCollectorName, tvPhone, tvBirthYear, tvAddress;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCollectorName = itemView.findViewById(R.id.tvCollectorName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvWorkplace);
            tvBirthYear = itemView.findViewById(R.id.tvBirthYear);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
