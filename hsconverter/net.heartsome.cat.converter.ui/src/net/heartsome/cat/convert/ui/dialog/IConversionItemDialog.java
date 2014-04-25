package net.heartsome.cat.convert.ui.dialog;

import net.heartsome.cat.convert.ui.model.IConversionItem;

/**
 * 选择转换项目的适配器接口，具体的实现可以为：在 RCP 环境中为 FileDialog 或 WorkspaceDialog；在 RAP 环境中为文件上传界面。
 * @author cheney
 * @since JDK1.6
 */
public interface IConversionItemDialog {

	/**
	 * @return 打开转换项目选择对话框，并返回操作结果：用户点击确定按钮则返回 IDialogConstants.OK_ID;
	 */
	int open();

	/**
	 * 获得转换项目
	 * @return 返回转换项目，如果用户没有选择任何转换项目，则返回一个空的转换项目;
	 */
	IConversionItem getConversionItem();

}
