package net.heartsome.cat.ts;

import java.net.URL;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.ContributionComparator;
import org.eclipse.ui.model.IContributionService;
import org.osgi.framework.Bundle;

/**
 * @author cheney
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	public static ApplicationWorkbenchWindowAdvisor WorkbenchWindowAdvisor = null;

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		WorkbenchWindowAdvisor = new ApplicationWorkbenchWindowAdvisor(configurer); 
        return WorkbenchWindowAdvisor;
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return TSPerspective.ID;
	}

	@Override
	public IAdaptable getDefaultPageInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		// make sure we always save and restore workspace state
		configurer.setSaveAndRestore(true);

		IDE.registerAdapters();
		declareWorkbenchImages();
	}

	/**
	 * 声明所需要使用的图片;
	 */
	private void declareWorkbenchImages() {
		final String iconsPath = "$nl$/icons/full/"; //$NON-NLS-1$
		final String pathElocaltool = iconsPath + "elcl16/"; // Enabled //$NON-NLS-1$
		final String pathObject = iconsPath + "obj16/"; // Model object //$NON-NLS-1$

		Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);

		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT, pathObject + "prj_obj.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, pathObject + "cprj_obj.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER, pathElocaltool + "gotoobj_tsk.gif", true); //$NON-NLS-1$

		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_TASK_TSK, pathObject + "taskmrk_tsk.gif", true); //$NON-NLS-1$
		declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_BKMRK_TSK, pathObject + "bkmrk_tsk.gif", true); //$NON-NLS-1$
	}

	/**
	 * Declares an IDE-specific workbench image.
	 * @param symbolicName
	 *            the symbolic name of the image
	 * @param path
	 *            the path of the image file; this path is relative to the base of the IDE plug-in
	 * @param shared
	 *            <code>true</code> if this is a shared image, and <code>false</code> if this is not a shared image
	 * @see IWorkbenchConfigurer#declareImage
	 */
	private void declareWorkbenchImage(Bundle ideBundle, String symbolicName, String path, boolean shared) {
		URL url = FileLocator.find(ideBundle, new Path(path), null);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		getWorkbenchConfigurer().declareImage(symbolicName, desc, shared);
	}
	
	/**
	 * 对首选项菜单排序时，需要覆盖该方法
	 */
	public ContributionComparator getComparatorFor(String contributionType) {
		if (contributionType.equals(IContributionService.TYPE_PREFERENCE)) {
			return new PreferencesComparator();
		}
		else {
			return super.getComparatorFor(contributionType);
		}
	}
}
