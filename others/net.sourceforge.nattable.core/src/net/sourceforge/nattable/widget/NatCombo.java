package net.sourceforge.nattable.widget;

import java.util.Arrays;

import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.style.VerticalAlignmentEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NatCombo extends Composite {

	public static final int DEFAULT_NUM_OF_VISIBLE_ITEMS = 5;

	private final IStyle cellStyle;

	private int maxVisibleItems = 10;

	private String[] items;

	private Text text;

	private Shell dropdownShell;

	private List dropdownList;

	private Image iconImage;

	public NatCombo(Composite parent, IStyle cellStyle) {
		this(parent, cellStyle, DEFAULT_NUM_OF_VISIBLE_ITEMS);
	}

	public NatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems) {
		super(parent, SWT.NONE);

		this.cellStyle = cellStyle;

		this.maxVisibleItems = maxVisibleItems;

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent event) {
				resizeDropdownControl();
			}

		});

		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent event) {
				dropdownShell.dispose();
				text.dispose();
			}

		});

		createTextControl();
		createDropdownControl();
	}

	public void setItems(String[] items) {
		if (items != null) {
			this.items = items;
			if (!dropdownList.isDisposed() && items != null && items.length > 0) {
				dropdownList.setItems(items);
			}
			resizeDropdownControl();
		}
	}

	public void setSelection(String[] items) {
		if(items != null){
			if (!dropdownList.isDisposed()) {
				dropdownList.setSelection(items);
			}
		}
		if(items[0] != null){
			text.setText(items[0]);
		}
	}

	public int getSelectionIndex() {
		if (!dropdownList.isDisposed()) {
			return dropdownList.getSelectionIndex();
		} else {
			return Arrays.asList(items).indexOf(text.getText());
		}
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
		dropdownList.addKeyListener(listener);
	}

	@Override
	public void addTraverseListener(TraverseListener listener) {
		text.addTraverseListener(listener);
		dropdownList.addTraverseListener(listener);
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		text.addMouseListener(listener);
		dropdownList.addMouseListener(listener);
	}

	@Override
	public void notifyListeners(int eventType, Event event) {
		dropdownList.notifyListeners(eventType, event);
	}

	private void createTextControl() {
		text = new Text(this, HorizontalAlignmentEnum.getSWTStyle(cellStyle));
		text.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		text.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		text.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		text.setLayoutData(gridData);
		text.forceFocus();

		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.ARROW_DOWN || event.keyCode == SWT.ARROW_UP) {
					showDropdownControl();

					int selectionIndex = dropdownList.getSelectionIndex();
					selectionIndex += event.keyCode == SWT.ARROW_DOWN ? 1 : -1;
					if (selectionIndex < 0) {
						selectionIndex = 0;
					}

					dropdownList.select(selectionIndex);
					text.setText(dropdownList.getSelection()[0]);
				}
			}

		});

		iconImage = GUIHelper.getImage("down_2");
		final Canvas iconCanvas = new Canvas(this, SWT.NONE) {

			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Rectangle iconImageBounds = iconImage.getBounds();
				return new Point(iconImageBounds.width + 2, iconImageBounds.height + 2);
			}

		};

		gridData = new GridData(GridData.BEGINNING, SWT.FILL, false, true);
		iconCanvas.setLayoutData(gridData);

		iconCanvas.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent event) {
				GC gc = event.gc;

				Rectangle iconCanvasBounds = iconCanvas.getBounds();
				Rectangle iconImageBounds = iconImage.getBounds();
				int horizontalAlignmentPadding = CellStyleUtil.getHorizontalAlignmentPadding(HorizontalAlignmentEnum.CENTER, iconCanvasBounds, iconImageBounds.width);
				int verticalAlignmentPadding = CellStyleUtil.getVerticalAlignmentPadding(VerticalAlignmentEnum.MIDDLE, iconCanvasBounds, iconImageBounds.height);
				gc.drawImage(iconImage, horizontalAlignmentPadding, verticalAlignmentPadding);

				Color originalFg = gc.getForeground();
				gc.setForeground(GUIHelper.COLOR_WIDGET_BORDER);
				gc.drawRectangle(0, 0, iconCanvasBounds.width - 1, iconCanvasBounds.height - 1);
				gc.setForeground(originalFg);
			}

		});

		iconCanvas.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				showDropdownControl();
			}

		});
	}

	private void showDropdownControl() {
		if (dropdownShell.isDisposed()) {
			createDropdownControl();
		}
		resizeDropdownControl();
	}

	private void createDropdownControl() {
		dropdownShell = new Shell(getShell(), SWT.MODELESS);
		dropdownShell.setLayout(new FillLayout());

		dropdownList = new List(dropdownShell, SWT.V_SCROLL | HorizontalAlignmentEnum.getSWTStyle(cellStyle));
		dropdownList.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		dropdownList.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		dropdownList.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));

		dropdownShell.addShellListener(new ShellAdapter() {

			@Override
			public void shellClosed(ShellEvent event) {
				text.forceFocus();
			}

		});

		dropdownList.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (dropdownList.getSelectionCount() > 0) {
					text.setText(dropdownList.getSelection()[0]);
				}
			}

		});

		setItems(items);
		dropdownList.setSelection(new String[] { text.getText() });
	}

	private void resizeDropdownControl() {
		if (dropdownShell != null && !dropdownShell.isDisposed() && !dropdownShell.isVisible()) {
			Point size = getSize();
			int itemCount = dropdownList.getItemCount();
			if (itemCount > 0 && size.x > 0 && size.y > 0) {
				int listHeight = Math.min(itemCount, maxVisibleItems) * dropdownList.getItemHeight();
				dropdownList.setSize(size.x, listHeight);

				Point point = toDisplay(0, 0);
				int shellHeight = dropdownShell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
				dropdownShell.setBounds(point.x, point.y + size.y, size.x, shellHeight);
				dropdownShell.open();
			}
		}
	}

	public void select(int index) {
		dropdownList.select(index);
	}

}
