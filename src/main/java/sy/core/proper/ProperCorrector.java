package sy.core.proper;

import com.alibaba.fastjson2.JSON;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.StringUtils;
import sy.core.spelling.SpellingUtils;
import sy.core.ngram.NgramUtil;
import sy.util.Segment;
import sy.util.Util;
import util.CollectionUtil;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sy
 * @date 2022/7/4 22:00
 */
public class ProperCorrector {

    HanyuPinyinOutputFormat format;
    public ProperCorrector(String properNamePath, String strokePath) {
        try {
            LoadDict.loadSetFile(properNamePath);
            LoadDict.loadDictFile(strokePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 取笔画
     * @param chr
     * @return
     */
    public String getStroke(String chr) {
        return LoadDict.strokeDict.getOrDefault(chr, "");
    }

    public String getPinyin(String chr) {
        try {
            return PinyinHelper.toHanYuPinyinString(chr, format, "", true);
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        return null;
    }

    public boolean isNearStrokeChr(String chr1, String chr2) {
        return isNearStrokeChr(chr1, chr2, 0.8);
    }

    /**
     * 判断两个字是否形似
     * @param chr1
     * @param chr2
     * @param strokeThreshold
     * @return
     */
    public boolean isNearStrokeChr(String chr1, String chr2, double strokeThreshold) {
        return getCharStrokeSimilarityScore(chr1, chr2) > strokeThreshold;
    }

    /**
     * 获取字符的字形相似度
     * @param chr1
     * @param chr2
     * @return
     */
    public double getCharStrokeSimilarityScore(String chr1, String chr2) {
        if(StringUtils.equals(chr1, chr2)) {
            return 1.0;
        }
        // 如果一个是中文，另一个不是，则为0
        if(Util.isChinese(chr1) != Util.isChinese(chr2)) {
            return 0.0;
        }
        if(!Util.isChinese(chr1)) {
            return 0.0;
        }
        String chrStroke1 = LoadDict.strokeDict.get(chr1);
        String chrStroke2 = LoadDict.strokeDict.get(chr2);
        double score = 0.0;
        // 相似度
        if(Optional.ofNullable(chrStroke1).isPresent() && Optional.ofNullable(chrStroke2).isPresent()) {
            score = 1.0 - SpellingUtils.editDistance(chrStroke1, chrStroke2);
        }
        return score;
    }

    /**
     * 计算两个词的字形相似度
     * @param word1
     * @param word2
     * @return
     */
    public double getWordStrokeSimilarityScore(String word1, String word2) {
        if(StringUtils.equals(word1, word2)) {
            return 1.0;
        }
        if(word1.length() != word2.length()) {
            return 0.0;
        }
        double totalScore = 0.0;
        for(int i=0; i<word1.length(); i++) {
            String chr1 = word1.substring(i, i+1);
            String chr2 = word2.substring(i, i+1);
            if(!isNearStrokeChr(chr1, chr2)) {
                return 0.0;
            }
            double chrSimScore = getCharStrokeSimilarityScore(chr1, chr2);
            totalScore += chrSimScore;
        }
        return totalScore / word1.length();
    }

    /**
     * 判断两个单字的拼音是否临近
     * @param chr1
     * @param chr2
     * @return
     */
    public boolean isNearPinyinChr(String chr1, String chr2) {
        String chrPinyin1 = getPinyin(chr1);
        String chrPinyin2 = getPinyin(chr2);
        if(StringUtils.isBlank(chrPinyin1) || StringUtils.isBlank(chrPinyin2)) {
            return false;
        }
        if(chrPinyin1.length() == chrPinyin2.length()) {
            return true;
        }
        Map<String, String> confuseDict = Map.of("l", "n",
                                                "zh", "z",
                                                "ch", "c",
                                                "sh", "s",
                                                "eng", "en",
                                                "ing", "in");
        for(Map.Entry<String, String> entry : confuseDict.entrySet()) {
            if(StringUtils.equals(chrPinyin1.replace(entry.getKey(), entry.getValue()), chrPinyin2.replace(entry.getKey(), entry.getValue()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取字符的拼音相似度
     * @param chr1
     * @param chr2
     * @return
     */
    public double getChrPinyinSimilarityScore(String chr1, String chr2) {
        if(StringUtils.equals(chr1, chr2)) {
            return 1.0;
        }
        // 如果一个是中文字符，另一个不是，为0
        if(Util.isChinese(chr1) != Util.isChinese(chr2)) {
            return 0.0;
        }
        if(!Util.isChinese(chr1)) {
            return 0.0;
        }
        String chrPinyin1 = getPinyin(chr1);
        String chrPinyin2 = getPinyin(chr2);
        // 相似度
        double score = 1.0 - SpellingUtils.editDistance(chrPinyin1, chrPinyin2);
        return score;
    }

    /**
     * 计算两个词的拼音相似度
     * @param word1
     * @param word2
     * @return
     */
    public double getWordPinyinSimilarityScore(String word1, String word2) {
        if(StringUtils.equals(word1, word2)) {
            return 1.0;
        }
        if(word1.length() != word2.length()) {
            return 0.0;
        }
        double totalScore = 0.0;
        for(int i=0; i<word1.length(); i++) {
            String chr1 = word1.substring(i, i+1);
            String chr2 = word2.substring(i, i+1);
            if(!isNearPinyinChr(chr1, chr2)) {
                return 0.0;
            }
            double chrSimScore = getChrPinyinSimilarityScore(chr1, chr2);
            totalScore += chrSimScore;
        }
        return totalScore / word1.length();
    }

    /**
     * 计算两个词的相似度
     * @param word1
     * @param word2
     * @return
     */
    public double getWordSimilarityScore(String word1, String word2) {
        return Math.max(
                getWordStrokeSimilarityScore(word1, word2),
                getWordPinyinSimilarityScore(word1, word2));
    }

    public String correct(String text) {
        return correct(text, 0, "char", 1234, 0.85, 4, 2);
    }

    /**
     * 专名纠错
     * @param text 待纠错的文本
     * @param startIdx 文本开始的索引，兼容correct方法
     * @param cutType 分词类型，'char' or 'word'
     * @param ngram 遍历句子的ngram
     * @param simThreshold 相似度得分阈值，超过该阈值才进行纠错
     * @param maxWordLength 专名词的最大长度为4
     * @param minWordLength 专名词的最小长度为2
     * @return
     */
    public String correct(String text, int startIdx, String cutType, int ngram, double simThreshold, int maxWordLength, int minWordLength) {
        List<Map<String, Object>> mistakeInfList = CollectionUtil.newArrayList();
        List<String> sents = Segment.splitSentence(text);
        int idx = 0;
        for(String sent: sents) {
            List<String> sentWords = null;
            if(StringUtils.equals(cutType, "char")) {
                sentWords = Arrays.asList(sent.split(""));
            } else if(StringUtils.equals(cutType, "word")) {
                sentWords = Segment.hanlpSegment(text).stream().map(entry -> entry.getWord()).collect(Collectors.toList());
            }
            if(Optional.ofNullable(sentWords).isPresent() && 0!=sentWords.size()) {
                List<String> ngramList = NgramUtil.nGrams(sentWords, ngram, "_");
                // 去重
                ngramList = ngramList.stream().map(term -> term.replace("_", "")).distinct().collect(Collectors.toList());
                // 词长度过滤
                ngramList = ngramList.stream().filter(term -> (term.length() >= minWordLength) && (term.length() <= maxWordLength)).collect(Collectors.toList());
                for(String curItem:ngramList) {
                    for(String name:LoadDict.properNames) {
                        if(this.getWordSimilarityScore(curItem, name) > simThreshold) {
                            if(!StringUtils.equals(curItem, name)) {
                                int curIdx = sent.indexOf(curItem);
                                Map<String, Object> misDict = CollectionUtil.newHashMap();
                                misDict.put("type", "专名错误");
                                misDict.put("error", curItem);
                                misDict.put("startIdx", idx + curIdx + startIdx);
                                misDict.put("endIdx", idx + curIdx + curItem.length() + startIdx);
                                misDict.put("correct", name);
                                mistakeInfList.add(misDict);
                            }
                        }
                    }
                }
            }
            idx += sent.length();
        }
        mistakeInfList = mistakeInfList.stream().sorted(Comparator.comparingInt(e -> (int) e.get("startIdx"))).collect(Collectors.toList());
        return JSON.toJSONString(mistakeInfList);
    }

}

