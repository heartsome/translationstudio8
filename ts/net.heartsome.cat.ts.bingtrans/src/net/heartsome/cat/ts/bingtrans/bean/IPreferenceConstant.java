/**
 * IPreferenceConstant.java
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
package net.heartsome.cat.ts.bingtrans.bean;

/**
 * bing翻译首选项常量
 * @author jason
 * @version
 * @since JDK1.6
 */
public interface IPreferenceConstant {

	/**
	 * 当前bing 翻译状态，用于记录当前是否通过了验证
	 */
	String STATE = "net.heartsome.cat.ts.bingtrans.state";  
	
	String ID = "net.heartsome.cat.ts.bingtrans.id";  
	/** bing 翻译的Key */
	String KEY = "net.heartsome.cat.ts.bingtrans.key";
	
	
	String NO_REPEATE_ACCESS = "net.heartsome.cat.ts.bingtrans.norepeate";
	
	String ALWAYS_ACCESS = "net.heartsome.cat.ts.bingtrans.always";
	
	String MANUAL_ACCESS = "net.heartsome.cat.ts.bingtrans.manual";
	
	
	/** 是否支持预翻译 */
	String PRETRANS_STATE = "net.heartsome.cat.ts.bingtrans.suportpretrans";
}
