package sy.core.spelling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.CollectionUtil;
import util.PropertiesReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sy
 * @date 2022/2/25 22:01
 */
public class LoadDetectorDict {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadDetectorDict.class);

    public static Map<String, Integer> wordFreq;
    public static Map<String, String> customConfusion;
    public static Map<String, Integer> customWordFreq;
    public static Map<String, Integer> personNames;
    public static Map<String, Integer> placeNames;
    public static Map<String, Integer> stopWords;

    public static void initDectectorDict() {
        LOGGER.info("dectector dict init...");
        //word frequency dict
        wordFreq = loadWordFreqDict(LoadDetectorDict.class.getClassLoader().getResource(PropertiesReader.get("word_freq_path")).getPath().replaceFirst("/", ""));
        //custom confusion dict
        customConfusion = getCustomConfusionDict(LoadDetectorDict.class.getClassLoader().getResource(PropertiesReader.get("custom_confusion_path")).getPath().replaceFirst("/", ""));
        //custom word segmentation dictionary
        customWordFreq = loadWordFreqDict(LoadDetectorDict.class.getClassLoader().getResource(PropertiesReader.get("custom_word_freq_path")).getPath().replaceFirst("/", ""));
        personNames = loadWordFreqDict(LoadDetectorDict.class.getClassLoader().getResource(PropertiesReader.get("person_name_path")).getPath().replaceFirst("/", ""));
        placeNames = loadWordFreqDict(LoadDetectorDict.class.getClassLoader().getResource(PropertiesReader.get("place_name_path")).getPath().replaceFirst("/", ""));
        stopWords = loadWordFreqDict(LoadDetectorDict.class.getClassLoader().getResource(PropertiesReader.get("stopwords_path")).getPath().replaceFirst("/", ""));

        //merge dict
        customWordFreq.putAll(personNames);
        customWordFreq.putAll(placeNames);
        customWordFreq.putAll(stopWords);
        wordFreq.putAll(customWordFreq);
    }

    /**
     * load word freq dict
     * @param filePath
     * @return
     */
    public static Map<String, Integer> loadWordFreqDict(String filePath) {
        Map<String, Integer> wordFreq = CollectionUtil.newHashMap();
        if(!Files.exists(Paths.get(filePath))) {
            LOGGER.warn("file not found -> " + filePath);
        } else {
            LOGGER.info("file load -> " + filePath);
            try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
                wordFreq = stream.filter(line -> !line.trim().startsWith("#"))
                        .filter(line -> line.trim().split(" ").length >= 1)
                        .map(line -> {
                            String[] info = line.trim().split(" ");
                            String word = info[0];
                            Integer freq;
                            if(info.length > 1) {
                                freq = Integer.valueOf(info[1].trim());
                            } else {
                                freq = 1;
                            }
                            return new SpellingEntry<>(word, freq);
                        }).collect(Collectors.toMap(SpellingEntry::getWord, SpellingEntry::getFreq, (oldValue, newValue) -> newValue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return wordFreq;
    }

    /**
     * load custom confusion dict
     * @param filePath
     * @return
     */
    public static Map<String, String> getCustomConfusionDict(String filePath) {
        Map<String, String> customConfusion = CollectionUtil.newHashMap();
        if(!Files.exists(Paths.get(filePath))) {
            LOGGER.warn("file not found -> " + filePath);
        } else {
            LOGGER.info("file load -> " + filePath);
            try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
                customConfusion = stream.filter(line -> !line.trim().startsWith("#"))
                        .filter(line -> line.trim().split(" ").length >= 2)
                        .map(line -> {
                            String[] info = line.trim().split(" ");
                            String variant = info[0];
                            String origin = info[1];
                            Integer freq;
                            if(info.length > 2) {
                                freq = Integer.valueOf(info[2]);
                            } else {
                                freq = 1;
                            }
                            wordFreq.put(origin, freq);
                            return new SpellingEntry<>(variant, origin);
                        }).collect(Collectors.toMap(SpellingEntry::getWord, SpellingEntry::getFreq, (oldValue, newValue) -> newValue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return customConfusion;
    }

    /**
     * set custom confusion dict
     * @param filePath
     */
    public static void setCustomConfusionDict(String filePath) {
        if(null == customConfusion){
            customConfusion = getCustomConfusionDict(filePath);
        } else {
            Map<String, String> customDict = getCustomConfusionDict(filePath);
            customConfusion.putAll(customDict);
        }
    }

}
