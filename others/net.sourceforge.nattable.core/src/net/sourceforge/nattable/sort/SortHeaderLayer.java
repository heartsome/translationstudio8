package net.sourceforge.nattable.sort;

import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.persistence.IPersistable;
import net.sourceforge.nattable.sort.command.SortCommandHandler;
import net.sourceforge.nattable.sort.config.DefaultSortConfiguration;

/**
 * Enables sorting of the data. Uses an {@link ISortModel} to do/track the sorting.
 * @param <T> Type of the Beans in the backing data source.
 *
 * @see DefaultSortConfiguration
 * @see SortStatePersistor
 */
public class SortHeaderLayer<T> extends AbstractLayerTransform implements IPersistable {

	/** Handles the actual sorting of underlying data */
	private final ISortModel sortModel;

	public SortHeaderLayer(ILayer underlyingLayer, ISortModel sortModel) {
		this(underlyingLayer, sortModel, true);
	}

	public SortHeaderLayer(ILayer underlyingLayer, ISortModel sortModel, boolean useDefaultConfiguration) {
		super(underlyingLayer);
		this.sortModel = sortModel;
		
		registerPersistable(new SortStatePersistor<T>(this));
		registerCommandHandler(new SortCommandHandler<T>(sortModel, this));

		if (useDefaultConfiguration) {
			addConfiguration(new DefaultSortConfiguration());
		}
	}

	/**
	 * @return adds a special configuration label to the stack taking into account the following:<br/>
	 * 	<ol>
	 * 		<li>Is the column sorted ?</li>
	 * 		<li>What is the sort order of the column</li>
	 * 	</ol>
	 * A special painter is registered against the above labels to render the sort arrows
	 */
	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack configLabels = super.getConfigLabelsByPosition(columnPosition, rowPosition);

		if (sortModel != null) {
			int columnIndex = getColumnIndexByPosition(columnPosition);
			if (sortModel.isColumnIndexSorted(columnIndex)) {
				SortDirectionEnum sortDirection = sortModel.getSortDirection(columnIndex);

				switch (sortDirection) {
				case ASC:
					configLabels.addLabel(DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
					break;
				case DESC:
					configLabels.addLabel(DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
					break;
				}
				String sortConfig = DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + sortModel.getSortOrder(columnIndex);
				configLabels.addLabel(sortConfig);
			}
		}
		return configLabels;
	}
	
	protected ISortModel getSortModel() {
		return sortModel;
	}
}