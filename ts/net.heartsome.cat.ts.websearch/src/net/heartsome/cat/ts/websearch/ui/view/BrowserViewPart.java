/**
 * BrowserViewPart.java
 *
 * Version information :
 *
 * Date:2013-9-16
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.websearch.ui.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.heartsome.cat.ts.ui.util.PreferenceUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.websearch.bean.SearchEntry;
import net.heartsome.cat.ts.websearch.config.WebSearchPreferencStore;
import net.heartsome.cat.ts.websearch.resource.Messages;
import net.heartsome.cat.ts.websearch.ui.browser.BrowserTab;
import net.heartsome.cat.ts.websearch.ui.preference.WebSearchPreferencePage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Administrator
 * @version
 * @since JDK1.6
 */
public class BrowserViewPart extends ViewPart {

	public static final String ID = "net.heartsome.cat.ts.websearch.ui.view.BrowserViewPart"; //$NON-NLS-1$

	private List<SearchEntry> urls;

	private BrowserTab[] browserTabs;

	private Text keyWordForSearch;

	private CTabFolder tabFolder;

	private PropertyChangeListener urlChangelistener = new ProperChangeListener();

	private ResfreshCurentTab refreshContentJob;

	private Map<String, Image> titleIamgeCache = new HashMap<String, Image>(5);

	private Font font;

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		urls = WebSearchPreferencStore.getIns().getUseredConfig();
		WebSearchPreferencStore.getIns().addProperChangeListener(urlChangelistener);
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		container.setLayout(gridLayout);

		Composite seachArea = new Composite(container, SWT.NONE);
		createSearchArea(seachArea);

		Composite separatorCmp = new Composite(container, SWT.NONE);
		createSeparatorArea(separatorCmp);

		Composite displayArea = new Composite(container, SWT.NONE);
		createBrowserArea(displayArea);
		refreshContentJob = new ResfreshCurentTab();
		refreshContentJob.start();

	}

	private Composite createSearchArea(Composite parent) {
		GridLayout gridLayout = new GridLayout(3, false);
		parent.setLayout(gridLayout);
		GridData gd_seachArea = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		parent.setLayoutData(gd_seachArea);

		keyWordForSearch = new Text(parent, SWT.SEARCH);
		GridData gd_keyWordForSearch = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_keyWordForSearch.heightHint = 20;
		keyWordForSearch.setLayoutData(gd_keyWordForSearch);
		keyWordForSearch.setText("");
		font = keyWordForSearch.getFont();

		FontData fontData = font.getFontData()[0];
		fontData.setStyle(fontData.getStyle());
		fontData.setHeight(12);
		font = new Font(Display.getDefault(), fontData);
		keyWordForSearch.setFont(font);

		keyWordForSearch.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					refreshKeyWordSearch(true);
				}
			}
		});
		Button searchBtn = new Button(parent, SWT.NONE);
		searchBtn.setText(Messages.getString("Websearch.browserViewPart.searchBtnLbl"));
		searchBtn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		searchBtn.addSelectionListener(new SelectionAdapter() {
			/**
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshKeyWordSearch(true);
			}
		});
		
		Button settingBtn = new Button(parent, SWT.NONE);
		settingBtn.setText(Messages.getString("Websearch.browserViewPart.settingBtnLbl"));
		settingBtn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		settingBtn.addSelectionListener(new SelectionAdapter() {
			/**
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferenceUtil.openPreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), WebSearchPreferencePage.ID);
			}
		});
		
		return parent;
	}

	private Composite createSeparatorArea(Composite parent) {
		GridLayout gridLayout = new GridLayout(1, false);
		parent.setLayout(gridLayout);
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return parent;
	}

	private Composite createBrowserArea(Composite parent) {
		GridLayout gridLayout = new GridLayout(1, false);
		parent.setLayout(gridLayout);
		GridData gd_displayArea = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		parent.setLayoutData(gd_displayArea);
		tabFolder = new CTabFolder(parent, SWT.TOP|SWT.MULTI|SWT.FLAT);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		UIJob  job = new UIJob(Display.getDefault(),"refresh browser") {			
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				refreshTabContent();
				return Status.OK_STATUS;
			}
			/** (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
			 */
			@Override
			public boolean shouldRun() {			
				return !tabFolder.isDisposed();
			}
		};
		job.schedule();
		return parent;

	}

	public BrowserTab getCurrentTab() {
		int selectionIndex = tabFolder.getSelectionIndex();
		return browserTabs[selectionIndex];

	}

	public void refreshTabContent() {
		if (browserTabs != null && browserTabs.length != 0) {
			for (BrowserTab tab : browserTabs) {
				tab.close();
			}
		}
		browserTabs = new BrowserTab[urls.size()];
		for (int i = 0; i < urls.size(); i++) {
			SearchEntry searchEntry = urls.get(i);
			browserTabs[i] = new BrowserTab(searchEntry);
			CTabItem item = new CTabItem(tabFolder, SWT.NONE);
			browserTabs[i].setItem(item);
			item.setText(searchEntry.getSearchName().replaceAll("&", "&&"));
			item.setControl(browserTabs[i].createTabFolderPage(tabFolder));
			item.setData(browserTabs[i]);
			Image image = getImage(searchEntry.getSearchUrl());
			if (null != image) {
				item.setImage(image);
			}
			browserTabs[i].searchKeyWord(keyWordForSearch.getText());
		}
		tabFolder.setSelection(0);
		tabFolder.layout();
	}

	public void refreshKeyWordSearch(boolean refreshAll) {
		if (browserTabs == null || browserTabs.length == 0) {
			return;
		}
		if (!refreshAll) {
			getCurrentTab().searchKeyWord(keyWordForSearch.getText());
		} else {

			for (BrowserTab tab : browserTabs) {
				tab.searchKeyWord(keyWordForSearch.getText());
			}
		}
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public void setKeyWord(String word, boolean refreshAll) {
		if (null == this.keyWordForSearch) {
			return;
		}

		this.keyWordForSearch.setText(word);
		refreshKeyWordSearch(refreshAll);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (browserTabs != null && browserTabs.length != 0) {
			for (BrowserTab tab : browserTabs) {					
					tab.close();
			}
		}
		disposeCacheImage();
		if (null != font) {
			font.dispose();
		}
		WebSearchPreferencStore.getIns().removeProperChangeListener(urlChangelistener);
		if (null != refreshContentJob) {
			refreshContentJob.setStop(true);
		}
		super.dispose();

	}

	public void disposeCacheImage() {
		Collection<Image> values = titleIamgeCache.values();
		for (Image image : values) {
			if (null != image) {
				image.dispose();
			}
		}
	}

	private class ProperChangeListener implements PropertyChangeListener {

		/**
		 * (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("URL".equals(evt.getPropertyName())) {
				BrowserViewPart.this.urls = (List<SearchEntry>) evt.getNewValue();
				refreshTabContent();
			}
		}

	}

	class ResfreshCurentTab extends Thread {

		private boolean isStop;

		public void run() {
			while (true) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {

				}
				if (isStop()) {
					shutDown();
					return;
				}

				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						if (isStop()) {
							return;
						}					
						 IWorkbench workbench = PlatformUI.getWorkbench();
						 IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
						 if(null == activeWorkbenchWindow){
							 return;
						 }
						 IViewPart findView=activeWorkbenchWindow.getActivePage()
								.findView(ID);
						if (null == findView) {
							return;
						}
						if (!activeWorkbenchWindow.getActivePage()
								.isPartVisible(findView)) {
							return;
						}
						StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getFocusCellEditor();
						if (null == cellEditor) {
							return;
						}
						if (cellEditor.getMouseState()) {
							return;
						}

						final String selectedPureText = cellEditor.getSelectedPureText();
						if (null == selectedPureText || selectedPureText.trim().isEmpty()) {
							return;
						}
						if (keyWordForSearch.getText().equals(selectedPureText)) {
							return;
						}
						keyWordForSearch.setText(selectedPureText);
						refreshKeyWordSearch(false);
					}
				});

			}
		}

		/** @return the isStop */
		public boolean isStop() {
			return isStop;
		}

		/**
		 * @param isStop
		 *            the isStop to set
		 */
		public void setStop(boolean isStop) {
			this.isStop = isStop;
		}

		public void shutDown() {
			this.interrupt();
		}
	}

	private String getImageUrl(String url) {

		String imageName = "favicon.ico";
		String HTTP = "http://";
		url = url.toLowerCase(Locale.ENGLISH);
		String imageUrl = "";
		if (url.startsWith(HTTP)) {
			url = url.substring(HTTP.length());
		}
		int separator = url.indexOf("/");
		if (-1 != separator) {
			url = url.substring(0, separator);
		}
		url = url+"/";
		imageUrl = HTTP + url + imageName;
		return imageUrl;
	}

	public Image getTitleImage(String iamgeUrl) {
		Image cacheImage = titleIamgeCache.get(iamgeUrl);
		if (null != cacheImage) {
			return cacheImage;
		}
		URL url = null;
		try {
			url = new URL(iamgeUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (null == url) {
			return null;
		}
		Image image = null;
		try {
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
			ImageData imageData = imageDescriptor.getImageData();
			ImageData scaledTo = imageData.scaledTo(16, 16);
			image = ImageDescriptor.createFromImageData(scaledTo).createImage();
		} catch (Exception e) {
			// no need handle
			return null;
		}
		if (null != image) {
			titleIamgeCache.put(iamgeUrl, image);
		}
		return image;
	}

	public Image getImage(String WebConfigUrl) {
		return getTitleImage(getImageUrl(WebConfigUrl));
	}
}
