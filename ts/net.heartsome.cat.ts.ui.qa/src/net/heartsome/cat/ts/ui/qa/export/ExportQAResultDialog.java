package net.heartsome.cat.ts.ui.qa.export;

import java.io.File;
import java.text.MessageFormat;

import net.heartsome.cat.ts.ui.composite.DialogLogoCmp;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.resource.ImageConstant;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 导出品质检查结果文件对话框
 * @author robert	2013-08-02
 */
public class ExportQAResultDialog extends Dialog{
	private Image logoImage;
	private String exportFilePath;
	private Text pathTxt;

	public ExportQAResultDialog(Shell parentShell) {
		super(parentShell);
		logoImage = Activator.getImageDescriptor(ImageConstant.QA_EXPORTQA_LOG)
				.createImage();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("qa.export.ExportQAResultDialog.dialogTitle"));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentData.widthHint = 450;
		parentData.heightHint = 150;
		tparent.setLayoutData(parentData);

		GridLayoutFactory.fillDefaults().extendedMargins(-1, -1, -1, 8)
				.numColumns(1).applyTo(tparent);

		createLogoArea(tparent);
		
		Composite cmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(cmp);
		GridLayoutFactory.fillDefaults().extendedMargins(9, 9, 0, 0).numColumns(3).applyTo(cmp);
		
		Label label = new Label(cmp, SWT.NONE);
		label.setText(Messages.getString("qa.export.ExportQAResultDialog.filePathLbl"));
		
		pathTxt = new Text(cmp, SWT.BORDER);
//		pathTxt.setText("/Users/Mac/Desktop/test.xlsx");
		exportFilePath = pathTxt.getText();
		pathTxt.setEditable(false);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(pathTxt);
		
		Button browseBtn = new Button(cmp, SWT.NONE);
		browseBtn.setText(Messages.getString("qa.export.ExportQAResultDialog.browseBtn"));
		browseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				FileDialog flg = new FileDialog(getShell(), SWT.SAVE);
				flg.setText(Messages.getString("qa.export.ExportQAResultDialog.fileDialog.title"));
				String[] filter = new String[] {"*.xlsx", "*"};
				flg.setFilterExtensions(filter);
				String path = flg.open();
				if (path != null) {
					if (!path.endsWith(".xlsx")) {
						path += ".xlsx";
					}
					pathTxt.setText(path);
				}
				exportFilePath = pathTxt.getText();
			}
		});
		

		return tparent;
	}

	/**
	 * 显示图片区
	 * 
	 * @param parent
	 */
	public void createLogoArea(Composite parent) {
		new DialogLogoCmp(parent, SWT.NONE, Messages.getString("qa.export.ExportQAResultDialog.logo.title"), 
				Messages.getString("qa.export.ExportQAResultDialog.logo.message"), logoImage);
	}

	public String getExportFilePath() {
		return exportFilePath;
	}
	
	

	@Override
	protected void okPressed() {
		if (new File(exportFilePath).exists()) {
			boolean confirm = MessageDialog.openConfirm(getShell(), Messages.getString("qa.all.dialog.confirm"),
					MessageFormat.format(Messages.getString("qa.export.ExportQAResultDialog.fileExsit"), exportFilePath));
			if (!confirm) {
				return;
			}
		}
		super.okPressed();
	}

	@Override
	public boolean close() {
		if (logoImage != null && !logoImage.isDisposed()) {
			logoImage.dispose();
		}
		return super.close();
	}
}
