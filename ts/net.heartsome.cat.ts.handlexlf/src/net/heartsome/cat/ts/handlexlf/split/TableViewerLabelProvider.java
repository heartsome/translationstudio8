package net.heartsome.cat.ts.handlexlf.split;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider{
	
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
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
