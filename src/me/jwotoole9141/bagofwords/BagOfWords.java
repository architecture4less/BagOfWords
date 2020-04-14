package me.jwotoole9141.bagofwords;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An n-gram that can be loaded from a plain text file.
 *
 * @author Jared O'Toole
 */
public class BagOfWords {

    /**
     * The default set of words ignored when loading a bag.
     */
    public static final Set<String> DEFAULT_STOP_WORDS = Set.of("the", "in", "a", "an", "to", "am", "is");

    private final int order;
    private final Set<String> stopWords;
    private Map<String, Integer> bagByWord;
    private Map<String, Integer> bagByAmount;
    private int sizeUnique;
    private int sizeTotal;

    public BagOfWords(int sequenceLen) {
        this(sequenceLen, null);
    }

    public BagOfWords(int sequenceLen, Set<String> wordsToIgnore) {
        order = sequenceLen;
        stopWords = wordsToIgnore != null ? Collections.unmodifiableSet(wordsToIgnore) : DEFAULT_STOP_WORDS;
        bagByWord = Collections.emptyMap();
        bagByAmount = Collections.emptyMap();
        sizeUnique = 0;
        sizeTotal = 0;
    }

    /**
     * @return the number of words that get grouped together
     */
    public int getOrder() {
        return order;
    }

    /**
     * @return the set of words ignored when loading the bag
     */
    public Set<String> getStopWords() {
        return stopWords;
    }

    /**
     * Fills the bag with all of the words found. Any characters in a
     * word that are outside the regex [a-zA-Z0-9-_] are ignored.
     * Spaces and blank words are also ignored.
     *
     * @param textFile  a plain text file
     * @param skipLines the number of file header lines to skip
     * @throws IOException the file could not be found or read
     */
    public void loadFrom(Path textFile, int skipLines) throws IOException {

        // the map reduce split step
        List<String> words = Files.lines(textFile)
                .skip(skipLines)
                .map(l -> l.trim().toLowerCase().split("\\s+"))
                .flatMap(Arrays::stream)
                .map(w -> w.replaceAll("[^a-zA-Z0-9-_]", ""))
                .filter(w -> !w.isBlank() && !stopWords.contains(w))
                .collect(Collectors.toList());

        // the map reduce shuffle and reduce steps
        bagByWord = new TreeMap<>();
        for (int i = 0; i <= words.size() - order; i++) {
            bagByWord.merge(String.join(" ", words.subList(i, i + order)), 1, Integer::sum);
        }

        bagByAmount = bagByWord.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));

        sizeUnique = bagByWord.keySet().size();
        sizeTotal = bagByAmount.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * @return the number of unique words in this bag
     */
    public int sizeUnique() {
        return sizeUnique;
    }

    /**
     * @return the total number of words counted
     */
    public int sizeTotal() {
        return sizeTotal;
    }

    /**
     * @return a word counter sorted alphabetically
     */
    public Map<String, Integer> viewMapByWord() {
        return Collections.unmodifiableMap(bagByWord);
    }

    /**
     * @return a word counter sorted in reverse amount order
     */
    public Map<String, Integer> viewMapByAmount() {
        return Collections.unmodifiableMap(bagByAmount);
    }
}
