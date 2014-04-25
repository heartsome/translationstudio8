package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.heartsome.cat.ts.ui.plugin.PluginConfigManage;
import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.bean.PluginConfigBean;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.keys.model.KeyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * 插件配置管理，包括添加与修改插件
 * @author robert 2012-03-05
 * @version
 * @since JDK1.6
 */
public class PluginConfigManageDialog extends Dialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigManageDialog.class);
	
	private boolean isAdd;
	private IWorkspaceRoot root;
	private String pluginXmlLocation;
	/** 当前已经添加或正在修改的插件配置pojo */
	private PluginConfigBean curPluginBean;
	private PluginConfigManage manage = new PluginConfigManage();

	private Text nameTxt;
	private Text commandTxt;
	private Color WHITE;
	private Color ORANGE;
	private Color BUTTON;
	private Combo keyCmb;
	private boolean errors;
	private KeySequenceText fKeySequenceText;
	private Text keyTxt;
	@SuppressWarnings("restriction")
	private KeyController keyController;
	private Text switchTxt;
	private Button switchBrowseBtn;

	/** 当前文本段 */
	private Button outputSegemntBtn;
	/** 当前文档 */
	private Button outputDocumentBtn;
	/** 空白 */
	private Button outputBlankBtn;
	/** 已更新的交换文件 */
	private Button inputUpdateFileBtn;
	/** 已更新的文档 */
	private Button inputUpdateDocuBtn;
	/** 空白 */
	private Button inputBlankBtn;

	public PluginConfigManageDialog(Shell parentShell, boolean isAdd) {
		super(parentShell);
		this.isAdd = isAdd;
		root = ResourcesPlugin.getWorkspace().getRoot();
		pluginXmlLocation = root.getLocation().append(PluginConstants.PC_pluginConfigLocation).toOSString();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(isAdd ? Messages.getString("dialog.PluginConfigManageDialog.title1") : Messages
				.getString("dialog.PluginConfigManageDialog.title2"));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(500, 350).minSize(500, 350).applyTo(tparent);

		// 插件基本信息，包括名称，命令行之类的东西
		Composite pluginInfoCmp = new Composite(tparent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(pluginInfoCmp);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(pluginInfoCmp);

		Label nameLbl = new Label(pluginInfoCmp, SWT.NONE);
		nameLbl.setText(Messages.getString("dialog.PluginConfigManageDialog.nameLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(nameLbl);

		nameTxt = new Text(pluginInfoCmp, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, SWT.DEFAULT)
				.applyTo(nameTxt);

		Label commandLbl = new Label(pluginInfoCmp, SWT.NONE);
		commandLbl.setText(Messages.getString("dialog.PluginConfigManageDialog.commandLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(commandLbl);

		commandTxt = new Text(pluginInfoCmp, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(commandTxt);

		Button browseBtn = new Button(pluginInfoCmp, SWT.NONE);
		browseBtn.setText(Messages.getString("dialog.PluginConfigManageDialog.browseBtn"));
		browseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] extensions = { "*.exe", "*.*" };
				String[] names = { Messages.getString("dialog.PluginConfigManageDialog.names1"),
						Messages.getString("dialog.PluginConfigManageDialog.names2") };
				String fileLocation = browseFile(Messages.getString("dialog.PluginConfigManageDialog.dialogTitle"),
						extensions, names);
				commandTxt.setText(fileLocation == null ? "" : fileLocation);
			}
		});

		createShortcutKeyGoup(tparent);
		createProcessesAddReturnGroup(tparent);
		createSwitchCmp(tparent);
		initListener();

		return tparent;
	}

	/**
	 * 创建快捷键面板
	 * @param tparent
	 *            ;
	 */
	private void createShortcutKeyGoup(Composite tparent) {
		Group group = new Group(tparent, SWT.None);
		group.setText(Messages.getString("dialog.PluginConfigManageDialog.group"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);

		Label keyLbl = new Label(group, SWT.NONE);
		keyLbl.setText(Messages.getString("dialog.PluginConfigManageDialog.keyLbl"));

		keyTxt = new Text(group, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(keyTxt);

		fKeySequenceText = new KeySequenceText(keyTxt);
		fKeySequenceText.setKeyStrokeLimit(4);
		fKeySequenceText.addPropertyChangeListener(new IPropertyChangeListener() {
			public final void propertyChange(final PropertyChangeEvent event) {
				if (!event.getOldValue().equals(event.getNewValue())) {
					final KeySequence keySequence = fKeySequenceText.getKeySequence();
					if (!keySequence.isComplete()) {
						return;
					}
					keyTxt.setSelection(keyTxt.getTextLimit());
				}
			}
		});
	}

	/**
	 * 创建进程与返回两个group
	 * @param tparent
	 *            ;
	 */
	private void createProcessesAddReturnGroup(Composite tparent) {
		Composite prCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(prCmp);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(prCmp);

		GridData buttonData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		// 进程，相当于界面上的输出
		Group processGroup = new Group(prCmp, SWT.None);
		processGroup.setText(Messages.getString("dialog.PluginConfigManageDialog.processGroup"));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(processGroup);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(processGroup);

		outputSegemntBtn = new Button(processGroup, SWT.RADIO);
		outputSegemntBtn.setText(Messages.getString("dialog.PluginConfigManageDialog.outputSegemntBtn"));
		outputSegemntBtn.setLayoutData(buttonData);

		outputDocumentBtn = new Button(processGroup, SWT.RADIO);
		outputDocumentBtn.setText(Messages.getString("dialog.PluginConfigManageDialog.outputDocumentBtn"));
		outputDocumentBtn.setLayoutData(buttonData);

		outputBlankBtn = new Button(processGroup, SWT.RADIO);
		outputBlankBtn.setText(Messages.getString("dialog.PluginConfigManageDialog.outputBlankBtn"));
		outputBlankBtn.setLayoutData(buttonData);

		// 返回（相当于界面上的输入）
		Group returnGroup = new Group(prCmp, SWT.None);
		returnGroup.setText(Messages.getString("dialog.PluginConfigManageDialog.returnGroup"));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(returnGroup);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(returnGroup);

		inputUpdateFileBtn = new Button(returnGroup, SWT.RADIO);
		inputUpdateFileBtn.setText(Messages.getString("dialog.PluginConfigManageDialog.inputUpdateFileBtn"));
		inputUpdateFileBtn.setLayoutData(buttonData);

		inputUpdateDocuBtn = new Button(returnGroup, SWT.RADIO);
		inputUpdateDocuBtn.setText(Messages.getString("dialog.PluginConfigManageDialog.inputUpdateDocuBtn"));
		inputUpdateDocuBtn.setLayoutData(buttonData);

		inputBlankBtn = new Button(returnGroup, SWT.RADIO);
		inputBlankBtn.setText(Messages.getString("dialog.PluginConfigManageDialog.inputBlankBtn"));
		inputBlankBtn.setLayoutData(buttonData);

		// 当前文本段的事件
		outputSegemntBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputUpdateFileBtn.setEnabled(true);
				inputUpdateDocuBtn.setEnabled(false);
				inputUpdateDocuBtn.setSelection(false);
				inputBlankBtn.setEnabled(true);
				switchTxt.setEnabled(true);
				switchBrowseBtn.setEnabled(true);
			}
		});

		// 当前文档的事件
		outputDocumentBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputUpdateFileBtn.setEnabled(false);
				inputUpdateFileBtn.setSelection(false);
				inputUpdateDocuBtn.setEnabled(true);
				inputBlankBtn.setEnabled(true);
				switchTxt.setEnabled(false);
				switchTxt.setText("");
				switchBrowseBtn.setEnabled(false);
			}
		});

		// 进程中空格按钮的事件
		outputBlankBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputUpdateFileBtn.setEnabled(false);
				inputUpdateFileBtn.setSelection(false);
				inputUpdateDocuBtn.setEnabled(false);
				inputUpdateDocuBtn.setSelection(false);
				inputBlankBtn.setEnabled(false);
				inputBlankBtn.setSelection(false);
				switchTxt.setEnabled(false);
				switchTxt.setText("");
				switchBrowseBtn.setEnabled(false);
			}
		});
	}

	private void createSwitchCmp(Composite tparent) {
		Composite cmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(cmp);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(cmp);

		Label switchLbl = new Label(cmp, SWT.NONE);
		switchLbl.setText(Messages.getString("dialog.PluginConfigManageDialog.switchLbl"));

		switchTxt = new Text(cmp, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(switchTxt);

		switchBrowseBtn = new Button(cmp, SWT.NONE);
		switchBrowseBtn.setText(Messages.getString("dialog.PluginConfigManageDialog.switchBrowseBtn"));
		switchBrowseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String fileLocation = browseFile(Messages.getString("dialog.PluginConfigManageDialog.dialogTitle2"),
						null, null);
				switchTxt.setText(fileLocation == null ? "" : fileLocation);
			}
		});
	}

	@Override
	protected void okPressed() {
		String id = "" + System.currentTimeMillis();
		String name = nameTxt.getText().trim();
		String command = commandTxt.getText().trim();
		if ("".equals(name) || name == null) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.PluginConfigManageDialog.msgTitle"),
					Messages.getString("dialog.PluginConfigManageDialog.msg1"));
			return;
		}
		if ("".equals(command) || command == null) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.PluginConfigManageDialog.msgTitle"),
					Messages.getString("dialog.PluginConfigManageDialog.msg2"));
			return;
		}
		String input = "";
		String output = "";
		if (inputUpdateFileBtn.getSelection()) {
			input = PluginConstants.EXCHANGEFILE;
		} else if (inputUpdateDocuBtn.getSelection()) {
			input = PluginConstants.DOCUMENT;
		} else if (inputBlankBtn.getSelection()) {
			input = PluginConstants.NONE;
		}

		if (outputSegemntBtn.getSelection()) {
			output = PluginConstants.SEGMENT;
		} else if (outputDocumentBtn.getSelection()) {
			output = PluginConstants.DOCUMENT;
		} else if (outputBlankBtn.getSelection()) {
			output = PluginConstants.NONE;
		}

		String shortcutKey = keyTxt.getText().trim();
		String outputPath = switchTxt.getText().trim();

		PluginConfigBean pluginBean = new PluginConfigBean(id, name, command, input, output, outputPath, shortcutKey);

		if (isAdd) {
			if (!addPluginData(pluginBean)) {
				return;
			} else {
				manage.addPluginMenu(pluginBean);
			}
		} else {
			// 修改之后的数据，ID是不能变的。
			pluginBean.setId(curPluginBean.getId());
			// 执行修改操作
			editPlugindata(pluginBean);
		}

		curPluginBean = pluginBean;
		super.okPressed();
	}

	/**
	 * 选择文件
	 * @param title
	 * @param extensions
	 * @param names
	 * @return ;
	 */
	private String browseFile(String title, String[] extensions, String[] names) {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		fd.setText(title);
		if (extensions != null) {
			fd.setFilterExtensions(extensions);
		}
		if (names != null) {
			fd.setFilterNames(names);
		}
		return fd.open();
	}

	/**
	 * 给所有的按钮添加事件 ;
	 */
	private void initListener() {

	}

	/**
	 * 添加插件配置信息到插件文件 ;
	 */
	private boolean addPluginData(PluginConfigBean pluginBean) {
		File pluginXMl = new File(pluginXmlLocation);
		if (!pluginXMl.getParentFile().exists()) {
			pluginXMl.getParentFile().mkdirs();
		}
		pluginXMl = new File(pluginXmlLocation);

		try {
			// 如果配置文件不存在，则创建
			if (!pluginXMl.exists()) {
				OutputStream output = new FileOutputStream(pluginXmlLocation);
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n".getBytes("UTF-8"));
				output.write("<shortcuts>\n</shortcuts>".getBytes("UTF-8"));
				output.close();
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}

		// 开始存放文件
		try {
			VTDGen vg = new VTDGen();
			vg.parseFile(pluginXmlLocation, true);
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);

			ap.selectXPath(manage.buildXpath(pluginBean));
			if (ap.evalXPath() != -1) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("dialog.PluginConfigManageDialog.msgTitle"),
						Messages.getString("dialog.PluginConfigManageDialog.msg3"));
				return false;
			}
			ap.resetXPath();

			// 先判断此次添加的数据是否存在

			String addData = manage.buildPluginData(pluginBean);
			ap.selectXPath("/shortcuts");
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.insertBeforeTail(addData + "\n");

				FileOutputStream fos = new FileOutputStream(pluginXmlLocation);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				xm.output(bos); // 写入文件
				bos.close();
				fos.close();
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return true;
	}

	public PluginConfigBean getCurPluginBean() {
		return curPluginBean;
	}

	/**
	 * 初始化编辑数据
	 * @param pluginBean
	 *            ;
	 */
	public void setEditInitData(PluginConfigBean pluginBean) {
		nameTxt.setText(pluginBean.getName());
		commandTxt.setText(pluginBean.getCommandLine());
		switchTxt.setText(pluginBean.getOutputPath());
		keyTxt.setText(pluginBean.getShortcutKey());

		String output = pluginBean.getOutput();
		if (output.equals(PluginConstants.SEGMENT)) {
			outputSegemntBtn.setSelection(true);
		} else if (output.equals(PluginConstants.DOCUMENT)) {
			outputDocumentBtn.setSelection(true);
		} else if (output.equals(PluginConstants.NONE)) {
			outputBlankBtn.setSelection(true);
		}

		String input = pluginBean.getInput();
		if (input.equals(PluginConstants.EXCHANGEFILE)) {
			inputUpdateFileBtn.setSelection(true);
		} else if (input.equals(PluginConstants.DOCUMENT)) {
			inputUpdateDocuBtn.setSelection(true);
		} else if (input.equals(PluginConstants.NONE)) {
			inputBlankBtn.setSelection(true);
		}

		// 当前文本段的事件
		if (outputSegemntBtn.getSelection()) {
			inputUpdateFileBtn.setEnabled(true);
			inputUpdateDocuBtn.setEnabled(false);
			inputUpdateDocuBtn.setSelection(false);
			inputBlankBtn.setEnabled(true);
			switchTxt.setEnabled(true);
			switchBrowseBtn.setEnabled(true);
		}

		// 当前文档的事件
		if (outputDocumentBtn.getSelection()) {
			inputUpdateFileBtn.setEnabled(false);
			inputUpdateFileBtn.setSelection(false);
			inputUpdateDocuBtn.setEnabled(true);
			inputBlankBtn.setEnabled(true);
			switchTxt.setEnabled(false);
			switchTxt.setText("");
			switchBrowseBtn.setEnabled(false);
		}

		// 进程中空格按钮的事件
		if (outputBlankBtn.getSelection()) {
			inputUpdateFileBtn.setEnabled(false);
			inputUpdateFileBtn.setSelection(false);
			inputUpdateDocuBtn.setEnabled(false);
			inputUpdateDocuBtn.setSelection(false);
			inputBlankBtn.setEnabled(false);
			inputBlankBtn.setSelection(false);
			switchTxt.setEnabled(false);
			switchTxt.setText("");
			switchBrowseBtn.setEnabled(false);
		}
		curPluginBean = pluginBean;
	}

	private void editPlugindata(PluginConfigBean bean) {
		VTDGen vg = new VTDGen();
		vg.parseFile(pluginXmlLocation, true);
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath(manage.buildXpath(curPluginBean));
			XMLModifier xm = new XMLModifier(vn);
			while (ap.evalXPath() != -1) {
				xm.remove();
				xm.insertAfterElement(manage.buildPluginData(bean));
				manage.updataPluginMenu(bean);
			}
			FileOutputStream fos = new FileOutputStream(pluginXmlLocation);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fos.close();
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}
}
