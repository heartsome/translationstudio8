package net.heartsome.cat.ts.handlexlf.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.ts.core.bean.SegPointBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.handlexlf.Activator;
import net.heartsome.cat.ts.handlexlf.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * 设置分割点的　dialog
 * UNDO 这个类写完后，当作一个示例代码进行保存。
 * @author robert	2013-10-15
 *
 */
public class SetSplitPointByWordNumDialog extends Dialog {
	private Image addImg;
	private Image deleteImg;
	/** 每一个片段的 数据类集合，该集合用于开始创建列表，以及最后点击确定时的数据返回 */
	private List<SegPointBean> segList;
	private Map<String, Integer> rowWordNumMap;
	private int totalWordNum = 0;
	private int totalTuNum = 0;
	private XLFHandler xlfHandler;
	private String xlfPath;
	
	private Button averageBtn;
	private Button wordNumberBtn;
	private Spinner segSpiner;
	private StackLayout stack = new StackLayout();
	/** 按平均字数进行分割的面板 */
	private Composite averageContentCmp;
	/** 按指定字数分割的面板 */
	private ScrolledComposite wordNumberScroll;
	/** 片段列表所在的面板 */
	private Composite segParentCmp;
	private Font totalLblFont;
	
	
	/**
	 * @param parentShell
	 * @param rowWordNumMap		rowId-->wordNum 的健值对
	 * @param segList
	 */
	public SetSplitPointByWordNumDialog(Shell parentShell, XLFHandler xlfHandler, String xlfPath, Map<String, Integer> rowWordNumMap, List<SegPointBean> segList) {
		super(parentShell);
		this.rowWordNumMap = rowWordNumMap;
		this.xlfHandler = xlfHandler;
		this.xlfPath = xlfPath;
		// 初始化总字数以及总行数
		totalTuNum = rowWordNumMap.size();
		for (Entry<String, Integer> entry : rowWordNumMap.entrySet()) {
			totalWordNum += entry.getValue();
		}
		// 如果已经有分割点，那么直接生成，否则平分字数到两个自动生成的片段中去。
		this.segList = segList;
		if (this.segList.size() < 2) {
			this.segList.clear();
			// 多余的字数，留在后面一个片段
			int halfWordNum = totalWordNum / 2;
			int segWordNum = 0;
			for(Entry<String, Integer> entry : this.rowWordNumMap.entrySet()){
				if (segWordNum < halfWordNum) {
					segWordNum += entry.getValue();
					if (segWordNum >= halfWordNum) {
						this.segList.add(new SegPointBean(segWordNum));
						this.segList.add(new SegPointBean(totalWordNum - segWordNum));
						break;
					}
				}
			}
		}
		
		addImg = Activator.getImageDescriptor("images/addSign.png").createImage();
		deleteImg = Activator.getImageDescriptor("images/deleteSign.png").createImage();
		FontData fd = new FontData();
		fd.setStyle(SWT.BOLD);
		totalLblFont = new Font(Display.getDefault(), fd);
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public boolean close() {
		if (addImg != null && !addImg.isDisposed()) {
			addImg.dispose();
		}
		if (deleteImg != null && !deleteImg.isDisposed()) {
			deleteImg.dispose();
		}
		if (totalLblFont != null && !totalLblFont.isDisposed()) {
			totalLblFont.dispose();
		}
		return super.close();
	}

	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("SetSplitPointDialog.dialog.title"));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button okBtn = getButton(IDialogConstants.OK_ID);
		okBtn.setText(Messages.getString("all.dialog.ok"));
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText(Messages.getString("all.dialog.cancel"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentData.widthHint = 400;
		parentData.heightHint = 350;
		tparent.setLayoutData(parentData);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(tparent);
		
		Group typeGroup = new Group(tparent, SWT.NONE);
		typeGroup.setText(Messages.getString("SetSplitPointByWordNumDialog.splitType"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(typeGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(typeGroup);
		
		averageBtn = new Button(typeGroup, SWT.RADIO);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(averageBtn);
		averageBtn.setText(Messages.getString("SetSplitPointByWordNumDialog.splitByAverWordNum"));
		averageBtn.setSelection(true);
		
		wordNumberBtn = new Button(typeGroup, SWT.RADIO);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(wordNumberBtn);
		wordNumberBtn.setText(Messages.getString("SetSplitPointByWordNumDialog.splitByFixWordNum"));
		
		// >显示总字数的面板
		Composite allNumCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(allNumCmp);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(allNumCmp);
		
		Label countLbl = new Label(allNumCmp, SWT.NONE);
		countLbl.setText(Messages.getString("SetSplitPointByWordNumDialog.curFileTotalWordNum"));
		countLbl.setFont(totalLblFont);
		
		Label countValueLbl = new Label(allNumCmp, SWT.NONE);
		countValueLbl.setText(totalWordNum + "");
		countValueLbl.setFont(totalLblFont);

		// >两个面板的父面板
		Composite contentCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(contentCmp);
		contentCmp.setLayout(stack);
		
		// >>平均字数分割方式的面板
		averageContentCmp = new Composite(contentCmp, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(averageContentCmp);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(averageContentCmp);
		
		Label segLbl = new Label(averageContentCmp, SWT.NONE);
		segLbl.setText(Messages.getString("SetSplitPointByWordNumDialog.segNumLbl"));
		
		segSpiner = new Spinner(averageContentCmp, SWT.BORDER);
		segSpiner.setIncrement(1);
		segSpiner.setMinimum(2);
		segSpiner.setMaximum(totalTuNum - 1);
		segSpiner.setSelection(segList.size());
		segSpiner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				String text = segSpiner.getText();
				if (!(text.matches("\\d{1,}") && Integer.parseInt(text) <= (totalTuNum - 1))) {
					segSpiner.setSelection(2);
				}
			}
		});
		
		GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(segSpiner);
		
		// >>按指定字数分割的面板
		wordNumberScroll = new ScrolledComposite(contentCmp, SWT.NONE | SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(wordNumberScroll);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(wordNumberScroll);
		wordNumberScroll.setExpandHorizontal(true);
		wordNumberScroll.setExpandVertical(true);
		
		segParentCmp = new Composite(wordNumberScroll, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(segParentCmp);
		GridLayoutFactory.swtDefaults().spacing(SWT.DEFAULT, 0).numColumns(1).applyTo(segParentCmp);
		wordNumberScroll.setContent(segParentCmp);
		
		averageBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				viewerTopControl();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				viewerTopControl();
			}
		});
		wordNumberBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				viewerTopControl();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				viewerTopControl();
			}
		});
		stack.topControl = averageContentCmp;
		
		//　开始创建片断列表
		for (int i = 0; i < segList.size(); i++) {
			SegPointBean bean = segList.get(i);
			createSegCmp(bean);
		}
		wordNumberScroll.setMinSize(segParentCmp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		segParentCmp.layout();
		wordNumberScroll.layout();

		return tparent;
	}
	
	
	/**
	 * 创建片段
	 * @param bean
	 */
	private void createSegCmp(SegPointBean bean){
		final Composite curSegCmp = new Composite(segParentCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(curSegCmp);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(5).applyTo(curSegCmp);
		
		// 第一列，一个label
		Label segLbl = new Label(curSegCmp, SWT.NONE);
		segLbl.setText(Messages.getString("SetSplitPointByWordNumDialog.segLbl"));
		
		Label segIndexLbl = new Label(curSegCmp, SWT.LEFT);
		segIndexLbl.setText("");
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).hint(20, SWT.DEFAULT).applyTo(segIndexLbl);
		
		final Text wordNumTxt = new Text(curSegCmp, SWT.BORDER);
		GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(wordNumTxt);
		wordNumTxt.setText(bean.getWordNumber() + "");
		wordNumTxt.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				String addText = e.text;
				if (addText.length() > 0) {
					e.doit = addText.matches("\\d+");
				}
			}
		});
		
		Button addBtn = new Button(curSegCmp, SWT.NONE);
		addBtn.setImage(addImg);
		
		Button deleteBtn = new Button(curSegCmp, SWT.NONE);
		deleteBtn.setImage(deleteImg);
		
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int addedWordNum = 0;
				for(Control control : segParentCmp.getChildren()){
					if (control instanceof Composite) {
						Control childControl = ((Composite) control).getChildren()[2];
						if (childControl instanceof Text) {
							addedWordNum += Integer.parseInt(((Text) childControl).getText());
						}
					}
				}
				SegPointBean addBean = new SegPointBean((totalWordNum - addedWordNum) > 0 ? (totalWordNum - addedWordNum) : 0 );
				
				//　开始创建片断列表
				createSegCmp(addBean);
				wordNumberScroll.setMinSize(segParentCmp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				segParentCmp.layout();
				wordNumberScroll.layout();
			}
		});
		
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof Button) {
					Composite parentCmp = ((Button)e.getSource()).getParent();
					parentCmp.dispose();
					wordNumberScroll.setMinSize(segParentCmp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					segParentCmp.layout();
					wordNumberScroll.layout();
					refreshSegListArea();
				}
			}
		});
		
		refreshSegListArea();
	}
	
	/**
	 * 刷新片段列表，针对删除当前片段后，控制每个片段的序列，以及删除按钮是否可用
	 */
	private void refreshSegListArea(){
		// 添加按钮是否可用，取决于片断总数是否大于 tu 总数减 1
		boolean addBntEnable = segParentCmp.getChildren().length < totalTuNum - 1;
		// 一个分割点两个片段。因此片段数必须大于 2
		boolean deleteBtnEnable = segParentCmp.getChildren().length > 2;
		
		Control[] controls = segParentCmp.getChildren();
		for (int i = 0; i < controls.length; i++) {
			Control curControl = controls[i];
			if (curControl instanceof Composite) {
				Control[] childControls = ((Composite) curControl).getChildren();
				Control segIndexControl = childControls[1];
				if (segIndexControl instanceof Label) {
					((Label)segIndexControl).setText((i + 1) + "");
				}

				// 控制添加按钮是否可用
				Control addBtnControl = childControls[3];
				if (addBtnControl instanceof Button) {
					((Button)addBtnControl).setEnabled(addBntEnable);
				}
				
				//　控制删除按钮是否可用
				Control deleteBtnControl = childControls[4];
				if (deleteBtnControl instanceof Button) {
					((Button) deleteBtnControl).setEnabled(deleteBtnEnable);
				}
			}
		}
		
	}
	
	
	/**
	 * 当两个按钮按下时，控制哪个面板显示在最上面
	 */
	private void viewerTopControl(){
		if (averageBtn.getSelection()) {
			stack.topControl = averageContentCmp;
			averageContentCmp.getParent().layout();
		}else if (wordNumberBtn.getSelection()) {
			stack.topControl = wordNumberScroll;
			wordNumberScroll.getParent().layout();
		}
	}
	

	@Override
	protected void okPressed() {
		segList.clear();
		if (averageBtn.getSelection()) {
			// 分配字数时，将多余的字往后分
			int averageWordNum = totalWordNum / segSpiner.getSelection();
			int segWordNum = 0;
			for(Entry<String, Integer> entry : rowWordNumMap.entrySet()){
				if (segWordNum < averageWordNum) {
					segWordNum += entry.getValue();
					if (segWordNum >= averageWordNum) {
						segList.add(new SegPointBean(entry.getKey()));
						segWordNum = 0;
					}
				}
			}
		}else {
			// UNDO 首先验证总字数是否超标，这里以后会改动，比如用户输入以后，强制进行提示
			int addedWordNum = 0;
			int curWordNum = 0;
			List<Integer> segWordNumList = new ArrayList<Integer>();
			for(Control control : segParentCmp.getChildren()){
				if (control instanceof Composite) {
					Control childControl = ((Composite) control).getChildren()[2];
					if (childControl instanceof Text) {
						curWordNum = Integer.parseInt(((Text) childControl).getText());
						if (curWordNum > 0) {
							addedWordNum += curWordNum;
							segWordNumList.add(curWordNum);
						}
					}
				}
			}
			if (addedWordNum > totalWordNum) {
				MessageDialog.openInformation(getShell(),
								Messages.getString("all.dialog.info"),
								Messages.getString("SetSplitPointByWordNumDialog.msg.wordNumError"));
				return;
			}
			
			// 将剩下的字数算到最后一个文本段中
			segWordNumList.add(totalWordNum - addedWordNum);
			
			int i = 0;
			curWordNum = segWordNumList.get(i);
			addedWordNum = 0;
			for (Entry<String, Integer> entry : rowWordNumMap.entrySet()) {
				if (addedWordNum < curWordNum) {
					addedWordNum += entry.getValue();
					if (addedWordNum >= curWordNum) {
						segList.add(new SegPointBean(entry.getKey()));
						i ++;
						curWordNum = segWordNumList.get(i);
						addedWordNum = 0;
					}
				}
			}
		}
		
		for(int i = 0; i < segList.size(); i ++){
			SegPointBean bean = segList.get(i);
			if (bean.getRowId() != null && !bean.getRowId().isEmpty()) {
				// 最后一个文本段不允许添加分割点
				String lastTURowid = xlfHandler.getRowIdByXpath(xlfPath,
						"/xliff/file[last()]/body/descendant::trans-unit[last()]");
				if (lastTURowid.equals(bean.getRowId())) {
					segList.remove(i);
					i --;
				}
			}
		}
		
		if (segList.size() <= 0) {
			MessageDialog.openInformation(getShell(),
					Messages.getString("all.dialog.info"),
					Messages.getString("SetSplitPointHandler.cantSetPoint"));
			return;
		}
		
		super.okPressed();
	}
	

}
