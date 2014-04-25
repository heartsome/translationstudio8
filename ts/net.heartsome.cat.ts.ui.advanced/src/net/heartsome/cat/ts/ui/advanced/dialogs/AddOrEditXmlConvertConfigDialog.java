package net.heartsome.cat.ts.ui.advanced.dialogs;

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.Activator;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 添加或者修改xml转换配置文件
 * @author robert 2012-02-22
 * @version
 * @since JDK1.6
 */
public class AddOrEditXmlConvertConfigDialog extends XmlConvertManagerDialog {
	private boolean isAdd;
	private Text rootTxt;
	private Label rootTipLbl;
	/** 编辑状态下的根元素 */
	private String curRootStr;

	private Image icon = Activator.getImageDescriptor("icons/tips.gif").createImage();
	
	public AddOrEditXmlConvertConfigDialog(Shell parentShell, boolean isAdd) {
		super(parentShell);
		this.isAdd = isAdd;
		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(isAdd ? Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.title1") : Messages
				.getString("dialogs.AddOrEditXmlConvertConfigDialog.title2"));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite buttonCmp = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		buttonCmp.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonCmp.setLayoutData(data);
		buttonCmp.setFont(parent.getFont());

		Composite leftCmp = new Composite(buttonCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(leftCmp);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(3).equalWidth(false).applyTo(leftCmp);

		addBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.addBtn"), false);
		editBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.editBtn"), false);
		deleteBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.deleteBtn"), false);

		Composite rightCmp = new Composite(buttonCmp, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).equalWidth(false).applyTo(rightCmp);

		// createButton(rightCmp, IDialogConstants.CLIENT_ID, "分析XML(&N)", false);
		new Label(rightCmp, SWT.NONE);

		Label separatorLbl = new Label(buttonCmp, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(separatorLbl);

		new Label(buttonCmp, SWT.NONE);
		Composite bottomCmp = new Composite(buttonCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(bottomCmp);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(2).applyTo(bottomCmp);

		createButton(bottomCmp, IDialogConstants.OK_ID,
				Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.ok"), false);
		createButton(bottomCmp, IDialogConstants.CANCEL_ID,
				Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.cancel"), true).setFocus();

		initListener();

		return buttonCmp;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(700, 500).applyTo(tparent);

		createRootTxt(tparent);
		createTable(tparent);

		return tparent;
	}

	/**
	 * 创建xml根，与存储目录
	 * @param tparent
	 *            ;
	 */
	private void createRootTxt(Composite tparent) {
		Composite composite = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		GridLayoutFactory.fillDefaults().spacing(0, SWT.DEFAULT).numColumns(5).applyTo(composite);

		Label rootLbl = new Label(composite, SWT.NONE);
		rootLbl.setText(Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.rootLbl"));

		rootTxt = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().indent(6, SWT.DEFAULT).hint(100, SWT.DEFAULT).applyTo(rootTxt);

		// 显示一个图标与“被保存到：”
		Label iconLbl = new Label(composite, SWT.NONE);
		iconLbl.setImage(icon);
		GridDataFactory.fillDefaults().indent(4, SWT.DEFAULT).applyTo(iconLbl);

		Label textLbl = new Label(composite, SWT.NONE);
		textLbl.setText(Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.textLbl"));

		rootTipLbl = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.LEFT, SWT.CENTER).applyTo(rootTipLbl);

		rootTxt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String tipText = root.getFullPath().append(ADConstants.AD_xmlConverterConfigFolder)
						.append("config_" + rootTxt.getText().trim() + ".xml").toOSString();
				rootTipLbl.setText(tipText);
				rootTipLbl.pack();
				rootTipLbl.setToolTipText(tipText);
			}
		});

		// 在添加状态下，当根元素文本框失去焦点后，验证是否为空，验证是否重复
		rootTxt.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String rootStr = rootTxt.getText().trim();
				if (isAdd || (!rootStr.equals(curRootStr))) {
					if ("".equals(rootStr)) {
						MessageDialog.openWarning(getShell(),
								Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msgTitle1"),
								Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msg1"));
					} else {
						// 提示文件是否重复
						String configXmlLoaction = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder)
								.append("config_" + rootStr + ".xml").toOSString();
						File xmlConfigFile = new File(configXmlLoaction);
						if (xmlConfigFile.exists()) {
							String configXmlFullPath = root.getFullPath()
									.append(ADConstants.AD_xmlConverterConfigFolder)
									.append("config_" + rootStr + ".xml").toOSString();
							MessageDialog.openWarning(getShell(), Messages
									.getString("dialogs.AddOrEditXmlConvertConfigDialog.msgTitle1"), MessageFormat
									.format(Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msg2"),
											configXmlFullPath));
						}
					}
					super.focusLost(e);
				}
			}
		});
	}

	@Override
	protected void okPressed() {
		String rootStr = rootTxt.getText().trim();
		// 验证根元素是否为空
		if (rootStr == null || "".equals(rootStr)) {
			MessageDialog.openWarning(getShell(),
					Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msgTitle1"),
					Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msg1"));
			return;
		}

		// 添加
		if (isAdd) {
			// 创建文件
			String configXmlLoaction = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder)
					.append("config_" + rootStr + ".xml").toOSString();
			File configXml = new File(configXmlLoaction);
			if (configXml.exists()) {
				String configXmlFullPath = root.getFullPath().append(ADConstants.AD_xmlConverterConfigFolder)
						.append("config_" + rootStr + ".xml").toOSString();
				boolean response = MessageDialog.openConfirm(getShell(), Messages
						.getString("dialogs.AddOrEditXmlConvertConfigDialog.msgTitle2"), MessageFormat.format(
						Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msg3"), configXmlFullPath));
				if (!response) {
					return;
				}
			}
			curConvertXml = configXmlLoaction;
			// 创建文件
			createConfigXML(configXmlLoaction);
		} else {
			String configXmlLoaction = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder)
					.append("config_" + rootStr + ".xml").toOSString();
			// 编辑,如果根元素与当前所编辑的根元素不相等，那么检查是否重复
			if (!rootStr.equals(curRootStr)) {
				File configXml = new File(configXmlLoaction);
				if (configXml.exists()) {
					String configXmlFullPath = root.getFullPath().append(ADConstants.AD_xmlConverterConfigFolder)
							.append("config_" + rootStr + ".xml").toOSString();
					boolean response = MessageDialog.openConfirm(getShell(), Messages
							.getString("dialogs.AddOrEditXmlConvertConfigDialog.msgTitle2"), MessageFormat.format(
							Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msg4"), configXmlFullPath));
					if (!response) {
						return;
					}
				}
				// 删除当前编辑的文件
				File curEditFile = new File(curConvertXml);
				curEditFile.delete();
			}
			curConvertXml = configXmlLoaction;
			// 覆盖或者生成新的文件
			createConfigXML(configXmlLoaction);
		}
		super.okPressed();
	}

	/**
	 * 编辑状态下设置初始化数据
	 * @param convertXml
	 * @return ;
	 */
	public boolean setInitEditData(String convertXml) {
		this.curConvertXml = convertXml;
		File xmlFile = new File(convertXml);
		// 开始填充表格
		Map<String, Object> newResultMap = handler.openFile(curConvertXml, null);
		// 文件解析出错
		if (newResultMap == null
				|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
						.get(QAConstant.RETURNVALUE_RESULT)) {

			MessageDialog.openWarning(getShell(),
					Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msgTitle1"),
					MessageFormat.format(Messages.getString("dialogs.AddOrEditXmlConvertConfigDialog.msg5"), xmlFile.getName()));
			return false;
		}
		// String fullPath = ResourceUtils.getIFileByLocation(curConfigXml).getFullPath().toOSString();
		// System.out.println("fullPath = " + fullPath);

		elementsList = handler.getconvertXmlElements(convertXml);
		refreshTable(null);
		String xmlName = xmlFile.getName();
		xmlName = xmlName.substring(7, xmlName.length() - 4); // 获取config_html.xml中的html
		rootTxt.setText(xmlName);
		curRootStr = xmlName;

		return true;
	}
	
	@Override
	public boolean close() {
		if(icon != null && !icon.isDisposed()){
			icon.dispose();
		}
		return super.close();
	}
}
