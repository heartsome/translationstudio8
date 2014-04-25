/**
 * WebSearchHandler.java
 *
 * Version information :
 *
 * Date:2013-9-18
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.websearch.handler;

import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.websearch.ui.view.BrowserViewPart;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class WebSearchHandler extends AbstractHandler {
	
	public final static Logger logger = LoggerFactory.getLogger(WebSearchHandler.class);

	/** (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		String selectPureText ="";
		if (editor instanceof IXliffEditor) {		
			IXliffEditor xliffEditor = (IXliffEditor) editor;
			selectPureText= xliffEditor.getSelectPureText();
		}
		
		try {
			IViewPart showView = getActivePage().showView(BrowserViewPart.ID);
			if(showView instanceof BrowserViewPart){
				BrowserViewPart browserViewPart = (BrowserViewPart) showView;
				browserViewPart.setKeyWord(selectPureText,true);				
			}
		} catch (PartInitException e) {
			e.printStackTrace();
			logger.error("", e);
		}
// 暂时去掉VIEW弹出显示		
//		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		if (activePage instanceof WorkbenchPage) {
//			WorkbenchPage workbenchPage = (WorkbenchPage) activePage;
//			IViewReference findViewReference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//					.findViewReference(BrowserViewPart.ID);			
//			workbenchPage.detachView(findViewReference);
//		}
	
		return null;

}
	
	public static  IWorkbenchPage getActivePage(){
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}	

}