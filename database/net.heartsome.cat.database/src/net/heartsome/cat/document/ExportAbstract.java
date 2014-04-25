/**
 * ExportAbstract.java
 *
 * Version information :
 *
 * Date:Feb 20, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document;

import java.util.List;

import net.heartsome.cat.database.bean.ExportDatabaseBean;
import net.heartsome.cat.database.bean.ExportFilterBean;
import net.heartsome.cat.database.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导出时基本参数
 * @author Jason
 * @version
 * @since JDK1.6
 */
public abstract class ExportAbstract {
	public final static Logger logger = LoggerFactory.getLogger(ExportTmxImpl.class);
	protected List<ExportDatabaseBean> dbList; // 需要导出的数据库

	protected ExportFilterBean filterBean; // 过滤规则

	protected String encoding; // 编码格式 默认值为UTF－8

	public ExportAbstract(List<ExportDatabaseBean> dbList, ExportFilterBean filterBean, String encoding) {
		this.dbList = dbList;
		this.filterBean = filterBean;
		this.encoding = encoding;
	}

	/**
	 * 执行导出
	 * @param monitor
	 * @return 返回结果信息;
	 */
	public abstract String executeExport(IProgressMonitor monitor);

	protected String USER_CANCEL = Messages.getString("document.ExportAbstract.msg1");
	protected String DBOP_ERROR = Messages.getString("document.ExportAbstract.msg2");
	protected String JDBC_ERROR = Messages.getString("document.ExportAbstract.msg3");
	protected String FILE_ERROR = Messages.getString("document.ExportAbstract.msg4");
	protected String SUCCESS = Messages.getString("document.ExportAbstract.msg5");
	protected String RELEASE_DB_ERROR = Messages.getString("document.ExportAbstract.msg6");
	protected String RELEASE_FILE_ERROR = Messages.getString("document.ExportAbstract.msg7");
	
}
