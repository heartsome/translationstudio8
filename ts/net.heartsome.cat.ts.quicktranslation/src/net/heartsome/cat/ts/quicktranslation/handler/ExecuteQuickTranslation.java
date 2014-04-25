/**
 * ExecuteQuickTranslation.java
 *
 * Version information :
 *
 * Date:2012-6-20
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.quicktranslation.handler;

import net.heartsome.cat.ts.quicktranslation.QuickTranslationImpl;
import net.heartsome.cat.ts.tm.complexMatch.IComplexMatch;
import net.heartsome.cat.ts.ui.bean.TranslateParameter;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.translation.view.MatchViewPart;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 针对选中的文本段执行快速翻译算法
 * @author  jason
 * @version 
 * @since   JDK1.6
 */
public class ExecuteQuickTranslation extends AbstractHandler {

	/** (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(TranslateParameter.getInstance().isAutoQuickTrans()){
			return null;
		}
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof IXliffEditor)) {
			return null;
		}
		final IXliffEditor xliffEditor = (IXliffEditor) editor;

		final int[] selectedRowIndexs = xliffEditor.getSelectedRows();
		if(selectedRowIndexs.length == 0){
			return null;
		}
//		TransUnitBean transUnitBean = xliffEditor.getRowTransUnitBean(selectedRowIndexs[selectedRowIndexs.length - 1]);
		IComplexMatch matcher = new QuickTranslationImpl();
//		FileEditorInput input = (FileEditorInput) editor.getEditorInput();
//		IProject project = input.getFile().getProject();
//		List<AltTransBean> newAltTrans = matcher.executeTranslation(transUnitBean, project);
//		if(newAltTrans.size() == 0){
//			return null;
//		}
		
		IViewPart viewPart = window.getActivePage().findView(MatchViewPart.ID);						
		if(viewPart != null && viewPart instanceof MatchViewPart){
			MatchViewPart matchView = (MatchViewPart) viewPart;
			matchView.manualExecComplexTranslation(selectedRowIndexs[0],xliffEditor, matcher);
//			matchView.replaceMatchs(newAltTrans);
//			matchView.refreshView(xliffEditor, selectedRowIndexs[selectedRowIndexs.length - 1]);
		}
		
//		IRunnableWithProgress runnable = new IRunnableWithProgress() {
//			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//				monitor.beginTask(Messages.getString("handler.ExecuteQuickTranslation.task1"), selectedRowIndexs.length);
//				IComplexMatch matcher = new QuickTranslationImpl();
//				
//				FileEditorInput input = (FileEditorInput) editor.getEditorInput();
//				IProject project = input.getFile().getProject();
//				
//				List<String> oldToolIds = new ArrayList<String>();
//				oldToolIds.add(matcher.getToolId());
//				Map<Integer, List<AltTransBean>> newAltTransMap = new HashMap<Integer, List<AltTransBean>>();
//				Map<Integer, List<String>> oldAltTransToolIdMap = new HashMap<Integer, List<String>>();				
//				for(int selectedRowindex : selectedRowIndexs){
//					TransUnitBean transUnitBean = xliffEditor.getRowTransUnitBean(selectedRowindex);					
//					List<AltTransBean> newAltTrans = matcher.executeTranslation(transUnitBean, project);
//					if(newAltTrans.size() == 0){
//						continue;
//					}
//					newAltTransMap.put(selectedRowindex, newAltTrans);
//					oldAltTransToolIdMap.put(selectedRowindex, oldToolIds);
//					monitor.worked(1);
//				}
//				if(newAltTransMap.size() != 0){					
//					xliffEditor.getXLFHandler().batchUpdateAltTrans(selectedRowIndexs, newAltTransMap, oldAltTransToolIdMap);
//				
//					window.getShell().getDisplay().asyncExec(new Runnable() {					
//						@Override
//						public void run() {
//							IViewPart viewPart = window.getActivePage().findView(MatchViewPart.ID);						
//							if(viewPart != null && viewPart instanceof MatchViewPart && selectedRowIndexs.length != 0){
//								MatchViewPart matchView = (MatchViewPart) viewPart;
//								matchView.refreshView(xliffEditor, selectedRowIndexs[selectedRowIndexs.length - 1]);
//							}						
//						}
//					});
//				}
//				monitor.done();
//			}
//		};

//		try {
//			new ProgressMonitorDialog(window.getShell()).run(true, false, runnable);
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		return null;
	}

}
