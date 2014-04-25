/**
 * TmxReaderEvent.java
 *
 * Version information :
 *
 * Date:2013-1-29
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document;

import net.heartsome.cat.common.bean.TmxTU;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxReaderEvent {
	public static final int NORMAL_READ = -1;
	public static final int END_FILE = 0;
	public static final int ERROR_TU = 1;
	public static final int READ_EXCEPTION = 2;

	private TmxTU tu;
	private int state;

	public TmxReaderEvent(TmxTU tu) {
		this.tu = tu;
	}

	public TmxReaderEvent(TmxTU tu, int state) {
		this(tu);
		this.state = state;
	}

	/** @return the tu */
	public TmxTU getTu() {
		return tu;
	}

	/**
	 * @param tu
	 *            the tu to set
	 */
	public void setTu(TmxTU tu) {
		this.tu = tu;
	}

	/** @return the readState */
	public int getState() {
		return state;
	}

	/**
	 * @param readState
	 *            the readState to set
	 */
	public void setState(int state) {
		this.state = state;
	}

}
