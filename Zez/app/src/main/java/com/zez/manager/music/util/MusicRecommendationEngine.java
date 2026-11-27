package com.zez.manager.music.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zez.manager.music.model.MusicTrack;
import com.zez.manager.music.player.MusicPlayerManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight on-device personalization, search relevance ranking, and duplicate-version filtering.
 * Stores only compact music taste weights locally (no sensitive user tracking).
 */
public class MusicRecommendationEngine {
    private static final String PREF_NAME = "zez_music_recs";
    private static final String KEY_ARTIST_SCORES = "artist_scores";
    private static final String KEY_RECENT_IDS = "recent_home_ids";
    private static final String KEY_REFRESH_CURSOR = "refresh_cursor";
    private static final Gson gson = new Gson();

    private static final List<String> DISCOVERY_BUCKETS = Arrays.asList(
            "Top Global Hits Official Audio",
            "Chill Acoustic & Indie Pop Mix",
            "Bollywood & Arijit Singh Romantic Hits",
            "Electronic Dance & Synthwave Hits",
            "Classic Rock & Pop Essentials",
            "Lo-Fi Beats & Focus Instrumental",
            "Hip Hop & R&B Top Tracks",
            "Late Night Soul & Jazz Vibes"
    );

    public static String normalizeTitleKey(String rawTitle) {
        if (rawTitle == null) return "";
        String s = rawTitle.toLowerCase(Locale.US);
        s = s.replaceAll("\\([^)]*\\)", " ");
        s = s.replaceAll("\\[[^\\]]*\\]", " ");
        s = s.replaceAll("(?i)\\b(official|music|video|audio|lyrics?|lyric|hd|4k|8d|slowed|reverb|cover|remix|live|version|full\\s+song|ft\\.?|feat\\.?)\\b", " ");
        s = s.replaceAll("[^a-z0-9\\s]", " ");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    public static String normalizeArtistKey(String rawArtist) {
        if (rawArtist == null) return "";
        return rawArtist.toLowerCase(Locale.US)
                .replaceAll("(?i)\\s*-\\s*topic$", "")
                .replaceAll("(?i)\\b(official|vevo|music|records)\\b", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static void recordTrackPlayed(Context context, MusicTrack track, boolean completed) {
        if (context == null || track == null) return;
        String artist = normalizeArtistKey(track.getArtist());
        if (artist.isEmpty() || "youtube artist".equals(artist) || "unknown artist".equals(artist)) {
            return;
        }
        Map<String, Float> scores = loadArtistScores(context);
        float curr = scores.containsKey(artist) ? scores.get(artist) : 0f;
        float delta = completed ? 2.5f : 1.0f;
        scores.put(artist, Math.min(50f, curr + delta));
        trimAndSaveScores(context, scores);
    }

    public static void recordTrackSkipped(Context context, MusicTrack track) {
        if (context == null || track == null) return;
        String artist = normalizeArtistKey(track.getArtist());
        if (artist.isEmpty()) return;
        Map<String, Float> scores = loadArtistScores(context);
        float curr = scores.containsKey(artist) ? scores.get(artist) : 0f;
        scores.put(artist, Math.max(-5f, curr - 0.8f));
        trimAndSaveScores(context, scores);
    }

    public static List<MusicTrack> rankSearchResults(String query, List<MusicTrack> raw) {
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        final String qNorm = normalizeTitleKey(query);
        final String qRaw = query != null ? query.toLowerCase(Locale.US).trim() : "";

        List<ScoredTrack> scored = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (int i = 0; i < raw.size(); i++) {
            MusicTrack t = raw.get(i);
            if (t == null || t.getId() == null || !seenIds.add(t.getId())) continue;

            String titleLow = t.getTitle().toLowerCase(Locale.US);
            String titleNorm = normalizeTitleKey(t.getTitle());
            String artistLow = t.getArtist().toLowerCase(Locale.US);

            double score = Math.max(0, 30 - i); // Preserve YouTube base relevance as tiebreaker
            if (!qNorm.isEmpty() && titleNorm.equals(qNorm)) {
                score += 60;
            } else if (!qNorm.isEmpty() && titleNorm.contains(qNorm)) {
                score += 35;
            } else if (!qRaw.isEmpty() && titleLow.contains(qRaw)) {
                score += 25;
            }
            if (!qRaw.isEmpty() && artistLow.contains(qRaw)) {
                score += 20;
            }

            // Official / trusted signal boost
            if (titleLow.contains("official audio") || titleLow.contains("official video")
                    || titleLow.contains("official music video") || artistLow.contains(" - topic")
                    || artistLow.contains("vevo")) {
                score += 22;
            }

            // Mild penalty for unsolicited covers/slowed/status clips unless searched for
            if (!qRaw.contains("cover") && titleLow.contains("cover")) score -= 18;
            if (!qRaw.contains("slowed") && (titleLow.contains("slowed") || titleLow.contains("reverb"))) score -= 14;
            if (!qRaw.contains("status") && (titleLow.contains("status") || titleLow.contains("shorts"))) score -= 25;

            int durSec = MusicPlayerManager.parseDurationToSeconds(t.getDuration());
            if (durSec > 0 && durSec < 45 && !qRaw.contains("short")) {
                score -= 22;
            } else if (durSec >= 90 && durSec <= 480) {
                score += 8;
            }

            scored.add(new ScoredTrack(t, score));
        }

        Collections.sort(scored, (a, b) -> Double.compare(b.score, a.score));
        List<MusicTrack> out = new ArrayList<>(scored.size());
        for (ScoredTrack st : scored) out.add(st.track);
        return out;
    }

    /**
     * Filters duplicate song titles so home feeds & queues don't replay alternate covers of the same track.
     */
    public static List<MusicTrack> deduplicateBySongTitle(List<MusicTrack> tracks) {
        if (tracks == null) return new ArrayList<>();
        List<MusicTrack> out = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        Set<String> seenTitles = new HashSet<>();
        for (MusicTrack t : tracks) {
            if (t == null || t.getId() == null || !seenIds.add(t.getId())) continue;
            String key = normalizeTitleKey(t.getTitle());
            if (key.length() >= 4 && !seenTitles.add(key)) {
                continue;
            }
            out.add(t);
        }
        return out;
    }

    /**
     * Builds personalized + diverse queries for the Home / Recommended / Trending tabs, rotating on refresh.
     */
    public static List<String> buildHomeQueries(Context context, String category, boolean forceRotate) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int cursor = sp.getInt(KEY_REFRESH_CURSOR, 0);
        if (forceRotate) {
            cursor = (cursor + 1) % 1000;
            sp.edit().putInt(KEY_REFRESH_CURSOR, cursor).apply();
        }

        List<String> queries = new ArrayList<>();
        if ("Recommended".equalsIgnoreCase(category) || "Trending".equalsIgnoreCase(category)) {
            List<String> topArtists = getTopArtists(context, 4);
            List<String> searchHist = MusicHistoryManager.getSearchHistory(context);

            if (!topArtists.isEmpty()) {
                String primaryArtist = topArtists.get(cursor % topArtists.size());
                queries.add(primaryArtist + " best songs mix");
                if (topArtists.size() > 1) {
                    String secondArtist = topArtists.get((cursor + 1) % topArtists.size());
                    queries.add(secondArtist + " similar artists music");
                }
            } else if (!searchHist.isEmpty()) {
                queries.add(searchHist.get(cursor % searchHist.size()) + " mix");
            }

            // Always blend fresh discovery diversity bucket
            String bucket1 = DISCOVERY_BUCKETS.get(cursor % DISCOVERY_BUCKETS.size());
            String bucket2 = DISCOVERY_BUCKETS.get((cursor + 3) % DISCOVERY_BUCKETS.size());
            queries.add(bucket1);
            if (queries.size() < 3) {
                queries.add(bucket2);
            }
        } else if ("Viral 50".equalsIgnoreCase(category)) {
            queries.add(cursor % 2 == 0 ? "Viral 50 Global Hits Official" : "Trending Viral Songs Right Now");
        } else if ("Top tracks".equalsIgnoreCase(category) || "Charts".equalsIgnoreCase(category)) {
            queries.add(cursor % 2 == 0 ? "Top 50 Global Music Charts Official" : "Most Played Songs Worldwide Official Audio");
        } else {
            queries.add(DISCOVERY_BUCKETS.get(cursor % DISCOVERY_BUCKETS.size()));
        }
        return queries;
    }

    /**
     * Interleaves query results for diversity, prefers unseen tracks on refresh, and avoids immediate repeats.
     */
    public static List<MusicTrack> mergeDiverseFeed(Context context, List<List<MusicTrack>> buckets) {
        Set<String> prevIds = loadRecentHomeIds(context);
        List<MusicTrack> fresh = new ArrayList<>();
        List<MusicTrack> seenFallback = new ArrayList<>();
        Set<String> pickedIds = new HashSet<>();
        Set<String> pickedTitleKeys = new HashSet<>();

        int maxLen = 0;
        for (List<MusicTrack> b : buckets) {
            if (b != null && b.size() > maxLen) maxLen = b.size();
        }

        for (int i = 0; i < maxLen; i++) {
            for (List<MusicTrack> b : buckets) {
                if (b == null || i >= b.size()) continue;
                MusicTrack t = b.get(i);
                if (t == null || t.getId() == null || !pickedIds.add(t.getId())) continue;
                String tKey = normalizeTitleKey(t.getTitle());
                if (tKey.length() >= 4 && !pickedTitleKeys.add(tKey)) continue;
                if (prevIds.contains(t.getId())) {
                    seenFallback.add(t);
                } else {
                    fresh.add(t);
                }
            }
        }

        List<MusicTrack> result = new ArrayList<>(fresh);
        if (result.size() < 12) {
            result.addAll(seenFallback);
        }

        // Save first 30 shown IDs to rotate next refresh
        Set<String> nextPrev = new LinkedHashSet<>();
        for (int i = 0; i < Math.min(30, result.size()); i++) {
            nextPrev.add(result.get(i).getId());
        }
        saveRecentHomeIds(context, nextPrev);
        return result;
    }

    private static List<String> getTopArtists(Context context, int limit) {
        Map<String, Float> scores = loadArtistScores(context);
        List<Map.Entry<String, Float>> entries = new ArrayList<>(scores.entrySet());
        Collections.sort(entries, (a, b) -> Float.compare(b.getValue(), a.getValue()));
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, Float> e : entries) {
            if (e.getValue() > 0.5f) {
                out.add(e.getKey());
                if (out.size() >= limit) break;
            }
        }
        return out;
    }

    private static Map<String, Float> loadArtistScores(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_ARTIST_SCORES, null);
        if (json == null || json.isEmpty()) return new HashMap<>();
        try {
            Type type = new TypeToken<Map<String, Float>>() {}.getType();
            Map<String, Float> map = gson.fromJson(json, type);
            return map != null ? map : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static void trimAndSaveScores(Context context, Map<String, Float> scores) {
        if (scores.size() > 60) {
            List<Map.Entry<String, Float>> list = new ArrayList<>(scores.entrySet());
            Collections.sort(list, (a, b) -> Float.compare(b.getValue(), a.getValue()));
            Map<String, Float> trimmed = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(40, list.size()); i++) {
                trimmed.put(list.get(i).getKey(), list.get(i).getValue());
            }
            scores = trimmed;
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_ARTIST_SCORES, gson.toJson(scores)).apply();
    }

    private static Set<String> loadRecentHomeIds(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_RECENT_IDS, null);
        if (json == null || json.isEmpty()) return new HashSet<>();
        try {
            Type type = new TypeToken<Set<String>>() {}.getType();
            Set<String> s = gson.fromJson(json, type);
            return s != null ? s : new HashSet<>();
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private static void saveRecentHomeIds(Context context, Set<String> ids) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_RECENT_IDS, gson.toJson(ids)).apply();
    }

    private static class ScoredTrack {
        final MusicTrack track;
        final double score;
        ScoredTrack(MusicTrack track, double score) {
            this.track = track;
            this.score = score;
        }
    }
}
