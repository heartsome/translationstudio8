package net.sourceforge.nattable.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

public class ColumnGroupUtils {

	public static MoveDirectionEnum getMoveDirection(int fromColumnPosition, int toColumnPosition) {
		if (fromColumnPosition > toColumnPosition){
			return MoveDirectionEnum.LEFT;
		} else if(fromColumnPosition < toColumnPosition){
			return MoveDirectionEnum.RIGHT;
		} else {
			return MoveDirectionEnum.NONE;
		}
	}

	public static boolean isInTheSameGroup(int fromColumnIndex, int toColumnIndex, ColumnGroupModel model) {
		String fromColumnGroupName = model.getColumnGroupNameForIndex(fromColumnIndex);
		String toColumnGroupName = model.getColumnGroupNameForIndex(toColumnIndex);

		return fromColumnGroupName != null
			   	&& toColumnGroupName != null
			   	&& fromColumnGroupName.equals(toColumnGroupName);
	}

	public static boolean isFirstVisibleColumnIndexInGroup(int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model){
		if (isColumnIndexHiddenInUnderLyingLayer(columnIndex, layer, underlyingLayer)) {
			return false;
		}

		int columnPosition = underlyingLayer.getColumnPositionByIndex(columnIndex);
		List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(columnIndex);
		List<Integer> previousVisibleColumnIndexes = new ArrayList<Integer>();

		//All other indexes in the column group which are visible and
		//are positioned before me
		for (Integer currentIndex : columnIndexesInGroup) {
			int currentPosition = underlyingLayer.getColumnPositionByIndex(currentIndex.intValue());
			if(!isColumnIndexHiddenInUnderLyingLayer(currentIndex.intValue(), layer, underlyingLayer)
					&& currentPosition < columnPosition){
				previousVisibleColumnIndexes.add(currentIndex);
			}
		}

		return previousVisibleColumnIndexes.isEmpty();
	}

	public static boolean isLastVisibleColumnIndexInGroup(int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
		if (isColumnIndexHiddenInUnderLyingLayer(columnIndex, layer, underlyingLayer)) {
			return false;
		}

		List<Integer> visibleIndexesToTheRight = getVisibleIndexesToTheRight(columnIndex, layer, underlyingLayer, model);
		return visibleIndexesToTheRight.size() == 1 && visibleIndexesToTheRight.get(0).intValue() == columnIndex;
	}

	/**
	 * Inclusive of the columnIndex passed as the parameter.
	 */
	public static List<Integer> getVisibleIndexesToTheRight(int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model){
		if(model.isCollapsed(columnIndex)){
			return Collections.emptyList();
		}

		List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(columnIndex);
		int columnPosition = underlyingLayer.getColumnPositionByIndex(columnIndex);
		List<Integer> visibleColumnIndexesOnRight = new ArrayList<Integer>();

		for (Integer currentIndex : columnIndexesInGroup) {
			int currentPosition = underlyingLayer.getColumnPositionByIndex(currentIndex.intValue());
			if(!isColumnIndexHiddenInUnderLyingLayer(currentIndex.intValue(), layer, underlyingLayer)
					&& currentPosition >= columnPosition){
				visibleColumnIndexesOnRight.add(currentIndex);
			}
		}

		return visibleColumnIndexesOnRight;
	}

	public static boolean isColumnIndexHiddenInUnderLyingLayer(int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer) {
		return underlyingLayer.getColumnPositionByIndex(columnIndex) == -1;
	}

	public static boolean isColumnPositionHiddenInUnderLyingLayer(int columnPosition, ILayer layer, IUniqueIndexLayer underlyingLayer) {
		if (columnPosition < underlyingLayer.getColumnCount() && columnPosition >= 0) {
			int columnIndex = underlyingLayer.getColumnIndexByPosition(columnPosition);
			return isColumnIndexHiddenInUnderLyingLayer(columnIndex, layer, underlyingLayer);
		}
		return true;
	}

	/**
	 * @see ColumnGroupUtilsTest
	 * @return TRUE if the given column is the <i>right</i> most column in a group
	 */
	public static boolean isRightEdgeOfAColumnGroup(ILayer natLayer, int columnPosition, int columnIndex, ColumnGroupModel model) {
		int nextColumnPosition = columnPosition + 1;

		if (nextColumnPosition < natLayer.getColumnCount()) {
			int nextColumnIndex = natLayer.getColumnIndexByPosition(nextColumnPosition);
			if ((model.isPartOfAGroup(columnIndex) && !model.isPartOfAGroup(nextColumnIndex))) {
				return true;
			}
			if ((model.isPartOfAGroup(columnIndex) && model.isPartOfAGroup(nextColumnIndex))
					&& !ColumnGroupUtils.isInTheSameGroup(columnIndex, nextColumnIndex, model)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see ColumnGroupUtilsTest
	 * @return TRUE if the given column is the <i>left</i> most column in a group
	 */
	public static boolean isLeftEdgeOfAColumnGroup(ILayer natLayer, int columnPosition, int columnIndex, ColumnGroupModel model) {
		int previousColumnPosition = columnPosition - 1;

		// First column && in a group
		if(columnPosition == 0 && model.isPartOfAGroup(columnIndex)){
			return true;
		}

		if (previousColumnPosition >= 0) {
			int previousColumnIndex = natLayer.getColumnIndexByPosition(previousColumnPosition);
			if ((model.isPartOfAGroup(columnIndex) && !model.isPartOfAGroup(previousColumnIndex))) {
				return true;
			}
			if ((model.isPartOfAGroup(columnIndex) && model.isPartOfAGroup(previousColumnIndex))
					&& !ColumnGroupUtils.isInTheSameGroup(columnIndex, previousColumnIndex, model)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return TRUE if there is a column group boundary between startX and endX
	 */
	public static boolean isBetweenTwoGroups(ILayer natLayer, int startX, int endX, ColumnGroupModel model) {
		return !ColumnGroupUtils.isInTheSameGroup(
				natLayer.getColumnIndexByPosition(natLayer.getColumnPositionByX(startX)),
				natLayer.getColumnIndexByPosition(natLayer.getColumnPositionByX(endX)),
				model);
	}
}
