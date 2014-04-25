package net.sourceforge.nattable.edit.editor;

import net.sourceforge.nattable.painter.cell.CheckBoxPainter;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CheckBoxCellEditor extends AbstractCellEditor {

	private boolean checked;
	private Canvas canvas;
	private final CheckBoxPainter checkBoxCellPainter;

	public CheckBoxCellEditor() {
		this.checkBoxCellPainter = new CheckBoxPainter();
	}

	public CheckBoxCellEditor(Image checkedImg, Image uncheckedImg) {
		this.checkBoxCellPainter = new CheckBoxPainter(checkedImg, uncheckedImg);
	}

	/**
	 * As soon as the editor is activated, flip the current data value and commit it.<br/>
	 * The repaint will pick up the new value and flip the image.
	 */
	@Override
	protected Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue) {
		setCanonicalValue(originalCanonicalValue);

		checked = !checked;

		canvas = new Canvas(parent, SWT.NONE);

		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent paintEvent) {
				Rectangle bounds = canvas.getBounds();
				Rectangle rect = new Rectangle(0, 0, bounds.width, bounds.height);
				checkBoxCellPainter.paintIconImage(paintEvent.gc, rect, bounds.height / 2 - checkBoxCellPainter.getPreferredHeight(checked) / 2, checked);
			}

		});

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				checked = !checked;
				canvas.redraw();
			}

		});

		commit(MoveDirectionEnum.NONE, false);

		return canvas;
	}

	public Object getCanonicalValue() {
		return getDataTypeConverter().displayToCanonicalValue(Boolean.valueOf(checked));
	}

	public void setCanonicalValue(Object canonicalValue) {
		if (canonicalValue == null) {
			checked = false;
		} else {
			checked = Boolean.valueOf((String) getDataTypeConverter().canonicalToDisplayValue(canonicalValue)).booleanValue();
		}
	}

	@Override
	public void close() {
		super.close();

		if (canvas != null && !canvas.isDisposed()) {
			canvas.dispose();
		}
	}

}
