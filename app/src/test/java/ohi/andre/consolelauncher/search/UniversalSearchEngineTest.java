package ohi.andre.consolelauncher.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UniversalSearchEngineTest {

    @Test
    public void testLevenshteinDistanceExactMatch() {
        int distance = UniversalSearchEngine.calculateLevenshteinDistance("spotify", "spotify");
        assertEquals(0, distance);
    }

    @Test
    public void testLevenshteinDistanceSmallDifference() {
        int distance = UniversalSearchEngine.calculateLevenshteinDistance("spotfy", "spotify");
        assertEquals(1, distance);
    }

    @Test
    public void testMatchScoreExact() {
        int score = UniversalSearchEngine.calculateMatchScore("youtube", "youtube");
        assertEquals(UniversalSearchEngine.SCORE_EXACT, score);
    }

    @Test
    public void testMatchScorePrefix() {
        int score = UniversalSearchEngine.calculateMatchScore("you", "youtube");
        assertEquals(UniversalSearchEngine.SCORE_PREFIX, score);
    }

    @Test
    public void testMatchScoreMultiToken() {
        int score = UniversalSearchEngine.calculateMatchScore("play store", "google play store");
        assertEquals(UniversalSearchEngine.SCORE_TOKEN_PREFIX, score);
    }

    @Test
    public void testMatchScoreFuzzy() {
        int score = UniversalSearchEngine.calculateMatchScore("spotfy", "spotify");
        assertTrue(score > 0);
        assertTrue(score < UniversalSearchEngine.SCORE_PREFIX);
    }
}
