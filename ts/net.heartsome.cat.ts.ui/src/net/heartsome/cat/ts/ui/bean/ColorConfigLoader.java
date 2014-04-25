/**
 * ViewerColorBean.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.bean;

import net.heartsome.cat.common.bean.ColorConfigBean;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.view.IMatchViewPart;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 视图中的相关颜色封装
 * @author jason
 * @version
 * @since JDK1.6
 */
public class ColorConfigLoader implements IPropertyChangeListener {

	private IPreferenceStore pfStore;

	private ColorConfigBean colorConfigBean;

	private static ColorConfigLoader instance;

	/** 当前颜色值的集合 */
	private String strCurColor;

	public static ColorConfigLoader init() {
		if (instance == null) {
			instance = new ColorConfigLoader();
		}
		return instance;
	}

	private ColorConfigLoader() {
		pfStore = Activator.getDefault().getPreferenceStore();
		pfStore.addPropertyChangeListener(this);
		colorConfigBean = ColorConfigBean.getInstance();
		initColor();
	}

	private void initColor() {
		Device device = Display.getDefault();
		if (pfStore == null) {
			pfStore = Activator.getDefault().getPreferenceStore();
			pfStore.addPropertyChangeListener(this);
		}
		colorConfigBean.release();
		colorConfigBean.setPtColor(getColor(device, IColorPreferenceConstant.PT_COLOR));
		colorConfigBean.setQtColor(getColor(device, IColorPreferenceConstant.QT_COLOR));
		colorConfigBean.setMtColor(getColor(device, IColorPreferenceConstant.MT_COLOR));
		colorConfigBean.setTm101Color(getColor(device, IColorPreferenceConstant.TM_MATCH101_COLOR));
		colorConfigBean.setTm100Color(getColor(device, IColorPreferenceConstant.TM_MATCH100_COLOR));
		colorConfigBean.setTm90Color(getColor(device, IColorPreferenceConstant.TM_MATCH90_COLOR));
		colorConfigBean.setTm80Color(getColor(device, IColorPreferenceConstant.TM_MATCH80_COLOR));
		colorConfigBean.setTm70Color(getColor(device, IColorPreferenceConstant.TM_MATCH70_COLOR));
		colorConfigBean.setTm0Color(getColor(device, IColorPreferenceConstant.TM_MATCH0_COLOR));

		colorConfigBean.setSrcDiffFgColor(getColor(device, IColorPreferenceConstant.DIFFERENCE_FG_COLOR));
		colorConfigBean.setSrcDiffBgColor(getColor(device, IColorPreferenceConstant.DIFFERENCE_BG_COLOR));

		colorConfigBean.setTagFgColor(getColor(device, IColorPreferenceConstant.TAG_FG_COLOR));
		colorConfigBean.setTagBgColor(getColor(device, IColorPreferenceConstant.TAG_BG_COLOR));
		colorConfigBean.setWrongTagColor(getColor(device, IColorPreferenceConstant.WRONG_TAG_COLOR));
		colorConfigBean.setErrorWordColor(getColor(device, IColorPreferenceConstant.WRONG_TAG_COLOR));

		Color highlightedTermColor = colorConfigBean.getHighlightedTermColor();
		RGB newRgb = StringConverter.asRGB(pfStore.getString(IColorPreferenceConstant.HIGHLIGHTED_TERM_COLOR));
		if (highlightedTermColor == null || highlightedTermColor.isDisposed()) {
			highlightedTermColor = new Color(device, newRgb);
		} else if (!highlightedTermColor.getRGB().equals(newRgb)) {
			highlightedTermColor.dispose();
			highlightedTermColor = new Color(device, newRgb);
			// had changed
		}
		colorConfigBean.setHighlightedTermColor(highlightedTermColor);
	}

	public void propertyChange(PropertyChangeEvent event) {
		loadColor();
	}

	private void loadColor() {
		Device device = Activator.getDefault().getWorkbench().getDisplay();
		if (pfStore == null) {
			pfStore = Activator.getDefault().getPreferenceStore();
			pfStore.addPropertyChangeListener(this);
		}
		colorConfigBean.release();
		colorConfigBean.setPtColor(getColor(device, IColorPreferenceConstant.PT_COLOR));
		colorConfigBean.setQtColor(getColor(device, IColorPreferenceConstant.QT_COLOR));
		colorConfigBean.setMtColor(getColor(device, IColorPreferenceConstant.MT_COLOR));
		colorConfigBean.setTm101Color(getColor(device, IColorPreferenceConstant.TM_MATCH101_COLOR));
		colorConfigBean.setTm100Color(getColor(device, IColorPreferenceConstant.TM_MATCH100_COLOR));
		colorConfigBean.setTm90Color(getColor(device, IColorPreferenceConstant.TM_MATCH90_COLOR));
		colorConfigBean.setTm80Color(getColor(device, IColorPreferenceConstant.TM_MATCH80_COLOR));
		colorConfigBean.setTm70Color(getColor(device, IColorPreferenceConstant.TM_MATCH70_COLOR));
		colorConfigBean.setTm0Color(getColor(device, IColorPreferenceConstant.TM_MATCH0_COLOR));

		colorConfigBean.setSrcDiffFgColor(getColor(device, IColorPreferenceConstant.DIFFERENCE_FG_COLOR));
		colorConfigBean.setSrcDiffBgColor(getColor(device, IColorPreferenceConstant.DIFFERENCE_BG_COLOR));

		colorConfigBean.setTagFgColor(getColor(device, IColorPreferenceConstant.TAG_FG_COLOR));
		colorConfigBean.setTagBgColor(getColor(device, IColorPreferenceConstant.TAG_BG_COLOR));
		colorConfigBean.setWrongTagColor(getColor(device, IColorPreferenceConstant.WRONG_TAG_COLOR));
		colorConfigBean.setErrorWordColor(getColor(device, IColorPreferenceConstant.WRONG_TAG_COLOR));
		String strColor = colorConfigBean.toString();

		IXliffEditor xliffEditor = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart editor = page.getActiveEditor();
				if (editor != null && editor instanceof IXliffEditor) {
					xliffEditor = (IXliffEditor) editor;
				}
			}
		}
		if (strCurColor != null && !strCurColor.equals(strColor)) {
			// 更改颜色设置后刷新界面
			if (xliffEditor != null) {
				xliffEditor.autoResize();
				xliffEditor.refresh();
			}
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IViewPart viewPart = page.findView("net.heartsome.cat.ts.ui.translation.view.matchview");
					if (viewPart != null) {
						IMatchViewPart part = (IMatchViewPart) viewPart;
						part.refreshTable();
					}
				}
			}
			strCurColor = strColor;
		}
		Color highlightedTermColor = colorConfigBean.getHighlightedTermColor();
		RGB newRgb = StringConverter.asRGB(pfStore.getString(IColorPreferenceConstant.HIGHLIGHTED_TERM_COLOR));
		if (highlightedTermColor == null || highlightedTermColor.isDisposed()) {
			highlightedTermColor = new Color(device, newRgb);
		} else if (!highlightedTermColor.getRGB().equals(newRgb)) {
			highlightedTermColor.dispose();
			highlightedTermColor = new Color(device, newRgb);
			// had changed
			if (xliffEditor != null) {
				xliffEditor.highlightedTerms(0, null); // 刷新
			}
		}
		colorConfigBean.setHighlightedTermColor(highlightedTermColor);
	}

	private Color getColor(Device device, String key) {
		String fg = pfStore.getString(key);
		return new Color(device, StringConverter.asRGB(fg));
	}
}
