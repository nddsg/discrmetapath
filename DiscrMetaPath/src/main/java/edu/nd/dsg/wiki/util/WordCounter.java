package edu.nd.dsg.wiki.util;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;

public class WordCounter {

    static HashMap<String, Integer> getWordCount(String inputStr){
        String escapedText = StringEscapeUtils.escapeHtml4(inputStr);
        String[] words = escapedText.split("[\\W]+");
        HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
        for(String word : words) {
            if(!wordCount.containsKey(word)){
                wordCount.put(word,0);
            }
            wordCount.put(word, wordCount.get(word)+1);
        }
        return wordCount;
    }

}
