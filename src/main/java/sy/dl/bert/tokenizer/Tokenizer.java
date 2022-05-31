package sy.dl.bert.tokenizer;

import java.util.List;

/**
 * @author sy
 * @date 2022/5/31 19:03
 */
public interface Tokenizer {
	public List<String> tokenize(String text);
}
