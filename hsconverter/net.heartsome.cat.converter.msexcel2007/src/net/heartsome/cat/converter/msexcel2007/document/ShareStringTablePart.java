/**
 * ShareStringPart.java
 *
 * Version information :
 *
 * Date:2012-8-1
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.common.SpreadsheetUtil;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ShareStringTablePart extends AbstractPart {

	public ShareStringTablePart(String target) throws InternalFileException, FileNotFoundException {
		super(SpreadsheetUtil.getPartXml(target, false));
	}

	public String getAllStringItem() {
		vu.pilot("/sst");
		try {
			return vu.getElementContent();
		} catch (VTDException e) {
			logger.error("", e);
		}
		return null;
	}

	public int getAllStringItemCount() {
		vu.pilot("/sst");
		try {
			return vu.getChildElementsCount();
		} catch (VTDException e) {
			logger.error("", e);
		}
		return 0;
	}

	public String getStringItemFragment(int index) {
		index = index + 1;
		String xpath = "/sst/si[" + index + "]";
		try {
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				String content = vu.getElementFragment();
				return content;
			} else {
				// TODO error
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateShareStringTable(String newVal) throws IOException, InternalFileException {
		// System.out.println(newVal);
		vu.update(null, xm, "/sst/text()", newVal);
		saveAndReload();
	}

	public void updateStringItem(int index, String value) {
		index += 1;
		vu.update(null, xm, "/sst/si[" + index + "]/text()", value);
	}
}
