/**
 * QuickTranslationImpl.java
 *
 * Version information :
 *
 * Date:2012-6-19
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.quicktranslation;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.cat.common.tm.MatchQuality;
import net.heartsome.cat.ts.core.bean.AltTransBean;
import net.heartsome.cat.ts.core.bean.Constants;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.tb.match.TbMatcher;
import net.heartsome.cat.ts.tm.complexMatch.IComplexMatch;

import org.eclipse.core.resources.IProject;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class QuickTranslationImpl implements IComplexMatch {
	private TbMatcher tbMatcher;

	/**
	 * 
	 */
	public QuickTranslationImpl() {
		tbMatcher = new TbMatcher();
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.tm.complexMatch.AbstractComplexMatch#executeTranslation()
	 */
	public Vector<AltTransBean> executeTranslation(TransUnitBean transUnitBean, IProject currentProject) {
		Vector<AltTransBean> result = new Vector<AltTransBean>();
		// 100%以上的记忆库匹配不做快译
		if (transUnitBean == null) {
			return result;
		}
		Vector<AltTransBean> tmalt = transUnitBean.getMatchesByToolId(Constants.TM_TOOLID);
		if (tmalt.size() < 1) {
			return result;
		}
		AltTransBean tmMatches = tmalt.get(0);

		if (tmMatches == null) {
			return result;
		}

		String tmQuality = tmMatches.getMatchProps().get("match-quality");
		if (tmQuality.endsWith("%")) {
			tmQuality = tmQuality.substring(0, tmQuality.length() - 1);
		}
		int tmQualityInt = Integer.parseInt(tmQuality);
		if (tmQualityInt >= 100) {
			return result;
		}

		String srcLang = tmMatches.getSrcLang();
		String tgtLang = tmMatches.getTgtLang();

		if (srcLang == null || srcLang.equals("")) {
			return result;
		}
		if (tgtLang == null || tgtLang.equals("")) {
			return result;
		}

		String tmSrcText = tmMatches.getSrcText();
		if (tmSrcText == null || tmSrcText.equals("")) {
			return result;
		}
		String tmTgtText = tmMatches.getTgtText();
		if (tmTgtText == null || tmTgtText.equals("")) {
			return result;
		}
		String tuSrcText = transUnitBean.getSrcText();
		if (tuSrcText == null || tuSrcText.equals("")) {
			return result;
		}

		tbMatcher.setCurrentProject(currentProject);
		// 获取翻译单源文中的术语
		Vector<Hashtable<String, String>> tuTerms = findTerms(tuSrcText, srcLang, tgtLang);
		if (tuTerms.size() == 0) {
			return result;
		}

		// 获取记忆库匹配中的术语
		Vector<Hashtable<String, String>> tmTerms = findTerms(tmSrcText, srcLang, tgtLang);
		if (tmTerms.size() == 0) {
			return result;
		}

		int tuTermSize = tuTerms.size();
		int tmTermSize = tmTerms.size();
		if (tuTermSize > tmTermSize) {
			int j = 0;
			while (j < tuTermSize) {
				Hashtable<String, String> tempTuTerm = tuTerms.get(j);
				String tmpTuSrcWord = tempTuTerm.get(CON_SRCWORD);
				for (Hashtable<String, String> tempTmTerm : tmTerms) {
					if (tempTmTerm.get(CON_SRCWORD).equals(tmpTuSrcWord)) {
						tuTerms.remove(j);
						tmTerms.remove(tempTmTerm);
						break;
					}
				}
				tuTermSize = tuTerms.size();
				j++;
			}
		} else {
			int j = 0;
			while (j < tmTermSize) {
				Hashtable<String, String> tempTmTerm = tmTerms.get(j);
				String tmpTmSrcWord = tempTmTerm.get(CON_SRCWORD);
				for (Hashtable<String, String> tempTuTerm : tuTerms) {
					if (tempTuTerm.get(CON_SRCWORD).equals(tmpTmSrcWord)) {
						tmTerms.remove(j);
						tuTerms.remove(tempTuTerm);
						break;
					}
				}
				tmTermSize = tmTerms.size();
				j++;
			}
		}
		tuTermSize = tuTerms.size();
		tmTermSize = tmTerms.size();
		if (tuTermSize == 0 || tmTermSize == 0) {
			return result;
		}
		int replaceSize = tuTermSize < tmTermSize ? tuTermSize : tmTermSize;
		for (int i = 0; i < replaceSize; i++) {
			Hashtable<String, String> tmTerm = tmTerms.get(i);
			String tmTermSrc = tmTerm.get(CON_SRCWORD);
			String tmTermTgt = tmTerm.get(CON_TGTWORD);

			Hashtable<String, String> tuTerm = tuTerms.get(i);
			String tuTermSrc = tuTerm.get(CON_SRCWORD);
			String tuTermTgt = tuTerm.get(CON_TGTWORD);

			tmSrcText = tmSrcText.replace(tmTermSrc, tuTermSrc);
			tmTgtText = tmTgtText.replace(tmTermTgt, tuTermTgt);
		}
		int quality = MatchQuality.similarity(tuSrcText, tmSrcText);
		AltTransBean bean = new AltTransBean(tmSrcText, tmTgtText, srcLang, tgtLang, getMathcerOrigin(), getToolId());
		bean.getMatchProps().put("match-quality", quality + "");
		bean.setSrcContent(tmSrcText);
		bean.setTgtContent(tmTgtText);
		bean.getMatchProps().put("hs:matchType", getMatcherType());
		result.add(bean);
		return result;
	}

	private Vector<Hashtable<String, String>> findTerms(String srcText, String srcLang, String tgtLang) {
		Vector<Hashtable<String, String>> terms = new Vector<Hashtable<String, String>>();
		terms = tbMatcher.serachTransUnitTerms(srcText, srcLang, tgtLang, true);

		// 根据术语的长度升序排列
		Collections.sort(terms, new TermComparatorByLength());

		// 处理查询结果--剔除重复(取最长的术语组),同时按术语在句子中出现的先后进行排序顺序
		for (int i = 0; i < terms.size(); i++) {
			Hashtable<String, String> termA = terms.get(i);
			String srcWordA = termA.get(CON_SRCWORD);
			for (int j = i + 1; j < terms.size(); j++) {
				Hashtable<String, String> termB = terms.get(j);
				String srcWordB = termB.get(CON_SRCWORD);
				if (srcWordA.toLowerCase().indexOf(srcWordB.toLowerCase()) != -1) {
					terms.remove(j);
					j--;
				}
			}
		}

		// 根据术语在srcText中出现的顺序排序
		Collections.sort(terms, new TermComparatorByIndex(srcText));
		return terms;
	}

	/**
	 * 根据术语的长度进行升序排序
	 */
	final class TermComparatorByLength implements Comparator<Hashtable<String, String>> {

		public TermComparatorByLength() {

		}

		public int compare(Hashtable<String, String> a, Hashtable<String, String> b) {
			Integer a1 = a.get(CON_SRCWORD).length();
			Integer b1 = b.get(CON_SRCWORD).length();
			return b1 - a1;
		}
	}

	/**
	 * 根据术语在当前文本段中的出现的顺序对术语列表进行排序
	 */
	final class TermComparatorByIndex implements Comparator<Hashtable<String, String>> {
		private String srcText;

		public TermComparatorByIndex(String srcText) {
			this.srcText = srcText;
		}

		public int compare(Hashtable<String, String> a, Hashtable<String, String> b) {
			Integer a1 = srcText.indexOf(a.get(CON_SRCWORD));
			Integer b1 = srcText.indexOf(b.get(CON_SRCWORD));
			return a1 - b1;
		}
	}

	/** 快速翻译中使用的常量 */
	public static final String CON_SRCWORD = "srcWord";
	public static final String CON_TGTWORD = "tgtWord";

	public String getToolId() {
		return "Quick Translation";
	}

	public String getMatcherType() {
		return "QT";
	}

	public String getMathcerOrigin() {
		return "Heartsome Quick Translation";
	}

}
