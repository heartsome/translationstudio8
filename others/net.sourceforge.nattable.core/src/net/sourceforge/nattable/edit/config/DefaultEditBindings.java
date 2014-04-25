package net.sourceforge.nattable.edit.config;

import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.edit.action.CellEditDragMode;
import net.sourceforge.nattable.edit.action.KeyEditAction;
import net.sourceforge.nattable.edit.action.MouseEditAction;
import net.sourceforge.nattable.edit.editor.CheckBoxCellEditor;
import net.sourceforge.nattable.edit.editor.ComboBoxCellEditor;
import net.sourceforge.nattable.edit.editor.TextCellEditor;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.BodyCellEditorMouseEventMatcher;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;

import org.eclipse.swt.SWT;

public class DefaultEditBindings extends AbstractUiBindingConfiguration {

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.F2), new KeyEditAction());
		uiBindingRegistry.registerKeyBinding(new LetterOrDigitKeyEventMatcher(), new KeyEditAction());
		
		uiBindingRegistry.registerFirstSingleClickBinding(
				new BodyCellEditorMouseEventMatcher(TextCellEditor.class),
				new MouseEditAction());
		
		uiBindingRegistry.registerFirstMouseDragMode(
				new BodyCellEditorMouseEventMatcher(TextCellEditor.class),
				new CellEditDragMode());

		uiBindingRegistry.registerFirstSingleClickBinding(
                new BodyCellEditorMouseEventMatcher(CheckBoxCellEditor.class),
                new MouseEditAction());
		
		uiBindingRegistry.registerFirstMouseDragMode(
				new BodyCellEditorMouseEventMatcher(CheckBoxCellEditor.class),
				new CellEditDragMode());

		uiBindingRegistry.registerFirstSingleClickBinding(
				new BodyCellEditorMouseEventMatcher(ComboBoxCellEditor.class), 
				new MouseEditAction());
		
		uiBindingRegistry.registerFirstMouseDragMode(
				new BodyCellEditorMouseEventMatcher(ComboBoxCellEditor.class),
				new CellEditDragMode());
	}

}
