package net.heartsome.cat.p2update.util;

import net.heartsome.cat.p2update.resource.Messages;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class P2UpdateUtil {

	public final static int INFO_TYPE_CHECK = 1;

	public final static int INFO_TYPE_AUTO_CHECK = 2;

	public static void openConnectErrorInfoDialog(Shell shell, int type) {
		if (type == INFO_TYPE_AUTO_CHECK) {
			MessageDialog.openInformation(shell, AUTO_UPDATE_PROMPT_INFO_TITLE, UPDATE_PROMPT_INFO_CONTENT);
		} else if (type == INFO_TYPE_CHECK) {
			MessageDialog.openInformation(shell, UPDATE_PROMPT_INFO_TITLE, UPDATE_PROMPT_INFO_CONTENT);
		}
	}
	
	
	public final static String ATUO_CHECK_UPDATE_JOB_NAME = Messages.getString("ATUO_CHECK_UPDATE_JOB_NAME");
	public final static String CHECK_UPDATE_JOB_NAME = Messages.getString("CHECK_UPDATE_JOB_NAME");;
	
	public final static String CHECK_UPDATE_TASK_NAME = Messages.getString("CHECK_UPDATE_TASK_NAME");
	
	public final static String EXECUTE_UPDATE_JOB_NAME = Messages.getString("EXECUTE_UPDATE_JOB_NAME");
	public final static String EXECUTE_UPDATE_Task_NAME = Messages.getString("EXECUTE_UPDATE_Task_NAME");
	
	public final static String AUTO_UPDATE_PROMPT_INFO_TITLE = Messages.getString("AUTO_UPDATE_PROMPT_INFO_TITLE");
	public final static String UPDATE_PROMPT_INFO_TITLE = Messages.getString("UPDATE_PROMPT_INFO_TITLE");
	public final static String UPDATE_PROMPT_INFO_CONTENT = Messages.getString("UPDATE_PROMPT_INFO_CONTENT");
	
	public final static String UI_WIZARD_DIALOG_TITLE = Messages.getString("UI_WIZARD_DIALOG_TITLE");
	public final static String UI_WIZARD_PAGE_TITLE = Messages.getString("UI_WIZARD_PAGE_TITLE");
	public final static String UI_WIZARD_PAGE_DESC = Messages.getString("UI_WIZARD_PAGE_DESC");
	public final static String UI_WIZARD_DESC_PAGE_TITLE = Messages.getString("UI_WIZARD_DESC_PAGE_TITLE");
	public final static String UI_WIZARD_DESC_PAGE_DESC = Messages.getString("UI_WIZARD_DESC_PAGE_DESC");	
	public final static String UPDATE_PROMPT_INFO_NO_UPDATE = Messages.getString("UPDATE_PROMPT_INFO_NO_UPDATE");
	
	public final static String UI_WIZARD_DESC_PAGE_DESC_DETAIL = Messages.getString("UI_WIZARD_DESC_PAGE_DESC_DETAIL"); 
}

