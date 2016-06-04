package com.dhorby.semantics.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StopWordUtils {

    public static Optional<String> tokenize(String stringVal, Analyzer analyzer) {
        StringBuffer retval = new StringBuffer();
        try (TokenStream ts = analyzer.tokenStream("text", new StringReader(stringVal))) {
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                if (termAtt.length() > 0) {
                    String word = ts.getAttribute(CharTermAttribute.class).toString();
                    if (!isNumeric(word)) retval.append(", " + word);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Optional.empty();
        }
        return Optional.of(retval.toString().trim());
    }

    public static Optional<List<String>> tokenizeList(String stringVal, Analyzer analyzer) {
        List<String> retList = new ArrayList<String>();
        try (TokenStream ts = analyzer.tokenStream("text", new StringReader(stringVal))) {
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            int i=0;
            while (ts.incrementToken()) {
                if (termAtt.length() > 0) {
                    String word = ts.getAttribute(CharTermAttribute.class).toString();
                    if (!isNumeric(word)) retList.add(i++, word);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Optional.empty();
        }
        return Optional.of(retList);
    }


    public static boolean isNumeric(String str) 	{
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
