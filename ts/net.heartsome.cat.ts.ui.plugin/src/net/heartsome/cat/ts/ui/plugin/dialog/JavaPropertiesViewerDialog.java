package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.Vector;

import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.TableViewerLabelProvider;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 插件java properties viewer
 * @author robert 2012-03-10
 * @version
 * @since JDK1.6 备注：--robert undone (没有完成帮助文档的完善);
 */
public class JavaPropertiesViewerDialog extends Dialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaPropertiesViewerDialog.class);
	
	private TableViewer tableViewer;
	private Table table;
	private String openFilePath;
	private String imagePath;

	public JavaPropertiesViewerDialog(Shell parentShell) {
		super(parentShell);
		openFilePath = PluginUtil.getAbsolutePath(PluginConstants.PIC_OPEN_CSV_PATH);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.JavaPropertiesViewerDialog.title"));
		imagePath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_PROERTIESVIEWER_PATH);
		newShell.setImage(new Image(Display.getDefault(), imagePath));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// super.createButtonsForButtonBar(parent);
		parent.dispose();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(800, 700).applyTo(tparent);
		GridLayoutFactory.fillDefaults().spacing(0, 0).extendedMargins(8, 8, 0, 8).applyTo(tparent);
		createMenu(tparent);
		createToolBar(tparent);
		createTableViewer(tparent);
		
		tparent.layout();
		getShell().layout();
		return tparent;
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
		fileItem.setText(Messages.getString("dialog.JavaPropertiesViewerDialog.fileMenu"));
		fileItem.setMenu(fileMenu);

		MenuItem openFileItem = new MenuItem(fileMenu, SWT.PUSH);
		openFileItem.setText(Messages.getString("dialog.JavaPropertiesViewerDialog.openFileItem"));
		openFileItem.setImage(new Image(Display.getDefault(), openFilePath));
		openFileItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFile();
			}
		});
		new MenuItem(fileMenu, SWT.SEPARATOR);

		MenuItem quitItem = new MenuItem(fileMenu, SWT.PUSH);
		quitItem.setText(Messages.getString("dialog.JavaPropertiesViewerDialog.quitItem"));
		quitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});

		// 帮助菜单
		Menu helpMenu = new Menu(menuBar);
		MenuItem helpItem = new MenuItem(menuBar, SWT.CASCADE);
		helpItem.setText(Messages.getString("dialog.JavaPropertiesViewerDialog.helpMenu"));
		helpItem.setMenu(helpMenu);

		MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(Messages.getString("dialog.JavaPropertiesViewerDialog.aboutItem"));
		String aboutPath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_PROERTIESVIEWER_MENU_PATH);
		aboutItem.setImage(new Image(Display.getDefault(), aboutPath));
		aboutItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(getShell(), Messages
						.getString("dialog.JavaPropertiesViewerDialog.aboutItemName"), imagePath, Messages
						.getString("dialog.JavaPropertiesViewerDialog.version"));
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void createToolBar(Composite tparent) {
		Composite toolBarCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(toolBarCmp);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(toolBarCmp);

		ToolBar toolBar = new ToolBar(toolBarCmp, SWT.NO_FOCUS | SWT.FLAT);
		ToolItem openItem = new ToolItem(toolBar, SWT.PUSH);
		openItem.setToolTipText(Messages.getString("dialog.JavaPropertiesViewerDialog.toolBar"));
		openItem.setImage(new Image(Display.getDefault(), openFilePath));

		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFile();
			}
		});

	}

	private void createTableViewer(Composite tparent) {
		tableViewer = new TableViewer(tparent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI
				| SWT.BORDER);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		String[] columnNames = new String[] { Messages.getString("dialog.JavaPropertiesViewerDialog.columnNames1"),
				Messages.getString("dialog.JavaPropertiesViewerDialog.columnNames2") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);
		}

		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.48, 0.48, };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});
	}

	/**
	 * 打开文件
	 */
	private void openFile() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		String[] extensions = { "*.properties", "*.*" };
		String[] names = { Messages.getString("dialog.JavaPropertiesViewerDialog.names1"),
				Messages.getString("dialog.JavaPropertiesViewerDialog.names2") };
		fd.setFilterNames(names);
		fd.setFilterExtensions(extensions);
		String fileLocation = fd.open();
		if (fileLocation == null) {
			return;
		}
		PropertyResourceBundle bundle;
		try {
			bundle = new PropertyResourceBundle(new FileInputStream(fileLocation));
			Vector<String> vector = getKeys(fileLocation);
			Iterator<String> keys = vector.iterator();
			List<String[]> data = new LinkedList<String[]>();
			while (keys.hasNext()) {
				String key = keys.next();
				String value = bundle.getString(key);
				data.add(new String[] { key, value });
			}
			tableViewer.setInput(data);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	private Vector<String> getKeys(String inputFile) throws IOException {
		FileInputStream stream = new FileInputStream(inputFile);
		InputStreamReader input = new InputStreamReader(stream, "UTF-8"); // ISO8859-1
		BufferedReader buffer = new BufferedReader(input);
		Vector<String> result = new Vector<String>();
		String line;
		while ((line = buffer.readLine()) != null) {

			if (line.trim().length() == 0) {
				// no text in this line
				// segment separator
			} else if (line.trim().startsWith("#")) {
				// this line is a comment
				// send to skeleton
			} else {
				String tmp = line;
				if (line.trim().endsWith("\\")) {
					do {
						line = buffer.readLine();
						tmp += "\n" + line;
					} while (line != null && line.trim().endsWith("\\"));
				}
				int index = tmp.indexOf("=");
				if (index != -1) {
					String key = tmp.substring(0, index).trim();
					result.add(key);
				} else {
					// this line may be wrong,
					// ignore and continue
				}
			}
		}
		input.close();
		return result;
	}
}
