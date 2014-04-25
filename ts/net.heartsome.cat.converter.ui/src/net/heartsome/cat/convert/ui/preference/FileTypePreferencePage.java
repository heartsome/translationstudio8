package net.heartsome.cat.convert.ui.preference;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.convert.ui.resource.Messages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 首选项的文件类型页面
 * @author peason
 * @version
 * @since JDK1.6
 */
public class FileTypePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public FileTypePreferencePage() {
		setTitle(Messages.getString("preference.FileTypePreferencePage.title"));
	}

	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		HsImageLabel imageLabel = new HsImageLabel(Messages.getString("preference.FileTypePreferencePage.imageLabel"),
				null);
		imageLabel.createControl(tparent);
		imageLabel.computeSize();
		return parent;
	}

}
