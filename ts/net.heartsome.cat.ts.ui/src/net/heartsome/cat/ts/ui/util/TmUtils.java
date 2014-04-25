/**
 * TmUtils.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.util;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.bean.ColorConfigBean;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.ts.core.bean.AltTransBean;
import net.heartsome.cat.ts.core.bean.PropBean;
import net.heartsome.cat.ts.core.bean.PropGroupBean;

import org.eclipse.swt.graphics.Color;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmUtils {

	public static Color getMatchTypeColor(String type, String quality) {
		ColorConfigBean colorConfigBean = ColorConfigBean.getInstance();
		if (type.equals("PT")) {
			return colorConfigBean.getPtColor();
		} else if (type.equals("TM")) {
			if (quality.endsWith("%")) {
				quality = quality.substring(0, quality.lastIndexOf("%"));
			}
			Integer q = Integer.parseInt(quality);
			if (q == 101) {
				return colorConfigBean.getTm101Color();
			} else if (q == 100) {
				return colorConfigBean.getTm100Color();
			} else if (q > 89 && q < 100) {
				return colorConfigBean.getTm90Color();
			} else if (q > 79 && q < 90) {
				return colorConfigBean.getTm80Color();
			} else if (q > 69 && q < 80) {
				return colorConfigBean.getTm70Color();
			} else {
				return colorConfigBean.getTm0Color();
			}
		} else if (type.equals("QT")) {
			return colorConfigBean.getQtColor();
		} else if (type.equals("MT")) {
			return colorConfigBean.getMtColor();
		} 
		return null;
	}

	public static Vector<AltTransBean> fuzzyResult2Alttransbean(List<FuzzySearchResult> fuzzyResults){
		Vector<AltTransBean> altTrans = new Vector<AltTransBean>();
		for (FuzzySearchResult result : fuzzyResults) {
			AltTransBean atb = new AltTransBean();
			
//			Map<String, String> match = tu.getTuInfo();
			TmxTU tu = result.getTu();
			
			// 获取源节点内容、属性及纯文本
			atb.setSrcText(tu.getSource().getPureText());
			atb.setTgtText(tu.getTarget().getPureText());

			Hashtable<String, String> matchProps = new Hashtable<String, String>();
			matchProps.put("match-quality", result.getSimilarity()+"");
			matchProps.put("origin", result.getDbName());
			matchProps.put("tool-id", "Translation Memory");
			matchProps.put("hs:matchType", "TM");
			matchProps.put("xml:space", "default");
			atb.setMatchProps(matchProps);

			Hashtable<String, String> srcProps = new Hashtable<String, String>();
			srcProps.put("xml:lang", tu.getSource().getLangCode());
			atb.setSrcProps(srcProps);
			atb.setSrcContent(tu.getSource().getFullText());

			Hashtable<String, String> tgtProps = new Hashtable<String, String>();
			tgtProps.put("xml:lang", tu.getTarget().getLangCode());
			atb.setTgtProps(tgtProps);
			atb.setTgtContent(tu.getTarget().getFullText());

			
			Vector<PropGroupBean> pgs = new Vector<PropGroupBean>();
			Vector<PropBean> props = new Vector<PropBean>();			
			PropBean pb = new PropBean("creationId", tu.getCreationUser());
			props.add(pb);
			pb = new PropBean("creationDate", tu.getCreationDate());
			props.add(pb);
			pb = new PropBean("changeId", tu.getChangeUser());
			props.add(pb);
			pb = new PropBean("changeDate", tu.getChangeDate());
			props.add(pb);
			
			List<TmxProp> attrValList = tu.getProps();
			for (TmxProp attr : attrValList) {
				String name = attr.getName();
				if(name == null || name.equals("")){
					continue;
				}
				String value = attr.getValue();
				if (value == null || value.equals("")) {
					continue;
				}
				PropBean prop = new PropBean(name, value);
				props.add(prop);
			}

			PropGroupBean pg = new PropGroupBean(props);
			// 获取属性组名称。
			pg.setName("hs:prop-group");
			pgs.add(pg);
			atb.setPropGroups(pgs);
			atb.setFuzzyResult(result);
			altTrans.add(atb);
		}
		return altTrans;
	}
	/**
	 * 将从库中获取的匹配转成以 AltTransBean 封装的匹配数据,在转换的过程与当前 AltTrans重复的记录将被忽略
	 * @param dbMatches
	 *            从数据库中获取的匹配
	 * @param currentAltTrans
	 *            当前已经存原altTrans
	 * @return 和当前匹配不重复的AltTrans集;
	 */
	public static Vector<AltTransBean> altTransInfoConverter(List<FuzzySearchResult> dbMatches,
			Vector<AltTransBean> currentAltTrans) {
		Vector<AltTransBean> altTrans = new Vector<AltTransBean>();
		Vector<AltTransBean> existAltTrans = new Vector<AltTransBean>();		
		for (FuzzySearchResult result : dbMatches) {
			AltTransBean atb = new AltTransBean();
			
//			Map<String, String> match = tu.getTuInfo();
			TmxTU tu = result.getTu();
			// 获取源节点内容、属性及纯文本
			atb.setSrcText(tu.getSource().getPureText());
			atb.setTgtText(tu.getTarget().getPureText());

			if (isMatchExist(currentAltTrans, atb, result.getDbName(), existAltTrans)) {
				continue;
			}

			Hashtable<String, String> matchProps = new Hashtable<String, String>();
			matchProps.put("match-quality", result.getSimilarity()+"");
			matchProps.put("origin", result.getDbName());
			matchProps.put("tool-id", "Translation Memory");
			matchProps.put("hs:matchType", "TM");
			matchProps.put("xml:space", "default");
			atb.setMatchProps(matchProps);

			Hashtable<String, String> srcProps = new Hashtable<String, String>();
			srcProps.put("xml:lang", tu.getSource().getLangCode());
			atb.setSrcProps(srcProps);
			atb.setSrcContent(tu.getSource().getFullText());

			Hashtable<String, String> tgtProps = new Hashtable<String, String>();
			tgtProps.put("xml:lang", tu.getTarget().getLangCode());
			atb.setTgtProps(tgtProps);
			atb.setTgtContent(tu.getTarget().getFullText());

			
			Vector<PropGroupBean> pgs = new Vector<PropGroupBean>();
			Vector<PropBean> props = new Vector<PropBean>();
			
			PropBean pb = new PropBean("creationId", tu.getCreationUser());
			props.add(pb);
			pb = new PropBean("creationDate", tu.getCreationDate());
			props.add(pb);
			pb = new PropBean("changeId", tu.getChangeUser());
			props.add(pb);
			pb = new PropBean("changeDate", tu.getChangeDate());
			props.add(pb);
			
			List<TmxProp> attrValList = tu.getProps();
			for (TmxProp attr : attrValList) {
				String name = attr.getName();
				if(name == null || name.equals("")){
					continue;
				}
				String value = attr.getValue();
				if (value == null || value.equals("")) {
					continue;
				}
				PropBean prop = new PropBean(name, value);
				props.add(prop);
			}

			PropGroupBean pg = new PropGroupBean(props);
			// 获取属性组名称。
			pg.setName("hs:prop-group");
			pgs.add(pg);
			atb.setPropGroups(pgs);

			altTrans.add(atb);
		}
		if(altTrans.size() > 0){
			altTrans.addAll(existAltTrans);
		} else {
			currentAltTrans.addAll(existAltTrans);
		}
		return altTrans;
	}

	/**
	 * 判断当的匹配中是否已经存在了
	 * @param altTransVector
	 *            匹配集,已经存在的
	 * @param currAltTrans
	 *            当前匹配
	 * @return ;
	 */
	public static boolean isMatchExist(Vector<AltTransBean> altTransVector, AltTransBean currAltTrans, String currDbName
			,Vector<AltTransBean> dubliAlttrans) {
		String src = currAltTrans.getSrcText();
		String tgt = currAltTrans.getTgtText();
		if (src == null || src.equals("") || tgt == null || tgt.equals("")) {
			return true; // 忽略源文为空，译文为空的匹配
		}
		for (int i = 0; i < altTransVector.size(); i++) {
			AltTransBean existAltTrans = altTransVector.get(i);
			String existSrc = existAltTrans.getSrcText();
			String existTgt = existAltTrans.getTgtText();
			if (existSrc == null || existTgt == null) {
				continue; // 当前匹配源文、译文为空将忽略配置
			}
			if (existSrc.trim().equals(src.trim()) && existTgt.trim().equals(tgt.trim())) {
				dubliAlttrans.add(existAltTrans);
				altTransVector.remove(i);
				return true;
			}
			String existDbName =  existAltTrans.getMatchProps().get("origin");
			if (existSrc.trim().equals(src.trim()) && !existTgt.trim().equals(tgt.trim()) && existDbName.equals(currDbName)){
				altTransVector.remove(i);
				i--;
			}
		}
		return false;
	}

	/**
	 * 检查Vector中的数据是否超过最大个数,如果超过将取0-size范围内的数据
	 * @param tmpVector
	 *            ;
	 */
	public static void checkVecotrMaxSize(Vector<?> tmpVector, int maxSize) {
		int size = tmpVector.size();
		while (size > maxSize) {
			size--;
			tmpVector.remove(size);
		}
	}

	/**
	 * 检查所有的匹配是否符合最低匹配率
	 * @param newAltTrans
	 * @param minQuality ;
	 */
	public static Vector<AltTransBean> checkMatchQuality(Vector<AltTransBean> newAltTrans, int minQuality){
		Vector<AltTransBean> result = new Vector<AltTransBean>();
		for(AltTransBean bean : newAltTrans){
			String qualityStr = bean.getMatchProps().get("match-quality").trim();
			if(qualityStr == null){
				continue;
			}
			if (qualityStr.endsWith("%")) {
				qualityStr = qualityStr.substring(0, qualityStr.lastIndexOf("%"));
			}
			Integer q = Integer.parseInt(qualityStr);
			if(q >= minQuality){
				result.add(bean);
			}
		}
		return result;
	}
	
	/**
	 * 将str1的首尾空格 添加到str2上，完成处理后str2的首尾空格和str1一样。
	 * @param str1
	 * @param str2
	 * @return 调整首尾空格后的字符串;
	 */
	public static String adjustSpace(String str1, String str2) {
		if(str2 == null || str2.equals("")){
			return str2;
		}
		if(str1 == null || str1.equals("")){
			return str2;
		}
		int len = str1.length();
		int st = 0;
		int last = 0;
		while (st < len) {
			if (str1.charAt(st) <= ' ') {
				st++;
			} else {
				break;
			}
		}
		while ((st < len)) {
			if ((str1.charAt(len - 1) <= ' ')) {
				len--;
				last++;
			} else {
				break;
			}
		}
		str2 = str2.trim();
		StringBuffer bf = new StringBuffer();
		while(st > 0){
			bf.append(" ");
			st--;
		}
		bf.append(str2);
		while(last > 0){
			bf.append(" ");
			last--;
		}
		return bf.toString();
	}
}
