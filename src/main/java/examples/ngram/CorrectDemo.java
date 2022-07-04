package examples.ngram;

import org.apache.commons.lang3.StringUtils;
import sy.core.spelling.Corrector;

import java.util.Scanner;

/**
 * @author sy
 * @date 2022/2/21 20:21
 */
public class CorrectDemo {
    public static void main(String[] args) {
        /**
         * 我的喉咙发炎了要买点阿莫细林吉
         * 少先队员因该为老人让坐
         */
        Corrector corrector = new Corrector();
        String sentence;
        while (true) {
            System.out.println("Please input a sentence:");
            Scanner scanner = new Scanner(System.in);
            sentence = scanner.next();
            System.out.println(corrector.correct(sentence));
            if(StringUtils.equals(sentence, "exit") || StringUtils.equals(sentence, "quit")) {
                System.out.println("exit or quit!");
                break;
            }
        }

    }
}
