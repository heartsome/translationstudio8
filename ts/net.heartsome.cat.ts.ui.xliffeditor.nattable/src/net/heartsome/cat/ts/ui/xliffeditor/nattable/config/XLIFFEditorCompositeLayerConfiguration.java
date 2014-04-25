package net.heartsome.cat.ts.ui.xliffeditor.nattable.config;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.actions.KeyEditAction;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.actions.MouseEditAction;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.config.AggregateConfiguration;
import net.sourceforge.nattable.edit.config.DefaultEditBindings;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import net.sourceforge.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.IKeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/**
 * 基于 NatTable 的 xliff 编辑中 Grid Layer 的默认 configuration，去除一些不必要 configuration，如单击可编辑单元格就进入编辑状态。
 * @author cheney
 * @since JDK1.5
 */
public class XLIFFEditorCompositeLayerConfiguration extends AggregateConfiguration {
	private IStyle oddStyle;
	private IStyle evenStyle;
	private XLIFFEditorImplWithNatTable xliffEditor;

	/**
	 * Grid Layer 的默认 configuration
	 * @param gridLayer
	 *            Grid Layer 对象
	 */
	public XLIFFEditorCompositeLayerConfiguration(CompositeLayer compositeLayer, IStyle oddStyle, IStyle evenStyle,
			XLIFFEditorImplWithNatTable xliffEditor) {
		this.xliffEditor = xliffEditor;
		this.oddStyle = oddStyle;
		this.evenStyle = evenStyle;
		addAlternateRowColoringConfig(compositeLayer);
		addEditingHandlerConfig();
		addEditingUIConfig();
	}

	/**
	 * 激活可编辑单元格的 UI 层配置 ;
	 */
	protected void addEditingUIConfig() {
		addConfiguration(new DefaultEditBindings() {

			KeyEditAction action = new KeyEditAction();

			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				// 在用户点击 Enter 键时，进入编辑状态
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.CR), action);
				// 在用户点击小键盘的 Enter 键时，进入编辑状态
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.KEYPAD_CR), action);
//				// 在用户点击 F2 时，进入编辑状态
//				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.F2), action);
				// 在用户点击 whitespace 键时，进入编辑状态
				uiBindingRegistry.registerKeyBinding(new IKeyEventMatcher() {
					public boolean matches(KeyEvent event) {
						return event.character == ' ';
					}
				}, action);
				// 在用户输入字母或数字时，进入编辑状态
				uiBindingRegistry.registerKeyBinding(new LetterOrDigitKeyEventMatcher(), action);
				// 在用户单击时，进入编辑状态
				uiBindingRegistry.registerFirstMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new MouseEditAction());
				uiBindingRegistry.unregisterMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.NONE));
			}
		});
	}

	/**
	 * Editing Handler 配置 ;
	 */
	protected void addEditingHandlerConfig() {
		addConfiguration(new XLIFFEditorEditConfiguration(xliffEditor));
	}

	/**
	 * 表格中的每行数据以不同的颜色轮流显示的配置
	 * @param gridLayer
	 *            Grid Layer 对象 ;
	 */
	protected void addAlternateRowColoringConfig(CompositeLayer compositeLayer) {
		DefaultRowStyleConfiguration configuration = new DefaultRowStyleConfiguration();
		configuration.oddRowBgColor = oddStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		configuration.evenRowBgColor = evenStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		addConfiguration(configuration);
		compositeLayer.setConfigLabelAccumulatorForRegion(GridRegion.BODY, new AlternatingRowConfigLabelAccumulator());
	}
}
