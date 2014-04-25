package net.heartsome.cat.convert.ui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.heartsome.cat.common.core.IPreferenceConstants;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.resource.Messages;
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
		Arrays.sort(FileFormats);
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

	public final static String INX = Messages.getString("utils.FileFormatUtils.INX");
	public final static String HTML = Messages.getString("utils.FileFormatUtils.HTML");
	/** JavaScript 脚本 (JS) */
	public final static String JS = Messages.getString("utils.FileFormatUtils.JS");
	/** Java 资源文件 (PROPERTIES) */
	public final static String JAVA = Messages.getString("utils.FileFormatUtils.JAVA");
	public final static String MIF = Messages.getString("utils.FileFormatUtils.MIF");
	public final static String OFF = Messages.getString("utils.FileFormatUtils.OFF");
	public final static String OO = Messages.getString("utils.FileFormatUtils.OO");
	public final static String TEXT = Messages.getString("utils.FileFormatUtils.TEXT");
	/** GNU gettext 可移植对象 (PO) */
	public final static String PO = Messages.getString("utils.FileFormatUtils.PO");
	/** Windows C/C++ 资源文件 (RC) */
	public final static String RC = Messages.getString("utils.FileFormatUtils.RC");
	public final static String RESX = Messages.getString("utils.FileFormatUtils.RESX");
	public final static String RTF = Messages.getString("utils.FileFormatUtils.RTF");
	public final static String TRTF = Messages.getString("utils.FileFormatUtils.TRTF");
	/** SDL TRADOStag 双语文件 (TTX) */
	public final static String TTX = Messages.getString("utils.FileFormatUtils.TTX");
	public final static String XML = Messages.getString("utils.FileFormatUtils.XML");
//	public final static String XMLG = Messages.getString("utils.FileFormatUtils.XMLG");
	public final static String MS = Messages.getString("utils.FileFormatUtils.MS");
	/** trados 2009的文件格式 */
	public final static String SDL = Messages.getString("utils.FileFormatUtils.SDL");
	/** dejavu X2的文件格式 */
	public final static String DU = Messages.getString("utils.FileFormatUtils.DU");

	public final static String IDML = Messages.getString("utils.FileFormatUtils.IDML");
	/** memoQ 6.0 的文件格式 */
	public final static String MQ = Messages.getString("utils.FileFormatUtils.MQ");
	public final static String PPTX = Messages.getString("utils.FileFormatUtils.PPTX");
	public final static String MSEXCEl2007 = Messages.getString("utils.FileFormatUtils.MSEXCEL");
	public final static String MSWORD2007 = Messages.getString("utils.FileFormatUtils.MSWORD2007");
	/** wordFast 3 的转换器 */
	public final static String WF = Messages.getString("utils.FileFormatUtils.WF");

	public static boolean canEmbedSkl(String fileFormat) {
//		return !(fileFormat.equals(TRTF) || fileFormat.equals(MS) || fileFormat.equals(OFF) || fileFormat.equals(OO)
//				|| fileFormat.equals(IDML) || fileFormat.equals(PPTX)|| fileFormat.equals(MSEXCEl2007) || fileFormat.equals(MSWORD2007));
		return false; // 所有文件都不嵌入骨架 
	}

	public static String detectFormat(String fileName) {
		
		/**
		 * 备注，新添加的转换器的后缀名，必须添加到　NewProjectWizardSourceFilePage 中的　"CONVERTEREXTENTION" 处
		 * robert 2013-04-09
		 */
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
			byte[] efbbbf = {-17,-69,-65}; // utf-8 bom 
			// there may be a BOM, now check the order
			if (array[0] == fffe[0] && array[1] == fffe[1]) {
				string = new String(array, 2 ,array.length - 2, "UTF-16BE"); //remove bom info
			} else if (array[0] == feff[0] && array[1] == feff[1]) {
				string = new String(array, 2 ,array.length - 2, "UTF-16LE"); //remove bom info
			} else if(array[0] == efbbbf[0] && array[1] == efbbbf[1] && array[2] == efbbbf[2]){
				string = new String(array, 3, array.length - 3, "UTF-8");
			}else {
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
				// 这是trados 2009的命名空间，若有此，则为trados 2009的双语文件。 --robert 2012-06-28
				if (string.indexOf("xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\"") != -1) {
					return SDL;
				}
				// 这是Deja VU X2的命名空间，若有此，则为Deja VU X2的双语文件。 --robert 2012-07-06
				if (string.indexOf("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"") != -1) {
					return DU;
				}
				
				// 为wordFast的双语文件。 --robert 2012-12-13
				if (fileName.endsWith(".txml") && string.indexOf("<txml") != -1) {
					return WF;
				}
				return XML;
			}

			if (string.startsWith("<!DOCTYPE") || string.startsWith('\uFEFF' + "<!DOCTYPE") || string.startsWith('\uFFFE' + "<!DOCTYPE")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				int index = string.indexOf("-//IETF//DTD HTML"); //$NON-NLS-1$
				if (index != -1) {
					return HTML;
				}
				if (string.indexOf("-//W3C//DTD HTML") != -1 || string.indexOf("-//W3C//DTD XHTML") != -1) {
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
				boolean isContainDesignMap = false;
				boolean isContainMimetype = false;
				boolean isPPTX = false;
				boolean isMsExcel2007 = false;
				boolean isMsWord2007 = false;
				while ((entry = in.getNextEntry()) != null) {
					if (entry.getName().equals("content.xml")) { //$NON-NLS-1$
						found = true;
						break;
					}
					if (entry.getName().startsWith("ppt/")) {
						isPPTX = true;
						break;
					}
					if(entry.getName().startsWith("xl/")){
						isMsExcel2007 = true;
						break;
					}
					if (entry.getName().equals("designmap.xml")) {
						isContainDesignMap = true;
					}
					if (entry.getName().startsWith("word/") || entry.getName().startsWith("word\\")) {
						isMsWord2007 = true;
					}
					if (entry.getName().equals("mimetype")) {
						isContainMimetype = true;
					}
					if (entry.getName().matches(".*\\.xml")) { //$NON-NLS-1$
						hasXML = true;
					}
				}
				in.close();
				if (fileName.toLowerCase().endsWith(".pptx") && isPPTX) {
					return PPTX;
				}
				if(fileName.toLowerCase().endsWith(".xlsx") && isMsExcel2007){
					return MSEXCEl2007;
				}
				if (fileName.toLowerCase().endsWith(".docx") && isMsWord2007) {
					return MSWORD2007;
				}
				if (found) {
					return OO;
				}
				if (fileName.toLowerCase().endsWith(".idml") && isContainDesignMap && isContainMimetype) {
					return IDML;
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
		if (fileName.endsWith(".doc") || fileName.endsWith(".xls") || fileName.endsWith(".ppt")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return MS;
		}
		if (fileName.endsWith(".mqxlz")) {
			return MQ;
		}
		if (fileName.endsWith(".txml")) {
			return WF;
		}
		return null;
	}
}
