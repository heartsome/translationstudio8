package net.heartsome.cat.ts.ui.qa;

import java.util.Map;

import net.heartsome.cat.ts.core.qa.QATUDataBean;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 所有品质检查项实现类的父类
 * @author robert 2011-11-10
 */
public abstract class QARealization {
	private QAResult qaResult = null;
	public final static Logger logger = LoggerFactory.getLogger(QARealization.class.getName());

	
	/**
	 * @param model
	 *            存放品质检查所用到的相关属性
	 * @param monitor
	 *            进度条实例
	 * @param iFile
	 *            要品质检查的文件
	 * @param handler
	 *            对文件操作的类
	 * @return
	 */
	public int start(QAModel model, IProgressMonitor monitor, IFile iFile, QAXmlHandler xmlHandler, QAResult qaResult) {
		this.qaResult = qaResult;
		return -1;
	}
	/**
	 * 给本类设置消息传送model
	 * @param qaResult ;
	 */
	public void setQaResult(QAResult qaResult) {
		this.qaResult = qaResult;
	}
	/**
	 * 当初始化品质检查项实例时，设置消息传送model
	 * @param qaResult ;
	 */
	abstract void setParentQaResult(QAResult qaResult);


	/**
	 * 将品质检查的结果输出到相关视图
	 * @param lineNumber
	 *            行号
	 * @param qaType
	 *            检查类别
	 * @param errorTip
	 *            错误信息提示
	 * @param filePath
	 *            文件资源
	 * @param langPair
	 *            语言对
	 */
	public void printQAResult(final QAResultBean bean) {
		// 设置当前的数据是否是 自动品质检查 的数据
		bean.setAutoQA(qaResult.isAutoQA());
		qaResult.firePropertyChange(bean);
	}
	
	/**
	 * 关闭数据库连接，目前只针对术语一致性分析
	 *  ;
	 */
	public void closeDB(){}
	
	/**
	 * 品质检查，针对于自动检查中，若返回结果的字符串长度大于1，则标识错误
	 * <div style="color:red">当未设置术语库、拼写检查词典配置 错误时才会返回 null，所以这时可以直接删。</div>
	 * @param model
	 * @param monitor
	 * @param iFile
	 * @param xmlHandler
	 * @param qaResult
	 * @param tuMap
	 * @return ;
	 */
	public String startQA(QAModel model, IProgressMonitor monitor, IFile iFile, QAXmlHandler xmlHandler,
			QATUDataBean tuDataBean) {
		return "";
	}
	
}
