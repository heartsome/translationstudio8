/**
 * Row.java
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
package net.heartsome.cat.tmx.converter.xlsx;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.util.TextUtil;

/**
 * @author  yule
 * @version 
 * @since   JDK1.6
 */
public class Row {
	/**
     * 行号，从1开始
     */
	private int rowNumber;
	/**
	 * 每一行的表格
	 */
	private List<Cell> cells;
	
    /** @return the rowNumber */
	public int getRowNumber() {
		return rowNumber;
	}
	/** @param rowNumber the rowNumber to set */
	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}
	/** @return the cells */
	public List<Cell> getCells() {
		return cells;
	}
	/** @param cells the cells to set */
	public void setCells(List<Cell> cells) {
		this.cells = cells;
	}
	
	public void addCell(Cell e){
		if(this.cells==null){
			this.cells = new ArrayList<Cell>();
		}
		this.cells.add(e);
	}
	public TmxTU toTmxTu() {
		TmxTU tu = new TmxTU();
		TmxSegement seg = null ;
		String cellContent = null ;
		for(int i=0 ; i< cells.size() ;i++ ){
			Cell cell = cells.get(i);
			cellContent =cell.getCellConentent();		
			if(null!= cell && null != cellContent && !cellContent.trim().isEmpty()){
				cellContent = TextUtil.cleanSpecialString(cellContent);
				seg = new TmxSegement();
				seg.setFullText(cellContent);
				seg.setPureText(cellContent);
				seg.setLangCode(cell.getLangCode());
				if(i==0){
					tu.setSource(seg);
				}else{
					tu.appendSegement(seg);
				}	
			}
			
		}		
		return tu;
	}
}
