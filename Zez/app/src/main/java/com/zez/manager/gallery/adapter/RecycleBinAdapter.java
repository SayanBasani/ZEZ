package com.zez.manager.gallery.adapter;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.manager.R;
import com.zez.manager.gallery.util.RecycleBinHelper;

import java.util.ArrayList;
import java.util.List;

public class RecycleBinAdapter extends RecyclerView.Adapter<RecycleBinAdapter.BinViewHolder> {

    public interface OnRestoreClickListener {
        void onRestore(RecycleBinHelper.RecycledItem item);
    }

    private List<RecycleBinHelper.RecycledItem> items = new ArrayList<>();
    private OnRestoreClickListener listener;

    public RecycleBinAdapter(OnRestoreClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RecycleBinHelper.RecycledItem> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_bin, parent, false);
        return new BinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BinViewHolder holder, int position) {
        RecycleBinHelper.RecycledItem item = items.get(position);
        holder.tvName.setText(item.name);
        String dateStr = "Deleted: " + DateFormat.format("dd MMM yyyy, hh:mm a", item.deletedTime).toString();
        holder.tvDate.setText(dateStr);

        holder.btnRestore.setOnClickListener(v -> {
            if (listener != null) listener.onRestore(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BinViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;
        Button btnRestore;

        public BinViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_bin_name);
            tvDate = itemView.findViewById(R.id.tv_bin_deleted_date);
            btnRestore = itemView.findViewById(R.id.btn_restore);
        }
    }
}
