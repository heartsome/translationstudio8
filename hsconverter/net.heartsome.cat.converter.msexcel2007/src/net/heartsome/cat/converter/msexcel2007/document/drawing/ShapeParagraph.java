/**
 * ShapeParagraph.java
 *
 * Version information :
 *
 * Date:2012-8-8
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document.drawing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.converter.msexcel2007.reader.ReaderUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ShapeParagraph extends AbstractDrawing {
	/** the paragraph pure text */
	private String pText;

	/** the paragraph text style */
	private List<Object[]> characterStyles;

	public ShapeParagraph(String pXML) {
		super(pXML);
		this.pText = getCellValueText();
		this.characterStyles = getParagraghCharacterStyle(this.pText);
	}

	public String getParagraghText() {
		return this.pText;
	}

	public void setParagraghText(String newValue) {
		try {
			if (this.characterStyles.size() > 1) {
				// multi r element
				vu.pilot(getDeclareNSAutoPilot(), "/a:p/a:r");
				xm.insertAfterElement(newValue);
				vu.delete(getDeclareNSAutoPilot(), xm, "/a:p/a:r", VTDUtils.PILOT_TO_END);

			} else if (this.characterStyles.size() == 1) {
				// single r element
				vu.update(getDeclareNSAutoPilot(), xm, "/a:p/a:r/a:t/text()", newValue);
			}
			save();
			// saveAndReload();
		} catch (VTDException e) {
			logger.error("",e);
		} catch (IOException e) {
			logger.error("",e);
		}
	}

	public List<Object[]> getParagraghCharacterStyle() {
		return this.characterStyles;
	}

	private String getCellValueText() {
		String result = "";
		try {
			AutoPilot ap = getDeclareNSAutoPilot();
			ap.selectXPath("/a:p/a:r");
			if (ap.evalXPath() != -1) {
				StringBuffer bf = new StringBuffer();
				do {
					String tVal = vu.getChildContent("a:t");
					bf.append(tVal);
				} while (ap.evalXPath() != -1);
				result = bf.toString();
			}
		} catch (VTDException e) {
			logger.error("",e);
		}
		return result;
	}

	private List<Object[]> getParagraghCharacterStyle(String cellText) {
		List<Object[]> result = new ArrayList<Object[]>();
		try {
			AutoPilot ap = getDeclareNSAutoPilot();
			ap.selectXPath("/a:p/a:r");
			if (ap.evalXPath() != -1) {
				StringBuffer bf = new StringBuffer();
				do {
					String rPrC = getChilderFragment("a:rPr");
					if (rPrC != null) {
						bf.append("ctype=\"a\" rPr=\"").append(ReaderUtil.cleanAttribute(rPrC)).append("\"");
					} else {
						bf.append("ctype=\"a\">");
					}
					String tVal = vu.getChildContent("a:t");
					int sos = cellText.indexOf(tVal);
					int length = tVal.length();

					Object[] obj = new Object[3];
					obj[0] = sos;
					obj[1] = sos + length;
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

	private String getChilderFragment(String elementName) throws VTDException {
		String text = null;
		AutoPilot ap = getDeclareNSAutoPilot();
		ap.selectXPath("./" + elementName);
		vu.getVTDNav().push();
		if (ap.evalXPath() != -1) {
			text = vu.getElementFragment();
		}
		vu.getVTDNav().pop();
		return text;
	}
}
