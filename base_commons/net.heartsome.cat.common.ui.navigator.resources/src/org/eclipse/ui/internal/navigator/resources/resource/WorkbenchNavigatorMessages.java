/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.resource;

import org.eclipse.osgi.util.NLS;

/**
 * Utility class which helps managing messages
 * 
 * 
 * @since 3.2
 */
public class WorkbenchNavigatorMessages extends NLS {
	/** The bundle properties file */
	public static final String BUNDLE_NAME = "org.eclipse.ui.internal.navigator.resources.resource.messages"; //$NON-NLS-1$

	public static String navigator_all_dialog_warning;
	public static String PortingActionProvider_ImportResourcesMenu_label;

	public static String PortingActionProvider_ExportResourcesMenu_label;

	public static String actions_NewActionProvider_NewMenu_label;

	public static String actions_OpenActionProvider_OpenWithMenu_label;

	public static String resources_ResourceDropAdapterAssistant_title;
	public static String resources_ResourceDropAdapterAssistant_problemImporting;
	public static String resources_ResourceDropAdapterAssistant_problemsMoving;
	public static String resources_ResourceDropAdapterAssistant_targetMustBeResource;
	public static String resources_ResourceDropAdapterAssistant_canNotDropIntoClosedProject;
	public static String resources_ResourceDropAdapterAssistant_resourcesCanNotBeSiblings;
	public static String resources_ResourceDropAdapterAssistant_canNotDropProjectIntoProject;
	public static String resources_ResourceDropAdapterAssistant_dropOperationErrorOther;

	public static String resources_ResourceDropAdapterAssistant_MoveResourceAction_title;
	public static String resources_ResourceDropAdapterAssistant_MoveResourceAction_checkMoveMessage;
	
	public static String actions_ResourceMgmtActionProvider_logTitle;

	public static String actions_WorkingSetRootModeActionGroup_Top_Level_Element_;
	public static String actions_WorkingSetRootModeActionGroup_Project_;
	public static String actions_WorkingSetRootModeActionGroup_Working_Set_;
	public static String actions_WorkingSetActionProvider_multipleWorkingSets;
	
	public static String actions_CopyAction_Cop_;
	public static String actions_CopyAction_Copy_selected_resource_s_;
	
	public static String actions_PasteAction_Past_;
	public static String actions_PasteAction_Paste_selected_resource_s_;

	public static String actions_GotoResourceDialog_GoToTitle;

	public static String resources_ProjectExplorer_toolTip;
	public static String resources_ProjectExplorer_toolTip2;
	public static String resources_ProjectExplorer_toolTip3;
	
	public static String resources_ProjectExplorerPart_workspace;
	public static String resources_ProjectExplorerPart_workingSetModel;
	
	public static String actions_CopyAction_msgTitle;
	public static String actions_CopyAction_msg;
	public static String actions_EditActionGroup_pasteAction;
	public static String actions_EditActionGroup_copyAction;
	public static String actions_EditActionGroup_deleteAction;
	public static String actions_OpenActionProvider_openFileAction;
	public static String actions_RefactorActionGroup_moveAction;
	public static String actions_RefactorActionGroup_renameAction;
	public static String actions_ResourceMgmtActionProvider_openProjectAction;
	public static String actions_ResourceMgmtActionProvider_closeProjectAction;
	public static String actions_ResourceMgmtActionProvider_refreshAction;
	public static String actions_OpenFileWithValidAction_notFindProgram;
	
	static {
		initializeMessages(BUNDLE_NAME, WorkbenchNavigatorMessages.class);
	}
}
