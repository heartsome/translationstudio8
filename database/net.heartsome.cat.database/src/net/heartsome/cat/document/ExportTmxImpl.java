/**
 * ExportTmxImpl.java
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.bean.ExportDatabaseBean;
import net.heartsome.cat.database.bean.ExportFilterBean;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.database.service.DatabaseService;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ExportTmxImpl extends ExportAbstract {
	private boolean isToplevelTmx; // 一级TMX
	private boolean isTagLevelTmx; // 带标记的TMX
	private FileOutputStream output;

	/**
	 * 构造函数
	 * @param dbList
	 *            数据库列表 {@link ExportDatabaseBean}
	 * @param filterBean
	 *            过滤条件 {@link ExportFilterBean}
	 * @param encoding
	 *            编辑规则
	 * @param exportPath
	 *            导出的文件路径,一个库的情况下为文件，多个库的情况下为文件夹
	 * @param isToplevelTmx
	 *            是否为一级TMX
	 * @param isTagLevelTmx
	 *            是否是带标记的TMX
	 */
	public ExportTmxImpl(List<ExportDatabaseBean> dbList, ExportFilterBean filterBean, String encoding,
			boolean isToplevelTmx, boolean isTagLevelTmx) {
		super(dbList, filterBean, encoding);
		this.isToplevelTmx = isToplevelTmx;
		this.isTagLevelTmx = isTagLevelTmx;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.document.ExportAbstract#executeExport()
	 */
	@Override
	public String executeExport(IProgressMonitor monitor) {
		String mTuFilter = "";
		String textDataFilter = "";
		String mNoteFilter = "";

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", dbList.size());
		for (Iterator<ExportDatabaseBean> iterator = dbList.iterator(); iterator.hasNext();) {
			ExportDatabaseBean db = iterator.next();
			String srcLang = db.getSrcLang();
			DBOperator dbOp = DatabaseService.getDBOperator(db.getDbBean());

			// 过滤条件
			if (this.filterBean != null) {
				mTuFilter = dbOp.generationExportTMXFilter("MTU", this.filterBean);
				textDataFilter = dbOp.generationExportTMXFilter("TEXTDATA", this.filterBean);
				mNoteFilter = dbOp.generationExportTMXFilter("MNOTE", this.filterBean);
			}
			try {
				dbOp.start();
				output = new FileOutputStream(db.getExportFilePath());
				if (encoding.equalsIgnoreCase("UTF-16LE")) {
					output.write(0xFF);
					output.write(0xFE);
				} else if (encoding.equalsIgnoreCase("UTF-16BE")) {
					output.write(0xFE);
					output.write(0xFF);
				} 
				List<Integer> filterTu = dbOp.getAfterFilterTuPk(mTuFilter, mNoteFilter, textDataFilter);

				writeHeader(srcLang);
				writeString("<body>\n");
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				subMonitor.beginTask(Messages.getString("document.ExportTmxImpl.task1")
						+ db.getDbBean().getDatabaseName(), filterTu.size());
				subMonitor.setTaskName(Messages.getString("document.ExportTmxImpl.task1")
						+ db.getDbBean().getDatabaseName());
				for (int i = 0; i < filterTu.size(); i++) {
					// long l = System.currentTimeMillis();
					if (monitor.isCanceled()) {
						clearResource();
						break;
					}
					int tuPk = filterTu.get(i);
					String tuNodeContent = dbOp
							.retrieveTu(tuPk, db.getHasSelectedLangs(), isToplevelTmx, isTagLevelTmx);
					if (tuNodeContent != null && !tuNodeContent.equals("")) {
						writeString(tuNodeContent);
					}
					subMonitor.worked(1);
				}
				writeString("</body>\n"); //$NON-NLS-1$
				writeString("</tmx>\n"); //$NON-NLS-1$	
				subMonitor.done();
			} catch (SQLException e) {
				logger.error(DBOP_ERROR, e);
				e.printStackTrace();
				clearResource();
				return DBOP_ERROR + db.getDbBean().getDbType() + " " + db.getDbBean().getDatabaseName();
			} catch (ClassNotFoundException e) {
				logger.error(JDBC_ERROR, e);
				e.printStackTrace();
				clearResource();
				return JDBC_ERROR + db.getDbBean().getDbType();
			} catch (IOException e) {
				logger.error(FILE_ERROR, e);
				e.printStackTrace();
				clearResource();
				return FILE_ERROR + db.getDbBean().getDbType() + " " + db.getDbBean().getDatabaseName();
			} finally {
				try {
					output.close();
					if (dbOp != null) {
						dbOp.end();
					}
				} catch (SQLException e) {
					logger.error(RELEASE_DB_ERROR, e);
				} catch (IOException e) {
					logger.error(RELEASE_FILE_ERROR, e);
				}
			}
		}
		if (monitor.isCanceled()) {
			return USER_CANCEL;
		}
		monitor.done();
		return SUCCESS;
	}

	/**
	 * 清除所有已经生成的文件 ;
	 */
	private void clearResource() {
		for (Iterator<ExportDatabaseBean> tempIt = dbList.iterator(); tempIt.hasNext();) {
			ExportDatabaseBean tempDb = tempIt.next();
			String path = tempDb.getExportFilePath();
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
		}
	}

	/**
	 * 输出Header内容
	 * @param srcLang
	 * @throws IOException
	 *             ;
	 */
	private void writeHeader(String srcLang) throws IOException {
		writeString("<?xml version=\"1.0\" encoding=\"" + this.encoding + "\"?>\n");

		writeString("<!DOCTYPE tmx PUBLIC \"-//LISA OSCAR:1998//DTD for Translation Memory eXchange//EN\" \"tmx14.dtd\" >\n");
		writeString("<tmx version=\"1.4\">\n");
		writeString("<header \n" + "      creationtool=\"Heartsome TM Server\" \n"
				+ "      creationtoolversion=\"2.0-1\" \n" + "      srclang=\"" + srcLang + "\" \n"
				+ "      adminlang=\"en\"  \n" + "      datatype=\"xml\" \n" + "      o-tmf=\"unknown\" \n"
				+ "      segtype=\"block\" \n" + "      creationdate=\"" + creationDate() + "\"\n>\n" + "</header>\n");
	}

	/**
	 * 将内容写入文件
	 * @param string
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 *             ;
	 */
	private void writeString(String string) throws UnsupportedEncodingException, IOException {
		output.write(string.getBytes(this.encoding));
	}

	/**
	 * 获取创建时间
	 * @return ;
	 */
	public String creationDate() {
		Calendar calendar = Calendar.getInstance(Locale.US);
		String sec = (calendar.get(Calendar.SECOND) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.SECOND);
		String min = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.MINUTE);
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.HOUR_OF_DAY);
		String mday = (calendar.get(Calendar.DATE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.DATE);
		String mon = (calendar.get(Calendar.MONTH) < 9 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR); //$NON-NLS-1$

		String date = longyear + mon + mday + "T" + hour + min + sec + "Z"; //$NON-NLS-1$ //$NON-NLS-2$
		return date;
	}

}
