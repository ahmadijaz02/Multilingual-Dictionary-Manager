package dal;

import dto.WordDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class DalFacade implements IDalFacade {

    private static final Logger logger = LogManager.getLogger(DalFacade.class);

    private IWordDAO wordDAO;

    public DalFacade(IWordDAO dao) {
        this.wordDAO = dao;
        logger.info("DalFacade initialized with DAO: {}", dao.getClass().getName());
    }

    @Override
    public List<WordDTO> searchWord(String word) {
        logger.info("searchWord() called with word: '{}'", word);
        return wordDAO.searchWord(word);
    }

    @Override
    public List<WordDTO> searchByMeaning(String meaning) {
        logger.info("searchByMeaning() called with meaning: '{}'", meaning);
        return wordDAO.searchByMeaning(meaning);
    }

    @Override
    public List<WordDTO> getAllWordDTOs() {
        logger.info("getAllWordDTOs() called");
        return wordDAO.getAllWordDTOs();
    }

    @Override
    public String addWord(String word, String arabicMeaning, String persianMeaning) {
        logger.info("addWord() called with word: '{}', Arabic meaning: '{}', Persian meaning: '{}'",
                word, arabicMeaning, persianMeaning);
        return wordDAO.addWord(word, arabicMeaning, persianMeaning);
    }

    @Override
    public String updateMeaning(String word, String oldMeaning, String newMeaning, String language) {
        logger.info("updateMeaning() called with word: '{}', old meaning: '{}', new meaning: '{}', language: '{}'",
                word, oldMeaning, newMeaning, language);
        return wordDAO.updateMeaning(word, oldMeaning, newMeaning, language);
    }

    @Override
    public String deleteWord(String word) {
        logger.info("deleteWord() called with word: '{}'", word);
        return wordDAO.deleteWord(word);
    }

    @Override
    public int getWordId(String word) {
        logger.info("getWordId() called with word: '{}'", word);
        return wordDAO.getWordId(word);
    }

    @Override
    public int insertWord(String word) {
        logger.info("insertWord() called with word: '{}'", word);
        return wordDAO.insertWord(word);
    }

    @Override
    public void insertUrduMeaning(int wordId, String meaning) {
        logger.info("insertUrduMeaning() called with wordId: '{}', meaning: '{}'", wordId, meaning);
        wordDAO.insertUrduMeaning(wordId, meaning);
    }

    @Override
    public void insertPersianMeaning(int wordId, String meaning) {
        logger.info("insertPersianMeaning() called with wordId: '{}', meaning: '{}'", wordId, meaning);
        wordDAO.insertPersianMeaning(wordId, meaning);
    }

    @Override
    public boolean urduMeaningExists(int wordId, String meaning) {
        logger.info("urduMeaningExists() called with wordId: '{}', meaning: '{}'", wordId, meaning);
        return wordDAO.urduMeaningExists(wordId, meaning);
    }

    @Override
    public boolean persianMeaningExists(int wordId, String meaning) {
        logger.info("persianMeaningExists() called with wordId: '{}', meaning: '{}'", wordId, meaning);
        return wordDAO.persianMeaningExists(wordId, meaning);
    }

    @Override
    public int[] processWordFromFile(String line) {
        logger.info("processWordFromFile() called with line: '{}'", line);
        return wordDAO.processWordFromFile(line);
    }

    @Override
    public WordDTO getMeaningsForWord(String word) {
        logger.info("getMeaningsForWord() called with word: '{}'", word);
        return wordDAO.getMeaningsForWord(word);
    }

    @Override
    public void insertRootWord(String arabicWord, String rootWord) {
        logger.info("insertRootWord() called with Arabic word: '{}', root word: '{}'", arabicWord, rootWord);
        wordDAO.insertRootWord(arabicWord, rootWord);
    }

    @Override
    public String getRootWord(String word) {
        logger.info("getRootWord() called with word: '{}'", word);
        return wordDAO.getRootWord(word);
    }

    @Override
    public boolean markWordAsFavorite(int wordId) {
        logger.info("markWordAsFavorite() called with wordId: '{}'", wordId);
        return wordDAO.markWordAsFavorite(wordId);
    }

    @Override
    public boolean unmarkWordAsFavorite(int wordId) {
        logger.info("unmarkWordAsFavorite() called with wordId: '{}'", wordId);
        return wordDAO.unmarkWordAsFavorite(wordId);
    }

    @Override
    public List<String> getFavoriteWords() {
        logger.info("getFavoriteWords() called");
        return wordDAO.getFavoriteWords();
    }

    @Override
    public boolean isFavorite(String word) {
        logger.info("isFavorite() called with word: '{}'", word);
        return wordDAO.isFavorite(word);
    }

    @Override
    public List<String> getRecentSearches() {
        logger.info("getRecentSearches() called");
        return wordDAO.getRecentSearches();
    }

    @Override
    public void addSearchToHistory(String word) {
        logger.info("addSearchToHistory() called with word: '{}'", word);
        if (word != null && !word.trim().isEmpty()) {
            wordDAO.addSearchToHistory(word);
        }
    }

    @Override
    public void insertPartOfSpeech(int wordId, String partOfSpeech) {
        logger.info("insertPartOfSpeech() called with wordId: '{}', part of speech: '{}'", wordId, partOfSpeech);
        wordDAO.insertPartOfSpeech(wordId, partOfSpeech);
    }

    @Override
    public List<String> getPartOfSpeech(String word) {
        logger.info("getPartOfSpeech() called with word: '{}'", word);
        return wordDAO.getPartOfSpeech(word);
    }

    @Override
    public void saveStems(int wordId, LinkedList<String> stemsList) {
        logger.info("saveStems() called with wordId: '{}', stemsList: '{}'", wordId, stemsList);
        wordDAO.saveStems(wordId, stemsList);
    }

    @Override
    public List<String> fetchSuggestions(String text) {
        logger.info("fetchSuggestions() called with text: '{}'", text);
        return wordDAO.fetchSuggestions(text);
    }

	@Override
	public void clearRecentSearchesTable() {
		wordDAO.clearRecentSearchesTable();
	}
}
