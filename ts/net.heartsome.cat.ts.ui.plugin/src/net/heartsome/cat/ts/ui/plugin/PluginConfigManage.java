package net.heartsome.cat.ts.ui.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.plugin.bean.PluginConfigBean;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * 插件配置管理的公用类
 * @author robert 2012-03-07
 * @version
 * @since JDK1.6
 */
public class PluginConfigManage {

	private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigManage.class);
	/** 插件配置文件的路径 */
	private String pluginXmlLocation;
	/** 菜单栏父菜单的管理类 */
	private MenuManager parentManager;
	private Shell shell;

	@SuppressWarnings("restriction")
	public PluginConfigManage() {
		pluginXmlLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.append(PluginConstants.PC_pluginConfigLocation).toOSString();
		WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		parentManager = window.getMenuBarManager();
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	@SuppressWarnings("restriction")
	public PluginConfigManage(String pluginXmlLocation) {
		this.pluginXmlLocation = pluginXmlLocation;
		WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		parentManager = window.getMenuBarManager();
	}

	/**
	 * 根据插件配置的数据，获取构造XPath的方法
	 * @param bean
	 * @return ;
	 */
	public String buildXpath(PluginConfigBean bean) {
		String name = bean.getName();
		String command = bean.getCommandLine();
		String input = bean.getInput();
		String output = bean.getOutput();
		String outputPath = bean.getOutputPath();
		String shortcutKey = bean.getShortcutKey();

		String xpath = "/shortcuts/plugin[@command='" + command + "' and @input='" + input + "' and @output='" + output
				+ "' and @outputpath='" + outputPath + "' and @shortcut-key='" + shortcutKey + "' and text()='" + name
				+ "']";
		return xpath;
	}

	/**
	 * 根据一个pluginConfigBean的实例，组合成添加到XML中的数据
	 * @param bean
	 * @return ;
	 */
	public String buildPluginData(PluginConfigBean bean) {
		StringBuffer pluginData = new StringBuffer();
		pluginData
				.append(MessageFormat
						.format("<plugin id=\"{0}\" command=\"{1}\" input=\"{2}\" output=\"{3}\" outputpath=\"{4}\" shortcut-key=\"{5}\">{6}</plugin>",
								new Object[] { bean.getId(), bean.getCommandLine(), bean.getInput(), bean.getOutput(),
										bean.getOutputPath(), bean.getShortcutKey(), bean.getName() }));

		return pluginData.toString();
	}

	/**
	 * 从插件配置文件中获取插件配置的相关信息
	 * @return ;
	 */
	public List<PluginConfigBean> getPluginCofigData() {
		System.out.println(pluginXmlLocation);
		List<PluginConfigBean> dataList = new LinkedList<PluginConfigBean>();

		File pluginXMl = new File(pluginXmlLocation);
		if (!pluginXMl.exists()) {
			return dataList;
		}

		VTDGen vg = new VTDGen();
		vg.parseFile(pluginXmlLocation, true);
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);

		try {
			ap.selectXPath("/shortcuts/plugin");
			PluginConfigBean bean;
			while (ap.evalXPath() != -1) {
				String id = "";
				String name = "";
				String commandLine = "";
				String output = "";
				String input = "";
				String shortcutKey = "";
				String outputPath = "";

				int index = -1;
				if ((index = vn.getAttrVal("id")) != -1) {
					id = vn.toString(index);
				}

				if ((index = vn.getText()) != -1) {
					name = vn.toString(index);
				}
				if ((index = vn.getAttrVal("command")) != -1) {
					commandLine = vn.toString(index);
				}
				if ((index = vn.getAttrVal("output")) != -1) {
					output = vn.toString(index);
				}
				if ((index = vn.getAttrVal("input")) != -1) {
					input = vn.toString(index);
				}
				if ((index = vn.getAttrVal("shortcut-key")) != -1) {
					shortcutKey = vn.toString(index);
				}
				if ((index = vn.getAttrVal("outputpath")) != -1) {
					outputPath = vn.toString(index);
				}
				bean = new PluginConfigBean(id, name, commandLine, input, output, outputPath, shortcutKey);
				dataList.add(bean);
			}

		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return dataList;
	}

	public void addPluginMenu(final PluginConfigBean bean) {
		for (int i = 0; i < parentManager.getItems().length; i++) {
			if ("net.heartsome.cat.ts.ui.menu.plugin".equals(parentManager.getItems()[i].getId())) {
				MenuManager pluginMenu = (MenuManager) parentManager.getItems()[i];
				// 开始添加新的菜单
				Action action = new Action() {
					@Override
					public void run() {
						executePlugin(bean);
					}
				};
				action.setText(bean.getName());
				action.setId(bean.getId());
				if (!"".equals(bean.getShortcutKey())) {
					action.setText(bean.getName() + "\t" + bean.getShortcutKey());
				}

				pluginMenu.add(action);
				pluginMenu.update();
			}
		}
	}

	/**
	 * 删除配置插件的菜单
	 * @param idList
	 *            ;
	 */
	public void deletePluginMenu(String deleteId) {
		for (int i = 0; i < parentManager.getItems().length; i++) {
			if ("net.heartsome.cat.ts.ui.menu.plugin".equals(parentManager.getItems()[i].getId())) {
				MenuManager pluginMenu = (MenuManager) parentManager.getItems()[i];
				// 开始删除已经添加的菜单
				for (int j = 0; j < pluginMenu.getItems().length; j++) {
					String actionId = pluginMenu.getItems()[j].getId();
					if (deleteId.equals(actionId)) {
						pluginMenu.remove(actionId);
					}
				}
				pluginMenu.update();
			}
		}
	}

	public void updataPluginMenu(PluginConfigBean bean) {
		String id = bean.getId();
		for (int i = 0; i < parentManager.getItems().length; i++) {
			if ("net.heartsome.cat.ts.ui.menu.plugin".equals(parentManager.getItems()[i].getId())) {
				MenuManager pluginMenu = (MenuManager) parentManager.getItems()[i];
				// 开始删除已经添加的菜单
				for (int j = 0; j < pluginMenu.getItems().length; j++) {
					String actionId = pluginMenu.getItems()[j].getId();
					if (id.equals(actionId)) {
						pluginMenu.remove(id);
						pluginMenu.update();
						addPluginMenu(bean);
					}
				}
			}
		}
	}

	/**
	 * 运行自定义的插件
	 * @param bean
	 *            ;
	 */
	@SuppressWarnings("unchecked")
	public void executePlugin(PluginConfigBean bean) {
		String commandLine = bean.getCommandLine();
		if (commandLine == null || "".equals(commandLine)) {
			MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
					Messages.getString("plugin.PluginConfigManage.msg1"));
			return;
		}

		try {
			// 当输出(进程)为当前文档时
			if (bean.getOutput().equals(PluginConstants.SEGMENT)) {
				// 先检查是否有已经打开的文件，若没有，退出插件执行
				XLIFFEditorImplWithNatTable nattable = XLIFFEditorImplWithNatTable.getCurrent();
				if (nattable == null) {
					MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
							Messages.getString("plugin.PluginConfigManage.msg2"));
					return;
				}

				XLFHandler handler = nattable.getXLFHandler();
				List<String> selectRowIds = nattable.getSelectedRowIds();
				if (selectRowIds.size() <= 0) {
					MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
							Messages.getString("plugin.PluginConfigManage.msg3"));
					return;
				}
				if (selectRowIds.size() > 1) {
					MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
							Messages.getString("plugin.PluginConfigManage.msg4"));
				}

				String rowId = selectRowIds.get(0);
				sendSegment(bean, handler, rowId);
				// 执行后返回的文件
				String returnContent = runPlugin(bean);
				// 如果返回为交换文件，则更新当前文本段
				if (bean.getInput().equals(PluginConstants.EXCHANGEFILE)) {
					handler.updateAndSave(rowId, "", returnContent);
					// 更新完后，要刷新界面。此处未做，滞留。
				}
			}

			// 当输出(进程)为当前文档时
			if (bean.getOutput().equals(PluginConstants.DOCUMENT)) {
				XLFHandler handler;
				IFile selectIFile = null;
				// 先检查是否有已经打开的文件，若没有，退出插件执行
				XLIFFEditorImplWithNatTable nattable = XLIFFEditorImplWithNatTable.getCurrent();
				if (nattable == null) {
					// 如果当前没有打开的文件，那么获取左边导航框中选中的文件
					ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
							.getSelection();
					if (selection == null || selection.isEmpty() || !(selection instanceof StructuredSelection)) {
						MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
								Messages.getString("plugin.PluginConfigManage.msg5"));
						return;
					}

					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Iterator<Object> selectIt = structuredSelection.iterator();
					if (selectIt.hasNext()) {
						Object object = selectIt.next();
						if (object instanceof IFile) {
							selectIFile = (IFile) object;
							String fileExtension = selectIFile.getFileExtension();
							if (!CommonFunction.validXlfExtension(fileExtension)) {
								MessageDialog.openInformation(shell,
										Messages.getString("plugin.PluginConfigManage.msgTitle"),
										Messages.getString("plugin.PluginConfigManage.msg5"));
								return;
							}
						}
					}

					handler = new XLFHandler();
					// 打开该xliff文件
					Map<String, Object> resultMap = handler.openFile(selectIFile.getLocation().toOSString());
					if (resultMap == null
							|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap
									.get(Constant.RETURNVALUE_RESULT)) {
						MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
								Messages.getString("plugin.PluginConfigManage.msg5"));
						return;
					}
				} else {
					handler = nattable.getXLFHandler();
					selectIFile = ResourceUtil.getFile(nattable.getEditorInput());
					// IEditorInput fileInput = nattable.getEditorInput();
				}

				sendDocument(bean, selectIFile);
				if (bean.getInput().equals(PluginConstants.DOCUMENT)) {
					// 重新解析该文件
					Map<String, Object> resultMap = handler.openFile(selectIFile.getLocation().toOSString());
					if (resultMap == null
							|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap
									.get(Constant.RETURNVALUE_RESULT)) {
						MessageDialog.openError(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
								Messages.getString("plugin.PluginConfigManage.msg6"));
						return;
					}
				}
			}

			// 当输出(进程)为空时
			if (bean.getOutput().equals(PluginConstants.NONE)) {
				runPlugin(bean);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.openError(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
					Messages.getString("plugin.PluginConfigManage.msg7"));
		}
	}

	/**
	 * 将当前值传送到目标文件或者插件(针对进程为segment的，因此一定要有交换文件)
	 * @param bean
	 *            ;
	 */
	public void sendSegment(PluginConfigBean bean, XLFHandler handler, String selectedRowId) {
		// 如果交换文件并没有设置 ，则退出程序
		String outputPath = bean.getOutputPath();
		if (outputPath == null || outputPath.equals("")) {
			MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
					Messages.getString("plugin.PluginConfigManage.msg8"));
			return;
		}
		// 备注：在发送内容之前，没有进行对当前文本段的保存，这是一个滞留问题

		// 获取选中的trans-unit节点的完整内容
		String transUnitStr = handler.getTUFragByRowId(selectedRowId);
		FileOutputStream output;
		try {
			output = new FileOutputStream(outputPath);
			output.write(transUnitStr.getBytes("UTF-8"));
			output.close();
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	public void sendDocument(PluginConfigBean bean, IFile curXliff) {
		String curXliffLocation = curXliff.getLocation().toOSString();
		File f = new File(curXliffLocation);
		if (!f.exists()) {
			MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
					MessageFormat.format(Messages.getString("plugin.PluginConfigManage.msg9"), curXliff.getFullPath()
							.toOSString()));
			return;
		}

		String commandLine = bean.getCommandLine();

		String[] cmdArray = { commandLine, curXliffLocation };
		try {
			Process pluginProcess = Runtime.getRuntime().exec(cmdArray);
			if (bean.getInput().equals(PluginConstants.EXCHANGEFILE)) {
				pluginProcess.waitFor();
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * 开始执行程序
	 * @param bean
	 * @return
	 * @throws Exception
	 *             ;
	 */
	public String runPlugin(PluginConfigBean bean) throws Exception {

		String commandLine = bean.getCommandLine();
		if (commandLine == null || "".equals(commandLine)) {
			MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
					Messages.getString("plugin.PluginConfigManage.msg1"));
			return null;
		}
		String output = bean.getOutput();
		String input = bean.getInput();
		if (output.equals(PluginConstants.NONE)) {
			Runtime.getRuntime().exec(commandLine);
			return null;
		} else if (output.equals(PluginConstants.SEGMENT)) {
			String fileName = bean.getOutputPath();
			if (fileName == null || fileName.equals("")) {
				MessageDialog.openInformation(shell, Messages.getString("plugin.PluginConfigManage.msgTitle"),
						Messages.getString("plugin.PluginConfigManage.msg8"));
				return null;
			}

			String[] cmdArray = { commandLine, fileName };
			Process pluginProcess = Runtime.getRuntime().exec(cmdArray);

			if (input.equals(PluginConstants.EXCHANGEFILE)) {
				pluginProcess.waitFor();
				InputStreamReader inReader = new InputStreamReader(new FileInputStream(fileName));
				BufferedReader b = new BufferedReader(inReader);
				StringBuffer responseSB = new StringBuffer();
				String line;
				while ((line = b.readLine()) != null) {
					responseSB.append(line + "\n");
				}
				inReader.close();
				b.close();
				return responseSB.toString();
			}
		}
		return "";
	}

}
