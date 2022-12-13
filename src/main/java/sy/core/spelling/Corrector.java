package sy.core.spelling;

import com.alibaba.fastjson2.JSON;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.StringUtils;
import sy.util.Util;
import util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sy
 * @date 2022/2/25 20:58
 */
public class Corrector extends Detector{

    public boolean initializedCorrector = false;
    public HanyuPinyinOutputFormat format;

    public Corrector() {
        if(!this.initializedCorrector) {
            this.initCorrector();
        }
    }

    public void initCorrector() {
        LoadCorrectorDict.initCorrectorDict();
        initPinYin();
        this.initializedCorrector = true;
    }

    public void initPinYin() {
        this.format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * get char of the same pinyin
     * @param chr
     * @return
     */
    public Set<String> getSamePinYin(String chr) {
        return LoadCorrectorDict.samePinYin.getOrDefault(chr, Collections.EMPTY_SET);
    }

    /**
     * get char of the same stroke
     * @param chr
     * @return
     */
    public Set<String> getSameStroke(String chr) {
        return LoadCorrectorDict.sameStroke.getOrDefault(chr, Collections.EMPTY_SET);
    }

    /**
     * get the part of the word sequence that belongs to common words
     * @param words
     * @return
     */
    public Set<String> known(Set<String> words) {
        Set<String> commonWordsSet = words.stream().filter(word -> LoadDetectorDict.wordFreq.containsKey(word)).collect(Collectors.toSet());
        return commonWordsSet;
    }

    private Set<String> confusionCharSet(String chr) {
        Set<String> confusionSet = CollectionUtil.newHashset();
        Set<String> samePinYinSet = this.getSamePinYin(chr);
        Set<String> sameStrokeSet = this.getSameStroke(chr);
        if(samePinYinSet.size() > 0) {
            confusionSet.addAll(samePinYinSet);
        }
        if(sameStrokeSet.size() > 0) {
            confusionSet.addAll(sameStrokeSet);
        }
        return confusionSet;
    }

    /**
     * same pinyin
     * @param word
     * @return
     */
    private Set<String> confusionWordSet(String word) {
        Set<String> candiateWords = this.known(SpellingUtils.editDisWord(word, LoadCorrectorDict.cnCharSet));

        Set<String> confusionSet = candiateWords.stream().filter(candiateWord -> {
            try {
                return StringUtils.equals(
                        PinyinHelper.toHanYuPinyinString(candiateWord, format, "", true), PinyinHelper.toHanYuPinyinString(word, format, "", true)
                );
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
            return false;
        }).collect(Collectors.toSet());

        return confusionSet;
    }

    private Set<String> confusionCustomSet(String word) {
        Set<String> confusionWordSet = CollectionUtil.newHashset();
        if(LoadDetectorDict.customConfusion.containsKey(word)) {
            confusionWordSet.add(LoadDetectorDict.customConfusion.get(word));
        }
        return confusionWordSet;
    }

    /**
     * generate an error-correction candidate set
     * @param word
     * @return
     */
    public List<String> generateItems(String word) {
        return generateItems(word, 1);
    }

    /**
     * generate an error-correction candidate set
     * @param word
     * @param fragment
     * @return
     */
    public List<String> generateItems(String word, int fragment) {
        // one word
        List<String> candidatesOne = CollectionUtil.newArrayList();
        // two words
        List<String> candidatesTwo = CollectionUtil.newArrayList();
        // more than two words
        List<String> candidatesThree = CollectionUtil.newArrayList();

        // same pinyin word
        candidatesOne.addAll(confusionWordSet(word));
        // custom confusion word
        candidatesOne.addAll(confusionCustomSet(word));

        // same pinying char
        int wordLen = word.length();
        List<String> confusion;
        switch (wordLen) {
            case 1:
                // same one char pinyin
                confusion = confusionCharSet(word.substring(0,1)).stream().filter(w -> StringUtils.isNotBlank(w)).collect(Collectors.toList());
                candidatesOne.addAll(confusion);
                break;
            case 2:
                // same first char pinyin
                confusion = confusionCharSet(word.substring(0,1)).stream().filter(w -> StringUtils.isNotBlank(w)).map(w -> w + word.substring(1)).collect(Collectors.toList());
                candidatesTwo.addAll(confusion);
                // same last char pinyin
                confusion = confusionCharSet(word.substring(word.length() - 1)).stream().filter(w -> StringUtils.isNotBlank(w)).map(w ->  word.substring(0, wordLen - 1) + w).collect(Collectors.toList());
                candidatesTwo.addAll(confusion);
                break;
            default:
                // same mid char pinyin
                confusion = confusionCharSet(word.substring(0, 1)).stream().map(w -> word.substring(0, 1) + w + word.substring(2)).collect(Collectors.toList());
                candidatesThree.addAll(confusion);
                // same first word pinyin
                confusion = confusionWordSet(word.substring(0, wordLen - 1)).stream().map(w -> w + word.substring(wordLen - 1)).collect(Collectors.toList());
                candidatesThree.addAll(confusion);
                // same last word pinyin
                confusion = confusionWordSet(word.substring(1)).stream().map(w -> word.substring(0, 1) + w).collect(Collectors.toList());
                candidatesThree.addAll(confusion);
                break;
        }
        // add all confusion word list
        List<String> allConfusionList = CollectionUtil.newArrayList();
        allConfusionList.addAll(candidatesOne);
        allConfusionList.addAll(candidatesTwo);
        allConfusionList.addAll(candidatesThree);
        Set<String> allConfusionSet = CollectionUtil.newHashset(allConfusionList);

        List<String> confusionWordList = allConfusionSet.stream().filter(candidateWord -> Util.isChinese(candidateWord)).collect(Collectors.toList());
        List<String> confusionListSorted = confusionWordList.stream().sorted(Comparator.comparing(w -> {if(LoadDetectorDict.wordFreq.containsKey(w)) {return LoadDetectorDict.wordFreq.get(w);} else {return 1;}}).reversed()).collect(Collectors.toList());

        if(fragment == 1) {
            return confusionListSorted.subList(0, confusionWordList.size());
        } else {
            return confusionListSorted.subList(0, confusionWordList.size() / fragment + 1);
        }

    }

    /**
     * correct word errors through language models
     * @param curItem
     * @param candidatesList
     * @param beforeSent
     * @param afterSent
     * @return
     */
    public List<String> getLmCorrectItem(String curItem, List<String> candidatesList, String beforeSent, String afterSent) {
        return getLmCorrectItem(curItem, candidatesList, beforeSent, afterSent, 57, "char");
    }

    public List<String> getLmCorrectItem(String curItem, List<String> candidatesList, String beforeSent, String afterSent, int threshold) {
        return getLmCorrectItem(curItem, candidatesList, beforeSent, afterSent, threshold, "char");
    }

    /**
     * correct word errors through language models
     * @param curItem current word
     * @param candidatesList candidate words
     * @param beforeSent the first half of the sentence
     * @param afterSent the second half of the sentence
     * @param threshold PPL threshold. If the value is greater than PPL after the original word is replaced, it is considered an error
     * @param cutType word segmentation type
     * @return
     */
    public List<String> getLmCorrectItem(String curItem, List<String> candidatesList, String beforeSent, String afterSent, int threshold, String cutType) {

//        String result = curItem;
        if(!candidatesList.contains(curItem)) {
            candidatesList.add(curItem);
        }
        Map<String, Double> pplScoreMap = candidatesList.stream().map(candidateWord -> {
                    double score = this.pplScore(this.tokenizer.cutSentence(beforeSent + candidateWord + afterSent, cutType).get(0));
                return new SpellingEntry<>(candidateWord, score);})
                .collect(Collectors.toMap(SpellingEntry::getWord, SpellingEntry::getFreq, (oldValue, newValue) -> newValue));

        Map<String, Double> sortedPplScoresMap = pplScoreMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue,LinkedHashMap::new));

        // increase the correct word correction range, reduce the error correction
        List<String> topItems = CollectionUtil.newArrayList();
        double topScore = 0.0;
        int i = 0;
        for(Map.Entry<String, Double> entry : sortedPplScoresMap.entrySet()) {
            String word = entry.getKey();
            double score = entry.getValue();
            if(0 == i) {
                topScore = score;
                topItems.add(word);
                // the range is corrected by the threshold
            } else if(score < (topScore + threshold)) {
                topItems.add(word);
            } else {
                break;
            }
            i++;
        }

//        if(!topItems.contains(curItem)) {
//            result = topItems.get(0);
//        }

      return topItems;
    }

    /**
     * correct sentence
     * @param text
     * @return
     */
    public String correct(String text) {
        return correct(text, 1, 57);
    }

    /**
     * correct sentence
     * @param text
     * @param numFragment number of segments in the error-correction candidate set, 1 / (num_fragment + 1)
     * @param threshold PPL threshold for language model error correction
     * @return
     */
    public String correct(String text, int numFragment, int threshold) {
        List<Map<String, Object>> mistakeInfList = CollectionUtil.newArrayList();
        // split sentence
        List<String> blocks = tokenizer.split2ShortSent(text);
        int startIdx = 0;
        for(String sent : blocks) {
            List<List<Object>> maybeErrors = detectShort(sent, startIdx);
            for(List<Object> error : maybeErrors) {
                String curItem = (String)error.get(0);
                int beginIdx = (int)error.get(1);
                int endIdx = (int)error.get(2);
                String errType = (String)error.get(3);
                // error correction, one by one processing
                String beforeSent = sent.substring(0, beginIdx - startIdx);
                String afterSent = sent.substring(endIdx - startIdx);

                // for the word specified in the confusion set, take the result directly
                if(StringUtils.equals(errType, ErrorTypeEnumMap.errorTypeEnumCategory.get(ErrorTypeEnum.CONFUSION))) {
                    String correctedItem = "";
                    if(LoadDetectorDict.customConfusion.containsKey(curItem)) {
                        correctedItem = LoadDetectorDict.customConfusion.get(curItem);
                    }
                    // output
                    if(!StringUtils.equals(correctedItem, curItem) && StringUtils.isNotBlank(correctedItem)) {
                        Map<String, Object> misDict = CollectionUtil.newHashMap();
                        misDict.put("type", "拼写错误");
                        misDict.put("error", curItem);
                        misDict.put("startIdx", beginIdx);
                        misDict.put("endIdx", endIdx);
                        misDict.put("correct", correctedItem);
                        mistakeInfList.add(misDict);
                    }
                } else {
                // get all the possible correct words
                    List<String> candidates = generateItems(curItem, numFragment);
                    if(!Optional.ofNullable(candidates).isPresent() && (candidates.size() == 0)) {
                        continue;
                    }
                    List<String> correctedItem = getLmCorrectItem(curItem, candidates, beforeSent, afterSent, threshold);
                    // output
                    if((correctedItem.size() > 0) && !StringUtils.equals(correctedItem.get(0), curItem)) {
                        Map<String, Object> misDict = CollectionUtil.newHashMap();
                        misDict.put("type", "拼写错误");
                        misDict.put("error", curItem);
                        misDict.put("startIdx", beginIdx);
                        misDict.put("endIdx", endIdx);
                        misDict.put("correct", correctedItem);
                        mistakeInfList.add(misDict);
                    }
                }

            }

            startIdx += sent.length();
        }

        mistakeInfList = mistakeInfList.stream().sorted(Comparator.comparingInt(e -> (int) e.get("startIdx"))).collect(Collectors.toList());
        return JSON.toJSONString(mistakeInfList);
    }

}

