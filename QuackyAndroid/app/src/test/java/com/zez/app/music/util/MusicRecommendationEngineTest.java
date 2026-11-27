package com.zez.app.music.util;

import com.zez.app.music.model.MusicTrack;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MusicRecommendationEngineTest {
    @Test
    public void normalizeAndDeduplicateFiltersAlternateVersions() {
        MusicTrack official = new MusicTrack("v1", "Kesariya (Official Audio)", "Arijit Singh - Topic", "4:28", "", "50M");
        MusicTrack cover = new MusicTrack("v2", "Kesariya [Cover by Creator]", "Random Cover", "3:10", "", "12K");
        MusicTrack slowed = new MusicTrack("v3", "Kesariya (Slowed + Reverb)", "Lofi Edits", "4:55", "", "80K");
        MusicTrack other = new MusicTrack("v4", "Apna Bana Le", "Arijit Singh", "4:21", "", "90M");

        List<MusicTrack> ranked = MusicRecommendationEngine.rankSearchResults("Kesariya", Arrays.asList(cover, slowed, official));
        assertEquals("v1", ranked.get(0).getId());

        List<MusicTrack> deduped = MusicRecommendationEngine.deduplicateBySongTitle(Arrays.asList(official, cover, slowed, other));
        assertEquals(2, deduped.size());
        assertEquals("v1", deduped.get(0).getId());
        assertEquals("v4", deduped.get(1).getId());
        assertTrue(MusicRecommendationEngine.normalizeTitleKey("Kesariya (Official Video) [4K]").equals("kesariya"));
    }
}
