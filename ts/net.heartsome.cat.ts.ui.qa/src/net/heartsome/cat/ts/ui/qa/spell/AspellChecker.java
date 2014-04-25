package net.heartsome.cat.ts.ui.qa.spell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.heartsome.cat.ts.core.bean.SingleWord;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.qa.spell.inter.HSSpellChecker;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspell拼写检查品
 * @author  robert	2012-02-07
 * @version 
 * @since   JDK1.6
 */
public class AspellChecker implements HSSpellChecker {

	private Shell shell;
	/** aspell 运行路径 */
	private String command;

	private BufferedReader spellReader;
	private BufferedWriter spellWriter;
	private String response;
	private  Process spellProcess;

	private List<SingleWord> errorWords = new LinkedList<SingleWord>();

	static String spellCheckLinePrefix = "^"; 
	Runtime runtime;
	
	private static IWorkspaceRoot root;
	private AspellConfig aspellConfig;
	private boolean isError = false;
	/** aspell 运行靠的是输入命令，因此这里面保存的是命令行代码，key 为 lang */
	private Map<String, String> commandLineMap;
	
	private List<Integer> tagPositionList;
	private List<SingleWord> wordList;
	
	public final static Logger logger = LoggerFactory.getLogger(AspellChecker.class.getName());
	
	// UNDO 今天发现 aspell 拼写检查器配置失败时，程序的提示框有问题，是线程异常的问题，应该改正。 --robert	2012-07-23
	public AspellChecker () {
		runtime = Runtime.getRuntime();
		root = ResourcesPlugin.getWorkspace().getRoot();
		
		String aspellConfigFile = root.getLocation().append(QAConstant.QA_SPELL_ASPELLCONFIGFILE).toOSString();
		if (!new File(aspellConfigFile).exists()) {
			logger.error(Messages.getString("qa.spellCheck.AspellChecker.log1"));
		}else {
			aspellConfig = new AspellConfig( aspellConfigFile);
			commandLineMap = new HashMap<String, String>();
			command = aspellConfig.getCommand();
			
			//如果路径与语言都没有配置，那么，Aspell都不可用，为空
//			if (("".equals(command) || "".equals(defaultDictionary) || defaultDictionary == null) && !isSpellNull) {
//				isSpellNull = true;
//				logger.error(Messages.getString(MessageFormat.format(
//						Messages.getString("qa.spellCheck.AspellChecker.log2"), target_lan)));
//			}
			
			//如果路径与语言都没有配置，那么，Aspell都不可用，为空
			if ("".equals(command)) {
				logger.error(Messages.getString(Messages.getString("qa.spellCheck.AspellChecker.log2")));
			}
		}
	}
	
	public List<SingleWord> getErrorWords(String pureText, List<SingleWord> wordList, String language) {
		this.wordList = wordList;
		errorWords.clear();

		if (!commandLineMap.containsKey(language)) {
			createCommandLine(language);
		}
		String commandLine = commandLineMap.get(language);
		// commandLine = /usr/bin/aspell --encoding=utf-8 -a --lang=en-US --master=en_US
		//               /usr/bin/aspell --encoding=utf-8 -a --lang=en-US --master=en_US
//		System.out.println("commandLine = " + commandLine);

		processTarget_1(commandLine, pureText.replace('\n', '\u0007'));
		
		return errorWords;
	}
	
	/**
	 * 创建 运行命令行代码
	 * @param language
	 */
	private void createCommandLine(String language){
		String dictionary = aspellConfig.getDictionaryForLang(language);
	
//		language = TextUtil.normLanguage(language);
		String commandLine = command + " -a --lang=" + language; 
		if (!dictionary.equals("")) { 
			commandLine = commandLine + " --master=" + dictionary; 
		}
		commandLineMap.put(language, commandLine);
	}
	
	/**
	 * 这是之前的方法，现在不于调用
	 */
	private void processTarget(String commandLine, String target) {
		try {
			spellProcess = runtime.exec(commandLine);
			
			spellReader = new BufferedReader(new InputStreamReader(spellProcess.getInputStream()));
			spellWriter = new BufferedWriter(new OutputStreamWriter(spellProcess.getOutputStream()));
			// 跳过aspell的版本号
			spellReader.readLine();
			
//			spellProcess.waitFor();
			target = target.replace('\n', '\u0007');
			spellWriter.write(spellCheckLinePrefix + target);
			spellWriter.newLine();
			spellWriter.flush();
			response = spellReader.readLine();
			
		} catch (Exception e) {
			isError = true;
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(shell, Messages.getString("qa.all.dialog.error"), Messages.getString("qa.spellCheck.AspellChecker.tip1"));
				}
			});
			logger.error(Messages.getString("qa.spellCheck.AspellChecker.tip1"), e);
			return;
		}
		checkStatus();
		
		try {
			spellReader.close();
			spellWriter.close();
			spellProcess.destroy();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.spellCheck.all.log1"), e);
		}
	}
	
	
	private void processTarget_1(final String commandLine, final String target) {
		try {
			spellProcess = runtime.exec(commandLine);

			spellReader = new BufferedReader(new InputStreamReader(
					spellProcess.getInputStream()));
			spellWriter = new BufferedWriter(new OutputStreamWriter(
					spellProcess.getOutputStream()));

			new Thread() {
				public void run() {
					spellReader = new BufferedReader(new InputStreamReader(
							spellProcess.getInputStream()));
					spellWriter = new BufferedWriter(new OutputStreamWriter(
							spellProcess.getOutputStream()));
					try {
						// 跳过aspell的版本号
						spellReader.readLine();
						spellWriter.write(spellCheckLinePrefix + target);
						spellWriter.newLine();
						spellWriter.flush();
						response = spellReader.readLine();
						
						checkStatus();
					} catch (IOException e) {
						e.printStackTrace();
						isError = true;
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(shell, Messages.getString("qa.all.dialog.error"), Messages.getString("qa.spellCheck.AspellChecker.tip1"));
							}
						});
						logger.error(Messages.getString("qa.spellCheck.AspellChecker.tip1"), e);
						return;
					} finally {
						try {
							spellReader.close();
							spellWriter.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();

			new Thread() {
				public void run() {
					BufferedReader errorReader = new BufferedReader(
							new InputStreamReader(spellProcess.getErrorStream()),
							4096);
					try {
						String line2 = null;
						while (spellProcess != null && errorReader != null && (line2 = errorReader.readLine()) != null) {
							if (line2 != null) {
//								System.out.println("line2 =" + line2);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							errorReader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
			
			int status = spellProcess.waitFor();
			if (status != 0) {
				isError = true;
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(shell, Messages.getString("qa.all.dialog.error"), Messages.getString("qa.spellCheck.AspellChecker.tip1"));
					}
				});
				logger.error(Messages.getString("qa.spellCheck.AspellChecker.tip1"));
			}
			spellProcess.destroy();
			
			try {
				spellReader.close();
				spellWriter.close();
				spellProcess.getErrorStream().close();
				spellProcess.getInputStream().close();
				spellProcess.getOutputStream().close();
			} catch (Exception ee) {
				logger.error("Aspell check error", ee);
			}
		} catch (Exception e) {
			try {
				if ((spellReader != null)) {
					spellReader.close();
				}
				if ((spellWriter != null)) {
					spellWriter.close();
				}
				if (spellProcess != null) {
					spellProcess.getErrorStream().close();
					spellProcess.getInputStream().close();
					spellProcess.getOutputStream().close();
				}
			} catch (Exception ee) {
				logger.error("Aspell check error", ee);
			}
		}
	}

	/**
	 * aspell 词典是否发生错误，如果错误，将不再进行拼写检查
	 * @return
	 */
	public boolean isError() {
		return isError;
	}

	private void checkStatus() {
		try {
			while (response != null && !response.equals("")) { 
				if (isError) {
					return;
				}
				if (response.equals("*")) {
					// 当前单词是存在于词典中的
					spellWriter.write(spellCheckLinePrefix);
					spellWriter.newLine();
					spellWriter.flush();
					response = spellReader.readLine();
				} else {
					if (response.startsWith("&") || response.startsWith("#")) { 
						parseSuggestions(response);
						response = spellReader.readLine();
					}
				}
			}
			if (response == null || response.equals("")) { 
				return;
			}
		} catch (IOException e) {
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
			box.setMessage(e.getMessage());
			box.open();
			logger.error(Messages.getString("qa.spellCheck.all.log2"), e);
        }
	}

    /**
     * 分解查询结果，将错误单词添加到结果集中
     * 查询结果如：& yayay 62 1: Maya, ayah, ya, Yalu, Yuan, yaws, yuan, yaw, 第二个值为错误单词，第三个值为建议单词个数，第四个值为该单词起始下标（从1开始的）
     * @param line ;
     */
	private void parseSuggestions(String line) {
		StringTokenizer st = new StringTokenizer(line);
		if (st.hasMoreTokens()) {
			st.nextToken();
			st.nextToken();
			st.nextToken();
			
			int start = -1;
			if (st.hasMoreTokens()) {
				// 备注，aspell 的查询结果，每个单词的起始坐标是从 1 开始的
				start = Integer.parseInt(st.nextToken().replace(":", "")) - 1;
			}else {
				return;
			}
			
			int startAdd = 0;
			if (tagPositionList != null) {
				// 将标记放回去，使每个非译片段回复之前未去标记的状态
				for(Integer tagIndex : tagPositionList){
					if (start > tagIndex) {
						startAdd ++;
					}
				}
				start = start + startAdd;
			}
			
			boolean exsit = false;
			for(SingleWord word : wordList){
				if (word.getStart() == start) {
					errorWords.add(word);
					exsit = true;
					break;
				}
			}
			
			if (!exsit) {
				logger.error("Aspell check error.");
			}
		}
	}


	public boolean langIsLoad(String language) {
		return false;
	}

	public boolean checkLangAvailable(String language) {
		if (aspellConfig == null || aspellConfig.getDictionaryList() == null) {
			return false;
		}
		return aspellConfig.getDictionaryList().containsKey(language);
	}

	public void setTagPosition(List<Integer> tagPositionList) {
		this.tagPositionList = tagPositionList;
	}
	
}