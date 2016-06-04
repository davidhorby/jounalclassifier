package com.dhorby.semantics.utils;

import com.dhorby.semantics.model.DataValue;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
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


public class ConvertToSeq {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertToSeq.class);

    private Analyzer analyzer;

    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.err.println("Arguments: [input tsv file] [output sequence file]");
            System.err.println("  -c, --clean        clean the training file (outputs as <file-name>.clean");
            return;
        }
        String inputFileName = args[0];
        String outputDirName = args[1];

        ConvertToSeq convertToSeq = new ConvertToSeq();
        convertToSeq.convert(inputFileName, outputDirName);
    }

    public int convert (String inputFileName, String outputDirName) throws IOException {
        return convert(new File(inputFileName), outputDirName, TSVMap.mapBMCDataSet);
    }

    public int convert(File inputFile, String outputDirName, Function<String, Optional<Object>> mapFunction)  {

        Configuration configuration = new Configuration();

        int count = 0;
        try (Writer writer = new SequenceFile.Writer(FileSystem.get(configuration), configuration, new Path(outputDirName + "/chunk-0"),
                Text.class, Text.class)) {
            List<DataValue> dataFromTsv =  ImportExport.readTSV(inputFile, mapFunction).stream().filter(e -> e instanceof DataValue).map(DataValue.class::cast).collect(Collectors.toList());
            dataFromTsv.forEach(dataValue -> dataValue.setValue(StopWordUtils.tokenize(dataValue.getValue(), englishAnalyzer).get()));
            List<DataValue> dataFiltered = dataFromTsv.stream().filter(dataValue -> dataValue.getValue().length() > 20).collect(Collectors.toList());

            dataFiltered.forEach(dataValue -> dataValue.setKey("/" + dataValue.getCategory() + "/" + dataValue.getKey()));
            for (DataValue dataValue : dataFiltered) {
                writer.append(new Text(dataValue.getKey()), new Text(dataValue.getValue()));
                LOG.info(dataValue.getKey() + ":" + dataValue.getValue());
                count++;
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        LOG.info("Wrote " + count + " entries.");
        return count;
    }

    public int clean(URL url, String outputFileName, Function<String, DataValue> mapFunction) throws IOException {

        File outFile = new File(outputFileName);
        return  this.writeTSV(url, outFile, mapFunction);

    }

    List<String> tokenizePhrase(String phrase, List<String> stopList) {
        List<String> tokens = new ArrayList<String>();
        CharArraySet stopWordSet = new CharArraySet(stopList, false);
        englishAnalyzer = new EnglishAnalyzer(stopWordSet);
        try (TokenStream stream = englishAnalyzer.tokenStream("someField", new StringReader(phrase));){
            CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                if (cattr.length() > 0) {
                    String word = stream.getAttribute(CharTermAttribute.class).toString();
                    if (!StopWordUtils.isNumeric(word)) tokens.add(stream.getAttribute(CharTermAttribute.class).toString());
                }
            }
            stream.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokens;
    }


    public static int i =100000;


    private EnglishAnalyzer englishAnalyzer = new EnglishAnalyzer(StopWordLists.bmcStopSet);


    public int writeTSV(URL inputURL, File outputFile, Function<String, DataValue> mappingFunction) throws IOException {

        File inputFile = new File(inputURL.getFile());
        InputStream is = new FileInputStream(inputFile);

        FileOutputStream os = new FileOutputStream(outputFile);
        OutputStreamWriter osr = new OutputStreamWriter(os, "UTF-8");
        List<DataValue> dataFromTsv = new ArrayList<DataValue>();
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
             BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            dataFromTsv =br.lines().map(mappingFunction).collect(Collectors.toList());
            for (DataValue dataValue : dataFromTsv) {
                String valueStr = StopWordUtils.tokenize(dataValue.getValue(), englishAnalyzer).get();
                if (valueStr.length() > 20) {
                    //?out.write(dataValue.toTSV());
                    out.newLine();
                }
            }

        }
        return dataFromTsv.size();

    }
}




