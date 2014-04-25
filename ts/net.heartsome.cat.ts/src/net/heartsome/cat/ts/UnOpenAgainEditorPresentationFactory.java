package net.heartsome.cat.ts;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultMultiTabListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultSimpleTabListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabFolder;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultThemeListener;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

/**
 * 不使用 RCP 自带的 WorkbenchPresentationFactory，用于去除编辑器标题右键菜单中的“新建编辑器”菜单项
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
@SuppressWarnings("restriction")
public class UnOpenAgainEditorPresentationFactory extends WorkbenchPresentationFactory {

	private static int editorTabPosition = PlatformUI.getPreferenceStore()
			.getInt(IWorkbenchPreferenceConstants.EDITOR_TAB_POSITION);
	
	public StackPresentation createEditorPresentation(Composite parent,
			IStackPresentationSite site) {
		DefaultTabFolder folder = new DefaultTabFolder(parent,
				editorTabPosition | SWT.BORDER, site
						.supportsState(IStackPresentationSite.STATE_MINIMIZED),
				site.supportsState(IStackPresentationSite.STATE_MAXIMIZED));

		/*
		 * Set the minimum characters to display, if the preference is something
		 * other than the default. This is mainly intended for RCP applications
		 * or for expert users (i.e., via the plug-in customization file).
		 * 
		 * Bug 32789.
		 */
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		if (store
				.contains(IWorkbenchPreferenceConstants.EDITOR_MINIMUM_CHARACTERS)) {
			final int minimumCharacters = store
					.getInt(IWorkbenchPreferenceConstants.EDITOR_MINIMUM_CHARACTERS);
			if (minimumCharacters >= 0) {
				folder.setMinimumCharacters(minimumCharacters);
			}
		}

		PresentablePartFolder partFolder = new PresentablePartFolder(folder);

		TabbedStackPresentation result = new TabbedStackPresentation(site,
				partFolder, new StandardEditorSystemMenu(site));

		DefaultThemeListener themeListener = new DefaultThemeListener(folder,
				result.getTheme());
		result.getTheme().addListener(themeListener);

		new DefaultMultiTabListener(result.getApiPreferences(),
				IWorkbenchPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS, folder);

		new DefaultSimpleTabListener(result.getApiPreferences(),
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
				folder);

		return result;
	}
	
}
