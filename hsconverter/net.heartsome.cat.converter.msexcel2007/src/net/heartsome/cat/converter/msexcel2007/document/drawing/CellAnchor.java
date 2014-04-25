/**
 * Anchor.java
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
public class CellAnchor extends AbstractDrawing{
	private String fromRow;
	private String fromCol;

	private List<ShapeTxBody> shapeList;

	public CellAnchor(String anchorXML, String fromRow, String fromCol) {
		super(anchorXML);
		this.fromRow = fromRow;
		this.fromCol = fromRow;
		loadShapeAnchor();
	}

	public void appendShape(ShapeTxBody shape) {
		shapeList.add(shape);
	}

	public String getFromRow() {
		return fromRow;
	}

	public void setFromRow(String fromRow) {
		this.fromRow = fromRow;
	}

	public String getFromCol() {
		return fromCol;
	}

	public void setFromCol(String fromCol) {
		this.fromCol = fromCol;
	}

	public List<ShapeTxBody> getShapeList() {
		return shapeList;
	}

	public void updateCellAnchor(){
		for(ShapeTxBody s : shapeList){
			s.updateParagragh();
		}
		String xpath = "//xdr:sp/xdr:txBody";
		AutoPilot ap = getDeclareNSAutoPilot();
		try {
			ap.selectXPath(xpath);
			int i = 0;
			while (ap.evalXPath() != -1) {
				ShapeTxBody s = shapeList.get(i);
				xm.remove();
				xm.insertAfterElement(s.getXmlContent());
				i++;
			}
			save();
			
		} catch (VTDException e) {
			logger.error("",e);
		} catch (UnsupportedEncodingException e) {
			logger.error("",e);
		} 
	}
	
	private void loadShapeAnchor() {
		this.shapeList = new ArrayList<ShapeTxBody>();
		String xpath = "//xdr:sp";
		AutoPilot ap = getDeclareNSAutoPilot();
		try {
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				AutoPilot _ap = getDeclareNSAutoPilot();
				_ap.selectXPath("./xdr:txBody");
				vu.getVTDNav().push();
				if (_ap.evalXPath() != -1) {
					ShapeTxBody s = new ShapeTxBody(vu.getElementFragment());
					shapeList.add(s);
				}
				vu.getVTDNav().pop();
			}
		} catch (VTDException e) {
			logger.error("",e);
		}
	}
}
