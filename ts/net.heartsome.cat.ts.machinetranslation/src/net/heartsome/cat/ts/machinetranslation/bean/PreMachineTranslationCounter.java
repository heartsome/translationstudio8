/**
 * PreTranslationCounter.java
 *
 * Version information :
 *
 * Date:2012-5-10
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.machinetranslation.bean;

/**
 * 预翻译计数器,用于对预翻译的文件进行统计
 * @author jason
 * @version
 * @since JDK1.6
 */
public class PreMachineTranslationCounter {
	private int transTuCount = 0;
	private int lockedFullCount = 0;
	private int lockedContextCount = 0;
	private int tuNumber = 0;
	private String currentFile = "";

	public PreMachineTranslationCounter(String file) {
		this.currentFile = file;
	}

	public void countTransTu() {
		this.transTuCount++;
	}

	public void countLockedFullMatch() {
		this.lockedFullCount++;
	}

	public void countLockedContextmatch() {
		this.lockedContextCount++;
	}

	/** @return the transTuCount */
	public int getTransTuCount() {
		return transTuCount;
	}

	/** @return the lockedFullCount */
	public int getLockedFullCount() {
		return lockedFullCount;
	}

	/** @return the lockedContextCount */
	public int getLockedContextCount() {
		return lockedContextCount;
	}

	/** @return the tuNumber */
	public int getTuNumber() {
		return tuNumber;
	}

	/**
	 * @param tuNumber
	 *            the tuNumber to set
	 */
	public void setTuNumber(int tuNumber) {
		this.tuNumber = tuNumber;
	}

	/** @return the currentFile */
	public String getCurrentFile() {
		return currentFile;
	}

}
