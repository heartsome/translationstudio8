package net.heartsome.cat.convert.ui.model;

import java.io.File;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * @author cheney
 * @since JDK1.6
 */
public final class ConverterUtil {
	private static final String PROPERTIES_NAME = "name";

	private static final String PROPERTIES_SELECTED_TYPE = "selectedType";

	private static IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	/**
	 * 私有构建函数
	 */
	private ConverterUtil() {
		// 防止创建实例
	}

	/**
	 * 对下拉列表和转换器列表进行绑定
	 * @param context
	 * @param comboViewer
	 * @param model
	 *            ;
	 */
	public static void bindValue(DataBindingContext context, ComboViewer comboViewer, ConverterViewModel model) {
		// ViewerSupport.bind(comboViewer, BeansObservables.observeList(
		// model, "supportTypes", String.class),
		// Properties.selfValue(String.class));
		//		
		//
		// context.bindValue(ViewersObservables
		// .observeSingleSelection(comboViewer), BeansObservables
		// .observeValue(model,
		// "selectedType"));

		// ObservableListContentProvider viewerContentProvider=new ObservableListContentProvider();
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setComparator(new ViewerComparator());
		// IObservableMap[] attributeMaps = BeansObservables.observeMaps(
		// viewerContentProvider.getKnownElements(),
		// ConverterBean.class, new String[] { "description" });
		// comboViewer.setLabelProvider(new ObservableMapLabelProvider(
		// attributeMaps));
		// comboViewer.setInput(Observables.staticObservableList(model.getSupportTypes(),ConverterBean.class));

		comboViewer.setInput(model.getSupportTypes());
		IViewerObservableValue selection = ViewersObservables.observeSingleSelection(comboViewer);
		IObservableValue observableValue = BeansObservables.observeDetailValue(selection, PROPERTIES_NAME, null);
		context.bindValue(observableValue, BeansObservables.observeValue(model, PROPERTIES_SELECTED_TYPE));
	}

	/**
	 * 是否为工作空间内的路径
	 * @param workspacePath
	 * @return ;
	 */
	private static boolean isWorkspacePath(String workspacePath) {
		IFile file = root.getFileForLocation(URIUtil.toPath(new File(workspacePath).toURI()));
		return file == null;
	}

	/**
	 * 得到本地文件系统路径
	 * @param workspacePath
	 * @return ;
	 */
	public static String toLocalPath(String workspacePath) {
		if (isWorkspacePath(workspacePath)) {
			IPath path = Platform.getLocation();
			return path.append(workspacePath).toOSString();
		} else {
			return workspacePath;
		}
	}

	/**
	 * 得到本地文件。
	 * @param workspacePath
	 * @return ;
	 */
	public static File toLocalFile(String workspacePath) {
		if (isWorkspacePath(workspacePath)) {
			IPath path = Platform.getLocation();
			return path.append(workspacePath).toFile();
		} else {
			return new File(workspacePath);
		}
	}
	
	public static IFile localPath2IFile(String localPath){
		return root.getFileForLocation(URIUtil.toPath(new File(localPath).toURI()));
	}
}
