package bl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;


public class WebScraper {


    public String scrapeUrduMeaning(String word) {
        String arabicMeaning = null;
        String filePath = "Urdu.html";

        try {
            Document doc = Jsoup.parse(new File(filePath), "UTF-8");
            Elements rows = doc.select("table.results tbody tr");

            for (Element row : rows) {
                String urduWordEntry = row.select("td").first().text();

                if (urduWordEntry.contains(word)) {
                    String fullMeaning = row.select("td").get(1).text();
                    
                    fullMeaning = fullMeaning.replaceAll("\\[.*?\\]", "").trim(); 
                    String[] meanings = fullMeaning.split("،| "); 
                    arabicMeaning = meanings[0].trim(); 
                    break; 
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the HTML file: " + e.getMessage());
        }

        return arabicMeaning;
    }


    public String scrapePersianMeaning(String arabicWord) {
        String filePath = "Persian.html";
        String persianMeaning = null;

        try {
            Document doc = Jsoup.parse(new File(filePath), "UTF-8");

            Elements rows = doc.select("table.results tbody tr");

            for (Element row : rows) {
                String arabicWordEntry = row.select("td").first().text();
                if (arabicWordEntry.contains(arabicWord)) { 
                    persianMeaning = row.select("td").get(1).text();
                    break; 
                }
            }
        } catch (IOException e) {
            System.out.println("Error scraping Persian meaning: " + e.getMessage());
        }

        return persianMeaning;
    }

}




