package com.zez.manager.music;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zez.manager.MainActivity;
import com.zez.manager.R;
import com.zez.manager.music.adapter.ActiveDownloadAdapter;
import com.zez.manager.music.adapter.MusicAdapter;
import com.zez.manager.music.adapter.MusicSuggestionAdapter;
import com.zez.manager.music.download.DownloadTask;
import com.zez.manager.music.download.MusicDownloadHelper;
import com.zez.manager.music.download.MusicDownloadManager;
import com.zez.manager.music.model.MusicTrack;
import com.zez.manager.music.player.MusicPlayerManager;
import com.zez.manager.music.util.MusicHistoryManager;
import com.zez.manager.music.util.MusicRecommendationEngine;
import com.zez.manager.music.util.YouTubeApiHelper;

import java.util.List;
import java.util.Locale;

/**
 * Controller for YMusic inside Zez Tab 1 (Primary Home Screen).
 * Manages category browsing, live search suggestions, downloads subtabs,
 * playback history, cross-tab mini-player dock, and full-screen player bottom sheet.
 */
public class MusicViewController implements MusicPlayerManager.PlayerListener {

    private static final String TAG = "MusicViewController";

    private final MainActivity activity;

    // YMusic Tab Views
    private DrawerLayout drawerMusic;
    private ImageButton btnMusicMenu;
    private ImageButton btnSearchToggle;
    private LinearLayout layoutSearchBox;
    private EditText etSearch;
    private ImageButton btnSearchClear;

    private TextView tvMusicMainTitle;
    private TextView tvMusicModeBadge;
    private TextView chipTrending;
    private TextView chipTopTracks;
    private TextView chipViral;
    private TextView chipRecommended;
    private TextView chipCharts;
    private TextView chipDownloads;
    private TextView chipHistory;
    private TextView activeChip;

    private View drawerItemYtMusic;
    private View drawerItemViral;
    private String currentContinuationToken = null;
    private boolean isLoadingMore = false;
    private boolean isYtMusicMode = false;

    // History Header
    private View layoutHistoryHeader;
    private TextView tvHistoryCount;
    private TextView btnClearPlaybackHistory;

    // Downloads Sub-Tabs
    private View layoutDownloadsSubtabs;
    private TextView btnSubtabDownloading;
    private TextView btnSubtabCompleted;
    private ActiveDownloadAdapter activeDownloadAdapter;
    private boolean isViewingDownloadingSubtab = false;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTracks;
    private RecyclerView rvSuggestions;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    // Cross-Tab Persistent Mini Player Views
    private View cardMiniPlayer;
    private ImageView ivMiniThumb;
    private TextView tvMiniTitle;
    private TextView tvMiniArtist;
    private ImageButton btnMiniPlayPause;
    private ImageButton btnMiniNext;
    private ImageButton btnMiniClose;
    private ProgressBar pbMiniProgress;

    // Full Player BottomSheet references
    private BottomSheetDialog playerSheetDialog;
    private TextView btnModeAudio;
    private TextView btnModeVideo;
    private FrameLayout layoutPlayerVideoContainer;
    private ImageButton btnPlayerQueueTop;
    private ImageButton btnPlayerMore;
    private View btnPlayerQueueBtn;
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
    private View btnPlayerDownload;
    private boolean isUserSeeking = false;
    private WebView activityVideoWebView = null;

    private MusicAdapter musicAdapter;
    private MusicSuggestionAdapter suggestionAdapter;
    private final Handler searchDebounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchDebounceRunnable;
    private boolean isSelectingSuggestion = false;
    private boolean isSearchResultMode = false;
    private String currentCategory = "Trending";

    public MusicViewController(@NonNull MainActivity activity) {
        this.activity = activity;
        MusicPlayerManager.getInstance().init(activity);
        initViews();
        setupListeners();
        setupRecyclerViews();

        // Load default category
        selectCategory(chipTrending, "Trending");
    }

    private void initViews() {
        drawerMusic = activity.findViewById(R.id.drawer_music);
        btnMusicMenu = activity.findViewById(R.id.btn_music_menu);
        btnSearchToggle = activity.findViewById(R.id.btn_music_search_toggle);
        layoutSearchBox = activity.findViewById(R.id.layout_music_search_box);
        etSearch = activity.findViewById(R.id.et_music_search);
        btnSearchClear = activity.findViewById(R.id.btn_music_search_clear);

        tvMusicMainTitle = activity.findViewById(R.id.tv_music_main_title);
        tvMusicModeBadge = activity.findViewById(R.id.tv_music_mode_badge);

        chipTrending = activity.findViewById(R.id.chip_cat_trending);
        chipTopTracks = activity.findViewById(R.id.chip_cat_top_tracks);
        chipViral = activity.findViewById(R.id.chip_cat_viral);
        chipRecommended = activity.findViewById(R.id.chip_cat_recommended);
        chipCharts = activity.findViewById(R.id.chip_cat_charts);
        chipDownloads = activity.findViewById(R.id.chip_cat_downloads);
        chipHistory = activity.findViewById(R.id.chip_cat_history);

        drawerItemYtMusic = activity.findViewById(R.id.drawer_item_yt_music);
        drawerItemViral = activity.findViewById(R.id.drawer_item_viral);

        swipeRefresh = activity.findViewById(R.id.swipe_music);
        rvTracks = activity.findViewById(R.id.rv_music_tracks);
        rvSuggestions = activity.findViewById(R.id.rv_music_suggestions);
        pbLoading = activity.findViewById(R.id.pb_music_loading);
        layoutEmpty = activity.findViewById(R.id.layout_music_empty);
        tvEmptyTitle = activity.findViewById(R.id.tv_music_empty_title);
        tvEmptySubtitle = activity.findViewById(R.id.tv_music_empty_subtitle);

        // Downloads Sub-Tabs
        layoutDownloadsSubtabs = activity.findViewById(R.id.layout_downloads_subtabs);
        btnSubtabDownloading = activity.findViewById(R.id.btn_subtab_downloading);
        btnSubtabCompleted = activity.findViewById(R.id.btn_subtab_completed);

        // History Header
        layoutHistoryHeader = activity.findViewById(R.id.layout_history_header);
        tvHistoryCount = activity.findViewById(R.id.tv_history_count);
        btnClearPlaybackHistory = activity.findViewById(R.id.btn_clear_playback_history);

        // Persistent Mini player
        cardMiniPlayer = activity.findViewById(R.id.card_mini_player);
        ivMiniThumb = activity.findViewById(R.id.iv_mini_thumb);
        tvMiniTitle = activity.findViewById(R.id.tv_mini_title);
        tvMiniArtist = activity.findViewById(R.id.tv_mini_artist);
        btnMiniPlayPause = activity.findViewById(R.id.btn_mini_play_pause);
        btnMiniNext = activity.findViewById(R.id.btn_mini_next);
        btnMiniClose = activity.findViewById(R.id.btn_mini_close);
        pbMiniProgress = activity.findViewById(R.id.pb_mini_progress);
    }

    private void setupListeners() {
        if (btnMusicMenu != null) {
            btnMusicMenu.setOnClickListener(v -> {
                if (drawerMusic != null) {
                    drawerMusic.openDrawer(GravityCompat.START);
                }
            });
        }

        // Drawer items
        setupDrawerItem(R.id.drawer_item_trending, chipTrending, "Trending");
        setupDrawerItem(R.id.drawer_item_top_tracks, chipTopTracks, "Top tracks");
        setupDrawerItem(R.id.drawer_item_recommended, chipRecommended, "Recommended");
        setupDrawerItem(R.id.drawer_item_charts, chipCharts, "Charts");
        setupDrawerItem(R.id.drawer_item_downloads, chipDownloads, "Downloads");
        setupDrawerItem(R.id.drawer_item_history, chipHistory, "History");

        View drawerSleep = activity.findViewById(R.id.drawer_item_sleep_timer);
        if (drawerSleep != null) {
            drawerSleep.setOnClickListener(v -> {
                if (drawerMusic != null) drawerMusic.closeDrawers();
                showSleepTimerDialog();
            });
        }

        if (drawerItemYtMusic != null) {
            drawerItemYtMusic.setOnClickListener(v -> switchMode(true));
        }
        if (drawerItemViral != null) {
            drawerItemViral.setOnClickListener(v -> {
                if (drawerMusic != null) drawerMusic.closeDrawers();
                if (chipViral != null) {
                    selectCategory(chipViral, "Viral 50");
                } else {
                    selectCategory(chipTrending, "Viral 50");
                }
            });
        }

        // Search bar toggle & input
        btnSearchToggle.setOnClickListener(v -> {
            if (layoutSearchBox.getVisibility() == View.VISIBLE) {
                layoutSearchBox.setVisibility(View.GONE);
                rvSuggestions.setVisibility(View.GONE);
                hideKeyboard();
            } else {
                layoutSearchBox.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
                showKeyboard();
                showRecentSearchSuggestions();
            }
        });

        btnSearchClear.setOnClickListener(v -> {
            etSearch.setText("");
            showRecentSearchSuggestions();
        });

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etSearch.getText().toString().trim().length() <= 1) {
                showRecentSearchSuggestions();
            }
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
                    showRecentSearchSuggestions();
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

        // Category Chips
        chipTrending.setOnClickListener(v -> selectCategory(chipTrending, "Trending"));
        chipTopTracks.setOnClickListener(v -> selectCategory(chipTopTracks, "Top tracks"));
        if (chipViral != null) {
            chipViral.setOnClickListener(v -> selectCategory(chipViral, "Viral 50"));
        }
        chipRecommended.setOnClickListener(v -> selectCategory(chipRecommended, "Recommended"));
        chipCharts.setOnClickListener(v -> selectCategory(chipCharts, "Charts"));
        chipDownloads.setOnClickListener(v -> selectCategory(chipDownloads, "Downloads"));
        if (chipHistory != null) {
            chipHistory.setOnClickListener(v -> selectCategory(chipHistory, "History"));
        }

        // Infinite Scrolling on rvTracks
        rvTracks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null && !isLoadingMore && currentContinuationToken != null && !currentContinuationToken.isEmpty()) {
                        int visibleCount = lm.getChildCount();
                        int totalCount = lm.getItemCount();
                        int firstVisible = lm.findFirstVisibleItemPosition();
                        if ((visibleCount + firstVisible) >= totalCount - 4 && totalCount >= 10) {
                            loadMoreTracks();
                        }
                    }
                }
            }
        });

        // History clear
        if (btnClearPlaybackHistory != null) {
            btnClearPlaybackHistory.setOnClickListener(v -> {
                new AlertDialog.Builder(activity)
                        .setTitle("Clear Playback History")
                        .setMessage("Are you sure you want to clear your recently played songs?")
                        .setPositiveButton("Clear", (dialog, which) -> {
                            MusicHistoryManager.clearPlaybackHistory(activity);
                            loadPlaybackHistory();
                            Toast.makeText(activity, "Playback history cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        // SwipeRefresh
        swipeRefresh.setOnRefreshListener(() -> {
            if ("Downloads".equalsIgnoreCase(currentCategory)) {
                if (isViewingDownloadingSubtab) {
                    refreshDownloadingList();
                } else {
                    loadCompletedDownloads();
                }
            } else if ("History".equalsIgnoreCase(currentCategory)) {
                loadPlaybackHistory();
            } else if (isSearchResultMode && etSearch != null && !etSearch.getText().toString().trim().isEmpty()) {
                performSearch(etSearch.getText().toString().trim());
            } else {
                loadCategoryMusic(currentCategory, true);
            }
        });

        // Mini player click opens full player
        View.OnClickListener openPlayerSheetListener = v -> showPlayerBottomSheet();
        cardMiniPlayer.setOnClickListener(openPlayerSheetListener);
        if (tvMiniTitle != null) tvMiniTitle.setOnClickListener(openPlayerSheetListener);
        if (tvMiniArtist != null) tvMiniArtist.setOnClickListener(openPlayerSheetListener);
        if (ivMiniThumb != null) ivMiniThumb.setOnClickListener(openPlayerSheetListener);
        btnMiniPlayPause.setOnClickListener(v -> MusicPlayerManager.getInstance().togglePlayPause());
        btnMiniNext.setOnClickListener(v -> MusicPlayerManager.getInstance().next());
        if (btnMiniClose != null) {
            btnMiniClose.setOnClickListener(v -> {
                MusicPlayerManager.getInstance().stop();
                if (cardMiniPlayer != null) cardMiniPlayer.setVisibility(View.GONE);
            });
        }

        // Downloads Sub-Tabs clicks
        if (btnSubtabDownloading != null) {
            btnSubtabDownloading.setOnClickListener(v -> switchDownloadsSubtab(true));
        }
        if (btnSubtabCompleted != null) {
            btnSubtabCompleted.setOnClickListener(v -> switchDownloadsSubtab(false));
        }
    }

    private void setupRecyclerViews() {
        musicAdapter = new MusicAdapter(activity, (track, position) -> {
            if (isSearchResultMode) {
                // Exact single-track selection: never auto-advance into alternate versions of the same song
                MusicPlayerManager.getInstance().playTrack(track);
            } else if ("Downloads".equalsIgnoreCase(currentCategory) || "History".equalsIgnoreCase(currentCategory)) {
                MusicPlayerManager.getInstance().playQueue(musicAdapter.getTracks(), position);
            } else {
                List<MusicTrack> deduped = MusicRecommendationEngine.deduplicateBySongTitle(musicAdapter.getTracks());
                int idx = deduped.indexOf(track);
                if (idx < 0) {
                    deduped.add(0, track);
                    idx = 0;
                }
                MusicPlayerManager.getInstance().playQueue(deduped, idx);
            }
            showPlayerBottomSheet();
        });

        activeDownloadAdapter = new ActiveDownloadAdapter(activity, task -> {
            MusicDownloadManager.getInstance().cancelDownload(task.getVideoId());
            Toast.makeText(activity, "Download cancelled", Toast.LENGTH_SHORT).show();
        });

        rvTracks.setLayoutManager(new LinearLayoutManager(activity));
        rvTracks.setAdapter(musicAdapter);

        suggestionAdapter = new MusicSuggestionAdapter(new MusicSuggestionAdapter.OnSuggestionClickListener() {
            @Override
            public void onSuggestionClick(String query) {
                isSelectingSuggestion = true;
                searchDebounceHandler.removeCallbacksAndMessages(null);
                etSearch.setText(query);
                etSearch.setSelection(query.length());
                rvSuggestions.setVisibility(View.GONE);
                hideKeyboard();
                performSearch(query);
                isSelectingSuggestion = false;
            }

            @Override
            public void onClearHistoryClick() {
                MusicHistoryManager.clearSearchHistory(activity);
                rvSuggestions.setVisibility(View.GONE);
                Toast.makeText(activity, "Search history cleared", Toast.LENGTH_SHORT).show();
            }
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(activity));
        rvSuggestions.setAdapter(suggestionAdapter);
    }

    public void selectCategory(TextView chip, String category) {
        if (activeChip != null) {
            activeChip.setTextColor(ContextCompat.getColor(activity, R.color.theme_text_secondary));
            activeChip.setBackgroundResource(R.drawable.bg_mode_toggle_normal);
        }
        activeChip = chip;
        if (activeChip != null) {
            activeChip.setTextColor(ContextCompat.getColor(activity, android.R.color.white));
            activeChip.setBackgroundResource(R.drawable.bg_mode_toggle_active);
        }

        this.isSearchResultMode = false;
        if (musicAdapter != null) musicAdapter.setSearchResultMode(false);
        this.currentCategory = category;
        if ("Downloads".equalsIgnoreCase(category)) {
            if (layoutDownloadsSubtabs != null) layoutDownloadsSubtabs.setVisibility(View.VISIBLE);
            if (layoutHistoryHeader != null) layoutHistoryHeader.setVisibility(View.GONE);
            loadDownloads();
        } else if ("History".equalsIgnoreCase(category)) {
            if (layoutDownloadsSubtabs != null) layoutDownloadsSubtabs.setVisibility(View.GONE);
            if (layoutHistoryHeader != null) layoutHistoryHeader.setVisibility(View.VISIBLE);
            rvTracks.setAdapter(musicAdapter);
            loadPlaybackHistory();
        } else {
            if (layoutDownloadsSubtabs != null) layoutDownloadsSubtabs.setVisibility(View.GONE);
            if (layoutHistoryHeader != null) layoutHistoryHeader.setVisibility(View.GONE);
            rvTracks.setAdapter(musicAdapter);
            loadCategoryMusic(category, false);
        }
    }

    private void loadCategoryMusic(String category, boolean forceRefresh) {
        currentContinuationToken = null;
        pbLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        YouTubeApiHelper.getInstance().getPersonalizedFeed(activity, category, forceRefresh, new YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>() {
            @Override
            public void onSuccess(YouTubeApiHelper.SearchResult result) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (result == null || result.getTracks() == null || result.getTracks().isEmpty()) {
                    showEmpty("No tracks found", "Could not load " + category + " music.");
                } else {
                    currentContinuationToken = result.getContinuationToken();
                    layoutEmpty.setVisibility(View.GONE);
                    musicAdapter.setTracks(result.getTracks());
                }
            }

            @Override
            public void onError(Exception e) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                showEmpty("Connection error", "Unable to load music. Please check your network.");
            }
        });
    }

    private void loadDownloads() {
        pbLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        if (layoutDownloadsSubtabs != null) layoutDownloadsSubtabs.setVisibility(View.VISIBLE);

        List<DownloadTask> active = MusicDownloadManager.getInstance().getActiveTasks();
        if (!active.isEmpty()) {
            switchDownloadsSubtab(true);
        } else {
            switchDownloadsSubtab(false);
        }
    }

    private void switchDownloadsSubtab(boolean showDownloading) {
        this.isViewingDownloadingSubtab = showDownloading;
        updateDownloadBadge();

        if (showDownloading) {
            btnSubtabDownloading.setTextColor(ContextCompat.getColor(activity, R.color.theme_text_primary));
            btnSubtabDownloading.setBackgroundResource(R.drawable.bg_mode_toggle_active);
            btnSubtabCompleted.setTextColor(ContextCompat.getColor(activity, R.color.theme_text_secondary));
            btnSubtabCompleted.setBackgroundResource(R.drawable.bg_mode_toggle_normal);

            rvTracks.setAdapter(activeDownloadAdapter);
            refreshDownloadingList();
        } else {
            btnSubtabCompleted.setTextColor(ContextCompat.getColor(activity, R.color.theme_text_primary));
            btnSubtabCompleted.setBackgroundResource(R.drawable.bg_mode_toggle_active);
            btnSubtabDownloading.setTextColor(ContextCompat.getColor(activity, R.color.theme_text_secondary));
            btnSubtabDownloading.setBackgroundResource(R.drawable.bg_mode_toggle_normal);

            rvTracks.setAdapter(musicAdapter);
            loadCompletedDownloads();
        }
    }

    private void updateDownloadBadge() {
        if (btnSubtabDownloading != null) {
            int count = MusicDownloadManager.getInstance().getActiveTasks().size();
            btnSubtabDownloading.setText("⏳ Downloading (" + count + ")");
        }
    }

    private void refreshDownloadingList() {
        pbLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        List<DownloadTask> tasks = MusicDownloadManager.getInstance().getActiveTasks();
        updateDownloadBadge();
        if (tasks.isEmpty()) {
            showEmpty("No active downloads", "Media currently downloading with live speed will appear here.");
            activeDownloadAdapter.setTasks(tasks);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            activeDownloadAdapter.setTasks(tasks);
        }
    }

    private void loadCompletedDownloads() {
        pbLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        List<MusicTrack> downloaded = MusicDownloadHelper.getDownloadedTracks(activity);
        updateDownloadBadge();
        if (downloaded.isEmpty()) {
            showEmpty("No downloaded tracks", "Downloaded music and videos will appear here for offline playback.");
            musicAdapter.setTracks(downloaded);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            musicAdapter.setTracks(downloaded);
        }
    }

    private void loadPlaybackHistory() {
        pbLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        List<MusicTrack> hist = MusicHistoryManager.getPlaybackHistory(activity);
        if (tvHistoryCount != null) {
            tvHistoryCount.setText("Recently Played (" + hist.size() + ")");
        }
        if (hist.isEmpty()) {
            showEmpty("No playback history", "Songs you listen to will automatically appear here.");
            musicAdapter.setTracks(hist);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            musicAdapter.setTracks(hist);
        }
    }

    private void showRecentSearchSuggestions() {
        if (isSelectingSuggestion || activity.isFinishing() || activity.isDestroyed()) return;
        List<String> history = MusicHistoryManager.getSearchHistory(activity);
        if (history != null && !history.isEmpty() && layoutSearchBox.getVisibility() == View.VISIBLE) {
            suggestionAdapter.setHistorySuggestions(history);
            rvSuggestions.setVisibility(View.VISIBLE);
        } else {
            rvSuggestions.setVisibility(View.GONE);
        }
    }

    private void performSearch(String query) {
        isSearchResultMode = true;
        if (musicAdapter != null) musicAdapter.setSearchResultMode(true);
        currentContinuationToken = null;
        MusicHistoryManager.addSearchQuery(activity, query);
        searchDebounceHandler.removeCallbacksAndMessages(null);
        rvSuggestions.setVisibility(View.GONE);
        pbLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        musicAdapter.setTracks(null);

        YouTubeApiHelper.getInstance().search(query, new YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>() {
            @Override
            public void onSuccess(YouTubeApiHelper.SearchResult result) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (result == null || result.getTracks() == null || result.getTracks().isEmpty()) {
                    showEmpty("No results found", "Try another search keyword.");
                } else {
                    currentContinuationToken = result.getContinuationToken();
                    layoutEmpty.setVisibility(View.GONE);
                    musicAdapter.setTracks(result.getTracks());
                }
            }

            @Override
            public void onError(Exception e) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                showEmpty("Search failed", "Please verify your internet connection.");
            }
        });
    }

    private void loadMoreTracks() {
        if (isLoadingMore || currentContinuationToken == null || currentContinuationToken.isEmpty()) return;
        isLoadingMore = true;
        YouTubeApiHelper.getInstance().searchContinuation(currentContinuationToken, new YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>() {
            @Override
            public void onSuccess(YouTubeApiHelper.SearchResult result) {
                isLoadingMore = false;
                if (activity.isFinishing() || activity.isDestroyed()) return;
                currentContinuationToken = result.getContinuationToken();
                if (result.getTracks() != null && !result.getTracks().isEmpty()) {
                    musicAdapter.appendTracks(result.getTracks());
                }
            }

            @Override
            public void onError(Exception e) {
                isLoadingMore = false;
                Log.w(TAG, "Error loading more tracks: " + e.getMessage());
            }
        });
    }

    private void switchMode(boolean isYtMusic) {
        this.isYtMusicMode = isYtMusic;
        if (drawerMusic != null) drawerMusic.closeDrawers();
        if (tvMusicMainTitle != null) {
            tvMusicMainTitle.setText(isYtMusic ? "YT Music" : "YMusic");
        }
        if (tvMusicModeBadge != null) {
            tvMusicModeBadge.setText(isYtMusic ? "YouTube Music" : "Music");
            tvMusicModeBadge.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.parseColor(isYtMusic ? "#EF4444" : "#FF0000")));
        }
        if (isYtMusic) {
            selectCategory(chipViral != null ? chipViral : chipTrending, "Viral 50");
            Toast.makeText(activity, "Switched to YT Music (Charts & Viral)", Toast.LENGTH_SHORT).show();
        } else {
            selectCategory(chipTrending, "Trending");
        }
    }

    private void fetchSuggestions(String query) {
        if (isSelectingSuggestion || pbLoading.getVisibility() == View.VISIBLE) return;
        YouTubeApiHelper.getInstance().getSuggestions(query, new YouTubeApiHelper.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                if (!isSelectingSuggestion && result != null && !result.isEmpty() && layoutSearchBox.getVisibility() == View.VISIBLE) {
                    suggestionAdapter.setSuggestions(result);
                    rvSuggestions.setVisibility(View.VISIBLE);
                } else {
                    rvSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                rvSuggestions.setVisibility(View.GONE);
            }
        });
    }

    private void showEmpty(String title, String subtitle) {
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(title);
        tvEmptySubtitle.setText(subtitle);
    }

    public void showPlayerBottomSheet() {
        if (playerSheetDialog == null) {
            playerSheetDialog = new BottomSheetDialog(activity, R.style.Theme_Zez_BottomSheetDialog);
            View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_music_player, null);
            playerSheetDialog.setContentView(sheetView);

            btnModeAudio = sheetView.findViewById(R.id.btn_mode_audio);
            btnModeVideo = sheetView.findViewById(R.id.btn_mode_video);
            layoutPlayerVideoContainer = sheetView.findViewById(R.id.layout_player_video_container);
            btnPlayerQueueTop = sheetView.findViewById(R.id.btn_player_queue_top);
            btnPlayerMore = sheetView.findViewById(R.id.btn_player_more);
            btnPlayerQueueBtn = sheetView.findViewById(R.id.btn_player_queue_btn);

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

            View btnPlayerClose = sheetView.findViewById(R.id.btn_player_close);
            if (btnPlayerClose != null) {
                btnPlayerClose.setOnClickListener(v -> {
                    playerSheetDialog.dismiss();
                    MusicPlayerManager.getInstance().stop();
                    if (cardMiniPlayer != null) cardMiniPlayer.setVisibility(View.GONE);
                });
            }

            if (btnModeAudio != null) {
                btnModeAudio.setOnClickListener(v -> MusicPlayerManager.getInstance().setVideoMode(false));
            }
            if (btnModeVideo != null) {
                btnModeVideo.setOnClickListener(v -> MusicPlayerManager.getInstance().setVideoMode(true));
            }

            if (btnPlayerQueueTop != null) {
                btnPlayerQueueTop.setOnClickListener(v -> showQueueBottomSheet());
            }
            if (btnPlayerQueueBtn != null) {
                btnPlayerQueueBtn.setOnClickListener(v -> showQueueBottomSheet());
            }

            if (btnPlayerMore != null) {
                btnPlayerMore.setOnClickListener(v -> {
                    MusicTrack current = MusicPlayerManager.getInstance().getCurrentTrack();
                    if (current != null && musicAdapter != null) {
                        musicAdapter.showTrackOptionsSheet(current);
                    }
                });
            }

            playerSheetDialog.setOnDismissListener(dialog -> {
                if (layoutPlayerVideoContainer != null) {
                    layoutPlayerVideoContainer.removeAllViews();
                }
            });

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
                    MusicDownloadHelper.showDownloadDialog(activity, current, null);
                }
            });

            sheetView.findViewById(R.id.btn_player_share).setOnClickListener(v -> {
                MusicTrack current = MusicPlayerManager.getInstance().getCurrentTrack();
                if (current != null) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_SUBJECT, current.getTitle());
                    share.putExtra(Intent.EXTRA_TEXT, "Listen to " + current.getTitle() + " on YouTube:\nhttps://www.youtube.com/watch?v=" + current.getId());
                    activity.startActivity(Intent.createChooser(share, "Share Song"));
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
        Glide.with(activity).load(current.getThumbnailUrl()).into(ivPlayerArt);

        int initialDur = parseDurationToSeconds(current.getDuration());
        if (initialDur > 0) {
            if (tvPlayerTotalTime != null) tvPlayerTotalTime.setText(formatTime(initialDur));
            if (sbPlayerProgress != null) sbPlayerProgress.setMax(initialDur);
        } else if (current.getDuration() != null && !current.getDuration().isEmpty()) {
            if (tvPlayerTotalTime != null) tvPlayerTotalTime.setText(current.getDuration());
        }

        boolean isPlaying = MusicPlayerManager.getInstance().isPlaying();
        fabPlayerPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);

        updateShuffleRepeatUI();
        updateVideoModeUI(MusicPlayerManager.getInstance().isVideoMode());
    }

    private void updateShuffleRepeatUI() {
        if (btnPlayerShuffle != null) {
            boolean shuffle = MusicPlayerManager.getInstance().isShuffle();
            btnPlayerShuffle.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(activity, shuffle ? R.color.primary : R.color.theme_icon_tint)
            ));
        }
        if (btnPlayerRepeat != null) {
            int repeatMode = MusicPlayerManager.getInstance().getRepeatMode();
            if (repeatMode == MusicPlayerManager.REPEAT_MODE_NONE) {
                btnPlayerRepeat.setImageResource(R.drawable.ic_repeat);
                btnPlayerRepeat.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(activity, R.color.theme_icon_tint)
                ));
            } else if (repeatMode == MusicPlayerManager.REPEAT_MODE_ALL) {
                btnPlayerRepeat.setImageResource(R.drawable.ic_repeat);
                btnPlayerRepeat.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(activity, R.color.primary)
                ));
            } else if (repeatMode == MusicPlayerManager.REPEAT_MODE_ONE) {
                btnPlayerRepeat.setImageResource(R.drawable.ic_repeat_one);
                btnPlayerRepeat.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(activity, R.color.primary)
                ));
            }
        }
    }

    private void updateVideoModeUI(boolean isVideo) {
        if (btnModeAudio == null || btnModeVideo == null) return;
        if (isVideo) {
            btnModeVideo.setTextColor(ContextCompat.getColor(activity, android.R.color.white));
            btnModeVideo.setBackgroundResource(R.drawable.bg_mode_toggle_active);
            btnModeAudio.setTextColor(ContextCompat.getColor(activity, R.color.theme_text_muted));
            btnModeAudio.setBackground(null);

            if (layoutPlayerVideoContainer != null) {
                MusicTrack current = MusicPlayerManager.getInstance().getCurrentTrack();
                if (current != null) {
                    if (activityVideoWebView == null) {
                        activityVideoWebView = new WebView(activity);
                        activityVideoWebView.getSettings().setJavaScriptEnabled(true);
                        activityVideoWebView.getSettings().setDomStorageEnabled(true);
                        activityVideoWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                    }
                    if (activityVideoWebView.getParent() != layoutPlayerVideoContainer) {
                        if (activityVideoWebView.getParent() instanceof ViewGroup) {
                            ((ViewGroup) activityVideoWebView.getParent()).removeView(activityVideoWebView);
                        }
                        layoutPlayerVideoContainer.removeAllViews();
                        layoutPlayerVideoContainer.addView(activityVideoWebView, new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    }
                    activityVideoWebView.loadUrl("https://www.youtube.com/embed/" + current.getId() + "?autoplay=1");
                }
                layoutPlayerVideoContainer.setVisibility(View.VISIBLE);
                layoutPlayerVideoContainer.setAlpha(1.0f);
                layoutPlayerVideoContainer.bringToFront();
            }
        } else {
            btnModeAudio.setTextColor(ContextCompat.getColor(activity, android.R.color.white));
            btnModeAudio.setBackgroundResource(R.drawable.bg_mode_toggle_active);
            btnModeVideo.setTextColor(ContextCompat.getColor(activity, R.color.theme_text_muted));
            btnModeVideo.setBackground(null);

            if (activityVideoWebView != null) {
                activityVideoWebView.loadUrl("about:blank");
                if (layoutPlayerVideoContainer != null) {
                    layoutPlayerVideoContainer.removeAllViews();
                }
            }

            if (ivPlayerArt != null) {
                ivPlayerArt.setVisibility(View.VISIBLE);
                ivPlayerArt.bringToFront();
            }
            if (layoutPlayerVideoContainer != null) {
                layoutPlayerVideoContainer.setVisibility(View.GONE);
                layoutPlayerVideoContainer.setAlpha(0.001f);
            }
        }
    }

    private void showQueueBottomSheet() {
        List<MusicTrack> queue = MusicPlayerManager.getInstance().getQueue();
        BottomSheetDialog queueDialog = new BottomSheetDialog(activity, R.style.Theme_Zez_BottomSheetDialog);
        View view = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_queue, null);
        queueDialog.setContentView(view);

        TextView tvHeading = view.findViewById(R.id.tv_queue_heading);
        TextView tvEmpty = view.findViewById(R.id.tv_queue_empty);
        Button btnClear = view.findViewById(R.id.btn_queue_clear);
        RecyclerView rvQueue = view.findViewById(R.id.rv_player_queue);

        if (queue == null || queue.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvQueue.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvQueue.setVisibility(View.VISIBLE);
            tvHeading.setText("Up Next (" + queue.size() + ")");
            MusicAdapter queueAdapter = new MusicAdapter(activity, (track, position) -> {
                queueDialog.dismiss();
                MusicPlayerManager.getInstance().playQueueIndex(position);
            });
            queueAdapter.setTracks(queue);
            rvQueue.setLayoutManager(new LinearLayoutManager(activity));
            rvQueue.setAdapter(queueAdapter);
        }

        btnClear.setOnClickListener(v -> {
            MusicPlayerManager.getInstance().clearQueue();
            queueDialog.dismiss();
            Toast.makeText(activity, "Queue cleared", Toast.LENGTH_SHORT).show();
        });

        queueDialog.show();
    }

    private void setupDrawerItem(int viewId, TextView chip, String category) {
        View item = activity.findViewById(viewId);
        if (item != null) {
            item.setOnClickListener(v -> {
                if (drawerMusic != null) drawerMusic.closeDrawers();
                selectCategory(chip, category);
            });
        }
    }

    private void showSleepTimerDialog() {
        String[] options = new String[]{"Turn off timer", "15 minutes", "30 minutes", "45 minutes", "60 minutes"};
        new AlertDialog.Builder(activity)
                .setTitle("Sleep Timer")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        MusicPlayerManager.getInstance().setSleepTimerMinutes(0);
                        Toast.makeText(activity, "Sleep timer disabled", Toast.LENGTH_SHORT).show();
                    } else {
                        int mins = which * 15;
                        MusicPlayerManager.getInstance().setSleepTimerMinutes(mins);
                        Toast.makeText(activity, "Sleep timer set for " + mins + " minutes", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public boolean onBackPressed() {
        if (drawerMusic != null && drawerMusic.isDrawerOpen(GravityCompat.START)) {
            drawerMusic.closeDrawer(GravityCompat.START);
            return true;
        }
        if (playerSheetDialog != null && playerSheetDialog.isShowing()) {
            playerSheetDialog.dismiss();
            return true;
        }
        if (layoutSearchBox != null && layoutSearchBox.getVisibility() == View.VISIBLE) {
            layoutSearchBox.setVisibility(View.GONE);
            if (rvSuggestions != null) rvSuggestions.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    // PlayerListener callbacks
    @Override
    public void onTrackChanged(MusicTrack track) {
        if (track == null) {
            if (cardMiniPlayer != null) cardMiniPlayer.setVisibility(View.GONE);
            if (playerSheetDialog != null && playerSheetDialog.isShowing()) {
                playerSheetDialog.dismiss();
            }
            return;
        }
        if (cardMiniPlayer != null) cardMiniPlayer.setVisibility(View.VISIBLE);
        if (tvMiniTitle != null) tvMiniTitle.setText(track.getTitle());
        if (tvMiniArtist != null) tvMiniArtist.setText(track.getArtist());
        if (ivMiniThumb != null) {
            Glide.with(activity).load(track.getThumbnailUrl()).into(ivMiniThumb);
        }

        if (pbMiniProgress != null) pbMiniProgress.setProgress(0);
        if (sbPlayerProgress != null) {
            sbPlayerProgress.setProgress(0);
            sbPlayerProgress.setMax(100);
        }
        if (tvPlayerCurrentTime != null) tvPlayerCurrentTime.setText("0:00");
        if (tvPlayerTotalTime != null) tvPlayerTotalTime.setText("0:00");

        if (playerSheetDialog != null && playerSheetDialog.isShowing()) {
            updateFullPlayerUI();
        }
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying, boolean isBuffering) {
        if (btnMiniPlayPause != null) {
            btnMiniPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        }
        if (fabPlayerPlayPause != null) {
            fabPlayerPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        }
    }

    public static int parseDurationToSeconds(String durStr) {
        if (durStr == null || durStr.trim().isEmpty()) return 0;
        try {
            String[] parts = durStr.trim().split(":");
            if (parts.length == 2) {
                return Integer.parseInt(parts[0].trim()) * 60 + Integer.parseInt(parts[1].trim());
            } else if (parts.length == 3) {
                return Integer.parseInt(parts[0].trim()) * 3600 + Integer.parseInt(parts[1].trim()) * 60 + Integer.parseInt(parts[2].trim());
            }
        } catch (Exception ignored) {}
        return 0;
    }

    @Override
    public void onProgressUpdate(int currentSeconds, int totalSeconds) {
        if (totalSeconds <= 0) {
            MusicTrack current = MusicPlayerManager.getInstance().getCurrentTrack();
            if (current != null) {
                totalSeconds = parseDurationToSeconds(current.getDuration());
            }
        }

        if (totalSeconds > 0) {
            int progressPercent = (int) (((float) currentSeconds / totalSeconds) * 100);
            if (pbMiniProgress != null) pbMiniProgress.setProgress(progressPercent);

            if (!isUserSeeking && sbPlayerProgress != null) {
                sbPlayerProgress.setMax(totalSeconds);
                sbPlayerProgress.setProgress(currentSeconds);
                tvPlayerCurrentTime.setText(formatTime(currentSeconds));
                tvPlayerTotalTime.setText(formatTime(totalSeconds));
            }
        } else {
            if (tvPlayerCurrentTime != null && !isUserSeeking) {
                tvPlayerCurrentTime.setText(formatTime(currentSeconds));
            }
        }
    }

    @Override
    public void onError(String error) {
        Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVideoModeChanged(boolean isVideoMode) {
        activity.runOnUiThread(() -> {
            if (playerSheetDialog != null && playerSheetDialog.isShowing()) {
                updateVideoModeUI(isVideoMode);
            }
        });
    }

    @Override
    public void onQueueChanged(List<MusicTrack> queue, int currentIndex) {
        // Queue updated
    }

    private String formatTime(int totalSecs) {
        int m = totalSecs / 60;
        int s = totalSecs % 60;
        return String.format(Locale.getDefault(), "%d:%02d", m, s);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private final MusicDownloadManager.DownloadProgressListener downloadProgressListener = new MusicDownloadManager.DownloadProgressListener() {
        @Override
        public void onDownloadStarted(DownloadTask task) {
            activity.runOnUiThread(() -> {
                updateDownloadBadge();
                if ("Downloads".equalsIgnoreCase(currentCategory) && isViewingDownloadingSubtab) {
                    refreshDownloadingList();
                }
            });
        }

        @Override
        public void onProgress(DownloadTask task) {
            activity.runOnUiThread(() -> {
                if ("Downloads".equalsIgnoreCase(currentCategory) && isViewingDownloadingSubtab) {
                    activeDownloadAdapter.updateTask(task);
                }
            });
        }

        @Override
        public void onCompleted(DownloadTask task) {
            activity.runOnUiThread(() -> {
                updateDownloadBadge();
                if ("Downloads".equalsIgnoreCase(currentCategory)) {
                    if (isViewingDownloadingSubtab) {
                        refreshDownloadingList();
                    } else {
                        loadCompletedDownloads();
                    }
                }
            });
        }

        @Override
        public void onError(DownloadTask task, String error) {
            activity.runOnUiThread(() -> {
                updateDownloadBadge();
                if ("Downloads".equalsIgnoreCase(currentCategory) && isViewingDownloadingSubtab) {
                    refreshDownloadingList();
                }
            });
        }

        @Override
        public void onCancelled(DownloadTask task) {
            activity.runOnUiThread(() -> {
                updateDownloadBadge();
                if ("Downloads".equalsIgnoreCase(currentCategory) && isViewingDownloadingSubtab) {
                    refreshDownloadingList();
                }
            });
        }
    };

    public void onResume() {
        MusicPlayerManager.getInstance().registerListener(this);
        MusicDownloadManager.getInstance().addListener(downloadProgressListener);
        updateDownloadBadge();

        // If a track is already playing, ensure mini player displays it
        MusicTrack current = MusicPlayerManager.getInstance().getCurrentTrack();
        if (current != null) {
            onTrackChanged(current);
            onPlaybackStateChanged(MusicPlayerManager.getInstance().isPlaying(), false);
        }
    }

    public void onPause() {
        MusicDownloadManager.getInstance().removeListener(downloadProgressListener);
        MusicPlayerManager.getInstance().unregisterListener(this);
    }

    public void onDestroy() {
        if (searchDebounceRunnable != null) {
            searchDebounceHandler.removeCallbacks(searchDebounceRunnable);
        }
        if (playerSheetDialog != null && playerSheetDialog.isShowing()) {
            playerSheetDialog.dismiss();
        }
        if (activityVideoWebView != null) {
            try {
                activityVideoWebView.destroy();
            } catch (Exception ignored) {}
            activityVideoWebView = null;
        }
    }
}
