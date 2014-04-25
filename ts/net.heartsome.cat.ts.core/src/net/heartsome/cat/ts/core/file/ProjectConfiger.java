/**
 * ProjectConfigerFile.java
 *
 * Version information :
 *
 * Date:Nov 25, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.core.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.IProjectConfigChangedListener;
import net.heartsome.cat.ts.core.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.org.tools.utils.encrypt.DESImpl;

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
public class ProjectConfiger {

	private VTDUtils vu;
	private File projCfgFile;
	private List<IProjectConfigChangedListener> listeners = new ArrayList<IProjectConfigChangedListener>();
	private static final Logger logger = LoggerFactory.getLogger(ProjectConfiger.class);

	public void addChangeListener(IProjectConfigChangedListener listener) {
		this.listeners.add(listener);
	}

	public void removeChangeListener(IProjectConfigChangedListener listener) {
		this.listeners.remove(listener);
	}

	private void fireChangedEvent() {
		for (IProjectConfigChangedListener listener : listeners) {
			listener.handProjectConfigChangedEvent();
		}
	}

	/**
	 * Constructor
	 * @param projCfgFile
	 *            配置文件路径
	 * @throws IOException
	 * @throws NavException
	 * @throws ParseException
	 */
	protected ProjectConfiger(String projCfgFile) throws IOException {
		initResource(projCfgFile);
		VTDGen vg = new VTDGen();
		try {
			if (vg.parseFile(this.projCfgFile.getPath(), true)) {
				vu = new VTDUtils(vg.getNav());
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
	 * @throws IOException
	 */
	private void initResource(String projectFile) throws IOException {
		File projCfgFile = new File(projectFile);
		if (!projCfgFile.exists() || projCfgFile.isDirectory()) {
			FileOutputStream fos = new FileOutputStream(projCfgFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			StringBuffer bf = new StringBuffer();
			bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			bf.append("\n<projectDescription>\n");
			bf.append("</projectDescription>");
			bos.write(bf.toString().getBytes());
			bos.close();
			fos.close();
		}
		this.projCfgFile = projCfgFile;
	}

	/**
	 * 获取项目配置信息
	 * @return 指定项目配置文件中的所有信息;
	 */
	public ProjectInfoBean getCurrentProjectConfig() {
		ProjectInfoBean bean = new ProjectInfoBean();
		vu.getVTDNav().push();
		AutoPilot hsAp = new AutoPilot(vu.getVTDNav());
		try {
			hsAp.selectXPath("/projectDescription/hs");
			if (hsAp.evalXPath() != -1) {
				bean.setProjectName(getProjectName());
				bean.setMapField(getFieldMap());
				bean.setMapAttr(getAttrMap());

				bean.setSourceLang(getSourceLanguage());
				bean.setTargetLang(getTargetlanguages());

				bean.setTmDb(getAllTmDbs());
				bean.setTbDb(getTermBaseDbs(false));
			}
		} catch (XPathParseException e) {
			logger.error("", e);
		} catch (XPathEvalException e) {
			logger.error("", e);
		} catch (NavException e) {
			logger.error("", e);
		}
		vu.getVTDNav().pop();
		return bean;
	}
	
	/**
	 * 获取配置文件中的项目文本字段信息
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException ;
	 */
	private LinkedHashMap<String, String> getFieldMap() throws XPathParseException, XPathEvalException, NavException {
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		Map<String, String> mapField = new HashMap<String, String>();
		ap.selectXPath("/projectDescription/hs//fieldList/field");
		while (ap.evalXPath() != -1) {
			String fieldName = TextUtil.stringToXML(vu.getCurrentElementAttribut("name", ""));
			String fieldValue = vu.getElementContent();
			mapField.put(fieldName, fieldValue);
		}
		List<Entry<String, String>> lstAttr = new ArrayList<Entry<String, String>>(mapField.entrySet());
		final Collator collatorChinese = Collator.getInstance(java.util.Locale.CHINA);
		Collections.sort(lstAttr, new Comparator<Entry<String, String>>() {

			public int compare(Entry<String, String> arg0, Entry<String, String> arg1) {
				return collatorChinese.compare(arg0.getKey(), arg1.getKey());
			}
		});
		LinkedHashMap<String, String> linkedMapAttr = new LinkedHashMap<String, String>();
		for (Entry<String, String> entry : lstAttr) {
			linkedMapAttr.put(entry.getKey(), entry.getValue());
		}
		return linkedMapAttr;
	}
	
	/**
	 * 获取配置文件中的项目属性字段信息
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException ;
	 */
	private LinkedHashMap<String, Object[]> getAttrMap() throws XPathParseException, XPathEvalException, NavException {
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		Map<String, Object[]> mapAttr = new HashMap<String, Object[]>();
		ap.selectXPath("/projectDescription/hs//attributeList/attribute");
		AutoPilot ap2 = new AutoPilot(vu.getVTDNav());
		final Collator collatorChinese = Collator.getInstance(java.util.Locale.CHINA);
		while (ap.evalXPath() != -1) {
			String attrName = TextUtil.stringToXML(vu.getCurrentElementAttribut("name", ""));
			String attrSelVal = TextUtil.stringToXML(vu.getCurrentElementAttribut("selection", ""));
			vu.getVTDNav().push();
			ap2.selectXPath("./item");
			ArrayList<String> lstAttrVal = new ArrayList<String>();
			while (ap2.evalXPath() != -1) {
				lstAttrVal.add(vu.getElementContent());
			}
			vu.getVTDNav().pop();
			Collections.sort(lstAttrVal, collatorChinese);
			mapAttr.put(attrName, new Object[]{attrSelVal, lstAttrVal});
		}
		List<Entry<String, Object[]>> lstAttr = new ArrayList<Entry<String, Object[]>>(mapAttr.entrySet());
		Collections.sort(lstAttr, new Comparator<Entry<String, Object[]>>() {

			public int compare(Entry<String, Object[]> arg0, Entry<String, Object[]> arg1) {
				return collatorChinese.compare(arg0.getKey(), arg1.getKey());
			}
		});
		LinkedHashMap<String, Object[]> linkedMapAttr = new LinkedHashMap<String, Object[]>();
		for (Entry<String, Object[]> entry : lstAttr) {
			linkedMapAttr.put(entry.getKey(), entry.getValue());
		}
		return linkedMapAttr;
	}

	/**
	 * 保存项目的配置信息
	 * @param bean
	 *            {@link ProjectInfoBean} 项目配置信息bean
	 * @throws NavException
	 * @throws ParseException
	 * @throws TranscodeException
	 * @throws ModifyException
	 * @throws IOException
	 *             ;
	 */
	public boolean saveProjectConfigInfo(ProjectInfoBean bean) {
		try {
			StringBuffer content = new StringBuffer();
			content.append("\n\t<hs>");
			content.append(createProjectNameNode(bean.getProjectName()));
			content.append(createFieldListNode(bean.getMapField()));
			content.append(createAttrListNode(bean.getMapAttr()));
			content.append(createLanguageNode(bean.getSourceLang(), bean.getTargetLang()));
			content.append(createTmNode(bean.getTmDb()));
			content.append(createTbNode(bean.getTbDb()));
			content.append("\n\t</hs>");

			XMLModifier xm = vu.update("/projectDescription/text()", content.toString());
			save(xm, this.projCfgFile);
			return true;
		} catch (Exception e) {
			logger.error(Messages.getString("file.ProjectConfiger.logger1"), e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 更新
	 * @param bean
	 *            ;
	 */
	public void updateProjectConfig(ProjectInfoBean bean) {
		saveProjectConfigInfo(bean);
		fireChangedEvent();
	}

	/**
	 * 获取项目名称,如果在设置项目信息时,未填写该值返回将为空串
	 * @return 如果返回null表示为获取到项目名称;
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 */
	public String getProjectName() throws XPathParseException, XPathEvalException, NavException {
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.selectXPath("/projectDescription/hs");
		if (ap.evalXPath() != -1) {
			return TextUtil.resetSpecialString(vu.getChildContent("projectname"));
		}
		return null;
	}

	/**
	 * 获取项目的所有目标语言
	 * @return 如果返回空的list表示没有获取到项目的目标语言;
	 * @throws XPathParseException
	 * @throws NavException
	 * @throws XPathEvalException
	 */
	public List<Language> getTargetlanguages() throws XPathParseException, NavException, XPathEvalException {
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.selectXPath("/projectDescription/hs/language/target");
		List<Language> targetLangs = new ArrayList<Language>();
		while (ap.evalXPath() != -1) {
			String code = vu.getCurrentElementAttribut("code", "");
			String name = vu.getElementContent();
			String image = vu.getCurrentElementAttribut("image", "");
			String isBidi = vu.getCurrentElementAttribut("isbidi", "false");
			targetLangs.add(new Language(code, name, image, isBidi.equals("false") ? false : true));
		}
		return targetLangs;
	}

	/**
	 * 获取项目源语言
	 * @return 返加null或Language,如果是null表示没有获取到项目的源语言;
	 * @throws XPathParseException
	 * @throws NavException
	 * @throws XPathEvalException
	 */
	public Language getSourceLanguage() throws XPathParseException, XPathEvalException, NavException {
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.selectXPath("/projectDescription/hs/language/source");
		if (ap.evalXPath() != -1) {
			String code = vu.getCurrentElementAttribut("code", "");
			String name = vu.getElementContent();
			String image = vu.getCurrentElementAttribut("image", "");
			String isBidi = vu.getCurrentElementAttribut("isbidi", "No");
			return new Language(code, name, image, isBidi.equals("NO") ? false : true);
		}
		return null;
	}

	/**
	 * 获取项目的所有术语库
	 * @param isDefault
	 *            是否获取默认术语库
	 * @return 数据库参数列表List<DatabaseModelBean> {@link DatabaseModelBean};;
	 */
	public List<DatabaseModelBean> getTermBaseDbs(boolean isDefault) {
		List<DatabaseModelBean> dbList = new ArrayList<DatabaseModelBean>();
		try {
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			if (isDefault) {
				ap.selectXPath("/projectDescription/hs/tb/db[@default='Y']");
			} else {
				ap.selectXPath("/projectDescription/hs/tb/db");
			}
			while (ap.evalXPath() != -1) {
				DatabaseModelBean dbm = new DatabaseModelBean();
				String defaultAttr = vu.getCurrentElementAttribut("default", "N");
				if (defaultAttr.equals("Y")) {
					dbm.setDefault(true);
				} else {
					dbm.setDefault(false);
				}
				dbm.setDbType(vu.getChildContent("type"));
				dbm.setDbName(vu.getChildContent("name"));
				dbm.setItlDBLocation(vu.getChildContent("location"));
				dbm.setInstance(vu.getChildContent("instance"));
				dbm.setHost(vu.getChildContent("server"));
				dbm.setPort(vu.getChildContent("port"));
				dbm.setUserName(vu.getChildContent("user"));
				dbm.setPassword(DESImpl.decrypt(vu.getChildContent("password")));
				String hasMatch = vu.getChildContent("hasmatch");
				if (hasMatch.equals("true")) {
					dbm.setHasMatch(true);
				} else {
					dbm.setHasMatch(false);
				}
				dbList.add(dbm);
			}
		} catch (Exception e) {
			logger.error(Messages.getString("file.ProjectConfiger.logger2"), e);
			return dbList;
		}
		return dbList;
	}

	/**
	 * 获取当前项目的所有翻译记忆库
	 * @return 数据库参数列表List<DatabaseModelBean> {@link DatabaseModelBean};
	 */
	public List<DatabaseModelBean> getAllTmDbs() {
		List<DatabaseModelBean> dbList = new ArrayList<DatabaseModelBean>();
		try {
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("/projectDescription/hs/tm/db");
			while (ap.evalXPath() != -1) {
				DatabaseModelBean dbm = new DatabaseModelBean();
				String defaultAttr = vu.getCurrentElementAttribut("default", "N");
				if (defaultAttr.equals("Y")) {
					dbm.setDefault(true);
				} else {
					dbm.setDefault(false);
				}
				dbm.setDbType(vu.getChildContent("type"));
				dbm.setDbName(vu.getChildContent("name"));
				dbm.setItlDBLocation(vu.getChildContent("location"));
				dbm.setInstance(vu.getChildContent("instance"));
				dbm.setHost(vu.getChildContent("server"));
				dbm.setPort(vu.getChildContent("port"));
				dbm.setUserName(vu.getChildContent("user"));
				dbm.setPassword(DESImpl.decrypt(vu.getChildContent("password")));
				String hasMatch = vu.getChildContent("hasmatch");
				if (hasMatch.equals("true")) {
					dbm.setHasMatch(true);
				} else {
					dbm.setHasMatch(false);
				}
				if (dbm.isDefault()) {
					dbList.add(0, dbm);
				} else {
					dbList.add(dbm);
				}
			}
		} catch (Exception e) {
			logger.error(Messages.getString("file.ProjectConfiger.logger3"), e);
			return dbList;
		}
		return dbList;
	}

	/**
	 * 获取当前项目的默认的记忆库,default='Y'
	 * @return 返回{@link DatabaseModelBean};
	 */
	public DatabaseModelBean getDefaultTMDb() {
		try {
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("/projectDescription/hs/tm/db[@default='Y']");
			if (ap.evalXPath() != -1) {
				DatabaseModelBean dbm = new DatabaseModelBean();
				dbm.setDefault(true);
				dbm.setDbType(vu.getChildContent("type"));
				dbm.setDbName(vu.getChildContent("name"));
				dbm.setItlDBLocation(vu.getChildContent("location"));
				dbm.setInstance(vu.getChildContent("instance"));
				dbm.setHost(vu.getChildContent("server"));
				dbm.setPort(vu.getChildContent("port"));
				dbm.setUserName(vu.getChildContent("user"));
				dbm.setPassword(DESImpl.decrypt(vu.getChildContent("password")));
				String hasMatch = vu.getChildContent("hasmatch");
				if (hasMatch.equals("true")) {
					dbm.setHasMatch(true);
				} else {
					dbm.setHasMatch(false);
				}
				return dbm;
			}
		} catch (Exception e) {
			logger.error(Messages.getString("file.ProjectConfiger.logger4"), e);
			return null;
		}
		return null;
	}

	/**
	 * 获取当前项目的所有参考翻译记忆库，default='N'
	 * @return 数据库参数列表List<DatabaseModelBean> {@link DatabaseModelBean};
	 */
	public List<DatabaseModelBean> getReferrenceTMDbs() {
		List<DatabaseModelBean> dbList = new ArrayList<DatabaseModelBean>();
		try {
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("/projectDescription/hs/tm/db[@default='N']");
			while (ap.evalXPath() != -1) {
				DatabaseModelBean dbm = new DatabaseModelBean();
				dbm.setDefault(false);
				dbm.setDbType(vu.getChildContent("type"));
				dbm.setDbName(vu.getChildContent("name"));
				dbm.setItlDBLocation(vu.getChildContent("location"));
				dbm.setInstance(vu.getChildContent("instance"));
				dbm.setHost(vu.getChildContent("server"));
				dbm.setPort(vu.getChildContent("port"));
				dbm.setUserName(vu.getChildContent("user"));
				dbm.setPassword(DESImpl.decrypt(vu.getChildContent("password")));
				String hasMatch = vu.getChildContent("hasmatch");
				if (hasMatch.equals("true")) {
					dbm.setHasMatch(true);
				} else {
					dbm.setHasMatch(false);
				}
				dbList.add(dbm);
			}
		} catch (Exception e) {
			logger.error(Messages.getString("file.ProjectConfiger.logger5"), e);
			return dbList;
		}
		return dbList;
	}

	/**
	 * 保存文件
	 * @param xm
	 *            XMLModifier对象
	 * @param fileName
	 *            文件名
	 * @return 是否保存成功;
	 */
	private boolean save(XMLModifier xm, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fos.close();
			VTDGen vg = new VTDGen();
			try {
				if (vg.parseFile(projCfgFile.getPath(), true)) {
					vu.bind(vg.getNav());
				} else {
					throw new ParseException();
				}
			} catch (NavException e) {
				logger.error("", e);
			} catch (ParseException e) {
				logger.error("", e);
			}
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

	private String createTmNode(List<DatabaseModelBean> dbList) {
		StringBuffer content = new StringBuffer();
		content.append("\n\t\t<tm>");
		for (int i = 0; i < dbList.size(); i++) {
			DatabaseModelBean dbModel = dbList.get(i);
			content.append(createDbNode(dbModel));
		}
		content.append("</tm>");
		return content.toString();
	}

	private String createTbNode(List<DatabaseModelBean> dbList) {
		StringBuffer content = new StringBuffer();
		content.append("\n\t\t<tb>");
		for (int i = 0; i < dbList.size(); i++) {
			DatabaseModelBean dbModel = dbList.get(i);
			content.append(createDbNode(dbModel));
		}
		content.append("</tb>");
		return content.toString();
	}

	private String createDbNode(DatabaseModelBean dbModel) {
		StringBuffer content = new StringBuffer();
		String authority = "N";
		if (dbModel.isDefault()) {
			authority = "Y";
		}
		content.append("<db default='" + authority + "'>");
		content.append("<type>" + dbModel.getDbType() + "</type>");
		content.append("<name>" + dbModel.getDbName() + "</name>");
		content.append("<location>" + dbModel.getItlDBLocation() + "</location>");
		content.append("<instance>" + dbModel.getInstance() + "</instance>");
		content.append("<server>" + dbModel.getHost() + "</server>");
		content.append("<port>" + dbModel.getPort() + "</port>");
		content.append("<user>" + dbModel.getUserName() + "</user>");
		content.append("<password>" + DESImpl.encrypt(dbModel.getPassword()) + "</password>");
		content.append("<hasmatch>" + dbModel.isHasMatch() + "</hasmatch>");
		content.append("</db>");

		return content.toString();
	}

	private String createLanguageNode(Language srcLang, List<Language> targetLang) {
		StringBuffer content = new StringBuffer();
		content.append("\n\t\t<language>");
		if (srcLang != null) {
			content.append("<source code='" + srcLang.getCode() + "' image='" + srcLang.getImagePath() + "' isbidi='"
					+ srcLang.isBidi() + "'>");
			content.append(srcLang.getName());
			content.append("</source>");
		}
		for (int i = 0; i < targetLang.size(); i++) {
			Language tLang = targetLang.get(i);
			content.append("<target code='" + tLang.getCode() + "' image='" + tLang.getImagePath() + "' isbidi='"
					+ tLang.isBidi() + "'>");
			content.append(tLang.getName());
			content.append("</target>");
		}
		content.append("</language>");
		return content.toString();
	}

	private String createFieldListNode(Map<String, String> mapField) {
		StringBuffer content = new StringBuffer();
		content.append("\n\t\t<fieldList>");
		Iterator<Entry<String, String>> it = mapField.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) it.next();
			content.append("\n\t\t\t<field name='" + entry.getKey() + "'>" + entry.getValue() + "</field>");
		}
		content.append("\n\t\t</fieldList>");
		return content.toString();
	}

	private String createAttrListNode(Map<String, Object[]> mapAttr) {
		StringBuffer content = new StringBuffer();
		content.append("\n\t\t<attributeList>");
		Iterator<Entry<String, Object[]>> it = mapAttr.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object[]> entry = (Entry<String, Object[]>) it.next();
			String attrName = entry.getKey();
			String attrSelVal = (String) entry.getValue()[0];
			@SuppressWarnings("unchecked")
			List<String> lstAttrVal = (List<String>) entry.getValue()[1];
			content.append("\n\t\t\t<attribute name='" + attrName + "' selection='"+attrSelVal+"'>");
			for (String attrVal : lstAttrVal) {
				content.append("\n\t\t\t\t<item>" + attrVal + "</item>");
			}
			content.append("\n\t\t\t</attribute>");
		}
		content.append("\n\t\t</attributeList>");
		return content.toString();
	}

	/**
	 * 2013-11-12 add by yule
	 *  重新从文件中加载一次数据
	 *  ;
	 */
	public  void  reloadConfig(){ 
		VTDGen vg = new VTDGen();
		try {
			if (vg.parseFile(this.projCfgFile.getPath(), true)) {
				vu = new VTDUtils(vg.getNav());
			} else {
				throw new ParseException();
			}
		} catch (NavException e) {
			logger.error("", e);
		} catch (ParseException e) {
			logger.error("", e);
		}
	}
	private String createProjectNameNode(String projectName) {
		StringBuffer content = new StringBuffer();
		content.append("\n\t\t<projectname>");
		// 将字符进行转义
		content.append(TextUtil.cleanSpecialString(projectName));
		content.append("</projectname>");
		return content.toString();
	}
}
