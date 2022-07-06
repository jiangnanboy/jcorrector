package sy.core.spelling;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sy
 * @date 2022/2/20 21:50
 */
public class SpellingUtils {

    /**
     * all edits that are one edit away from 'word'
     * @param word
     * @param charSet
     */
    public static Set<String> editDisWord(String word, Set<String> charSet)  {

        // splits = [(word[:i], word[i:]) for i in range(len(word) + 1)]
        List<String> splitLRList = CollectionUtil.newArrayList();
        for(int i=0; i<=word.length(); i++) {
            String left = word.substring(0, i);
            String right = word.substring(i);
            splitLRList.add(left + "," + right);
        }

        // transposes = [L + R[1] + R[0] + R[2:] for L, R in splits if len(R) > 1]
        List<String> transposesList = splitLRList.stream()
                .filter(str -> (str.split(",").length == 2) && (str.split(",")[1].length() > 1))
                .map(str -> {
                    String[] strSplit = str.split(",");
                    String left = strSplit[0];
                    String right = strSplit[1];
                    return left + right.substring(1, 2) + right.substring(0, 1) + right.substring(2);
                }).collect(Collectors.toList());

        // replaces = [L + c + R[1:] for L, R in splits if R for c in char_set]
        List<String> replacesList = splitLRList.stream()
                .filter(str -> (str.split(",").length == 2) && str.split(",")[1].length() >= 1)
                .flatMap(str -> charSet.stream().map(chr -> {
                    String[] strSplit = str.split(",");
                    String left = strSplit[0];
                    String right = strSplit[1];
                    return left + chr + right.substring(1);
                })).collect(Collectors.toList());

        List<String> editDistList = CollectionUtil.newArrayList();
        if(Optional.ofNullable(transposesList).isPresent() && (transposesList.size() != 0)) {
            editDistList.addAll(transposesList);
        }

        if(Optional.ofNullable(replacesList).isPresent() && (replacesList.size() != 0)) {
            editDistList.addAll(replacesList);
        }

        return CollectionUtil.newHashset(editDistList);
    }

    public static double editDistance(String chr1, String chr2) {
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        return l.distance(chr1, chr2);
    }

}
