package com.zez.app;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zez.app.util.PdfDocumentHelper;
import com.zez.app.util.PptxGenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfGeneratorActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URIS = "extra_image_uris";

    private final List<String> imageUris = new ArrayList<>();
    private boolean isPdfMode = true; // true = PDF, false = PPT
    private boolean isAdjustMode = false;

    private View layoutPdfHeader;
    private ImageButton btnBack;
    private TextView tvTitle;

    private View layoutPreviewActions;
    private ImageButton btnShare;
    private ImageButton btnSave;

    private View layoutAdjustActions;
    private ImageButton btnAdjustDone;
    private ImageButton btnAdjustAdd;

    private View layoutSubheader;
    private View btnFormatDropdown;
    private TextView tvFormatName;
    private ImageButton btnAddMore;

    private RecyclerView rvPreview;
    private RecyclerView rvAdjust;
    private View layoutBottomBar;
    private View btnBottomViewFiles;
    private View btnBottomAdjustImage;

    private PreviewAdapter previewAdapter;
    private AdjustAdapter adjustAdapter;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_generator);

        // Receive image list
        ArrayList<String> incoming = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URIS);
        if (incoming != null && !incoming.isEmpty()) {
            imageUris.addAll(incoming);
        } else {
            String[] arr = getIntent().getStringArrayExtra(EXTRA_IMAGE_URIS);
            if (arr != null && arr.length > 0) {
                imageUris.addAll(Arrays.asList(arr));
            }
        }

        initViews();
        setupAdapters();
        setupPicker();
        setupListeners();
    }

    private void initViews() {
        layoutPdfHeader = findViewById(R.id.layout_pdf_header);
        btnBack = findViewById(R.id.btn_pdf_back);
        tvTitle = findViewById(R.id.tv_pdf_title);

        layoutPreviewActions = findViewById(R.id.layout_header_preview_actions);
        btnShare = findViewById(R.id.btn_pdf_share);
        btnSave = findViewById(R.id.btn_pdf_save);

        layoutAdjustActions = findViewById(R.id.layout_header_adjust_actions);
        btnAdjustDone = findViewById(R.id.btn_adjust_done);
        btnAdjustAdd = findViewById(R.id.btn_adjust_add);

        layoutSubheader = findViewById(R.id.layout_pdf_subheader);
        btnFormatDropdown = findViewById(R.id.btn_format_dropdown);
        tvFormatName = findViewById(R.id.tv_format_name);
        btnAddMore = findViewById(R.id.btn_add_more);

        rvPreview = findViewById(R.id.rv_pdf_preview);
        rvAdjust = findViewById(R.id.rv_pdf_adjust);
        layoutBottomBar = findViewById(R.id.layout_pdf_bottom_bar);
        btnBottomViewFiles = findViewById(R.id.btn_bottom_view_files);
        btnBottomAdjustImage = findViewById(R.id.btn_bottom_adjust_image);
    }

    private void setupAdapters() {
        // Vertical Preview Adapter
        rvPreview.setLayoutManager(new LinearLayoutManager(this));
        previewAdapter = new PreviewAdapter();
        rvPreview.setAdapter(previewAdapter);

        // Adjust Grid Adapter
        rvAdjust.setLayoutManager(new GridLayoutManager(this, 2));
        adjustAdapter = new AdjustAdapter();
        rvAdjust.setAdapter(adjustAdapter);

        // Drag & drop item touch helper for Adjust Grid
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                if (from != RecyclerView.NO_POSITION && to != RecyclerView.NO_POSITION && from != to) {
                    Collections.swap(imageUris, from, to);
                    adjustAdapter.notifyItemMoved(from, to);
                    adjustAdapter.notifyItemRangeChanged(Math.min(from, to), Math.abs(from - to) + 1);
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(rvAdjust);
    }

    private void setupPicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri u : uris) {
                            if (u != null) {
                                imageUris.add(u.toString());
                            }
                        }
                        previewAdapter.notifyDataSetChanged();
                        adjustAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Added " + uris.size() + " images", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (isAdjustMode) {
                exitAdjustMode();
            } else {
                finish();
            }
        });

        btnFormatDropdown.setOnClickListener(this::showFormatMenu);

        View.OnClickListener pickImagesListener = v -> {
            try {
                imagePickerLauncher.launch("image/*");
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open gallery", Toast.LENGTH_SHORT).show();
            }
        };
        btnAddMore.setOnClickListener(pickImagesListener);
        btnAdjustAdd.setOnClickListener(pickImagesListener);

        btnBottomAdjustImage.setOnClickListener(v -> enterAdjustMode());
        btnAdjustDone.setOnClickListener(v -> exitAdjustMode());

        btnBottomViewFiles.setOnClickListener(v -> showSavedFilesDialog());
        btnSave.setOnClickListener(v -> generateAndSaveDocument(false));
        btnShare.setOnClickListener(v -> generateAndSaveDocument(true));
    }

    private void showFormatMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Generate PDF");
        popup.getMenu().add(0, 2, 1, "Generate PPT");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                isPdfMode = true;
                tvFormatName.setText("Generate PDF");
            } else {
                isPdfMode = false;
                tvFormatName.setText("Generate PPT");
            }
            return true;
        });
        popup.show();
    }

    private void enterAdjustMode() {
        isAdjustMode = true;
        tvTitle.setText("Adjust image");
        layoutPreviewActions.setVisibility(View.GONE);
        layoutAdjustActions.setVisibility(View.VISIBLE);
        layoutSubheader.setVisibility(View.GONE);
        layoutBottomBar.setVisibility(View.GONE);
        rvPreview.setVisibility(View.GONE);
        rvAdjust.setVisibility(View.VISIBLE);
        adjustAdapter.notifyDataSetChanged();
    }

    private void exitAdjustMode() {
        isAdjustMode = false;
        tvTitle.setText("PDF/PPT");
        layoutPreviewActions.setVisibility(View.VISIBLE);
        layoutAdjustActions.setVisibility(View.GONE);
        layoutSubheader.setVisibility(View.VISIBLE);
        layoutBottomBar.setVisibility(View.VISIBLE);
        rvAdjust.setVisibility(View.GONE);
        rvPreview.setVisibility(View.VISIBLE);
        previewAdapter.notifyDataSetChanged();
    }

    private void generateAndSaveDocument(boolean andShare) {
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        String ext = isPdfMode ? "pdf" : "pptx";
        String fileName = PdfDocumentHelper.generateDefaultFileName(ext);
        File exportDir = PdfDocumentHelper.getExportDirectory(this);
        File targetFile = new File(exportDir, fileName);

        Toast.makeText(this, "Generating " + (isPdfMode ? "PDF" : "PPT") + "...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            boolean success;
            if (isPdfMode) {
                success = PdfDocumentHelper.createPdf(this, imageUris, targetFile);
            } else {
                success = PptxGenerator.createPresentation(this, imageUris, targetFile);
            }

            runOnUiThread(() -> {
                if (success) {
                    MediaScannerConnection.scanFile(this, new String[]{targetFile.getAbsolutePath()}, null, null);
                    if (andShare) {
                        shareFile(targetFile);
                    } else {
                        Toast.makeText(this, "Saved: " + targetFile.getName(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to generate " + (isPdfMode ? "PDF" : "PPT"), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void shareFile(File file) {
        try {
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(isPdfMode ? "application/pdf" : "application/vnd.openxmlformats-officedocument.presentationml.presentation");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share " + file.getName()));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSavedFilesDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_pdf_files, null);
        dialog.setContentView(view);

        RecyclerView rvFiles = view.findViewById(R.id.rv_saved_files);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_files);
        View btnClose = view.findViewById(R.id.btn_close_files_dialog);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        File dir = PdfDocumentHelper.getExportDirectory(this);
        File[] rawFiles = dir.listFiles((d, name) -> name.endsWith(".pdf") || name.endsWith(".pptx"));
        List<File> filesList = new ArrayList<>();
        if (rawFiles != null) {
            filesList.addAll(Arrays.asList(rawFiles));
            Collections.sort(filesList, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        }

        if (filesList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFiles.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFiles.setVisibility(View.VISIBLE);
            rvFiles.setLayoutManager(new LinearLayoutManager(this));
            SavedFilesAdapter filesAdapter = new SavedFilesAdapter(filesList, dialog);
            rvFiles.setAdapter(filesAdapter);
        }

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (isAdjustMode) {
            exitAdjustMode();
        } else {
            super.onBackPressed();
        }
    }

    // ── Preview Mode Adapter ───────────────────────────────────────────
    class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.PreviewViewHolder> {

        @NonNull
        @Override
        public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_page_preview, parent, false);
            return new PreviewViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
            String uriStr = imageUris.get(position);
            holder.tvBadge.setText((position + 1) + "/" + imageUris.size());

            Uri parsed = uriStr.startsWith("content://") || uriStr.startsWith("file://")
                    ? Uri.parse(uriStr)
                    : Uri.fromFile(new File(uriStr));

            Glide.with(holder.itemView.getContext())
                    .load(parsed)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(holder.ivImage);
        }

        @Override
        public int getItemCount() {
            return imageUris.size();
        }

        class PreviewViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvBadge;

            PreviewViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.iv_page_image);
                tvBadge = itemView.findViewById(R.id.tv_page_badge);
            }
        }
    }

    // ── Adjust Grid Mode Adapter ───────────────────────────────────────────
    class AdjustAdapter extends RecyclerView.Adapter<AdjustAdapter.AdjustViewHolder> {

        @NonNull
        @Override
        public AdjustViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_adjust_grid, parent, false);
            return new AdjustViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull AdjustViewHolder holder, int position) {
            String uriStr = imageUris.get(position);
            holder.tvBadge.setText((position + 1) + "/" + imageUris.size());

            Uri parsed = uriStr.startsWith("content://") || uriStr.startsWith("file://")
                    ? Uri.parse(uriStr)
                    : Uri.fromFile(new File(uriStr));

            Glide.with(holder.itemView.getContext())
                    .load(parsed)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(holder.ivThumb);
        }

        @Override
        public int getItemCount() {
            return imageUris.size();
        }

        class AdjustViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            TextView tvBadge;

            AdjustViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.iv_adjust_thumb);
                tvBadge = itemView.findViewById(R.id.tv_adjust_badge);
            }
        }
    }

    // ── Saved Files Adapter ───────────────────────────────────────────
    class SavedFilesAdapter extends RecyclerView.Adapter<SavedFilesAdapter.FileViewHolder> {

        private final List<File> files;
        private final BottomSheetDialog dialog;

        SavedFilesAdapter(List<File> files, BottomSheetDialog dialog) {
            this.files = files;
            this.dialog = dialog;
        }

        @NonNull
        @Override
        public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_saved_file, parent, false);
            return new FileViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
            File file = files.get(position);
            holder.tvName.setText(file.getName());

            boolean isPpt = file.getName().toLowerCase().endsWith(".pptx");
            holder.ivIcon.setImageResource(isPpt ? R.drawable.ic_document : R.drawable.ic_document);
            holder.ivIcon.setColorFilter(Color.parseColor(isPpt ? "#FB923C" : "#38BDF8"));

            String dateStr = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(file.lastModified()));
            long bytes = file.length();
            String sizeStr = bytes > 1024 * 1024
                    ? String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
                    : String.format(Locale.getDefault(), "%d KB", bytes / 1024);
            holder.tvInfo.setText(sizeStr + " • " + dateStr);

            holder.itemView.setOnClickListener(v -> {
                try {
                    Uri uri = FileProvider.getUriForFile(PdfGeneratorActivity.this, getPackageName() + ".fileprovider", file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, isPpt ? "application/vnd.openxmlformats-officedocument.presentationml.presentation" : "application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(PdfGeneratorActivity.this, "No app found to open this file", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnShare.setOnClickListener(v -> {
                try {
                    Uri uri = FileProvider.getUriForFile(PdfGeneratorActivity.this, getPackageName() + ".fileprovider", file);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType(isPpt ? "application/vnd.openxmlformats-officedocument.presentationml.presentation" : "application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Share " + file.getName()));
                } catch (Exception e) {
                    Toast.makeText(PdfGeneratorActivity.this, "Error sharing file", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(PdfGeneratorActivity.this)
                        .setTitle("Delete Document")
                        .setMessage("Delete " + file.getName() + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            file.delete();
                            int pos = holder.getBindingAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                files.remove(pos);
                                notifyItemRemoved(pos);
                            }
                            if (files.isEmpty() && dialog != null) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        class FileViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvName;
            TextView tvInfo;
            ImageButton btnShare;
            ImageButton btnDelete;

            FileViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_file_icon);
                tvName = itemView.findViewById(R.id.tv_file_name);
                tvInfo = itemView.findViewById(R.id.tv_file_info);
                btnShare = itemView.findViewById(R.id.btn_file_share);
                btnDelete = itemView.findViewById(R.id.btn_file_delete);
            }
        }
    }
}
