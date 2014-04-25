/**
 * HsMultiCellEditor.java
 *
 * Version information :
 *
 * Date:2012-12-17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.heartsome.cat.common.bean.ColorConfigBean;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.ts.core.bean.SingleWord;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.bean.XliffEditorParameter;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.style.IStyle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class HsMultiCellEditor {
	/**
	 * marked as source editor or target editor <br>
	 * {@link NatTableConstant#SOURCE}
	 */
	private String type;
	private StyledTextCellEditor cellEditor;
	private Control activeCellEditorControl;
	private ICellEditHandler editHandler;

	private int columnPosition = -1;
	private int rowPosition = -1;
	private int columnIndex = -1;
	private int rowIndex = -1;

	private IDataValidator dataValidator;
	private Object originalCanonicalValue;
	// private IDisplayConverter displayConverter;
	private IStyle cellStyle;
	private Rectangle editorBounds;

	/**
	 * @param type
	 * @param cellEditor
	 * @param activeCellEditorControl
	 * @param editHandler
	 * @param columnPosition
	 * @param rowPosition
	 * @param columnIndex
	 * @param rowIndex
	 * @param dataValidator
	 * @param originalCanonicalValue
	 * @param displayConverter
	 * @param cellStyle
	 * @param editorBounds
	 */
	public HsMultiCellEditor(String type, StyledTextCellEditor cellEditor, ICellEditHandler editHandler,
			int columnPosition, int rowPosition, int columnIndex, int rowIndex, IDataValidator dataValidator,
			Object originalCanonicalValue, IDisplayConverter displayConverter, IStyle cellStyle, Rectangle editorBounds) {
		this.type = type;
		this.cellEditor = cellEditor;
		this.editHandler = editHandler;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
		this.dataValidator = dataValidator;
		this.originalCanonicalValue = originalCanonicalValue;
		// this.displayConverter = displayConverter;
		this.cellStyle = cellStyle;
		this.editorBounds = editorBounds;
	}

	public void activeCurrentEditor(Composite parent) {
		activeCellEditorControl = cellEditor.activateCell(parent, this);
//		if (activeCellEditorControl != null) {
//			activeCellEditorControl.setBounds(this.editorBounds);
//		}
	}

	public boolean isFocus() {
		if (activeCellEditorControl != null && !activeCellEditorControl.isDisposed())
			return activeCellEditorControl.isFocusControl();
		return false;
	}

	public void forceFocus() {
		if (activeCellEditorControl != null && !activeCellEditorControl.isDisposed()) {
			activeCellEditorControl.forceFocus();
		}
	}

	public boolean isValid() {
		return cellEditor != null && !cellEditor.isClosed();
	}

	public boolean validateCanonicalValue() {
		if (dataValidator != null) {
			return dataValidator.validate(columnIndex, rowIndex, getCanonicalValue());
		} else {
			return true;
		}
	}

	public Object getCanonicalValue() {
		if (isValid()) {
			return cellEditor.getCanonicalValue();
		} else {
			return null;
		}
	}

	public void close() {
		if (cellEditor != null && !cellEditor.isClosed()) {
			cellEditor.close();
		}
		cellEditor = null;
		editHandler = null;
		dataValidator = null;
		if (activeCellEditorControl != null && !activeCellEditorControl.isDisposed()) {
			activeCellEditorControl.dispose();
		}
		activeCellEditorControl = null;
		columnPosition = -1;
		rowPosition = -1;
		columnIndex = -1;
		rowIndex = -1;
	}

	public void highlightedTerms(List<String> terms) {
		if (!isValid()) {
			return;
		}
		StyledText styledText = cellEditor.viewer.getTextWidget();
		String text = styledText.getText();
		char[] source = text.toCharArray();
		List<StyleRange> ranges = new ArrayList<StyleRange>();
		TextStyle style = new TextStyle(cellEditor.getSegmentViewer().getTextWidget().getFont(), null,
				ColorConfigBean.getInstance().getHighlightedTermColor());
		for (String term : terms) {
			if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
				term = term.replaceAll("\\n", Constants.LINE_SEPARATOR_CHARACTER + "\n");
				term = term.replaceAll("\\t", Constants.TAB_CHARACTER + "\u200B");
				term = term.replaceAll(" ", Constants.SPACE_CHARACTER + "\u200B");
			}
			ranges.addAll(calculateTermsStyleRange(source, term.toCharArray(), style));
		}
		for (StyleRange range : ranges) {
			styledText.setStyleRange(range);
		}
	}
	
	/**
	 * 实时拼检查时高亮错误单词	robert	2013-01-21
	 * @param terms
	 */
	public void highLightedErrorWord(String tgtText, List<SingleWord> errorWordList) {
		if (!isValid()) {
			return;
		}
		List<StyleRange> ranges = new ArrayList<StyleRange>();
		TextStyle style = new TextStyle(cellEditor.getSegmentViewer().getTextWidget().getFont(), null,
				null);
		for(SingleWord singleWord : errorWordList){
			Matcher match = PlaceHolderEditModeBuilder.PATTERN.matcher(singleWord.getWord());
			
			// 这里是处理一个单词中有一个或多个标记，从而导致标记绘画失败的BUG，如果其中有标记，那么这个 StyleRange 就应该被切断
			boolean hasTag = false;
			int index = 0;
			while (match.find()) {
				StyleRange range = getErrorWordRange(style, singleWord.getStart() + index, match.start() - index);
				ranges.add(range);
				index = match.end();
				hasTag = true;
			}
			
			if (hasTag) {
				if (index < singleWord.getLength()) {
					StyleRange range = getErrorWordRange(style, singleWord.getStart() + index,
							singleWord.getLength() - index);
					ranges.add(range);
				}
			}else {
				ranges.add(getErrorWordRange(style, singleWord.getStart(), singleWord.getLength()));
			}
		}
		refreshErrorWordsStyle(ranges);
		
	}
	
	/**
	 * 刷新拼写检查中错误单词的样式
	 * @param ranges
	 */
	public void refreshErrorWordsStyle(List<StyleRange> ranges){
		StyledText styledText = cellEditor.viewer.getTextWidget();
		List<StyleRange> oldRangeList = new ArrayList<StyleRange>();
		for(StyleRange oldRange : styledText.getStyleRanges()){
			if (oldRange.underlineStyle != SWT.UNDERLINE_ERROR) {
				oldRangeList.add(oldRange);
			}
		}
		styledText.setStyleRange(null);

		styledText.setStyleRanges(oldRangeList.toArray(new StyleRange[oldRangeList.size()]));
		if (ranges != null) {
			for (StyleRange range : ranges) {
				styledText.setStyleRange(range);
			}
		}
	}
	
	/**
	 * 根据传入的相关参数获取错误单词的样式	robert	2013-01-22
	 * @param style
	 * @param start
	 * @param length
	 * @return
	 */
	private StyleRange getErrorWordRange(TextStyle style, int start, int length){
		StyleRange range = new StyleRange(style);
		range.start = start;
		range.length = length;
		range.underline = true;
		range.underlineStyle = SWT.UNDERLINE_ERROR;
		range.underlineColor = ColorConfigBean.getInstance().getErrorWordColor();
		return range;
	}

	/** @return the editorBounds */
	public Rectangle getEditorBounds() {
		return editorBounds;
	}

	public Point computeSize(){
		StyledText textControl = cellEditor.getSegmentViewer().getTextWidget();
		Rectangle controlBounds = textControl.getBounds();
		Point x = textControl.computeSize(controlBounds.width, SWT.DEFAULT, true);
		return x;
	}
	
	/**
	 * Set the editor bounds
	 * @param editorBounds
	 * @param isApply
	 *            is apply now;
	 */
	public void setEditorBounds(Rectangle editorBounds, boolean isApply) {
		this.editorBounds = editorBounds;
		if (isApply && this.activeCellEditorControl != null && !this.activeCellEditorControl.isDisposed()) {
			this.activeCellEditorControl.setBounds(editorBounds);
		}
	}

	/** @return the type */
	public String getType() {
		return type;
	}

	/** @return the cellEditor */
	public StyledTextCellEditor getCellEditor() {
		return cellEditor;
	}

	/** @return the activeCellEditorControl */
	public Control getActiveCellEditorControl() {
		return activeCellEditorControl;
	}

	/** @return the editHandler */
	public ICellEditHandler getEditHandler() {
		return editHandler;
	}

	/** @return the columnPosition */
	public int getColumnPosition() {
		return columnPosition;
	}

	/** @return the rowPosition */
	public int getRowPosition() {
		return rowPosition;
	}

	/** @return the columnIndex */
	public int getColumnIndex() {
		return columnIndex;
	}

	/** @return the rowIndex */
	public int getRowIndex() {
		return rowIndex;
	}

	/** @return the cellStyle */
	public IStyle getCellStyle() {
		return cellStyle;
	}

	/** @return the originalCanonicalValue */
	public Object getOriginalCanonicalValue() {
		return originalCanonicalValue;
	}

	/**
	 * @param columnPosition
	 *            the columnPosition to set
	 */
	public void setColumnPosition(int columnPosition) {
		this.columnPosition = columnPosition;
	}

	/**
	 * @param rowPosition
	 *            the rowPosition to set
	 */
	public void setRowPosition(int rowPosition) {
		this.rowPosition = rowPosition;
	}

	private List<StyleRange> calculateTermsStyleRange(char[] source, char[] target, TextStyle style) {
		int sourceOffset = 0;
		int sourceCount = source.length;
		int targetOffset = 0, targetCount = target.length;

		char first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);
		List<StyleRange> rangeList = new ArrayList<StyleRange>();
		for (int i = sourceOffset; i <= max; i++) {
			/* Look for first character. */
			if (source[i] != first) {
				while (++i <= max && source[i] != first)
					;
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				List<StyleRange> tempList = new ArrayList<StyleRange>();
				int start = i;
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end; j++, k++) {
					Matcher matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(source[j] + "");
					if (matcher.matches()) {
						StyleRange range = new StyleRange(style);
						range.start = start;
						range.length = j - start;
						start = j + 1;
						k--;
						end++;
						if (end > sourceCount) {
							break;
						}
						tempList.add(range);
						continue;
					}
					if (source[j] != target[k]) {
						break;
					}
				}

				if (j == end) {
					/* Found whole string. */
					StyleRange range = new StyleRange(style);
					range.start = start;
					range.length = j - start;
					rangeList.addAll(tempList);
					rangeList.add(range);
				}
			}
		}
		return rangeList;
	}
}
