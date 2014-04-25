/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     mark.melvin@onsemi.com - bug 288997 [CommonNavigator] Double-clicking an adapted resource in 
 *        Common Navigator does not open underlying IFile
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.internal.navigator.resources.resource.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Provides the open and open with menus for IResources.
 * @since 3.2
 */
public class OpenActionProvider extends CommonActionProvider {

	private OpenFileWithValidAction openFileAction;

	private ICommonViewerWorkbenchSite viewSite = null;

	private boolean contribute = false;

	public void init(ICommonActionExtensionSite aConfig) {
		if (aConfig.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			viewSite = (ICommonViewerWorkbenchSite) aConfig.getViewSite();
			openFileAction = new OpenFileWithValidAction(viewSite.getPage());
			openFileAction.setText(WorkbenchNavigatorMessages.actions_OpenActionProvider_openFileAction);
			contribute = true;
		}
	}

	public void fillContextMenu(IMenuManager aMenu) {
		if (!contribute || getContext().getSelection().isEmpty()) {
			return;
		}

		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		openFileAction.selectionChanged(selection);
		if (openFileAction.isEnabled()) {
			aMenu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openFileAction);
		}
//		addOpenWithMenu(aMenu);
	}

	public void fillActionBars(IActionBars theActionBars) {
		if (!contribute) {
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection.size() == 1 && selection.getFirstElement() instanceof IFile) {
			openFileAction.selectionChanged(selection);
			theActionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openFileAction);
		}

	}

//	private void addOpenWithMenu(IMenuManager aMenu) {
//		IStructuredSelection ss = (IStructuredSelection) getContext().getSelection();
//
//		if (ss == null || ss.size() != 1) {
//			return;
//		}
//
//		Object o = ss.getFirstElement();
//
//		// first try IResource
//		IAdaptable openable = (IAdaptable) AdaptabilityUtility.getAdapter(o, IResource.class);
//		// otherwise try ResourceMapping
//		if (openable == null) {
//			openable = (IAdaptable) AdaptabilityUtility.getAdapter(o, ResourceMapping.class);
//		} else if (((IResource) openable).getType() != IResource.FILE) {
//			openable = null;
//		}
//
//		if (openable != null) {
//			// Create a menu flyout.
//
//			IMenuManager submenu = new MenuManager(WorkbenchNavigatorMessages.actions_OpenActionProvider_OpenWithMenu_label,
//					ICommonMenuConstants.GROUP_OPEN_WITH);
//			/*
//			 * IMenuManager submenu = new MenuManager(Messages.getString("actions.OpenActionProvider.submenu"),
//			 * ICommonMenuConstants.GROUP_OPEN_WITH);
//			 */
//			submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
//			submenu.add(new OpenWithMenu(viewSite.getPage(), openable));
//			submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_ADDITIONS));
//
//			// Add the submenu.
//			if (submenu.getItems().length > 2 && submenu.isEnabled()) {
//				aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
//			}
//		}
//	}

}
