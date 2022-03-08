package sy.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Character.UnicodeBlock;

/**
 * @author sy
 * @date 2022/2/2 20:30
 */
public class Util {

    public static String getString(List<String> sentence) {
        String str = "";
        for(String token : sentence) {
            str += token;
        }
        return str;
    }

    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    public static boolean isAlpha(String str) {
        return StringUtils.isAlpha(str);
    }

    public static boolean isChinese(char c) {
        UnicodeBlock ub = UnicodeBlock.of(c);
        if (ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    public static boolean isChinese(String str) {
        int n = 0;
        for(int i=0; i<str.length(); i++) {
            n = (int)str.charAt(i);
            if(!(19968 <= n && n < 40869)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(String str) {
        String regex = "^[-+]?[-0-9]\\d*\\.\\d*|[-+]?\\.?[0-9]\\d*$";
        return isMatch(regex, str);
    }

    public static boolean isEnglish(String str){
        String regex = "^[a-zA-Z]*";
        return isMatch(regex, str);
    }

    public static boolean isUrl(String str) {
        String regex = "(http(s)?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
        return isMatch(regex, str);
    }

    public static boolean isMatch(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean isSubMatch(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }


    /**
     * 半角转全角
     * @param input String.
     * @return 全角字符串.
     */
    public static String half2FullChange(String input) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);

            }
        }
        return new String(c);
    }

    /**
     * 全角转半角
     * @param input String.
     * @return 半角字符串
     */
    public static String full2HalfChange(String input) {

        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);

            }
        }
        String returnString = new String(c);

        return returnString;
    }

    public static String utf8ToUnicode(String str) {
        char[] myBuffer = str.toCharArray();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            UnicodeBlock ub = UnicodeBlock.of(myBuffer[i]);
            if(ub == UnicodeBlock.BASIC_LATIN){
                //英文及数字等
                sb.append(myBuffer[i]);
            }else if(ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS){
                //全角半角字符
                int j = (int) myBuffer[i] - 65248;
                sb.append((char)j);
            }else{
                //汉字
                short s = (short) myBuffer[i];
                String hexS = Integer.toHexString(s);
                String unicode = "\\u"+hexS;
                sb.append(unicode.toLowerCase());
            }
        }
        return sb.toString();
    }
}
