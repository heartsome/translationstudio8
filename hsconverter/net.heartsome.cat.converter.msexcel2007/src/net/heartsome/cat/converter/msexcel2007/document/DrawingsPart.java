/**
 * DrawingsPart.java
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.heartsome.cat.converter.msexcel2007.common.Constants;
import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.common.SpreadsheetUtil;
import net.heartsome.cat.converter.msexcel2007.document.drawing.CellAnchor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class DrawingsPart extends AbstractPart {

	// private RelsPart rels;

	private List<CellAnchor> cellAnchorList;

	public DrawingsPart(String target) throws InternalFileException, FileNotFoundException {
		super(SpreadsheetUtil.getPartXml(target, false));

		// spreadsheet package root is "drawings"
		// rels = new RelsPart(this); // load the relationship of this part

		loadShapeAnchor();
	}

	public List<CellAnchor> getCellAnchorList() {
		return this.cellAnchorList;
	}

	public void updateDrawingObject() throws InternalFileException {
		StringBuffer bf = new StringBuffer();
		for (CellAnchor c : cellAnchorList) {
			c.updateCellAnchor();
			bf.append(c.getXmlContent());
		}
		vu.update(getAutoPilot(), xm, "/xdr:wsDr/text()", bf.toString());
		save();
	}

	private void loadShapeAnchor() {
		cellAnchorList = new ArrayList<CellAnchor>();
		AutoPilot ap = getAutoPilot();
		try {
			ap.selectXPath("/xdr:wsDr/xdr:twoCellAnchor | xdr:oneCellAnchor");
			while (ap.evalXPath() != -1) {
				String anchorXML = vu.getElementFragment();
				String fromCol = vu.getElementContent("./xdr:from/xdr:col");
				String fromRow = vu.getElementContent("./xdr:from/xdr:row");
				CellAnchor a = new CellAnchor(anchorXML, fromRow, fromCol);
				cellAnchorList.add(a);
			}
		} catch (VTDException e) {

		}

		// sort by row&col ASC
		if (cellAnchorList.size() > 0) {
			Collections.sort(cellAnchorList, new Comparator<CellAnchor>() {

				public int compare(CellAnchor o1, CellAnchor o2) {
					int o1r = Integer.parseInt(o1.getFromRow());
					int o2r = Integer.parseInt(o2.getFromRow());

					int o1c = Integer.parseInt(o1.getFromCol());
					int o2c = Integer.parseInt(o2.getFromCol());
					if (o1r != o2r) {
						return o1r - o2r;
					} else {
						return o1c - o2c;
					}
				}
			});
		}
	}

	private AutoPilot getAutoPilot() {
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.declareXPathNameSpace(Constants.NAMESPACE_DRAWING_PREFIX_XDR, Constants.NAMESPACE_DRAWING_URL_XDR);
		ap.declareXPathNameSpace(Constants.NAMESPACE_DRAWING_PREFIX_A, Constants.NAMESPACE_DRAWING_URL_A);
		return ap;
	}
}
