package sy.dl.bert.tokenizerimpl;

import sy.dl.bert.tokenizer.Tokenizer;
import sy.dl.bert.utils.TokenizerUtils;
import util.CollectionUtil;

import java.util.List;

/**
 * @author sy
 * @date 2022/5/31 19:03
 */
public class BasicTokenizer implements Tokenizer {
	private boolean do_lower_case = true;
	private List<String> never_split;
	private boolean tokenize_chinese_chars = true;
	private List<String> specialTokens;
	public BasicTokenizer(boolean do_lower_case, List<String> never_split, boolean tokenize_chinese_chars) {
		this.do_lower_case = do_lower_case;
		if (never_split == null) {
			this.never_split = CollectionUtil.newArrayList();
		} else {
			this.never_split = never_split;
		}
		this.tokenize_chinese_chars = tokenize_chinese_chars;
	}

	public BasicTokenizer() {
	}

	@Override
	public List<String> tokenize(String text) {
		text = TokenizerUtils.clean_text(text);
		if (tokenize_chinese_chars) {
			text = TokenizerUtils.tokenize_chinese_chars(text);
		}
		List<String> orig_tokens = TokenizerUtils.whitespace_tokenize(text);
		List<String> split_tokens = CollectionUtil.newArrayList();
		for (String token : orig_tokens) {
			if (do_lower_case && !never_split.contains(token)) {
				token = TokenizerUtils.run_strip_accents(token);
				split_tokens.addAll(TokenizerUtils.run_split_on_punc(token, never_split));
			} else {
				split_tokens.add(token);
			}
		}
		return TokenizerUtils.whitespace_tokenize(String.join(" ", split_tokens));
	}

}
