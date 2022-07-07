package sy.core.proper;

import org.apache.commons.lang3.StringUtils;
import util.CollectionUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sy
 * @date 2022/7/5 21:00
 */
public class LoadDict {

    static Set<String> properNames = CollectionUtil.newHashset();
    static Map<String, String> strokeDict = CollectionUtil.newHashMap();

    /**
     * 名词典，包括成语、俗语、专业领域词等
     * @param properNamePath
     */
    public static void loadSetFile(String properNamePath) throws FileNotFoundException {
        if(Files.exists(Paths.get(properNamePath))) {
            try (Stream<String> stream = Files.lines(Paths.get(properNamePath), StandardCharsets.UTF_8)){
                properNames = stream.filter(line -> !StringUtils.equals(line.strip(), "#") && StringUtils.isNotBlank(line.strip())).collect(Collectors.toSet());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("file not found " + properNamePath);
        }
    }

    /**
     * 五笔笔画字典
     * @param strokePath
     */
    public static void loadDictFile(String strokePath) throws FileNotFoundException {
        if(Files.exists(Paths.get(strokePath))) {
            try (Stream<String> stream = Files.lines(Paths.get(strokePath), StandardCharsets.UTF_8)){
                strokeDict = stream.filter(line -> (!StringUtils.equals(line.strip(), "#")) && (line.strip().split("\\s+").length >= 2))
                        .map(line -> line.split("\\s+"))
                        .collect(Collectors.toMap(term -> term[0], term -> term[1], (oldValue, newValue) -> newValue));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("file not found " + strokePath);
        }
    }

}

