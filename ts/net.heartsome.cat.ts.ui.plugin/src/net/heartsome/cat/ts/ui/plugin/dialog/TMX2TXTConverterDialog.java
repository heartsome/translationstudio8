package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;
import net.heartsome.cat.ts.util.ProgressIndicatorManager;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * 插件TMX to TXT Converter
 * @author robert 2012-03-10
 * @version
 * @since JDK1.6 备注：--robert undone (没有完成帮助文档的完善，关于获取seg节点下的内容的问题，VTD中有BUG未解决);
 */
public class TMX2TXTConverterDialog extends Dialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TMX2TXTConverterDialog.class);
	
	private Text tmxTxt;
	private Text txtTxt;
	private VTDNav vn;
	private FileOutputStream output;
	/** TMX文件TU节点的个数 */
	private int tuNodesCount;
	/** 进度条的前进刻度 */
	private int workInterval = 10;
	
	private String imagePath;

	public TMX2TXTConverterDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.TMX2TXTConverterDialog.title"));
		imagePath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_TMX2TXTCONVERTER_PATH);
		newShell.setImage(new Image(Display.getDefault(), imagePath));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setText(Messages.getString("dialog.TMX2TXTConverterDialog.ok"));
		getButton(IDialogConstants.CANCEL_ID).setText(Messages.getString("dialog.TMX2TXTConverterDialog.cancel"));
		parent.layout();
		
		getDialogArea().getParent().layout();
		getShell().layout();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(450, 190).applyTo(tparent);
		createMenu(tparent);
		createContent(tparent);
		
		tparent.layout();
		getShell().layout();
		return tparent;
	}

	@Override
	protected void okPressed() {
		convert();
		super.okPressed();
	}

	private void createMenu(Composite tparent) {
		Menu menuBar = new Menu(getShell(), SWT.BAR);
		getShell().setMenuBar(menuBar);
		getShell().pack();

		Rectangle screenSize = Display.getDefault().getClientArea();
		Rectangle frameSize = getShell().getBounds();
		getShell().setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		// 文件菜单
		Menu fileMenu = new Menu(menuBar);
		MenuItem fileItem = new MenuItem(menuBar, SWT.CASCADE);
		fileItem.setText(Messages.getString("dialog.TMX2TXTConverterDialog.fileMenu"));
		fileItem.setMenu(fileMenu);

		MenuItem quitItem = new MenuItem(fileMenu, SWT.PUSH);
		quitItem.setText(Messages.getString("dialog.TMX2TXTConverterDialog.quitItem"));
		quitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});

		// 帮助菜单
		Menu helpMenu = new Menu(menuBar);
		MenuItem helpItem = new MenuItem(menuBar, SWT.CASCADE);
		helpItem.setText(Messages.getString("dialog.TMX2TXTConverterDialog.helpMenu"));
		helpItem.setMenu(helpMenu);

		MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(Messages.getString("dialog.TMX2TXTConverterDialog.aboutItem"));
		String aboutPath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_TMX2TXTCONVERTER_MENU_PATH);
		aboutItem.setImage(new Image(Display.getDefault(), aboutPath));
		aboutItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(getShell(), Messages
						.getString("dialog.TMX2TXTConverterDialog.aboutItemName"), imagePath, Messages
						.getString("dialog.TMX2TXTConverterDialog.version"));
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void createContent(Composite tparent) {
		Composite contentCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(contentCmp);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(contentCmp);

		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);

		Label tmxLbl = new Label(contentCmp, SWT.NONE);
		tmxLbl.setText(Messages.getString("dialog.TMX2TXTConverterDialog.tmxLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(tmxLbl);

		tmxTxt = new Text(contentCmp, SWT.BORDER);
		tmxTxt.setLayoutData(textData);

		Button tmxBrowseBtn = new Button(contentCmp, SWT.NONE);
		tmxBrowseBtn.setText(Messages.getString("dialog.TMX2TXTConverterDialog.tmxBrowseBtn"));
		tmxBrowseBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selecteTMXFile();
			}
		});

		Label txtLbl = new Label(contentCmp, SWT.NONE);
		txtLbl.setText(Messages.getString("dialog.TMX2TXTConverterDialog.txtLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(txtLbl);

		txtTxt = new Text(contentCmp, SWT.BORDER);
		txtTxt.setLayoutData(textData);

		Button txtBrowseBtn = new Button(contentCmp, SWT.NONE);
		txtBrowseBtn.setText(Messages.getString("dialog.TMX2TXTConverterDialog.txtBrowseBtn"));
		txtBrowseBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selecteTXTFile();
			}
		});

	}

	/**
	 * 选择TMX文件
	 */
	private void selecteTMXFile() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		String[] extensions = { "*.tmx", "*.*" };
		fd.setFilterExtensions(extensions);
		String[] names = { Messages.getString("dialog.TMX2TXTConverterDialog.TMXNames1"),
				Messages.getString("dialog.TMX2TXTConverterDialog.TMXNames2") };
		fd.setFilterNames(names);
		String file = fd.open();
		if (file != null) {
			tmxTxt.setText(file);
			if (txtTxt.getText().equals("")) {
				txtTxt.setText(file.substring(0, file.lastIndexOf(".")) + ".txt");
			}
		}
	}

	/**
	 * 选择TXT文件 ;
	 */
	private void selecteTXTFile() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		String[] extensions = { "*.txt", "*.*" };
		fd.setFilterExtensions(extensions);
		String[] names = { Messages.getString("dialog.TMX2TXTConverterDialog.TXTNames1"),
				Messages.getString("dialog.TMX2TXTConverterDialog.TXTNames2") };
		fd.setFilterNames(names);
		String file = fd.open();
		if (file != null) {
			txtTxt.setText(file);
		}
	}

	/**
	 * 开始转换
	 */
	private void convert() {
		final String tmxLocation = tmxTxt.getText();
		if (tmxLocation.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TMX2TXTConverterDialog.msgTitle"),
					Messages.getString("dialog.TMX2TXTConverterDialog.msg1"));
			return;
		}
		final String txtLocation = txtTxt.getText();
		if (txtLocation.equals("")) { //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TMX2TXTConverterDialog.msgTitle"),
					Messages.getString("dialog.TMX2TXTConverterDialog.msg2"));
			return;
		}

		File txtFile = new File(txtLocation);
		if (txtFile.exists()) {
			boolean response = MessageDialog.openConfirm(getShell(),
					Messages.getString("dialog.TMX2TXTConverterDialog.msgTitle2"),
					MessageFormat.format(Messages.getString("dialog.TMX2TXTConverterDialog.msg3"), txtLocation));
			if (!response) {
				return;
			}
		} else if (!txtFile.getParentFile().exists()) {
			txtFile.getParentFile().mkdirs();
		}

		Job job = new Job(Messages.getString("dialog.TMX2TXTConverterDialog.job")) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("dialog.TMX2TXTConverterDialog.task"), 10);
				// 先解析文件，花费一格
				int openResult = openFile(tmxLocation, monitor);
				if (openResult == 0) {
					return Status.CANCEL_STATUS;
				} else if (openResult == -2) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(getShell(),
									Messages.getString("dialog.TMX2TXTConverterDialog.msgTitle"),
									Messages.getString("dialog.TMX2TXTConverterDialog.msg4"));
						}
					});
					return Status.CANCEL_STATUS;
				} else if (openResult == -1) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(getShell(),
									Messages.getString("dialog.TMX2TXTConverterDialog.msgTitle"),
									Messages.getString("dialog.TMX2TXTConverterDialog.msg5"));
						}
					});
					return Status.CANCEL_STATUS;
				}

				if (!validVersion(tmxLocation)) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(getShell(),
									Messages.getString("dialog.TMX2TXTConverterDialog.msgTitle"),
									Messages.getString("dialog.TMX2TXTConverterDialog.msg6"));
						}
					});
					return Status.CANCEL_STATUS;
				}

				if ((tuNodesCount = getTUCount(tmxLocation)) <= 0) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(getShell(),
									Messages.getString("dialog.TMX2TXTConverterDialog.msgTitle"),
									Messages.getString("dialog.TMX2TXTConverterDialog.msg7"));
						}
					});
					return Status.CANCEL_STATUS;
				}

				try {
					output = new FileOutputStream(txtLocation);
					byte[] bom = new byte[3];
					bom[0] = (byte) 0xEF;
					bom[1] = (byte) 0xBB;
					bom[2] = (byte) 0xBF;
					output.write(bom);
					writeHeader();
					monitor.worked(1);

					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 8,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
					return processTU(tmxLocation, txtLocation, subMonitor);
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.error("", e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		
		// 当程序退出时，检测当前　job 是否正常关闭
		CommonFunction.jobCantCancelTip(job);
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void running(IJobChangeEvent event) {
				ProgressIndicatorManager.displayProgressIndicator();
				super.running(event);
			}
			@Override
			public void done(IJobChangeEvent event) {
				ProgressIndicatorManager.hideProgressIndicator();
				super.done(event);
			}
		});
		job.setUser(true);
		job.schedule();

	}

	/**
	 * 解析文件（同时操作进度条）
	 * @param file
	 * @param monitor
	 * @param totalWork
	 * @return ;
	 */
	private int openFile(String tmxLocation, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			// 解析文件并获取索引
			VTDGen vg = new VTDGen();
			if (vg.parseFile(tmxLocation, true)) {
				vn = vg.getNav();
				if (monitor.isCanceled()) {
					return 0; // 终止程序的执行
				}

				AutoPilot ap = new AutoPilot(vn);
				ap.selectXPath("/tmx");
				if (ap.evalXPath() == -1) {
					return -2; // 解析的文件不符合TMX标准
				}
				monitor.worked(1);
				return 1; // TMX文件解析成功
			} else {
				return -1; // 解析失败
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("", e);
		}
		return -1;
	}

	/**
	 * 验证TMX文件是不是1.4版本的，如果不是，退出程序
	 * @param txmLocation
	 * @return ;
	 */
	private boolean validVersion(String tmxLocation) {
		Assert.isNotNull(vn,
				MessageFormat.format(Messages.getString("dialog.TMX2TXTConverterDialog.msg8"), tmxLocation));
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/tmx");
			if (ap.evalXPath() != -1) {
				int index;
				if ((index = vn.getAttrVal("version")) != -1) {
					if ("1.4".equals(vn.toString(index))) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("", e);
		}
		return false;
	}

	private void writeHeader() throws UnsupportedEncodingException, IOException {
		writeString("<TWBExportFile version=\"7.0\" generator=\"TMX to TXT Converter\" build=\"1.0-1\">\r\n"); //$NON-NLS-1$
		writeString("<RTF Preamble>\r\n"); //$NON-NLS-1$
		writeString("<FontTable>\r\n"); //$NON-NLS-1$
		writeString("{\\fonttbl \r\n"); //$NON-NLS-1$
		writeString("{\\f1 \\fmodern\\fprq1 \\fcharset0 Courier New;}\r\n"); //$NON-NLS-1$
		writeString("{\\f2 \\fswiss\\fprq2 \\fcharset0 Arial;}\r\n"); //$NON-NLS-1$
		writeString("{\\f3 \\fcharset128 MS Mincho;}}\r\n"); //$NON-NLS-1$
		writeString("{\\f4 \\fcharset134 SimSun;}\r\n"); //$NON-NLS-1$
		writeString("{\\f5 \\fcharset136 MingLiU;}\r\n"); //$NON-NLS-1$
		writeString("{\\f6 \\fcharset129 Gulim;}\r\n"); //$NON-NLS-1$
		writeString("{\\f7 \\froman\\fprq2 \\fcharset238 Times New Roman CE;}\r\n"); //$NON-NLS-1$
		writeString("{\\f8 \\froman\\fprq2 \\fcharset204 Times New Roman Cyr;}\r\n"); //$NON-NLS-1$
		writeString("{\\f9 \\froman\\fprq2 \\fcharset161 Times New Roman Greek;}\r\n"); //$NON-NLS-1$
		writeString("{\\f10 \\froman\\fprq2 \\fcharset162 Times New Roman Tur;}\r\n"); //$NON-NLS-1$
		writeString("{\\f11 \\froman\\fprq2 \\fcharset177 Times New Roman (Hebrew);}\r\n"); //$NON-NLS-1$
		writeString("{\\f12 \\froman\\fprq2 \\fcharset178 Times New Roman (Arabic);}\r\n"); //$NON-NLS-1$
		writeString("{\\f13 \\froman\\fprq2 \\fcharset186 Times New Roman Baltic;}\r\n"); //$NON-NLS-1$
		writeString("{\\f14 \\froman\\fprq2 \\fcharset222 Angsana New;}\r\n"); //$NON-NLS-1$
		writeString("<StyleSheet>\r\n"); //$NON-NLS-1$
		writeString("{\\stylesheet \r\n"); //$NON-NLS-1$
		writeString("{\\St \\s0 {\\StN Normal}}\r\n"); //$NON-NLS-1$
		writeString("{\\St \\cs1 {\\StB \\v\\f1\\fs24\\sub\\cf12 }{\\StN tw4winMark}}\r\n"); //$NON-NLS-1$
		writeString("{\\St \\cs2 {\\StB \\cf4\\fs40\\f1 }{\\StN tw4winError}}\r\n"); //$NON-NLS-1$
		writeString("{\\St \\cs3 {\\StB \\f1\\cf11\\lang1024 }{\\StN tw4winPopup}}\r\n"); //$NON-NLS-1$
		writeString("{\\St \\cs4 {\\StB \\f1\\cf10\\lang1024 }{\\StN tw4winJump}}\r\n"); //$NON-NLS-1$
		writeString("{\\St \\cs5 {\\StB \\f1\\cf15\\lang1024 }{\\StN tw4winExternal}}\r\n"); //$NON-NLS-1$
		writeString("{\\St \\cs6 {\\StB \\f1\\cf6\\lang1024 }{\\StN tw4winInternal}}\r\n"); //$NON-NLS-1$
		writeString("{\\St \\cs7 {\\StB \\cf2 }{\\StN tw4winTerm}}\r\n"); //$NON-NLS-1$
		writeString("{\\St \\cs8 {\\StB \\f1\\cf13\\lang1024 }{\\StN DO_NOT_TRANSLATE}}}\r\n"); //$NON-NLS-1$
		writeString("</RTF Preamble>\r\n"); //$NON-NLS-1$
	}

	private void writeString(String string) throws UnsupportedEncodingException, IOException {
		output.write(string.getBytes("UTF-8"));
	}

	/**
	 * 开始获取tmx中的tu节点进行添加到TXT文件中
	 * @param txtLocation
	 * @param monitor
	 *            ;
	 */
	private IStatus processTU(String tmxLocation, String txtLocation, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", tuNodesCount % workInterval == 0 ? tuNodesCount / workInterval : tuNodesCount
				/ workInterval + 1);

		try {

			int travelIndex = 0;

			AutoPilot ap = new AutoPilot(vn);
			AutoPilot tuvAp = new AutoPilot(vn);
			AutoPilot segAp = new AutoPilot(vn);
			AutoPilot segChildAp = new AutoPilot(vn);
			Assert.isNotNull(vn,
					MessageFormat.format(Messages.getString("dialog.TMX2TXTConverterDialog.msg8"), tmxLocation));
			ap.selectXPath("/tmx/body/tu");
			segAp.selectXPath("./seg");
			segChildAp.selectXPath("./*");
			while (ap.evalXPath() != -1) {
				travelIndex++;

				writeString("<TrU>\r\n");
				String creationdate = getAttributeValue("creationdate", "");
				if (!creationdate.equals("")) {
					writeString("<CrD>" + creationdate.substring(6, 8) + creationdate.substring(4, 6)
							+ creationdate.substring(0, 4) + ", " + creationdate.substring(9, 11) + ":"
							+ creationdate.substring(11, 13) + ":" + creationdate.substring(13, 15) + "\r\n");
				}

				String creationid = getAttributeValue("creationid", "");
				if (creationid.equals("")) {
					writeString("<CrU>" + System.getProperty("user.name").replaceAll("\\s", "") + "\r\n");
				} else {
					writeString("<CrU>" + creationid + "\r\n");
				}

				String changedate = getAttributeValue("changedate", "");
				if (!changedate.equals("")) {
					writeString("<ChD>" + changedate.substring(6, 8) + changedate.substring(4, 6)
							+ changedate.substring(0, 4) + ", " + changedate.substring(9, 11) + ":"
							+ changedate.substring(11, 13) + ":" + changedate.substring(13, 15) + "\r\n");
				}

				String changeid = getAttributeValue("changeid", "");
				if (!changeid.equals("")) {
					writeString("<ChU>" + changeid + "\r\n");
				}

				// 开始遍历TU节点的子节点：tuv
				tuvAp.selectXPath("./tuv");
				while (tuvAp.evalXPath() != -1) {
					String lang = getAttributeValue("xml:lang", null).toLowerCase();
					String font = "\\f1";
					if (lang.matches("ja.*")) {
						// Japanese
						font = "\\f3";
					}
					if (lang.matches("zh.*")) {
						// Simplified Chinese
						font = "\\f4";
					}
					if (lang.matches("zh.tw")) {
						// Traditional Chinese
						font = "\\f5";
					}
					if (lang.matches("ko.*")) {
						// Korean
						font = "\\f6";
					}
					if (lang.matches("pl.*") || lang.matches("cs.*") || lang.matches("bs.*") || lang.matches("sk.*")
							|| lang.matches("sl.*") || lang.matches("hu.*")) {
						// Eastern European
						font = "\\f7";
					}
					if (lang.matches("ru.*") || lang.matches("bg.*") || lang.matches("mk.*") || lang.matches("sr.*")
							|| lang.matches("be.*") || lang.matches("uk.*")) {
						// Russian
						font = "\\f8";
					}
					if (lang.matches("el.*")) {
						// Greek
						font = "\\f9";
					}
					if (lang.matches("tr.*")) {
						// Turkish
						font = "\\f10";
					}
					if (lang.matches("he.*") || lang.matches("yi.*")) {
						// Hebrew
						font = "\\f11";
					}
					if (lang.matches("ar.*")) {
						// Arabic
						font = "\\f12";
					}
					if (lang.matches("lv.*") || lang.matches("lt.*")) {
						// Baltic
						font = "\\f13";
					}
					if (lang.matches("th.*")) {
						// Thai
						font = "\\f14";
					}

					// 开始遍历tuv节点的子节点seg
					StringBuffer segSB = new StringBuffer();
					vn.push();
					if (segAp.evalXPath() != -1) {
						if (vn.getText() != -1) {
							segSB.append(cleanString(vn.toString(vn.getText())));
						} else {
							vn.push();
							while (segChildAp.evalXPath() != -1) {
								String nodeName = vn.toString(vn.getCurrentIndex());
								if ("ph".equals(nodeName) || "bpt".equals(nodeName) || "ept".equals(nodeName)) {
									segSB.append("{\\cs6" + font + "\\cf6\\lang1024 ");
									String value = "";
									if (vn.getText() != -1) {
										value = vn.toString(vn.getText());
									}
									segSB.append(cleanString(value));
									segSB.append("}");
								} else {
									System.out.println("vn.getTokenType(vn.getCurrentIndex()) = "
											+ vn.getTokenType(vn.getCurrentIndex()));
								}
							}
							vn.pop();
						}
						writeString("<Seg L=" + lang.toUpperCase() + ">" + segSB.toString() + "\r\n");
					}
					vn.pop();
					segAp.resetXPath();
					segChildAp.resetXPath();
				}

				writeString("</TrU>\r\n");
				if (!monitorWork(monitor, travelIndex, false)) {
					return Status.CANCEL_STATUS;
				}
			}
			if (!monitorWork(monitor, travelIndex, true)) {
				return Status.CANCEL_STATUS;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("", e);
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	/**
	 * 获取属性值
	 * @param attributeName
	 * @param defaultValue
	 * @return
	 * @throws NavException
	 *             ;
	 */
	private String getAttributeValue(String attributeName, String defaultValue) throws NavException {
		int index = vn.getAttrVal(attributeName);
		if (index != -1) {
			return vn.toString(index);
		}
		return defaultValue;
	}

	/**
	 * 获取TMX文件的TU节点的总数
	 * @return ;
	 */
	private int getTUCount(String tmxLocation) {
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn,
				MessageFormat.format(Messages.getString("dialog.TMX2TXTConverterDialog.msg8"), tmxLocation));

		try {
			ap.selectXPath("count(/tmx/body/tu)");
			return (int) ap.evalXPathToNumber();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("", e);
		}
		return 0;
	}

	private String cleanString(String value) {
		value = replaceToken(value, "\\", "\\\'5C"); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u2002', "\\enspace "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u2003', "\\emspace "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u2005', "\\qmspace "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u2014', "\\emdash "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u2013', "\\endash "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u2018', "\\lquote "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u2019', "\\rquote "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u201C', "\\ldblquote "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u201D', "\\rdblquote "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "{", "\\{"); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "}", "\\}"); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u0009', "\\tab "); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u00A0', "\\~"); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u2011', "\\_"); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "" + '\u00AD', "\\-"); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
		value = replaceToken(value, "\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return value;
	}

	private String replaceToken(String string, String token, String newText) {
		int index = string.indexOf(token);
		while (index != -1) {
			String before = string.substring(0, index);
			String after = string.substring(index + token.length());
			if (token.endsWith(" ") && after.length() > 0 && Character.isSpaceChar(after.charAt(0))) { //$NON-NLS-1$
				after = after.substring(1);
			}
			string = before + newText + after;
			index = string.indexOf(token, index + newText.length());
		}
		return string;
	}

	/**
	 * 进度条前进处理类，若返回false,则标志退出程序的执行
	 * @param monitor
	 * @param traversalTuIndex
	 * @param last
	 * @return ;
	 */
	public boolean monitorWork(IProgressMonitor monitor, int traversalTuIndex, boolean last) {
		if (last) {
			if (traversalTuIndex % workInterval != 0) {
				if (monitor.isCanceled()) {
					return false;
				}
				monitor.worked(1);
			}
		} else {
			if (traversalTuIndex % workInterval == 0) {
				if (monitor.isCanceled()) {
					return false;
				}
				monitor.worked(1);
			}
		}
		return true;
	}
}
