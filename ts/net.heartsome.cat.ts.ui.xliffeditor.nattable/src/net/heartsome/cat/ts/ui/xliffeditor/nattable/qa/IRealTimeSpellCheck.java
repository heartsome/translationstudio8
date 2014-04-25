package net.heartsome.cat.ts.ui.xliffeditor.nattable.qa;

import java.util.List;

import net.heartsome.cat.ts.core.bean.SingleWord;

/**
 * 实时检查的接口
 * @author robert	2013-01-21
 */
public interface IRealTimeSpellCheck {
	/** 
	 * 根据传入的文本段以及语种，获取错误的单词 
	 * @param tgtText ,这是获取的带标记的目标文本段
	 */
	List<SingleWord> getErrorWords(String tgtText, String language);
	
	/**
	 * 根据传入的语言，检查当前拼写检查器是否支持
	 * @param language
	 * @return
	 */
	boolean checkLangAvailable(String language);
	
	
}
