package net.heartsome.cat.ts.test.ui.views;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableColumn;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * 视图：文档属性，单例模式
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class DocumentPropertiesView extends SWTBotView {

	private SWTBot viewBot = this.bot();
	private static SWTBotView view;

	/**
	 * 按标题查找视图
	 */
	private DocumentPropertiesView() {
		super(HSBot.bot().viewByTitle(TsUIConstants.getString("viewTitleDocumentProperties")).getReference(), HSBot
				.bot());
	}

	/**
	 * @return 文档属性视图实例;
	 */
	public static SWTBotView getInstance() {
		if (view == null) {
			view = new DocumentPropertiesView();
		}
		return view;
	}

	/**
	 * @return 按钮：接受;
	 */
	public SWTBotButton btnAccept() {
		return viewBot.button(TsUIConstants.getString("btnAccept"));
	}

	/**
	 * @return 按钮：添加;
	 */
	public SWTBotButton btnAdd() {
		return viewBot.button(TsUIConstants.getString("btnAdd"));
	}

	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return viewBot.button(TsUIConstants.getString("btnCancel"));
	}

	/**
	 * @return 按钮：删除;
	 */
	public SWTBotButton btnDelete() {
		return viewBot.button(TsUIConstants.getString("btnDelete"));
	}

	/**
	 * @return 按钮：编辑;
	 */
	public SWTBotButton btnEdit() {
		return viewBot.button(TsUIConstants.getString("btnEdit"));
	}

	/**
	 * @return 下拉列表：文件;
	 */
	public SWTBotCombo cmbWLblFile() {
		return viewBot.comboBoxWithLabel(TsUIConstants.getString("cmbWLblFile"));
	}

	/**
	 * @return 表格：文件属性;
	 */
	public SWTBotTable tblProperties() {
		return viewBot.table();
	}

	/**
	 * @return 表格列：属性名称;
	 */
	public SWTBotTableColumn tblColProperty() {
		return tblProperties().header(TsUIConstants.getString("tblColProperty"));
	}

	/**
	 * @return 表格列：属性值;
	 */
	public SWTBotTableColumn tblColValue() {
		return tblProperties().header(TsUIConstants.getString("tblColValue"));
	}

	/**
	 * @return 文本框：客户;
	 */
	public SWTBotText txtWLblClient() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblClient"));
	}

	/**
	 * @return 文本框：作业日期;
	 */
	public SWTBotText txtWLblJobDate() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblJobDate"));
	}

	/**
	 * @return 文本框：作业相关信息;
	 */
	public SWTBotText txtWLblJobInfo() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblJobInfo"));
	}

	/**
	 * @return 文本框：负责人;
	 */
	public SWTBotText txtWLblOwner() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblOwner"));
	}

	/**
	 * @return 文本框：项目相关信息;
	 */
	public SWTBotText txtWlblProjectInfo() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWlblProjectInfo"));
	}

	/**
	 * @return 文本框：骨架文件;
	 */
	public SWTBotText txtWLblSkeleton() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblSkeleton"));
	}

	/**
	 * @return 文本框：原始数据类型;
	 */
	public SWTBotText txtWLblSourceDataType() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblSourceDataType"));
	}

	/**
	 * @return 文本框：源文件编码;
	 */
	public SWTBotText txtWLblSourceEncoding() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblSourceEncoding"));
	}

	/**
	 * @return 文本框：源语言;
	 */
	public SWTBotText txtWLblSourceLanguage() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblSourceLanguage"));
	}

	/**
	 * @return 文本框：目标语言;
	 */
	public SWTBotText txtWLblTargetLanguage() {
		return viewBot.textWithLabel(TsUIConstants.getString("txtWLblTargetLanguage"));
	}
}
