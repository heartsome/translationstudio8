package net.heartsome.cat.ts.ui.external;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.docx.Activator;
import net.heartsome.cat.ts.ui.docx.ImportDocx;
import net.heartsome.cat.ts.ui.docx.common.CommentBean;
import net.heartsome.cat.ts.ui.docx.common.DocxCommonFuction;
import net.heartsome.cat.ts.ui.docx.common.DocxConstant;
import net.heartsome.cat.ts.ui.docx.common.ErrorBean;
import net.heartsome.cat.ts.ui.docx.common.FlagErrorException;
import net.heartsome.cat.ts.ui.docx.common.RowBean;
import net.heartsome.cat.ts.ui.docx.common.TagBean;
import net.heartsome.cat.ts.ui.docx.dialog.ErrorTipDialog;
import net.heartsome.cat.ts.ui.docx.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathParseException;

public class ImportConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImportConfig.class);

	/** 导入类型. */
	private int importType;
	/** xliff文件全路径. */
	private String xliffFile;
	/** 外部文件全路径. */
	private String externalFile;

	/** 过度字段，为兼容以前写的代码，xliff 相对路径 */
	@Deprecated
	private String _xliff;
	/** 过度字段，为兼容以前写的代码 */
	@Deprecated
	private XLIFFEditorImplWithNatTable xliffEditor;

	// 交互对话
	private Shell shell;
	// 进度条支持
	private IProgressMonitor monitor;

	private MonitorRunner runner = new MonitorRunner();
	private List<ExportReporter> exportReporter = null;
	
	
	public String getXliffFile() {
		return xliffFile;
	}

	public void setXliffFile(String xliffFile) {
		this.xliffFile = xliffFile;
	}

	public String getExternalFile() {
		return externalFile;
	}

	public void setExternalFile(String externalFile) {
		this.externalFile = externalFile;
	}

	public int getImportType() {
		return importType;
	}

	public void setImportType(int importType) {
		this.importType = importType;
	}

	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public IProgressMonitor getMonitor() {
		return monitor;
	}

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	@Deprecated
	public String get_xliff() {
		return _xliff;
	}

	@Deprecated
	public void set_xliff(String _xliff) {
		this._xliff = _xliff;
	}

	@Deprecated
	public XLIFFEditorImplWithNatTable getXliffEditor() {
		return xliffEditor;
	}

	@Deprecated
	public void setXliffEditor(XLIFFEditorImplWithNatTable xliffEditor) {
		this.xliffEditor = xliffEditor;
	}

	public void doImport() {
		switch (importType) {
		case ExportExternal.EXPORT_HSPROOF:
			ImportHsproof ih = new ImportHsproof();
			ih.doImport();
			break;
		case ExportExternal.EXPORT_SDLUNCLEAN:
			final UncleanImporter unclean = new UncleanImporter();
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			try {
				dialog.run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							ImportConfig.this.monitor = monitor;
							unclean.doImport();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			break;
		}
	}

	
	
	class UncleanImporter {

		private AutoPilot tmpAp = null;
		private XliffUpdater xlfUpdater = null;

		public void doImport() throws ExportExternalException {
			
			xlfUpdater = new XliffUpdater();
			
			VTDGen vg = new VTDGen();
			if (!vg.parseZIPFile(externalFile, "word/document.xml", true)) {
				canceled(false);
				throw new ExportExternalException(MessageFormat.format(
						Messages.getString("ImportDocxDialog.ok.parseError"), externalFile));
			}

			VTDNav vn = vg.getNav();
			tmpAp = new AutoPilot(vn);
			tmpAp.declareXPathNameSpace("w", ExportExternal.NAMESPACE_W);
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace("w", ExportExternal.NAMESPACE_W);

			try {
				ap.selectXPath("/w:document/w:body/w:p/w:r");
				tmpAp.selectXPath("count(/w:document/w:body/w:p/w:r)");
				// 工作量
				runner.monitorSupport("", tmpAp.evalXPathToNumber());
				
				String rowid = null;
				String rowNo = null;
				
				String tmpTextStyle = null;
				StringBuilder tmpInstrTextBuf = new StringBuilder();
				StringBuilder tmpWTBuf = new StringBuilder();
				StringBuilder tmpLfBuf = new StringBuilder();
				StringBuilder tmpTargetBuf = new StringBuilder();
				StringBuilder tmpTagBuf = new StringBuilder();
				
				int next = 0;
				int codepoint = 0;

				boolean again = false;
				final int get_rowid = 1;
				final int check_rowid = 2;
				final int error_rowid = -1;
				final int get_left = 3;
				final int check_left = 31;
				final int cp_4 = 4;
				final int cp_5 = 5;
				final int cp_6 = 6;
				final int cp_6_1 = 61;
				final int cp_7 = 7;
				final int cp_8 = 8;
				final int cp_9 = 9;
				final int cp_fldChar = 10;
				codepoint = get_rowid;
				next = cp_9;
				int pop = 0;
				
				final String isFldCharBegin = "./w:fldChar[@w:fldCharType='begin']";
				final String isFldCharEnd = "./w:fldChar[@w:fldCharType='end']";
				final String isTw4winMark = "./w:rPr/w:rStyle[@w:val='tw4winMark']";
				
				// 循环所有 w:r
				wr: while (ap.evalXPath() != -1) {
					// 进度条支持
					try {
						runner.worked(1);
					} catch (OperationCanceledException e) {
						canceled(true);
						return;
					}
					
					do {
						again = false;
						switch (codepoint) {
						case get_rowid:// get rowid
							if (testXpath(isFldCharBegin)) {
								tmpWTBuf.setLength(0);
								tmpInstrTextBuf.setLength(0);
								tmpTextStyle = null;
								again = true;
								codepoint = cp_fldChar;
								next = check_rowid;
								break;
							}
							continue wr;
						case check_rowid:
							if (!"HSRow".equals(tmpTextStyle)) {// clean
								codepoint = cp_9;
								again = true;
								break;
							}
							rowid = resolveQuote(tmpInstrTextBuf);
							rowNo = tmpWTBuf.toString();
							codepoint = get_left;
						case get_left:
							if (testXpath(isFldCharBegin)) {
								tmpWTBuf.setLength(0);
								tmpTextStyle = null;
								tmpInstrTextBuf.setLength(0);
								codepoint = cp_fldChar;
								next = error_rowid;
								pop = check_left;
								again = true;
								break;
							}
							if (testXpath(isTw4winMark)) {
								codepoint = check_left;
								again = true;
								break;
							}
							continue wr;
						case check_left:
							if (testXpath(isTw4winMark)) {
								vn.push();
								tmpAp.selectXPath("./w:t/text()");
								while (tmpAp.evalXPath() != -1) {
									tmpLfBuf.append(vn.toRawString(vn.getText()));
								}
								vn.pop();
								continue wr;
							} else {
								if (tmpLfBuf.toString().trim().equals("{0&gt;")) {
									codepoint = cp_4;
									tmpLfBuf.setLength(0);
								} else {
									codepoint = cp_9;
									again = true;
									break;
								}
							}
						case cp_4:// skip src, should we check at first??
							if (testXpath(isFldCharBegin)) {
								tmpWTBuf.setLength(0);
								tmpInstrTextBuf.setLength(0);
								tmpTextStyle = null;
								codepoint = cp_fldChar;
								next = error_rowid;
								pop = cp_4;
								again = true;
								break;
							}
							tmpAp.selectXPath(isTw4winMark);
							if (!tmpAp.evalXPathToBoolean()) {
								continue wr;
							} else {
								codepoint = cp_5;
							}
						case cp_5:// check match <}100{>
							if (testXpath(isTw4winMark)) {
								vn.push();
								tmpAp.selectXPath("./w:t/text()");
								while (tmpAp.evalXPath() != -1) {
									tmpLfBuf.append(vn.toRawString(vn.getText()));
								}
								vn.pop();
								continue wr;
							} else {
								if (tmpLfBuf.toString().trim().matches("&lt;\\}[0-9]*\\{&gt;")) {
									codepoint = cp_6;
									tmpLfBuf.setLength(0);
								} else {
									codepoint = cp_9;
									again = true;
									break;
								}
							}
						case cp_6:// check tgt
							if (testXpath(isFldCharBegin)) {
								tmpWTBuf.setLength(0);
								tmpInstrTextBuf.setLength(0);
								tmpTextStyle = null;
								codepoint = cp_fldChar;
								next = error_rowid;
								pop = cp_6_1;
								again = true;
								break;
							}
							if (testXpath(isTw4winMark)) {
								codepoint = cp_7;
								again = true;
								break;
							}
							vn.push();
							tmpAp.selectXPath("./w:t/text()");
							while (tmpAp.evalXPath() != -1) {
								tmpTargetBuf.append(vn.toRawString(vn.getText()));
							}
							vn.pop();
							continue wr;
						case cp_6_1:// check tgt tag
							if ("Tag".equals(tmpTextStyle)) {
								tmpTargetBuf.append(resolveQuote(tmpInstrTextBuf));
							}
							codepoint = cp_6;
							again = true;
							break;
						case cp_7:// check right flag {0>
							if (testXpath(isTw4winMark)) {
								vn.push();
								tmpAp.selectXPath("./w:t/text()");
								while (tmpAp.evalXPath() != -1) {
									tmpLfBuf.append(vn.toRawString(vn.getText()));
								}
								vn.pop();
								continue wr;
							} else {
								if (tmpLfBuf.toString().trim().equals("&lt;0}")) {
									codepoint = cp_8;
									tmpLfBuf.setLength(0);
								} else {
									codepoint = cp_9;
									again = true;
									break;
								}
							}
						case cp_8:// update
							try {
								xlfUpdater.updateByRowId(rowid, tmpTargetBuf.toString());
							} catch (FlagErrorException e) {
								appendReport(e, rowNo);
							}
							codepoint = cp_9;
						case cp_9:// reset
							rowid =null;
							tmpWTBuf.setLength(0);
							tmpInstrTextBuf.setLength(0);
							tmpTargetBuf.setLength(0);
							tmpLfBuf.setLength(0);
							tmpTagBuf.setLength(0);
							codepoint = get_rowid;
							again = true;
							break;
						case cp_fldChar: {
							if (tmpTextStyle == null) {
								tmpAp.selectXPath("./w:rPr/w:rStyle/@w:val");
								if (tmpAp.evalXPathToBoolean()) {
									tmpTextStyle = tmpAp.evalXPathToString();
								}
							}
							if (next == check_rowid) {
								vn.push();
								tmpAp.selectXPath("./w:t/text()");
								while (tmpAp.evalXPath() != -1) {
									tmpWTBuf.append(vn.toRawString(vn.getCurrentIndex()));
								}
								vn.pop();
							}
							
							vn.push();
							tmpAp.selectXPath("./w:instrText/text()");
							while (tmpAp.evalXPath() != -1) {
								tmpInstrTextBuf.append(vn.toRawString(vn.getCurrentIndex()));
							}
							vn.pop();
							if (testXpath(isFldCharEnd)) {
								codepoint = next;
							}
							continue wr;
							//break;
						}
						case error_rowid: {// check left flag
							if ("HSRow".equals(tmpTextStyle)) {
								codepoint = get_left;
							} else {
								codepoint = pop;
							}
							again = true;
							break;
						}
						}
					} while (again);
				}
				
				// last can not loop
				if (tmpLfBuf.toString().trim().equals("&lt;0}")) {
					try {
						xlfUpdater.updateByRowId(rowid, tmpTargetBuf.toString());
					} catch (FlagErrorException e) {
						appendReport(e, rowNo);
					}
					rowid = null;
					tmpTargetBuf.setLength(0);
				}
				tmpLfBuf.setLength(0);
				xlfUpdater.flush();
				runner.done();
			} catch (ExportExternalException e) {
				throw e;
			} catch (Exception e) {
				openErrorDialog(e);
				canceled(true);// 清理
				throw new ExportExternalException(Messages.getString("ImportDocxDialog.import.errorTip1"), e); // 提示
			} finally {
				canceled(false);// 清理
				// open report if exits
				if (exportReporter != null) {
					openErrorTipsDialog();
				}
				exportReporter = null;
			}
		}

		private void appendReport(FlagErrorException e, String row) {
			ExportReporter reporter = new ExportReporter();
			reporter.setE(e);
			reporter.setRowInfo(MessageFormat.format(
					Messages.getString("ImportDocxDialog.import.errorTip2"), row));
			if (exportReporter == null) {
				exportReporter = new LinkedList<ExportReporter>();
			}
			exportReporter.add(reporter);
		}
		
		private String resolveQuote(StringBuilder builder) {
			//QUOTE "text"
			int start  = builder.indexOf("\"");
			int end = builder.lastIndexOf("\"");
			if (end <= start) {
				return builder.toString();
			}
			return ExportExternal.decodeXml(builder.substring(start + 1, end));
		}
		
		private boolean testXpath(String xpath) {
			try {
				tmpAp.selectXPath(xpath);
				return tmpAp.evalXPathToBoolean();
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				return false;
			}
		}

		void canceled(boolean recovery) throws ExportExternalException {
			// TODO should do something ?
		}
	}

	
	
	private class XliffUpdater {

		private String tgtLang;
		private VTDGen xmlChecker = new VTDGen();
		private VTDNav vn;
		private XMLModifier xm;
		private AutoPilot ap;
		private AutoPilot tmpAp;

		XliffUpdater() throws ExportExternalException {
			VTDGen vg = new VTDGen();
			if (!vg.parseFile(xliffFile, true)) {
				throw new ExportExternalException(MessageFormat.format(
						Messages.getString("ExportDocxDialog.ok.parseError"), _xliff));
			}
			try {
				vn = vg.getNav();
				xm = new XMLModifier(vn);
				ap = new AutoPilot(vn);
				tmpAp = new AutoPilot(vn);
				ap.selectXPath("/xliff/file/@target-language");
				tgtLang = ap.evalXPathToString();
				if (tgtLang.isEmpty()) {
					ap.selectXPath("/xliff/file/body//trans-unit/target/@xml:lang/text()");
					vn.pop();
					while (ap.evalXPath() != -1) {
						tgtLang = vn.toString(vn.getCurrentIndex());
						break;
					}
					vn.push();
				}
				ap.selectXPath("/xliff/file[1]/body/descendant::trans-unit[1]");
				if (ap.evalXPath() == -1) {
					throw new ExportExternalException(Messages.getString("ImportDocxDialog.import.errorTip3"));
				}
			} catch (Exception e) {
				throw new ExportExternalException(MessageFormat.format(
						Messages.getString("ExportDocxDialog.ok.parseError"), _xliff), e);
			}
		}

		public void updateByRowId(String rowid, String update) throws FlagErrorException, ExportExternalException {

			// tag 检查
			try {
				xmlChecker.clear();
				xmlChecker.setDoc(new StringBuilder().append("<check>").append(update).append("</check>").toString().getBytes());
				xmlChecker.parse(true);
			} catch (Exception e) {
				StringBuilder msgbuf = new StringBuilder();
				msgbuf.append(update);
				msgbuf.append("\n");
				throw new FlagErrorException(e, msgbuf.toString());
			}
			// 导入操作
			try {
				if (polit(rowid)) {
					String target = null;
					// get target,
					if (testXpath("./target")) {
						tmpAp.selectXPath("./target");
						target = tmpAp.evalXPathToString();
					}
					// should update,(only if the target has changed)
					if (!update.equals(target)) {
						// trans-unit
						if (testXpath("./@approved='yes'")) {
							xm.removeAttribute(vn.getAttrVal("approved") - 1);
						}
						// add target
						try {
							xm.insertBeforeTail(buildTarget(update));
						} catch (Exception e) {
							LOGGER.error(e.getMessage(), e);
							return;
						}
						// remove if exist
						vn.push();
						tmpAp.selectXPath("./target");
						if (tmpAp.evalXPath() != -1) {
							xm.remove();
						}
						vn.pop();
					}
				}
			} catch (Exception e) {
				throw new ExportExternalException("", e);
			}
		}

		public void flush() throws ExportExternalException {
			try {
				xm.output(xliffFile);
			} catch (Exception e) {
				throw new ExportExternalException("", e);
			}
		}

		/**
		 * 根据当前节点导航，加快导航速度
		 * @return ;
		 * @throws ExportExternalException 
		 */
		private boolean polit(String id) throws ExportExternalException {
			if (id == null) {
				return false;
			}
			String[] strArr = id.split("_");
			if (strArr.length != 2) {
				return false;
			}
			try {
				ap.selectXPath("/xliff/file["+ strArr[0] +"]/body/descendant::trans-unit[@id='" + strArr[1] + "']");
				if (ap.evalXPath() != -1) {
					return true;
				}
			} catch (Exception e) {
				throw new ExportExternalException(Messages.getString("all.dialog.error"), e);
			}
			return false;
		}

		private String buildTarget(String update) {
			return new StringBuilder().append("\r<target").append(" xml:lang=\"").append(tgtLang)
					.append("\" state=\"new\">").append(update).append("</target>").toString();
		}

		private boolean testXpath(String xpath) {
			try {
				tmpAp.selectXPath(xpath);
			} catch (XPathParseException e) {
				LOGGER.error("", e);
			}
			return tmpAp.evalXPathToBoolean();
		}
	}

	class MonitorRunner {
		private final int steps = 100;

		private double total;
		private int worked;
		private int tmp;
		private int count;

		public void monitorSupport(String taskName, double total) {
			if (monitor != null) {
				this.total = total;
				monitor.beginTask(taskName, (int) (total > steps ? steps : total) + 1);
			}
		}

		public void worked(int i) throws OperationCanceledException {
			if (monitor != null) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				this.count += i;
				tmp = (int) ((count / total) * steps);
				if (tmp > worked) {
					monitor.worked(tmp - worked);
					worked = tmp;
				}
			}
		}

		public void setTaskName(String taskName) {
			if (monitor != null) {
				monitor.setTaskName(taskName);
			}
		}

		public boolean isCanceled() {
			if (monitor != null) {
				return monitor.isCanceled();
			}
			return false;
		}

		public void done() {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	class ExportReporter {
		private String rowInfo;
		private FlagErrorException e;
		public String getRowInfo() {
			return rowInfo;
		}
		public void setRowInfo(String rowInfo) {
			this.rowInfo = rowInfo;
		}
		public FlagErrorException getE() {
			return e;
		}
		public void setE(FlagErrorException e) {
			this.e = e;
		}
	}
	
	/**
	 * robert 所写代码，不进行任何修改，(兼容以前代码)
	 * @author Austen
	 * @version
	 * @since JDK1.6
	 */
	@Deprecated
	class ImportHsproof {

		private XLFHandler xlfHandler;
		private XMLModifier xm;
		private XLFHandler tempXlfHandler;
		private VTDNav vn;
		private AutoPilot ap;
		private VTDUtils vu;
		private boolean hasComment;
		private List<ErrorBean> errorList = new ArrayList<ErrorBean>();
		private Set<String> errorRowSet = new HashSet<String>();
		protected boolean continuImport = true;

		protected void doImport() {
			XLFValidator.resetFlag();
			if (!XLFValidator.validateXliffFile(xliffFile)) {
				return;
			}
			XLFValidator.resetFlag();
			final String docxPath = externalFile;
//			if (docxPath == null || docxPath.trim().equals("")) {
//				MessageDialog.openInformation(getShell(), Messages.getString("all.dialog.ok.title"),
//						Messages.getString("ImportDocxDialog.ok.msg1"));
//				return;
//			}
//			if (!new File(docxPath).exists()) {
//				MessageDialog.openWarning(getShell(), Messages.getString("all.dialog.warning"),
//						MessageFormat.format(Messages.getString("ImportDocxDialog.ok.msg2"), new Object[]{docxPath}));
//				return;
//			}
			// 开始进行处理
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask(Messages.getString("ImportDocxDialog.ok.monitor.msg0"), 10);
						beginImport(docxPath);
						monitor.done();
					} catch (Exception e) {
						LOGGER.error("", e);
					}
				}
			};

			try {
				new ProgressMonitorDialog(getShell()).run(true, true, runnable);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}

		/**
		 * 开始导入功能
		 */
		public boolean beginImport(final String docxPath) throws Exception {
			xlfHandler = new XLFHandler();
//			if (xliffEditor == null || xliffEditor.getXLFHandler() == null) {
//				
//			} else {
//				xlfHandler = xliffEditor.getXLFHandler();
//				Display.getDefault().syncExec(new Runnable() {
//					@Override
//					public void run() {
//						HsMultiActiveCellEditor.commit(true);
//					}
//				});
//				xm = new XMLModifier(xlfHandler.getVnMap().get(xliffFile));
//				xlfHandler.saveAndReparse(xm, xliffFile);
//			}

			// 开始解析　xliff 文件
			tempXlfHandler = new XLFHandler();
			parseXliff(tempXlfHandler);

			// UNDO 这里还应判断导入时两个文件是否对应，
			try {
				ImportDocx importWord = new ImportDocx(docxPath, xliffFile);
				List<RowBean> rowList = importWord.getDataFromWord();
				hasComment = importWord.isHasComment();

				// 现在开始判断每个tu 节点的标记是否异常。若有异常进行提示
				rowListFor: for (RowBean rowBean : rowList) {
					String rowId = rowBean.getRowId();
					if (rowId == null || rowId.length() <= 0) {
						continue rowListFor;
					}

					if (curRowIsLocked(rowId)) {
						rowBean.setLocked(true);
						continue rowListFor;
					}

					String srcText = getSrcTextFromXliffByRowId(rowId);
					List<String> xliffSrcTagList = getTagFromSrcText(srcText);
					List<String> xliffTgtTagList = new ArrayList<String>();
					xliffTgtTagList.addAll(xliffSrcTagList);

					// 获取 rowBean　中源文的标记
					List<TagBean> rowSrcTagList = new ArrayList<TagBean>();
					for (Object object : rowBean.getSrcElement()) {
						if (object instanceof TagBean) {
							rowSrcTagList.add((TagBean) object);
						}
					}

					// 下面开始验证文本段
					// 0、首先验证 word 文档中的当前行能否找到与之对应的　xliff　文件
					if (srcText == null) {
						errorList.add(new ErrorBean(rowBean.getIndex(), true, DocxConstant.ERROR_notFindTU));
						errorRowSet.add(rowBean.getRowId());
						continue rowListFor;
					}

					// 1、首先验证源文标记是否缺失
					if (rowSrcTagList.size() < xliffSrcTagList.size()) {
						errorList.add(new ErrorBean(rowBean.getIndex(), true, DocxConstant.ERROR_tagLose));
						errorRowSet.add(rowBean.getRowId());
						continue rowListFor;
					}

					// 2、验证word源文标记是否多出
					if (rowSrcTagList.size() > xliffSrcTagList.size()) {
						errorList.add(new ErrorBean(rowBean.getIndex(), true, DocxConstant.ERROR_tagMore));
						errorRowSet.add(rowBean.getRowId());
						continue rowListFor;
					}

					// 3、检查　word 源文本是否存在标记位置不对的情况，即成对标记开始与结束颠倒。
					// 根据源文的标记信息，将 rowBean 中的标记补全
					// UNDO 这里还应确定导出的标记是否发生改变
					for (TagBean bean : rowSrcTagList) {
						String rowTagText = bean.getText();
						for (int i = 0; i < xliffSrcTagList.size(); i++) {
							String xlfTagText = xliffSrcTagList.get(i);
							if (xlfTagText.indexOf(rowTagText) == 0) {
								if (!xlfTagText.equals(rowTagText)) {
									bean.setText(xlfTagText);
								}
								xliffSrcTagList.remove(i);
								bean.setTagType(DocxCommonFuction.getTagType(xlfTagText));
								break;
							}
						}
					}
					if (xliffSrcTagList.size() > 0) {
						// docx 文档中的标记被更换了
						errorList.add(new ErrorBean(rowBean.getIndex(), true, DocxConstant.ERROR_tagNotSame));
						errorRowSet.add(rowBean.getRowId());
						continue rowListFor;
					}
					int startTag = 0;
					for (TagBean bean : rowSrcTagList) {
						if (bean.getTagType() == DocxConstant.PAIRSTAR) {
							startTag++;
						} else if (bean.getTagType() == DocxConstant.PAIREND) {
							startTag--;
						}
						if (startTag < 0) {
							errorList.add(new ErrorBean(rowBean.getIndex(), true, DocxConstant.ERROR_tagPostionError));
							errorRowSet.add(rowBean.getRowId());
							continue rowListFor;
						}
					}

					// 4、验证 目标文本段中标记错误，或者位置不对应的情况
					// 先获得word 中目标文本中的标记
					// 获取 rowBean　中源文的标记
					List<TagBean> rowTgtTagList = new ArrayList<TagBean>();
					for (Object object : rowBean.getTgtElement()) {
						if (object instanceof TagBean) {
							rowTgtTagList.add((TagBean) object);
						}
					}
					int modifiedTagSum = 0;
					for (TagBean bean : rowTgtTagList) {
						String rowTagText = bean.getText();
						// 因为标记不允许修改，因此在　xliff　中，目标文本中的标记就是源文中的标记
						for (int i = 0; i < xliffTgtTagList.size(); i++) {
							String xlfTagText = xliffTgtTagList.get(i);
							if (xlfTagText.indexOf(rowTagText) == 0) {
								if (!xlfTagText.equals(rowTagText)) {
									bean.setText(xlfTagText);
								}
								xliffTgtTagList.remove(i);
								modifiedTagSum++;
								bean.setTagType(DocxCommonFuction.getTagType(xlfTagText));
								break;
							}
						}
					}
					if (modifiedTagSum != rowTgtTagList.size()) {
						// docx 文档中的标记被更换了
						errorList.add(new ErrorBean(rowBean.getIndex(), false, DocxConstant.ERROR_tagNotSame));
						errorRowSet.add(rowBean.getRowId());
						continue rowListFor;
					}
					startTag = 0;
					for (TagBean bean : rowTgtTagList) {
						if (bean.getTagType() == DocxConstant.PAIRSTAR) {
							startTag++;
						} else if (bean.getTagType() == DocxConstant.PAIREND) {
							startTag--;
						}
						if (startTag < 0) {
							errorList.add(new ErrorBean(rowBean.getIndex(), false, DocxConstant.ERROR_tagPostionError));
							errorRowSet.add(rowBean.getRowId());
							continue rowListFor;
						}
					}
					if (startTag != 0) {
						errorList.add(new ErrorBean(rowBean.getIndex(), false, DocxConstant.ERROR_pairTagError));
						errorRowSet.add(rowBean.getRowId());
					}
				}

				// 验证完后，开始导入功能，如果有错误提示信息，开始提示
				if (errorList.size() > 0) {
					StringBuffer errorSB = new StringBuffer();
					errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip1"));
					for (ErrorBean bean : errorList) {
						errorSB.append(MessageFormat.format(Messages.getString("ImportDocxDialog.import.errorTip2"),
								bean.getIndex()));

						if (bean.getErrorType() == DocxConstant.ERROR_notFindTU) {
							errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip3"));
							continue;
						}

						if (bean.isSrc()) {
							errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip4"));
						} else {
							errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip5"));
						}

						switch (bean.getErrorType()) {
						case DocxConstant.ERROR_tagLose:
							errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip6"));
							break;
						case DocxConstant.ERROR_tagMore:
							errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip7"));
							break;
						case DocxConstant.ERROR_tagPostionError:
							errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip8"));
							break;
						case DocxConstant.ERROR_tagNotSame:
							errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip9"));
							break;
						case DocxConstant.ERROR_pairTagError:
							errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip11"));
							break;
						default:
							break;
						}
					}
					errorSB.append(Messages.getString("ImportDocxDialog.import.errorTip10"));
					final String errorTip = errorSB.toString();
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							ErrorTipDialog errorDialog = new ErrorTipDialog(getShell(), errorTip);
							int result = errorDialog.open();

							if (result == IDialogConstants.CANCEL_ID) {
								continuImport = false;
							}
						}
					});

					if (!continuImport) {
						return false;
					}

					String rowId = "";
					// 先将　有错误的文本段进行清除
					for (int i = 0; i < rowList.size(); i++) {
						rowId = rowList.get(i).getRowId();
						if (errorRowSet.contains(rowId)) {
							rowList.remove(i);
							i--;
						}
					}
				}
				importDocxToXliffList(rowList);
				
//				if (xliffEditor != null) {
//					parseXliff(xlfHandler);
//					Display.getDefault().syncExec(new Runnable() {
//						@Override
//						public void run() {
//							xliffEditor.reloadData();
//							HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
//						}
//					});
//				}

			} catch (final FlagErrorException e) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openWarning(Display.getDefault().getActiveShell(),
								Messages.getString("all.dialog.warning"), e.getMessage());
					}
				});
				LOGGER.error(e.getMessage(), e);
				return false;
			} catch (Exception e) {
				LOGGER.error(Messages.getString("ImportDocxDialog.LOGGER.logger2"), e);
			}

			return true;
		}

		private boolean parseXliff(XLFHandler xlfHandler) throws Exception {
			Map<String, Object> resultMap = xlfHandler.openFile(xliffFile);
			if (resultMap == null
					|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap.get(Constant.RETURNVALUE_RESULT)) {
				// 打开文件失败。
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openWarning(getShell(), Messages.getString("all.dialog.warning"),
								MessageFormat.format(Messages.getString("ImportDocxDialog.ok.parseError"), _xliff));
						LOGGER.error(MessageFormat.format(Messages.getString("ImportDocxDialog.ok.parseError"), _xliff));
					}
				});
				return false;
			}

			vn = xlfHandler.getVnMap().get(xliffFile);
			ap = new AutoPilot(vn);
			vu = new VTDUtils(vn);
			xm = new XMLModifier(vn);

			return true;
		}

		/**
		 * 根据传入的　rowId 从　xliff 中获取当前文本段是否处理锁定状态，如果是锁定的，将不予更新。
		 * @param rowId
		 * @return
		 */
		private boolean curRowIsLocked(String rowId) throws Exception {
			ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
			if (ap.evalXPath() != -1) {
				int index = -1;
				if ((index = vn.getAttrVal("translate")) != -1) {
					return "no".equalsIgnoreCase(vn.toRawString(index));
				}
			}
			return false;
		}

		/**
		 * 根据传入的　rowId　获取源文文本段
		 * @param rowId
		 * @return <div style='color:red'>备注：如果返回为 null，证明无法找到对应的　tu 节点</div>
		 */
		private String getSrcTextFromXliffByRowId(String rowId) throws Exception {
			String srcText = null;
			ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
			if (ap.evalXPath() != -1) {
				ap.selectXPath("./source");
				if (ap.evalXPath() != -1) {
					srcText = vu.getElementContent();
				}
			}
			return srcText;
		}

		/**
		 * 该方法与类 {@link #ananysisTextWithTag} 类似
		 * @param srcText
		 * @return
		 */
		private List<String> getTagFromSrcText(String srcText) {
			List<String> tagList = new ArrayList<String>();
			if (srcText == null) {
				return tagList;
			}

			// 先不管是什么标记，按照　xml 的标记，把文本段中的节点提取出来。
			int index = srcText.indexOf("<");
			Map<Integer, TagBean> tagMap = new LinkedHashMap<Integer, TagBean>();
			int tagType = -1;
			while (index != -1) {
				int endIndex = srcText.length();
				int end_1 = srcText.indexOf(">", index + 1);
				int end_2 = srcText.indexOf("\\>", index + 1);
				endIndex = end_1 != -1 ? (endIndex < end_1 ? endIndex : end_1) : endIndex;
				endIndex = end_2 != -1 ? (endIndex < end_2 ? endIndex : end_2) : endIndex;
				String tagText = srcText.substring(index, endIndex + 1);

				if (tagText.indexOf("/>") != -1) {
					tagType = DocxConstant.NOTPAIR;
				} else if (tagText.indexOf("</") != -1) {
					tagType = DocxConstant.PAIREND;
				} else {
					tagType = DocxConstant.PAIRSTAR;
				}

				tagMap.put(index, new TagBean(index, endIndex, tagType, tagText));
				index = srcText.indexOf("<", index + 1);
			}

			// 开始处理　<ph> 标记的特殊情况
			TagBean bean = null;
			Integer[] keyArray = tagMap.keySet().toArray(new Integer[] {});
			int key = -1;
			for (int i = 0; i < keyArray.length; i++) {
				key = keyArray[i];
				bean = tagMap.get(key);
				if (bean.getText().indexOf("<ph") != -1 && bean.getTagType() == DocxConstant.PAIRSTAR) {
					int start = bean.getStartIndex();
					int end = bean.getEndIndex();

					int nextPhEndTagIdx = i + 1;
					while (nextPhEndTagIdx <= keyArray.length) {
						TagBean nextBean = tagMap.get(keyArray[nextPhEndTagIdx]);
						tagMap.remove(keyArray[nextPhEndTagIdx]);
						if (nextBean.getText().indexOf("</ph") != -1) {
							int nextEnd = nextBean.getEndIndex();
							end = nextEnd;
							String newText = srcText.substring(start, end + 1);
							bean.setTagType(DocxConstant.NOTPAIR);
							bean.setEndIndex(end);
							bean.setText(newText);
							i = nextPhEndTagIdx;
							break;
						}
						nextPhEndTagIdx++;
					}
				}
			}

			// 开始将所有标记装入结果集合中
			bean = null;
			for (Entry<Integer, TagBean> entry : tagMap.entrySet()) {
				tagList.add(entry.getValue().getText());
			}

			return tagList;
		}

		/**
		 * 将数据从 docx 中导入　xliff 文件。
		 */
		private void importDocxToXliffList(List<RowBean> rowList) throws Exception {
			String rowId = "";
			StringBuffer contentSB = new StringBuffer();
			for (RowBean bean : rowList) {
				rowId = bean.getRowId();
				if (bean.isLocked()) {
					continue;
				}

				if (rowId == null || rowId.length() <= 0) {
					continue;
				}

				ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
				if (ap.evalXPath() != -1) {
					// 开始处理源文本
					contentSB = new StringBuffer();
					vn.push();
					ap.selectXPath("./source");
					if (ap.evalXPath() != -1) {
						String header = vu.getElementHead();
						contentSB.append(header);
						for (Object object : bean.getSrcElement()) {
							if (object instanceof TagBean) {
								contentSB.append(((TagBean) object).getText());
							} else {
								contentSB.append(object);
							}
						}
						contentSB.append("</source>");
						xm.remove();
					}
					vn.pop();

					// 开始处理目标文本段
					ap.selectXPath("./target");
					if (ap.evalXPath() != -1) {
						String header = vu.getElementHead();
						contentSB.append(header);
						for (Object object : bean.getTgtElement()) {
							if (object instanceof TagBean) {
								contentSB.append(((TagBean) object).getText());
							} else {
								contentSB.append(object);
							}
						}
						contentSB.append("</target>");
						xm.remove();
						xm.insertAfterElement(contentSB.toString());
					} else {
						contentSB.append("<target>");
						for (Object object : bean.getTgtElement()) {
							if (object instanceof TagBean) {
								contentSB.append(((TagBean) object).getText());
							} else {
								contentSB.append(object);
							}
						}
						contentSB.append("</target>");

						xm.insertBeforeTail(contentSB.toString());
					}
				}
			}

			tempXlfHandler.saveAndReparse(xm, xliffFile);
			parseXliff(tempXlfHandler);

			// 再处理状态与批注的问题
			ananysisStatusAndComment(rowList);
			tempXlfHandler.saveAndReparse(xm, xliffFile);
		}

		/**
		 * 处理状态以及批注
		 * @param rowList
		 */
		private void ananysisStatusAndComment(List<RowBean> rowList) throws Exception {
			String rowId = "";

			for (RowBean bean : rowList) {
				if (bean.isLocked()) {
					continue;
				}
				rowId = bean.getRowId();
				if (rowId == null || rowId.length() <= 0) {
					continue;
				}

				int status = -1;
				boolean targetNull = true;
				ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
				if (ap.evalXPath() != -1) {
					// 先处理状态
					// 检查　target 是否为空
					vn.push();
					ap.selectXPath("./target[text()!='' or *]");
					if (ap.evalXPath() != -1) {
						targetNull = false;
					}
					vn.pop();

					// 如果译文为空，那状态应为未翻译，如果译文不为空，那状态应不为未翻译
					if (targetNull) {
						status = DocxConstant.STATUS_unstrans;
					}

					setOtherStatus(status);

					if (hasComment) {
						// 处理批注的问题。
						// 首先删除所有批注
						vn.push();
						ap.selectXPath("./note");
						while (ap.evalXPath() != -1) {
							xm.remove();
						}
						StringBuffer commentSB = new StringBuffer();

						if (bean.getComment() != null) {
							for (CommentBean commentBean : bean.getComment()) {
								// <note from='Mac'>2013-05-13:test</note>
								commentSB.append("<note from='");
								commentSB.append(commentBean.getUser() + "'>");
								commentSB.append(commentBean.getTime() + ":" + commentBean.getText());
								commentSB.append("</note>");
							}
						}
						vn.pop();
						if (commentSB.length() > 0) {
							xm.insertBeforeTail(commentSB.toString());
						}
					}

				}
			}
		}

		/**
		 * 处理状态，若译文为空，将状态更改成未翻译，若不为空，将状态更改成其他
		 */
		private void setOtherStatus(int status) throws Exception {
			vn.push();
			int index = -1;

			if (status == DocxConstant.STATUS_unstrans) {
				// 如果变成了未翻译，那就删除如下属性　translate　approved　state
				if ((index = vn.getAttrVal("approved")) != -1) {
					xm.removeAttribute(index - 1);
				}
				vn.push();
				ap.selectXPath("./target");
				if (ap.evalXPath() != -1) {
					if ((index = vn.getAttrVal("state")) != -1) {
						xm.removeAttribute(index - 1);
					}
				}
				vn.pop();
			} else {
				// 如果不是未翻译，则检查是否是草稿以上的状态
				vn.push();
				ap.selectXPath("./target");
				if (ap.evalXPath() != -1) {
					if ((index = vn.getAttrVal("state")) == -1) {
						xm.insertAttribute(" state=\"new\"");
					}
				}
				vn.pop();
			}

			vn.pop();
		}
	}

	static void printVN(VTDNav vn, boolean bool) {
		boolean debug = false || bool;
		if (!debug) {
			return;
		}
		try {
			long l = vn.getElementFragment();
			System.out.println(vn.toString((int) l, (int) (l >> 32)));
		} catch (NavException e) {
			e.printStackTrace();
		}
	}

	public void openErrorDialog(final Throwable e) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ErrorDialog.openError(shell, Messages.getString("all.dialog.error"), e.getMessage(), new Status(
						IStatus.ERROR, Activator.PLUGIN_ID, e.getCause() == null ? null : e.getCause().getMessage(), e));
			}
		});
	}

	// error
	public boolean openErrorDialog(final String message) {
		if (shell != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(shell, Messages.getString("all.dialog.error"), message);
				}
			});
		}
		return true;
	}

	// confirm
	public boolean openConfirmDialog(final String message) {
		final boolean[] bools = new boolean[1];
		bools[0] = false;
		if (shell != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					bools[0] = MessageDialog.openConfirm(shell, Messages.getString("all.dialog.confirm"), message);
				}
			});
		}
		return bools[0];
	}
	
	public boolean openErrorTipsDialog() {
		final boolean[] bools = new boolean[1];
		bools[0] = false;
		if (shell != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					StringBuilder builder = new StringBuilder();
					for (ExportReporter reporter : exportReporter) {
						builder.append(reporter.getRowInfo()).append("\n");
						Throwable cause = reporter.getE().getCause();
						if (cause != null) {
							builder.append(cause.getMessage());
							builder.append("\n");
						}
					}
					ErrorTipDialog dialog = new ErrorTipDialog(shell, builder.toString());
					dialog.open();
				}
			});
		}
		return bools[0];
	}
}
