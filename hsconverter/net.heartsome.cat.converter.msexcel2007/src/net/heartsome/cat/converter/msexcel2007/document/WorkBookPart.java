package net.heartsome.cat.converter.msexcel2007.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.converter.msexcel2007.common.Constants;
import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.common.SpreadsheetUtil;
import net.heartsome.cat.converter.msexcel2007.document.rels.Relationship;
import net.heartsome.cat.converter.msexcel2007.resource.Messages;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class WorkBookPart extends AbstractPart {

	private RelsPart relsPart;
	private List<SheetPart> sheetPartList;
	private ShareStringTablePart sst;

	private StylesPart stylePart;

	public WorkBookPart() throws InternalFileException, FileNotFoundException {
		super(SpreadsheetUtil.getPartXml(Constants.WORKBOOK_PATH, false));
		this.relsPart = new RelsPart(this);
		loadShareStringTable();
		loadSytlesPart();
		loadWorkSheets();
	}

	public List<SheetPart> getSheetParts() {
		return sheetPartList;
	}

	public ShareStringTablePart getShareStringTablePart() {
		return sst;
	}

	public StylesPart getStylesPart() {
		return stylePart;
	}

	private void loadWorkSheets() throws InternalFileException, FileNotFoundException {
		sheetPartList = new ArrayList<SheetPart>();
		int ssiIndex = 0;
		StringBuffer siBf = new StringBuffer();
		try {
			String xpath = "/workbook/sheets/sheet";
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				int index = vu.getVTDNav().getAttrVal("name");
				if (index == -1) {
					throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
				}
				String name = vu.getVTDNav().toRawString(index);

				index = vu.getVTDNav().getAttrVal("sheetId");
				if (index == -1) {
					throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
				}
				String id = vu.getVTDNav().toRawString(index);

				index = vu.getVTDNav().getAttrVal("r:id");
				if (index == -1) {
					throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
				}
				String rid = vu.getVTDNav().toRawString(index);

				Relationship r = relsPart.getRelationshipById(rid);
				SpreadsheetUtil.assertNull(r);
				String target = r.getTarget();
				SheetPart sp = new SheetPart(name, id, target);
				ssiIndex = sp.fillShareStringTable(sst, ssiIndex, siBf);
				sheetPartList.add(sp);
			}
			// fix bug Bug #2925 转换excel文件--空指针异常，转换失败 by jason
			if (sst != null && siBf.length() != 0) {
				sst.updateShareStringTable(siBf.toString());
			}
		} catch (VTDException e) {
			logger.error("", e);
		} catch (IOException e) {

		}
	}

	private void loadShareStringTable() throws InternalFileException {
		List<Relationship> rList = relsPart.getRelationshipByType(Constants.PART_TYPE_SST);
		if (rList.size() != 1) {
			return;
		}
		Relationship r = rList.get(0);
		try {
			sst = new ShareStringTablePart(r.getTarget());
		} catch (FileNotFoundException e) {
			// 可能没有sharedStrings.xml
		}
	}

	private void loadSytlesPart() throws InternalFileException, FileNotFoundException {
		List<Relationship> rList = relsPart.getRelationshipByType(Constants.PART_TYPE_STYLE);
		if (rList.size() != 1) {
			throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
		}
		Relationship r = rList.get(0);
		stylePart = new StylesPart(r.getTarget());
	}

	void updateSheetName(String sheetId, String sheetName) {
		String xpath = "/workbook/sheets/sheet[@sheetId = '" + sheetId + "']/@name";
		vu.update(null, xm, xpath, sheetName);
	}

	@Override
	public void save() throws InternalFileException {
		for (SheetPart s : sheetPartList) {
			s.save();
		}
		if (this.sst != null) {
			this.sst.save();
		}
		super.save();
	}
}
