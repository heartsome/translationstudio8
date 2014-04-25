package net.heartsome.cat.converter.idml;

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
import net.heartsome.cat.converter.idml.resource.Messages;
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
 * XLIFF 转换为 IDML
 * @author peason
 * @version
 * @since JDK1.6
 */
public class XLIFF2IDML implements Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IDML2XLIFF.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "idml";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("idml.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to IDML Conveter";

	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		XLIFF2IDMLImpl impl = new XLIFF2IDMLImpl();
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
	 * XLIFF 转换为 IDML 的实现类
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	class XLIFF2IDMLImpl {

		/** IDML 中 Story 文件的前缀 */
		private static final String IDML_PREFIX = "idPkg";

		/** IDML 中 Story 文件的命名空间 */
		private static final String IDML_NAMESPACE = "http://ns.adobe.com/AdobeInDesign/idml/1.0/packaging";

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

		/**
		 * 转换器的处理顺序如下：<br/>
		 * 1. 解析 XLIFF 文件，获取所有文本段并存放入集合中。<br/>
		 * 2. 将骨架文件解压到临时目录。<br/>
		 * 3. 将临时目录中除 Stories 文件夹之外的其他文件及 Stories 文件夹中以 .xml 结尾的文件放入目标文件。<br/>
		 * 4. 解析 Stories 文件夹中以 .skl 结尾的文件，并替换文件中的骨架信息，替换完成之后将此文件去除 .skl 后缀放入目标文件。<br/>
		 * 5. 删除临时解压目录。<br/>
		 * @param args
		 * @param monitor
		 * @return
		 * @throws ConverterException
		 */
		public Map<String, String> run(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			monitor.beginTask("", 5);
			// 转换过程分为 11 部分，loadSegment 占 1，releaseSklZip 占 1，createZip 占 1， handleStoryFile 占 7，deleteFileOrFolder 占 1
			IProgressMonitor firstMonitor = Progress.getSubMonitor(monitor, 1);
			firstMonitor.beginTask(Messages.getString("idml.XLIFF2IDML.task2"), 1);
			firstMonitor.subTask("");
			Map<String, String> result = new HashMap<String, String>();
			strXLIFFPath = args.get(Converter.ATTR_XLIFF_FILE);
			String strTgtPath = args.get(Converter.ATTR_TARGET_FILE);
			strSklPath = args.get(Converter.ATTR_SKELETON_FILE);
			try {
				zipOut = new ZipOutputStream(new FileOutputStream(strTgtPath));
				if (firstMonitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				loadSegment();
				firstMonitor.worked(1);
				firstMonitor.done();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				IProgressMonitor secondMonitor = Progress.getSubMonitor(monitor, 1);
				secondMonitor.beginTask(Messages.getString("idml.XLIFF2IDML.task3"), 1);
				secondMonitor.subTask("");
				// 将骨架文件解压到临时目录。
				releaseSklZip(strSklPath);
				secondMonitor.worked(1);
				secondMonitor.done();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				IProgressMonitor thirdMonitor = Progress.getSubMonitor(monitor, 1);
				thirdMonitor.beginTask(Messages.getString("idml.XLIFF2IDML.task4"), 1);
				thirdMonitor.subTask("");
				fileManager.createZip(strTmpFolderPath, zipOut, strTmpFolderPath + File.separator + "Stories");
				thirdMonitor.worked(1);
				thirdMonitor.done();

				handleStoryFile(Progress.getSubMonitor(monitor, 7));
				zipOut.flush();
				zipOut.close();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				IProgressMonitor fourthMonitor = Progress.getSubMonitor(monitor, 1);
				fourthMonitor.beginTask(Messages.getString("idml.XLIFF2IDML.task5"), 1);
				fourthMonitor.subTask("");
				// 删除解压目录
				fileManager.deleteFileOrFolder(new File(strTmpFolderPath));
				fourthMonitor.worked(1);
				fourthMonitor.done();
				result.put(Converter.ATTR_TARGET_FILE, strTgtPath);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(Messages.getString("idml.XLIFF2IDML.logger1"), e);
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("idml.XLIFF2IDML.msg1"),
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
				AutoPilot gAp = new AutoPilot(vn);
				while (ap.evalXPath() != -1) {
					// 没有g标签
					if(vn.getAttrVal("gid")!=-1){
						String gId = vu.getCurrentElementAttribut("gid", null);
						vn.push();
						gAp.selectXPath("./source");
						while (gAp.evalXPath() != -1) {							
							if (gId != null) {
								String[] arrText = new String[2];
								arrText[0] = vu.getElementContent();
								mapSegment.put(gId, arrText);
							}
						}
						vn.pop();

						vn.push();
						gAp.selectXPath("./target");
						while (gAp.evalXPath() != -1) {
							if (gId != null) {
								if (mapSegment.containsKey(gId)) {
									String[] arrText = mapSegment.get(gId);
									arrText[1] = vu.getElementContent();
								} else {
									String[] arrText = new String[2];
									arrText[1] = vu.getElementContent();
									mapSegment.put(gId, arrText);
								}
							}
						}
						vn.pop();
						continue;
					}
					// 有<g>标签的情况
					vn.push();
					gAp.selectXPath("./source/g");
					while (gAp.evalXPath() != -1) {
						String gId = vu.getCurrentElementAttribut("id", null);
						if (gId != null) {
							String[] arrText = new String[2];
							arrText[0] = vu.getElementContent();
							mapSegment.put(gId, arrText);
						}
					}
					vn.pop();

					vn.push();
					gAp.selectXPath("./target/g");
					while (gAp.evalXPath() != -1) {
						String gId = vu.getCurrentElementAttribut("id", null);
						if (gId != null) {
							if (mapSegment.containsKey(gId)) {
								String[] arrText = mapSegment.get(gId);
								arrText[1] = vu.getElementContent();
							} else {
								String[] arrText = new String[2];
								arrText[1] = vu.getElementContent();
								mapSegment.put(gId, arrText);
							}
						}
					}
					vn.pop();
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
			String dirName = "tmpidmlfolder" + System.currentTimeMillis();
			File dir = new File(sysTemp + File.separator + dirName);
			dir.mkdirs();
			strTmpFolderPath = dir.getAbsolutePath();
			fileManager.releaseZipToFile(sklPath, strTmpFolderPath);
		}

		/**
		 * 处理临时解压目录中的 Story 文件
		 * @throws Exception
		 */
		private void handleStoryFile(IProgressMonitor monitor) throws Exception {
			List<File> lstStoryFile = fileManager.getSubFiles(new File(strTmpFolderPath + File.separator + "Stories"),
					null);
			monitor.beginTask("", lstStoryFile.size() + 10);
			VTDGen vg = new VTDGen();
			VTDUtils vu = new VTDUtils();
			XMLModifier xm = new XMLModifier();
			AutoPilot ap = new AutoPilot();
			ap.declareXPathNameSpace(IDML_PREFIX, IDML_NAMESPACE);
			String strSklTag = "###";
			List<String> lstX = new ArrayList<String>();
			List<String> lstStoryPath = new ArrayList<String>();
			List<String> lstId = new ArrayList<String>();
			Pattern pattern = Pattern.compile("###\\d+###");
			ParagraphManagement paraManagement = new ParagraphManagement();
			StringBuffer sbText = new StringBuffer();
			for (File storyFile : lstStoryFile) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}

				monitor.subTask(MessageFormat.format(Messages.getString("idml.XLIFF2IDML.task6"), storyFile.getName()));
				if (storyFile.getAbsolutePath().endsWith(".xml")) {
					lstStoryPath.add(storyFile.getAbsolutePath());
					// fileManager.addFileToZip(zipOut, strTmpFolderPath, storyFile.getAbsolutePath());
				} else if (storyFile.getAbsolutePath().endsWith(".x")) {
					lstX.add(storyFile.getAbsolutePath());
				} else if (storyFile.getAbsolutePath().endsWith(".skl")) {
					String strStoryPath = storyFile.getAbsolutePath();
					if (vg.parseFile(strStoryPath, true)) {
						VTDNav vn = vg.getNav();
						vu.bind(vn);
						ap.bind(vn);
						xm.bind(vn);
						ap.selectXPath("/idPkg:Story/Story//Content/text()");
						while (ap.evalXPath() != -1) {
							String content = vn.toString(vn.getCurrentIndex());
							if (content == null || content.equals("")) {
								continue;
							}
							String string = paraManagement.toHexString(content.replaceAll("\r\n", "").replaceAll("\n",
									""));
							if (string.toUpperCase().replaceAll("FFFE", "").replaceAll("FEFF", "")
									.replaceAll("2028", "").trim().equals("")) {
								continue;
							}
							lstId.clear();
							Matcher matcher = pattern.matcher(content);
							while (matcher.find()) {
								String group = matcher.group();
								lstId.add(group.substring(group.indexOf(strSklTag) + strSklTag.length(),
										group.lastIndexOf(strSklTag)));
							}
							if (lstId.size() == 0) {
								continue;
							}
							if (lstId.size() == 1) {
								String id = lstId.get(0);
								String[] arrSrcAndTgt = mapSegment.get(id);
								if (arrSrcAndTgt != null) {
									String strSrc = arrSrcAndTgt[0];
									String strTgt = arrSrcAndTgt[1];
									if (strTgt != null) {
										strTgt = cleanTag(strTgt);
										if (!"".equals(strTgt)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											xm.updateToken(vn.getCurrentIndex(), strTgt);
										} else {
											xm.updateToken(vn.getCurrentIndex(), cleanTag(strSrc));
										}
									} else {
										xm.updateToken(vn.getCurrentIndex(), cleanTag(strSrc));
									}
								} else {
									ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
											MessageFormat.format(Messages.getString("idml.XLIFF2IDML.msg2"), id));
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
											if (!"".equals(strTgt)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
								xm.updateToken(vn.getCurrentIndex(), sbText.toString());
							}
						}
						strStoryPath = strStoryPath.substring(0, strStoryPath.lastIndexOf(".skl"));
						xm.output(strStoryPath);
						lstStoryPath.add(strStoryPath);
					}
				}
				monitor.worked(1);
			}

			if (lstX.size() > 0) {
				IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, lstX.size());
				subMonitor.beginTask("", lstX.size());
				AutoPilot apX = new AutoPilot();
				VTDGen vgX = new VTDGen();
				boolean isUpdate = false;
				for (String strX : lstX) {
					vgX.clear();
					subMonitor.subTask(MessageFormat.format(Messages.getString("idml.XLIFF2IDML.task6"),
							strX.substring(strX.lastIndexOf(File.separator) + File.separator.length())));
					if (vgX.parseFile(strX, true)) {
						isUpdate = false;
						VTDNav vn = vgX.getNav();
						vu.bind(vn);
						apX.bind(vn);
						apX.selectXPath("/root/x");

						vg.clear();
						vg.parseFile(strX + "ml", true);
						VTDNav vnXML = vg.getNav();
						ap.bind(vnXML);
						xm.bind(vnXML);

						while (apX.evalXPath() != -1) {
							String id = vu.getCurrentElementAttribut("id", null);
							String content = vu.getElementContent();

							// id 非空时，解析对应的 xml 文件
							if (id != null) {
								ap.selectXPath("/idPkg:Story/Story//ParagraphStyleRange/CharacterStyleRange/x[@id='"
										+ id + "']");
								if (ap.evalXPath() != -1) {
									xm.remove();
									xm.insertAfterElement(content);
									isUpdate = true;
								}
							}
						}
						if (isUpdate) {
							xm.output(strX + "ml");
						}
					}
					subMonitor.worked(1);
				}
				subMonitor.done();
			}

			monitor.worked(1);
			for (String storyPath : lstStoryPath) {
				fileManager.addFileToZip(zipOut, strTmpFolderPath, storyPath);
			}

			monitor.done();
		}

		/** 匹配 ph 标记的正则表达式 */
		private Pattern pattern = Pattern.compile("<ph>[^<>]*</ph>");

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
				// group = group.substring(group.indexOf(">") + 1, group.lastIndexOf("<"));
				group.replaceAll("<ph>", "");
				group.replaceAll("</ph>", "");
				group = TextUtil.resetSpecialString(group);

				start = matcher.start();
				sbText.append(string.substring(end, start)).append(group);
				end = matcher.end();
			}
			sbText.append(string.substring(end, string.length()));
			String text = sbText.toString();
			return text.replaceAll("'", "&apos;");
		}
	}
}
