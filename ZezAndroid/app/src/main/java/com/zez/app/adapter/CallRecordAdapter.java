package com.zez.app.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.app.R;
import com.zez.app.model.CallRecordItem;
import com.zez.app.util.AudioPlaybackManager;
import com.zez.app.util.CallRecordDbHelper;
import com.zez.app.util.CallRecordManager;
import com.zez.app.util.PreferencesHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CallRecordAdapter extends RecyclerView.Adapter<CallRecordAdapter.RecordViewHolder> {

    public interface OnRecordDeletedListener {
        void onRecordDeleted(CallRecordItem item);
    }

    private final Context context;
    private List<CallRecordItem> items;
    private String currentlyPlayingId = null;
    private OnRecordDeletedListener onRecordDeletedListener;

    public CallRecordAdapter(Context context) {
        this(context, new ArrayList<>());
    }

    public CallRecordAdapter(Context context, List<CallRecordItem> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
    }

    public void setItems(List<CallRecordItem> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateList(List<CallRecordItem> list) {
        setItems(list);
    }

    public void setOnRecordDeletedListener(OnRecordDeletedListener listener) {
        this.onRecordDeletedListener = listener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_recording, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        CallRecordItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void stopPlayback() {
        AudioPlaybackManager.getInstance().stop();
        currentlyPlayingId = null;
        notifyDataSetChanged();
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName, tvDirection, tvPhoneNumber, tvTypeBadge, tvDate;
        ImageButton btnPlayPause;
        SeekBar sbProgress;
        TextView tvTimeCurrent, tvTimeTotal, tvSize;
        View btnShare, btnRename, btnDelete;
        ImageView ivTypeIcon, ivCloudStatus;
        View layoutSilencedBanner;
        TextView tvSilencedMsg, btnInfo;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tv_rec_contact_name);
            tvDirection = itemView.findViewById(R.id.tv_rec_direction);
            tvPhoneNumber = itemView.findViewById(R.id.tv_rec_phone_number);
            tvTypeBadge = itemView.findViewById(R.id.tv_rec_type_badge);
            tvDate = itemView.findViewById(R.id.tv_rec_date);
            ivCloudStatus = itemView.findViewById(R.id.iv_rec_cloud_status);

            layoutSilencedBanner = itemView.findViewById(R.id.layout_rec_silenced_banner);
            tvSilencedMsg = itemView.findViewById(R.id.tv_rec_silenced_msg);
            btnInfo = itemView.findViewById(R.id.btn_rec_info);

            btnPlayPause = itemView.findViewById(R.id.btn_rec_play_pause);
            sbProgress = itemView.findViewById(R.id.sb_rec_progress);
            tvTimeCurrent = itemView.findViewById(R.id.tv_rec_time_current);
            tvTimeTotal = itemView.findViewById(R.id.tv_rec_time_total);
            tvSize = itemView.findViewById(R.id.tv_rec_size);

            btnShare = itemView.findViewById(R.id.btn_rec_share);
            btnRename = itemView.findViewById(R.id.btn_rec_rename);
            btnDelete = itemView.findViewById(R.id.btn_rec_delete);
            ivTypeIcon = itemView.findViewById(R.id.iv_rec_type_icon);
        }

        void bind(CallRecordItem item) {
            int accentColor = PreferencesHelper.getAccentColorInt(context);

            String displayName = item.getContactName();
            String phone = item.getPhoneNumber();
            tvContactName.setText(displayName);
            if (phone != null && !phone.trim().isEmpty() && !phone.equals(displayName)) {
                tvPhoneNumber.setText(phone);
                tvPhoneNumber.setVisibility(View.VISIBLE);
            } else {
                tvPhoneNumber.setVisibility(View.GONE);
            }
            tvDate.setText(item.getFormattedDate());
            tvSize.setText(item.getFormattedSize());
            tvTimeTotal.setText(item.getFormattedDuration());

            String badgeText = item.getSourceBadgeText();
            tvTypeBadge.setText(badgeText);
            if (item.isImported()) {
                tvTypeBadge.setTextColor(Color.parseColor("#10B981")); // Emerald green for native cellular
            } else {
                tvTypeBadge.setTextColor(accentColor);
            }

            if (ivCloudStatus != null) {
                boolean hasCloud = item.isSynced() || (item.getCloudUrl() != null && !item.getCloudUrl().trim().isEmpty());
                ivCloudStatus.setVisibility(hasCloud ? View.VISIBLE : View.GONE);
            }

            boolean isIncoming = "INCOMING".equalsIgnoreCase(item.getDirection());
            if (isIncoming) {
                tvDirection.setText("↙ Incoming");
                tvDirection.setTextColor(Color.parseColor("#22C55E"));
            } else {
                tvDirection.setText("↗ Outgoing");
                tvDirection.setTextColor(Color.parseColor("#3B82F6"));
            }

            // Silenced Audio Notice (not applicable for imported native dialer recordings)
            if (layoutSilencedBanner != null) {
                if (item.isSilenced() && !item.isImported()) {
                    layoutSilencedBanner.setVisibility(View.VISIBLE);
                    if (tvSilencedMsg != null) {
                        tvSilencedMsg.setText("Audio capture silenced by Android OS");
                    }
                    if (btnInfo != null) {
                        btnInfo.setOnClickListener(v -> showSilencedExplanation(item));
                    }
                } else {
                    layoutSilencedBanner.setVisibility(View.GONE);
                }
            }

            ViewCompat.setBackgroundTintList(btnPlayPause, ColorStateList.valueOf(accentColor));
            sbProgress.setProgressTintList(ColorStateList.valueOf(accentColor));
            sbProgress.setThumbTintList(ColorStateList.valueOf(accentColor));

            boolean isPlayingThis = item.getId().equals(currentlyPlayingId) && AudioPlaybackManager.getInstance().isPlaying();
            boolean isPausedThis = item.getId().equals(currentlyPlayingId) && AudioPlaybackManager.getInstance().isPaused();

            if (isPlayingThis) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            }

            if (!item.getId().equals(currentlyPlayingId)) {
                sbProgress.setProgress(0);
                tvTimeCurrent.setText("00:00");
            }

            btnPlayPause.setOnClickListener(v -> {
                if (item.isSilenced() && !item.isImported() && !isPlayingThis && !isPausedThis) {
                    Toast.makeText(context, "Note: Recording audio was silenced by Android OS during this call", Toast.LENGTH_SHORT).show();
                }

                if (isPlayingThis) {
                    AudioPlaybackManager.getInstance().pause();
                    btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                } else if (isPausedThis) {
                    AudioPlaybackManager.getInstance().resume();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                } else {
                    currentlyPlayingId = item.getId();
                    notifyDataSetChanged();

                    AudioPlaybackManager.getInstance().play(item.getFilePath(), new AudioPlaybackManager.OnPlaybackListener() {
                        @Override
                        public void onPrepared(int totalDurationMs) {
                            sbProgress.setMax(totalDurationMs);
                            int secs = totalDurationMs / 1000;
                            tvTimeTotal.setText(String.format(Locale.getDefault(), "%02d:%02d", secs / 60, secs % 60));
                        }

                        @Override
                        public void onProgress(int currentMs, int totalMs) {
                            if (item.getId().equals(currentlyPlayingId)) {
                                sbProgress.setProgress(currentMs);
                                int curSecs = currentMs / 1000;
                                tvTimeCurrent.setText(String.format(Locale.getDefault(), "%02d:%02d", curSecs / 60, curSecs % 60));
                            }
                        }

                        @Override
                        public void onPaused() {
                            if (item.getId().equals(currentlyPlayingId)) {
                                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                            }
                        }

                        @Override
                        public void onResumed() {
                            if (item.getId().equals(currentlyPlayingId)) {
                                btnPlayPause.setImageResource(R.drawable.ic_pause);
                            }
                        }

                        @Override
                        public void onCompleted() {
                            if (item.getId().equals(currentlyPlayingId)) {
                                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                                sbProgress.setProgress(0);
                                tvTimeCurrent.setText("00:00");
                                currentlyPlayingId = null;
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "Playback error: " + error, Toast.LENGTH_SHORT).show();
                            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                            currentlyPlayingId = null;
                        }
                    });
                }
            });

            sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && item.getId().equals(currentlyPlayingId)) {
                        AudioPlaybackManager.getInstance().seekTo(progress);
                        int curSecs = progress / 1000;
                        tvTimeCurrent.setText(String.format(Locale.getDefault(), "%02d:%02d", curSecs / 60, curSecs % 60));
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            btnShare.setOnClickListener(v -> shareRecording(item));
            if (btnRename != null) btnRename.setOnClickListener(v -> promptRename(item, getBindingAdapterPosition()));
            btnDelete.setOnClickListener(v -> confirmDelete(item, getBindingAdapterPosition()));
        }

        private void showSilencedExplanation(CallRecordItem item) {
            String reason = item.getSilenceReason();
            if (reason == null || reason.isEmpty()) {
                reason = "Android AudioPolicyService muted the microphone during this cellular call.";
            }

            String msg = "Why is this recording silent?\n\n"
                    + "During an active cellular phone call, Android OS AudioPolicyService strictly silences third-party audio recording to protect call privacy.\n\n"
                    + "Diagnostic Report:\n"
                    + "• Status: AUDIO_CAPTURE_SILENCED\n"
                    + "• Peak Amplitude: " + item.getPeakAmplitude() + "\n"
                    + "• Cause: " + reason + "\n\n"
                    + "Standard user apps are prohibited by Android platform security from intercepting cellular baseband calls without system platform firmware signing.\n\n"
                    + "Tip: For 100% two-way crystal clear call recording, use TWING's in-app Voice Calling.";

            new AlertDialog.Builder(context)
                    .setTitle("Audio Silenced by Android")
                    .setMessage(msg)
                    .setPositiveButton("Understood", null)
                    .show();
        }

        private void promptRename(CallRecordItem item, int position) {
            android.widget.EditText input = new android.widget.EditText(context);
            input.setText(item.getContactName());
            input.setSelection(input.getText().length());
            int pad = (int) (16 * context.getResources().getDisplayMetrics().density);
            input.setPadding(pad, pad, pad, pad);

            new AlertDialog.Builder(context)
                    .setTitle("Rename Recording")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            item.setContactName(newName);
                            CallRecordDbHelper.getInstance(context).updateContactAndPhone(item.getId(), item.getPhoneNumber(), newName);
                            if (position >= 0 && position < items.size()) {
                                notifyItemChanged(position);
                            }
                            Toast.makeText(context, "Recording renamed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void shareRecording(CallRecordItem item) {
            try {
                File file = new File(item.getFilePath());
                if (!file.exists()) {
                    Toast.makeText(context, "Recording file not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri fileUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        file
                );
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("audio/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Call Recording: " + item.getContactName());
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Call Recording with " + item.getContactName() + " (" + item.getFormattedDate() + ")");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(shareIntent, "Share Call Recording"));
            } catch (Exception e) {
                Toast.makeText(context, "Failed to share: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        private void confirmDelete(CallRecordItem item, int position) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Call Recording")
                    .setMessage("Are you sure you want to delete this recorded call with " + item.getContactName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (item.getId().equals(currentlyPlayingId)) {
                            AudioPlaybackManager.getInstance().stop();
                            currentlyPlayingId = null;
                        }
                        boolean deleted = CallRecordManager.getInstance(context).deleteRecording(item.getId());
                        if (deleted) {
                            if (position >= 0 && position < items.size()) {
                                items.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, items.size());
                            }
                            if (onRecordDeletedListener != null) {
                                onRecordDeletedListener.onRecordDeleted(item);
                            }
                            Toast.makeText(context, "Call recording deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete recording", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}
