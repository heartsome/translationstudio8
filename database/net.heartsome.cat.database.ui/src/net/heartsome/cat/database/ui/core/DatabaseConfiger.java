/**
 * DatabaseConfiger.java
 *
 * Version information :
 *
 * Date:Dec 1, 2011
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.database.ui.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.resources.ResourcesPlugin;
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
 * 数据库服务器配置文件管理
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class DatabaseConfiger {

	private VTDUtils vu;
	private File serverConfigFile;
	private AutoPilot ap;
	private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiger.class);

	/**
	 * 构造器,如果文件不存在先构建文件,再用vtd解析.
	 */
	public DatabaseConfiger() {
		initResource();
		VTDGen vg = new VTDGen();
		try {
			if (vg.parseFile(serverConfigFile.getPath(), true)) {
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
		File dbListFile = new File(path + System.getProperty("file.separator") + ".servers");
		if (!dbListFile.exists() || dbListFile.isDirectory()) {
			try {
				FileOutputStream fos = new FileOutputStream(dbListFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				StringBuffer bf = new StringBuffer();
				bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				bf.append("<servers>");
				bf.append("</servers>");
				bos.write(bf.toString().getBytes());
				bos.close();
				fos.close();
			} catch (IOException e) {
				logger.error(Messages.getString("core.DatabaseConfiger.logger1"), e);
			}
		}
		this.serverConfigFile = dbListFile;
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

	/**
	 * 更新指定id的server配置信息
	 * @param id
	 * @param serverBean
	 *            ;
	 */
	public void updateServerConfigById(String id, DatabaseModelBean serverBean) {
		String newValue = generateServerNode(serverBean.getDbType(), serverBean);
		XMLModifier xm = vu.update("/servers/server[@id='" + id + "']", newValue);
		try {
			vu.bind(xm.outputAndReparse());
			saveToFile(xm, serverConfigFile);
		} catch (Exception e) {
			logger.error(Messages.getString("core.DatabaseConfiger.logger2"), e);
		}
	}

	/**
	 * 删除指定id的服务器配置信息
	 * @param id
	 *            服务器配置信息id;
	 */
	public void deleteServerById(String id) {
		if (id != null && !id.equals("")) {
			try {
				XMLModifier xm = vu.delete("/servers/server[@id='" + id + "']");
				vu.bind(xm.outputAndReparse());
				saveToFile(xm, serverConfigFile);
			} catch (Exception e) {
				logger.error(Messages.getString("core.DatabaseConfiger.logger3"), e);
			}
		}
	}

	/**
	 * 添加一个全新的服务器到配置文件
	 * @param serverBean
	 *            ;
	 */
	public void addServerConfig(DatabaseModelBean serverBean) {
		serverBean.setId(this.generateServerId());
		String content = generateServerNode(serverBean.getDbType(), serverBean);
		if (!content.equals("")) {
			try {
				XMLModifier xm = vu.insert("/servers/text()", content.toString());
				vu.bind(xm.outputAndReparse());
				saveToFile(xm, serverConfigFile);
			} catch (Exception e) {
				logger.error("", e);
			}
			ap.resetXPath();
		}
	}

	/**
	 * 获取数据库服务器配置文件中保存的所有服务器信息
	 * @return Map<数据库类型,List<数据库配置信息> 参考{@link DatabaseModelBean};
	 */
	public Map<String, List<DatabaseModelBean>> getAllServerConfig() {
		Map<String, List<DatabaseModelBean>> map = new HashMap<String, List<DatabaseModelBean>>();
		try {
			AutoPilot tempAp = new AutoPilot(vu.getVTDNav());
			tempAp.selectXPath("/servers/server");
			while (tempAp.evalXPath() != -1) {
				String type = vu.getCurrentElementAttribut("type", "");
				if (map.containsKey(type)) {
					continue;
				} else {
					map.put(type, getServersConfigByType(type));
				}
			}
		} catch (XPathParseException e) {
			logger.error("", e);
		} catch (XPathEvalException e) {
			logger.error("", e);
		} catch (NavException e) {
			logger.error("", e);
		}
		return map;
	}

	/**
	 * 获取指定类型数据库的所有服务器配置信息
	 * @param type
	 *            数据库类型
	 * @return ;
	 */
	public List<DatabaseModelBean> getServersConfigByType(String type) {
		List<DatabaseModelBean> list = new ArrayList<DatabaseModelBean>();
		try {
			vu.getVTDNav().push();
			ap.selectXPath("/servers/server[@type='" + type + "']");
			while (ap.evalXPath() != -1) {
				DatabaseModelBean dbm = new DatabaseModelBean();
				dbm.setId(vu.getCurrentElementAttribut("id", ""));
				dbm.setItlDBLocation(vu.getChildContent("location"));
				dbm.setInstance(vu.getChildContent("instance"));
				dbm.setHost(vu.getChildContent("host"));
				dbm.setPort(vu.getChildContent("port"));
				dbm.setUserName(vu.getChildContent("user"));
				dbm.setPassword(DESImpl.decrypt(vu.getChildContent("password")));
				dbm.setDbType(type);
				list.add(dbm);
			}
			ap.resetXPath();
			vu.getVTDNav().pop();
		} catch (XPathParseException e) {
			logger.error("", e);
		} catch (XPathEvalException e) {
			logger.error("", e);
		} catch (NavException e) {
			logger.error("", e);
		}
		return list;
	}

	/**
	 * 生成配置文件中的<server>节点
	 * @param type
	 *            数据库类型
	 * @param serverBean
	 *            服务器配置信息
	 * @return 生成的内容文本;
	 */
	private String generateServerNode(String type, DatabaseModelBean serverBean) {
		StringBuffer content = new StringBuffer();

		content.append("<server type='" + type + "' id='" + serverBean.getId() + "'>");

		content.append("<location>");
		content.append(serverBean.getItlDBLocation());
		content.append("</location>");

		content.append("<instance>");
		content.append(serverBean.getInstance());
		content.append("</instance>");

		content.append("<host>");
		content.append(serverBean.getHost());
		content.append("</host>");

		content.append("<port>");
		content.append(serverBean.getPort());
		content.append("</port>");

		content.append("<user>");
		content.append(serverBean.getUserName());
		content.append("</user>");

		content.append("<password>");
		content.append(DESImpl.encrypt(serverBean.getPassword()));
		content.append("</password>");

		content.append("</server>");

		return content.toString();
	}

	/**
	 * 生成Server配置ID
	 * @return 一个字符串,当前操作系统中唯一 ;
	 */
	public String generateServerId() {
		StringBuffer bf = new StringBuffer();
		bf.append(System.currentTimeMillis());
		bf.append(new Random().nextInt(10000));
		return bf.toString();
	}

	/**
	 * 判断是否存在相同的服务器.存在则返加id
	 * @param xpath
	 *            需要查询的Server的xpath
	 * @return ;
	 */
	private String existServer(String xpath) {
		try {
			ap = new AutoPilot(vu.getVTDNav());
			ap.selectXPath("/servers/server[" + xpath + "]");
			if (ap.evalXPath() != -1) {
				return vu.getCurrentElementAttribut("id", "");
			}
		} catch (Exception e) {
			logger.error(Messages.getString("core.DatabaseConfiger.logger4"), e);
		}
		return "";
	}

	/**
	 * 根据当前的数据库类型,生成XPATH,并调用{@link DatabaseConfiger}中判断服务器在配置文件中是否存在
	 * @param bean
	 *            需要检查是否存在的服务器配置信息
	 * @param metaData
	 *            数据库元数据
	 * @return 存在返回当前配置信息在文件中的id;
	 */
	public String isServerExist(DatabaseModelBean bean, MetaData metaData) {
		List<String> xpaths = new ArrayList<String>();
		if (metaData.dataPathSupported()) {
			xpaths.add("location='" + bean.getItlDBLocation() + "'");
		}
		if (metaData.serverNameSupported()) {
			xpaths.add("host='" + bean.getHost() + "'");
		}
		if (metaData.portSupported()) {
			xpaths.add("port='" + bean.getPort() + "'");
		}
		if (metaData.instanceSupported()) {
			xpaths.add("instance='" + bean.getInstance() + "'");
		}

		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < xpaths.size(); i++) {
			bf.append(xpaths.get(i));
			if (i != xpaths.size() - 1) {
				bf.append(" and ");
			}
		}
		return existServer(bf.toString());
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