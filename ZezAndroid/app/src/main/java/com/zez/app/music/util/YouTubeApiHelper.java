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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.HttpUrl;
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

    private static class CachedStream {
        final String fastStreamUrl;
        final String fallbackStreamUrl;
        final long timestamp;
        CachedStream(String fastStreamUrl, String fallbackStreamUrl) {
            this.fastStreamUrl = fastStreamUrl;
            this.fallbackStreamUrl = fallbackStreamUrl;
            this.timestamp = System.currentTimeMillis();
        }
        boolean isValid() {
            return (System.currentTimeMillis() - timestamp) < (4 * 3600 * 1000L); // 4 hours
        }
    }
    private final Map<String, CachedStream> streamCache = new ConcurrentHashMap<>();

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private YouTubeApiHelper() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    private final Map<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        if (cookies != null && !cookies.isEmpty()) {
                            cookieStore.put(url.host(), cookies);
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
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
                        JsonObject lenObj = v.getAsJsonObject("lengthText");
                        if (lenObj.has("simpleText")) {
                            duration = lenObj.get("simpleText").getAsString();
                        } else if (lenObj.has("runs")) {
                            JsonArray runs = lenObj.getAsJsonArray("runs");
                            if (runs.size() > 0 && runs.get(0).getAsJsonObject().has("text")) {
                                duration = runs.get(0).getAsJsonObject().get("text").getAsString();
                            }
                        }
                    }
                    if ("--:--".equals(duration) && v.has("thumbnailOverlays")) {
                        try {
                            JsonArray overlays = v.getAsJsonArray("thumbnailOverlays");
                            for (JsonElement o : overlays) {
                                if (!o.isJsonObject()) continue;
                                JsonObject ro = o.getAsJsonObject();
                                if (ro.has("thumbnailOverlayTimeStatusRenderer")) {
                                    JsonObject timeObj = ro.getAsJsonObject("thumbnailOverlayTimeStatusRenderer");
                                    if (timeObj != null && timeObj.has("text")) {
                                        JsonObject textObj = timeObj.getAsJsonObject("text");
                                        if (textObj.has("simpleText")) {
                                            duration = textObj.get("simpleText").getAsString();
                                            break;
                                        } else if (textObj.has("runs")) {
                                            JsonArray r = textObj.getAsJsonArray("runs");
                                            if (r.size() > 0 && r.get(0).getAsJsonObject().has("text")) {
                                                duration = r.get(0).getAsJsonObject().get("text").getAsString();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
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

    public void extractAudioStream(String videoId, Callback<String> callback) {
        if (videoId == null || videoId.isEmpty()) {
            mainHandler.post(() -> callback.onError(new IllegalArgumentException("Video ID is empty")));
            return;
        }

        CachedStream cached = streamCache.get(videoId);
        if (cached != null && cached.isValid() && cached.fastStreamUrl != null) {
            mainHandler.post(() -> callback.onSuccess(cached.fastStreamUrl));
            return;
        }

        new Thread(() -> {
            String url = getStreamUrlSync(videoId, true);
            if (url != null && !url.isEmpty()) {
                mainHandler.post(() -> callback.onSuccess(url));
            } else {
                mainHandler.post(() -> callback.onError(new IOException("Could not resolve playable audio stream for " + videoId)));
            }
        }).start();
    }

    private String cachedVisitorData = null;
    private int cachedSts = 20775;

    private synchronized String ensureVisitorData() {
        if (cachedVisitorData != null && !cachedVisitorData.isEmpty()) {
            return cachedVisitorData;
        }
        // 1. Fetch official VISITOR_DATA & STS from YouTube homepage (same reliable method as yt-dlp)
        try {
            Request req = new Request.Builder()
                    .url("https://www.youtube.com/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    String html = resp.body().string();
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("ytcfg\\.set\\((\\{.+?\\})\\);").matcher(html);
                    if (m.find()) {
                        JsonObject ytcfg = JsonParser.parseString(m.group(1)).getAsJsonObject();
                        if (ytcfg.has("VISITOR_DATA")) {
                            cachedVisitorData = ytcfg.get("VISITOR_DATA").getAsString();
                            Log.i(TAG, "Acquired official VISITOR_DATA length=" + cachedVisitorData.length());
                        }
                        if (ytcfg.has("STS")) {
                            cachedSts = ytcfg.get("STS").getAsInt();
                            Log.i(TAG, "Acquired official STS=" + cachedSts);
                        }
                        if (cachedVisitorData != null && !cachedVisitorData.isEmpty()) {
                            return cachedVisitorData;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not acquire VISITOR_DATA from homepage: " + e.getMessage());
        }

        // 2. Fallback: visitor_id endpoint
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

            Request req = new Request.Builder()
                    .url("https://www.youtube.com/youtubei/v1/visitor_id?prettyPrint=false")
                    .post(RequestBody.create(bodyObj.toString(), JSON_MEDIA_TYPE))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Origin", "https://www.youtube.com")
                    .header("Referer", "https://www.youtube.com/")
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    JsonObject resJson = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                    if (resJson.has("responseContext")) {
                        JsonObject ctx = resJson.getAsJsonObject("responseContext");
                        if (ctx.has("visitorData")) {
                            cachedVisitorData = ctx.get("visitorData").getAsString();
                            Log.i(TAG, "Acquired visitorData from visitor_id endpoint: " + cachedVisitorData);
                            return cachedVisitorData;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not acquire visitorData from visitor_id endpoint: " + e.getMessage());
        }
        return cachedVisitorData;
    }

    public String getStreamUrlSync(String videoId) {
        return getStreamUrlSync(videoId, true);
    }

    public String getFallbackStreamUrlSync(String videoId) {
        if (videoId == null || videoId.isEmpty()) return null;
        CachedStream cached = streamCache.get(videoId);
        if (cached != null && cached.isValid() && cached.fallbackStreamUrl != null) {
            return cached.fallbackStreamUrl;
        }
        return getStreamUrlSync(videoId, false);
    }

    public String getStreamUrlSync(String videoId, boolean preferFastLowBandwidth) {
        if (videoId == null || videoId.isEmpty()) return null;
        CachedStream cached = streamCache.get(videoId);
        if (cached != null && cached.isValid()) {
            if (preferFastLowBandwidth && cached.fastStreamUrl != null) {
                return cached.fastStreamUrl;
            }
            if (!preferFastLowBandwidth && cached.fallbackStreamUrl != null) {
                return cached.fallbackStreamUrl;
            }
            if (cached.fastStreamUrl != null) return cached.fastStreamUrl;
            if (cached.fallbackStreamUrl != null) return cached.fallbackStreamUrl;
        }

        try {
            String visitorData = ensureVisitorData();

            // Query player API with VISIONOS client + visitorData
            JsonObject clientObj = new JsonObject();
            clientObj.addProperty("clientName", "VISIONOS");
            clientObj.addProperty("clientVersion", "1.02");
            clientObj.addProperty("deviceMake", "Apple");
            clientObj.addProperty("deviceModel", "RealityDevice17,1");
            clientObj.addProperty("userAgent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15");
            clientObj.addProperty("osName", "visionOS");
            clientObj.addProperty("osVersion", "26.5.23O471");
            clientObj.addProperty("hl", "en");
            clientObj.addProperty("timeZone", "UTC");
            clientObj.addProperty("utcOffsetMinutes", 0);
            if (visitorData != null && !visitorData.isEmpty()) {
                clientObj.addProperty("visitorData", visitorData);
            }

            JsonObject contextObj = new JsonObject();
            contextObj.add("client", clientObj);

            JsonObject playbackContext = new JsonObject();
            JsonObject contentPlaybackContext = new JsonObject();
            contentPlaybackContext.addProperty("html5Preference", "HTML5_PREF_WANTS");
            contentPlaybackContext.addProperty("signatureTimestamp", cachedSts > 0 ? cachedSts : 20775);
            playbackContext.add("contentPlaybackContext", contentPlaybackContext);

            JsonObject playerBody = new JsonObject();
            playerBody.add("context", contextObj);
            playerBody.addProperty("videoId", videoId);
            playerBody.add("playbackContext", playbackContext);
            playerBody.addProperty("contentCheckOk", true);
            playerBody.addProperty("racyCheckOk", true);

            Request.Builder playerReqBuilder = new Request.Builder()
                    .url("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
                    .post(RequestBody.create(playerBody.toString(), JSON_MEDIA_TYPE))
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15")
                    .header("X-YouTube-Client-Name", "101")
                    .header("X-YouTube-Client-Version", "1.02")
                    .header("Origin", "https://www.youtube.com");

            if (visitorData != null && !visitorData.isEmpty()) {
                playerReqBuilder.header("X-Goog-Visitor-Id", visitorData);
            }

            String fastStream = null;
            String fallbackStream = null;

            try (Response playerResp = httpClient.newCall(playerReqBuilder.build()).execute()) {
                if (playerResp.isSuccessful() && playerResp.body() != null) {
                    String resStr = playerResp.body().string();
                    try {
                        JsonObject root = JsonParser.parseString(resStr).getAsJsonObject();
                        if (root.has("responseContext")) {
                            JsonObject ctx = root.getAsJsonObject("responseContext");
                            if (ctx.has("visitorData")) {
                                cachedVisitorData = ctx.get("visitorData").getAsString();
                            }
                        }
                    } catch (Exception ignored) {}

                    String[] streams = parseAudioStreams(resStr);
                    fastStream = streams[0];
                    fallbackStream = streams[1];
                } else {
                    Log.e(TAG, "VISIONOS Player request failed with code: " + playerResp.code());
                }
            }

            // Fallback: if VISIONOS returned null, try ANDROID_VR client
            if (fastStream == null && fallbackStream == null) {
                Log.w(TAG, "VISIONOS returned no stream for " + videoId + ", trying ANDROID_VR fallback...");
                JsonObject vrClient = new JsonObject();
                vrClient.addProperty("clientName", "ANDROID_VR");
                vrClient.addProperty("clientVersion", "1.61.48");
                vrClient.addProperty("deviceMake", "Oculus");
                vrClient.addProperty("deviceModel", "Quest 3");
                vrClient.addProperty("osName", "Android");
                vrClient.addProperty("osVersion", "12");
                vrClient.addProperty("hl", "en");
                vrClient.addProperty("timeZone", "UTC");
                vrClient.addProperty("utcOffsetMinutes", 0);
                if (visitorData != null && !visitorData.isEmpty()) {
                    vrClient.addProperty("visitorData", visitorData);
                }
                JsonObject vrContext = new JsonObject();
                vrContext.add("client", vrClient);
                JsonObject vrBody = new JsonObject();
                vrBody.add("context", vrContext);
                vrBody.addProperty("videoId", videoId);
                vrBody.add("playbackContext", playbackContext);
                vrBody.addProperty("contentCheckOk", true);
                vrBody.addProperty("racyCheckOk", true);

                Request.Builder vrReqBuilder = new Request.Builder()
                        .url("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
                        .post(RequestBody.create(vrBody.toString(), JSON_MEDIA_TYPE))
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; Quest 3) AppleWebKit/537.36")
                        .header("X-YouTube-Client-Name", "28")
                        .header("X-YouTube-Client-Version", "1.61.48")
                        .header("Origin", "https://www.youtube.com");
                if (visitorData != null && !visitorData.isEmpty()) {
                    vrReqBuilder.header("X-Goog-Visitor-Id", visitorData);
                }
                try (Response vrResp = httpClient.newCall(vrReqBuilder.build()).execute()) {
                    if (vrResp.isSuccessful() && vrResp.body() != null) {
                        String[] vrStreams = parseAudioStreams(vrResp.body().string());
                        fastStream = vrStreams[0];
                        fallbackStream = vrStreams[1];
                    }
                } catch (Exception vre) {
                    Log.w(TAG, "ANDROID_VR fallback error: " + vre.getMessage());
                }
            }

            if (fastStream != null || fallbackStream != null) {
                Log.i(TAG, "Audio stream resolved successfully for " + videoId + " (fast=" + (fastStream != null) + ", fallback=" + (fallbackStream != null) + ")");
                streamCache.put(videoId, new CachedStream(fastStream != null ? fastStream : fallbackStream, fallbackStream != null ? fallbackStream : fastStream));
                return preferFastLowBandwidth ? (fastStream != null ? fastStream : fallbackStream) : (fallbackStream != null ? fallbackStream : fastStream);
            } else {
                Log.w(TAG, "Both VISIONOS and fallback returned null streams for " + videoId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resolving audio stream for " + videoId, e);
        }
        return null;
    }

    private String[] parseAudioStreams(String jsonStr) {
        String fastUrl = null;
        String fallbackUrl = null;
        try {
            JsonObject root = JsonParser.parseString(jsonStr).getAsJsonObject();
            JsonObject streamingData = root.getAsJsonObject("streamingData");
            if (streamingData == null) {
                return new String[]{null, null};
            }

            JsonArray formats = streamingData.getAsJsonArray("adaptiveFormats");
            if (formats != null) {
                String opus70 = null;
                String opus50 = null;
                String aac128 = null;
                String opus160 = null;
                String genericAudio = null;

                for (JsonElement el : formats) {
                    if (!el.isJsonObject()) continue;
                    JsonObject f = el.getAsJsonObject();
                    if (!f.has("url")) continue;
                    String u = f.get("url").getAsString();
                    int itag = f.has("itag") ? f.get("itag").getAsInt() : 0;
                    String mime = f.has("mimeType") ? f.get("mimeType").getAsString() : "";

                    if (itag == 250) opus70 = u;
                    else if (itag == 249) opus50 = u;
                    else if (itag == 140) aac128 = u;
                    else if (itag == 251) opus160 = u;
                    else if (mime.startsWith("audio/")) {
                        if (genericAudio == null) genericAudio = u;
                    }
                }

                // Fast low-bandwidth stream: Opus 70kbps or 50kbps (tiny 4KB header, minimal buffering, starts instantly with 70% less data!)
                if (opus70 != null) fastUrl = opus70;
                else if (opus50 != null) fastUrl = opus50;
                else if (aac128 != null) fastUrl = aac128;
                else if (opus160 != null) fastUrl = opus160;
                else fastUrl = genericAudio;

                // High-quality / universal fallback stream: AAC 128kbps or Opus 160kbps
                if (aac128 != null) fallbackUrl = aac128;
                else if (opus160 != null) fallbackUrl = opus160;
                else if (opus70 != null) fallbackUrl = opus70;
                else fallbackUrl = fastUrl;
            }

            // Fallback: combined formats
            if (fastUrl == null) {
                JsonArray combinedFormats = streamingData.getAsJsonArray("formats");
                if (combinedFormats != null) {
                    for (JsonElement el : combinedFormats) {
                        if (!el.isJsonObject()) continue;
                        JsonObject f = el.getAsJsonObject();
                        if (f.has("url")) {
                            fastUrl = f.get("url").getAsString();
                            fallbackUrl = fastUrl;
                            break;
                        }
                    }
                }
            }

            // Fallback: HLS master manifest
            if (fastUrl == null && streamingData.has("hlsManifestUrl")) {
                fastUrl = streamingData.get("hlsManifestUrl").getAsString();
                fallbackUrl = fastUrl;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing audio stream URLs", e);
        }
        return new String[]{fastUrl, fallbackUrl};
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
