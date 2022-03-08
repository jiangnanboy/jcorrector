package sy.core.ngram;

import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.io.ArpaLmReader;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.util.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sy
 * @date 2022/2/2 21:06
 */
public class NGramModel {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NGramModel.class);

    public static void main(String[] args) {
        String outputFilePath = "D:/data/data_char.bin";

        String inputFilePath = "D:/data/data_char.txt";
        int nGram = 3;
        makeLM(nGram, inputFilePath, outputFilePath);

//        ArrayEncodedProbBackoffLm<String> lm = getLm(false, outputFilePath);
//        System.out.println(lm.scoreSentence(Stream.of("我们是一家人".split("")).collect(Collectors.toList())));
//        System.out.println(lm.scoreSentence(Stream.of("我们是一个人".split("")).collect(Collectors.toList())));
    }

    /**
     * load language model
     * @param compress
     * @param filePath
     * @return
     */
    public static ArrayEncodedProbBackoffLm<String> getLm(boolean compress, String filePath) {
        if(!Files.exists(Paths.get(filePath))) {
            LOGGER.warn("model file not found " + filePath);
            return null;
        } else {
            LOGGER.info("load model from -> " + filePath);
            File lmFile = new File(filePath);
            ConfigOptions configOptions = new ConfigOptions();
            configOptions.unknownWordLogProb = 0.0f;
            ArrayEncodedProbBackoffLm<String> lm = (ArrayEncodedProbBackoffLm)LmReaders.readArrayEncodedLmFromArpa(filePath, compress, new StringWordIndexer(), configOptions, Integer.MAX_VALUE);
            return lm;
        }
    }

    /**
     * train n-gram language model
     * @param nGram
     * @param inputFilePath
     * @param outputFilePath
     */
    public static void makeLM(int nGram, String inputFilePath, String outputFilePath) {
        if(Files.exists(Paths.get(inputFilePath)) && Files.exists(Paths.get(outputFilePath))) {
            LOGGER.info("input file path : " + inputFilePath + "\n" + "outout file : " + outputFilePath);
            trianLM(nGram, inputFilePath, outputFilePath);
        } else if(Files.exists(Paths.get(inputFilePath)) && !Files.exists(Paths.get(outputFilePath))){
            LOGGER.info("outputFilePath not found -> " + outputFilePath + " , it will be created!");
            try {
                Files.createFile(Paths.get(outputFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            trianLM(nGram, inputFilePath, outputFilePath);
        } else {
            LOGGER.warn("file not found, please check file path!");
        }
    }

    /**
     * train n-gram language model
     * @param nGram
     * @param inputFilePath
     * @param outputFilePath
     */
    public static void trianLM(int nGram, String inputFilePath, String outputFilePath) {
        Logger.setGlobalLogger(new Logger.SystemLogger(System.out, System.err));
        Logger.startTrack("Reading text files " + inputFilePath + " and writing to " + outputFilePath);
        StringWordIndexer wordIndexer = new StringWordIndexer();
        wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
        wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
        wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);
        LmReaders.createKneserNeyLmFromTextFiles(Stream.of(inputFilePath).collect(Collectors.toList()), wordIndexer, nGram, new File(outputFilePath), new ConfigOptions());
        Logger.endTrack();
    }

}
