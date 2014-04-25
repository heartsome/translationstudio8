package net.heartsome.cat.ts.ui.preferencepage.colors;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.bean.IColorPreferenceConstant;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * TS 应用中颜色设置的首选项页
 * @author cheney
 * @since JDK1.6
 */
public class ColorsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.ts.ui.preferencepage.colors.ColorsPreferencePage";
	private List<Image> imageList = new ArrayList<Image>();
	private List<Color> colorList = new ArrayList<Color>();

	/**
	 * 无参构造函数
	 */
	public ColorsPreferencePage() {
		super(GRID);
		// setTitle("Colors");
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		// setDescription("系统颜色配置");
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		Composite container = new Composite(parent, SWT.None);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group groupMatch = new Group(container, SWT.None);
		groupMatch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupMatch.setText(Messages.getString("colors.ColorsPreferencePage.groupMatch"));
		if (CommonFunction.checkEdition("U")) {
			addField(new ColorFieldEditor2(IColorPreferenceConstant.TM_MATCH101_COLOR,
					Messages.getString("colors.ColorsPreferencePage.match101"), groupMatch));
		}
		addField(new ColorFieldEditor2(IColorPreferenceConstant.TM_MATCH100_COLOR,
				Messages.getString("colors.ColorsPreferencePage.match100"), groupMatch));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.TM_MATCH90_COLOR,
				Messages.getString("colors.ColorsPreferencePage.match90"), groupMatch));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.TM_MATCH80_COLOR,
				Messages.getString("colors.ColorsPreferencePage.match80"), groupMatch));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.TM_MATCH70_COLOR,
				Messages.getString("colors.ColorsPreferencePage.match70"), groupMatch));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.TM_MATCH0_COLOR,
				Messages.getString("colors.ColorsPreferencePage.match0"), groupMatch));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.QT_COLOR,
				Messages.getString("colors.ColorsPreferencePage.QT"), groupMatch));
		groupMatch.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		if (!CommonFunction.checkEdition("L")) {
			Group groupTerm = new Group(container, SWT.None);
			groupTerm.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			groupTerm.setText(Messages.getString("colors.ColorsPreferencePage.groupTerms"));
			addField(new ColorFieldEditor2(IColorPreferenceConstant.HIGHLIGHTED_TERM_COLOR,
					Messages.getString("colors.ColorsPreferencePage.highlightedTermColor"), groupTerm));
			groupTerm.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		}

		Group groupTag = new Group(container, SWT.None);
		groupTag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupTag.setText(Messages.getString("colors.ColorsPreferencePage.groupTag"));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.TAG_FG_COLOR,
				Messages.getString("colors.ColorsPreferencePage.tagForground"), groupTag));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.TAG_BG_COLOR,
				Messages.getString("colors.ColorsPreferencePage.tagBackground"), groupTag));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.WRONG_TAG_COLOR,
				Messages.getString("colors.ColorsPreferencePage.wrongTagColor"), groupTag));
		groupTag.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		Group groupSource = new Group(container, SWT.None);
		groupSource.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupSource.setText(Messages.getString("colors.ColorsPreferencePage.groupSource"));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.DIFFERENCE_BG_COLOR,
				Messages.getString("colors.ColorsPreferencePage.SourceForground"), groupSource));
		addField(new ColorFieldEditor2(IColorPreferenceConstant.DIFFERENCE_FG_COLOR,
				Messages.getString("colors.ColorsPreferencePage.SourceBackground"), groupSource));
		groupSource.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}

	public void init(IWorkbench workbench) {

	}

	private class ColorFieldEditor2 extends ColorFieldEditor {

		Button colorButton;
		Point fExtent;

		public ColorFieldEditor2(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		protected void doFillIntoGrid(Composite parent, int numColumns) {
			Control control = getLabelControl(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = numColumns - 1;
			control.setLayoutData(gd);

			colorButton = getChangeControl(parent);
			GridData dataBtn = new GridData();
			dataBtn.widthHint = 100;
			colorButton.setLayoutData(dataBtn);
			fExtent = computeImageSize(parent);
			colorButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					updateColorImage();
				}
			});
		}

		@Override
		public void load() {
			super.load();
			updateColorImage();
		}

		@Override
		public void loadDefault() {
			super.loadDefault();
			updateColorImage();
		}

		protected void updateColorImage() {
			Display display = colorButton.getDisplay();
			Image i = new Image(colorButton.getDisplay(), fExtent.x + 30, fExtent.y);
			imageList.add(i);
			GC gc = new GC(i);
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.drawRectangle(0, 2, fExtent.x + 28, fExtent.y - 3);
			if (colorButton.getBackground() != null) {
				colorButton.getBackground().dispose();
			}
			Color c = new Color(display, getColorSelector().getColorValue());
			colorList.add(c);
			gc.setBackground(c);
			gc.fillRectangle(0, 2, fExtent.x + 32, fExtent.y + 5);
			gc.dispose();
			colorButton.setImage(i);
		}

	}

	@Override
	public void dispose() {
		for (Image img : imageList) {
			if (img != null && !img.isDisposed()) {
				img.dispose();
			}
		}
		imageList.clear();
		for (Color c : colorList) {
			if (c != null && !c.isDisposed()) {
				c.dispose();
			}
		}
		colorList.clear();
		super.dispose();
	}
}
