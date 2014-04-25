/**
 * ExportTbxImpl.java
 *
 * Version information :
 *
 * Date:Feb 24, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.bean.ExportDatabaseBean;
import net.heartsome.cat.database.bean.ExportFilterBean;
import net.heartsome.cat.database.bean.ExportFilterComponentBean;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.XMLModifier;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ExportTbxImpl extends ExportAbstract {

	private FileOutputStream output;

	public ExportTbxImpl(List<ExportDatabaseBean> dbList, ExportFilterBean filterBean, String encoding) {
		super(dbList, filterBean, encoding);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.document.ExportAbstract#executeExport(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String executeExport(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		String whereResult = "";
		if (this.filterBean != null) {
			StringBuffer whereBf = new StringBuffer();
			String connector = this.filterBean.getFilterConnector();
			List<ExportFilterComponentBean> filterOptions = this.filterBean.getFilterOption();
			whereBf.append(" AND ");
			for (Iterator<ExportFilterComponentBean> iterator = filterOptions.iterator(); iterator.hasNext();) {
				ExportFilterComponentBean filterBean = iterator.next();
				whereBf.append("PURE ");
				whereBf.append("LIKE ");
				whereBf.append("'%" + filterBean.getFilterVlaue() + "%' ");
				whereBf.append(connector + " ");
			}
			whereResult = whereBf.toString();
			whereResult = whereResult.substring(0, whereResult.lastIndexOf(connector));
		}
		monitor.beginTask("", dbList.size());
		for (Iterator<ExportDatabaseBean> iterator = dbList.iterator(); iterator.hasNext();) {
			ExportDatabaseBean db = iterator.next();
			String srcLang = db.getSrcLang();
			List<String> needLang = db.getHasSelectedLangs();

			DBOperator dbOp = DatabaseService.getDBOperator(db.getDbBean());
			try {
				dbOp.start();
				output = new FileOutputStream(db.getExportFilePath());
				writeHeader(srcLang);
				writeString("<text>\n<body>\n");
				List<Integer> tPkId = dbOp.getAfterFilterTermEntryPK(whereResult, needLang);
				// Fix Bug #2361 TBX文件导出问题--语言不能正确过滤导出 by Jason
				tPkId = dbOp.validateTermEntryPk(tPkId, needLang, srcLang);
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				subMonitor.beginTask(Messages.getString("document.ExportTbxImpl.task1") + db.getDbBean().getDatabaseName(), tPkId.size());
				subMonitor.setTaskName(Messages.getString("document.ExportTbxImpl.task1") + db.getDbBean().getDatabaseName());

				for (int i = 0; i < tPkId.size(); i++) {
					// long l = System.currentTimeMillis();
					if (monitor.isCanceled()) {
						clearResource();
						return USER_CANCEL;
					}
					int termEntryPK = tPkId.get(i);
					String termEntry = dbOp.retrieveTermEntry(termEntryPK);
					if (termEntry != null && !termEntry.equals("")) {						
						writeString(termEntry);
					}
					subMonitor.worked(1);
				}

				writeString("</body>\n</text>\n</martif>\n");
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
			filterLangSet(db.getExportFilePath(),srcLang, needLang);
		}
		monitor.done();
		return SUCCESS;
	}

	/**
	 * 过滤从库中导出的langSet节点<br>
	 * 在库中TremEntry是以整个节点进行存储的，因此，在导出后也是整个节点导出，所以会将无关的语言也导出来。<br>
	 * 在导出后，生成的TBX文件中进语言进行过滤
	 * @param filePath
	 *            导出后生成的TBX文件路径
	 * @param srcLang
	 *            源语言
	 * @param needLang
	 *            当前需要导出的语言;
	 */
	private void filterLangSet(String filePath, String srcLang, List<String> needLang) {
		try {
			VTDGen vg = new VTDGen();
			if (vg.parseFile(filePath, true)) {
				VTDUtils vu = new VTDUtils(vg.getNav());
				StringBuffer xpath = new StringBuffer("/martif/text/body/termEntry/langSet[");
				
				String noteXpathtemp = "starts-with(@id,'__LANG__,') or ends-with(@id,',__LANG__')";			
				
				StringBuffer noteTgtXpath = new StringBuffer();
				for (String lang : needLang) {
					xpath.append("not(@xml:lang='" + lang + "') and ");
					if(!lang.equals(srcLang)){
						noteTgtXpath.append(noteXpathtemp.replace("__LANG__", lang)).append(" or ");
					}
				}
				String tgtLangXpath = noteTgtXpath.substring(0, noteTgtXpath.lastIndexOf("or"));
				
				StringBuffer noteXpath = new StringBuffer();
				noteXpath.append("/martif/text/body/termEntry/note[");
				noteXpath.append("not(");
				noteXpath.append("(").append(noteXpathtemp.replace("__LANG__", srcLang)).append(")");
				noteXpath.append(" and ");
				noteXpath.append("(").append(tgtLangXpath).append(")");
				noteXpath.append(")");
				noteXpath.append("]");
				
				String xpathStr = xpath.substring(0, xpath.lastIndexOf("and")) + "]";
				XMLModifier xm = new XMLModifier(vu.getVTDNav());
				AutoPilot ap = new AutoPilot(vu.getVTDNav());
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				xm = vu.delete(ap, xm, xpathStr, VTDUtils.PILOT_TO_END);
				xm = vu.delete(ap, xm, noteXpath.toString(), VTDUtils.PILOT_TO_END);
				
				FileOutputStream fos = new FileOutputStream(filePath);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				xm.output(bos); // 写入文件
				bos.close();
				fos.close();
			}
		} catch (NavException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (TranscodeException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		}
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
	 * 输出Header内容
	 * @param srcLang
	 * @throws IOException
	 *             ;
	 */
	private void writeHeader(String srcLang) throws IOException {
		writeString("<?xml version=\"1.0\" encoding=\"" + this.encoding + "\"?>\n");

		writeString("<martif type=\"TBX\" xml:lang=\"" + srcLang + "\">\n");
		writeString("<martifHeader>\n");
		writeString("<fileDesc>Generated by Heartsome Translation Studio TBX Exporter</fileDesc>\n");
		writeString("<encodingDesc><p type=\"DCSName\">tbxdefault.xcs</p></encodingDesc>\n");
		writeString("</martifHeader>\n");
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

}
