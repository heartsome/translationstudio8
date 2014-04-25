/**
 * TmImporter.java
 *
 * Version information :
 *
 * Date:2012-5-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.tm.importer;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.tm.TmTransParamsBean;
import net.heartsome.cat.database.tm.resource.Messages;
import net.heartsome.cat.ts.core.IProjectConfigChangedListener;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.tm.importer.extension.ITmImporter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmImporter implements ITmImporter, IProjectConfigChangedListener {
	public static final Logger logger = LoggerFactory.getLogger(TmImporter.class);

	private ProjectConfiger projConfiger;
	private IProject currentProj;
	private DBOperator dbOp;
	private TmTransParamsBean transParameters = new TmTransParamsBean();

	/**
	 * 
	 */
	public TmImporter() {
	}

	/**
	 * (non-Javadoc)
	 * @throws ImportException
	 * @see net.heartsome.cat.ts.tm.importer.extension.ITmImporter#executeImport(java.lang.String)
	 */
	public int executeImport(String tmxContent, String srcLang, IProgressMonitor monitor) throws ImportException {
		if (dbOp != null) {
			int strategy = transParameters.getTmUpdateStrategy();
			// TODO 检查上下文
			int result = DatabaseService.importTmxWithString(dbOp, tmxContent, monitor, strategy, false, srcLang);
		} else {
			return IMPORT_STATE_NODB;
		}
		return IMPORT_STATE_SUCCESSED;
	}

	public void setProject(IProject project) {
		if (project == null) {
			clearResources();
			return;
		}
		if (currentProj == null) {
			currentProj = project;
		} else if (project != null && !project.getName().equals(currentProj.getName())) {
			currentProj = project;
		} else {
			return;
		}

		if (currentProj != null) {
			projConfiger = ProjectConfigerFactory.getProjectConfiger(currentProj);
			projConfiger.addChangeListener(this);
			loadTmDbConn();
		}
	}

	public void handProjectConfigChangedEvent() {
		loadTmDbConn();
	}

	/**
	 * 释放连接 ;
	 */
	private void releaseTmDbConn() {
		if (dbOp != null) {
			try {
				dbOp.end();
			} catch (SQLException e) {
				logger.error(Messages.getString("importer.TmImporter.logger1") + dbOp.getMetaData().getDatabaseName(),
						e);
			}
		}
		dbOp = null;
	}

	/**
	 * 加载连接 ;
	 */
	private void loadTmDbConn() {
		releaseTmDbConn(); // 加载连接前先释放已经存在连接

		if (projConfiger != null) {
			DatabaseModelBean dmb = projConfiger.getDefaultTMDb();
			if (dmb != null) {
				this.dbOp = DatabaseService.getDBOperator(dmb.toDbMetaData());
				try {
					dbOp.start();
				} catch (Exception e) {
					logger.error(MessageFormat.format(Messages.getString("importer.TmImporter.logger2"),
							dmb.getDbType(), dmb.getDbName()), e);
					dbOp = null;
				}
			}
		}
	}

	public void clearResources() {
		this.releaseTmDbConn();
		if (projConfiger != null)
			projConfiger.removeChangeListener(this);
		// if (transParameters != null)
		// transParameters.clearResources();
		currentProj = null;
	}

	public int getContextSize() {
		return transParameters.getContextSize();
	}

	public boolean checkImporter() {
		if (dbOp != null) {
			return true;
		}
		return false;
	}

}
