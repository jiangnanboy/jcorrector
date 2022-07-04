package examples.ngram;

import org.apache.commons.lang3.StringUtils;
import sy.core.spelling.Detector;

import java.util.Scanner;

/**
 * @author sy
 * @date 2022/2/5 20:42
 */
public class DetectDemo {
    public static void main(String[] args) {
        /**
         * 我的喉咙发炎了要买点阿莫细林吉
         * 少先队员因该为老人让坐
         */
        Detector detector = new Detector();
        String sentence;
        while (true) {
            System.out.println("Please input a sentence:");
            Scanner scanner = new Scanner(System.in);
            sentence = scanner.next();
            System.out.println(detector.detect(sentence));
            if(StringUtils.equals(sentence, "exit") || StringUtils.equals(sentence, "quit")) {
                System.out.println("exit or quit!");
                break;
            }
        }
    }

}

