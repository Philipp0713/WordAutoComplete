package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class FrequencyTree {
    /**
     * A Collection of all words. Ordered in terms of the usage frequency.
     */
    private final TreeSet<WordWithUsage> words;
    /**
     * A Map of words that were already used by the user with the respective usage of each word.
     */
    private HashMap<String, Integer> userWords;

    /**
     * Stores a map of a large number of common German words and their respective usage. If the usage is smaller, then
     * it is a less common word.
     */
    private final HashMap<String, Integer> mostCommonWords;

    /**
     * Stores if the program should pay attention to the case of the letters.
     */
    private boolean attentionToLowerUppercase;

    /**
     * the method that should be used to get the suggestions
     * 0: lastWord is a prefix of the suggestions
     * 1: lastWord is an infix of the suggestions
     * 2: lastWord is a subsequence of the suggestions
     */
    private int methodUsedToGetWords;

    public FrequencyTree(boolean attentionToLowerUppercase, int methodUsedToGetWords) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader("top10000de.txt"));

        AtomicInteger usage = new AtomicInteger(0);

        words = reader.lines()
                .filter(string -> !string.isBlank())
                .map(string -> new WordWithUsage(string, usage.getAndDecrement()))
                .collect(Collectors.toCollection(TreeSet::new));

        mostCommonWords = new HashMap<>();

        for (WordWithUsage word : words) {
            mostCommonWords.put(word.getWord(), word.getUsage());
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            userWords = mapper.readValue(new File("userWords.json"),
                            new TypeReference<HashMap<String, Integer>>() {});
        } catch (IOException e) {
            userWords = new HashMap<>();
        }

        this.attentionToLowerUppercase = attentionToLowerUppercase;
        this.methodUsedToGetWords = methodUsedToGetWords;

        updateWords();
    }

    public boolean isAttentionToLowerUppercase() {
        return attentionToLowerUppercase;
    }

    public void setAttentionToLowerUppercase(boolean attentionToLowerUppercase) {
        this.attentionToLowerUppercase = attentionToLowerUppercase;
    }

    public int getMethodUsedToGetWords() {
        return methodUsedToGetWords;
    }

    public void setMethodUsedToGetWords(int methodUsedToGetWords) {
        this.methodUsedToGetWords = methodUsedToGetWords;
    }

    public void updateWords() {

        for (Map.Entry<String, Integer> entry : userWords.entrySet()) {
            WordWithUsage newElement = new WordWithUsage(entry.getKey(), entry.getValue());

            boolean isNewWord = true;

            for (WordWithUsage foundElement : words) {
                if (foundElement.equals(newElement)) {
                    if (foundElement.getUsage() <= 0) {
                        // it is a word not created by the user
                        words.remove(foundElement);
                        words.add(newElement);
                    } else {
                        // it is a word already created by the user
                        words.remove(foundElement);
                        newElement.setUsage(newElement.getUsage());
                        words.add(newElement);
                    }
                    isNewWord = false;
                    break;
                }
            }
            if (isNewWord) {
                // it is a new Word
                words.add(newElement);
            }
        }
    }

    /**
     * Adds the String word to userWords. If it is already present, the usage-counter will be incremented.
     * @param word the word that is added.
     */
    public void addWordToWords(String word) {
        if (word.isBlank()) {
            return;
        }

        StringBuilder trimmedWord = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            if (Character.isAlphabetic(c)
                    || Character.isDigit(c)
                    || c == '\''
                    || c == '-') {
                trimmedWord.append(c);
            } else if(c == '/' || c == '+') {
                userWords.put(trimmedWord.toString(), userWords.getOrDefault(trimmedWord.toString(), 0) + 1);
                trimmedWord = new StringBuilder();
            }
        }
        userWords.put(trimmedWord.toString(), userWords.getOrDefault(trimmedWord.toString(), 0) + 1);

        updateWords();

        try {
            saveUserWords();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the string word from the database of words used by the program.
     * @param word string that will be deleted
     */
    public void deleteWordFromWords(String word) {

        if (!isUserWord(word)) {
            //no element could be found, and you should not be able to delete non-user generated words
            return;
        }
        words.remove(new WordWithUsage(word, userWords.get(word)));
        userWords.remove(word);

        try {
            saveUserWords();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns if the String word is a word generated by the user.
     * @param word String that will be tested
     * @return if the String word is a word generated by the user.
     */
    public boolean isUserWord(String word) {
        return userWords.containsKey(word) && !mostCommonWords.containsKey(word);
    }

    /**
     * Saves all the information in userWords to userWords.json.
     * @throws IOException if an IOException occurs
     */
    public void saveUserWords() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("userWords.json"), userWords);
    }

    /**
     * Method, that returns the numberOfWords most frequent words that are the most likely to be autocompleted.
     * @param lastWord string that should be autocompleted
     * @param numberOfWords number of words that are suggested as options
     * @return the most frequent words that are the most likely to be autocompleted
     */
    public String[] getAutoCompletedWords(String lastWord, int numberOfWords) {
        return switch (methodUsedToGetWords) {
            case 0 -> getAutoCompletedWordsUsingPredicate(lastWord, numberOfWords, this::isPrefix);
            case 1 -> getAutoCompletedWordsUsingPredicate(lastWord, numberOfWords, this::isInfix);
            case 2 -> getAutoCompletedWordsUsingPredicate(lastWord, numberOfWords, this::isSubsequence);
            default -> null;
        };
    }

    /**
     * Method, that returns the numberOfWords most frequent words that are the most likely to be autocompleted.
     * lastWord has to be a prefix or infix of the suggestions.
     * @param lastWord string that should be autocompleted
     * @param numberOfWords number of words that are suggested as options
     * @param predicate the BiPredicate that decides if a String should be suggested. First argument is always lastword
     *                  and the second argument is the String
     * @return the most frequent words that are the most likely to be autocompleted
     */
    private String[] getAutoCompletedWordsUsingPredicate(String lastWord, int numberOfWords,
                                                         BiPredicate<String, String> predicate) {
        ArrayList<String> foundWords = new ArrayList<>();

        for (WordWithUsage entry : words) {
            String word = entry.getWord();

            if (predicate.test(lastWord, word)) {
                foundWords.add(word);
            }
            if (foundWords.size() >= numberOfWords) {
                break;
            }
        }

        return foundWords.toArray(String[]::new);
    }

    /**
     * Method that returns if a given subsequence is a subsequence of a given sequence
     * @param subsequence the subsequence
     * @param sequence the sequence
     * @return if a given subsequence is a subsequence of a given sequence
     */
    private boolean isSubsequence(String subsequence, String sequence) {
        if (subsequence.isBlank()) {
            return false;
        }

        if (!attentionToLowerUppercase) {
            subsequence = subsequence.toLowerCase();
            sequence = sequence.toLowerCase();
        }

        int pos = 0;

        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == subsequence.charAt(pos)) {
                pos++;
            }

            if (pos >= subsequence.length()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the String prefix is a prefix of the String string.
     * @param prefix potential prefix
     * @param string string the prefix is compared to
     * @return if prefix is a prefix of string
     */
    private boolean isPrefix(String prefix, String string) {
        if (string.length() < prefix.length()) {
            return false;
        }

        if (!attentionToLowerUppercase) {
            prefix = prefix.toLowerCase();
            string = string.toLowerCase();
        }

        for (int i = 0; i < prefix.length(); i++) {
            if (prefix.charAt(i) != string.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns if the String infix is an infix of the String string.
     * @param infix potential infix
     * @param string string the infix is compared to
     * @return if infix is an infix of string
     */
    private boolean isInfix(String infix, String string) {

        if (!attentionToLowerUppercase) {
            infix = infix.toLowerCase();
            string = string.toLowerCase();
        }

        for (int i = 0; i < string.length(); i++) {
            if (isPrefix(infix, string.substring(i))) {
                return true;
            }
        }
        return false;
    }

    private static class WordWithUsage implements Comparable<WordWithUsage> {
        private String word;
        private int usage;

        public WordWithUsage(String word, int usage) {
            this.word = word;
            this.usage = usage;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getUsage() {
            return usage;
        }

        public void setUsage(int usage) {
            this.usage = usage;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof WordWithUsage wordWithUsage)) {
                return false;
            }

            if (this.getWord() == null) {
                return wordWithUsage.getWord() == null;
            }

            return this.getWord().equals(wordWithUsage.getWord());
        }

        @Override
        public int compareTo(WordWithUsage b) {
            if (this.getWord().equals(b.getWord())) {
                // no duplicate words in the Set
                return 0;
            }

            int cmp = b.getUsage() - this.getUsage(); // descending by usage
            if (cmp == 0) {
                return this.getWord().compareTo(b.getWord()); // Tie-Breaker by word-order
            }
            return cmp;
        }
    }
}
