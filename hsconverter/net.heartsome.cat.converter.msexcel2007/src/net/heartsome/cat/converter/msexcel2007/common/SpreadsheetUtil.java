/**
 * SpreadsheetUtil.java
 *
 * Version information :
 *
 * Date:2012-8-1
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.common;

import java.io.File;
import java.io.FileNotFoundException;

import net.heartsome.cat.converter.msexcel2007.document.AbstractPart;
import net.heartsome.cat.converter.msexcel2007.document.SpreadsheetDocument;
import net.heartsome.cat.converter.msexcel2007.document.SpreadsheetPackage;
import net.heartsome.cat.converter.msexcel2007.resource.Messages;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class SpreadsheetUtil {

	public static String getPartXml(String target, boolean isBack2TopLeve) throws FileNotFoundException {
		SpreadsheetPackage spkg = SpreadsheetDocument.spreadsheetPackage;
		if (spkg == null || target == null || target.length() == 0) {
			throw new NullPointerException();
		}

		String r = spkg.getPackageFilePath(target);
		if (isBack2TopLeve) {
			spkg.back2TopLeve();
		}
		return r;
	}

	public static String getPartRelsTarget(AbstractPart part) throws FileNotFoundException {
		String containerPartXml = part.getPartXmlPath();
		File f = new File(containerPartXml);
		String partXmlName = f.getName();
		partXmlName = "_rels/" + partXmlName + ".rels";

		return getPartXml(partXmlName, true);
	}

	public static void assertNull(Object obj) throws InternalFileException {
		if (obj == null) {
			throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
		}
	}
}
