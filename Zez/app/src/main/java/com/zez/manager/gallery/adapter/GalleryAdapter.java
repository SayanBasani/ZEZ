package com.zez.manager.gallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zez.manager.R;
import com.zez.manager.gallery.model.GalleryListItem;
import com.zez.manager.gallery.model.MediaItem;
import com.zez.manager.gallery.util.FavoritesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = GalleryListItem.TYPE_HEADER;
    private static final int TYPE_MEDIA  = GalleryListItem.TYPE_MEDIA;

    public static final int FILTER_ALL    = 0;
    public static final int FILTER_PHOTOS = 1;
    public static final int FILTER_VIDEOS = 2;

    public interface Listener {
        void onMediaClick(MediaItem item, int indexInFlatList, List<MediaItem> flatList);
        void onSelectionChanged(int count);
        void onMultiSelectStart();
    }

    private List<MediaItem>       masterList  = new ArrayList<>();
    private List<GalleryListItem> displayList = new ArrayList<>();
    private final Set<Long>       selectedIds = new HashSet<>();
    private boolean               selectMode  = false;
    private int                   filter      = FILTER_ALL;
    private final Listener        listener;

    public GalleryAdapter(Listener listener) {
        this.listener = listener;
    }

    // Span size lookup: headers span all 3 cols, media spans 1
    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int pos) {
                if (pos < 0 || pos >= displayList.size()) return 1;
                return displayList.get(pos).type == TYPE_HEADER ? 3 : 1;
            }
        };
    }

    public void setItems(List<MediaItem> list) {
        this.masterList = list != null ? list : new ArrayList<>();
        exitSelectMode();
        rebuildDisplayList();
    }

    public void setFilter(int f) {
        this.filter = f;
        exitSelectMode();
        rebuildDisplayList();
    }

    private void rebuildDisplayList() {
        List<MediaItem> filtered = new ArrayList<>();
        for (MediaItem m : masterList) {
            if (filter == FILTER_ALL)                        filtered.add(m);
            else if (filter == FILTER_PHOTOS && !m.isVideo()) filtered.add(m);
            else if (filter == FILTER_VIDEOS &&  m.isVideo()) filtered.add(m);
        }
        displayList = buildGroupedList(filtered);
        notifyDataSetChanged();
    }

    private List<GalleryListItem> buildGroupedList(List<MediaItem> sorted) {
        List<GalleryListItem> result = new ArrayList<>();
        String lastHeader = null;
        Calendar today     = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance(); yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar weekAgo   = Calendar.getInstance(); weekAgo.add(Calendar.DAY_OF_YEAR, -7);
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        for (MediaItem item : sorted) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(item.getDateAdded() * 1000L);
            String header;
            if (sameDay(c, today))         header = "Today";
            else if (sameDay(c, yesterday)) header = "Yesterday";
            else if (c.after(weekAgo))      header = "This Week";
            else                            header = fmt.format(c.getTime());

            if (!header.equals(lastHeader)) {
                result.add(new GalleryListItem(header));
                lastHeader = header;
            }
            result.add(new GalleryListItem(item));
        }
        return result;
    }

    private boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR)        == b.get(Calendar.YEAR) &&
               a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    // Selection
    public boolean isSelectMode() { return selectMode; }

    public void enterSelectMode(long id) {
        selectMode = true;
        selectedIds.clear();
        selectedIds.add(id);
        notifyDataSetChanged();
        if (listener != null) {
            listener.onMultiSelectStart();
            listener.onSelectionChanged(1);
        }
    }

    public void exitSelectMode() {
        selectMode = false;
        selectedIds.clear();
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(0);
    }

    public List<MediaItem> getSelectedItems() {
        List<MediaItem> sel = new ArrayList<>();
        for (MediaItem m : masterList) {
            if (selectedIds.contains(m.getId())) sel.add(m);
        }
        return sel;
    }

    public int getSelectedCount() { return selectedIds.size(); }

    @Override public int getItemViewType(int pos) { return displayList.get(pos).type; }
    @Override public int getItemCount()           { return displayList.size(); }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_gallery_header, parent, false);
            return new HeaderVH(v);
        }
        View v = inf.inflate(R.layout.item_gallery_media, parent, false);
        return new MediaVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        GalleryListItem item = displayList.get(pos);
        if (item.type == TYPE_HEADER) {
            ((HeaderVH) holder).tvHeader.setText(item.header);
        } else {
            bindMedia((MediaVH) holder, item.media);
        }
    }

    private void bindMedia(MediaVH h, MediaItem item) {
        Glide.with(h.itemView.getContext())
            .load(item.getUri())
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .placeholder(R.drawable.ic_photo_library)
            .into(h.ivThumb);

        // Video duration badge
        if (item.isVideo()) {
            h.tvDuration.setVisibility(View.VISIBLE);
            h.tvDuration.setText(fmtDur(item.getDuration()));
        } else {
            h.tvDuration.setVisibility(View.GONE);
        }

        // Selection state
        boolean selected = selectedIds.contains(item.getId());
        h.viewOverlay.setVisibility(selected ? View.VISIBLE : View.GONE);
        h.ivCheck.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        h.ivCheck.setSelected(selected);

        // Favorite badge
        boolean isFav = FavoritesHelper.isFavorite(h.itemView.getContext(), item.getId());
        if (h.ivFavBadge != null) {
            h.ivFavBadge.setVisibility(isFav && !selectMode ? View.VISIBLE : View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (selectMode) {
                if (selectedIds.contains(item.getId())) selectedIds.remove(item.getId());
                else selectedIds.add(item.getId());
                if (selectedIds.isEmpty()) exitSelectMode();
                else {
                    notifyItemChanged(h.getAdapterPosition());
                    if (listener != null) listener.onSelectionChanged(selectedIds.size());
                }
            } else {
                if (listener != null) {
                    List<MediaItem> flat = new ArrayList<>();
                    int clickedIdx = 0, cur = 0;
                    for (GalleryListItem gi : displayList) {
                        if (gi.type == TYPE_MEDIA) {
                            if (gi.media.getId() == item.getId()) clickedIdx = cur;
                            flat.add(gi.media);
                            cur++;
                        }
                    }
                    listener.onMediaClick(item, clickedIdx, flat);
                }
            }
        });

        h.itemView.setOnLongClickListener(v -> {
            if (!selectMode) {
                enterSelectMode(item.getId());
                notifyItemChanged(h.getAdapterPosition());
            }
            return true;
        });
    }

    private String fmtDur(long ms) {
        long s = ms / 1000, m = s / 60; s %= 60;
        if (m >= 60) { long hr = m / 60; m %= 60;
            return String.format(Locale.US, "%d:%02d:%02d", hr, m, s); }
        return String.format(Locale.US, "%d:%02d", m, s);
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(View v) { super(v); tvHeader = v.findViewById(R.id.tv_gallery_header); }
    }

    static class MediaVH extends RecyclerView.ViewHolder {
        ImageView ivThumb, ivCheck, ivFavBadge;
        View      viewOverlay;
        TextView  tvDuration;
        MediaVH(View v) {
            super(v);
            ivThumb     = v.findViewById(R.id.iv_thumbnail);
            viewOverlay = v.findViewById(R.id.view_select_overlay);
            ivCheck     = v.findViewById(R.id.iv_check_circle);
            ivFavBadge  = v.findViewById(R.id.iv_favorite_badge);
            tvDuration  = v.findViewById(R.id.tv_duration);
        }
    }
}
