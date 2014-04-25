package net.heartsome.cat.ts.ui.qa.spell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.core.bean.SingleWord;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.qa.spell.inter.HSSpellChecker;
import net.heartsome.cat.ts.ui.qa.spell.inter.HunspellLibrary;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Hunspell 管理类
 * @author robert	2013-01-10
 */
public class Hunspell implements HSSpellChecker{
    
    /** 单例模式， Hunspell 实例 */
    private static Hunspell instance = null;
    
    /** 由 JNA 创建的 Library 实例,由这个实例加载相关函数，进行词典查询 */
    private HunspellLibrary hunspellLibrary = null;
    
    private QAXmlHandler xmlHandler;
    
    /** hunspell 支持的语言，若一种语言不满足此项，则退出 */
    private Map<String, String> availableLangMap = new HashMap<String, String>();
    
    /** 保存当前已经加载的语言词典 */
    private HashMap<String, Dictionary> dictionariesMap = new HashMap<String, Dictionary>();
    
	/** hunspell 运行库文件 */
	private String libFile;
	
	/** hunspell 拼写检查器是否运行错误的标识符 */
	private boolean isError = false;
	
	private Shell shell;
	private final static Logger logger = LoggerFactory.getLogger(Hunspell.class.getName());
	private IWorkspaceRoot root;
	
	/**
	 * 管理 hunspell 运行库，以及词典
	 * @return
	 * @throws UnsatisfiedLinkError
	 * @throws UnsupportedOperationException
	 */
    public static synchronized void synchronizeInit(Shell shell) throws UnsatisfiedLinkError, UnsupportedOperationException { 
        if (instance == null) {
        	instance = new Hunspell(shell);
        }
    }
    
    public static Hunspell getInstance(Shell shell){
    	if (instance == null) {
			synchronizeInit(shell);
		}
    	return instance;
    	
    }

    protected void tryLoad(String libFile) throws UnsupportedOperationException {
		hunspellLibrary = (HunspellLibrary)Native.loadLibrary(libFile, HunspellLibrary.class);
    }

    /**
     * hunspell 运行实例
     */
    public Hunspell(Shell _shell){
    	this.shell = _shell;
    	
    	try {
    		root = ResourcesPlugin.getWorkspace().getRoot();
    		// 先求出所有所支持的语言
    		xmlHandler = new QAXmlHandler();
//    		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
//    		String configXml = FileLocator.toFileURL(bundle.getEntry(QAConstant.QA_SPELL_hunspellConfigFile)).getPath();
//    		String configXml = root.getLocation().append(QAConstant.QA_SPELL_hunspellConfigFile).toOSString() ;
    		String configXml = Platform.getConfigurationLocation().getURL().getPath() + QAConstant.QA_SPELL_hunspellConfigFile;
    		
//    		if (!new File(configXml).exists() || new File(configXml).isDirectory()) {
//    			copyHunspellData();
//			}
    		
    		if (!new File(configXml).exists() || new File(configXml).isDirectory()) {
    			isError = true;
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(shell, Messages.getString("qa.all.dialog.error"), 
								Messages.getString("qa.spell.hunspell.notFindHunpsellConfigTip"));
					}
				});
				return;
			}
    		availableLangMap = xmlHandler.getHunspellAvailableLang(configXml);
    		if (availableLangMap == null) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(shell, Messages.getString("qa.all.dialog.error"),
								Messages.getString("qa.spell.hunspell.hunspellConfigErrorTip"));
					}
				});
				isError = true;
				return;
			}
    		
//    		libFile = FileLocator.toFileURL(bundle.getEntry(QAConstant.QA_SPELL_hunspellLibraryFolder)).getFile();
//    		libFile = new File(libFile).getAbsolutePath() + System.getProperty("file.separator") + libName();
//    		libFile = "C:\\Documents and Settings\\Administrator\\桌面\\h\\lib\\" + libName();
//    		libFile = root.getLocation().append(".metadata/h/native-library").append(libName()).toOSString();
//    		libFile = new File(Platform.getConfigurationLocation().getURL().getPath() + "h/native-library"  + System.getProperty("file.separator") + libName()).getAbsolutePath();
    		libFile = root.getLocation().append(QAConstant.QA_SPELL_hunspellLibraryFolder).append(libName()).toOSString() ;
    		if (!new File(libFile).exists() || new File(libFile).isDirectory()) {
    			copyHunspellLibFile();
			}
    		
    		if (!new File(libFile).exists()) {
    			MessageFormat.format(Messages.getString("qa.spell.hunspell.notFindHunspellLibTip"), new Object[]{libFile});
    			isError = true;
    			return;
			}
    		// 加载运行库
			hunspellLibrary = (HunspellLibrary)Native.loadLibrary(libFile, HunspellLibrary.class);
		} catch (Exception e) {
			isError = true;
			e.printStackTrace();
		}
    }

	public String getLibFile() {
		return libFile;
	}

    /**
     * 获取各个版本下的 hunspell 软件的函数库
     */
    public static String libName() throws UnsupportedOperationException {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			return libNameBare()+".dll";
		} else if (os.startsWith("mac os x")) {
			return libNameBare()+".jnilib";
		} else {
			return "lib"+libNameBare()+".so";
		}  
    }

    public static String libNameBare() throws UnsupportedOperationException {
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch").toLowerCase();

		boolean x86  = arch.equals("x86")    || arch.equals("i386")  || arch.equals("i686");
		boolean amd64= arch.equals("x86_64") || arch.equals("amd64") || arch.equals("ia64n");
	
		if (os.startsWith("windows")) {
			if (x86) {
				return "hunspell-win-x86-32";
			}
			if (amd64) { 
				return "hunspell-win-x86-64";
			}

		} else if (os.startsWith("mac os x")) {
			if (x86) {
				return "hunspell-darwin-x86-32";
			}
			if (amd64) {
				return "hunspell-darwin-x86-64";
			}
			if (arch.equals("ppc")) {		    
				return "hunspell-darwin-ppc-32";
			}
		} else if (os.startsWith("linux")) {
			if (x86) {
				return "hunspell-linux-x86-32";
			}
			if (amd64) {
				return "hunspell-linux-x86-64";
			}
		}
		throw new UnsupportedOperationException("Unknown OS/arch: "+os+"/"+arch);
    }    


    /**
     * 获取某种语言的字典实例
     */
    public Dictionary getDictionary(String language)
		throws FileNotFoundException, UnsupportedEncodingException {
		// 如果语言发生改变，重新加载该语言的词典
		if (dictionariesMap.containsKey(language)) {
			return dictionariesMap.get(language);
		} else {
			Dictionary dictionary = new Dictionary(language);
			dictionariesMap.put(language, dictionary);
			return dictionary;
		}
    }

    /**
     * 将词典的语言从内存中移除
     */
    public void destroyDictionary(String baseFileName) {
		if (dictionariesMap.containsKey(baseFileName)) {
			dictionariesMap.remove(baseFileName);
		}
    }

    /**
     * 某种语言的词典实例（单例模式）
     */
    public class Dictionary {
		/** hunspell 函数库的指针  */
		private Pointer hunspellDictPoint = null;
		/** hunspell 所用的编码 */
		private String encoding;

		/** 某种语言的词典实例 */
		public Dictionary(String language){
			try {
				String dictionaryName = availableLangMap.get(language);
				
//				Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
//				String dictionaryFolder = FileLocator.toFileURL(bundle.getEntry(QAConstant.QA_SPELL_hunspellDictionaryFolder)).getPath();
//				String dictionaryFolder = "C:\\Documents and Settings\\Administrator\\桌面\\h\\hunspellDictionaries";
//				String dictionaryFolder = root.getLocation().append(".metadata/h/hunspellDictionaries").toOSString();
//				String dictionaryFolder = new File(Platform.getConfigurationLocation().getURL().getPath() + "h/hunspellDictionaries").getAbsolutePath();
				
				String dictionaryFolder = root.getLocation().append(QAConstant.QA_SPELL_hunspellDictionaryFolder).toOSString() ;
	    		if (!new File(dictionaryFolder).exists() || !new File(dictionaryFolder).isDirectory()) {
	    			copyHunspellDictionaries(dictionaryName, language);
				}
				
				String dicPath = dictionaryFolder + System.getProperty("file.separator") + dictionaryName + ".dic";
				String affPath = dictionaryFolder + System.getProperty("file.separator") + dictionaryName + ".aff";
				
				File dic = new File(dicPath);
				File aff = new File(affPath);
				
				if (!dic.exists() || !aff.exists() || !dic.canRead() || !aff.canRead()) {
					copyHunspellDictionaries(dictionaryName, language);
				}
				
				if (!dic.exists() || !aff.exists()) {
					throw new FileNotFoundException(MessageFormat.format(Messages.getString("qa.spell.hunspell.notFindDictionaryTip"), new Object[]{language}));
				}
				
				if (!dic.canRead() || !aff.canRead()) {
					throw new FileNotFoundException(MessageFormat.format(Messages.getString("qa.spell.hunspell.dicReadErrorTip"), new Object[]{language}));
				}
				
				hunspellDictPoint = hunspellLibrary.Hunspell_create(aff.getAbsolutePath(), dic.getAbsolutePath());
				encoding = hunspellLibrary.Hunspell_get_dic_encoding(hunspellDictPoint);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 当程序结束时，销毁词典，释放资源
		 */
		public void destroy() {
			if (hunspellLibrary != null && hunspellDictPoint != null) {
				hunspellLibrary.Hunspell_destroy(hunspellDictPoint);
				hunspellDictPoint = null;
			}
		}

		/**
		 * 查询一个单词是否拼写错误，若拼写规范，则返回 true
		 */
		public boolean misspelled(String word) {
			try {
				return hunspellLibrary.Hunspell_spell(hunspellDictPoint, stringToBytes(word)) == 0;
			} catch (UnsupportedEncodingException e) {
				return true;
			}
		}

		/**
		 * 在词典的编码中，将一个 java 字符串转换成一个 0 节字的数组，这如 hunspell 功能所预期的一样
		 */
		protected byte[] stringToBytes(String str) throws UnsupportedEncodingException {
			return (str + "\u0000").getBytes(encoding);
//			return (new String((str + "\u0000").getBytes(), "GB2312")).getBytes(encoding);
//			return (str + (new String("\u0000".getBytes(), "utf8"))).getBytes(encoding);
//			return ((new String((str + "\u0000").getBytes(), encoding))).getBytes(encoding);	//ISO8859-1
//			return ((new String(str.getBytes(), "utf-8")) + new String("\u0000".getBytes(), "utf-8")).getBytes(encoding);
//			return ((new String(str.getBytes(), encoding)) + new String("\u0000".getBytes(), encoding)).getBytes(encoding);
//			return ((new String(str.getBytes(), "GBK")) + new String("\u0000".getBytes(), "GBK")).getBytes(encoding);
//			logger.info(str);
//			String word = new String((str + "\u0000").getBytes("UTF-8"), encoding);
//			logger.info(word);
//			word = new String((str + "\u0000").getBytes("GB2312"), encoding);
//			logger.info(word);
//			word = new String((str + "\u0000").getBytes("iso-8859-1"), encoding);
//			logger.info(word);
//			word = new String((str + "\u0000").getBytes("GBK"), encoding);
//			logger.info(word);
//			word = new String((str + "\u0000").getBytes(encoding), encoding);
//			logger.info(word);
//			logger.info("--------------------------");
//			
//			
////			return (new String((str + "\u0000").getBytes("UTF-8"), encoding)).getBytes(encoding);
////			return str.getBytes("iso-8859-1");
////			return (new String((str + "\u0000").getBytes(encoding), "UTF-8")).getBytes("UTF-8");
//			String word1 = str + "\u0000";
//			return word1.getBytes(encoding);	// 不加括号
		}
		
		

		/**
		 * 返回一个单词的正确拼写建议
		 */
		public List<String> suggest(String word) {
			try {		
				int suggestionsCount = 0;
				PointerByReference suggestions = new PointerByReference();
                suggestionsCount = hunspellLibrary.Hunspell_suggest(
                		hunspellDictPoint, suggestions, stringToBytes(word));
				return pointerToCStringsToList(suggestions, suggestionsCount);
			} catch (UnsupportedEncodingException ex) { 
				return Collections.emptyList();
			} 
		}
		
		private List<String> pointerToCStringsToList(PointerByReference slst, int n) {
			if ( n == 0 ) {
				return Collections.emptyList();
			}
			List<String> strings = new ArrayList<String>(n);
			try {
				// Get each of the suggestions out of the pointer array.
				Pointer[] pointerArray = slst.getValue().getPointerArray(0, n);
				for (int i=0; i<n; i++) {
					// 工作编码为 8bit 或者 utf-8	    
					long len = pointerArray[i].indexOf(0, (byte)0); 
					if (len != -1) {
						if (len > Integer.MAX_VALUE) {
							throw new RuntimeException("String improperly terminated: " + len);
						}
						byte[] data = pointerArray[i].getByteArray(0, (int)len);
						strings.add(new String(data, encoding));
					}
				}
			} catch (UnsupportedEncodingException e) {
				// Shouldn't happen...
			} finally {
				hunspellLibrary.Hunspell_free_list(hunspellDictPoint, slst, n);
			}
			return strings;
		}
    }

	public void setTarget(String t) {
		
	}

	public List<SingleWord> getErrorWords(String tgtText, List<SingleWord> wordList, String language) {
		List<SingleWord> errorWords = new LinkedList<SingleWord>();
		try {
			Dictionary dictionary = getDictionary(language);
			for (SingleWord wordBean : wordList) {
//				if (SEPARATORS.indexOf(token) != -1) {
//					continue;
//				}
				String pureWord = wordBean.getPureWord();
				// 以数字开头
				if (pureWord.matches("[\\d]*")) {
					continue;
				}
				if (pureWord.length() == 1 && pureWord.charAt(0) >= '\uE000' && pureWord.charAt(0) <= '\uF8FF') {
					continue;
				}
				if (pureWord.trim().equals("") || pureWord.trim().equals("–")) {
					continue;
				}
				
				if (dictionary.misspelled(pureWord)) {
					errorWords.add(wordBean);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return errorWords;
	}


	public boolean isError() {
		return isError;
	}

	public boolean langIsLoad(String language) {
		return dictionariesMap.containsKey(language);
	}

	public boolean checkLangAvailable(String language) {
		return availableLangMap.containsKey(language);
	}
	
	
	/**
	 * 将 hunspell 的词典从 configurationa 中拷到 工作空间下 的 .metadata/hunspell/hunspellDictionaries 下面
	 */
	private void copyHunspellDictionaries(String dictionaryName, String language) throws Exception{
		String tgtDicFolderPath = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().append(QAConstant.QA_SPELL_hunspellDictionaryFolder).toOSString();
		File dicFolder = new File(tgtDicFolderPath);
		if (!dicFolder.exists() || dicFolder.isFile()) {
			dicFolder.mkdirs();
		}
		String srcDicFolderPath = Platform.getConfigurationLocation().getURL().getPath()
				+ "net.heartsome.cat.ts.ui" + System.getProperty("file.separator") + "hunspell"
				+ System.getProperty("file.separator") + "hunspellDictionaries";
		
		String srcDicPath = srcDicFolderPath + System.getProperty("file.separator") + dictionaryName + ".dic";
		String srcAffPath = srcDicFolderPath + System.getProperty("file.separator") + dictionaryName + ".aff";
		
		if (!new File(srcDicPath).exists() || !new File(srcAffPath).exists() || !new File(srcDicPath).canRead() || !new File(srcAffPath).canRead()) {
			throw new FileNotFoundException(MessageFormat.format(Messages.getString("qa.spell.hunspell.notFindDictionaryTip"), new Object[]{language}));
		}
		
		try {
			String tgtDicPath = tgtDicFolderPath + System.getProperty("file.separator") + dictionaryName + ".dic";
			String tgtAffPath = tgtDicFolderPath + System.getProperty("file.separator") + dictionaryName + ".aff";
			
			ResourceUtils.copyDirectory(new File(srcDicPath), new File(tgtDicPath));
			ResourceUtils.copyDirectory(new File(srcAffPath), new File(tgtAffPath));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.spell.hunspell.LOG.copyError"), e);
		}
	}
	
	/**
	 * 将 hunspell 的运行库拷贝到工作空间的 .metadata/hunspell/native-library 下
	 */
	private void copyHunspellLibFile(){
		String hunspellPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(QAConstant.QA_SPELL_hunspellFolder).toOSString();
		File hunspellFolder = new File(hunspellPath);
		if (!hunspellFolder.exists() || hunspellFolder.isFile()) {
			hunspellFolder.mkdirs();
		}
		
		String srcLocation = Platform.getConfigurationLocation().getURL().getPath()
				+ "net.heartsome.cat.ts.ui" + System.getProperty("file.separator") + "hunspell" 
				+ System.getProperty("file.separator") + "native-library";
		try {
			String libFolderPath = root.getLocation().append(QAConstant.QA_SPELL_hunspellLibraryFolder).toOSString();
			ResourceUtils.copyDirectory(new File(srcLocation), new File(libFolderPath));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.spell.hunspell.LOG.copyError"), e);
		}
	}
	
	public void setTagPosition(List<Integer> tagPositionList) {
		// do nothing, 该方法只用于 aspell
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(("this" + "\u0000").getBytes("utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
//		return (str + "\u0000").getBytes(encoding);
	}

}
