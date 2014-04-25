package net.heartsome.cat.ts.ui.qa.spell.inter;

import java.util.List;

import net.heartsome.cat.ts.core.bean.SingleWord;

/**
 * 两种拼写检查类的接口
 * @author  robert	2012-02-06
 * @version 
 * @since   JDK1.6
 */
public interface HSSpellChecker {
	/** 
	 * 获取拼写检查中查询不到的单词
	 * @param pureText	纯文，该参数用于 aspell
	 * @param tgtWordList	经过拆分后的单词集合，该参数用于 hunspell
	 */
	public List<SingleWord> getErrorWords(String pureText, List<SingleWord> tgtWordList, String language);
	/** 词典是否发生错误，如果错误，将不再进行拼写检查 */
	public boolean isError();
	/** 标记当前语言的拼写检查器是否被初始化，如果没有初始化。那么还要初始化 <div style='color:red'>目前该方法只用于 hunspell </div> */
	public boolean langIsLoad(String language);
	/** 针对拼写检查。确定当前语言是否支持 */
	public boolean checkLangAvailable(String language);
	/** 用于 aspell ，向 aspell 传入所有标记的下标值 */
	public void setTagPosition(List<Integer> tagPositionList);
	
}
