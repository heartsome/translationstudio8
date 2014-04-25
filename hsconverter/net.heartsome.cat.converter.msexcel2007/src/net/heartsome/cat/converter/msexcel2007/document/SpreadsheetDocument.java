package net.heartsome.cat.converter.msexcel2007.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.common.ZipUtil;
import net.heartsome.cat.converter.msexcel2007.resource.Messages;

public class SpreadsheetDocument {

	public static WorkBookPart workBookPart;

	public static SpreadsheetPackage spreadsheetPackage;

	public static void open(String file) throws InternalFileException {
		String root = "";
		try {
			root = ZipUtil.upZipFile(file, null);
		} catch (IOException e1) {
			file = file.substring(file.lastIndexOf(File.separator));
			throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
		}
		
		try {
			spreadsheetPackage = new SpreadsheetPackage(root);
		} catch (FileNotFoundException e) {
			throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
		} 
		
		try {
			workBookPart = new WorkBookPart();
		} catch (FileNotFoundException e) {
			throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
		}
	}

	public static void close() {
		spreadsheetPackage.close();
		spreadsheetPackage = null;
		workBookPart = null;
	}
}
