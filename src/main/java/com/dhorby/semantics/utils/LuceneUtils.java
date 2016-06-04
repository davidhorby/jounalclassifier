package com.dhorby.semantics.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class LuceneUtils {

    private static Analyzer englishAnalyzer;

    public static List<String> tokenizePhrase(String phrase, CharArraySet stopWordSet) {
        List<String> tokens = new ArrayList<String>();
        englishAnalyzer = new EnglishAnalyzer(stopWordSet);
        try (TokenStream stream = englishAnalyzer.tokenStream("someField", new StringReader(phrase));){
            CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                if (cattr.length() > 0) {
                    String word = stream.getAttribute(CharTermAttribute.class).toString();
                    if (!isNumeric(word)) tokens.add(stream.getAttribute(CharTermAttribute.class).toString());
                }
            }
            stream.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokens;
    }

    public static boolean isNumeric(String str) 	{
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

}

