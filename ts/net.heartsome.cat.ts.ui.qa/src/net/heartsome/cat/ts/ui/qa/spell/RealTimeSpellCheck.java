package net.heartsome.cat.ts.ui.qa.spell;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.ts.core.bean.SingleWord;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.nonTransElement.NonTransElementOperate;
import net.heartsome.cat.ts.ui.qa.spell.inter.HSSpellChecker;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.qa.IRealTimeSpellCheck;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实时拼写检查实现类，之前考虑将些类做成单例模式，但后来将 RealTimeSpellCheckTrigger 做成了单例模式，故这个类不需要再这样做了。
 * @author robert	2013-01-21
 */
public class RealTimeSpellCheck implements IRealTimeSpellCheck, IPropertyChangeListener{
	/** 拼写检查器类型 */
	private boolean isHunspell = false;
	private HSSpellChecker spelling = null;
	private NonTransElementOperate nontransOper;
	private IPreferenceStore preferenceStore;
	private boolean isRealTimeSpell = false;
	private static StringTokenizer stringToken;
	/** 是否忽略非译元素 */
	private boolean ignoreNontrans;
	/** 是否忽略单词首字母为数字 */
	private boolean ignoreDigitalFirst;
	/** 是否忽略单词首字母为大写 */
	private boolean ignoreUpperCaseFirst;
	/** 忽略全大写单词 */
	private boolean ignoreAllUpperCase;

	private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeSpellCheck.class.getName());
	
	public RealTimeSpellCheck(){
		preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(this);
		
		loadParams();
	}
	
	private void loadParams(){
		isHunspell = preferenceStore.getBoolean(QAConstant.QA_PREF_isHunspell);
		isRealTimeSpell = preferenceStore.getBoolean(QAConstant.QA_PREF_realTimeSpell);
		ignoreNontrans = preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreNontrans);
		ignoreDigitalFirst = preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreDigitalFirst);
		ignoreUpperCaseFirst = preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreUpperCaseFirst);
		ignoreAllUpperCase = preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreAllUpperCase);
		
		if (isHunspell) {
			if (ignoreNontrans) {
				nontransOper = new NonTransElementOperate();
				nontransOper.openNonTransDB();
			}
			spelling = new Hunspell(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		}else {
			spelling = new AspellChecker();
		}
		
	}
	
	// interface
	public boolean checkLangAvailable(String language){
		// 首选项是否开启实时拼写检查
		if (!isRealTimeSpell) {
			return false;
		}
		return spelling.checkLangAvailable(language);
	}

	// interface
	public List<SingleWord> getErrorWords(String tgtText, String language) {
		if (isHunspell) {
			LinkedList<SingleWord> wordList = new LinkedList<SingleWord>();
			getSingleWords(tgtText, wordList);
			return spelling.getErrorWords(null, wordList, language);
		}else {
			List<Integer> tagPositionList = getTagPosition(tgtText);
			String pureText = PlaceHolderEditModeBuilder.PATTERN.matcher(tgtText).replaceAll("");
			LinkedList<SingleWord> wordList = new LinkedList<SingleWord>();
			getSingleWords(tgtText, wordList);
			spelling.setTagPosition(tagPositionList);
			return spelling.getErrorWords(pureText, wordList, language);
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		loadParams();
	}

	/**
	 * 获取单个单词，这里面主要是根据不同的选项。得到要进行拼写检查的单词
	 */
	private void getSingleWords(String tgtText, List<SingleWord> tgtWordList) {
		stringToken = new StringTokenizer(tgtText, Constant.SEPARATORS, false);
		List<Integer[]> ignoreParaList = null;
		// 如果要处理忽略非译元素，那么执行如下操作 (备注：所有的忽略项。只是针对 hunspell)
		if (isHunspell && ignoreNontrans) {
			List<Integer> tagPositionList = getTagPosition(tgtText);
			String pureText = PlaceHolderEditModeBuilder.PATTERN.matcher(tgtText).replaceAll("");
			ignoreParaList = nontransOper.getIgnorePara(pureText, tagPositionList);
		}
		
		int start = 0;
		int length = 0;
		int end = 0;
		while(stringToken.hasMoreTokens()){
			String word = stringToken.nextToken();
			String pureWord = PlaceHolderEditModeBuilder.PATTERN.matcher(word).replaceAll("");
			start = tgtText.indexOf(word, start);
			length = word.length();
			end = start + length;
			
			// 经过一系列的判断，从而删除一些不符合标准的单词。将剩下的单词传入拼写检查器中进行检查
			if (isHunspell) {
				// 是否忽略非译元素
				if (ignoreNontrans) {
					boolean needIgnore = false;
					for(Integer[] ignoreIndexs : ignoreParaList){
						if (start >= ignoreIndexs[0] && end <= ignoreIndexs[1] ) {
							needIgnore = true;
							break;
						}
					}
					if (needIgnore) {
						start = start + word.length();
						continue;
					}
				}
				// 是否忽略首字母为数字
				if (ignoreDigitalFirst && checkDigitalFirst(pureWord)) {
					start = start + word.length();
					continue;
				}
				// 是否忽略首字母为大写
				if (ignoreUpperCaseFirst && checkUpperCaseFirst(pureWord)) {
					start = start + word.length();
					continue;
				}
				
				//是否忽略全大写单词
				if (ignoreAllUpperCase && checkAllUpperCase(pureWord)) {
					start = start + word.length();
					continue;
				}
			}
			
			tgtWordList.add(new SingleWord(word, pureWord, start, length));
			start = start + word.length();
		}
	}
	
	/**
	 * 检查单词是否首字母大写
	 * @return
	 */
	private boolean checkDigitalFirst(String pureWord){
		return Character.isDigit(pureWord.charAt(0));
	}
	
	/**
	 * 检查单词是否首字母为数字
	 * @return
	 */
	private boolean checkUpperCaseFirst(String pureWord){
		for (int i = 0; i < pureWord.length(); i++) {
			if (Character.isDigit(pureWord.charAt(i))) {
				continue;
			}else {
				return Character.isUpperCase(pureWord.charAt(i));
			}
		}
		return true;
	}
	
	/**
	 * 检查单词是否首字母为数字
	 * @return
	 */
	private boolean checkAllUpperCase(String pureWord){
		return pureWord.equals(pureWord.toUpperCase());
	}
	
	/**
	 * 获取每个标记占位符的位置
	 * @param text
	 */
	private List<Integer> getTagPosition(String text){
		List<Integer> tagPostionList = new LinkedList<Integer>();
		Matcher matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(text);
		while(matcher.find()){
			tagPostionList.add(matcher.start());
		}
		return tagPostionList;
	}
	
	public static void main(String[] args) {
		String pureWord = "@1RObert";
		if (Character.isDigit(pureWord.charAt(0))) {
			System.out.println("是的");
		}else {
			System.out.println("不是的");
		}
	}
}
