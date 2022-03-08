package sy.core.spelling;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import org.apache.commons.lang3.StringUtils;
import sy.util.Entry;
import sy.util.Segment;
import util.CollectionUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sy
 * @date 2022/2/28 21:05
 */
public class Tokenizer {

    public Tokenizer(String wordFreqPath, Map<String, Integer> customWordFreq, Map<String, String> customConfusion) {
        addCustomDictionary(wordFreqPath, customWordFreq, customConfusion);
    }

    private void addCustomDictionary(String wordFreqPath, Map<String, Integer> customWordFreq, Map<String, String> customConfusion) {
        try(Stream<String> stream = Files.lines(Paths.get(wordFreqPath))) {
            stream.forEach(line -> {
                String[] wordInfo = line.trim().split(" ");
                CustomDictionary.add(wordInfo[0], wordInfo[2] + " " + wordInfo[1]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        customWordFreq.keySet().forEach(k -> CustomDictionary.add(k));
        customConfusion.forEach((k, v) -> {CustomDictionary.add(k); CustomDictionary.add(v);});
    }

    public List<Entry> tokenize(String text, boolean posi) {
        List<Entry> entryList = Segment.hanlpSegment(text, posi);
        return entryList;
    }

    public List<String> split2ShortSent(String text) {
        return Segment.splitSentence(text);
    }

    public List<String> whiteSpaceTokenizer(String text) {
        List<String> tokens = CollectionUtil.newArrayList();
        List<String> sents = split2ShortSent(text);
        sents.forEach(sent -> tokens.addAll(Arrays.asList(sent.split(""))));
        return tokens;
    }

    public List<Entry> tokenizer(String text) {
        return tokenize(text, true);
    }

    public List<List<String>> cutSentence(String sentence) {
        return cutSentence(sentence, "word", false);
    }

    public List<List<String>> cutSentence(String sentence, String cutType) {
        return cutSentence(sentence, cutType, false);
    }

    public List<List<String>> cutSentence(String sentence, String cutType, boolean pos) {
        List<List<String>> wordPosList = CollectionUtil.newArrayList();
        List<String> wordsList = CollectionUtil.newArrayList();
        List<String> posList = CollectionUtil.newArrayList();
        if (pos) {
            if (StringUtils.equals(cutType, "word")) {
                List<Entry> entryList = tokenizer(sentence);
                wordsList.addAll(entryList.stream().map(entry -> entry.getWord()).collect(Collectors.toList()));
                posList.addAll(entryList.stream().map(entry -> entry.getPos()).collect(Collectors.toList()));
            } else if (StringUtils.equals(cutType, "char")) {
                wordsList = Arrays.asList(sentence.split(""));
                posList.addAll(wordsList.stream().map(word -> {
                    List<Entry> entry = tokenizer(word);
                    return entry.get(0).getPos();
                }).collect(Collectors.toList()));
            }
            wordPosList.add(wordsList);
            wordPosList.add(posList);
        } else {
            if(StringUtils.equals(cutType, "word")) {
                wordsList.addAll(tokenizer(sentence).stream().map(entry -> entry.getWord()).collect(Collectors.toList()));
            } else if(StringUtils.equals(cutType, "char")) {
                wordsList.addAll(Arrays.asList(sentence.split("")));
            }
            wordPosList.add(wordsList);
            wordPosList.add(posList);
        }
        return wordPosList;
    }

}

