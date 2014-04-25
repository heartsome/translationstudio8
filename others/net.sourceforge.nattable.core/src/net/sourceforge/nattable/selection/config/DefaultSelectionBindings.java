package net.sourceforge.nattable.selection.config;

import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.copy.action.CopyDataAction;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.action.CellSelectionDragMode;
import net.sourceforge.nattable.selection.action.MoveSelectionAction;
import net.sourceforge.nattable.selection.action.MoveToFirstColumnAction;
import net.sourceforge.nattable.selection.action.MoveToFirstRowAction;
import net.sourceforge.nattable.selection.action.MoveToLastColumnAction;
import net.sourceforge.nattable.selection.action.MoveToLastRowAction;
import net.sourceforge.nattable.selection.action.PageDownAction;
import net.sourceforge.nattable.selection.action.PageUpAction;
import net.sourceforge.nattable.selection.action.SelectAllAction;
import net.sourceforge.nattable.selection.action.SelectCellAction;
import net.sourceforge.nattable.ui.action.IKeyAction;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.viewport.action.ViewportSelectColumnAction;
import net.sourceforge.nattable.viewport.action.ViewportSelectRowAction;

import org.eclipse.swt.SWT;

public class DefaultSelectionBindings extends AbstractUiBindingConfiguration {

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		// Move up
		configureMoveUpBindings(uiBindingRegistry, new MoveSelectionAction(MoveDirectionEnum.UP));

		// Move down
		configureMoveDownBindings(uiBindingRegistry, new MoveSelectionAction(MoveDirectionEnum.DOWN));

		// Move left
		configureMoveLeftBindings(uiBindingRegistry, new MoveSelectionAction(MoveDirectionEnum.LEFT));

		// Move right
		configureMoveRightBindings(uiBindingRegistry, new MoveSelectionAction(MoveDirectionEnum.RIGHT));

		// Page Up
		configurePageUpButtonBindings(uiBindingRegistry, new PageUpAction());

		// Page down
		configurePageDownButtonBindings(uiBindingRegistry, new PageDownAction());

		// Home - Move to first column
		configureHomeButtonBindings(uiBindingRegistry, new MoveToFirstColumnAction());

		// End - Move to last column
		configureEndButtonBindings(uiBindingRegistry, new MoveToLastColumnAction());

		// Select all
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, 'a'), new SelectAllAction());

		// Copy
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, 'c'), new CopyDataAction());

		// Mouse bindings - select Cell
		configureBodyMouseClickBindings(uiBindingRegistry);

		// Mouse bindings - select columns
		configureColumnHeaderMouseClickBindings(uiBindingRegistry);

		// Mouse bindings - select rows
		configureRowHeaderMouseClickBindings(uiBindingRegistry);

		// Mouse bindings - Drag
		configureBodyMouseDragMode(uiBindingRegistry);
	}

	// *** pg. up, pg. down, home, end keys selection bindings ***

	protected void configureEndButtonBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.END), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.END), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.END), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.END), action);
	}

	protected void configureHomeButtonBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.HOME), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.HOME), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.HOME), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.HOME), action);
	}

	protected void configurePageDownButtonBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.PAGE_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.PAGE_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.PAGE_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.PAGE_DOWN), action);
	}

	protected void configurePageUpButtonBindings(UiBindingRegistry uiBindingRegistry, PageUpAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.PAGE_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.PAGE_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.PAGE_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.PAGE_UP), action);
	}

	// *** Arrow keys selection bindings ***

	protected void configureMoveRightBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_RIGHT), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_RIGHT), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.ARROW_RIGHT), new MoveToLastColumnAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.ARROW_RIGHT), new MoveToLastColumnAction());

		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.TAB), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.TAB), action);
	}

	protected void configureMoveLeftBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_LEFT), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_LEFT), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.ARROW_LEFT), new MoveToFirstColumnAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.ARROW_LEFT), new MoveToFirstColumnAction());

		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.TAB), new MoveSelectionAction(MoveDirectionEnum.LEFT, false, false));
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.TAB), action);
	}

	protected void configureMoveDownBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.ARROW_DOWN), new MoveToLastRowAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.ARROW_DOWN), new MoveToLastRowAction());

		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.CR), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.CR), action);
	}

	protected void configureMoveUpBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.ARROW_UP), new MoveToFirstRowAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.ARROW_UP), new MoveToFirstRowAction());

		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.CR), new MoveSelectionAction(MoveDirectionEnum.UP, false, false));
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.CR), action);
	}

	// *** Mouse bindings ***

	protected void configureBodyMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
		IMouseAction action = new SelectCellAction();
		uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), action);
		uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), action);
		uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.CTRL), action);
		uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.SHIFT | SWT.CONTROL), action);
	}

	protected void configureColumnHeaderMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.columnHeaderLeftClick(SWT.NONE), new ViewportSelectColumnAction(false, false));
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.columnHeaderLeftClick(SWT.SHIFT), new ViewportSelectColumnAction(true, false));
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.columnHeaderLeftClick(SWT.CONTROL), new ViewportSelectColumnAction(false, true));
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.columnHeaderLeftClick(SWT.SHIFT | SWT.CONTROL), new ViewportSelectColumnAction(true, true));
	}

	protected void configureRowHeaderMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.rowHeaderLeftClick(SWT.NONE), new ViewportSelectRowAction(false, false));
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.rowHeaderLeftClick(SWT.SHIFT), new ViewportSelectRowAction(true, false));
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.rowHeaderLeftClick(SWT.CONTROL), new ViewportSelectRowAction(false, true));
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.rowHeaderLeftClick(SWT.SHIFT | SWT.CONTROL), new ViewportSelectRowAction(true, true));
	}

	protected void configureBodyMouseDragMode(UiBindingRegistry uiBindingRegistry) {
		CellSelectionDragMode dragMode = new CellSelectionDragMode();
		uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.NONE), dragMode);
		uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), dragMode);
		uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.CONTROL), dragMode);
		uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT | SWT.CONTROL), dragMode);
	}

}
