package net.heartsome.cat.tmx.converter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;

/**
 * 该类的作用是： TmxTU对象转换成XML格式的字符串
 * @author yule
 * @version
 * @since JDK1.6
 */
public class Model2String {

	/**
	 * 将TmxTU转换成TMX格式字XML字符串
	 * @param tu
	 *            ：TmxTU对象
	 * @param fullText
	 *            ：是否使用完整文本还是纯文本
	 * @return ;
	 */
	public static String TmxTU2TmxXmlString(TmxTU tu, boolean fullText,String attributeString) {
		List<TmxSegement> segments = tu.getSegments();
		StringBuilder sb = new StringBuilder();
		tu.setTuId(getUUID());
		sb.append("<tu id=\"" + tu.getTuId() + "\">\n");
		if(attributeString != null && !attributeString.trim().isEmpty()){
			sb.append(attributeString);
		}
		List<TmxSegement> seg = new ArrayList<TmxSegement>();
		if(null !=tu.getSource() ){			
			seg.add(tu.getSource());
		}
		if (segments != null && !segments.isEmpty()) {
			seg.addAll(segments);
		}
		for (int i = 0; i < seg.size(); i++) {
			sb.append(getTUXmlString(seg.get(i), tu.getTuId() + "_" + i, fullText));
		}
		sb.append("</tu>\n");

		return sb.toString();
	}

	/**
	 * 将TmxTU转换成Tbx格式字XML字符串
	 * @param tu
	 *            ：TmxTU对象
	 * @param fullText
	 *            ：是否使用完整文本还是纯文本
	 * @return ;
	 */
	public static String TmxTu2TbxXMLString(TmxTU tu, boolean fullText) {
		if(null == tu){
			return "";
		}
		List<TmxSegement> segments = tu.getSegments();
		StringBuilder sb = new StringBuilder();
		sb.append("<termEntry id=\"" + tu.getTuId() + "\">\n");
		List<TmxSegement> seg = new ArrayList<TmxSegement>();
		if(tu.getSource()!=null){			
			seg.add(tu.getSource());
		}
		if (segments != null && !segments.isEmpty()) {
			seg.addAll(segments);
		}
		for (int i = 0; i < seg.size(); i++) {
			sb.append(getLangSet(seg.get(i), getUUID() + "_" + i, fullText));
		}
		sb.append("</termEntry>\n");

		return sb.toString();
	}

	private static String getLangSet(TmxSegement seg, String id, boolean fullText) {
		if (null == seg) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<langSet id=\"" + id + "\" xml:lang=\"" + seg.getLangCode() + "\">\n");
		sb.append("<tig>\n");
		if (fullText) {
			sb.append("<term>" + seg.getFullText() + "</term>\n");
		} else {
			sb.append("<term>" + seg.getPureText() + "</term>\n");
		}
		sb.append("</tig>\n</langSet>\n");
		return sb.toString();
	}

	private static String getTUXmlString(TmxSegement seg, String id, boolean fullText) {
		if (null == seg) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<tuv id=\"" + id + "\" xml:lang=\"" + seg.getLangCode() + "\">\n");
		sb.append("<seg>\n");
		if (fullText) {
			sb.append(seg.getFullText());
		} else {
			sb.append(seg.getPureText());
		}
		sb.append("</seg>\n</tuv>\n");
		return sb.toString();
	}

	public static String getUUID() {
		return UUID.randomUUID().toString();
		// return String.valueOf(System.currentTimeMillis());
	}

	/**
	 * 获取创建时间
	 * @return ;
	 */
	public static String creationDate() {
		Calendar calendar = Calendar.getInstance(Locale.US);
		String sec = (calendar.get(Calendar.SECOND) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.SECOND);
		String min = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.MINUTE);
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.HOUR_OF_DAY);
		String mday = (calendar.get(Calendar.DATE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.DATE);
		String mon = (calendar.get(Calendar.MONTH) < 9 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR); //$NON-NLS-1$

		String date = longyear + mon + mday + "T" + hour + min + sec + "Z"; //$NON-NLS-1$ //$NON-NLS-2$
		return date;
	}
	
	public static String getCustomArributeXML(Map<String,String> attributes){
		if(attributes== null ||attributes.isEmpty()){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Set<Entry<String,String>> entrySet = attributes.entrySet();
		for(Entry<String,String> entry :entrySet){
			sb.append("<prop type =\"");
			sb.append(entry.getKey());
			sb.append("\">");
			sb.append(entry.getValue());
			sb.append("</prop>");
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Map<String,String> attributes = new HashMap<String ,String>();
		attributes.put("a", "1");
		attributes.put("b", "2");
		System.out.println(getCustomArributeXML(attributes));
		
	}
}