package net.heartsome.cat.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.core.resource.Messages;
import net.heartsome.cat.common.resources.ResourceUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorHistory;
import org.eclipse.ui.internal.EditorHistoryItem;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * 公共方法
 * @author Gonzalo To change the template for this generated type comment go to Window>Preferences>Java>Code
 *         Generation>Code and Comments
 */
public class CommonFunction {
	/**
	 * <div style='color:red'>备注：R8所支持的文件后缀名，当后支持文件后缀名要进行扩展时，修改四处，<br>
	 * 第一即xlfExtensionList常量，<br>
	 * 第二在xlfExtesionArray变量，<br>
	 * 第三处在nattable插件的net.heartsome.cat.ts.ui.xliffeditor.nattable.editor编辑器定义里面。<br>
	 * 第四处为R8XliffExtension常量</div>
	 * 别，带 $extension$ 标记的地方，手动修改
	 * robert 2012-06-21
	 */
	private final static List<String> xlfExtensionList = new ArrayList<String>();
	public final static String[] xlfExtesionArray = new String[]{"hsxliff"};
	/** R8 xliff文件的后缀名，带“.”的 */
	public final static String R8XliffExtension_1 = ".hsxliff";
	/** R8 xliff文件的后缀名，不带“.”的 */
	public final static String R8XliffExtension = "hsxliff";
	
	/** r8 系统语言，与prefrenceStore中的数据同步。robert	2012-09-15 */
	private static String systemLanguage = "en";
	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	
	
	static{
		xlfExtensionList.add("hsxliff");
		xlfExtensionList.add(".hsxliff");
	}
	/**
	 * 判断数组中是否包含指定元素
	 * @param array
	 *            数组
	 * @param element
	 *            元素
	 * @return ;
	 */
	public static <T> boolean contains(T[] array, T element) {
		if (array == null || array.length == 0) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				if (element == null) {
					return true;
				}
			} else if (array[i].equals(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断忽略大小写时字符串数组中是否包含指定字符串。
	 * @param stringArray
	 *            字符串数组
	 * @param string
	 *            字符串
	 * @return ;
	 */
	public static boolean containsIgnoreCase(String[] stringArray, String string) {
		if (stringArray == null || stringArray.length == 0) {
			return false;
		}
		for (int i = 0; i < stringArray.length; i++) {
			if (stringArray[i] == null) {
				if (string == null) {
					return true;
				}
			} else if (stringArray[i].equalsIgnoreCase(string)) {
				return true;
			}
		}
		return false;
	}

	public static String[] getItemBySeparator(String s, char separator) {

		if (s == null) {
			return new String[0];
		}
		String separators = "" + separator; //$NON-NLS-1$
		StringTokenizer tokenizer = new StringTokenizer(s, separators);

		int size = tokenizer.countTokens();
		String[] result = new String[size];
		for (int i = 0; i < size; i++) {
			result[i] = tokenizer.nextToken();
		}

		return result;
	}

	public static String[] vector2StringArray(Vector<String> vector) {
		String[] result = new String[vector.size()];
		for (int i = 0; i < vector.size(); i++) {
			result[i] = vector.get(i);
		}
		return result;
	}

	public static void stringArray2Vector(String[] stringArray, Vector<String> vector) {
		for (int i = 0; i < stringArray.length; i++) {
			vector.add(stringArray[i]);
		}
	}

	/**
	 * 将数组转为 List
	 * @param array
	 *            home path
	 */
	public static <T> List<T> array2List(T[] array) {
		ArrayList<T> list = new ArrayList<T>(array.length);
		for (int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
		return list;
	}

	public static int indexOf(Vector<String> vector, String string) {
		for (int i = 0; i < vector.size(); i++) {
			if (vector.get(i).equals(string)) {
				return i;
			}
		}
		return -1;

	}

	public static int indexOf(String[] array, String string) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(string)) {
				return i;
			}
		}
		return -1;
	}

	public static String[] getWords(String s) {
		if (s == null) {
			return new String[0];
		}
		StringTokenizer tokenizer = new StringTokenizer(s, Constant.SEPARATORS_1, true);

		Vector<String> result = new Vector<String>();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			if (!token.equals("")) { //$NON-NLS-1$
				result.add(token);
			}
		}
		return vector2StringArray(result);
	}

	public static String retTMXDate() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		String sec = (calendar.get(Calendar.SECOND) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.SECOND);
		String min = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.MINUTE);
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.HOUR_OF_DAY);
		String mday = (calendar.get(Calendar.DATE) < 10 ? "0" : "") + calendar.get(Calendar.DATE); //$NON-NLS-1$ //$NON-NLS-2$
		String mon = (calendar.get(Calendar.MONTH) < 9 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR); //$NON-NLS-1$

		String date = longyear + mon + mday + "T" + hour + min + sec + "Z"; //$NON-NLS-1$ //$NON-NLS-2$
		return date;
	}

	public static String retGMTdate(String TMXDate) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		try {
			int second = Integer.parseInt(TMXDate.substring(13, 15));
			int minute = Integer.parseInt(TMXDate.substring(11, 13));
			int hour = Integer.parseInt(TMXDate.substring(9, 11));
			int date = Integer.parseInt(TMXDate.substring(6, 8));
			int month = Integer.parseInt(TMXDate.substring(4, 6)) - 1;
			int year = Integer.parseInt(TMXDate.substring(0, 4));
			calendar.set(year, month, date, hour, minute, second);
			DateFormat dt = DateFormat.getDateTimeInstance();
			return dt.format(calendar.getTime());
		} catch (Exception e) {
			return ""; //$NON-NLS-1$
		}
	}


	/**
	 * 得到文件夹下的所有文件（包括子文件夹中的文件）。
	 * @param folder
	 *            文件夹
	 * @param fileExtension
	 *            后缀名
	 * @param list
	 *            所有文件 ;
	 */
	public static void getChildFiles(File folder, String fileExtension, List<File> list) {
		if (list == null) {
			list = new ArrayList<File>();
		}
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					String fileName = file.getName().toLowerCase();
					if (fileName.endsWith("." + fileExtension)) {
						list.add(file);
					}
				} else if (file.isDirectory()) {
					getChildFiles(file, fileExtension, list);
				}
			}
		}
	}

	/**
	 * break a path down into individual elements and add to a list. example : if a path is /a/b/c/d.txt, the breakdown
	 * will be [d.txt,c,b,a]
	 * @param file
	 *            input file
	 * @return a Vector with the individual elements of the path in reverse order
	 */
	private static Vector<String> getPathList(File file) throws IOException {
		Vector<String> list = new Vector<String>();
		File r;
		r = file.getCanonicalFile();
		while (r != null) {
			list.add(r.getName());
			r = r.getParentFile();
		}
		return list;
	}

	/**
	 * figure out a string representing the relative path of 'f' with respect to 'r'
	 * @param r
	 *            home path
	 * @param f
	 *            path of file
	 */
	private static String matchPathLists(Vector<String> r, Vector<String> f) {
		int i;
		int j;
		String s = ""; //$NON-NLS-1$
		// start at the beginning of the lists
		// iterate while both lists are equal
		i = r.size() - 1;
		j = f.size() - 1;

		// first eliminate common root
		while (i >= 0 && j >= 0 && r.get(i).equals(f.get(j))) {
			i--;
			j--;
		}

		// for each remaining level in the home path, add a ..
		for (; i >= 0; i--) {
			s += ".." + File.separator; //$NON-NLS-1$
		}

		// for each level in the file path, add the path
		for (; j >= 1; j--) {
			s += f.get(j) + File.separator;
		}

		// file name
		if (j >= 0 && j < f.size()) {
			s += f.get(j);
		}
		return s;
	}

	/**
	 * get relative path of File 'f' with respect to 'home' directory example : home = /a/b/c f = /a/d/e/x.txt s =
	 * getRelativePath(home,f) = ../../d/e/x.txt
	 * @param home
	 *            base path, should be a directory, not a file, or it doesn't make sense
	 * @param f
	 *            file to generate path for
	 * @return path from home to f as a string
	 */
	public static String getRelativePath(String homeFile, String filename) throws Exception {
		File home = new File(homeFile);
		// If home is a file, get the parent
		if (!home.isDirectory()) {
			home = new File(home.getParent());
		}
		File file = new File(filename);
		// Check for relative path
		if (!home.isAbsolute() || !file.isAbsolute()) {
			throw new Exception(Messages.getString("util.CommonFunction.logger1"));
		}
		Vector<String> homelist;
		Vector<String> filelist;

		homelist = getPathList(home);
		filelist = getPathList(file);
		return matchPathLists(homelist, filelist);
	}

	public static String getAbsolutePath(String homeFile, String relative) throws IOException {
		File home = new File(homeFile);
		// If home is a file, get the parent
		File result;
		if (home.isDirectory()) {
			result = new File(home.getAbsolutePath(), relative);
		} else {
			result = new File(home.getParent(), relative);
		}
		return result.getCanonicalPath();
	}
	
	
	/**
	 * 验证xliff文件的后缀名	robert	2012-06-20
	 * @param extention
	 * @return
	 */
	public static boolean validXlfExtension(String extention){
		return xlfExtensionList.contains(extention);
	}
	/**
	 * 通过xliff文件的名称或路径验证xliff文件的后缀名是否合法	robert	2012-06-20
	 * @param extention
	 * @return
	 */
	public static boolean validXlfExtensionByFileName(String xlfName){
		if (xlfName.lastIndexOf(".") < 0) {
			return false;
		}
		String extention = xlfName.substring(xlfName.lastIndexOf("."), xlfName.length());
		if (extention == null) {
			return false;
		}
		return xlfExtensionList.contains(extention);
	}
	/**
	 * 获取当前系统语言
	 * @return
	 */
	public static String getSystemLanguage() {
		return systemLanguage;
	}
	
	/**
	 * 设置当前系统语言，标识，与preferenceStore中的数据同步
	 * @return
	 */
	public static void setSystemLanguage(String systemLanguage) {
		CommonFunction.systemLanguage = systemLanguage;
	}
	
	/**
	 * 删除 选中文件集合 中重复的文件	robert	2012-11-09
	 * @param selectIFileList
	 */
	public static void removeRepeateSelect(List<IFile> selectIFileList){
		HashSet<IFile> set = new HashSet<IFile>(selectIFileList);
		selectIFileList.clear();
		selectIFileList.addAll(set);
	}
	
	/**
	 * 验证当前版本
	 * @param editionId
	 * 				版本 Id 值（旗舰版为 U，专业版为 F，个人版为 P，精简版为 L）
	 * @return ;
	 */
	public static boolean checkEdition(String editionId) {
		return editionId == null ? false : System.getProperty("TSEdition").equals(editionId);
	}
	
	/**
	 * 当删除一个文件时，刷新历史记录	--robert	2012-11-20
	 */
	@SuppressWarnings("restriction")
	public static void refreshHistoryWhenDelete(IEditorInput input){
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench instanceof Workbench) {
			EditorHistory history = ((Workbench) workbench).getEditorHistory();
			for (EditorHistoryItem item : history.getItems()) {
				if (item.matches(input)) {
					history.remove(item);
				}
			}
			history.refresh();
		}
	}
	
	/**
	 * 判断指定语言是否是亚洲语系	robert	2012-12-27
	 * @return
	 */
	public static boolean isAsiaLang(String lang){
		if (lang.toLowerCase().matches("zh.*|ja.*|ko.*|th.*|he.*")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 当程序开始运行时，检查 qa 插件是否存在，如果不存在，则不执行任何操作，如果存在，则检查 configuration\net.heartsome.cat.ts.ui\hunspell 
	 * 下面是否有 hunspell 运行所需要的词典以及运行函数库。如果没有，则需要从 qa 插件中进行获取，并且解压到上述目录下。
	 * robert	2013-02-28
	 */
	public static void unZipHunspellDics() throws Exception{
		Bundle bundle = Platform.getBundle("net.heartsome.cat.ts.ui.qa");
		if (bundle == null) {
			return;
		}
		
		// 查看解压的 hunspell词典 文件夹是否存在
		String configHunspllDicFolder = Platform.getConfigurationLocation().getURL().getPath() 
				+ "net.heartsome.cat.ts.ui" + System.getProperty("file.separator") + "hunspell"
				+ System.getProperty("file.separator") + "hunspellDictionaries";
		if (!new File(configHunspllDicFolder).exists()) {
			new File(configHunspllDicFolder).mkdirs();
			String hunspellDicZipPath = FileLocator.toFileURL(bundle.getEntry("hunspell/hunspellDictionaries.zip")).getPath();
			upZipFile(hunspellDicZipPath, configHunspllDicFolder);
		}
	}
	
	public static String upZipFile(String zipFile, String baseDir) throws IOException {
		File f = new File(zipFile);
		if (baseDir == null) {
			baseDir = f.getPath() + "_files";
		}
		ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
		ZipEntry ze;
		byte[] buf = new byte[1024];
		while ((ze = zis.getNextEntry()) != null) {
			File outFile = getRealFileName(baseDir, ze.getName());
			FileOutputStream os = new FileOutputStream(outFile);
			int readLen = 0;
			while ((readLen = zis.read(buf, 0, 1024)) != -1) {
				os.write(buf, 0, readLen);
			}
			os.close();
		}
		zis.close();
		return baseDir;
	}
	
	/**
	 * 给定根目录，返回一个相对路径所对应的实际文件名.
	 * @param baseDir
	 *            指定根目录
	 * @param absFileName
	 *            相对路径名，来自于ZipEntry中的name
	 * @return java.io.File 实际的文件
	 */
	private static File getRealFileName(String baseDir, String absFileName) {
		String[] dirs = absFileName.split("/");
		File ret = new File(baseDir);
		if (!ret.exists()) {
			ret.mkdirs();
		}
		
		if ("/".equals(System.getProperty("file.separator"))) {
			for (int i = 0; i < dirs.length; i++) {
				dirs[i] = dirs[i].replace("\\", "/");
			}
		}
		
		if (dirs.length >= 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				ret = new File(ret, dirs[i]);
			}
			if (!ret.exists()) {
				ret.mkdirs();
			}
			ret = new File(ret, dirs[dirs.length - 1]);
			if(!ret.exists()){
				File p  = ret.getParentFile();
				if(!p.exists()){
					p.mkdirs();
				}
			}
		}
		return ret;
	}
	
	/**
	 * 关闭指定文件的编辑器		-- robert 2013-04-01
	 * 备注：这里面的方法，是不能获取　nattable 的实例，故，在处理　合并打开的情况时，是通过　vtd 进行解析　合并临时文件从而获取相关文件的
	 * @param iFileList
	 */
	public static void closePointEditor(List<IFile> iFileList){
		Map<IFile, IEditorPart> openedIfileMap = new HashMap<IFile, IEditorPart>();
		IEditorReference[] referenceArray = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for(IEditorReference reference : referenceArray){
			IEditorPart editor = reference.getEditor(true);
			IFile iFile = ((FileEditorInput)editor.getEditorInput()).getFile();
			// 如果这是一个　nattable 编辑器
			if (XLIFF_EDITOR_ID.equals(editor.getSite().getId())) {
				String iFilePath = iFile.getLocation().toOSString();
				String extension = iFile.getFileExtension();
				if ("hsxliff".equals(extension)) {
					openedIfileMap.put(iFile, editor);
				}else if ("xlp".equals(extension)) {
					// 这是合并打开的情况
					// 开始解析这个合并打开临时文件，获取合并打开的文件。
					VTDGen vg = new VTDGen();
					if (vg.parseFile(iFilePath, true)) {
						VTDNav vn = vg.getNav();
						AutoPilot ap = new AutoPilot(vn);
						try {
							ap.selectXPath("/mergerFiles/mergerFile/@filePath");
							int index = -1;
							while ((index = ap.evalXPath()) != -1) {
								String fileLC = vn.toString(index + 1);
								if (fileLC != null && !"".equals(fileLC)) {
									openedIfileMap.put(ResourceUtils.fileToIFile(fileLC), editor);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}else {
				// 其他情况，直接将文件丢进去就行了
				openedIfileMap.put(iFile, editor);
			}
			
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			for(IFile curIfile : iFileList){
				if (openedIfileMap.containsKey(curIfile)) {
					page.closeEditor(openedIfileMap.get(curIfile), false);
				}
			}
		}
	}
	
	/**
	 * 验证资源的合法性	robert	2013-07-01
	 * @param resourceName
	 * @return	如果返回的是　null ，则标志验证通过，否则，将返回的　字符串　以对话框的形式弹出
	 */
	public static String validResourceName(String resourceName){
		char[] errorChars = new char[]{'/', '\\', ':', '?', '"', '<', '>', '|'};
		if (Platform.getOS().indexOf("win") == -1) {
			String errorCharStr = "";
			for (int i = 0; i < errorChars.length; i++){
				if (resourceName.indexOf(errorChars[i]) != -1) {
					errorCharStr += errorChars[i] + "、";
				}
			}
			
			if (errorCharStr.length() > 0) {
				errorCharStr = errorCharStr.substring(0, errorCharStr.length() - 1);
				String errorTip = MessageFormat.format(Messages.getString("Commonfunciton.valid.waring"), errorCharStr, resourceName);
				return errorTip;
			}
		}
		return null;
	}
	
	/**
	 * 当退出软件时。若有未关闭的　job。进行提示		robert	2013-09-12
	 * @param jobName
	 */
	public static void jobCantCancelTip(final Job job){
		PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				System.out.println("job.isBlocking() = " + (job.getState()));
				if (job.getState() != Job.NONE) {
					MessageDialog.openInformation(Display.getDefault().getActiveShell(),
							Messages.getString("file.XLFValidator.msgTitle"),
							MessageFormat.format(Messages.getString("Commonfunction.job.tip"), job.getName()));
					return false;
				}else {
					return true;
				}
			}
			public void postShutdown(IWorkbench workbench) {
				
			}
		});
	}
	
	/**
	 * 创建　UUID，唯一标识符	--robert	2013-10-17
	 * @return
	 */
	public static String createUUID(){
		UUID uuId = UUID.randomUUID();
		return uuId.toString();
	}
	
}
