/**
 * TmxContexts.java
 *
 * Version information :
 *
 * Date:2013-1-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.bean;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxContexts {

	/** context Prop XmlElement type attribute value in R8 */
	public static final String PRE_CONTEXT_NAME = "x-preContext";
	/** context Prop XmlElement type attribute value in R8 */
	public static final String NEXT_CONTEXT_NAME = "x-nextContext";

	private String[] preContext;
	private String[] nextContext;

	public TmxContexts() {
		preContext = new String[2];
		nextContext = new String[2];
	}

	public void appendPreContext(String val) {
		if (val == null || val.length() == 0) {
			return;
		}
		if (preContext[0] != null && preContext[1] != null) {
			String s1 = preContext[0];
			preContext[0] = val;
			preContext[1] = s1;
		} else if (preContext[1] == null) {
			preContext[1] = val;
		} else if (preContext[0] == null) {
			preContext[0] = val;
		}
	}

	public void appendNextContext(String val) {
		if (val == null || val.length() == 0) {
			return;
		}
		if (nextContext[0] != null && nextContext[1] != null) {
			String s1 = nextContext[0];
			nextContext[0] = val;
			nextContext[1] = s1;
		} else if (nextContext[1] == null) {
			nextContext[1] = val;
		} else if (nextContext[0] == null) {
			nextContext[0] = val;
		}
	}

	/** @return the nextContext */
	public String getNextContext() {
		StringBuffer bf = new StringBuffer();
		for (String s : nextContext) {
			if (s != null && s.length() != 0) {
				bf.append("," + s);
			}
		}
		if (bf.length() != 0) {
			return bf.substring(1);
		}
		return "";
	}

	/** @return the preContext */
	public String getPreContext() {
		StringBuffer bf = new StringBuffer();
		for (String s : preContext) {
			if (s != null && s.length() != 0) {
				bf.append("," + s);
			}
		}
		if (bf.length() != 0) {
			return bf.substring(1);
		}
		return "";
	}
}
