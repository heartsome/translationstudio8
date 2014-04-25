/**
 * XLIFFEditor.java
 *
 * Version information :
 *
 * Date:Jan 27, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.ts.ui.editors;

import java.util.Vector;

import net.heartsome.cat.ts.core.bean.TransUnitBean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 * (此类未使用)
 * @author stone
 * @version
 * @since JDK1.6
 */
public class XLIFFEditor extends EditorPart implements ISelectionProvider, IHSEditor {

	/** 常量，编辑器ID。 */
	public static final String ID = "net.heartsome.cat.ts.ui.editors.XLIFFEditor";

	/** 常量，日志记录器。 */
	private static final Logger LOGGER = LoggerFactory.getLogger(XLIFFEditor.class);

	/** 所有的翻译单元。 */
	private Vector<TransUnitBean> allVector;

	/** 显示和编辑翻译单元容器观察者。 */
//	private CompositeViewer viewer;

	/** 显示在状态栏处的条目。 */
	private StatusLineContributionItem statusItem;

	/** 状态栏管理器。 */
	private IStatusLineManager statusLineManager;

	/** 输入对象的文件名。 */
	private String fileName;

	/** 事件监听器提供者，任务编辑器通知监听器的人物。 */
	ISelectionProvider provider = new SelectionProviderAdapter();

	/** 编辑器左上角的图标 */
	private Image titleImage;

	/**
	 * 启动编辑器。
	 * 
	 * @param site
	 *            the site
	 * @param input
	 *            the input
	 * @throws PartInitException
	 *             the part init exception
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("init(IEditorSite site, IEditorInput input)");
		}
		setSite(site);
		setInput(input);
		// 设置Editor标题栏的显示名称，否则名称用plugin.xml中的name属性
		setPartName(input.getName());

		Image oldTitleImage = titleImage;
		if (input != null) {
			IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor editorDesc = editorRegistry.findEditor(getSite().getId());
			ImageDescriptor imageDesc = editorDesc != null ? editorDesc.getImageDescriptor() : null;
			titleImage = imageDesc != null ? imageDesc.createImage() : null;
		}

		setTitleImage(titleImage);
		if (oldTitleImage != null && !oldTitleImage.isDisposed()) {
			oldTitleImage.dispose();
		}

		getSite().setSelectionProvider(this);
	}

	/**
	 * 创建显示在面板上的控件。
	 * 
	 * @param parent
	 *            the parent
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
//		viewer = new CompositeViewer(parent, SWT.BORDER);
//		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
//		IURIEditorInput input = (IURIEditorInput) getEditorInput();
//		URI path = input.getURI();
//		File file = new File(path);
//		fileName = file.getName();
//		TSFileHandler handler = new TSFileHandler();
//		Map<String, Object> result = handler.openFile(file);
//
//		if (Constant.RETURNVALUE_RESULT_SUCCESSFUL == (Integer) result.get(Constant.RETURNVALUE_RESULT)) {
//
//			Hashtable<String, Vector<TransUnitBean>> ransunits = handler.getTransunits();
//			Set<Entry<String, Vector<TransUnitBean>>> entrySet = ransunits.entrySet();
//
//			// handler.get
//
//			allVector = new Vector<TransUnitBean>();
//			for (Entry<String, Vector<TransUnitBean>> entry : entrySet) {
//				allVector.addAll(entry.getValue());
//			}
//
//			// TODO:
//			// 数据通过解析xliff文件，取xliff文件中File节点的属性source-language和target-language
//			viewer.setTableTitleString(null, "en-AU", "zh-HK", "en-Au -> zh-HK");
//			viewer.setHSContentProvider(new XLIFFContentProvider());
//			viewer.setInput(allVector);
//			
//			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
//				@Override
//				public void selectionChanged(SelectionChangedEvent event) {
//					showMessageOnStatusLine();
//				}
//
//			});
//		} else {
//			//TODO 弹出错误对话框，提示文件解析失败。
//			LOGGER.error("errorMsg", result.get("errorMsg"));
//		}
//		statusItem = new StatusLineContributionItem(ID);
//		// TODO 请使用真实数据
//		statusItem.setText("文件状态。");
//		statusLineManager = getEditorSite().getActionBars().getStatusLineManager();
//		statusLineManager.add(statusItem);
	}

	/**
	 * 在状态栏上显示被编辑文件的信息。
	 */
//	private void showMessageOnStatusLine() {
//		int order = viewer.getSelectionIndex();
//		MessageFormat messageFormat = null;
//		if (order > 0) {
//			/* 一个Xliff文件，可能有多个File节点，这里使用File结点的original属性 */
//			/* 当前文件：{0} | 顺序号:{1} | 可编辑文本段数:{2} | 文本段总数:{3} | 当前用户名" */
//			messageFormat = new MessageFormat("当前文件：{0} | 顺序号：{1} | 可编辑文本段数：{2} | 文本段总数：{3} | 用户名：{4}");
//		} else {
//			messageFormat = new MessageFormat("当前文件：{0} | 可编辑文本段数：{2} | 文本段总数：{3} | 用户名：{4}");
//		}
//		// statusItem.setText(messageFormat.format(new
//		// String[]{String.valueOf(order)}));
//		statusLineManager.setMessage(messageFormat.format(new String[] { fileName, String.valueOf(order),
//				String.valueOf(0), String.valueOf(0), "Stone" }));
//		setSelection(new ISelection() {
//			@Override
//			public boolean isEmpty() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//		});
//	}

	/**
	 * 获得选中的翻译单元，
	 * 
	 * @return 选中的翻译单元或null。
	 * @see net.heartsome.cat.ts.ui.editors.IHSEditor#getSelectTransUnitBean()
	 */
	public TransUnitBean getSelectTransUnitBean() {
//		try {
//			return allVector.get(viewer.getSelectionIndex());
//		} catch (ArrayIndexOutOfBoundsException e) {
//			return null;
//		}
		return null;
	}

	/**
	 * 保存文档。
	 * 
	 * @param monitor
	 *            the monitor
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		// TODO
	}

	/**
	 * 是否允许保存文档。
	 * 
	 * @return true, 允许保存文档返回true。
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		// TODO
		return false;
	}

	/**
	 * 另存为。
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		// TODO
	}

	/**
	 * 文本内容被改变。
	 * 
	 * @return true, if checks if is dirty
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	public boolean isDirty() {
		// TODO
		return false;
	}

	/**
	 * 编辑器获得焦点时调用的方法。
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		getSite().setSelectionProvider(this);
//		showMessageOnStatusLine();
	}

	/**
	 * 添加监听器。
	 * 
	 * @param listener
	 *            the listener
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		provider.addSelectionChangedListener(listener);
	}

	/**
	 * 获得选中对象。
	 * 
	 * @return the selection
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return provider.getSelection();
	}

	/**
	 * 删除监听器。
	 * 
	 * @param listener
	 *            the listener
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		provider.removeSelectionChangedListener(listener);
	}

	/**
	 * 设置选中对象。
	 * 
	 * @param selection
	 *            the selection
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		provider.setSelection(selection);
	}

	/**
	 * 修改选中翻译单元的翻译。
	 * 
	 * @param value
	 *            the value
	 * @see net.heartsome.cat.ts.ui.editors.IHSEditor#changeData(java.lang.String)
	 */
	public void changeData(String value) {
//		viewer.changeData(value);
	}

	/**
	 * 改变源语言和目标语言排列模式。
	 * 
	 * @see net.heartsome.cat.ts.ui.editors.IHSEditor#changeModel()
	 */
	public void changeModel() {
//		viewer.changeModel();
	}

	/**
	 * 释放编辑器，同时释放其他相关资源。
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		if (titleImage != null && !titleImage.isDisposed()) {
			titleImage.dispose();
			titleImage = null;
		}
		getEditorSite().getActionBars().getStatusLineManager().remove(statusItem);
		super.dispose();
	}

	/**
	 * .
	 * 
	 * @author stone
	 * @version
	 * @since JDK1.6
	 */
//	private static final class XLIFFContentProvider implements IHSContentProvider {
//
//		/** 输入的数据对象。 */
//		Vector<TransUnitBean> input;
//
//		/** 界面显示内容的索引。使用Vector获取指定数据太慢。 */
//		String[][] data;
//
//		/** 界面显示内容的索引。使用Vector获取指定数据太慢。 */
//		TransUnitBean[] dataBean;
//
//		/** 默认数据对象，编号、源文本、翻译都是空字符串。 */
//		private static final String[] DEFAULT_DATA = new String[] { "", "", "" };
//
//		/** 翻译单元个数。 */
//		int size = 0;
//
//		/**
//		 * 获得翻译单元个数。
//		 * 
//		 * @return the size
//		 * @see net.heartsome.cat.common.ui.customtable.IHSContentProvider#getSize()
//		 */
//		@Override
//		public int getSize() {
//			return size;
//		}
//
//		/**
//		 * 获得指定索引上翻译单元的值。
//		 * 
//		 * @param index
//		 *            the index
//		 * @return the value
//		 * @see net.heartsome.cat.common.ui.customtable.IHSContentProvider#getValue(int)
//		 */
//		@Override
//		public String[] getValue(int index) {
//			return (index >= size && index < 0) ? DEFAULT_DATA : data[index];
//		}
//
//		/**
//		 * 设置输入对象。
//		 * 
//		 * @param input
//		 *            the input
//		 * @see net.heartsome.cat.common.ui.customtable.IHSContentProvider#setInput(java.lang.Object)
//		 */
//		@SuppressWarnings("unchecked")
//		@Override
//		public void setInput(Object input) {
//			this.input = (Vector<TransUnitBean>) input;
//			size = this.input.size();
//			data = new String[size][3];
//			dataBean = new TransUnitBean[size];
//			for (int i = 0; i < size; i++) {
//				TransUnitBean transUnitBean = this.input.get(i);
//				data[i] = createStringArray("" + (i + 1), transUnitBean.getSrcContent(), transUnitBean.getTgtContent());
//				dataBean[i] = transUnitBean;
//			}
//		}
//
//		/**
//		 * 创建字符串（String）数组，如果传入字符串是null用空串代替。
//		 * 
//		 * @param args
//		 *            the args
//		 * @return the string[]
//		 */
//		private String[] createStringArray(String... args) {
//			for (int i = 0; i < args.length; i++) {
//				if (args[i] == null) {
//					args[i] = "";
//				}
//			}
//			return args;
//		}
//
//		/**
//		 * 修改指定索引的翻译的值。
//		 * 
//		 * @param index
//		 *            修改对象索引
//		 * @param text
//		 *            值
//		 * @see net.heartsome.cat.common.ui.customtable.IHSContentProvider#setValue(int,
//		 *      java.lang.String)
//		 */
//		public void setValue(int index, String text) {
//			if (index >= 0 && index < data.length) {
//				data[index][2] = text;
//				dataBean[index].setTgtContent(text);
//			}
//		}
//
//	}
//
//	/**
//	 * 设置编辑器的源文本和翻译排列的方式。
//	 * 
//	 * @param layoutModel
//	 * @see net.heartsome.cat.ts.ui.editors.IHSEditor#setModel(boolean)
//	 */
 	public void setModel(int layoutModel) {
//		viewer.setModel(layoutModel);
	}

}