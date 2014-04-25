package net.heartsome.cat.ts.test.basecase.common;

import static org.junit.Assert.assertTrue;

import java.util.List;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.DB;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.ExpectedResult;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.UpdateMode;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

/**
 * 专门用来从 Excel 文件中读取测试数据的类，所有存放测试数据的 Excel 表都必须使用此类规定的列头。
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class ExcelData {

	private HsRow row;

	/** 用来保存创建项目测试数据的 Excel 表格中的列头文本 */

	public static String colDBType = "DBType";
	public static String colServer = "Server";
	public static String colPort = "Port";
	public static String colInstance = "Instance";
	public static String colDBPath = "DBPath";
	public static String colUsername = "Username";
	public static String colPassword = "Password";
	public static String colSaveConn = "Save Connection";
	public static String colDBName = "DBName";
	public static String colTMDBName = "TMDBName";
	public static String colTBDBName = "TBDBName";
	public static String colExpResult = "Expected Result";
	public static String colConfirm = "Confirm";
	public static String colFilePath = "File Path";
	public static String colUpdateMode = "Update Mode";
	public static String colPrjName = "Project Name";
	public static String colRemark = "Remark";
	public static String colClient = "Client";
	public static String colCompany = "Company";
	public static String colEMail = "EMail";
	public static String colSrcLang = "Source Language";
	public static String colTgtLang = "Target Language";
	public static String colInvChar = "Invalid Character";
	public static String colDeleteContent = "Delete Content";
	public static String colFileType = "File Type";
	public static String colFileName = "File Name";
	public static String colOldName = "Old Name";
	public static String colNewName = "New Name";
	public static String colOverwrite = "Overwrite";
	public static String colLock100Match = "Lock 100% Match";
	public static String colLockContextMatch = "Lock Context Match";
	public static String colCaseSensitive = "Case Sensitive";
	public static String colMinPercentage = "Min Percentage";
	public static String colOverwriteMode = "Overwrite Mode";

	/**
	 * @param row
	 *            用以读取测试数据的 Excel 数据行对象
	 */
	public ExcelData(HsRow row) {
		this.row = row;
	}

	/**
	 * @param colHeader
	 *            Excel 列头文本，请用本类提供的常量
	 * @return 一个文本值，若不是刚好取到一个非空值，则会报错。适用于必填项 ;
	 */
	public String getText(String colHeader) {
		return row.getOneResultOfCol(colHeader);
	}

	/**
	 * @param colHeader
	 *            Excel 列头文本，请用本类提供的常量
	 * @return 一个文本值，可能为 null 或空。适用于非必填项 ;
	 */
	public String getTextOrNull(String colHeader) {
		return row.getNullOrOneResultOfCol(colHeader);
	}

	/**
	 * @param colHeader
	 *            Excel 列头文本，请用本类提供的常量
	 * @return 一个文本值，以空字符串代替 null 值。适用于非必填项 ;
	 */
	public String getTextOrEmpty(String colHeader) {
		String str = row.getNullOrOneResultOfCol(colHeader);
		return (str == null ? "" : str);
	}

	/**
	 * @param colHeader
	 *            Excel 列头文本，请用本类提供的常量
	 * @return 字符串 List，不含空值 ;
	 */
	public List<String> getTextList(String colHeader) {
		return row.getTextOfCol(colHeader);
	}

	/**
	 * @param colHeader
	 *            Excel 列头文本，请用本类提供的常量
	 * @return 字符串数组，不含空值 ;
	 */
	public String[] getTextArray(String colHeader) {
		return row.getArrayResultOfCol(colHeader);
	}

	/**
	 * @param colHeader
	 *            Excel 列头文本，请用本类提供的常量
	 * @return 布尔值 ;
	 */
	public boolean getBoolean(String colHeader) {
		return row.getBooleanResultOfCol(colHeader);
	}

	/**
	 * @return 从 Excel 中读取文字内容的预期结果，并转换为相应的枚举值供判断，若无则报错并返回 null;
	 */
	public ExpectedResult getExpectedResult() {
		String strResult = row.getOneResultOfCol(colExpResult);
		if (strResult.equals("SUCCESS")) {
			return TsUIConstants.ExpectedResult.SUCCESS;

		} else if (strResult.equals("DUPLICATED_NAME")) {
			return TsUIConstants.ExpectedResult.DUPLICATED_NAME;

		} else if (strResult.equals("INVALID_NAME")) {
			return TsUIConstants.ExpectedResult.INVALID_NAME;

		} else if (strResult.equals("NO_FILE")) {
			return TsUIConstants.ExpectedResult.NO_FILE;

		} else if (strResult.equals("NO_DB")) {
			return TsUIConstants.ExpectedResult.NO_DB;

		} else if (strResult.equals("FILE_ERROR")) {
			return TsUIConstants.ExpectedResult.FILE_ERROR;

		} else if (strResult.equals("WRONG_TYPE")) {
			return TsUIConstants.ExpectedResult.WRONG_TYPE;

		} else if (strResult.equals("INVALID_FILE")) {
			return TsUIConstants.ExpectedResult.INVALID_FILE;

		} else if (strResult.equals("INVALID_PATH")) {
			return TsUIConstants.ExpectedResult.INVALID_PATH;

		} else if (strResult.equals("NO_SERVER")) {
			return TsUIConstants.ExpectedResult.NO_SERVER;

		} else if (strResult.equals("NO_PORT")) {
			return TsUIConstants.ExpectedResult.NO_PORT;

		} else if (strResult.equals("NO_INSTANCE")) {
			return TsUIConstants.ExpectedResult.NO_INSTANCE;

		} else if (strResult.equals("NO_USERNAME")) {
			return TsUIConstants.ExpectedResult.NO_USERNAME;

		} else if (strResult.equals("NO_PATH")) {
			return TsUIConstants.ExpectedResult.NO_PATH;

		} else if (strResult.equals("CONNECTION_ERROR")) {
			return TsUIConstants.ExpectedResult.CONNECTION_ERROR;

		} else if (strResult.equals("LONG_NAME")) {
			return TsUIConstants.ExpectedResult.LONG_NAME;

		} else {
			assertTrue("无此预期结果：" + strResult, false);
			return null;
		}
	}

	/**
	 * @return 从 Excle 文件中读取的数据库更新模式;
	 */
	public UpdateMode getUpdateMode() {
		String strResult = row.getOneResultOfCol(colUpdateMode);
		if (strResult.equals("ALWAYS_ADD")) {
			return TsUIConstants.UpdateMode.ALWAYS_ADD;

		} else if (strResult.equals("OVERWRITE")) {
			return TsUIConstants.UpdateMode.OVERWRITE;

		} else if (strResult.equals("IGNORE")) {
			return TsUIConstants.UpdateMode.IGNORE;

		} else if (strResult.equals("MERGE")) {
			return TsUIConstants.UpdateMode.MERGE;

		} else {
			assertTrue("参数错误，无此更新模式：" + strResult, false);
			return null;
		}
	}

	/**
	 * @return 从 Excel 文件中读取到的数据库类型，转为常量;
	 */
	public DB getDBType() {
		String strType = row.getOneResultOfCol(colDBType);
		if (strType.equals("INTERNAL")) {
			return TsUIConstants.DB.INTERNAL;
		} else if (strType.equals("MYSQL")) {
			return TsUIConstants.DB.MYSQL;
		} else if (strType.equals("ORACLE")) {
			return TsUIConstants.DB.ORACLE;
		} else if (strType.equals("POSTGRESQL")) {
			return TsUIConstants.DB.POSTGRESQL;
		} else if (strType.equals("MSSQL")) {
			return TsUIConstants.DB.MSSQL;
		} else {
			assertTrue("无此数据库类型：" + strType, false);
			return null;
		}
	}

	/**
	 * @return 从 Excel 中读取的覆盖模式/策略;
	 */
	public UpdateMode getOverwriteMode() {
		String strResult = row.getOneResultOfCol(colOverwriteMode);
		if (strResult.equals("KEEP_CURRENT")) {
			return TsUIConstants.UpdateMode.KEEP_CURRENT;
		} else if (strResult.equals("OVERWRITE_IF_HIGHER")) {
			return TsUIConstants.UpdateMode.OVERWRITE_IF_HIGHER;
		} else if (strResult.equals("ALWAYS_OVERWRITE")) {
			return TsUIConstants.UpdateMode.OVERWRITE;
		} else {
			assertTrue("无此覆盖模式：" + strResult, false);
			return null;
		}
	}

}
