package sy.core.ngram;

import util.CollectionUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author sy
 * @date 2022/7/5 19:15
 */
public class NgramUtil {

    /**
     * @param words a list of words,  e.g., ["I", "am", "Denny"]
     * @return a list of unigram
     */
    public static List<String> uniGrams(List<String> words) {
        return words;
    }

    public static List<String> biGrams(List<String> words, String joinString) {
        return biGrams(words, joinString, 0);
    }

    /**
     * @param words a list of words,  e.g., ["I", "am", "Denny"]
     * @param joinString
     * @param skip
     * @return a list of bigram, e.g., ["I_am", "am_Denny"]
     */
    public static List<String> biGrams(List<String> words, String joinString, int skip) {
        int wordsLen = words.size();

        if( wordsLen > 1) {
            List<String> lst = CollectionUtil.newArrayList();
            IntStream.range(0, wordsLen - 1).boxed().forEach(i -> {
                IntStream.range(1, skip + 2).boxed().filter(k -> (i + k) < wordsLen)
                .forEach(k -> lst.add(String.join(joinString, words.get(i), words.get(i + k))));
            });
            return lst;
        } else {
            // set it as unigram
            return uniGrams(words);
        }
    }

    public static List<String> triGrams(List<String> words, String joinString) {
        return triGrams(words, joinString, 0);
    }

    /**
     * @param words a list of words, e.g., ["I", "am", "Denny"]
     * @param joinString
     * @param skip
     * @return a list of trigram, e.g., ["I_am_Denny"]
     */
    public static List<String> triGrams(List<String> words, String joinString, int skip) {
        int wordsLen = words.size();
        if(wordsLen > 2) {
            List<String> lst = CollectionUtil.newArrayList();
            IntStream.range(0, wordsLen - 2).boxed().forEach(i ->
                    IntStream.range(1, skip + 2).boxed().forEach(k1 ->
                            IntStream.range(1, skip + 2).boxed().filter(k2 -> (i + k1 + k2) < wordsLen).forEach(k2 ->
                                    lst.add(String.join(joinString, words.get(i), words.get(i + k1), words.get(i + k1 + k2)))
                            )));
            return lst;
        } else {
            // set it as bigram
            return biGrams(words, joinString, skip);
        }
    }

    /**
     * @param words a list of words, e.g., ["I", "am", "Denny", "boy"]
     * @param joinString
     * @return a list of trigram, e.g., ["I_am_Denny_boy"]
     */
    public static List<String> fourGrams(List<String> words, String joinString) {
        int wordsLen = words.size();
        if(wordsLen > 3) {
            List<String> lst = CollectionUtil.newArrayList();
            IntStream.range(0, wordsLen - 3).boxed().forEach(i ->
                    lst.add(String.join(joinString, words.get(i), words.get(i + 1), words.get(i + 2), words.get(i + 3))));
            return lst;
        } else {
            // set it as trigram
            return triGrams(words, joinString);
        }
    }

    public static List<String> uniTerms(List<String> words) {
        return uniGrams(words);
    }

    /**
     * @param words a list of words, e.g., ["I", "am", "Denny", "boy"]
     * @param joinString
     * @return a list of biterm, e.g., ["I_am", "I_Denny", "I_boy", "am_Denny", "am_boy", "Denny_boy"]
     */
    public static List<String> biTerms(List<String> words, String joinString) {
        int wordsLen = words.size();
        if(wordsLen > 1) {
            List<String> lst = CollectionUtil.newArrayList();
            IntStream.range(0, wordsLen - 1).boxed().forEach(i ->
                    IntStream.range(i + 1, wordsLen).boxed().forEach(j ->
                            lst.add(String.join(joinString, words.get(i), words.get(j)))));
            return lst;
        } else {
            // set it as uniterm
            return uniTerms(words);
        }
    }

    /**
     * @param words a list of words, e.g., ["I", "am", "Denny", "boy"]
     * @param joinString
     * @return a list of triterm, e.g., ["I_am_Denny", "I_am_boy", "I_Denny_boy", "am_Denny_boy"]
     */
    public static List<String> triTerms(List<String> words, String joinString) {
        int wordsLen = words.size();
        if(wordsLen > 2) {
            List<String> lst = CollectionUtil.newArrayList();
            IntStream.range(0, wordsLen - 2).boxed().forEach(i ->
                    IntStream.range(i + 1, wordsLen - 1).boxed().forEach(j ->
                            IntStream.range(j + 1, wordsLen).boxed().forEach(k ->
                                    lst.add(String.join(joinString, words.get(i), words.get(j), words.get(k))))));
            return lst;
        } else {
            // set it as biterms
            return biTerms(words, joinString);
        }
    }

    /**
     * @param words a list of words, e.g., ["I", "am", "Denny", "boy", "ha"]
     * @param joinString
     * @return a list of fourterm, e.g., ["I_am_Denny_boy", "I_am_Denny_ha", "I_am_boy_ha", "I_Denny_boy_ha", "am_Denny_boy_ha"]
     */
    public static List<String> fourTerms(List<String> words, String joinString) {
        int wordsLen = words.size();
        if(wordsLen > 3) {
            List<String> lst = CollectionUtil.newArrayList();
            IntStream.range(0, wordsLen - 3).boxed().forEach(i ->
                    IntStream.range(i + 1, wordsLen - 2).boxed().forEach(j ->
                            IntStream.range(j + 1, wordsLen - 1).boxed().forEach(k ->
                                    IntStream.range(k + 1, wordsLen).boxed().forEach(l ->
                                            lst.add(String.join(joinString, words.get(i), words.get(j), words.get(k), words.get(l)))))));
            return lst;
        } else {
            // set it as triterm
            return triTerms(words, joinString);
        }
    }

    public static List<String> nGrams(List<String> words, int ngram) {
        return nGrams(words,ngram, " ");
    }

    /**
     * wrapper for ngram
     * @param words
     * @param ngram
     * @param joinString
     * @return
     */
    public static List<String> nGrams(List<String> words, int ngram, String joinString) {
        List<String> ngramList = CollectionUtil.newArrayList();
        if(1 == ngram) {
            ngramList = uniGrams(words);
        } else if(2 == ngram) {
            ngramList = biGrams(words, joinString);
        } else if(3 == ngram) {
            ngramList = triGrams(words, joinString);
        } else if(4 == ngram) {
            ngramList = fourGrams(words, joinString);
        } else if(12 == ngram) {
            List<String> unigram = uniGrams(words);
            List<String> bigram = biGrams(words, joinString).stream()
                    .filter(x -> x.split(joinString).length == 2).collect(Collectors.toList());
            unigram.addAll(bigram);
            return unigram;
        } else if(123 == ngram) {
            List<String> unigram = uniGrams(words);
            List<String> bigram = biGrams(words, joinString).stream()
                    .filter(x -> x.split(joinString).length == 2).collect(Collectors.toList());
            List<String> trigram = triGrams(words, joinString).stream()
                    .filter(x -> x.split(joinString).length == 3).collect(Collectors.toList());
            ngramList.addAll(unigram);
            ngramList.addAll(bigram);
            ngramList.addAll(trigram);
        } else if(1234 == ngram) {
            List<String> unigram = uniGrams(words);
            List<String> bigram = biGrams(words, joinString).stream()
                    .filter(x -> x.split(joinString).length == 2).collect(Collectors.toList());
            List<String> trigram = triGrams(words, joinString).stream()
                    .filter(x -> x.split(joinString).length == 3).collect(Collectors.toList());
            List<String> fourgram = fourGrams(words, joinString).stream()
                    .filter(x -> x.split(joinString).length == 4).collect(Collectors.toList());
            ngramList.addAll(unigram);
            ngramList.addAll(bigram);
            ngramList.addAll(trigram);
            ngramList.addAll(fourgram);
        }
        return ngramList;
    }

    public static List<String> nTerms(List<String> words, int nterm) {
        return nTerms(words, nterm, " ");
    }

    /**
     * wrapper for nterm
     * @param words
     * @param nterm
     * @param joinString
     * @return
     */
    public static List<String> nTerms(List<String> words, int nterm, String joinString) {
        List<String> ntermList = CollectionUtil.newArrayList();
        if(1 == nterm) {
            ntermList = uniTerms(words);
        } else if(2 == nterm) {
            ntermList = biTerms(words, joinString);
        } else if(3 == nterm) {
            ntermList = triTerms(words, joinString);
        } else if(4 == nterm) {
            ntermList = fourTerms(words, joinString);
        }
        return ntermList;
    }

}

