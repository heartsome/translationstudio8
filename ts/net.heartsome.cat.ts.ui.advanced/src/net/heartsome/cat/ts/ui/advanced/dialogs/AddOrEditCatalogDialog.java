package net.heartsome.cat.ts.ui.advanced.dialogs;

import java.text.MessageFormat;

import net.heartsome.cat.common.ui.dialog.FileFolderSelectionDialog;
import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.handlers.ADXmlHandler;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 添加条目框
 * @author robert 2011-02-20
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class AddOrEditCatalogDialog extends Dialog {
	private Button publicBtn;
	private Button systermBtn;
	private Button uriBtn;
	private Button nextCataBtn;
	private Text idTxt;
	private Text urlTxt;
	private IWorkspaceRoot root;
	private ADXmlHandler adHandler;
	private String catalogXmlLocation;
	private Label idLbl;
	private boolean isAdd;
	private String curXpath;

	public AddOrEditCatalogDialog(Shell parentShell, IWorkspaceRoot root, ADXmlHandler adHandler, boolean isAdd) {
		super(parentShell);
		this.root = root;
		this.adHandler = adHandler;
		catalogXmlLocation = root.getLocation().append(ADConstants.catalogueXmlPath).toOSString();
		this.isAdd = isAdd;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(isAdd ? Messages.getString("dialogs.AddOrEditCatalogDialog.title1") : Messages
				.getString("dialogs.AddOrEditCatalogDialog.title2"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tParent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().hint(600, 150).grab(true, true).applyTo(tParent);

		Composite contentCmp = new Composite(tParent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(contentCmp);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(contentCmp);

		GridData labelData = new GridData(SWT.FILL, SWT.CENTER, false, false);

		// 第一行，类型选择行
		Label typeLbl = new Label(contentCmp, SWT.RIGHT | SWT.NONE);
		typeLbl.setText(Messages.getString("dialogs.AddOrEditCatalogDialog.typeLbl"));
		typeLbl.setLayoutData(labelData);

		Composite radioCmp = new Composite(contentCmp, SWT.NONE);
		radioCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, SWT.DEFAULT));
		radioCmp.setLayout(new GridLayout(4, false));

		publicBtn = new Button(radioCmp, SWT.RADIO);
		publicBtn.setText(Messages.getString("dialogs.AddOrEditCatalogDialog.publicBtn"));

		systermBtn = new Button(radioCmp, SWT.RADIO);
		systermBtn.setText(Messages.getString("dialogs.AddOrEditCatalogDialog.systermBtn"));

		uriBtn = new Button(radioCmp, SWT.RADIO);
		uriBtn.setText(Messages.getString("dialogs.AddOrEditCatalogDialog.uriBtn"));

		nextCataBtn = new Button(radioCmp, SWT.RADIO);
		nextCataBtn.setText(Messages.getString("dialogs.AddOrEditCatalogDialog.nextCataBtn"));

		// 第二行--id行
		idLbl = new Label(contentCmp, SWT.RIGHT);
		idLbl.setText(Messages.getString("dialogs.AddOrEditCatalogDialog.idLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(idLbl);

		idTxt = new Text(contentCmp, SWT.BORDER);
		idTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, SWT.DEFAULT));

		// 第三行--url选择行
		Label urlLbl = new Label(contentCmp, SWT.RIGHT);
		urlLbl.setText(Messages.getString("dialogs.AddOrEditCatalogDialog.urlLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(urlLbl);

		urlTxt = new Text(contentCmp, SWT.BORDER);
		urlTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		urlTxt.setEditable(false);
		
		Button browseBtn = new Button(contentCmp, SWT.NONE);
		browseBtn.setText(Messages.getString("dialogs.AddOrEditCatalogDialog.browseBtn"));

		browseBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browseFiles();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				browseFiles();
			}
		});

		publicBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayIdTxt(true);
			}
		});

		systermBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayIdTxt(true);
			}
		});
		uriBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayIdTxt(true);
			}
		});
		nextCataBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayIdTxt(false);
			}
		});

		return tParent;
	}

	@Override
	protected void okPressed() {
		if (!publicBtn.getSelection() && !systermBtn.getSelection() && !uriBtn.getSelection()
				&& !nextCataBtn.getSelection()) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialogs.AddOrEditCatalogDialog.msgTitle"),
					Messages.getString("dialogs.AddOrEditCatalogDialog.msg1"));
			return;
		}

		StringBuffer strB = new StringBuffer();
		if (publicBtn.getSelection()) {
			strB.append(MessageFormat.format("{2}<public publicId=\"{0}\" uri=\"{1}\"/>",
					new Object[] { idTxt.getText(), urlTxt.getText(), isAdd ? "\t" : "" }));
		} else if (systermBtn.getSelection()) {
			strB.append(MessageFormat.format("{2}<system systemId=\"{0}\" uri=\"{1}\" />",
					new Object[] { idTxt.getText(), urlTxt.getText(), isAdd ? "\t" : "" }));
		} else if (uriBtn.getSelection()) {
			strB.append(MessageFormat.format("{2}<uri name=\"{0}\" uri=\"{1}\" />", new Object[] { idTxt.getText(),
					urlTxt.getText(), isAdd ? "\t" : "" }));
		} else if (nextCataBtn.getSelection()) {
			strB.append(MessageFormat.format("{1}<nextCatalog catalog=\"{0}\" />", new Object[] { urlTxt.getText(),
					isAdd ? "\t" : "" }));
		}

		// 添加
		if (isAdd) {
			if (!adHandler.addDataToXml(catalogXmlLocation, "/catalog", strB.toString())) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("dialogs.AddOrEditCatalogDialog.msgTitle"),
						Messages.getString("dialogs.AddOrEditCatalogDialog.msg2"));
			}
		} else {
			// 修改
			if (!adHandler.updataDataToXml(catalogXmlLocation, curXpath, strB.toString())) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("dialogs.AddOrEditCatalogDialog.msgTitle"),
						Messages.getString("dialogs.AddOrEditCatalogDialog.msg2"));
			}
		}

		super.okPressed();
	}

	public void setInitData(String name, String id, String url, String curXpath) {
		this.curXpath = curXpath;

		if ("public".equalsIgnoreCase(name)) {
			publicBtn.setSelection(true);
			idTxt.setText(id);
			urlTxt.setText(url);
		} else if ("system".equalsIgnoreCase(name)) {
			systermBtn.setSelection(true);
			idTxt.setText(id);
			urlTxt.setText(url);
		} else if ("uri".equalsIgnoreCase(name)) {
			uriBtn.setSelection(true);
			idTxt.setText(id);
			urlTxt.setText(url);
		} else if ("nextCatalog".equalsIgnoreCase(name)) {
			nextCataBtn.setSelection(true);
			displayIdTxt(false);
			urlTxt.setText(url);
		}
	}

	public void displayIdTxt(boolean isDisplay) {
		idLbl.setVisible(isDisplay);
		idTxt.setVisible(isDisplay);
	}

	/**
	 * 选择文件 ;
	 */
	public void browseFiles() {
		FileFolderSelectionDialog dialog = new FileFolderSelectionDialog(getShell(), false, IResource.FILE);
		dialog.setMessage(Messages.getString("dialogs.AddOrEditCatalogDialog.dialogMsg"));
		dialog.setDoubleClickSelects(true);

		try {
			dialog.setInput(EFS.getStore(URIUtil.toURI(root.getLocation().append(ADConstants.cataloguePath))));

		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		dialog.create();
		dialog.getShell().setText(Messages.getString("dialogs.AddOrEditCatalogDialog.dialogTitle"));
		dialog.open();
		if (dialog.getFirstResult() != null) {
			Object object = dialog.getFirstResult();
			if (object instanceof LocalFile) {
				LocalFile localFile = (LocalFile) object;
				String location = localFile.toString();
				String catalogurePath = root.getLocation().append(ADConstants.cataloguePath).toOSString();
				String uriStr = "";
				if (location.indexOf(catalogurePath) != -1) {
					uriStr = location.substring(location.indexOf(catalogurePath) + catalogurePath.length(), location.length());
				}
				uriStr = uriStr.substring(1, uriStr.length());
				urlTxt.setText(uriStr);
			}
		}
	}
}
