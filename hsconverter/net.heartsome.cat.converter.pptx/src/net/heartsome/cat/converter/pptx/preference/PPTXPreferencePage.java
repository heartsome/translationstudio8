package net.heartsome.cat.converter.pptx.preference;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.converter.pptx.Activator;
import net.heartsome.cat.converter.pptx.resource.Messages;

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
 * 首选项-->Microsoft PowerPoint 2007 页面
 * @author peason
 * @version
 * @since JDK1.6
 */
public class PPTXPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IPreferenceStore preferenceStore;

	/** 备注复选框 */
	private Button btnNote;

	public PPTXPreferencePage() {
		setTitle(Messages.getString("preference.PPTXPreferencePage.title"));
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
		groupCommon.setText(Messages.getString("preference.PPTXPreferencePage.groupCommon"));

		HsImageLabel imageLabel = new HsImageLabel(Messages.getString("preference.PPTXPreferencePage.imageLabel"),
				Activator.getImageDescriptor(Constants.PREFERENCE_PPTX_32));
		Composite cmpCommon = imageLabel.createControl(groupCommon);
		cmpCommon.setLayout(new GridLayout());
		cmpCommon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnNote = new Button(cmpCommon, SWT.CHECK);
		btnNote.setText(Messages.getString("preference.PPTXPreferencePage.btnNote"));
		GridDataFactory.fillDefaults().applyTo(btnNote);

		imageLabel.computeSize();
		btnNote.setSelection(preferenceStore.getBoolean(Constants.PPTX_FILTER));
		return parent;
	}

	public void init(IWorkbench workbench) {

	}

	@Override
	protected void performDefaults() {
		btnNote.setSelection(preferenceStore.getDefaultBoolean(Constants.PPTX_FILTER));
	}

	@Override
	public boolean performOk() {
		preferenceStore.setValue(Constants.PPTX_FILTER, btnNote.getSelection());
		return true;
	}
}
