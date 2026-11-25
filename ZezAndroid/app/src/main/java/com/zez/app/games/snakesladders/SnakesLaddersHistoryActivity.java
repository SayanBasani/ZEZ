package com.zez.app.games.snakesladders;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.app.R;
import com.zez.app.theme.ThemeManager;
import com.zez.app.util.ChatPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SnakesLaddersHistoryActivity extends AppCompatActivity {

    private TextView tvTotalGames;
    private TextView tvTotalWins;
    private TextView tvWinRate;
    private TextView tvPlayTime;
    private TextView tvEmpty;
    private RecyclerView recycler;
    private final List<SnakesLaddersModels.HistoryMatch> items = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snakes_ladders_history);

        tvTotalGames = findViewById(R.id.tv_stat_total_games);
        tvTotalWins = findViewById(R.id.tv_stat_total_wins);
        tvWinRate = findViewById(R.id.tv_stat_win_rate);
        tvPlayTime = findViewById(R.id.tv_stat_play_time);
        tvEmpty = findViewById(R.id.tv_sl_history_empty);
        recycler = findViewById(R.id.recycler_sl_history);
        ImageButton btnBack = findViewById(R.id.btn_sl_history_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(items);
        recycler.setAdapter(adapter);

        String userId = ChatPreferencesHelper.getUserId(this);
        if (userId == null) {
            tvEmpty.setText("Sign in to chat to view game stats");
            return;
        }

        SnakesLaddersApiClient.getHistory(this, userId, new SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.HistoryResponse>() {
            @Override
            public void onSuccess(SnakesLaddersModels.HistoryResponse data) {
                if (isFinishing() || isDestroyed() || data == null) return;
                tvTotalGames.setText(String.valueOf(data.totalGames));
                tvTotalWins.setText(String.valueOf(data.totalWins));
                tvWinRate.setText(data.winRatePercent + "%");
                tvPlayTime.setText(formatDuration(data.totalPlaySeconds));

                items.clear();
                items.addAll(data.matches);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                if (items.isEmpty()) {
                    tvEmpty.setText("No finished Snakes & Ladders matches yet.");
                }
            }

            @Override
            public void onError(String message, int httpCode, String code) {
                if (isFinishing() || isDestroyed()) return;
                tvEmpty.setText("Could not load history: " + message);
            }
        });
    }

    private static String formatDuration(long secs) {
        if (secs < 60) return secs + "s";
        long mins = secs / 60;
        if (mins < 60) return mins + "m";
        return (mins / 60) + "h " + (mins % 60) + "m";
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        private final List<SnakesLaddersModels.HistoryMatch> data;

        HistoryAdapter(List<SnakesLaddersModels.HistoryMatch> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sl_history_match, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            SnakesLaddersModels.HistoryMatch m = data.get(position);
            h.tvOpponent.setText("vs " + m.opponentName);
            h.tvPositions.setText("Final: You Pos " + m.myFinalPosition + " · " + m.opponentName + " Pos " + m.opponentFinalPosition);
            String dateLabel = formatIsoDate(m.finishedAt);
            h.tvDateDur.setText(dateLabel + " · " + formatDuration(m.durationSeconds));
            if (m.didWin) {
                h.tvBadge.setText("WON");
                h.tvBadge.getBackground().setTint(Color.parseColor("#10B981"));
            } else {
                h.tvBadge.setText("LOST");
                h.tvBadge.getBackground().setTint(Color.parseColor("#EF4444"));
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private String formatIsoDate(String iso) {
            if (iso == null || iso.isEmpty()) return "Recent";
            try {
                Date d = Date.from(java.time.Instant.parse(iso));
                return new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(d);
            } catch (Exception e) {
                return iso.length() > 10 ? iso.substring(0, 10) : iso;
            }
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvOpponent, tvPositions, tvDateDur, tvBadge;
            VH(@NonNull View itemView) {
                super(itemView);
                tvOpponent = itemView.findViewById(R.id.tv_match_opponent);
                tvPositions = itemView.findViewById(R.id.tv_match_positions);
                tvDateDur = itemView.findViewById(R.id.tv_match_date_dur);
                tvBadge = itemView.findViewById(R.id.tv_match_result_badge);
            }
        }
    }
}
