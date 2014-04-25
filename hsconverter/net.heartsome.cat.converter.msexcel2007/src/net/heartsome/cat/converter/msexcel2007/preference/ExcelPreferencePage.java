package net.heartsome.cat.converter.msexcel2007.preference;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.converter.msexcel2007.Activator;
import net.heartsome.cat.converter.msexcel2007.resource.Messages;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 首选项-->Microsoft Excel 2007 页面
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ExcelPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IPreferenceStore preferenceStore;
	
	private Button btnRedFont;
	
	public ExcelPreferencePage() {
		setTitle(Messages.getString("preference.ExcelPreferencePage.title"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group groupCommon = new Group(tparent, SWT.NONE);
		groupCommon.setLayout(new GridLayout());
		groupCommon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupCommon.setText(Messages.getString("preference.ExcelPreferencePage.groupCommon"));

		HsImageLabel imageLabel = new HsImageLabel(
				Messages.getString("preference.ExcelPreferencePage.imageLabel"),
				Activator.getImageDescriptor(Constants.PREFERENCE_EXCEL_32));
		Composite cmpCommon = imageLabel.createControl(groupCommon);
		cmpCommon.setLayout(new GridLayout());
		cmpCommon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRedFont = new Button(cmpCommon, SWT.CHECK);
		btnRedFont.setText(Messages.getString("preference.ExcelPreferencePage.btnRedFont"));
		GridDataFactory.fillDefaults().applyTo(btnRedFont);
		
		imageLabel.computeSize();
		btnRedFont.setSelection(preferenceStore.getBoolean(Constants.EXCEL_FILTER));
		return parent;
	}

	public void init(IWorkbench workbench) {
		
	}
	
	@Override
	protected void performDefaults() {
		btnRedFont.setSelection(preferenceStore.getDefaultBoolean(Constants.EXCEL_FILTER));
	}

	@Override
	public boolean performOk() {
		preferenceStore.setValue(Constants.EXCEL_FILTER, btnRedFont.getSelection());
		return true;
	}
}
