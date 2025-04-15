package com.example.btl_androidnc.Collector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_androidnc.R;

import java.util.List;

public class CollectorAdapter extends RecyclerView.Adapter<CollectorAdapter.ViewHolder> {
    private Context context;
    private List<Collector> collectorList;
    private int selectedPosition = -1;
    private OnCollectorClickListener listener;

    public interface OnCollectorClickListener {
        void onCollectorClick(Collector collector, int position);
    }

    public CollectorAdapter(Context context, List<Collector> collectorList) {
        this.context = context;
        this.collectorList = collectorList;
    }

    public void setOnCollectorClickListener(OnCollectorClickListener listener) {
        this.listener = listener;
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

        // Bind data to views
        holder.tvCollectorName.setText(collector.getName());
        holder.tvPhone.setText(collector.getPhone());
        holder.tvAddress.setText("Địa chỉ: " + collector.getAddress());
        holder.tvBirthYear.setText("Năm sinh: " + collector.getBirthYear());

        // Highlight selected item
        if (selectedPosition == position) {
            holder.tvTitleCollector.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_color));
        } else {
            holder.tvTitleCollector.setBackgroundColor(ContextCompat.getColor(context, R.color.default_item_color));
        }

        // Load image with Glide
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

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCollectorName, tvPhone, tvBirthYear, tvAddress, tvTitleCollector;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCollectorName = itemView.findViewById(R.id.tvCollectorName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvWorkplace);
            tvBirthYear = itemView.findViewById(R.id.tvBirthYear);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvTitleCollector = itemView.findViewById(R.id.tvTitleCollector);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position);
                    if (listener != null) {
                        listener.onCollectorClick(collectorList.get(position), position);
                    }
                }
            });
        }
    }
}