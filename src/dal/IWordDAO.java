package dal;

import dto.WordDTO;

import java.util.LinkedList;
import java.util.List;

public interface IWordDAO {
	List<WordDTO> searchWord(String word);
	List<WordDTO> searchByMeaning(String meaning); 
	List<WordDTO> getAllWordDTOs();
	public String addWord(String word, String arabicMeaning, String persianMeaning);
	public String updateMeaning(String word, String oldMeaning, String newMeaning, String language);
	public String deleteWord(String word);
	public int getWordId(String word);
	public int insertWord(String word);
	public void insertPartOfSpeech(int wordId, String partOfSpeech);
	public void insertUrduMeaning(int wordId, String meaning);
	public void insertPersianMeaning(int wordId, String meaning);
	public boolean urduMeaningExists(int wordId, String meaning);
	public boolean persianMeaningExists(int wordId, String meaning);
	public int[] processWordFromFile(String line);
	public List<String> getPartOfSpeech(String word);
	public WordDTO getMeaningsForWord(String word);
	public void insertRootWord(String arabicWord, String rootWord) ;
	public String getRootWord(String word) ;
	public boolean markWordAsFavorite(int wordId);
	public boolean unmarkWordAsFavorite(int wordId);
	public List<String> getFavoriteWords();
	public boolean isFavorite(String word);
	public List<String> getRecentSearches();
	public void addSearchToHistory(String word);
	public void saveStems(int wordId, LinkedList<String> stemsList);
	public List<String> fetchSuggestions(String text);
	void clearRecentSearchesTable();

}
