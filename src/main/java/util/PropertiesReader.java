package util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author sy
 * @date 2022/2/2 9:07
 */
public class PropertiesReader {
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new InputStreamReader(PropertiesReader.class.getClassLoader().getResourceAsStream("properties.properties"), "UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String keyName) {
        return properties.getProperty(keyName);
    }

}
