package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import java.util.TreeMap;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.data.convert.DefaultDisplayConverter;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.cell.LayerCell;

/**
 * Tag 显示格式转换器
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class TagDisplayConverter extends DefaultDisplayConverter {

	private final XLIFFEditorImplWithNatTable xliffEditor;

	private final NatTable table;

	private LayerCell currentCell;

	TagDisplayConverter(XLIFFEditorImplWithNatTable xliffEditor) {
		this.xliffEditor = xliffEditor;
		table = xliffEditor.getTable();
	}

	public void setCell(LayerCell cell) {
		this.currentCell = cell;
	}

	/**
	 * (non-Javadoc)
	 * @see net.sourceforge.nattable.data.convert.DefaultDisplayConverter#canonicalToDisplayValue(java.lang.Object)
	 */
	public Object canonicalToDisplayValue(Object xmlValue) {
		if (xmlValue == null || xmlValue.toString().length() == 0) {
			return "";
		}
		String originalValue = xmlValue.toString();

		TagStyle tagStyle = xliffEditor.getTagStyleManager().getTagStyle();
//		if (TagStyle.FULL.equals(tagStyle)) {
//			return originalValue;
//		} else {
			StringBuffer displayValue = new StringBuffer(originalValue);
			int columnIndex = table.getColumnIndexByPosition(currentCell.getColumnPosition());

			if (xliffEditor.isHorizontalLayout()) {
				if (columnIndex == xliffEditor.getSrcColumnIndex()) {
					InnerTagUtil.parseXmlToDisplayValue(displayValue, tagStyle); // 设置内部标记索引及样式
				} else if (columnIndex == xliffEditor.getTgtColumnIndex()) {
					int rowIndex = currentCell.getLayer().getRowIndexByPosition(
							currentCell.getRowPosition()) ;
					int srcColumnPosition = LayerUtil.getColumnPositionByIndex(table, xliffEditor.getSrcColumnIndex());
					if (srcColumnPosition != -1) { // 得到Source列的位置
						DataLayer dataLayer = LayerUtil.getLayer(table, DataLayer.class);
						String srcOriginalValue = dataLayer.getDataValueByPosition(srcColumnPosition, rowIndex).toString();
						InnerTagUtil.parseXmlToDisplayValueFromSource(srcOriginalValue, displayValue, tagStyle);
					} else {
						InnerTagUtil.parseXmlToDisplayValue(displayValue, tagStyle); // 设置内部标记索引及样式
					}
	
					currentCell = null; // 恢复初始值
				} else {
					// do nothing
				}
			} else {
				int rowIndex = currentCell.getLayer().getRowIndexByPosition(
						currentCell.getRowPosition());
				if (columnIndex == xliffEditor.getSrcColumnIndex() && rowIndex % 2 == 0) {		//源语言
					InnerTagUtil.parseXmlToDisplayValue(displayValue, tagStyle); // 设置内部标记索引及样式
				} else if (columnIndex == xliffEditor.getTgtColumnIndex()) {		//目标语言
					int srcColumnPosition = LayerUtil.getColumnPositionByIndex(table, xliffEditor.getSrcColumnIndex());
					if (srcColumnPosition != -1) { // 得到Source列的位置
//						DataLayer dataLayer = LayerUtil.getLayer(table, DataLayer.class);
						String srcOriginalValue = table.getDataValueByPosition(srcColumnPosition, rowIndex - 1).toString();
						InnerTagUtil.parseXmlToDisplayValueFromSource(srcOriginalValue, displayValue, tagStyle);
					} else {
						InnerTagUtil.parseXmlToDisplayValue(displayValue, tagStyle); // 设置内部标记索引及样式
					}
	
					currentCell = null; // 恢复初始值
				} else {
					// do nothing
				}
			}
			return InnerTagUtil.resolveTag(displayValue.toString());
//		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.sourceforge.nattable.data.convert.DefaultDisplayConverter#displayToCanonicalValue(java.lang.Object)
	 */
	public Object displayToCanonicalValue(Object tagValue) {
		String displayValue = tagValue == null ? "" : tagValue.toString();
		String content = InnerTagUtil.escapeTag(displayValue);
		
		int rowIndex = currentCell.getLayer().getRowIndexByPosition(
				currentCell.getRowPosition());
		int srcColumnPosition = LayerUtil.getColumnPositionByIndex(table, xliffEditor.getSrcColumnIndex());
		if (srcColumnPosition != -1) { // 得到Source列的位置
			DataLayer dataLayer = LayerUtil.getLayer(table, DataLayer.class);
			String srcOriginalValue = dataLayer.getDataValueByPosition(srcColumnPosition, rowIndex).toString();
			TreeMap<String, InnerTagBean> sourceTags = InnerTagUtil.parseXmlToDisplayValue(new StringBuffer(
					srcOriginalValue), xliffEditor.getTagStyleManager().getTagStyle());
			return InnerTagUtil.parseDisplayToXmlValue(sourceTags, content); // 换回xml格式的内容
		} else {
			return content; // 设置内部标记索引及样式
		}
	}
}
