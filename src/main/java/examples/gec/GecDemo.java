package examples.gec;

import org.apache.commons.lang3.StringUtils;
import sy.core.gec.GecCheck;
import util.PropertiesReader;

import java.util.Scanner;

/**
 * @author YanShi
 * @date 2022/7/28 21:15
 */
public class GecDemo {
    public static void main(String...args) {
        String templatePath = GecDemo.class.getClassLoader().getResource(PropertiesReader.get("template")).getPath().replaceFirst("/", "");
        GecCheck gecRun = new GecCheck();
        gecRun.init(templatePath);
        String sentence;
        while (true) {
            System.out.println("Please input a sentence:");
            Scanner scanner = new Scanner(System.in);
            sentence = scanner.next();
            String infoStr = gecRun.checkCorrect(sentence);
            if(StringUtils.isNotBlank(infoStr)) {
                System.out.println(infoStr);
            }
        }
    }
}
