package net.heartsome.cat.ts.ui.advanced.dialogs;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.ts.ui.advanced.model.ElementBean;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * XML转换器配置的元素配置，包括添加或修改
 * @author robert 2012-02-23
 * @version
 * @since JDK1.6
 */
public class AddOrEditElementOfXmlConvertDialog extends Dialog {
	/** 是否是添加界面 */
	private boolean isAdd;
	/** 元素名称文本框 */
	private Text nameTxt;
	/** 类型下拉框 */
	private Combo typeCmb;
	/** 内联类型下拉框 */
	private Combo inlineCmb;
	/** 可翻译属性文本框 */
	private Text transAtrriTxt;
	/** 保留空格下拉框 */
	private Combo remainSpaceCmb;
	private List<ElementBean> elementsList;
	/** 当前所添加，或修改的元素，用于操作之后列表的定位 */
	private ElementBean currentElement;

	public AddOrEditElementOfXmlConvertDialog(Shell parentShell, boolean isAdd, List<ElementBean> elementsList) {
		super(parentShell);
		this.isAdd = isAdd;
		this.elementsList = elementsList;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.title"));

	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setText(Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.ok"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tparent);

		Composite composite = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(composite);

		// 元素类型下拉框的值， 备注：不能本地化
		String[] typeValues = { "segment", "inline", "ignore" };
		// 内联类型下拉框的值， 备注：不能本地化
		String[] internalValues = { "", "image", "pb", "lb", "x-bold", "x-entry", "x-font", "x-italic", "x-link",
				"x-underlined", "x-other" };
		// 保留空格下拉框的值， 备注：不能本地化
		String[] remainSpaceVlaues = { "", "yes", "no" };

		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textData.widthHint = 100;

		// 元素名称
		Label nameLbl = new Label(composite, SWT.NONE);
		nameLbl.setText(Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.nameLbl"));
		nameLbl.setAlignment(SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(nameLbl);

		nameTxt = new Text(composite, SWT.BORDER);
		nameTxt.setLayoutData(textData);

		// 元素类型
		Label typeLbl = new Label(composite, SWT.NONE);
		typeLbl.setText(Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.typeLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(typeLbl);

		typeCmb = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		typeCmb.setLayoutData(textData);
		typeCmb.setItems(typeValues);
		typeCmb.select(0);

		// 内联类型
		Label inlineLbl = new Label(composite, SWT.NONE);
		inlineLbl.setText(Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.inlineLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(inlineLbl);

		inlineCmb = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		inlineCmb.setLayoutData(textData);
		inlineCmb.setItems(internalValues);
		inlineCmb.setEnabled(false);

		// 可翻译属性
		Label transAttriLbl = new Label(composite, SWT.NONE);
		transAttriLbl.setText(Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.transAttriLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(transAttriLbl);

		transAtrriTxt = new Text(composite, SWT.BORDER);
		transAtrriTxt.setLayoutData(textData);

		// 保留空格
		Label remainSpaceLbl = new Label(composite, SWT.NONE);
		remainSpaceLbl.setText(Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.remainSpaceLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(remainSpaceLbl);

		remainSpaceCmb = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		remainSpaceCmb.setLayoutData(textData);
		remainSpaceCmb.setItems(remainSpaceVlaues);

		// 当元素类型是segment时，禁用内联内型，当元素类型是inline时，禁用可翻译属性。当元素类型是ignore时，禁用可翻译属性与内联内型
		typeCmb.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String type = typeCmb.getText();
				if ("segment".equals(type)) {
					inlineCmb.setText("");
					inlineCmb.setEnabled(false);
					transAtrriTxt.setEnabled(true);
				} else if ("inline".equals(type)) {
					inlineCmb.setEnabled(true);
					transAtrriTxt.setText("");
					transAtrriTxt.setEnabled(false);
				} else if ("ignore".equals(type)) {
					inlineCmb.setText("");
					inlineCmb.setEnabled(false);
					transAtrriTxt.setText("");
					transAtrriTxt.setEnabled(false);
				}
			}
		});

		return tparent;
	}

	@Override
	protected void okPressed() {
		String elementName = nameTxt.getText().trim();
		// 添加
		if (isAdd) {
			int index = -1;
			// 验证是否为空
			if (validNameNull(elementName)) {
				return;
			} else {
				ElementBean element = new ElementBean(elementName, typeCmb.getText(), inlineCmb.getText(),
						transAtrriTxt.getText().trim(), remainSpaceCmb.getText());
				// 验证添加的元素名是否重复
				if ((index = validrepeat(elementName)) != -1) {
					boolean response = MessageDialog.openConfirm(getShell(), Messages
							.getString("dialogs.AddOrEditElementOfXmlConvertDialog.msgTitle1"), MessageFormat.format(
							Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.msg1"), elementName));

					if (response) {
						elementsList.set(index, element);
					} else {
						return;
					}
				} else {
					// 没有重复。则添加
					elementsList.add(element);
				}
				currentElement = element;
			}
		} else {// 这是修改
				// 验证是否为空
			if (validNameNull(elementName)) {
				return;
			} else {
				int index = elementsList.indexOf(currentElement);
				ElementBean element = new ElementBean(elementName, typeCmb.getText(), inlineCmb.getText(),
						transAtrriTxt.getText().trim(), remainSpaceCmb.getText());
				elementsList.set(index, element);
				currentElement = element;
			}
		}
		super.okPressed();
	}

	/**
	 * 设置编辑时的初始化数据 ;
	 */
	public void setInitEditData(ElementBean bean) {
		nameTxt.setText(bean.getName());
		typeCmb.setText(bean.getType());
		inlineCmb.setText(bean.getInlineType());
		transAtrriTxt.setText(bean.getTransAttribute());
		remainSpaceCmb.setText(bean.getRemainSpace());

		currentElement = bean;
	}

	/**
	 * 验证所要添加的元素名是否重复 返回重复元素的下标值，若返回－1，则标志不重复
	 * @return ;
	 */
	public int validrepeat(String elementName) {
		Iterator<ElementBean> iter = elementsList.iterator();
		ElementBean bean;
		while (iter.hasNext()) {
			bean = iter.next();
			if (elementName.equals(bean.getName())) {
				return elementsList.indexOf(bean);
			}
		}
		return -1;
	}

	public ElementBean getElementBean(String elementName) {
		ElementBean bean;
		Iterator<ElementBean> iter = elementsList.iterator();
		while (iter.hasNext()) {
			bean = iter.next();
			if (elementName.equals(bean.getName())) {
				return bean;
			}
		}
		return null;
	}

	public ElementBean getCurrentElement() {
		return currentElement;

	}

	/**
	 * 验证元素名是否为空，针对用户未在元素名一栏填写元素名
	 * @param elementName
	 * @return ;若为空，则为true
	 */
	public boolean validNameNull(String elementName) {
		if (elementName == null || "".equals(elementName)) {
			MessageDialog.openInformation(getShell(),
					Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.msgTitle2"),
					Messages.getString("dialogs.AddOrEditElementOfXmlConvertDialog.msg2"));
			nameTxt.setFocus();
			return true;
		}
		return false;
	}
}
