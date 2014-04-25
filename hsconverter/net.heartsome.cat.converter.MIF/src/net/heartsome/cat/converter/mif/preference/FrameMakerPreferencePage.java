package net.heartsome.cat.converter.mif.preference;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.converter.mif.Activator;
import net.heartsome.cat.converter.mif.resource.Messages;

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
 * 首选项-->Adobe FrameMarker 页面
 * @author peason
 * @version
 * @since JDK1.6
 */
public class FrameMakerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IPreferenceStore preferenceStore;

	/** 母版内容复选框 */
	private Button btnMaster;

	public FrameMakerPreferencePage() {
		setTitle(Messages.getString("preference.FrameMakerPreferencePage.title"));
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
		groupCommon.setText(Messages.getString("preference.FrameMakerPreferencePage.groupCommon"));

		HsImageLabel imageLabel = new HsImageLabel(
				Messages.getString("preference.FrameMakerPreferencePage.imageLabel"),
				Activator.getImageDescriptor(Constants.PREFERENCE_FRAMEMAKER_32));
		Composite cmpCommon = imageLabel.createControl(groupCommon);
		cmpCommon.setLayout(new GridLayout());
		cmpCommon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnMaster = new Button(cmpCommon, SWT.CHECK);
		btnMaster.setText(Messages.getString("preference.FrameMakerPreferencePage.btnMaster"));
		GridDataFactory.fillDefaults().applyTo(btnMaster);

		imageLabel.computeSize();
		btnMaster.setSelection(preferenceStore.getBoolean(Constants.FRAMEMAKER_FILTER));
		return parent;
	}

	public void init(IWorkbench workbench) {

	}

	@Override
	protected void performDefaults() {
		btnMaster.setSelection(preferenceStore.getDefaultBoolean(Constants.FRAMEMAKER_FILTER));
	}

	@Override
	public boolean performOk() {
		preferenceStore.setValue(Constants.FRAMEMAKER_FILTER, btnMaster.getSelection());
		return true;
	}
}
