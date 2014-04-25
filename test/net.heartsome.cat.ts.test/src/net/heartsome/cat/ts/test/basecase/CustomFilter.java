package net.heartsome.cat.ts.test.basecase;

import net.heartsome.cat.ts.test.ui.dialogs.ManageCustomFilterDialog;
import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.tasks.Waits;


/**
 * 自定义过滤器相关操作
 * @author felix_lu
 *
 */
public class CustomFilter {
	
	private XlfEditor xe;
	private ManageCustomFilterDialog mcfd;
	
	public CustomFilter(XlfEditor xe) {
		this.xe = xe;
	}

	public void addFilter(String filterName, String matchAndOr, String[]... conditions) {
	}
	
	public void openDialog() {
		xe.btnAddCustomFilter().click();
		mcfd = new ManageCustomFilterDialog();
		mcfd.isActive();
	}
	
	public void closeDialog() {
		mcfd = new ManageCustomFilterDialog();
		mcfd.btnClose().click();
		Waits.shellClosed(mcfd);
	}
	
	public void saveFilter(String isValid) {
		
	}
	
	public void addFilter_Name(String filterName, String isValid) {
		
	}
	
	public void addCondition_Keyword(String matchType, String keywordValue, boolean isValid) {
		
	}
	
	public void addCondition_State(String matchType, String stateValue, boolean isValid) {
		
	}
	
	public void addCondition_Note(String matchType, String noteValue, boolean isValid) {
		
	}
	
	public void addCondition_Prop(String propName, String propValue, boolean isValid) {
		
	}
}
