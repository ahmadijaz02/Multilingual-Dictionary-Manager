package testing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import dal.DataAccessLayer;
import dal.IWordDAO;
import dto.WordDTO;

@TestMethodOrder(OrderAnnotation.class)
public class DataAccessLayerTest {
    private IWordDAO wordDAO;

    @BeforeEach
    public void setUp() throws Exception {
        wordDAO = new DataAccessLayer();
    }

    @Test
    @Order(1)
    public void testAddWord_ValidInput() {
        String result = wordDAO.addWord("example", "مثال", "نمونه");
        assertEquals("Word and meanings added successfully.", result);
    }

    @Test
    @Order(2)
    public void testAddWord_WordAlreadyExists() {
        wordDAO.addWord("example", "مثال", "نمونه");
        // Try inserting the same word again
        String result = wordDAO.addWord("example", "مثال", "نمونه");
        assertEquals("Meanings added to existing word (if not already present).", result);
    }

    @Test
    @Order(3)
    public void testAddWord_EmptyMeaning() {
        String result = wordDAO.addWord("example", "", "");
        assertEquals("Meanings added to existing word (if not already present).", result);
    }

    @Test
    @Order(4)
    public void testAddWord_NullWord() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            wordDAO.addWord(null, "مثال", "نمونه");
        });
        assertEquals("Word cannot be null or empty", exception.getMessage());
    }

    @Test
    @Order(5)
    public void testSearchWord_ValidInput() {
        List<WordDTO> results = wordDAO.searchWord("example");
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("example", results.get(0).getWord());
    }

    @Test
    @Order(6)
    public void testSearchWord_EmptyInput() {
        List<WordDTO> results = wordDAO.searchWord("");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @Order(7)
    public void testUpdateMeaning_ValidUpdate() {
        wordDAO.addWord("example", "oldMeaning", "oldMeaning");
        String result = wordDAO.updateMeaning("example", "oldMeaning", "newMeaning", "Urdu");
        assertEquals("Urdu meaning updated successfully.", result);
    }

    @Test
    @Order(8)
    public void testUpdateMeaning_InvalidWord() {
        String result = wordDAO.updateMeaning("nonexistent", "oldMeaning", "newMeaning", "Urdu");
        assertEquals("Word not found in the database.", result);
    }

    @Test
    @Order(9)
    public void testMarkWordAsFavorite() {
        String word = "example";       
        int wordId = wordDAO.getWordId(word);

        boolean result = wordDAO.markWordAsFavorite(wordId);
        assertTrue(result, "Word should be marked as favorite.");
        assertTrue(wordDAO.isFavorite(word), "The word should be marked as favorite in the database.");
    }
    
    @Test
    @Order(10)
    public void testIsFavorite() {
        String word = "example";
        int wordId = wordDAO.getWordId(word);

        // Check if the word is marked as favorite
        boolean isFavorite = wordDAO.isFavorite(word);
        assertTrue(isFavorite, "The word should be marked as favorite.");

        // Now unmark the word as favorite
        wordDAO.unmarkWordAsFavorite(wordId);

        // Check if the word is still marked as favorite
        isFavorite = wordDAO.isFavorite(word);
        assertFalse(isFavorite, "The word should not be marked as favorite.");
    }
    @Test
    @Order(11)
    public void testUnmarkWordAsFavorite() {
        String word = "example";
        int wordId = wordDAO.getWordId(word);

        // Mark the word as favorite first
        wordDAO.markWordAsFavorite(wordId);

        // Now unmark the word as favorite
        boolean result = wordDAO.unmarkWordAsFavorite(wordId);
        assertTrue(result, "Word should be unmarked as favorite.");
        assertFalse(wordDAO.isFavorite(word), "The word should no longer be marked as favorite in the database.");
    }

    @Test
    @Order(12)
    public void testGetFavoriteWords() {
        String word1 = "example";
       
        int wordId1 = wordDAO.getWordId(word1);
        wordDAO.markWordAsFavorite(wordId1);

        List<String> favoriteWords = wordDAO.getFavoriteWords();

        assertTrue(favoriteWords.contains(word1), "The word 'example' should be in the list of favorite words.");
    }

    @Test
    @Order(13)
    public void testInsertRootWord_ValidWord()
    {
        String word = "example";
        String rootWord = "rootExample";

        int wordId = wordDAO.getWordId(word);
        assertTrue(wordId > 0, "Word ID should be a positive integer.");

        wordDAO.insertRootWord(word, rootWord);

        String fetchedRootWord = wordDAO.getRootWord(word);
        assertEquals(rootWord, fetchedRootWord, "Root word should match the inserted value.");
    }
    
    @Test
    @Order(14)
    public void testInsertRootWord_NonExistingWord() {
        String nonExistingWord = "nonExistingWord";
        String rootWord = "nonExistingRoot";

        //  Attempt to insert a root word for a non-existing word
        wordDAO.insertRootWord(nonExistingWord, rootWord);

        //  Fetch the root word; it should return null
        String fetchedRootWord = wordDAO.getRootWord(nonExistingWord);
        assertEquals(null, fetchedRootWord);
    }

    @Test
    @Order(15)
    public void testGetRootWord_ExistingWordWithRoot() {
        String word = "example";
        String rootWord = "rootExample";

        String fetchedRootWord = wordDAO.getRootWord(word);

        assertEquals(rootWord, fetchedRootWord, "Root word should match the previously inserted value.");
    }

    @Test
    @Order(16)
    public void testGetRootWord_ExistingWordWithoutRoot() {
        String word = "example1";

        wordDAO.addWord(word, "مثال", "نمونه");

        String fetchedRootWord = wordDAO.getRootWord(word);

        assertEquals(null, fetchedRootWord);
    }

    @Test
    @Order(17)
    public void testGetRootWord_NonExistingWord() {
        String nonExistingWord = "nonExistingRootWord";

        String fetchedRootWord = wordDAO.getRootWord(nonExistingWord);
        assertEquals(null, fetchedRootWord);
    }
    
    
    @Test
    @Order(18)
    public void testInsertPartOfSpeech_ValidWordId() {
        String word = "example";
        String partOfSpeech = "noun";

        int wordId = wordDAO.getWordId(word);
        assertTrue(wordId > 0, "Word ID should be valid and positive.");

        wordDAO.insertPartOfSpeech(wordId, partOfSpeech);

        List<String> posList = wordDAO.getPartOfSpeech(word);
        assertNotNull(posList, "Part of Speech list should not be null.");
        assertTrue(posList.contains(partOfSpeech), "Inserted part of speech should be present.");
    }

    @Test
    @Order(19)
    public void testGetPartOfSpeech_MultipleEntries() {
        String word = "example";
        String pos1 = "noun";
        String pos2 = "verb";

        int wordId = wordDAO.getWordId(word);
        wordDAO.insertPartOfSpeech(wordId, pos1);
        wordDAO.insertPartOfSpeech(wordId, pos2);

        List<String> posList = wordDAO.getPartOfSpeech(word);

        assertNotNull(posList, "Parts of speech list should not be null.");
        assertTrue(posList.contains(pos1), "First part of speech should be present.");
        assertTrue(posList.contains(pos2), "Second part of speech should be present.");
    }

    @Test
    @Order(20)
    public void testGetPartOfSpeech_NoEntries() {
        String word = "example1";

        List<String> posList = wordDAO.getPartOfSpeech(word);

        // List should be empty
        assertTrue(posList.isEmpty(), "List should be empty for a word with no parts of speech.");
    }

    @Test
    @Order(21)
    public void testGetPartOfSpeech_NonExistingWord() {
        String nonExistingWord = "nonExistingWord";

        List<String> posList = wordDAO.getPartOfSpeech(nonExistingWord);

        // Result should be null
        assertEquals(null, posList);
    }
    
    @Test
    @Order(22)
    public void testProcessWordFromFile_NewWord() {
        String line = "exampleWord,مثال,نمونه,noun";
        int[] expectedInsertCounts = {1, 2}; // 1 word, 2 meanings
        
        int[] insertCounts = wordDAO.processWordFromFile(line);

        assertEquals(expectedInsertCounts[0], insertCounts[0], "Word insertion count should be 1.");
        assertEquals(expectedInsertCounts[1], insertCounts[1], "Meanings insertion count should be 2.");
    }

    @Test
    @Order(23)
    public void testProcessWordFromFile_ExistingWordNewMeanings() {

        String line =  "example,نیا مثال,نیا نمونه,verb";
        int[] expectedInsertCounts = {0, 2}; // New meanings only

        int[] insertCounts = wordDAO.processWordFromFile(line);

        assertEquals(expectedInsertCounts[0], insertCounts[0], "Word insertion count should be 0.");
        assertEquals(expectedInsertCounts[1], insertCounts[1], "Meanings insertion count should be 2.");
    }

    @Test
    @Order(24)
    public void testProcessWordFromFile_InvalidInput() {

    	String line = "invalidInput"; // Less than 4 parts
        int[] expectedInsertCounts = {0, 0};

        int[] insertCounts = wordDAO.processWordFromFile(line);

        assertArrayEquals(expectedInsertCounts, insertCounts, "No insertions should occur for invalid input.");
    }

    @Test
    @Order(25)
    public void testProcessWordFromFile_DuplicateWordAndMeanings() {
        String line = "exampleWord,مثال,نمونه,noun";
        int[] expectedInsertCounts = {0, 0}; // No new inserts

        int[] insertCounts = wordDAO.processWordFromFile(line);

        assertEquals(expectedInsertCounts[0], insertCounts[0], "Word insertion count should be 0.");
        assertEquals(expectedInsertCounts[1], insertCounts[1], "Meanings insertion count should be 0.");
    }
    
    @Test
    @Order(26)
    public void testDeleteWord_NullWord() {
        String result = wordDAO.deleteWord(null);
        assertEquals("Word cannot be empty", result);
    }

    @Test
    @Order(27)
    public void testDeleteWord_ValidWord() {
        wordDAO.addWord("example", "meaning", "meaning");
        String result = wordDAO.deleteWord("example");
        String result1 = wordDAO.deleteWord("example1");
        String result2 = wordDAO.deleteWord("exampleWord");

        assertEquals("Word and all its meanings deleted successfully.", result);
        assertEquals("Word and all its meanings deleted successfully.", result1);
        assertEquals("Word and all its meanings deleted successfully.", result2);

    }
    
    @Test
    @Order(28)
    public void testAddSearchToHistory_NewWord() {
    	wordDAO.clearRecentSearchesTable();
        wordDAO.addSearchToHistory("word1");
        List<String> recentSearches = wordDAO.getRecentSearches();

        assertEquals(1, recentSearches.size(), "History should contain 1 word.");
        assertEquals("word1", recentSearches.get(0), "The added word should be 'word1'.");
    }

    @Test
    @Order(29)

    public void testAddSearchToHistory_DuplicateWord()  {
        wordDAO.addSearchToHistory("word1");
        wordDAO.addSearchToHistory("word1"); // Adding duplicate

        List<String> recentSearches = wordDAO.getRecentSearches();

        assertEquals(1, recentSearches.size(), "Duplicate words should not be added.");
        assertEquals("word1", recentSearches.get(0), "The word in history should still be 'word1'.");
    }

    @Test
    @Order(30)

    public void testAddSearchToHistory_LimitHistorySize() {
        wordDAO.addSearchToHistory("word1");
        wordDAO.addSearchToHistory("word2");
        wordDAO.addSearchToHistory("word3");
        wordDAO.addSearchToHistory("word4");
        wordDAO.addSearchToHistory("word5");
        wordDAO.addSearchToHistory("word6"); 

        List<String> recentSearches = wordDAO.getRecentSearches();
        wordDAO.clearRecentSearchesTable();
        assertEquals(5, recentSearches.size(), "History should only contain 5 words.");
        assertFalse(recentSearches.contains("word1"), "The oldest word 'word1' should be removed.");
        assertEquals("word6", recentSearches.get(0), "The most recent word should be 'word6'.");
    }


}
