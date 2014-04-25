/**
 * TermDbOperatorManager.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.tb;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.tb.resource.Messages;
import net.heartsome.cat.ts.core.IProjectConfigChangedListener;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;

import org.eclipse.core.resources.IProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 术语库连接管理器
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TbDbOperatorManager implements IProjectConfigChangedListener {
	public final static Logger logger = LoggerFactory.getLogger(TbDbOperatorManager.class);

	private IProject currentProj;

	private ProjectConfiger projConfiger;

	private List<DBOperator> dbOpList;

	public TbDbOperatorManager() {
		dbOpList = new ArrayList<DBOperator>();
	}

	/**
	 * 设置项目信息
	 * @param project
	 *            ;
	 */
	public void setProject(IProject project) {
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
			List<DatabaseModelBean> tmDbConfigList = projConfiger.getTermBaseDbs(false);
			for (int i = 0; i < tmDbConfigList.size(); i++) {
				DatabaseModelBean dmb = tmDbConfigList.get(i);
				DBOperator db = DatabaseService.getDBOperator(dmb.toDbMetaData());
				if(null == db){
					continue;
				}
				try {
					db.start();
				} catch (Exception e) {
					logger.error(MessageFormat.format(Messages.getString("tb.TbDbOperatorManager.logger1"),
							dmb.getDbType(), dmb.getDbName()), e);
					continue;
				}
				this.dbOpList.add(db);
			}
		}
	}

	/**
	 * 释放连接 ;
	 */
	private void releaseTmDbConn() {
		for (DBOperator dbop : dbOpList) {
			try {
				if (dbop != null) {
					dbop.end();
				}
			} catch (SQLException e) {
				logger.error(Messages.getString("tb.TbDbOperatorManager.logger2")
						+ dbop.getMetaData().getDatabaseName(), e);
				continue; // 继续释放其他数据库资源
			}
		}
		dbOpList.clear();
	}

	public void clearResource() {
		currentProj = null;
		if (projConfiger != null) {
			projConfiger.removeChangeListener(this);
		}
		releaseTmDbConn();
	}

	public List<DBOperator> getDbOperatorList() {
		return this.dbOpList;
	}
}
