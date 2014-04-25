/**
 * Task.java
 *
 * Version information :
 *
 * Date:2013-1-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.translation.view;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.tm.complexMatch.IComplexMatch;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TranslationTaskContainer {

	private List<TranslationTaskData> dataList;
//	
//	private List<ISimpleMatcher> simpleMatchers;
//
//	private List<IComplexMatch> complexMatchers;

	public TranslationTaskContainer() {
		dataList = new ArrayList<TranslationTaskData>();
	}

	public synchronized void clearContainer(){
		dataList.clear();
	}
	
	public boolean isEmpty() {
		if(dataList.size() == 0){
			return true;
		}
		return false;
	}

	public synchronized TranslationTaskData popTranslationTask() {
		if (dataList.size() != 0) {
			return dataList.remove(dataList.size() - 1);
		}
		return null;
	}

	public synchronized void pushTranslationTask(TranslationTaskData data) {
		Object matcher = data.getMatcher();
		if (matcher instanceof ISimpleMatcher) {
			ISimpleMatcher _matcher = (ISimpleMatcher) matcher;
			for(TranslationTaskData d: dataList){
				Object m = d.getMatcher();
				if(m instanceof ISimpleMatcher){
					ISimpleMatcher _m = (ISimpleMatcher) m;
					if (_m.getMathcerToolId().equals(_matcher.getMathcerToolId())) {
						return;
					}
				}
			}
		} else if (matcher instanceof IComplexMatch) {
			IComplexMatch _matcher = (IComplexMatch) matcher;
			for(TranslationTaskData d: dataList){
				Object m = d.getMatcher();
				if(m instanceof IComplexMatch){
					IComplexMatch _m = (IComplexMatch) m;
					if (_m.getToolId().equals(_matcher.getToolId())) {
						return;
					}
				}
			}			
		}
		dataList.add(0, data);
	}
}
