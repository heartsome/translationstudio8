package net.heartsome.cat.converter.msexcel2007.document;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.common.SpreadsheetUtil;
import net.heartsome.cat.converter.msexcel2007.document.rels.Relationship;
import net.heartsome.cat.converter.msexcel2007.resource.Messages;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;

public class SheetPart extends AbstractPart {

	private String name;
	private String sheetId;

	private RelsPart relsPart;
	private DrawingsPart drawingsPart;
	private List<Cell> cellList;

	private List<HeaderFooter> headers;
	private List<HeaderFooter> footers;

	public SheetPart(String name, String sheetId, String target) throws InternalFileException, FileNotFoundException {
		super(SpreadsheetUtil.getPartXml(target, false));

		this.name = name;
		this.sheetId = sheetId;
		try {
			this.relsPart = new RelsPart(this); // load relationship
		} catch (FileNotFoundException e) {
			// no rels resource
			this.relsPart = null;
		}

		SpreadsheetDocument.spreadsheetPackage.back2TopLeve();
		if (this.relsPart != null) {
			loadDrawingsPart();
		}
		loadHeaderFooter();
	}

	public List<Cell> getCells(String dataType) throws InternalFileException {
		cellList = new ArrayList<Cell>();
		String xpath = "/worksheet/sheetData/row/c[@t='" + dataType + "']";
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		try {
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				Hashtable<String, String> attrs = vu.getCurrentElementAttributs();
				SpreadsheetUtil.assertNull(attrs);
				String styleIndex = attrs.get("s");
				styleIndex = styleIndex == null ? "-1" : styleIndex;
				String val = vu.getChildContent("v");
				val = val == null ? "" : val;
				Cell cell = new Cell(val, Integer.parseInt(styleIndex), dataType);
				cellList.add(cell);
			}
		} catch (VTDException e) {
			logger.error("", e);
		}
		return cellList;
	}

	/***
	 * 将Share String Tbale中的数据填充满，即每一个引用都对应独立的String item
	 * @param sst
	 *            ;
	 * @throws InternalFileException
	 */
	public int fillShareStringTable(ShareStringTablePart sst, int index,StringBuffer siBf) throws InternalFileException {
		String xpath = "/worksheet/sheetData/row/c[@t='s']/v";
		try {
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				String c = vu.getElementContent();
				int cInt = 0;
				try {
					cInt = Integer.parseInt(c);
				} catch (NumberFormatException e) {
					throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
				}
				siBf.append(sst.getStringItemFragment(cInt));
				xm.updateToken(vu.getVTDNav().getCurrentIndex() + 1, index+"");
				index++;
			}
			saveAndReload();		
		} catch (VTDException e) {
			logger.error("", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		} 
		return index;
	}

	public String getName() {
		return this.name;
	}

	public DrawingsPart getDrawingsPart() {
		return drawingsPart;
	}

	public void setSheetName(String name) {
		this.name = name;
		SpreadsheetDocument.workBookPart.updateSheetName(sheetId, name);
	}

	public List<HeaderFooter> getHeader() {
		return this.headers;
	}

	public void setHeaderFooter(List<HeaderFooter> headerFooters) {
		this.headers = headerFooters;
		for (HeaderFooter h : headerFooters) {
			String xpath = "/worksheet/headerFooter/" + h.getType() + "/text()";
			vu.update(null, xm, xpath, h.getContent());
		}
	}

	public List<HeaderFooter> getFoolter() {
		return this.footers;
	}

	private void loadDrawingsPart() throws InternalFileException {
		String xpath = "/worksheet/drawing";

		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		try {
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				Hashtable<String, String> attrs = vu.getCurrentElementAttributs();
				SpreadsheetUtil.assertNull(attrs);
				String rId = attrs.get("r:id");
				Relationship r = relsPart.getRelationshipById(rId);
				SpreadsheetUtil.assertNull(r);

				SpreadsheetDocument.spreadsheetPackage.markRoot();
				try {
					this.drawingsPart = new DrawingsPart(r.getTarget());
				} catch (FileNotFoundException e) {
					// no drawingsPart
				}
				SpreadsheetDocument.spreadsheetPackage.resetRoot();
			}
		} catch (VTDException e) {
			logger.error("", e);
		}
	}

	private void loadHeaderFooter() {
		this.headers = new ArrayList<HeaderFooter>();
		this.footers = new ArrayList<HeaderFooter>();

		String xpath = "/worksheet/headerFooter/*";
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		try {
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				String type = vu.getCurrentElementName();
				String c = vu.getElementContent();
				HeaderFooter e = new HeaderFooter(type, c);
				if (type.equals("oddHeader") || type.equals("evenHeader") || type.equals("firstHeader")) {
					headers.add(e);
				} else if (type.equals("oddFooter") || type.equals("evenFooter") || type.equals("firstFooter")) {
					footers.add(e);
				}
			}
		} catch (VTDException e) {
			logger.error("", e);
		}
	}
}
