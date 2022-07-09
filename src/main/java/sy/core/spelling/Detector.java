package sy.core.spelling;

import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sy.core.ngram.NGramModel;
import sy.util.Util;
import util.CollectionUtil;
import util.PropertiesReader;
import sy.util.Entry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sy
 * @date 2022/2/25 22:57
 */
public class Detector {

    private static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

    public Tokenizer tokenizer = null;
    public ArrayEncodedProbBackoffLm<String> model = null;
    public boolean isCharErrorDetect = true;
    public boolean isWordErrorDetect = true;
    public boolean initializedDetector = false;

    public Detector() {
        if(!initializedDetector) {
            initDetector();
        }
    }

    /**
     * init detector
     */
    public void initDetector() {
        LOGGER.info("init detector...");
        LoadDetectorDict.initDectectorDict();
        tokenizer = new Tokenizer(Detector.class.getClassLoader().getResource(PropertiesReader.get("word_freq_path")).getPath().replaceFirst("/", ""),
                LoadDetectorDict.customWordFreq, LoadDetectorDict.customConfusion);
        model = NGramModel.getLm(false, PropertiesReader.get("language_model_path"));
        initializedDetector = true;
    }

    public void enableCharError(boolean isCharErrorDetect) {
        this.isCharErrorDetect = isCharErrorDetect;
    }

    public void enableWordError(boolean isWordErrorDetect) {
        this.isWordErrorDetect = isWordErrorDetect;
    }

    /**
     * language model score
     * @param wordsList
     * @return
     */
    public double ngramScore(List<String> wordsList) {
        return this.model.getLogProb(wordsList);
    }

    /**
     * perplexity score
     * @param wordsList
     * @return
     */
    public double pplScore(List<String> wordsList) {
        return this.model.scoreSentence(wordsList);
    }

    /**
     * get frequency from wordfreq dict
     * @param word
     * @return
     */
    public int wordFrequency(String word) {
        return LoadDetectorDict.wordFreq.get(word);
    }

    /**
     * update frequency
     * @param word
     * @param freq
     */
    public void setWordFrequency(String word, int freq) {
        LoadDetectorDict.wordFreq.put(word, freq);
    }

    /**
     * check the error set contains the error location ?
     * @param mayBeErr [ErrorWord, beginPos, endPos, errorType]
     * @param mayBeErrors list
     * @return
     */
    private static boolean checkContainError(List<Object> mayBeErr, List<List<Object>> mayBeErrors) {
        int errorWordIdx = 0;
        int beginIdx = 1;
        int endIdx = 2;
        for(List<Object> list : mayBeErrors) {
            if(((String)list.get(errorWordIdx)).contains((String)mayBeErr.get(errorWordIdx)) &&
                    (((int)mayBeErr.get(beginIdx)) >= ((int)list.get(beginIdx))) &&
                    (((int)mayBeErr.get(endIdx)) <= ((int)list.get(endIdx)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * add error
     * @param mayBeErr
     * @param mayBeErrors
     */
    private void addMayBeErrorItem(List<Object> mayBeErr, List<List<Object>> mayBeErrors) {
        if((!mayBeErrors.contains(mayBeErr)) && (!checkContainError(mayBeErr, mayBeErrors))) {
            mayBeErrors.add(mayBeErr);
        }
    }

    /**
     * take the position of the suspected error word by MAD(the mean absolute deviation)
     * @param scores INDArray
     * @returnse
     */
    private static List<Integer> getMaybeErrorIndex(INDArray scores) {
        return getMaybeErrorIndex(scores,0.6745f, 2);
    }

    /**
     * take the position of the suspected error word by MAD(the mean absolute deviation)
     * @param scores INDArray
     * @param ratio normal distribution parameter
     * @param threshold the smaller the threshold is, the more suspected error words are obtained
     * @return all suspected error words index
     */
    private static List<Integer> getMaybeErrorIndex(INDArray scores, double ratio, int threshold) {
        // median
        INDArray median = scores.median();
        // margin median
        INDArray marginMedian = Transforms.abs(scores.sub(median));
        // mad
        double med_abs_deviation = marginMedian.median().getDouble();
        INDArray yScore = marginMedian.mul(ratio).div(med_abs_deviation);
        INDArray maybeErrorBool = Transforms.and(yScore.gt(threshold), scores.lt(median));
        INDArray maybeErrorIndices = Nd4j.where(maybeErrorBool, null, null)[0];
        List<Integer> MaybeErrorIndexList = Arrays.stream(maybeErrorIndices.toIntVector()).boxed().collect(Collectors.toList());

        return MaybeErrorIndexList;
    }

    /**
     * take the position of the suspected error words，n standard deviations above or below the mean, belong normal condition
     * @param scores
     * @returnInteger
     */
    private static List<Integer> getMaybeErrorIndexByStddev(INDArray scores) {
        return getMaybeErrorIndexByStddev(scores,2);
    }

    /**
     * take the position of the suspected error words，n standard deviations above or below the mean, belong normal condition
     * @param scores
     * @param n
     * @return
     */
    private static List<Integer> getMaybeErrorIndexByStddev(INDArray scores, int n) {
        double std = scores.std(1).getDouble();
        double mean = scores.mean().getDouble();
        double downLimit = mean - n * std;
        double upperLimit = mean + n * std;
        INDArray maybeErrorBool = Transforms.or(scores.gt(upperLimit), scores.lt(downLimit));
        INDArray maybeErrorIndices = Nd4j.where(maybeErrorBool, null, null)[0];
        List<Integer> MaybeErrorIndexList = Arrays.stream(maybeErrorIndices.toIntVector()).boxed().collect(Collectors.toList());
        return MaybeErrorIndexList;
    }

    /**
     * filter the token ?
     * @param token
     * @return
     */
    public static boolean isFilterToken(String token) {
        boolean result = false;

        if(Util.isBlank(token.trim())) {
            result = true;
        } else if(Util.isNumber(token)) {
            result = true;
        } else if(Util.isEnglish(token.toLowerCase())) {
            result = true;
        } else if(!Util.isChinese(token)) {
            result = true;
        }

        return result;
    }

    /**
     * text detection
     * @param text
     * @return
     */
    public List<List<Object>> detect(String text) {
        List<List<Object>> mayBeErrors = CollectionUtil.newArrayList();
        if(Util.isBlank(text.trim())) {
            return mayBeErrors;
        }
        // full2half, upper2low
        text = Util.full2HalfChange(text).toLowerCase();
        // split long sent to short sents
        List<String> splitSentsList = this.tokenizer.split2ShortSent(text);

        int startIdx = 0;
        for(String sent: splitSentsList) {
            mayBeErrors.addAll(detectShort(sent, startIdx));
            startIdx += sent.length();
        }

        return mayBeErrors;
    }

    /**
     * text detection
     * @param sentence
     * @param startIdx
     * @return
     */
    public List<List<Object>> detectShort(String sentence, int startIdx) {
        List<List<Object>> maybeErrors = CollectionUtil.newArrayList();
        LoadDetectorDict.customConfusion.keySet().forEach(confuse -> {
            int idx = sentence.indexOf(confuse);
            if(idx > -1) {
                List<Object> mayBeErr = CollectionUtil.newArrayList();
                mayBeErr.add(confuse);
                mayBeErr.add(idx + startIdx);
                mayBeErr.add(idx + confuse.length() + startIdx);
                mayBeErr.add(ErrorTypeEnumMap.errorTypeEnumCategory.get(ErrorTypeEnum.CONFUSION));
                addMayBeErrorItem(mayBeErr, maybeErrors);
            }
        });

        if(isWordErrorDetect) {
            // segment
            List<Entry> tokens = this.tokenizer.tokenizer(sentence);
            // Unregistered words are added to the suspected error dictionary
            tokens.stream()
                    .filter(entry -> !isFilterToken(entry.getWord()))
                    .filter(entry -> !LoadDetectorDict.wordFreq.containsKey(entry.getWord()))
                    .forEach(entry -> {
                String token = entry.getWord();
                int beginIdx = entry.getOffset();
                int endIdx = beginIdx + token.length();
                List<Object> mayBeErr = CollectionUtil.newArrayList();
                mayBeErr.add(token);
                mayBeErr.add(beginIdx + startIdx);
                mayBeErr.add(endIdx + startIdx);
                mayBeErr.add(ErrorTypeEnumMap.errorTypeEnumCategory.get(ErrorTypeEnum.WORD));
                addMayBeErrorItem(mayBeErr, maybeErrors);
            });
        }

        try {
        // The language model detects suspected error words
        if(isCharErrorDetect) {
            List<List<Double>> ngramAvgScores = CollectionUtil.newArrayList();
            List<Integer> ngramNum = Stream.of(2, 3).collect(Collectors.toList());
            for(int ngram : ngramNum) {
                List<Double> scores = CollectionUtil.newArrayList();
                for(int i=0; i<(sentence.length() - ngram + 1); i++) {
                    List<String> word = Arrays.asList(sentence.substring(i, i + ngram).split(""));
                    double score = this.ngramScore(word);
                    scores.add(score);
                }
                if(scores.size()==0) {
                    continue;
                }
                for(int i=0; i<(ngram - 1); i++) {
                    scores.add(0, scores.get(0));
                    scores.add(scores.get(scores.size() - 1));
                }
                List<Double> avgScoresList = CollectionUtil.newArrayList();
                for(int i=0; i < sentence.length(); i++) {
                    double avgScore = scores.subList(i, i + ngram).stream().mapToDouble(score -> score.doubleValue()).average().getAsDouble();
                    avgScoresList.add(avgScore);
                }
                ngramAvgScores.add(avgScoresList);
            }
            if(ngramAvgScores.size() != 0) {
                // n-gram score
                double[][] ngramAvgDouble = new double[ngramAvgScores.size()][ngramAvgScores.get(0).size()];
                for(int i=0; i < ngramAvgScores.size(); i++) {
                    List<Double> ngramAvgList = ngramAvgScores.get(i);
                    for(int j=0; j < ngramAvgList.size(); j++) {
                        ngramAvgDouble[i][j] = ngramAvgList.get(j);
                    }
                }
                INDArray indArray = Nd4j.create(ngramAvgDouble);
                indArray = indArray.mean(0);
                List<Integer> maybeErrorIndex = getMaybeErrorIndex(indArray);
                // error word info
                for(Integer idx : maybeErrorIndex) {
                    String token = sentence.substring(idx, idx + 1);
                    if(isFilterToken(token)) {
                        continue;
                    }
                    if(LoadDetectorDict.stopWords.containsKey(token)) {
                        continue;
                    }
                    List<Object> mayBeErr = CollectionUtil.newArrayList();
                    mayBeErr.add(token);
                    mayBeErr.add(idx + startIdx);
                    mayBeErr.add(idx + startIdx + 1);
                    mayBeErr.add(ErrorTypeEnumMap.errorTypeEnumCategory.get(ErrorTypeEnum.CHR));
                    addMayBeErrorItem(mayBeErr, maybeErrors);
                }

            }
        }
        } catch (Exception e) {
            LOGGER.warn("index error or detect error: " + sentence + "\n" + e);
        }

        return maybeErrors.stream().sorted(Comparator.comparing(result -> (int)result.get(1))).collect(Collectors.toList());
    }

}
