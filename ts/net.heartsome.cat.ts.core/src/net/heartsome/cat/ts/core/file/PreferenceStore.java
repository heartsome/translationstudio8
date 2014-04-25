package net.heartsome.cat.ts.core.file;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

public class PreferenceStore {

	private static IPreferenceStore store = PlatformUI.getPreferenceStore();

	private static String separator = "###SEP###";
	private static String separator1 = "###SEP1###";

	public static LinkedHashMap<String, String> getMap(String key) {
		String filterString = store.getString(key);
		if (filterString == null || "".equals(filterString)) {
			return null;
		}
		String[] temp = filterString.split(separator);
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		String value;
		for (int i = 0; i < temp.length; i += 2) {
			value = "";
			if (i + 1 < temp.length) {
				value = temp[i + 1];
			}
			map.put(temp[i], value);
		}
		return map;
	}

	public static void saveMap(String key, Map<String, String> map) {
		if (map == null) {
			return;
		}
		if(map.isEmpty()){
			store.setValue(key, "");
		}
		StringBuffer temp = new StringBuffer();
		for (Entry<String, String> entry : map.entrySet()) {
			temp.append(entry.getKey());
			temp.append(separator);
			temp.append(entry.getValue());
			temp.append(separator);
		}
		temp.delete(temp.length() - separator.length(), temp.length());
		store.setValue(key, temp.toString());
	}
	
	public static LinkedHashMap<String, ArrayList<String[]>> getCustomCondition(String key) {
		String filterString = store.getString(key+"_custom");
		if (filterString == null || "".equals(filterString)) {
			return null;
		}
		String[] temp = filterString.split(separator);
		LinkedHashMap<String, ArrayList<String[]>> map = new LinkedHashMap<String, ArrayList<String[]>>();
		for (int i = 0; i < temp.length; i += 2) {
			ArrayList<String[]> value = new ArrayList<String[]>();
			if (i + 1 < temp.length) {
				String[] tempValue = temp[i + 1].split(separator1);
				for (int j = 0; j < tempValue.length; j++) {
					value.add(tempValue[j].split(","));
				}
			}
			map.put(temp[i], value);
		}
		return map;
	}

	public static void saveCustomCondition(String key, LinkedHashMap<String, ArrayList<String[]>> map) {
		if (map == null) {
			return;
		}
		if(map.isEmpty()){
			store.setValue(key, "");
		}
		StringBuffer temp = new StringBuffer();
		for (Entry<String, ArrayList<String[]>> entry : map.entrySet()) {
			temp.append(entry.getKey());
			temp.append(separator);
			for(String[] temp1 : entry.getValue()){
				StringBuilder tmpSb = new StringBuilder();
				for(String tmpStr : temp1){
					tmpSb.append(tmpStr.trim());
					tmpSb.append(",");
				}
				if(tmpSb.length()>0){
					tmpSb.delete(tmpSb.length()-1, tmpSb.length());
					temp.append(tmpSb+separator1);
				}
			}
			if(entry.getValue().size()>0){
				temp.delete(temp.length() - separator1.length(), temp.length());
			}
			temp.append(separator);
		}
		temp.delete(temp.length() - separator.length(), temp.length());
		store.setValue(key+"_custom", temp.toString());
	}

}
