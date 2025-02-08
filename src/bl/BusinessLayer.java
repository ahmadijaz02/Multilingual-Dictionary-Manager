package bl;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.qcri.farasa.segmenter.Farasa;

import dal.DalFacade;
import dal.IDalFacade;
import dal.IWordDAO;
import dto.WordDTO;

public class BusinessLayer {
    private IDalFacade facade;
    private WebScraper scraper;
    private RootWord rootTagger;
    private static final Logger logger = LogManager.getLogger(BusinessLayer.class);

    public BusinessLayer(IWordDAO dao) {
        logger.info("Initializing BusinessLayer with IWordDAO");
        this.facade = new DalFacade(dao);
        this.scraper = new WebScraper();
        this.rootTagger = new RootWord();
    }

    public List<WordDTO> fetchWords() {
        logger.info("Fetching all words");
        return facade.getAllWordDTOs();
    }

    public List<String> segmentArabicWord(String word) {
        logger.info("Segmenting Arabic word: {}", word);
        try {
            Farasa farasa = new Farasa();
            List<String> segmentedWords = farasa.segmentLine(word);

            List<String> processedWords = new ArrayList<>();
            for (String segmentedWord : segmentedWords) {
                String[] splitWords = segmentedWord.split("\\+");
                Collections.addAll(processedWords, splitWords);
            }

            if (!processedWords.isEmpty()) {
                logger.debug("Segmentation result: {}", processedWords);
                return processedWords;
            } else {
                logger.warn("Segmentation result is empty for word: {}", word);
                return Collections.singletonList("Segmentation result is empty.");
            }
        } catch (Exception e) {
            logger.error("Error during segmentation of word: {}", word, e);
            return Collections.singletonList("Error during segmentation: " + e.getMessage());
        }
    }

    public List<WordDTO> searchWord(String word) {
        logger.info("Searching for word: {}", word);
        return facade.searchWord(word);
    }

    public List<WordDTO> searchByMeaning(String meaning) {
        logger.info("Searching by meaning: {}", meaning);
        return facade.searchByMeaning(meaning);
    }

    public boolean importWordDetailsFromWebsite(String arabicWord) {
        logger.info("Importing word details from website for: {}", arabicWord);
        try {
            String urduDetails = scraper.scrapeUrduMeaning(arabicWord);
            if (urduDetails.isEmpty()) {
                logger.warn("No Urdu details found for: {}", arabicWord);
                return false;
            }

            int wordId = facade.insertWord(arabicWord);
            facade.insertUrduMeaning(wordId, urduDetails);

            String persianMeaning = scraper.scrapePersianMeaning(arabicWord);
            if (persianMeaning != null) {
                facade.insertPersianMeaning(wordId, persianMeaning);
            }

            logger.info("Successfully imported details for: {}", arabicWord);
            return true;
        } catch (Exception e) {
            logger.error("Error importing word details for: {}", arabicWord, e);
            return false;
        }
    }

    public String addWord(String word, String urduMeaning, String persianMeaning) {
        logger.info("Adding word: {}", word);
        if (word == null || word.trim().isEmpty() || urduMeaning == null ||
                urduMeaning.trim().isEmpty() || persianMeaning == null || persianMeaning.trim().isEmpty()) {
            logger.error("Invalid input: Word or meanings cannot be empty");
            throw new IllegalArgumentException("Word or meaning cannot be empty");
        }
        return facade.addWord(word, urduMeaning, persianMeaning);
    }

    public String updateMeaning(String word, String oldMeaning, String newMeaning, String language) {
        logger.info("Updating meaning for word: {}", word);
        if (word == null || word.trim().isEmpty() || oldMeaning == null || oldMeaning.trim().isEmpty() || newMeaning == null || newMeaning.trim().isEmpty()) {
            logger.error("Invalid input: Word, old meaning, and new meaning cannot be empty");
            throw new IllegalArgumentException("Word, old meaning, and new meaning cannot be empty");
        }
        return facade.updateMeaning(word, oldMeaning, newMeaning, language);
    }

    public String deleteWord(String word) {
        logger.info("Deleting word: {}", word);
        if (word == null || word.trim().isEmpty()) {
            logger.error("Invalid input: Word must not be empty.");
            throw new IllegalArgumentException("Word must not be empty.");
        }
        return facade.deleteWord(word);
    }

    public int[] addWordsFromFileWithThreads(String filePath) {
        logger.info("Adding words from file with threads: {}", filePath);
        AtomicInteger newWordsCount = new AtomicInteger(0);
        AtomicInteger newMeaningsCount = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Runnable task = new ImportFileThreading(line, facade, newWordsCount, newMeaningsCount);
                Thread thread = new Thread(task);
                threads.add(thread);
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error processing file: {}", filePath, e);
        }

        logger.info("Words added: {}, Meanings added: {}", newWordsCount.get(), newMeaningsCount.get());
        return new int[]{newWordsCount.get(), newMeaningsCount.get()};
    }
    
    public void insertPartOfSpeech(String word, String manualPos) {
        try {
            logger.info("Inserting part of speech for word: {}", word);
            int wordId = facade.getWordId(word);
            facade.insertPartOfSpeech(wordId, manualPos);
            logger.info("Successfully inserted part of speech: {} for word: {}", manualPos, word);
        } catch (Exception e) {
            logger.error("Error inserting part of speech for word: {}", word, e);
        }
    }

    public String tagPartOfSpeech(String word) {
        logger.info("Starting part of speech tagging for word: {}", word);
        
        try {
            // Load the JAR file and class
            File jarFile = new File("/mnt/data/AlKhalil-2.1.21.jar");
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
            logger.debug("Loading class from JAR: AlKhalil2.AnalyzedWords");

            Class<?> analyzedWordsClass = classLoader.loadClass("AlKhalil2.AnalyzedWords");
            Object analyzedWordsInstance = analyzedWordsClass.getDeclaredConstructor().newInstance();
            Method analyzeMethod = analyzedWordsClass.getMethod("analyzedWords", String.class);

            // Invoke the analyze method
            LinkedList<?> posTaggedResult = (LinkedList<?>) analyzeMethod.invoke(analyzedWordsInstance, word);
            logger.debug("POS tagged result: {}", posTaggedResult);

            if (!posTaggedResult.isEmpty()) {
                Set<String> uniqueWordTypes = new HashSet<>();

                // Collect unique word types
                for (Object result : posTaggedResult) {
                    Method getWordTypeMethod = result.getClass().getMethod("getWordType");
                    String wordType = (String) getWordTypeMethod.invoke(result);
                    uniqueWordTypes.add(wordType);
                }

                int wordId = facade.getWordId(word);
                if (wordId == -1) {
                    wordId = facade.insertWord(word);
                    logger.info("Inserted new word: {} with ID: {}", word, wordId);
                }

                // Insert each part of speech
                for (String pos : uniqueWordTypes) {
                    facade.insertPartOfSpeech(wordId, pos);
                    logger.info("Inserted part of speech: {} for word ID: {}", pos, wordId);
                }

                logger.info("Tagging completed. Unique word types: {}", uniqueWordTypes);
                return uniqueWordTypes.toString();
            } else {
                logger.warn("No part of speech found for word: {}", word);
            }
        } catch (Exception e) {
            logger.error("Error tagging part of speech for word: {}", word, e);
        }

        return "No part of speech found";
    }


	public String findRootWord(String arabicWord) {
	    logger.info("Finding root word for: {}", arabicWord);
	    String rootWord = facade.getRootWord(arabicWord);

	    if (rootWord == null) {
	        rootWord = rootTagger.getFirstWordRoot(arabicWord);
	        if (rootWord != null) {
	            logger.info("Root word found: {}. Saving to database.", rootWord);
	            facade.insertRootWord(arabicWord, rootWord);
	        } else {
	            logger.warn("No root word found for: {}", arabicWord);
	        }
	    } else {
	        logger.info("Root word retrieved from database: {}", rootWord);
	    }

	    return rootWord;
	}

	public String tokenization(String arabicWord) {
	    logger.info("Tokenizing word: {}", arabicWord);
	    String rootWord = findRootWord(arabicWord);

	    if (rootWord == null || rootWord.isEmpty()) {
	        logger.warn("No root word found for tokenization of: {}", arabicWord);
	        return "No Root Word Found";
	    }

	    StringBuilder formattedRoot = new StringBuilder();
	    for (int i = 0; i < rootWord.length(); i++) {
	        formattedRoot.append(rootWord.charAt(i));
	        if (i < rootWord.length() - 1) {
	            formattedRoot.append("-");
	        }
	    }
	    logger.info("Tokenized word: {} -> {}", arabicWord, formattedRoot.toString());
	    return formattedRoot.toString();
	}


	public List<WordDTO> getMeaningsForCustomDictionary(String passage) {
        logger.info("Starting to get meanings for custom dictionary from passage: {}", passage);
        
        List<WordDTO> wordDTOList = Collections.synchronizedList(new ArrayList<>());
        String[] words = passage.split("\\s+");
        List<Thread> threads = new ArrayList<>();

        
        for (String word : words) {
            Runnable task = () -> {
                logger.debug("Processing word: {}", word);

               
                WordDTO wordDTO = facade.getMeaningsForWord(word);
                if (wordDTO == null) {
                    logger.warn("No meaning found for word: {}", word);
                    List<String> segmentedWords = segmentArabicWord(word);
                    logger.debug("Segmenting word: {} into: {}", word, segmentedWords);

                    for (String segmentedWord : segmentedWords) {
                        WordDTO segmentedWordDTO = facade.getMeaningsForWord(segmentedWord);
                        if (segmentedWordDTO != null) {
                            logger.info("Meaning found for segmented word: {}", segmentedWord);
                            wordDTOList.add(segmentedWordDTO);
                        } else {
                            logger.warn("No meaning found for segmented word: {}", segmentedWord);
                        }
                    }
                } else {
                    logger.info("Meaning found for word: {}", word);
                    wordDTOList.add(wordDTO);
                }
            };

            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();  
        }

        
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("Thread interrupted: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();  
            }
        }

        logger.info("Completed processing. Total word meanings found: {}", wordDTOList.size());
        return wordDTOList;
    }


	public String exportMeaningsToFile(List<WordDTO> wordDTOList) {
        String filePath = "arabic_passage_meanings_" + System.currentTimeMillis() + ".txt";
        logger.info("Exporting meanings to file: {}", filePath);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Word Meanings for Arabic Passage\n");
            writer.write("=====================================\n\n");

            for (WordDTO wordDTO : wordDTOList) {
                logger.debug("Writing meaning for word: {}", wordDTO.getWord());

                writer.write("Word: " + wordDTO.getWord() + "\n");
                writer.write("Part of Speech: " + wordDTO.getPartOfSpeech() + "\n");
                writer.write("Urdu Meaning: " + wordDTO.getUrduMeaning() + "\n");
                writer.write("Persian Meaning: " + wordDTO.getPersianMeaning() + "\n");
                writer.write("--------------------------------------\n");
            }

            logger.info("File export completed successfully: {}", filePath);
        } catch (IOException e) {
            logger.error("Error exporting meanings to file: {}", filePath, e);
        }

        return filePath; 
    }

	public boolean addToFavorite(String word) {
        logger.info("Attempting to add word to favorites: {}", word);
        int wordId = facade.getWordId(word);

        if (!facade.isFavorite(word)) {
            boolean result = facade.markWordAsFavorite(wordId);
            if (result) {
                logger.info("Word successfully added to favorites: {}", word);
            } else {
                logger.warn("Failed to mark word as favorite: {}", word);
            }
            return result;
        }

        logger.info("Word is already a favorite: {}", word);
        return false;
    }

    public boolean unmarkWordAsFavorite(String word) {
        logger.info("Attempting to unmark word as favorite: {}", word);
        int wordId = facade.getWordId(word);
        boolean result = facade.unmarkWordAsFavorite(wordId);

        if (result) {
            logger.info("Word successfully unmarked as favorite: {}", word);
        } else {
            logger.warn("Failed to unmark word as favorite: {}", word);
        }

        return result;
    }

    public List<String> getAllFavoriteWords() {
        logger.info("Fetching all favorite words.");
        List<String> favoriteWords = facade.getFavoriteWords();
        logger.info("Retrieved {} favorite words.", favoriteWords.size());
        return favoriteWords;
    }

    public void removeAllFavoriteWords() {
        logger.info("Removing all favorite words.");
        List<String> favoriteWords = getAllFavoriteWords();

        for (String word : favoriteWords) {
            logger.debug("Removing word from favorites: {}", word);
            unmarkWordAsFavorite(word);
        }

        logger.info("All favorite words have been removed.");
    }

    public void addSearch(String word) {
        if (word != null && !word.trim().isEmpty()) {
            logger.info("Adding search to history: {}", word);
            facade.addSearchToHistory(word);
            logger.info("Search added to history: {}", word);
        } else {
            logger.warn("Attempted to add an empty or null word to search history.");
        }
    }

    public List<String> getRecent() {
        logger.info("Fetching recent searches.");
        List<String> recentSearches = facade.getRecentSearches();
        logger.info("Fetched {} recent searches.", recentSearches.size());
        return recentSearches;
    }

    public LinkedList<String> getStemsForWord(String arabicText) {
        logger.info("Attempting to extract stems for word: {}", arabicText);

        LinkedHashSet<String> uniqueStemsSet = new LinkedHashSet<>();
        LinkedList<String> stemsList = new LinkedList<>();

        try {
            logger.debug("Loading AlKhalil jar for stemming process.");
            File jarFile = new File("/mnt/data/AlKhalil-2.1.21.jar");
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
            Class<?> posTaggerClass = classLoader.loadClass("AlKhalil2.AnalyzedWords");
            Object posTaggerInstance = posTaggerClass.getDeclaredConstructor().newInstance();
            Method tagMethod = posTaggerClass.getMethod("analyzedWords", String.class);

            logger.debug("Invoking analyzedWords method for Arabic text.");
            LinkedList<?> posTaggedResult = (LinkedList<?>) tagMethod.invoke(posTaggerInstance, arabicText);

            logger.debug("Extracting unique stems from the POS tagged result.");
            for (Object word : posTaggedResult) {
                String resultString = word.toString();
                String stem = resultString.replaceAll(".*stem=([^,]+).*", "$1");
                uniqueStemsSet.add(stem);
            }

            stemsList = new LinkedList<>(uniqueStemsSet);
            logger.info("Extracted {} unique stems.", stemsList.size());

            int wordId = facade.getWordId(arabicText);
            logger.debug("Saving extracted stems to database for word ID: {}", wordId);
            facade.saveStems(wordId, stemsList);

        } catch (Exception e) {
            logger.error("Error occurred while extracting stems for word: {}", arabicText, e);
        }

        return stemsList;
    }

    
    public List<String> getSuggestions(String text) {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Empty or null input provided for suggestions.");
            return Collections.emptyList();
        }

        logger.info("Fetching suggestions for text: {}", text);
        SearchThreading task = new SearchThreading(text, facade);
        Thread thread = new Thread(task);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while getting suggestions for text: {}", text, e);
            Thread.currentThread().interrupt();
        }

        List<String> suggestions = task.getSuggestions();
        logger.info("Suggestions fetched: {}", suggestions);
        return suggestions;
    }

}