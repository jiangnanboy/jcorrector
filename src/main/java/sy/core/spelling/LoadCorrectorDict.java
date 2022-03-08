package sy.core.spelling;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CollectionUtil;
import util.PropertiesReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sy
 * @date 2022/2/24 21:39
 */
public class LoadCorrectorDict {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadCorrectorDict.class);

    public static Set<String> cnCharSet;
    public static Map<String, Set<String>> samePinYin;
    public static Map<String, Set<String>> sameStroke;

    public static void initCorrectorDict() {
        LOGGER.info("corrector dict init...");
        cnCharSet = loadSetFile(LoadCorrectorDict.class.getClassLoader().getResource(PropertiesReader.get("common_char_path")).getPath().replaceFirst("/", ""));
        samePinYin = loadSamePinYin(LoadCorrectorDict.class.getClassLoader().getResource(PropertiesReader.get("same_pinyin_path")).getPath().replaceFirst("/", ""));
        sameStroke = loadSameStroke(LoadCorrectorDict.class.getClassLoader().getResource(PropertiesReader.get("same_stroke_path")).getPath().replaceFirst("/", ""));

    }

    public static Set<String> loadSetFile(String filePath) {
        Set<String> cnCharSet = CollectionUtil.newHashset();
        if(!Files.exists(Paths.get(filePath))) {
            LOGGER.warn("file not found -> " + filePath);
        } else {
            LOGGER.info("file load -> " + filePath);
            try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
                cnCharSet = stream.filter(line -> !line.trim().startsWith("#"))
                        .filter(line -> StringUtils.isNotBlank(line.trim()))
                        .map(line -> line.trim()).collect(Collectors.toSet());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cnCharSet;
    }

    public static Map<String, Set<String>> loadSamePinYin(String filePath) {
        return loadSamePinYin(filePath, "\t");
    }

    /**
     * load same pinyin
     * @param filePath
     * @param sep
     * @return
     */
    public static Map<String, Set<String>> loadSamePinYin(String filePath, String sep) {
        Map<String, Set<String>> samePinYin = CollectionUtil.newHashMap();
        if(!Files.exists(Paths.get(filePath))) {
            LOGGER.warn("file not found -> " + filePath);
        } else {
            LOGGER.info("file load -> " + filePath);
            try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
                samePinYin = stream.filter(line -> !line.trim().startsWith("#"))
                        .filter(line -> (line.trim().split(sep).length > 2))
                        .map(line -> {
                            String[] info = line.trim().split(sep);
                            String keyChar = info[0];
                            Set<String> value = CollectionUtil.newHashset();
                            Set<String> samePronSameTone = CollectionUtil.newHashset(Arrays.asList(info[1].split("")));
                            Set<String> samePronDiffTone = CollectionUtil.newHashset(Arrays.asList(info[2].split("")));
                            value.addAll(samePronSameTone);
                            value.addAll(samePronDiffTone);
                            if(StringUtils.isNotBlank(keyChar) && value.size() > 0) {
                                return new SpellingEntry<>(keyChar, value);
                            }
                            return null;
                        }).filter(pinyinMap -> Optional.ofNullable(pinyinMap).isPresent()).collect(Collectors.toMap(SpellingEntry::getWord, SpellingEntry::getFreq, (oldValue, newValue) -> newValue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return samePinYin;
    }

    public static Map<String, Set<String>> loadSameStroke(String filePath) {
        return loadSameStroke(filePath, "\t");
    }

    /**
     * load same stroke
     * @param filePath
     * @param sep
     * @return
     */
    public static Map<String, Set<String>> loadSameStroke(String filePath, String sep) {
        Map<String, Set<String>> sameStroke = CollectionUtil.newHashMap();
        if(!Files.exists(Paths.get(filePath))) {
            LOGGER.warn("file not found -> " + filePath);
        } else {
            LOGGER.info("file load -> " + filePath);
            try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
                stream.filter(line -> !line.trim().startsWith("#"))
                        .filter(line -> (line.trim().split(sep).length > 1))
                        .forEach(line -> {
                            List<String> info = Arrays.asList(line.trim().split(sep));
                            for(int i=0; i<info.size(); i++) {
                                Set<String> exist = sameStroke.getOrDefault(info.get(i), CollectionUtil.newHashset());
                                List<String> subsList = CollectionUtil.newArrayList();
                                subsList.addAll(info.subList(0, i));
                                subsList.addAll(info.subList(i+1, info.size()));
                                Set<String> current = CollectionUtil.newHashset(subsList);
                                Set<String> value = CollectionUtil.newHashset();
                                value.addAll(exist);
                                value.addAll(current);
                                sameStroke.put(info.get(i), value);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sameStroke;
    }

}
