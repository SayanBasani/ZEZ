package com.zez.app.music.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.app.music.model.MusicTrack;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class YouTubeApiHelper {
    private static final String TAG = "YouTubeApiHelper";
    private static final String SEARCH_URL = "https://www.youtube.com/youtubei/v1/search?prettyPrint=false";
    private static final String SUGGEST_URL = "https://suggestqueries-clients6.youtube.com/complete/search?client=youtube&ds=yt&q=";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static YouTubeApiHelper instance;
    private final OkHttpClient httpClient;
    private final Handler mainHandler;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private YouTubeApiHelper() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .dns(new Dns() {
                    @Override
                    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                        try {
                            return Dns.SYSTEM.lookup(hostname);
                        } catch (Exception e) {
                            Log.w(TAG, "System DNS failed for " + hostname + ", using fallback resolver");
                            List<InetAddress> fallback = resolveFallback(hostname);
                            if (fallback != null && !fallback.isEmpty()) {
                                return fallback;
                            }
                            throw new UnknownHostException("Could not resolve " + hostname);
                        }
                    }
                })
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    private static List<InetAddress> resolveFallback(String hostname) {
        List<InetAddress> list = new ArrayList<>();
        try {
            if ("www.youtube.com".equalsIgnoreCase(hostname)) {
                for (String ip : Arrays.asList("142.251.154.4", "142.251.150.4", "142.251.157.4", "142.251.155.4")) {
                    list.add(InetAddress.getByAddress(hostname, InetAddress.getByName(ip).getAddress()));
                }
                return list;
            } else if ("suggestqueries-clients6.youtube.com".equalsIgnoreCase(hostname)) {
                for (String ip : Arrays.asList("142.250.193.142", "142.251.150.4")) {
                    list.add(InetAddress.getByAddress(hostname, InetAddress.getByName(ip).getAddress()));
                }
                return list;
            } else if ("i.ytimg.com".equalsIgnoreCase(hostname)) {
                for (String ip : Arrays.asList("142.251.148.6", "142.251.146.6", "192.178.41.6")) {
                    list.add(InetAddress.getByAddress(hostname, InetAddress.getByName(ip).getAddress()));
                }
                return list;
            } else if ("m.youtube.com".equalsIgnoreCase(hostname)) {
                for (String ip : Arrays.asList("142.250.122.102", "142.250.122.100", "142.250.122.138")) {
                    list.add(InetAddress.getByAddress(hostname, InetAddress.getByName(ip).getAddress()));
                }
                return list;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static synchronized YouTubeApiHelper getInstance() {
        if (instance == null) {
            instance = new YouTubeApiHelper();
        }
        return instance;
    }

    public void search(String query, Callback<List<MusicTrack>> callback) {
        new Thread(() -> {
            try {
                JsonObject clientObj = new JsonObject();
                clientObj.addProperty("clientName", "WEB");
                clientObj.addProperty("clientVersion", "2.20240101.01.00");
                clientObj.addProperty("hl", "en");
                clientObj.addProperty("gl", "US");

                JsonObject contextObj = new JsonObject();
                contextObj.add("client", clientObj);

                JsonObject bodyObj = new JsonObject();
                bodyObj.add("context", contextObj);
                bodyObj.addProperty("query", query);

                RequestBody body = RequestBody.create(bodyObj.toString(), JSON_MEDIA_TYPE);
                Request request = new Request.Builder()
                        .url(SEARCH_URL)
                        .post(body)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Origin", "https://www.youtube.com")
                        .header("Referer", "https://www.youtube.com/")
                        .build();

                Log.d(TAG, "Executing YouTube search request for: " + query);
                Response response = httpClient.newCall(request).execute();
                Log.d(TAG, "Search response code: " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    Log.e(TAG, "Search failed with HTTP " + response.code() + ": " + errBody);
                    mainHandler.post(() -> callback.onError(new IOException("Unexpected HTTP code " + response.code())));
                    return;
                }

                String responseStr = response.body().string();
                List<MusicTrack> tracks = parseSearchResults(responseStr);
                Log.d(TAG, "Parsed " + tracks.size() + " tracks for query: " + query);
                mainHandler.post(() -> callback.onSuccess(tracks));
            } catch (Throwable e) {
                Log.e(TAG, "Search error for query: " + query + " -> " + e.getClass().getName() + ": " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError(new Exception(e.getMessage())));
            }
        }).start();
    }

    public void getTrending(String category, Callback<List<MusicTrack>> callback) {
        String searchQuery = "Trending Music";
        if (category != null && !category.isEmpty()) {
            if ("Top tracks".equalsIgnoreCase(category)) {
                searchQuery = "Top Global Music Songs";
            } else if ("Recommended".equalsIgnoreCase(category)) {
                searchQuery = "Popular Music Hits";
            } else if ("Charts".equalsIgnoreCase(category)) {
                searchQuery = "Top 50 Songs Billboard Charts";
            } else {
                searchQuery = category + " Music";
            }
        }
        search(searchQuery, callback);
    }

    public void getSuggestions(String query, Callback<List<String>> callback) {
        if (query == null || query.trim().isEmpty()) {
            mainHandler.post(() -> callback.onSuccess(new ArrayList<>()));
            return;
        }

        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(query.trim(), "UTF-8");
                Request request = new Request.Builder()
                        .url(SUGGEST_URL + encoded)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build();

                Response response = httpClient.newCall(request).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    mainHandler.post(() -> callback.onSuccess(new ArrayList<>()));
                    return;
                }

                String body = response.body().string();
                List<String> suggestions = parseSuggestions(body);
                mainHandler.post(() -> callback.onSuccess(suggestions));
            } catch (Exception e) {
                Log.e(TAG, "Suggest error", e);
                mainHandler.post(() -> callback.onSuccess(new ArrayList<>()));
            }
        }).start();
    }

    private List<MusicTrack> parseSearchResults(String jsonStr) {
        List<MusicTrack> results = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(jsonStr).getAsJsonObject();
            JsonObject contents = root.getAsJsonObject("contents");
            if (contents == null) return results;

            JsonObject twoCol = contents.getAsJsonObject("twoColumnSearchResultsRenderer");
            if (twoCol == null) return results;

            JsonObject primary = twoCol.getAsJsonObject("primaryContents");
            if (primary == null) return results;

            JsonObject sectionList = primary.getAsJsonObject("sectionListRenderer");
            if (sectionList == null) return results;

            JsonArray sectionContents = sectionList.getAsJsonArray("contents");
            if (sectionContents == null) return results;

            for (JsonElement secEl : sectionContents) {
                if (!secEl.isJsonObject()) continue;
                JsonObject secObj = secEl.getAsJsonObject();
                JsonObject itemSec = secObj.getAsJsonObject("itemSectionRenderer");
                if (itemSec == null) continue;

                JsonArray itemContents = itemSec.getAsJsonArray("contents");
                if (itemContents == null) continue;

                for (JsonElement itemEl : itemContents) {
                    if (!itemEl.isJsonObject()) continue;
                    JsonObject itemObj = itemEl.getAsJsonObject();
                    JsonObject v = itemObj.getAsJsonObject("videoRenderer");
                    if (v == null) continue;

                    String videoId = v.has("videoId") ? v.get("videoId").getAsString() : null;
                    if (videoId == null || videoId.isEmpty()) continue;

                    // Extract title
                    String title = "";
                    if (v.has("title")) {
                        JsonObject titleObj = v.getAsJsonObject("title");
                        if (titleObj.has("runs")) {
                            JsonArray runs = titleObj.getAsJsonArray("runs");
                            if (runs.size() > 0) {
                                title = runs.get(0).getAsJsonObject().get("text").getAsString();
                            }
                        } else if (titleObj.has("simpleText")) {
                            title = titleObj.get("simpleText").getAsString();
                        }
                    }

                    // Extract artist
                    String artist = "YouTube Artist";
                    if (v.has("ownerText")) {
                        JsonObject ownerObj = v.getAsJsonObject("ownerText");
                        if (ownerObj.has("runs")) {
                            JsonArray runs = ownerObj.getAsJsonArray("runs");
                            if (runs.size() > 0) {
                                artist = runs.get(0).getAsJsonObject().get("text").getAsString();
                            }
                        }
                    } else if (v.has("shortBylineText")) {
                        JsonObject shortObj = v.getAsJsonObject("shortBylineText");
                        if (shortObj.has("runs")) {
                            JsonArray runs = shortObj.getAsJsonArray("runs");
                            if (runs.size() > 0) {
                                artist = runs.get(0).getAsJsonObject().get("text").getAsString();
                            }
                        }
                    }

                    // Extract duration
                    String duration = "--:--";
                    if (v.has("lengthText")) {
                        duration = v.getAsJsonObject("lengthText").get("simpleText").getAsString();
                    }

                    // Extract view count
                    String views = "";
                    if (v.has("viewCountText")) {
                        JsonObject viewObj = v.getAsJsonObject("viewCountText");
                        if (viewObj.has("simpleText")) {
                            views = viewObj.get("simpleText").getAsString();
                        } else if (viewObj.has("runs")) {
                            StringBuilder sb = new StringBuilder();
                            for (JsonElement r : viewObj.getAsJsonArray("runs")) {
                                sb.append(r.getAsJsonObject().get("text").getAsString());
                            }
                            views = sb.toString();
                        }
                    }

                    // Extract best thumbnail
                    String thumbUrl = "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg";
                    if (v.has("thumbnail")) {
                        JsonObject thumbObj = v.getAsJsonObject("thumbnail");
                        if (thumbObj.has("thumbnails")) {
                            JsonArray thumbs = thumbObj.getAsJsonArray("thumbnails");
                            if (thumbs.size() > 0) {
                                thumbUrl = thumbs.get(thumbs.size() - 1).getAsJsonObject().get("url").getAsString();
                            }
                        }
                    }

                    results.add(new MusicTrack(videoId, title, artist, duration, thumbUrl, views));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing search results", e);
        }
        return results;
    }

    private List<String> parseSuggestions(String body) {
        List<String> list = new ArrayList<>();
        try {
            int start = body.indexOf('(');
            int end = body.lastIndexOf(')');
            if (start != -1 && end != -1 && end > start) {
                String jsonStr = body.substring(start + 1, end);
                JsonArray rootArr = JsonParser.parseString(jsonStr).getAsJsonArray();
                if (rootArr.size() > 1 && rootArr.get(1).isJsonArray()) {
                    JsonArray suggArr = rootArr.get(1).getAsJsonArray();
                    for (JsonElement el : suggArr) {
                        if (el.isJsonArray()) {
                            JsonArray item = el.getAsJsonArray();
                            if (item.size() > 0) {
                                list.add(item.get(0).getAsString());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing suggestions", e);
        }
        return list;
    }
}
