package net.heartsome.cat.ts.drop;

import java.util.Iterator;

import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An editor area drop adapter to handle transfer types <code>LocalSelectionTransfer</code> and
 * <code>FileTransfer</code>.
 * @author weachy
 * @version
 * @since JDK1.5
 * @see org.eclipse.ui.internal.ide.EditorAreaDropAdapter
 */
@SuppressWarnings("restriction")
public class EditorAreaDropAdapter extends DropTargetAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EditorAreaDropAdapter.class);

	private IWorkbenchWindow window;

	public EditorAreaDropAdapter(IWorkbenchWindow window) {
		this.window = window;
	}

	public void dragEnter(DropTargetEvent event) {
		// always indicate a copy
		event.detail = DND.DROP_COPY;
	}

	public void dragOperationChanged(DropTargetEvent event) {
		// always indicate a copy
		event.detail = DND.DROP_COPY;
	}

	public void drop(final DropTargetEvent event) {
		Display d = window.getShell().getDisplay();
		final IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			d.asyncExec(new Runnable() {
				public void run() {
					asyncDrop(event, page);
				}
			});
		}
	}

	private void asyncDrop(DropTargetEvent event, IWorkbenchPage page) {
		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) { // 处理“导航视图”拖拽过来的文件
			if (!(event.data instanceof StructuredSelection)) {
				return;
			}
			StructuredSelection selection = (StructuredSelection) event.data;
			for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof IFile) {
					IFile file = (IFile) o;
//					仅打开从导航视图中拖过来的 XLIFF 文件
					if (CommonFunction.validXlfExtensionByFileName(file.getName())) {
						try {
							IDE.openEditor(page, file, true);
						} catch (PartInitException e) {
							LOGGER.error(" ", e);
							// silently ignore problems opening the editor
						}
					}
				}
			}
		} else if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) { // 处理从工作空间以外拖拽过来的文件
//			从工作空间以外拖过来的文件不做处理
//			String[] filePaths = (String[]) event.data;
//			if (filePaths != null && filePaths.length != 0) {
//				boolean isXliffFile = true; // 是 XLIFF 文件
//				for (String filePath : filePaths) {
//					if (!CommonFunction.validXlfExtensionByFileName(filePath)) {
//						isXliffFile = false;
//						break;
//					}
//				}
//				if (isXliffFile) { // 全部都是 XLIFF 文件
//					for (int i = 0; i < filePaths.length; i++) {
//						IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(filePaths[i]));
//						try {
//							IDE.openEditorOnFileStore(page, fileStore);
//						} catch (PartInitException e) {
//							LOGGER.error("", e);
//							// silently ignore problems opening the editor
//						}
//					}
//				} else { // 否则，就弹出创建项目的对话框，要求创建项目。
//					NewProjectAction action = new NewProjectAction(window);
//					action.run();
//					// TODO 如果成功创建项目，则把拖进来的文件自动拷贝到项目中来。
//					// 未解决问题：无法获取是否已经成功创建（可能用户点击了“取消”）。
//				}
//			}
		}
	}

}
