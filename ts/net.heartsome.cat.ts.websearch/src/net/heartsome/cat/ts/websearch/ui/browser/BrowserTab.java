/**
 * BrowserTab.java
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
package net.heartsome.cat.ts.websearch.ui.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.heartsome.cat.ts.websearch.bean.SearchEntry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yule_chen
 * @version
 * @since JDK1.6
 */
public class BrowserTab {
	public final static Logger logger = LoggerFactory.getLogger(BrowserTab.class);

	private Browser browser;

	private SearchEntry searchEntry;

	private Composite tabFolderPage;

	private CTabItem item;

	private OpenWindowListener openLisenter;


	/** @return the searchEntry */
	public SearchEntry getSearchEntry() {
		return searchEntry;
	}

	/** @return the item */
	public CTabItem getItem() {
		return item;
	}

	/**
	 * @param item
	 *            the item to set
	 */
	public void setItem(CTabItem item) {
		this.item = item;
	}

	/**
	 * @param searchEntry
	 *            the searchEntry to set
	 */
	public void setSearchEntry(SearchEntry searchEntry) {
		this.searchEntry = searchEntry;
	}

	public BrowserTab(SearchEntry searchEntry) {
		this.searchEntry = searchEntry;
	}

	public Composite createTabFolderPage(CTabFolder tabFolder) {
		tabFolderPage = new Composite(tabFolder, SWT.NONE);
		tabFolderPage.setLayout(new FillLayout());
		browser = new Browser(tabFolderPage, SWT.NONE);
		hookOpenListner();
		return tabFolderPage;
	}

	public void hookOpenListner() {
		openLisenter = new OpenWindowListener() {
			@Override
			public void open(WindowEvent event) {
				event.required=true;
				BrowserComponent app = new BrowserComponent(false);
				event.browser = app.getBrowser();
			}
		};
		browser.addOpenWindowListener(openLisenter);

	}

	/**
	 * 清理资源 ;
	 */
	public void close() {
		if (null != browser) {
			if(!browser.isDisposed()){
				if(null != openLisenter){					
					browser.removeOpenWindowListener(openLisenter);
				}
			}
			browser.dispose();
		}
		if (tabFolderPage != null) {
			tabFolderPage.dispose();
		}
	
		if (null != item) {
			item.dispose();
		}
	}

	public void searchKeyWord(String keyWord) {
		String searchUrl = searchEntry.getSearchUrl();
		searchUrl = searchUrl.replaceAll(SearchEntry.KEY_WORD, encodeUrl(keyWord));
		browser.setUrl(searchUrl);
	}

	public String encodeUrl(String keyWord) {
		if (null == keyWord) {
			return "";
		}
		try {
			return URLEncoder.encode(keyWord, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("", e);

		}
		return "";
	}

	public Browser getBrowser() {
		return browser;
	}
}
