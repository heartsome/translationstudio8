package net.heartsome.cat.ts.ui.external;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.InnerTagClearUtil;
import net.heartsome.cat.ts.core.bean.XliffBean;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.ui.docx.Activator;
import net.heartsome.cat.ts.ui.docx.ExportDocx;
import net.heartsome.cat.ts.ui.docx.common.CommentBean;
import net.heartsome.cat.ts.ui.docx.common.DisplayTags;
import net.heartsome.cat.ts.ui.docx.common.DocxConstant;
import net.heartsome.cat.ts.ui.docx.common.TUBean;
import net.heartsome.cat.ts.ui.docx.common.TagsResolver;
import net.heartsome.cat.ts.ui.docx.common.ZipUtil;
import net.heartsome.cat.ts.ui.docx.resource.Messages;
import net.heartsome.cat.ts.util.ProgressIndicatorManager;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathParseException;

public class ExportConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExportConfig.class.getName());

	/**  导出类型 <br>
	 * {@link ExportExternal#EXPORT_HSPROOF}<br>
	 * {@link ExportExternal#EXPORT_SDLUNCLEAN}<br>
	 * {@link ExportExternal#EXPORT_SDLXLIFF}<br>
	 * {@link ExportExternal#EXPORT_TMX}.*/
	private int exportType = -1;

	/**  导出 R8 校对文件时，是否导出状态. */
	private boolean hsProofState;
	/**  导出 R8 校对文件时，是否导出批注. */
	private boolean hsProofNote;

	/**  全部文本. */
	private boolean filterAllTrans;
	/**  仅导出. */
	private boolean filterSpecial;
	/**  状态为. */
	private boolean filterHasState;

	/**  排除锁定. */
	private boolean exceptLocked;
	/**  排除完全匹配. */
	private boolean exceptFullMatch;
	/**  排除上下文匹配. */
	private boolean exceptContextMatch;

	/**  含有批注. */
	private boolean withNote;
	/**  有疑问. */
	private boolean withNeedReview;

	/**  未翻译. */
	private boolean noTrans;
	/**  草稿. */
	private boolean newTrans;
	/**  已翻译. */
	private boolean transed;
	/**  已批准. */
	private boolean approved;

	/**  保存目录. */
	private String saveAsFolder;

	private List<IProject> projects;
	private Map<IProject, List<XliffBean>> mapXlfBeans;
	private Map<XliffBean, String> mapSaveAs;
	private Set<String> setSaveAs;
	
	// 进度条支持
	private IProgressMonitor monitor;
	// dialog 交互
	private Shell shell;

	public ExportConfig() {
	}

	public ExportConfig(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public void addXlfBean(IProject project, XliffBean xlfBean) {
		if (mapXlfBeans == null) {
			mapXlfBeans = new HashMap<IProject, List<XliffBean>>();
		}

		if (mapXlfBeans.get(project) == null) {
			if (projects == null) {
				projects = new LinkedList<IProject>();
				projects.add(project);
			}
			mapXlfBeans.put(project, new LinkedList<XliffBean>());
		}
		mapXlfBeans.get(project).add(xlfBean);
	}

	public String getSaveas() {
		return saveAsFolder;
	}

	public void setSaveas(String saveas) {
		this.saveAsFolder = saveas;
	}

	public List<XliffBean> getXlfBean(IProject project) {
		return mapXlfBeans.get(project);
	}

	public List<IProject> getProjects() {
		return projects;
	}

	public void setProjects(List<IProject> projects) {
		this.projects = projects;
	}

	public int getExportType() {
		return exportType;
	}

	public void setExportType(int exportType) {
		this.exportType = exportType;
	}

	public boolean isHsProofState() {
		return hsProofState;
	}

	public void setHsProofState(boolean hsProofState) {
		this.hsProofState = hsProofState;
	}

	public boolean isHsProofNote() {
		return hsProofNote;
	}

	public void setHsProofNote(boolean hsProofNote) {
		this.hsProofNote = hsProofNote;
	}

	public boolean isFilterAllTrans() {
		return filterAllTrans;
	}

	public void setFilterAllTrans(boolean filterAllTrans) {
		this.filterAllTrans = filterAllTrans;
	}

	public boolean isFilterSpecial() {
		return filterSpecial;
	}

	public void setFilterSpecial(boolean filterSpecial) {
		this.filterSpecial = filterSpecial;
	}

	public boolean isFilterHasState() {
		return filterHasState;
	}

	public void setFilterHasState(boolean filterHasState) {
		this.filterHasState = filterHasState;
	}

	public boolean isNotLocked() {
		return exceptLocked;
	}

	public void setNotLocked(boolean notLocked) {
		this.exceptLocked = notLocked;
	}

	public boolean isNotFullMatch() {
		return exceptFullMatch;
	}

	public void setNotFullMatch(boolean notFullMatch) {
		this.exceptFullMatch = notFullMatch;
	}

	public boolean isNotContextMatch() {
		return exceptContextMatch;
	}

	public void setNotContextMatch(boolean notContextMatch) {
		this.exceptContextMatch = notContextMatch;
	}

	public boolean isWithNote() {
		return withNote;
	}

	public void setWithNote(boolean withNote) {
		this.withNote = withNote;
	}

	public boolean isWithNeedReview() {
		return withNeedReview;
	}

	public void setWithNeedReview(boolean withNeedReview) {
		this.withNeedReview = withNeedReview;
	}

	public boolean isNoTrans() {
		return noTrans;
	}

	public void setNoTrans(boolean noTrans) {
		this.noTrans = noTrans;
	}

	public boolean isNewTrans() {
		return newTrans;
	}

	public void setNewTrans(boolean newTrans) {
		this.newTrans = newTrans;
	}

	public boolean isTransed() {
		return transed;
	}

	public void setTransed(boolean transed) {
		this.transed = transed;
	}

	public boolean isApprove() {
		return approved;
	}

	public void setApprove(boolean approve) {
		this.approved = approve;
	}

	public IProgressMonitor getMonitor() {
		return monitor;
	}

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	/**
	 * 导出 ;
	 * @throws IOException
	 */
	public void doExport() throws IOException {
		
		switch (exportType) {
		case ExportExternal.EXPORT_SDLUNCLEAN:
			//save as check
			saveAsCheckerUnclean();
			transunitLooper(new UncleanExporter());
			break;
		case ExportExternal.EXPORT_TMX:
			//save as check
			saveAsChecker();
			transunitLooper(new TmxExporter());
			break;
		case ExportExternal.EXPORT_HSPROOF:
			saveAsCheckerUnclean();
			// 封装 robert 所写的功能，不进行任何修改
			List<XliffBean> xlfBeanList = new LinkedList<XliffBean>();
			for (Entry<XliffBean, String> entry : mapSaveAs.entrySet()) {
				xlfBeanList.add(entry.getKey());
			}
			if (xlfBeanList.size() < 1) {
				return;
			}
			ExportHsproof export = new ExportHsproof(saveAsFolder, hsProofNote, hsProofState);
			export.okPressed(xlfBeanList);
			break;
		case ExportExternal.EXPORT_SDLXLIFF:

			break;
		default:
			break;
		}
	}
	
	private void saveAsCheckerUnclean() {
		setSaveAs = new LinkedHashSet<String>();
		mapSaveAs = new LinkedHashMap<XliffBean, String>();
		Map<String, XliffBean> tmpMap = new HashMap<String, XliffBean>();
		Set<String> tmpSet = new LinkedHashSet<String>();
		boolean overwrite = false;
		boolean noMoreAlert = false;
		for (Entry<IProject, List<XliffBean>> entry : mapXlfBeans.entrySet()) {
			for (XliffBean bean : entry.getValue()) {
				String strippedName = bean.getXliffFile().substring(bean.getXliffFile().lastIndexOf(File.separator) + 1);
				String saveAsName = "";
				if (exportType == ExportExternal.EXPORT_HSPROOF) {
					saveAsName = new File(bean.getXliffFile()).getName() + "_" + bean.getTargetLanguage() + ".docx";
				} else {
					saveAsName = strippedName(strippedName, "_" + bean.getTargetLanguage() + "_unclean.docx");
				}
				String saveAsFile = new StringBuilder(saveAsFolder).append(File.separator).append(saveAsName).toString();
				tmpMap.put(saveAsFile, bean);
				tmpSet.add(saveAsFile);
				mapSaveAs.put(bean, saveAsFile);
				setSaveAs.add(saveAsFile);
			}
		}
		
		for (String saveAsFile : tmpSet) {
			File file = new File(saveAsFile);
			if (file.exists()) {
				if (!noMoreAlert) {
					// 已存在名为 xx 的文件，是否覆盖？
					// 是，全部是，否，全部否。
					int i = ExportExternal.openConfirmDialog(getShell(), MessageFormat.format(Messages.getString("ExportDocxDialog.ok.msg5"), "docx", saveAsFile));
					switch (i) {
					case ExportExternal.CONFIRM_YES:
						noMoreAlert = false;
						overwrite = true;
						break;
					case ExportExternal.CONFIRM_YESTOALL:
						noMoreAlert = true;
						overwrite = true;
						break;
					case ExportExternal.CONFIRM_NO:
						noMoreAlert = false;
						overwrite = false;
						break;
					case ExportExternal.CONFIRM_NOTOALL:
						noMoreAlert = true;
						overwrite = false;
					}
				}
				if (! overwrite) {
					setSaveAs.remove(saveAsFile);
					mapSaveAs.remove(tmpMap.get(saveAsFile));
					//remove
				} else {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(file, true);
					} catch (FileNotFoundException e) {
						setSaveAs.remove(saveAsFile);
						mapSaveAs.remove(tmpMap.get(saveAsFile));
						//remove
						openErrorDialog(new ExportCanceledException(MessageFormat.format(
								Messages.getString("ExportExternal.error.cannotExport"), saveAsFile), e));
					} finally {
						if (fos != null) {
							try {
								fos.flush();
								fos.close();
							} catch (Exception e) {}// already closed
						}
					}
				}
			}
		}
	}
	
	private void saveAsChecker() {
		setSaveAs = new LinkedHashSet<String>();
		mapSaveAs = new LinkedHashMap<XliffBean, String>();
		Map<String, XliffBean> tmpMap = new HashMap<String, XliffBean>();
		Set<String> tmpSet = new LinkedHashSet<String>();
		boolean overwrite = false;
		boolean noMoreAlert = false;
		for (Entry<IProject, List<XliffBean>> entry : mapXlfBeans.entrySet()) {
			for (XliffBean bean : entry.getValue()) {
				String projectName = entry.getKey().getLocation().toOSString();
				projectName = projectName.substring(projectName.lastIndexOf(File.separator) + 1);
				String saveAsName = new StringBuilder().append(projectName).append("_").append(bean.getSourceLanguage()).append("_").append(bean.getTargetLanguage()).append(".tmx").toString();
				String saveAsFile = saveAsFolder + File.separator + saveAsName;
				mapSaveAs.put(bean, saveAsFile);
				setSaveAs.add(saveAsFile);
				tmpSet.add(saveAsFile);
				tmpMap.put(saveAsFile, bean);
			}
		}

		for (String saveAsFile : tmpSet) {
			File file = new File(saveAsFile);
			if (file.exists()) {
				if (!noMoreAlert) {
					// 已存在名为 xx 的文件，是否覆盖？
					// 是，全部是，否，全部否。
					int i = ExportExternal.openConfirmDialog(getShell(), MessageFormat.format(Messages.getString("ExportDocxDialog.ok.msg5"), "TMX", saveAsFile));
					switch (i) {
					case 0:
						noMoreAlert = false;
						overwrite = true;
						break;
					case 1:
						noMoreAlert = true;
						overwrite = true;
						break;
					case 2:
						noMoreAlert = false;
						overwrite = false;
						break;
					case 3:
						noMoreAlert = true;
						overwrite = false;
					}
				}
				if (! overwrite) {
					mapSaveAs.remove(tmpMap.get(saveAsFile));
					setSaveAs.remove(saveAsFile);
				} else {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(file);
					} catch (FileNotFoundException e) {
						mapSaveAs.remove(tmpMap.get(saveAsFile));
						setSaveAs.remove(saveAsFile);
						openErrorDialog(new ExportCanceledException(MessageFormat.format(
								Messages.getString("ExportExternal.error.cannotExport"), saveAsFile), e));
					} finally {
						if (fos != null) {
							try {
								fos.flush();
								fos.close();
							} catch (Exception e) {}// already closed
						}
					}
				}
			}
		}
	}
	
	private void transunitLooper(ExportWriter exper) {
		if (projects == null) {
			return;
		}
		final int steps = 100;
		int totalFile = mapSaveAs.size();
		
		String baseXpath1 = "/xliff/file";
		String baseXpath = "./body/descendant::trans-unit";
		int index = 0;
		for (IProject project : projects) {
			inner : for (XliffBean bean : mapXlfBeans.get(project)) {
				
				String saveAs = mapSaveAs.get(bean);
				if (!setSaveAs.contains(saveAs)) {
					return;
				}
				String xlfFileName = bean.getXliffFile().substring(bean.getXliffFile().lastIndexOf(File.separator) + 1);

				IProgressMonitor subMonitor = null;
				// 进度条信息
				if (monitor != null) {
					monitor.beginTask("[" + ++index + "/" + totalFile + "]", 1);
				}

				try {
					exper.init(project, bean);
				} catch (OperationCanceledException e) {
					continue;
				} catch (ExportExternalException e) {
					LOGGER.error("", e);
					openErrorDialog(e);
					return;
				} catch (ExportCanceledException e) {
					LOGGER.error("", e);
					openErrorDialog(e);
					continue;
				}

				VTDUtils vu = new VTDUtils();
				try {
					vu.parseFile(bean.getXliffFile(), true);
				} catch (Exception e) {
					if (openErrorDialog(MessageFormat.format(Messages.getString("ExportDocxDialog.ok.parseError"),
							xlfFileName))) {
						continue;
					}
				}
				try {
					VTDNav vn = vu.getVTDNav();
					AutoPilot apFile = new AutoPilot(vn);
					AutoPilot ap = new AutoPilot(vn);// 总控制
					AutoPilot _ap = new AutoPilot(vn);// 临时用
					apFile.declareXPathNameSpace("hs", ExportExternal.NAMESPACE_HS);
					ap.declareXPathNameSpace("hs", ExportExternal.NAMESPACE_HS);
					_ap.declareXPathNameSpace("hs", ExportExternal.NAMESPACE_HS);
					

					int page = 0;
					
					double total = 0;
					int worked = 0;
					int count = 0;
					int tmp = 0;
					if (monitor != null) {
						_ap.selectXPath("count(/xliff/file/body/descendant::trans-unit)");
						total = _ap.evalXPathToNumber();
						if (total == 0) {
							exper.canceled();
							continue;
						}
						subMonitor = new SubProgressMonitor(monitor, 1);
						subMonitor.setTaskName("[" + index + "/" + totalFile + "]"
								+ Messages.getString("ExportDocxDialog.ok.monitor.msg0") + xlfFileName);
						subMonitor.beginTask("subtask", (total > steps ? steps : (int) total) + 1);
					}

					apFile.selectXPath(baseXpath1);
					while (apFile.evalXPath() != -1) {
						page++;
						ap.selectXPath(baseXpath);
						while (ap.evalXPath() != -1) {
							// monitor
							if (subMonitor != null) {
								if (subMonitor.isCanceled()) {
									exper.canceled();
									return;
								}
								tmp = (int) ((count++ / total) * steps);
								if (tmp > worked) {
									subMonitor.worked(tmp - worked);
									worked = tmp;
								}
							}
	
							if (filterAllTrans) {// all except locked, full match, context match
								// continue if bingo （排除此项）
								if (exceptLocked &&
										testXpath(_ap, "./@translate='no'")) {
										continue;
								}
								// should we use machine translation??
								if (exceptFullMatch
										&& testXpath(_ap, "./target/@hs:matchType and ./target/@hs:quality='100'")) {
										continue;
								}
								// should we except machine translation ??
								if (exceptContextMatch
										&& testXpath(_ap, "./target/@hs:matchType and ./target/@hs:quality='101'")) {
										continue;
								}
							} else if (filterSpecial) {// just contains <note> or state='need-review'
								boolean bingo = false;
								if (withNote) {
									bingo = testXpath(_ap, "./note");
								}
								if (!bingo && withNeedReview) {
									bingo = testXpath(_ap, "./@hs:needs-review='yes'");
								}
								if (!bingo) {
									continue;
								}
							} else if (filterHasState) {// when tu not/new/has translated, or approved
								boolean bingo = false;
								if (!bingo && noTrans) {
									bingo = testXpath(_ap, "string-length(./target/text()) < 1") && !testXpath(_ap, "./target/*");
								}
								if (!bingo && newTrans) {
									bingo = testXpath(_ap, "./target/@state='new'");
								}
								if (!bingo && transed) {
									bingo = testXpath(_ap, "./target/@state='translated'")
											&& !testXpath(_ap, "./@approved='yes'");
								}
								if (!bingo && approved) {
									bingo = testXpath(_ap, "./@approved='yes'");
								}
								if (!bingo) {
									continue;
								}
							}
							
							try {
								exper.append(vu, count, page);
							} catch (ExportExternalException e) {
								openErrorDialog(e);
								continue inner;
							}
						}
					}
					try {
						exper.flush();
					} catch (ExportExternalException e) {
						openErrorDialog(e);
						continue;
					}

					if (subMonitor != null) {
						subMonitor.done();
					}
				} catch (Exception e) {
					e.printStackTrace();
					openErrorDialog(e);
					return;
				}
			}
		}
		exper.close();
		if (monitor != null) {
			monitor.done();
		}
	}

	abstract class ExportWriter {

		/**
		 * @param project
		 * @param bean
		 * @throws ExportExternalException
		 *             模板错误，程序内部错误，无法保存新文件等
		 * @throws OperationCanceledException
		 *             生成新文件不进行覆盖操作;
		 */
		abstract void init(IProject project, XliffBean bean) throws ExportExternalException, ExportCanceledException,
				OperationCanceledException;

		public void close() {
		}

		abstract void append(VTDUtils vu, int row, int page) throws ExportExternalException;

		abstract void flush() throws ExportExternalException;

		/**
		 * 导出失败时清理临时;
		 */
		abstract void canceled();
	}

	/**
	 * 导出 unclean <li>1.init 文件合法性检测，其他初始化操作；</li> <li>2.写 trans-unit 字段；</li> <li>3.flush 到硬盘</li> <li>
	 * 4.如果有异常，或者人为结束任务，清理临时文件</li>
	 * @author Austen
	 * @version
	 * @since JDK1.6
	 */
	class UncleanExporter extends ExportWriter {

		private int calibration = 0;
		private final String fldCharBegin = "<w:fldChar w:fldCharType=\"begin\"/>";
		private final String fldCharSeparate = "<w:fldChar w:fldCharType=\"separate\"/>";
		private final String fldCharEnd = "<w:fldChar w:fldCharType=\"end\"/>";
		private final String tw4winMark = "<w:rStyle w:val=\"tw4winMark\" />";
		private final String hsrow = "<w:rStyle w:val=\"HSRow\" />";
		private final String noProof = "<w:noProof w:val=\"true\"/>";

		private String templateDocxFile;
		private String tmpUnzipFolder;
		private String doc_end_fragment;
		private String saveAsFile;

		private FileWriter docxmlWriter;
		private TagsResolver tr = new TagsResolver();
		
		// 生成 sdl 空文档
		@Override
		void init(IProject project, XliffBean bean) throws ExportExternalException, ExportCanceledException,
				OperationCanceledException {
			saveAsFile = mapSaveAs.get(bean);
			// check template file.
			try {
				templateDocxFile = Activator.getLocation(ExportExternal.TEMPLATE_DOCX);
				if (templateDocxFile == null) {
					ExportExternalException e = new ExportExternalException(
							Messages.getString("ExportDocx.logger.templeDocxNotExsit"));
					LOGGER.error("", e);
					throw e;
				}
				tmpUnzipFolder = saveAsFolder + "/.tmp" + System.currentTimeMillis();
				ZipUtil.upZipFile(templateDocxFile, tmpUnzipFolder);
				docxmlWriter = new FileWriter(tmpUnzipFolder + "/word/document.xml");
			} catch (IOException e) {
				LOGGER.error("", e);
				canceled();
				throw new ExportExternalException(Messages.getString("ExportDocx.logger.templeDocxNotExsit"), e);
			}

			// add sdl/tag style and build document
			writeDocumentxml(bean);
			addSDLStyle();
		}

		@Override
		void append(VTDUtils vu, int row, int page) throws ExportExternalException {
			StringBuilder builder = new StringBuilder();
			// let do it
			AutoPilot _ap = new AutoPilot(vu.getVTDNav());
			_ap.declareXPathNameSpace("hs", ExportExternal.NAMESPACE_HS);
			String rowid = null;
			String src = null;
			String tgt = null;
			String match = null;
			try {
				_ap.selectXPath("./@id");
				rowid = _ap.evalXPathToString();
				src = vu.getElementContent("./source");
				if (src == null) {
					calibration++;
					return;
				}
				tgt = vu.getElementContent("./target");
				_ap.selectXPath("./target/@hs:quality");
				match = _ap.evalXPathToString();
				match = match.isEmpty() ? "0" : match;
				if (tgt == null || tgt.isEmpty()) {
					tgt = src;
				}
			} catch (Exception e) {
				LOGGER.error("", e);
				canceled();
				throw new ExportExternalException(Messages.getString("all.dialog.error"), e);
			}
			builder.append("<w:p>")
					// p
					.append("<w:pPr><w:jc w:val=\"left\" /></w:pPr>")
					// append rowid .append("<w:rPr>").append(hsrow).append("</w:rPr>")
					.append("<w:r>").append(fldCharBegin).append("</w:r>")
					.append("<w:r><w:rPr><w:noProof /></w:rPr>").append("<w:instrText>").append("QUOTE \"").append(page).append("_").append(rowid).append("\"</w:instrText></w:r>")
					.append("<w:r>").append(fldCharSeparate).append("</w:r>")
					.append("<w:r><w:rPr>").append(hsrow).append("<w:noProof /></w:rPr><w:t>[").append(row - calibration).append("]</w:t></w:r>")
					.append("<w:r>").append(fldCharEnd).append("</w:r>")
					// append left flag
					.append("<w:r>").append("<w:rPr>").append(tw4winMark).append("<w:noProof /></w:rPr>").append("<w:t>").append("{0&gt;").append("</w:t>").append("</w:r>")
					// append src
					.append(encodeSrcContent(src))
					// append center flag
					.append("<w:r>").append("<w:rPr>").append(tw4winMark).append("<w:noProof /></w:rPr>").append("<w:t>")
					.append("&lt;}").append(match).append("{&gt;").append("</w:t>").append("</w:r>")
					// append tgt
					.append(encodeTgtContent(tgt == null ? src : tgt))
					// append end flag
					.append("<w:r>").append("<w:rPr>").append(tw4winMark).append("<w:noProof /></w:rPr>").append("<w:t>")
					.append("&lt;0}").append("</w:t>").append("</w:r>").append("</w:p>");
			try {
				docxmlWriter.write(builder.toString());
			} catch (IOException e) {
				LOGGER.error("", e);
				canceled();
				throw new ExportExternalException("", e);
			}
		}

		void flush() throws ExportExternalException {
			try {
				docxmlWriter.write(doc_end_fragment);
				docxmlWriter.close();
				ZipUtil.zipFolder(saveAsFile, tmpUnzipFolder);
				deleteTmpFiles();
			} catch (IOException e) {
				LOGGER.error("", e);
				canceled();
				throw new ExportExternalException(e.getMessage(), e);
			}
		}

		@Override
		void canceled() {
			if (docxmlWriter != null) {
				try {
					docxmlWriter.close();
				} catch (IOException e) {
				}
			}
			deleteTmpFiles();
		}

		private String encodeTgtContent(String content) {
			StringBuilder builder = new StringBuilder();
			tr.reset(content);
			tr.reslove();
			List<DisplayTags> dl = tr.getDisplayText();
			for (DisplayTags dt : dl) {
				if (dt.isShow()) {
					builder.append("<w:r><w:rPr><w:noProof /></w:rPr>").append("<w:t xml:space=\"preserve\">").append(dt.getContent())
							.append("</w:t>").append("</w:r>");
				} else {
					builder
					// begin
					.append("<w:r>").append(fldCharBegin).append("</w:r>")
							.append("<w:r><w:rPr><w:noProof /></w:rPr>")
							// tag content
							.append("<w:instrText>").append("QUOTE \"").append(dt.getContent())
							.append("\"</w:instrText>")
							.append("</w:r>")
							// separate
							.append("<w:r>").append(fldCharSeparate)
							.append("</w:r>")
							// text
							.append("<w:r>").append("<w:rPr>").append(tw4winMark)
							.append("<w:rStyle w:val=\"Tag\"/><w:noProof/></w:rPr>").append("<w:t>")
							.append(dt.getDisplayText()).append("</w:t>").append("</w:r>")
							// end
							.append("<w:r>").append(fldCharEnd).append("</w:r>");
				}
			}
			return builder.toString();
		}

		private String encodeSrcContent(String content) {
			StringBuilder builder = new StringBuilder();
			tr.reset(content);
			tr.reslove();
			List<DisplayTags> dl = tr.getDisplayText();
			for (DisplayTags dt : dl) {
				if (dt.isShow()) {
					builder.append("<w:r>").append("<w:rPr><w:vanish /><w:noProof /></w:rPr>")
							.append("<w:t xml:space=\"preserve\">").append(dt.getContent()).append("</w:t>")
							.append("</w:r>");
				} else {
					builder
					// begin
					.append("<w:r>").append(fldCharBegin).append("</w:r>")
							// insert tag
							.append("<w:r xml:space=\"preserve\"><w:rPr><w:noProof /></w:rPr><w:instrText>").append("QUOTE \"").append(dt.getDisplayText()).append("\"</w:instrText></w:r>")
							// separate
							.append("<w:r>").append(fldCharSeparate).append("</w:r>")
							// display
							.append("<w:r><w:rPr><w:rStyle w:val=\"Tag\"/><w:noProof /><w:vanish /></w:rPr>").append("<w:t>")
							.append(dt.getDisplayText()).append("</w:t>").append("</w:r>")
							// end
							.append("<w:r>").append(fldCharEnd).append("</w:r>");
				}
			}
			return builder.toString();
		}

		/**
		 * sdl 双语样式。
		 * @throws ExportExternalException
		 *             模板错误或者程序内部错误
		 */
		private void addSDLStyle() throws ExportExternalException {
			VTDGen vg = new VTDGen();
			if (!vg.parseZIPFile(templateDocxFile, "word/styles.xml", true)) {
				throw new ExportExternalException(Messages.getString("ExportExternal.error.templateError"));
			}
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace("w", ExportExternal.NAMESPACE_W);

			String styleCode = "<w:style w:type=\"character\" w:customStyle=\"1\" w:styleId=\"tw4winMark\"><w:name w:val=\"tw4winMark\"/>"
					+ "<w:rPr><w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\" w:cs=\"Courier New\"/><w:vanish/><w:color w:val=\"800080\"/>"
					+ "</w:rPr></w:style>\n";
			String styleCode_tag = "<w:style w:type=\"character\" w:styleId=\"Tag\" w:customStyle=\"true\">"
					+ "<w:name w:val=\"Tag\"/><w:basedOn w:val=\"DefaultParagraphFont\"/><w:uiPriority w:val=\"1\"/><w:qFormat/><w:rPr><w:i/><w:color w:val=\"FF0066\"/></w:rPr></w:style>";
			String rowidStyle = "<w:style w:type=\"character\" w:styleId=\"HSRow\" w:customStyle=\"true\">" +
					"<w:name w:val=\"HSRow\" />" +
					"<w:rPr><w:rFonts w:ascii=\"Consolas\"/><w:color w:val=\"0070C0\"/><w:b /></w:rPr></w:style>";
			
			try {
				ap.selectXPath("/w:styles/w:style[last()]");
				if (ap.evalXPath() != -1) {
					XMLModifier xm = new XMLModifier(vn);
					xm.insertAfterElement(styleCode + styleCode_tag + rowidStyle);
					xm.output(tmpUnzipFolder + "/word/styles.xml");
				}
			} catch (Exception e) {
				LOGGER.error("", e);
				throw new ExportExternalException(e.getMessage(), e);
			}
		}

		private void writeDocumentxml(XliffBean bean) throws ExportExternalException {
			VTDGen vg = new VTDGen();
			if (!vg.parseZIPFile(templateDocxFile, "word/document.xml", true)) {
				canceled();
				throw new ExportExternalException(Messages.getString("ExportExternal.error.templateError"));
			}
			try {
				VTDNav vn = vg.getNav();
				AutoPilot ap = new AutoPilot(vn);
				ap.declareXPathNameSpace("w", ExportExternal.NAMESPACE_W);
				ap.selectXPath("/w:document");
				if (ap.evalXPath() == -1) {
					throw new ExportExternalException(Messages.getString("ExportExternal.error.templateError"));
				}

				long l = vn.getElementFragment();
				int dOffset = (int) l;
				int dLength = (int) (l >> 32);

				ap.selectXPath("/w:document/w:body/w:p");
				if (ap.evalXPath() == -1) {
					// we should write in body!!
					canceled();
					throw new ExportExternalException(Messages.getString("ExportExternal.error.templateError"));
				} else {
					l = vn.getElementFragment();
					int offset = (int) l;
					int length = (int) (l >> 32);
					String str = vn.toString(0, length + offset);
					docxmlWriter.write(str);
					doc_end_fragment = vn.toString(length + offset, dOffset + dLength - offset - length);
				}
				//we should export some info about this unclean
			} catch (Exception e) {
				LOGGER.error("", e);
				canceled();
				throw new ExportExternalException(e.getMessage(), e);
			}
		}

		private void deleteTmpFiles() {
			if (tmpUnzipFolder != null) {
				deleteTmpFiles(new File(tmpUnzipFolder));
			}
		}

		private void deleteTmpFiles(File file) {
			if (!file.exists()) {
				return;
			}
			if (file.isDirectory()) {
				for (File subFile : file.listFiles()) {
					deleteTmpFiles(subFile);
				}
			}
			file.delete();
		}
	}

	class TmxExporter extends ExportWriter {

		private String srcLang;
		private String tgtLang;
		private String saveFile;
		private Map<String, FileWriter> mapWriter = new HashMap<String, FileWriter>();
		private String saveAsFile;
		
		@Override
		void init(IProject project, XliffBean bean) throws ExportCanceledException {
			saveAsFile = mapSaveAs.get(bean);
			srcLang = bean.getSourceLanguage();
			tgtLang = bean.getTargetLanguage();
			try {
				if (mapWriter.get(saveAsFile)== null) {
					File file = new File(saveAsFile);
					if (file.exists()) {
						file.delete();
					}
					FileWriter writer = new FileWriter(file, true);
					mapWriter.put(saveAsFile, writer);
					writeHeader();
				}
			} catch (IOException e) {
				LOGGER.error("", e);
				throw new ExportCanceledException(MessageFormat.format(
						Messages.getString("ExportExternal.error.cannotExport"), mapSaveAs.get(bean)), e);
			}
		}

		@Override
		void append(VTDUtils vu, int row, int page) throws ExportExternalException {
			String src = null;
			String tgt = null;
			try {
				src = InnerTagClearUtil.clearXliffTag4Tmx(vu.getElementContent("./source"));
				tgt = InnerTagClearUtil.clearXliffTag4Tmx(vu.getElementContent("./target"));
				if (tgt == null || tgt.isEmpty() || src == null || src.isEmpty()) {
					return;
				}
			} catch (Exception e) {
				LOGGER.error("", e);
				throw new ExportExternalException(Messages.getString("all.dialog.error"), e);
			}

			StringBuilder builder = new StringBuilder();
			builder.append("<tu>\n");
			builder.append("<tuv xml:lang=\"").append(srcLang).append("\"><seg>").append(src)
					.append("</seg></tuv>\n<tuv xml:lang=\"").append(tgtLang).append("\"><seg>");
			builder.append(tgt).append("</seg></tuv>\n");
			builder.append("</tu>\n");
			try {
				mapWriter.get(saveAsFile).append(builder.toString());
			} catch (IOException e) {
				LOGGER.error("", e);
				throw new ExportExternalException(Messages.getString("all.dialog.error"), e);
			}
		}

		@Override
		void flush() {
			//do nothing
		}

		@Override
		public void close() {
			for (Entry<String, FileWriter> entry : mapWriter.entrySet()) {
				try {
				FileWriter w = entry.getValue();
				w.write("</body>");
				w.write("</tmx>");
				w.close();
				} catch (Exception e) { // already closed.
					LOGGER.error("", e);
				}
			}
		}
		
		@Override
		void canceled() {
			// delete source, close writer
			if (mapWriter.get(saveAsFile) != null) {
				try {
					mapWriter.get(saveAsFile).close();
				} catch (Exception e) {// already closed
				}
			}
			if (saveFile != null) {
				File file = new File(saveFile);
				if (file.exists()) {
					file.delete();
				}
			}

		}

		private void writeHeader() throws IOException {
			StringBuilder builder = new StringBuilder();
			builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
					.append("<!DOCTYPE tmx PUBLIC \"-//LISA OSCAR:1998//DTD for Translation Memory eXchange//EN\" \"tmx14.dtd\" >\n")
					.append("<tmx version=\"1.4\">\n")
					.append("<header \n" + "      creationtool=\"Heartsome TM Server\" \n"
							+ "      creationtoolversion=\"2.0-1\" \n" + "      srclang=\"" + srcLang + "\" \n"
							+ "      adminlang=\"en\"  \n" + "      datatype=\"xml\" \n" + "      o-tmf=\"unknown\" \n"
							+ "      segtype=\"block\" \n" + "      creationdate=\"" + creationDate() + "\"\n>\n"
							+ "</header>\n<body>\n");
			mapWriter.get(saveAsFile).write(builder.toString());
		}

		/**
		 * 获取创建时间
		 * @return ;
		 */
		private String creationDate() {
			System.currentTimeMillis();

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

	private class ExportHsproof {

		private String strXliffFullPath;
		private boolean commentSelection;
		private boolean statusSelection;
		private AutoPilot otherAP;

		ExportHsproof(String tgtLang, boolean commentSelection, boolean statusSelection) {
			this.commentSelection = commentSelection;
			this.statusSelection = statusSelection;
		}

		void okPressed(final List<XliffBean> xlfBeanList) {
			int index = 0;
			for (XliffBean bean : xlfBeanList) {
				XLFValidator.resetFlag();
				if (!XLFValidator.validateXliffFile(bean.getXliffFile())) {
					xlfBeanList.remove(index);
				}
				index++;
			}

			// 设置查询每个 tu 的条件，比如排除或者　仅导出
			String expandXpath = "";
			if (filterAllTrans) {
				if (exceptLocked) {
					expandXpath += " and not(@translate='no')";
				}
				if (exceptFullMatch) {
					expandXpath += " and not(target[@hs:quality='100'])";
				}
				if (exceptContextMatch) {
					expandXpath += " and not(target[@hs:quality='101'])";
				}
			} else if (filterSpecial) {
				if (withNote || withNeedReview) {
					expandXpath += " and (";
					if (withNote) {
						expandXpath += "note/text()!=''";
					}
					if (withNeedReview) {
						if (withNote) {
							expandXpath += " or ";
						}
						expandXpath += " @hs:needs-review='yes'";
					}
					expandXpath += ")";
				}
			} else if (filterHasState) {
				boolean first = false;
				if (newTrans || noTrans || transed || approved) {
					expandXpath += " and (";
				}
				if (newTrans) {
					expandXpath += "./target/@state='new'";
					first = true;
				}
				if (noTrans) {
					if (first) {
						expandXpath += " or ";
					}
					expandXpath += "(string-length(./target/text()) < 1 and not(./target/*))";
					first = true;
				}
				if (transed) {
					if (first) {
						expandXpath += " or ";

					}
					expandXpath += "(./target/@state='translated' and not(./@approved='yes'))";
					first = true;
				}
				if (approved) {
					if (first) {
						expandXpath += " or ";
					}
					expandXpath += "./@approved='yes'";
					first = true;
				}
				if (first) {
					expandXpath += ")";
				}
			}
			final String finalExpandXpath = expandXpath;

			// 这里开始调用导出的方法
			Job job = new Job(Messages.getString("ExportDocxDialog.ok.monitor.title")) {
				protected IStatus run(final IProgressMonitor monitor) {
					try {
						// 文件等分 austen
						monitor.beginTask(Messages.getString("ExportDocxDialog.ok.monitor.msg0"), xlfBeanList.size());
						for (XliffBean bean : xlfBeanList) {
							strXliffFullPath = bean.getXliffFile();
							final String finalDocxPath = mapSaveAs.get(bean);
									//saveAsFolder + File.separator + new File(strXliffFullPath).getName() + "_" + bean.getTargetLanguage() +".docx";

							IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
							// 解析文件花1格。读取　xliff 数据花 1 格，导出花　18　格。
							subMonitor.beginTask(Messages.getString("ExportDocxDialog.ok.monitor.msg0"), 20);
							beginExport(subMonitor, finalDocxPath, commentSelection, statusSelection, finalExpandXpath);
							// Display.getDefault().syncExec(new Runnable() {
							// @Override
							// public void run() {
							// MessageDialog.openInformation(getShell(), Messages.getString("all.dialog.ok.title"),
							// Messages.getString("ExportDocxDialog.ok.msg4"));
							// }
							// });
							subMonitor.done();
						}
						monitor.done();
					} catch (OperationCanceledException e) {
						// do nothing
					} catch (final Exception e) {
						e.printStackTrace();
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openError(getShell(), Messages.getString("all.dialog.error"),
										Messages.getString("ExportDocxDialog.ok.exportError") + "\n" + e.getMessage());
							}
						});
						LOGGER.error("Export xliff to MS WORD error\n" + e.getMessage(), e);
					}
					return Status.OK_STATUS;
				}
			};

			// 当程序退出时，检测当前　job 是否正常关闭
			CommonFunction.jobCantCancelTip(job);
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void running(IJobChangeEvent event) {
					ProgressIndicatorManager.displayProgressIndicator();
					super.running(event);
				}

				@Override
				public void done(IJobChangeEvent event) {
					ProgressIndicatorManager.hideProgressIndicator();
					super.done(event);
				}
			});
			job.setUser(true);
			job.schedule();
		}

		private Shell getShell() {
			return shell;
		}

		/**
		 * 开始导出功能
		 */
		private void beginExport(IProgressMonitor monitor, final String docxPath, boolean exportComment,
				boolean exportStatus, String expandXpath) throws Exception {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}

			VTDGen vg = new VTDGen();
			if (!vg.parseFile(strXliffFullPath, true)) {
				final String parseErrorTip = MessageFormat.format(Messages.getString("ExportDocxDialog.parseError"),
						ResourceUtils.fileToIFile(docxPath).getFullPath().toOSString());
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(getShell(), Messages.getString("all.dialog.warning"), parseErrorTip);
					}
				});
				throw new Exception(parseErrorTip);
			}
			monitor.worked(1);

			VTDNav vn = vg.getNav();
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace("hs", "http://www.heartsome.net.cn/2008/XLFExtension");
			otherAP = new AutoPilot(vn);
			otherAP.declareXPathNameSpace("hs", "http://www.heartsome.net.cn/2008/XLFExtension");
			AutoPilot childAP = new AutoPilot(vn);
			childAP.declareXPathNameSpace("hs", "http://www.heartsome.net.cn/2008/XLFExtension");
			String srcLang = "";
			String tgtLang = "";
			// 备注：目前只支持处理一种目标语言的情况
			ap.selectXPath("/xliff//file[1]");
			if (ap.evalXPath() != -1) {
				srcLang = vu.getCurrentElementAttribut("source-language", "");
				tgtLang = vu.getCurrentElementAttribut("target-language", "");
			}

			if ("".equals(srcLang)) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openWarning(getShell(), Messages.getString("all.dialog.warning"),
								Messages.getString("ExportDocxDialog.ok.msg1"));
					}
				});
				// LOGGER.error(Messages.getString("ExportDocxDialog.ok.msg1"));
			}
			if ("".equals(tgtLang)) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openWarning(getShell(), Messages.getString("all.dialog.warning"),
								Messages.getString("ExportDocxDialog.ok.msg2"));
					}
				});
				// LOGGER.error(Messages.getString("ExportDocxDialog.ok.msg2"));
			}

			String xpath = "/xliff/file[@source-language='" + srcLang + "' and @target-language='" + tgtLang
					+ "']/body/descendant::trans-unit[(source/text()!='' or source/*)" + expandXpath + "]";
			ap.selectXPath(xpath);
			List<TUBean> tuDataList = new ArrayList<TUBean>();

			String rowId = "";
			int status = DocxConstant.STATUS_unstrans;
			boolean approved = false;
			boolean isLocked = false;
			boolean isNotSendToTm = false;
			boolean needsReview = false;
			int index = -1;
			while (ap.evalXPath() != -1) {
				status = DocxConstant.STATUS_unstrans;
				approved = false;
				isLocked = false;
				isNotSendToTm = false;
				needsReview = false;

				TUBean bean = new TUBean();
				rowId = RowIdUtil.getSpecialRowId(vn);
				bean.setRowId(rowId);

				// 是否批准
				if ((index = vn.getAttrVal("approved")) != -1) {
					if ("yes".equals(vn.toString(index))) {
						approved = true;
					}
				}

				// 是否锁定
				if ((index = vn.getAttrVal("translate")) != -1) {
					if ("no".equals(vn.toString(index))) {
						isLocked = true;
					}
				}

				// 是否不发送数据库
				if ((index = vn.getAttrVal("hs:send-to-tm")) != -1) {
					if ("no".equals(vn.toString(index))) {
						isNotSendToTm = true;
					}
				}

				// 是否是疑问文本段
				if ((index = vn.getAttrVal("hs:needs-review")) != -1) {
					if ("yes".equals(vn.toString(index))) {
						needsReview = true;
					}
				}

				vn.push();
				childAP.selectXPath("./source");
				if (childAP.evalXPath() != -1) {
					bean.setSrcText(vu.getElementContent());
				}
				vn.pop();

				vn.push();
				childAP.selectXPath("./target");
				if (childAP.evalXPath() != -1) {
					if ((index = vn.getAttrVal("state")) != -1) {
						String stateStr = vn.toString(index);
						if ("new".equals(stateStr)) {
							status = DocxConstant.STATUS_draft; // 草稿
						} else if ("translated".equals(stateStr)) {
							if (approved) {
								status = DocxConstant.STATUS_approved; // 批准翻译
							} else {
								status = DocxConstant.STATUS_translated; // 　完成翻译
							}
						} else if ("signed-off".equals(stateStr) && approved) {
							status = DocxConstant.STATUS_signedOff; // 签发
						}
					}

					bean.setTgtText(vu.getElementContent());
				} else {
					status = DocxConstant.STATUS_unstrans;
				}
				vn.pop();

				// 这里参照界面上状态的写法，分三个部份，第一个为（草稿，已翻译，完成翻译，批注，签发，锁定），　第二部份为不送至库，第三部份为疑问。
				String beanStatus = "";
				if (isLocked) {
					beanStatus += Messages.getString("ExportDocxDialog.ok.status.locked");
				} else {
					switch (status) {
					case DocxConstant.STATUS_unstrans:
						beanStatus += Messages.getString("ExportDocxDialog.ok.status.unstrans");
						break;
					case DocxConstant.STATUS_draft:
						beanStatus += Messages.getString("ExportDocxDialog.ok.status.draft");
						break;
					case DocxConstant.STATUS_translated:
						beanStatus += Messages.getString("ExportDocxDialog.ok.status.translated");
						break;
					case DocxConstant.STATUS_approved:
						beanStatus += Messages.getString("ExportDocxDialog.ok.status.approved");
						break;
					case DocxConstant.STATUS_signedOff:
						beanStatus += Messages.getString("ExportDocxDialog.ok.status.signedOff");
						break;
					default:
						break;
					}
				}

				if (isNotSendToTm) {
					beanStatus += "、";
					beanStatus += Messages.getString("ExportDocxDialog.ok.status.NotSendToTm");
				}
				if (needsReview) {
					beanStatus += "、";
					beanStatus += Messages.getString("ExportDocxDialog.ok.status.Review");
				}

				bean.setStatus(beanStatus);

				getComments(vn, vu, bean);

				bean.setIndex("" + (tuDataList.size() + 1));
				tuDataList.add(bean);
			}
			monitor.worked(1);

			// 开始导出操作。规划出 subMonitor
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 18,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

			ExportDocx wordOutput = new ExportDocx(srcLang, tgtLang, docxPath, getShell(), exportComment, exportStatus);
			wordOutput.output(tuDataList, subMonitor);
			subMonitor.done();
		}

		/**
		 * 获取批注
		 * @param vn
		 * @param bean
		 */
		private void getComments(VTDNav vn, VTDUtils vu, TUBean bean) throws Exception {
			vn.push();
			List<CommentBean> commentList = new ArrayList<CommentBean>();
			otherAP.selectXPath("./note");
			String content = "";
			String user = "";
			String time = "";
			String text = "";
			int index = -1;
			while (otherAP.evalXPath() != -1) {
				// <note from='Mac'>2013-05-13:test</note>
				if ((index = vn.getAttrVal("from")) != -1) {
					user = vn.toString(index);
				}
				content = vu.getElementContent();
				if (content == null || content.length() <= 0) {
					continue;
				}
				if ((index = content.indexOf(":")) != -1) {
					time = content.substring(0, index);
					text = content.substring(index + 1, content.length());
				} else {
					text = content;
				}
				commentList.add(new CommentBean(user, time, text));
			}
			bean.setComment(commentList);
			vn.pop();
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
//	public int openConfirmDialog(final String message) {
//		final int[] bools = new int[1];
//		bools[0] = 0;
//		if (shell != null) {
//			Display.getDefault().syncExec(new Runnable() {
//				@Override
//				public void run() {
////					bools[0] = MessageDialog.openConfirm(shell, Messages.getString("all.dialog.confirm"), message);
//					MessageDialog md = new MessageDialog(shell, Messages.getString("all.dialog.confirm"), null, message, 0,
//							new String[] {"是", "全部是", "否", "全部否"}, 2);
//					bools[0] = md.open();
//				}
//			});
//		}
//		return bools[0];
//	}

	/**
	 * 精简文件名，should we??
	 * @param name
	 * @return ;
	 */
	public String strippedName(String name, String suffix) {
		int lastDotIndex = name.length();
		for (int i = name.length() - 1, j = 0; i > 0; i--) {
			if (name.charAt(i) == '.') {
				lastDotIndex = i;
				if (++j == 2) {
					break;
				}
			}
		}
		name = name.substring(0, lastDotIndex);
		return new StringBuffer().append(name).append(suffix).toString();
	}

	private boolean testXpath(AutoPilot _ap, String xpath) {
		try {
			_ap.selectXPath(xpath);
			return _ap.evalXPathToBoolean();
		} catch (XPathParseException e) {
			e.printStackTrace();
			return false;
		}
	}
}