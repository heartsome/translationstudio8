package net.heartsome.cat.ts.ui.advanced.dialogs.srx;

import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.ts.ui.advanced.model.LanguageRuleBean;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 添加语言规则对话框
 * @author robert 2012-02-29
 * @version
 * @since JDK1.6
 */
public class AddOrEditLangRuleOfSrxDialog extends Dialog {
	private boolean isAdd;
	private List<LanguageRuleBean> langRulesList = new LinkedList<LanguageRuleBean>();
	private Button isBreakBtn;
	private Text preBreakTxt;
	private Text afterBreakTxt;
	/** 当前所添加的或正在修改的语言规则 */
	private LanguageRuleBean curLangRuleBean;

	public AddOrEditLangRuleOfSrxDialog(Shell parentShell, boolean isAdd, List<LanguageRuleBean> langRulesList) {
		super(parentShell);
		this.isAdd = isAdd;
		this.langRulesList = langRulesList;

	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(isAdd ? Messages.getString("srx.AddOrEditLangRuleOfSrxDialog.title1") : Messages
				.getString("srx.AddOrEditLangRuleOfSrxDialog.title2"));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tparent);

		Composite langCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).hint(450, 100).applyTo(langCmp);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(langCmp);

		isBreakBtn = new Button(langCmp, SWT.CHECK);
		isBreakBtn.setText(Messages.getString("srx.AddOrEditLangRuleOfSrxDialog.isBreakBtn"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, SWT.DEFAULT)
				.applyTo(isBreakBtn);

		Label preLbl = new Label(langCmp, SWT.NONE);
		preLbl.setText(Messages.getString("srx.AddOrEditLangRuleOfSrxDialog.preLbl"));

		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);

		preBreakTxt = new Text(langCmp, SWT.BORDER);
		preBreakTxt.setLayoutData(textData);

		Label afterLbl = new Label(langCmp, SWT.NONE);
		afterLbl.setText(Messages.getString("srx.AddOrEditLangRuleOfSrxDialog.afterLbl"));

		afterBreakTxt = new Text(langCmp, SWT.BORDER);
		afterBreakTxt.setLayoutData(textData);

		return tparent;
	}

	@Override
	protected void okPressed() {
		String isBreak = isBreakBtn.getSelection() ? "yes" : "no";
		String preBreak = preBreakTxt.getText();
		String afterBreak = afterBreakTxt.getText();

		LanguageRuleBean bean = new LanguageRuleBean(isBreak, preBreak, afterBreak);

		if (isAdd) {
			// 添加之前验证是否重复
			if (langRulesList.indexOf(bean) == -1) {
				langRulesList.add(bean);
				this.curLangRuleBean = bean;
			} else {
				MessageDialog.openInformation(getShell(), Messages.getString("srx.AddOrEditLangRuleOfSrxDialog.msgTitle1"),
						Messages.getString("srx.AddOrEditLangRuleOfSrxDialog.msg1"));
				return;
			}
		} else {
			// 先验证是否该条数据是否已经被修改，如果已经被修改，但是修改后的数据已经存在，那么直接覆盖
			if (!curLangRuleBean.equals(bean)) {
				if (langRulesList.indexOf(bean) != -1) {
					boolean response = MessageDialog.openConfirm(getShell(), Messages.getString("srx.AddOrEditLangRuleOfSrxDialog.msgTitle2"),
							Messages.getString("srx.AddOrEditLangRuleOfSrxDialog.msg2"));
					if (response) {
						langRulesList.remove(curLangRuleBean);
						curLangRuleBean = bean;
					} else {
						return;
					}
				} else {
					// 如果修改后的数据没有重复，那么删除修改之前的数据，并且添加到修改前数据的位置
					langRulesList.add(langRulesList.indexOf(curLangRuleBean), bean);
					langRulesList.remove(curLangRuleBean);
					curLangRuleBean = bean;
				}
			}
		}
		super.okPressed();
	}

	/**
	 * 设置编辑的初始化数据
	 * @param editBean
	 *            ;
	 */
	public void setEditInitData(LanguageRuleBean editBean) {
		curLangRuleBean = editBean;
		if (editBean.getIsBreak().equals("yes")) {
			isBreakBtn.setSelection(true);
		}
		preBreakTxt.setText(editBean.getPreBreak());
		afterBreakTxt.setText(editBean.getAfterBreak());
	}

	public LanguageRuleBean getCurLangRuleBean() {
		return curLangRuleBean;
	}
}
