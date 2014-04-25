package net.heartsome.cat.ts.ui.advanced.handlers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.advanced.model.ElementBean;
import net.heartsome.cat.ts.ui.advanced.model.LanguageRuleBean;
import net.heartsome.cat.ts.ui.advanced.model.MapRuleBean;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * 品质检查的xml文件解析类
 * @author robert 2012-02-20
 * @version
 * @since JDK1.6
 */
public class ADXmlHandler extends QAXmlHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ADXmlHandler.class);
	/**
	 * 获取catalogue.xml文件中的数据
	 * @param xmlLocation
	 * @return ;
	 */
	public List<String[]> getCatalogValueList(String xmlLocation){
		List<String[]> catalogList = new LinkedList<String[]>();
		int index = 1;
		VTDNav vn = super.getVTDNav(xmlLocation);
		AutoPilot ap = new AutoPilot(vn);
		
		try {
			ap.selectXPath("/catalog/*");
			while (ap.evalXPath() != -1) {
				String cataName = vn.toString(vn.getCurrentIndex());
				String name = null;
				int idIdx = -1;
				int urlIdx = -1;
				String idStr;
				String urlStr ;
				if ("public".equals(cataName)) {
					name = "PUBLIC";
					idIdx = vn.getAttrVal("publicId");
					urlIdx = vn.getAttrVal("uri");
				}else if ("system".equals(cataName)) {
					name = "SYSTEM";
					idIdx = vn.getAttrVal("systemId");
					urlIdx = vn.getAttrVal("uri");
				}else if ("uri".equals(cataName)) {
					name = "URI";
					idIdx = vn.getAttrVal("name");
					urlIdx = vn.getAttrVal("uri");
				}else if ("nextCatalog".equals(cataName)) {
					name = "nextCatalog";
					urlIdx = vn.getAttrVal("catalog");
				}
				
				if (name == null) {
					continue;
				}
				idStr = (idIdx != -1 ? vn.toString(idIdx) : "" );
				urlStr = (urlIdx != -1 ? vn.toString(urlIdx) : "" );
				
				catalogList.add(new String[]{"" + index, name, idStr, urlStr});
				index ++;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return catalogList;
	} 
	
	/**
	 * 向catalogue.xml中添加数据
	 * @param xmlLocation
	 * @param addData ;
	 */
	public void deleteCatalog(String xmlLocation, List<String> xpathList, List<String[]> dataList){
		VTDNav vn = super.getVTDNav(xmlLocation);
		AutoPilot ap = new AutoPilot(vn);
		try {
			VTDUtils vu = new VTDUtils();
			for (int i = 0; i < xpathList.size(); i++) {
				vn = super.getVTDNav(xmlLocation);
				vu.bind(vn);
				String xpath = xpathList.get(i);
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					XMLModifier xm = vu.delete(xpath);
					saveAndReparse(xm, xmlLocation);
					ap.resetXPath();
				}else {
					//如果找不到，就是xpath配置问题，比如没有任何一个属性
					/*String name = dataList.get(i)[0];
					String id = dataList.get(i)[1];
					String uri = dataList.get(i)[2];
					if ("".equals(id) && "".equals(uri)) {
						
					}*/
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}
	
	/**
	 * 修改数据(修改整个节点)
	 * @param xmlLocation
	 * @param xpath
	 * @param newData ;
	 */
	public boolean updataDataToXml(String xmlLocation, String xpath, String newData){
		VTDNav vn = super.getVTDNav(xmlLocation);
		AutoPilot ap = new AutoPilot(vn);
		try {
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				return saveAndReparse(vu.update(xpath, newData), xmlLocation);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return false;
	}
	
	/**
	 * 获取转换配置文件的所有内容
	 * @return ;
	 */
	public List<ElementBean> getconvertXmlElements(String xmlLocation) {
		List<ElementBean> elementsList = new LinkedList<ElementBean>();
		VTDNav vn = super.getVTDNav(xmlLocation);
		AutoPilot ap = new AutoPilot(vn);
		ElementBean bean;
		try {
			ap.selectXPath("/ini-file/tag");
			while (ap.evalXPath() != -1) {
				// 如果元素名与元素类型为空，那么当前节点不被添加
				if (vn.getText() == -1 || vn.getAttrVal("hard-break") == -1 || "".equals(vn.toString(vn.getText()))
						|| "".equals(vn.toString(vn.getAttrVal("hard-break")))) {
					continue;
				}
				//开始添加元素名称
				bean = new ElementBean();
				bean.setName(vn.toString(vn.getText()));
				bean.setType(vn.toString(vn.getAttrVal("hard-break")));
				bean.setInlineType(vn.getAttrVal("ctype") != -1 ? vn.toString(vn.getAttrVal("ctype")) : "");
				bean.setTransAttribute(vn.getAttrVal("attributes") != -1 ? vn.toString(vn.getAttrVal("attributes")) : "");
				bean.setRemainSpace(vn.getAttrVal("keep-format") != -1 ? vn.toString(vn.getAttrVal("keep-format")) : "");
				elementsList.add(bean);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return elementsList;
	}
	
	/**
	 * 获取分析XML文件后的数据(针对于分析XML文件)
	 * @param xmlLocation
	 * @return ;
	 */
	public List<ElementBean> getAnalysisXmlData(String xmlLocation) {
		List<ElementBean> beanList = new LinkedList<ElementBean>();
		List<String> elementNameList = new ArrayList<String>();
		VTDNav vn = getVTDNav(xmlLocation);
		AutoPilot ap = new AutoPilot(vn);
		ElementBean bean;
		try {
			ap.selectXPath("//*[text()!='' or not(./*)]");
			while (ap.evalXPath() != -1) {
				String elementName = vn.toString(vn.getCurrentIndex());
				//如果元素名不重复，那么就添加到结果集中
				if (elementNameList.indexOf(elementName) == -1) {
					bean = new ElementBean();
					bean.setName(elementName);
					bean.setType("segment");
					bean.setInlineType("");
					bean.setTransAttribute("");
					bean.setRemainSpace("");
					beanList.add(bean);
					elementNameList.add(elementName);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return beanList;
	}
	
	/**
	 * 验证指定的srx文件是否符合srx标准，通过验证其根元素是否为srx
	 * @param srxLoaction
	 * @return ;
	 */
	public boolean validSrx(String srxLoaction){
		VTDNav vn = super.getVTDNav(srxLoaction);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/srx");
			if (ap.evalXPath() != -1) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			return false;
		}
		return false;
	}
	
	/**
	 * 验证给出的xpath所表示的节点是否存在，如果存在，则返回true，如果不存在，则返回false
	 * @param xmlLoaction
	 * @param validXpath
	 * @return ;
	 */
	public boolean validNodeExist(String xmlLocation, String validXpath) {
		VTDNav vn = super.getVTDNav(xmlLocation);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath(validXpath);
			if (ap.evalXPath() != -1) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return false;
	}
	
	/**
	 * 将言语规则节点添加到指定的文件中
	 * @param srxLoaction
	 * @param langRulsList
	 * @return true:添加成功,false:添加失败;
	 */
	public boolean addLanguageRules(String srxLoaction, String languageRulesData) {
		VTDNav vn = super.getVTDNav(srxLoaction);
		AutoPilot ap = new AutoPilot(vn);
		//在添加之前，先判断body与languagerules节点是否存在，如果存在，则创建
		try {
			//在添加之前，先判断body节点是否存在，如果不存在，则创建
			ap.selectXPath("/srx/body");
			if (ap.evalXPath() == -1) {
				String bodyStr = "\t<body>\n\t</body>";
				if (super.addDataToXml(srxLoaction, "/srx", bodyStr)) {
					vn = super.getVTDNav(srxLoaction);
				}
			}
			ap.resetXPath();
			//若无languagerule节点存在，创建此节点
			ap.selectXPath("/srx/body/languagerules");
			if (ap.evalXPath() == -1) {
				String langRuleStr = "\t<languagerules>\n" + "\t\t</languagerules>";
				if (super.addDataToXml(srxLoaction, "/srx/body", langRuleStr)) {
					vn = super.getVTDNav(srxLoaction);
				}
			}
			ap.resetXPath();
			
			return super.addDataToXml(srxLoaction, "/srx/body/languagerules", languageRulesData);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return false;
	}
	
	/**
	 * 分段规则中，添加映射规则
	 * @param srxLoaction
	 * @param mapRulesData
	 * @param mapRuleName
	 * @return true:添加成功,false:添加失败;
	 */
	public boolean addMapRules(String srxLoaction, String mapRulesData) {
		VTDNav vn = super.getVTDNav(srxLoaction);
		AutoPilot ap = new AutoPilot(vn);
		//在添加之前，先判断body与maprules节点是否存在，如果存在，则创建
		try {
			//在添加之前，先判断body节点是否存在，如果不存在，则创建
			ap.selectXPath("/srx/body");
			if (ap.evalXPath() == -1) {
				String bodyStr = "\t<body>\n\t</body>";
				if (super.addDataToXml(srxLoaction, "/srx", bodyStr)) {
					vn = super.getVTDNav(srxLoaction);
				}
			}
			ap.resetXPath();
			//若无languagerule节点存在，创建此节点
			ap.selectXPath("/srx/body/maprules");
			if (ap.evalXPath() == -1) {
				String langRuleStr = "\t<maprules>\n" + "\t\t</maprules>";
				if (super.addDataToXml(srxLoaction, "/srx/body", langRuleStr)) {
					vn = super.getVTDNav(srxLoaction);
				}
			}
			ap.resetXPath();
			
			return super.addDataToXml(srxLoaction, "/srx/body/maprules", mapRulesData);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return false;
	}
	
	/**
	 * 获取语言规则的名称，返回结果为list<String[]>
	 * @param srxLoaction
	 * @return ;
	 */
	public List<String[]> getLanguageRuleNamesOfSrx_1(String srxLocation){
		List<String[]> langRuleNames = new LinkedList<String[]>();
		VTDNav vn = super.getVTDNav(srxLocation);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/srx/body/languagerules/languagerule");
			while(ap.evalXPath() != -1){
				if (vn.getAttrVal("languagerulename") != -1) {
					langRuleNames.add(new String[] { "" + (langRuleNames.size() + 1),
							vn.toString(vn.getAttrVal("languagerulename")) });
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return langRuleNames;
	}
	
	/**
	 * 获取语言规则的名称，返回结果为list<String>类型
	 * @param srxLocation
	 * @return ;
	 */
	public List<String> getLanguageRuleNamesOfSrx_2(String srxLocation) {
		List<String> langRuleNames = new LinkedList<String>();
		VTDNav vn = super.getVTDNav(srxLocation);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/srx/body/languagerules/languagerule");
			while (ap.evalXPath() != -1) {
				if (vn.getAttrVal("languagerulename") != -1) {
					langRuleNames.add(vn.toString(vn.getAttrVal("languagerulename")));
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return langRuleNames;
	}
	
	/**
	 * 获取srx文件所有的映射节点名称
	 * @param srxLocation
	 * @return ;
	 */
	public List<String[]> getMapRuleNames(String srxLocation){
		List<String[]> mapRuleNames = new LinkedList<String[]>();
		VTDNav vn = super.getVTDNav(srxLocation);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/srx/body/maprules/maprule");
			while (ap.evalXPath() != -1) {
				if (vn.getAttrVal("maprulename") != -1) {
					mapRuleNames.add(new String[] { "" + (mapRuleNames.size() + 1),
							vn.toString(vn.getAttrVal("maprulename")) });
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return mapRuleNames;
	}
	
	
	public List<LanguageRuleBean> getLangRulesByName(String srxLocation, String langRuleName){
		List<LanguageRuleBean> languageRulesList = new LinkedList<LanguageRuleBean>();
		VTDNav vn = super.getVTDNav(srxLocation);
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot ruleAP = new AutoPilot(vn);
		AutoPilot childAP = new AutoPilot(vn);
		try {
			ap.selectXPath("/srx/body/languagerules/languagerule[@languagerulename='"+ langRuleName +"']");
			if (ap.evalXPath() != -1) {
				ruleAP.selectXPath("./rule");
				LanguageRuleBean bean;
				while (ruleAP.evalXPath() != -1) {
					String isBreak = "";
					String preBreak = "";
					String afterBreak = "";
					if (vn.getAttrVal("break") != -1) {
						isBreak = vn.toString(vn.getAttrVal("break"));
					}
					vn.push();
					childAP.selectXPath("./beforebreak");
					if (childAP.evalXPath() != -1) {
						if (vn.getText() != -1) {
							preBreak = vn.toString(vn.getText());
						}
					}
					vn.pop();
					childAP.resetXPath();
					
					vn.push();
					childAP.selectXPath("./afterbreak");
					if (childAP.evalXPath() != -1) {
						if (vn.getText() != -1) {
							afterBreak = vn.toString(vn.getText());
						}
					}
					vn.pop();
					
					bean = new LanguageRuleBean(isBreak, preBreak, afterBreak);
					languageRulesList.add(bean);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return languageRulesList;
		
	}
	
	public List<MapRuleBean> getMapRulesByName(String srxLocation, String mapRuleName){
		List<MapRuleBean> mapRulesList = new LinkedList<MapRuleBean>();
		VTDNav vn = super.getVTDNav(srxLocation);
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot ruleAP = new AutoPilot(vn);
		try {
			MapRuleBean bean;
			ap.selectXPath("/srx/body/maprules/maprule[@maprulename='"+ mapRuleName +"']");
			if (ap.evalXPath() != -1) {
				ruleAP.selectXPath("./languagemap");
				while (ruleAP.evalXPath() != -1) {
					String languageModel = "";
					String langRuleName = "";
					
					int index = 0;
					if ((index = vn.getAttrVal("languagepattern")) != -1) {
						languageModel = vn.toString(index);
					}
					
					if ((index = vn.getAttrVal("languagerulename")) != -1) {
						langRuleName = vn.toString(index);
					}
					
					bean = new MapRuleBean(languageModel, langRuleName);
					mapRulesList.add(bean);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		
		return mapRulesList;
	}
	
	/**
	 * 验证所指定的语言规则是否添加映射，如果已经添加，就返回true,若未被添加映射，则返回false
	 * @param srxLoaction
	 * @param languageRuleName
	 * @return ;
	 */
	public boolean checkLangRuleNameMaped(String srxLoaction, String languageRuleName){
		VTDNav vn = super.getVTDNav(srxLoaction);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/srx/body/maprules/maprule/languagemap[@languagerulename='"+ languageRuleName +"']");
			if (ap.evalXPath() != -1) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return false;
	}
	
	/**
	 * 当语言规则名称改变以后，同步更新映射规则中的语言规则名称
	 * @param xmlLocation
	 * @param updateXpath
	 * @param newData
	 * @return ;
	 */
	public boolean updateMapRuleLangName(String xmlLocation, String updateXpath, String newValue) {
		VTDNav vn = super.getVTDNav(xmlLocation);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath(updateXpath);
			XMLModifier xm = new XMLModifier(vn);
			boolean needReparse = false;
			while (ap.evalXPath() != -1) {
				xm.updateToken(vn.getAttrVal("languagerulename"), newValue);
				needReparse = true;
			}
			if (needReparse) {
				return saveAndReparse(xm, xmlLocation);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return true;
	}
	
}
