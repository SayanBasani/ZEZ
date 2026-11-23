package com.zez.app.music;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zez.app.R;
import com.zez.app.music.adapter.MusicAdapter;
import com.zez.app.music.adapter.MusicSuggestionAdapter;
import com.zez.app.music.download.MusicDownloadHelper;
import com.zez.app.music.model.MusicTrack;
import com.zez.app.music.player.MusicPlayerManager;
import com.zez.app.music.util.YouTubeApiHelper;

import java.util.List;
import java.util.Locale;

public class MusicActivity extends AppCompatActivity implements MusicPlayerManager.PlayerListener {

    private ImageButton btnBack;
    private ImageButton btnSearchToggle;
    private LinearLayout layoutSearchBox;
    private EditText etSearch;
    private ImageButton btnSearchClear;

    private TextView chipTrending;
    private TextView chipTopTracks;
    private TextView chipRecommended;
    private TextView chipCharts;
    private TextView chipDownloads;
    private TextView activeChip;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTracks;
    private RecyclerView rvSuggestions;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    // Mini Player
    private View cardMiniPlayer;
    private ImageView ivMiniThumb;
    private TextView tvMiniTitle;
    private TextView tvMiniArtist;
    private ImageButton btnMiniPlayPause;
    private ImageButton btnMiniNext;
    private ProgressBar pbMiniProgress;

    // Full Player BottomSheet references
    private BottomSheetDialog playerSheetDialog;
    private ImageView ivPlayerArt;
    private TextView tvPlayerTitle;
    private TextView tvPlayerArtist;
    private SeekBar sbPlayerProgress;
    private TextView tvPlayerCurrentTime;
    private TextView tvPlayerTotalTime;
    private FloatingActionButton fabPlayerPlayPause;
    private ImageButton btnPlayerPrev;
    private ImageButton btnPlayerNext;
    private ImageButton btnPlayerShuffle;
    private ImageButton btnPlayerRepeat;
    private ImageButton btnPlayerDownload;
    private boolean isUserSeeking = false;

    private MusicAdapter musicAdapter;
    private MusicSuggestionAdapter suggestionAdapter;
    private final Handler searchDebounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchDebounceRunnable;
    private boolean isSelectingSuggestion = false;
    private String currentCategory = "Trending";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        MusicPlayerManager.getInstance().init(this);

        initViews();
        setupListeners();
        setupRecyclerViews();

        // Load default category
        selectCategory(chipTrending, "Trending");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_music_back);
        btnSearchToggle = findViewById(R.id.btn_music_search_toggle);
        layoutSearchBox = findViewById(R.id.layout_music_search_box);
        etSearch = findViewById(R.id.et_music_search);
        btnSearchClear = findViewById(R.id.btn_music_search_clear);

        chipTrending = findViewById(R.id.chip_cat_trending);
        chipTopTracks = findViewById(R.id.chip_cat_top_tracks);
        chipRecommended = findViewById(R.id.chip_cat_recommended);
        chipCharts = findViewById(R.id.chip_cat_charts);
        chipDownloads = findViewById(R.id.chip_cat_downloads);

        swipeRefresh = findViewById(R.id.swipe_music);
        rvTracks = findViewById(R.id.rv_music_tracks);
        rvSuggestions = findViewById(R.id.rv_music_suggestions);
        pbLoading = findViewById(R.id.pb_music_loading);
        layoutEmpty = findViewById(R.id.layout_music_empty);
        tvEmptyTitle = findViewById(R.id.tv_music_empty_title);
        tvEmptySubtitle = findViewById(R.id.tv_music_empty_subtitle);

        // Mini player
        cardMiniPlayer = findViewById(R.id.card_mini_player);
        ivMiniThumb = findViewById(R.id.iv_mini_thumb);
        tvMiniTitle = findViewById(R.id.tv_mini_title);
        tvMiniArtist = findViewById(R.id.tv_mini_artist);
        btnMiniPlayPause = findViewById(R.id.btn_mini_play_pause);
        btnMiniNext = findViewById(R.id.btn_mini_next);
        pbMiniProgress = findViewById(R.id.pb_mini_progress);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSearchToggle.setOnClickListener(v -> {
            if (layoutSearchBox.getVisibility() == View.VISIBLE) {
                layoutSearchBox.setVisibility(View.GONE);
                rvSuggestions.setVisibility(View.GONE);
                hideKeyboard();
            } else {
                layoutSearchBox.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
                showKeyboard();
            }
        });

        btnSearchClear.setOnClickListener(v -> {
            etSearch.setText("");
            rvSuggestions.setVisibility(View.GONE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSearchClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (isSelectingSuggestion) {
                    rvSuggestions.setVisibility(View.GONE);
                    return;
                }
                if (s.length() > 1) {
                    if (searchDebounceRunnable != null) {
                        searchDebounceHandler.removeCallbacks(searchDebounceRunnable);
                    }
                    searchDebounceRunnable = () -> fetchSuggestions(s.toString());
                    searchDebounceHandler.postDelayed(searchDebounceRunnable, 300);
                } else {
                    rvSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String q = etSearch.getText().toString().trim();
                if (!q.isEmpty()) {
                    searchDebounceHandler.removeCallbacksAndMessages(null);
                    rvSuggestions.setVisibility(View.GONE);
                    hideKeyboard();
                    performSearch(q);
                }
                return true;
            }
            return false;
        });

        chipTrending.setOnClickListener(v -> selectCategory(chipTrending, "Trending"));
        chipTopTracks.setOnClickListener(v -> selectCategory(chipTopTracks, "Top tracks"));
        chipRecommended.setOnClickListener(v -> selectCategory(chipRecommended, "Recommended"));
        chipCharts.setOnClickListener(v -> selectCategory(chipCharts, "Charts"));
        chipDownloads.setOnClickListener(v -> selectCategory(chipDownloads, "Downloads"));

        swipeRefresh.setOnRefreshListener(() -> {
            if ("Downloads".equalsIgnoreCase(currentCategory)) {
                loadDownloads();
            } else {
                loadCategoryMusic(currentCategory);
            }
        });

        // Mini player click opens full player
        cardMiniPlayer.setOnClickListener(v -> showPlayerBottomSheet());
        btnMiniPlayPause.setOnClickListener(v -> MusicPlayerManager.getInstance().togglePlayPause());
        btnMiniNext.setOnClickListener(v -> MusicPlayerManager.getInstance().next());
    }

    private void setupRecyclerViews() {
        musicAdapter = new MusicAdapter(this, (track, position) -> {
            MusicPlayerManager.getInstance().playQueue(musicAdapter.getTracks(), position);
            showPlayerBottomSheet();
        });
        rvTracks.setLayoutManager(new LinearLayoutManager(this));
        rvTracks.setAdapter(musicAdapter);

        suggestionAdapter = new MusicSuggestionAdapter(query -> {
            isSelectingSuggestion = true;
            searchDebounceHandler.removeCallbacksAndMessages(null);
            etSearch.setText(query);
            etSearch.setSelection(query.length());
            rvSuggestions.setVisibility(View.GONE);
            hideKeyboard();
            performSearch(query);
            isSelectingSuggestion = false;
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(suggestionAdapter);
    }

    private void selectCategory(TextView chip, String category) {
        if (activeChip != null) {
            activeChip.setTextColor(ContextCompat.getColor(this, R.color.theme_text_secondary));
            activeChip.setBackgroundResource(R.drawable.bg_mode_toggle_normal);
        }
        activeChip = chip;
        activeChip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        activeChip.setBackgroundResource(R.drawable.bg_mode_toggle_active);

        this.currentCategory = category;
        if ("Downloads".equalsIgnoreCase(category)) {
            loadDownloads();
        } else {
            loadCategoryMusic(category);
        }
    }

    private void loadCategoryMusic(String category) {
        pbLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        YouTubeApiHelper.getInstance().getTrending(category, new YouTubeApiHelper.Callback<List<MusicTrack>>() {
            @Override
            public void onSuccess(List<MusicTrack> result) {
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (result == null || result.isEmpty()) {
                    showEmpty("No tracks found", "Could not load " + category + " music.");
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    musicAdapter.setTracks(result);
                }
            }

            @Override
            public void onError(Exception e) {
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                showEmpty("Connection error", "Unable to load music. Please check your network.");
            }
        });
    }

    private void loadDownloads() {
        pbLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        List<MusicTrack> downloaded = MusicDownloadHelper.getDownloadedTracks(this);
        if (downloaded.isEmpty()) {
            showEmpty("No downloaded tracks", "Downloaded music and videos will appear here for offline playback.");
            musicAdapter.setTracks(downloaded);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            musicAdapter.setTracks(downloaded);
        }
    }

    private void performSearch(String query) {
        searchDebounceHandler.removeCallbacksAndMessages(null);
        rvSuggestions.setVisibility(View.GONE);
        pbLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        musicAdapter.setTracks(null);

        YouTubeApiHelper.getInstance().search(query, new YouTubeApiHelper.Callback<List<MusicTrack>>() {
            @Override
            public void onSuccess(List<MusicTrack> result) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (result == null || result.isEmpty()) {
                    showEmpty("No results found", "Try another search keyword.");
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    musicAdapter.setTracks(result);
                }
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                showEmpty("Search failed", "Please verify your internet connection.");
            }
        });
    }

    private void fetchSuggestions(String query) {
        if (isSelectingSuggestion || pbLoading.getVisibility() == View.VISIBLE) return;
        YouTubeApiHelper.getInstance().getSuggestions(query, new YouTubeApiHelper.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                if (isFinishing() || isDestroyed()) return;
                if (!isSelectingSuggestion && result != null && !result.isEmpty() && layoutSearchBox.getVisibility() == View.VISIBLE) {
                    suggestionAdapter.setSuggestions(result);
                    rvSuggestions.setVisibility(View.VISIBLE);
                } else {
                    rvSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                rvSuggestions.setVisibility(View.GONE);
            }
        });
    }

    private void showEmpty(String title, String subtitle) {
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(title);
        tvEmptySubtitle.setText(subtitle);
    }

    private void showPlayerBottomSheet() {
        if (playerSheetDialog == null) {
            playerSheetDialog = new BottomSheetDialog(this, R.style.Theme_Zez_BottomSheetDialog);
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_music_player, null);
            playerSheetDialog.setContentView(sheetView);

            ivPlayerArt = sheetView.findViewById(R.id.iv_player_album_art);
            tvPlayerTitle = sheetView.findViewById(R.id.tv_player_title);
            tvPlayerArtist = sheetView.findViewById(R.id.tv_player_artist);
            sbPlayerProgress = sheetView.findViewById(R.id.sb_player_progress);
            tvPlayerCurrentTime = sheetView.findViewById(R.id.tv_player_current_time);
            tvPlayerTotalTime = sheetView.findViewById(R.id.tv_player_total_time);
            fabPlayerPlayPause = sheetView.findViewById(R.id.fab_player_play_pause);
            btnPlayerPrev = sheetView.findViewById(R.id.btn_player_prev);
            btnPlayerNext = sheetView.findViewById(R.id.btn_player_next);
            btnPlayerShuffle = sheetView.findViewById(R.id.btn_player_shuffle);
            btnPlayerRepeat = sheetView.findViewById(R.id.btn_player_repeat);
            btnPlayerDownload = sheetView.findViewById(R.id.btn_player_download);

            sheetView.findViewById(R.id.btn_player_collapse).setOnClickListener(v -> playerSheetDialog.dismiss());

            fabPlayerPlayPause.setOnClickListener(v -> MusicPlayerManager.getInstance().togglePlayPause());
            btnPlayerNext.setOnClickListener(v -> MusicPlayerManager.getInstance().next());
            btnPlayerPrev.setOnClickListener(v -> MusicPlayerManager.getInstance().previous());

            btnPlayerShuffle.setOnClickListener(v -> {
                MusicPlayerManager.getInstance().toggleShuffle();
                updateShuffleRepeatUI();
            });

            btnPlayerRepeat.setOnClickListener(v -> {
                MusicPlayerManager.getInstance().toggleRepeat();
                updateShuffleRepeatUI();
            });

            btnPlayerDownload.setOnClickListener(v -> {
                MusicTrack current = MusicPlayerManager.getInstance().getCurrentTrack();
                if (current != null) {
                    MusicDownloadHelper.showDownloadDialog(this, current, null);
                }
            });

            sheetView.findViewById(R.id.btn_player_share).setOnClickListener(v -> {
                MusicTrack current = MusicPlayerManager.getInstance().getCurrentTrack();
                if (current != null) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_SUBJECT, current.getTitle());
                    share.putExtra(Intent.EXTRA_TEXT, "Listen to " + current.getTitle() + " on YouTube:\nhttps://www.youtube.com/watch?v=" + current.getId());
                    startActivity(Intent.createChooser(share, "Share Song"));
                }
            });

            sbPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        tvPlayerCurrentTime.setText(formatTime(progress));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isUserSeeking = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isUserSeeking = false;
                    MusicPlayerManager.getInstance().seekTo(seekBar.getProgress());
                }
            });
        }

        updateFullPlayerUI();
        playerSheetDialog.show();
    }

    private void updateFullPlayerUI() {
        MusicTrack current = MusicPlayerManager.getInstance().getCurrentTrack();
        if (current == null || playerSheetDialog == null) return;

        tvPlayerTitle.setText(current.getTitle());
        tvPlayerArtist.setText(current.getArtist());
        Glide.with(this).load(current.getThumbnailUrl()).into(ivPlayerArt);

        boolean isPlaying = MusicPlayerManager.getInstance().isPlaying();
        fabPlayerPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);

        updateShuffleRepeatUI();
    }

    private void updateShuffleRepeatUI() {
        if (btnPlayerShuffle != null) {
            boolean shuffle = MusicPlayerManager.getInstance().isShuffle();
            btnPlayerShuffle.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, shuffle ? R.color.primary : R.color.theme_icon_tint)
            ));
        }
        if (btnPlayerRepeat != null) {
            boolean repeat = MusicPlayerManager.getInstance().isRepeat();
            btnPlayerRepeat.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, repeat ? R.color.primary : R.color.theme_icon_tint)
            ));
        }
    }

    // PlayerListener callbacks
    @Override
    public void onTrackChanged(MusicTrack track) {
        if (track == null) return;
        cardMiniPlayer.setVisibility(View.VISIBLE);
        tvMiniTitle.setText(track.getTitle());
        tvMiniArtist.setText(track.getArtist());
        Glide.with(this).load(track.getThumbnailUrl()).into(ivMiniThumb);

        if (playerSheetDialog != null && playerSheetDialog.isShowing()) {
            updateFullPlayerUI();
        }
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying, boolean isBuffering) {
        btnMiniPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        if (fabPlayerPlayPause != null) {
            fabPlayerPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        }
    }

    @Override
    public void onProgressUpdate(int currentSeconds, int totalSeconds) {
        if (totalSeconds > 0) {
            int progressPercent = (int) (((float) currentSeconds / totalSeconds) * 100);
            pbMiniProgress.setProgress(progressPercent);

            if (!isUserSeeking && sbPlayerProgress != null) {
                sbPlayerProgress.setMax(totalSeconds);
                sbPlayerProgress.setProgress(currentSeconds);
                tvPlayerCurrentTime.setText(formatTime(currentSeconds));
                tvPlayerTotalTime.setText(formatTime(totalSeconds));
            }
        }
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private String formatTime(int totalSecs) {
        int m = totalSecs / 60;
        int s = totalSecs % 60;
        return String.format(Locale.getDefault(), "%d:%02d", m, s);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicPlayerManager.getInstance().registerListener(this);
    }

    @Override
    protected void onPause() {
        MusicPlayerManager.getInstance().unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (searchDebounceRunnable != null) {
            searchDebounceHandler.removeCallbacks(searchDebounceRunnable);
        }
        if (playerSheetDialog != null && playerSheetDialog.isShowing()) {
            playerSheetDialog.dismiss();
        }
        super.onDestroy();
    }
}
