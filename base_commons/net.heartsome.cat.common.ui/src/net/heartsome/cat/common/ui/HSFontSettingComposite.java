/**
 * HSFontSettingComposite.java
 *
 * Version information :
 *
 * Date:2013-4-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.heartsome.cat.common.ui.resource.Messages;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * 字体设置组件
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class HSFontSettingComposite extends Composite {

	// 界面组件
	ComboViewer fontNameComboViewer;
	ComboViewer fontSizeComboViewer;
	Label previewFontText;
	Font previewFont;
	String title;

	/**
	 * @param parent
	 * @param style
	 * @param title
	 *            此字体设计说明文字，如果为null,则不创建。
	 */
	public HSFontSettingComposite(Composite parent, int style, String title) {
		super(parent, style);
		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginWidth = 1;
		this.title = title;
		setLayout(layout);
		createContent();
	}

	public FontData[] getFontSetingFont() {
		return previewFont.getFontData();
	}

	public void initFont(String name, int size){
		fontNameComboViewer.setSelection(new StructuredSelection(name));
		fontSizeComboViewer.setSelection(new StructuredSelection(size));
		
		FontData fd = new FontData();
		fd.setName(name);
		fd.setHeight(size);
		if (previewFont != null && !previewFont.isDisposed()) {
			previewFont.dispose();
		}
		previewFont = new Font(Display.getDefault(), fd);
		previewFontText.setFont(previewFont);
	}
	
	@Override
	public void dispose() {
		if (previewFont != null && !previewFont.isDisposed()) {
			previewFont.dispose();
		}
		super.dispose();
	}

	public void createContent() {
		if (this.title != null && title.length() != 0) {
			Label titleLabel = new Label(this, SWT.NONE);
			titleLabel.setText(title);
			GridData titleLabelGridData = new GridData();
			titleLabelGridData.horizontalSpan = 2;
			titleLabelGridData.horizontalAlignment = GridData.CENTER;
			titleLabel.setLayoutData(titleLabelGridData);
		}
		Label lblFontName = new Label(this, SWT.NONE);
		lblFontName.setText(Messages.getString("HSFontSettingComposite.fontNameLabel"));
		fontNameComboViewer = new ComboViewer(this);
		fontNameComboViewer.setContentProvider(new ArrayContentProvider());
		Combo fontNameCombo = fontNameComboViewer.getCombo();
		GridData fnGd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		fontNameCombo.setLayoutData(fnGd);
		fontNameComboViewer.setInput(getSystemFonts());

		Label lblFontSize = new Label(this, SWT.NONE);
		lblFontSize.setText(Messages.getString("HSFontSettingComposite.fontSizeLabel"));
		fontSizeComboViewer = new ComboViewer(this);
		fontSizeComboViewer.setContentProvider(new ArrayContentProvider());
		Combo fontSize = fontSizeComboViewer.getCombo();
		fontSize.setLayoutData(fnGd);
		fontSizeComboViewer.setInput(new Integer[] { 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
				26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, });

		Label lblFontPreview = new Label(this, SWT.None);
		lblFontPreview.setText(Messages.getString("HSFontSettingComposite.fontPreViewLabel"));

		previewFontText = new Label(this, SWT.READ_ONLY | SWT.BORDER);
		previewFontText.setText("abcdefg ABCDEFG");
		GridData previewFontTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		previewFontTextGridData.heightHint = 50;
		previewFontText.setLayoutData(previewFontTextGridData);

		fontNameComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				String strFontName = fontNameComboViewer.getCombo().getText();
				String intFontSize = fontSizeComboViewer.getCombo().getText();
				if(strFontName == null || intFontSize == null || strFontName.length() == 0 || intFontSize.length() == 0){
					return;
				}
				FontData fd = JFaceResources.getDefaultFont().getFontData()[0];
				fd.setName(strFontName);
				fd.setHeight(Integer.parseInt(intFontSize));
				if (previewFont != null && !previewFont.isDisposed()) {
					previewFont.dispose();
				}
				previewFont = new Font(Display.getDefault(), fd);
				previewFontText.setFont(previewFont);
			}
		});
		fontSizeComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				String strFontName = fontNameComboViewer.getCombo().getText();
				String intFontSize = fontSizeComboViewer.getCombo().getText();
				if(strFontName == null || intFontSize == null || strFontName.length() == 0 || intFontSize.length() == 0){
					return;
				}
				FontData fd = JFaceResources.getDefaultFont().getFontData()[0];
				fd.setName(strFontName);
				fd.setHeight(Integer.parseInt(intFontSize));
				if (previewFont != null && !previewFont.isDisposed()) {
					previewFont.dispose();
				}
				previewFont = new Font(Display.getDefault(), fd);
				previewFontText.setFont(previewFont);
			}
		});
	}

	private String[] getSystemFonts() {
		Set<String> s = new HashSet<String>();
		// Add names of all bitmap fonts.
		FontData[] fds = Display.getDefault().getFontList(null, false);
		for (int i = 0; i < fds.length; ++i) {
			s.add(fds[i].getName());
		}
		// Add names of all scalable fonts.
		fds = Display.getDefault().getFontList(null, true);
		for (int i = 0; i < fds.length; ++i) {
			s.add(fds[i].getName());
		}
		// Sort the result and print it.
		String[] fontNames = new String[s.size()];
		s.toArray(fontNames);
		Arrays.sort(fontNames); // sort
		return fontNames;
	}

}
