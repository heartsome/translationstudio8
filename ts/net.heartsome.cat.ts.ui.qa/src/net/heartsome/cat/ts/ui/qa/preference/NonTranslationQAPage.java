package net.heartsome.cat.ts.ui.qa.preference;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.dialogs.AddOrEditNontransElementDialog;
import net.heartsome.cat.ts.ui.qa.model.NontransElementBean;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.nonTransElement.NonTransElementOperate;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.resource.ImageConstant;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 非译元素的首选项设置
 * @author robert 2011-11-30
 */
public class NonTranslationQAPage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.ts.ui.qa.preference.NonTranslationQAPage";

	private Button addBtn;
	private Button editBtn;
	private Button deleteBtn;

	private TableViewer tableViewer;
	private ComboViewer comboViewer;

	private NonTransElementOperate operate;
	/** 内置非译元素，从QAMolde中获取 */
	private List<NontransElementBean> internalElementList = new ArrayList<NontransElementBean>();
	private List<NontransElementBean> defaultTipList = new ArrayList<NontransElementBean>();
	/** 从非译元素库中取出的非译元素的集合 */
	private List<NontransElementBean> dataList;
	private boolean isInit = false;
	
	public NonTranslationQAPage() {
		setTitle(Messages.getString("qa.preference.NonTranslationQAPage.nonTransElement"));
		initValue();
	}
	
	/**
	 * 初始化相关数据
	 */
	public void initValue() {
		operate = new NonTransElementOperate();
		operate.openNonTransDB();
		internalElementList = QAModel.getInterNonTransElements();
		defaultTipList.add(new NontransElementBean(null, Messages.getString("qa.preference.NonTranslationQAPage.addInterElement"),
				null, null));
		dataList = operate.getNonTransElements();
	}

	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		isInit = true;
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).hint(550, 400).applyTo(tparent);
		
		Group instalGroup = new Group(tparent, SWT.NONE);
		instalGroup.setLayout(new GridLayout());
		instalGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		instalGroup.setText(Messages.getString("qa.preference.NonTranslationQAPage.nonTransElementInstal"));
		
		String tip = Messages.getString("preference.NonTranslationQAPage.instalLbl");
		HsImageLabel instalLbl = new HsImageLabel(tip, 
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_QA_nontrans_nontransInstal));
		Composite instalCmp = instalLbl.createControl(instalGroup);
		instalCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		// 一排按钮
		Composite buttonCmp = new Composite(instalGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(4).applyTo(buttonCmp);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonCmp);

		addBtn = new Button(buttonCmp, SWT.NONE);
		addBtn.setText(Messages.getString("qa.preference.NonTranslationQAPage.add"));
		
		editBtn = new Button(buttonCmp, SWT.NONE);
		editBtn.setText(Messages.getString("qa.preference.NonTranslationQAPage.editBtn"));
		editBtn.setEnabled(false);

		deleteBtn = new Button(buttonCmp, SWT.NONE);
		deleteBtn.setText(Messages.getString("qa.preference.NonTranslationQAPage.delete"));
		deleteBtn.setEnabled(false);

		Point addPoint = addBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point editPoint = editBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point delPoint = deleteBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		GridData btnData = new GridData();
		int width = Math.max(addPoint.x, Math.max(editPoint.x, delPoint.x));
		btnData.widthHint = width + 10;
		addBtn.setLayoutData(btnData);
		editBtn.setLayoutData(btnData);
		deleteBtn.setLayoutData(btnData);
		
		Map<String, String> comboTip = new HashMap<String, String>();
		comboTip.put(QAConstant.QA_NONTRANS_NAME, Messages.getString("qa.preference.NonTranslationQAPage.addInterElement"));

		comboViewer = new ComboViewer(buttonCmp, SWT.NONE);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new NonTransElementCmbProvider());

		comboViewer.getCombo().setToolTipText(Messages.getString("qa.preference.NonTranslationQAPage.addInterElement"));
		comboViewer.setInput(internalElementList);
		comboViewer.getCombo().setText(Messages.getString("qa.preference.NonTranslationQAPage.addInterElement"));
		
		GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).grab(false, false).applyTo(comboViewer.getCombo());

		// ---------------------下面是非译元素展示框---------------------------------
		tableViewer = new TableViewer(instalGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		final Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(tableViewer,ToolTip.NO_RECREATE);
		
		String[] columnNames = new String[] {Messages.getString("qa.preference.NonTranslationQAPage.tipColumn"),
				Messages.getString("qa.preference.NonTranslationQAPage.contentColumn"),
				Messages.getString("qa.preference.NonTranslationQAPage.regularColumn")};
		tableViewer.setLabelProvider(new NonTransElementTableProvider());
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT};
		
		for (int i = 0; i < columnNames.length; i++) {
			TableViewerColumn column = new TableViewerColumn(tableViewer, columnAlignments[i]);
			column.getColumn().setText(columnNames[i]);
			column.getColumn().setWidth(50);
			column.setLabelProvider(new NonTransElementTableProvider(i));
			if (i == 0) {
				column.getColumn().addSelectionListener(new SelectionAdapter() {
					boolean asc = true;
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? TableSorter.name_ASC : TableSorter.name_DESC);
						asc = !asc;
					}
				});
			}
			
			if (i == 1) {
				column.getColumn().addSelectionListener(new SelectionAdapter() {
					boolean asc = true;
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? TableSorter.content_ASC : TableSorter.content_DESC);
						asc = !asc;
					}
				});
			}
		}
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer.setInput(dataList);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] {0.2, 0.2, 0.58};
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		instalLbl.computeSize();
		initListener();
		
		return parent;
	}


	/**
	 * 一些事件的添加
	 */
	public void initListener() {

		// 非译元素的添加事件
		addBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				addNonTransElement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				addNonTransElement();
			}
		});
		
		editBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				editNontransElement();
			}
			public void widgetDefaultSelected(SelectionEvent arg0) {
				editNontransElement();
			}
		});

		// 删除按钮的点击操作
		deleteBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				deleteElement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				deleteElement();
			}
		});

		// 给comboViewer添加事件，同时传入内置元素
		comboViewer.getCombo().addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				addInternalElement();
				comboViewer.getCombo().setText(Messages.getString("qa.preference.NonTranslationQAPage.addInterElement"));
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				addInternalElement();
				comboViewer.getCombo().setText(Messages.getString("qa.preference.NonTranslationQAPage.addInterElement"));
			}
		});

		// 非译元素列表的点击事件
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = tableViewer.getSelection();
				if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					if (structuredSelection.getFirstElement() instanceof NontransElementBean) {
						if (structuredSelection.size() == 1 && 
								!validIsInternalElementNonTip((NontransElementBean)structuredSelection.getFirstElement())) {
							editBtn.setEnabled(true);
						}else {
							editBtn.setEnabled(false);
						}
						deleteBtn.setEnabled(true);
					}else {
						setAddModel();
					}
				}else {
					setAddModel();
				}
			}
		});
		
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent arg0) {
				editNontransElement();
			}
		});
	}
	
	/**
	 * 设置添加模式
	 */
	private void setAddModel(){
		editBtn.setEnabled(false);
		deleteBtn.setEnabled(false);
	}
	
	/**
	 * 验证是否是内置非译元素
	 * @return
	 */
	private boolean validIsInternalElementNonTip(NontransElementBean curElement){
		String selectedElementId = curElement.getId();
		if ("qaInternalNonTrans_ip".equals(selectedElementId) || "qaInternalNonTrans_email".equals(selectedElementId)
				|| "qaInternalNonTrans_web".equals(selectedElementId)) {
			return true;
		}
		return false;
	}


	/**
	 * 添加非译元素
	 */
	public void addNonTransElement() {
		NontransElementBean bean = new NontransElementBean();
		AddOrEditNontransElementDialog dialog = new AddOrEditNontransElementDialog(getShell(), true, tableViewer, bean);
		int result = dialog.open();
		if (result == IDialogConstants.OK_ID) {
			// 先添加到listViewer中
			dataList.add(bean);
			tableViewer.refresh();
			tableViewer.setSelection(new StructuredSelection(bean));
		}
	}
	
	/**
	 * 编辑非译元素
	 */
	private void editNontransElement(){
		NontransElementBean bean = null;
		ISelection selection = tableViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.getFirstElement() instanceof NontransElementBean) {
				bean = (NontransElementBean) structuredSelection.getFirstElement();
				if (validIsInternalElementNonTip(bean)) {
					return;
				}
				AddOrEditNontransElementDialog dialog = new AddOrEditNontransElementDialog(getShell(), false, tableViewer, bean);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					tableViewer.refresh();
				}
			}
		}
	}

	/**
	 * 删除列表中的非译元素
	 */
	@SuppressWarnings("unchecked")
	public void deleteElement() {
		ISelection selection = tableViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			boolean confirm = MessageDialog.openConfirm(getShell(), Messages.getString("qa.preference.NonTranslationQAPage.enter"),
					Messages.getString("qa.preference.NonTranslationQAPage.enterDelete"));
			if (!confirm) {
				return;
			}
			
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Iterator<NontransElementBean> iter = structuredSelection.iterator();
			while (iter.hasNext()) {
				NontransElementBean selectElement = iter.next();
				dataList.remove(selectElement);
			}
			tableViewer.refresh();
		}
	}

	/**
	 * 如果点击默认，那么重新从非译元素的库中取值，再删除之前所添加或删除的非译元素
	 */
	@Override
	protected void performDefaults() {
		dataList = QAModel.getInterNonTransElements();
		tableViewer.setInput(dataList);
		
		setAddModel();
	}

	@Override
	public boolean performOk() {
		if (!isInit) {
			return true;
		}
		// 遍历列表，获取出要添加的数据
		List<NontransElementBean> addElementList = new ArrayList<NontransElementBean>();
		NontransElementBean listViewerBean;
		int listViewerNum = tableViewer.getTable().getItemCount();
		for (int i = 0; i < listViewerNum; i++) {
			if (tableViewer.getElementAt(i) instanceof NontransElementBean) {
				listViewerBean = (NontransElementBean)tableViewer.getElementAt(i);
				addElementList.add(listViewerBean);
			}
		}

		// 删除所有非译元素
		operate.deleteAllElement();
		
		// 再进行相关操作，先添加
		if (addElementList.size() > 0) {
			operate.addNonTransElement(addElementList);
		}
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		int oldValue = store.getInt(QAConstant.QA_PREF_nonTrans_changeTag);
		store.setValue(QAConstant.QA_PREF_nonTrans_changeTag, ++oldValue);
		return true;
	}

	/**
	 * 给comboViewer初始化内置元素
	 */
	public void initInternalValue() {
		comboViewer.getCombo().removeAll();
		comboViewer.setInput(internalElementList);
		comboViewer.refresh();
	}

	/**
	 * 添加内置非译元素
	 */
	public void addInternalElement() {
		ISelection selection = comboViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			NontransElementBean interBean = (NontransElementBean) structuredSelection.getFirstElement();
			if (interBean.getId() == null) {
				return;
			}
			
			int eleSum = tableViewer.getTable().getItemCount();
			for (int i = 0; i < eleSum; i++) {
				NontransElementBean curBean = new NontransElementBean(); 
				if (tableViewer.getElementAt(i) instanceof NontransElementBean) {
					curBean = (NontransElementBean) tableViewer.getElementAt(i);
					
					if (curBean.getId().equals(interBean.getId())) {
						MessageDialog.openWarning(getShell(), Messages.getString("qa.all.dialog.warning"), MessageFormat
								.format(Messages.getString("qa.preference.NonTranslationQAPage.tip5"),
										interBean.getName()));
						return;
					}
				}
			}
			dataList.add(interBean);
			tableViewer.refresh();
		}
	}

	/**
	 * tableViewer的标签提供器
	 * @author robert
	 */
	class NonTransElementTableProvider extends CellLabelProvider implements ITableLabelProvider {
		private int index;
		public NonTransElementTableProvider(){}
		
		public NonTransElementTableProvider(int index){
			this.index = index;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof NontransElementBean) {
				NontransElementBean bean = (NontransElementBean)element;
				switch (columnIndex) {
				case 0:
					return bean.getName();
				case 1:
					return isNull(bean.getContent()) ? "" : bean.getContent();
				case 2:
					return isNull(bean.getRegular()) ? "" : bean.getRegular();
				default:
					return null;
				}
			}
			return null;
		}
		@Override
		public void update(ViewerCell cell) {
			cell.setText(getColumnText(cell.getElement(), index));
		}
		
		@Override
		public String getToolTipText(Object element) {
			if (element instanceof NontransElementBean) {
				NontransElementBean bean = (NontransElementBean)element;
				switch (index) {
				case 0:
					return bean.getName();
				case 1:
					return isNull(bean.getContent()) ? "" : bean.getContent();
				case 2:
					return isNull(bean.getRegular()) ? "" : bean.getRegular();
				default:
					return null;
				}
			}
			return null;
		}

		@Override
		public Point getToolTipShift(Object object) {
			return new Point(5, 5);
		}
	}
	
	/**
	 * 非译元素列表的标签提供类
	 * @author robert 2011-11-30
	 */
	class NonTransElementCmbProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof NontransElementBean) {
				return ((NontransElementBean)element).getName();
			}
			return "";
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	
	
	/**
	 * 非译元素的所显示的列中的排序类
	 * @version
	 * @since JDK1.6
	 */
	static class TableSorter extends ViewerSorter {
		private static final int name_ID = 1;	//非译元素名称
		private static final int content_ID = 2; //非译元素内容

		public static final TableSorter name_ASC = new TableSorter(name_ID);
		public static final TableSorter name_DESC = new TableSorter(-name_ID);

		public static final TableSorter content_ASC = new TableSorter(content_ID);
		public static final TableSorter content_DESC = new TableSorter(-content_ID);


		private int sortType;

		private TableSorter(int sortType) {
			this.sortType = sortType;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			NontransElementBean bean1 = (NontransElementBean) e1;
			NontransElementBean bean2 = (NontransElementBean) e2;
			switch (sortType) {
				case name_ID: {
					String name1 = bean1.getName();
					String name2 = bean2.getName();
					return name1.compareToIgnoreCase(name2);
				}
				case -name_ID: {
					String name1 = bean1.getName();
					String name2 = bean2.getName();
					return name2.compareToIgnoreCase(name1);
				}
				case content_ID: {
					String content1 = bean1.getContent();
					String content2 = bean2.getContent();
					return content1.compareToIgnoreCase(content2);
				}
				case -content_ID: {
					String content1 = bean1.getContent();
					String content2 = bean2.getContent();
					return content2.compareToIgnoreCase(content1);
				}
			}
			return 0;
		}
	}
	
	/**
	 * 判断是否为空
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str){
		if (str == null || "".equals(str)) {
			return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		String regular = "(?i)this is a regular";
		if (regular.contains("\\b")) {
			System.out.println("------------");
		}
	}

}
