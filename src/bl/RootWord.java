package bl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.util.LinkedList;

public class RootWord {

    public String getFirstWordRoot(String word) {
        
            File jarFile = new File("/mnt/data/AlKhalil-2.1.21.jar");
           try( URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()})){

            Class<?> posTaggerClass = classLoader.loadClass("AlKhalil2.AnalyzedWords");

            Object posTaggerInstance = posTaggerClass.getDeclaredConstructor().newInstance();

            Method tagMethod = posTaggerClass.getMethod("analyzedWords", String.class);

            LinkedList<?> posTaggedResult = (LinkedList<?>) tagMethod.invoke(posTaggerInstance, word);

            if (!posTaggedResult.isEmpty()) {
                Object firstResult = posTaggedResult.get(0);
                String resultString = firstResult.toString();
                
                String wordRoot = resultString.replaceAll(".*wordRoot=([^,]+).*", "$1");
                return wordRoot;

            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}