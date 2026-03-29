package org.example;

import com.abahgat.suffixtree.GeneralizedSuffixTree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FrequencyTree {
    private final GeneralizedSuffixTree tree;
    private final String[] words; // if i < j, then words[i] is at least as frequent as words[j]
    private final int NUMBER_OF_WORDS = 10000;

    public FrequencyTree() throws FileNotFoundException {
        tree = new GeneralizedSuffixTree();
        words = new String[NUMBER_OF_WORDS];

        BufferedReader reader = new BufferedReader(new FileReader("top10000de.txt"));

        for (int i = 0; i < NUMBER_OF_WORDS; i++) {
            String word;
            try {
                word = reader.readLine();
            } catch (IOException e) {
                break;
            }
            words[i] = word;
        }

        for (int i = 0; i < words.length; i++) {
            tree.put(words[i], i);
        }
    }

    public static void main(String[] args) {
        FrequencyTree tree;
        try {
            tree = new FrequencyTree();
        } catch (FileNotFoundException e) {
            return;
        }
        String[] words = tree.getAutoCompletedWords("dam", 10);

        for (String word : words) {
            System.out.println(word);
        }
    }


    public String[] getAutoCompletedWords(String lastWord, int numberOfWords) {
        Collection<Integer> indices = tree.search(lastWord);

        List<Integer> smallestIndices = getMostFrequentIndices(lastWord, numberOfWords, indices);

        Collections.sort(smallestIndices);

        String[] result = new String[smallestIndices.size()];

        for (int i = 0; i < smallestIndices.size(); i++) {
            result[i] = words[smallestIndices.get(i)];
        }

        return result;
    }

    private List<Integer> getMostFrequentIndices(String lastWord, int numberOfWords, Collection<Integer> indices) {
        PriorityQueue<Integer> heap = new PriorityQueue<>(numberOfWords, (a, b) -> b - a);
        for (int index : indices) {
            if(Main.ONLY_PREFIXES) {
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
}
