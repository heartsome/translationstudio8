/**
 * IMatchViewPart.java
 *
 * Version information :
 *
 * Date:2012-9-21
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.view;

import net.heartsome.cat.ts.tm.complexMatch.IComplexMatch;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public interface IMatchViewPart {

	/**
	 * 刷新Table ;
	 */
	void refreshTable();

	/**
	 * 重新加载所有匹配
	 * @param editor
	 *            当前正在编辑的编辑器
	 * @param rowIndex
	 *            当前选中的文本段索引;
	 */
	void reLoadMatches(IXliffEditor editor, int rowIndex);

	/**
	 * 接受匹配面板中的第 index 个匹配
	 * @param index
	 *            匹配面板中的序号，从0开始
	 */
	void acceptMatchByIndex(int index);

	/**
	 * 执行复杂匹配（目前只实现了快速翻译)
	 * @param complexMatcher
	 *            ;
	 */
	void manualExecComplexTranslation(int rowIndex, IXliffEditor editor, IComplexMatch complexMatcher);

	/**
	 * 执行简单匹配（目前只实现了google,bing）
	 * @param simpleMatcher
	 *            ;
	 */
	void manualExecSimpleTranslation(int rowIndex, IXliffEditor editor, ISimpleMatcher simpleMatcher);
}
