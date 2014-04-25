/**
 * ExportFilterStoreConfiger.java
 *
 * Version information :
 *
 * Date:Feb 16, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.database.bean.ExportFilterBean;
import net.heartsome.cat.database.bean.ExportFilterComponentBean;
import net.heartsome.cat.database.ui.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ExportFilterStoreConfiger {
	private VTDUtils vu;
	private File filterConfigFile;
	private AutoPilot ap;

	private static final Logger logger = LoggerFactory.getLogger(ExportFilterStoreConfiger.class);
	
	public ExportFilterStoreConfiger() {
		initResource();
		VTDGen vg = new VTDGen();
		try {
			if (vg.parseFile(filterConfigFile.getPath(), true)) {
				vu = new VTDUtils(vg.getNav());
				ap = new AutoPilot(vu.getVTDNav());
			} else {
				throw new ParseException();
			}
		} catch (NavException e) {
			logger.error("", e);
		} catch (ParseException e) {
			logger.error("", e);
		}
	}

	/**
	 * 初始化资源。主要判断存储的文件存不存在，如果不存在则创建
	 */
	private void initResource() {
		String path = checkConfigDirectory(".config");
		File dbListFile = new File(path + System.getProperty("file.separator") + ".exportFilter");
		if (!dbListFile.exists() || dbListFile.isDirectory()) {
			try {
				FileOutputStream fos = new FileOutputStream(dbListFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				StringBuffer bf = new StringBuffer();
				bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				bf.append("<filter>");
				bf.append("<tmx>");
				bf.append("</tmx>");
				bf.append("<tbx>");
				bf.append("</tbx>");
				bf.append("</filter>");
				bos.write(bf.toString().getBytes());
				bos.close();
				fos.close();
			} catch (IOException e) {
				logger.error(Messages.getString("core.ExportFilterStoreConfiger.logger1"), e);
			}
		}
		this.filterConfigFile = dbListFile;
	}

	/**
	 * 检查配置文件存放目录.如果不存在则创建
	 * @param cfgDirName
	 *            需要检查目录的名称
	 * @return ;
	 */
	private String checkConfigDirectory(String cfgDirName) {
		String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		workspacePath = workspacePath + System.getProperty("file.separator") + cfgDirName;

		File dbListFolder = new File(workspacePath);
		if (!dbListFolder.exists() || dbListFolder.isFile()) {
			dbListFolder.mkdirs();
		}
		return workspacePath;
	}

	public void saveFilterRule(ExportFilterBean filter) {
		String content = generateContent(filter);
		if (!content.equals("")) {
			try {
				XMLModifier xm = vu.insert("/filter/" + filter.getFilterType() + "/text()", content.toString());
				vu.bind(xm.outputAndReparse());
				saveToFile(xm, filterConfigFile);
			} catch (Exception e) {
				logger.error("", e);
			}
			ap.resetXPath();
		}
	}

	/**
	 * 检查filterName是否已经存在
	 * @param filterName
	 * @param ruleType
	 *            "TMX" 或者 "TBX"
	 * @return ;
	 */
	public boolean isFilterNameExist(String filterName, String ruleType) {
		Assert.isLegal(ruleType.equals("TMX") || ruleType.equals("TBX"), Messages.getString("core.ExportFilterStoreConfiger.msg1"));
		try {
			AutoPilot tempAp = new AutoPilot(vu.getVTDNav());
			tempAp.selectXPath("/filter/" + ruleType + "/content[@name='" + filterName + "']");
			if (tempAp.evalXPath() != -1) {
				return true;
			}
		} catch (XPathParseException e) {
			logger.error("", e);
		} catch (XPathEvalException e) {
			logger.error("", e);
		} catch (NavException e) {
			logger.error("", e);
		}
		return false;
	}

	/**
	 * 获取过滤规则
	 * @param ruleType
	 *            "TMX" 或者 "TBX"
	 * @return 空的集合或者带有内容的集合;
	 */
	public List<ExportFilterBean> getFilterRule(String ruleType) {
		Assert.isLegal(ruleType.equals("TMX") || ruleType.equals("TBX"), Messages.getString("core.ExportFilterStoreConfiger.msg1"));	
		List<ExportFilterBean> filterList = new ArrayList<ExportFilterBean>();
		try {
			AutoPilot tempAp = new AutoPilot(vu.getVTDNav());
			tempAp.selectXPath("/filter/" + ruleType + "/content");
			while (tempAp.evalXPath() != -1) {
				Hashtable<String, String> attrs = vu.getCurrentElementAttributs();
				ExportFilterBean bean = new ExportFilterBean();
				bean.setFilterType(ruleType);
				bean.setFilterName(attrs.get("name"));
				bean.setFilterConnector(attrs.get("type"));
				AutoPilot ap = new AutoPilot(vu.getVTDNav());
				ap.selectXPath("./option");
				List<ExportFilterComponentBean> cBeanList = new ArrayList<ExportFilterComponentBean>();
				while (ap.evalXPath() != -1) {
					Hashtable<String, String> oAttrs = vu.getCurrentElementAttributs();
					ExportFilterComponentBean cBean = new ExportFilterComponentBean(ruleType);
					cBean.setOptionName(oAttrs.get("name"));
					cBean.setCurrentExpression(oAttrs.get("operator"));
					cBean.setFilterVlaue(oAttrs.get("value"));
					cBeanList.add(cBean);
				}
				if (cBeanList.size() == 0) { // 没有条件的规则无意义
					// deleteFilterRuleByName(bean.getFilterName(),ruleType);
					continue;
				}
				bean.setFilterOption(cBeanList);
				filterList.add(bean);
			}
		} catch (XPathParseException e) {
			logger.error("", e);
		} catch (XPathEvalException e) {
			logger.error("", e);
		} catch (NavException e) {
			logger.error("", e);
		}
		return filterList;
	}

	/**
	 * 删除指定名称的规则
	 * @param filterName
	 *            规则名称
	 * @param ruleType
	 *            "TMX" 或者 "TBX";
	 */
	public void deleteFilterRuleByName(String filterName, String ruleType) {
		Assert.isLegal(!"TMX".equals(ruleType) || !"TBX".equals(ruleType));
		if (filterName != null && !filterName.equals("")) {
			try {
				XMLModifier xm = vu.delete("/filter/" + ruleType + "/content[@name='" + filterName + "']");
				vu.bind(xm.outputAndReparse());
				saveToFile(xm, filterConfigFile);
			} catch (Exception e) {
				logger.error(Messages.getString("core.ExportFilterStoreConfiger.logger2"), e);
			}
		}
	}

	private String generateContent(ExportFilterBean filter) {
		StringBuffer bf = new StringBuffer();
		bf.append("<content name=\"" + filter.getFilterName() + "\" type=\"" + filter.getFilterConnector() + "\">");
		List<ExportFilterComponentBean> options = filter.getFilterOption();
		for (Iterator<ExportFilterComponentBean> iterator = options.iterator(); iterator.hasNext();) {
			ExportFilterComponentBean bean = iterator.next();
			bf.append("<option name=\"" + bean.getOptionName() + "\" operator=\"" + bean.getCurrentExpression()
					+ "\" value=\"" + bean.getFilterVlaue() + "\"/>");
		}

		bf.append("</content>");
		return bf.toString();
	}

	/**
	 * 保存文件
	 * @param xm
	 *            XMLModifier对象
	 * @param fileName
	 *            文件名
	 * @return 是否保存成功;
	 */
	private boolean saveToFile(XMLModifier xm, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fos.close();
			return true;
		} catch (ModifyException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (TranscodeException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		}

		return false;
	}
}
