/**
 * ISimpleMatcher.java
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
package net.heartsome.cat.ts.tm.simpleMatch;

import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;

/**
 * 简单匹配接口定义<br>
 * 简单匹配是指，简单的传入需要翻译的内容和源语言代码、目标语言代码后，返回对应的译文
 * @author jason
 * @version
 * @since JDK1.6
 */
public interface ISimpleMatcher {

	/**
	 * 获取匹配的类型简称，如机器翻译则近回MT，用于显示到匹配视图中
	 * @return ;
	 */
	String getMatcherType();

	/**
	 * 获取匹配的来源
	 * @return ;
	 */
	String getMathcerOrigin();

	/**
	 * 获取匹配算法来源，如google翻译，用于显示到匹配的属性中
	 * @return ;
	 */
	String getMathcerToolId();

	/**
	 * 检查当前翻译是否可用<br>
	 * 如果当前没有通过验证，返回false<br>
	 * 如果当前访问方式为手动，返回false
	 * @return true可用,false不可以用;
	 */
	boolean matchChecker();

	/**
	 * 执行翻译
	 * @param tuInfo
	 *            翻译时需要的翻译单元信息
	 * @return String 翻译结果
	 */
	String executeMatch(TransUnitInfo2TranslationBean tuInfo);

	/**
	 * 判断是否支持预翻译，如果当产翻译不可用，也将返回false
	 * @return true 支持预翻译，false不支持预翻译
	 */
	boolean isSuportPreTrans();

	/**
	 * 是否覆盖当前已经存在翻译<br>
	 * 根据种简单翻译定义，一种匹配算法只会存在一个匹配，此方法用于确定是否覆盖上一次的匹配<br>
	 * 如google翻译，是否进行二次请求
	 * @return ;
	 */
	boolean isOverwriteMatch();

}
