/**
 * SimpleMatcherGoogleImpl.java
 *
 * Version information :
 *
 * Date:2012-5-13
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.machinetranslation;



import net.heartsome.cat.common.util.InnerTagClearUtil;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.machinetranslation.bean.PrefrenceParameters;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;

import com.google.api.GoogleAPI;
import com.google.api.GoogleAPIException;
import com.google.api.translate.Translate;

/**
 * http://code.google.com/p/google-api-translate-java/
 * @author jason
 * @version
 * @since JDK1.6
 */
public class SimpleMatcherGoogleImpl implements ISimpleMatcher {

	private PrefrenceParameters parameters = PrefrenceParameters.getInstance();
	private String type = "Google";
	private String toolId = "Google Translation";
	private String origin = "Google Translation";

	/**
	 * 
	 */
	public SimpleMatcherGoogleImpl() {
		String googleKey = this.parameters.getGoolgeKey();
		GoogleAPI.setHttpReferrer("http://www.heartsome.net");
		GoogleAPI.setKey(googleKey);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher#matchChecker()
	 */
	public boolean matchChecker() {
		if (!parameters.isGoogleState()) {
			return false;
		}
		if(parameters.isManualAccess()){
			return false;
		}
		return true;
	}



	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher#executeMatch(net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean)
	 */
	public String executeMatch(TransUnitInfo2TranslationBean tuInfo) {
		try {
			String srcText = tuInfo.getSrcPureText();
			String srcLang = tuInfo.getSrcLanguage();
			String tgtLang = tuInfo.getTgtLangugage();
			String result = Translate.DEFAULT.execute(srcText, GoogleTransUtils.processLanguage(srcLang),
					GoogleTransUtils.processLanguage(tgtLang));
			result=TextUtil.convertMachineTranslateResult(result);
			if (result != null) {
				return InnerTagClearUtil.clearTmx4Xliff(result);
			}
		} catch (GoogleAPIException e) {
			return "";
		}
		return "";
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher#isSuportPreTrans()
	 */
	public boolean isSuportPreTrans() {
		if (parameters != null) {
			return  parameters.isGoogleState();
		}
		return false;
	}

	public boolean isOverwriteMatch() {
		/*if (parameters != null) {
			return parameters.isAlwaysAccess();
		}*/
		return false;
	}

	public String getMatcherType() {
		return type;
	}

	public String getMathcerToolId() {
		return toolId;
	}

	public String getMathcerOrigin() {
		return origin;
	}
}
