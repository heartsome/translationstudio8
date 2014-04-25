/**
 * DatabaseService.java
 *
 * Version information :
 *
 * Date:Nov 29, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.service;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.database.Activator;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.DBServiceProvider;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.document.ExportAbstract;
import net.heartsome.cat.document.ImportAbstract;
import net.heartsome.cat.document.ImportTbx;
import net.heartsome.cat.document.ImportTmx;
import net.heartsome.cat.tmx.converter.ConverterUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库模块外部接口,供其他程序获取数据库服务
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class DatabaseService {

	public static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

	/** 成功标志 */
	public static final int SUCCESS = ImportAbstract.SUCCESS;

	/** 失败标志 */
	public static final int FAILURE = ImportAbstract.FAILURE;

	/** 不是正确的 TMX 或 TBX 文件 */
	public static final int FAILURE_1 = ImportAbstract.FAILURE_1;

	/** 解析文件失败 */
	public static final int FAILURE_2 = ImportAbstract.FAILURE_2;

	/** 数据库异常 */
	public static final int FAILURE_3 = ImportAbstract.FAILURE_3;

	/** XPath 错误 */
	public static final int FAILURE_4 = ImportAbstract.FAILURE_4;

	/** 取消标志 */
	public static final int CANCEL = ImportAbstract.CANCEL;;

	/**
	 * 将TMX文件导入到指定的数据库中
	 * @param metaData
	 *            数据库元数据，封装了一系列连接数据库的参数，用于连接数据，参考{@link MetaData}
	 * @param fileName
	 *            TMX文件完整路径
	 * @param monitor
	 *            需要使用的进度条，如果不需要使用进度条，则为null
	 * @param strategy
	 *            TMX导入策略，参考{@link Constants}中的定义
	 * @param checkContext
	 *            TMX导入时，是否需要检查上下文
	 * @return 导入结果，为int型数据，参考{@link ImportAbstract}中的定义;
	 * @throws ImportException
	 */
	public static int importTmxWithFile(MetaData metaData, String fileName, IProgressMonitor monitor, int strategy,
			boolean checkContext) throws ImportException {
		// add convert to tmx method
		File convert2Tmx = null;
		try {
			convert2Tmx = ConverterUtil.convert2Tmx(fileName, monitor);
		} catch (ImportException e) {
			LOGGER.error("", e);
			throw new ImportException(e.getMessage().replace("\n", " "));
		}
		File file = null;
		if (convert2Tmx != null) {
			file = convert2Tmx;
		} else {
			file = new File(fileName);
		}
		if (file.exists() || !file.isDirectory()) {
			// try {
			// // 检查原始文件是否能通过 VTD 的解析
			// new TmxReader(file);
			// } catch (TmxReadException e1) {
			// throw new ImportException(e1.getMessage().replace("\n", " "));
			// }
			// File _file = Utils.clearTmxFile(file);
			DBOperator dbOp = getDBOperator(metaData);
			try {
				dbOp.start();
				// ImportAbstract impOp = new ImportTmx(dbOp, strategy, checkContext);
				// int result = impOp.doImport(_file.getAbsolutePath(), monitor);
				// return result;
				ImportTmx i = new ImportTmx(dbOp, strategy, monitor);
				i.importTmxFile(file);
				return 1;
			} catch (SQLException e) {
				LOGGER.error(Messages.getString("service.DatabaseService.logger1")
						+ dbOp.getMetaData().getDatabaseName(), e);
				return ImportAbstract.FAILURE_3;
			} catch (ClassNotFoundException e) {
				LOGGER.error(Messages.getString("service.DatabaseService.logger2"), e);
				return ImportAbstract.FAILURE_3;
			} finally {
				if (dbOp != null) {
					try {
						dbOp.end();
					} catch (SQLException e) {
						LOGGER.error(Messages.getString("service.DatabaseService.logger3"), e);
					}
				}
				if (null != convert2Tmx) {
					convert2Tmx.delete();
				}

				// _file.delete();
			}
		}
		return ImportAbstract.FAILURE_1;
	}

	/**
	 * 将内容为TMX标准格式的字符串数据导入到指定的数据库中
	 * @param metaData
	 *            数据库元数据，封装了一系列连接数据库的参数，用于连接数据，参考{@link MetaData}
	 * @param tmxStr
	 *            内容为TMX标准格式的字符串
	 * @param monitor
	 *            需要使用的进度条，如果不需要使用进度条，则为null
	 * @param strategy
	 *            TMX导入策略，参考{@link Constants}中的定义
	 * @param checkContext
	 *            TMX导入时，是否需要检查上下文
	 * @param sourceLanguage
	 *            该字符串内容的TMX源语言
	 * @return 导入结果，为int型数据，参考{@link ImportAbstract}中的定义;
	 * @throws ImportException
	 */
	public static int importTmxWithString(MetaData metaData, String tmxStr, IProgressMonitor monitor, int strategy,
			boolean checkContext, String sourceLanguage) throws ImportException {
		if (tmxStr != null && tmxStr.length() > 0) {
			DBOperator dbOp = getDBOperator(metaData);
			try {
				dbOp.start();
				// ImportAbstract impOp = new ImportTmx(dbOp, strategy, checkContext);
				// int result = impOp.doImport(tmxStr, sourceLanguage, monitor);
				ImportTmx i = new ImportTmx(dbOp, strategy, monitor);
				i.importTmxContent(tmxStr);
				return 1;
			} catch (SQLException e) {
				LOGGER.error(Messages.getString("service.DatabaseService.logger4")
						+ dbOp.getMetaData().getDatabaseName(), e);
				return ImportAbstract.FAILURE_3;
			} catch (ClassNotFoundException e) {
				LOGGER.error(Messages.getString("service.DatabaseService.logger5"), e);
				return ImportAbstract.FAILURE_3;
			} finally {
				if (dbOp != null) {
					try {
						dbOp.end();
					} catch (SQLException e) {
						LOGGER.error(Messages.getString("service.DatabaseService.logger6"), e);
					}
				}
			}
		}
		return ImportAbstract.FAILURE;
	}

	/**
	 * 将内容为TMX标准格式的字符串数据导入到指定的数据库中,数据库连接由外部管理
	 * @param dbOp
	 * @param tmxStr
	 * @param monitor
	 * @param strategy
	 * @param checkContext
	 * @param sourceLanguage
	 * @return ;
	 * @throws ImportException
	 */
	public static int importTmxWithString(DBOperator dbOp, String tmxStr, IProgressMonitor monitor, int strategy,
			boolean checkContext, String sourceLanguage) throws ImportException {
		// ImportAbstract impOp = new ImportTmx(dbOp, strategy, checkContext);
		// int result = impOp.doImport(tmxStr, sourceLanguage, monitor);
		ImportTmx i = new ImportTmx(dbOp, strategy, monitor);
		i.importTmxContent(tmxStr);
		return 1;
	}

	public static int importTbxWithString(String tbxStr, IProgressMonitor monitor, DBOperator dbOp, int strategy,
			String srcLang) throws ImportException {
		ImportAbstract impOp = new ImportTbx(dbOp, strategy);
		int result = impOp.doImport(tbxStr, srcLang, monitor);
		return result;
	}

	/**
	 * 将TBX文件导入到指定的数据库中
	 * @param fileName
	 *            TBX文件完整路径
	 * @param monitor
	 *            需要使用的进度条，如果不需要使用进度条，则为null
	 * @param metaData
	 *            数据库元数据，封装了一系列连接数据库的参数，用于连接数据，参考{@link MetaData}
	 * @param strategy
	 *            TBX导入策略，参考{@link Constants}中的定义
	 * @return 导入结果，为int型数据，参考{@link ImportAbstract}中的定义;;
	 * @throws ImportException
	 */
	public static int importTbxWithFile(String fileName, IProgressMonitor monitor, MetaData metaData, int strategy)
			throws ImportException {
		File file = new File(fileName);
		if (file.exists() || !file.isDirectory()) {
			if (!fileName.toLowerCase().endsWith(".tbx")) {
				monitor.beginTask("", 100);
				File convet2tbx = null;
				try {
					convet2tbx = ConverterUtil.convert2Tbx(fileName, new SubProgressMonitor(monitor, 30));
				} catch (OperationCanceledException e) {
					return CANCEL;
				} catch (ImportException e) {
					LOGGER.error("", e);
					throw new ImportException(e.getMessage().replace("\n", " "));
				}
				if (convet2tbx != null) {
					file = convet2tbx;
				} else {
					file = new File(fileName);
				}
			} else {
				monitor.beginTask("", 70);
			}
			DBOperator dbOp = getDBOperator(metaData);
			try {
				dbOp.start();
				ImportAbstract impOp = new ImportTbx(dbOp, strategy);
				int result = impOp.doImport(file.getAbsolutePath(), new SubProgressMonitor(monitor, 70));
				return result;
			} catch (SQLException e) {
				LOGGER.error(Messages.getString("service.DatabaseService.logger10")
						+ dbOp.getMetaData().getDatabaseName(), e);
				return ImportAbstract.FAILURE_3;
			} catch (ClassNotFoundException e) {
				LOGGER.error(Messages.getString("service.DatabaseService.logger11"), e);
				return ImportAbstract.FAILURE_3;
			} finally {
				if (dbOp != null) {
					try {
						dbOp.end();
					} catch (SQLException e) {
						LOGGER.error(Messages.getString("service.DatabaseService.logger12"), e);
					}
				}
				monitor.done();
			}

		}
		return ImportAbstract.FAILURE;
	}

	/**
	 * 获取数据处理对象{@link DBOperator},该数据处理对象用于访问指定的数据库中的数据<br>
	 * 在访问数据库时,根据不同数据库类型获取相应的处理对象.这些数据库被封装在{@link MetaData}中
	 * @param metaData
	 *            当前数据库的元数据,封装了连接数据库的一系列参数
	 * @return 返回null时,表示未获取到数据处理对象;
	 */
	public static DBOperator getDBOperator(MetaData metaData) {
		DBOperator dbOperator = null;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ServiceTracker tracker = new ServiceTracker(Activator.context, DBServiceProvider.class.getName(), null);
		tracker.open();
		try {
			Object[] services = tracker.getServices();
			for (Object i : services) {
				DBServiceProvider serviceProvider = (DBServiceProvider) i;
				DBOperator temp = serviceProvider.getTmDatabaseInstance();
				if (temp == null) {
					continue;
				}
				if (temp.getDbConfig().getDefaultType().equals(metaData.getDbType())) {
					dbOperator = temp;
					dbOperator.setMetaData(metaData);
				}
			}
		} finally {
			tracker.close();
		}
		return dbOperator;
	}

	/**
	 * 根据当前系统支持的数据库类型,获取所有的库处理对象,<br>
	 * 该库处理对象是对服务器中的数据库进行管理,包括创建,删除等一系列操作.参见{@link SystemDBOperator} *
	 * @return 返回当前系统所支持的库处理对象集;
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<SystemDBOperator> getSystemDbOperaterList() {
		List<SystemDBOperator> list = new ArrayList<SystemDBOperator>();
		ServiceTracker operateDbTracker = new ServiceTracker(Activator.context, DBServiceProvider.class.getName(), null);
		operateDbTracker.open();
		try {
			Object[] services = operateDbTracker.getServices();
			for (Object i : services) {
				DBServiceProvider temp = (DBServiceProvider) i;
				list.add(temp.getOperateDBInstance());
			}
		} finally {
			operateDbTracker.close();
		}
		if (list.size() == 0) {
			return list;
		}
		int size = list.size();
		SystemDBOperator[] temp = new SystemDBOperator[size];
		for (SystemDBOperator op : list) {
			if (op.getDBConfig().getDefaultType().equals(Constants.DBTYPE_INTERNALDB)) {
				temp[size - 1] = op;
			}
			if (op.getDBConfig().getDefaultType().equals(Constants.DBTYPE_MYSQL)) {
				temp[size - 5] = op;
			}
			if (op.getDBConfig().getDefaultType().equals(Constants.DBTYPE_MSSQL2005)) {
				temp[size - 4] = op;
			}
			if (op.getDBConfig().getDefaultType().equals(Constants.DBTYPE_POSTGRESQL)) {
				temp[size - 2] = op;
			}
			if (op.getDBConfig().getDefaultType().equals(Constants.DBTYPE_Oracle)) {
				temp[size - 3] = op;
			}
			if (op.getDBConfig().getDefaultType().equals(Constants.DBTYPE_SQLITE)) {
				temp[0] = op;
			}
		}
		return Arrays.asList(temp);
	}

	/**
	 * 获取当前系统所支持的数据库类型
	 * @return ;
	 */
	public static List<String> getSystemSuportDbType() {
		List<String> list = new ArrayList<String>();
		List<SystemDBOperator> sysDbOp = getSystemDbOperaterList();
		for (int i = 0; i < sysDbOp.size(); i++) {
			list.add(sysDbOp.get(i).getMetaData().getDbType());
		}
		Collections.sort(list);
		return list;

	}

	/**
	 * 获取系统支持数据库的元数据
	 * @return ;
	 */
	public static Map<String, MetaData> getSystemSuportDbMetaData() {
		Map<String, MetaData> map = new HashMap<String, MetaData>();
		List<SystemDBOperator> sysDbOp = getSystemDbOperaterList();
		for (int i = 0; i < sysDbOp.size(); i++) {
			SystemDBOperator dbOp = sysDbOp.get(i);
			map.put(dbOp.getMetaData().getDbType(), dbOp.getMetaData());
		}
		return map;
	}

	/**
	 * 通过数据库元数据库获取数据库连接,主要取数据库类型
	 * @param metaData
	 * @return ;
	 */
	public static SystemDBOperator getSysDbOperateByMetaData(MetaData metaData) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ServiceTracker tracker = new ServiceTracker(Activator.context, DBServiceProvider.class.getName(), null);
		tracker.open();
		try {
			Object[] services = tracker.getServices();
			for (Object i : services) {
				DBServiceProvider serviceProvider = (DBServiceProvider) i;
				SystemDBOperator temp = serviceProvider.getOperateDBInstance();
				if (temp == null) {
					continue;
				}
				MetaData tempMetaData = temp.getMetaData();
				if (tempMetaData.getDbType().equals(metaData.getDbType())) {
					tempMetaData.setDatabaseName(metaData.getDatabaseName());
					tempMetaData.setDataPath(metaData.getDataPath());
					tempMetaData.setInstance(metaData.getInstance());
					tempMetaData.setPassword(metaData.getPassword());
					tempMetaData.setPort(metaData.getPort());
					tempMetaData.setServerName(metaData.getServerName());
					tempMetaData.setTB(metaData.isTB());
					tempMetaData.setTM(metaData.isTM());
					tempMetaData.setUserName(metaData.getUserName());
					return temp;
				}
			}
		} finally {
			tracker.close();
		}
		return null;
	}

	public static String executeExport(ExportAbstract export, IProgressMonitor monitor) {
		return export.executeExport(monitor);
	}

	/**
	 * 预翻译
	 * @param db
	 * @param srcText
	 * @param srcLang
	 * @param tgtLang
	 * @param minSimilarity
	 * @param caseSensitive
	 *            ;
	 */
	public static void preTrasnlationMatch(DBOperator db, String srcText, String srcLang, String tgtLang,
			int minSimilarity, boolean caseSensitive) {
	}

}
