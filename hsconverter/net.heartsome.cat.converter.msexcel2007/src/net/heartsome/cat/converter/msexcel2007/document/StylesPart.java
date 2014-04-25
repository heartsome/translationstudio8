/**
 * StylesPart.java
 *
 * Version information :
 *
 * Date:2012-8-2
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.common.SpreadsheetUtil;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class StylesPart extends AbstractPart {

	private List<Integer> redStyleIndexs;

	public StylesPart(String target) throws InternalFileException, FileNotFoundException {
		super(SpreadsheetUtil.getPartXml(target, false));
		loadRedFgStyle();
	}

	/**
	 * 加载红前景色样式
	 */
	private boolean isRedColor(int idx) {
		idx += 1;
		String xpath = "/styleSheet/fonts/font["+idx+"]";
		try {
			vu.getVTDNav().push();
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				String rgb = vu.getElementAttribute("./color", "rgb");
				if(rgb != null && rgb.equals("FFFF0000")){
					return true;
				}
			}
			vu.getVTDNav().pop();
		} catch (VTDException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void loadRedFgStyle(){
		redStyleIndexs = new ArrayList<Integer>();
		String xpath = "/styleSheet/cellXfs/xf";
		try {
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				String fIdx = vu.getCurrentElementAttribut("fontId", "");
				if(!fIdx.equals("") && isRedColor(Integer.parseInt(fIdx))){
					redStyleIndexs.add(vu.getVTDNav().getCurrentDepth() - 1);
				}
			}
		} catch (VTDException e) {
			e.printStackTrace();
		}
	}
	
	public List<Integer> getFilterCellStyle(){
		return this.redStyleIndexs;
	}
}
