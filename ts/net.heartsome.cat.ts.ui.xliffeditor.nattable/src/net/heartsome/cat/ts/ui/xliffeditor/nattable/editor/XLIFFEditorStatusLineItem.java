/**
 * XLIFFEditorStatusLineContributionItem.java
 *
 * Version information :
 *
 * Date:Mar 5, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class XLIFFEditorStatusLineItem extends ContributionItem {

	private Label label;
	private String text = "";
	private Image image;
	public Composite statusLine = null;

	public XLIFFEditorStatusLineItem(String id, String defaultMessage) {
		super(id);
		this.text = defaultMessage;
	}

	public void fill(Composite parent) {
		statusLine = parent;
		new Label(parent, SWT.SEPARATOR);
		
		Composite container = new Composite(parent, SWT.NONE);		
		GridLayout gl = new GridLayout(1, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.marginTop = 0;
		gl.marginRight = 0;
		gl.marginBottom = 0;
		container.setLayout(gl);
		
		label = new Label(container, SWT.SHADOW_NONE);	
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		label.setAlignment(SWT.CENTER);
		label.setText(text);
		if (image != null) {
			label.setImage(image);
			label.setToolTipText(text);
		}

//		Point preferredSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//		int widthHint = preferredSize.x;
//		int heightHint = preferredSize.y;
//		if (widthHint < 0) {
//			// Compute the size base on 'charWidth' average char widths
//			GC gc = new GC(statusLine);
//			gc.setFont(statusLine.getFont());
//			FontMetrics fm = gc.getFontMetrics();
//			widthHint = fm.getAverageCharWidth() * 40;
//			heightHint = fm.getHeight();
//			gc.dispose();
//		}
		
//		StatusLineLayoutData data = new StatusLineLayoutData();
//		data.widthHint = widthHint;
//		label.setLayoutData(data);
		
//		StatusLineLayoutData data = new StatusLineLayoutData();
//		data.heightHint = heightHint;
//		speLb.setLayoutData(data);
	}

	public void setText(String text) {
		Assert.isNotNull(text);
		this.text = LegacyActionTools.escapeMnemonics(text);
		if (label != null && !label.isDisposed()) {
			label.setText(this.text);
		}
		if (this.text.length() == 0) {
			if (isVisible()) {
				setVisible(false);
				IContributionManager contributionManager = getParent();

				if (contributionManager != null) {
					contributionManager.update(true);
				}
			}
		} else {
			if (!isVisible()) {
				setVisible(true);
				IContributionManager contributionManager = getParent();

				if (contributionManager != null) {
					contributionManager.update(true);
				}
			}
		}

	}

	public void setText(String text, Image image) {
		Assert.isNotNull(image);
		Assert.isNotNull(text);
		this.text = LegacyActionTools.escapeMnemonics(text);
		this.image = image;
		if (label != null && !label.isDisposed()) {
			label.setText(this.text);
			label.setImage(this.image);
		}

		if (this.text.length() == 0) {
			if (isVisible()) {
				setVisible(false);
				IContributionManager contributionManager = getParent();

				if (contributionManager != null) {
					contributionManager.update(true);
				}
			}
		} else {
			if (!isVisible()) {
				setVisible(true);
				IContributionManager contributionManager = getParent();

				if (contributionManager != null) {
					contributionManager.update(true);
				}
			}
		}
	}
	
	@Override
	public void dispose() {
		if(image != null && !image.isDisposed()){
			image.dispose();
		}
		super.dispose();
	}
}
