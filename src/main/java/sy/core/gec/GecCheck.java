package sy.core.gec;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import util.CollectionUtil;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sy
 * @date 2022/7/27 22:16
 */
public class GecCheck {
    List<Map.Entry<String, Pattern>> entryList = null;
    Map<String, Pattern> patternMap = null;
    List<String> filterToken = Arrays.asList(new String[]{"。", "，", "：", "、", "？", "！", "及", "和", "或", "而", "且", "但"});

    /**
     * init
     * @param templatePath
     */
    public void init(String templatePath) {
        loadPattern(templatePath);
    }

    /**
     * load pattern
     * @param templatePath
     */
    private void loadPattern(String templatePath) {
        patternMap = CollectionUtil.newHashMap();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(templatePath), StandardCharsets.UTF_8)) {
            String line;
            while((line = br.readLine()) != null) {
                String[] tokenWay = line.split(";");
                String[] tokens = tokenWay[0].split(",");
                String token1 = tokens[0].trim();
                String token2 = tokens[1].trim();
                String correctWay = tokenWay[1].trim();
                String regularExpression = "(" + token1 + ".*" + token2 + ")";
                patternMap.put(token1 + "," + token2 + "," + correctWay, Pattern.compile(regularExpression));
                if(StringUtils.equals("3", correctWay)) {
                    regularExpression = "(" + token2 + ".*" + token1 + ")";
                    patternMap.put(token2 + "," + token1 + "," + correctWay, Pattern.compile(regularExpression));
                }
            }
            entryList = new ArrayList<>(patternMap.entrySet());
            Collections.sort(entryList, (o1, o2) -> o2.getKey().length() - o1.getKey().length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String checkCorrect(String line) {
        List<Map<String, Object>> infoMapList = CollectionUtil.newArrayList();
        for(Map.Entry<String, Pattern> entry : entryList) {
            Pattern pattern = entry.getValue();
            String[] tokens = entry.getKey().split(",");
            String token1 = tokens[0];
            String token2 = tokens[1];
            String correctWay = tokens[2];
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String errorSpan = matcher.group();
                boolean b = false;
                for(String t : filterToken) {
                    if(errorSpan.contains(t)) {
                        b = true;
                        break;
                    }
                }
                if(!b) {
                    String correctSpan;
                    if(StringUtils.equals(correctWay, "0")) {
                        continue;
                    } else if(StringUtils.equals(correctWay, "1")) {
                        correctSpan = errorSpan.replace(token1, "");
                    } else if(StringUtils.equals(correctWay, "2")) {
                        correctSpan = errorSpan.replace(token2, "");
                    } else if(StringUtils.equals(correctWay, "3")) {
                        if(token1.length() < token2.length()) {
                            correctSpan = errorSpan.replace(token1, "");
                        } else {
                            correctSpan = errorSpan.replace(token2, "");
                        }
                    } else {
                        String c = correctWay.split("")[0];
                        String t = correctWay.split("")[1];
                        if(StringUtils.equals(c, "1")) {
                            correctSpan = t + errorSpan.replace(token1, "");
                        } else {
                            correctSpan = errorSpan.replace(token2, "") + t;
                        }
                    }
                    Map<String, Object> infoMap = CollectionUtil.newHashMap();
                    System.out.println(line);
                    line = line.replace(errorSpan, correctSpan);
                    System.out.println(line);
                    infoMap.put("start", matcher.start());
                    infoMap.put("end", matcher.end());
                    infoMap.put("error", errorSpan);
                    infoMap.put("correct", correctSpan);
                    infoMapList.add(infoMap);
                }
            }
        }
        if(infoMapList.size() > 0) {
            return JSON.toJSONString(infoMapList);
        } else {
            return null;
        }

    }

}


