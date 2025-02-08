package dto;

import java.util.List;

public class WordDTO {
    private int wordId;
    private String word;
    private List<String> partOfSpeech;  
    private String persianMeaning;
    private String urduMeaning;

    public WordDTO(int wordId, String word, List<String> partOfSpeech, String persianMeaning, String urduMeaning) {
        this.wordId = wordId;
        this.word = word;
        this.partOfSpeech = partOfSpeech;
        this.persianMeaning = persianMeaning;
        this.urduMeaning = urduMeaning;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(List<String> partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getPersianMeaning() {
        return persianMeaning;
    }

    public void setPersianMeaning(String persianMeaning) {
        this.persianMeaning = persianMeaning;
    }

    public String getUrduMeaning() {
        return urduMeaning;
    }

    public void setUrduMeaning(String urduMeaning) {
        this.urduMeaning = urduMeaning;
    }

    @Override
    public String toString() {
        return "Word: " + word +
               ", Part of Speech: " + partOfSpeech +
               ", Persian Meaning: " + persianMeaning +
               ", Urdu Meaning: " + urduMeaning;
    }
}
