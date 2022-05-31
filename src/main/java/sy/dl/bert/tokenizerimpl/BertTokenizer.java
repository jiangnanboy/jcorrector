package sy.dl.bert.tokenizerimpl;

import sy.dl.bert.tokenizer.Tokenizer;
import sy.dl.bert.utils.TokenizerUtils;
import util.CollectionUtil;
import util.PropertiesReader;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sy
 * @date 2022/5/31 19:03
 */
public class BertTokenizer implements Tokenizer {
	private String vocab_file = BertTokenizer.class.getClassLoader().getResource(PropertiesReader.get("bert_vocab")).getPath().replaceFirst("/", "");
	private Map<String, Integer> token_id_map;
	private Map<Integer, String> id_token_map;
	private boolean do_lower_case = true;
	private boolean do_basic_tokenize = true;
	private List<String> never_split;
	public String unk_token = "[UNK]";
	public String sep_token = "[SEP]";
	public String pad_token = "[PAD]";
	public String cls_token = "[CLS]";
	public String mask_token = "[MASK]";
	private boolean tokenize_chinese_chars = true;
	private BasicTokenizer basic_tokenizer;
	private WordpieceTokenizer wordpiece_tokenizer;

	private static final int MAX_LEN = 512;

	public BertTokenizer(String vocab_file, boolean do_lower_case, boolean do_basic_tokenize, List<String> never_split,
			String unk_token, String sep_token, String pad_token, String cls_token, String mask_token,
			boolean tokenize_chinese_chars) {
		this.vocab_file = vocab_file;
		this.do_lower_case = do_lower_case;
		this.do_basic_tokenize = do_basic_tokenize;
		this.never_split = never_split;
		this.unk_token = unk_token;
		this.sep_token = sep_token;
		this.pad_token = pad_token;
		this.cls_token = cls_token;
		this.mask_token = mask_token;
		this.tokenize_chinese_chars = tokenize_chinese_chars;
		init();
	}

	public BertTokenizer() {
		init();
	}

	private void init() {
		System.out.println("init bertTokenizer...");
		try {
			this.token_id_map = load_vocab(vocab_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.id_token_map = CollectionUtil.newHashMap();
		for (String key : token_id_map.keySet()) {
			this.id_token_map.put(token_id_map.get(key), key);
		}
		never_split = CollectionUtil.newArrayList();
		never_split.add(unk_token);
		never_split.add(sep_token);
		never_split.add(pad_token);
		never_split.add(cls_token);
		never_split.add(mask_token);
		if (do_basic_tokenize) {
			this.basic_tokenizer = new BasicTokenizer(do_lower_case, never_split, tokenize_chinese_chars);
		}
		this.wordpiece_tokenizer = new WordpieceTokenizer(token_id_map, unk_token, never_split);
	}

	private Map<String, Integer> load_vocab(String vocab_file_name) throws IOException {
		System.out.println("load vocab ...");
		return TokenizerUtils.generateTokenIdMap(vocab_file_name);
	}

	@Override
	public List<String> tokenize(String text) {
		List<String> split_tokens = CollectionUtil.newArrayList();
		if (do_basic_tokenize) {
			for (String token : basic_tokenizer.tokenize(text)) {
				for (String sub_token : wordpiece_tokenizer.tokenize(token)) {
					split_tokens.add(sub_token);
				}
			}
		} else {
			split_tokens = wordpiece_tokenizer.tokenize(text);
		}
		split_tokens.add(0, "[CLS]");
		split_tokens.add("[SEP]");
		return split_tokens;
	}

	public List<String> basicTokenize(String text) {
		List<String> tokenizeList = basic_tokenizer.tokenize(text);
		tokenizeList.add(0, "[CLS]");
		tokenizeList.add("[SEP]");
		return tokenizeList;
	}

	public String convert_tokens_to_string(List<String> tokens) {
		// Converts a sequence of tokens (string) in a single string.
		return tokens.stream().map(s -> s.replace("##", "")).collect(Collectors.joining(" "));
	}

	public List<Integer> convert_tokens_to_ids(List<String> tokens) {
		List<Integer> output = CollectionUtil.newArrayList();
		for (String s : tokens) {
			output.add(token_id_map.get(s.toLowerCase()));
		}
		return output;
	}

	public int convert_tokens_to_ids(String token) {
		return token_id_map.get(token.toLowerCase());
	}

	public List<String> convert_ids_to_tokens(List<Integer> ids) {
		List<String> output = CollectionUtil.newArrayList();
		for(int id : ids) {
			output.add(id_token_map.get(id));
		}
		return output;
	}

	public String convert_ids_to_tokens(int id) {
		return id_token_map.get(id);
	}

	public int vocab_size() {
		return token_id_map.size();
	}
}
