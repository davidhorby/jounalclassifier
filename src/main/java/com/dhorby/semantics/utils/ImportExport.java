package com.dhorby.semantics.utils;

import com.dhorby.semantics.model.DataValue;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
public class ImportExport {

    private static final Logger LOG = LoggerFactory.getLogger(ImportExport.class);

    private Analyzer analyzer;


    public int writeTSV(URL inputURL, File outputFile, Function<String, DataValue> mappingFunction, CharArraySet stopSet) throws IOException {

        List<DataValue> dataFromTsv = new ArrayList<DataValue>();

        File inputFile = new File(inputURL.getFile());
        InputStream is = new FileInputStream(inputFile);

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
             BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            dataFromTsv =br.lines().map(mappingFunction).collect(Collectors.toList());
            for (DataValue dataValue : dataFromTsv) {
                List<String> valueStr = LuceneUtils.tokenizePhrase(dataValue.getValue(), stopSet);
                valueStr.stream().filter(word -> word.length() > 20).forEach((word) -> {try {out.write(word); out.newLine();} catch(Exception e) {}});
            }
        }
        return dataFromTsv.size();
    }


    public void convertTSV(URL inputURL, File directory, Function<String, DataValue> mappingFunction, CharArraySet stopSet) throws IOException {

        List<DataValue> dataFromTsv = new ArrayList<DataValue>();

        File inputFile = new File(inputURL.getFile());
        InputStream is = new FileInputStream(inputFile);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            dataFromTsv =br.lines().map(mappingFunction).collect(Collectors.toList());
            int count=0;
            String carrotStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><searchresult><query>BMC</query>";
            File opFile = new File(directory,"structuralbiology.xml");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(opFile), "UTF-8"));
            out.write(carrotStr);


            for (DataValue dataValue : dataFromTsv) {
                //String opStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><article><category><![CDATA[" + dataValue.getCategory() + "]]></category><articleid>" + dataValue.getKey() + "</articleid><abstract><![CDATA[" + dataValue.getValue() + "]]></abstract></article>";
                //String opStr = "<add><doc><field name=\"category\"><![CDATA[" + dataValue.getCategory() + "]]></field><field name=\"id\">" + dataValue.getKey() + "</field><field name=\"abstract_s\"><![CDATA[" + dataValue.getValue() + "]]></field></doc></add>";
                if (dataValue.getCategory().contains("Structural Biology")) {
                    carrotStr = "<document id=\"" + dataValue.getKey() + "\">";
                    carrotStr = carrotStr + "<url>http://www.biomedcentral.com</url>";

                    carrotStr = carrotStr + "<snippet><![CDATA[";
                    //String strIn = stripNonValidXMLCharacters(removeStopWords(dataValue.getValue().replace("background", ""), stopSet));
                    String strIn = stripNonValidXMLCharacters(dataValue.getValue().replace("Background", ""));
                    carrotStr = carrotStr + strIn;
                    carrotStr = carrotStr + "]]></snippet>";
                    carrotStr = carrotStr + "</document>";
                    out.write(carrotStr);


                    if (count++ > 200) break;
                }
                //List<String> valueStr = LuceneUtils.tokenizePhrase(dataValue.getValue(), stopSet);
                //valueStr.stream().filter(word -> word.length() > 20).forEach((word) -> {try {out.write(word); out.newLine();} catch(Exception e) {}});
            }
            carrotStr =  "</searchresult>";
            out.write(carrotStr);

            out.close();

        }
    }

    public String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                    (current == 0xA) ||
                    (current == 0xD) ||
                    ((current >= 0x20) && (current <= 0xD7FF)) ||
                    ((current >= 0xE000) && (current <= 0xFFFD)) ||
                    ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

    public String removeStopWords(String stringVal, CharArraySet stopWordSet) {
        String retval = "";
        try {
            analyzer = new StandardAnalyzer(stopWordSet);
            //analyzer = new EnglishAnalyzer(Version.LUCENE_4_9, stopWordSet);
            TokenStream ts = analyzer.tokenStream("text", new StringReader(stringVal));
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                if (termAtt.length() > 0) {
                    String word = ts.getAttribute(CharTermAttribute.class).toString();
                    if (!isNumeric(word)) retval = retval + " " + word;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }

    public static boolean isNumeric(String str) 	{
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static void unzip(File sourceFile, String destFileName){
        try {
            ZipFile zipFile = new ZipFile(sourceFile);
            zipFile.extractAll(destFileName);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public static List<Object> readTSV(File inputFile, Function<String, Optional<Object>> mappingFunction) throws IOException {

        InputStream is = new FileInputStream(inputFile);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            return br.lines().map(mappingFunction).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        }
    }

    public static List<String> readFile(File inputFile) throws IOException {

        InputStream is = new FileInputStream(inputFile);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            return br.lines().collect(Collectors.toList());
        }
    }

}


