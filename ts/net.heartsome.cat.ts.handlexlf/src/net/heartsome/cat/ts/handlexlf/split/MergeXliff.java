package net.heartsome.cat.ts.handlexlf.split;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.handlexlf.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 合并xliff文件
 * @author robert 2011-10-25
 */
public class MergeXliff {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MergeXliff.class);
	
	private Shell shell;
	/** XLIFF 文件处理 */
	private XLFHandler xlfhandler = new XLFHandler();
	private SplitOrMergeXlfModel model;
	private int runIndex = 1; // 全并程序运行次数
	/** 合并文件的存储路径 */
	private String targetFilePath = null;

	public MergeXliff(SplitOrMergeXlfModel model) {
		this.shell = model.getShell();
		this.model = model;
	}

	/**
	 * 合并前进行相关的验证
	 * @param srcFilesPath
	 *            要合并的xliff文件绝对路径
	 */
	public boolean merge(Vector<String> srcFilesPath, IProgressMonitor monitor) {
		if (runIndex == 1) { // 第一次运行
			monitor.beginTask(Messages.getString("splitAndMergeXliff.MergeXliff.task1"), 2);
		}
		if (!checkCanMerge(srcFilesPath)) {
			return false;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			// 验证XLF是否合法,提取每个文件中的合并信息。
			// key 为 Depth, key 为 splitTime , key 为 ID
			Map<String, Map<String, Map<String, Map<String, String>>>> splitInfo = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();
			int maxDepth = 1;
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			subMonitor.beginTask(Messages.getString("splitAndMergeXliff.MergeXliff.task2"), srcFilesPath.size() + 1);
			for (int i = 0, size = srcFilesPath.size(); i < size; i++) {
				final String srcXlfPath = srcFilesPath.get(i);
				// 开始解析文件
				Map<String, Object> resultMap = xlfhandler.openFile(srcXlfPath);
				if (resultMap == null
						|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap
								.get(Constant.RETURNVALUE_RESULT)) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(shell,
									Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle1"),
									Messages.getString("splitAndMergeXliff.MergeXliff.msg1"));
						}
					});
					return false;
				}

				if (targetFilePath == null || "".equals(targetFilePath)) {
					getTargetFilePath(srcFilesPath);
					if (targetFilePath == null) {
						return false;
					}
				}

				// 若其file元素个数小于1，或者其根元素不为"xliff"，那么就这个不是XLIFF文档
				if (!xlfhandler.validateSplitXlf(srcXlfPath) || xlfhandler.getFileCountInXliff(srcXlfPath) < 1) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(shell,
									Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle2"),
									Messages.getString("splitAndMergeXliff.MergeXliff.msg2"));
						}
					});
					return false;
				}
				// 得到最后一次的分割信息
				Map<String, String> oldInfo = null;
				// 这是针对切分已经切分的文件，获取其最后一次的切分信息
				oldInfo = xlfhandler.getOldSplitInfo(srcXlfPath);

				if (oldInfo == null || oldInfo.size() <= 0) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(shell, Messages
									.getString("splitAndMergeXliff.MergeXliff.msgTitle2"), MessageFormat.format(
									Messages.getString("splitAndMergeXliff.MergeXliff.msg3"),
									new Object[] { getFullPath(srcXlfPath) }));
						}
					});
					return false;
				}

				// 从各文件中提取出ID存入不重复的集合中
				String id = oldInfo.get("id");
				String index = oldInfo.get("index");
				String count = oldInfo.get("count");
				String splitTime = oldInfo.get("splitTime");
				String strDepth = oldInfo.get("depth");
				String name = oldInfo.get("name");

				// key 为 splitTime, key 为 id
				Map<String, Map<String, Map<String, String>>> groupByDepth = splitInfo.get(strDepth);
				if (groupByDepth == null) {
					groupByDepth = new HashMap<String, Map<String, Map<String, String>>>();
				}

				// key 为 id
				Map<String, Map<String, String>> groupBySplitTime = groupByDepth.get(splitTime);
				if (groupBySplitTime == null) {
					groupBySplitTime = new HashMap<String, Map<String, String>>();
				}

				Map<String, String> groupById = groupBySplitTime.get(id);
				if (groupById == null) {
					groupById = new HashMap<String, String>();
				}

				groupById.put("fileIndex", "" + i);
				groupById.put("splitTime", splitTime);
				groupById.put("id", id);
				groupById.put("count", count);
				groupById.put("index", index);
				groupById.put("depth", strDepth);
				groupById.put("name", name);

				groupBySplitTime.put(id, groupById);
				groupByDepth.put(splitTime, groupBySplitTime);
				splitInfo.put(strDepth, groupByDepth);

				int depth = Integer.parseInt(strDepth);
				if (depth > maxDepth) {
					maxDepth = depth;
				}
				subMonitor.worked(1);
			}

			// 对分组后的信息进行验证，通过验证的进行合并。
			Iterator<String> itByDepth = splitInfo.keySet().iterator();
			Vector<String> neededRemove = new Vector<String>();
			subMonitor.subTask(Messages.getString("splitAndMergeXliff.MergeXliff.task3"));
			while (itByDepth.hasNext()) {
				String curDepth = itByDepth.next();
				if (curDepth.equals("" + maxDepth)) {
					Map<String, Map<String, Map<String, String>>> groupBySplitTime = splitInfo.get(curDepth);

					// 对同一深度不同时间分割的文件分别进行验证
					Iterator<String> itBySplitTime = groupBySplitTime.keySet().iterator();
					while (itBySplitTime.hasNext()) {
						String curSplitTime = itBySplitTime.next();
						Map<String, Map<String, String>> groupById = groupBySplitTime.get(curSplitTime);

						Iterator<String> itById = groupById.keySet().iterator();
						Vector<String> mergeFilesPath = new Vector<String>();
						while (itById.hasNext()) {
							String curId = itById.next();
							Map<String, String> curInfo = groupById.get(curId);

							int count = Integer.parseInt(curInfo.get("count"));

							if (groupById.size() != count) {
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										MessageDialog.openError(shell,
												Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle1"),
												Messages.getString("splitAndMergeXliff.MergeXliff.msg4"));
									}
								});
								return false;
							}

							// 验证其他文件是否存，并取出其文件索引
							String prefixId = "";
							int pos1 = curId.lastIndexOf("~");
							if (pos1 != -1) {
								prefixId = curId.substring(0, pos1 + 1);
							} else {
								prefixId = curId.substring(0, curId.lastIndexOf("-") + 1);
							}

							for (int i = 1; i <= count; i++) {
								String id = prefixId + i + "/" + count;
								if (!groupById.containsKey(id)) {
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											MessageDialog.openError(shell,
													Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle1"),
													Messages.getString("splitAndMergeXliff.MergeXliff.msg5"));
										}
									});
									return false;
								} else {

									Map<String, String> fileInfo = groupById.get(id);

									int index = Integer.parseInt(fileInfo.get("index"));
									int fileIndex = Integer.parseInt(fileInfo.get("fileIndex"));

									// 如果存在，就添加进需合并的文件名集合。
									mergeFilesPath.add(index - 1, srcFilesPath.get(fileIndex));
								}
							}

							// 只需取出一个元素去判断其他元素是否存在即可
							break;
						}

						IProgressMonitor mergeSubMonitor = new SubProgressMonitor(monitor, 1,
								SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
						// 如果深度为1，则取用户选的文件名，否则建一个临时文件
						if (curDepth.equals("1")) {
							boolean returnMerge = mergeXLF(targetFilePath, mergeFilesPath, mergeSubMonitor);
							xlfhandler.deleteSplitInfoParent(targetFilePath);
							// 处理关于重复节点的情况，2012-05-13
							xlfhandler.operateMergedXliff(targetFilePath);
							return returnMerge;
						} else {
							// 这是多次分割的情况，创建一个临时文件
							File tmpXLF = xlfhandler.createTmpFile();
							neededRemove.addAll(mergeFilesPath);
							srcFilesPath.add(tmpXLF.getAbsolutePath());
							if (!mergeXLF(tmpXLF.getAbsolutePath(), mergeFilesPath, mergeSubMonitor)) {
								return false;
							}

						}
					}
				}
			}

			subMonitor.worked(1);
			subMonitor.done();

			int remain = srcFilesPath.size() - neededRemove.size();
			if (remain > 0) {
				String[] srcFileArray = new String[remain];
				for (int i = 0, j = 0, length = srcFileArray.length; i < length; i++) {
					for (int size = srcFilesPath.size(); j < size; j++) {
						if (!neededRemove.contains(srcFilesPath.get(j))) {
							srcFileArray[i] = srcFilesPath.get(j);
							j++;
							break;
						}
					}
				}
				Vector<String> srcFiles = new Vector<String>();
				for (int i = 0, size = srcFileArray.length; i < size; i++) {
					srcFiles.add(srcFileArray[i]);
				}
				// 如果调用这个方法后有错误并返回false,那么返回false;
				if (!merge(srcFiles, monitor)) {
					return false;
				}
				if (runIndex == 1) {
					monitor.done();
				}
				runIndex++;
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(shell, Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle1"),
								Messages.getString("splitAndMergeXliff.MergeXliff.msg5"));
					}
				});
				return false;
			}

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(shell, Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle1"),
							Messages.getString("splitAndMergeXliff.MergeXliff.msg6"));
				}
			});
		}
		return true;
	}

	/**
	 * 合并xliff文件
	 * @param thisTarFilePath
	 *            本次要合并而成的目标文件的绝对路径
	 * @param mergeFilesPath
	 *            要合并文件的绝对路径
	 * @throws Exception
	 */
	private boolean mergeXLF(String thisTarFilePath, Vector<String> srcFilesPath, IProgressMonitor monitor)
			throws Exception {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.getString("splitAndMergeXliff.MergeXliff.task4"), srcFilesPath.size());

		// 因为合并的文件都是由一个文件分割而成的，因此获取其中一个文件的xliff节点的头
		String xliffNodeHeader = xlfhandler.getNodeHeader(srcFilesPath.get(0), "xliff", "/xliff");
		createTargetXliff(thisTarFilePath, xliffNodeHeader);
		// 解析目标文件
		Map<String, Object> tarResultMap = xlfhandler.openFile(thisTarFilePath);
		if (tarResultMap == null
				|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) tarResultMap.get(Constant.RETURNVALUE_RESULT)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(shell, Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle1"),
							Messages.getString("splitAndMergeXliff.MergeXliff.msg7"));
				}
			});
			return false;
		}

		for (int i = 0, size = srcFilesPath.size(); i < size; i++) {
			/*
			 * Map<String, Object> curSrcResultMap = xlfhandler.openFile(srcFilesPath.get(i)); if (curSrcResultMap ==
			 * null || Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer)
			 * curSrcResultMap.get(Constant.RETURNVALUE_RESULT)) { MessageDialog.openError(shell, "Error", "文件合并失败!");
			 * return; }
			 */

			// 循环当前目标文件的所有file子节点
			int srcFileNodeCount = xlfhandler.getNodeCount(srcFilesPath.get(i), "/xliff/file");
			for (int srcFileNodeIndex = 1; srcFileNodeIndex <= srcFileNodeCount; srcFileNodeIndex++) {
				// 与目标文件中的file子节点的属性相比较，若相同，则将body节点的内容添加到目标文件中，若不相同，则将该源文件的整个file节点添加到目标文件中
				int tarFileNodeCount = xlfhandler.getNodeCount(thisTarFilePath, "/xliff/file");
				// 若目标文件中没有file节点，那么新建此节点
				if (tarFileNodeCount < 1) {
					String xliffNodeFrag = xlfhandler.getNodeFrag(srcFilesPath.get(i), "/xliff/file["
							+ srcFileNodeIndex + "]");
					xlfhandler.addDataToXlf(thisTarFilePath, xliffNodeFrag, "/xliff");
				} else {
					Hashtable<String, String> srcFileNodeAttrs = xlfhandler.getNodeAttributes(srcFilesPath.get(i),
							"/xliff/file[" + srcFileNodeIndex + "]");

					boolean hasTheSame = false;
					for (int tarFileNodeIndex = 1; tarFileNodeIndex <= tarFileNodeCount; tarFileNodeIndex++) {
						Hashtable<String, String> tarFileNodeAttrs = xlfhandler.getNodeAttributes(thisTarFilePath,
								"/xliff/file[" + tarFileNodeIndex + "]");

						// 比较两个file节点的所有属性,如果两个file节点的属性相等 ，则证明是同一个file节点，那么，将源文件的该file节点的body内容拷进目标文件中
						if (compareValue(srcFileNodeAttrs, tarFileNodeAttrs)) {
							String bodyNodeContent = xlfhandler.getNodeContent(srcFilesPath.get(i), "/xliff/file["
									+ srcFileNodeIndex + "]/body");
							xlfhandler.addDataToXlf(thisTarFilePath, bodyNodeContent, "/xliff/file[" + tarFileNodeIndex
									+ "]/body");
							hasTheSame = true;
						}
					}

					if (!hasTheSame) {
						// 如果不相等，则将源文件的file直接添加到目标文件中去
						String xliffNodeFrag = xlfhandler.getNodeFrag(srcFilesPath.get(i), "/xliff/file["
								+ srcFileNodeIndex + "]");
						xlfhandler.addDataToXlf(thisTarFilePath, xliffNodeFrag, "/xliff");
					}
				}
			}

			monitor.worked(1);

		}
		// 删除目标文件的header节点下的最后一条切割信息
		xlfhandler.deleteLastSplitInfo(thisTarFilePath);

		monitor.done();
		return true;
	}

	/**
	 * 验证所选文件能否合并
	 * @param srcFilesPath
	 * @return
	 */
	public boolean checkCanMerge(Vector<String> srcFilesPath) {
		try {
			Set<String> sourceName = new HashSet<String>();
			// key is id, value is split info.
			Map<String, Map<String, String>> info = new HashMap<String, Map<String, String>>();
			String originalName = "";
			for (int i = 0; i < srcFilesPath.size(); i++) {
				Map<String, Object> curSrcResultMap = xlfhandler.openFile(srcFilesPath.get(i));
				if (curSrcResultMap == null
						|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) curSrcResultMap
								.get(Constant.RETURNVALUE_RESULT)) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(shell,
									Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle1"),
									Messages.getString("splitAndMergeXliff.MergeXliff.msg7"));
						}
					});
					return false;
				}
				// 不同文件进行分割的文件不能进行合并
				if (originalName == null || "".equals(originalName)) {
					originalName = xlfhandler.getSplitOriginalName(srcFilesPath.get(i));
				}else {
					if (!originalName.equals(xlfhandler.getSplitOriginalName(srcFilesPath.get(i)))) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								MessageDialog.openInformation(shell, Messages
										.getString("splitAndMergeXliff.MergeXliff.msgTitle2"), 
										Messages.getString("splitAndMergeXliff.MergeXliff.addTip1"));
							}
						});
						return false;
					}
				}

				Map<String, String> oldInfo = xlfhandler.getOldSplitInfo(srcFilesPath.get(i));
				if (oldInfo == null || oldInfo.size() <= 0) {
					final String srcXlfPath = srcFilesPath.get(i);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(shell, Messages
									.getString("splitAndMergeXliff.MergeXliff.msgTitle2"), MessageFormat.format(
									Messages.getString("splitAndMergeXliff.MergeXliff.msg3"),
									new Object[] { srcXlfPath }));
						}
					});
					return false;
				}
				// 从各文件中提取出ID存入不重复的集合中
				String id = oldInfo.get("id"); //$NON-NLS-1$
				String sourceFileName = id.substring(0, id.lastIndexOf("-")); //$NON-NLS-1$
				sourceName.add(sourceFileName);
				oldInfo.put("fileIndex", "" + i); //$NON-NLS-1$ //$NON-NLS-2$
				info.put(id, oldInfo);

			}

			// 检查是否有父子同在的情况
			Iterator<String> infoIt = info.keySet().iterator();
			while (infoIt.hasNext()) {
				String id = infoIt.next();

				Iterator<String> tarIt = info.keySet().iterator();
				while (tarIt.hasNext()) {
					String tarId = tarIt.next();

					// 如果是自己就继续下一个的比较
					if (tarId.equals(id)) {
						continue;
					}

					if (tarId.startsWith(id)) {
						Map<String, String> srcInfo = info.get(id);
						Map<String, String> tarInfo = info.get(tarId);

						int srcFileIndex = Integer.parseInt(srcInfo.get("fileIndex")); //$NON-NLS-1$
						int tarFileIndex = Integer.parseInt(tarInfo.get("fileIndex")); //$NON-NLS-1$

						final String parentPath = getFullPath(srcFilesPath.get(srcFileIndex));
						final String childPath = getFullPath(srcFilesPath.get(tarFileIndex));
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								MessageDialog.openInformation(shell, Messages
										.getString("splitAndMergeXliff.MergeXliff.msgTitle2"), MessageFormat.format(
										Messages.getString("splitAndMergeXliff.MergeXliff.msg8"), new Object[] {
												parentPath, childPath }));
							}
						});

						return false;
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * 获取合并后的文件的路径
	 * @param srcXlfPath
	 *            被分割的第一个文件
	 */
	private void getTargetFilePath(final Vector<String> srcFilesPath) {
		String originalFileName = xlfhandler.getSplitOriginalName(srcFilesPath.get(0));
		if (originalFileName == null) {
			targetFilePath = null;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(shell, Messages.getString("splitAndMergeXliff.MergeXliff.msgTitle2"),
							Messages.getString("splitAndMergeXliff.MergeXliff.msg9"));
				}
			});
		} else {
			String srcXlfLC = null;
			int leastLength = -1;
			// 循环遍历每一个文件，找出其中深度最低的文件，之后再取其父目录做为合并文件的位置
			for(String fileLC : srcFilesPath){
				IFile iFile = ResourceUtils.fileToIFile(fileLC);
				String fullStr = iFile.getFullPath().toOSString();
				String separator = "\\".equals(System.getProperty("file.separator")) ? "\\\\" : "/";
				String[] array = fullStr.split(separator);
				if (leastLength == -1) {
					leastLength = array.length;
					srcXlfLC = fileLC;
				}else {
					if (array.length < leastLength) {
						srcXlfLC = fileLC;
					}
				}
			}
			
			if (srcXlfLC == null || "".equals(srcXlfLC)) {
				return;
			}
			
			final String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."),
					originalFileName.length());
			originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf(fileExtension)) + "_merged"
					+ fileExtension;
			
			// 下面判断这个层次最高的文件，它的父的父是不是"XLIFF" 文件夹，如果是的话
			IFile srcXlfIFile = ResourceUtils.fileToIFile(srcXlfLC);
			IPath mergeFileParentIPath = null;
			if ("XLIFF".equals(srcXlfIFile.getParent().getParent().getName())) {
				mergeFileParentIPath = srcXlfIFile.getParent().getLocation();
			}else {
				mergeFileParentIPath = srcXlfIFile.getParent().getParent().getLocation();
			}
			
			
			targetFilePath = mergeFileParentIPath.append(originalFileName).toOSString();
			if (new File(targetFilePath).exists()) {
				final String initValue = "Copy of " + originalFileName;
				final String message = MessageFormat.format(Messages.getString("splitAndMergeXliff.MergeXliff.msg10"),
						originalFileName);
				final IPath curPath = mergeFileParentIPath;
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						InputDialog dialog = new InputDialog(shell, Messages
								.getString("splitAndMergeXliff.MergeXliff.dialogTitle"), message, initValue, null);
						dialog.open();
						if (dialog.getReturnCode() == Window.CANCEL) {
							targetFilePath = null;
						} else {
							String newFileName = dialog.getValue();
							if (!fileExtension.equals(newFileName.substring(newFileName.lastIndexOf("."),
									newFileName.length()))) {
								newFileName = newFileName + fileExtension;
							}
							targetFilePath = curPath.append(newFileName).toOSString();
						}
					}
				});
			}
		}
	}

	/**
	 * 创建合并后的目标文件, 并添加xliff节点
	 * @param tarXlfPath
	 *            切割文件的路径
	 * @param newSliptXlfPath
	 *            新的xliff文件的路径
	 */
	public void createTargetXliff(String tarXlfPath, String xliffNodeHeader) {
		FileOutputStream output;
		try {
			output = new FileOutputStream(tarXlfPath);
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

	public boolean compareValue(Hashtable<String, String> hashValueOne, Hashtable<String, String> hashValueTwo) {
		if (hashValueOne.size() != hashValueTwo.size()) {
			return false;
		}
		Iterator<String> key = hashValueOne.keySet().iterator();
		while (key.hasNext()) {
			String keyValue = key.next();
			if (!hashValueOne.get(keyValue).equals(hashValueTwo.get(keyValue))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取相对于项目的相对路径
	 * @param absolutePath
	 * @return
	 */
	public String getFullPath(String absolutePath) {
		// UNDO 合并后的文件好像不能转换回原文件，这里还需完善。
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile iFile = root.getFileForLocation(Path.fromOSString(absolutePath));
		return iFile.getFullPath().toOSString();
	}

	public String getTargetFilePath() {
		return targetFilePath;
	}

	public static void main(String[] args) {
		String xlf_1 = "/testBug/XLIFF/zh-CN/BUG 2424 合并xliff判断出错/测试合并 (zh-cn).txt.hsxliff";
		String[] array_1 = xlf_1.split(System.getProperty("file.separator"));
		System.out.println(array_1.length);
		System.out.println(array_1[2]);
		
		
	}

}



