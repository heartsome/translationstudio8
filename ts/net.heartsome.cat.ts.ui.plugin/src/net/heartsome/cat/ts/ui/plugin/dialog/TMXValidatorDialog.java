package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.StringTokenizer;

import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.TMXValidator;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 插件TMX Validator，
 * @author robert 2012-03-14
 * @version
 * @since JDK1.6
 */
public class TMXValidatorDialog extends Dialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TMXValidatorDialog.class);
	
	private String openFilePath;
	private String clearCharPath;
	private StyledText styledText;
	private Color red;

	private Cursor cursorWait = new Cursor(Display.getDefault(), SWT.CURSOR_WAIT);
	private Cursor cursorArrow = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
	
	private String imagePath;
	
	public TMXValidatorDialog(Shell parentShell) {
		super(parentShell);
		openFilePath = PluginUtil.getAbsolutePath(PluginConstants.PIC_OPEN_CSV_PATH);
		clearCharPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_clearChar_PATH);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.TMXValidatorDialog.title")); //$NON-NLS-1$
		imagePath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_TMXVALIDATOR_PATH);
		newShell.setImage(new Image(Display.getDefault(), imagePath));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.dispose();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(500, 450).applyTo(tparent);
		GridLayoutFactory.fillDefaults().spacing(0, 0).extendedMargins(8, 8, 0, 8).applyTo(tparent);

		createMenu(tparent);
		createToolBar(tparent);

		styledText = new StyledText(tparent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(100, 100).grab(true, true).applyTo(styledText);
		styledText.setText("");

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
		fileItem.setText(Messages.getString("dialog.TMXValidatorDialog.fileMenu"));
		fileItem.setMenu(fileMenu);

		MenuItem openFileItem = new MenuItem(fileMenu, SWT.PUSH);
		openFileItem.setText(Messages.getString("dialog.TMXValidatorDialog.openFileItem"));
		openFileItem.setImage(new Image(Display.getDefault(), openFilePath));
		openFileItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFile();
			}
		});

		MenuItem clearCharItem = new MenuItem(fileMenu, SWT.PUSH);
		clearCharItem.setText(Messages.getString("dialog.TMXValidatorDialog.clearCharItem"));
		clearCharItem.setImage(new Image(Display.getDefault(), clearCharPath));
		clearCharItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cleanCharacters();
			}

		});

		new MenuItem(fileMenu, SWT.SEPARATOR);

		MenuItem quitItem = new MenuItem(fileMenu, SWT.PUSH);
		quitItem.setText(Messages.getString("dialog.TMXValidatorDialog.quitItem"));
		quitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});

		// 帮助菜单
		Menu helpMenu = new Menu(menuBar);
		MenuItem helpItem = new MenuItem(menuBar, SWT.CASCADE);
		helpItem.setText(Messages.getString("dialog.TMXValidatorDialog.helpMenu"));
		helpItem.setMenu(helpMenu);

		MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(Messages.getString("dialog.TMXValidatorDialog.aboutItem"));
		String aboutPath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_TMXVALIDATOR_MENU_PATH);
		aboutItem.setImage(new Image(Display.getDefault(), aboutPath));
		aboutItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(getShell(), Messages
						.getString("dialog.TMXValidatorDialog.aboutItemName"), imagePath, Messages
						.getString("dialog.TMXValidatorDialog.version"));
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void createToolBar(Composite tparent) {
		Composite toolBarCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(toolBarCmp);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(toolBarCmp);

		ToolBar toolBar = new ToolBar(toolBarCmp, SWT.NO_FOCUS | SWT.FLAT);
		ToolItem openItem = new ToolItem(toolBar, SWT.PUSH);
		openItem.setToolTipText(Messages.getString("dialog.TMXValidatorDialog.openItem"));
		openItem.setImage(new Image(Display.getDefault(), openFilePath));

		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFile();
			}
		});

		ToolItem clearCharsItem = new ToolItem(toolBar, SWT.PUSH);
		clearCharsItem.setImage(new Image(Display.getDefault(), clearCharPath));
		clearCharsItem.setToolTipText(Messages.getString("dialog.TMXValidatorDialog.clearCharsItem"));
		clearCharsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cleanCharacters();
			}
		});
	}

	/**
	 * 打开文件
	 */
	private void openFile() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		String[] extensions = { "*.tmx", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(extensions);
		String[] names = { Messages.getString("dialog.TMXValidatorDialog.names1"),
				Messages.getString("dialog.TMXValidatorDialog.names2") };
		fd.setFilterNames(names);
		String name = fd.open();
		if (name != null) {
			validate(name);
		}
	}

	private void validate(String tmxLocation) {
		getShell().setCursor(cursorWait);
		styledText.setText("");
		TMXValidator validator = new TMXValidator(tmxLocation, getShell());
		validator.validate(tmxLocation, styledText);
		getShell().setCursor(cursorArrow);

	}

	private void cleanCharacters() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		String[] extensions = { "*.tmx", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(extensions);
		String[] names = { Messages.getString("dialog.TMXValidatorDialog.names1"),
				Messages.getString("dialog.TMXValidatorDialog.names2") };
		fd.setFilterNames(names);
		String name = fd.open();
		if (name != null) {
			red = Display.getDefault().getSystemColor(SWT.COLOR_RED);

			styledText.setText("");
			styledText.append(Messages.getString("dialog.TMXValidatorDialog.styledText1"));
			getShell().setCursor(cursorWait);
			try {

			} catch (Exception e) {
				LOGGER.error("", e);
				String errorTip = e.getMessage();
				if (errorTip == null) {
					errorTip = MessageFormat.format(Messages.getString("dialog.TMXValidatorDialog.msg1"), name);
				}
				StyleRange range = new StyleRange(styledText.getText().length(), errorTip.length(), red, null);
				styledText.append(errorTip);
				styledText.setStyleRange(range);
			}

			styledText.append(Messages.getString("dialog.TMXValidatorDialog.styledText2"));
			getShell().setCursor(cursorArrow);
		}
	}

	void clean(String name) throws Exception {
		FileInputStream stream = new FileInputStream(name);
		String encoding = getXMLEncoding(name);
		InputStreamReader input = new InputStreamReader(stream, encoding);
		BufferedReader buffer = new BufferedReader(input);
		FileOutputStream output = new FileOutputStream(name + ".tmp"); //$NON-NLS-1$
		String line = buffer.readLine();
		while (line != null) {
			line = validChars(line) + "\n"; //$NON-NLS-1$
			output.write(line.getBytes(encoding));
			line = buffer.readLine();
		}
		output.close();
		input.close();
		String backup = name + ".bak"; //$NON-NLS-1$
		if (name.indexOf(".") != -1 && name.lastIndexOf(".") < name.length()) {
			backup = name.substring(0, name.lastIndexOf(".")) + ".~" + name.substring(name.lastIndexOf(".") + 1);
		}
		File f = new File(backup);
		if (f.exists()) {
			f.delete();
		}
		File original = new File(name);
		original.renameTo(f);
		File ok = new File(name + ".tmp"); //$NON-NLS-1$
		original = null;
		original = new File(name);
		ok.renameTo(original);
	}

	private static String getXMLEncoding(String fileName) throws Exception {
		// return UTF-8 as default
		String result = "UTF-8"; //$NON-NLS-1$
		// check if there is a BOM (byte order mark)
		// at the start of the document
		FileInputStream inputStream = new FileInputStream(fileName);
		byte[] array = new byte[2];
		inputStream.read(array);
		inputStream.close();
		byte[] lt = "<".getBytes(); //$NON-NLS-1$
		byte[] feff = { -1, -2 };
		byte[] fffe = { -2, -1 };
		if (array[0] != lt[0]) {
			// there is a BOM, now check the order
			if (array[0] == fffe[0] && array[1] == fffe[1]) {
				return "UTF-16BE"; //$NON-NLS-1$
			}
			if (array[0] == feff[0] && array[1] == feff[1]) {
				return "UTF-16LE"; //$NON-NLS-1$
			}
		}
		// check declared encoding
		FileReader input = new FileReader(fileName);
		BufferedReader buffer = new BufferedReader(input);
		String line = buffer.readLine();
		input.close();
		if (line.startsWith("<?")) { //$NON-NLS-1$
			line = line.substring(2, line.indexOf("?>")); //$NON-NLS-1$
			line = line.replaceAll("\'", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.startsWith("encoding")) { //$NON-NLS-1$
					result = token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		if (result.equals("utf-8")) { //$NON-NLS-1$
			result = "UTF-8"; //$NON-NLS-1$
		}
		return result;
	}

	private String validChars(String input) {
		// Valid: #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
		// [#x10000-#x10FFFF]
		// Discouraged: [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF]
		//
		StringBuffer buffer = new StringBuffer();
		char c;
		int length = input.length();
		for (int i = 0; i < length; i++) {
			c = input.charAt(i);
			if ((c == '\t' || c == '\n' || c == '\r' || (c >= '\u0020' && c <= '\uD7DF') || (c >= '\uE000' && c <= '\uFFFD'))) {
				// normal character
				buffer.append(c);
			} else if ((c >= '\u007F' && c <= '\u0084') || (c >= '\u0086' && c <= '\u009F')
					|| (c >= '\uFDD0' && c <= '\uFDDF')) {
				// Control character
				buffer.append("&#x" + Integer.toHexString(c) + ";"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if ((c >= '\uDC00' && c <= '\uDFFF') || (c >= '\uD800' && c <= '\uDBFF')) {
				// Multiplane character
				buffer.append(input.substring(i, i + 1));
			}
		}
		return buffer.toString();
	}
	@Override
	public boolean close() {
		if(cursorWait != null && !cursorWait.isDisposed()){
			cursorWait.dispose();
		}
		if(cursorArrow != null && !cursorArrow.isDisposed()){
			cursorArrow.dispose();
		}
		return super.close();
	}
}
