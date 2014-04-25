/**
 * XLIFFEditorStatusLineItemWithProgressBar.java
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
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class XLIFFEditorStatusLineItemWithProgressBar extends XLIFFEditorStatusLineItem {

//	private ProgressBar progressBar;
	private int progressValue;
	private Label label;
//	private String defaultMessage;

	public XLIFFEditorStatusLineItemWithProgressBar(String id, String defaultMessage) {
		super(id, defaultMessage);
//		this.defaultMessage = defaultMessage;
	}

	public void fill(Composite parent) {
		super.fill(parent);
		Composite container = new Composite(parent, SWT.NONE);
		
		GridLayout gl = new GridLayout(1, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.marginTop = 0;
		gl.marginRight = 0;
		gl.marginBottom = 0;
		container.setLayout(gl);

//		progressBar = new ProgressBar(container, SWT.SMOOTH);
//		GridData gdPprogressBar = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
//		gdPprogressBar.heightHint = 16;
//		gdPprogressBar.widthHint = 130;
//		progressBar.setLayoutData(gdPprogressBar);
//		progressBar.setMinimum(0); // 最小值
//		progressBar.setMaximum(100);// 最大值
//		progressBar.setSelection(progressValue);
//		progressBar.setToolTipText(defaultMessage);

		GC gc = new GC(statusLine);
		int widthHint = gc.textExtent("100%").x;
//		int height = gc.textExtent("100%").y;
		gc.dispose();
		
		label = new Label(container, SWT.NONE);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		gd.widthHint = widthHint;
		label.setLayoutData(gd);
		label.setText(progressValue + "%");
		
		StatusLineLayoutData data = new StatusLineLayoutData();
		container.setLayoutData(data);
	}

	public void setProgressValue(int value) {
		Assert.isLegal(value >= 0 && value <= 100);
		this.progressValue = value;
		if (label != null && !label.isDisposed()) {
			label.setText(value + "%");
//			progressBar.setSelection(value);
			label.getParent().layout();
		}
	}
}
