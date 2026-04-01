package org.example;

import com.abahgat.suffixtree.GeneralizedSuffixTree;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class FrequencyTree {
    private final GeneralizedSuffixTree tree;
    /**
     * An Array of a large number of common german words.
     * If i < j, then words[i] is at least as frequent as words[j].
     */
    private String[] words;
    private final int numberOfWordsWithoutUser;
    /**
     * A Collection of words that were already used by the user. Ordered in terms of the usage frequency.
     */
    private HashMap<String, Integer> userWords;

    public FrequencyTree() throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader("top10000de.txt"));

        words = reader.lines()
                .filter(string -> !string.isBlank())
                .toArray(String[]::new);
        numberOfWordsWithoutUser = words.length;

        tree = new GeneralizedSuffixTree();

        for (int i = 0; i < words.length; i++) {
            tree.put(words[i], i);
        }
        ObjectMapper mapper = new ObjectMapper();

        try {
            userWords = mapper.readValue(new File("userWords.json"),
                            new TypeReference<HashMap<String, Integer>>() {});
        } catch (IOException e) {
            userWords = new HashMap<>();
        }

        updateWordArray();
    }

    public void updateWordArray() {
        TreeSet<Map.Entry<String, Integer>> userWordSet = new TreeSet<>(
                (a, b) -> {
                    int cmp = b.getValue().compareTo(a.getValue()); // absteigend nach Wert
                    if (cmp == 0) {
                        return a.getKey().compareTo(b.getKey()); // Tie-Breaker nach String
                    }
                    return cmp;
                });

        userWordSet.addAll(userWords.entrySet());

        String[] newWordArray = new String[userWordSet.size() + numberOfWordsWithoutUser];

        int i = 0;

        for (Map.Entry<String, Integer> entry : userWordSet) {
            newWordArray[i] = entry.getKey();
            i++;
        }

        for (i = userWordSet.size(); i < newWordArray.length; i++) {
            newWordArray[i] = words[i - userWordSet.size()];
        }
        words = newWordArray;
    }

    public static void main(String[] args) {
        FrequencyTree tree;
        try {
            tree = new FrequencyTree();
        } catch (FileNotFoundException e) {
            return;
        }
        String[] words = tree.getAutoCompletedWords("dam", 9, 0);

        for (String word : words) {
            System.out.println(word);
        }
    }

    /**
     * Adds the String word to userWords. If it is already present, the usage-counter will be incremented.
     * @param word the word that is added.
     */
    public void addWordToUserWords(String word) {
        if(word.isBlank()) {
            return;
        }

        userWords.put(word, userWords.getOrDefault(word, 0) + 1);

        try {
            saveUserWords();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     * @param method the method that should be used to obtain the suggestions
     *               0: lastWord is a prefix of the suggestions
     *               1: lastWord is an infix of the suggestions
     *               2: lastWord is a subsequence of the suggestions
     * @return the most frequent words that are the most likely to be autocompleted
     */
    public String[] getAutoCompletedWords(String lastWord, int numberOfWords, int method) {
        return switch (method) {
            case 0 -> getAutoCompletedWordsUsingConnectedString(lastWord, numberOfWords, true);
            case 1 -> getAutoCompletedWordsUsingConnectedString(lastWord, numberOfWords, false);
            case 2 -> getAutoCompletedWordsUsingUnconnectedString(lastWord, numberOfWords);
            default -> null;
        };
    }

    /**
     * Method, that returns the numberOfWords most frequent words that are the most likely to be autocompleted.
     * lastWord has to be a prefix or infix of the suggestions.
     * @param lastWord string that should be autocompleted
     * @param numberOfWords number of words that are suggested as options
     * @param onlyUsingPrefix If true, lastWord has to be a prefix. If false, lastWord has to be an infix.
     * @return the most frequent words that are the most likely to be autocompleted
     */
    private String[] getAutoCompletedWordsUsingConnectedString(String lastWord, int numberOfWords,
                                                               boolean onlyUsingPrefix) {
        Collection<Integer> indices = tree.search(lastWord);

        List<Integer> smallestIndices = getMostFrequentIndices(lastWord, numberOfWords, indices, onlyUsingPrefix);

        Collections.sort(smallestIndices);

        String[] result = new String[smallestIndices.size()];

        for (int i = 0; i < smallestIndices.size(); i++) {
            result[i] = words[smallestIndices.get(i)];
        }

        return result;
    }

    private List<Integer> getMostFrequentIndices(String lastWord, int numberOfWords, Collection<Integer> indices,
                                                 boolean onlyUsingPrefix) {
        PriorityQueue<Integer> heap = new PriorityQueue<>(numberOfWords, (a, b) -> b - a);
        for (int index : indices) {
            if(onlyUsingPrefix) {
                if(!words[index].startsWith(lastWord)) {
                    continue;
                }
            }

            if (heap.size() < numberOfWords) {
                heap.add(index);
            } else if (index > heap.peek()) {
                heap.poll();
                heap.add(index);
            }
        }

        return new ArrayList<>(heap);
    }

    /**
     * Method, that returns the numberOfWords most frequent words that are the most likely to be autocompleted.
     * lastWord has to be a subsequence of the suggestions.
     * @param lastWord string that should be autocompleted
     * @param numberOfWords number of words that are suggested as options
     * @return the most frequent words that are the most likely to be autocompleted.
     */
    private String[] getAutoCompletedWordsUsingUnconnectedString(String lastWord, int numberOfWords) {
        ArrayList<String> foundWords = new ArrayList<>();

        for (String word : words) {
            if (isSubsequence(lastWord, word)) {
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
    private static boolean isSubsequence(String subsequence, String sequence) {
        if (subsequence.isBlank()) {
            return false;
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
}
