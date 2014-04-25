package net.heartsome.cat.ts.ui.advanced.dialogs.srx;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;

import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 创建(修改)srx文件(名)
 * @author robert 2012-02-28
 * @version
 * @since JDK1.6
 */
public class CreateOrUpdataSRXDialog extends Dialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateOrUpdataSRXDialog.class);
	private boolean isAdd;
	private Text nameTxt;
	private IWorkspaceRoot root;
	/** 当前已经创建完成的，或者正在修改文件文件的名称 */
	private String curSrxName;

	public CreateOrUpdataSRXDialog(Shell parentShell, boolean isAdd) {
		super(parentShell);
		root = ResourcesPlugin.getWorkspace().getRoot();
		this.isAdd = isAdd;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(isAdd ? Messages.getString("srx.CreateOrUpdataSRXDialog.title1") : Messages
				.getString("srx.CreateOrUpdataSRXDialog.title2"));

	}

	@Override
	protected boolean isResizable() {
		return false;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(300, 50).minSize(300, 50).applyTo(tparent);

		Composite nameCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameCmp);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(nameCmp);

		Label nameLbl = new Label(nameCmp, SWT.NONE);
		nameLbl.setText(Messages.getString("srx.CreateOrUpdataSRXDialog.nameLbl"));

		nameTxt = new Text(nameCmp, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameTxt);

		return tparent;
	}

	@Override
	protected void okPressed() {
		String srxName = nameTxt.getText().trim();
		// 先判断该文件名是否合法
		if ("".equals(srxName) || srxName == null) {
			MessageDialog.openInformation(getShell(), Messages.getString("srx.CreateOrUpdataSRXDialog.msgTitle1"),
					Messages.getString("srx.CreateOrUpdataSRXDialog.msg1"));
			return;
		} else if (!srxName.endsWith(".srx")) {
			MessageDialog.openInformation(getShell(), Messages.getString("srx.CreateOrUpdataSRXDialog.msgTitle1"),
					Messages.getString("srx.CreateOrUpdataSRXDialog.msg2"));
			return;
		}
//		String srxLoaction = root.getLocation().append(ADConstants.AD_SRXConfigFolder).append(srxName).toOSString();
		String srxLocation = ADConstants.configLocation + ADConstants.AD_SRXConfigFolder + File.separator + srxName;

		// 创建文件的情况
		if (isAdd) {
			// 在添加之前先难证是否重复
			File srxFile = new File(srxLocation);
			if (srxFile.exists()) {
				boolean response = MessageDialog.openConfirm(getShell(),
						Messages.getString("srx.CreateOrUpdataSRXDialog.msgTitle2"),
						MessageFormat.format(Messages.getString("srx.CreateOrUpdataSRXDialog.msg3"), srxName));
				if (!response) {
					return;
				}
			}

			try {
				FileOutputStream output = new FileOutputStream(srxLocation);
				String initData = "<?xml version=\"1.0\"?>\n"
						+ "<!DOCTYPE srx PUBLIC \"-//SRX//DTD SRX//EN\" \"srx.dtd\">\n" + "<srx version=\"1.0\">\n"
						+ "\t<header segmentsubflows=\"yes\">\n"
						+ "\t\t<formathandle type=\"start\" include=\"no\"/>\n"
						+ "\t\t<formathandle type=\"end\" include=\"yes\"/>\n"
						+ "\t\t<formathandle type=\"isolated\" include=\"yes\"/>\n" + "\t</header>\n" + "</srx>\n";
				output.write(initData.getBytes("UTF-8"));
				output.close();
			} catch (Exception e) {
				LOGGER.error("", e);
			}
			curSrxName = srxName;
		} else {
			// 修改
			if (!curSrxName.equals(srxName)) {
				// 验证修改之后的文件名是否重复
				File editedSrx = new File(ADConstants.configLocation + ADConstants.AD_SRXConfigFolder + File.separator + srxName);
				if (editedSrx.exists()) {
					boolean response = MessageDialog.openConfirm(getShell(),
							Messages.getString("srx.CreateOrUpdataSRXDialog.msgTitle2"),
							MessageFormat.format(Messages.getString("srx.CreateOrUpdataSRXDialog.msg4"), srxName));
					if (!response) {
						return;
					}
				}

				// 执行对文件名的修改工作
				File curSrx = new File(ADConstants.configLocation + ADConstants.AD_SRXConfigFolder + File.separator + curSrxName);
				curSrx.renameTo(editedSrx);
				curSrxName = srxName;
			}
		}
		super.okPressed();
	}

	public String getCurSrxName() {
		return curSrxName;
	}

	public void setEditInitData(String srxName) {
		nameTxt.setText(srxName);
		curSrxName = srxName;
	}
}
