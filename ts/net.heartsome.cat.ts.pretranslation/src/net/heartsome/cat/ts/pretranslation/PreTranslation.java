/**
 * PreTranslation.java
 *
 * Version information :
 *
 * Date:Dec 13, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.pretranslation;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.ts.core.bean.AltTransBean;
import net.heartsome.cat.ts.core.bean.Constants;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.pretranslation.bean.PreTransParameters;
import net.heartsome.cat.ts.pretranslation.bean.PreTranslationCounter;
import net.heartsome.cat.ts.pretranslation.resource.Messages;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;
import net.heartsome.cat.ts.tm.match.TmMatcher;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;
import net.heartsome.cat.ts.tm.simpleMatch.SimpleMatcherFactory;
import net.heartsome.cat.ts.ui.util.IntelligentTagPrcessor;
import net.heartsome.cat.ts.ui.util.TmUtils;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 预翻译业务逻辑实现
 * @author Jason
 * @version
 * @since JDK1.6
 */
/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class PreTranslation {
	public static final Logger logger = LoggerFactory.getLogger(PreTranslation.class);
	private int updateStrategy;

	private PreTransParameters parameters;
	private IProject currentProject;
	private TmMatcher tmMatcher;

	/** 上下文个数 */
	private int contextSize = 1;

	/** 项目中的XLIFF文件解析 */
	private XLFHandler xlfHandler;

	/** 项目中的XLIFF文件路径,绝对路径 */
	private List<String> xlfFiles;

	private List<PreTranslationCounter> transCounters;
	private PreTranslationCounter currentCounter;

	public PreTranslation(XLFHandler xlfHandler, List<String> xlfFiles, IProject currIProject,
			PreTransParameters parameters) {

		this.xlfHandler = xlfHandler;
		this.xlfFiles = xlfFiles;

		this.parameters = parameters;
		tmMatcher = new TmMatcher();
		tmMatcher.setCustomeMatchParameters(tmMatcher.getMaxMatchSize(), parameters.getIgnoreTag(),
				parameters.getLowestMatchPercent(), !parameters.getIgnoreCase(), 1,
				parameters.getPanalty());
		this.updateStrategy = parameters.getUpdateStrategy();
		this.currentProject = currIProject;
		this.contextSize = tmMatcher.getContextSize();

		transCounters = new ArrayList<PreTranslationCounter>();
	}

	/**
	 * 根据构建参数执行预翻译 ;
	 * @throws InterruptedException
	 */
	public List<PreTranslationCounter> executeTranslation(IProgressMonitor monitor) throws InterruptedException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("", this.xlfFiles.size());
		monitor.setTaskName(Messages.getString("pretranslation.PreTranslation.task1"));
		try {
			for (String xlfPath : xlfFiles) {
				if (monitor != null && monitor.isCanceled()) {
					throw new InterruptedException();
				}

				currentCounter = new PreTranslationCounter(xlfPath);
				this.transCounters.add(currentCounter);

				VTDNav vn = xlfHandler.getVnMap().get(xlfPath);
				VTDUtils vu = new VTDUtils(vn);
				AutoPilot ap = new AutoPilot(vu.getVTDNav());
				int tuNumber = xlfHandler.getNodeCount(xlfPath,
						"/xliff/file//descendant::trans-unit[(source/text()!='' or source/*)]");

				currentCounter.setTuNumber(tuNumber);

				ap.selectXPath("/xliff/file");
				String srcLang = "";
				String tgtLang = "";
				XMLModifier xm = new XMLModifier(vn);
				IProgressMonitor monitor2 = new SubProgressMonitor(monitor, 1);
				monitor2.beginTask(Messages.getString("pretranslation.PreTranslation.task2"), tuNumber);
				while (ap.evalXPath() != -1) { // 循环 file 节点
					String _srcLang = vu.getCurrentElementAttribut("source-language", "");
					String _tgtLang = vu.getCurrentElementAttribut("target-language", "");

					if (!_srcLang.equals("")) {
						srcLang = _srcLang;
					}
					if (!_tgtLang.equals("")) {
						tgtLang = _tgtLang;
					}

					if (srcLang.equals("") || tgtLang.equals("")) {
						continue;
					}

					if (updateStrategy == PreTransParameters.KEEP_OLD_TARGET) {
						keepCurrentMatchs(vu, _srcLang, _tgtLang, xm, monitor2);
					} else if (updateStrategy == PreTransParameters.KEEP_BEST_MATCH_TARGET) {
						keepHigherMatchs(vu, _srcLang, _tgtLang, xm, monitor2);
					} else if (updateStrategy == PreTransParameters.KEEP_NEW_TARGET) {
						overwriteMatchs(vu, srcLang, tgtLang, xm, monitor2);
					}
				}
				monitor2.done();
				FileOutputStream fos = new FileOutputStream(xlfPath);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				xm.output(bos); // 写入文件
				bos.close();
				fos.close();
			}
		} catch (XPathParseException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (TranscodeException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		}
		monitor.done();
		return this.transCounters;
	}

	public void clearResources() {
		// 完成翻译后清除使用的资源
		tmMatcher.clearResources();
		this.transCounters.clear();
		this.transCounters = null;
	}

	/**
	 * 保留最大上限匹配数量
	 * @param tmpVector
	 *            ;
	 */
	private void checkMaxMatchSize(Vector<Hashtable<String, String>> tmpVector) {
		int size = tmpVector.size();
		while (size > tmMatcher.getMaxMatchSize()) {
			size--;
			tmpVector.remove(size);
		}
	}

	private void keepCurrentMatchs(VTDUtils vu, String srcLang, String tgtLang, XMLModifier xm, IProgressMonitor monitor)
			throws NavException, XPathParseException, XPathEvalException, ModifyException,
			UnsupportedEncodingException, InterruptedException {
		AutoPilot tuAp = new AutoPilot(vu.getVTDNav());
		tuAp.selectXPath("./body//trans-unit");
		boolean needUpdateTgt = true;
		while (tuAp.evalXPath() != -1) { // 循环 Trans-unit
			if (monitor != null && monitor.isCanceled()) {
				throw new InterruptedException();
			}

			// skip locked segment
			String locked = vu.getCurrentElementAttribut("translate", "yes");
			if (locked.equals("no")) {
				continue;
			}

			String tgtContent = vu.getElementContent("./target");
			if (tgtContent != null && !tgtContent.trim().equals("")) {
				needUpdateTgt = false;
			}
			TransUnitInfo2TranslationBean tuInfo = getTransUnitInfo(vu);
			if (tuInfo == null) {
				continue;
			}
			tuInfo.setSrcLanguage(srcLang);
			tuInfo.setTgtLangugage(tgtLang);
			getTuContext(vu, contextSize, tuInfo);
			List<FuzzySearchResult> result = tmMatcher.executeFuzzySearch(currentProject, tuInfo);
			updateXliffFile(vu, tuInfo, result, xm, needUpdateTgt);
			needUpdateTgt = true;
			monitor.worked(1);
		}
	}

	private void updateXliffFile(VTDUtils vu, TransUnitInfo2TranslationBean tuInfo,
			List<FuzzySearchResult> fuzzyResult, XMLModifier xm, boolean updateTarget) throws XPathParseException,
			XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
		String altTransContent = "";
		String targetContent = "";
		vu.delete(new AutoPilot(vu.getVTDNav()), xm, "./alt-trans[@tool-id='" + Constants.TM_TOOLID + "']",
				VTDUtils.PILOT_TO_END);
		if (!fuzzyResult.isEmpty()) {
			altTransContent = generateAltTransUnitNodeXML(fuzzyResult);
		}

		if (updateTarget && !fuzzyResult.isEmpty()) {
			vu.delete(new AutoPilot(vu.getVTDNav()), xm, "./target", VTDUtils.PILOT_TO_END);
			targetContent = generateTargetNodeXML(fuzzyResult.get(0), tuInfo);
			currentCounter.countTransTu();
		}

		targetContent += altTransContent;

		String simpleMatchContent = ""; // executeSimpleMatch(vu, tuInfo, xm/* , updateTargetTemp */);
		targetContent += simpleMatchContent;

		if (targetContent.length() > 0) {
			xm.insertBeforeTail(targetContent);
		}

		if (fuzzyResult.size() > 0) {
			String similarity = fuzzyResult.get(0).getSimilarity() + ""; // 取最大匹配率的
			if (parameters.isLockContextMatch() && similarity.equals("101")) {
				lockTransUnit(vu.getVTDNav(), xm);
				currentCounter.countLockedContextmatch();
			} else if (parameters.isLockFullMatch() && similarity.equals("100")) {
				lockTransUnit(vu.getVTDNav(), xm);
				currentCounter.countLockedFullMatch();
			}
		}
	}

	// private String defaultSimplematcher;

	private String executeSimpleMatch(VTDUtils vu, TransUnitInfo2TranslationBean tuInfo, XMLModifier xm/*
																										 * , boolean
																										 * updateTarget
																										 */)
			throws XPathParseException, XPathEvalException, NavException {
		List<ISimpleMatcher> simpleMatchers = SimpleMatcherFactory.getInstance().getCuurentMatcher();
		StringBuffer bf = new StringBuffer();
		/*
		 * final List<String> toolIds = new ArrayList<String>(); if (defaultSimplematcher == null) { for (ISimpleMatcher
		 * matcher : simpleMatchers) { if (matcher.isSuportPreTrans() && matcher.matchChecker()) {
		 * toolIds.add(matcher.getMathcerToolId()); } } if (toolIds.size() > 1) { Display.getDefault().syncExec(new
		 * Runnable() {
		 * 
		 * @Override public void run() { Shell shell = Display.getCurrent().getActiveShell(); PromptDialog dlg = new
		 * PromptDialog(shell, toolIds); if (dlg.open() == Window.OK) { defaultSimplematcher = dlg.getChoiceResult(); }
		 * } }); } else if (toolIds.size() == 1) { defaultSimplematcher = toolIds.get(0); } else { defaultSimplematcher
		 * = null; } }
		 */

		for (ISimpleMatcher matcher : simpleMatchers) {
			if (!matcher.isSuportPreTrans()) {
				continue;
			}
			String toolId = matcher.getMathcerToolId();
			boolean isOverwrite = matcher.isOverwriteMatch();

			boolean needClear = false;
			vu.getVTDNav().push();
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("./alt-trans[@tool-id='" + toolId + "']");
			if (ap.evalXPath() != -1) {
				if (!isOverwrite) {
					vu.getVTDNav().pop();
					continue;
				} else {
					needClear = true;
				}
			}
			vu.getVTDNav().pop();

			if (needClear) {
				vu.delete(new AutoPilot(vu.getVTDNav()), xm, "./alt-trans[@tool-id='" + toolId + "']",
						VTDUtils.PILOT_TO_END);
			}

			String tgtText = matcher.executeMatch(tuInfo);
			if (tgtText.equals("")) {
				continue;
			}

			/*
			 * if (updateTarget && defaultSimplematcher.equals(toolId)) { vu.delete(new AutoPilot(vu.getVTDNav()), xm,
			 * "./target", VTDUtils.PILOT_TO_END); bf.append("<target xml:lang=\"" + tuInfo.getTgtLangugage() +
			 * "\" state=\"new\"  hs:matchType=\"" + matcher.getMatcherType() + "\" hs:quality=\"100\">");
			 * bf.append(tgtText); bf.append("</target>");
			 * 
			 * currentCounter.countTransTu(); }
			 */

			AltTransBean bean = new AltTransBean(tuInfo.getSrcPureText(), tgtText, tuInfo.getSrcLanguage(),
					tuInfo.getTgtLangugage(), matcher.getMathcerOrigin(), matcher.getMathcerToolId());
			bean.getMatchProps().put("match-quality", "100");
			bean.setSrcContent(tuInfo.getSrcPureText());
			bean.setTgtContent(tgtText);
			bean.getMatchProps().put("hs:matchType", matcher.getMatcherType());
			bf.append(bean.toXMLString());

		}
		return bf.toString();
	}

	private void lockTransUnit(VTDNav vn, XMLModifier xm) throws NavException, ModifyException,
			UnsupportedEncodingException, XPathParseException, XPathEvalException {
		vn.push();
		int attrIdx = vn.getAttrVal("translate");
		if (attrIdx != -1) { // 存在translate属性
			String translate = vn.toString(attrIdx);
			if (!translate.equals("no")) { // translate属性值不为指定的translateValue
				xm.updateToken(attrIdx, "no");
			}
		} else {
			xm.insertAttribute(" translate=\"no\" ");
		}
		vn.pop();
	}

	private boolean checkTuCurrentMatch(VTDUtils vu, Vector<Hashtable<String, String>> tmMatch)
			throws XPathParseException, XPathEvalException, NavException {
		if (tmMatch.size() == 0) {
			return false;
		}
		Vector<Hashtable<String, String>> currentMatch = new Vector<Hashtable<String, String>>();
		vu.getVTDNav().push();
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.selectXPath("./alt-trans[@tool-id='" + Constants.TM_TOOLID + "']");
		int existMatchCount = 0;
		while (ap.evalXPath() != -1) {
			Hashtable<String, String> altTrans = new Hashtable<String, String>();
			String quality = vu.getCurrentElementAttribut("match-quality", "0");
			String srcText = vu.getElementContent("./source");
			String tgtText = vu.getElementContent("./target");
			if (srcText != null && tgtText != null) {
				altTrans.put("srcText", srcText);
				altTrans.put("tgtText", tgtText);
			} else {
				continue;
			}

			if (!isDuplicated(tmMatch, altTrans)) {
				String content = vu.getElementFragment();
				altTrans.put("content", content);
				if (quality.endsWith("%")) {
					quality = quality.substring(0, quality.length() - 1);
				}
				altTrans.put("similarity", quality);
				altTrans.put("flag", "exist");
				currentMatch.add(altTrans);
			} else {
				existMatchCount++;
			}
		}
		vu.getVTDNav().pop();
		if (existMatchCount == tmMatch.size()) { // 库中查询的内容和文件中的内容是一样的
			return false;
		} else {
			tmMatch.addAll(currentMatch);
			Collections.sort(tmMatch, new FindMatchComparator());
			checkMaxMatchSize(tmMatch);
			return true;
		}
	}

	private TransUnitInfo2TranslationBean getTransUnitInfo(VTDUtils vu) throws XPathParseException, XPathEvalException,
			NavException {
		TransUnitInfo2TranslationBean tuInfo = new TransUnitInfo2TranslationBean();

		vu.getVTDNav().push();
		AutoPilot sourceAp = new AutoPilot(vu.getVTDNav());
		sourceAp.selectXPath("./source");
		String fullText = "";
		String pureText = "";
		if (sourceAp.evalXPath() != -1) {
			fullText = vu.getElementContent();
			pureText = xlfHandler.getTUPureText(vu.getVTDNav());
		}
		vu.getVTDNav().pop();

		if (fullText == null || fullText.equals("") || pureText.equals("")) {
			return null;
		}
		tuInfo.setSrcFullText(fullText);
		tuInfo.setSrcPureText(pureText);
		return tuInfo;
	}

	private void getTuContext(VTDUtils vu, int contextSize, TransUnitInfo2TranslationBean tuInfo) {
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		String preContext = xlfHandler.getContext(vu, ap, contextSize, true);
		String nextContext = xlfHandler.getContext(vu, ap, contextSize, false);
		tuInfo.setPreContext(preContext);
		tuInfo.setNextContext(nextContext);
	}

	private void keepHigherMatchs(VTDUtils vu, String srcLang, String tgtLang, XMLModifier xm, IProgressMonitor monitor)
			throws NavException, XPathParseException, XPathEvalException, ModifyException,
			UnsupportedEncodingException, InterruptedException {
		AutoPilot tuAp = new AutoPilot(vu.getVTDNav());
		tuAp.selectXPath("./body//trans-unit");
		boolean needUpdateTarget = false;
		while (tuAp.evalXPath() != -1) { // 循环 Trans-unit
			if (monitor != null && monitor.isCanceled()) {
				throw new InterruptedException();
			}

			String locked = vu.getCurrentElementAttribut("translate", "yes");
			if (locked.equals("no")) {
				continue;
			}

			// 　===从库中查询匹配===
			TransUnitInfo2TranslationBean tuInfo = getTransUnitInfo(vu);
			if (tuInfo == null) {
				continue;
			}
			tuInfo.setSrcLanguage(srcLang);
			tuInfo.setTgtLangugage(tgtLang);
			getTuContext(vu, contextSize, tuInfo);

			List<FuzzySearchResult> result = tmMatcher.executeFuzzySearch(currentProject, tuInfo);
			// Vector<Hashtable<String, String>> tmMatches = tmMatcher.executeSearch(currentProject, tuInfo);
			// 　====查询结束===

			if (!result.isEmpty()) {
				int matchMaxSimiInt = result.get(0).getSimilarity();
				// ===获取当前目标的匹配率===
				int currMaxSimInt = 0;
				vu.getVTDNav().push();
				AutoPilot targetAp = new AutoPilot(vu.getVTDNav());
				targetAp.selectXPath("./target");
				if (targetAp.evalXPath() != -1) {
					String targetContent = vu.getElementContent();
					if (targetContent != null && !targetContent.equals("")) {
						Hashtable<String, String> attrs = vu.getCurrentElementAttributs();
						if (attrs != null) {
							String type = attrs.get("hs:matchType");
							String quality = attrs.get("hs:quality");
							if (type != null && type.equals("TM") && quality != null && !quality.equals("")) {
								currMaxSimInt = Integer.parseInt(quality);
							} else {
								//对于这种没有匹配率的 segment（手动翻译了，但是没入库，没锁定的情况。），需要覆盖2013-09-02, Austen。
								currMaxSimInt = 1;
							}
						}
					} else { // target内容为空
						needUpdateTarget = true;
					}
				} else { // 无target内容
					needUpdateTarget = true;
				}
				vu.getVTDNav().pop();
				if (currMaxSimInt != 0 && matchMaxSimiInt > currMaxSimInt) {
					needUpdateTarget = true;
				}
			}
			// 　===获取当前最大匹配结束===
			updateXliffFile(vu, tuInfo, result, xm, needUpdateTarget);
			needUpdateTarget = false;
			monitor.worked(1);
		}

	}

	private void overwriteMatchs(VTDUtils vu, String srcLang, String tgtLang, XMLModifier xm, IProgressMonitor monitor)
			throws NavException, XPathParseException, XPathEvalException, ModifyException,
			UnsupportedEncodingException, InterruptedException {
		AutoPilot tuAp = new AutoPilot(vu.getVTDNav());
		tuAp.selectXPath("./body//trans-unit");
		while (tuAp.evalXPath() != -1) { // 循环 Trans-unit
			if (monitor != null && monitor.isCanceled()) {
				throw new InterruptedException();
			}

			String locked = vu.getCurrentElementAttribut("translate", "yes");
			if (locked.equals("no")) {
				continue;
			}

			TransUnitInfo2TranslationBean tuInfo = getTransUnitInfo(vu);
			if (tuInfo == null) {
				continue;
			}
			// System.out.println(tuInfo.getSrcFullText());
			tuInfo.setSrcLanguage(srcLang);
			tuInfo.setTgtLangugage(tgtLang);
			getTuContext(vu, contextSize, tuInfo);
			// Vector<Hashtable<String, String>> result = tmMatcher.executeSearch(currentProject, tuInfo);
			List<FuzzySearchResult> result = tmMatcher.executeFuzzySearch(currentProject, tuInfo);

			updateXliffFile(vu, tuInfo, result, xm, true);
			monitor.worked(1);
		}

	}

	private boolean isDuplicated(Vector<Hashtable<String, String>> vector, Hashtable<String, String> tu) {
		int size = vector.size();
		String src = tu.get("srcText"); //$NON-NLS-1$
		String tgt = tu.get("tgtText"); //$NON-NLS-1$
		for (int i = 0; i < size; i++) {
			Hashtable<String, String> table = vector.get(i);
			if (src.trim().equals(table.get("srcText").trim()) //$NON-NLS-1$
					&& tgt.trim().equals(table.get("tgtText").trim())) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/**
	 * 生成target节点
	 * @param target
	 * @return ;
	 */
	private String generateTargetNodeXML(FuzzySearchResult fuzzyResult, TransUnitInfo2TranslationBean tuInfo) {
		String lang = fuzzyResult.getTu().getTarget().getLangCode();
		String quality = fuzzyResult.getSimilarity() + "";
		StringBuffer bf = new StringBuffer();
		bf.append("<target xml:lang=\"" + lang + "\" state=\"new\" hs:matchType=\"TM\" hs:quality=\"" + quality + "\">");

		String content = fuzzyResult.getTu().getTarget().getFullText();
		String srcFullText = tuInfo.getSrcFullText();
		String temp = IntelligentTagPrcessor.intelligentAppendTag(srcFullText, content);		
		bf.append(temp);
		bf.append("</target>");
		return bf.toString();
	}

	/**
	 * 根据匹配结果生成alt-trans节点
	 * @param altMatchs
	 *            　所有匹配信息
	 * @return 生成alt-trans节点;
	 */
	private String generateAltTransUnitNodeXML(List<FuzzySearchResult> fuzzyResults) {
		List<AltTransBean> altTransBeans = TmUtils.fuzzyResult2Alttransbean(fuzzyResults);

		StringBuffer bf = new StringBuffer();
		for (AltTransBean bean : altTransBeans) {
			bf.append(bean.toXMLString());
		}
		return bf.toString();
	}

	/**
	 * 字符串状态的匹配率排序器
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	private final class SimilarityComparator implements Comparator<String> {
		public SimilarityComparator() {
		}

		public int compare(String o1, String o2) {
			try {
				Integer a = Integer.parseInt(o1.endsWith("%") ? o1.substring(0, o1.length() - 1) : o1);
				Integer b = Integer.parseInt(o2.endsWith("%") ? o2.substring(0, o2.length() - 1) : o2);
				if (a < b) {
					return 1;
				} else {
					return -1;
				}
			} catch (Exception e) {
				return 0;
			}
		}
	}

	/**
	 * 查找匹配结果排序器
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	private final class FindMatchComparator implements Comparator<Hashtable<String, String>> {
		public FindMatchComparator() {
		}

		public int compare(Hashtable<String, String> a, Hashtable<String, String> b) {
			Integer a1 = Integer.parseInt(a.get("similarity"));
			Integer b1 = Integer.parseInt(b.get("similarity"));
			if (a1 < b1) {
				return 1;
			} else {
				return -1;
			}
		}
	}
}
