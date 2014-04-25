/**
 * Cell.java
 *
 * Version information :
 *
 * Date:2013-7-17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document.xlsx;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class Cell {

	/** 单元格的序号.从A,B...X,Y,Z,AA,AB,AC....AX,AY,AZ,BA...开始 */
	private String cellNumber;

	/** 单元格中的内容 */
	private String cellConentent;

	/**
	 * 单元格的序号.从A,B...X,Y,Z,AA,AB,AC....AX,AY,AZ,BA...开始
	 * @return the cellNumber
	 */
	public String getCellNumber() {
		return cellNumber;
	}

	/**
	 * @param cellNumber
	 *            the cellNumber to set
	 */
	public void setCellNumber(String cellNumber) {
		this.cellNumber = cellNumber;
	}

	/** @return the cellConentent */
	public String getCellConentent() {
		return cellConentent;
	}

	/**
	 * @param cellConentent
	 *            the cellConentent to set
	 */
	public void setCellConentent(String cellConentent) {
		this.cellConentent = cellConentent;
	}

}
