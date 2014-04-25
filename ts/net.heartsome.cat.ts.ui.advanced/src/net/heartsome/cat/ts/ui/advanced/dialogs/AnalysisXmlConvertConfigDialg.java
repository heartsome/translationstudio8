package net.heartsome.cat.ts.ui.advanced.dialogs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 分析XML转换配置文件
 * @author robert 2012-02-25
 * @version
 * @since JDK1.6
 */
public class AnalysisXmlConvertConfigDialg extends XmlConvertManagerDialog {
	private Text analysisTxt;
	private Button okBtn;
	/** 正在分析的XML的路径 */
	private String curAnalysisXmlLocation;

	private Image icon = Activator.getImageDescriptor("icons/tips.gif").createImage();
	
	public AnalysisXmlConvertConfigDialg(Shell parentShell) {
		super(parentShell);
		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.title"));
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
				Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.addBtn"), false);
		editBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.editBtn"), false);
		deleteBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.deleteBtn"), false);

		Composite rightCmp = new Composite(buttonCmp, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).equalWidth(false).applyTo(rightCmp);

		new Label(rightCmp, SWT.NONE);

		Label separatorLbl = new Label(buttonCmp, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(separatorLbl);

		new Label(buttonCmp, SWT.NONE);
		Composite bottomCmp = new Composite(buttonCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(bottomCmp);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(2).applyTo(bottomCmp);

		okBtn = createButton(bottomCmp, IDialogConstants.OK_ID,
				Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.ok"), false);
		okBtn.setEnabled(false); // 禁用确定按钮
		createButton(bottomCmp, IDialogConstants.CANCEL_ID,
				Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.cancel"), true).setFocus();
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

	private void createRootTxt(Composite tparent) {
		Composite composite = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		GridLayoutFactory.fillDefaults().spacing(0, 1).numColumns(5).applyTo(composite);

		Label analysisXmlLbl = new Label(composite, SWT.NONE);
		analysisXmlLbl.setText(Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.analysisXmlLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(analysisXmlLbl);

		Composite browseCmp = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().indent(6, SWT.DEFAULT).grab(true, false).span(4, SWT.DEFAULT).applyTo(browseCmp);
		GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(0, 0, 0, 0).applyTo(browseCmp);

		analysisTxt = new Text(browseCmp, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(analysisTxt);

		Button browseBtn = new Button(browseCmp, SWT.NONE);
		browseBtn.setText(Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.browseBtn"));

		Label rootLbl = new Label(composite, SWT.NONE);
		rootLbl.setText(Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.rootLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(rootLbl);

		rootTxt = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().indent(6, SWT.DEFAULT).hint(100, SWT.DEFAULT).applyTo(rootTxt);

		// 显示一个图标与“被保存到：”
		Label iconLbl = new Label(composite, SWT.NONE);
		iconLbl.setImage(icon);
		GridDataFactory.fillDefaults().indent(4, SWT.DEFAULT).applyTo(iconLbl);

		Label textLbl = new Label(composite, SWT.NONE);
		textLbl.setText(Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.textLbl"));

		rootTipLbl = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.LEFT, SWT.CENTER).applyTo(rootTipLbl);

		rootTxt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String tipText = root.getFullPath().append(ADConstants.AD_xmlConverterConfigFolder)
						.append("config_" + rootTxt.getText().trim().toLowerCase() + ".xml").toOSString();
				rootTipLbl.setText(tipText);
				rootTipLbl.pack();
				rootTipLbl.setToolTipText(tipText);
			}
		});

		// 在添加状态下，当根元素文本框失去焦点后，验证是否为空，验证是否重复
		rootTxt.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				String rootStr = rootTxt.getText().trim().toLowerCase();
				if ("".equals(rootStr)) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msgTitle"),
							Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msg1"));
				} else {
					// 提示文件是否重复
					String configXmlLoaction = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder)
							.append("config_" + rootStr + ".xml").toOSString();
					File xmlConfigFile = new File(configXmlLoaction);
					if (xmlConfigFile.exists()) {
						String configXmlFullPath = root.getFullPath().append(ADConstants.AD_xmlConverterConfigFolder)
								.append("config_" + rootStr + ".xml").toOSString();
						MessageDialog.openInformation(getShell(), Messages
								.getString("dialogs.AnalysisXmlConvertConfigDialg.msgTitle"), MessageFormat.format(
								Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msg2"), configXmlFullPath));
					}
				}
				super.focusLost(e);
			}
		});

		browseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				String[] extensions = { "*.xml", "*" };
				String[] names = { Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.filterXML"),
						Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.filterAll") };
				fd.setText(Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.fdTitle"));
				fd.setFilterExtensions(extensions);
				fd.setFilterNames(names);
				String xmlLocation = fd.open();
				analysisTxt.setText(xmlLocation);
				// 解析XML文件并且填充到列表
				analysisXml(xmlLocation);
			}
		});

		analysisTxt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				analysisXml(analysisTxt.getText());
				super.focusLost(e);
			}
		});
	}

	@Override
	protected void okPressed() {
		String rootStr = rootTxt.getText().trim();
		// 验证根元素是否为空
		if (rootStr == null || "".equals(rootStr)) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msgTitle"),
					Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msg1"));
			return;
		}
		// 创建文件
		String configXmlLoaction = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder)
				.append("config_" + rootStr.toLowerCase() + ".xml").toOSString();
		System.out.println("configXmlLoaction = " + configXmlLoaction);
		File configXml = new File(configXmlLoaction);
		if (configXml.exists()) {
			String configXmlFullPath = root.getFullPath().append(ADConstants.AD_xmlConverterConfigFolder)
					.append("config_" + rootStr + ".xml").toOSString();
			boolean response = MessageDialog.openConfirm(getShell(), Messages
					.getString("dialogs.AnalysisXmlConvertConfigDialg.msgTitle2"), MessageFormat.format(
					Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msg3"), configXmlFullPath));
			if (!response) {
				return;
			}
		}
		curConvertXml = configXmlLoaction;
		// 创建文件
		createConfigXML(configXmlLoaction);
		super.okPressed();
	}

	private void analysisXml(String xmlLocation) {
		curAnalysisXmlLocation = xmlLocation;
		// 如果所选文件不为空，就解析该文件
		if (xmlLocation != null && !"".equals(xmlLocation)) {
			// 判断是否是文件
			File xmlFile = new File(xmlLocation);
			if (xmlFile.isDirectory()) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msgTitle"),
						Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msg4"));
				// 禁用okbutton
				okBtn.setEnabled(false);
				return;
			}

			if (!xmlFile.exists()) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msgTitle"),
						Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msg5"));
				okBtn.setEnabled(false);
			}

			String encoding = getEncoding(new File(xmlLocation));
			if (!"UTF-8".equalsIgnoreCase(encoding)) {
				try {
					File tempFile = File.createTempFile("analysisXmlCverter", ".xml");
//					File tempFile = new File("/home/robert/Desktop/test.xml");
					copyFile(xmlLocation, tempFile.getAbsolutePath(), encoding, "utf-8");
					xmlLocation = tempFile.getAbsolutePath();
					tempFile.deleteOnExit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			// 开始填充表格
			Map<String, Object> newResultMap = handler.openFile(xmlLocation, null);
			// 文件解析出错
			if (newResultMap == null
					|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
							.get(QAConstant.RETURNVALUE_RESULT)) {
				MessageDialog.openInformation(getShell(), Messages
						.getString("dialogs.AnalysisXmlConvertConfigDialg.msgTitle"), MessageFormat.format(
						Messages.getString("dialogs.AnalysisXmlConvertConfigDialg.msg6"), xmlLocation));
				// 禁用okbutton
				okBtn.setEnabled(false);
				return;
			}
			okBtn.setEnabled(true);
			elementsList = handler.getAnalysisXmlData(xmlLocation);
			refreshTable(null);
		}
	}
	
	@Override
	public boolean close() {
		if(icon != null && !icon.isDisposed()){
			icon.dispose();
		}
		return super.close();
	}
	

	public static String getEncoding(File file) {
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		boolean checked = false;
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1)
				return charset;
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE";
				checked = true;
			}
			else if (first3Bytes[0] == (byte) 0xFE
					&& first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE";
				checked = true;
			}
			else if (first3Bytes[0] == (byte) 0xEF
					&& first3Bytes[1] == (byte) 0xBB
					&& first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF-8";
				checked = true;
			}
			bis.reset();
			if (!checked) {
				int loc = 0;
				while ((read = bis.read()) != -1) {
					loc++;
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)// (0x80 - 0xBF),也可能在GB编码内
							continue;
						else
							break;
					}
					else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								charset = "UTF-8";
								break;
							}
							else
								break;
						}
						else
							break;
					}
				}
			}
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return charset;
	}  
	
	private static void copyFile(String oldFile, String newFilePath,
			String strOldEncoding, String strNewEncoding) throws Exception {
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamRead = null;
		BufferedReader bufferRead = null;

		BufferedWriter newFileBW = null;
		OutputStreamWriter outputStreamWriter = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileInputStream = new FileInputStream(oldFile);
			inputStreamRead = new InputStreamReader(fileInputStream,
					strOldEncoding);
			bufferRead = new BufferedReader(inputStreamRead);

			fileOutputStream = new FileOutputStream(newFilePath, false);
			outputStreamWriter = new OutputStreamWriter(fileOutputStream,
					strNewEncoding);
			newFileBW = new BufferedWriter(outputStreamWriter);

			String strTSVLine = "";
			
			while ((strTSVLine = bufferRead.readLine()) != null) {
				if (strTSVLine.equals("")) {
					continue;
				}
				newFileBW.write(strTSVLine.replaceAll("Shift_JIS", "UTF-8"));
				newFileBW.write("\n");
			}
		} finally {
			if (bufferRead != null)
				bufferRead.close();
			if (newFileBW != null) {
				newFileBW.flush();
				newFileBW.close();
			}
		}
	}

}
