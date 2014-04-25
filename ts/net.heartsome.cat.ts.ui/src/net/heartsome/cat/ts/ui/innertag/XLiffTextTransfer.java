/**
 * HSTextTransfer.java
 *
 * Version information :
 *
 * Date:2013-6-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.innertag;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class XLiffTextTransfer extends ByteArrayTransfer {
	static final String MIME_TYPE = "custom/HSTextTransfer";
	final int MIME_TYPE_ID = registerType(MIME_TYPE);

	private static XLiffTextTransfer _instance = new XLiffTextTransfer();

	private XLiffTextTransfer() {
	}

	/**
	 * Returns the singleton instance of the TextTransfer class.
	 * @return the singleton instance of the TextTransfer class
	 */
	public static XLiffTextTransfer getInstance() {
		return _instance;
	}

	protected int[] getTypeIds() {
		return new int[] { MIME_TYPE_ID };
	}

	protected String[] getTypeNames() {
		return new String[] { MIME_TYPE };
	}

	public void javaToNative(Object object, TransferData transferData) {
		if (!checkMyType(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		String string = (String) object;
		byte[] bytes = string.getBytes();
		if (bytes != null) {
			super.javaToNative(bytes, transferData);
		}
	}

	public Object nativeToJava(TransferData transferData) {
		if (!isSupportedType(transferData))
			return null;
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		return bytes == null ? null : new String(bytes);
	}

	boolean checkMyType(Object object) {
		return (object != null && object instanceof String && ((String) object).length() > 0);
	}

	protected boolean validate(Object object) {
		return checkMyType(object);
	}

}
