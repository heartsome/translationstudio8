/**
 * ConverterFactory.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.document.converter;

import java.util.Locale;

import net.heartsome.cat.tmx.converter.AbstractFile2Tmx;
import net.heartsome.cat.tmx.converter.AbstractTmx2File;
import net.heartsome.cat.tmx.converter.txt.Tmx2Txt;
import net.heartsome.cat.tmx.converter.txt.Txt2Tmx;
import net.heartsome.cat.tmx.converter.xlsx.Tmx2xlsx;
import net.heartsome.cat.tmx.converter.xlsx.Xlsx2Tmx;

public final class ConverterFactory {

	public final static int FILE_TYPE_DOCX = 1;

	public final static int FILE_TYPE_CSV = 2;

	public final static int FILE_TYPE_XLSX = 3;

	public final static int FILE_TYPE_TXT = 4;

	public final static int FILE_TYPE_TBX = 5;

	public final static int FILE_TYPE_HSTM = 6;

	/**
	 * Tmx转换成其他文件
	 * @param fileType
	 * @return ;
	 */
	public static AbstractTmx2File getTmx2FileConverter(int fileType) {
		switch (fileType) {
		case FILE_TYPE_XLSX:
			return new Tmx2xlsx();

		case FILE_TYPE_TXT:
			return new Tmx2Txt();

		default:
			return null;
		}

	}

	/**
	 * 其他文件转化成Tmx
	 * @param sourceFile
	 * @return ;
	 */
	public static AbstractFile2Tmx getFile2TmxConverter(String sourceFile) {
		return getFile2TmxConverter(getFileType(sourceFile));
	}

	/**
	 * 其他文件转化成Tmx
	 * @param sourceFile
	 * @return ;
	 */
	public static AbstractFile2Tmx getFile2TmxConverter(int fileType) {
		switch (fileType) {

		case FILE_TYPE_XLSX:
			return new Xlsx2Tmx();
		case FILE_TYPE_TXT:
			return new Txt2Tmx();
		default:
			return null;
		}
	}

	/**
	 * 文件名转换成文件类型
	 * @param sourceFile
	 * @return ;
	 */
	public static int getFileType(String sourceFile) {
		if (null == sourceFile || sourceFile.trim().isEmpty()) {
			return -1;
		}
		String tempName = sourceFile.toLowerCase(Locale.ENGLISH);
		if (tempName.endsWith(".docx")) {
			return FILE_TYPE_DOCX;
		} else if (tempName.endsWith(".csv")) {
			return FILE_TYPE_CSV;
		} else if (tempName.endsWith(".xlsx")) {
			return FILE_TYPE_XLSX;
		} else if (tempName.endsWith(".txt")) {
			return FILE_TYPE_TXT;
		} else if (tempName.endsWith(".tbx")) {
			return FILE_TYPE_TBX;
		} else if (tempName.endsWith(".hstm")) {
			return FILE_TYPE_HSTM;
		} else {
			return -1;
		}
	}

	public static AbstractConverter getFile2TbxConverter(String sourceFile) {
		int fileType = getFileType(sourceFile);
		if (fileType == -1) {
			return null;
		}
		switch (fileType) {

		case FILE_TYPE_XLSX:
			return new Xlsx2TbxConverter(sourceFile);
		case FILE_TYPE_TXT:
			return new Txt2TbxConverter(sourceFile);
		default:
			return null;
		}
	}

}
