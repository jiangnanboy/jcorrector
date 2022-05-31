package sy.dl.bert.utils;

import util.CollectionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author sy
 * @date 2022/5/31 19:03
 */
public class TokenizerUtils {

	public static String clean_text(String text) {
		// Performs invalid character removal and whitespace cleanup on text."""

		StringBuilder output = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			Character c = text.charAt(i);
			int cp = (int) c;
			if (cp == 0 || cp == 0xFFFD || _is_control(c)) {
				continue;
			}
			if (_is_whitespace(c)) {
				output.append(" ");
			} else {
				output.append(c);
			}
		}
		return output.toString();
	}

	public static String tokenize_chinese_chars(String text) {
		// Adds whitespace around any CJK character.
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			Character c = text.charAt(i);
			int cp = (int) c;
			if (_is_chinese_char(cp)) {
				output.append(" ");
				output.append(c);
				output.append(" ");
			} else {
				output.append(c);
			}
		}
		return output.toString();
	}

	public static List<String> whitespace_tokenize(String text) {
		// Runs basic whitespace cleaning and splitting on a piece of text.
		text = text.trim();
		if ((text != null) && (text != "")) {
			return CollectionUtil.newArrayList(Arrays.asList(text.split("\\s+")));
		}
		return CollectionUtil.newArrayList();

	}

	public static String run_strip_accents(String token) {
		token = Normalizer.normalize(token, Form.NFD);
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < token.length(); i++) {
			Character c = token.charAt(i);
			if (Character.NON_SPACING_MARK != Character.getType(c)) {
				output.append(c);
			}
		}
		return output.toString();
	}

	public static List<String> run_split_on_punc(String token, List<String> never_split) {
		// Splits punctuation on a piece of text.
		List<String> output = CollectionUtil.newArrayList();
		if (Optional.of(never_split).isPresent()) {
			if(never_split.contains(token)) {
				output.add(token);
				return output;
			} else {
				for(String specialToken : never_split) {
					if(token.contains(specialToken)) {
						int specialTokenIndex = token.indexOf(specialToken);
						if(specialTokenIndex == 0) {
							String other = token.substring(specialToken.length());
							output.add(specialToken);
							output.add(other);
							return output;
						} else {
							String other = token.substring(0, token.indexOf(specialToken));
							output.add(other);
							output.add(specialToken);
							String another = token.substring(specialTokenIndex + specialToken.length());
							if (another.length() != 0) {
								output.add(another);
							}
							return output;
						}
					}
				}
			}
		}

		boolean start_new_word = true;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < token.length(); i++) {
			Character c = token.charAt(i);
			if (_is_punctuation(c)) {
				if (str.length() > 0) {
					output.add(str.toString());
					str.setLength(0);
				}
				output.add(c.toString());
				start_new_word = true;
			} else {
				if (start_new_word && str.length() > 0) {
					output.add(str.toString());
					str.setLength(0);
				}
				start_new_word = false;
				str.append(c);
			}
		}
		if (str.length() > 0) {
			output.add(str.toString());
		}
		return output;
	}

	public static Map<String, Integer> generateTokenIdMap(String file) throws IOException {
		Map<String, Integer> token_id_map = CollectionUtil.newHashMap();
		if (file == null)
			return token_id_map;
		try(BufferedReader br = Files.newBufferedReader(Paths.get(file), StandardCharsets.UTF_8)) {
			String line;
			int index = 0;
			while ((line = br.readLine()) != null) {
				token_id_map.put(line.trim().toLowerCase(), index);
				index ++;
			}
		}
		return token_id_map;
	}

	private static boolean _is_punctuation(char c) {
		// Checks whether `chars` is a punctuation character.
		int cp = (int) c;
		// We treat all non-letter/number ASCII as punctuation.
		// Characters such as "^", "$", and "`" are not in the Unicode
		// Punctuation class but we treat them as punctuation anyways, for
		// consistency.
		if ((cp >= 33 && cp <= 47) || (cp >= 58 && cp <= 64) || (cp >= 91 && cp <= 96) || (cp >= 123 && cp <= 126)) {
			return true;
		}
		int charType = Character.getType(c);
		if (Character.CONNECTOR_PUNCTUATION == charType || Character.DASH_PUNCTUATION == charType
				|| Character.END_PUNCTUATION == charType || Character.FINAL_QUOTE_PUNCTUATION == charType
				|| Character.INITIAL_QUOTE_PUNCTUATION == charType || Character.OTHER_PUNCTUATION == charType
				|| Character.START_PUNCTUATION == charType) {
			return true;
		}
		return false;
	}

	private static boolean _is_whitespace(char c) {
		// Checks whether `chars` is a whitespace character.
		// \t, \n, and \r are technically contorl characters but we treat them
		// as whitespace since they are generally considered as such.
		if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
			return true;
		}

		int charType = Character.getType(c);
		if (Character.SPACE_SEPARATOR == charType) {
			return true;
		}
		return false;
	}

	private static boolean _is_control(char c) {
		// Checks whether `chars` is a control character.
		// These are technically control characters but we count them as whitespace
		// characters.
		if (c == '\t' || c == '\n' || c == '\r') {
			return false;
		}

		int charType = Character.getType(c);
		if (Character.CONTROL == charType || Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR == charType
				|| Character.FORMAT == charType || Character.PRIVATE_USE == charType || Character.SURROGATE == charType
				|| Character.UNASSIGNED == charType) {
			return true;
		}
		return false;
	}

	private static boolean _is_chinese_char(int cp) {
		// Checks whether CP is the codepoint of a CJK character."""
		// This defines a "chinese character" as anything in the CJK Unicode block:
		// https://en.wikipedia.org/wiki/CJK_Unified_Ideographs_(Unicode_block)
		//
		// Note that the CJK Unicode block is NOT all Japanese and Korean characters,
		// despite its name. The modern Korean Hangul alphabet is a different block,
		// as is Japanese Hiragana and Katakana. Those alphabets are used to write
		// space-separated words, so they are not treated specially and handled
		// like the all of the other languages.
		if ((cp >= 0x4E00 && cp <= 0x9FFF) || (cp >= 0x3400 && cp <= 0x4DBF) || (cp >= 0x20000 && cp <= 0x2A6DF)
				|| (cp >= 0x2A700 && cp <= 0x2B73F) || (cp >= 0x2B740 && cp <= 0x2B81F)
				|| (cp >= 0x2B820 && cp <= 0x2CEAF) || (cp >= 0xF900 && cp <= 0xFAFF)
				|| (cp >= 0x2F800 && cp <= 0x2FA1F)) {
			return true;
		}

		return false;
	}
}
