package bl;

import java.util.concurrent.atomic.AtomicInteger;

import dal.IDalFacade;

public class ImportFileThreading implements Runnable {
    private final String line;
    private final IDalFacade facade;
    private final AtomicInteger newWordsCount;
    private final AtomicInteger newMeaningsCount;

    public ImportFileThreading(String line, IDalFacade facade, AtomicInteger newWordsCount, AtomicInteger newMeaningsCount) {
        this.line = line;
        this.facade = facade;
        this.newWordsCount = newWordsCount;
        this.newMeaningsCount = newMeaningsCount;
    }

    @Override
    public void run() {
        try {
            int[] result = facade.processWordFromFile(line);
            newWordsCount.addAndGet(result[0]);
            newMeaningsCount.addAndGet(result[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("Error processing line: " + line + " - " + e.getMessage());
        }
    }
}
