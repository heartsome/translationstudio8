package net.heartsome.cat.ts.ui.advanced.dialogs;

import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.ts.ui.advanced.handlers.ADXmlHandler;
import net.heartsome.cat.ts.ui.advanced.model.ElementBean;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XMLl转换器管理(这是添加，和修改，和分析XML样式文件的父类)
 * @author robert 2012-02-27
 * @version
 * @since JDK1.6
 */
public class XmlConvertManagerDialog extends Dialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlConvertManagerDialog.class);
	
	protected TableViewer tableViewer;
	protected Table table;
	protected List<ElementBean> elementsList = new LinkedList<ElementBean>();
	protected ADXmlHandler handler = new ADXmlHandler();
	/** 当前生成的XML转换文件 */
	protected String curConvertXml;
	protected Button addBtn;
	protected Button editBtn;
	protected Button deleteBtn;
	protected IWorkspaceRoot root;
	protected Text rootTxt;
	protected Label rootTipLbl;

	protected XmlConvertManagerDialog(Shell parentShell) {
		super(parentShell);
	}

	protected void createTable(Composite tparent) {
		tableViewer = new TableViewer(tparent, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI
				| SWT.BORDER);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		createTableColumns();

		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.2, 0.2, 0.2, 0.20, 0.14 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		tableViewer.setLabelProvider(new TViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editElement();
			}
		});
	}

	/**
	 * 创建列表的头并且排序 ;
	 */
	private void createTableColumns() {
		String[] columnNames = new String[] { Messages.getString("dialogs.XmlConvertManagerDialog.columnNames1"),
				Messages.getString("dialogs.XmlConvertManagerDialog.columnNames2"),
				Messages.getString("dialogs.XmlConvertManagerDialog.columnNames3"),
				Messages.getString("dialogs.XmlConvertManagerDialog.columnNames4"),
				Messages.getString("dialogs.XmlConvertManagerDialog.columnNames5") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);

			// 处理排序的问题
			switch (i) {
			case 0:
				tableColumn.addSelectionListener(new SelectionAdapter() {
					boolean asc = true;

					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? ElementsOrder.name_ASC : ElementsOrder.name_DESC);
						asc = !asc;
					}
				});
				break;
			case 1:
				tableColumn.addSelectionListener(new SelectionAdapter() {
					boolean asc = true;

					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? ElementsOrder.type_ASC : ElementsOrder.type_DESC);
						asc = !asc;
					}
				});
				break;
			case 2:
				tableColumn.addSelectionListener(new SelectionAdapter() {
					boolean asc = true;

					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? ElementsOrder.inlineType_ASC : ElementsOrder.inlineType_DESC);
						asc = !asc;
					}
				});
				break;
			case 3:
				tableColumn.addSelectionListener(new SelectionAdapter() {
					boolean asc = true;

					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? ElementsOrder.attributes_ASC : ElementsOrder.attributes_DESC);
						asc = !asc;
					}
				});
				break;
			case 4:
				tableColumn.addSelectionListener(new SelectionAdapter() {
					boolean asc = true;

					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? ElementsOrder.remainSpace_ASC : ElementsOrder.remainSpace_DESC);
						asc = !asc;
					}
				});
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 刷新列表，并且定位
	 * @param bean
	 *            ;
	 */
	protected void refreshTable(ElementBean bean) {
		tableViewer.setInput(elementsList);
		if (bean != null) {
			tableViewer.setSelection(new StructuredSelection(bean));
		}
	}

	/**
	 * 给增删改三个按钮添加点击事件 ;
	 */
	protected void initListener() {
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddOrEditElementOfXmlConvertDialog dialog = new AddOrEditElementOfXmlConvertDialog(getShell(), true,
						elementsList);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshTable(dialog.getCurrentElement());
				}
			}
		});

		editBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editElement();
			}
		});

		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
					if (MessageDialog.openConfirm(getShell(),
							Messages.getString("dialogs.XmlConvertManagerDialog.msgTitle1"),
							Messages.getString("dialogs.XmlConvertManagerDialog.msg1"))) {
						IStructuredSelection structuredSelection = (IStructuredSelection) selection;
						@SuppressWarnings("unchecked")
						Iterator<ElementBean> iter = structuredSelection.iterator();
						ElementBean bean = new ElementBean();
						while (iter.hasNext()) {
							bean = iter.next();
							elementsList.remove(bean);
						}

						refreshTable(null);
					}
				} else {
					MessageDialog.openInformation(getShell(),
							Messages.getString("dialogs.XmlConvertManagerDialog.msgTitle2"),
							Messages.getString("dialogs.XmlConvertManagerDialog.msg2"));
				}
			}
		});
	}

	/**
	 * 创建新的配置文件
	 * @param configXMLLocation
	 *            ;
	 */
	protected void createConfigXML(String configXMLLocation) {
		// 先创建一个空文本
		try {
			FileOutputStream outPut = new FileOutputStream(configXMLLocation);
			StringBuffer configDataSB = new StringBuffer();
			configDataSB.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			configDataSB
					.append("<!DOCTYPE ini-file PUBLIC \"-//HEARTSOME//Converters 2.0.0//EN\" \"configuration.dtd\">\n");
			configDataSB.append("<ini-file>\n");

			ElementBean bean;
			for (int i = 0; i < elementsList.size(); i++) {
				configDataSB.append("\t<tag ");
				bean = elementsList.get(i);
				// 添加元素类型
				configDataSB.append(MessageFormat.format("hard-break=\"{0}\" ", bean.getType()));

				// 添加可翻译属性
				String attributes = bean.getTransAttribute();
				if (!"".equals(attributes) && attributes != null) {
					configDataSB.append(MessageFormat.format("attributes=\"{0}\" ", attributes));
				}

				// 添加内联类型
				String inlineType = bean.getInlineType();
				if (!"".equals(inlineType) && inlineType != null) {
					configDataSB.append(MessageFormat.format("ctype=\"{0}\" ", inlineType));
				}

				// 添加可保留空格属性
				String remainSpace = bean.getRemainSpace();
				if (!"".equals(remainSpace) && remainSpace != null) {
					configDataSB.append(MessageFormat.format("keep-format=\"{0}\" ", remainSpace));
				}
				// 添加元素名
				configDataSB.append(MessageFormat.format(">{0}</tag>\n", bean.getName()));

			}
			configDataSB.append("</ini-file>");

			outPut.write(configDataSB.toString().getBytes("UTF-8"));
			outPut.close();
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	public String getCurentConverXML() {
		return curConvertXml;
	}

	/**
	 * 编辑选中的元素 ;
	 */
	protected void editElement() {
		ISelection selection = tableViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator<ElementBean> iter = structuredSelection.iterator();
			ElementBean bean = iter.next();

			AddOrEditElementOfXmlConvertDialog dialog = new AddOrEditElementOfXmlConvertDialog(getShell(), false,
					elementsList);
			dialog.create();
			dialog.setInitEditData(bean);
			int result = dialog.open();
			if (result == IDialogConstants.OK_ID) {
				refreshTable(dialog.getCurrentElement());
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("dialogs.XmlConvertManagerDialog.msgTitle2"),
					Messages.getString("dialogs.XmlConvertManagerDialog.msg3"));
		}
	}

	/**
	 * XML配置文件列表的标签提供器
	 * @author robert 2012-02-24
	 * @version
	 * @since JDK1.6
	 */
	private class TViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof ElementBean) {
				ElementBean bean = (ElementBean) element;
				switch (columnIndex) {
				case 0:
					return bean.getName();
				case 1:
					return bean.getType();
				case 2:
					return bean.getInlineType();
				case 3:
					return bean.getTransAttribute();
				case 4:
					return bean.getRemainSpace();
				default:
					return null;
				}
			}
			return null;
		}
	}

	/**
	 * XML转换器列表排序类
	 * @author robert
	 * @version
	 * @since JDK1.6
	 */
	static class ElementsOrder extends ViewerSorter {
		private static final int name_ID = 1; // 第一列，元素名
		private static final int type_ID = 2; // 第二列，元素类型
		private static final int inlineType_ID = 3; // 第三列，内联类型
		private static final int attributes_ID = 4; // 第四列，可翻译属性
		private static final int remainSpace_ID = 5; // 第五行，保留空格

		public static final ElementsOrder name_ASC = new ElementsOrder(name_ID);
		public static final ElementsOrder name_DESC = new ElementsOrder(-name_ID);

		public static final ElementsOrder type_ASC = new ElementsOrder(type_ID);
		public static final ElementsOrder type_DESC = new ElementsOrder(-type_ID);

		public static final ElementsOrder inlineType_ASC = new ElementsOrder(inlineType_ID);
		public static final ElementsOrder inlineType_DESC = new ElementsOrder(-inlineType_ID);

		public static final ElementsOrder attributes_ASC = new ElementsOrder(attributes_ID);
		public static final ElementsOrder attributes_DESC = new ElementsOrder(-attributes_ID);

		public static final ElementsOrder remainSpace_ASC = new ElementsOrder(remainSpace_ID);
		public static final ElementsOrder remainSpace_DESC = new ElementsOrder(-remainSpace_ID);

		private int sortType;

		private ElementsOrder(int sortType) {
			this.sortType = sortType;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			ElementBean bean1 = (ElementBean) e1;
			ElementBean bean2 = (ElementBean) e2;
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

			case type_ID: {
				String type1 = bean1.getType();
				String type2 = bean2.getType();
				return type1.compareToIgnoreCase(type2);
			}
			case -type_ID: {
				String type1 = bean1.getType();
				String type2 = bean2.getType();
				return type2.compareToIgnoreCase(type1);
			}

			case inlineType_ID: {
				String inlineType1 = bean1.getInlineType();
				String inlineType2 = bean2.getInlineType();
				return inlineType1.compareToIgnoreCase(inlineType2);
			}
			case -inlineType_ID: {
				String inlineType1 = bean1.getInlineType();
				String inlineType2 = bean2.getInlineType();
				return inlineType2.compareToIgnoreCase(inlineType1);
			}

			case attributes_ID: {
				String attributes1 = bean1.getTransAttribute();
				String attributes2 = bean2.getTransAttribute();
				return attributes1.compareToIgnoreCase(attributes2);
			}
			case -attributes_ID: {
				String attributes1 = bean1.getTransAttribute();
				String attributes2 = bean2.getTransAttribute();
				return attributes2.compareToIgnoreCase(attributes1);
			}

			case remainSpace_ID: {
				String remainSpace1 = bean1.getRemainSpace();
				String remainSpace2 = bean2.getRemainSpace();
				return remainSpace1.compareToIgnoreCase(remainSpace2);
			}
			case -remainSpace_ID: {
				String remainSpace1 = bean1.getRemainSpace();
				String remainSpace2 = bean2.getRemainSpace();
				return remainSpace2.compareToIgnoreCase(remainSpace1);
			}
			}
			return 0;
		}
	}
}
