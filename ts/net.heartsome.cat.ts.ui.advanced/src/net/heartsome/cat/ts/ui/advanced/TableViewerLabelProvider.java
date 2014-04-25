package net.heartsome.cat.ts.ui.advanced;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
/**
 * tableViewer的标签提供器
 * @author robert
 */
public class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof String[]) {
			String[] array = (String[]) element;
			return array[columnIndex];
		}
		return null;
	}
}
