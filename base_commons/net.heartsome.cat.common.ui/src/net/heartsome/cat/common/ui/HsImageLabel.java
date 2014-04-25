/**
 * ImageText.java
 *
 * Version information :
 *
 * Date:2012-6-13
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class HsImageLabel {

	private Control control;
	private Composite body;
	private Label imageLabel;
	private Label descriptionLabel;

	private ImageDescriptor imageDescription;
	private String description;
	private Image image;

	private Point size;

	public HsImageLabel(String description, ImageDescriptor imageDescription) {
		this.description = description;
		this.imageDescription = imageDescription;
		if (imageDescription != null) {
			image = imageDescription.createImage();
		}
	}

	public Composite createControl(Composite parent) {
		parent.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout contentLayout = new GridLayout(2, false);
		contentLayout.marginWidth = 0;
		contentLayout.marginHeight = 0;
		content.setLayout(contentLayout);
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.control = content;

		imageLabel = createImageLabel(content);
		if (imageLabel != null) {
			imageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		}

		Composite composite = new Composite(content, SWT.NONE);
		GridLayout comLayout = new GridLayout();
		comLayout.marginWidth = 0;
		comLayout.marginHeight = 0;
		composite.setLayout(comLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		descriptionLabel = createDescriptionLabel(composite);
		if (descriptionLabel != null) {
			descriptionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		body = new Composite(composite, SWT.NONE);
		GridLayout gd = new GridLayout();
		gd.marginRight = 0;
		gd.marginHeight = 0;
		body.setLayout(gd);
		if (body != null) {
			body.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
		return body;
	}

	public Point computeSize() {
		return computeSize(130);
	}
	
	public Point computeSize(int width) {
		if (size != null) {
			return size;
		}
		Control control = getControl();
		if (control != null) {
			size = doComputeSize(width);
			return size;
		}
		return new Point(0, 0);
	}

	protected Point doComputeSize(int width) {
		Point bodySize = getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		if (descriptionLabel != null) {
			GridData gd = (GridData) descriptionLabel.getLayoutData();
			gd.widthHint = bodySize.x + width;
			descriptionLabel.setText(description);
		}
		return bodySize;
	}

	protected Label createDescriptionLabel(Composite parent) {
		Label result = null;
		String description = getDescription();
		if (description != null) {
			result = new Label(parent, SWT.WRAP);
			result.setFont(parent.getFont());
//			result.setText(description);
		}
		return result;
	}

	protected Label createImageLabel(Composite parent) {
		Label result = null;
		if (image != null) {
			result = new Label(parent, SWT.NONE);
			result.setFont(parent.getFont());
			result.setImage(image);
		}
		return result;
	}

	public void dispose() {
		// deallocate SWT resources
		if (image != null && !image.isDisposed()) {
			image.dispose();
			image = null;
		}
	}

	public Control getControl() {
		return control;
	}

	public ImageDescriptor getImageDescription() {
		return imageDescription;
	}

	public String getDescription() {
		return description;
	}

}
