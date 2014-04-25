package net.heartsome.cat.converter.pptx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import net.heartsome.cat.common.file.FileManager;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.pptx.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * XLIFF 转换为 PPTX
 * @author peason
 * @version
 * @since JDK1.6
 */
public class XLIFF2PPTX implements Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(XLIFF2PPTX.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "pptx";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("pptx.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to MS Office PowerPoint 2007 Conveter";

	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		XLIFF2PPTXImpl impl = new XLIFF2PPTXImpl();
		return impl.run(args, monitor);
	}

	public String getName() {
		return NAME_VALUE;
	}

	public String getType() {
		return TYPE_VALUE;
	}

	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	/**
	 * XLIFF 转换为 PPTX 的实现类
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	class XLIFF2PPTXImpl {

		/** SlideMaster 文件中 a 的前缀 */
		private static final String PREFIX_A = "a";

		/** SlideMaster 文件中 a 的命名空间 */
		private static final String NAMESPACE_A = "http://schemas.openxmlformats.org/drawingml/2006/main";

		/** SlideMaster 文件中 p 的前缀 */
		private static final String PREFIX_P = "p";

		/** SlideMaster 文件中 a 的命名空间 */
		private static final String NAMESPACE_P = "http://schemas.openxmlformats.org/presentationml/2006/main";

		/** XLIFF 文件路径 */
		private String strXLIFFPath;

		/** 骨架文件路径 */
		private String strSklPath;

		private ZipOutputStream zipOut;

		/** 骨架文件的临时解压目录 */
		private String strTmpFolderPath;

		private FileManager fileManager = new FileManager();

		/** 存放文本段的集合，key 为 trans-unit 的 id, value 的第一个值为 source, 第二个为 target */
		private HashMap<String, String[]> mapSegment = new HashMap<String, String[]>();

		/** 是否为预览翻译模式 */
		private boolean blnIsPreviewMode;

		/**
		 * 转换器的处理流程如下： <br/>
		 * 1. 解析 XLIFF 文件，获取所有文本段并存放入集合中。 <br/>
		 * 2. 将骨架文件解压到临时目录。<br/>
		 * 3. 将临时目录中除 slides 和 notesSlides 文件夹之外的其他文件放入目标文件。<br/>
		 * 4. 解析 slides 目录下以 .skl 结尾的文件，并替换文件中的骨架信息，替换完成后去除 .skl 后缀放入目标文件, <br/>
		 * slides 目录下的其他文件直接放入目标文件。<br/>
		 * 5. 解析 notesSlides 目录下以 .skl 结尾的文件，并替换文件中的骨架信息，替换完成后去除 .skl 后缀放入目标文件, <br/>
		 * slides 目录下的其他文件直接放入目标文件。<br/>
		 * 6. 删除临时解压目录。<br/>
		 * @param args
		 * @param monitor
		 * @return
		 * @throws ConverterException
		 *             ;
		 */
		public Map<String, String> run(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			monitor.beginTask("", 12);
			// 转换过程分为 11 部分，loadSegment 占 1，releaseSklZip 占 1，createZip 占 1， handleSklFile(slides) 占 5，
			// handleSklFile(notesSlides) 占 3，deleteFileOrFolder 占 1
			IProgressMonitor firstMonitor = Progress.getSubMonitor(monitor, 1);
			firstMonitor.beginTask(Messages.getString("pptx.XLIFF2PPTX.task1"), 1);
			firstMonitor.subTask("");
			Map<String, String> result = new HashMap<String, String>();
			strXLIFFPath = args.get(Converter.ATTR_XLIFF_FILE);
			String strTgtPath = args.get(Converter.ATTR_TARGET_FILE);
			strSklPath = args.get(Converter.ATTR_SKELETON_FILE);
			String strIsPreviewMode = args.get(Converter.ATTR_IS_PREVIEW_MODE);
			blnIsPreviewMode = strIsPreviewMode != null && strIsPreviewMode.equals(Converter.TRUE);
			try {
				zipOut = new ZipOutputStream(new FileOutputStream(strTgtPath));
				if (firstMonitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}
				loadSegment();
				firstMonitor.worked(1);
				firstMonitor.done();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}
				IProgressMonitor secondMonitor = Progress.getSubMonitor(monitor, 1);
				secondMonitor.beginTask(Messages.getString("pptx.XLIFF2PPTX.task2"), 1);
				secondMonitor.subTask("");
				releaseSklZip(strSklPath);
				secondMonitor.worked(1);
				secondMonitor.done();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}
				IProgressMonitor thirdMonitor = Progress.getSubMonitor(monitor, 1);
				thirdMonitor.beginTask(Messages.getString("pptx.XLIFF2PPTX.task3"), 1);
				thirdMonitor.subTask("");
				List<String> lstExcludeFolder = new ArrayList<String>();
				lstExcludeFolder.add(strTmpFolderPath + File.separator + "ppt" + File.separator + "slides");
				lstExcludeFolder.add(strTmpFolderPath + File.separator + "ppt" + File.separator + "notesSlides");
				fileManager.createZip(strTmpFolderPath, zipOut, lstExcludeFolder);
				thirdMonitor.worked(1);
				thirdMonitor.done();
				handleSklFile(strTmpFolderPath + File.separator + "ppt" + File.separator + "slides",
						Progress.getSubMonitor(monitor, 5));
				handleSklFile(strTmpFolderPath + File.separator + "ppt" + File.separator + "notesSlides",
						Progress.getSubMonitor(monitor, 3));
				zipOut.flush();
				zipOut.close();
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}
				IProgressMonitor fourthMonitor = Progress.getSubMonitor(monitor, 1);
				fourthMonitor.beginTask(Messages.getString("pptx.XLIFF2PPTX.task4"), 1);
				fourthMonitor.subTask("");
				fileManager.deleteFileOrFolder(new File(strTmpFolderPath));
				fourthMonitor.worked(1);
				fourthMonitor.done();
				result.put(Converter.ATTR_TARGET_FILE, strTgtPath);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(Messages.getString("pptx.XLIFF2PPTX.logger1"), e);
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("pptx.XLIFF2PPTX.msg1"),
						e);
			} finally {
				monitor.done();
			}
			return result;
		}

		/**
		 * 解析 XLIFF 文件，获取所有文本段集合
		 * @throws NavException
		 * @throws XPathParseException
		 * @throws XPathEvalException
		 */
		private void loadSegment() throws NavException, XPathParseException, XPathEvalException {
			VTDGen vg = new VTDGen();
			if (vg.parseFile(strXLIFFPath, true)) {
				VTDNav vn = vg.getNav();
				VTDUtils vu = new VTDUtils(vn);
				AutoPilot ap = new AutoPilot(vn);
				ap.selectXPath("/xliff/file/body//trans-unit");
				while (ap.evalXPath() != -1) {
					String strTuId = vu.getCurrentElementAttribut("id", null);
					String strSource = vu.getElementContent("./source");
					String strTarget = vu.getElementContent("./target");
					mapSegment.put(strTuId, new String[] { strSource, strTarget });
				}
			}
		}

		/**
		 * 解压骨架文件到临时目录
		 * @param sklPath
		 *            骨架文件路径
		 * @throws IOException
		 */
		private void releaseSklZip(String sklPath) throws IOException {
			String sysTemp = System.getProperty("java.io.tmpdir");
			String dirName = "tmpPPTXfolder" + System.currentTimeMillis();
			File dir = new File(sysTemp + File.separator + dirName);
			dir.mkdirs();
			strTmpFolderPath = dir.getAbsolutePath();
			fileManager.releaseZipToFile(sklPath, strTmpFolderPath);
		}

		/**
		 * 处理 filePath 指定目录下的骨架文件，非骨架文件直接放入压缩包。
		 * @param filePath
		 * @throws Exception
		 *             ;
		 */
		private void handleSklFile(String filePath, IProgressMonitor monitor) throws Exception {
			File file = new File(filePath);
			if (!file.exists()) {
				monitor.done();
				return;
			}
			List<File> lstSlideFile = fileManager.getSubFiles(file, null);
			monitor.beginTask("", lstSlideFile.size());
			VTDGen vg = new VTDGen();
			VTDUtils vu = new VTDUtils();
			XMLModifier xm = new XMLModifier();
			AutoPilot ap = new AutoPilot();
			ap.declareXPathNameSpace(PREFIX_A, NAMESPACE_A);
			ap.declareXPathNameSpace(PREFIX_P, NAMESPACE_P);
			String strSklTag = "%%%";
			Pattern pattern = Pattern.compile("%%%\\d+%%%");
			List<String> lstId = new ArrayList<String>();
			StringBuffer sbText = new StringBuffer();
			for (File slideFile : lstSlideFile) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}
				monitor.subTask(MessageFormat.format(Messages.getString("pptx.XLIFF2PPTX.task5"), slideFile.getName()));
				if (slideFile.isDirectory()) {
					for (File tmpFile : slideFile.listFiles()) {
						fileManager.addFileToZip(zipOut, strTmpFolderPath, tmpFile.getAbsolutePath());
					}
				} else if (!slideFile.getName().endsWith(".skl")) {
					fileManager.addFileToZip(zipOut, strTmpFolderPath, slideFile.getAbsolutePath());
				} else if (slideFile.getName().endsWith(".skl")) {
					// 解析骨架文件
					String strSlidePath = slideFile.getAbsolutePath();
					vg = new VTDGen();
					if (vg.parseFile(strSlidePath, true)) {
						VTDNav vn = vg.getNav();
						vu.bind(vn);
						ap.bind(vn);
						xm.bind(vn);
						ap.selectXPath("//p:cSld/p:spTree//a:t");
						while (ap.evalXPath() != -1) {
							String content = vu.getValue("./text()");
							if (content == null) {
								continue;
							}
							Matcher matcher = pattern.matcher(content);
							lstId.clear();
							while (matcher.find()) {
								String group = matcher.group();
								lstId.add(group.substring(group.indexOf(strSklTag) + strSklTag.length(),
										group.lastIndexOf(strSklTag)));
							}
							if (lstId.size() == 1) {
								String[] arrSrcAndTgt = mapSegment.get(lstId.get(0));
								if (arrSrcAndTgt != null) {
									String strSrc = arrSrcAndTgt[0];
									String strTgt = arrSrcAndTgt[1];
									if (strTgt != null) {
										strTgt = cleanTag(strTgt);
										if (blnIsPreviewMode || !"".equals(strTgt.trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											xm = vu.update(null, xm, "./text()", strTgt);
										} else {
											xm = vu.update(null, xm, "./text()", cleanTag(strSrc));
										}
									} else {
										xm = vu.update(null, xm, "./text()", cleanTag(strSrc));
									}
								} else {
									ConverterUtils.throwConverterException(
											Activator.PLUGIN_ID,
											MessageFormat.format(Messages.getString("pptx.XLIFF2PPTX.msg2"),
													lstId.get(0)));
								}
							} else {
								sbText.delete(0, sbText.length());
								for (String id : lstId) {
									String[] arrSrcAndTgt = mapSegment.get(id);
									if (arrSrcAndTgt != null) {
										String strSrc = arrSrcAndTgt[0];
										String strTgt = arrSrcAndTgt[1];
										if (strTgt != null) {
											strTgt = cleanTag(strTgt);
											if (blnIsPreviewMode || !"".equals(strTgt.trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												sbText.append(strTgt);
											} else {
												sbText.append(cleanTag(strSrc));
											}
										} else {
											sbText.append(cleanTag(strSrc));
										}
									} else {
										ConverterUtils.throwConverterException(
												Activator.PLUGIN_ID,
												MessageFormat.format(Messages.getString("pptx.XLIFF2PPTX.msg2"),
														lstId.get(0)));
									}
								}
								xm = vu.update(null, xm, "./text()", sbText.toString());
							}
						}

						strSlidePath = strSlidePath.substring(0, strSlidePath.lastIndexOf(".skl"));
						xm.output(strSlidePath);
						fileManager.addFileToZip(zipOut, strTmpFolderPath, strSlidePath);
					}
				}
				monitor.worked(1);
			}
			monitor.done();
		}

		/** 匹配 ph 标记的正则表达式 */
		private Pattern pattern = Pattern.compile("<ph\\s*(\\w*=('|\")[^</>'\"]+('|\"))*>[^<>]*</ph>");

		/**
		 * 清除 string 中的标记
		 * @param string
		 *            待处理的字符串
		 * @return ;
		 */
		private String cleanTag(String string) {
			Matcher matcher = pattern.matcher(string);
			int start = 0;
			int end = 0;
			StringBuffer sbText = new StringBuffer();
			while (matcher.find()) {
				String group = matcher.group();
				// 去除标记。如 <ph id="1">abcd</ph> 去除标记后为 abcd
				group = group.substring(group.indexOf(">") + 1, group.lastIndexOf("<"));
				group = TextUtil.resetSpecialString(group);
				start = matcher.start();
				sbText.append(string.substring(end, start)).append(group);
				end = matcher.end();
			}
			sbText.append(string.substring(end, string.length()));
			return sbText.toString();
		}
	}
}
