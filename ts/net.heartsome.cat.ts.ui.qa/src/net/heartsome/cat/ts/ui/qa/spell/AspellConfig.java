/*
 * Created on Jan 2, 2004
 *
 */
package net.heartsome.cat.ts.ui.qa.spell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管理aspell配置文件的类
 * @author  robert	2012-02-07
 * @version 
 * @since   JDK1.6
 */
public class AspellConfig {

	Shell shell;
	static String commandLine;
	static String configFile;
	private Hashtable<String, String> dictionaryList;
    static boolean utf8;
    public final static Logger logger = LoggerFactory.getLogger(AspellConfig.class.getName());
    
	public AspellConfig(String _configFile) {
		configFile = _configFile;
		//开始解析aspell配置文件
		getConfiguration(configFile);
	}
	
	
	void setCommand(String _commandLine) {
		commandLine = _commandLine;
	}
	
	/**
	 * 获取aspell程序的可执行文件的路径
	 */
	public String getCommand() {
		if ( utf8 && !commandLine.endsWith(" --encoding=utf-8") && !commandLine.equals("")) { 
			commandLine = commandLine + " --encoding=utf-8"; 
		}
		if (!utf8 && commandLine.endsWith(" --encoding=utf-8")) { 
		    commandLine = commandLine.substring(0, commandLine.indexOf(" --encoding=utf-8")); 
		}
		return commandLine;
	}

	public String getDictionaryForLang(String lang){
		String result = null;
		//getConfiguration(_configFile);
		if (dictionaryList != null){
			Object dictionary = dictionaryList.get(lang);
			result = (String) dictionary; 
		}
		return result;
	}
	
	void setDictionaryForLang(String lang, String dictionary){
		if (dictionaryList == null){
			dictionaryList = new Hashtable<String, String>();
		}

		String[] list = getDictionaries(commandLine);
		for (int i=0 ; i<list.length ; i++) {
			if (list[i].equals(dictionary)) {
				dictionaryList.remove(lang);
				dictionaryList.put(lang, dictionary);
				break;
			}
		}
	}
	
	/**
	 * 获取aspell配置，比如aspell安装路径，编码等
	 * @param shell2
	 * @param _configFile
	 */
	private void getConfiguration(String aspellConfigFile) {
		try {
			QAXmlHandler handler = new QAXmlHandler();
			Map<String, Object> newResultMap = handler.openFile(aspellConfigFile);
			// 针对退出解析
			if (newResultMap != null
					&& QAConstant.RETURNVALUE_RESULT_RETURN.equals(newResultMap.get(QAConstant.RETURNVALUE_RESULT))) {
				return;
			}
			
			commandLine = handler.getNodeText(aspellConfigFile, "/aspell/commandLine", "");
			
			if (!commandLine.equals("")) { 
				if (handler.getNodeText(aspellConfigFile, "/aspell/utf8", "yes").equals("yes")) {
					utf8 = true;
				} else {
					utf8 = false;
				}
			} else {
				utf8 = false;
			}
			dictionaryList = handler.getAspellDictionaries(aspellConfigFile);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.spellCheck.all.log2"), e);
		}		
	}
	
	public String[] getDictionaries(String command) {
		String response;
		Process spellProcess;

		Runtime runtime = Runtime.getRuntime();
		try {
			spellProcess = runtime.exec(command + " dump dicts --encoding=utf-8"); //$NON-NLS-1$
			BufferedReader spellReader =
				new BufferedReader(new InputStreamReader(spellProcess.getInputStream()));
			response = spellReader.readLine();
			Vector<String> result = new Vector<String>();
			while (response != null && !response.equals("")) { //$NON-NLS-1$
				result.add(response);
				response = spellReader.readLine();
			}
			String[] dicts = new String[result.size()];
			for (int i = 0; i < result.size(); i++) {
				dicts[i] = result.get(i);
			}
			return dicts;
		} catch (IOException e) {
			logger.error(Messages.getString("qa.spellCheck.all.log3"), e);
			return new String[0];
		}
	}

	public Hashtable<String, String> getDictionaryList() {
		return dictionaryList;
	}
	
}
