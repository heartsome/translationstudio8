/**
 * TbImporter.java
 *
 * Version information :
 *
 * Date:2012-5-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.tb.importer;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.tb.TbDbOperatorManager;
import net.heartsome.cat.database.tb.TbParameters;
import net.heartsome.cat.database.tb.resource.Messages;
import net.heartsome.cat.document.ImportAbstract;
import net.heartsome.cat.ts.core.IProjectConfigChangedListener;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.tb.importer.extension.ITbImporter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TbImporter implements ITbImporter, IProjectConfigChangedListener {

	private final static Logger logger = LoggerFactory.getLogger(TbDbOperatorManager.class);

	private ProjectConfiger projConfiger;
	private IProject currentProj;
	private DBOperator dbOp;

	private TbParameters tbParas = TbParameters.getInstance();

	/**
	 * 
	 */
	public TbImporter() {

	}

	public boolean checkImporter() {
		if (dbOp != null) {
			return true;
		}
		return false;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.tb.importer.extension.ITbImporter#setProject(org.eclipse.core.resources.IProject)
	 */
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
	 * 加载连接 ;
	 */
	private void loadTmDbConn() {
		releaseTmDbConn(); // 加载连接前先释放已经存在连接

		if (projConfiger != null) {
			List<DatabaseModelBean> termdbList = projConfiger.getTermBaseDbs(true);
			if (termdbList.size() == 0) {
				return;
			}

			DatabaseModelBean dmb = termdbList.get(0);
			if (dmb != null) {
				this.dbOp = DatabaseService.getDBOperator(dmb.toDbMetaData());
				try {
					dbOp.start();
				} catch (Exception e) {
					logger.error(MessageFormat.format(Messages.getString("importer.TbImporter.logger1"),
							dmb.getDbType(), dmb.getDbName()), e);
					dbOp = null;
				}
			}
		}
	}

	/**
	 * 释放连接 ;
	 */
	private void releaseTmDbConn() {
		if (dbOp != null) {
			try {
				if (dbOp != null) {
					dbOp.end();
				}
			} catch (SQLException e) {
				logger.error(Messages.getString("importer.TbImporter.logger2") + dbOp.getMetaData().getDatabaseName(),
						e);
			}
			dbOp = null;
		}
	}

	/**
	 * (non-Javadoc)
	 * @throws ImportException 
	 * @see net.heartsome.cat.ts.tb.importer.extension.ITbImporter#executeImport(java.lang.String, java.lang.String)
	 */
	public int executeImport(String tbxStr, String srcLang, IProgressMonitor monitor) throws ImportException {
		if (dbOp != null) {
			int result = DatabaseService.importTbxWithString(tbxStr, monitor, dbOp, tbParas.getTbUpdateStrategy(),
					srcLang);
			if (result == ImportAbstract.SUCCESS) {
				return ITbImporter.IMPORT_STATE_SUCCESSED;
			}
		} else {
			return ITbImporter.IMPORT_STATE_NODB;
		}
		return ITbImporter.IMPORT_STATE_FAILED;
	}

	public void clearResources() {
		this.releaseTmDbConn();
		if (projConfiger != null)
			projConfiger.removeChangeListener(this);
		currentProj = null;
	}
}
