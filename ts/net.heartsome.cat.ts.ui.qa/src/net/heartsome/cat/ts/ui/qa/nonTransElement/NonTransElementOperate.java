package net.heartsome.cat.ts.ui.qa.nonTransElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.NontransElementBean;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 非译元素处理类，包括获取相关数据，新建非译元素库（xml）等。
 * 默认非译元素库的库名为	.nonTransElement,保存在工作空间下
 * 其结构为
 * <nonTrans>		//这是根节点
 * 		<element id='152054313205'>			//id为毫秒
 * 			<name>			//非译元素的名字，对应界面上的说明
 * 				ip地址
 * 			</name>
 * 			<content>		//非译元素的内容
 * 				http://....
 * 			</content>
 * 			<regular>		//非译元素的正则表达式
 * 				\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+
 * 			</regular>
 * 		</element>
 * </nonTrans>
 * @author robert	2011-11-31
 */
public class NonTransElementOperate {
	private String fileName = ".nonTransElement";
	private QAXmlHandler handler;
	private String path;
	public final static Logger logger = LoggerFactory.getLogger(NonTransElementOperate.class.getName());
	
	public NonTransElementOperate(){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		handler = new QAXmlHandler();
		path = root.getLocation().append(fileName).toOSString();
		
		validExist();
	}
	
	/**
	 * 首先验证.nonTransElement是否存在,若不存在，则进行创建，
	 */
	public void validExist(){
		
		File file = new File(path);
		if (!file.exists()) {
			FileOutputStream output;
			try {
				output = new FileOutputStream(path);
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
				output.write("<nonTrans>\n</nonTrans>".getBytes("UTF-8"));
				output.close();
				openNonTransDB();
				addNonTransElement(QAModel.getInterNonTransElements());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.preference.NonTranslationQAPage.log1"), e);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.preference.NonTranslationQAPage.log2"), e);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.preference.NonTranslationQAPage.log3"), e);
			}
		}
	}
	
	/**
	 * 打开非译元素库，即解析非译元素所在的xml文件
	 * @return
	 */
	public boolean openNonTransDB(){
		Map<String, Object> newResultMap = handler.openFile(path);
		if (newResultMap == null
				|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap.get(QAConstant.RETURNVALUE_RESULT)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					Assert.isNotNull(window);
					Shell shell = window.getShell();
					MessageDialog.openError(shell, Messages.getString("qa.all.dialog.error"), MessageFormat.format(
							Messages.getString("qa.all.log.openThisXmlError"), new Object[] { path }));
				}
			});
			return false;
		}
		return true;
	}
	
	
	public List<NontransElementBean> getNonTransElements(){
		List<NontransElementBean> result = new ArrayList<NontransElementBean>();
		List<Map<String, String>> elementList = handler.getNonTransElements(path, "/nonTrans/element");
		for(Map<String, String> element : elementList){
			String id = element.get("id");
			String name = element.get("name");
			String content = element.get("content");
			String regular = TextUtil.resetSpecialString(element.get("regular"));
			result.add(new NontransElementBean(id, name, content, regular));
		}
		return result;
	}
	
	/**
	 * 只获取非译元素的 正则表达式
	 */
	public List<String> getNontransElementRegex(){
		List<String> regexList = new ArrayList<String>();
		regexList = handler.getNonTransElementsRegex(path);
		return regexList;
	}
	
	/**
	 * 添加非译元素
	 * @return
	 */
	public boolean addNonTransElement(List<NontransElementBean> elementList){
		StringBuffer dataSB = new StringBuffer();
		for(NontransElementBean bean : elementList){
			dataSB.append("\t<element id='"+ bean.getId() +"'>\n") ;
			dataSB.append("\t\t<name>" + bean.getName() + "</name>\n") ;
			dataSB.append( "\t\t<content>" + bean.getContent() + "</content>\n");
			dataSB.append( "\t\t<regular>" + TextUtil.cleanSpecialString(bean.getRegular()) + "</regular>\n");
			dataSB.append("\t</element>\n") ;
		}
		if (elementList.size() > 0) {
			return handler.addDataToXml(path, "/nonTrans", dataSB.substring(0, dataSB.length() - 1));
		}
		return false;
	}
	
	
	/**
	 * 根据配置的map值获取String类型的数据
	 * @param configList
	 * @return
	 */
	private String getConfig(List<Map<String, String>> configList){
		StringBuffer configSB = new StringBuffer();
		configSB.append("<configs>\n");
		for (Map<String, String> configMap : configList) {
			String position = configMap.get("position");
			String operate = configMap.get("operate");
			String value = configMap.get("value");
			configSB.append(MessageFormat.format("\t\t\t<config position=\"{0}\" operate=\"{1}\" value=\"{2}\"></config>\n",
					new Object[] { position, operate, value }));
		}
		configSB.append("\t\t</configs>\n");
		return configSB.toString();
	}
	
	/**
	 * 从非译元素库时面删除非译元素
	 * @param idList
	 * @return
	 */
	public void deleteElement(List<String> idList){
		for (int i = 0; i < idList.size(); i++) {
			String nodeXpath = "/nonTrans/element[@id='" + idList.get(i) + "']";
			handler.deleteNode(path, nodeXpath);
		}
	}
	
	/**
	 * 删除所有的非译元素
	 */
	public void deleteAllElement(){
		String nodeXpath = "/nonTrans/element";
		handler.deleteAllNode(path, nodeXpath);
	}
	
	/**
	 * 获取忽略非译元素的文本片段的起始与结束的标记
	 * @param List<Integer[]> 第一个数为 start index， 第二个数为 end index
	 * @return
	 */
	public List<Integer[]> getIgnorePara(String text, List<Integer> tagPositionList){
		List<Integer[]> ignoreParaList = new LinkedList<Integer[]>();
		List<String> regexList = getNontransElementRegex();
		Matcher matcher = null;
		for (String regex : regexList) {
			matcher = Pattern.compile(regex).matcher(text);
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				int startAdd = 0;
				int endAdd = 0;
				
				// 将标记放回去，使每个非译片段回复之前未去标记的状态
				if (tagPositionList != null) {
					for(Integer tagIndex : tagPositionList){
						if (start >= tagIndex) {
							startAdd ++;
						}
						if (end >= tagIndex) {
							endAdd ++;
						}
					}
				}
				ignoreParaList.add(new Integer[]{start + startAdd, end + endAdd});
			}
		}
		return ignoreParaList;
	}
}
