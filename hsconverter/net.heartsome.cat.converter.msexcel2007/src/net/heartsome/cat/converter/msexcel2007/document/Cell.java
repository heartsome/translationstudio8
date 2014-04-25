/**
 * Cell.java
 *
 * Version information :
 *
 * Date:2012-8-3
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.converter.msexcel2007.reader.ReaderUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class Cell {
	
	private final static Logger logger = LoggerFactory.getLogger(Cell.class);
	
	private String valIdx;
	
	private String content;

	private String val;

	private Integer styleIdx;

	private String dataType;

	private ShareStringTablePart ssi;
	

	private List<Object[]> characterStyles;

	private Cell() {
		this.val = "";
		this.styleIdx = -1;
		this.dataType = "";
		this.ssi = SpreadsheetDocument.workBookPart.getShareStringTablePart();
	}

	public Cell(String value, Integer styleIndex, String dataType) {
		this();
		if (dataType.equals("s")) {
			this.valIdx = value;
			this.content = ssi.getStringItemFragment(Integer.parseInt(this.valIdx));		
			this.val = loadCellText(content);
			this.characterStyles = loadCellCharacterStyle(content, this.val);
		} else {
			this.val = value;
		}
		this.styleIdx = styleIndex;
		this.dataType = dataType;
	}
	
	public String getValue() {
		return val;
	}

	public String getFullContent(){
		return content;
	}
	
	public List<Object[]> getCellCharacterStyles() {
		return this.characterStyles;
	}

	/**
	 * 直接将值更新到Share String table中<br>
	 * 其实在这儿执行这个操作是不合理的，但是为了简化就在这儿做这个操作了<br>
	 * （更新的操作应该是在{@link SheetPart}中完成，此Cell类只是一个缓存作用）
	 * @param val ;
	 */
	public void setValue(String val) {
		if(dataType.equals("s")){
			if(characterStyles.size() != 0){
				// have character style
				ssi.updateStringItem(Integer.parseInt(this.valIdx),val+getNonTextContent());
			}else {
				// no character style
//				System.out.println(content);
				ssi.updateStringItem(Integer.parseInt(this.valIdx),"<t xml:space=\"preserve\">"+val+"</t>"+getNonTextContent());
			}
		}
		this.val = val;
	}
	
	/**
	 * 更新si节点中的内容
	 * @param content ;
	 */
	public void setShareStringItemFullContent(String content){
		content = content.replace("<si>", "").replace("</si>", "");
		ssi.updateStringItem(Integer.parseInt(this.valIdx), content);
	}
	
	/**
	 * 获取当前单元格的样式序号
	 * @return ;
	 */
	public Integer getStyleIndex() {
		return styleIdx;
	}

	public void setStyleIdx(Integer styleIdx) {
		this.styleIdx = styleIdx;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * Get rPh & phoneticPr Element fragment
	 * @return ;
	 */
	private String getNonTextContent(){
		StringBuffer result = new StringBuffer();
		VTDGen vg = new VTDGen();
		vg.setDoc(content.getBytes());
		VTDUtils vu = null;
		try {
			vg.parse(true);
			vu = new VTDUtils(vg.getNav());
	
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("/si/rPh | phoneticPr");
			while(ap.evalXPath() != -1){
				result.append(vu.getElementFragment());
			}			
		} catch (VTDException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 加载Cell的文本内容
	 * @param content
	 * @return ;
	 */
	private String loadCellText(String content) {
		String result = "";
		VTDGen vg = new VTDGen();
		vg.setDoc(content.getBytes());
		VTDUtils vu = null;
		try {
			vg.parse(true);
			vu = new VTDUtils(vg.getNav());
	
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("/si/r");
			if (ap.evalXPath() != -1) {
				StringBuffer bf = new StringBuffer();
				do {
					String tVal = vu.getChildContent("t");
					bf.append(tVal);
				} while (ap.evalXPath() != -1);
				result = bf.toString();
			} else {
				vu.pilot("/si/t");
				result = vu.getElementContent();
			}
		} catch (VTDException e) {
			e.printStackTrace();
		}
		return result;
	}

	private List<Object[]> loadCellCharacterStyle(String content, String cellText) {
		List<Object[]> result = new ArrayList<Object[]>();
		VTDGen vg = new VTDGen();	
		vg.setDoc(content.getBytes());
		VTDUtils vu = null;
		try {
			vg.parse(true);
			vu = new VTDUtils(vg.getNav());
	
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("/si/r");
			if (ap.evalXPath() != -1) {
				StringBuffer bf = new StringBuffer();
				int start = 0;
				do {
					String rPrC = vu.getChildContent("rPr");
					if (rPrC != null) {
						rPrC = "<rPr>" + rPrC + "</rPr>";
						bf.append("rPr=\"").append(ReaderUtil.cleanAttribute(rPrC)).append("\"");
					} else {
						bf.append(" ");
					}
					String tVal = vu.getChildContent("t");
					if(tVal == null || tVal.length() == 0){
						bf.delete(0, bf.length()); // clear
						continue;
					}
					int sos = cellText.indexOf(tVal, start);
					int length = tVal.length();
	
					Object[] obj = new Object[3];
					obj[0] = sos;
					obj[1] = start = sos + length;
					obj[2] = bf.toString();
					result.add(obj);
	
					bf.delete(0, bf.length()); // clear
				} while (ap.evalXPath() != -1);
			}
		} catch (VTDException e) {
			logger.error("",e);
		}
		return result;
	}
}
