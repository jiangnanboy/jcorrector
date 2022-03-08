package sy.util;

import com.github.houbb.segment.api.ISegmentResult;
import com.github.houbb.segment.bs.SegmentBs;
import com.github.houbb.segment.support.tagging.pos.tag.impl.SegmentPosTaggings;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import util.CollectionUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sy
 * @date 2022/2/2 21:15
 */
public class Segment {

    public static List<Entry> jiebaSegment(String text) {
        List<Entry> entryList = CollectionUtil.newArrayList();
        List<ISegmentResult> resultList = SegmentBs.newInstance()
                .posTagging(SegmentPosTaggings.simple())
                .segment(text);
        resultList.forEach(result -> entryList.add(new Entry(result.word(), result.pos())));

        return entryList;
    }

    public static List<Entry> hanlpSegment(String text, boolean posi) {
        List<Entry> entryList = CollectionUtil.newArrayList();
        com.hankcs.hanlp.seg.Segment seg = HanLP.newSegment();
        seg.enableOffset(true);
        List<Term> termList = seg.seg(text);
        if(posi) {
            termList.forEach(term -> entryList.add(new Entry(term.word, term.nature.toString(), term.offset)));
        } else {
            termList.forEach(term -> entryList.add(new Entry(term.word, term.nature.toString())));
        }
        return entryList;
    }

    public static List<Entry> hanlpSegment(String text) {
        return hanlpSegment(text, false);
    }

    public static List<String> splitSentence(String text){
        List<String> sentences = CollectionUtil.newArrayList();
        String regEx = "[，。！、？；：,?!.:;]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        String[] sent = p.split(text);
        int sentLen = sent.length;
        if(sentLen > 0){
            int count = 0;
            while(count < sentLen){
                if(m.find()){
                    sent[count] += m.group();
                }
                count ++;
            }
        }
        for(String sentence : sent){
            sentence = sentence.replaceAll("(&rdquo;|&ldquo;|&mdash;|&lsquo;|&rsquo;|&middot;|&quot;|&darr;|&bull;)", "");
            sentences.add(sentence.trim());
        }
        return sentences;
    }


}

