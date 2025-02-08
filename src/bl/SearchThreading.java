package bl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dal.IDalFacade;

public class SearchThreading implements Runnable {
    private final String searchText;
    private List<String> suggestions;
    private final IDalFacade facade; 

    public SearchThreading(String searchText, IDalFacade facade) {
        this.searchText = searchText;
        this.facade = facade;
        this.suggestions = new ArrayList<>();
    }

    @Override
    public void run() {
        if (searchText != null && !searchText.trim().isEmpty()) {
            suggestions = facade.fetchSuggestions(searchText);
        } else {
            suggestions = Collections.emptyList();
        }
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
