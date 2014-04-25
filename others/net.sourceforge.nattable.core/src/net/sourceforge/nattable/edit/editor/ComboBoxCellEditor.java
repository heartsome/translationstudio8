package net.sourceforge.nattable.edit.editor;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.util.ArrayUtil;
import net.sourceforge.nattable.widget.NatCombo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Renders an SWT combo box.
 *    Users can select from the drop down or enter their own values.
 */
public class ComboBoxCellEditor extends AbstractCellEditor {

	private NatCombo combo;

	private int maxVisibleItems = 10;
	private Object originalCanonicalValue;
	private final List<?> canonicalValues;

	/**
	 * @see this{@link #ComboBoxCellEditor(List, int)}
	 */
	public ComboBoxCellEditor(List<?> canonicalValues){
		this(canonicalValues, NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
	}

	/**
	 * @see this{@link #ComboBoxCellEditor(List, int)}
	 */
	public ComboBoxCellEditor(IComboBoxDataProvider dataProvider){
		this(dataProvider.getValues(), NatCombo.DEFAULT_NUM_OF_VISIBLE_ITEMS);
	}

	/**
	 * @param canonicalValues Array of items to be shown in the drop down box. These will be
	 * 	converted using the {@link IDisplayConverter} for display purposes
	 * @param maxVisibleItems the max items the drop down will show before introducing a scroll bar.
	 */
	public ComboBoxCellEditor(List<?> canonicalValues, int maxVisibleItems) {
		this.canonicalValues = canonicalValues;
		this.maxVisibleItems = maxVisibleItems;
	}

	@Override
	protected Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue) {
		this.originalCanonicalValue = originalCanonicalValue;

		combo = new NatCombo(parent, getCellStyle(), maxVisibleItems);

		combo.setItems(getDisplayValues());

		if (originalCanonicalValue != null) {
			combo.setSelection(new String[] { getDisplayValue() });
		}

		combo.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if ((event.keyCode == SWT.CR && event.stateMask == 0)
						|| (event.keyCode == SWT.KEYPAD_CR && event.stateMask == 0)) {
					commit(MoveDirectionEnum.NONE);
				} else if (event.keyCode == SWT.ESC && event.stateMask == 0){
					close();
				}
			}

		});

		combo.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent event) {
				if (event.keyCode == SWT.TAB && event.stateMask == SWT.SHIFT) {
					commit(MoveDirectionEnum.LEFT);
				} else if (event.keyCode == SWT.TAB && event.stateMask == 0) {
					commit(MoveDirectionEnum.RIGHT);
				}
			}

		});

		combo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				commit(MoveDirectionEnum.NONE);
			}
		});

		return combo;
	}

	public Object getCanonicalValue() {
		int selectionIndex = combo.getSelectionIndex();

		//Item selected from list
		if (selectionIndex >= 0) {
			return canonicalValues.get(selectionIndex);
		} else {
			return originalCanonicalValue;
		}
	}

	public void select(int index){
		combo.select(index);
	}

	public void setCanonicalValue(Object value) {
		//No op - combo is not dynamic
	}

	@Override
	public void close() {
		super.close();

		if (combo != null && !combo.isDisposed()) {
			combo.dispose();
		}
	}

	private String getDisplayValue() {
		return (String) getDataTypeConverter().canonicalToDisplayValue(originalCanonicalValue);
	}

	private String[] getDisplayValues() {
		List<String> displayValues = new ArrayList<String>();

		for (Object canonicalValue : canonicalValues) {
			displayValues.add((String) getDataTypeConverter().canonicalToDisplayValue(canonicalValue));
		}

		return displayValues.toArray(ArrayUtil.STRING_TYPE_ARRAY);
	}

}
