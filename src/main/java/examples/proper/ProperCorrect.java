package examples.proper;

import sy.core.proper.ProperCorrector;
import util.PropertiesReader;

import java.util.List;

/**
 * @author sy
 * @date 2022/7/5 21:05
 */
public class ProperCorrect {
    public static void main(String...args) {
        String properNamePath = ProperCorrect.class.getClassLoader().getResource(PropertiesReader.get("proper_name_path")).getPath().replaceFirst("/", "");
        String strokePath = ProperCorrect.class.getClassLoader().getResource(PropertiesReader.get("stroke_path")).getPath().replaceFirst("/", "");;
        ProperCorrector properCorrector = new ProperCorrector(properNamePath, strokePath);

        List<String> testLine = List.of(
                "报应接中迩来",
                "这块名表带带相传",
                "这块名表代代相传",
                "他贰话不说把牛奶喝完了",
                "这场比赛我甘败下风",
                "这场比赛我甘拜下封",
                "这家伙还蛮格尽职守的",
                "报应接中迩来",  // 接踵而来
                "人群穿流不息",
                "这个消息不径而走",
                "这个消息不胫儿走",
                "眼前的场景美仑美幻简直超出了人类的想象",
                "看着这两个人谈笑风声我心理不由有些忌妒",
                "有了这一番旁证博引",
                "有了这一番旁针博引",
                "这群鸟儿迁洗到远方去了",
                "这群鸟儿千禧到远方去了",
                "美国前总统特琅普给普京点了一个赞，特朗普称普金做了一个果断的决定"
        );
        for(String line : testLine) {
            System.out.println(properCorrector.correct(line));
        }
    }

}
