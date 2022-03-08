package sy.core.spelling;

import util.CollectionUtil;

import java.util.Map;

/**
 * @author sy
 * @date 2022/2/28 21:04
 */
public class ErrorTypeEnumMap {
    public static final Map<ErrorTypeEnum, String> errorTypeEnumCategory = CollectionUtil.newHashMap();

    static {
        errorTypeEnumCategory.put(ErrorTypeEnum.CONFUSION, "confusion");
        errorTypeEnumCategory.put(ErrorTypeEnum.WORD, "word");
        errorTypeEnumCategory.put(ErrorTypeEnum.CHR, "char");
    }
}
