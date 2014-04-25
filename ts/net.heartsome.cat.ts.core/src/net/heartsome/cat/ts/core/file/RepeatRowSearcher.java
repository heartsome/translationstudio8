package net.heartsome.cat.ts.core.file;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.ts.core.bean.FuzzyTransDataBean;


/**
 * 获取重复文本段
 * @author robert	2012-09-19
 */
public class RepeatRowSearcher {
	private static boolean ignoreTag = true;
	private static boolean isIgnoreCase = true;
	private boolean isEditor = false;
	
	public RepeatRowSearcher(){ }
	
	/**
	 * 针对 nattable 编辑器上的重复文本段过滤条件，获取重复文本段的 rowId
	 */
	public static ArrayList<String> getRepeateRowsForFilter(XLFHandler handler, 
			Map<String, String> xliffXmlnsMap) throws Exception{
		
		long time1 = System.currentTimeMillis();
		Map<String, String> allSrcTextMap = new LinkedHashMap<String, String>(); 
		for(Entry<String, String> xmlNsEntry : xliffXmlnsMap.entrySet()){
			// 获取每个文件名
			String fileName = xmlNsEntry.getKey();
			allSrcTextMap.putAll(handler.getAllSrcTextForRepeat(fileName, ignoreTag, null, null));
		}
		ArrayList<String> resultList = new ArrayList<String>();
		
		List<Entry<String, String>> mapList = new ArrayList<Entry<String, String>>(
				allSrcTextMap.entrySet());
		
		//排序
//		Collections.sort(mapList, new Comparator<Entry<String, String>>() {   
//		    public int compare(Entry<String, String> o1, Entry<String, String> o2) {      
//		        return o1.getValue().compareTo(o2.getValue());
////		    	if (isIgnoreCase ? o1.getValue().equalsIgnoreCase(o2.getValue()) : o1.getValue().equals(o2.getValue())) {
////					return 0;
////				}else {
////					return 1;
////				}
////		    	return o1.getValue().compareTo(o2.getValue()) != 0 ? 1 : o1.getValue().compareTo(o2.getValue());
//		    }
//		}); 
		
		for (int i = 0; i < mapList.size(); i++) {
			Entry<String, String> entry = mapList.get(i);
			boolean isSame = false;
			for (int j = i + 1; j < mapList.size(); j++) {
				Entry<String, String> curEntry = mapList.get(j);
				if (entry.getValue().length() != (curEntry.getValue().length())) {
					continue;
				}
				if (entry.getValue().equalsIgnoreCase(curEntry.getValue())) {
					if (!isSame) {
						resultList.add(entry.getKey());
					}
					isSame = true;
					resultList.add(curEntry.getKey());
					mapList.remove(j);
					j --;
				}
			}
		}


//		for (int i = 0; i < mapList.size(); i++) {
//			Entry<String, String> entry = mapList.get(i);
//			String srcText = entry.getValue();
//			String rowId = entry.getKey();
//			
//			for (int j = i + 1; j < mapList.size(); j++) {
//				Entry<String, String> curEntry = mapList.get(j);
//				String curSrcText = curEntry.getValue();
//				String curRowId = curEntry.getKey();
//
//				if (isIgnoreCase ? curSrcText.equalsIgnoreCase(srcText) : curSrcText.equals(srcText)) {
//					if (j == i + 1) {
//						resultList.add(rowId);
//					}
//					resultList.add(curRowId);
//					if (j == mapList.size() - 1) {
//						i = j - 1;
//					}
//				}else {
//					i = j - 1;
//					break;
//				}
//			}
//		}
		
		System.out.println("所需时间为 = " + (System.currentTimeMillis() - time1));
		return resultList;
	}
	

	/**
	 * 针对繁殖翻译，获取所有满足条件的rowId
	 */
	public static Map<String, List<String>> getRepeateRowsForFuzzy(XLFHandler handler) {
		
		Map<String, List<String>> resultMap = new LinkedHashMap<String, List<String>>();
		// 获取当前界面上所显示的所有行的 rowId
		ArrayList<String> rowIdList = handler.getRowIds();
		Map<String, FuzzyTransDataBean> srcTextMap = handler.getAllSrcTextForFuzzy(rowIdList, ignoreTag);

		// 排序
		List<Map.Entry<String, FuzzyTransDataBean>> mapList = new ArrayList<Map.Entry<String, FuzzyTransDataBean>>(
				srcTextMap.entrySet());
		
		Map<String, FuzzyTransDataBean> sameMap = new LinkedHashMap<String, FuzzyTransDataBean>();
		for (int i = 0; i < mapList.size(); i++) {
			Entry<String, FuzzyTransDataBean> entry = mapList.get(i);
			FuzzyTransDataBean bean = entry.getValue();
			boolean isSame = false;
			sameMap.clear();
			for (int j = i + 1; j < mapList.size(); j++) {
				Entry<String, FuzzyTransDataBean> curEntry = mapList.get(j);
				FuzzyTransDataBean curBean = curEntry.getValue();
				if (bean.getSrcText().equalsIgnoreCase(curBean.getSrcText())) {
					if (!isSame) {
						sameMap.put(entry.getKey(), bean);
					}
					isSame = true;
					sameMap.put(curEntry.getKey(), curBean);
					mapList.remove(j);
					j --;
				}
			}
			ananysisFuzzyDataMap(sameMap, resultMap);
		}
		
		return resultMap;
	}
	
	/**
	 * 处理源文相同的文本段。得到要进行繁殖翻译的 rowId
	 * @param sameMap
	 * @param resultMap
	 */
	private static void ananysisFuzzyDataMap(Map<String, FuzzyTransDataBean> sameMap, Map<String, List<String>> resultMap){
		// 先获取目标文本不为空的文本段。以最后一个为主
		String rootRowId = "";
		for(Entry<String, FuzzyTransDataBean> entry : sameMap.entrySet()){
			if (!entry.getValue().isTgtNull()) {
				rootRowId = entry.getKey();
			}
		}
		if ("".equals(rootRowId)) {
			return;
		}
		
		List<String> childRowIdList = new ArrayList<String>();
		for (Entry<String, FuzzyTransDataBean> entry : sameMap.entrySet()) {
			FuzzyTransDataBean bean = entry.getValue();
			if (bean.isTgtNull() && !bean.isLock()) {
				childRowIdList.add(entry.getKey());
			}
		}
		if (childRowIdList.size() > 0) {
			resultMap.put(rootRowId, childRowIdList);
		}
	}
	
	/**
	 * 针对锁定内部重复文本段而写的方法
	 * @param handler
	 * @param xliffXmlnsMap
	 * @param srcLan	源语言
	 * @param tgtLan	目标语言
	 * @return
	 */
	public static ArrayList<String> getRepeateRowsForLockInterRepeat(XLFHandler handler, 
			Map<String, String> xliffXmlnsMap, String srcLan, String tgtLan) {
		ArrayList<String> resultList = new ArrayList<String>();
		
		Map<String, String> allSrcTextMap = new LinkedHashMap<String, String>(); 
		for(Entry<String, String> xmlNsEntry : xliffXmlnsMap.entrySet()){
			// 获取每个文件名
			String fileName = xmlNsEntry.getKey();
			allSrcTextMap.putAll(handler.getAllSrcTextForRepeat(fileName, ignoreTag, srcLan, tgtLan));
		}
		
		List<Map.Entry<String, String>> mapList = new ArrayList<Map.Entry<String, String>>(
				allSrcTextMap.entrySet());

		for (int i = 0; i < mapList.size(); i++) {
			Entry<String, String> entry = mapList.get(i);
			for (int j = i + 1; j < mapList.size(); j++) {
				Entry<String, String> curEntry = mapList.get(j);
				if (entry.getValue().length() != (curEntry.getValue().length())) {
					continue;
				}
				if (entry.getValue().equalsIgnoreCase(curEntry.getValue())) {
					resultList.add(curEntry.getKey());
					mapList.remove(j);
					j --;
				}
			}
		}
		
		return resultList;
	}
	
	/**
	 * 针对字数分析的结果，获取其除第一个之外的其他相同文本段的list
	 * <div style='color:red'>该方法已经不再采用，因为要求第一个重复的文本段不进行锁定，后来的需求又是，第一个重复文本段应算成新字数。</div>
	 * @param rowIdMap
	 * @return
	 */
	public static ArrayList<String> getRepeateRowsEscapeFirstForFA(Map<String, String> rowIdMap){
		ArrayList<String> resultList = new ArrayList<String>();

		List<Entry<String, String>> mapList = new ArrayList<Entry<String, String>>(
				rowIdMap.entrySet());
		for (int i = 0; i < mapList.size(); i++) {
			Entry<String, String> entry = mapList.get(i);
			for (int j = i + 1; j < mapList.size(); j++) {
				Entry<String, String> curEntry = mapList.get(j);
				if (entry.getValue().length() != (curEntry.getValue().length())) {
					continue;
				}
				if (entry.getValue().equalsIgnoreCase(curEntry.getValue())) {
					resultList.add(curEntry.getKey());
					mapList.remove(j);
					j --;
				}
			}
		}
		return resultList;
	}
	
	

}
