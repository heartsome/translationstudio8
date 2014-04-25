package net.heartsome.cat.ts.ui.xliffeditor.nattable.config;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.config.AbstractLayerConfiguration;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IEditableRule;
import net.sourceforge.nattable.data.validate.DefaultDataValidator;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.edit.command.EditCellCommandHandler;
import net.sourceforge.nattable.layer.CompositeLayer;

public class XLIFFEditorEditConfiguration extends AbstractLayerConfiguration<CompositeLayer> {

//	private XLIFFEditorImplWithNatTable xliffEditor;

	public XLIFFEditorEditConfiguration(XLIFFEditorImplWithNatTable xliffEditor) {
//		this.xliffEditor = xliffEditor;
	}

	@Override
	public void configureTypedLayer(CompositeLayer compositeLayer) {
		compositeLayer.registerCommandHandler(new EditCellCommandHandler());
	}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE);
//		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new StyledTextCellEditor(xliffEditor));
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, new DefaultDataValidator());
	}

}
