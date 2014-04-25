package net.heartsome.cat.ts.core.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.xml.vtdimpl.VTDUtils;

/**
 * 翻译单元对象。参见 XLIFF v1.2 标准 trans-unit 节点。
 */
public class TransUnitBean implements IXMLBean {

	/**
	 * 翻译单元 ID。对象 trans-unit 节点 id 属性。
	 */
	private String id;

	/**
	 * 翻译单元节点属性集合。键为属性名，参见 XLIFF 标准。
	 */
	private Hashtable<String, String> tuProps;

	/**
	 * 翻译单元的匹配集合。即所有 alt-trans 节点。
	 */
	private Vector<AltTransBean> matches;

	/**
	 * 翻译单元的属性组集合，即所有的 prop-group 节点。
	 */
	private Vector<PropGroupBean> propGroups;

	/**
	 * 翻译单元的批注集合，即所有的 note 节点。
	 */
	private Vector<NoteBean> notes;

	/**
	 * 翻译单元源文本。即 source 节点文本。
	 */
	private String srcText;

	/**
	 * 翻译单元源节点内容。即 source 节点内容，含内部标记等子节点。
	 */
	private String srcContent;

	/**
	 * 翻译单元目标文本，即 target 节点文本。无此节点则为 null。
	 */
	private String tgtText;

	/**
	 * 翻译单元目标节点内容。即 target 节点内容，含内部标记等子节点。
	 */
	private String tgtContent;

	/**
	 * 当前翻译单元的标记状态
	 */
	private String flag = "flag";

	/**
	 * 当前翻译单元的标记状态
	 * @return ;
	 */
	public String getFlag() {
		return flag;
	}

	/**
	 * 当前翻译单元的标记状态
	 * @param flag
	 *            ;
	 */
	public void setFlag(String flag) {
		this.flag = flag;
	}

	/**
	 * 获取翻译单元源节点内容，含内部标记等子节点。无此节点则为 null。
	 */
	public String getSrcContent() {
		return srcContent;
	}

	/**
	 * 设置翻译单元源节点内容，含内部标记等子节点。
	 */
	public void setSrcContent(String srcContent) {
		this.srcContent = srcContent;
	}

	/**
	 * 获取翻译单元目标节点内容，含内部标记等子节点。无此节点则为 null。
	 */
	public String getTgtContent() {
		return tgtContent;
	}

	/**
	 * 获取翻译单元目标节点内容，含内部标记等子节点。
	 */
	public void setTgtContent(String tgtContent) {
		this.tgtContent = tgtContent;
	}

	/**
	 * 翻译单元中源节点属性。
	 */
	private Hashtable<String, String> srcProps;

	/**
	 * 翻译单元中目标节点属性。
	 */
	private Hashtable<String, String> tgtProps;

	/**
	 * 构建一个指定翻译单元编号和源文本的翻译单元对象。
	 * @param id
	 *            翻译单元编号。
	 * @param srcContent
	 *            源节点内容。
	 */
	public TransUnitBean(String id, String srcContent) {
		this.id = id;
		this.srcContent = srcContent;
	}

	/**
	 * 构建一个指定翻译单元编号和源节点及其纯文本的翻译单元对象。
	 * @param id
	 *            翻译单元编号。
	 * @param srcContent
	 *            源节点内容。含内部标记等子节点的文本。
	 * @param srcText
	 *            源文本。不含内部标记的纯文本。
	 */
	public TransUnitBean(String id, String srcContent, String srcText) {
		this.id = id;
		this.srcContent = srcContent;
		this.srcText = srcText;
	}

	/**
	 * 获取翻译单元编号。
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置翻译单元编号。
	 * @param id
	 *            翻译单元编号。
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取所有的翻译单元属性。未设置则返回 null。
	 */
	public Hashtable<String, String> getTuProps() {
		if (tuProps == null) {
			tuProps = new Hashtable<String, String>();
		}
		return tuProps;
	}

	/**
	 * 设置翻译单元属性集。
	 * @param tuProps
	 *            翻译单元属性集。
	 */
	public void setTuProps(Hashtable<String, String> tuProps) {
		this.tuProps = tuProps;
	}

	/**
	 * 获取所有的匹配。
	 */
	public Vector<AltTransBean> getMatches() {
		return matches;
	}

	/**
	 * 分离各种类型的匹配,并返回指定类型的匹配
	 * @param type
	 *            指定类型的Translation Memory ,Auto Fuzzy,Quick Translation,Machine Translation
	 * @return ;
	 */
	public Vector<AltTransBean> getMatchesByToolId(String type) {
		Vector<AltTransBean> result = new Vector<AltTransBean>();
		if(matches == null){
			return result;
		}
		for(int i = 0 ; i < matches.size() ; i++){
			AltTransBean altTransBean = matches.get(i);
			String toolId = altTransBean.getMatchProps().get("tool-id");
			if (toolId != null && toolId.equals(type)) {
				result.add(altTransBean);				
			}
		}
		
		if (result.size() > 1) {	
			// 排序
			ArrayList<AltTransBean> list = new ArrayList<AltTransBean>(result);
			Collections.sort(list, new Comparator<AltTransBean>() {

				public int compare(AltTransBean o1, AltTransBean o2) {
					int mq1 = Integer.parseInt(o1.getMatchProps().get("match-quality").replace("%", ""));
					int mq2 = Integer.parseInt(o2.getMatchProps().get("match-quality").replace("%", ""));
					return mq2 - mq1;
				}
			});
			
		}
		return result;
	}

	/**
	 * 设置所有的匹配。
	 */
	public void setMatches(Vector<AltTransBean> matches) {
		this.matches = matches;
	}

	/**
	 * 更新指定类型的匹配
	 * @param type
	 * @param newMatches ;
	 */
	public void updateMatches(String type,Vector<AltTransBean> newMatches){
		if(matches == null){
			matches = new Vector<AltTransBean>();
		}
		for(int i = 0 ; i < matches.size() ; i++){
			AltTransBean altTransBean = matches.get(i);
			String toolId = altTransBean.getMatchProps().get("tool-id");
			if (toolId != null && toolId.equals(type)) {
				matches.remove(altTransBean);
				i--;
			}
		}
		matches.addAll(newMatches);
	}
	
	/**
	 * 获取所有的属性组。
	 */
	public Vector<PropGroupBean> getPropgroups() {
		return propGroups;
	}

	/**
	 * 设置所有的属性组。
	 * @param propgroups
	 *            属性组集。
	 */
	public void setPropgroups(Vector<PropGroupBean> propgroups) {
		this.propGroups = propgroups;
	}

	/**
	 * 获取所有的批注。
	 */
	public Vector<NoteBean> getNotes() {
		return notes;
	}

	/**
	 * 设置所有的批注。
	 * @param notes
	 *            批注集。
	 */
	public void setNotes(Vector<NoteBean> notes) {
		this.notes = notes;
	}

	/**
	 * 获取源文本。
	 */
	public String getSrcText() {
		return srcText;
	}

	/**
	 * 设置源文本。含内部标记。
	 * @param srcText
	 *            源文本。
	 */
	public void setSrcText(String srcText) {
		this.srcText = srcText;
	}

	/**
	 * 获取目标文本。含内部标记。
	 */
	public String getTgtText() {
		return tgtText;
	}

	/**
	 * 设置目标文本。
	 * @param tgtText
	 *            目标文本。
	 */
	public void setTgtText(String tgtText) {
		this.tgtText = tgtText;
	}

	/**
	 * 获取所有的源节点属性。未设置返回null。
	 */
	public Hashtable<String, String> getSrcProps() {
		if (srcProps == null) {
			srcProps = new Hashtable<String, String>();
		}
		return srcProps;
	}

	/**
	 * 获取源语言。未设置返回 null。
	 */
	public String getSrcLang() {
		if (srcProps != null) {
			return srcProps.get("xml:lang");
		} else {
			return null;
		}
	}

	/**
	 * 获取目标语言。未设置返回 null。
	 */
	public String getTgtLang() {
		if (tgtProps != null) {
			return tgtProps.get("xml:lang");
		} else {
			return null;
		}
	}

	/**
	 * 获取源节点指定属性。未设置返回 null。
	 * @param propname
	 *            属性名。
	 */
	public String getSrcPropValue(String propname) {
		if (propname == null) {
			return null;
		}

		if (srcProps != null) {
			return srcProps.get(propname);
		} else {
			return null;
		}
	}

	/**
	 * 获取目标节点指定属性。未设置返回 null。
	 * @param propname
	 *            属性名。
	 */
	public String getTgtLang(String propname) {
		if (propname == null) {
			return null;
		}

		if (tgtProps != null) {
			return tgtProps.get(propname);
		} else {
			return null;
		}
	}

	/**
	 * 设置所有的源节点属性。
	 * @param srcProps
	 *            源节点属性集。
	 */
	public void setSrcProps(Hashtable<String, String> srcProps) {
		this.srcProps = srcProps;
	}

	/**
	 * 获取所有的目标节点属性。
	 */
	public Hashtable<String, String> getTgtProps() {
		if (tgtProps == null) {
			tgtProps = new Hashtable<String, String>();
		}
		return tgtProps;
	}

	/**
	 * 设置所有目标节点属性。
	 * @param tgtProps
	 *            目标节点属性集。
	 */
	public void setTgtProps(Hashtable<String, String> tgtProps) {
		this.tgtProps = tgtProps;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TransUnitBean other = (TransUnitBean) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	private static Comparator<AltTransBean> comparator = new Comparator<AltTransBean>() {

		public int compare(AltTransBean o1, AltTransBean o2) {
			int mq1 = Integer.parseInt(o1.getMatchProps().get("match-quality").replace("%", ""));
			int mq2 = Integer.parseInt(o2.getMatchProps().get("match-quality").replace("%", ""));
			return mq2 - mq1;
		}
	};

	private static final int NUMBER_OF_MATCHES = 10;

	public String toXMLString() {
		String srcXML = VTDUtils.getNodeXML("source", srcContent, srcProps);
		String tgtXML = VTDUtils.getNodeXML("target", tgtContent, tgtProps);

		String propGroupsXML = "";
		if (propGroups != null && !propGroups.isEmpty()) {
			for (PropGroupBean propGroupBean : propGroups) {
				propGroupsXML += propGroupBean.toXMLString();
			}
		}
		String noteXML = "";
		if (notes != null && !notes.isEmpty()) {
			for (NoteBean noteBean : notes) {
				noteXML += noteBean.toXMLString();
			}
		}
		String matchesXML = "";
		if (matches != null && !matches.isEmpty()) {
			int matchCount = 0;
			int fuzzyCount = 0;
			Collections.sort(matches, comparator);
			for (AltTransBean altTransBean : matches) {
				String orgin = altTransBean.getMatchOrigin();
				if (fuzzyCount >= NUMBER_OF_MATCHES || matchCount >= NUMBER_OF_MATCHES) {
					break;
				}
				if (orgin.indexOf("autoFuzzy_") > -1) { // 繁殖翻译
					if (fuzzyCount < NUMBER_OF_MATCHES) { // 限制在10个以内
						matchesXML += altTransBean.toXMLString();
						fuzzyCount++;
					}
				} else { // 匹配
					if (matchCount < NUMBER_OF_MATCHES) { // 限制在10个以内
						matchesXML += altTransBean.toXMLString();
						matchCount++;
					}
				}
			}
		}
		String content = srcXML + tgtXML + propGroupsXML + noteXML + matchesXML;
		return VTDUtils.getNodeXML("trans-unit", content, tuProps);// + Utils.getLineSeparator();
	}

}
