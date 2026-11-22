package com.zez.app.recording;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Diagnostic utility for real-time PCM audio evaluation and post-recording sample verification.
 * Calculates peak amplitude, RMS audio level in dB, non-zero sample ratios,
 * and detects operating system zero-buffer injection.
 */
public class AudioCaptureValidator {
    private static final String TAG = "AudioCaptureValidator";

    public static class PcmAnalysisResult {
        public final int peakAmplitude;
        public final double rmsLevelDb;
        public final long totalSamples;
        public final long nonZeroSamples;
        public final boolean isCompletelyZero;

        public PcmAnalysisResult(int peakAmplitude, double rmsLevelDb, long totalSamples, long nonZeroSamples) {
            this.peakAmplitude = peakAmplitude;
            this.rmsLevelDb = rmsLevelDb;
            this.totalSamples = totalSamples;
            this.nonZeroSamples = nonZeroSamples;
            this.isCompletelyZero = (nonZeroSamples == 0);
        }
    }

    /**
     * Evaluates a PCM 16-bit sample buffer to compute peak, RMS dB, and non-zero counts.
     */
    public static PcmAnalysisResult analyzePcm(short[] buffer, int readCount) {
        if (buffer == null || readCount <= 0) {
            return new PcmAnalysisResult(0, -96.0, 0, 0);
        }

        int peak = 0;
        long sumSquares = 0;
        long nonZero = 0;

        for (int i = 0; i < readCount; i++) {
            short sample = buffer[i];
            if (sample != 0) {
                nonZero++;
            }
            int abs = Math.abs(sample);
            if (abs > peak) {
                peak = abs;
            }
            sumSquares += (long) sample * sample;
        }

        double rms = Math.sqrt((double) sumSquares / readCount);
        double rmsDb = (rms > 0) ? 20.0 * Math.log10(rms / 32767.0) : -96.0;

        return new PcmAnalysisResult(peak, rmsDb, readCount, nonZero);
    }

    /**
     * Probes hardware microphone availability using a low-overhead AudioRecord instance.
     */
    public static boolean probeHardwareMic(Context context, int audioSource) {
        int sampleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        if (bufferSize <= 0) {
            bufferSize = sampleRate * 2;
        }

        AudioRecord record = null;
        try {
            record = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.w(TAG, "AudioRecord probe failed to initialize with source " + audioSource);
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "AudioRecord probe exception for source " + audioSource + ": " + e.getMessage());
            return false;
        } finally {
            if (record != null) {
                try {
                    record.release();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Inspects an encoded .m4a/.mp4 recording file using MediaExtractor and MediaCodec
     * to determine if its decoded audio frames contain authentic non-zero audio samples
     * or are 100% zero-filled silence injected by Android's AudioPolicyService.
     */
    public static PcmAnalysisResult validateRecordedAudioFile(String filePath) {
        if (filePath == null) {
            return new PcmAnalysisResult(0, -96.0, 0, 0);
        }

        File file = new File(filePath);
        if (!file.exists() || file.length() < 200) {
            return new PcmAnalysisResult(0, -96.0, 0, 0);
        }

        MediaExtractor extractor = new MediaExtractor();
        MediaCodec decoder = null;
        try {
            extractor.setDataSource(filePath);
            int trackIndex = -1;
            MediaFormat format = null;

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat trackFormat = extractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    trackIndex = i;
                    format = trackFormat;
                    break;
                }
            }

            if (trackIndex < 0 || format == null) {
                Log.w(TAG, "No audio track found in file: " + filePath);
                return new PcmAnalysisResult(0, -96.0, 0, 0);
            }

            extractor.selectTrack(trackIndex);
            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, null, null, 0);
            decoder.start();

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean sawInputEOS = false;
            boolean sawOutputEOS = false;
            int maxFramesToExamine = 50; // Check up to 50 audio frames for non-zero acoustic content
            int framesExamined = 0;

            int globalPeak = 0;
            long globalSumSq = 0;
            long globalTotalSamples = 0;
            long globalNonZeroSamples = 0;

            while (!sawOutputEOS && framesExamined < maxFramesToExamine) {
                if (!sawInputEOS) {
                    int inputBufIndex = decoder.dequeueInputBuffer(5000);
                    if (inputBufIndex >= 0) {
                        ByteBuffer inputBuf = decoder.getInputBuffer(inputBufIndex);
                        if (inputBuf != null) {
                            int sampleSize = extractor.readSampleData(inputBuf, 0);
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                sawInputEOS = true;
                            } else {
                                decoder.queueInputBuffer(inputBufIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                                extractor.advance();
                            }
                        }
                    }
                }

                int outputBufferIndex = decoder.dequeueOutputBuffer(info, 5000);
                if (outputBufferIndex >= 0) {
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true;
                    }

                    ByteBuffer outputBuf = decoder.getOutputBuffer(outputBufferIndex);
                    if (outputBuf != null && info.size > 0) {
                        framesExamined++;
                        outputBuf.position(info.offset);
                        outputBuf.limit(info.offset + info.size);
                        short[] pcmChunk = new short[info.size / 2];
                        outputBuf.asShortBuffer().get(pcmChunk);

                        for (short s : pcmChunk) {
                            globalTotalSamples++;
                            if (s != 0) {
                                globalNonZeroSamples++;
                            }
                            int abs = Math.abs(s);
                            if (abs > globalPeak) {
                                globalPeak = abs;
                            }
                            globalSumSq += (long) s * s;
                        }
                    }
                    decoder.releaseOutputBuffer(outputBufferIndex, false);
                }
            }

            double rms = (globalTotalSamples > 0) ? Math.sqrt((double) globalSumSq / globalTotalSamples) : 0;
            double rmsDb = (rms > 0) ? 20.0 * Math.log10(rms / 32767.0) : -96.0;

            Log.d(TAG, "Audio file sample inspection complete for " + filePath +
                    " -> Peak: " + globalPeak + ", NonZero: " + globalNonZeroSamples + "/" + globalTotalSamples +
                    ", RMS: " + String.format("%.1f", rmsDb) + " dB");

            return new PcmAnalysisResult(globalPeak, rmsDb, globalTotalSamples, globalNonZeroSamples);

        } catch (Exception e) {
            Log.w(TAG, "Error validating audio file samples: " + e.getMessage());
            return new PcmAnalysisResult(0, -96.0, 0, 0);
        } finally {
            if (decoder != null) {
                try {
                    decoder.stop();
                    decoder.release();
                } catch (Exception ignored) {}
            }
            try {
                extractor.release();
            } catch (Exception ignored) {}
        }
    }
}
