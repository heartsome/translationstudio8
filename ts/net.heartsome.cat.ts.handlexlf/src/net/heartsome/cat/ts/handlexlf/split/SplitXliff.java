package net.heartsome.cat.ts.handlexlf.split;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 切割xliff
 * @author robert 2011-10-18
 */
public class SplitXliff {

	private static final Logger LOGGER = LoggerFactory.getLogger(SplitXliff.class);
	private final String NATABLE_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";

	/** 要切割的Xliff文件 */
	private IFile splitFile;

	/** 切割Xliff所需要的切割点, 保存的是序列号 */
	private List<Integer> splitPointsIndex;
	private List<String> splitPointsRowId;

	private Shell shell;

	/** XLIFF 文件处理 */
	private XLFHandler xlfhandler;
	/** 专门用来解析处理分割后生成新文件的处理类实例 */
	private XLFHandler splitHandler;
	private SplitOrMergeXlfModel model;

	private static int CONSTANT_ONE = 1;
	private Map<String, String> oldInfo;
	/** <trans-unit>节点数据 */
	private int transUnitNum;
	private boolean needCover = false; // 当文件重复时定义的是否应覆盖
	/** file节点的信息，第一个值为file节点的序列，从1开始，第二个值为该file节点下trans-unit的个数 */
	private Map<Integer, Integer> fileInfo;
	private String separator = "";
	/** 总字数 */
	private int sumTotal;
	/** 总等效数 */
	private int sumEquivalent;
	
	private List<IFile> repeateFileList = new ArrayList<IFile>();

	public SplitXliff(SplitOrMergeXlfModel model) {
		this.shell = model.getShell();
		this.splitFile = model.getSplitFile();
		this.splitPointsIndex = model.getSplitXlfPointsIndex();
		this.xlfhandler = model.getXliffEditor().getXLFHandler();
		this.splitPointsRowId = model.getSplitXlfPointsRowId();
		this.model = model;
		splitHandler = new XLFHandler();

		if ("\\".equals(System.getProperty("file.separator"))) {
			separator = "\\";
		} else {
			separator = "/";
		}
	}

	/**
	 * 切割Xliff文件 注意：源文本没有解析，因为xlfhandler是从xlfeditor取过来的，因此不用再解析一次。
	 */
	public boolean splitTheXliff(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.getString("splitAndMergeXliff.SplitXliff.task1"), splitPointsIndex.size());
		String xlfPath = splitFile.getLocation().toOSString();

		if (xlfPath != null) {
			File f = new File(xlfPath);
			// 如果文件不存在，提示并退出操作
			if (!f.exists()) {
				f = null;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(shell, Messages
								.getString("splitAndMergeXliff.SplitXliff.msgTitle1"), MessageFormat.format(Messages
								.getString("splitAndMergeXliff.SplitXliff.msg1"), new Object[] { splitFile
								.getFullPath().toOSString() }));
					}
				});
				return false;
			} else {
				
				Map<String, String> newInfo = null;

				File src = new File(xlfPath);
				String fileName = null;
				String splitXliffName = null;
				try {
					splitXliffName = src.getName();
					fileName = new String(splitXliffName.getBytes("UTF-8"));// 源文件的文件名

				} catch (UnsupportedEncodingException e1) {
					LOGGER.error("", e1);
					e1.printStackTrace();
				}

				// 判断分割后生成的子文件是否重复，如果重复就进行提示
				String copyFiles = "";
				LinkedList<String> newSplitedFilesName = model.getNewSplitedFilesName();
				for (int i = 0; i < newSplitedFilesName.size(); i++) {
					final String newXlfPath = model.getSplitXlfsContainer().getLocation()
							.append(newSplitedFilesName.get(i)).toOSString();
					File newXlfFile = new File(newXlfPath);
					if (newXlfFile.exists()) {
						copyFiles += getFullPath(newXlfPath) + "\n";
						repeateFileList.add(ResourceUtils.fileToIFile(newXlfPath));
					}
				}
				final String copyFilesTip = copyFiles.substring(0, copyFiles.length());
				// 如果新生成的文件已经存在，那么提示是否覆盖
				if (copyFiles.length() > 0) {
					try {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								boolean response = MessageDialog.openConfirm(shell, Messages
										.getString("splitAndMergeXliff.SplitXliff.msgTitle2"), MessageFormat.format(
										Messages.getString("splitAndMergeXliff.SplitXliff.msg2"),
										new Object[] { copyFilesTip }));
								if (!response) {
									needCover = true;
								}
							}
						});
					} catch (Exception e) {
						LOGGER.error("", e);
						e.printStackTrace();
					}

					if (needCover) {
						return false;
					}
				}
				
				// 先删除重复的文件，再关闭已经打开的重复文件的编辑器
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						try {
							IEditorReference[] refrences = window.getActivePage().getEditorReferences();
							for(IEditorReference refrence : refrences){
								if (refrence.getEditor(true).getEditorSite().getId().equals(NATABLE_ID)) {
									XLIFFEditorImplWithNatTable nattable = (XLIFFEditorImplWithNatTable)refrence.getEditor(true);
									if (nattable.isMultiFile()) {
										for (File file : nattable.getMultiFileList()) {
											if (repeateFileList.indexOf(ResourceUtils.fileToIFile(file.getAbsolutePath())) >= 0) {
												window.getActivePage().closeEditor(nattable, true);
												break;
											}
										}
									}else {
										if (repeateFileList.indexOf(((FileEditorInput)nattable.getEditorInput()).getFile()) >= 0) {
											window.getActivePage().closeEditor(nattable, true);
										}
									}
								}
							}
							for (IFile iFile : repeateFileList) {
								ResourceUtil.getFile(iFile).delete(true, null);
							}
							model.getSplitFile().getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
						} catch (Exception e) {
							LOGGER.error("", e);
						}
					}
				});
				
				// 得到当前的时间， 开始进行循环切割
				long splitTime = System.currentTimeMillis();

				for (int i = CONSTANT_ONE, pointsSize = (splitPointsIndex.size() + 1); i <= pointsSize; i++) {
					newInfo = getNewSplitInfo(fileName, oldInfo, i, pointsSize, splitTime);
					monitor.subTask(Messages.getString("splitAndMergeXliff.SplitXliff.task2") + newInfo.get("name"));

					// 创建新的XLIFF的文件路径
					final String newXlfPath = model.getSplitXlfsContainer().getLocation()
							.append(getSplitFileName(splitXliffName, oldInfo, i)).toOSString();

					String xliffNodeHeader = xlfhandler.getNodeHeader(xlfPath, "xliff", "/xliff");
					createNewSplitXliff(newXlfPath, xliffNodeHeader);

					// 打开这个新创建的xliff文件，将xliff与file,header等节点写入进去
					Map<String, Object> newResultMap = splitHandler.openFile(newXlfPath);
					if (newResultMap == null
							|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
									.get(Constant.RETURNVALUE_RESULT)) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(shell,
										Messages.getString("splitAndMergeXliff.SplitXliff.msgTitle3"),
										Messages.getString("splitAndMergeXliff.SplitXliff.msg3"));
							}
						});
						return false;
					}

					if (fileInfo == null) {
						fileInfo = xlfhandler.getFileInfo(xlfPath);
					}

					// 获取当前分割段的首末rowId
					String startRowId = xlfhandler.getNextRowId(xlfPath,
							i == CONSTANT_ONE ? "start" : splitPointsRowId.get(i - 2));
					String endRowId = (i == pointsSize) ? xlfhandler.getNextRowId(xlfPath, "end") : splitPointsRowId
							.get(i - CONSTANT_ONE); // 因为这里的I是从1开始的，故要减1

					// 获取分割段的file，与body第一子节点的序列
					Map<String, Integer> startNodeIdxMap = xlfhandler.getSplitNodeIdx(xlfPath,
							i == CONSTANT_ONE ? "start" : splitPointsRowId.get(i - 2));
					Map<String, Integer> endNodeIdxMap = (i == pointsSize) ? xlfhandler.getSplitNodeIdx(xlfPath, "end")
							: xlfhandler.getSplitNodeIdx(xlfPath, splitPointsRowId.get(i - CONSTANT_ONE));
					// 获取当前起始rowId所在的file节点的序列号
					int startFileNodeIdx = startNodeIdxMap.get("fileNodeIdx");
					int endFileNodeIdx = endNodeIdxMap.get("fileNodeIdx");
					int startBodyChildIdx = startNodeIdxMap.get("bodyChildNodeIdx");

					// 开始循环每一个file节点，进行获取相关数据
					int n = 1; // 这是新生成的xliff文件中的file的序列号
					for (int fileIdx = startFileNodeIdx; fileIdx <= endFileNodeIdx; fileIdx++) {
						// 开始将数据存入新切割的xliff文件中,先存放file节点的头
						String fileNodeHeader = xlfhandler.getNodeHeader(xlfPath, "file", "/xliff/file[" + fileIdx
								+ "]");
						splitHandler.addDataToXlf(newXlfPath, fileNodeHeader, "/xliff");

						String headerFrag = xlfhandler.getNodeFrag(xlfPath, "/xliff/file[" + fileIdx + "]/header");
						splitHandler.addDataToXlf(newXlfPath, headerFrag, "/xliff/file[" + n + "]");

						// 向新生成xliff文件添加body元素
						String bodyNodeHeader = xlfhandler.getNodeHeader(xlfPath, "body", "/xliff/file[" + fileIdx
								+ "]/body");
						splitHandler.addDataToXlf(newXlfPath, bodyNodeHeader, "/xliff/file[" + n + "]");
						boolean isLastOfFile = false;
						if (i == pointsSize && fileIdx == endFileNodeIdx) {
							isLastOfFile = true;
						}
						// UNDO 分割这里还要好发测试一下，针对不同情况。
						String tuData = xlfhandler.getSplitTuData(xlfPath, fileIdx, n == 1 ? startBodyChildIdx : 1,
								n == 1 ? startRowId : null, endRowId, n == 1, isLastOfFile);
						splitHandler.addDataToXlf(newXlfPath, tuData, "/xliff/file[" + n + "]/body");
						n++;
					}

					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("splitAndMergeXliff.SplitXliff.msg3"));
					}
					monitor.worked(1);
					// 添加新的切割信息
					splitHandler.addNewInfoToSplitXlf(newXlfPath, newInfo);
				}
				monitor.done();
			}
		}
		return true;

	}

	/**
	 * 验证文件是否为xliff文件
	 * @return
	 */
	public boolean validXLiff() {
		String xlfPath = splitFile.getLocation().toOSString();
		// 若其file元素个数小于1，或者其根元素不为"xliff"，那么就这个不是XLIFF文档
		if (!xlfhandler.validateSplitXlf(xlfPath) || xlfhandler.getFileCountInXliff(xlfPath) < 1) {
			return false;
		}
		return true;
	}

	/**
	 * 获取分割文件名
	 * @param fileName
	 * @param oldInfo
	 * @param index
	 * @return ;
	 */
	public String getSplitFileName(String fileName, Map<String, String> oldInfo, int index) {
		String fileExtesion = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		if (oldInfo == null || oldInfo.size() <= 0) {
			return fileName.substring(0, fileName.lastIndexOf(fileExtesion)) + "_" + index + fileExtesion; // 生成这种形式:name="test.txt_1.xlf"
		} else {
			String name = oldInfo.get("name");
			return name.substring(0, name.lastIndexOf(fileExtesion)) + "_" + index + fileExtesion; // 生成这种形式:name="test.txt_2_1.xlf"
		}
	}

	/**
	 * 生成新的切割信息
	 * @param fileName
	 *            文件名
	 * @param oldInfo
	 *            该文件以前的分割信息
	 * @param index
	 *            序号
	 * @param pointsSize
	 *            分割的段数
	 * @param splitTime
	 *            此次分割的时间
	 * @return
	 */
	public Map<String, String> getNewSplitInfo(String fileName, Map<String, String> oldInfo, int index, int pointsSize,
			long splitTime) {
		Map<String, String> newInfoMap = new HashMap<String, String>();
		String fileExtension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		// 第一次分割
		if (oldInfo == null || oldInfo.size() <= 0) {
			String id = fileName.substring(fileName.lastIndexOf(separator) + 1) + "-" + index + "/" + pointsSize; // 生成这种形式:id="test.txt.xlf-1/2"
			String name = fileName.substring(0, fileName.lastIndexOf(fileExtension)) + "_" + index + fileExtension; // 生成这种形式:name="test.txt_1.xlf"
			newInfoMap.put("id", id);
			newInfoMap.put("name", name);
			newInfoMap.put("depth", "1");
		} else {
			String id = oldInfo.get("id");
			String name = oldInfo.get("name");
			String depth = oldInfo.get("depth");
			id += "~" + index + "/" + pointsSize; // 生成这种形式:id="test.txt.xlf-1/2~1/2"
			name = name.substring(0, name.lastIndexOf(fileExtension)) + "_" + index + fileExtension; // 生成这种形式:name="test.txt_2_1.xlf"
			newInfoMap.put("id", id);
			newInfoMap.put("name", name);
			newInfoMap.put("depth", "" + (Integer.parseInt(depth) + 1));
		}

		newInfoMap.put("index", "" + index);
		newInfoMap.put("count", "" + pointsSize);
		newInfoMap.put("splitTime", "" + splitTime);
		newInfoMap.put("original", fileName);

		return newInfoMap;
	}

	/**
	 * 创建新的切割后的文件, 并添加xliff节点
	 * @param xlfPath
	 *            切割文件的路径
	 * @param newSliptXlfPath
	 *            新的xliff文件的路径
	 */
	public void createNewSplitXliff(String newSliptXlfPath, String xliffNodeHeader) {
		FileOutputStream output;
		try {
			output = new FileOutputStream(newSliptXlfPath);
			output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
			output.write(xliffNodeHeader.getBytes("UTF-8"));
			output.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 针对每个切割段，获取该段内的file节点信息，
	 * @param startIndex
	 * @param endIndex
	 * @param fileInfo
	 * @return result{0,0} 第一个数据，是该切割段内的第一个file的序列号（是从1开始的）, 第二个数据是切割段内的的最后一个file的序列号(是从1开始)
	 */
	public int[] getFileSpliteIndex(int startIndex, int endIndex, Map<Integer, Integer> fileInfo) {
		int[] result = new int[] { 0, 0 };
		Iterator<Integer> it = fileInfo.keySet().iterator();
		int count = 0;
		while (it.hasNext()) {
			Integer key = it.next();
			Integer value = fileInfo.get(key);
			count += value;

			if (result[0] == 0 && startIndex <= count) {
				result[0] = key;
			}

			if (result[1] == 0 && endIndex <= count) {
				result[1] = key;
			}

			if (result[0] != 0 && result[1] != 0) {
				return result;
			}
		}
		return result;

	}

	/**
	 * 获取当前file节点之前的所有file节点的trans-unit节点之和
	 * @param fileIndex
	 *            当前节点的序列号（从1开始）
	 * @param infoMap
	 * @return
	 */
	public int getPurFileTUCount(int fileIndex, Map<Integer, Integer> fileInfo) {
		int purFileTUCount = 0;
		for (int i = 1; i < fileIndex; i++) {
			purFileTUCount += fileInfo.get(i);
		}

		return purFileTUCount;
	}

	/**
	 * 根据绝对路径获取其项目路径
	 * @param filePath
	 * @return
	 */
	public String getFullPath(String filePath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(filePath);
		IFile ifile = workspace.getRoot().getFileForLocation(location);
		return ifile.getFullPath().toOSString();

	}

	/**
	 * 获取切割后的文件名
	 * @param fileName
	 * @param oldInfo
	 * @param index
	 * @param pointsSize
	 * @return
	 */
	public String getNewSplitName(String fileName, Map<String, String> oldInfo, int index) {
		String newSplitName = "";
		String fileExtention = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		// 第一次分割
		if (oldInfo == null || oldInfo.size() <= 0) {
			newSplitName = fileName.substring(0, fileName.lastIndexOf(fileExtention)) + "_" + index + fileExtention; // 生成这种形式:name="test.txt_1.xlf"
		} else {
			String name = oldInfo.get("name");
			newSplitName = name.substring(0, name.lastIndexOf(fileExtention)) + "_" + index + fileExtention; // 生成这种形式:name="test.txt_2_1.xlf"
		}
		return newSplitName;

	}

	/**
	 * 获取当前分割点的起始段 splitPoints是从1开始的
	 * @param splitPointIndex
	 * @return
	 */
	public int getStartIndex(int splitPointIndex) {
		return (splitPointIndex == 1 ? 1 : splitPointsIndex.get(splitPointIndex - 2) + 1); // 由于splitPoints中是以0开始的，故在此需要加1，endIndex同理
	}

	/**
	 * 获取当前分割点的终止段
	 * @param splitPointIndex
	 * @return
	 */
	public int getEndIndex(int splitPointIndex) {
		// splitPointIndex的起始游标为0，现从1开始，最大游标等于splitPoints.size() + 1
		return (splitPointIndex == splitPointsIndex.size() + 1 ? transUnitNum : splitPointsIndex
				.get(splitPointIndex - 1));
	}

	// **************************下面是等效系统与总字数的相关代码**************************

	/**
	 * 获取文件列表中所展示的数据
	 */
	public String[][] getSplitTableInfos() {
		ArrayList<String[]> SplitTableInfos = new ArrayList<String[]>();
		String xlfPath = splitFile.getLocation().toOSString();
		oldInfo = xlfhandler.getOldSplitInfo(xlfPath);
		File src = new File(xlfPath);
		String fileName = src.getName(); // 源文件的文件名
		transUnitNum = xlfhandler.getAllTransUnitNum(xlfPath); // trans-unit节点总数

		for (int pointIndex = CONSTANT_ONE, pointsSize = (splitPointsIndex.size() + 1); pointIndex <= pointsSize; pointIndex++) {
			String newSplitXlfName = getNewSplitName(fileName, oldInfo, pointIndex);
			// 将这些分割后的子文件名添加到model中，以便后用
			model.getNewSplitedFilesName().add(newSplitXlfName);
			String paragraph = getStartIndex(pointIndex) + " -> " + getEndIndex(pointIndex);
			String[] tableInfo = new String[] { "" + pointIndex, newSplitXlfName, paragraph };
			SplitTableInfos.add(tableInfo);

			sumTotal += 0;
			sumEquivalent += 0;
		}

		return SplitTableInfos.toArray(new String[][] {});
	}

	public int getSumTotal() {
		return sumTotal;
	}

	public int getSumEquivalent() {
		return sumEquivalent;
	}

}
