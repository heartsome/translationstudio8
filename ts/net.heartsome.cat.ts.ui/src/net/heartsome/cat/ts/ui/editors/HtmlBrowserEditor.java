package net.heartsome.cat.ts.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * html浏览器
 * @author  robert	2012-04-07
 * @version 
 * @since   JDK1.6
 */
public class HtmlBrowserEditor extends EditorPart {
	/** 事件监听器提供者，任务编辑器通知监听器的人物。 */
	ISelectionProvider provider = new SelectionProviderAdapter();
	/** 编辑器左上角的图标 */
	private Image titleImage;
	private Browser browser;
	private String htmlUrl;
	private Composite cmp;
	
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		
		Image oldTitleImage = titleImage;
		if (input != null) {
			IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor editorDesc = editorRegistry.findEditor(getSite().getId());
			ImageDescriptor imageDesc = editorDesc != null ? editorDesc.getImageDescriptor() : null;
			titleImage = imageDesc != null ? imageDesc.createImage() : null;
		}

		setTitleImage(titleImage);
		if (oldTitleImage != null && !oldTitleImage.isDisposed()) {
			oldTitleImage.dispose();
		}

		FileEditorInput fileInput = (FileEditorInput) input;
		htmlUrl = fileInput.getFile().getLocation().toOSString();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		cmp = new Composite(parent, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(cmp);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(cmp);
		
		browser = new Browser(cmp, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		browser.setUrl(htmlUrl);

		browser.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				getSite().getPart().setFocus();
				super.mouseDown(e);
			}
		});
	}

	@Override
	public void setFocus() {
		cmp.setFocus();
	}
/*
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		provider.addSelectionChangedListener(listener);
	}

	public ISelection getSelection() {
		return provider.getSelection();
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		provider.removeSelectionChangedListener(listener);
	}

	public void setSelection(ISelection selection) {
		provider.setSelection(selection);
	}*/
	
	@Override
	public void dispose() {
		if(titleImage != null && !titleImage.isDisposed()){
			titleImage.dispose();
		}
		super.dispose();
	}

}
