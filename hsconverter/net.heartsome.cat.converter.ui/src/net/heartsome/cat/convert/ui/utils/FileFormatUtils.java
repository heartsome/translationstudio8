package net.heartsome.cat.convert.ui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.heartsome.cat.common.core.IPreferenceConstants;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterBean;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/**
 * 文件格式相关辅助类。<br/>
 * 由于 R8 中采用 OSGI 服务的方式将转换器分割成单个插件，因而增加了此类来取代 R7 中的 FileFormats.java。<br/>
 * 用以在注册的转换器服务中获取转换器支持的文件类型名、文件拓展名等等。
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class FileFormatUtils {

	private static ConverterBean allFileBean = new ConverterBean("All", "All", new String[] { "*.*" });

	private static final ConverterViewModel MODEL = new ConverterViewModel(Activator.getContext(),
			Converter.DIRECTION_POSITIVE);

	/**
	 * 得到支持的类型
	 * @return ;
	 */
	public static List<ConverterBean> getSupportTypes() {
		return MODEL.getSupportTypes();
	}

	/**
	 * 得到转换器支持的所有文件类型的拓展名
	 * @return ;
	 */
	public static String[] getExtensions() {
		List<ConverterBean> list = MODEL.getSupportTypes();
		checkAutomaticOO(list);

		ArrayList<String> FileFormats = new ArrayList<String>();
		for (ConverterBean bean : list) {
			String[] extensions = bean.getExtensions();
			for (String extension : extensions) {
				FileFormats.add(extension);
			}
		}
		return FileFormats.toArray(new String[] {});
	}

	/**
	 * 得到所有文件格式
	 * @return ;
	 */
	public static String[] getFileFormats() {
		List<ConverterBean> list = MODEL.getSupportTypes();
		checkAutomaticOO(list);

		String[] FileFormats = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			FileFormats[i] = list.get(i).getDescription();
		}
		return FileFormats;
	}

	/**
	 * 得到所有文件过滤拓展名。
	 * @return ;
	 */
	public static String[] getFilterExtensions() {
		List<ConverterBean> list = MODEL.getSupportTypes();
		checkAutomaticOO(list);

		list.add(0, allFileBean);
		String[] FilterExtensions = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			FilterExtensions[i] = list.get(i).getFilterExtensions();
		}
		return FilterExtensions;
	}

	/**
	 * 得到所有文件过滤条件名。
	 * @return ;
	 */
	public static String[] getFilterNames() {
		List<ConverterBean> list = MODEL.getSupportTypes();
		checkAutomaticOO(list);

		list.add(0, allFileBean);
		String[] filterNames = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			filterNames[i] = list.get(i).getFilterNames();
		}
		return filterNames;
	}

	/**
	 * 检查是否启用 Open Office
	 */
	private static void checkAutomaticOO(List<ConverterBean> list) {
		IPreferencesService service = Platform.getPreferencesService();
		String qualifier = Activator.getDefault().getBundle().getSymbolicName();
		boolean automaticOO = service.getBoolean(qualifier, IPreferenceConstants.AUTOMATIC_OO, false, null);
		if (!automaticOO) {
			list.remove(new ConverterBean("MS Office Document to XLIFF Conveter", null));
		}
	}

	public static String[] formats = { "Adobe InDesign Interchange", //$NON-NLS-1$
			"HTML Page", //$NON-NLS-1$
			"JavaScript", //$NON-NLS-1$
			"Java Properties", //$NON-NLS-1$
			"MIF (Maker Interchange Format)", //$NON-NLS-1$
			"Microsoft Office 2007 Document", //$NON-NLS-1$
			"OpenOffice Document", //$NON-NLS-1$
			"Plain Text", //$NON-NLS-1$
			"PO (Portable Objects)", //$NON-NLS-1$
			"RC (Windows C/C++ Resources)", //$NON-NLS-1$
			"ResX (Windows .NET Resources)", //$NON-NLS-1$
			"RTF (Rich Text Format)", //$NON-NLS-1$
			"Tagged RTF", //$NON-NLS-1$
			"TTX Document", //$NON-NLS-1$
			"XML Document", //$NON-NLS-1$
			"XML (Generic)" //$NON-NLS-1$
	};

	public final static String INX = formats[0];
	public final static String HTML = formats[1];
	public final static String JS = formats[2];
	public final static String JAVA = formats[3];
	public final static String MIF = formats[4];
	public final static String OFF = formats[5];
	public final static String OO = formats[6];
	public final static String TEXT = formats[7];
	public final static String PO = formats[8];
	public final static String RC = formats[9];
	public final static String RESX = formats[10];
	public final static String RTF = formats[11];
	public final static String TRTF = formats[12];
	public final static String TTX = formats[13];
	public final static String XML = formats[14];
	public final static String XMLG = formats[15];

	public static String detectFormat(String fileName) {

		File file = new File(fileName);
		if (!file.exists()) {
			return null;
		}
		try {
			FileInputStream input = new FileInputStream(file);
			byte[] array = new byte[40960];
			input.read(array);
			input.close();
			String string = ""; //$NON-NLS-1$
			byte[] feff = { -1, -2 };
			byte[] fffe = { -2, -1 };
			// there may be a BOM, now check the order
			if (array[0] == fffe[0] && array[1] == fffe[1]) {
				string = new String(array, "UTF-16BE"); //$NON-NLS-1$
			} else if (array[0] == feff[0] && array[1] == feff[1]) {
				string = new String(array, "UTF-16LE"); //$NON-NLS-1$
			} else {
				string = new String(array);
			}
			if (string.startsWith("<MIFFile")) { //$NON-NLS-1$
				return MIF;
			}

			if (string.startsWith("{\\rtf")) { //$NON-NLS-1$
				// TODO check all header for Trados styles
				if (string.indexOf("tw4win") != -1) { //$NON-NLS-1$
					return TRTF;
				}
				return RTF;
			}

			if (string.startsWith("<?xml") || string.startsWith('\uFEFF' + "<?xml") || string.startsWith('\uFFFE' + "<?xml")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (string.indexOf("<TRADOStag ") != -1) { //$NON-NLS-1$
					return TTX;
				}
				if (string.indexOf("<docu") != -1 && string.indexOf("<?aid ") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
					return INX;
				}
				if (string.indexOf("xmlns:msdata") != -1 && string.indexOf("<root") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
					return RESX;
				}
				return XML;
			}

			if (string.startsWith("<!DOCTYPE") || string.startsWith('\uFEFF' + "<!DOCTYPE") || string.startsWith('\uFFFE' + "<!DOCTYPE")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				int index = string.indexOf("-//IETF//DTD HTML"); //$NON-NLS-1$
				if (index != -1) {
					return HTML;
				}
				index = string.indexOf("-//W3C//DTD HTML"); //$NON-NLS-1$
				if (index != -1) {
					return HTML;
				}
				return XML;
			}

			int index = string.toLowerCase().indexOf("<html"); //$NON-NLS-1$
			if (index != -1) {
				return HTML;
			}

			if (string.indexOf("msgid") != -1 && string.indexOf("msgstr") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
				return PO;
			}
			if (string.startsWith("PK")) { //$NON-NLS-1$
				// might be a zipped file
				ZipInputStream in = new ZipInputStream(new FileInputStream(file));
				ZipEntry entry = null;
				boolean found = false;
				boolean hasXML = false;
				while ((entry = in.getNextEntry()) != null) {
					if (entry.getName().equals("content.xml")) { //$NON-NLS-1$
						found = true;
						break;
					}
					if (entry.getName().matches(".*\\.xml")) { //$NON-NLS-1$
						hasXML = true;
					}
				}
				in.close();
				if (found) {
					return OO;
				}
				if (hasXML) {
					return OFF;
				}
			}
			if (string.indexOf("#include") != -1 || //$NON-NLS-1$
					string.indexOf("#define") != -1 || //$NON-NLS-1$
					string.indexOf("DIALOG") != -1 || //$NON-NLS-1$
					string.indexOf("DIALOGEX") != -1 || //$NON-NLS-1$
					string.indexOf("MENU") != -1 || //$NON-NLS-1$
					string.indexOf("MENUEX") != -1 || //$NON-NLS-1$
					string.indexOf("POPUP") != -1 || //$NON-NLS-1$
					string.indexOf("STRINGTABLE") != -1 || //$NON-NLS-1$
					string.indexOf("AUTO3STATE") != -1 || //$NON-NLS-1$
					string.indexOf("AUTOCHECKBOX") != -1 || //$NON-NLS-1$
					string.indexOf("AUTORADIOBUTTON") != -1 || //$NON-NLS-1$
					string.indexOf("CHECKBOX") != -1 || //$NON-NLS-1$
					string.indexOf("COMBOBOX") != -1 || //$NON-NLS-1$
					string.indexOf("CONTROL") != -1 || //$NON-NLS-1$
					string.indexOf("CTEXT") != -1 || //$NON-NLS-1$
					string.indexOf("DEFPUSHBUTTON") != -1 || //$NON-NLS-1$
					string.indexOf("GROUPBOX") != -1 || //$NON-NLS-1$
					string.indexOf("ICON") != -1 || //$NON-NLS-1$
					string.indexOf("LISTBOX") != -1 || //$NON-NLS-1$
					string.indexOf("LTEXT") != -1 || //$NON-NLS-1$
					string.indexOf("PUSHBOX") != -1 || //$NON-NLS-1$
					string.indexOf("PUSHBUTTON") != -1 || //$NON-NLS-1$
					string.indexOf("RADIOBUTTON") != -1 || //$NON-NLS-1$
					string.indexOf("RTEXT") != -1 || //$NON-NLS-1$
					string.indexOf("SCROLLBAR") != -1 || //$NON-NLS-1$
					string.indexOf("STATE3") != -1) //$NON-NLS-1$
			{
				return RC;
			}
		} catch (Exception e) {
			// do nothing
		}
		if (fileName.endsWith(".properties")) { //$NON-NLS-1$
			return JAVA;
		}
		if (fileName.toLowerCase().endsWith(".rc")) { //$NON-NLS-1$
			return RC;
		}
		if (fileName.endsWith(".txt")) { //$NON-NLS-1$
			return TEXT;
		}
		if (fileName.endsWith(".js")) { //$NON-NLS-1$
			return JS;
		}
		return null;
	}
}
