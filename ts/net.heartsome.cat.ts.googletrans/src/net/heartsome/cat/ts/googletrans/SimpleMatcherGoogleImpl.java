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
package net.heartsome.cat.ts.googletrans;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.googletrans.bean.PrefrenceParameters;
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
		String googleKey = this.parameters.getKey();
		GoogleAPI.setHttpReferrer("http://www.heartsome.net");
		GoogleAPI.setKey(googleKey);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher#matchChecker()
	 */
	public boolean matchChecker() {
		if (!parameters.getState()) {
			return false;
		}
		if (parameters.isManualAccess()) {
			// 紧支持手动访问
			return false;
		}
		return true;
	}

	//
	// private boolean validator() {
	// try {
	// String result = Translate.DEFAULT.execute("test", GoogleTransUtils.processLanguage("en-US"),
	// GoogleTransUtils.processLanguage("zh-CN"));
	// if (result.equals("测试")) {
	// return true;
	// }
	// } catch (GoogleAPIException e) {
	// return false;
	// }
	// return false;
	// }

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

			// 处理字符转义,有可能翻译的字符有一部分是转义了的,而另一部没有转义
			//bug:3317 fixed by yule 2013-7-26
			String resetSpecialString = TextUtil.resetSpecialString(result);
			result = TextUtil.cleanSpecialString(resetSpecialString);
			if (result != null) {
				return result;
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
			return parameters.isSuportPreTrans() && parameters.getState();
		}
		return false;
	}

	public boolean isOverwriteMatch() {
		if (parameters != null) {
			return parameters.isAlwaysAccess();
		}
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
