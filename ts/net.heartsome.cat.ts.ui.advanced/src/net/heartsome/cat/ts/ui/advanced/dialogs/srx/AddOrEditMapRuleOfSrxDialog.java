package net.heartsome.cat.ts.ui.advanced.dialogs.srx;

import java.util.List;

import net.heartsome.cat.ts.ui.advanced.handlers.ADXmlHandler;
import net.heartsome.cat.ts.ui.advanced.model.MapRuleBean;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 添加或修改映射规则窗体
 * @author robert 2012-03-02
 * @version
 * @since JDK1.6
 */
public class AddOrEditMapRuleOfSrxDialog extends Dialog {
	private ADXmlHandler handler;
	private boolean isAdd;
	private Text langModelTxt;
	private Combo langRuleNameCmb;
	private List<MapRuleBean> mapRulesList;
	private MapRuleBean curMapRuleBean;
	private String srxLocation;

	public AddOrEditMapRuleOfSrxDialog(Shell parentShell, boolean isAdd, List<MapRuleBean> mapRulesList,
			ADXmlHandler handler, String srxLocation) {
		super(parentShell);
		this.isAdd = isAdd;
		this.mapRulesList = mapRulesList;
		this.handler = handler;
		this.srxLocation = srxLocation;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(isAdd ? Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.title1") : Messages
				.getString("srx.AddOrEditMapRuleOfSrxDialog.title2"));
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

		Label modelLbl = new Label(langCmp, SWT.NONE);
		modelLbl.setText(Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.modelLbl"));

		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);

		langModelTxt = new Text(langCmp, SWT.BORDER);
		langModelTxt.setLayoutData(textData);

		Label langRuleNameLbl = new Label(langCmp, SWT.NONE);
		langRuleNameLbl.setText(Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.langRuleNameLbl"));

		langRuleNameCmb = new Combo(langCmp, SWT.BORDER | SWT.READ_ONLY);
		langRuleNameCmb.setLayoutData(textData);
		// 给语言规则名称下拉框赋值
		langRuleNameCmb.setItems(handler.getLanguageRuleNamesOfSrx_2(srxLocation).toArray(new String[] {}));

		return tparent;
	}

	@Override
	protected void okPressed() {
		String langModel = langModelTxt.getText().trim();
		String langRuleName = langRuleNameCmb.getText().trim();
		MapRuleBean bean = new MapRuleBean(langModel, langRuleName);

		if ("".equals(langModel) || "".equals(langRuleName)) {
			MessageDialog.openInformation(getShell(), Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.msgTitle1"),
					Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.msg1"));
			return;
		}

		if (isAdd) {
			// 添加之前验证是否重复
			if (mapRulesList.indexOf(bean) == -1) {
				mapRulesList.add(bean);
				this.curMapRuleBean = bean;
			} else {
				MessageDialog.openInformation(getShell(),
						Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.msgTitle1"),
						Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.msg2"));
				return;
			}
		} else {
			// 先验证是否该条数据是否已经被修改，如果已经被修改，但是修改后的数据已经存在，那么直接覆盖
			if (!curMapRuleBean.equals(bean)) {
				if (mapRulesList.indexOf(bean) != -1) {
					boolean response = MessageDialog.openConfirm(getShell(),
							Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.msgTitle2"),
							Messages.getString("srx.AddOrEditMapRuleOfSrxDialog.msg3"));
					if (response) {
						mapRulesList.remove(curMapRuleBean);
						curMapRuleBean = bean;
					} else {
						return;
					}
				} else {
					// 如果修改后的数据没有重复，那么删除修改之前的数据，并且添加到修改前数据的位置
					mapRulesList.add(mapRulesList.indexOf(curMapRuleBean), bean);
					mapRulesList.remove(curMapRuleBean);
					curMapRuleBean = bean;
				}
			}
		}
		super.okPressed();
	}

	public MapRuleBean getCurMapRuleBean() {
		return curMapRuleBean;
	}

	public void setEditInitData(MapRuleBean editBean) {
		curMapRuleBean = editBean;
		langModelTxt.setText(editBean.getLanguageModel());
		langRuleNameCmb.setText(editBean.getLangRuleName());
	}
}
