const express = require('express');
const router = express.Router();
const { Ytdl } = require('@distube/ytdl-core');
const https = require('https');
const http = require('http');

const ytdl = new Ytdl();

// In-memory cache for resolved track stream URLs (valid for 30 minutes)
const streamCache = new Map();

/**
 * GET /api/music/resolve?videoId=...
 * Resolves direct audio stream URL and video info without requiring client-side cipher extraction.
 */
router.get('/resolve', async (req, res) => {
  const videoId = req.query.videoId || req.query.id;
  if (!videoId) {
    return res.status(400).json({ success: false, error: 'Missing videoId parameter' });
  }

  const cached = streamCache.get(videoId);
  if (cached && (Date.now() - cached.timestamp < 30 * 60 * 1000)) {
    return res.json({ success: true, ...cached.data });
  }

  try {
    const videoUrl = `https://www.youtube.com/watch?v=${videoId}`;
    const info = await ytdl.getInfo(videoUrl);
    const audioFormats = info.formats.filter(f => f.hasAudio && !f.hasVideo);
    audioFormats.sort((a, b) => (b.audioBitrate || 0) - (a.audioBitrate || 0));

    if (audioFormats.length === 0) {
      return res.status(404).json({ success: false, error: 'No audio format found for this video' });
    }

    // Prefer AAC (m4a/mp4, typically itag 140) for universal hardware compatibility on Vivo, OnePlus, Samsung etc.
    const m4aFormat = audioFormats.find(f => f.container === 'mp4' || (f.mimeType && f.mimeType.includes('audio/mp4')));
    const bestAudio = m4aFormat || audioFormats[0];
    const data = {
      videoId,
      title: info.videoDetails.title,
      author: info.videoDetails.author?.name || info.videoDetails.ownerChannelName,
      lengthSeconds: parseInt(info.videoDetails.lengthSeconds, 10) || 0,
      audioUrl: bestAudio.url,
      container: bestAudio.container || 'mp4',
      mimeType: bestAudio.mimeType || 'audio/mp4',
      audioBitrate: bestAudio.audioBitrate,
      contentLength: bestAudio.contentLength
    };

    streamCache.set(videoId, { timestamp: Date.now(), data });
    res.json({ success: true, ...data });
  } catch (err) {
    console.error(`[Music API] Resolve error for ${videoId}:`, err.message);
    res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/music/stream?videoId=...
 * Streams audio bytes directly through backend so emulator/device with DNS issues streams 100% reliably.
 * Reuses resolved stream URLs from streamCache to avoid hammering YouTube with repeated Range requests.
 */
router.get('/stream', async (req, res) => {
  const videoId = req.query.videoId || req.query.id;
  if (!videoId) return res.status(400).send('Missing videoId');

  try {
    let streamUrl = null;
    let mimeType = 'audio/mp4';

    const cached = streamCache.get(videoId);
    if (cached && (Date.now() - cached.timestamp < 30 * 60 * 1000) && cached.data?.audioUrl) {
      streamUrl = cached.data.audioUrl;
      mimeType = cached.data.mimeType || 'audio/mp4';
    } else {
      const videoUrl = `https://www.youtube.com/watch?v=${videoId}`;
      const info = await ytdl.getInfo(videoUrl);
      const audioFormats = info.formats.filter(f => f.hasAudio && !f.hasVideo);
      audioFormats.sort((a, b) => (b.audioBitrate || 0) - (a.audioBitrate || 0));

      if (audioFormats.length === 0) {
        return res.status(404).send('No audio formats found');
      }

      const m4aFormat = audioFormats.find(f => f.container === 'mp4' || (f.mimeType && f.mimeType.includes('audio/mp4')));
      const stream = m4aFormat || audioFormats[0];
      streamUrl = stream.url;
      mimeType = stream.mimeType || 'audio/mp4';

      const data = {
        videoId,
        title: info.videoDetails.title,
        author: info.videoDetails.author?.name || info.videoDetails.ownerChannelName,
        lengthSeconds: parseInt(info.videoDetails.lengthSeconds, 10) || 0,
        audioUrl: stream.url,
        container: stream.container || 'mp4',
        mimeType: stream.mimeType || 'audio/mp4',
        audioBitrate: stream.audioBitrate,
        contentLength: stream.contentLength
      };
      streamCache.set(videoId, { timestamp: Date.now(), data });
    }

    const headers = {
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
      'Accept': '*/*'
    };
    if (req.headers.range) {
      headers['Range'] = req.headers.range;
    }

    const client = streamUrl.startsWith('https') ? https : http;
    const proxyReq = client.get(streamUrl, { headers }, (streamRes) => {
      res.status(streamRes.statusCode || 200);
      if (streamRes.headers['content-range']) res.setHeader('Content-Range', streamRes.headers['content-range']);
      if (streamRes.headers['content-length']) res.setHeader('Content-Length', streamRes.headers['content-length']);
      res.setHeader('Content-Type', streamRes.headers['content-type'] || mimeType);
      res.setHeader('Accept-Ranges', 'bytes');
      streamRes.pipe(res);
    });

    proxyReq.on('error', (e) => {
      console.error('[Music Stream] Pipe error:', e);
      if (!res.headersSent) res.status(502).send('Upstream stream error');
    });

    req.on('close', () => {
      try { proxyReq.destroy(); } catch (ignored) {}
    });
  } catch (err) {
    console.error(`[Music Stream] Error for ${videoId}:`, err.message);
    if (!res.headersSent) res.status(500).send(err.message);
  }
});

/**
 * GET /api/music/download?videoId=...&title=...
 * Pipes or redirects audio directly for clean download in DownloadManager or browser
 */
router.get('/download', async (req, res) => {
  const videoId = req.query.videoId || req.query.id;
  let customTitle = req.query.title || 'track';
  customTitle = customTitle.replace(/[\\/:*?"<>|]/g, '_').trim();

  if (!videoId) {
    return res.status(400).send('Missing videoId');
  }

  try {
    let stream = null;
    const cached = streamCache.get(videoId);
    if (cached && (Date.now() - cached.timestamp < 30 * 60 * 1000) && cached.data?.audioUrl) {
      stream = {
        url: cached.data.audioUrl,
        container: cached.data.container || 'mp4',
        mimeType: cached.data.mimeType || 'audio/mp4',
        contentLength: cached.data.contentLength
      };
    } else {
      const videoUrl = `https://www.youtube.com/watch?v=${videoId}`;
      const info = await ytdl.getInfo(videoUrl);
      const audioFormats = info.formats.filter(f => f.hasAudio && !f.hasVideo);
      audioFormats.sort((a, b) => (b.audioBitrate || 0) - (a.audioBitrate || 0));

      if (audioFormats.length === 0) {
        return res.status(404).send('No audio formats available');
      }

      // Prefer AAC (m4a/mp4) for highest compatibility across Android and iOS
      const m4aFormat = audioFormats.find(f => f.container === 'mp4' || (f.mimeType && f.mimeType.includes('audio/mp4')));
      stream = m4aFormat || audioFormats[0];
    }
    const isM4A = stream.container === 'mp4' || (stream.mimeType && stream.mimeType.includes('audio/mp4'));
    const extension = isM4A ? 'm4a' : (stream.container || 'mp3');
    const filename = `${customTitle}.${extension}`;

    if (req.query.redirect === 'true') {
      return res.redirect(stream.url);
    }

    res.setHeader('Content-Disposition', `attachment; filename="${encodeURIComponent(filename)}"; filename*=UTF-8''${encodeURIComponent(filename)}`);
    if (stream.mimeType) res.setHeader('Content-Type', stream.mimeType);
    if (stream.contentLength) res.setHeader('Content-Length', stream.contentLength);
    res.setHeader('Accept-Ranges', 'bytes');

    const headers = { 'User-Agent': 'Mozilla/5.0' };
    if (req.headers.range) {
      headers['Range'] = req.headers.range;
    }

    const client = stream.url.startsWith('https') ? https : http;
    client.get(stream.url, { headers }, (streamRes) => {
      if (streamRes.headers['content-range']) res.setHeader('Content-Range', streamRes.headers['content-range']);
      if (streamRes.headers['content-length']) res.setHeader('Content-Length', streamRes.headers['content-length']);
      res.status(streamRes.statusCode || 200);
      streamRes.pipe(res);
    }).on('error', (e) => {
      console.error('[Music API] Pipe error:', e);
      if (!res.headersSent) res.status(502).send('Upstream download stream error');
    });
  } catch (err) {
    console.error(`[Music API] Download error for ${videoId}:`, err.message);
    res.status(500).send(`Failed to prepare download: ${err.message}`);
  }
});

module.exports = router;
