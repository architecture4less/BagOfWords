package me.jwotoole9141.bagofwords;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tests {@link BagOfWords} with the text file {@code basketball_words_only.txt}
 *
 * @author Jared O'Toole
 */
public class Main {

    public static String asPrettyJson(Map<String, Integer> map) {
        return '{' + map.entrySet().stream()
                .map(e -> String.format("\"%s\": %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(",\n  "))
                + '}';
    }

    /**
     * @param args unused command line args
     */
    public static void main(String[] args) {

        Path words = Paths.get("basketball_words_only.txt");
        BagOfWords bag = new BagOfWords(2);
        try {
            bag.loadFrom(words);
            System.out.println("\nLoaded the bag of words! ("
                    + bag.getOrder() + "-gram / "
                    + bag.sizeUnique() + " unique / "
                    + bag.sizeTotal() + " total)");
        }
        catch (IOException ex) {
            System.out.println("\nCouldn't load '" + words + "': " + ex.getMessage());
            return;
        }

        System.out.println("\nThe bag, ordered by word: \n" + asPrettyJson(bag.viewMapByWord()));
        System.out.println("\nThe bag, ordered by amount: \n" + asPrettyJson(bag.viewMapByAmount()));
    }
}
