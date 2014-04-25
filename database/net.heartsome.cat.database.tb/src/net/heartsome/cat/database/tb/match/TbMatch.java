/**
 * TermMatch.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.tb.match;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.NGrams;
import net.heartsome.cat.database.bean.TBPreferenceConstants;
import net.heartsome.cat.database.tb.TbDbOperatorManager;
import net.heartsome.cat.database.ui.tb.Activator;
import net.heartsome.cat.ts.tb.match.extension.AbstractTbMatch;
import net.heartsome.cat.ts.tb.match.extension.ITbMatch;

import org.eclipse.core.resources.IProject;

/**
 * 术语库匹配实现
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TbMatch extends AbstractTbMatch implements ITbMatch {

	private TbDbOperatorManager termDbOpManager = new TbDbOperatorManager();

	/**
	 * 
	 */
	public TbMatch() {
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ITbMatch.term.extensionpoint.ITermMatch#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		termDbOpManager.setProject(project);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ITbMatch.term.extensionpoint.ITermMatch#getTransUnitTerms()
	 */
	public Vector<Hashtable<String, String>> getTransUnitTerms() {
		Vector<Hashtable<String, String>> terms = new Vector<Hashtable<String, String>>();
		List<DBOperator> dbOperatorList = termDbOpManager.getDbOperatorList();
		for (int i = 0; i < dbOperatorList.size(); i++) {
			try {				
				terms.addAll(dbOperatorList.get(i).findAllTermsByText(super.srcPureText, super.srcLanguage,
						super.tgtLanguage));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (terms == null || terms.size() <= 0) {
			return terms;
		}
		// 删除所有的特殊字符 -robert//\f\t\u2028\u2029,.;\"\':<>?!()[]{}=+-/*\u00AB\u00BB\u201C\u201D\u201E\uFF00\u2003
		// srcPureText =
		// srcPureText.replaceAll("[^\\w\\s\\.\\,\\;\\\"\\'\\:\\<\\>\\?\\!\\(\\)\\[\\]\\{\\}\\=\\+\\/\\*&@#$%_~`-]",
		// " ");
		// 注释以上代码解决德语、俄语中带注音字符部分被忽略掉的问题

		cleanTerms(srcPureText, srcLanguage, terms, super.isSort);
		resetCleanTerms(terms);
		boolean caseSencitive = Activator.getDefault().getPreferenceStore()
				.getBoolean(TBPreferenceConstants.TB_CASE_SENSITIVE);
		if (!caseSencitive) {
			doWithQueryResultsCaseSensitive(terms, srcPureText);
		}
		if (isSort) {
			return sortTerms(terms, srcLanguage);
		} else {
			return terms;
		}
	}

	/**
	 * 清理非亚洲语系中不能匹配整个单词及重复出现的术语。
	 */
	public static void cleanTerms(String source, String srcLang, Vector<Hashtable<String, String>> terms, boolean isSort) {
		Vector<String> srcWord = buildWordList(source.toLowerCase(), srcLang);

		for (int i = 0; i < terms.size(); i++) {
			Hashtable<String, String> term = terms.get(i);
			String key = term.get("srcWord");
			Vector<String> keyWord = buildWordList(key.toLowerCase(), srcLang);
			// 其他文字，要进行进一步判断，如果判断不成功，删除该术语匹配
			if (!srcLang.toLowerCase().matches("zh.*|ja.*|ko.*|th.*|he.*")) {
				if (!termAnalysis(srcWord, keyWord)) {
					terms.remove(i);
					i--;
				}
			}

			// 如果要排序的话，获取术语单词数与字数，方便排序时进行比较,以及在自动翻译中计算匹配率，避免重复取出来文本来计算。
			if (isSort) {
				term.put("WordSize", String.valueOf(keyWord.size()));
				term.put("CharLength", String.valueOf(key.length()));
				term.put("index", "" + source.indexOf(key));
			}

			// 下面判断重复的术语的情况，与下面所有的术语项进行匹配，若有相同的，就删除。 备注：注释该过滤代码，--robert 2012-06-18
			// for (int j = i + 1; j < terms.size(); j++) {
			// Hashtable<String, String> term_j = terms.get(j);
			// if (term.get("srcWord").equals(term_j.get("srcWord"))
			// && term.get("tgtWord").equals(term_j.get("tgtWord"))) {
			// terms.remove(j);
			// j--;
			// }
			// }
		}
	}

	private static Vector<String> buildWordList(String source, String srcLang) {
		Vector<String> result = new Vector<String>();
		StringTokenizer tk = new StringTokenizer(source, NGrams.SEPARATORS, true);
		while (tk.hasMoreTokens()) {
			result.add(tk.nextToken());
		}
		return result;
	}

	public static boolean termAnalysis(Vector<String> srcWord, Vector<String> termWords) {
		int termSize = termWords.size();
		if (termSize == 0) {
			return false;
		}

		if (termSize == 1) {
			if (srcWord.indexOf(termWords.get(0).toLowerCase()) != -1) {
				return true;
			} else {
				return false;
			}
		}

		int sIndex = srcWord.indexOf(termWords.get(0).toLowerCase(), 0);
		while (sIndex != -1 && sIndex + termSize - 1 < srcWord.size()) { // 有开始并且长度够
			// 从术语长度处取结尾单词比较是否相同
			if (srcWord.get(sIndex + termSize - 1).equalsIgnoreCase(termWords.get(termSize - 1))) {
				boolean isTerm = true;
				for (int i = 1, size = termSize - 1; i < size; i++) {
					if (!srcWord.get(sIndex + i).equalsIgnoreCase(termWords.get(i))) {
						isTerm = false;
						break;
					}
				}
				if (isTerm) {
					return true;
				}
			}
			sIndex = srcWord.indexOf(termWords.get(0).toLowerCase(), sIndex + 1);
		}
		return false;
	}

	/**
	 * 对术语按字数或单词数进行从长到短的降序排列，方便正确替换。
	 */
	public static Vector<Hashtable<String, String>> sortTerms(Vector<Hashtable<String, String>> terms, String srcLang) {
		Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();
		int maxLength = 0;

		TreeMap<Integer, Vector<Hashtable<String, String>>> termMap = new TreeMap<Integer, Vector<Hashtable<String, String>>>();
		// 先按术语在原文中的顺序排序
		for (Hashtable<String, String> term : terms) {
			int index = Integer.parseInt(term.get("index"));
			if (termMap.get(index) == null) {
				termMap.put(index, new Vector<Hashtable<String, String>>());
			}
			termMap.get(index).add(term);
		}

		Hashtable<Integer, Vector<Hashtable<String, String>>> groupTerms = new Hashtable<Integer, Vector<Hashtable<String, String>>>();
		for (Entry<Integer, Vector<Hashtable<String, String>>> entry : termMap.entrySet()) {
			groupTerms.clear();
			Vector<Hashtable<String, String>> termVector = entry.getValue();
			// 按单词数量来排序
			for (int i = 0, size = termVector.size(); i < size; i++) {
				Hashtable<String, String> term = termVector.get(i);

				int termSize = Integer.parseInt(term.get("WordSize"));
				if (termSize > maxLength) {
					maxLength = termSize;
				}
				Vector<Hashtable<String, String>> sameSizeTerms = groupTerms.get(termSize);
				if (sameSizeTerms == null) {
					sameSizeTerms = new Vector<Hashtable<String, String>>();
				}
				sameSizeTerms.add(term);
				groupTerms.put(termSize, sameSizeTerms);
			}

			// 按字符数量来排序
			for (int key = maxLength; key > 0; key--) {
				Vector<Hashtable<String, String>> partOfTerms = groupTerms.get(key);
				if (partOfTerms == null) {
					continue;
				}

				// 如果同样单词数量的术语只有一个，则无须按字符长度再排序
				if (partOfTerms.size() == 1) {
					result.addAll(partOfTerms);
					continue;
				}

				// 当同样单词数量的术语超过一个时，按字符长度再排序
				Hashtable<Integer, Vector<Hashtable<String, String>>> charsGroupTerms = new Hashtable<Integer, Vector<Hashtable<String, String>>>();
				int maxChars = 0;
				for (int i = 0, size = partOfTerms.size(); i < size; i++) {
					Hashtable<String, String> term = partOfTerms.get(i);
					int charCount = Integer.parseInt(term.get("CharLength"));
					maxChars = maxChars > charCount ? maxChars : charCount;

					Vector<Hashtable<String, String>> tmpTerms = charsGroupTerms.get(charCount);
					if (tmpTerms == null) {
						tmpTerms = new Vector<Hashtable<String, String>>();
					}

					tmpTerms.add(term);
					charsGroupTerms.put(charCount, tmpTerms);
				}

				for (int charKey = maxChars; charKey > 0; charKey--) {
					Vector<Hashtable<String, String>> charPartOfTerms = charsGroupTerms.get(charKey);
					if (charPartOfTerms == null) {
						continue;
					}
					result.addAll(charPartOfTerms);
				}
			}
		}
		return result;
	}

	public void clearResources() {
		srcPureText = "";
		srcLanguage = "";
		tgtLanguage = "";
		isSort = false;
		termDbOpManager.clearResource();
	}

	private static void resetCleanTerms(Vector<Hashtable<String, String>> terms) {
		for (Hashtable<String, String> term : terms) {
			String srcWord = term.get("srcWord");
			String tgtWord = term.get("tgtWord");

			String srcWord_new = resetCleanString(srcWord);
			String tgtWord_new = resetCleanString(tgtWord);
			if (!srcWord_new.equals(srcWord)) {
				term.put("srcWord", srcWord_new);
			}
			if (!tgtWord_new.equals(tgtWord)) {
				term.put("tgtWord", tgtWord_new);
			}
		}

	}

	private static String resetCleanString(String string) {
		string = string.replaceAll("&lt;", "<");
		string = string.replaceAll("&gt;", ">");
		// string = string.replaceAll("&quot;", "\"");
		string = string.replaceAll("&amp;", "&");

		return string;
	}

	public boolean checkTbMatcher(IProject project) {
		this.setProject(project);
		if (termDbOpManager.getDbOperatorList().size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * 如果结果要区分大小写
	 * @param terms
	 * @param caseSensitive
	 *            ;
	 */
	private void doWithQueryResultsCaseSensitive(Vector<Hashtable<String, String>> terms, String pureText) {
		Iterator<Hashtable<String, String>> iterator = terms.iterator();
		while(iterator.hasNext()){
			Hashtable<String, String> item = iterator.next();
			String srcWord = item.get("srcWord");
			if (!pureText.contains(srcWord)) {
				iterator.remove();
			}
		}
	}
}
