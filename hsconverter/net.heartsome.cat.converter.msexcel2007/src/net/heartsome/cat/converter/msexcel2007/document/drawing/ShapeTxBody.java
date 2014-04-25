/**
 * Shape.java
 *
 * Version information :
 *
 * Date:2012-8-7
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document.drawing;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ShapeTxBody extends AbstractDrawing {

	private List<ShapeParagraph> pList;

	public ShapeTxBody(String txBodyXml) {
		super(txBodyXml);
		loadParagraph();
	}

	public List<ShapeParagraph> getTxBodyParagraghList() {
		return pList;
	}

	void updateParagragh(){
		String xpath = "/xdr:txBody/a:p";
		AutoPilot ap = getDeclareNSAutoPilot();
		try {
			ap.selectXPath(xpath);
			int i = 0;
			while (ap.evalXPath() != -1) {
				ShapeParagraph p = pList.get(i);
				xm.remove();
				xm.insertAfterElement(p.getXmlContent());
				i++;
			}
			save();
			
		} catch (VTDException e) {
			logger.error("",e);
		} catch (UnsupportedEncodingException e) {
			logger.error("",e);
		} 
	}
	
	private void loadParagraph() {
		pList = new ArrayList<ShapeParagraph>();
		String xpath = "/xdr:txBody/a:p";
		AutoPilot ap = getDeclareNSAutoPilot();
		try {
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				ShapeParagraph p = new ShapeParagraph(vu.getElementFragment());
				pList.add(p);
			}
		} catch (VTDException e) {
			logger.error("",e);
		}
	}
}
