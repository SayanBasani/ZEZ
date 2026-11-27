package com.zez.app.util;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zez.app.R;
import com.zez.app.model.MediaItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WhatsApp-style expandable gallery bottom-sheet popup supporting By-Date section headers,
 * Normal flat grid toggle, specific calendar date filtering, multi-selection with order numbers, and caption input.
 */
public class GalleryPopupHelper {

    public interface GallerySelectionCallback {
        void onMediaSelected(List<MediaItem> selectedItems, String caption);
        void onRequestFolderFallback(String filterMode);
    }

    private static final int MAX_SELECTION = 20;
    private static final int DATE_FILTER_ALL = 0;
    private static final int DATE_FILTER_TODAY = 1;
    private static final int DATE_FILTER_LAST_7_DAYS = 2;
    private static final int DATE_FILTER_THIS_MONTH = 3;
    private static final int DATE_FILTER_EXACT_DAY = 4;

    private static final ExecutorService loaderExecutor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static boolean hasMediaPermission(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasImg = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean hasVid = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
            boolean hasPartial = Build.VERSION.SDK_INT >= 34
                    && ContextCompat.checkSelfPermission(context, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == PackageManager.PERMISSION_GRANTED;
            return hasImg || hasVid || hasPartial;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static String[] getRequiredMediaPermissions() {
        if (Build.VERSION.SDK_INT >= 34) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    private static class GridRow {
        final boolean isHeader;
        final String headerTitle;
        final int headerCount;
        final MediaItem item;

        GridRow(String headerTitle, int headerCount) {
            this.isHeader = true;
            this.headerTitle = headerTitle;
            this.headerCount = headerCount;
            this.item = null;
        }

        GridRow(MediaItem item) {
            this.isHeader = false;
            this.headerTitle = null;
            this.headerCount = 0;
            this.item = item;
        }
    }

    public static void show(Activity activity, String initialFilter, GallerySelectionCallback callback) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_gallery_popup, null);
        sheet.setContentView(view);
        if (sheet.getWindow() != null) {
            sheet.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        int sheetHeight = (int) (dm.heightPixels * 0.80f);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sheetHeight));

        final int[] baseVisibleBottom = new int[]{0};
        final int[] lastAppliedHeight = new int[]{-1};
        final int[] lastAppliedPad = new int[]{-1};
        final Runnable updateSheetForIme = () -> {
            if (sheet.getWindow() == null || !sheet.isShowing()) return;
            View decor = sheet.getWindow().getDecorView();
            View bs = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (decor == null || bs == null) return;

            android.graphics.Rect visibleRect = new android.graphics.Rect();
            decor.getWindowVisibleDisplayFrame(visibleRect);
            if (visibleRect.bottom <= 0) return;

            if (baseVisibleBottom[0] == 0 || visibleRect.bottom > baseVisibleBottom[0]) {
                baseVisibleBottom[0] = visibleRect.bottom;
            }
            int imeOverlap = Math.max(0, baseVisibleBottom[0] - visibleRect.bottom);
            if (imeOverlap < (int) (80 * dm.density)) {
                imeOverlap = 0;
            }

            int availHeight = Math.max((int) (240 * dm.density),
                    visibleRect.bottom - visibleRect.top - (int) (12 * dm.density));
            int contentHeight = imeOverlap > 0 ? Math.min(sheetHeight, availHeight) : sheetHeight;
            int sheetTotalHeight = contentHeight + imeOverlap;

            if (lastAppliedHeight[0] == sheetTotalHeight && lastAppliedPad[0] == imeOverlap) {
                return;
            }
            lastAppliedHeight[0] = sheetTotalHeight;
            lastAppliedPad[0] = imeOverlap;

            ViewGroup.LayoutParams vlp = view.getLayoutParams();
            if (vlp != null) {
                vlp.height = contentHeight;
                view.setLayoutParams(vlp);
            }
            bs.setPadding(bs.getPaddingLeft(), bs.getPaddingTop(), bs.getPaddingRight(), imeOverlap);
            ViewGroup.LayoutParams blp = bs.getLayoutParams();
            if (blp != null) {
                blp.height = sheetTotalHeight;
                bs.setLayoutParams(blp);
            }
            try {
                BottomSheetBehavior<View> b = BottomSheetBehavior.from(bs);
                b.setPeekHeight(sheetTotalHeight);
                b.setState(BottomSheetBehavior.STATE_EXPANDED);
            } catch (Exception ignored) {}
        };
        final android.view.ViewTreeObserver.OnGlobalLayoutListener imeLayoutListener = updateSheetForIme::run;

        View parentSheet = (View) view.getParent();
        if (parentSheet != null) {
            parentSheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parentSheet);
            behavior.setPeekHeight(sheetHeight);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        sheet.setOnShowListener(d -> {
            View bs = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) {
                bs.setBackgroundColor(Color.TRANSPARENT);
                bs.getLayoutParams().height = sheetHeight;
                BottomSheetBehavior<View> b = BottomSheetBehavior.from(bs);
                b.setPeekHeight(sheetHeight);
                b.setSkipCollapsed(true);
                b.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            if (sheet.getWindow() != null) {
                sheet.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(imeLayoutListener);
            }
            updateSheetForIme.run();
        });
        sheet.setOnDismissListener(d -> {
            try {
                if (sheet.getWindow() != null) {
                    sheet.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(imeLayoutListener);
                }
            } catch (Exception ignored) {}
        });

        LinearLayout albumPicker = view.findViewById(R.id.layout_gallery_album_picker);
        TextView tvAlbumName = view.findViewById(R.id.tv_gallery_album_name);
        TextView tvSelectedCount = view.findViewById(R.id.tv_gallery_selected_count);
        ImageButton btnClose = view.findViewById(R.id.btn_gallery_close);
        TextView chipAll = view.findViewById(R.id.chip_gallery_all);
        TextView chipPhotos = view.findViewById(R.id.chip_gallery_photos);
        TextView chipVideos = view.findViewById(R.id.chip_gallery_videos);
        TextView chipByDate = view.findViewById(R.id.chip_view_by_date);
        TextView chipNormal = view.findViewById(R.id.chip_view_normal);
        TextView btnDateFilter = view.findViewById(R.id.btn_gallery_date_filter);
        TextView btnFolderFallback = view.findViewById(R.id.btn_gallery_system_fallback);
        RecyclerView rvGallery = view.findViewById(R.id.rv_gallery_popup);
        ProgressBar progressBar = view.findViewById(R.id.progress_gallery_popup);
        TextView tvEmpty = view.findViewById(R.id.tv_gallery_empty);
        LinearLayout sendBar = view.findViewById(R.id.layout_gallery_send_bar);
        EditText etCaption = view.findViewById(R.id.et_gallery_caption);
        FrameLayout btnSend = view.findViewById(R.id.btn_gallery_send);
        TextView tvSendBadge = view.findViewById(R.id.tv_gallery_send_badge);

        int accentColor = PreferencesHelper.getAccentColorInt(activity);
        if (btnSend != null) {
            btnSend.setBackgroundTintList(ColorStateList.valueOf(accentColor));
        }

        int spacingPx = (int) (18 * dm.density);
        int tileSize = Math.max(92, (dm.widthPixels - spacingPx) / 3);

        final List<MediaItem> allMedia = new ArrayList<>();
        final List<GridRow> gridRows = new ArrayList<>();
        final List<MediaItem> selectedOrder = new ArrayList<>();
        final String[] currentFilter = new String[]{
                (initialFilter != null && !initialFilter.isEmpty()) ? initialFilter : "all"
        };
        final String[] currentAlbum = new String[]{"Recent"};
        final boolean[] groupByDate = new boolean[]{true};
        final int[] dateFilterMode = new int[]{DATE_FILTER_ALL};
        final int[] exactDateYmd = new int[]{0, 0, 0};

        Runnable updateSelectionUi = () -> {
            int count = selectedOrder.size();
            if (count > 0) {
                tvSelectedCount.setVisibility(View.VISIBLE);
                tvSelectedCount.setText(count + " selected");
                tvSelectedCount.setBackgroundTintList(ColorStateList.valueOf(accentColor));
                sendBar.setVisibility(View.VISIBLE);
                tvSendBadge.setText(String.valueOf(count));
            } else {
                tvSelectedCount.setVisibility(View.GONE);
                sendBar.setVisibility(View.GONE);
            }
        };

        GalleryAdapter adapter = new GalleryAdapter(gridRows, selectedOrder, tileSize, accentColor, item -> {
            int existingIdx = -1;
            for (int i = 0; i < selectedOrder.size(); i++) {
                if (selectedOrder.get(i).getId() == item.getId() && selectedOrder.get(i).isVideo() == item.isVideo()) {
                    existingIdx = i;
                    break;
                }
            }
            if (existingIdx >= 0) {
                selectedOrder.remove(existingIdx);
            } else {
                if (selectedOrder.size() >= MAX_SELECTION) {
                    Toast.makeText(activity, "You can select up to " + MAX_SELECTION + " items at once", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedOrder.add(item);
            }
            updateSelectionUi.run();
        });

        GridLayoutManager glm = new GridLayoutManager(activity, 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position >= 0 && position < gridRows.size() && gridRows.get(position).isHeader) {
                    return 3;
                }
                return 1;
            }
        });
        rvGallery.setLayoutManager(glm);
        rvGallery.setAdapter(adapter);

        Runnable applyFilters = () -> {
            List<MediaItem> matched = new ArrayList<>();
            long nowMs = System.currentTimeMillis();
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(nowMs);
            int curYear = nowCal.get(Calendar.YEAR);
            int curDayOfYear = nowCal.get(Calendar.DAY_OF_YEAR);
            int curMonth = nowCal.get(Calendar.MONTH);
            Calendar itemCal = Calendar.getInstance();

            for (MediaItem item : allMedia) {
                if ("image".equals(currentFilter[0]) && item.isVideo()) continue;
                if ("video".equals(currentFilter[0]) && !item.isVideo()) continue;
                if (!"Recent".equals(currentAlbum[0]) && !currentAlbum[0].equals(item.getBucketName())) continue;

                long itemMs = item.getDateAdded() * 1000L;
                if (dateFilterMode[0] != DATE_FILTER_ALL) {
                    itemCal.setTimeInMillis(itemMs);
                    if (dateFilterMode[0] == DATE_FILTER_TODAY) {
                        if (itemCal.get(Calendar.YEAR) != curYear || itemCal.get(Calendar.DAY_OF_YEAR) != curDayOfYear) {
                            continue;
                        }
                    } else if (dateFilterMode[0] == DATE_FILTER_LAST_7_DAYS) {
                        if ((nowMs - itemMs) > 7L * 24 * 3600 * 1000L) {
                            continue;
                        }
                    } else if (dateFilterMode[0] == DATE_FILTER_THIS_MONTH) {
                        if (itemCal.get(Calendar.YEAR) != curYear || itemCal.get(Calendar.MONTH) != curMonth) {
                            continue;
                        }
                    } else if (dateFilterMode[0] == DATE_FILTER_EXACT_DAY) {
                        if (itemCal.get(Calendar.YEAR) != exactDateYmd[0]
                                || itemCal.get(Calendar.MONTH) != exactDateYmd[1]
                                || itemCal.get(Calendar.DAY_OF_MONTH) != exactDateYmd[2]) {
                            continue;
                        }
                    }
                }
                matched.add(item);
            }

            gridRows.clear();
            if (groupByDate[0] && !matched.isEmpty()) {
                Map<String, List<MediaItem>> grouped = new LinkedHashMap<>();
                for (MediaItem m : matched) {
                    String section = formatSectionDate(m.getDateAdded() * 1000L, nowCal);
                    List<MediaItem> bucket = grouped.get(section);
                    if (bucket == null) {
                        bucket = new ArrayList<>();
                        grouped.put(section, bucket);
                    }
                    bucket.add(m);
                }
                for (Map.Entry<String, List<MediaItem>> e : grouped.entrySet()) {
                    gridRows.add(new GridRow(e.getKey(), e.getValue().size()));
                    for (MediaItem m : e.getValue()) {
                        gridRows.add(new GridRow(m));
                    }
                }
            } else {
                for (MediaItem m : matched) {
                    gridRows.add(new GridRow(m));
                }
            }

            adapter.notifyDataSetChanged();
            tvAlbumName.setText(currentAlbum[0]);
            tvEmpty.setVisibility(matched.isEmpty() ? View.VISIBLE : View.GONE);
        };

        Runnable styleChips = () -> {
            styleChip(chipAll, "all".equals(currentFilter[0]), accentColor);
            styleChip(chipPhotos, "image".equals(currentFilter[0]), accentColor);
            styleChip(chipVideos, "video".equals(currentFilter[0]), accentColor);
            styleChip(chipByDate, groupByDate[0], accentColor);
            styleChip(chipNormal, !groupByDate[0], accentColor);
        };
        styleChips.run();

        chipAll.setOnClickListener(v -> {
            currentFilter[0] = "all";
            styleChips.run();
            applyFilters.run();
        });
        chipPhotos.setOnClickListener(v -> {
            currentFilter[0] = "image";
            styleChips.run();
            applyFilters.run();
        });
        chipVideos.setOnClickListener(v -> {
            currentFilter[0] = "video";
            styleChips.run();
            applyFilters.run();
        });

        chipByDate.setOnClickListener(v -> {
            groupByDate[0] = true;
            styleChips.run();
            applyFilters.run();
        });
        chipNormal.setOnClickListener(v -> {
            groupByDate[0] = false;
            styleChips.run();
            applyFilters.run();
        });

        btnDateFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, btnDateFilter);
            popup.getMenu().add(0, DATE_FILTER_ALL, 0, "All Dates");
            popup.getMenu().add(0, DATE_FILTER_TODAY, 1, "Today");
            popup.getMenu().add(0, DATE_FILTER_LAST_7_DAYS, 2, "Last 7 Days");
            popup.getMenu().add(0, DATE_FILTER_THIS_MONTH, 3, "This Month");
            popup.getMenu().add(0, DATE_FILTER_EXACT_DAY, 4, "Choose Specific Date...");
            popup.setOnMenuItemClickListener(mi -> {
                int id = mi.getItemId();
                if (id == DATE_FILTER_EXACT_DAY) {
                    Calendar cal = Calendar.getInstance();
                    int initY = exactDateYmd[0] > 0 ? exactDateYmd[0] : cal.get(Calendar.YEAR);
                    int initM = exactDateYmd[0] > 0 ? exactDateYmd[1] : cal.get(Calendar.MONTH);
                    int initD = exactDateYmd[0] > 0 ? exactDateYmd[2] : cal.get(Calendar.DAY_OF_MONTH);
                    new DatePickerDialog(activity, (dp, y, m, d) -> {
                        dateFilterMode[0] = DATE_FILTER_EXACT_DAY;
                        exactDateYmd[0] = y;
                        exactDateYmd[1] = m;
                        exactDateYmd[2] = d;
                        Calendar sel = Calendar.getInstance();
                        sel.set(y, m, d);
                        String label = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(sel.getTime());
                        btnDateFilter.setText(label + " ▾");
                        applyFilters.run();
                    }, initY, initM, initD).show();
                    return true;
                }
                dateFilterMode[0] = id;
                if (id == DATE_FILTER_ALL) btnDateFilter.setText("All Dates ▾");
                else if (id == DATE_FILTER_TODAY) btnDateFilter.setText("Today ▾");
                else if (id == DATE_FILTER_LAST_7_DAYS) btnDateFilter.setText("Last 7 Days ▾");
                else if (id == DATE_FILTER_THIS_MONTH) btnDateFilter.setText("This Month ▾");
                applyFilters.run();
                return true;
            });
            popup.show();
        });

        albumPicker.setOnClickListener(v -> {
            Map<String, Integer> buckets = new LinkedHashMap<>();
            int matchingTotal = 0;
            for (MediaItem item : allMedia) {
                if ("image".equals(currentFilter[0]) && item.isVideo()) continue;
                if ("video".equals(currentFilter[0]) && !item.isVideo()) continue;
                matchingTotal++;
                String b = item.getBucketName();
                buckets.put(b, (buckets.containsKey(b) ? buckets.get(b) : 0) + 1);
            }
            PopupMenu popup = new PopupMenu(activity, albumPicker);
            popup.getMenu().add(0, 0, 0, "Recent (" + matchingTotal + ")");
            int idx = 1;
            final List<String> albumNames = new ArrayList<>();
            albumNames.add("Recent");
            for (Map.Entry<String, Integer> entry : buckets.entrySet()) {
                albumNames.add(entry.getKey());
                popup.getMenu().add(0, idx++, idx, entry.getKey() + " (" + entry.getValue() + ")");
            }
            popup.setOnMenuItemClickListener(mi -> {
                int chosen = mi.getItemId();
                if (chosen >= 0 && chosen < albumNames.size()) {
                    currentAlbum[0] = albumNames.get(chosen);
                    applyFilters.run();
                }
                return true;
            });
            popup.show();
        });

        btnClose.setOnClickListener(v -> sheet.dismiss());

        btnFolderFallback.setOnClickListener(v -> {
            sheet.dismiss();
            if (callback != null) {
                callback.onRequestFolderFallback(currentFilter[0]);
            }
        });

        btnSend.setOnClickListener(v -> {
            if (selectedOrder.isEmpty()) return;
            String caption = etCaption.getText() != null ? etCaption.getText().toString().trim() : "";
            List<MediaItem> copy = new ArrayList<>(selectedOrder);
            sheet.dismiss();
            if (callback != null) {
                callback.onMediaSelected(copy, caption);
            }
        });

        sheet.show();

        progressBar.setVisibility(View.VISIBLE);
        loaderExecutor.execute(() -> {
            List<MediaItem> loaded = MediaHelper.getImagesAndVideos(activity.getApplicationContext());
            Collections.sort(loaded, (a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
            mainHandler.post(() -> {
                if (!sheet.isShowing() || activity.isFinishing() || activity.isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                allMedia.clear();
                allMedia.addAll(loaded);
                applyFilters.run();
            });
        });
    }

    private static String formatSectionDate(long epochMs, Calendar nowCal) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(epochMs);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_YEAR);
        int curYear = nowCal.get(Calendar.YEAR);
        int curDay = nowCal.get(Calendar.DAY_OF_YEAR);
        if (year == curYear && day == curDay) {
            return "Today";
        }
        Calendar yest = (Calendar) nowCal.clone();
        yest.add(Calendar.DAY_OF_YEAR, -1);
        if (year == yest.get(Calendar.YEAR) && day == yest.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }
        if (year == curYear) {
            return new SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(new Date(epochMs));
        }
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(epochMs));
    }

    private static void styleChip(TextView chip, boolean selected, int accentColor) {
        if (chip == null) return;
        if (selected) {
            chip.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A3244")));
            chip.setTextColor(Color.parseColor("#CBD5E1"));
        }
    }

    private interface OnTileClickListener {
        void onToggle(MediaItem item);
    }

    private static class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        private final List<GridRow> rows;
        private final List<MediaItem> selectedOrder;
        private final int tileSize;
        private final int accentColor;
        private final OnTileClickListener clickListener;

        GalleryAdapter(List<GridRow> rows, List<MediaItem> selectedOrder, int tileSize, int accentColor, OnTileClickListener clickListener) {
            this.rows = rows;
            this.selectedOrder = selectedOrder;
            this.tileSize = tileSize;
            this.accentColor = accentColor;
            this.clickListener = clickListener;
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View hv = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_popup_header, parent, false);
                return new HeaderVH(hv);
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_popup_thumb, parent, false);
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp == null) {
                lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tileSize);
            } else {
                lp.height = tileSize;
            }
            v.setLayoutParams(lp);
            return new ThumbVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
            GridRow row = rows.get(position);
            if (vh instanceof HeaderVH) {
                HeaderVH h = (HeaderVH) vh;
                h.tvDate.setText(row.headerTitle);
                h.tvCount.setText(String.valueOf(row.headerCount));
                return;
            }

            ThumbVH holder = (ThumbVH) vh;
            MediaItem item = row.item;
            if (item == null) return;

            Glide.with(holder.ivThumb.getContext())
                    .load(item.getUri())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .override(360, 360)
                    .placeholder(R.drawable.bg_search_input)
                    .error(R.drawable.bg_search_input)
                    .centerCrop()
                    .into(holder.ivThumb);

            if (item.isVideo()) {
                holder.layoutVideoBadge.setVisibility(View.VISIBLE);
                holder.tvDuration.setText(formatDuration(item.getDuration()));
            } else {
                holder.layoutVideoBadge.setVisibility(View.GONE);
            }

            int selNum = getSelectionNumber(item);
            if (selNum > 0) {
                holder.viewDim.setVisibility(View.VISIBLE);
                holder.viewUnselectedRing.setVisibility(View.GONE);
                holder.tvSelectNumber.setVisibility(View.VISIBLE);
                holder.tvSelectNumber.setText(String.valueOf(selNum));
                holder.tvSelectNumber.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            } else {
                holder.viewDim.setVisibility(View.GONE);
                holder.viewUnselectedRing.setVisibility(View.VISIBLE);
                holder.tvSelectNumber.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                clickListener.onToggle(item);
                notifyDataSetChanged();
            });
        }

        private int getSelectionNumber(MediaItem target) {
            for (int i = 0; i < selectedOrder.size(); i++) {
                MediaItem s = selectedOrder.get(i);
                if (s.getId() == target.getId() && s.isVideo() == target.isVideo()) {
                    return i + 1;
                }
            }
            return -1;
        }

        private String formatDuration(long ms) {
            if (ms <= 0) return "0:00";
            long totalSec = ms / 1000;
            long min = totalSec / 60;
            long sec = totalSec % 60;
            return String.format(Locale.US, "%d:%02d", min, sec);
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        static class HeaderVH extends RecyclerView.ViewHolder {
            TextView tvDate;
            TextView tvCount;
            HeaderVH(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_gallery_date_header);
                tvCount = itemView.findViewById(R.id.tv_gallery_date_count);
            }
        }

        static class ThumbVH extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            View viewDim;
            View viewUnselectedRing;
            TextView tvSelectNumber;
            LinearLayout layoutVideoBadge;
            TextView tvDuration;

            ThumbVH(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.iv_gallery_thumb);
                viewDim = itemView.findViewById(R.id.view_gallery_selected_dim);
                viewUnselectedRing = itemView.findViewById(R.id.view_gallery_unselected_ring);
                tvSelectNumber = itemView.findViewById(R.id.tv_gallery_select_number);
                layoutVideoBadge = itemView.findViewById(R.id.layout_gallery_video_badge);
                tvDuration = itemView.findViewById(R.id.tv_gallery_duration);
            }
        }
    }
}
