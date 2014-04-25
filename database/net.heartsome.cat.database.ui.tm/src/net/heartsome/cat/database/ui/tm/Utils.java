/**
 * Utils.java
 *
 * Version information :
 *
 * Date:2013-4-23
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tm;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tm.resource.Messages;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class Utils {
	public static Logger LOGGER = LoggerFactory.getLogger(Utils.class);
	
	public static Map<DatabaseModelBean, String> convertFile2TmModel(File f, boolean loadLang) throws Exception {
		String path = f.getParent();
		String name = f.getName();
		DatabaseModelBean selectedVal = new DatabaseModelBean();
		selectedVal.setDbName(name);
		selectedVal.setDbType(Constants.DBTYPE_SQLITE);
		selectedVal.setItlDBLocation(path);

		DBOperator dbOp = DatabaseService.getDBOperator(selectedVal.toDbMetaData());
		String lang = "";
		try {
			dbOp.start();
			Statement stmt = dbOp.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name");
			List<String> tables = new ArrayList<String>();
			while (rs.next()) {
				String tname = rs.getString(1);
				if (tname.toUpperCase().startsWith("MATRIX_")) {
					continue;
				} else {
					tables.add(tname);
				}
			}
			List<String> l = Arrays.asList("BATTRIBUTE", "BMARTIFHEADER", "BNODE", "BREFOBJECTLIST", "BTERMENTRY",
					"LANG", "MEXTRA", "MHEADER", "MHEADERNODE", "MNOTE", "MPROP", "MTU", "TEXTDATA");
			if (!tables.containsAll(l)) {
				throw new Exception(Messages.getString("tm.dialog.addFileTm.error.msg1"));
			}
			if (loadLang) {
				List<String> langs = dbOp.getLanguages();
				for (int j = 0; j < langs.size(); j++) {
					lang += langs.get(j);
					if (j != langs.size() - 1) {
						lang += ",";
					}
				}
			}
			Map<DatabaseModelBean, String> result = new HashMap<DatabaseModelBean, String>();
			result.put(selectedVal, lang);
			return result;
		} catch (Exception ex) {
			LOGGER.error("", ex);
			String message = Messages.getString("tm.dialog.addFileTm.error.msg2");
			message = MessageFormat.format(message, name);
			throw new Exception(message);
		} finally {
			try {
				if (dbOp != null) {
					dbOp.end();
				}
			} catch (SQLException ex) {
				LOGGER.error("", ex);
			}
		}
	}
}
