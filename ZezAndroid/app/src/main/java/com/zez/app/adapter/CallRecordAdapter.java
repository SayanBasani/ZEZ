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
import com.zez.app.util.CallRecordManager;
import com.zez.app.util.PreferencesHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CallRecordAdapter extends RecyclerView.Adapter<CallRecordAdapter.RecordViewHolder> {
    private List<CallRecordItem> items = new ArrayList<>();
    private final Context context;
    private String currentlyPlayingId = null;
    private OnRecordDeletedListener onRecordDeletedListener;

    public interface OnRecordDeletedListener {
        void onRecordDeleted(CallRecordItem item);
    }

    public CallRecordAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<CallRecordItem> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
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
        View btnShare, btnDelete;
        ImageView ivTypeIcon, ivCloudStatus;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tv_rec_contact_name);
            tvDirection = itemView.findViewById(R.id.tv_rec_direction);
            tvPhoneNumber = itemView.findViewById(R.id.tv_rec_phone_number);
            tvTypeBadge = itemView.findViewById(R.id.tv_rec_type_badge);
            tvDate = itemView.findViewById(R.id.tv_rec_date);
            ivCloudStatus = itemView.findViewById(R.id.iv_rec_cloud_status);

            btnPlayPause = itemView.findViewById(R.id.btn_rec_play_pause);
            sbProgress = itemView.findViewById(R.id.sb_rec_progress);
            tvTimeCurrent = itemView.findViewById(R.id.tv_rec_time_current);
            tvTimeTotal = itemView.findViewById(R.id.tv_rec_time_total);
            tvSize = itemView.findViewById(R.id.tv_rec_size);

            btnShare = itemView.findViewById(R.id.btn_rec_share);
            btnDelete = itemView.findViewById(R.id.btn_rec_delete);
            ivTypeIcon = itemView.findViewById(R.id.iv_rec_type_icon);
        }

        void bind(CallRecordItem item) {
            int accentColor = PreferencesHelper.getAccentColorInt(context);

            tvContactName.setText(item.getContactName());
            tvPhoneNumber.setText(item.getPhoneNumber());
            tvDate.setText(item.getFormattedDate());
            tvSize.setText(item.getFormattedSize());
            tvTimeTotal.setText(item.getFormattedDuration());

            boolean isVoip = "VOICE_CALL".equalsIgnoreCase(item.getCallType());
            tvTypeBadge.setText(isVoip ? "TWING Voice" : "Phone Call");
            tvTypeBadge.setTextColor(accentColor);

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
            btnDelete.setOnClickListener(v -> confirmDelete(item, getBindingAdapterPosition()));
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
