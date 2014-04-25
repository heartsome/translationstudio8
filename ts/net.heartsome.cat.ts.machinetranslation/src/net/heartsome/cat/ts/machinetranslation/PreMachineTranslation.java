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
package net.heartsome.cat.ts.machinetranslation;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.core.bean.AltTransBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.machinetranslation.bean.PreMachineTransParameters;
import net.heartsome.cat.ts.machinetranslation.bean.PreMachineTranslationCounter;
import net.heartsome.cat.ts.machinetranslation.resource.Messages;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;
import net.heartsome.cat.ts.tm.simpleMatch.SimpleMatcherFactory;
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
 * 预存机器翻译
 * @author yule
 * @version
 * @since JDK1.6
 */
/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class PreMachineTranslation {
	
	public static final Logger logger = LoggerFactory.getLogger(PreMachineTranslation.class);

	private PreMachineTransParameters parameters;


	/** 项目中的XLIFF文件解析 */
	private XLFHandler xlfHandler;

	/** 项目中的XLIFF文件路径,绝对路径 */
	private List<String> xlfFiles;

	private List<PreMachineTranslationCounter> transCounters;

	private PreMachineTranslationCounter currentCounter;
	/**
	 * 需要的机器翻译服务
	 */
	private List<ISimpleMatcher> simpleMatchers;


	public PreMachineTranslation(XLFHandler xlfHandler, List<String> xlfFiles, IProject currIProject,
			PreMachineTransParameters parameters) {
		this.xlfHandler = xlfHandler;
		this.xlfFiles = xlfFiles;
		this.parameters = parameters;
		transCounters = new ArrayList<PreMachineTranslationCounter>();
	}

	/**
	 * 根据构建参数执行预翻译 ;
	 * @throws InterruptedException
	 */
	public List<PreMachineTranslationCounter> executeTranslation(IProgressMonitor monitor) throws InterruptedException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		simpleMatchers = getMacthers();
		if (null == simpleMatchers || simpleMatchers.isEmpty()) {
			return this.transCounters;
		}

		monitor.beginTask("", this.xlfFiles.size());
		monitor.setTaskName(Messages.getString("pretranslation.PreTranslation.task1"));
		try {
			for (String xlfPath : xlfFiles) {
				if (monitor != null && monitor.isCanceled()) {
					throw new InterruptedException();
				}

				currentCounter = new PreMachineTranslationCounter(xlfPath);
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
					keepCurrentMatchs(vu, _srcLang, _tgtLang, xm, monitor2);
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
		this.transCounters.clear();
		this.transCounters = null;
	}

	private void keepCurrentMatchs(VTDUtils vu, String srcLang, String tgtLang, XMLModifier xm, IProgressMonitor monitor)
			throws NavException, XPathParseException, XPathEvalException, ModifyException,
			UnsupportedEncodingException, InterruptedException {
		AutoPilot tuAp = new AutoPilot(vu.getVTDNav());
		tuAp.selectXPath("./body//trans-unit");

		while (tuAp.evalXPath() != -1) { // 循环 Trans-unit
			
			if (monitor != null && monitor.isCanceled()) {
				throw new InterruptedException();
			}

			
			if (parameters.isIgnoreLock()) {// 1、如果忽略锁定文本
				String locked = vu.getCurrentElementAttribut("translate", "yes");
				if (locked.equals("no")) {
					currentCounter.countLockedContextmatch();
					continue;
				}
			}
			
			
			if(parameters.isIgnoreExactMatch()){// 2、如果忽略匹配率				
				String qualityValue = vu.getElementAttribute("./target", "hs:quality");
				if(null != qualityValue && !qualityValue.isEmpty()){
					qualityValue = qualityValue.trim();
					if (qualityValue.equals("100") || qualityValue.equals("101")) {
						currentCounter.countLockedFullMatch();
						continue;
					}	
				}
				
			}
			

			TransUnitInfo2TranslationBean tuInfo = getTransUnitInfo(vu);
			if (tuInfo == null) {
				continue;
			}
			tuInfo.setSrcLanguage(srcLang);
			tuInfo.setTgtLangugage(tgtLang);

			updateXliffFile(vu, tuInfo, xm);

			monitor.worked(1);
		}
	}

	/**
	 * 将机器翻译结果存入XLIFF
	 * @param vu
	 * @param tuInfo
	 * @param xm
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 * @throws ModifyException
	 * @throws UnsupportedEncodingException
	 *             ;
	 */
	private void updateXliffFile(VTDUtils vu, TransUnitInfo2TranslationBean tuInfo, XMLModifier xm)
			throws XPathParseException, XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {

		String targetContent = "";
		String simpleMatchContent = executeSimpleMatch(vu, tuInfo, xm);
		targetContent += simpleMatchContent;
		if (targetContent.length() > 0) {
			currentCounter.countTransTu();
			xm.insertBeforeTail(targetContent);
		}

	}

	private List<ISimpleMatcher> getMacthers() {
		List<ISimpleMatcher> spMatchers = SimpleMatcherFactory.getInstance().getCuurentMatcher();
		List<ISimpleMatcher> useableSimeMathers =new ArrayList<ISimpleMatcher>();

		
		if (parameters.isBingTranslate()) {// 1、是否勾选bing翻译
			for (ISimpleMatcher matcher : spMatchers) {
				if (matcher.getMathcerOrigin().equals("Bing Translation")) {
					useableSimeMathers.add(matcher);
					break;
				}
			}
		
		}
		
		if (parameters.isGoogleTranslate()) {// 2、是否勾选google翻译
			for (ISimpleMatcher matcher : spMatchers) {
				if (matcher.getMathcerOrigin().equals("Google Translation")) {
					useableSimeMathers.add(matcher);
					break;
				}
			}
			
		}
		return useableSimeMathers;
	}

	/**
	 * 访问机器翻译API
	 * @param vu
	 * @param tuInfo
	 * @param xm
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 *             ;
	 */
	private String executeSimpleMatch(VTDUtils vu, TransUnitInfo2TranslationBean tuInfo, XMLModifier xm)
			throws XPathParseException, XPathEvalException, NavException {

		StringBuffer bf = new StringBuffer();

		for (ISimpleMatcher matcher : simpleMatchers) {
			// 1、是否支持预存机器翻译
			if (!matcher.isSuportPreTrans()) {
				continue;
			}

			String toolId = matcher.getMathcerToolId();
			vu.getVTDNav().push();
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("./alt-trans[@tool-id='" + toolId + "']");
			// 3、 是否有预存翻译，有预存的机器翻译，不进行预存
			if (ap.evalXPath() != -1) {
				vu.getVTDNav().pop();
				continue;
			}
			vu.getVTDNav().pop();

			String tgtText = matcher.executeMatch(tuInfo);
			if (tgtText.equals("")) {
				continue;
			}

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


}
