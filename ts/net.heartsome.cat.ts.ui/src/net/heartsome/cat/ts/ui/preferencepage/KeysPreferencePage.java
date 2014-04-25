package net.heartsome.cat.ts.ui.preferencepage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.resource.ImageConstant;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.keys.NewKeysPreferenceMessages;
import org.eclipse.ui.internal.keys.model.BindingElement;
import org.eclipse.ui.internal.keys.model.BindingModel;
import org.eclipse.ui.internal.keys.model.CommonModel;
import org.eclipse.ui.internal.keys.model.ContextModel;
import org.eclipse.ui.internal.keys.model.ModelElement;
import org.eclipse.ui.internal.keys.model.SchemeModel;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;

/**
 * 快捷键首选项页面，该类与 org.eclipse.ui.internal.keys.NewKeysPreferencePage 类似，只是在原来的基础上修改了界面。
 * @author peason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings({ "restriction", "unchecked", "rawtypes", "unused" })
public class KeysPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

	private static final String TRACING_COMPONENT = "NewKeysPref";

	public final static String TAG_DIALOG_SECTION = "org.eclipse.ui.preferences.keysPreferencePage";

	private static final String TAG_FILTER_ACTION_SETS = "actionSetFilter"; //$NON-NLS-1$

	private static final String TAG_FILTER_INTERNAL = "internalFilter"; //$NON-NLS-1$

	private static final String TAG_FILTER_UNCAT = "uncategorizedFilter"; //$NON-NLS-1$

	private boolean fFilterActionSetContexts = true;

	private boolean fFilterInternalContexts = true;

	private CategoryPatternFilter fPatternFilter;

	private CategoryFilterTree fFilteredTree;

	private Category fDefaultCategory;

	/**
	 * The number of items to show in the bindings table tree.
	 */
	private static final int ITEMS_TO_SHOW = 7;

	private static final int COMMAND_NAME_COLUMN = 0;
	private static final int KEY_SEQUENCE_COLUMN = 1;
	private static final int CATEGORY_COLUMN = 2;
	private static int NUM_OF_COLUMNS = CATEGORY_COLUMN + 1;

	private KeyController2 keyController;

	private ICommandImageService commandImageService;

	private ICommandService commandService;

	private IBindingService fBindingService;

	private KeySequenceText fKeySequenceText;

	private TreeViewer viewer;

	protected class CategoryFilterTree extends FilteredTree {

		private CategoryPatternFilter filter;

		/**
		 * Constructor for PatternFilteredTree.
		 * @param parent
		 * @param treeStyle
		 * @param filter
		 */
		protected CategoryFilterTree(Composite parent, int treeStyle, CategoryPatternFilter filter) {
			super(parent, treeStyle, filter, true);
			this.filter = filter;
		}

		public void filterCategories(boolean b) {
			filter.filterCategories(b);
			textChanged();
		}

		public boolean isFilteringCategories() {
			return filter.isFilteringCategories();
		}
	}

	private final class BindingModelComparator extends ViewerComparator {
		private LinkedList sortColumns = new LinkedList();
		private boolean ascending = true;

		public BindingModelComparator() {
			for (int i = 0; i < NUM_OF_COLUMNS; i++) {
				sortColumns.add(new Integer(i));
			}
		}

		public int getSortColumn() {
			return ((Integer) sortColumns.getFirst()).intValue();
		}

		public void setSortColumn(int column) {
			if (column == getSortColumn()) {
				return;
			}
			Integer sortColumn = new Integer(column);
			sortColumns.remove(sortColumn);
			sortColumns.addFirst(sortColumn);
		}

		/**
		 * @return Returns the ascending.
		 */
		public boolean isAscending() {
			return ascending;
		}

		/**
		 * @param ascending
		 *            The ascending to set.
		 */
		public void setAscending(boolean ascending) {
			this.ascending = ascending;
		}

		public final int compare(final Viewer viewer, final Object a, final Object b) {
			int result = 0;
			Iterator i = sortColumns.iterator();
			while (i.hasNext() && result == 0) {
				int column = ((Integer) i.next()).intValue();
				result = compareColumn(viewer, a, b, column);
			}
			return ascending ? result : (-1) * result;
		}

		private int compareColumn(final Viewer viewer, final Object a, final Object b, final int columnNumber) {
			// if (columnNumber == CATEGORY_COLUMN) {
			// return sortUser(a, b);
			// }
			IBaseLabelProvider baseLabel = ((TreeViewer) viewer).getLabelProvider();
			if (baseLabel instanceof ITableLabelProvider) {
				ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
				String e1p = tableProvider.getColumnText(a, columnNumber);
				String e2p = tableProvider.getColumnText(b, columnNumber);
				if (e1p != null && e2p != null) {
					return getComparator().compare(e1p, e2p);
				}
			}
			return 0;
		}

		private int sortUser(final Object a, final Object b) {
			int typeA = ((BindingElement) a).getUserDelta().intValue();
			int typeB = ((BindingElement) b).getUserDelta().intValue();
			int result = typeA - typeB;
			return result;
		}

	}

	private final class ResortColumn extends SelectionAdapter {
		private final BindingModelComparator comparator;
		private final TreeColumn treeColumn;
		private final TreeViewer viewer;
		private final int column;

		private ResortColumn(BindingModelComparator comparator, TreeColumn treeColumn, TreeViewer viewer, int column) {
			this.comparator = comparator;
			this.treeColumn = treeColumn;
			this.viewer = viewer;
			this.column = column;
		}

		public void widgetSelected(SelectionEvent e) {
			if (comparator.getSortColumn() == column) {
				comparator.setAscending(!comparator.isAscending());
				viewer.getTree().setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
			} else {
				viewer.getTree().setSortColumn(treeColumn);
				comparator.setSortColumn(column);
			}
			try {
				viewer.getTree().setRedraw(false);
				viewer.refresh();
				changeBackground();
			} finally {
				viewer.getTree().setRedraw(true);
			}
		}
	}

	private void changeBackground() {
		for (TreeItem item : viewer.getTree().getItems()) {
			BindingElement element = (BindingElement) item.getData();
			if (element.getConflict()) {
				item.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			} else {
				item.setBackground(null);
			}
		}
	}
	
	private List<String> lstRemove = Constants.lstRemove;
	
	class ModelContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof BindingModel) {
				HashSet<BindingElement> set = ((BindingModel) parentElement).getBindings();
				Iterator<BindingElement> iterator = set.iterator();
				while (iterator.hasNext()) {
					BindingElement bindingElement = iterator.next();
					if (lstRemove.contains(bindingElement.getId())){
//						bindingElement.setTrigger(null);
						
						iterator.remove();
					}
					
				}
				return set.toArray();
			}
			if (parentElement instanceof ContextModel) {
				return ((ContextModel) parentElement).getContexts().toArray();
			}
			if (parentElement instanceof SchemeModel) {
				return ((SchemeModel) parentElement).getSchemes().toArray();
			}
			if (parentElement instanceof BindingElement) {
				BindingElement bindingElement = (BindingElement) parentElement;
				if (lstRemove.contains(bindingElement.getId())){
//					bindingElement.setTrigger(null);
					return new Object[0];
				} else {
					return new BindingElement[]{bindingElement};
				}
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return ((ModelElement) element).getParent();
		}

		public boolean hasChildren(Object element) {
			return (element instanceof BindingModel) || (element instanceof ContextModel)
					|| (element instanceof SchemeModel);
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class BindingElementLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		 * A resource manager for this preference page.
		 */
		private final LocalResourceManager localResourceManager = new LocalResourceManager(
				JFaceResources.getResources());

		public final void dispose() {
			super.dispose();
			localResourceManager.dispose();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			String rc = getColumnText(element, 0);
			if (rc == null) {
				super.getText(element);
			}
			StringBuffer buf = new StringBuffer(rc);
			for (int i = 1; i < CATEGORY_COLUMN; i++) {
				String text = getColumnText(element, i);
				if (text != null) {
					buf.append(' ');
					buf.append(text);
				}
			}
			return buf.toString();
		}

		public String getColumnText(Object element, int index) {
			BindingElement bindingElement = ((BindingElement) element);
			switch (index) {
			case COMMAND_NAME_COLUMN: {// name
				String name = bindingElement.getName();
				if (name != null && name.endsWith("()")) {
					name = name.substring(0, name.length() - 2);
				}
				return name;
			}
			case KEY_SEQUENCE_COLUMN: // keys
				TriggerSequence seq = bindingElement.getTrigger();
				return seq == null ? Util.ZERO_LENGTH_STRING : seq.format();
			case CATEGORY_COLUMN: // category
				String id = bindingElement.getId();
				if (id.equalsIgnoreCase("net.heartsome.cat.ts.command.preference")) {
					return Messages.getString("preferencepage.KeysPreferencePage.toolCategory");
				} else if (id.equalsIgnoreCase("org.eclipse.ui.window.lockToolBar")) {
					return Messages.getString("preferencepage.KeysPreferencePage.toolbarCategory");
				} else if (id.equalsIgnoreCase("org.eclipse.ui.window.showKeyAssist")) {
					return Messages.getString("preferencepage.KeysPreferencePage.helpCategory");
				}
				return bindingElement.getCategory();
			}
			return null;
		}

		public Image getColumnImage(Object element, int index) {
			BindingElement be = (BindingElement) element;
			switch (index) {
			case COMMAND_NAME_COLUMN:
				final String commandId = be.getId();
				final ImageDescriptor imageDescriptor = commandImageService.getImageDescriptor(commandId);
				if (imageDescriptor == null) {
					return null;
				}
				try {
					return localResourceManager.createImage(imageDescriptor);
				} catch (final DeviceResourceException e) {
					final String message = "Problem retrieving image for a command '" //$NON-NLS-1$
							+ commandId + '\'';
					final IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
					WorkbenchPlugin.log(message, status);
				}
				return null;
			}

			return null;
		}
	}

	private BindingModel model;
	
	public void init(IWorkbench workbench) {
		keyController = new KeyController2();
		keyController.init(workbench, lstRemove);
		model = keyController.getBindingModel();
		
		commandService = (ICommandService) workbench.getService(ICommandService.class);
		Collection definedCommandIds = commandService.getDefinedCommandIds();
		
		fDefaultCategory = commandService.getCategory(null);
		fBindingService = (IBindingService) workbench.getService(IBindingService.class);

		commandImageService = (ICommandImageService) workbench.getService(ICommandImageService.class);
	}

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.KEYS_PREFERENCE_PAGE);
		final Composite page = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		page.setLayout(layout);

		Group groupParent = new Group(page, SWT.None);
		groupParent.setLayout(new GridLayout());
		groupParent.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupParent.setText(Messages.getString("preferencepage.KeysPreferencePage.groupParent"));

		HsImageLabel imageLabel = new HsImageLabel(Messages.getString("preferencepage.KeysPreferencePage.imageLabel"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_SYS_KEY));
		Composite cmp = imageLabel.createControl(groupParent);
		cmp.setLayout(new GridLayout());
		Composite cmpTemp = (Composite) imageLabel.getControl();
		cmpTemp.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite cmpContent = new Composite(cmpTemp, SWT.None);
		cmpContent.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		cmpContent.setLayoutData(data);

		// 不显示过滤文本框
		PlatformUI.getPreferenceStore().setDefault(IWorkbenchPreferenceConstants.SHOW_FILTERED_TEXTS, false);

		IDialogSettings settings = getDialogSettings();

		fPatternFilter = new CategoryPatternFilter(true, commandService.getCategory(null));
		if (settings.get(TAG_FILTER_UNCAT) != null) {
			fPatternFilter.filterCategories(settings.getBoolean(TAG_FILTER_UNCAT));
		}

		createTree(cmpContent);

		fill();

		applyDialogFont(cmpContent);
		imageLabel.computeSize();
		return page;
	}
	
	private void fill() {
		// Apply context filters
		keyController.filterContexts(true, true, true);

		fFilteredTree.filterCategories(fPatternFilter.isFilteringCategories());
		
		fFilteredTree.getViewer().setInput(model);
		changeBackground();
	}

	private void createTree(Composite parent) {
		fPatternFilter = new CategoryPatternFilter(true, fDefaultCategory);
		fPatternFilter.filterCategories(true);

		GridData gridData;

		fFilteredTree = new CategoryFilterTree(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION, fPatternFilter);
		final GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		fFilteredTree.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		fFilteredTree.setLayoutData(gridData);

		viewer = fFilteredTree.getViewer();
		// Make sure the filtered tree has a height of ITEMS_TO_SHOW
		final Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		final Object layoutData = tree.getLayoutData();
		if (layoutData instanceof GridData) {
			gridData = (GridData) layoutData;
			final int itemHeight = tree.getItemHeight();
			if (itemHeight > 1) {
				gridData.heightHint = ITEMS_TO_SHOW * itemHeight;
			}
		}

		BindingModelComparator comparator = new BindingModelComparator();
		comparator.setSortColumn(2);
		viewer.setComparator(comparator);

		final TreeColumn commandNameColumn = new TreeColumn(tree, SWT.LEFT, COMMAND_NAME_COLUMN);
		commandNameColumn.setText(Messages.getString("preferencepage.KeysPreferencePage.commandNameColumn"));
		tree.setSortColumn(commandNameColumn);
		tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
		commandNameColumn.addSelectionListener(new ResortColumn(comparator, commandNameColumn, viewer,
				COMMAND_NAME_COLUMN));

		final TreeViewerColumn triggerSequenceColumn = new TreeViewerColumn(viewer, SWT.LEFT, KEY_SEQUENCE_COLUMN);
		triggerSequenceColumn.getColumn().setText(
				Messages.getString("preferencepage.KeysPreferencePage.triggerSequenceColumn"));
		triggerSequenceColumn.getColumn().addSelectionListener(
				new ResortColumn(comparator, triggerSequenceColumn.getColumn(), viewer, KEY_SEQUENCE_COLUMN));
		triggerSequenceColumn.setEditingSupport(new TableViewerEditingSupport(viewer, 1));

		final TreeColumn categoryColumn = new TreeColumn(tree, SWT.LEFT, CATEGORY_COLUMN);
		categoryColumn.setText(Messages.getString("preferencepage.KeysPreferencePage.categoryColumn"));
		categoryColumn.addSelectionListener(new ResortColumn(comparator, categoryColumn, viewer, CATEGORY_COLUMN));

		viewer.setContentProvider(new ModelContentProvider());
		viewer.setLabelProvider(new BindingElementLabelProvider());

		fFilteredTree.getPatternFilter().setIncludeLeadingWildcard(true);
		final TreeColumn[] columns = viewer.getTree().getColumns();

		columns[COMMAND_NAME_COLUMN].setWidth(240);
		columns[KEY_SEQUENCE_COLUMN].setWidth(130);
		columns[CATEGORY_COLUMN].setWidth(130);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			// When the viewer changes selection, update the model's current
			// selection
			public void selectionChanged(SelectionChangedEvent event) {
				changeBackground();
				ModelElement binding = (ModelElement) ((IStructuredSelection) event.getSelection()).getFirstElement();
				model.setSelectedElement(binding);
			}
		});

		IPropertyChangeListener treeUpdateListener = new IPropertyChangeListener() {

			// When the model changes a property, update the viewer
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getSource() == model
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
					Object newVal = event.getNewValue();
					StructuredSelection structuredSelection = newVal == null ? null : new StructuredSelection(newVal);
					viewer.setSelection(structuredSelection, true);
				} else if (event.getSource() instanceof BindingElement
						&& ModelElement.PROP_MODEL_OBJECT.equals(event.getProperty())) {
					viewer.update(event.getSource(), null);
				} else if (BindingElement.PROP_CONFLICT.equals(event.getProperty())) {
					viewer.update(event.getSource(), null);
				} else if (BindingModel.PROP_BINDINGS.equals(event.getProperty())) {
					// viewer.refresh();
				} else if (BindingModel.PROP_BINDING_ADD.equals(event.getProperty())) {
					viewer.add(model, event.getNewValue());
				} else if (BindingModel.PROP_BINDING_REMOVE.equals(event.getProperty())) {
					viewer.remove(event.getNewValue());
				} else if (BindingModel.PROP_BINDING_FILTER.equals(event.getProperty())) {
					// viewer.refresh();
				}
				changeBackground();
				// isValid();
			}
		};
		keyController.addPropertyChangeListener(treeUpdateListener);

		// IPropertyChangeListener conflictsListener = new IPropertyChangeListener() {
		// public void propertyChange(PropertyChangeEvent event) {
		// // System.out.println(event.getNewValue().getClass());
		// if (keyController.getConflictModel().getConflicts() != null) {
		// if (ConflictModel.PROP_CONFLICTS.equals(event.getProperty())) {
		// // lstConflict.clear();
		// if (event.getNewValue() != null) {
		// lstConflict.addAll((Collection<? extends Object>) event.getNewValue());
		// }
		// // conflictViewer.setInput(event.getNewValue());
		// } else if (ConflictModel.PROP_CONFLICTS_ADD.equals(event.getProperty())) {
		// if (event.getNewValue() != null) {
		// lstConflict.add(event.getNewValue());
		// }
		// // conflictViewer.add(event.getNewValue());
		// } else if (ConflictModel.PROP_CONFLICTS_REMOVE.equals(event.getProperty())) {
		// // conflictViewer.remove(event.getNewValue());
		// if (event.getNewValue() != null) {
		// lstConflict.remove(event.getNewValue());
		// }
		// }
		// } else {
		// lstConflict.clear();
		// }
		// // if (event.getSource() == keyController.getConflictModel()
		// // && CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
		// // if (keyController.getConflictModel().getConflicts() != null) {
		// // Object newVal = event.getNewValue();
		// // StructuredSelection structuredSelection = newVal == null ? null : new StructuredSelection(
		// // newVal);
		// // // conflictViewer.setSelection(structuredSelection, true);
		// // }
		// // } else if (ConflictModel.PROP_CONFLICTS.equals(event.getProperty())) {
		// // // lstConflict.clear();
		// // if (event.getNewValue() != null) {
		// // lstConflict.addAll((Collection<? extends Object>) event.getNewValue());
		// // }
		// // // conflictViewer.setInput(event.getNewValue());
		// // } else if (ConflictModel.PROP_CONFLICTS_ADD.equals(event.getProperty())) {
		// // if (event.getNewValue() != null) {
		// // lstConflict.clear();
		// // lstConflict.add(event.getNewValue());
		// // }
		// // // conflictViewer.add(event.getNewValue());
		// // } else if (ConflictModel.PROP_CONFLICTS_REMOVE.equals(event.getProperty())) {
		// // // conflictViewer.remove(event.getNewValue());
		// // if (event.getNewValue() != null) {
		// // lstConflict.clear();
		// // lstConflict.remove(event.getNewValue());
		// // }
		// // }
		// }
		// };
		// keyController.addPropertyChangeListener(conflictsListener);

		IPropertyChangeListener dataUpdateListener = new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				BindingElement bindingElement = null;
				boolean weCare = false;
				if (event.getSource() == model
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
					bindingElement = (BindingElement) event.getNewValue();
					weCare = true;
				} else if (event.getSource() == model.getSelectedElement()
						&& ModelElement.PROP_MODEL_OBJECT.equals(event.getProperty())) {
					bindingElement = (BindingElement) event.getSource();
					weCare = true;
				}
				if (bindingElement == null && weCare) {
					fBindingText.setText(""); //$NON-NLS-1$
				} else if (bindingElement != null) {
					KeySequence trigger = (KeySequence) bindingElement.getTrigger();
					fKeySequenceText.setKeySequence(trigger);
				}
			}
		};
		keyController.addPropertyChangeListener(dataUpdateListener);
	}

	List<Object> lstConflict = new ArrayList<Object>();

	Text fBindingText;

	class TableViewerEditingSupport extends EditingSupport {

		int column;
		private TreeViewer columnViewer;
		private CellEditor editor;

		public TableViewerEditingSupport(ColumnViewer viewers, int column) {
			super(viewers);
			this.columnViewer = (TreeViewer) viewers;
			if (column == 1) {
				editor = new TextCellEditor(columnViewer.getTree(), SWT.SINGLE | SWT.BORDER) {
					protected Control createControl(Composite parent) {
						super.createControl(parent);
						fBindingText = text;
						text.addFocusListener(new FocusListener() {
							public void focusGained(FocusEvent e) {
								fBindingService.setKeyFilterEnabled(false);
							}

							public void focusLost(FocusEvent e) {
								fBindingService.setKeyFilterEnabled(true);
							}
						});
						text.addDisposeListener(new DisposeListener() {
							public void widgetDisposed(DisposeEvent e) {
								if (!fBindingService.isKeyFilterEnabled()) {
									fBindingService.setKeyFilterEnabled(true);
								}
							}
						});
						fKeySequenceText = new KeySequenceText(text);
						fKeySequenceText.setKeyStrokeLimit(1);
						fKeySequenceText.addPropertyChangeListener(new IPropertyChangeListener() {
							public final void propertyChange(final PropertyChangeEvent event) {
								if (!event.getOldValue().equals(event.getNewValue())) {
									final KeySequence keySequence = fKeySequenceText.getKeySequence();
									if (!keySequence.isComplete()) {
										return;
									}
									// BindingElement activeBinding = (BindingElement)
									// model.getSelectedElement();
									// if (activeBinding != null) {
									// activeBinding.setTrigger(keySequence);
									// }
									// changeBackground();
									text.setSelection(0, text.getText().length());
									// isValid();
								}
							}
						});
						return text;
					}

				};
			}
			this.column = column;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			// System.out.println(element);
			BindingElement bindingElement = ((BindingElement) element);
			TriggerSequence seq = bindingElement.getTrigger();
			return seq == null ? Util.ZERO_LENGTH_STRING : seq.format();
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (column == 1) {
				// BindingElement activeBinding = (BindingElement) model.getSelectedElement();
				BindingElement activeBinding = (BindingElement) element;
				if (activeBinding != null) {
					KeySequence keySequence = fKeySequenceText.getKeySequence();
//					Bug #2740
					if (keySequence == null || !keySequence.toString().endsWith("+")) {
						activeBinding.setTrigger(keySequence);
					}
				}
				changeBackground();
				// isValid();
			}
		}
	}

	public boolean validateConflict() {
		for (TreeItem item : viewer.getTree().getItems()) {
			BindingElement element = (BindingElement) item.getData();
			if (element.getConflict()) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("preferencepage.KeysPreferencePage.msgTitle"),
						Messages.getString("preferencepage.KeysPreferencePage.msg"));
				// setValid(false);
				return false;
			}
		}
		// setValid(true);
		return true;
	}

	public void applyData(Object data) {
		// if (!validate()) {
		// return;
		// }
		if (data instanceof ModelElement) {
			model.setSelectedElement((ModelElement) data);
		}
		if (data instanceof Binding && fFilteredTree != null) {
			BindingElement be = (BindingElement) model.getBindingToElement().get(data);
			fFilteredTree.getViewer().setSelection(new StructuredSelection(be), true);
		}
		if (data instanceof ParameterizedCommand) {
			Map commandToElement = model.getCommandToElement();

			BindingElement be = (BindingElement) commandToElement.get(data);
			if (be != null) {
				fFilteredTree.getViewer().setSelection(new StructuredSelection(be), true);
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		if (!validateConflict()) {
			return false;
		}
		keyController.saveBindings(fBindingService);
		saveState(getDialogSettings());
		return super.performOk();
	}

	/**
	 * Save the state of the receiver.
	 * @param dialogSettings
	 */
	public void saveState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			return;
		}
		dialogSettings.put(TAG_FILTER_ACTION_SETS, fFilterActionSetContexts);
		dialogSettings.put(TAG_FILTER_INTERNAL, fFilterInternalContexts);
		dialogSettings.put(TAG_FILTER_UNCAT, fFilteredTree.isFilteringCategories());
	}

	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();

		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);

		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		return settings;
	}

	protected void performDefaults() {

		// Ask the user to confirm
		final String title = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxText;
		final String message = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxMessage;
		final boolean confirmed = MessageDialog.open(MessageDialog.CONFIRM, getShell(), title, message, SWT.SHEET);

		if (confirmed) {
			long startTime = 0L;
			if (DEBUG) {
				startTime = System.currentTimeMillis();
			}

			fFilteredTree.setRedraw(false);
			BusyIndicator.showWhile(fFilteredTree.getViewer().getTree().getDisplay(), new Runnable() {
				public void run() {
					try {
						keyController.setDefaultBindings(fBindingService, lstRemove);
					} catch (NotDefinedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			fFilteredTree.setRedraw(true);
			if (DEBUG) {
				final long elapsedTime = System.currentTimeMillis() - startTime;
				Tracing.printTrace(TRACING_COMPONENT, "performDefaults:model in " //$NON-NLS-1$
						+ elapsedTime + "ms"); //$NON-NLS-1$
			}
		}

		super.performDefaults();
	}
}
