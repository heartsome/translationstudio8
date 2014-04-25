/**
 * PreTransUitls.java
 *
 * Version information :
 *
 * Date:2012-6-25
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.pretranslation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.core.bean.XliffBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.pretranslation.bean.PreTransParameters;
import net.heartsome.cat.ts.pretranslation.bean.PreTranslationCounter;
import net.heartsome.cat.ts.pretranslation.dialog.PreTranslationDialog;
import net.heartsome.cat.ts.pretranslation.dialog.PreTranslationResultDialog;
import net.heartsome.cat.ts.pretranslation.resource.Messages;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.VTDGen;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class PreTransUitls {
	public static final Logger logger = LoggerFactory.getLogger(PreTransUitls.class);

	public static void executeTranslation(List<IFile> list, final Shell shell) {
		HsMultiActiveCellEditor.commit(true);
		try {
			if (list.size() == 0) {
				MessageDialog.openInformation(shell, Messages.getString("pretranslation.PreTransUitls.msgTitle"),
						Messages.getString("pretranslation.PreTransUitls.msg1"));
				return;
			}

			List<IFile> lstFiles = new ArrayList<IFile>();
			XLFValidator.resetFlag();
			for (IFile iFile : list) {
				if (!XLFValidator.validateXliffFile(iFile)) {
					lstFiles.add(iFile);
				}
			}
			XLFValidator.resetFlag();
			list = new ArrayList<IFile>(list);
			list.removeAll(lstFiles);
			if (list.size() == 0) {
				return;
			}

			final IProject project = list.get(0).getProject();
			final List<String> filesWithOsPath = ResourceUtils.IFilesToOsPath(list);

			final XLFHandler xlfHandler = new XLFHandler();
			Map<String, Object> resultMap = xlfHandler.openFiles(filesWithOsPath);

			if (resultMap == null
					|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap.get(Constant.RETURNVALUE_RESULT)) {
				// 打开文件失败。
				MessageDialog.openInformation(shell, Messages.getString("pretranslation.PreTransUitls.msgTitle"),
						Messages.getString("pretranslation.PreTransUitls.msg2"));
				return;
			}
			Map<String, List<XliffBean>> map = xlfHandler.getXliffInfo();

			final PreTransParameters parameters = new PreTransParameters();
			PreTranslationDialog dialog = new PreTranslationDialog(shell, map, parameters);
			if (dialog.open() == Window.OK) {
				if (project == null) {
					MessageDialog.openInformation(shell, Messages.getString("pretranslation.PreTransUitls.msgTitle"),
							Messages.getString("pretranslation.PreTransUitls.msg3"));
					return;
				}
				if (filesWithOsPath == null || filesWithOsPath.size() == 0) {
					MessageDialog.openInformation(shell, Messages.getString("pretranslation.PreTransUitls.msgTitle"),
							Messages.getString("pretranslation.PreTransUitls.msg4"));
					return;
				}

				final List<IFile> lstFile = list;
				IRunnableWithProgress runnable = new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						PreTranslation pt = new PreTranslation(xlfHandler, filesWithOsPath, project, parameters);
						try {
							final List<PreTranslationCounter> result = pt.executeTranslation(monitor);
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									PreTranslationResultDialog dialog = new PreTranslationResultDialog(shell, result);
									dialog.open();
								}
							});
							project.refreshLocal(IResource.DEPTH_INFINITE, null);
							result.clear();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (CoreException e) {
							logger.error("", e);
							e.printStackTrace();
						} finally {
							pt.clearResources();
						}
						Display.getDefault().syncExec(new Runnable() {

							public void run() {

								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
										.getActivePage();
								for (IFile file : lstFile) {
									FileEditorInput editorInput = new FileEditorInput(file);
									IEditorPart editorPart = page.findEditor(editorInput);
									// 选择所有语言
									XLFHandler handler = null;
									if (editorPart != null && editorPart instanceof IXliffEditor) {
										// xliff 文件已用 XLIFF 编辑器打开
										IXliffEditor xliffEditor = (IXliffEditor) editorPart;
										handler = xliffEditor.getXLFHandler();
										handler.resetCache();
										VTDGen vg = new VTDGen();
										String path = ResourceUtils.iFileToOSPath(file);
										if (vg.parseFile(path, true)) {
											handler.getVnMap().put(path, vg.getNav());
											xliffEditor.refresh();
										}
									}
								}
							}

						});
					}
				};

				try {
					new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(
							true, true, runnable);
				} catch (InvocationTargetException e) {
					logger.error(Messages.getString("pretranslation.PreTransUitls.logger1"), e);
				} catch (InterruptedException e) {
					logger.error(Messages.getString("pretranslation.PreTransUitls.logger1"), e);
				}
			}
		} finally {
			HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.getCurrent());
		}
	}
}
