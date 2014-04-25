package net.heartsome.cat.ts.ui.qa.dialogs;

import net.heartsome.cat.ts.ui.qa.model.NontransElementBean;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 添加或修改非译元素的对话框
 * @author robert	2013-01-02
 */
public class AddOrEditNontransElementDialog extends Dialog {
	/** 是否是修改模式 */
	private boolean isAdd;
	private TableViewer tableViewer;
	private NontransElementBean curElementBean;
	
	private static final String REGULAR_ignore = "(?i)";
	private static final String REGULAR_allWrod = "\\b";

	private Text tipTxt;
	private Text contentTxt;
	private Text regularTxt;
	
	/**	整词匹配 */
	private Button wordBtn;
	/** 忽略大小写 */
	private Button ignoreCaseBtn; 

	
	
	public AddOrEditNontransElementDialog(Shell parentShell, boolean isAdd, TableViewer tableViewer, NontransElementBean curElementBean) {
		super(parentShell);
		this.isAdd = isAdd;
		this.tableViewer = tableViewer;
		this.curElementBean = curElementBean;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(isAdd ? Messages.getString("qa.preference.NonTranslationQAPage.addModle")
				: Messages.getString("qa.preference.NonTranslationQAPage.editModle"));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button okBtn = getButton(IDialogConstants.OK_ID);
		okBtn.setText(Messages.getString("qa.preference.NonTranslationQAPage.enterBtn"));
		
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText(Messages.getString("qa.preference.NonTranslationQAPage.cancelBtn"));
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().hint(500, 180).grab(true, true).applyTo(tparent);

		Composite composite = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(composite);
		
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).equalWidth(false).numColumns(2).applyTo(composite);

		GridData textData = new GridData(GridData.FILL, SWT.CENTER, true, false);
		//非译元素的名称
		Label tipLbl = new Label(composite, SWT.NONE);
		tipLbl.setText(Messages.getString("qa.preference.NonTranslationQAPage.tip"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(tipLbl);
		
		tipTxt = new Text(composite, SWT.BORDER);
		tipTxt.setLayoutData(textData);

		//非译元素的非译内容
		Label contentLbl = new Label(composite, SWT.NONE);
		contentLbl.setText(Messages.getString("qa.preference.NonTranslationQAPage.content"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(contentLbl);

		contentTxt = new Text(composite, SWT.BORDER);
		contentTxt.setLayoutData(textData);

		//非译元素的正则表达式
		Label regularLbl = new Label(composite, SWT.NONE);
		regularLbl.setText(Messages.getString("qa.preference.NonTranslationQAPage.regular"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(regularLbl);

		regularTxt = new Text(composite, SWT.BORDER);
		regularTxt.setLayoutData(textData);

		
		// 正则表达式自定义选项
		new Label(composite, SWT.NONE);
		Composite pesonalCmp = new Composite(composite, SWT.NONE);
		pesonalCmp.setLayoutData(textData);
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).applyTo(pesonalCmp);
		
		wordBtn = new Button(pesonalCmp, SWT.CHECK);
		wordBtn.setText(Messages.getString("qa.preference.NonTranslationQAPage.addTip1"));
		
		ignoreCaseBtn = new Button(pesonalCmp, SWT.CHECK);
		ignoreCaseBtn.setText(Messages.getString("qa.preference.NonTranslationQAPage.addTip2"));
		
		new Label(composite, SWT.NONE);
		initListener();
		initValue();
		return tparent;
	}
	
	/**
	 * 一些事件的添加
	 */
	public void initListener() {
		contentTxt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				if (!contentTxt.isFocusControl()) {
					return;
				}
				String regular = contentTxt.getText();
				if (regular.length() == 0) {
					regularTxt.setText("");
					return;
				}
				if (ignoreCaseBtn.getSelection()) {
					regular = "(?i)" + regular;
				}
				if (wordBtn.getSelection()) {
					regular = "\\b" + regular + "\\b";
				}
				regularTxt.setText(regular);
			}
		});
		
		regularTxt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				if (!regularTxt.isFocusControl()) {
					// 如果正则表达式文本段没有焦点，那么就决定 两个按钮的状态
					String curRegular = regularTxt.getText();
					wordBtn.setSelection(curRegular.contains(REGULAR_allWrod));
					ignoreCaseBtn.setSelection(curRegular.contains(REGULAR_ignore));
				}
			}
		});
		
		ignoreCaseBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				setIgnoreRegular();
			}
			public void widgetDefaultSelected(SelectionEvent arg0) {
				setIgnoreRegular();
			}
		});
		
		wordBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				setAllWordRegular();
			}
			public void widgetDefaultSelected(SelectionEvent arg0) {
				setAllWordRegular();
			}
		});
	}
	
	/**
	 * 编辑模式下初始化相关数据
	 */
	private void initValue(){
		if (!isAdd && curElementBean != null) {
			tipTxt.setText(curElementBean.getName());
			contentTxt.setText(curElementBean.getContent());
			regularTxt.setText(curElementBean.getRegular());
		}
	}

	/**
	 * 给正则表达式设置忽略大小写
	 */
	private void setIgnoreRegular(){
		String regular = regularTxt.getText();
		if (regular.length() <= 0) {
			return;
		}
		
		if (ignoreCaseBtn.getSelection()) {
			if (!regular.contains("(?i)")) {
				int index = -1;
				if ((index = regular.indexOf("\\b")) != -1) {
					regular = regular.substring(0, index + REGULAR_allWrod.length())
						+ REGULAR_ignore + regular.substring(index + REGULAR_allWrod.length());
				}else {
					String content = contentTxt.getText();
					if (content.length() > 0 && (index = regular.indexOf(content)) != -1) {
						regular = regular.substring(0, index)
							+ REGULAR_ignore + regular.substring(index);
					}else if (content.length() > 0 && (index = regular.indexOf(content)) == -1) {
						regular = REGULAR_ignore + regular;
					}
				}
			}
		}else {
			if (regular.contains(REGULAR_ignore)) {
				regular = regular.replace(REGULAR_ignore, "");
			}
		}
		regularTxt.setText(regular);
		
	}
	
	/**
	 * 给正则表达式设置整词匹配
	 */
	private void setAllWordRegular(){
		String regular = regularTxt.getText();
		if (regular.length() <= 0) {
			return;
		}
		
		if (wordBtn.getSelection()) {
			// 一个 \\b 都没得
			regular = regular.replace(REGULAR_allWrod, "");
			regular = REGULAR_allWrod + regular + REGULAR_allWrod;
		}else {
			regular = regular.replace(REGULAR_allWrod, "");
		}
		regularTxt.setText(regular);
		
	}


	@Override
	protected void okPressed() {
		String name = tipTxt.getText();
		String content = contentTxt.getText();
		String regular = regularTxt.getText();
		
		if ("".equals(name) || name == null) {
			MessageDialog.openWarning(getShell(), Messages.getString("qa.all.dialog.warning"), Messages.getString("qa.preference.NonTranslationQAPage.tip2"));
			return;
		}

		if ("".equals(content) || content == null) {
			MessageDialog.openWarning(getShell(), Messages.getString("qa.all.dialog.warning"), Messages.getString("qa.preference.NonTranslationQAPage.tip3"));
			return;
		}
		
		// 添加模式时才会指定 id
		if (isAdd) {
			String id = "" + System.nanoTime();
			curElementBean.setId(id);
		}
		curElementBean.setName(name);
		curElementBean.setContent(content);
		curElementBean.setRegular(regular);
		// 如果重复，将不添加
		if (validElementIsRepeate(curElementBean)) {
			return;
		}
		super.okPressed();
	}
	
	
	/**
	 * 验证非译元素是否重复添加，如果是，则返回 true,如果不是，则返回 false
	 * @param bean
	 * @return
	 */
	private boolean validElementIsRepeate(NontransElementBean bean){
		// 判断是否是重复添加
		int eleSum = tableViewer.getTable().getItemCount();
		for (int i = 0; i < eleSum; i++) {
			if (tableViewer.getElementAt(i) instanceof NontransElementBean) {
				NontransElementBean curBean = (NontransElementBean) tableViewer.getElementAt(i);
				if (bean.equalsOfEdit(curBean)) {
					MessageDialog.openWarning(getShell(), Messages.getString("qa.all.dialog.warning"),
							Messages.getString("qa.preference.NonTranslationQAPage.addTip6"));
					return true;
				}
			}else {
				continue;
			}
		}
		return false;
	}
	
}
