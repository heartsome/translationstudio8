package net.heartsome.cat.ts.ui.plugin;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * TableViewer的标签提供者
 * @author robert 2012-03-10
 * @version
 * @since JDK1.6
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
