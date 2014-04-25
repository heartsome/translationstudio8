/**
 * AbstractImport.java
 *
 * Version information :
 *
 * Date:Nov 7, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.EOFException;
import com.ximpleware.EncodingException;
import com.ximpleware.EntityException;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 此类及其实现类仅限于 TBX，原来的 TMX 导入已经完全独立出去。和这个不在有任何关系。
 * @author Jason
 * @version
 * @since JDK1.6
 */
public abstract class ImportAbstract {

	/** XML解析封装 */
	protected VTDUtils vu;

	/** 文件类型 */
	protected String fileType;

	/** 导入策略 */
	protected int importStrategy;

	/** 数据库操作对象 */
	protected DBOperator dbOperator;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected IProgressMonitor monitor;

	/***
	 * 将已解析XML文件导入到库中
	 * @param srcLang
	 *            TMX源语言
	 * @param vtdUtils
	 *            封装的VTD解析工具
	 * @throws SQLException
	 * @throws NavException
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 *             ;
	 * @throws ParseException
	 * @throws EntityException
	 * @throws EOFException
	 * @throws EncodingException
	 * @throws IOException
	 * @throws ModifyException
	 * @throws TranscodeException
	 */
	protected abstract void executeImport(String srcLang) throws SQLException, NavException, XPathParseException,
			XPathEvalException, EncodingException, EOFException, EntityException, ParseException, TranscodeException,
			ModifyException, IOException, ImportException;

	/**
	 * 将TMX文件导入数据库中,在执行导入前对文件处理以及对需要使用的资源进行初始化
	 * @param fileName
	 *            TMX文件路径
	 * @param monitor
	 *            进度条
	 * @return ;
	 * @throws ImportException
	 */
	public int doImport(String fileName, IProgressMonitor monitor) throws ImportException {
		this.monitor = monitor;
		// 文件检查
		String message = "";
		try {
			if (Constants.TBX.equals(fileType)) {
				if ((vu = DocUtils.isTBX(fileName)) == null) {
					return FAILURE_1;
				}
			} else {
				if ((vu = DocUtils.isTMX(fileName)) == null) {
					return FAILURE_1;
				}
				// 解析 header 接点
				AutoPilot ap = new AutoPilot(vu.getVTDNav());
				try {
					ap.selectXPath("/tmx/header");
					if (ap.evalXPath() != -1) {
						int id = vu.getVTDNav().getAttrVal("srclang");
						if (id != -1 && "*all*".equalsIgnoreCase(vu.getVTDNav().toString(id).trim())) {
							final boolean[] flag = new boolean[1];
							Display.getDefault().syncExec(new Runnable() {

								public void run() {
									flag[0] = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
											Messages.getString("document.ImportTmx.title"),
											Messages.getString("document.ImportTmx.msg2"));
								}
							});
							if (!flag[0]) {
								return CANCEL;
							}
						}
					}
				} catch (VTDException e) {
					logger.error("", e);
				}

			}
		} catch (EncodingException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger1"), e);
			message = Messages.getString("document.ImportAbstract.msg1");
			throw new ImportException(message + e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger2"), e);
			message = Messages.getString("document.ImportAbstract.msg2");
			throw new ImportException(message + e.getMessage());
		} catch (ParseException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger3"), e);
			String errMsg = e.getMessage();
			if (errMsg.indexOf("invalid encoding") != -1) {
				// 编码异常
				message = Messages.getString("document.ImportAbstract.msg1");
			} else {
				message = Messages.getString("document.ImportAbstract.tbx.msg1");
			}
			throw new ImportException(message + e.getMessage());
		}

		try {
			dbOperator.beginTransaction();
			executeImport(null); // 执行导入
			dbOperator.commit();
		} catch (VTDException e) {
			logger.error("", e);
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("", e);
			}
			throw new ImportException(Messages.getString("document.ImportAbstract.tbx.msg1") + e.getMessage());
		} catch (SQLException e) {
			logger.error("", e);
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("", e1);
			}
			throw new ImportException(Messages.getString("document.ImportAbstract.tbx.importDbError") + e.getMessage());
		} catch (OperationCanceledException e) {
			logger.error("", e);
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("", e1);
				return CANCEL;
			}
			return CANCEL;
		}  catch (Exception e) {
			logger.error("", e);
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("", e1);
			}
			throw new ImportException(Messages.getString("document.ImportAbstract.tbx.importDbError") + e.getMessage());
		}
		return SUCCESS;

	}

	/**
	 * 导入文件内容
	 * @param fileContent
	 * @param srcLang
	 * @param monitor
	 * @return ;
	 * @throws ImportException
	 */
	public int doImport(String fileContent, String srcLang, IProgressMonitor monitor) throws ImportException {
		if (monitor == null) {
			this.monitor = new NullProgressMonitor();
		} else {
			this.monitor = monitor;
		}

		// 解析文件
		String message = "";
		VTDGen vg = new VTDGen();
		vg.setDoc(fileContent.getBytes());
		try {
			vg.parse(true);
		} catch (EncodingException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger1"), e);
			message = Messages.getString("document.ImportAbstract.msg1");
			throw new ImportException(message + e.getMessage());
		} catch (EOFException e) {
			logger.error("", e);
			message = Messages.getString("document.ImportAbstract.tbx.msg1");
			throw new ImportException(message + e.getMessage());
		} catch (EntityException e) {
			logger.error("", e);
			message = Messages.getString("document.ImportAbstract.tbx.msg1");
			throw new ImportException(message + e.getMessage());
		} catch (ParseException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger3"), e);
			String errMsg = e.getMessage();
			if (errMsg.indexOf("invalid encoding") != -1) {
				// 编码异常
				message = Messages.getString("document.ImportAbstract.msg1");
			} else {
				message = Messages.getString("document.ImportAbstract.tbx.msg1");
			}
			throw new ImportException(message + e.getMessage());
		}

		try {
			vu = new VTDUtils(vg.getNav()); // 构建VTD解析工具
			dbOperator.beginTransaction();
			executeImport(srcLang); // 执行导入
			dbOperator.commit();
		} catch (VTDException e) {
			logger.error("", e);
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("", e);				
			}
			return FAILURE_4;
		} catch (SQLException e) {
			logger.error("", e);
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("", e1);				
			}
			throw new ImportException(Messages.getString("document.ImportAbstract.tbx.importDbError") + e.getMessage());
		} catch (OperationCanceledException e) {
			logger.error("", e);
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("", e1);
				return CANCEL;
			}
		} catch (Exception e) {
			logger.error("", e);
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("", e1);				
			}
			throw new ImportException(Messages.getString("document.ImportAbstract.tbx.importDbError") + e.getMessage());
		}
		return SUCCESS;
	}

	/**
	 * 取得语言代码
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 */
	protected String getLang() throws XPathParseException, XPathEvalException, NavException {
		String lang = vu.getCurrentElementAttributs().get("xml:lang");
		if (lang == null) {
			lang = vu.getCurrentElementAttributs().get("lang");
		}
		return Utils.convertLangCode(lang);
	}

	/** 成功标志 */
	public static final int SUCCESS = 1;

	/** 失败标志 */
	public static final int FAILURE = 0;

	/** 不是正确的 TMX 或 TBX 文件 */
	public static final int FAILURE_1 = -1;

	/** 解析文件失败 */
	public static final int FAILURE_2 = -2;

	/** 数据库异常 */
	public static final int FAILURE_3 = -3;

	/** XPath 错误 */
	public static final int FAILURE_4 = -4;

	/** 取消标志 */
	public static final int CANCEL = 2;

}
