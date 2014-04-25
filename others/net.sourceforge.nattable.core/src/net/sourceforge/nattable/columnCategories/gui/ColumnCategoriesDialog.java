package net.sourceforge.nattable.columnCategories.gui;

import static net.sourceforge.nattable.columnChooser.ColumnChooserUtils.getColumnEntryPositions;
import static net.sourceforge.nattable.util.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.columnCategories.ColumnCategoriesModel;
import net.sourceforge.nattable.columnCategories.IColumnCategoriesDialogListener;
import net.sourceforge.nattable.columnCategories.Node;
import net.sourceforge.nattable.columnCategories.Node.Type;
import net.sourceforge.nattable.columnChooser.ColumnEntry;
import net.sourceforge.nattable.columnChooser.gui.AbstractColumnChooserDialog;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.util.GUIHelper;
import net.sourceforge.nattable.util.ObjectUtils;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * JFace/SWT based column chooser dialog which displays the available/hidden
 * columns in a tree viewer. This tree viewer is based on the {@link ColumnCategoriesModel}.
 */
public class ColumnCategoriesDialog extends AbstractColumnChooserDialog {

	private final ColumnCategoriesModel model;
	private List<ColumnEntry> hiddenColumnEntries;
	private List<ColumnEntry> visibleColumnsEntries;
	private TreeViewer treeViewer;
	private ListViewer listViewer;
	private ISelection lastListSelection;

	public ColumnCategoriesDialog(Shell shell,
				ColumnCategoriesModel model,
				List<ColumnEntry> hiddenColumnEntries,
				List<ColumnEntry> visibleColumnsEntries) {
		super(shell);
		this.model = model;
		this.hiddenColumnEntries = hiddenColumnEntries;
		this.visibleColumnsEntries = visibleColumnsEntries;
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
	}

	@Override
	public void populateDialogArea(Composite parent) {
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		parent.setLayout(new GridLayout(4, false));

		// Labels
		createLabels(parent, "Available columns", "Selected columns");
		GridData gridData = GridDataFactory.fillDefaults().grab(true, true).create();

		// Left tree - column categories
		treeViewer = new TreeViewer(parent);

		populateAvailableTree();
		treeViewer.getControl().setLayoutData(gridData);

		// Add/remove buttons
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, true));
		createAddButton(buttonComposite);
		createRemoveButton(buttonComposite);
		addListenersToTreeViewer();

		// Right list - selected columns
		listViewer = new ListViewer(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		populateSelectedList();
		addListenersToListViewer();

		// Up/down buttons
		Composite upDownbuttonComposite = new Composite(parent, SWT.NONE);
		upDownbuttonComposite.setLayout(new GridLayout(1, true));
		createUpButton(upDownbuttonComposite);
		createDownButton(upDownbuttonComposite);
	}

	private void populateSelectedList() {
		VisibleColumnsProvider listProvider = new VisibleColumnsProvider(visibleColumnsEntries);
		listViewer.setContentProvider(listProvider);
		listViewer.setLabelProvider(listProvider);
		listViewer.setInput(listProvider);

		listViewer.setContentProvider(listProvider);
		listViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
	}

	private void addListenersToTreeViewer() {
		treeViewer.getControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				addSelected();
			}
		});
	}

	private void addListenersToListViewer() {
		listViewer.getControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				removeSelected();
			}
		});

		listViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				boolean controlMask = (e.stateMask & SWT.CONTROL) == SWT.CONTROL;
				if (controlMask && e.keyCode == SWT.ARROW_UP) {
					moveSelectedUp();
					e.doit = false;
				} else if (controlMask && e.keyCode == SWT.ARROW_DOWN) {
					moveSelectedDown();
					e.doit = false;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == ' ')
					removeSelected();
			}
		});
	}

	private void populateAvailableTree() {
		AvailableColumnCategoriesProvider provider = new AvailableColumnCategoriesProvider(model);
		provider.hideEntries(visibleColumnsEntries);

		treeViewer.setContentProvider(provider);
		treeViewer.setLabelProvider(new ColumnCategoriesLabelProvider(hiddenColumnEntries));
		treeViewer.setInput(provider);
	}

	private Button createDownButton(Composite upDownbuttonComposite) {
		Button downButton = new Button(upDownbuttonComposite, SWT.PUSH);
		downButton.setImage(GUIHelper.getImage("arrow_down"));
		downButton.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).create());
		downButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				moveSelectedDown();
			}
		});
		return downButton;
	}

	private Button createUpButton(Composite upDownbuttonComposite) {
		Button upButton = new Button(upDownbuttonComposite, SWT.PUSH);
		upButton.setImage(GUIHelper.getImage("arrow_up"));
		upButton.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).create());
		upButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				moveSelectedUp();
			}

		});
		return upButton;
	}

	private Button createRemoveButton(Composite buttonComposite) {
		Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setImage(GUIHelper.getImage("arrow_left"));
		removeButton.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).create());
		removeButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				removeSelected();
			}
		});
		return removeButton;
	}

	private Button createAddButton(Composite buttonComposite) {
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setImage(GUIHelper.getImage("arrow_right"));
		addButton.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).create());
		addButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				addSelected();
			}

		});
		return addButton;
	}

	// Respond to button clicks / user actions

	protected void removeSelected() {
		fireItemsRemoved(getColumnPositionsFromListViewer());
	}

	protected void addSelected() {
		fireItemsSelected(getColumnIndexesFromTreeNodes());
	}

	protected final void fireItemsSelected(List<Integer> addedColumnIndexes) {
		if (isNotEmpty(addedColumnIndexes)) {
			for (Object listener : listeners.getListeners()) {
				((IColumnCategoriesDialogListener) listener).itemsSelected(addedColumnIndexes);
			}
		}
	}

	protected final void fireItemsRemoved(List<Integer> removedColumnPositions) {
		if (isNotEmpty(removedColumnPositions)) {
			for (Object listener : listeners.getListeners()) {
				((IColumnCategoriesDialogListener) listener).itemsRemoved(removedColumnPositions);
			}
		}
	}

	protected final void fireItemsMoved(MoveDirectionEnum direction, List<Integer> toPositions) {
		for (Object listener : listeners.getListeners()) {
			((IColumnCategoriesDialogListener) listener).itemsMoved(direction, toPositions);
		}
	}

	protected void moveSelectedUp() {
		List<Integer> selectedPositions = getColumnEntryPositions(getSelectedColumnEntriesFromListViewer());

		// First position selected
		if(!selectedPositions.contains(0)){
			fireItemsMoved(MoveDirectionEnum.UP, selectedPositions);
		}
	}

	protected void moveSelectedDown() {
		List<Integer> selectedPositions = getColumnEntryPositions(getSelectedColumnEntriesFromListViewer());

		// Last position selected
		if(!selectedPositions.contains(visibleColumnsEntries.size())){
			fireItemsMoved(MoveDirectionEnum.DOWN, selectedPositions);
		}
	}

	/**
	 * @return selected column position(s) from the list viewer
	 */
	private List<Integer> getColumnPositionsFromListViewer() {
		return getColumnEntryPositions(getSelectedColumnEntriesFromListViewer());
	}

	private List<ColumnEntry> getSelectedColumnEntriesFromListViewer() {
		lastListSelection = listViewer.getSelection();
		Object[] objects = ((StructuredSelection) lastListSelection).toArray();
		List<ColumnEntry> entries = new ArrayList<ColumnEntry>();

		for (Object object : objects) {
			entries.add((ColumnEntry) object);
		}
		return entries;
	}


	/**
	 * @return selected columns index(s) from the tree viewer
	 */
	private List<Integer> getColumnIndexesFromTreeNodes() {
		Object[] nodes = ((TreeSelection)treeViewer.getSelection()).toArray();

		List<Integer> indexes = new ArrayList<Integer>();
		for (Object object : nodes) {
			Node node = (Node) object;
			if(Type.COLUMN == node.getType()){
				indexes.add(Integer.parseInt(node.getData()));
			}
		}
		return indexes;
	}


	public void refresh(List<ColumnEntry> hiddenColumnEntries, List<ColumnEntry> visibleColumnsEntries) {
		this.hiddenColumnEntries = hiddenColumnEntries;
		this.visibleColumnsEntries = visibleColumnsEntries;
		populateAvailableTree();
		populateSelectedList();
		if(ObjectUtils.isNotNull(lastListSelection)){
			listViewer.setSelection(lastListSelection);
		}
	}
}
